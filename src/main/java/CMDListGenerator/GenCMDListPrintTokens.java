package CMDListGenerator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class GenCMDListPrintTokens implements GenCMDList{

    ArrayList<String> cmdPrefix;

    public GenCMDListPrintTokens(){}

    public GenCMDListPrintTokens(ArrayList<String> prefix){
        this.cmdPrefix = prefix;
    }

    @Override
    public void initialize(ArrayList<String> cmdList){
        this.cmdPrefix = cmdList;
    }

    @Override
    public ArrayList<String> genCMDList(String pathOfProgramUnderTest, File execBaseDir, String additionalParaFileName){
        ArrayList<String> cmdFull = new ArrayList<>();
        cmdFull.addAll(this.cmdPrefix);
        cmdFull.add(pathOfProgramUnderTest);
        ArrayList<String> additionalParas = prepareAdditionalParas(execBaseDir, additionalParaFileName);
        cmdFull.addAll(additionalParas);
        return cmdFull;
    }

    public ArrayList<String> prepareAdditionalParas(File execBaseDir, String additionalParaFileNames){
        String[] fileNameArray = additionalParaFileNames.split(" ");
        return new ArrayList<>(Arrays.asList(fileNameArray));
    }
}
