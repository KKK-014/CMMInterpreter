package liushuo;

import java.util.LinkedList;

import java.io.IOException;
import exception.InterpretException;
import model.FourCode;
import model.TreeNode;
import model.Token;

public class Main {

    private static SymbolTable symbolTable;

    public static void main(String[] args) {
        java.util.Scanner input = new java.util.Scanner(System.in);
        System.out.println("请输入您要分析的文件地址：");
        String inputpath = input.next();
        LinkedList<FourCode> codes;
        symbolTable = SymbolTable.getSymbolTable();
            try {
                symbolTable.newTable();
                codes = CodeGenerater.generateCode(inputpath);
                symbolTable.deleteTable();
                for (FourCode code : codes) {
                    System.out.println(code.toString());
                }
            } catch (InterpretException e) {
                System.out.println(e.toString());
            }
    }

}
