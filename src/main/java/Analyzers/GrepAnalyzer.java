package Analyzers;

import CMDListGenerator.GenCMDList;
import CMDListGenerator.GenCMDListGrep;

import java.util.ArrayList;

public class GrepAnalyzer {

    public static void main(String[] args){
        String origProgName = "grep";
        String projBasePath = "/home/anfu/testexecfolder/grep/group-20211022-test-exec";
        GenCMDList generator = new GenCMDListGrep();
        int numOfTestExecThreads = 12;
        generator.initialize(new ArrayList<>());
        GeneralAnalyzer analyzer = new GeneralAnalyzer(origProgName, projBasePath, generator, numOfTestExecThreads);
        analyzer.analyze();
    }
}
