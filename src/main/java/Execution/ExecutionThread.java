package Execution;

import CMDListGenerator.GenCMDList;
import java.io.File;
import java.util.ArrayList;
import java.util.Set;

public class ExecutionThread{

    int currMrID;
    String projBasePath;
    ArrayList<String> currFNames;
    Set<Integer> linesOfInterest;
    GenCMDList generator;
    File programUnderTest;
    ArrayList<String> genGcovCMDList;
    ArrayList<String> analyseCMDList;

    public ExecutionThread(int currMrID, String projBasePath, ArrayList<String> currFNames, Set<Integer> linesOfInterest, GenCMDList generator, File programUnderTest,
                           ArrayList<String> genGcovCMDList, ArrayList<String> analyseCMDList){
        this.currMrID = currMrID;
        this.projBasePath = projBasePath;
        this.currFNames = currFNames;
        this.linesOfInterest = linesOfInterest;
        this.generator = generator;
        this.programUnderTest = programUnderTest;
        this.genGcovCMDList = genGcovCMDList;
        this.analyseCMDList = analyseCMDList;
    }

    public String[] call() throws InterruptedException{
        String[] stmtCovResultsOfMTGs = new String[currFNames.size()];
        String currLine;
        String[] arrs = null;
        String currMTGId;
        String sourceInFNames;
        String followInFNames;

        ArrayList<String> execCMDList;
        File execBaseDir;
        File analyseBaseDir = programUnderTest.getParentFile();
        TestDataExecutor exec;
        ArrayList<Integer> sourceReturn;
        ArrayList<Integer> followReturn;
        StringBuilder builder;
        for (int mtgIndex = 0; mtgIndex < currFNames.size(); mtgIndex ++){
            currLine = currFNames.get(mtgIndex);
            arrs = currLine.split("\\|");
            if (arrs.length != 3){
                System.out.println("Invalid input file names!!!" + currLine);
                break;
            }
            currMTGId = arrs[0];
            sourceInFNames = arrs[1];
            followInFNames = arrs[2];
            execBaseDir = new File(projBasePath + "/mtgs/" + currMrID + "/" + currMTGId);

            execCMDList = generator.genCMDList(programUnderTest.getAbsolutePath(), execBaseDir, sourceInFNames);
            exec = new TestDataExecutor(execCMDList, execBaseDir, genGcovCMDList, analyseCMDList, analyseBaseDir);
            sourceReturn = exec.execute();
            execCMDList = generator.genCMDList(programUnderTest.getAbsolutePath(), execBaseDir, followInFNames);
            exec = new TestDataExecutor(execCMDList, execBaseDir, genGcovCMDList, analyseCMDList, analyseBaseDir);
            followReturn = exec.execute();
            builder = new StringBuilder();
            builder.append(mtgIndex);
            builder.append("|");
            builder.append("[");
            for (Integer num: sourceReturn){
                if (this.linesOfInterest.contains(num)){
                    builder.append(num);
                    builder.append(",");
                }
            }
            builder.deleteCharAt(builder.length()-1);
            builder.append("]|[");
            for (Integer num: followReturn){
                if (this.linesOfInterest.contains(num)){
                    builder.append(num);
                    builder.append(",");
                }
            }
            builder.deleteCharAt(builder.length()-1);
            builder.append("]");
            stmtCovResultsOfMTGs[mtgIndex] = builder.toString();
        }
        return stmtCovResultsOfMTGs;
    }
}
