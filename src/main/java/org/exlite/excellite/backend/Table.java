package org.exlite.excellite.backend;

import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import org.exlite.excellite.GUI.TablePane;

import java.util.ArrayList;

public class Table {

    private TablePane tablePane;

    public static final int N = 4;
    private int columns = N, lines = N;

    private ArrayList<ArrayList<Cell>> cells;
    private ArrayList<Cell> smartCells;

    public Table(ScrollPane grid, TextField positionText, TextField innerText) {

        tablePane = new TablePane(this, grid, positionText, innerText);
        initialization();
    }

    private void initialization() {


        cells = new ArrayList<>();
        smartCells = new ArrayList<>();

        for (int i = 0; i < N; i++) {
            cells.add(new ArrayList<>());
            for (int j = 0; j < N; j++) {
                cells.getLast().add(new Cell(this, j, i, TablePane.columnWidth, TablePane.lineHeight));
            }
        }
    }


    public void addColumn() {
        var column = new ArrayList<Cell>();
        for (int i = 0; i < cells.getFirst().size(); i++) {
            column.add(new Cell(this, i, cells.size(), TablePane.columnWidth, TablePane.lineHeight));
        }
        cells.add(column);
        columns++;
    }

    public void addLine() {
        int i = 0;
        for (var line : cells) {
            line.add(new Cell(this, line.size(), i,TablePane.columnWidth, TablePane.lineHeight));
            i++;
        }
        lines++;
    }

    public void updateSmartCells(Cell curr){
        for(var smart : smartCells){
            if(smart != curr){
                smart.reEvaluate();
                smart.setOutText();
            }
        }
    }


    public void voidSearch(String value) {
        System.out.println(value);
        for (var column : cells) {
            for (var cell : column) {
                if(cell.getOutputStr().contains(value) && !value.isEmpty())
                    cell.highlight();
                else
                    cell.deHighlight();
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

    public TablePane getTable() {
        return tablePane;
    }
}
