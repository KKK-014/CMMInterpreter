package liushuo;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedList;

public class Lexer {
    private static BufferedReader mBufferedReader;
    private static int currentInt;//当前读取的字符的ASCII码
    private static char currentChar;//当前读取的字符
    private static int lineNo;//当前行数

    public static LinkedList<Token> lexicalAnalyse(BufferedReader br) throws IOException {

        lineNo = 1;
        mBufferedReader = br;
        LinkedList<Token> tokenList = new LinkedList<Token>();
        StringBuilder sb = new StringBuilder();

        readChar();
        a:while(currentInt != -1) {

            //忽略空格、制表符等间隔符
            if (endOfSegment(currentChar)) {
                readChar();
                continue;
            }

            //最开始，排除掉所有非法字符，如@
            if(currentChar=='~'|| currentChar=='!'
            ||currentChar=='@'||currentChar=='#'
            ||currentChar=='$'||currentChar=='%'
            ||currentChar=='^'||currentChar=='&'
            ||currentChar=='`'||currentChar=='|'
            ||currentChar=='\\'||currentChar==':'
            ||currentChar=='"'||currentChar=='\''
            ||currentChar=='?'){
                tokenList.add(new Token(Token.ILLEGAL_INPUT,sb.toString(),lineNo));
                break a;
            }

            //简单特殊符号
            switch (currentChar) {
                case ';':
                    tokenList.add(new Token(Token.SEMI, ";",lineNo));
                    readChar();
                    continue;
                case '+':
                    tokenList.add(new Token(Token.PLUS,"+", lineNo));
                    readChar();
                    continue;
                case '-':
                    tokenList.add(new Token(Token.MINUS,"-",lineNo));
                    readChar();
                    continue;
                case '*':
                    tokenList.add(new Token(Token.MUL, "*",lineNo));
                    readChar();
                    continue;
                case '(':
                    tokenList.add(new Token(Token.LPARENT,"(", lineNo));
                    readChar();
                    continue;
                case ')':
                    tokenList.add(new Token(Token.RPARENT,")", lineNo));
                    readChar();
                    continue;
                case '[':
                    tokenList.add(new Token(Token.LBRACKET,"[", lineNo));
                    readChar();
                    continue;
                case ']':
                    tokenList.add(new Token(Token.RBRACKET,"]", lineNo));
                    readChar();
                    continue;
                case '{':
                    tokenList.add(new Token(Token.LBRACE, "{",lineNo));
                    readChar();
                    continue;
                case '}':
                    tokenList.add(new Token(Token.RBRACE,"}", lineNo));
                    readChar();
                    continue;
            }

            //处理"/"可能出现的多种情况：多行注释，单行注释，除号
            if (currentChar == '/') {
                readChar();
                if (currentChar == '*') {//多行注释
                    int errorNo=lineNo;
                    readChar();
                    while (true) {//死循环，忽略多行注释的内部字符
                        if (currentChar == '*') {//如果是*,那么有可能是多行注释结束的地方
                            readChar();
                            if (currentChar == '/') {//多行注释结束
                                readChar();
                                break;
                            }
                        } else {//如果不是*就继续读下一个,相当于忽略了这个字符
                            readChar();
                        }
                        if(currentInt==-1){//报错，注释未闭合
                            tokenList.add(new Token(Token.COMMENT_NOT_CLOSED,sb.toString(),errorNo));
                            break a;
                        }
                    }
                    continue;
                } else if (currentChar == '/') {//单行注释
                    while (currentChar != '\n') {//消耗这一行之后的内容
                        readChar();
                        if(currentInt==-1){
                            break a;
                        }
                    }
                    continue;
                } else {//是除号
                    tokenList.add(new Token(Token.DIV,"/", lineNo));
                    continue;
                }

            //处理 "="可能出现的多种情况，==和=
            } else if (currentChar == '=') {
                readChar();
                if (currentChar == '=') {
                    tokenList.add(new Token(Token.EQ,"==", lineNo));
                    readChar();
                } else {
                    tokenList.add(new Token(Token.ASSIGN,"=", lineNo));
                }
                continue;
            }

            //处理">"的多种情况，>=和>
            else if (currentChar == '>') {
                readChar();
                if (currentChar == '=') {
                    tokenList.add(new Token(Token.GET,">=", lineNo));
                    readChar();
                } else {
                    tokenList.add(new Token(Token.GT, ">",lineNo));
                }
                continue;
            }

            //处理"<"的多种情况，<=和<>和<
            else if (currentChar == '<') {
                readChar();
                if (currentChar == '=') {
                    tokenList.add(new Token(Token.LET,"<=", lineNo));
                    readChar();
                } else if (currentChar == '>') {
                    tokenList.add(new Token(Token.NEQ,"<>", lineNo));
                    readChar();
                } else {
                    tokenList.add(new Token(Token.LT,"<", lineNo));
                }
                continue;
            }

            //以下划线开头的标识符
            if(currentChar=='_'){
                while((currentChar >= '0' && currentChar <= '9')
                        || (currentChar >= 'a' && currentChar <= 'z')
                        || (currentChar >= 'A' && currentChar <= 'Z')
                        || currentChar == '_') {
                    sb.append(currentChar);
                    readChar();
                }
                    Token token = new Token(lineNo);
                    String sbString = sb.toString();
                    token.setType(Token.ID);
                    token.setValue(sbString);
                    sb.delete(0, sb.length());
                    tokenList.add(token);
                    continue;
            }

            //数字
            if (currentChar >= '0' && currentChar <= '9') {
                boolean isReal = false;//是否为实数
                boolean isError=false;//是否为非法变量值
                boolean isPoint=false;//判断是否只有一个小数点
                while ((currentChar >= '0' && currentChar <= '9')
                        || currentChar == '.'
                        || (currentChar >= 'a' && currentChar <= 'z')
                        || (currentChar >= 'A' && currentChar <= 'Z')
                        || currentChar == '_') {
                    if((currentChar >= 'a' && currentChar <= 'z')
                            || (currentChar >= 'A' && currentChar <= 'Z')
                            || currentChar== '_') {//以数字开头的非法变量
                        while (true) {
                            if (endOfSegment(currentChar) || currentChar==';') {
                                tokenList.add(new Token(Token.NUM_BEGIN_ERROR, sb.toString(), lineNo));
                                isError=true;
                                break;
                            }
                            else {
                                sb.append(currentChar);
                                readChar();
                            }
                        }
                    }
                    if (currentChar == '.') {
                        sb.append(".");//字符串连接
                        if (isReal) {//多个小数点
                            while (true){
                                if (endOfSegment(currentChar) || currentChar==';') {
                                    tokenList.add(new Token(Token.POINT_POINT, sb.toString(), lineNo));
                                    isError=true;
                                    break;
                                } else {
                                    readChar();
                                    sb.append(currentChar);
                                }
                            }
                        }
                        else {
                            isReal = true;
                            //往后读一位
                            readChar();
                            if(endOfSegment(currentChar) || !isNum(currentChar)) {//小数未正确闭合
                                tokenList.add(new Token(Token.POINT_NOT_CLOSED, sb.toString(), lineNo));
                                isError = true;
                                isPoint = true;
                                continue;
                            }
                        }
                    }
                    if(!isPoint && isNum(currentChar)) {//接着读数字
                        sb.append(currentChar);
                        readChar();
                    }
                }
                if(!isError) {//数字开头的segment没有问题
                    if (isReal) {
                        tokenList.add(new Token(Token.LITERAL_REAL, sb.toString(), lineNo));
                    } else {
                        tokenList.add(new Token(Token.LITERAL_INT, sb.toString(), lineNo));
                    }
                }
                sb.delete(0, sb.length());//清空0<=索引<sb.length()
                continue;
            }

            //字符组成的标识符,包括保留字和ID
            if ((currentChar >= 'a' && currentChar <= 'z'
                    ||(currentChar>= 'A' && currentChar <= 'Z'))) {
                //取剩下的可能是的字符
                Boolean id_value_error  = false;
                while ((currentChar >= 'a' && currentChar <= 'z')
                        || (currentChar >= 'A' && currentChar <= 'Z')
                        || currentChar== '_'
                        || (currentChar>= '0' && currentChar <= '9')) {
                    sb.append(currentChar);
                    //判断是否以下划线结尾
                    if(currentChar=='_') {
                        readChar();
                        if(endOfSegment(currentChar))
                        {
                            //遇到以下划线为结尾的变量
                            id_value_error = true;
                            break;
                        }
                    }
                    else {
                        readChar();
                    }
                }
                Token token = new Token(lineNo);
                String sbString = sb.toString();
                if (id_value_error) {
                    token.setType(Token.VAR_ERROR);
                    token.setValue(sbString);
                }
                else{
                    if (sbString.equals("if")) {
                        token.setType(Token.IF);
                        token.setValue("if");
                    } else if (sbString.equals("else")) {
                        token.setType(Token.ELSE);
                        token.setValue("else");
                    } else if (sbString.equals("while")) {
                        token.setType(Token.WHILE);
                        token.setValue("while");
                    } else if (sbString.equals("read")) {
                        token.setType(Token.READ);
                        token.setValue("read");
                    } else if (sbString.equals("write")) {
                        token.setType(Token.WRITE);
                        token.setValue("write");
                    } else if (sbString.equals("int")) {
                        token.setType(Token.INT);
                        token.setValue("int");
                    } else if (sbString.equals("real")) {
                        token.setType(Token.REAL);
                        token.setValue("real");
                    } else {
                        token.setType(Token.ID);
                        token.setValue(sbString);
                    }
                }
                sb.delete(0, sb.length());
                tokenList.add(token);
                continue;
            }

            //如果以上情况都无法匹配，则跳出，报错
            else{
                while (true) {
                    if (endOfSegment(currentChar)) {
                        tokenList.add(new Token(Token.VAR_ERROR, sb.toString(),lineNo));
                        break;
                    }
                    else {
                        sb.append(currentChar);
                        readChar();
                    }
                }
                sb.delete(0, sb.length());
            }
            readChar();
        }
        return tokenList;
    }

    //忽略间隔符：换行、回车、水平制表、换页
    private static boolean endOfSegment(char currentChar) {
        if(currentChar == '\n'    || currentChar == '\r'
                || currentChar == '\t' || currentChar == '\f'
                || currentChar == ' '  || currentChar == '\uFFFF') {
            return true;
        } else {
            return false;
        }
    }

    private static boolean isNum(char currentChar) {
        if(currentChar >= '0' && currentChar <= '9') {
            return true;
        } else {
            return false;
        }
    }
    private static void readChar() throws IOException {
        currentChar = (char) (currentInt = mBufferedReader.read());
        if (currentChar == '\n') {
            lineNo++;
        }
    }

}
