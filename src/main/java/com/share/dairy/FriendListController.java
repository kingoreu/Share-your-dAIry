package com.share.dairy;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.Node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FriendListController {

    @FXML private GridPane friendGrid;
    @FXML private Button selectButton;
    @FXML private Button deleteButton;
    @FXML private ScrollPane scrollPane;

    // 예시용 데이터 모델
    private List<Friend> friends = new ArrayList<>();
    private HBox selectedBox = null;

    @FXML
    public void initialize() {
        // 실제론 DAO나 서비스에서 불러오기
        // 경로를 /com/share/images/... 형식으로 지정
        friends.add(new Friend("K.K",   "/com/share/images/kk.png"));
        friends.add(new Friend("Naki",  "/com/share/images/naki.png"));
        friends.add(new Friend("Guide", "/com/share/images/guide.png"));
        populateGrid();
    }

    private void populateGrid() {
        friendGrid.getChildren().clear();
        int col = 0, row = 0;

        for (Friend f : friends) {
            HBox entry = createFriendEntry(f);
            friendGrid.add(entry, col, row);
            col++;
            if (col > 1) {
                col = 0;
                row++;
            }
        }
    }

    private HBox createFriendEntry(Friend friend) {
        HBox box = new HBox(10);
        box.getStyleClass().add("friend-entry");
        ImageView iv = new ImageView(
                new Image(
                        // 절대경로 getResource → 클래스패스 루트(/)부터 찾습니다
                        getClass().getResource(friend.getImagePath())
                                .toExternalForm()
                )
        );
        iv.setFitWidth(40);
        iv.setFitHeight(40);

        iv.setFitWidth(40);
        iv.setFitHeight(40);

        Label name = new Label(friend.getName());
        name.getStyleClass().add("friend-name");

        box.getChildren().addAll(iv, name);

        box.setOnMouseClicked(e -> {
            clearSelection();
            box.getStyleClass().add("selected-entry");
            selectedBox = box;
        });

        return box;
    }

    private void clearSelection() {
        if (selectedBox != null) {
            selectedBox.getStyleClass().remove("selected-entry");
            selectedBox = null;
        }
    }

    @FXML
    private void handleSelect() {
        if (selectedBox != null) {
            Label lbl = (Label) selectedBox.getChildren().get(1);
            System.out.println("선택된 친구: " + lbl.getText());
            // TODO: 실제 로직 연결
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedBox != null) {
            Label lbl = (Label) selectedBox.getChildren().get(1);
            friends.removeIf(f -> f.getName().equals(lbl.getText()));
            populateGrid();
            selectedBox = null;
        }
    }

    private void loadView(String fxmlName) {
        try {
            Parent view = FXMLLoader.load(
                    getClass().getResource("/com/share/dairy/" + fxmlName)
            );
            // 최상위 BorderPane의 center 교체
            BorderPane root = (BorderPane) selectButton.getScene().getRoot();
            root.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML private void goHome() {
        // TODO: 나중에 MainView.fxml 로 교체
        loadView("MainView.fxml");
    }

    @FXML private void showMyInfo() {
        loadView("MyInfoView.fxml");
    }

    @FXML private void showAddFriends() {
        loadView("AddFriendsView.fxml");
    }

    @FXML private void showBuddyList() {
        loadView("FriendListUI.fxml");
    }

    // 간단한 내부 모델 클래스
    private static class Friend {
        private final String name;
        private final String imagePath;

        public Friend(String name, String imagePath) {
            this.name = name;
            this.imagePath = imagePath;
        }
        public String getName() { return name; }
        public String getImagePath() { return imagePath; }
    }
}
