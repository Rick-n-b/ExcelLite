package org.exlite.excellite.backend;

import javafx.beans.property.SimpleStringProperty;
import org.exlite.excellite.GUI.CellView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Stack;
import java.util.regex.Pattern;

public class Cell {
    public final CellView cellView;

    private final Table owner;
    public final int line, column;

    private String outputStr;
    private SimpleStringProperty innerStrProperty;

    public enum DataType {
        STRING,
        FORMULA,
        NUMBER,
        DATE
    }

    private DataType dataType;

    // Форматы для дат
    private static final DateTimeFormatter[] DATE_FORMATTERS = {
            DateTimeFormatter.ofPattern("dd.MM.yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy")
    };

    // Регулярное выражение для чисел
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^(-?)(0|([1-9][0-9]*))(\\.[0-9]+)?$");

    public Cell(Table owner, int line, int column, double width, double height) {
        this.owner = owner;
        this.line = line;
        this.column = column;
        dataType = DataType.STRING;
        innerStrProperty = new SimpleStringProperty("");
        outputStr = "";
        cellView = new CellView(owner.getTable(), this, width, height);
    }

    private void dataValidation() {
        if (innerStrProperty == null || innerStrProperty.get() == null || innerStrProperty.get().isEmpty()) {
            dataType = DataType.STRING;
            return;
        }

        String text = innerStrProperty.get();

        if (!text.isEmpty() && text.charAt(0) == '=') {
            dataType = DataType.FORMULA;
        } else if (isDate(text)) {
            dataType = DataType.DATE;
        } else if (isNumber(text)) {
            dataType = DataType.NUMBER;
        } else {
            dataType = DataType.STRING;
        }

        updateSmart();
    }

    // Проверка, является ли строка датой
    private boolean isDate(String text) {
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                LocalDate.parse(text, formatter);
                return true;
            } catch (DateTimeParseException e) {
                // Продолжаем проверку со следующим форматом
            }
        }
        return false;
    }

    // Проверка, является ли строка числом
    private boolean isNumber(String text) {
        return NUMBER_PATTERN.matcher(text).matches();
    }

    // Преобразование строки в соответствующий тип данных
    private Object parseValue(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        // Пробуем распарсить как дату
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(text, formatter);
            } catch (DateTimeParseException e) {
                // Продолжаем проверку
            }
        }

        // Пробуем распарсить как число
        if (isNumber(text)) {
            try {
                return Double.parseDouble(text);
            } catch (NumberFormatException e) {
                // Не должно происходить из-за проверки isNumber
            }
        }

        // Возвращаем как строку
        return text;
    }

    public void changeText(String text) {
        if (text == null) {
            text = "";
        }

        if (innerStrProperty == null) {
            innerStrProperty = new SimpleStringProperty(text);
        } else {
            innerStrProperty.set(text);
        }

        dataValidation();

        if (dataType == DataType.FORMULA && text.length() > 1) {
            String result = evaluate(text.substring(1));
            outputStr = result != null ? result : "err";
        } else {
            // Для не-формул outputStr совпадает с innerStr
            outputStr = text;
        }

        if (!cellView.getFocused())
            setOutText();

        // Обновляем все зависимые ячейки
        owner.updateSmartCells(this);
    }

    // Метод для определения приоритета оператора
    private static int getPrecedence(char operator) {
        return switch (operator) {
            case '+', '-' -> 1;
            case '*', '/' -> 2;
            case '^' -> 3;
            default -> 0;
        };
    }

    // Получение значения ячейки по координатам
    private Object getCellValue(int column, int line) {
        if (column < 0 || column >= owner.getColumns() || line < 0 || line >= owner.getLines()) {
            return "err";
        }

        Cell targetCell = owner.getCell(column, line);
        String cellOutput = targetCell.getOutputStr();

        if (cellOutput == null || cellOutput.isEmpty() || "err".equals(cellOutput)) {
            return cellOutput != null ? cellOutput : "";
        }

        // В зависимости от типа данных ячейки возвращаем соответствующий объект
        switch (targetCell.getType()) {
            case NUMBER:
                try {
                    return Double.parseDouble(cellOutput);
                } catch (NumberFormatException e) {
                    return cellOutput; // Если не число, возвращаем как строку
                }
            case DATE:
                for (DateTimeFormatter formatter : DATE_FORMATTERS) {
                    try {
                        return LocalDate.parse(cellOutput, formatter);
                    } catch (DateTimeParseException e) {
                        // Продолжаем проверку
                    }
                }
                return cellOutput; // Если не дата, возвращаем как строку
            case FORMULA:
                // Для формул пытаемся определить тип результата
                Double numberResult = tryParseToDouble(cellOutput);
                if (numberResult != null) {
                    return numberResult;
                }
                LocalDate dateResult = tryParseToDate(cellOutput);
                if (dateResult != null) {
                    return dateResult;
                }
                return cellOutput; // Если не число и не дата, возвращаем как строку
            default: // STRING
                return cellOutput;
        }
    }

    private LocalDate tryParseToDate(String text) {
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(text, formatter);
            } catch (DateTimeParseException e) {
                // Продолжаем проверку
            }
        }
        return null;
    }

    private Double tryParseToDouble(Object obj) {
        if (obj instanceof Double) {
            return (Double) obj;
        }
        if (obj instanceof String) {
            try {
                return Double.parseDouble((String) obj);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        if (obj instanceof Long) {
            return ((Long) obj).doubleValue();
        }
        if (obj instanceof Integer) {
            return ((Integer) obj).doubleValue();
        }
        return null;
    }

    // Проверка совместимости типов для операций
    private boolean areTypesCompatible(Object a, Object b, char operator) {
        // Оператор + разрешен для любых типов
        if (operator == '+') {
            return true;
        }

        // Для оператора - проверяем допустимые комбинации
        if (operator == '-') {
            return (a instanceof Double && b instanceof Double) ||
                    (a instanceof LocalDate && b instanceof LocalDate) ||
                    (a instanceof LocalDate && b instanceof Double) ||
                    tryParseToDouble(a) != null && tryParseToDouble(b) != null;
        }

        // Для оператора * проверяем допустимые комбинации
        if (operator == '*') {
            return (a instanceof Double && b instanceof Double) ||
                    (a instanceof String && b instanceof Double) ||
                    (a instanceof Double && b instanceof String) ||
                    tryParseToDouble(a) != null && tryParseToDouble(b) != null;
        }

        // Для операторов / и ^ нужны числа
        if (operator == '/' || operator == '^') {
            return (a instanceof Double && b instanceof Double) ||
                    tryParseToDouble(a) != null && tryParseToDouble(b) != null;
        }

        return false;
    }

    // Выполнение операции с учетом типов данных
    private Object applyOperation(Object b, Object a, char operator) {
        try {
            // Сначала выполняем операцию над исходными типами, потом преобразуем в строку

            // Оператор + - особый случай, работает с разными типами
            if (operator == '+') {
                // Если оба числа - математическое сложение
                if (a instanceof Double && b instanceof Double) {
                    return (Double) a + (Double) b;
                }
                // Если оба даты - ошибка (нельзя складывать даты)
                if (a instanceof LocalDate && b instanceof LocalDate) {
                    return "err";
                }
                // Дата + число дней
                if (a instanceof LocalDate && b instanceof Double) {
                    return ((LocalDate) a).plusDays(((Double) b).longValue());
                }
                // Число дней + дата
                if (a instanceof Double && b instanceof LocalDate) {
                    return ((LocalDate) b).plusDays(((Double) a).longValue());
                }
                // Во всех остальных случаях - конкатенация
                return a.toString() + b.toString();
            }

            // Оператор -
            if (operator == '-') {
                // Оба числа - вычитание
                if (a instanceof Double && b instanceof Double) {
                    return (Double) a - (Double) b;
                }
                // Разница между датами
                if (a instanceof LocalDate && b instanceof LocalDate) {
                    return java.time.temporal.ChronoUnit.DAYS.between((LocalDate) b, (LocalDate) a);
                }
                // Дата - число дней
                if (a instanceof LocalDate && b instanceof Double) {
                    return ((LocalDate) a).minusDays(((Double) b).longValue());
                }
                // Попробуем преобразовать в числа
                Double numA = tryParseToDouble(a);
                Double numB = tryParseToDouble(b);
                if (numA != null && numB != null) {
                    return numA - numB;
                }
                return "err";
            }

            // Оператор *
            if (operator == '*') {
                // Оба числа - умножение
                if (a instanceof Double && b instanceof Double) {
                    return (Double) a * (Double) b;
                }
                // Строка * число (повторение)
                if (a instanceof String && b instanceof Double) {
                    String str = (String) a;
                    int count = ((Double) b).intValue();
                    if (count < 0) return "err";
                    return str.repeat(count);
                }
                // Число * строка (повторение)
                if (a instanceof Double && b instanceof String) {
                    String str = (String) b;
                    int count = ((Double) a).intValue();
                    if (count < 0) return "err";
                    return str.repeat(count);
                }
                // Попробуем преобразовать в числа
                Double numA = tryParseToDouble(a);
                Double numB = tryParseToDouble(b);
                if (numA != null && numB != null) {
                    return numA * numB;
                }
                return "err";
            }

            // Оператор /
            if (operator == '/') {
                // Оба числа - деление
                if (a instanceof Double && b instanceof Double) {
                    if ((Double) b == 0) return "err";
                    return (Double) a / (Double) b;
                }
                // Попробуем преобразовать в числа
                Double numA = tryParseToDouble(a);
                Double numB = tryParseToDouble(b);
                if (numA != null && numB != null) {
                    if (numB == 0) return "err";
                    return numA / numB;
                }
                return "err";
            }

            // Оператор ^ (степень)
            if (operator == '^') {
                // Оба числа - возведение в степень
                if (a instanceof Double && b instanceof Double) {
                    return Math.pow((Double) a, (Double) b);
                }
                // Попробуем преобразовать в числа
                Double numA = tryParseToDouble(a);
                Double numB = tryParseToDouble(b);
                if (numA != null && numB != null) {
                    return Math.pow(numA, numB);
                }
                return "err";
            }

            return "err";

        } catch (Exception e) {
            return "err";
        }
    }

    public String evaluate(String expression) {
        if (expression == null || expression.isEmpty()) {
            return "";
        }

        expression = expression.replaceAll("\\s", "");

        Stack<Object> values = new Stack<>();
        Stack<Character> operators = new Stack<>();

        try {
            for (int i = 0; i < expression.length(); i++) {
                char c = expression.charAt(i);

                if (Character.isDigit(c) || Character.isAlphabetic(c) || c == '\\' ||
                        c == '"' || (c == '-' && (i == 0 || expression.charAt(i - 1) == '('))) {

                    StringBuilder sb = new StringBuilder();
                    boolean negative = false;

                    // Обработка отрицательных чисел
                    if (c == '-') {
                        negative = true;
                        i++;
                        if (i >= expression.length()) return "err";
                        c = expression.charAt(i);
                    }

                    // Обработка строковых литералов
                    if (c == '"') {
                        i++;
                        while (i < expression.length() && expression.charAt(i) != '"') {
                            sb.append(expression.charAt(i));
                            i++;
                        }
                        if (i >= expression.length()) {
                            return "err"; // Незакрытая кавычка
                        }
                        values.push(sb.toString());
                        continue;
                    }

                    // Обработка чисел, ссылок на ячейки и констант
                    if (Character.isDigit(c)) {
                        // Обработка чисел
                        while (i < expression.length() &&
                                (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
                            sb.append(expression.charAt(i));
                            i++;
                        }
                        try {
                            double value = Double.parseDouble(sb.toString());
                            values.push(negative ? -value : value);
                        } catch (NumberFormatException e) {
                            return "err";
                        }
                        i--;
                    } else if (Character.isUpperCase(c)) {
                        // Обработка ссылок на ячейки (A1, B2, etc.)
                        while (i < expression.length() &&
                                (Character.isDigit(expression.charAt(i)) || Character.isUpperCase(expression.charAt(i)))) {
                            sb.append(expression.charAt(i));
                            i++;
                        }
                        int[] coor = Cell.coordinateDeParse(sb.toString());
                        if (coor[0] == -1 || coor[1] == -1 ||
                                coor[0] >= owner.getColumns() || coor[1] >= owner.getLines()) {
                            return "err";
                        }

                        Object cellValue = getCellValue(coor[0], coor[1]);
                        if ("err".equals(cellValue)) {
                            return "err";
                        }
                        values.push(cellValue);
                        i--;
                    } else if (c == '\\') {
                        // Обработка констант
                        while (i < expression.length() &&
                                (Character.isAlphabetic(expression.charAt(i)) || expression.charAt(i) == '\\')) {
                            sb.append(expression.charAt(i));
                            i++;
                        }
                        double myConst = switch (sb.toString().toLowerCase()) {
                            case "\\pi", "pi" -> Math.PI;
                            case "\\e", "e" -> Math.E;
                            default -> Double.NaN;
                        };
                        if (Double.isNaN(myConst)) {
                            return "err";
                        }
                        values.push(negative ? -myConst : myConst);
                        i--;
                    } else {
                        // Нераспознанный символ
                        return "err";
                    }
                } else if (c == '(') {
                    operators.push(c);
                } else if (c == ')') {
                    while (!operators.isEmpty() && operators.peek() != '(') {
                        if (values.size() < 2) return "err";
                        Object b = values.pop();
                        Object a = values.pop();
                        char op = operators.pop();

                        if (!areTypesCompatible(a, b, op)) {
                            return "err";
                        }
                        Object result = applyOperation(b, a, op);
                        if ("err".equals(result)) return "err";
                        values.push(result);
                    }
                    if (operators.isEmpty() || operators.pop() != '(') {
                        return "err";
                    }
                } else if (c == '+' || c == '-' || c == '*' || c == '/' || c == '^') {
                    while (!operators.isEmpty() && getPrecedence(c) <= getPrecedence(operators.peek())) {
                        if (values.size() < 2) return "err";
                        Object b = values.pop();
                        Object a = values.pop();
                        char op = operators.pop();

                        if (!areTypesCompatible(a, b, op)) {
                            return "err";
                        }
                        Object result = applyOperation(b, a, op);
                        if ("err".equals(result)) return "err";
                        values.push(result);
                    }
                    operators.push(c);
                } else {
                    return "err";
                }
            }

            // Обработка оставшихся операторов
            while (!operators.isEmpty()) {
                if (values.size() < 2) return "err";
                Object b = values.pop();
                Object a = values.pop();
                char operator = operators.pop();

                if (!areTypesCompatible(a, b, operator)) {
                    return "err";
                }
                Object result = applyOperation(b, a, operator);
                if ("err".equals(result)) return "err";
                values.push(result);
            }

            // Преобразование результата в строку
            if (values.size() == 1) {
                Object result = values.pop();
                if (result instanceof LocalDate) {
                    return ((LocalDate) result).format(DATE_FORMATTERS[0]);
                } else if (result instanceof Double) {
                    // Убираем .0 у целых чисел для красоты
                    double d = (Double) result;
                    if (d == (long) d) {
                        return String.valueOf((long) d);
                    }
                    return String.valueOf(d);
                }
                return String.valueOf(result);
            } else {
                return "err";
            }
        } catch (Exception e) {
            return "err";
        }
    }

    public void reEvaluate() {
        if (dataType == DataType.FORMULA && innerStrProperty != null &&
                innerStrProperty.get().length() >= 2) {
            String result = evaluate(innerStrProperty.get().substring(1));
            outputStr = result != null ? result : "err";
            setOutText();
        }
    }

    private void updateSmart() {
        if (dataType == DataType.FORMULA) {
            if (!owner.getSmartCells().contains(this)) {
                owner.getSmartCells().add(this);
            }
        } else {
            owner.getSmartCells().remove(this);
        }
    }

    // Остальные методы остаются без изменений...

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
        cellView.highlight();
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