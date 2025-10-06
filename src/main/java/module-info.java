module org.exlite.excellite {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;


    opens org.exlite.excellite to javafx.fxml;
    exports org.exlite.excellite;
}