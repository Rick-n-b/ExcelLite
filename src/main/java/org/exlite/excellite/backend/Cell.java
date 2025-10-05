package org.exlite.excellite.backend;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;

import java.util.Stack;

public class Cell {
    private final Table owner;
    private TextField textField;
    public final int line, column;
    private double width, height;
    private boolean isFocused;

    public String outputStr;
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
        outputStr = "";
        innerStrProperty = new SimpleStringProperty("");
        innerStrProperty.addListener((
                (observableValue, oldStr, nStr) -> {
                    if(nStr != null){
                        var coor = Cell.coordinateDeParse(Table.positionText.getText());

                        if(coor[0] == column)
                            if(coor[1] == line)
                                changeText(innerStrProperty.get());
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
                }else{
                    textField.setText(outputStr);
                }
            }
        }));

        textField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                if(isFocused){
                    innerStrProperty.set(textField.getText());
                    Table.innerText.setText(innerStrProperty.get());
                }

                System.out.println("O: " + outputStr + " ||  I: " + innerStrProperty.get());
            }
        });

        owner.table.getChildren().add(textField);
    }

    private void dataValidation() {
        if (innerStrProperty.get() != null)
            if (!innerStrProperty.get().isEmpty()){
                if (innerStrProperty.get().charAt(0) == '=')
                    dataType = DataType.FORMULA;
                else if (innerStrProperty.get().matches("dd/MM/YYYY"))//doesnt work as wanted
                    dataType = DataType.DATE;
                else
                    dataType = DataType.STRING;
            }else
                dataType = DataType.STRING;


    }

    public void changeText(String text){

        dataValidation();
        if(dataType != DataType.FORMULA){
            innerStrProperty.set(text);
            outputStr = text;
        }else{
            //calculate(text.substring(1));
            innerStrProperty.set(text);
            outputStr = String.valueOf(evaluate(text.substring(1)));
        }

        if(!isFocused)
            textField.setText(outputStr);
    }

    private String calculate(String text){

        int first = -1;

        for(int i = 0; i < text.length(); i++){
            if(text.charAt(i) == '(')
                first = i;
            if(text.charAt(i) == ')'){
                if(first < 0)
                    return "Err";
                text = text.replace(text.substring(first, i), calculate(text.substring(first, i)));
                i = 0;
                first = -1;
            }
        }
        if(first >= 0)
            return "Err";

        return text;
    }
    // Метод для определения приоритета оператора
    private static int getPrecedence(char operator) {
        switch (operator) {
            case '+':
            case '-':
                return 1;
            case '*':
            case '/':
                return 2;
        }
        return 0; // Для скобок и других символов
    }

    // Метод для выполнения операции
    private static double applyOperation(double a, double b, char operator) {
        switch (operator) {
            case '+':
                return a + b;
            case '-':
                return a - b;
            case '*':
                return a * b;
            case '/':
                if (b == 0) {
                    throw new ArithmeticException("Деление на ноль!");
                }
                return a / b;
        }
        return 0; // Не должно произойти
    }

    public String evaluate(String expression) {
        // Убираем пробелы для удобства парсинга
        expression = expression.replaceAll("\\s", "");

        Stack<Double> values = new Stack<>();
        Stack<Character> operators = new Stack<>();

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            if (Character.isDigit(c) || Character.isUpperCase(c) || (c == '-' && (i == 0 || expression.charAt(i - 1) == '('))) {
                // Если это число или отрицательное число в начале или после скобки
                StringBuilder sb = new StringBuilder();
                if (c == '-') {
                    sb.append(c);
                    i++;
                    c = expression.charAt(i); // Берем следующую цифру
                }
                if(Character.isDigit(c)){
                    while (i < expression.length() && (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
                        sb.append(expression.charAt(i));
                        i++;
                    }
                    values.push(Double.valueOf(sb.toString()));
                    i--; // Откат на один индекс, так как внешний цикл инкрементирует i
                }
                else if(Character.isUpperCase(c)){
                    while (i < expression.length() && (Character.isDigit(expression.charAt(i)) || Character.isUpperCase(expression.charAt(i)))) {
                        sb.append(expression.charAt(i));
                        i++;
                    }
                    var coor = Cell.coordinateDeParse(sb.toString());
                    if(coor[0] == -1 || coor[1] == -1 || coor[0] > owner.getColumns() - 1 || coor[1] > owner.getLines() - 1)
                        return "err";

                    double cellValue = 0;
                    if(!owner.getCell(coor[0], coor[1]).outputStr.isEmpty())
                        if(owner.getCell(coor[0], coor[1]).outputStr.matches("^(-?)(0|([1-9][0-9]*))(\\.[0-9]+)?$"))
                            cellValue = Double.parseDouble(owner.getCell(coor[0], coor[1]).outputStr);
                        else
                            return "err";
                    values.push(cellValue);
                    i--; // Откат на один индекс, так как внешний цикл инкрементирует i
                }


            } else if (c == '(') {
                operators.push(c);
            } else if (c == ')') {
                while (!operators.isEmpty() && operators.peek() != '(') {
                    values.push(applyOperation(values.pop(), values.pop(), operators.pop()));
                }
                if (!operators.isEmpty() && operators.peek() == '(') {
                    operators.pop(); // Удаляем открывающую скобку
                } else {
                    //throw new IllegalArgumentException("Неправильные скобки в выражении!");
                    return "err";
                }
            } else if (c == '+' || c == '-' || c == '*' || c == '/') {
                // Обработка оператора
                while (!operators.isEmpty() && getPrecedence(c) <= getPrecedence(operators.peek())) {
                    values.push(applyOperation(values.pop(), values.pop(), operators.pop()));
                }
                operators.push(c);
            } else {
                //throw new IllegalArgumentException("Недопустимый символ в выражении: " + c);
                return "err";
            }
        }

        // Обработка оставшихся операторов
        while (!operators.isEmpty()) {
            if(!values.isEmpty()){
                var firstOperand = values.pop();
                if(!values.isEmpty()) {
                    var secondOperand = values.pop();
                    values.push(applyOperation(firstOperand, secondOperand, operators.pop()));
                }else{
                    break;
                }
            }else{
                break;
            }
        }

        // Результат находится на вершине стека чисел
        if (values.size() == 1) {
            return String.valueOf(values.pop());
        } else {
            return "err";
        }
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

    public static int[] coordinateDeParse(String coordinate) {
        var out = new int[2];
        out[0] = -1;
        out[1] = -1;

        if (coordinate == null || coordinate.isEmpty())
            return out;

        char[] chars = coordinate.toCharArray();
        int i = 0;
        while (i < chars.length && (chars[i] <= 'Z' && chars[i] >= 'A')) {
            out[0] += chars[i] - 64;
            if (i == 3) {
                out[0] = -1;
                break;
            }
            i++;
        }
        String sub = coordinate.substring(i);
        if (sub.matches("\\d{1,3}"))
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
