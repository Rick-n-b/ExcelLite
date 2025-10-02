package org.exlite.excellite.backend;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;

import java.util.ArrayList;

public class Table {
    public Pane grid;
    private ScrollPane controlGrid;
    private boolean isSelected;
    private double columnWidth = 100, lineHeight = 40;
    public static final double ASSIST_COLUMN_SIZE = 30;
    private Button addColumnButton;
    private Button addLineButton;
    private ArrayList<ArrayList<Cell>> cells;

    public Table(Pane grid){
        this.grid = grid;

        cells = new ArrayList<>();
        initialization();
    }

    public void initialization(){

        for(int i = 0; i < 4; i++){
            createLeftAssistCell(i);
            createTopAssistCell(i);
        }
        for(int i = 0; i < 4; i++){
            cells.add(new ArrayList<>());
            for(int j = 0; j < 4; j++){
                cells.getLast().add(new Cell(this, j, i, columnWidth, lineHeight));
                cells.getLast().getLast().outputStr = j + " " + i;
            }
        }
        buttonInitialize();
        repositionColumnAddButton();
        repositionLineAddButton();

    }

    private void buttonInitialize(){
        addLineButton = new Button();
        addColumnButton = new Button();

        addLineButton.setText("+");
        addLineButton.setMinSize(ASSIST_COLUMN_SIZE, ASSIST_COLUMN_SIZE);
        addLineButton.setPrefSize(ASSIST_COLUMN_SIZE, ASSIST_COLUMN_SIZE);
        addLineButton.setAlignment(Pos.CENTER);
        addLineButton.setFont(Font.font(14));
        addLineButton.setOnAction(e -> addLine());

        addColumnButton.setText("+");
        addColumnButton.setMinSize(ASSIST_COLUMN_SIZE, ASSIST_COLUMN_SIZE);
        addColumnButton.setPrefSize(ASSIST_COLUMN_SIZE, ASSIST_COLUMN_SIZE);
        addColumnButton.setAlignment(Pos.CENTER);
        addColumnButton.setFont(Font.font(14));
        addColumnButton.setOnAction(e -> addColumn());

        grid.getChildren().add(addColumnButton);
        grid.getChildren().add(addLineButton);
    }

    private void createLeftAssistCell(int num){
        var numeric = new TextField();
        numeric.setText("" + (num + 1));
        numeric.setEditable(false);
        numeric.setMinSize(ASSIST_COLUMN_SIZE, lineHeight);
        numeric.setPrefSize(ASSIST_COLUMN_SIZE, lineHeight);
        numeric.setAlignment(Pos.CENTER);
        numeric.setLayoutX(0);
        numeric.setLayoutY(ASSIST_COLUMN_SIZE + num * lineHeight);
        numeric.setFont(Font.font(14));
        grid.getChildren().add(numeric);
    }

    private void createTopAssistCell(int num){
        var numeric = new TextField();
        String numLetter = "";
        do{
            numLetter += String.valueOf((char)((num % 25) + 64 + 1));
        }while (num / 25 > 0);
        numeric.setText(numLetter);
        numeric.setEditable(false);
        numeric.setMinSize(columnWidth, ASSIST_COLUMN_SIZE);
        numeric.setPrefSize(columnWidth, ASSIST_COLUMN_SIZE);
        numeric.setAlignment(Pos.CENTER);
        numeric.setLayoutX(ASSIST_COLUMN_SIZE + num * columnWidth);
        numeric.setLayoutY(0);
        numeric.setFont(Font.font(14));
        grid.getChildren().add(numeric);
    }

    public void addColumn(){
        createTopAssistCell(cells.size());
        var column =  new ArrayList<Cell>();
        for(int i = 0; i < cells.getFirst().size(); i++){
            column.add(new Cell(this, i, cells.size(), columnWidth, lineHeight));
        }
        cells.add(column);
        repositionColumnAddButton();
    }

    public void addLine(){
        createLeftAssistCell(cells.getLast().size());
        int i = 0;
        for(var line : cells){
            line.add(new Cell(this, line.size(), i, columnWidth, lineHeight));
            i++;
        }
        repositionLineAddButton();
    }

    private void repositionLineAddButton(){
        addLineButton.setLayoutX(0);
        addLineButton.setLayoutY(ASSIST_COLUMN_SIZE + cells.getFirst().size() * lineHeight);

    }

    private void repositionColumnAddButton(){
        addColumnButton.setLayoutX(ASSIST_COLUMN_SIZE + cells.size() * columnWidth);
        addColumnButton.setLayoutY(0);
    }



    public void show(){
        for(var column : cells)
            for(var cell : column)
                cell.show();
    }

    public void hide(){
        for(var column : cells)
            for(var cell : column)
                cell.hide();
    }

}
