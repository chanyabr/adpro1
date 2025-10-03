module se233.project1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires jaffree;


    opens se233.project1 to javafx.fxml;
    opens se233.project1.view to javafx.fxml, javafx.graphics;
    exports se233.project1;
}
