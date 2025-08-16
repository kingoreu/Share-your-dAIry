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
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;   // 스페이서에 필요
import javafx.geometry.Insets;        // 간격/패딩 조정에 필요

import java.net.URL;

public class DiaryHubController {

    @FXML private StackPane content;       // 허브 전체 루트 (키 필터 붙일 곳)
    @FXML private StackPane centerHolder;  // 중앙 교체 대상 컨테이너
    @FXML private Button shellFab;         // 허브 셸(fxml)에 숨겨둔 FAB(없으면 null)
    @FXML private VBox sidebar;            // 왼쪽 버튼 3개 담는 VBox (fx:id 없더라도 백업 룩업 적용)

    // 중앙에 띄울 화면 경로들
    private static final String FXML_HUB   = "/fxml/diary/diary_hub/hub-list.fxml";
    private static final String FXML_MY    = "/fxml/diary/my_diary/my-diary-view.fxml";
    private static final String FXML_OUR   = "/fxml/diary/our_diary/our-diary-view.fxml";
    private static final String FXML_BUDDY = "/fxml/diary/buddy_diary/buddy-diary-view.fxml";

    // ESC → Home.fxml
    private final javafx.event.EventHandler<KeyEvent> escFilter = e -> {
        if (e.getCode() == KeyCode.ESCAPE) {
            goHome();
            e.consume();
        }
    };

    // 중복 로딩 방지
    private String activeFxml = null;

    public void initialize() {
        // ESC 필터 장착
        content.sceneProperty().addListener((obs, oldScene, scene) -> {
            if (oldScene != null) oldScene.removeEventFilter(KeyEvent.KEY_PRESSED, escFilter);
            if (scene != null)    scene.addEventFilter(KeyEvent.KEY_PRESSED, escFilter);
        });

        // FAB 기본 숨김
        if (shellFab != null) {
            shellFab.setVisible(false);
            shellFab.setManaged(false);
        }

        // 런타임 준비 후
        javafx.application.Platform.runLater(() -> {
            // FAB fx:id 불일치 대비: 런타임에 찾아 연결
            if (shellFab == null) {
                var n = content.lookup("#fabNew");
                if (n instanceof Button b) shellFab = b;
            }
            boolean onHub = FXML_HUB.equals(activeFxml) || activeFxml == null;
            showFab(onHub);

            // 시작 화면
            showDiaryHub();

            // 포커스
            content.setFocusTraversable(true);
            content.requestFocus();

            // ✅ 왼쪽 버튼 3개만 조정(아래로 내림 + 구분선 제거 + 간격/패딩)
            adjustSidebarButtons();
        });
    }

    /* 좌/상단 버튼 핸들러 — 중앙만 교체 */
    @FXML private void showDiaryHub()   { setCenter(FXML_HUB); }
    @FXML private void showMyDiary()    { setCenter(FXML_MY); }
    @FXML private void showOurDiary()   { setCenter(FXML_OUR); }
    @FXML private void showBuddyDiary() { setCenter(FXML_BUDDY); }

    // FAB 토글만 담당
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

    /** 필요 시 사용: 사이드바의 "MY DIARY" 버튼을 리스트 화면으로 고정 */
    private void forceSidebarMyDiaryToHub() {
        javafx.application.Platform.runLater(() -> {
            if (content.getScene() == null) return;
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
        if (shellFab != null) {
            shellFab.setVisible(showOnHub);
            shellFab.setManaged(showOnHub);
            shellFab.setOnAction(showOnHub ? e -> showMyDiary() : null);
        }
        if (content.getScene() != null) {
            content.getScene().getRoot().lookupAll(".fab").forEach(n -> {
                if (n != shellFab) {
                    n.setVisible(false);
                    n.setManaged(false);
                    if (n.getParent() instanceof javafx.scene.layout.Pane p) {
                        p.getChildren().remove(n);
                    }
                }
            });
        }
    }

    /* ================== 여기만 '추가/수정' ================== */
    private void adjustSidebarButtons() {
        try {
            // 1) Sidebar VBox 확보: @FXML 주입 실패 시 styleClass로 룩업
            VBox v = this.sidebar;
            if (v == null) {
                if (content == null || content.getScene() == null) return;
                var node = content.getScene().getRoot().lookup(".sidebar");
                if (node instanceof VBox) v = (VBox) node; else return;
            }

            // 2) BUDDY DIARY 아래 얇은 바 제거
            v.getChildren().removeIf(n -> n instanceof javafx.scene.control.Separator);

            // 3) 버튼 묶음을 아래로 — 맨 위에 스페이서 1개(중복 방지)
            if (v.getChildren().isEmpty() || !(v.getChildren().get(0) instanceof Region)) {
                Region spacer = new Region();
                VBox.setVgrow(spacer, Priority.ALWAYS);
                v.getChildren().add(0, spacer);
            }

            // 4) 간격/패딩 미세 조정
            v.setSpacing(32);
            v.setPadding(new Insets(12, 12, 4, 12));
        } catch (Exception ignore) { /* 다른 기능 영향 없도록 조용히 */ }
    }
}
