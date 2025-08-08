module com.share.dairy {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;

    opens com.share.dairy to javafx.fxml;
    exports com.share.dairy;
}