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

        // 첫 친구 선택
        if (!buddies.isEmpty()) selectBuddy(buddies.get(0).id());
    }

    // 1주 데이터 로드
    @FXML
    private void loadWeekData() {
        loadWeekData(null);
    }

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

    // 친구 아이템
    private Node buildBuddyItem(Buddy b) {
        HBox item = new HBox(10);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(10));

        // 크기 고정 → 이름 길이에 상관없이 버튼 크기 동일
        item.setPrefWidth(210);
        item.setMinWidth(210);
        item.setMaxWidth(210);

        // 기본 배경
        item.setStyle("-fx-background-color:#CBAFD1; -fx-background-radius:14;");
        item.setUserData(b.id());

        // 이름 (음표 제거)
        Label name = new Label(b.name());
        name.setStyle("-fx-font-size:16; -fx-font-weight:bold; -fx-text-fill:#141414;");
        name.setMaxWidth(150);
        name.setWrapText(false);

        item.getChildren().add(name);

        // 클릭 시 선택
        item.setOnMouseClicked(e -> selectBuddy(b.id()));
        return item;
    }

    private Node loadAvatar(String id) {
        // 필요 시 프로필 이미지 로드 로직
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
            Label l = new Label("🎵");
            l.setStyle("-fx-font-size:18;");
            return l;
        }
    }

    // 친구 선택
    private void selectBuddy(String buddyId) {
        this.selectedBuddyId = buddyId;

        for (Node n : buddyList.getChildren()) {
            boolean sel = Objects.equals(n.getUserData(), buddyId);
            if (sel) {
                n.setStyle("""
                    -fx-background-color:#CBAFD1;
                    -fx-background-radius:14;
                    -fx-border-color:#222;
                    -fx-border-width:2;
                    -fx-border-radius:14;
                """);
            } else {
                n.setStyle("-fx-background-color:#CBAFD1; -fx-background-radius:14;");
            }
        }

        List<DiaryEntry> entries = FAKE_DATA ? fakeEntriesFor(buddyId)
                                             : fetchEntriesFromDB(buddyId);
        renderEntriesGrid(entries);
    }

    /* ---------------- 우측: 2×2 카드 ---------------- */

    private void renderEntriesGrid(List<DiaryEntry> entries) {
        entriesGrid.getChildren().clear();

        int max = Math.min(entries.size(), 4); // 2×2
        for (int i = 0; i < max; i++) {
            int col = i % 2;
            int row = i / 2;
            entriesGrid.add(buildEntryCell(entries.get(i)), col, row);
        }
    }

private VBox buildEntryCell(DiaryEntry e) {
    VBox wrap = new VBox(8);

    // 날짜 라벨
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("M월 d일");
    Label date = new Label(e.date().format(fmt));
    date.setStyle("-fx-font-size:14; -fx-font-weight:bold; -fx-text-fill:#222;");

    // 카드 본문
    StackPane card = new StackPane();
    card.setPadding(new Insets(12));

    // 📌 카드 높이 고정 (길이와 무관)
    card.setMinHeight(160);
    card.setPrefHeight(160);
    card.setMaxHeight(160);

    card.setStyle("""
        -fx-background-color: white;
        -fx-background-radius: 16;
        -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 12, 0, 0, 4);
    """);

    // 접힌 모서리
    Pane corner = new Pane();
    corner.setPrefSize(26, 18);
    corner.setStyle("-fx-background-color:#E7E6EE; -fx-background-radius:0 16 0 16;");
    StackPane.setAlignment(corner, Pos.TOP_RIGHT);
    StackPane.setMargin(corner, new Insets(6, 6, 0, 0));

    // 본문 텍스트
    Label text = new Label(e.text());
    text.setWrapText(true);
    text.setStyle("-fx-font-size:13; -fx-text-fill:#333;");

    // 📌 텍스트 높이 제한
    text.setMaxHeight(120);

    card.getChildren().addAll(text, corner);

    // 📌 전체 wrap 높이 고정
    wrap.setMinHeight(190);
    wrap.setPrefHeight(190);
    wrap.setMaxHeight(190);

    wrap.getChildren().addAll(date, card);
    return wrap;
}


    /* ---------------- 더미 데이터 ---------------- */

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
