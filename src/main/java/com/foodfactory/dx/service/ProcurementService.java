package com.foodfactory.dx.service;

import com.foodfactory.dx.domain.HoldResolution;
import com.foodfactory.dx.domain.MaterialArrivalLine;
import com.foodfactory.dx.domain.MaterialLot;
import com.foodfactory.dx.domain.MaterialOrder;
import com.foodfactory.dx.mapper.HoldResolutionMapper;
import com.foodfactory.dx.mapper.MaterialArrivalLineMapper;
import com.foodfactory.dx.mapper.MaterialArrivalMapper;
import com.foodfactory.dx.mapper.MaterialLotMapper;
import com.foodfactory.dx.mapper.MaterialOrderMapper;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 「入荷明細を検品結果込みで登録する」という一連の業務処理をまとめて行うService。
 *
 * この登録処理は複数のテーブルにまたがる。
 *   1. material_arrival_line に検品結果込みの明細を1件登録する
 *      (materialId・orderIdは、1回の配送内で複数の材料・複数の発注が混在してもよいよう
 *       明細側に持たせている。ヘッダー(material_arrival)は配送イベントの情報だけを持つ)
 *   2. 検品合格数量(acceptedQty)が1以上あれば、material_lot を1件自動生成する
 *   3. 検品保留数量(heldQty)が1以上あれば、hold_resolution を1件自動生成する(ON_HOLD)
 *   4. その明細が発注(material_order)に紐づく場合、発注の充足状況を再集計し、
 *      status(未入荷/一部入荷/入荷完了)を更新する
 *
 * これらは「全部まとめて成功する」か「全部まとめて失敗する(=何も変更されない)」かの
 * どちらかであるべきなので、@Transactional でひとまとまりの処理として扱う。
 */
@Service
public class ProcurementService {

    private final MaterialArrivalLineMapper materialArrivalLineMapper;
    private final MaterialArrivalMapper materialArrivalMapper;
    private final MaterialOrderMapper materialOrderMapper;
    private final MaterialLotMapper materialLotMapper;
    private final HoldResolutionMapper holdResolutionMapper;

    public ProcurementService(MaterialArrivalLineMapper materialArrivalLineMapper,
                               MaterialArrivalMapper materialArrivalMapper,
                               MaterialOrderMapper materialOrderMapper,
                               MaterialLotMapper materialLotMapper,
                               HoldResolutionMapper holdResolutionMapper) {
        this.materialArrivalLineMapper = materialArrivalLineMapper;
        this.materialArrivalMapper = materialArrivalMapper;
        this.materialOrderMapper = materialOrderMapper;
        this.materialLotMapper = materialLotMapper;
        this.holdResolutionMapper = holdResolutionMapper;
    }

    /**
     * 入荷明細を検品結果込みで登録する(通常の新規入荷登録)。
     * 保留対応(交換品)としての登録は registerInspectedLine(line, resolvesHoldId) を使う。
     */
    @Transactional
    public MaterialArrivalLine registerInspectedLine(MaterialArrivalLine line) {
        return registerInspectedLine(line, null);
    }

    /**
     * 入荷明細を検品結果込みで登録する。
     *
     * @param resolvesHoldId
     *   この明細が、既存の保留(hold_resolution)に対する交換品である場合に、
     *   その保留のIDを指定する。人が「新規」か「保留対応」かを選ぶという、
     *   フェーズ3で決めたUIの考え方をそのままAPIのパラメータとして表現している。
     *   新規入荷の場合はnullでよい。
     *
     * 引数の line には、呼び出し側があらかじめ以下を設定しておく想定:
     *   arrivalId, materialId, orderId(任意), supplierLotNo, origin, expiryDate,
     *   packageCount, packageWeightSnapshot,
     *   acceptedQty, heldQty,
     *   checkDamage, checkExpiry, checkContamination
     * (lineId と arrivedQty はこのメソッドの中で決定するので、呼び出し側は設定不要)
     */
    @Transactional
    public MaterialArrivalLine registerInspectedLine(MaterialArrivalLine line, Long resolvesHoldId) {

        // --- 手順0: 入荷ヘッダーが実在するか確認する ---
        materialArrivalMapper.findById(line.getArrivalId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "指定された入荷ヘッダーが見つかりません: arrivalId=" + line.getArrivalId()));

        // --- 手順0.5: 発注に紐づく明細の場合、発注の材料と矛盾していないか確認する ---
        if (line.getOrderId() != null) {
            MaterialOrder order = materialOrderMapper.findById(line.getOrderId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "指定された発注が見つかりません: orderId=" + line.getOrderId()));
            if (!order.getMaterialId().equals(line.getMaterialId())) {
                throw new IllegalArgumentException(
                        "指定された発注(orderId=" + line.getOrderId() + ")の材料と、"
                                + "この明細のmaterialId(" + line.getMaterialId() + ")が一致しません。");
            }
        }

        // --- 手順0.8: 交換品として登録する場合、対象の保留が本当にON_HOLD状態か確認する ---
        HoldResolution targetHold = null;
        if (resolvesHoldId != null) {
            targetHold = holdResolutionMapper.findById(resolvesHoldId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "指定された保留が見つかりません: holdId=" + resolvesHoldId));
            if (targetHold.getStatus() != HoldResolution.Status.ON_HOLD) {
                throw new IllegalStateException(
                        "この保留は既に対応済みです。holdId=" + resolvesHoldId);
            }
            // 交換品の明細から、元の保留明細を辿れるようにしておく(トレーサビリティのため)。
            line.setExchangeSourceLineId(targetHold.getLineId());
        }

        // --- 手順1: 入荷総量を計算し、入力値の整合性をチェックする ---
        BigDecimal arrivedQty = line.getPackageWeightSnapshot()
                .multiply(BigDecimal.valueOf(line.getPackageCount()));
        line.setArrivedQty(arrivedQty);

        BigDecimal acceptedPlusHeld = line.getAcceptedQty().add(line.getHeldQty());
        if (acceptedPlusHeld.compareTo(arrivedQty) != 0) {
            throw new IllegalArgumentException(
                    "検品結果の数量が入荷総量と一致しません。"
                            + " 入荷総量=" + arrivedQty
                            + ", 合格+保留=" + acceptedPlusHeld);
        }

        // --- 手順2: 入荷明細を登録する ---
        materialArrivalLineMapper.insert(line);

        // --- 手順3: 合格数量が1件でもあれば、材料ロットを自動生成する ---
        if (line.getAcceptedQty().compareTo(BigDecimal.ZERO) > 0) {
            MaterialLot lot = new MaterialLot(
                    line.getMaterialId(),
                    line.getLineId(),
                    line.getSupplierLotNo(),
                    line.getOrigin(),
                    line.getExpiryDate(),
                    line.getAcceptedQty()
            );
            materialLotMapper.insert(lot);

            if (line.getOrderId() != null) {
                recalculateOrderStatus(line.getOrderId());
            }
        }

        // --- 手順4: 保留数量が1件でもあれば、保留対応記録を自動生成する(ON_HOLD) ---
        if (line.getHeldQty().compareTo(BigDecimal.ZERO) > 0) {
            HoldResolution hold = new HoldResolution(line.getLineId(), line.getHeldQty());
            holdResolutionMapper.insert(hold);
        }

        // --- 手順5: 交換品としての登録であれば、元の保留をRESOLVED・EXCHANGEDにする ---
        if (targetHold != null) {
            holdResolutionMapper.resolve(
                    targetHold.getHoldId(),
                    HoldResolution.ResolutionType.EXCHANGED,
                    line.getLineId(),
                    "交換品(line_id=" + line.getLineId() + ")を受け入れて対応");
        }

        return line;
    }

    /**
     * 指定した発注について、これまでに検品合格した数量の合計を、
     * 発注数量と比較し、状態(未入荷/一部入荷/入荷完了)を判定してDBに反映する。
     * HoldResolutionService(結局受け入れる対応)からも呼ばれるため public にしている。
     */
    public void recalculateOrderStatus(Long orderId) {
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

    /** 特定の入荷ヘッダー(arrivalId)に属する明細を全件取得する。 */
    public List<MaterialArrivalLine> listByArrivalId(Long arrivalId) {
        return materialArrivalLineMapper.findByArrivalId(arrivalId);
    }

    /** 特定の発注(orderId)に紐づく明細を全件取得する(発注の充足内訳を確認する用途)。 */
    public List<MaterialArrivalLine> listByOrderId(Long orderId) {
        return materialArrivalLineMapper.findByOrderId(orderId);
    }
}
