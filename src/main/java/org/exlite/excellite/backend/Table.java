package org.exlite.excellite.backend;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

import java.util.ArrayList;

public class Table {
    private ScrollPane controlGrid;
    private Button addColumnButton;
    private Button addLineButton;
    public static TextField positionText;
    public static TextField innerText;
    public AnchorPane table;

    private double columnWidth = 100, lineHeight = 40;
    public static final double ASSIST_COLUMN_SIZE = 40;
    public static final int N = 4;
    private int columns = N, lines = N;

    private ArrayList<ArrayList<Cell>> cells;
    private ArrayList<Cell> smartCells;

    public Table(ScrollPane grid, TextField positionText, TextField innerText) {
        this.controlGrid = grid;
        Table.positionText = positionText;
        Table.innerText = innerText;
        initialization();
    }

    private void initialization() {

        scrollPaneInit();

        cells = new ArrayList<>();
        smartCells = new ArrayList<>();

        for (int i = 0; i < N; i++) {
            createLeftAssistCell(i);
            createTopAssistCell(i);
        }
        for (int i = 0; i < N; i++) {
            cells.add(new ArrayList<>());
            for (int j = 0; j < N; j++) {
                cells.getLast().add(new Cell(this, j, i, columnWidth, lineHeight));
            }
        }
        buttonInitialize();
        repositionColumnAddButton();
        repositionLineAddButton();

    }

    private void scrollPaneInit() {
        table = new AnchorPane();

        table.resize(columnWidth * N + ASSIST_COLUMN_SIZE * 2, lineHeight * N + ASSIST_COLUMN_SIZE * 2);
        controlGrid.resize(controlGrid.getScene().getWindow().getWidth(), controlGrid.getScene().getWindow().getHeight() * 0.85);
        ;

        ChangeListener<Number> stageSizeListener = (observable, oldValue, newValue) ->
        {
            controlGrid.resize(controlGrid.getScene().getWindow().getWidth(), controlGrid.getScene().getWindow().getHeight() * 0.85);
        };

        controlGrid.getScene().getWindow().widthProperty().addListener(stageSizeListener);

        controlGrid.setContent(table);
    }

    private void buttonInitialize() {
        addLineButton = new Button();
        addColumnButton = new Button();

        addLineButton.setText("+");
        addLineButton.resize(ASSIST_COLUMN_SIZE, ASSIST_COLUMN_SIZE);
        addLineButton.setPrefSize(ASSIST_COLUMN_SIZE, ASSIST_COLUMN_SIZE);
        addLineButton.setAlignment(Pos.CENTER);
        addLineButton.setFont(Font.font(14));
        addLineButton.setOnAction(e -> addLine());

        addColumnButton.setText("+");
        addColumnButton.resize(ASSIST_COLUMN_SIZE, ASSIST_COLUMN_SIZE);
        addColumnButton.setPrefSize(ASSIST_COLUMN_SIZE, ASSIST_COLUMN_SIZE);
        addColumnButton.setAlignment(Pos.CENTER);
        addColumnButton.setFont(Font.font(14));
        addColumnButton.setOnAction(e -> addColumn());

        table.getChildren().add(addColumnButton);
        table.getChildren().add(addLineButton);
    }

    private void createLeftAssistCell(int num) {
        var numeric = new TextField();
        numeric.setText("" + (num + 1));
        numeric.setEditable(false);
        numeric.setMinSize(ASSIST_COLUMN_SIZE, lineHeight);
        numeric.setPrefSize(ASSIST_COLUMN_SIZE, lineHeight);
        numeric.setAlignment(Pos.CENTER);
        numeric.setLayoutX(0);
        numeric.setLayoutY(ASSIST_COLUMN_SIZE + num * lineHeight);
        numeric.setFont(Font.font(14));
        table.getChildren().add(numeric);
    }

    private void createTopAssistCell(int num) {
        var numeric = new TextField();
        String numLetter = "";
        do {
            numLetter += String.valueOf((char) ((num % 25) + 64 + 1));
        } while (num / 25 > 0);
        numeric.setText(numLetter);
        numeric.setEditable(false);
        numeric.setMinSize(columnWidth, ASSIST_COLUMN_SIZE);
        numeric.setPrefSize(columnWidth, ASSIST_COLUMN_SIZE);
        numeric.setAlignment(Pos.CENTER);
        numeric.setLayoutX(ASSIST_COLUMN_SIZE + num * columnWidth);
        numeric.setLayoutY(0);
        numeric.setFont(Font.font(14));
        table.getChildren().add(numeric);
    }

    public void addColumn() {
        createTopAssistCell(cells.size());
        var column = new ArrayList<Cell>();
        for (int i = 0; i < cells.getFirst().size(); i++) {
            column.add(new Cell(this, i, cells.size(), columnWidth, lineHeight));
        }
        cells.add(column);
        table.resize(table.getWidth() + columnWidth, table.getHeight());
        repositionColumnAddButton();
        columns++;
    }

    public void addLine() {
        createLeftAssistCell(cells.getLast().size());
        int i = 0;
        for (var line : cells) {
            line.add(new Cell(this, line.size(), i, columnWidth, lineHeight));
            i++;
        }

        table.resize(table.getWidth(), table.getHeight() + lineHeight);
        repositionLineAddButton();
        lines++;
    }

    private void repositionLineAddButton() {
        addLineButton.setLayoutX(0);
        addLineButton.setLayoutY(ASSIST_COLUMN_SIZE + cells.getFirst().size() * lineHeight);
    }

    private void repositionColumnAddButton() {
        addColumnButton.setLayoutX(ASSIST_COLUMN_SIZE + cells.size() * columnWidth);
        addColumnButton.setLayoutY(0);
    }

    public void updateSmartCells(Cell curr){
        for(var smart : smartCells){
            if(smart != curr){
                smart.outputStr = String.valueOf(smart.evaluate(smart.innerStrProperty.get().substring(1)));
                smart.getTextField().setText(smart.outputStr);
            }
        }
    }

    public void show() {
        for (var column : cells)
            for (var cell : column)
                cell.show();
    }

    public void hide() {
        for (var column : cells)
            for (var cell : column)
                cell.hide();
    }

    public void voidSearch(String value) {
        for (var column : cells) {
            for (var cell : column) {
                cell.getTextField().setSkin(new TextFieldSkin(cell.getTextField()) {
                    @Override
                    protected void layoutChildren(double x, double y, double w, double h) {
                        super.layoutChildren(x, y, w, h);
                        if(cell.getTextField().getText().contains(value) && !value.isEmpty())
                            textFillProperty().setValue(Color.GREEN);
                        else if(value.isEmpty())
                            textFillProperty().setValue(Color.BLACK);
                        else
                            textFillProperty().setValue(Color.BLACK);
                        cell.getTextField().getProperties().put("colorChanged", true);

                    }
                });

            }
        }
    }

    public ArrayList<ArrayList<Cell>> getCells() {
        return cells;
    }

    public Cell getCell(int column, int line) {
        return cells.get(column).get(line);
    }

    public ArrayList<Cell> getSmartCells() {
        return smartCells;
    }

    public int getColumns() {
        return columns;
    }

    public int getLines() {
        return lines;
    }
}
