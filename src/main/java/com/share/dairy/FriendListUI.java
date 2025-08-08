package com.share.dairy;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class FriendListUI extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // FXML 파일 로드
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/share/dairy/FriendListUI.fxml")
        );
        Parent root = loader.load();

        // Scene 생성
        Scene scene = new Scene(root);

        // (선택) 외부 CSS를 추가로 적용하고 싶다면
        // scene.getStylesheets().add(
        //     getClass().getResource("/com/share/dairy/friend-list.css").toExternalForm()
        // );

        // Stage 세팅
        primaryStage.setTitle("구리구리’s Buddy List");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(400);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
