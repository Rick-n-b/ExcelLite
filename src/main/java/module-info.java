module org.exlite.excellite {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.exlite.excellite to javafx.fxml;
    exports org.exlite.excellite;
}