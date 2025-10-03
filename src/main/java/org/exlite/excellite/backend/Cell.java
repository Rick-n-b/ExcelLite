package org.exlite.excellite.backend;

import javafx.scene.control.TextField;

public class Cell {
    private Table owner;
    private TextField textField;
    public final int line, column;
    private double width, height;
    private boolean isFocused;

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
        this.width = width;
        this.height = height;

        textFieldInit();

    }

    private void textFieldInit(){
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
                    Table.innerText.setText(innerStr);
                }
            }
        }));

        textField.onKeyTypedProperty().addListener(((obs, oldVal, newVal) -> {

        }));


        owner.table.getChildren().add(textField);
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
}
