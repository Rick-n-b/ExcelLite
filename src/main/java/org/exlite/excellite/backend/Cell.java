package org.exlite.excellite.backend;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

public class Cell {
    private Table owner;
    private TextField textField;
    public final int line, column;
    private double width, height;
    private boolean isFocused;

    public String outputStr;
    private String innerStr;
    public SimpleStringProperty innerStrProperty;

    public enum DataType{
        STRING,
        DATE,
        FORMULA
    }

    public DataType dataType;

    public Cell(Table owner, int line, int column, double width, double height){
        this.owner = owner;
        this.line = line;
        this.column = column;
        this.width = width;
        this.height = height;
        dataType = DataType.STRING;
        textFieldInit();
    }

    private void textFieldInit(){
        innerStrProperty = new SimpleStringProperty(innerStr);
        innerStrProperty.addListener(((observableValue, oldStr, nStr) -> {
            if(nStr != null){
                var coor = Cell.coordinateDeParse(Table.positionText.getText());

                if(coor[0] == column){
                    if(coor[1] == line){
                        changeText(innerStrProperty.get());
                    }
                }
            }
        }));

        textField = new TextField();
        textField.resize(width, height);
        textField.setPrefSize(width, height);
        textField.setLayoutX(column * width + Table.ASSIST_COLUMN_SIZE);
        textField.setLayoutY(line  * height + Table.ASSIST_COLUMN_SIZE);

        textField.focusedProperty().addListener(((obs, oldVal, newVal) -> {
            if(newVal != null){
                isFocused = newVal;
                if(isFocused){
                    Table.positionText.setText(coordinateParse());
                    Table.innerText.setText(innerStrProperty.get());
                    textField.setText(innerStrProperty.get());

                }else{
                    textField.setText(outputStr);
                }
                System.out.println();
            }
        }));

        textField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                changeText(textField.getText());
            }
        });

        owner.table.getChildren().add(textField);
    }

    public void changeText(String text){
        innerStrProperty.set(text);
        if(dataType != DataType.FORMULA){
            outputStr = text;
        }else{
            calculate(text);
        }
    }

    private void calculate(String text){

    }

    private String coordinateParse(){
        String out = "";
        var local = column;
        do{
            out += String.valueOf((char)((local % 25) + 64 + 1));
        }while (local / 25 > 0);
        out += line + 1;
        return out;
    }

    public static String coordinateParse(int column, int line){
        String out = "";
        do{
            out += String.valueOf((char)((column % 25) + 64 + 1));
        }while (column / 25 > 0);
        out += line + 1;
        return out;
    }

    public static int[] coordinateDeParse(String coordinate){
        var out = new int[2];
        out[0] = -1;
        out[1] = -1;
        char[] chars = coordinate.toCharArray();
        int i = 0;
        while(chars[i] != '\0' && (chars[i] <= 'Z' && chars[i] >= 'A')){
            out[0] += chars[i] - 64;
            if(i > 3){
                out[0] = -1;
                break;
            }
            i++;
        }
        String sub = coordinate.substring(i);
        if(sub.matches("\\d{1,3}"))
            out[1] = Integer.parseInt(sub) - 1;


        return out;
    }

    public void setFocused(){
        textField.requestFocus();
    }

    public void hide(){
        textField.setVisible(false);
    }

    public void show(){
        textField.setVisible(true);
    }

    public TextField getTextField() {
        return textField;
    }

    public void setInnerStr(String innerStr) {
        this.innerStr = innerStr;
    }
    public String getInnerStr() {
        return innerStr;
    }
}
