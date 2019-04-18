package liushuo;

import java.io.IOException;
import java.util.LinkedList;

import exception.InterpretException;
import model.FourCode;
import model.Symbol;
import model.Token;
import model.TreeNode;
import java.util.regex.Pattern;

public class CodeGenerater {

    private static int mLevel;
    private static int mLine;
    private static LinkedList<FourCode> codes;
    private static SymbolTable symbolTable;

    public static LinkedList<FourCode> generateCode(String filename) throws InterpretException {
        mLine = -1;//代码编号从0开始
        mLevel = 0;
        codes = new LinkedList<FourCode>();
        try {
            LinkedList<Token> tokens = Util.getTokenList(filename);
            if(tokens != null){
                Util.get_token_out(tokens);
                System.out.println("\n\n\n\n");
                LinkedList<TreeNode> nodeList = Util.getNodeList(tokens);
                if(nodeList != null){
                    Util.get_node_out(nodeList);
                    System.out.println("\n\n\n\n");
                    symbolTable = SymbolTable.getSymbolTable();
                    symbolTable.newTable();
                    CodeGenerater generator = new CodeGenerater();
                    for (TreeNode node : nodeList) {
                        generator.interpret(node);
                    }
                    symbolTable.deleteTable();
                }else{
                    System.out.println("语法分析错误：");
                    Util.get_error_out(Parser.errors);
                }
            }else{
                System.out.println("词法分析错误：");
                Util.get_error_out(Lexer.errors);
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return codes;
    }

    private void interpret(TreeNode node) throws InterpretException {
        while (true) {
            switch (node.getType()) {
                //解释If语句
                case TreeNode.IF_STMT:
                    interpretIfStmt(node);
                    break;
                //解释while语句
                case TreeNode.WHILE_STMT:
                {
                    int jmpline = mLine + 1;
                    FourCode falsejmp = new FourCode(FourCode.JMP, interpretExp(node.getLeft()), null, null);
                    codes.add(falsejmp);
                    mLine++;
                    codes.add(new FourCode(FourCode.IN, null, null, null));
                    mLine++;
                    mLevel++;
                    //while的middle是循环体
                    interpret(node.getMiddle());
                    SymbolTable.getSymbolTable().deregister(mLevel);
                    mLevel--;
                    codes.add(new FourCode(FourCode.OUT, null, null, null));
                    mLine++;
                    codes.add(new FourCode(FourCode.JMP, null, null, jmpline + ""));
                    mLine++;
                    falsejmp.setForth(String.valueOf(mLine + 1));
                    break;
                }
                //解释read
                case TreeNode.READ_STMT:
                {
                    String varname = null;
                    int type = symbolTable.getSymbolType(node.getLeft().getValue());
                    switch (type) {
                        case Symbol.SINGLE_INT:
                        case Symbol.SINGLE_REAL:
                            codes.add(new FourCode(FourCode.READ, null, null, node.getLeft().getValue()));
                            mLine++;
                            break;
                        case Symbol.ARRAY_INT:
                        case Symbol.ARRAY_REAL:
                            codes.add(new FourCode(FourCode.READ, null, null, node.getLeft().getValue() + "[" + interpretExp(node.getLeft().getLeft()) + "]"));
                            mLine++;
                            break;
                        case Symbol.TEMP:
                        default:
                            throw new InterpretException("输入语句有误");
                    }
                    break;
                }
                //解释write
                case TreeNode.WRITE_STMT:
                    codes.add(new FourCode(FourCode.WRITE, null, null, interpretExp(node.getLeft())));
                    mLine++;
                    break;
                //解释声明
                case TreeNode.DECLARE_STMT:
                {
                    SymbolTable table = SymbolTable.getSymbolTable();
                    TreeNode var = node.getLeft();
                    if (var.getLeft() == null) {//单值
                        String value = null;
                        if (node.getMiddle() != null) {
                            value = interpretExp(node.getMiddle());
                        }
                        if (var.getDataType() == Token.INT) {
                            codes.add(new FourCode(FourCode.INT, value, null, var.getValue()));
                            mLine++;
                            Symbol symbol = new Symbol(var.getValue(), Symbol.SINGLE_INT, mLevel);
                            table.register(symbol);
                        } else if (var.getDataType() == Token.REAL) {
                            codes.add(new FourCode(FourCode.REAL, value, null, var.getValue()));
                            mLine++;
                            Symbol symbol = new Symbol(var.getValue(), Symbol.SINGLE_REAL, mLevel);
                            table.register(symbol);
                        }
                    } else {
                        String len = interpretExp(var.getLeft());
                        if (var.getDataType() == Token.INT) {
                            codes.add(new FourCode(FourCode.INT, null, len, var.getValue()));
                            mLine++;
                            Symbol symbol = new Symbol(var.getValue(), Symbol.ARRAY_INT, mLevel);
                            table.register(symbol);
                        } else {
                            codes.add(new FourCode(FourCode.REAL, null, len, var.getValue()));
                            mLine++;
                            Symbol symbol = new Symbol(var.getValue(), Symbol.ARRAY_REAL, mLevel);
                            table.register(symbol);
                        }
                    }
                    break;
                }
                //解释赋值
                case TreeNode.ASSIGN_STMT:
                {
                    SymbolTable table = SymbolTable.getSymbolTable();
                    String value = interpretExp(node.getMiddle());

                    TreeNode var = node.getLeft();
                    if (var.getLeft() == null) {//单值
                        codes.add(new FourCode(FourCode.ASSIGN, value, null, var.getValue()));
                        mLine++;
                    } else {
                        String index = interpretExp(var.getLeft());
                        codes.add(new FourCode(FourCode.ASSIGN, value, null, var.getValue() + "[" + index + "]"));
                        mLine++;
                    }
                    break;
                }
                default:
                    break;
            }
            symbolTable.clearTempNames();
            if (node.getNext() != null) {
                node = node.getNext();
            } else {
                break;
            }
        }
    }

    private void interpretIfStmt(TreeNode node) throws InterpretException {
        if (node.getType() == TreeNode.IF_STMT) {
            //条件跳转 jmp 条件  null 目标  条件为假时跳转
            FourCode falsejmp = new FourCode(FourCode.JMP, interpretExp(node.getLeft()), null, null);
            codes.add(falsejmp);
            mLine++;
            codes.add(new FourCode(FourCode.IN, null, null, null));
            mLine++;
            mLevel++;
            //if的middle存的是if条件正确的代码块
            interpret(node.getMiddle());
            SymbolTable.getSymbolTable().deregister(mLevel);
            mLevel--;
            codes.add(new FourCode(FourCode.OUT, null, null, null));
            mLine++;
            //else中的代码块
            if (node.getRight() != null) {
                FourCode outjump = new FourCode(FourCode.JMP, null, null, null);
                codes.add(outjump);
                mLine++;
                falsejmp.setForth(String.valueOf(mLine + 1));
                codes.add(new FourCode(FourCode.IN, null, null, null));
                mLine++;
                mLevel++;
                interpret(node.getRight());
                codes.add(new FourCode(FourCode.OUT, null, null, null));
                mLine++;
                SymbolTable.getSymbolTable().deregister(mLevel);
                mLevel--;
                outjump.setForth(String.valueOf(mLine + 1));
            } else {
                falsejmp.setForth(String.valueOf(mLine + 1));
            }
        }
    }

    private String interpretExp(TreeNode node) throws InterpretException {
        if (node.getType() == TreeNode.EXP) {
            switch (node.getDataType()) {
                case Token.LOGIC_EXP:
                    return interpretLogicExp(node);
                case Token.ADDTIVE_EXP:
                    return interpretAddtiveExp(node);
                case Token.TERM_EXP:
                    return interpretTermExp(node);
                default:
                    throw new InterpretException("复合表达式非法");
            }
        } else if (node.getType() == TreeNode.FACTOR) {
            if (node.getDataType() == Token.MINUS) {
                String temp = symbolTable.getTempSymbol().getName();
                //因子
                codes.add(new FourCode(FourCode.MINUS, interpretExp(node.getLeft()), null, temp));
                mLine++;
                return temp;
            } else {
                return interpretExp(node.getLeft());
            }
        } else if (node.getType() == TreeNode.VAR) {
            if (node.getLeft() == null) {//单值
                if (symbolTable.getSymbolType(node.getValue()) == Symbol.SINGLE_INT || symbolTable.getSymbolType(node.getValue()) == Symbol.SINGLE_REAL) {
                    return node.getValue();
                }
            } else {
                if (symbolTable.getSymbolType(node.getValue()) == Symbol.ARRAY_INT || symbolTable.getSymbolType(node.getValue()) == Symbol.ARRAY_REAL) {
                    String temp = symbolTable.getTempSymbol().getName();
                    String index = interpretExp(node.getLeft());
                    codes.add(new FourCode(FourCode.ASSIGN, node.getValue() + "[" + index + "]", null, temp));
                    mLine++;
                    return temp;
                }
            }
        } else if (node.getType() == TreeNode.LITREAL) {
            return node.getValue();
        }
        throw new InterpretException("表达式非法");
    }

    private String interpretLogicExp(TreeNode node) throws InterpretException {
        String temp = symbolTable.getTempSymbol().getName();
        switch (node.getMiddle().getDataType()) {
            case Token.GT:
                codes.add(new FourCode(FourCode.GT, interpretExp(node.getLeft()), interpretExp(node.getRight()), temp));
                break;
            case Token.GET:
                codes.add(new FourCode(FourCode.GET, interpretExp(node.getLeft()), interpretExp(node.getRight()), temp));
                break;
            case Token.LT:
                codes.add(new FourCode(FourCode.LT, interpretExp(node.getLeft()), interpretExp(node.getRight()), temp));
                break;
            case Token.LET:
                codes.add(new FourCode(FourCode.LET, interpretExp(node.getLeft()), interpretExp(node.getRight()), temp));
                break;
            case Token.EQ:
                codes.add(new FourCode(FourCode.EQ, interpretExp(node.getLeft()), interpretExp(node.getRight()), temp));
                break;
            case Token.NEQ:
                codes.add(new FourCode(FourCode.NEQ, interpretExp(node.getLeft()), interpretExp(node.getRight()), temp));
                break;
            default:
                throw new InterpretException("逻辑比较非法");
        }
        mLine++;
        return temp;
    }

    private String interpretAddtiveExp(TreeNode node) throws InterpretException {
        String temp = symbolTable.getTempSymbol().getName();
        switch (node.getMiddle().getDataType()) {
            case Token.PLUS:
                codes.add(new FourCode(FourCode.PLUS, interpretExp(node.getLeft()), interpretExp(node.getRight()), temp));

                break;
            case Token.MINUS:
                codes.add(new FourCode(FourCode.MINUS, interpretExp(node.getLeft()), interpretExp(node.getRight()), temp));

                break;
            default:
                throw new InterpretException("算数运算非法");
        }
        mLine++;
        return temp;
    }

    /**
     * 修正存储结构带来的整数乘除法从右往左的计算错误
     * 注意term的TreeNode left一定是factor
     */
    private String interpretTermExp(TreeNode node) throws InterpretException {
        String opcode = getOpcode(node.getMiddle().getDataType());
        String temp1 = symbolTable.getTempSymbol().getName();
        if (node.getRight().getType() == TreeNode.FACTOR) {
            codes.add(new FourCode(opcode, interpretExp(node.getLeft()), interpretExp(node.getRight()), temp1));
            mLine++;
        } else {
            codes.add(new FourCode(opcode, interpretExp(node.getLeft()), interpretExp(node.getRight().getLeft()), temp1));
            mLine++;
            node = node.getRight();
            String temp2 = null;
            while (node.getRight() != null && node.getRight().getType() != TreeNode.FACTOR) {
                opcode = getOpcode(node.getMiddle().getDataType());
                temp2 = symbolTable.getTempSymbol().getName();
                codes.add(new FourCode(opcode, temp1, interpretExp(node.getRight().getLeft()), temp2));
                mLine++;
                node = node.getRight();
                temp1 = temp2;
            }
            opcode = getOpcode(node.getMiddle().getDataType());
            temp2 = symbolTable.getTempSymbol().getName();
            codes.add(new FourCode(opcode, temp1, interpretExp(node.getRight()), temp2));
            mLine++;
            temp1 = temp2;
        }
        return temp1;
    }

    private String getOpcode(int op) {
        if (op == Token.MUL) {
            return FourCode.MUL;
        } else {//Token.DIV
            return FourCode.DIV;
        }
    }
}
