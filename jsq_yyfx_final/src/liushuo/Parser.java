package liushuo;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.ArrayList;

import model.Token;
import model.TreeNode;
import model.Error;

public class Parser {

    private static LinkedList<TreeNode> treeNodeList;
    private static Token currentToken = null;
    private static ListIterator<Token> iterator = null;

    public static  ArrayList<Error> errors;

    public static LinkedList<TreeNode> syntacticAnalyse(LinkedList<Token> tokenList) {
        treeNodeList = new LinkedList<TreeNode>();
        errors = new ArrayList<Error>();
        iterator = tokenList.listIterator();
        while (iterator.hasNext()) {
            treeNodeList.add(parseStmt());
        }
        if(errors.size()==0)
            return treeNodeList;
        else
            return null;
    }

    /**
     * 入口处的语句处理
     */
    private static TreeNode parseStmt()  {
        switch (getNextTokenType()) {
            case Token.IF: return parseIfStmt();
            case Token.WHILE: return parseWhileStmt();
            case Token.READ: return parseReadStmt();
            case Token.WRITE: return parseWriteStmt();
            case Token.INT: // same as REAL
            case Token.REAL: return parseDeclareStmt();
            case Token.ID: return parseAssignStmt();
            default:
                String S =(new Token(getNextTokenType(), 0)).toString();
                consumeNextToken(getNextTokenType());
                errorStmt(S);
                return new TreeNode(TreeNode.ERR);
        }
    }

    /**
     * if语句
     */
    private static TreeNode parseIfStmt() {
        TreeNode node = new TreeNode(TreeNode.IF_STMT);
        consumeNextToken(Token.IF);
        consumeNextToken(Token.LPARENT);
        node.setLeft(parseExp());
        consumeNextToken(Token.RPARENT);
        if(getNextTokenType()==Token.LBRACE)
        {
            node.setMiddle(parseStmtBlock());
        }else
            node.setMiddle(parseStmt());
        if (getNextTokenType() == Token.ELSE) {
            consumeNextToken(Token.ELSE);
            if(getNextTokenType()==Token.LBRACE)
            {
                node.setRight(parseStmtBlock());
            }else
                node.setRight(parseStmt());
        }
        return node;
    }

    /**
     * while语句
     */
    private static TreeNode parseWhileStmt() {
        TreeNode node = new TreeNode(TreeNode.WHILE_STMT);
        consumeNextToken(Token.WHILE);
        consumeNextToken(Token.LPARENT);
        node.setLeft(parseExp());
        consumeNextToken(Token.RPARENT);
        if(getNextTokenType()==Token.LBRACE)
        {
            node.setMiddle(parseStmtBlock());
        }else
            node.setMiddle(parseStmt());
        return node;
    }

    /**
     * read语句
     */
    private static TreeNode parseReadStmt() {
        TreeNode node = new TreeNode(TreeNode.READ_STMT);
        consumeNextToken(Token.READ);
        node.setLeft(variableName());
        consumeNextToken(Token.SEMI);
        return node;
    }

    /**
     * write语句
     */
    private static TreeNode parseWriteStmt() {
        TreeNode node = new TreeNode(TreeNode.WRITE_STMT);
        consumeNextToken(Token.WRITE);
        node.setLeft(parseExp());
        consumeNextToken(Token.SEMI);
        return node;
    }

    /**
     * declare语句
     */
    private static TreeNode parseDeclareStmt() {
        TreeNode node = new TreeNode(TreeNode.DECLARE_STMT);
        TreeNode varNode = new TreeNode(TreeNode.VAR);
        currentToken = iterator.next();
        int type = currentToken.getType();
        if (type == Token.INT) {
            varNode.setDataType(Token.INT);
        } else {//type == Token.REAL
            varNode.setDataType(Token.REAL);
        }

        if (checkNextTokenType(Token.ID)) {
            currentToken = iterator.next();
            varNode.setValue(currentToken.getValue());
        } else {
            errorStmt("ID");
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
    private static TreeNode parseAssignStmt()  {
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
    private static TreeNode parseStmtBlock()  {
        TreeNode node = new TreeNode(TreeNode.NULL);
        TreeNode header = node;
        TreeNode temp = null;
        consumeNextToken(Token.LBRACE);
        while (getNextTokenType() != Token.RBRACE && getNextTokenType() != Token.NULL
                &&getNextTokenType()!=Token.ELSE) {//允许语句块中没有语句
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
    private static TreeNode parseExp() {
        TreeNode node = new TreeNode(TreeNode.EXP);
        node.setDataType(Token.LOGIC_EXP);
        TreeNode leftNode = addtiveExp();
        if (checkNextTokenType(Token.EQ, Token.NEQ, Token.GT, Token.GET, Token.LT, Token.LET)) {
            node.setLeft(leftNode);
            node.setMiddle(logicalOp());
            node.setRight(addtiveExp());
        }else if(checkNextTokenType(Token.ASSIGN)){
            node.setLeft(leftNode);
            currentToken = iterator.next();
            int type = currentToken.getType();
            TreeNode anode = new TreeNode(TreeNode.OP);
            anode.setDataType(type);
            node.setMiddle(anode);
            node.setRight(addtiveExp());
        }else {
            return leftNode;
        }
        return node;
    }

    /**
     * 多项式
     */
    private static TreeNode addtiveExp() {
        TreeNode node = new TreeNode(TreeNode.EXP);
        node.setDataType(Token.ADDTIVE_EXP);
        TreeNode leftNode = term();
        if (checkNextTokenType(Token.PLUS, Token.MINUS)) {
            node.setLeft(leftNode);
            node.setMiddle(addtiveOp());
            node.setRight(addtiveExp());
        } else {
            return leftNode;
        }
        return node;
    }

    /**
     * 项
     */
    private static TreeNode term() {
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
    private static TreeNode factor() {
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
        errorStmt("factor");
        return new TreeNode(TreeNode.ERR);
    }

    private static TreeNode litreal()  {
        if (iterator.hasNext()) {
            currentToken = iterator.next();
            int type = currentToken.getType();
            TreeNode node = new TreeNode(TreeNode.LITREAL);
            node.setDataType(type);
            node.setValue(currentToken.getValue());
            if (type == Token.LITERAL_INT || type == Token.LITERAL_REAL) {
                return node;
            }
            // continue execute until throw
        }
        errorStmt("litreal value");
        return new TreeNode(TreeNode.ERR);
    }

    /**
     * 逻辑运算符
     */
    private static TreeNode logicalOp() {
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
        errorStmt("logical operator");
        return new TreeNode(TreeNode.ERR);
    }

    /**
     * 加减运算符
     */
    private static TreeNode addtiveOp() {
        if (iterator.hasNext()) {
            currentToken = iterator.next();
            int type = currentToken.getType();
            if (type == Token.PLUS || type == Token.MINUS) {
                TreeNode node = new TreeNode(TreeNode.OP);
                node.setDataType(type);
                return node;
            }
        }
        errorStmt("addtive operator");
        return new TreeNode(TreeNode.ERR);
    }

    /**
     * 乘除运算符
     */
    private static TreeNode multiplyOp()  {
        if (iterator.hasNext()) {
            currentToken = iterator.next();
            int type = currentToken.getType();
            if (type == Token.MUL || type == Token.DIV) {
                TreeNode node = new TreeNode(TreeNode.OP);
                node.setDataType(type);
                return node;
            }
        }
        errorStmt("multiple operator");
        return new TreeNode(TreeNode.ERR);
    }

    /**
     * 变量名,可能是单个的变量,也可能是数组的一个元素
     */
    private static TreeNode variableName() {
        TreeNode node = new TreeNode(TreeNode.VAR);
        if (checkNextTokenType(Token.ID)) {
            currentToken = iterator.next();
            node.setValue(currentToken.getValue());
        }
        else {
            errorStmt("ID");
            return new TreeNode(TreeNode.ERR);
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
    private static void consumeNextToken(int type) {
        if (iterator.hasNext()) {
            if (checkNextTokenType(type)) {
                currentToken = iterator.next();
                return;
            }
        }
        String S =(new Token(type, 0)).toString();
        errorStmt(S);
        return;
    }

    /**
     * 记录错误信息
     */
    private static void errorStmt(String S){
        Error node = new Error("lack or shouldn't be " + S, currentToken.getLineNo());
        errors.add(node);
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

