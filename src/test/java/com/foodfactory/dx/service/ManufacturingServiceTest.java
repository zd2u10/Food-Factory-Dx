//package com.foodfactory.dx.service;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyBoolean;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//import com.foodfactory.dx.domain.ManufacturingBatch;
//import com.foodfactory.dx.mapper.BatchMaterialUsageMapper;
//import com.foodfactory.dx.mapper.ItemMapper;
//import com.foodfactory.dx.mapper.ManufacturingBatchMapper;
//import com.foodfactory.dx.mapper.MaterialLotMapper;
//import com.foodfactory.dx.mapper.MaterialMapper;
//import com.foodfactory.dx.mapper.RecipeItemMapper;
//import java.math.BigDecimal;
//import java.util.Optional;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
///**
// * ManufacturingService.completeBatch() の単体テスト。
// *
// * 【このテストの特徴】
// * DB(MySQL)には一切接続しない。Mapperは全て「本物の代わりの偽物」(モック)に差し替え、
// * 「Mapperがこう返してきたら、Serviceはこう動くはず」というロジックだけを検証する。
// * そのため draft作成→confirm-plan→execute→complete という一連の手順をThunder Clientで
// * 手動でやり直さなくても、Eclipse上で実行するだけで一瞬(1秒未満)で結果が分かる。
// *
// * @ExtendWith(MockitoExtension.class):
// *   このクラスの中で @Mock を使えるようにするための設定。JUnit5とMockitoを連携させる目印。
// */
//@ExtendWith(MockitoExtension.class)
//class ManufacturingServiceTest {
//
//    // @Mock: 本物のMapperの代わりに使う「偽物」。
//    // 実際にDBへ問い合わせるのではなく、テストコード側で「こう聞かれたらこう答えて」と
//    // あらかじめ設定した通りの値を返すだけの、テスト専用のダミーオブジェクト。
//    @Mock
//    private ManufacturingBatchMapper manufacturingBatchMapper;
//    @Mock
//    private BatchMaterialUsageMapper batchMaterialUsageMapper;
//    @Mock
//    private ItemMapper itemMapper;
//    @Mock
//    private RecipeItemMapper recipeItemMapper;
//    @Mock
//    private MaterialMapper materialMapper;
//    @Mock
//    private MaterialLotMapper materialLotMapper;
//
//    // テスト対象の本体。上記の偽物Mapperをコンストラクタに渡して組み立てる。
//    private ManufacturingService manufacturingService;
//
//    private static final Long BATCH_ID = 1L;
//
//    /**
//     * @BeforeEach: 各テストメソッドを実行する「直前」に、毎回自動的に呼ばれる初期化処理。
//     * テストごとに新しいServiceインスタンスを作ることで、
//     * あるテストの中での変更が別のテストに影響しないようにしている。
//     */
//    @BeforeEach
//    void setUp() {
//        manufacturingService = new ManufacturingService(
//                manufacturingBatchMapper, batchMaterialUsageMapper, itemMapper,
//                recipeItemMapper, materialMapper, materialLotMapper);
//    }
//
//    /** 計画数量(198)ちょうど、またはそれ以下の場合は exceededPlan が false になることを確認する。 */
//    @Test
//    void completeBatch_計画数量以内なら_exceededPlanはfalse() {
//        // --- 準備(Arrange): モックの「振る舞い」を設定する ---
//        // 「findByIdが呼ばれたら、plannedQty=198のMANUFACTURING状態バッチを返してください」と指示する。
//        ManufacturingBatch batch = new ManufacturingBatch();
//        batch.setBatchId(BATCH_ID);
//        batch.setStatus(ManufacturingBatch.Status.MANUFACTURING);
//        batch.setPlannedQty(new BigDecimal("198"));
//        when(manufacturingBatchMapper.findById(BATCH_ID)).thenReturn(Optional.of(batch));
//
//        // --- 実行(Act): テスト対象のメソッドを呼び出す ---
//        // acceptedQty=195, lossQty=3 → producedQty=198(計画ちょうど)
//        manufacturingService.completeBatch(BATCH_ID, new BigDecimal("195"), new BigDecimal("3"), "軽微な不良3個");
//
//        // --- 検証(Assert): 期待通りの引数でMapperが呼ばれたか確認する ---
//        // ArgumentCaptor: モックが実際に呼ばれた際に「何を渡されたか」を後から取り出して検証する仕組み。
//        ArgumentCaptor<Boolean> exceededPlanCaptor = ArgumentCaptor.forClass(Boolean.class);
//        verify(manufacturingBatchMapper).completeBatch(
//                eq(BATCH_ID), any(), any(), any(), any(), exceededPlanCaptor.capture());
//
//        assertFalse(exceededPlanCaptor.getValue(), "計画数量ちょうどなので、超過扱いにはならないはず");
//    }
//
//    /** 計画数量(198)を超えた場合は exceededPlan が true になることを確認する。 */
//    @Test
//    void completeBatch_計画数量を超えたら_exceededPlanはtrue() {
//        ManufacturingBatch batch = new ManufacturingBatch();
//        batch.setBatchId(BATCH_ID);
//        batch.setStatus(ManufacturingBatch.Status.MANUFACTURING);
//        batch.setPlannedQty(new BigDecimal("198"));
//        when(manufacturingBatchMapper.findById(BATCH_ID)).thenReturn(Optional.of(batch));
//
//        // acceptedQty=205, lossQty=0 → producedQty=205(計画198を超過)
//        manufacturingService.completeBatch(BATCH_ID, new BigDecimal("205"), BigDecimal.ZERO, null);
//
//        ArgumentCaptor<Boolean> exceededPlanCaptor = ArgumentCaptor.forClass(Boolean.class);
//        verify(manufacturingBatchMapper).completeBatch(
//                eq(BATCH_ID), any(), any(), any(), any(), exceededPlanCaptor.capture());
//
//        assertTrue(exceededPlanCaptor.getValue(), "計画数量を超えているので、超過扱いになるはず");
//    }
//
//    /** producedQty(合格+不良)が正しく足し算されているかも、ついでに確認しておく。 */
//    @Test
//    void completeBatch_producedQtyはacceptedQtyとlossQtyの合計になる() {
//        ManufacturingBatch batch = new ManufacturingBatch();
//        batch.setBatchId(BATCH_ID);
//        batch.setStatus(ManufacturingBatch.Status.MANUFACTURING);
//        batch.setPlannedQty(new BigDecimal("198"));
//        when(manufacturingBatchMapper.findById(BATCH_ID)).thenReturn(Optional.of(batch));
//
//        manufacturingService.completeBatch(BATCH_ID, new BigDecimal("195"), new BigDecimal("3"), "コメント");
//
//        // completeBatchメソッドの2番目の引数(producedQty)をキャプチャして検証する。
//        ArgumentCaptor<BigDecimal> producedQtyCaptor = ArgumentCaptor.forClass(BigDecimal.class);
//        verify(manufacturingBatchMapper).completeBatch(
//                eq(BATCH_ID), producedQtyCaptor.capture(), any(), any(), any(), anyBoolean());
//
//        // BigDecimalの比較は compareTo を使う(==や.equals()は精度違いで一致しないことがあるため)。
//        assertEquals(0, producedQtyCaptor.getValue().compareTo(new BigDecimal("198")),
//                "195 + 3 = 198 になっているはず");
//    }
//
//    /** MANUFACTURING状態でないバッチを完了しようとするとエラーになることを確認する。 */
//    @Test
//    void completeBatch_MANUFACTURING状態でなければ例外が飛ぶ() {
//        ManufacturingBatch batch = new ManufacturingBatch();
//        batch.setBatchId(BATCH_ID);
//        batch.setStatus(ManufacturingBatch.Status.PLAN); // まだ実行前(MANUFACTURINGではない)
//        batch.setPlannedQty(new BigDecimal("198"));
//        when(manufacturingBatchMapper.findById(BATCH_ID)).thenReturn(Optional.of(batch));
//
//        // assertThrows: 「このコードを実行したら、指定した種類の例外が投げられるはず」を検証するメソッド。
//        org.junit.jupiter.api.Assertions.assertThrows(
//                IllegalStateException.class,
//                () -> manufacturingService.completeBatch(
//                        BATCH_ID, new BigDecimal("195"), new BigDecimal("3"), null)
//        );
//    }
//}
