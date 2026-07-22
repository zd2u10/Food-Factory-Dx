package com.foodfactory.dx;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * アプリケーションのエントリーポイント。
 * Eclipse/VSCodeどちらからでも、このクラスのmainメソッドを実行して起動できる。
 *
 * @MapperScan: 「com.foodfactory.dx.mapper パッケージの中にある @Mapper 付きインターフェースを
 *              全部自動的に見つけて、Springが使える形にしてください」という指示。
 *              これを書いておくことで、mapperパッケージに新しいインターフェースを追加しても、
 *              その都度ここを書き換える必要がなくなる。
 *              (mybatis-spring-boot-starter は本来この指定がなくても自動探索してくれることが多いが、
 *               「どこにMapperがあるか」をコードの見た目からも分かるように明示している)
 */
@SpringBootApplication
@MapperScan("com.foodfactory.dx.mapper")
public class DxApplication {

    public static void main(String[] args) {
        SpringApplication.run(DxApplication.class, args);
    }
}
