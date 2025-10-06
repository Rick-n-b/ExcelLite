package org.exlite.excellite;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import org.exlite.excellite.backend.Cell;
import org.exlite.excellite.backend.Table;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class AppController implements Initializable {

    ArrayList<Table> tables;

    @FXML
    private Button addTableButton;

    @FXML
    private Button deleteTableButton;

    @FXML
    private TextField innerText;

    @FXML
    private TextField positionText;

    @FXML
    private TextField searchText;

    @FXML
    private TextField tableRenameText;

    @FXML
    private Button searchButton;

    @FXML
    private Button renameTableButton;

    @FXML
    private SplitPane splitPane;

    @FXML
    private TabPane tabPane;

    @FXML
    private AnchorPane topMenu;

    Table currentTable;

    @FXML
    void addTable(ActionEvent event) {

        tabPane.getTabs().add(new Tab("Table " + (tabPane.getTabs().size() + 1), new ScrollPane()));
        tables.add(new Table((ScrollPane) tabPane.getTabs().getLast().getContent(), positionText, innerText));
        tabPane.getSelectionModel().clearAndSelect(tabPane.getTabs().size() - 1);
        currentTable = tables.get(tabPane.getSelectionModel().getSelectedIndex());
        positionText.clear();
        innerText.clear();

    }

    @FXML
    void deleteTable(ActionEvent event) {

        if(tabPane.getSelectionModel().getSelectedIndex() != -1){
            tabPane.getTabs().remove(tabPane.getSelectionModel().getSelectedIndex());
            tables.remove(currentTable);
            positionText.clear();
            innerText.clear();
        }

    }

    @FXML
    void renameTable(ActionEvent event) {

        if(tabPane.getSelectionModel().getSelectedIndex() != -1)
            tabPane.getTabs().get(tabPane.getSelectionModel().getSelectedIndex()).setText(tableRenameText.getText());

    }

    @FXML
    void search(ActionEvent event) {
        if(currentTable != null)
            currentTable.voidSearch(searchText.getText());
    }

    private void positionSet(){
        if(positionText.getText() == null || positionText.getText().isEmpty() || currentTable == null)
            return;

        var column = Cell.coordinateDeParse(positionText.getText())[0];
        var line = Cell.coordinateDeParse(positionText.getText())[1];
            if (column < currentTable.getColumns() && column >= 0) {
                if (line < currentTable.getLines() && line >= 0) {
                    currentTable.getCells().get(column).get(line).setFocused();
                }
            }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tables = new ArrayList<>();


        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if(tabPane.getSelectionModel().getSelectedIndex() < tables.size())
                    currentTable = tables.get(tabPane.getSelectionModel().getSelectedIndex());
                tableRenameText.setText(tabPane.getSelectionModel().getSelectedItem().getText());
                positionText.clear();
                innerText.clear();
            }
        });

        positionText.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                switch (keyEvent.getCode()) {
                    case ENTER:
                        positionSet();
                        break;
                }
            }
        });

        positionText.focusedProperty().addListener( (obs, oldVal, nVal)-> {
            if(!nVal){
                positionSet();
            }
        });

        innerText.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                var column = Cell.coordinateDeParse(positionText.getText())[0];
                var line = Cell.coordinateDeParse(positionText.getText())[1];

                if(currentTable != null)
                    if (column < currentTable.getColumns() && column >= 0) {
                        if (line < currentTable.getLines() && line >= 0) {
                            currentTable.getCells().get(column).get(line).changeText(innerText.getText());
                        }
                    }
            }
        });


    }


}
