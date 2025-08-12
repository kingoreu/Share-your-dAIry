package com.share.dairy.controller;

import com.share.dairy.app.Router;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 5-3 OUR DIARY 화면 (카드형)
 * 변경 최소 원칙:
 * - Router 구조/네이밍은 기존 그대로 사용 (goHub() -> Router.go("DiaryHub"))
 * - DB 연동 전이라도 모양 확인 위해 FAKE_DATA 제공 (스위치 하나로 끄고 켤 수 있음)
 * - ESC로 허브 복귀: 기존 UX 유지
 */
public class OurDiaryController {

    @FXML private FlowPane cardsFlow; // FXML의 카드 컨테이너 (FlowPane)

    // 🔧 임시 데이터 스위치: 디자인 확인 끝나면 false + DB 메서드 연결
    private static final boolean FAKE_DATA = true;

    @FXML
    public void initialize() {
        // ESC → 허브(5-1) 복귀 (씬 준비 후 이벤트 필터 등록)
        cardsFlow.sceneProperty().addListener((obs, oldS, s) -> {
            if (s != null) {
                s.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                    if (e.getCode() == KeyCode.ESCAPE) {
                        goHub();
                        e.consume();
                    }
                });
            }
        });

        // 초기 카드 렌더링: 더미 또는 DB
        List<DiaryCardData> data = FAKE_DATA ? fakeCards() : fetchFromDB();
        renderCards(data);
    }

    /* -------------------- 네비게이션 (기존 라우팅 유지) -------------------- */

    // 홈(허브)으로 복귀 — 기존 Router 네이밍 재사용
    @FXML public void goHub() {
        Router.go("DiaryHub"); // ✅ 팀 전체 라우팅과 일관
    }

    // 좌측 사이드: My / Our / Buddy — 지금은 Our 화면 유지, 나머지는 허브로만 연결(변경 최소)
    @FXML public void goMyDiary()    { Router.go("DiaryHub"); }    // 팀원이 만든 5-2 있으면 거기로 라우팅만 교체
    @FXML public void goOurDiary()   { renderCards(FAKE_DATA ? fakeCards() : fetchFromDB()); } // 현재 화면 새로고침
    @FXML public void goBuddyDiary() { Router.go("DiaryHub"); }     // 추후 5-4 연결

    // 우하단 NEW 버튼
    @FXML public void onNew() {
        // TODO: 공유 일기장 생성 화면 라우팅
        new Alert(Alert.AlertType.INFORMATION, "New! 공유 일기장 생성은 추후 연결").show();
    }

    /* -------------------- 카드 렌더링 -------------------- */

    // 카드 목록을 그리드에 채우는 메서드
    private void renderCards(List<DiaryCardData> list) {
        cardsFlow.getChildren().clear();
        for (DiaryCardData d : list) cardsFlow.getChildren().add(buildCard(d));
    }

    // 개별 카드 UI 구성 (디자인 스케치 맞춤)
    private Node buildCard(DiaryCardData d) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(16));
        card.setPrefWidth(200); // 카드 폭: 200px (FlowPane에서 자동 줄바꿈)
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius:18;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 12, 0, 0, 4);"
        );

        // 제목
        Label title = new Label(d.title);
        title.setStyle("-fx-font-size:18; -fx-font-weight:bold;");

        // 멤버 리스트 (아이콘 텍스트로 단순화 — 나중에 SVG/이미지로 교체 가능)
        VBox membersBox = new VBox(6);
        for (String m : d.members) {
            Label row = new Label("👤 " + m);
            row.setStyle("-fx-font-size:13;");
            membersBox.getChildren().add(row);
        }

        // 시작 날짜
        Label start = new Label("start " + d.startDate);
        start.setStyle("-fx-font-size:12; -fx-text-fill:#666;");

        // 클릭 시 동작 (상세 화면 연결은 추후)
        card.setOnMouseClicked(e ->
            new Alert(Alert.AlertType.INFORMATION, d.title + " 열기(상세는 추후 연결)").show()
        );

        card.getChildren().addAll(title, membersBox, start);
        return card;
    }

    /* -------------------- 데이터 소스 -------------------- */

    // 임시 카드 데이터 (디자인 확인용)
    private List<DiaryCardData> fakeCards() {
        List<DiaryCardData> list = new ArrayList<>();
        list.add(new DiaryCardData("TITLE 1", Arrays.asList("Member1", "Member2", "Member3"), LocalDate.now().minusDays(15)));
        list.add(new DiaryCardData("TITLE 2", Arrays.asList("Member1", "Member2"), LocalDate.now().minusDays(30)));
        list.add(new DiaryCardData("TITLE 3", Arrays.asList("Member1", "Member2"), LocalDate.now().minusDays(50)));
        return list;
    }

    // TODO: DB에서 OUR DIARY 목록 조회 → 카드 데이터로 변환
    private List<DiaryCardData> fetchFromDB() {
        // 팀 DB 규약에 맞춰 추후 구현 (현재는 빈 목록 반환)
        return new ArrayList<>();
    }

    /* -------------------- 내부 모델 -------------------- */
    private static class DiaryCardData {
        final String title;
        final List<String> members;
        final LocalDate startDate;
        DiaryCardData(String title, List<String> members, LocalDate startDate) {
            this.title = title; this.members = members; this.startDate = startDate;
        }
    }
}
