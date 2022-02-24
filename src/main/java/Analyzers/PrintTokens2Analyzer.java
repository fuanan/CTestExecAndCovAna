package Analyzers;

import CMDListGenerator.GenCMDList;
import CMDListGenerator.GenCMDListPrintTokens;

import java.util.ArrayList;

public class PrintTokens2Analyzer {

    public static void main(String[] args){
        String origProgName = "print_tokens2";
        String projBasePath = "/home/anfu/testexecfolder/print_tokens2/group-for-real-test";
        GenCMDList generator = new GenCMDListPrintTokens();
        int numOfTestExecThreads = 8;
        generator.initialize(new ArrayList<>());
        GeneralAnalyzer analyzer = new GeneralAnalyzer(origProgName, projBasePath, generator, numOfTestExecThreads);
        analyzer.analyze();
    }
}
