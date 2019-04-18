package liushuo;

import java.io.IOException;
import java.util.LinkedList;

import exception.ParserException;
import model.TreeNode;

public class Main {
    public static void main(String[] args) {
        try {
            java.util.Scanner input = new java.util.Scanner(System.in);
            System.out.println("请输入您要分析的文件地址：");
            String inputpath = input.next();
            LinkedList<TreeNode> nodes = Util.getNodeList(Util.getTokenList(inputpath));
            for(TreeNode node : nodes) {
                StringBuilder sb = new StringBuilder("");
                int indent=0;
                node.treeOutput(sb, node, indent);
                System.out.print(sb.toString());
            }
        }
        catch (IOException e) {
            System.out.println(args[0] + "文件不存在");
            e.printStackTrace();
        } catch (ParserException e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }
}
