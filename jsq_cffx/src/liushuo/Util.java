package liushuo;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;

/*读取要进行词法分析的文件*/
public class Util {

    public static LinkedList<Token> getTokenList(String filestr) throws IOException {
//        FileReader fr;
//        fr = new FileReader(filestr);
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filestr), "UTF-8"));
        LinkedList<Token> tokenList = Lexer.lexicalAnalyse(br);
        br.close();
//        fr.close();
        return tokenList;
    }
}
