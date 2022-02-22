import javafx.util.Pair;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.Callable;

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

    public ArrayList<Integer> execute(){
        try{
            StringBuilder currExecReturn = new StringBuilder();
            //execution
            ProcessBuilder builder = new ProcessBuilder(execCmdList);
            builder.directory(execBaseDir);
            //long t1 = System.currentTimeMillis();
            Process execProcess = builder.start();
            InputStream in = execProcess.getInputStream();
            InputStreamReader inr = new InputStreamReader(in);
            int temp;
            while((temp = inr.read()) != -1){
                //do nothing
            }
            inr.close();
            in.close();
            execProcess.destroy();
            // gen gcov file
            builder = new ProcessBuilder(genGcovCMDList);
            builder.directory(analyseBaseDir);
            execProcess = builder.start();
            in = execProcess.getInputStream();
            inr = new InputStreamReader(in);
            while ((temp = inr.read())!= -1){
                //do nothing
            }
            inr.close();
            in.close();
            execProcess.destroy();
            // analyse code cov
            builder = new ProcessBuilder(analyseCMDList);
            builder.directory(execBaseDir);
            execProcess = builder.start();
            in = execProcess.getInputStream();
            inr = new InputStreamReader(in);
            while ((temp = inr.read())!= -1){
                currExecReturn.append(temp);
            }
            inr.close();
            in.close();
            execProcess.destroy();
            String[] strs = currExecReturn.toString().split("\n");
            ArrayList<Integer> list = new ArrayList<>();
            for (String s : strs) {
                list.add(Integer.parseInt(s));
            }
            return list;
        }catch (IOException e){
            System.out.println("Error encountered while executing program under tests!!");
            return null;
        }
    }
}
