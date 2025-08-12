package com.share.dairy.app;

import com.share.dairy.ServerApplication;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Map;

public class HelloApplication extends Application {

    private ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        // Spring Boot 서버 기동 (별도 스레드)
        Thread serverThread = new Thread(() -> {
            springContext = new SpringApplicationBuilder(ServerApplication.class)
                    .properties(Map.of(
                            "server.port", "8080",
                            "spring.datasource.url", "jdbc:mysql://localhost:3306/diary?serverTimezone=Asia/Seoul",
                            "spring.datasource.username", "root",
                            "spring.datasource.password", "sohyun"
                    ))
                    .run();
        });
        serverThread.setDaemon(true);
        serverThread.start();
    }



    @Override
    public void start(Stage stage) throws Exception {
        Router.init(stage);

        // 클래스패스 루트 기준으로 절대경로 사용
        Parent root = FXMLLoader.load(
                getClass().getResource("/fxml/mainFrame/Main.fxml")
        );
        Scene scene = new Scene(root, 800, 600);
        stage.setTitle("공유일기");
        stage.setScene(scene);
        stage.show();

    }

    @Override
    public void stop() {
        if (springContext != null) {
            springContext.close();
        }
        Platform.exit();
    }

    public static void main(String[] args) {
        launch();
    }
}
