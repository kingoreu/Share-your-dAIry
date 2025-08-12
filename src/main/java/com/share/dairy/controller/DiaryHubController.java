package com.share.dairy.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class DiaryHubController {

    @FXML private StackPane content;

    @FXML
    public void initialize() {
        // ESC: 허브에서 "방 화면"으로 복귀 (※ 기존 OurDiary로 가던 버그 수정)
        content.sceneProperty().addListener((obs, oldScene, scene) -> {
            if (scene != null) {
                scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                    if (e.getCode() == KeyCode.ESCAPE) {
                        switchTo("/fxml/mainFrame/Main.fxml");  // ✅ Home으로
                        e.consume();
                    }
                });
            }
        });
        content.setFocusTraversable(true);
        content.requestFocus();

        // 5-1 허브 기본: 비워둠
        content.getChildren().clear();
    }

    // 왼쪽 버튼 핸들러들
    @FXML private void showMyDiary()    { setCenter("/fxml/diary/my_diary/my-diary-view.fxml"); }

    // ✅ Our Diary는 사진처럼 "풀 화면 전환"
    @FXML private void showOurDiary()   { setCenter("/fxml/diary/our_diary/our-diary-view.fxml"); }

    @FXML private void showBuddyDiary() { setCenter("/fxml/diary/buddy_diary/buddy-diary-view.fxml"); }

    // 허브 안에서 중앙 영역만 바꾸는 방식 (그대로 유지)
    private void setCenter(String fxml) {
        try {
            Parent node = FXMLLoader.load(getClass().getResource(fxml));
            content.getChildren().setAll(node);
            content.requestFocus(); // ESC 포커스 유지
        } catch (Exception e) {
            System.err.println("FXML 로드 실패: " + fxml);
            e.printStackTrace();
        }
    }

    // ✅ 씬 전체 교체(풀 전환) — Router 안 타고 바로 전환해서 확실하게 넘어감
    private void switchTo(String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage st = (Stage) content.getScene().getWindow();
            // 방 사이즈 유지
            javafx.scene.Scene sc = new javafx.scene.Scene(root, st.getWidth(), st.getHeight());
            sc.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            st.setScene(sc);
        } catch (Exception e) {
            System.err.println("씬 전환 실패: " + fxml);
            e.printStackTrace();
        }
    }
}
