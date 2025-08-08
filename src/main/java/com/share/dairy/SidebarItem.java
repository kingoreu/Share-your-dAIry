package com.share.dairy;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * 사이드바 아이템 컴포넌트
 */
public class SidebarItem extends StackPane {
    @FXML private Region sidebarItemBg;
    @FXML private ImageView iconView;
    @FXML private Label textLabel;

    public SidebarItem() {
        // 1) FXML 로드
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("SidebarItem.fxml")
        );
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException("SidebarItem.fxml 로드 실패", e);
        }
    }

    /** 아이콘 이미지 설정 (클래스패스 상대경로) */
    public void setIcon(String path) {
        iconView.setImage(new Image(
                getClass().getResourceAsStream(path)
        ));
    }

    /** 텍스트 설정 */
    public void setText(String text) {
        textLabel.setText(text);
    }

    /** 클릭 이벤트 핸들러 설정 */
    public void setOnAction(javafx.event.EventHandler<MouseEvent> h) {
        this.setOnMouseClicked(h);
    }
}
