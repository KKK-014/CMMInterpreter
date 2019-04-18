package liushuo;

import java.util.ArrayList;
import java.util.LinkedList;

import exception.InterpretException;
import model.Symbol;
import model.Value;

public class SymbolTable {

    private static final String TEMP_PREFIX = "*temp";

    private static SymbolTable symbolTable = new SymbolTable();
    private static LinkedList<Symbol> tempNames;

    public  ArrayList<Symbol> symbolList;

    private SymbolTable() {}

    public static SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public void newTable() {
        symbolList = new ArrayList<Symbol>();
        tempNames = new LinkedList<Symbol>();
    }

    public void deleteTable() {
        if (symbolList != null) {
            symbolList.clear();
            symbolList = null;
        }
        if (tempNames != null) {
            tempNames.clear();
            tempNames = null;
        }
    }

    public void register(Symbol symbol) throws InterpretException {
        for (int i=0; i<symbolList.size(); i++) {
            if (symbolList.get(i).getName().equals(symbol.getName())) {
                if (symbolList.get(i).getLevel() < symbol.getLevel()) {
                    symbol.setNext(symbolList.get(i));
                    symbolList.set(i, symbol);
                    return;
                } else {
                    throw new InterpretException("变量 <" + symbol.getName() +"> 重复声明");
                }
            }
        }
        symbolList.add(symbol);
    }

    public void deregister(int level) {
        for (int i=0; i<symbolList.size(); i++) {
            if (symbolList.get(i).getLevel() == level) {
                symbolList.set(i, symbolList.get(i).getNext());
            }
        }
        for (int i=symbolList.size()-1 ; i>=0; i--) {
            if (symbolList.get(i) == null) {
                symbolList.remove(i);
            }
        }
    }

    public void setSymbolValue(String name, Value value) throws InterpretException {
        getSymbol(name).setValue(value);
    }

    public void setSymbolValue(String name, int value, int index) throws InterpretException {
        if (getSymbol(name).getValue().getArrayInt().length > index) {
            getSymbol(name).getValue().getArrayInt()[index] = value;
        } else {
            throw new InterpretException("数组 <" + name + "> 下标 " + index +" 越界");
        }
    }

    public void setSymbolValue(String name, double value, int index) throws InterpretException {
        if (getSymbol(name).getValue().getArrayReal().length > index) {
            getSymbol(name).getValue().getArrayReal()[index] = value;
        } else {
            throw new InterpretException("数组 <" + name + "> 下标 " + index +" 越界");
        }
    }

    /**
     * 返回Symbol中的类型
     */
    public int getSymbolType(String name) throws InterpretException {
        return getSymbol(name).getType();
    }

    /**
     * 取单值用这个函数
     */
    public Value getSymbolValue(String name) throws InterpretException {
        return getSymbolValue(name, -1);
    }

    /**
     * 取值用这个函数
     */
    public Value getSymbolValue(String name, int index) throws InterpretException {
        Symbol s = getSymbol(name);
        if (index == -1) {//单值
            return s.getValue();
        } else {
            if (s.getValue().getArrayInt().length < index + 1) {
                throw new InterpretException("数组 <" + name + "> 下标 " + index +" 越界");
            }
            if (s.getType() == Symbol.ARRAY_INT) {
                Value rv = new Value(Symbol.SINGLE_INT);
                rv.setInt(s.getValue().getArrayInt()[index]);
                return rv;
            } else {
                Value rv = new Value(Symbol.SINGLE_REAL);
                rv.setReal(s.getValue().getArrayReal()[index]);
                return rv;
            }
        }
    }

    private Symbol getSymbol(String name) throws InterpretException {
        for (Symbol s : symbolList) {
            if (s.getName().equals(name)) {
                return s;
            }
        }
        for (Symbol s : tempNames) {
            if (s.getName().equals(name)) {
                return s;
            }
        }
        if (name.startsWith(TEMP_PREFIX)) {
            Symbol s = new Symbol(name, Symbol.TEMP, -1);
            tempNames.add(s);
            return s;
        }
        throw new InterpretException("变量 <" + name + "> 不存在");
    }

    /**
     * 获取一个没有使用的临时符号名
     */
    public Symbol getTempSymbol() {
        String temp = null;
        for (int i = 1; ; i++) {
            temp = TEMP_PREFIX + i;
            boolean exist = false;
            for (Symbol s : tempNames) {
                if (s.getName().equals(temp)) {
                    exist = true;
                    break;
                }
            }
            for (Symbol s : symbolList) {
                if (s.getName().equals(temp)) {
                    exist = true;
                    break;
                }
            }
            if (exist) {
                continue;
            }
            Symbol s = new Symbol(temp, Symbol.TEMP, -1);
            tempNames.add(s);
            return s;
        }
    }

    /**
     * 清空临时符号名
     */
    public void clearTempNames() {
        tempNames.clear();
    }

    public static void printSymbolList(SymbolTable table){
        for(Symbol s: table.symbolList){
            System.out.print(s.getName()+" ");
        }
    }
}
