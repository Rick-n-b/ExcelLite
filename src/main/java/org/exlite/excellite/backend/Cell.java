package org.exlite.excellite.backend;

import javafx.scene.control.TextField;

public class Cell {
    private Table owner;
    private TextField textField;
    private int line, column;

    public String outputStr = "";
    private String innerStr;

    public enum DataType{
        STRING,
        DATE,
        FORMULA
    }


    public Cell(Table owner, int line, int column, double width, double height){
        this.owner = owner;
        this.line = line;
        this.column = column;
        textField = new TextField();
        textField.resize(width, height);
        textField.setPrefSize(width, height);
        textField.setLayoutX(column * width + Table.ASSIST_COLUMN_SIZE);
        textField.setLayoutY(line  * height + Table.ASSIST_COLUMN_SIZE);
        owner.table.getChildren().add(textField);
        //owner.grid.getChildren().add(textField);
    }

    public void hide(){
        textField.setVisible(false);
    }

    public void show(){
        textField.setVisible(true);
    }

}
