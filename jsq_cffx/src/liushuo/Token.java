package  liushuo;

public class Token {
    /** if */
    public static final int IF = 1;
    /** else */
    public static final int ELSE = 2;
    /** while */
    public static final int WHILE = 3;
    /** read */
    public static final int READ = 4;
    /** write */
    public static final int WRITE = 5;
    /** int */
    public static final int INT = 6;
    /** real */
    public static final int REAL = 7;
    /** + */
    public static final int PLUS = 8;
    /** - */
    public static final int MINUS = 9;
    /** * */
    public static final int MUL = 10;
    /** / */
    public static final int DIV = 11;
    /** = */
    public static final int ASSIGN = 12;
    /** < */
    public static final int LT = 13;
    /** == */
    public static final int EQ = 14;
    /** <> */
    public static final int NEQ = 15;
    /** ( */
    public static final int LPARENT = 16;
    /** ) */
    public static final int RPARENT = 17;
    /** ; */
    public static final int SEMI = 18;
    /** { */
    public static final int LBRACE = 19;
    /** } */
    public static final int RBRACE = 20;
    /** /* */
    public static final int LCOM = 21;
    /** *\/ */
    public static final int RCOM = 22;
    /** // */
    public static final int SCOM = 23;
    /** [ */
    public static final int LBRACKET = 24;
    /** ] */
    public static final int RBRACKET = 25;
    /** <= */
    public static final int LET = 26;
    /** > */
    public static final int GT = 27;
    /** >= */
    public static final int GET = 28;
    /** 标识符,由数字,字母或下划线组成,首字符不能是数字 */
    public static final int ID = 29;
    /** int型字面值 */
    public static final int LITERAL_INT = 30;
    /** real型字面值 */
    public static final int LITERAL_REAL = 31;
    /*各情况都无法匹配，默认报错*/
    public static final int VAR_ERROR = 32;
    /*注释未闭合*/
    public static final int COMMENT_NOT_CLOSED=33;
    /*以数字开头的错误变量*/
    public static final int NUM_BEGIN_ERROR=34;
    /*多个小数点错误，如3.6.0*/
    public static final int POINT_POINT=35;
    /*小数点未闭合错误，如3.*/
    public static final int POINT_NOT_CLOSED=36;
    /*最初，排除所有非法字符，如@*/
    public static final int ILLEGAL_INPUT=37;

    private int type;
    /**
     * 如果一个token需要值,则使用这个存储,比如ID,LITERAL_INT,LITERAL_REAL,LITERAL_BOOL
     */
    private String value;
    private int lineNo;

    public Token(int lineNo) {
        this(0, lineNo);
    }

    public Token(int type, int lineNo) {
        this(type, null, lineNo);
    }

    public Token(int type, String value, int lineNo) {
        super();
        this.type = type;
        this.value = value;
        this.lineNo = lineNo;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getLineNo() {
        return lineNo;
    }

    public void setLineNo(int lineNo) {
        this.lineNo = lineNo;
    }

    public String toStringWithLine() {
        switch (type) {
            case IF: return  + this.lineNo + ": IF"+", 值为："+"if";
            case ELSE: return  + this.lineNo + ": ELSE"+", 值为："+"else";
            case WHILE: return  + this.lineNo + ": WHILE"+", 值为："+"while";
            case READ: return  + this.lineNo + ": READ"+", 值为："+"read";
            case WRITE: return  + this.lineNo + ": WRITE"+", 值为："+"write";
            case INT: return  + this.lineNo + ": INT"+", 值为："+"int";
            case REAL: return  + this.lineNo + ": REAL"+", 值为："+"real";
            case PLUS: return  + this.lineNo + ": 运算符"+"，值为："+"+";
            case MINUS: return  + this.lineNo + ": 运算符"+"，值为："+"-";
            case MUL: return  + this.lineNo + ": 运算符"+"，值为："+"*";
            case DIV: return  + this.lineNo + ": 运算符"+"，值为："+"/";
            case ASSIGN: return  + this.lineNo + ": 运算符"+"，值为："+"=";
            case LT: return  + this.lineNo + ": 运算符"+"，值为："+"<";
            case EQ: return  + this.lineNo + ": 运算符"+"，值为："+"==";
            case NEQ: return  + this.lineNo +": 运算符"+"，值为："+"<>";
            case LPARENT: return  + this.lineNo + ": 括号"+"，值为："+"(";
            case RPARENT: return  + this.lineNo + ": 括号"+"，值为："+")";
            case SEMI: return  + this.lineNo + ": 分号"+"，值为："+";";
            case LBRACE: return  + this.lineNo + ": 括号"+"，值为："+"{";
            case RBRACE: return  + this.lineNo + ": 括号"+"，值为："+"}";
            case LCOM: return  + this.lineNo +":LCOM";
            case RCOM: return  + this.lineNo +":RCOM";
            case SCOM: return + this.lineNo + ":SCOM";
            case LBRACKET: return  + this.lineNo +": 括号"+"，值为："+"[";
            case RBRACKET: return  + this.lineNo +": 括号"+"，值为："+"]";
            case LET: return  + this.lineNo + ": 运算符"+"，值为："+"<=";
            case GT: return  + this.lineNo +": 运算符"+"，值为："+">";
            case GET: return  + this.lineNo + ": 运算符"+"，值为："+">=";
            case ID:  return + this.lineNo+"：ID"+", 值为："+this.value;
            case LITERAL_INT:
            case LITERAL_REAL: return  + this.lineNo + ": 实数"+"，值为：" + this.value;
            case VAR_ERROR:return  + this.lineNo + ": " + "错误"+", 值为："+this.value;
            case COMMENT_NOT_CLOSED:return  + this.lineNo + ": " + "错误"+", 注释未闭合"+this.value;
            case NUM_BEGIN_ERROR:return  + this.lineNo + ": " + "错误"+", 标识符以数字开头，"+this.value;
            case POINT_POINT:return  + this.lineNo + ": " + "错误"+", 多个小数点，"+this.value;
            case POINT_NOT_CLOSED:return  + this.lineNo + ": " + "错误"+", 小数未正确闭合，"+this.value;
            case ILLEGAL_INPUT:return  + this.lineNo + ": " + "错误"+", 请不要输入非法字符"+this.value;
            default: return  + this.lineNo + ": 未知";
        }
    }
}
