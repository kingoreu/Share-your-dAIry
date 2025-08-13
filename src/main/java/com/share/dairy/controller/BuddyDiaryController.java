package com.share.dairy.controller;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class BuddyDiaryController {

    /* ==== FXML 바인딩 ==== */
    @FXML private GridPane entriesGrid; // 우측 2×2 그리드 (fx:id="entriesGrid")
    @FXML private VBox buddyList;       // 좌측 버디 리스트 컨테이너 (fx:id="buddyList")

    /* ==== 상태 ==== */
    private String selectedBuddyId;
    private boolean gridInitialized = false;

    // 우측 4칸의 날짜/텍스트 라벨을 고정으로 들고 있음
    private final Label[] dateLabels = new Label[4];
    private final Label[] textLabels  = new Label[4];

    private static final boolean FAKE_DATA = true;
    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    /* ================= 라이프사이클 ================= */

    @FXML
    public void initialize() {
        // ESC 키 처리 + 씬 로드 후 버튼 크기 고정(한 번만)
        entriesGrid.sceneProperty().addListener((obs, o, s) -> {
            if (s != null) {
                s.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                    if (e.getCode() == KeyCode.ESCAPE) e.consume();
                });
                // 🔒 버튼 크기 들쑥날쑥 방지: 최초 레이아웃 후 모든 버튼 크기 고정
                freezeAllButtonSizesOnce(s);
            }
        });

        // 우측 2×2 그리드 레이아웃
        entriesGrid.setHgap(16);
        entriesGrid.setVgap(16);
        entriesGrid.setPadding(new Insets(16));
        setupGridConstraints(); // 2열 50/50 설정
        setupRowConstraints();  // 2행 50/50 설정

        // 그리드 자체가 부모 영역을 꽉 채우도록 (원래 크기 유지)
        if (entriesGrid.getParent() instanceof Region prGrid) {
            entriesGrid.prefWidthProperty().bind(prGrid.widthProperty());
            entriesGrid.prefHeightProperty().bind(prGrid.heightProperty());
        } else {
            entriesGrid.parentProperty().addListener((o, oldP, p) -> {
                if (p instanceof Region prGrid2) {
                    entriesGrid.prefWidthProperty().bind(prGrid2.widthProperty());
                    entriesGrid.prefHeightProperty().bind(prGrid2.heightProperty());
                }
            });
        }

        // === 회색 배경 패널 세로 확장 (그리드 크기는 건드리지 않음) ===
        javafx.application.Platform.runLater(() -> {
            Region rightPanel = (Region) entriesGrid.getParent();

            if (rightPanel.getParent() instanceof Region pr) {
                rightPanel.prefHeightProperty().bind(pr.heightProperty());
            }
            if (rightPanel.getParent() instanceof HBox row) {
                row.setFillHeight(true);
            }

            Insets pad = rightPanel.getPadding() == null ? Insets.EMPTY : rightPanel.getPadding();
            rightPanel.setPadding(new Insets(40, pad.getRight(), 40, pad.getLeft()));
        });

        // 좌측 버디 리스트
        buddyList.setAlignment(Pos.TOP_CENTER);
        buddyList.setFillWidth(true);
        buddyList.setSpacing(12);
        buddyList.setPadding(new Insets(0, 10, 0, 10));
        buddyList.setClip(null);
        if (buddyList.getParent() instanceof Region pr) {
            pr.setPadding(Insets.EMPTY);
        }

        // 데이터 렌더링
        List<Buddy> buddies = FAKE_DATA ? fakeBuddies() : fetchBuddiesFromDB();
        renderBuddyList(buddies);

        // 2×2 셀 고정 생성
        ensureGridBuilt();

        // 첫 친구 선택
        if (!buddies.isEmpty()) {
            selectBuddy(buddies.get(0).id());
        }
    }

    /* === 버튼 들쑥날쑥 방지: 씬의 모든 Button을 최초 CSS/레이아웃 이후 고정 === */
    private void freezeAllButtonSizesOnce(Scene scene) {
        javafx.application.Platform.runLater(() -> {
            Parent root = scene.getRoot();
            root.applyCss();
            root.layout();

            for (Node n : root.lookupAll(".button")) {
                if (n instanceof Button b) {
                    double w = b.prefWidth(-1);
                    double h = b.prefHeight(-1);
                    b.setMinSize(w, h);
                    b.setPrefSize(w, h);
                    b.setMaxSize(w, h);
                }
            }
        });
    }

    /* ================ 주간 데이터 로드 (옵션) ================ */

    @FXML private void loadWeekData() { loadWeekData(null); }

    @FXML
    private void loadWeekData(javafx.event.ActionEvent e) {
        if (selectedBuddyId == null || selectedBuddyId.isBlank()) return;
        var entries = FAKE_DATA ? fakeEntriesFor(selectedBuddyId)
                                : fetchEntriesFromDB(selectedBuddyId);
        renderEntriesGrid(entries);
    }

    /* ================= 좌측: 친구 리스트 ================= */

    private void renderBuddyList(List<Buddy> buddies) {
        buddyList.getChildren().clear();
        for (Buddy b : buddies) buddyList.getChildren().add(buildBuddyItem(b));
    }

    // 친구 아이템(가운데 정렬, 좌우 여백 대칭, 세로 키움 OK)
    private Node buildBuddyItem(Buddy b) {
        HBox card = new HBox(12);
        card.setAlignment(Pos.CENTER_LEFT);

        // 세로 크기(이미지 넣을 대비)
        card.setPadding(new Insets(16));
        card.setMinHeight(64);
        card.setPrefHeight(68);
        card.setMaxHeight(80);

        // 폭은 slot에 맞춰 자동
        card.setMinWidth(0);
        card.setPrefWidth(Region.USE_COMPUTED_SIZE);
        card.setMaxWidth(Double.MAX_VALUE);

        final String BASE =
            "-fx-background-color:#CBAFD1; -fx-background-radius:14;";
        final String HILITE =
            "-fx-background-color:white; -fx-background-radius:14; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 8, 0, 0, 3);";
        card.setStyle(BASE);

        Label nameLabel = new Label(b.name());
        nameLabel.setStyle("-fx-font-size:17; -fx-font-weight:bold; -fx-text-fill:#141414;");
        nameLabel.setWrapText(false);
        card.getChildren().add(nameLabel);

        double GUTTER = buddyList.getPadding().getLeft();   // buddyList 패딩과 동기화
        StackPane slot = new StackPane(card);
        slot.setAlignment(Pos.CENTER);
        slot.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(slot, new Insets(8, 0, 8, 0));
        StackPane.setMargin(card, new Insets(0, GUTTER, 0, GUTTER));

        // 카드 폭 = slot 폭 - 좌우 마진*2 (대칭)
        card.maxWidthProperty().bind(slot.widthProperty().subtract(GUTTER * 2));

        // 이벤트(스타일은 card에 적용)
        slot.setUserData(b.id());
        slot.setOnMouseClicked(e -> selectBuddy(b.id()));
        slot.setOnMouseEntered(e -> card.setStyle(HILITE));
        slot.setOnMouseExited(e -> {
            boolean sel = Objects.equals(slot.getUserData(), selectedBuddyId);
            card.setStyle(sel ? HILITE : BASE);
        });

        return slot;
    }

    // (옵션) 아바타 로더 — 현재는 사용 안 하지만 남겨둠
    private Node loadAvatar(String id) {
        try {
            Image img = new Image(Objects.requireNonNullElse(
                getClass().getResourceAsStream("/images/buddy/" + id + ".png"),
                getClass().getResourceAsStream("/images/buddy/_fallback.png")
            ));
            ImageView iv = new ImageView(img);
            iv.setFitWidth(36); iv.setFitHeight(36);
            Rectangle clip = new Rectangle(36, 36);
            clip.setArcWidth(36); clip.setArcHeight(36);
            iv.setClip(clip);
            return iv;
        } catch (Exception ignore) {
            return new Label("");
        }
    }

    // 친구 선택 → 좌/우 동기화
    private void selectBuddy(String buddyId) {
        this.selectedBuddyId = buddyId;

        final String BASE =
            "-fx-background-color:#CBAFD1; " +
            "-fx-background-radius:14;";
        final String HILITE =
            "-fx-background-color:white; " +
            "-fx-background-radius:14; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 8, 0.0, 0, 3);";

        for (Node slot : buddyList.getChildren()) {
            boolean sel = Objects.equals(slot.getUserData(), buddyId);
            Node card = slot;
            if (slot instanceof Pane p && !p.getChildren().isEmpty()) {
                card = p.getChildren().get(0);
            }
            card.setStyle(sel ? HILITE : BASE);
        }

        // 우측 2×2 갱신
        var entries = FAKE_DATA ? fakeEntriesFor(buddyId) : fetchEntriesFromDB(buddyId);
        renderEntriesGrid(entries);
    }

    /* ================ 우측: 2×2 카드 (고정 셀) ================ */

    // 처음 한 번만 4개의 셀을 만들어 GridPane에 배치
    private void ensureGridBuilt() {
        if (gridInitialized) return;

        entriesGrid.getChildren().clear();
        for (int i = 0; i < 4; i++) {
            VBox cell = createCell(i);
            GridPane.setHgrow(cell, Priority.ALWAYS);
            GridPane.setVgrow(cell, Priority.ALWAYS);
            entriesGrid.add(cell, i % 2, i / 2); // 0,1 / 2,3
        }
        gridInitialized = true;
    }

    // 고정 셀 하나(날짜는 카드 바깥 위쪽, 카드는 행을 넉넉히 채움)
    private VBox createCell(int idx) {
        final double GAP_BELOW_DATE = 6;  // 날짜와 카드 사이 간격

        VBox wrap = new VBox(GAP_BELOW_DATE);
        wrap.setFillWidth(true);
        wrap.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(wrap, Priority.ALWAYS);

        // 날짜 라벨 (카드 바깥)
        Label date = new Label();
        date.setStyle("-fx-font-size:15; -fx-font-weight:bold; -fx-text-fill:#222;");
        dateLabels[idx] = date;

        // 카드
        StackPane card = new StackPane();
        card.setStyle(
            "-fx-background-color:white;" +
            "-fx-background-radius:16;" +
            "-fx-effect:dropshadow(gaussian, rgba(0,0,0,0.14), 14, 0, 0, 4);"
        );
        card.setPadding(new Insets(14));
        VBox.setVgrow(card, Priority.ALWAYS);

        // 둥근 모서리 clip
        Rectangle clip = new Rectangle();
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        clip.widthProperty().bind(card.widthProperty());
        clip.heightProperty().bind(card.heightProperty());
        card.setClip(clip);

        // ✅ 카드 높이는 "행의 가용 높이"에 맞춰 자연스럽게 커지도록 (원래 크기 유지 목적)
        //    별도의 고정 비율 바인딩 제거하여 부모(행 50%)를 꽉 채우는 기존 레이아웃으로 복원
        //    (만약 이전에 height=width*0.90 바인딩을 넣었었다면 아래 세 줄은 삭제/주석 유지)
        // card.minHeightProperty().bind(card.widthProperty().multiply(0.90));
        // card.prefHeightProperty().bind(card.widthProperty().multiply(0.90));
        // card.maxHeightProperty().bind(card.widthProperty().multiply(0.90));

        // 내부 placeholder (연보라 영역)
        Region placeholder = new Region();
        placeholder.setStyle("-fx-background-color:#E7E6EE; -fx-background-radius:14;");
        placeholder.maxWidthProperty().bind(card.widthProperty().subtract(28));   // 패딩 14*2
        placeholder.maxHeightProperty().bind(card.heightProperty().subtract(28)); // 패딩 14*2
        StackPane.setAlignment(placeholder, Pos.CENTER);
        card.getChildren().add(placeholder);

        // 텍스트 라벨은 데이터만 보관(표시는 안 함)
        Label body = new Label();
        body.setManaged(false);
        body.setVisible(false);
        body.setWrapText(true);
        textLabels[idx] = body;

        // 우상단 모서리 장식
        Pane corner = new Pane();
        corner.setPrefSize(30, 20);
        corner.setStyle("-fx-background-color:#E7E6EE; -fx-background-radius:0 16 0 16;");
        StackPane.setAlignment(corner, Pos.TOP_RIGHT);
        StackPane.setMargin(corner, new Insets(8, 8, 0, 0));
        card.getChildren().add(corner);

        // 날짜(바깥) + 카드
        wrap.getChildren().addAll(date, card);
        return wrap;
    }

    // 내용 교체: 날짜만 보이게, 본문 텍스트는 숨김 유지
    private void renderEntriesGrid(List<DiaryEntry> entries) {
        ensureGridBuilt();
        for (int i = 0; i < 4; i++) {
            if (i < entries.size()) {
                DiaryEntry e = entries.get(i);
                dateLabels[i].setText(e.date().format(DAY_FMT));
            } else {
                dateLabels[i].setText("");
            }
        }
    }

    /* ================= 더미 데이터 ================= */

    private List<Buddy> fakeBuddies() {
        return List.of(
            new Buddy("kk",    "K.K"),
            new Buddy("naki",  "NaKi"),
            new Buddy("guide", "Guide"),
            new Buddy("kk2",   "K.K"),
            new Buddy("kk3",   "K.K")
        );
    }

    private List<DiaryEntry> fakeEntriesFor(String buddyId) {
        String base = switch (buddyId) {
            case "kk"    -> "소리 메모와 곡 아이디어 정리 중.";
            case "naki"  -> "UI 스케치/피드백 정리본.";
            case "guide" -> "가이드 문서 초안 업데이트.";
            default      -> "하루 기록 메모.";
        };
        return List.of(
            new DiaryEntry(LocalDate.now().minusDays(3), base + " #1"),
            new DiaryEntry(LocalDate.now().minusDays(2), base + " #2"),
            new DiaryEntry(LocalDate.now().minusDays(1), base + " #3"),
            new DiaryEntry(LocalDate.now(),              base + " #4")
        );
    }

    /** GridPane의 2개 열을 화면 폭의 50%/50%로 고정 */
    private void setupGridConstraints() {
        entriesGrid.getColumnConstraints().clear();

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(50);
        c1.setHgrow(Priority.ALWAYS);

        ColumnConstraints c2 = new ColumnConstraints();
        c2.setPercentWidth(50);
        c2.setHgrow(Priority.ALWAYS);

        entriesGrid.getColumnConstraints().addAll(c1, c2);
    }

    /** GridPane의 2개 행을 화면 높이의 50%/50%로 고정 */
    private void setupRowConstraints() {
        entriesGrid.getRowConstraints().clear();

        RowConstraints r1 = new RowConstraints();
        r1.setPercentHeight(50);
        r1.setVgrow(Priority.ALWAYS);

        RowConstraints r2 = new RowConstraints();
        r2.setPercentHeight(50);
        r2.setVgrow(Priority.ALWAYS);

        entriesGrid.getRowConstraints().addAll(r1, r2);
    }

    private List<Buddy> fetchBuddiesFromDB() { return Collections.emptyList(); }
    private List<DiaryEntry> fetchEntriesFromDB(String buddyId) { return Collections.emptyList(); }

    /* ================= 내부 모델 ================= */
    private record Buddy(String id, String name) {}
    private record DiaryEntry(LocalDate date, String text) {}
}
