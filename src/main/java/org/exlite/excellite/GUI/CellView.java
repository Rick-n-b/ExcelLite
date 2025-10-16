package org.exlite.excellite.GUI;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;
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
        textField = new TextField();
        textField.resize(width, height);
        textField.setPrefSize(width, height);
        textField.setLayoutX(cell.column * width + TablePane.ASSIST_COLUMN_SIZE);
        textField.setLayoutY(cell.line * height + TablePane.ASSIST_COLUMN_SIZE);

        // Устанавливаем начальное значение
        textField.setText(cell.getOutputStr());

        textField.focusedProperty().addListener(((obs, oldVal, newVal) -> {
            if (newVal != null) {
                isFocused = newVal;
                if (isFocused) {
                    // При фокусе показываем исходную формулу/текст
                    TablePane.positionText.setText(Cell.coordinateParse(cell.column, cell.line));
                    TablePane.innerText.setText(cell.getInnerStr());
                    textField.setText(cell.getInnerStr());
                } else {
                    // При потере фокуса показываем результат вычисления
                    textField.setText(cell.getOutputStr());
                }
            }
        }));

        textField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldText, String newText) {
                if (isFocused) {
                    cell.setInnerStr(newText);
                    TablePane.innerText.setText(newText);
                }
            }
        });

        // Слушатель изменений внутреннего текста ячейки
        cell.getInnerStrProperty().addListener((observable, oldValue, newValue) -> {
            if (!isFocused) {
                // Если ячейка не в фокусе, обновляем отображение результата
                textField.setText(cell.getOutputStr());
            }
        });

        tablePane.mainView.getChildren().add(textField);
    }

    public void highlight(){
        textField.setStyle("-fx-text-fill: green;");
    }

    public void deHighlight(){
        textField.setStyle("-fx-text-fill: black;");
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

    public boolean getFocused(){
        return isFocused;
    }

}
