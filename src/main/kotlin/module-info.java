module ru.welfegor.imageEditor {
    requires javafx.swing;
    requires javafx.fxml;
    requires javafx.controls;
    requires kotlin.stdlib;
    requires opencv;

    opens ru.welfegor.imageEditor to javafx.fxml;
    exports ru.welfegor.imageEditor;
}