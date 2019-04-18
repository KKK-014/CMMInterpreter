package  liushuo;

import java.util.LinkedList;
import java.util.Scanner;

import exception.InterpretException;
import model.FourCode;
import model.Symbol;
import model.Token;
import model.TreeNode;
import model.Value;

public class Interpreter {

    private static int mLevel;
    private static int pc;
    private static SymbolTable symbolTable;

    public static void main(String[] args) {
        LinkedList<FourCode> codes;
        symbolTable = SymbolTable.getSymbolTable();
        Interpreter interpreter = new Interpreter();
        try {
            symbolTable.newTable();
            codes = CodeGenerater.generateCode(args[0]);
            symbolTable.deleteTable();
            for (FourCode code : codes) {
                System.out.println(code.toString());
            }
            System.out.println("\n\n\n\n");
            int length = codes.size();
            pc = 0;
            mLevel = 0;
            symbolTable.newTable();
            while (pc < length) {//not end
                interpreter.interpretFourCode(codes.get(pc));
            }
        } catch (InterpretException e) {
            System.out.println(e.toString());
        }
        symbolTable.deleteTable();
    }

    private void interpretFourCode(FourCode code) throws InterpretException {
        String instype = code.getFirst();
        if (instype.equals(FourCode.JMP)) {//跳转指令
            if (code.getSecond() == null || symbolTable.getSymbolValue(code.getSecond()).getType() == Symbol.FALSE) {//需要跳转
                pc = getValue(code.getForth()).getInt();
                return;//如果继续执行就会+1
            }
        }
        if (instype.equals(FourCode.READ)) {//输入指令
            Scanner sc = new Scanner(System.in);
            String input = sc.next();
            int type = symbolTable.getSymbolType(getId(code.getForth()));
            switch (type) {
                case Symbol.SINGLE_INT:
                case Symbol.ARRAY_INT:
                {
                    Value value = parseValue(input);
                    if (value.getType() == Symbol.SINGLE_INT) {
                        setValue(code.getForth(), value);
                    } else {
                        throw new InterpretException("类型不匹配");
                    }
                    break;
                }
                case Symbol.SINGLE_REAL:
                case Symbol.ARRAY_REAL:
                {
                    Value value = parseValue(input);
                    setValue(code.getForth(), value);
                    break;
                }
                case Symbol.TEMP://impossible
                default:
                    break;
            }
        }
        if (instype.equals(FourCode.WRITE)) {
            int index = -1;
            if (isArrayElement(code.getForth())) {
                index = getIndex(code.getForth());
            }
            System.out.println(symbolTable.getSymbolValue(code.getForth(), index));
        }
        if (instype.equals(FourCode.IN)) {
            mLevel++;
        }
        if (instype.equals(FourCode.OUT)) {
            symbolTable.deregister(mLevel);
            mLevel--;
        }
        if (instype.equals(FourCode.INT)) {
            if (code.getThird() != null) {
                Symbol symbol = new Symbol(code.getForth(), Symbol.ARRAY_INT, mLevel);
                symbol.getValue().initArray(getInt(code.getThird()));
                symbolTable.register(symbol);
            } else {
                int intvalue = 0;
                if (code.getSecond() != null) {
                    intvalue = getInt(code.getSecond());
                }
                Symbol symbol = new Symbol(code.getForth(), Symbol.SINGLE_INT, mLevel, intvalue);
                symbolTable.register(symbol);
            }
        }
        if (instype.equals(FourCode.REAL)) {
            if (code.getThird() != null) {
                Symbol symbol = new Symbol(code.getForth(), Symbol.ARRAY_REAL, mLevel);
                symbol.getValue().initArray(getInt(code.getThird()));
                symbolTable.register(symbol);
            } else {
                double doublevalue = 0;
                if (code.getSecond() != null) {
                    doublevalue = getDouble(code.getSecond());
                }
                Symbol symbol = new Symbol(code.getForth(), Symbol.SINGLE_REAL, mLevel, doublevalue);
                symbolTable.register(symbol);
            }
        }
        if (instype.equals(FourCode.ASSIGN)) {
            Value value = getValue(code.getSecond());
            setValue(code.getForth(), value);
        }
        if (instype.equals(FourCode.PLUS)) {
            setValue(code.getForth(), getValue(code.getSecond()).PLUS(getValue(code.getThird())));
        }
        if (instype.equals(FourCode.MINUS)) {
            if (code.getThird() != null) {
                setValue(code.getForth(), getValue(code.getSecond()).MINUS(getValue(code.getThird())));
            } else {
                setValue(code.getForth(), Value.NOT(getValue(code.getSecond())));
            }
        }
        if (instype.equals(FourCode.MUL)) {
            setValue(code.getForth(), getValue(code.getSecond()).MUL(getValue(code.getThird())));
        }
        if (instype.equals(FourCode.DIV)) {
            setValue(code.getForth(), getValue(code.getSecond()).DIV(getValue(code.getThird())));
        }
        if (instype.equals(FourCode.GT)) {
            setValue(code.getForth(), getValue(code.getSecond()).GT(getValue(code.getThird())));
        }
        if (instype.equals(FourCode.LT)) {
            setValue(code.getForth(), getValue(code.getSecond()).LT(getValue(code.getThird())));
        }
        if (instype.equals(FourCode.EQ)) {
            setValue(code.getForth(), getValue(code.getSecond()).EQ(getValue(code.getThird())));
        }
        if (instype.equals(FourCode.GET)) {
            setValue(code.getForth(), getValue(code.getSecond()).GET(getValue(code.getThird())));
        }
        if (instype.equals(FourCode.LET)) {
            setValue(code.getForth(), getValue(code.getSecond()).LET(getValue(code.getThird())));
        }
        if (instype.equals(FourCode.NEQ)) {
            setValue(code.getForth(), getValue(code.getSecond()).NEQ(getValue(code.getThird())));
        }
        pc++;//正常+1,需要jmp时不要执行到这里,务必直接return
    }


    private Value getValue(String id) throws InterpretException {
        if (id.matches("\\d*\\.\\d*")) {
            Value value = new Value(Symbol.SINGLE_REAL);
            value.setReal(Double.parseDouble(id));
            return value;
        }
        if (id.matches("\\d+")) {
            Value value = new Value(Symbol.SINGLE_INT);
            value.setInt(Integer.parseInt(id));
            return value;
        }
        int index = -1;
        if (isArrayElement(id)) {
            index = getIndex(id);
        }
        return symbolTable.getSymbolValue(getId(id), index);
    }

    /**
     * 给xx[xx]或者xx赋值
     */
    private void setValue(String id, Value value) throws InterpretException {
        int index = -1;
        if (isArrayElement(id)) {
            index = getIndex(id);
        }
        int type = symbolTable.getSymbolType(getId(id));
        switch (type) {
            case Symbol.SINGLE_INT:
            case Symbol.SINGLE_REAL:
            {
                if (type == Symbol.SINGLE_REAL) {
                    symbolTable.setSymbolValue(getId(id), value.toReal());
                } else {
                    if (value.getType() == Symbol.SINGLE_REAL) {
                        throw new InterpretException("表达式" + id + "与变量类型不匹配");
                    } else {
                        symbolTable.setSymbolValue(getId(id), value);
                    }
                }
                break;
            }
            case Symbol.ARRAY_INT:
            case Symbol.ARRAY_REAL:
            {
                if (symbolTable.getSymbolValue(getId(id), index).getType() == Symbol.SINGLE_REAL) {
                    symbolTable.setSymbolValue(getId(id), value.toReal().getReal(), index);
                } else {
                    if (value.getType() == Symbol.SINGLE_REAL) {
                        throw new InterpretException("表达式 <" + id + "> 与变量类型不匹配");
                    } else {
                        symbolTable.setSymbolValue(getId(id), value.getInt(), index);
                    }
                }
                break;
            }
            case Symbol.TEMP:
                symbolTable.setSymbolValue(getId(id), value);
                break;
            default:
                break;
        }
    }

    /**
     * 判断是否是数组
     */
    private boolean isArrayElement(String id) {
        return id.contains("[");
    }

    /**
     * 将用户输入的数据转为Value
     */
    private Value parseValue(String str) throws InterpretException {
        if (str.matches("^(-?\\d+)(\\.\\d?)$")) {
            Value value = new Value(Symbol.SINGLE_REAL);
            value.setReal(Double.parseDouble(str));
            return value;
        }
        if (str.matches("^(-?\\d+)$")) {
            Value value = new Value(Symbol.SINGLE_INT);
            value.setInt(Integer.parseInt(str));
            return value;
        }
        throw new InterpretException("输入非法");
    }

    /**
     * 传入形如 xx[xx],获取其中的索引值
     */
    private int getIndex(String id) throws InterpretException {
        String indexstr = id.substring(id.indexOf("[") + 1, id.length() - 1) + "";
        return getInt(indexstr);
    }

    /**
     * 传入一个字面值或者标识符,获取对应int值
     */
    private int getInt(String value) throws InterpretException {
        if (value.matches("^(-?\\d+)$")) {
            return Integer.parseInt(value);
        }
        Value valueint;
        try{
            valueint = symbolTable.getSymbolValue(value);
        } catch (InterpretException e) {
            throw new InterpretException("字面值 "+value+" 不是整数");
        }
        if (valueint.getType() == Symbol.SINGLE_INT) {
            return valueint.getInt();
        } else {
            throw new InterpretException("变量 "+value+" 不是整数");
        }
    }

    /**
     * 传入一个字面值或者标识符,获取对应double值
     */
    private double getDouble(String value) throws InterpretException {
        if (value.matches("^(-?\\d+)(\\.\\d+)?$")) {
            return Double.parseDouble(value);
        }
        Value valueint = symbolTable.getSymbolValue(value);
        return valueint.toReal().getReal();
    }

    /**
     * 传入形如xx[xx]或者xx 获取前面的id
     */
    private String getId(String id) {
        if (isArrayElement(id)) {
            return id.substring(0, id.indexOf("[")) + "";//prevent from memory leak
        }
        return id;
    }
}
