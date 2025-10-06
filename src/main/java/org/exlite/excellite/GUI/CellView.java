package org.exlite.excellite.GUI;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.paint.Color;
import org.exlite.excellite.backend.Cell;

public class CellView {

    private TextField textField;
    private double width, height;
    private final Cell cell;
    private final TablePane tablePane;

    private boolean isFocused;

    public CellView(TablePane tablePane, Cell cell, double width, double height){
        this.tablePane = tablePane;
        this.cell = cell;
        this.width = width;
        this.height = height;
        textFieldInit();
    }

    private void textFieldInit() {
        cell.setOutputStr("");
        cell.setInnerStr("");
        cell.getInnerStrProperty().addListener((
                (observableValue, oldStr, nStr) -> {
                    if (nStr != null) {
                        var coor = Cell.coordinateDeParse(TablePane.positionText.getText());

                        if (coor[0] == cell.column)
                            if (coor[1] == cell.line){
                                cell.changeText(cell.getInnerStr());
                                if (!isFocused)
                                    textField.setText(cell.getOutputStr());
                            }

                    }
                }));

        textField = new TextField();
        textField.resize(width, height);
        textField.setPrefSize(width, height);
        textField.setLayoutX(cell.column * width + TablePane.ASSIST_COLUMN_SIZE);
        textField.setLayoutY(cell.line * height + TablePane.ASSIST_COLUMN_SIZE);

        textField.focusedProperty().addListener(((obs, oldVal, newVal) -> {
            if (newVal != null) {
                isFocused = newVal;
                if (isFocused) {
                    TablePane.positionText.setText(Cell.coordinateParse(cell.column, cell.line));
                    TablePane.innerText.setText(cell.getInnerStr());
                    if(cell.getType() == Cell.DataType.FORMULA)
                        textField.setText(cell.getInnerStr());
                } else {
                    textField.setText(cell.getOutputStr());
                }
            }
        }));

        textField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                if (isFocused) {
                    cell.setInnerStr(textField.getText());
                    TablePane.innerText.setText(cell.getInnerStr());
                }
            }
        });

        tablePane.mainView.getChildren().add(textField);
    }

    public void hightlight(){
        textField.setSkin(new TextFieldSkin(textField) {
            @Override
            protected void layoutChildren(double x, double y, double w, double h) {
                super.layoutChildren(x, y, w, h);
                textFillProperty().setValue(Color.GREEN);
                textField.getProperties().put("colorChanged", true);
            }
        });
    }

    public void deHighlight(){
        textField.setSkin(new TextFieldSkin(textField) {
            @Override
            protected void layoutChildren(double x, double y, double w, double h) {
                super.layoutChildren(x, y, w, h);
                textFillProperty().setValue(Color.BLACK);
                textField.getProperties().put("colorChanged", true);
            }
        });
    }


    public void setFocused() {
        textField.requestFocus();
    }

    public void hide() {
        textField.setVisible(false);
    }

    public void show() {
        textField.setVisible(true);
    }

    public TextField getTextField() {
        return textField;
    }

    public void setOutText(String text){
        textField.setText(text);
    }

}
