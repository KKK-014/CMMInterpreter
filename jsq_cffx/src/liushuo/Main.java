package  liushuo;
import java.io.IOException;
import java.util.LinkedList;

public class Main {

    public static void main(String[] args) {

        try {
            java.util.Scanner input = new java.util.Scanner (System.in);
            System.out.println("请输入您要分析的文件地址：");
            String inputpath=input.next();
            //获取tokenList
            LinkedList<Token> tokenList = Util.getTokenList(inputpath);
            //打印分析结果
            printToken(tokenList);
        } catch (IOException e) {
            System.out.println(args[0] + "文件不存在");
            e.printStackTrace();
        }
    }

    //打印分析结果
    private static void printToken(LinkedList<Token> tokenList) {
        for (int j = 0; j < tokenList.size(); j++) {
            for(Token token : tokenList) {
                if(token.getLineNo()==j+1){
                    System.out.println("\t"
                            +token.toStringWithLine()+"，种别码为： "+
                            token.getType());
                }
            }
        }
    }
}


