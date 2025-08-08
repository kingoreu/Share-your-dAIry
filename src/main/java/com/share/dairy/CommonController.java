package com.share.dairy;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

/**
 * common.fxml 의 컨트롤러
 */
public class CommonController {
    @FXML private Label pageTitle;           // 헤더 중앙 제목
    @FXML private Region contentRegion;      // 컨텐츠 자리

    @FXML private SidebarItem itemMyInfo;    // MY Info 버튼
    @FXML private SidebarItem itemAddFriends;// ADD Friends 버튼
    @FXML private SidebarItem itemBuddyList; // BUDDY List 버튼

    /** FXML 로드 직후 호출 */
    @FXML
    public void initialize() {
        // MY Info 세팅
        itemMyInfo.setIcon("/com/share/images/myinfo-icon.png");
        itemMyInfo.setText("MY Info");
        itemMyInfo.setOnAction(e -> loadMyInfo());

        // ADD Friends 세팅
        itemAddFriends.setIcon("/com/share/images/addfriends-icon.png");
        itemAddFriends.setText("ADD Friends");
        itemAddFriends.setOnAction(e -> loadAddFriends());

        // BUDDY List 세팅
        itemBuddyList.setIcon("/com/share/images/buddylist-icon.png");
        itemBuddyList.setText("BUDDY List");
        itemBuddyList.setOnAction(e -> loadBuddyList());
    }

    /** 홈 아이콘 클릭 */
    @FXML private void onHomeClicked(MouseEvent e) {
        pageTitle.setText("Home");
        System.out.println("Home clicked");
        // TODO: contentRegion 교체 로직
    }

    private void loadMyInfo() {
        pageTitle.setText("MY Info");
        System.out.println("Load My Info view");
    }

    private void loadAddFriends() {
        pageTitle.setText("ADD Friends");
        System.out.println("Load AddFriends view");
    }

    private void loadBuddyList() {
        pageTitle.setText("BUDDY List");
        System.out.println("Load BuddyList view");
    }
}
