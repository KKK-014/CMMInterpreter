package liushuo;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.ArrayList;
import model.Token;
import model.TreeNode;
import model.Error;

public class Util {
    public static void println(String arg0) {
        System.out.println(arg0);
    }

    public static void println(char arg0) {
        System.out.println(arg0);
    }

    public static void println(Object arg0) {
        System.out.println(arg0);
    }


    public static LinkedList<Token> getTokenList(String filestr) throws IOException {
        FileReader fr;
        fr = new FileReader(filestr);
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filestr), "UTF-8"));
        LinkedList<Token> tokenList = Lexer.lexicalAnalyse(br);
        br.close();
        fr.close();
        if(Lexer.errors.size()!=0){
            return null;
        }
        return tokenList;
    }

    public static LinkedList<TreeNode> getNodeList(LinkedList<Token> tokenList) {
        LinkedList<TreeNode> nodeList = Parser.syntacticAnalyse(tokenList);
        if(Parser.errors.size()!=0){
            return null;
        }
        return nodeList;
    }

    public static void get_error_out(ArrayList<Error> errors){
        for(Error e: errors){
            System.out.println(e.toString());
        }
    }

    public static void get_token_out(LinkedList<Token> tokenList) {
        for(Token token : tokenList) {
            System.out.println(token.toStringWithLine()+", type: "+token.getType());
        }
    }

    public static void get_node_out(LinkedList<TreeNode> nodes){
        for(TreeNode node : nodes) {
            StringBuilder sb = new StringBuilder("");
            int indent=0;
            node.treeOutput(sb, node, indent);
            System.out.print(sb.toString());
        }
    }
}
