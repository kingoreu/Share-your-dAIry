package com.share.dairy.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.net.URL;

/**
 * DiaryHubController
 * - 허브의 중앙 영역(StackPane content)에 하위 화면(FXML)을 주입
 * - ESC: 허브에서 방(Main.fxml)로 복귀
 * - setCenterSafe(): FXML 경로 검증/에러 알림/로그 포함 (한 번에 확실히 로드되게)
 */
public class DiaryHubController {

    @FXML private StackPane content;

    @FXML
    public void initialize() {
        // ESC → Home(방)으로 풀화면 전환
        content.sceneProperty().addListener((obs, oldScene, scene) -> {
            if (scene != null) {
                scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                    if (e.getCode() == KeyCode.ESCAPE) {
                        switchTo("/fxml/mainFrame/Main.fxml"); // ✅ Home
                        e.consume();
                    }
                });
            }
        });

        // 포커스 유지(ESC 안정적으로 잡기 위함)
        content.setFocusTraversable(true);
        content.requestFocus();

        // 허브 초기 화면은 비워둠(5-1)
        content.getChildren().clear();
    }

    // ───────────────── 버튼 핸들러(중앙만 교체) ─────────────────
    @FXML private void showMyDiary()    { setCenterSafe("/fxml/diary/my_diary/my-diary-view.fxml"); }
    @FXML private void showOurDiary()   { setCenterSafe("/fxml/diary/our_diary/our-diary-view.fxml"); }
    @FXML private void showBuddyDiary() { setCenterSafe("/fxml/diary/buddy_diary/buddy-diary-view.fxml"); }

    // ───────────────── 중앙 영역 교체 (안정판) ─────────────────
    private void setCenterSafe(String fxml) {
        try {
            URL url = getClass().getResource(fxml);
            System.out.println("[Hub] load center: " + fxml + " -> " + url);
            if (url == null) {
                alert("FXML not found:\n" + fxml);
                return;
            }
            Parent node = FXMLLoader.load(url);
            content.getChildren().setAll(node);
            content.requestFocus(); // ESC 포커스 유지
        } catch (Exception e) {
            e.printStackTrace();
            alert("Load failed:\n" + fxml + "\n" + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    // ───────────────── 씬 전체 교체(ESC/홈 복귀용) ─────────────────
    private void switchTo(String fxml) {
        try {
            URL url = getClass().getResource(fxml);
            System.out.println("[Hub] switchTo: " + fxml + " -> " + url);
            if (url == null) {
                alert("FXML not found:\n" + fxml);
                return;
            }
            Parent root = FXMLLoader.load(url);
            Stage st = (Stage) content.getScene().getWindow();
            Scene sc = new Scene(root, st.getWidth(), st.getHeight());
            URL css = getClass().getResource("/css/style.css");
            if (css != null) sc.getStylesheets().add(css.toExternalForm());
            st.setScene(sc);
        } catch (Exception e) {
            e.printStackTrace();
            alert("Scene switch failed:\n" + fxml + "\n" + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    // ───────────────── 공통 알림 ─────────────────
    private static void alert(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
