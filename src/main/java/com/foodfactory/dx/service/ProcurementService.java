package com.foodfactory.dx.service;

import com.foodfactory.dx.domain.MaterialArrival;
import com.foodfactory.dx.domain.MaterialArrivalLine;
import com.foodfactory.dx.domain.MaterialLot;
import com.foodfactory.dx.domain.MaterialOrder;
import com.foodfactory.dx.mapper.MaterialArrivalLineMapper;
import com.foodfactory.dx.mapper.MaterialArrivalMapper;
import com.foodfactory.dx.mapper.MaterialLotMapper;
import com.foodfactory.dx.mapper.MaterialOrderMapper;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 「入荷明細を検品結果込みで登録する」という一連の業務処理をまとめて行うService。
 *
 * フェーズ0の設計で確認した通り、この登録処理は複数のテーブルにまたがる。
 *   1. material_arrival_line に検品結果込みの明細を1件登録する
 *      (この時点で supplierLotNo = 仕入先が発行したロット番号は、
 *       呼び出し側が既に手入力した値としてlineに設定されている)
 *   2. 検品合格数量(acceptedQty)が1以上あれば、material_lot を1件自動生成する
 *      (ここで新しくロット番号を採番しているわけではない。
 *       あくまで①で入力済みの仕入先ロット番号を引き継いで、
 *       「在庫として追跡するための内部レコード」を自動的に作るだけ)
 *   3. その入荷が発注(material_order)に紐づく場合、発注の充足状況を再集計し、
 *      status(未入荷/一部入荷/入荷完了)を更新する
 *
 * これらは「全部まとめて成功する」か「全部まとめて失敗する(=何も変更されない)」かの
 * どちらかであるべきで、例えば「明細の登録は成功したのに、ロットの生成だけ失敗して
 * 中途半端にデータが残る」という状態は絶対に避けたい。
 * このような「複数の処理をひとまとまりとして扱う」ことを実現するのが
 * @Transactional アノテーションの役割になる。
 */
@Service
public class ProcurementService {

    private final MaterialArrivalLineMapper materialArrivalLineMapper;
    private final MaterialArrivalMapper materialArrivalMapper;
    private final MaterialOrderMapper materialOrderMapper;
    private final MaterialLotMapper materialLotMapper;

    public ProcurementService(MaterialArrivalLineMapper materialArrivalLineMapper,
                               MaterialArrivalMapper materialArrivalMapper,
                               MaterialOrderMapper materialOrderMapper,
                               MaterialLotMapper materialLotMapper) {
        this.materialArrivalLineMapper = materialArrivalLineMapper;
        this.materialArrivalMapper = materialArrivalMapper;
        this.materialOrderMapper = materialOrderMapper;
        this.materialLotMapper = materialLotMapper;
    }

    /**
     * 入荷明細を検品結果込みで登録し、材料ロットの生成・発注ステータスの更新までを一括で行う。
     *
     * @Transactional: このメソッドの中で行われるDBへの変更(insert/update)は、
     *   メソッドが正常に終了した時点で初めてまとめて確定(コミット)される。
     *   途中で例外(エラー)が発生した場合は、それまでに行った変更も含めて
     *   全て取り消される(ロールバックされる)。
     *   これにより、「明細は登録されたのにロットは作られなかった」というような
     *   中途半端な状態がDBに残ることを防げる。
     *
     * 引数の line には、呼び出し側があらかじめ以下を設定しておく想定:
     *   arrivalId, supplierLotNo, origin, expiryDate,
     *   packageCount, packageWeightSnapshot,
     *   acceptedQty, heldQty,
     *   checkDamage, checkExpiry, checkContamination
     * (lineId と arrivedQty はこのメソッドの中で決定するので、呼び出し側は設定不要)
     */
    @Transactional
    public MaterialArrivalLine registerInspectedLine(MaterialArrivalLine line) {

        // --- 手順1: 入荷総量を計算し、入力値の整合性をチェックする ---
        //
        // arrivedQty(総量)は「箱数 × 1箱あたりの目安重量」で機械的に決まる値であり、
        // 人がここを直接入力すると入力ミスの余地が生まれるため、
        // 呼び出し側からは受け取らず、このメソッドの中で計算して確定させる。
        BigDecimal arrivedQty = line.getPackageWeightSnapshot()
                .multiply(BigDecimal.valueOf(line.getPackageCount()));
        line.setArrivedQty(arrivedQty);

        // 合格数量+保留数量が、計算した総量と一致しているかを確認する。
        // 一致しない場合、検品結果の入力ミス(数え間違い等)の可能性が高いため、
        // ここで処理を止めて呼び出し側にエラーを返す。
        BigDecimal acceptedPlusHeld = line.getAcceptedQty().add(line.getHeldQty());
        if (acceptedPlusHeld.compareTo(arrivedQty) != 0) {
            throw new IllegalArgumentException(
                    "検品結果の数量が入荷総量と一致しません。"
                            + " 入荷総量=" + arrivedQty
                            + ", 合格+保留=" + acceptedPlusHeld);
        }

        // --- 手順2: 入荷明細を登録する ---
        //
        // insertを実行すると、XML側のuseGeneratedKeysの設定により、
        // line.lineId に自動採番されたIDが詰め直される。
        materialArrivalLineMapper.insert(line);

        // --- 手順3: 合格数量が1件でもあれば、材料ロットを自動生成する ---
        //
        // compareTo(BigDecimal.ZERO) > 0 は「0より大きいか」の判定。
        // BigDecimalは == で比較すると正しく動作しないため(参照の比較になってしまうため)、
        // 数値としての大小比較には必ず compareTo を使う。
        if (line.getAcceptedQty().compareTo(BigDecimal.ZERO) > 0) {
            // このロットがどの材料のものかを知るために、入荷ヘッダーを取得する。
            MaterialArrival arrival = materialArrivalMapper.findById(line.getArrivalId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "指定された入荷ヘッダーが見つかりません: arrivalId=" + line.getArrivalId()));

            MaterialLot lot = new MaterialLot(
                    arrival.getMaterialId(),
                    line.getLineId(),
                    line.getSupplierLotNo(), // 仕入先発行のロット番号をそのままコピーする(新規採番ではない)
                    line.getOrigin(),
                    line.getExpiryDate(),
                    line.getAcceptedQty() // 生成直後のロットの残量は、合格数量そのもの
            );
            materialLotMapper.insert(lot);

            // --- 手順4: この入荷が発注に紐づく場合、発注の充足状況を再集計する ---
            if (arrival.getOrderId() != null) {
                recalculateOrderStatus(arrival.getOrderId());
            }
        }

        return line;
    }

    /**
     * 指定した発注について、これまでに検品合格した数量の合計を、
     * 発注数量と比較し、状態(未入荷/一部入荷/入荷完了)を判定してDBに反映する。
     *
     * この処理を「入荷明細を登録するたびに毎回再計算する」形にしているのがポイント。
     * 合計を都度足し算・引き算で更新していく方式(差分更新)も可能だが、
     * 更新のたびに毎回SQLで合計を取り直す(再集計)方式の方が、
     * 「今現在DBに保存されている実際のデータと、必ず一致した結果になる」という安心感がある。
     * (差分更新は、途中でどこか1回でも更新を取りこぼすと、以降ずっとズレたままになる弱点がある)
     */
    private void recalculateOrderStatus(Long orderId) {
        MaterialOrder order = materialOrderMapper.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "指定された発注が見つかりません: orderId=" + orderId));

        BigDecimal totalAccepted = materialArrivalLineMapper.sumAcceptedQtyByOrderId(orderId);

        MaterialOrder.Status newStatus;
        if (totalAccepted.compareTo(BigDecimal.ZERO) <= 0) {
            newStatus = MaterialOrder.Status.NOT_ARRIVED;
        } else if (totalAccepted.compareTo(order.getOrderQty()) >= 0) {
            newStatus = MaterialOrder.Status.FULLY_ARRIVED;
        } else {
            newStatus = MaterialOrder.Status.PARTIALLY_ARRIVED;
        }

        materialOrderMapper.updateStatus(orderId, newStatus);
    }
}
