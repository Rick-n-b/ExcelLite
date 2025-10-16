package org.exlite.excellite.backend;

import javafx.beans.property.SimpleStringProperty;
import org.exlite.excellite.GUI.CellView;

import java.util.Stack;

public class Cell {
    public final CellView cellView;

    private final Table owner;
    public final int line, column;


    private String outputStr;
    private SimpleStringProperty innerStrProperty;

    public enum DataType {
        STRING,
        FORMULA
    }

    private DataType dataType;

    public Cell(Table owner, int line, int column, double width, double height) {
        this.owner = owner;
        this.line = line;
        this.column = column;
        dataType = DataType.STRING;
        cellView = new CellView(owner.getTable(), this, width, height);
    }

    private void dataValidation() {
        if (!innerStrProperty.get().isEmpty()) {
            if (innerStrProperty.get().charAt(0) == '=')
                dataType = DataType.FORMULA;
            else
                dataType = DataType.STRING;
        } else
            dataType = DataType.STRING;
        updateSmart();
    }

    public void changeText(String text) {

        dataValidation();
        if (dataType != Cell.DataType.FORMULA) {
            innerStrProperty.set(text);
            outputStr = text;
        } else {
            innerStrProperty.set(text);
            if(text.length() >= 2)
                outputStr = String.valueOf(evaluate(text.substring(1)));
        }
        owner.updateSmartCells(this);

    }

    // Метод для определения приоритета оператора
    private static int getPrecedence(char operator) {
        return switch (operator) {
            case '+', '-' -> 1;
            case '*', '/' -> 2;
            default -> 0;
        };
    }

    // Метод для выполнения операции
    private static double applyOperation(double b, double a, char operator) {
        return switch (operator) {
            case '+' -> a + b;
            case '-' -> a - b;
            case '*' -> a * b;
            case '/' -> {
                if (b == 0) {
                    yield Double.MAX_VALUE;
                }
                yield a / b;
            }
            case '^' -> Math.pow(a, b);
            default -> 0;
        };
    }

    private static Object applyOp(Object bb, Object aa, char operator){
        if(bb instanceof Double && aa instanceof Double){
            double b = (Double) bb;
            double a = (Double) aa;
            return switch (operator) {
                case '+' -> a + b;
                case '-' -> a - b;
                case '*' -> a * b;
                case '/' -> {
                    if (b == 0) {
                        yield Double.MAX_VALUE;
                    }
                    yield a / b;
                }
                case '^' -> Math.pow(a, b);
                default -> 0;
            };
        }
        if(bb instanceof Double && aa instanceof String) {
            double b = (Double) bb;
            String a = (String) aa;
            String out = "";
             switch (operator) {
                 case '+':
                     out = a + b;
                    break;
                 case '*':
                     for(int i = 0; i < b; i++ ){
                         out += a;
                     }
                     break;
                 default:
                     out = "";
                     break;
            }
             return out;
        }
        if(bb instanceof String && aa instanceof Double) {
            String b = (String) bb;
            double a = (Double) aa;
            String out = b;
            StringBuilder stringBuilder = new StringBuilder(b);
            switch (operator) {
                case '+':
                    out = a + b;
                    break;
                case '*':
                    for(int i = 0; i < a; i++ ){
                        out += b;
                    }
                    break;
                case '^':
                    for(int i = 1; i < a; i++ ){
                        for(int j = 0; j < out.length(); j++) {
                            stringBuilder.insert(j, out);
                        }
                        out = stringBuilder.toString();
                    }
                    break;
                default:
                    out = "";
                    break;
            }
            return out;
        }


        return null;
    }

    public String evaluate(String expression) {
        expression = expression.replaceAll("\\s", "");

        Stack<Double> values = new Stack<>();
        Stack<Character> operators = new Stack<>();

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            if (Character.isDigit(c) || Character.isAlphabetic(c) || c == '\\' || (c == '-' && (i == 0 || expression.charAt(i - 1) == '('))) {

                StringBuilder sb = new StringBuilder();
                if (c == '-') {
                    sb.append(c);
                    i++;
                    c = expression.charAt(i);
                }
                if (Character.isDigit(c)) {
                    while (i < expression.length() && (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
                        sb.append(expression.charAt(i));
                        i++;
                    }
                    values.push(Double.valueOf(sb.toString()));
                    i--; // Откат на один индекс, так как внешний цикл i++
                } else if (Character.isUpperCase(c)) {
                    while (i < expression.length() && (Character.isDigit(expression.charAt(i)) || Character.isUpperCase(expression.charAt(i)))) {
                        sb.append(expression.charAt(i));
                        i++;
                    }
                    var coor = Cell.coordinateDeParse(sb.toString());
                    if (coor[0] == -1 || coor[1] == -1 || coor[0] > owner.getColumns() - 1 || coor[1] > owner.getLines() - 1)
                        return "err";

                    double cellValue = 0;
                    if (!owner.getCell(coor[0], coor[1]).outputStr.isEmpty())
                        if (owner.getCell(coor[0], coor[1]).outputStr.matches("^(-?)(0|([1-9][0-9]*))(\\.[0-9]+)?$"))
                            cellValue = Double.parseDouble(owner.getCell(coor[0], coor[1]).outputStr);
                        else
                            return "err";
                    values.push(cellValue);
                    i--;
                } else if (c == '\\') {
                    while (i < expression.length() && (Character.isAlphabetic(expression.charAt(i)) || expression.charAt(i) == '\\')) {
                        sb.append(expression.charAt(i));
                        i++;
                    }
                    double myConst = switch (sb.toString()){
                        case "\\PI", "\\Pi", "\\pi" -> Math.PI;
                        case "\\E", "\\e" -> Math.E;
                        default -> 0;
                    };
                    if(myConst == 0)
                        return "err";
                    values.push(myConst);
                    i--;
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
                    return "err";
                }
            } else if (c == '+' || c == '-' || c == '*' || c == '/' || c == '^') {
                while (!operators.isEmpty() && getPrecedence(c) <= getPrecedence(operators.peek())) {
                    values.push(applyOperation(values.pop(), values.pop(), operators.pop()));
                }
                operators.push(c);
            } else {
                return "err";
            }
        }

        // Обработка оставшихся операторов
        while (!operators.isEmpty()) {
            if (!values.isEmpty()) {
                var secondOperand = values.pop();
                if (!values.isEmpty()) {
                    var firstOperand = values.pop();
                    values.push(applyOperation(secondOperand, firstOperand, operators.pop()));
                } else {
                    break;
                }
            } else {
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

    public void reEvaluate(){
        outputStr = evaluate(innerStrProperty.get().substring(1));
    }

    private void updateSmart(){
        if(dataType == DataType.FORMULA){
            if (!owner.getSmartCells().contains(this))
                owner.getSmartCells().add(this);
        }else{
            owner.getSmartCells().remove(this);
        }
    }

    private String coordinateParse() {
        StringBuilder out = new StringBuilder();
        var local = column;
        do {
            out.append(String.valueOf((char) ((local % 25) + 64 + 1)));
        } while (local / 25 > 0);
        out.append(line + 1);
        return out.toString();
    }

    public static String coordinateParse(int column, int line) {
        StringBuilder out = new StringBuilder();
        do {
            out.append(String.valueOf((char) ((column % 25) + 64 + 1)));
        } while (column / 25 > 0);
        out.append(line + 1);
        return out.toString();
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

    public void highlight(){
        cellView.hightlight();
    }

    public void deHighlight(){
        cellView.deHighlight();
    }

    public void setFocused(){
        cellView.setFocused();
    }





    //--getters ands setters
    public DataType getType() {
        return dataType;
    }

    public String getOutputStr(){
        return outputStr;
    }

    public SimpleStringProperty getInnerStrProperty() {
        return innerStrProperty;
    }
    public String getInnerStr(){
        return innerStrProperty.get();
    }

    public void setOutputStr(String str){
        this.outputStr = str;
    }

    public void setInnerStr(String str){
        if(innerStrProperty != null)
            this.innerStrProperty.set(str);
        else
            this.innerStrProperty = new SimpleStringProperty(str);
    }

    public void setOutText(String text){
        cellView.setOutText(text);
    }

    public void setOutText(){
        cellView.setOutText(outputStr);
    }
}
