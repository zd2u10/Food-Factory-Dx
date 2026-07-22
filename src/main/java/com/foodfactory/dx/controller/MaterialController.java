package com.foodfactory.dx.controller;

import com.foodfactory.dx.domain.Material;
import com.foodfactory.dx.service.MaterialService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 材料マスタのREST APIエンドポイント。
 *
 * ここは「動作確認用」の最小構成。
 * 本来はリクエスト/レスポンス専用のDTO(dtoパッケージ)を間に挟むのが望ましいが、
 * 今回はMyBatisのSQLが正しく動くかどうかを素早く確認する目的のため、
 * domainクラス(Material)をそのままやり取りしている。
 *
 * @RestController: このクラスの各メソッドの戻り値を、そのままJSONに変換して
 *                  HTTPレスポンスとして返す、という意味の目印
 *                  (画面のHTMLを返す通常のControllerとは違い、APIサーバー用の動き方になる)。
 * @RequestMapping("/api/materials"): このクラスの全メソッドは
 *                  "http://localhost:8080/api/materials" から始まるURLに反応する、という意味。
 */
@RestController
@RequestMapping("/api/materials")
public class MaterialController {

    private final MaterialService materialService;

    public MaterialController(MaterialService materialService) {
        this.materialService = materialService;
    }

    /**
     * 材料の一覧を取得する。
     *
     * @GetMapping: HTTPのGETメソッドでアクセスされた時にこのメソッドを実行する、という目印。
     *              引数を何も書いていないので、"/api/materials" にGETでアクセスすると呼ばれる。
     *              ブラウザにURLをそのまま打ち込むだけで試せる(ブラウザの通常のアクセスはGETのため)。
     */
    @GetMapping
    public List<Material> list() {
        return materialService.listMaterials();
    }

    /**
     * 材料を1件新規登録する。
     *
     * @PostMapping: HTTPのPOSTメソッドでアクセスされた時にこのメソッドを実行する、という目印。
     *               POSTは「新しいデータを送って登録する」際に使うのが一般的な約束事。
     *               ブラウザにURLを打つだけでは試せず、curlやPostmanなどのツールが必要になる。
     *
     * @RequestBody Material material: HTTPリクエストの本文(JSON形式)を、
     *              自動的にMaterialオブジェクトに変換して受け取る、という意味。
     *              例えば {"name":"米粉","category":"RAW","baseUnit":"WEIGHT","mainMaterial":true}
     *              というJSONが送られてきたら、それぞれの値がMaterialのフィールドに詰められる。
     *
     * ResponseEntity<Material>: レスポンスの中身(登録された材料の情報)に加えて、
     *              HTTPステータスコード(処理が成功したかどうかを表す数字)も
     *              合わせて返すための入れ物。
     *              HttpStatus.CREATED は「201番、新規作成に成功しました」という意味のコード。
     *              (単に material をそのまま返すだけでもJSONは返せるが、
     *               ステータスコードまで明示することでAPIとしてより丁寧な作りになる)
     */
    @PostMapping
    public ResponseEntity<Material> create(@RequestBody Material material) {
        Material created = materialService.createMaterial(material);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
