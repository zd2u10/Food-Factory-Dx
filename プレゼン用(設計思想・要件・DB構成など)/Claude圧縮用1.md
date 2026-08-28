# 食品工場DXシステム(フェーズ0: 基盤マスタ + プロジェクト初期構成)

## DBアクセス方式について

このプロジェクトは **MyBatis** を使ってDBアクセスを行う(JPA/Hibernateは使わない)。
MyBatisは「SQL文を自分で書き、その結果とJavaオブジェクトの対応付けだけをXMLで管理する」方式。
JPAのように「Entityを書けばSQLが自動生成される」方式とは思想が異なるため、
既にJPAで実装していた場合は entity/repository を削除し、domain/mapperに置き換える必要がある
(このZIPは既にMyBatis構成に統一済み)。

## 含まれるもの

```
pom.xml                              Maven依存関係定義(Spring Boot 3.3.4 / Java 21 / MyBatis)
.gitignore                           Java/Eclipse/VSCode向け
sql/phase0_master_schema.sql         MySQL 8.0向けDDL(テーブル作成SQL)
src/main/java/com/foodfactory/dx/
  DxApplication.java                 起動エントリーポイント(@MapperScanでmapperの場所を指定)
  domain/                            DBの1行に対応するJavaオブジェクト(JPAのEntityに相当するもの)
  mapper/                            「このメソッドを呼んだらどのSQLが実行されるか」の宣言(@Mapperインターフェース)
  controller/                        (空、フェーズ1以降でREST APIを追加)
  service/                           (空、フェーズ1以降でビジネスロジックを追加)
  dto/                                (空、フェーズ1以降でAPI入出力用クラスを追加)
  exception/                          (空、フェーズ1以降で業務例外クラスを追加)
src/main/resources/
  application.properties             MySQL接続・MyBatis設定
  mybatis/*.xml                      実際のSQL文を書いたXMLファイル(mapperと同名で対応)
```

対象テーブル(フェーズ0): `material`, `material_package_spec`, `items`, `recipe_item`

controller/service/dto/exceptionは中身が空ですが、`package-info.java`だけ置いてあります。
Eclipse/VSCodeでフォルダ自体は認識されるので、今後の実装時にそのままクラスを追加していけます。

## 前提・仮定した点(要確認)

1. **主キーはBIGINT AUTO_INCREMENT**にしました。これまでのER図では `string` 表記でしたが、
   実装上は自動採番の数値IDの方が結合・インデックス効率が良いため変更しています。
2. **Javaパッケージ名は `com.foodfactory.dx`** としています。実際のプロジェクトのパッケージ名に
   合わせて全ファイルで一括置換してください(VSCode/Eclipseどちらでも「フォルダ内で置換」機能で対応可能です)。
3. **`allowed_origins`(許可産地)はカンマ区切りの文字列**として保存する簡易実装です。
   `RecipeItem` (domain)クラスに `getAllowedOriginList()` / `setAllowedOriginList()` を用意しており、
   リスト形式で読み書きできます。検索性を上げたくなったら、将来的に別テーブルに正規化できます。
4. **Lombokは使用していません**(未導入の可能性を考慮し、素のgetter/setterで実装)。
   導入済み、または今後導入する場合は `@Getter @Setter` 等に置き換えると記述量を減らせます。
5. **domainクラスは外部キーをID(Long型)のまま持たせています**(例: `RecipeItem.itemId`)。
   JPAの`@ManyToOne`のように関連先オブジェクトをまるごと持たせる書き方はしていません。
   関連する商品や材料の詳細情報が必要な場合は、Service層でそれぞれのMapperを呼んで
   自分でオブジェクトを組み立てる形になります(MyBatisの一般的な作法です)。

## 既存プロジェクト(JPA版)からの移行手順

Eclipseに既にJPA版の`Food-Factory-Dx`プロジェクトを取り込み済みの場合、
このZIPの中身をそのまま上書きするのではなく、以下の手順で差し替えることを推奨します。

1. **不要になったファイルを削除する**
   - `src/main/java/com/foodfactory/dx/entity/` フォルダごと削除
   - `src/main/java/com/foodfactory/dx/repository/` フォルダごと削除
2. **このZIPの以下のファイルをコピーして追加・上書きする**
   - `pom.xml`(MyBatisの依存関係に変更済み)
   - `src/main/java/com/foodfactory/dx/DxApplication.java`(`@MapperScan`を追加済み)
   - `src/main/java/com/foodfactory/dx/domain/`(新規、JPAのentityに相当)
   - `src/main/java/com/foodfactory/dx/mapper/`(新規、JPAのrepositoryに相当)
   - `src/main/resources/application.properties`(MyBatis用に整理済み)
   - `src/main/resources/mybatis/`(新規、実際のSQL文)
3. **Eclipseでプロジェクトを右クリック→`Maven > Update Project`を実行**
   `pom.xml`の依存関係の変更(JPA削除・MyBatis追加)を反映させるために必要です。
4. Gitでコミットする際、`git status`で「entity/repositoryが削除され、domain/mapperが追加された」
   という差分になっていることを確認してからコミットしてください。

## セットアップ手順

### 1. MySQL側の準備(MySQL Workbench 8.0 CE)

`sql/phase0_master_schema.sql` をMySQL Workbenchで開き、実行してください。
`food_factory_dx` データベースと4つのテーブルが作成されます。

### 2. プロジェクトの取り込み

**Eclipseの場合**
1. ZIPを展開したフォルダをそのまま任意の場所に置く
2. Eclipseで `File > Import > Maven > Existing Maven Projects` を選び、展開したフォルダを指定
3. `pom.xml` を読み込んで自動的に依存関係(Spring Boot等)がダウンロードされる

**VSCodeの場合**
1. 展開したフォルダを `File > Open Folder` で開く
2. 「Extension Pack for Java」がインストール済みであれば自動的にMavenプロジェクトとして認識される
3. 初回はライブラリのダウンロードに少し時間がかかる

### 3. 接続情報の設定

`src/main/resources/application.properties` の以下の値を、
お使いのMySQL Workbench環境に合わせて書き換えてください。

```
spring.datasource.username=root
spring.datasource.password=your_password_here
```

### 4. 起動確認

`DxApplication.java` を右クリックして「Run」(Eclipse)、または `mvn spring-boot:run`(VSCodeのターミナル)で起動します。

MyBatisはJPAと違い「起動時にテーブル構造を自動検証する」機能を持たないため、
エラーなく起動できても、それはあくまで「MySQLへの接続自体には成功した」ことしか意味しません。
SQLの中身(列名の誤字など)に問題があった場合、そのSQLを実際に呼び出すAPIを叩いて初めてエラーが分かります。
そのため、フェーズ1でController層を作った際に、実際に登録・取得のAPIを呼んで動作確認することが重要になります。

`server.port=8080` としているので、起動後は `http://localhost:8080` にアクセス可能な状態になります
(ただしまだControllerを作っていないため、現時点ではAPIエンドポイントは何も返しません)。

## フェーズ1: 発注・入荷管理(追加分)

対象テーブル: `material_order`, `material_arrival`, `material_arrival_line`, `material_lot`
(DDLは `sql/phase1_procurement_schema.sql`。phase0のDDLを実行済みであることが前提)

### 追加されたAPI

| メソッド | URL | 内容 |
|---|---|---|
| POST | `/api/material-orders` | 発注を1件登録 |
| GET  | `/api/material-orders` | 発注の一覧取得 |
| POST | `/api/material-arrivals` | 入荷ヘッダー(伝票)を1件登録 |
| GET  | `/api/material-arrivals` | 入荷ヘッダーの一覧取得(`?orderId=`で絞り込み可) |
| POST | `/api/material-arrivals/{arrivalId}/lines` | 入荷明細を検品結果込みで登録(材料ロット自動生成・発注ステータス自動更新) |
| GET  | `/api/material-lots?materialId=` | 指定した材料のロットを賞味期限順(FEFO順)で取得 |

### 動作確認の流れ(curlコマンド例)

事前に `material` テーブルに材料が1件登録されている必要があります(フェーズ0で試した米粉登録がまだの場合は先に実行してください)。以下は `materialId=1` が米粉である前提の例です。

**1. 発注を登録する**
```
curl -X POST http://localhost:8080/api/material-orders -H "Content-Type: application/json" -d "{\"materialId\":1,\"supplierId\":\"仕入先A\",\"orderQty\":45000,\"orderDate\":\"2026-07-08\",\"expectedDate\":\"2026-07-09\"}"
```
返ってきたJSONの `orderId` を控えておいてください(以降の手順で使います)。

**2. 入荷ヘッダーを登録する**(`orderId`は手順1で控えた値に置き換える)
```
curl -X POST http://localhost:8080/api/material-arrivals -H "Content-Type: application/json" -d "{\"orderId\":1,\"supplierId\":\"仕入先A\",\"arrivalDate\":\"2026-07-09\"}"
```
返ってきたJSONの `arrivalId` を控えます。`materialId`が発注側から自動的にコピーされていることも確認してください。

**3. 入荷明細を検品結果込みで登録する**(`arrivalId`はURLの中に埋め込みます)

以前の会話で例に出した「三重産ロットA×5箱、三重産ロットB×2箱、愛知産ロットC×3箱、すべて検品合格」のうち、まず1件目(ロットA)を登録してみます。

```
curl -X POST http://localhost:8080/api/material-arrivals/1/lines -H "Content-Type: application/json" -d "{\"supplierLotNo\":\"LOT-A\",\"origin\":\"三重\",\"expiryDate\":\"2026-10-01\",\"packageCount\":5,\"packageWeightSnapshot\":15000,\"acceptedQty\":75000,\"heldQty\":0,\"checkDamage\":true,\"checkExpiry\":true,\"checkContamination\":true}"
```
(packageWeightSnapshotは1箱15000g=15kgとして計算しています。5箱なので合格数量は75000gになります)

**4. 材料ロットが自動生成されたか確認する**
```
curl http://localhost:8080/api/material-lots?materialId=1
```
先ほど登録した三重産・LOT-A由来のロットが1件返ってくれば成功です。

**5. 発注のステータスが自動更新されたか確認する**
```
curl http://localhost:8080/api/material-orders
```
`orderId=1`の発注の`status`が、発注数量45000に対して75000が入荷しているため`FULLY_ARRIVED`(入荷完了)になっているはずです。

### 意図的に組み込んだエラーチェックの確認

検品結果の数量が入荷総量と一致しない場合、登録が拒否されることも確認できます。

```
curl -X POST http://localhost:8080/api/material-arrivals/1/lines -H "Content-Type: application/json" -d "{\"supplierLotNo\":\"LOT-X\",\"origin\":\"三重\",\"expiryDate\":\"2026-10-01\",\"packageCount\":1,\"packageWeightSnapshot\":15000,\"acceptedQty\":10000,\"heldQty\":0,\"checkDamage\":true,\"checkExpiry\":true,\"checkContamination\":true}"
```
1箱15000g届いたはずなのに合格数量を10000gとしているため一致せず、400エラーと理由メッセージが返ってくるはずです(`GlobalExceptionHandler`による変換)。

### フェーズ0の設計からの修正点

実装の過程で、`material_arrival`(入荷ヘッダー)に`material_id`が無く、発注に紐づかない緊急入荷の場合にどの材料の入荷か特定できないという設計漏れが見つかったため、`material_arrival`に`material_id`列を追加しています(発注に紐づく場合は自動的にコピーされます)。

## フェーズ2: 製造管理(先行実装・未検証)

対象テーブル: `manufacturing_batch`, `batch_material_usage`
(DDLは `sql/phase2_manufacturing_schema.sql`。phase0, phase1のDDLが実行済みであることが前提)

**注意**: このフェーズはコードとしては書き上げているが、まだ実機での動作確認(MySQL接続確認・API呼び出し確認)ができていない。次回作業時に、フェーズ1の残り(検品登録の確認)を終えたあと、こちらの動作確認に進む。

### レシピ登録の例(Thunder Client)

`materialId`は既に登録済みの材料(例:米粉、materialId=1)を指定してください。

```
POST http://localhost:8080/api/items/1/recipe-items
Body(JSON):
{
  "materialId": 1,
  "useQty": 15000,
  "allowedOrigins": "愛知,三重",
  "mainMaterial": true,
  "liquid": false
}
```



```
POST http://localhost:8080/api/items
Body(JSON):
{
  "name": "醤油ラーメン",
  "safetyStockQty": 500,
  "targetStockQty": 500,
  "standardBatchQty": 200,
  "shelfLifeDays": 90
}
```


### 追加されたAPI

| メソッド | URL | 内容 |
|---|---|---|
| GET  | `/api/items` | 商品の一覧取得 |
| POST | `/api/items` | 商品を1件登録 |
| GET  | `/api/items/{itemId}/recipe-items` | 指定した商品のレシピ明細一覧を取得 |
| POST | `/api/items/{itemId}/recipe-items` | レシピ明細を1件登録 |
| POST | `/api/items/{itemId}/batches` | バッチを新規作成(DRAFT) |
| GET  | `/api/items/{itemId}/fefo-preview` | FEFO自動選定結果のプレビュー(在庫は変更しない) |
| GET  | `/api/batches` | バッチの一覧取得 |
| GET  | `/api/batches/{batchId}/usages` | バッチの材料使用記録を取得 |
| POST | `/api/batches/{batchId}/confirm-plan` | DRAFT→PLAN |
| POST | `/api/batches/{batchId}/execute` | PLAN→MANUFACTURING(材料ロット消費) |
| POST | `/api/batches/{batchId}/complete` | MANUFACTURING→COMPLETED(検品結果確定) |
| POST | `/api/batches/{batchId}/reject` | MANUFACTURING→REJECTED(重大な異常時) |

### 実装にあたって置いた前提(要確認)

1. **`recipe_item.use_qty`はバッチ1回あたりの固定使用量**として扱っている(商品の個数に比例して増減しない)。そのため`manufacturing_batch.plannedQty`は常に`items.standard_batch_qty`と同じ値になる想定で実装した。
2. **完成品(商品)の在庫を追跡する仕組みはまだ無い**。`completeBatch`で`acceptedQty`(合格数)は記録するが、それを`items`側の在庫として反映する処理はフェーズ5(出荷管理)と合わせて設計する想定のため、今回は未実装。

### 単体テスト(DB不要、Eclipse上で即実行できる)

`ManufacturingService.completeBatch()`の「計画超過判定」ロジックだけを、DBに接続せず検証するテストを用意した。
draft作成→confirm-plan→execute→completeという手順をThunder Clientで毎回やり直さなくても、
Eclipse上で以下の手順ですぐに実行・確認できる。

1. Eclipseのパッケージエクスプローラーで `src/test/java/com/foodfactory/dx/service/ManufacturingServiceTest.java` を開く
2. ファイルを右クリック → `Run As > JUnit Test`
3. 緑のバー(全テスト成功)が表示されれば正常

含まれるテスト内容:
- 計画数量ちょうど・以内 → `exceededPlan = false`
- 計画数量を超過 → `exceededPlan = true`
- `producedQty`が`acceptedQty + lossQty`と一致するか
- `MANUFACTURING`状態でないバッチを完了しようとすると例外が飛ぶか

## フェーズ1の構造変更(重要)

実装を進める中で、`material_arrival`(入荷ヘッダー)に`material_id`を1つしか持たせられず、
**1回の配送で複数の異なる材料・複数の異なる発注が混在するケースを正しく表現できない**という
設計上の問題が見つかった。`material_id`・`order_id`はヘッダーではなく、
明細(`material_arrival_line`)側に持たせるよう変更した。

この変更に伴い、以下のAPIの挙動が変わっている。

- `POST /api/material-arrivals`: リクエストボディから`materialId`/`orderId`が無くなった(`supplierId`と`arrivalDate`だけになった)
- `POST /api/material-arrivals/{arrivalId}/lines`: リクエストボディに`materialId`(必須)・`orderId`(任意)を追加する必要がある
- `GET /api/material-arrivals?orderId=`: 廃止。代わりに`GET /api/material-orders/{orderId}/lines`を使う

**この変更に伴い、テーブル構造そのものが変わっているため、既存のテストデータはリセットが必要。**
`sql/migration_arrival_line_restructure.sql`を実行してから、`phase1`〜`phase3`のDDLを再実行すること。

## フェーズ3: 保留対応・手動在庫調整

対象テーブル: `hold_resolution`, `stock_adjustment`
(DDLは `sql/phase3_hold_adjustment_schema.sql`)

### 設計のポイント

- 検品で保留(`heldQty > 0`)が発生すると、`ProcurementService.registerInspectedLine()`が
  自動的に`hold_resolution`を1件作成する(`status=ON_HOLD`)
- 保留への対応は3パターン
  - **返品(RETURNED)**: 在庫は増減しない。`POST /api/holds/{holdId}/resolve-returned`
  - **交換(EXCHANGED)**: 新しい入荷明細を登録する際、`resolvesHoldId`パラメータで
    どの保留に対する交換品かを指定する。`POST /api/material-arrivals/{arrivalId}/lines?resolvesHoldId={holdId}`
  - **結局受け入れる(ACCEPTED_LATE)**: 保留分を合格分に繰り入れ、対応する材料ロットの残量を増やす
    (無ければ新規作成)。増減は必ず`stock_adjustment`に記録してから反映する。
    `POST /api/holds/{holdId}/resolve-accepted-late`
- 在庫の手動調整(棚卸し補正等)は`POST /api/material-lots/{lotId}/adjustments`で行う。
  `material_lot.remainingQty`を直接書き換えるAPIは無く、必ずこの経路(`stock_adjustment`記録付き)を通す

### 追加されたAPI

| メソッド | URL | 内容 |
|---|---|---|
| GET  | `/api/holds` | 対応待ち(ON_HOLD)の保留一覧 |
| POST | `/api/holds/{holdId}/resolve-returned` | 返品として対応 |
| POST | `/api/holds/{holdId}/resolve-accepted-late` | 結局受け入れるとして対応 |
| GET  | `/api/material-lots/{lotId}/adjustments` | ロットの調整履歴一覧 |
| POST | `/api/material-lots/{lotId}/adjustments` | ロットの残量を手動補正(理由コメント必須) |
| GET  | `/api/material-orders/{orderId}/lines` | 発注に紐づく入荷明細の一覧(充足内訳の確認用) |
| GET  | `/api/material-arrivals/{arrivalId}/lines` | 入荷ヘッダーに属する明細の一覧 |

## フェーズ5: 注文管理・出荷

対象テーブル: `customer`, `carrier`, `customer_order`, `order_line`, `shipment`, `shipment_line`
(DDLは `sql/phase5_order_shipment_schema.sql`)

### 設計のポイント

- 出荷のFEFO選定は、材料側(産地フィルター)と同じ考え方で、取引先の残存期限ルール
  (`customer.requiredResidualRatio`)を満たさないバッチは現場判断での代替を許可せず、
  候補から除外する
- `manufacturing_batch.remainingQty`は、材料ロットと同じ「条件付きDB更新」方式で減算する
  (`decrementRemainingQty`。同時出荷による二重出荷を防ぐ)
- 受注のキャンセルは在庫プール型(ステータス変更のみ、バッチとの紐付け解除処理は不要)

### 追加されたAPI

| メソッド | URL | 内容 |
|---|---|---|
| GET/POST | `/api/customers` | 取引先の一覧取得・登録 |
| GET/POST | `/api/carriers` | 配送会社の一覧取得・登録 |
| GET/POST | `/api/customer-orders` | 受注の一覧取得・登録 |
| POST | `/api/customer-orders/{orderId}/confirm` | 受注確定(NEW→CONFIRMED) |
| POST | `/api/customer-orders/{orderId}/cancel` | 受注キャンセル |
| GET/POST | `/api/customer-orders/{orderId}/lines` | 受注明細の一覧取得・登録 |
| GET/POST | `/api/shipments` | 出荷ヘッダーの一覧取得・登録 |
| GET | `/api/order-lines/{orderLineId}/shipment-preview` | 出荷FEFO自動選定のプレビュー |
| POST | `/api/shipments/{shipmentId}/lines` | 出荷明細登録(バッチ残量を減算) |

## フェーズ4: MRP自動化

対象テーブル: `mrp_run`(新規)、`manufacturing_batch`(ALTER: `CANCELLED`ステータス・`cancel_comment`追加)
(DDLは `sql/phase4_mrp_schema.sql`。既存環境にALTERで適用できる)

### 計算式の修正について

以前の計算式には「有効在庫を2回差し引く」二重控除の誤りがあった。標準的なMRPの
正味所要量計算に合わせ、以下の式に修正した(要件定義書 5.2節を参照)。

```
需要量 = 受注残 + 適正在庫(そのまま)
正味不足量 = 需要量 − (有効在庫 + 供給予定量)
```

### CANCELLEDステータスとMRPの即時再計算

- `DRAFT`/`PLAN`状態のバッチは、製造開始前であれば`CANCELLED`として取り消せる
- `CANCELLED`・`REJECTED`が発生すると、供給予定量から即座に外れることになるため、
  `ManufacturingService`が`MrpService`を呼び出し、その商品についてMRPを即座に再計算する
  (`triggeredBy=EVENT`)。次回の定期実行を待たずに不足を検知できる
- `ManufacturingService`と`MrpService`は互いを参照し合う関係になるため、
  `MrpService`側の注入を`@Lazy`にして循環依存を解消している

### 追加されたAPI

| メソッド | URL | 内容 |
|---|---|---|
| POST | `/api/batches/{batchId}/cancel` | DRAFT/PLAN→CANCELLED(MRP即時再計算) |
| POST | `/api/batches/confirm-plan-bulk` | 複数バッチを一括でDRAFT→PLANに確定 |
| GET | `/api/batches/stale-drafts?days=3` | 指定日数以上放置されているDraftの一覧 |
| POST | `/api/mrp/run` | 全商品についてMRPを手動実行(AUTO相当) |

## 次のステップ

このフェーズ0はdomain/mapperまでの実装です。次に必要になるのは:

1. **Service層**: バリデーション(例:同じ商品・材料の組み合わせのレシピ重複チェックなど)
2. **Controller層**: REST API(`GET /materials`, `POST /materials` など)
3. **React側の画面**: 一覧表示・登録フォーム

これらは別途、必要になったタイミングで相談しながら進めましょう。
