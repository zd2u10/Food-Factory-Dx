# 食品工場DXシステム(フェーズ0: 基盤マスタ + プロジェクト初期構成)

## 含まれるもの

```
pom.xml                              Maven依存関係定義(Spring Boot 3.3.4 / Java 21)
.gitignore                           Java/Eclipse/VSCode向け
sql/phase0_master_schema.sql         MySQL 8.0向けDDL(テーブル作成SQL)
src/main/java/com/foodfactory/dx/
  DxApplication.java                 起動エントリーポイント
  entity/                            JPA Entityクラス(4つ)
  repository/                        Spring Data JPA Repositoryインターフェース(4つ)
  controller/                        (空、フェーズ1以降でREST APIを追加)
  service/                           (空、フェーズ1以降でビジネスロジックを追加)
  dto/                                (空、フェーズ1以降でAPI入出力用クラスを追加)
  exception/                          (空、フェーズ1以降で業務例外クラスを追加)
src/main/resources/application.properties   MySQL接続・JPA設定
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
   `RecipeItem` エンティティに `getAllowedOriginList()` / `setAllowedOriginList()` を用意しており、
   リスト形式で読み書きできます。検索性を上げたくなったら、将来的に別テーブルに正規化できます。
4. **Lombokは使用していません**(未導入の可能性を考慮し、素のgetter/setterで実装)。
   導入済み、または今後導入する場合は `@Getter @Setter` 等に置き換えると記述量を減らせます。

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
エラーなく起動できれば、Entityの定義とテーブル構造(`ddl-auto=validate`)が一致していることが確認できたことになります。
`server.port=8080` としているので、起動後は `http://localhost:8080` にアクセス可能な状態になります
(ただしまだControllerを作っていないため、現時点ではAPIエンドポイントは何も返しません)。

## 次のステップ

このフェーズ0はEntity/Repositoryまでの実装です。次に必要になるのは:

1. **Service層**: バリデーション(例:同じ商品・材料の組み合わせのレシピ重複チェックなど)
2. **Controller層**: REST API(`GET /materials`, `POST /materials` など)
3. **React側の画面**: 一覧表示・登録フォーム

これらは別途、必要になったタイミングで相談しながら進めましょう。
