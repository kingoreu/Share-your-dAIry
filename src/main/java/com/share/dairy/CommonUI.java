package com.share.dairy;

import java.net.URL;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * 공통 UI만 테스트하는 애플리케이션
 */
public class CommonUI extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // FXML 로드
        URL fxml = getClass().getResource("/com/share/dairy/common.fxml");
        if (fxml == null) throw new IllegalStateException("common.fxml 없음");
        Parent root = FXMLLoader.load(fxml);

        // Scene 생성 + CSS 적용
        Scene scene = new Scene(root, 800, 600);
        URL css = getClass().getResource("/com/share/styles/common.css");
        if (css == null) throw new IllegalStateException("common.css 없음");
        scene.getStylesheets().add(css.toExternalForm());

        // Stage 세팅
        stage.setTitle("Common UI Test");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
