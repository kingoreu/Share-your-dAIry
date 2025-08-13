package com.share.dairy.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import java.net.URL;

public class DiaryHubController {

    @FXML private StackPane content;       // 허브 전체 루트 (키 필터 붙일 곳)
    @FXML private StackPane centerHolder;  // 중앙 교체 대상 컨테이너
    @FXML private Button shellFab;         // 허브 셸(fxml)에 숨겨둔 FAB(없으면 null)

    // 중앙에 띄울 화면 경로들
    private static final String FXML_HUB   = "/fxml/diary/diary_hub/hub-list.fxml";
    private static final String FXML_MY    = "/fxml/diary/my_diary/my-diary-view.fxml";
    private static final String FXML_OUR   = "/fxml/diary/our_diary/our-diary-view.fxml";
    private static final String FXML_BUDDY = "/fxml/diary/buddy_diary/buddy-diary-view.fxml";

    // ✅ ESC → Home.fxml 로 즉시 전환 (Router 안 씀)
    private final javafx.event.EventHandler<KeyEvent> escFilter = e -> {
        if (e.getCode() == KeyCode.ESCAPE) {
            goHome();
            e.consume();
        }
    };

    // 중복 로딩 방지
    private String activeFxml = null;

    @FXML
public void initialize() {
    // (기존 코드 유지)
    content.sceneProperty().addListener((obs, oldScene, scene) -> {
        if (oldScene != null) oldScene.removeEventFilter(KeyEvent.KEY_PRESSED, escFilter);
        if (scene != null)    scene.addEventFilter(KeyEvent.KEY_PRESSED, escFilter);
    });

    // 처음에는 숨김(허브 리스트에서만 보이게 할 것이므로)
    if (shellFab != null) {
        shellFab.setVisible(false);
        shellFab.setManaged(false);
    }

    // ✅ fx:id 불일치 대비: 실제 FAB를 런타임에 찾아서 연결
    javafx.application.Platform.runLater(() -> {
        if (shellFab == null) {
            var n = content.lookup("#fabNew"); // FXML에 fabNew로 돼 있어도 잡힘
            if (n instanceof Button b) shellFab = b;
        }
        // 현재 화면 상태에 맞춰 보이기/숨기기 재적용
        boolean onHub = FXML_HUB.equals(activeFxml) || activeFxml == null;
        showFab(onHub);
    });

    // 시작 화면
    showDiaryHub();

    content.setFocusTraversable(true);
    content.requestFocus();
    }


    /* 좌/상단 버튼 핸들러 — 중앙만 교체 */
    @FXML private void showDiaryHub()   { setCenter(FXML_HUB); }
    @FXML private void showMyDiary()    { setCenter(FXML_MY); }
    @FXML private void showOurDiary()   { setCenter(FXML_OUR); }
    @FXML private void showBuddyDiary() { setCenter(FXML_BUDDY); }

    // ✅ FAB 토글만 담당(제거하지 않음: 사라지는 문제 방지)
    private void showFab(boolean show) {
    if (shellFab != null) {
        shellFab.setVisible(show);
        shellFab.setManaged(show);
        shellFab.setOnAction(show ? e -> showMyDiary() : null); // 허브 리스트에서만 동작
    }
    }

    private void setCenter(String fxml) {
    try {
        if (fxml != null && fxml.equals(activeFxml) && !centerHolder.getChildren().isEmpty()) {
            content.requestFocus();
            return;
        }

        URL url = getClass().getResource(fxml);
        if (url == null) throw new IllegalStateException("FXML not found: " + fxml);

        Parent node = FXMLLoader.load(url);
        centerHolder.getChildren().setAll(node);

        if (node instanceof Region r) {
            r.prefWidthProperty().bind(centerHolder.widthProperty());
            r.prefHeightProperty().bind(centerHolder.heightProperty());
        }

        activeFxml = fxml;

        // ✅ 허브 리스트일 때만 FAB 보이게
        boolean onHub = FXML_HUB.equals(fxml);
        showFab(onHub);

        content.requestFocus();
    } catch (Exception e) {
        e.printStackTrace();
    }
    }
    

    /** 홈 화면(Main.fxml)로 즉시 복귀 */
    private void goHome() {
        try {
            Parent home = FXMLLoader.load(getClass().getResource("/fxml/mainFrame/Main.fxml"));
            content.getScene().setRoot(home);
        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "홈으로 이동 실패").showAndWait();
        }
    }

/** 사이드바의 "MY DIARY" 버튼을 무조건 리스트 화면으로 고정 */
private void forceSidebarMyDiaryToHub() {
    javafx.application.Platform.runLater(() -> {
        if (content.getScene() == null) return;
        // 화면 어디에 있든 텍스트가 "MY DIARY" 인 버튼 찾아서 핸들러 덮어쓰기
        content.getScene().getRoot().lookupAll(".button").forEach(n -> {
            if (n instanceof javafx.scene.control.Button b) {
                if ("MY DIARY".equals(b.getText())) {
                    b.setOnAction(e -> showDiaryHub());
                }
            }
        });
    });
}

// 중복으로 떠있는 FAB(.fab)을 하나만 남기고 정리
private void dedupeFab(boolean showOnHub) {
    // 내가 관리하는 FAB 보이기/숨기기
    if (shellFab != null) {
        shellFab.setVisible(showOnHub);
        shellFab.setManaged(showOnHub);
        shellFab.setOnAction(showOnHub ? e -> showMyDiary() : null);
    }
    // 씬에 붙은 다른 .fab 스타일의 버튼들은 제거
    if (content.getScene() != null) {
        content.getScene().getRoot().lookupAll(".fab").forEach(n -> {
            if (n != shellFab) {
                n.setVisible(false);
                n.setManaged(false);
                if (n.getParent() instanceof javafx.scene.layout.Pane p) {
                    p.getChildren().remove(n); // 실제로 제거
                }
            }
        });
    }
}
}
