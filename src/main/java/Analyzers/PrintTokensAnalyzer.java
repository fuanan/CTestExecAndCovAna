package Analyzers;

import CMDListGenerator.GenCMDList;
import CMDListGenerator.GenCMDListGrep;
import CMDListGenerator.GenCMDListPrintTokens;

import java.util.ArrayList;

public class PrintTokensAnalyzer {

    public static void main(String[] args){
        String origProgName = "print_tokens";
        String projBasePath = "/home/anfu/testexecfolder/print_tokens/group0";
        GenCMDList generator = new GenCMDListPrintTokens();
        int numOfTestExecThreads = 8;
        generator.initialize(new ArrayList<>());
        GeneralAnalyzer analyzer = new GeneralAnalyzer(origProgName, projBasePath, generator, numOfTestExecThreads);
        analyzer.analyze();
    }
}
