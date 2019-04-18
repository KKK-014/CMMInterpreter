package liushuo;

import java.util.LinkedList;
import java.util.ListIterator;

import exception.ParserException;
import model.Token;
import model.TreeNode;

public class Parser {
    
    private static LinkedList<TreeNode> treeNodeList;
    private static Token currentToken = null;
    private static ListIterator<Token> iterator = null;
    
    public static LinkedList<TreeNode> syntacticAnalyse(LinkedList<Token> tokenList) throws ParserException {
        treeNodeList = new LinkedList<TreeNode>();
        iterator = tokenList.listIterator();//迭代器，既可以向后，也可以向前读取
        while (iterator.hasNext()) {
            treeNodeList.add(parseStmt());
        }
        return treeNodeList;
    }
    
    /**
     * 入口处的语句处理
     */
    private static TreeNode parseStmt() throws ParserException {
        switch (getNextTokenType()) {
        case Token.IF: return parseIfStmt();
        case Token.WHILE: return parseWhileStmt();
        case Token.READ: return parseReadStmt();
        case Token.WRITE: return parseWriteStmt();
        case Token.INT: // same as REAL
        case Token.REAL: return parseDeclareStmt();
        case Token.LBRACE: return parseStmtBlock();
        case Token.ID: return parseAssignStmt();
        default:
            throw new ParserException("line " + getNextTokenLineNo() + " : next token should be variable type");
        }
    }
    
    /**
     * if语句
     */
    private static TreeNode parseIfStmt() throws ParserException {
        TreeNode node = new TreeNode(TreeNode.IF_STMT);
        consumeNextToken(Token.IF);
        consumeNextToken(Token.LPARENT);
        node.setLeft(parseExp());
        consumeNextToken(Token.RPARENT);
        node.setMiddle(parseStmt());
        if (getNextTokenType() == Token.ELSE) {
            consumeNextToken(Token.ELSE);
            node.setRight(parseStmt());
        }
        return node;
    }
    
    /**
     * while语句
     */
    private static TreeNode parseWhileStmt() throws ParserException {
        TreeNode node = new TreeNode(TreeNode.WHILE_STMT);
        consumeNextToken(Token.WHILE);
        consumeNextToken(Token.LPARENT);
        node.setLeft(parseExp());
        consumeNextToken(Token.RPARENT);
        node.setMiddle(parseStmt());
        return node;
    }
    
    /**
     * read语句
     */
    private static TreeNode parseReadStmt() throws ParserException {
        TreeNode node = new TreeNode(TreeNode.READ_STMT);
        consumeNextToken(Token.READ);
        node.setLeft(variableName());
        consumeNextToken(Token.SEMI);
        return node;
    }
    
    /**
     * write语句
     */
    private static TreeNode parseWriteStmt() throws ParserException {
        TreeNode node = new TreeNode(TreeNode.WRITE_STMT);
        consumeNextToken(Token.WRITE);
        node.setLeft(parseExp());
        consumeNextToken(Token.SEMI);
        return node;
    }
    
    /**
     * declare语句
     */
    private static TreeNode parseDeclareStmt() throws ParserException {
        TreeNode node = new TreeNode(TreeNode.DECLARE_STMT);
        TreeNode varNode = new TreeNode(TreeNode.VAR);
        if (checkNextTokenType(Token.INT, Token.REAL)) {
            currentToken = iterator.next();
            int type = currentToken.getType();
            if (type == Token.INT) {
                varNode.setDataType(Token.INT);
            } else {//type == Token.REAL
                varNode.setDataType(Token.REAL);
            }
        } else {
            throw new ParserException("line " + getNextTokenLineNo() + " : next token should be variable type");
        }
        if (checkNextTokenType(Token.ID)) {
            currentToken = iterator.next();
            varNode.setValue(currentToken.getValue());
        } else {
            throw new ParserException("line " + getNextTokenLineNo() + " : next token should be ID");
        }
        if (getNextTokenType() == Token.ASSIGN) {
            consumeNextToken(Token.ASSIGN);
            node.setMiddle(parseExp());
        } else if (getNextTokenType() == Token.LBRACKET) {
            consumeNextToken(Token.LBRACKET);
            varNode.setLeft(parseExp());
            consumeNextToken(Token.RBRACKET);
        }
        consumeNextToken(Token.SEMI);
        node.setLeft(varNode);
        return node;
    }
    
    /**
     * assign语句
     */
    private static TreeNode parseAssignStmt() throws ParserException {
        TreeNode node = new TreeNode(TreeNode.ASSIGN_STMT);
        node.setLeft(variableName());
        consumeNextToken(Token.ASSIGN);
        node.setMiddle(parseExp());
        consumeNextToken(Token.SEMI);
        return node;
    }
    
    /**
     * 语句block
     */
    private static TreeNode parseStmtBlock() throws ParserException {
        TreeNode node = new TreeNode(TreeNode.NULL);
        TreeNode header = node;
        TreeNode temp = null;
        consumeNextToken(Token.LBRACE);
        while (getNextTokenType() != Token.RBRACE) {//允许语句块中没有语句
            temp = parseStmt();
            node.setNext(temp);
            node = temp;
        }
        consumeNextToken(Token.RBRACE);
        return header;
    }
    
    /**
     * 表达式
     */
    private static TreeNode parseExp() throws ParserException {
        TreeNode node = new TreeNode(TreeNode.EXP);
        node.setDataType(Token.LOGIC_EXP);
        TreeNode leftNode = addtiveExp();
        if (checkNextTokenType(Token.EQ, Token.NEQ, Token.GT, Token.GET, Token.LT, Token.LET)) {
            node.setLeft(leftNode);
            node.setMiddle(logicalOp());
            node.setRight(addtiveExp());
        } else {
            return leftNode;
        }
        return node;
    }
    
    /**
     * 多项式
     */
    private static TreeNode addtiveExp() throws ParserException {
        TreeNode node = new TreeNode(TreeNode.EXP);
        node.setDataType(Token.ADDTIVE_EXP);
        TreeNode leftNode = term();
        if (checkNextTokenType(Token.PLUS)) {
            node.setLeft(leftNode);
            node.setMiddle(addtiveOp());
            node.setRight(addtiveExp());
        } else if (checkNextTokenType(Token.MINUS)) {
            node.setLeft(leftNode);
            TreeNode opnode = new TreeNode(TreeNode.OP);
            opnode.setDataType(Token.PLUS);
            node.setMiddle(opnode);
            node.setRight(addtiveExp());
        } else {
            return leftNode;
        }
        return node;
    }
    
    /**
     * 项
     */
    private static TreeNode term() throws ParserException {
        TreeNode node = new TreeNode(TreeNode.EXP);
        node.setDataType(Token.TERM_EXP);
        TreeNode leftNode = factor();
        if (checkNextTokenType(Token.MUL, Token.DIV)) {
            node.setLeft(leftNode);
            node.setMiddle(multiplyOp());
            node.setRight(term());
        } else {
            return leftNode;
        }
        return node;
    }
    
    /**
     * 因子
     */
    private static TreeNode factor() throws ParserException {
        if (iterator.hasNext()) {
            TreeNode expNode = new TreeNode(TreeNode.FACTOR);
            switch (getNextTokenType()) {
            case Token.LITERAL_INT:
            case Token.LITERAL_REAL:
                expNode.setLeft(litreal());
                break;
            case Token.LPARENT:
                consumeNextToken(Token.LPARENT);
                expNode = parseExp();
                consumeNextToken(Token.RPARENT);
                break;
            case Token.MINUS:
                expNode.setDataType(Token.MINUS);
                currentToken = iterator.next();
                expNode.setLeft(term());
                break;
            case Token.PLUS:
                currentToken = iterator.next();
                expNode.setLeft(term());
                break;
            default:
                //返回的不是expNode
                return variableName();
            }
            return expNode;
        }
        throw new ParserException("line " + getNextTokenLineNo() + " : next token should be factor");
    }
    
    private static TreeNode litreal() throws ParserException {
        if (iterator.hasNext()) {
            currentToken = iterator.next();
            int type = currentToken.getType();
            TreeNode node = new TreeNode(TreeNode.LITREAL);
            node.setDataType(type);
            node.setValue(currentToken.getValue());
            if (type == Token.LITERAL_INT || type == Token.LITERAL_REAL) {
                return node;
            } else {
                // continue execute until throw
            }
        }
        throw new ParserException("line " + getNextTokenLineNo() + " : next token should be litreal value");
    }
    
    /**
     * 逻辑运算符
     */
    private static TreeNode logicalOp() throws ParserException {
        if (iterator.hasNext()) {
            currentToken = iterator.next();
            int type = currentToken.getType();
            if (type == Token.EQ
                    || type == Token.GET
                    || type == Token.GT
                    || type == Token.LET
                    || type == Token.LT
                    || type == Token.NEQ) {
                TreeNode node = new TreeNode(TreeNode.OP);
                node.setDataType(type);
                return node;
            }
        }
        throw new ParserException("line " + getNextTokenLineNo() + " : next token should be logical operator");
    }
    
    /**
     * 加减运算符
     */
    private static TreeNode addtiveOp() throws ParserException {
        if (iterator.hasNext()) {
            currentToken = iterator.next();
            int type = currentToken.getType();
            if (type == Token.PLUS || type == Token.MINUS) {
                TreeNode node = new TreeNode(TreeNode.OP);
                node.setDataType(type);
                return node;
            }
        }
        throw new ParserException("line " + getNextTokenLineNo() + " : next token should be addtive operator");
    }
    
    /**
     * 乘除运算符
     */
    private static TreeNode multiplyOp() throws ParserException {
        if (iterator.hasNext()) {
            currentToken = iterator.next();
            int type = currentToken.getType();
            if (type == Token.MUL || type == Token.DIV) {
                TreeNode node = new TreeNode(TreeNode.OP);
                node.setDataType(type);
                return node;
            }
        }
        throw new ParserException("line " + getNextTokenLineNo() + " : next token should be multiple operator");
    }
    
    /**
     * 变量名,可能是单个的变量,也可能是数组的一个元素
     */
    private static TreeNode variableName() throws ParserException {
        TreeNode node = new TreeNode(TreeNode.VAR);
        if (checkNextTokenType(Token.ID)) {
            currentToken = iterator.next();
            node.setValue(currentToken.getValue());
        } else {
            throw new ParserException("line " + getNextTokenLineNo() + " : next token should be ID");
        }
        if (getNextTokenType() == Token.LBRACKET) {
            consumeNextToken(Token.LBRACKET);
            node.setLeft(parseExp());
            consumeNextToken(Token.RBRACKET);
        }
        return node;
    }
    
    /**
     * 消耗掉下一个token,要求必须是type类型,消耗之后currentToken值将停在最后消耗的token上
     */
    private static void consumeNextToken(int type) throws ParserException {
        if (iterator.hasNext()) {
            currentToken = iterator.next();
            if (currentToken.getType() == type) {
                return;
            }
        }
        throw new ParserException("line " + getNextTokenLineNo() + " : next token should be -> " + new Token(type, 0));
    }
    
    /**
     * 检查下一个token的类型是否和type中的每一个元素相同,调用此函数currentToken位置不会移动
     */
    private static boolean checkNextTokenType(int ... type) {
        if (iterator.hasNext()) {
            int nextType = iterator.next().getType();
            iterator.previous();
            for (int each : type) {
                if (nextType == each) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * 获取下一个token的type,如果没有下一个token,则返回{ Token#NULL}
     */
    private static int getNextTokenType() {
        if (iterator.hasNext()) {
            int type = iterator.next().getType();
            iterator.previous();
            return type;
        }
        return Token.NULL;
    }
    
    /**
     * 获取下一个token的lineNo,如果没有下一个token,则返回-1
     */
    private static int getNextTokenLineNo() {
        if (iterator.hasNext()) {
            int lineNo = iterator.next().getLineNo();
            iterator.previous();
            return lineNo;
        }
        return -1;
    }
}
