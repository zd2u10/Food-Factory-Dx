package com.foodfactory.dx.mapper;

import com.foodfactory.dx.domain.Material;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 材料マスタに対するDB操作の「入り口」を定義するインターフェース。
 *
 * ここには処理の中身(SQL)を一切書かない。あくまで「こういうメソッドがあります」という
 * 宣言だけを書き、実際のSQL文は同名の resources/mybatis/MaterialMapper.xml に書く。
 *
 * @Mapper: このインターフェースをMyBatisに認識させるための目印。
 *          これを付けておくと、Spring Bootが起動時に自動的にこのインターフェースの
 *          「実体(裏側でXMLのSQLを実行してくれる代理オブジェクト)」を作ってくれる。
 *          そのため、Service層などでは new せずに @Autowired 等でそのまま注入して使える。
 */
@Mapper
public interface MaterialMapper {

    /**
     * 材料を1件登録する。
     * 戻り値のintは「何行のデータが変更されたか」を表す(登録成功なら通常1)。
     *
     * 補足: このメソッドを呼ぶと、XML側の設定により
     * 渡した material オブジェクトの materialId フィールドに、
     * DBが自動採番したIDが自動的に詰め直される(呼び出し側で改めて取得しに行く必要がない)。
     */
    int insert(Material material);

    /**
     * IDを指定して1件取得する。
     *
     * Optional<Material> : 「該当データが見つからないかもしれない」ことを型で表現する仕組み。
     * 見つからなかった場合、XML側からはnullが返ってくるが、
     * ここでOptionalに包むことで、呼び出し側に「nullチェックを忘れずに」と型レベルで注意を促せる。
     *
     * @Param("materialId") : XML側のSQL文の中で #{materialId} という書き方で
     *                        この引数の値を参照できるようにするための目印。
     *                        引数が1つだけの場合は省略可能なことも多いが、
     *                        分かりやすさのため明示的に付けている。
     */
    Optional<Material> findById(@Param("materialId") Long materialId);

    /** 登録されている材料を全件取得する。 */
    List<Material> findAll();

    /**
     * 分類(RAW/ADDITIVE)・有効フラグで絞り込んで取得する。
     * category/activeはどちらもnullを許容する。nullの場合はその条件を無視して全件対象にする
     * (XML側のMyBatis動的SQL(<if>タグ)で、値がある条件だけWHERE句に含める仕組みになっている)。
     */
    List<Material> findByFilters(@Param("category") Material.Category category,
                                  @Param("active") Boolean active);

    /** 材料の内容を更新する。戻り値は変更された行数。 */
    int update(Material material);

    /**
     * 有効/廃版フラグだけを更新する専用メソッド(論理削除・復元の両方に使う)。
     * activeにfalseを渡せば廃版(論理削除)、trueを渡せば復元になる。
     * 物理的な削除(DELETE文)は行わない。他のテーブル(recipe_item等)から
     * 既に参照されている可能性があり、参照整合性を壊さないようにするため。
     */
    int setActive(@Param("materialId") Long materialId, @Param("active") boolean active);
}
