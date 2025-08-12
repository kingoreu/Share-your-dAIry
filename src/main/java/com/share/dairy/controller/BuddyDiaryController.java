package com.share.dairy.controller;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class BuddyDiaryController {

    @FXML private VBox buddyList;       // 좌측 친구 리스트 컨테이너
    @FXML private GridPane entriesGrid; // 우측 2×2 카드 그리드

    private static final boolean FAKE_DATA = true;
    private String selectedBuddyId;
    
    // ---- 2×2 셀을 고정해 두고 라벨만 교체하는 구조 ----
    private final Label[] dateLabels = new Label[4];
    private final Label[] textLabels = new Label[4];
    private final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("M월 d일");
    private boolean gridInitialized = false;

    @FXML
    public void initialize() {
        // ESC 키 허브로 넘기기
        entriesGrid.sceneProperty().addListener((obs, o, s) -> {
            if (s != null) {
                s.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                    if (e.getCode() == KeyCode.ESCAPE) e.consume();
                });
            }
        });

        // 친구 목록 렌더링
        List<Buddy> buddies = FAKE_DATA ? fakeBuddies() : fetchBuddiesFromDB();
        renderBuddyList(buddies);

        // 2×2 셀 고정 생성 (초기 1회)
        ensureGridBuilt();

        // 첫 친구 선택
        if (!buddies.isEmpty()) selectBuddy(buddies.get(0).id());
    }

    // 1주 데이터 로드 (필요 시 FXML onAction에서 호출)
    @FXML private void loadWeekData() { loadWeekData(null); }
    @FXML
    private void loadWeekData(javafx.event.ActionEvent e) {
        if (selectedBuddyId == null || selectedBuddyId.isBlank()) return;
        var entries = FAKE_DATA ? fakeEntriesFor(selectedBuddyId)
                                : fetchEntriesFromDB(selectedBuddyId);
        renderEntriesGrid(entries);
    }

    /* ---------------- 좌측: 친구 리스트 ---------------- */

    private void renderBuddyList(List<Buddy> buddies) {
        buddyList.getChildren().clear();
        for (Buddy b : buddies) buddyList.getChildren().add(buildBuddyItem(b));
    }

    // 친구 아이템(폭 고정: 이름 길이에 상관없이 동일)
    private Node buildBuddyItem(Buddy b) {
        HBox item = new HBox(10);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(10));

        item.setPrefWidth(210);
        item.setMinWidth(210);
        item.setMaxWidth(210);

        item.setStyle("-fx-background-color:#CBAFD1; -fx-background-radius:14;");
        item.setUserData(b.id());

        Label name = new Label(b.name());
        name.setStyle("-fx-font-size:16; -fx-font-weight:bold; -fx-text-fill:#141414;");
        name.setMaxWidth(150);
        name.setWrapText(false);

        item.getChildren().addAll(name);
        item.setOnMouseClicked(e -> selectBuddy(b.id()));
        return item;
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
            iv.setClip(new javafx.scene.shape.Rectangle(36, 36) {{
                setArcWidth(36); setArcHeight(36);
            }});
            return iv;
        } catch (Exception ignore) {
            return new Label(""); // 이미지 없을 땐 아무 것도 표시 안 함
        }
    }

    // 친구 선택
    private void selectBuddy(String buddyId) {
        this.selectedBuddyId = buddyId;

        // 좌측 선택 스타일 토글
       for (Node n : buddyList.getChildren()) {
    boolean sel = Objects.equals(n.getUserData(), buddyId);
    if (sel) {
        n.setStyle("""
            -fx-background-radius:14;
            -fx-background-insets: 0, 2;          /* 2px 안쪽 레이어 */
            -fx-background-color: #222, #CBAFD1;  /* 바깥: 검정, 안쪽: 보라 */
        """);
    } else {
        n.setStyle("-fx-background-color:#CBAFD1; -fx-background-radius:14;");
    }
}

        // 우측 내용 갱신(셀은 고정, 텍스트만 교체)
        List<DiaryEntry> entries = FAKE_DATA ? fakeEntriesFor(buddyId)
                                             : fetchEntriesFromDB(buddyId);
        renderEntriesGrid(entries);
    }

    /* ---------------- 우측: 2×2 카드 (고정 셀) ---------------- */

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

    // 고정 셀 하나 만들기(크기 고정 + 모서리 장식)
    private VBox createCell(int idx) {
        VBox wrap = new VBox(8);

        Label date = new Label();
        date.setStyle("-fx-font-size:14; -fx-font-weight:bold; -fx-text-fill:#222;");
        dateLabels[idx] = date;

        StackPane card = new StackPane();
        card.setPadding(new Insets(12));
        // 카드 높이 고정 → 클릭해도 흔들리지 않음
        card.setMinHeight(160);
        card.setPrefHeight(160);
        card.setMaxHeight(160);
        card.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 16;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 12, 0, 0, 4);
        """);

        Label body = new Label();
        body.setWrapText(true);
        body.setMaxHeight(120);
        body.setStyle("-fx-font-size:13; -fx-text-fill:#333;");
        textLabels[idx] = body;

        Pane corner = new Pane();
        corner.setPrefSize(26, 18);
        corner.setStyle("-fx-background-color:#E7E6EE; -fx-background-radius:0 16 0 16;");
        StackPane.setAlignment(corner, Pos.TOP_RIGHT);
        StackPane.setMargin(corner, new Insets(6, 6, 0, 0));

        card.getChildren().addAll(body, corner);

        // 셀 전체 높이 고정
        wrap.setMinHeight(190);
        wrap.setPrefHeight(190);
        wrap.setMaxHeight(190);
        wrap.getChildren().addAll(date, card);

        return wrap;
    }

    // 내용만 교체해서 갱신
    private void renderEntriesGrid(List<DiaryEntry> entries) {
        ensureGridBuilt();

        for (int i = 0; i < 4; i++) {
            if (i < entries.size()) {
                DiaryEntry e = entries.get(i);
                dateLabels[i].setText(e.date().format(DAY_FMT));
                textLabels[i].setText(e.text());
            } else {
                dateLabels[i].setText("");
                textLabels[i].setText("");
            }
        }
    }

    /* ---------------- 더미 데이터 ---------------- */

   // 스크롤 테스트용: 버디 많이 넣기
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

    private List<Buddy> fetchBuddiesFromDB() { return Collections.emptyList(); }
    private List<DiaryEntry> fetchEntriesFromDB(String buddyId) { return Collections.emptyList(); }

    /* ---------------- 내부 모델 ---------------- */
    private record Buddy(String id, String name) {}
    private record DiaryEntry(LocalDate date, String text) {}
}
