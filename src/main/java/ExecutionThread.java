import CMDListGenerator.GenCMDList;
import javafx.util.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Callable;

public class ExecutionThread{

    int currMrID;
    String projBasePath;
    ArrayList<String> currFNames;
    GenCMDList generator;
    File programUnderTest;
    ArrayList<String> genGcovCMDList;
    ArrayList<String> analyseCMDList;

    public ExecutionThread(int currMrID, String projBasePath, ArrayList<String> currFNames, GenCMDList generator, File programUnderTest,
                           ArrayList<String> genGcovCMDList, ArrayList<String> analyseCMDList){
        this.currMrID = currMrID;
        this.projBasePath = projBasePath;
        this.currFNames = currFNames;
        this.generator = generator;
        this.programUnderTest = programUnderTest;
        this.genGcovCMDList = genGcovCMDList;
        this.analyseCMDList = analyseCMDList;
    }

    public Pair<Integer, String[]> call(){
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
            stmtCovResultsOfMTGs[mtgIndex] = mtgIndex + "|" + sourceReturn.toString() + "|" + followReturn.toString();
        }
        return new Pair<>(currMrID, stmtCovResultsOfMTGs);
    }
}
