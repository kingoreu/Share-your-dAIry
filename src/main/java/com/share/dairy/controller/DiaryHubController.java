package com.share.dairy.controller;

import com.share.dairy.app.Router;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import java.net.URL;

/**
 * DiaryHubController
 * - 허브의 중앙 영역(centerHolder)에 하위 화면(FXML)을 주입
 * - ESC: 홈(방)으로 복귀 (Router.go("Home"))
 * - setCenter(): FXML 경로 검증/바인딩/에러 알림 포함
 */
public class DiaryHubController {

    @FXML private StackPane content;      // 허브 전체(상단/좌측/FAB 포함) 루트
    @FXML private StackPane centerHolder; // 중앙 교체 대상 컨테이너

    /** ESC → Home 라우팅 */
    private final EventHandler<KeyEvent> escFilter = e -> {
        if (e.getCode() == KeyCode.ESCAPE) {
            Router.go("Home"); // Main.fxml로 복귀 (팀 라우팅 규칙)
            e.consume();
        }
    };

    @FXML
    public void initialize() {
        // ESC 필터 등록/해제 (Scene 교체 시 누수 방지)
        content.sceneProperty().addListener((obs, oldScene, scene) -> {
            if (oldScene != null) oldScene.removeEventFilter(KeyEvent.KEY_PRESSED, escFilter);
            if (scene != null)     scene.addEventFilter(KeyEvent.KEY_PRESSED, escFilter);
        });

        // 포커스 줘서 ESC가 안정적으로 잡히게
        content.setFocusTraversable(true);
        content.requestFocus();

        // 진입 시 허브 리스트 표시
        showDiaryHub();
    }

    /* 좌/상단 버튼 핸들러 — 중앙만 교체 */
    @FXML private void showDiaryHub()   { setCenter("/fxml/diary/diary_hub/hub-list.fxml"); }
    @FXML private void showMyDiary()    { setCenter("/fxml/diary/my_diary/my-diary-view.fxml"); }
    @FXML private void showOurDiary()   { setCenter("/fxml/diary/our_diary/our-diary-view.fxml"); }
    @FXML private void showBuddyDiary() { setCenter("/fxml/diary/buddy_diary/buddy-diary-view.fxml"); }

    /** 중앙 영역 교체 (상단/좌측/FAB는 그대로 유지) */
    private void setCenter(String fxml) {
        try {
            URL url = getClass().getResource(fxml);
            if (url == null) throw new IllegalStateException("FXML not found: " + fxml);

            FXMLLoader loader = new FXMLLoader(url);
            Parent node = loader.load();

            centerHolder.getChildren().setAll(node);

            // 자식이 Region이면 컨테이너 크기에 맞춰 바인딩
            if (node instanceof Region r) {
                r.prefWidthProperty().bind(centerHolder.widthProperty());
                r.prefHeightProperty().bind(centerHolder.heightProperty());
            }

            // ESC 포커스 유지
            content.requestFocus();

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Load failed: " + fxml).showAndWait();
        }
    }
}
