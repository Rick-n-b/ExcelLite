package org.exlite.excellite.GUI;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import org.exlite.excellite.backend.Cell;
import org.exlite.excellite.backend.Table;

import java.util.ArrayList;

public class TablePane {
    private ScrollPane controlGrid;
    private Button addColumnButton;
    private Button addLineButton;
    public static TextField positionText;
    public static TextField innerText;
    public final AnchorPane mainView;

    public static final double columnWidth = 100, lineHeight = 40;
    public static final double ASSIST_COLUMN_SIZE = 40;

    private final Table table;

    public TablePane(Table table, ScrollPane scrollPane, TextField positionText, TextField innerText){
        this.controlGrid = scrollPane;
        this.table = table;
        TablePane.innerText = innerText;
        TablePane.positionText = positionText;
        mainView = new AnchorPane();
        initialization();
    }

    private void initialization() {

        scrollPaneInit();

        for (int i = 0; i < Table.N; i++) {
            createLeftAssistCell(i);
            createTopAssistCell(i);
        }
        buttonInitialize();

        repositionColumnAddButton();
        repositionLineAddButton();
    }


    private void scrollPaneInit() {


        mainView.resize(columnWidth * Table.N + ASSIST_COLUMN_SIZE * 2, lineHeight * Table.N + ASSIST_COLUMN_SIZE * 2);
        controlGrid.resize(controlGrid.getScene().getWindow().getWidth(), controlGrid.getScene().getWindow().getHeight() * 0.85);
        ;

        ChangeListener<Number> stageSizeListener = (observable, oldValue, newValue) ->
        {
            controlGrid.resize(controlGrid.getScene().getWindow().getWidth(), controlGrid.getScene().getWindow().getHeight() * 0.85);
        };

        controlGrid.getScene().getWindow().widthProperty().addListener(stageSizeListener);

        controlGrid.setContent(mainView);
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

        mainView.getChildren().add(addColumnButton);
        mainView.getChildren().add(addLineButton);
    }

    private void repositionLineAddButton() {
        addLineButton.setLayoutX(0);
        addLineButton.setLayoutY(ASSIST_COLUMN_SIZE + table.getLines() * lineHeight);
    }

    private void repositionColumnAddButton() {
        addColumnButton.setLayoutX(ASSIST_COLUMN_SIZE + table.getColumns() * columnWidth);
        addColumnButton.setLayoutY(0);
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
        mainView.getChildren().add(numeric);
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
        mainView.getChildren().add(numeric);
    }

    public void addColumn() {
        createTopAssistCell(table.getColumns());
        table.addColumn();
        mainView.resize(mainView.getWidth() + columnWidth, mainView.getHeight());
        repositionColumnAddButton();
    }

    public void addLine() {
        createLeftAssistCell(table.getLines());
        table.addLine();
        mainView.resize(mainView.getWidth(), mainView.getHeight() + lineHeight);
        repositionLineAddButton();
    }





}
