package org.exlite.excellite;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import org.exlite.excellite.backend.Table;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class AppController implements Initializable {

    ArrayList<Table> tables;

    @FXML
    private Button addTableButton;

    @FXML
    private Button constant;

    @FXML
    private Button deleteTableButton;

    @FXML
    private TextField innerText;

    @FXML
    private TextField positionText;

    @FXML
    private Button renameTableButton;

    @FXML
    private SplitPane splitPane;

    @FXML
    private TabPane tabPane;

    @FXML
    private TextField tableRenameText;

    @FXML
    private AnchorPane topMenu;



    @FXML
    void addTable(ActionEvent event) {
        tabPane.getTabs().add(new Tab("Table " + (tabPane.getTabs().size() + 1), new Pane()));
        tables.add(new Table((Pane) tabPane.getTabs().getLast().getContent()));
        tabPane.getSelectionModel().clearAndSelect(tabPane.getTabs().size() - 1);
    }

    @FXML
    void deleteTable(ActionEvent event) {
        for(int i = 0; i < tabPane.getTabs().size(); i++){
            if(tabPane.getSelectionModel().isSelected(i)){
                tabPane.getTabs().remove(i);
                tables.remove(i);
                break;
            }
        }
    }

    @FXML
    void renameTable(ActionEvent event) {
        for(int i = 0; i < tabPane.getTabs().size(); i++){
            if(tabPane.getSelectionModel().isSelected(i)){
                tabPane.getTabs().get(i).setText(tableRenameText.getText());
                break;
            }
        }
    }


    @FXML
    void constantPressed(ActionEvent event) {

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tables = new ArrayList<>();
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                int i = 0;
                for(; i < tabPane.getTabs().size(); i++) {
                    if(tabPane.getSelectionModel().isSelected(i)){
                        break;
                    }
                }

                tableRenameText.setText(tabPane.getTabs().get(i).getText());
            }
        });

    }


}
