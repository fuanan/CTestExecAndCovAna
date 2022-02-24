package Execution;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.ArrayList;

public class TestDataExecutor {

    ArrayList<String> execCmdList;
    File execBaseDir;
    ArrayList<String> genGcovCMDList;
    ArrayList<String> analyseCMDList;
    File analyseBaseDir;
    String[] envp = {""};


    //有些情况下，变异体会陷入无限循环，并输出很多内容。读取这些内容会花费大量时间，导致测试过程耗时很长。
    //在这种情况下，除了及时kill以外，还需要指定最大读入char的数量，以减少时间花费。
    //int maxOutputChars;
    public TestDataExecutor(ArrayList<String> execCmdList, File execBaseDir, ArrayList<String> genGcovCMDList, ArrayList<String> analyseCMDList, File analyseBaseDir){
        this.execCmdList = execCmdList;
        this.execBaseDir = execBaseDir;
        this.genGcovCMDList = genGcovCMDList;
        this.analyseCMDList = analyseCMDList;
        this.analyseBaseDir = analyseBaseDir;
    }

    public ArrayList<Integer> execute() throws InterruptedException{
        try{
            StringBuilder currExecReturn = new StringBuilder();
            //execute
            Process execProcess = new ProcessBuilder(execCmdList).directory(execBaseDir).start();
            InputStream in = execProcess.getInputStream();
            InputStreamReader inr = new InputStreamReader(in);
            int temp;
            while((temp = inr.read()) != -1){/*do nothing*/}
            /* The following statement is necessary, since the instrumented program is still running
            * after the execution results have been printed on the screen.*/
            execProcess.waitFor();
            /******************/
            //currExecReturn.setLength(0);
            inr.close();
            in.close();
            execProcess.destroy();
            // gen gcov file
            execProcess = new ProcessBuilder(genGcovCMDList).directory(analyseBaseDir).start();
            in = execProcess.getInputStream();
            inr = new InputStreamReader(in);
            while ((temp = inr.read())!= -1){/*do nothing*/}
            /*******************/
            execProcess.waitFor();
            /*******************/
            inr.close();
            in.close();
            execProcess.destroy();
            // analyse code cov
            execProcess = new ProcessBuilder(analyseCMDList).directory(analyseBaseDir).start();
            in = execProcess.getInputStream();
            inr = new InputStreamReader(in);
            while ((temp = inr.read())!= -1){
                currExecReturn.append((char)temp);
            }
            execProcess.waitFor();
            inr.close();
            in.close();
            execProcess.destroy();
            String r = currExecReturn.toString();
            String[] strs = r.split("\n");
            ArrayList<Integer> list = new ArrayList<>();
            for (String s : strs) {
                if (StringUtils.isNumeric(s)){
                    list.add(Integer.parseInt(s));
                }else{
                    System.out.println("Invalid!");
                }
            }
            return list;
        }catch (IOException e){
            System.out.println("Error encountered while executing program under test!!");
            return null;
        }
    }
}
