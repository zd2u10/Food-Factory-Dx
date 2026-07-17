package com.foodfactory.dx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * アプリケーションのエントリーポイント。
 * Eclipse/VSCodeどちらからでも、このクラスのmainメソッドを実行して起動できる。
 */
@SpringBootApplication
public class DxApplication {

    public static void main(String[] args) {
        SpringApplication.run(DxApplication.class, args);
    }
}
