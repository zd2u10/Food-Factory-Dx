package com.foodfactory.dx.service;

import com.foodfactory.dx.domain.Material;
import com.foodfactory.dx.mapper.MaterialMapper;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 材料マスタに関するビジネスロジックを置く場所。
 *
 * 今はまだ「MyBatisのSQLが正しく動くかどうかの確認」が目的のため、
 * MapperのメソッドをほぼそのままController向けに橋渡しするだけの薄い実装になっている。
 * 今後フェーズが進むにつれて、ここに「同じ名前の材料は登録できないようにする」といった
 * バリデーション(入力値の妥当性チェック)を追加していく想定。
 *
 * @Service: 「これはビジネスロジックを持つ部品です」とSpringに伝えるための目印。
 *           これを付けておくと、Spring起動時にこのクラスのインスタンスが1つだけ自動的に作られ、
 *           他のクラス(Controllerなど)から自由に呼び出せるようになる
 *           (この「Springが自動でインスタンスを作って必要な場所に配ってくれる」仕組みを
 *            DI: Dependency Injection = 依存性の注入 と呼ぶ)。
 */
@Service
public class MaterialService {

    private final MaterialMapper materialMapper;

    /**
     * コンストラクタインジェクション。
     *
     * 「MaterialServiceを作る時には、MaterialMapperの実体を一緒に渡してください」
     * とSpringに指示している形になる。Spring Bootは @Mapper が付いた MaterialMapper の
     * 実体を既に用意しているので、このコンストラクタが呼ばれる際に自動的に渡してくれる。
     * (newで自分でインスタンスを作る必要が一切ない)
     */
    public MaterialService(MaterialMapper materialMapper) {
        this.materialMapper = materialMapper;
    }

    /** 登録されている材料を全件取得する。 */
    public List<Material> listMaterials() {
        return materialMapper.findAll();
    }

    /**
     * 新しい材料を1件登録する。
     *
     * mapper.insert(material) を呼んだ時点で、
     * 渡した material オブジェクトの materialId フィールドに
     * DBが自動採番したIDが詰め直される(MaterialMapper.xmlのuseGeneratedKeysの設定による)。
     * そのため、insert実行後にそのまま material を返せば、
     * 呼び出し元(Controller)は採番されたIDを含む完成形のデータを受け取れる。
     */
    public Material createMaterial(Material material) {
        materialMapper.insert(material);
        return material;
    }
}
