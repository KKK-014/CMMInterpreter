package model;
import java.util.*;

public class TreeNode {
    
    /**
     * 语句块使用链表存储,使用NULL类型的TreeNode作为头部,注意不要使用NULL的节点存储信息,仅仅使用next指向下一个TreeNode
     */
    public static final int NULL = 0;
    /**
     * if语句
     * left存放exp表达式
     * middle存放if条件正确时的TreeNode
     * right存放else的TreeNode，如果有的话
     */
    public static final int IF_STMT = 1;
    /**
     * left存储EXP
     * middle存储循环体
     */
    public static final int WHILE_STMT = 2;
    /**
     * left存储一个VAR
     */
    public static final int READ_STMT = 3;
    /**
     * left存储一个EXP
     */
    public static final int WRITE_STMT = 4;
    /**
     * 声明语句
     * left中存放VAR节点
     * 如果有赋值EXP,则存放中middle中
     */
    public static final int DECLARE_STMT = 5;
    /**
     * 赋值语句
     * left存放var节点
     * middle存放exp节点
     */
    public static final int ASSIGN_STMT = 6;
    /**
     * 复合表达式
     * 复合表达式则形如left middle right
     * 此时datatype为可能为  LOGIC_EXP ADDTIVE_EXP TERM_EXP
     * value==null
     */
    public static final int EXP = 7;
    /**
     * 变量
     * datatype存放变量类型Token.INT 和 REAL
     * value存放变量名
     * left:
     * 在声明语句中变量的left值代表变量长度exp,在其他的调用中变量的left代表变量索引值exp,若为null,则说明是单个的变量,不是数组
     * 不存储值
     */
    public static final int VAR = 8;
    /**
     * 运算符
     * 在datatype中存储操作符类型
     */
    public static final int OP = 9;

    /**
     * 因子
     * 有符号datatype存储TOKEN.PLUS/MINUS,默认为Token.PLUS
     * 只会在left中存放一个TreeNode
     * 如果那个TreeNode是var,代表一个变量/数组元素
     * 如果这个TreeNode是exp,则是一个表达式因子
     * 如果是LITREAL,该LITREAL的value中存放字面值的字符形式
     * EXP为因子时,mDataType存储符号PLUS/MINUS
     */
    public static final int FACTOR = 10;
    
    /**
     * 字面值
     * value中存放字面值,无符号
     * datatype存放类型,在TOKEN中
     */
    public static final int LITREAL = 11;
    /**
     * 错误情况
     * value中存储错误原因
    */
    public static final int ERR = 12;
    
    //当出现ERROR时，设为true，然后只输出错误提示
    public static boolean hasError = false;
    public static List<TreeNode> errList = new ArrayList<TreeNode>();

    private int type;
    private TreeNode mLeft;
    private TreeNode mMiddle;
    private TreeNode mRight;
    /**
     * 时存储变量类型,具体定义在{ Token}中INT / REAL
     * 时存储操作符类型,具体定义在{ Token}中 LT GT
     * 为{ TreeNode#EXP}时表示复合表达式
     * 为{ TreeNode#FACTOR}表示因子,mDataType处存储表达式的前置符号,具体定义在{ Token}
     * 中PLUS/MINUS, 默认为PLUS
     * 为{ TreeNode#LITREAL}表示字面值,存储类型
     */    
    private int mDataType;
    /**
     * 为{ TreeNode#FACTOR}时存储表达式的字符串形式的值
     * 为{ TreeNode#VAR}时存储变量名
     */
    private String value;
    /**
     * 如果是代码块中的代码,则mNext指向其后面的一条语句
     * 普通的顶级代码都是存在linkedlist中,不需要使用这个参数
     */
    private TreeNode mNext;
    
    public TreeNode(int type) {
        super();
        this.type = type;
        this.mLeft = null;
        this.mMiddle = null;
        this.mRight = null;
        this.mNext = null;
        switch (this.type) {
        case FACTOR:
        case LITREAL:
            this.mDataType = Token.PLUS;
            break;
        default:
            break;
        }
    }
    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }
    public TreeNode getLeft() {
        return mLeft;
    }
    public void setLeft(TreeNode mLeft) {
        this.mLeft = mLeft;
    }
    public TreeNode getMiddle() {
        return mMiddle;
    }
    public void setMiddle(TreeNode mMiddle) {
        this.mMiddle = mMiddle;
    }
    public TreeNode getRight() {
        return mRight;
    }
    public void setRight(TreeNode mRight) {
        this.mRight = mRight;
    }
    public TreeNode getNext() {
        return mNext;
    }
    public void setNext(TreeNode mNext) {
        this.mNext = mNext;
    }
    public int getDataType() {
        return mDataType;
    }
    public void setDataType(int type) {
        this.mDataType = type;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
    @Override
    public String toString() {
        switch (this.type) {
        case IF_STMT: return "IF_STMT";
        case WHILE_STMT: return "WHILE_STMT";
        case READ_STMT: return "READ_STMT";
        case WRITE_STMT: return "WRITE_STMT";
        case DECLARE_STMT: return "DECLARE_STMT";
        case ASSIGN_STMT: return "ASSIGN_STMT";
        case EXP:// return "EXP";
        case VAR: return this.value;// return "VAR";
        case OP: return new Token(this.mDataType).toString();// return "OP";
        case NULL: return "";// return "BLOCK HEADER";
        default: return "UNKNOWN";
        }
    }
    
    public void treeOutput(StringBuilder sb, TreeNode node, int indent){
        sb.append(printIndent(indent));
        //code_block
        if(node.getType() == 0){
            TreeNode temp = node;
            sb.append("{\n");
            while(temp.mNext != null){
                treeOutput(sb, temp.mNext, indent+2);
                temp = temp.mNext;
            }
            sb.append(printIndent(indent));
            sb.append("}\n");
        }
        
        //if_stmt
        if(node.getType() == 1) {
            sb.append("IF_STMT:\n");
            //左孩子exp
            if(node.mLeft!=null) {
                treeOutput(sb, node.mLeft, indent+2);
            }
            //中间是if代码块
            if(node.mMiddle!=null) {
                treeOutput(sb, node.mMiddle, indent+2);
            }
            //右孩子是else
            if(node.mRight!=null) {
                sb.append(printIndent(indent)+"ELSE:\n");
                treeOutput(sb, node.mRight, indent+2);
            }
        }

        //while_stmt
        if(node.getType() == 2) {
            sb.append("WHILE_STMT:\n");
            //左孩子exp
            if(node.mLeft!=null) {
                treeOutput(sb, node.mLeft, indent+2);
            }
            //middle循环体
            if(node.mMiddle!=null) {
                treeOutput(sb, node.mMiddle, indent+2);
            }
        }

        //read_stmt
        if(node.getType() == 3) {
            sb.append("READ:\n");
            //变量
            if(node.mLeft != null) {
                treeOutput(sb, node.mLeft, indent+2);
            }
        }

        //write_stmt
        if(node.getType()==4){
            sb.append("WRITE:\n");
            //exp
            if(node.mLeft != null){
                treeOutput(sb, node.mLeft, indent+2);
            }
            
        }

        //declare_stmt
        if(node.getType()==5) {
            sb.append("DECLARE_STMT:\n");
            //左孩子是变量
            if(node.mLeft!=null){
                treeOutput(sb, node.mLeft, indent+2);
                sb.append(printIndent(indent+2));
            }
            //中间是exp
            if(node.mMiddle!=null){
                treeOutput(sb, node.mMiddle, indent+2);
            }
        }

        //assign_stmt
        if(node.getType()==6){
            sb.append("ASSIGN_STMT:\n");
            //左孩子是变量
            if(node.mLeft!=null){
                treeOutput(sb, node.mLeft, indent+2);
                sb.append(printIndent(indent+2));
                sb.append(" = \n");
            }
            //中间是exp
            if(node.mMiddle!=null){
                treeOutput(sb, node.mMiddle, indent+2);
            }
        }

        //exp
        if(node.getType()==7){
            sb.append("EXP:\n");
            //左孩子exp
            if(node.mLeft!=null){
                treeOutput(sb, node.mLeft, indent+2);
            }
            //中间是if代码块
            if(node.mMiddle!=null){
                treeOutput(sb, node.mMiddle, indent+2);
            }
            //右孩子是else
            if(node.mRight!=null){
                treeOutput(sb, node.mRight, indent+2);
            }
        }

        //var
        if(node.getType()==8){
            sb.append("VAR: ");
            if(node.mDataType==Token.INT){
                sb.append("int: ");
                sb.append(node.value);
                sb.append('\n');
            }else{
                sb.append("real: ");
                sb.append(node.value);
                sb.append('\n');
            }
            if(node.mLeft!=null){
                sb.append(printIndent(indent));
                sb.append("array: ");
                sb.append(node.value);
                sb.append(" length: \n");
                treeOutput(sb, node.mLeft, indent+2);
            }
        }

        //op
        if(node.getType()==9){
            sb.append("OP: ");
            sb.append(this.getOp(node.mDataType));
            sb.append('\n');
        }

        //factor
        if(node.getType()==10){
            sb.append("FACTOR: \n");
            if(node.mLeft.type==8 || node.mLeft.type==7 || node.mLeft.type==11){
                treeOutput(sb, node.mLeft, indent+2);
            }
        }

        //literal
        if(node.getType()==11){
            sb.append("LITERAL: ");
            if(node.mDataType==Token.LITERAL_INT){
                sb.append("int: ");
            }else{
                sb.append("real: ");
            }
            sb.append(node.value);
            sb.append('\n');
        }
    }

    private String printIndent(int indent){
        String str = "";
        for(int i=0; i<indent; i++){
            str+="-";
        }
        return str;
    }

    private String getOp(int OpID) {
        switch(OpID){
            case 8: return "+";
            case 9: return "-";
            case 10: return "*";
            case 11: return "/";
            case 13: return "<";
            case 14: return "==";
            case 15: return "<>";
            case 26: return "<=";
            case 27: return ">";
            case 28: return ">=";
            default: return "";
        }
    }

}
