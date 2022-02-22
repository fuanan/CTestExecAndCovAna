package CMDListGenerator;

import java.io.*;
import java.util.ArrayList;

public class GenCMDListGrep implements GenCMDList {
    ArrayList<String> cmdPrefix;

    public GenCMDListGrep(){}

    public GenCMDListGrep(ArrayList<String> prefix){
        this.cmdPrefix = prefix;
    }

    @Override
    public void initialize(ArrayList<String> cmdList){
        this.cmdPrefix = cmdList;
    }

    @Override
    public ArrayList<String> genCMDList(String pathOfProgramUnderTest, File execBaseDir, String additionalParaFileName){
        //cmdList for grep
        //timeout -s 9 0.1s [path of program under test] -E regex [content of regex] -n --text [name of input file]
        ArrayList<String> cmdFull = new ArrayList<>();
        cmdFull.addAll(this.cmdPrefix);
        cmdFull.add(pathOfProgramUnderTest);
        cmdFull.add("-E");
        cmdFull.add("-e");
        File regexFile = new File(execBaseDir + "/" + additionalParaFileName);
        try{
            FileInputStream fis = new FileInputStream(regexFile);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            String line = br.readLine();
            cmdFull.add(line);
            br.close();
            isr.close();
            fis.close();
        }catch(IOException e){
            e.printStackTrace();
        }
        cmdFull.add("-n");
        cmdFull.add("--text");
        cmdFull.add("input_file_0");
        return cmdFull;
    }
}
