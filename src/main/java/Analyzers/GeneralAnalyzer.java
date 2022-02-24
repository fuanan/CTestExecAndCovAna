package Analyzers;

import CMDListGenerator.GenCMDList;
import Execution.ExecutionThread;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class GeneralAnalyzer {

    String origProgName;
    String projBasePath;
    String linesOfInterestFilePath;
    GenCMDList generator;
    String covInfoOutputPath;
    int numOfTestExecThreads;

    public GeneralAnalyzer(String origProgName, String projBasePath, GenCMDList generator, int numOfTestExecThreads){
        this.origProgName = origProgName;
        this.projBasePath = projBasePath;
        this.generator = generator;
        this.numOfTestExecThreads = numOfTestExecThreads;
        this.covInfoOutputPath = projBasePath + File.separator + "code_cov_result";
        this.linesOfInterestFilePath = projBasePath + File.separator + "code_cov_lines_of_interest.txt";
    }

    public void analyze(){
        File basePath = new File(projBasePath);
        File covInfoOutputFolder = new File(covInfoOutputPath);
        File linesOfInterestFile = new File(linesOfInterestFilePath);
        if ( !basePath.exists() || !basePath.isDirectory() ){
            System.out.println(projBasePath + " does not exist!!");
        }else if (!covInfoOutputFolder.exists() || !covInfoOutputFolder.isDirectory()) {
            System.out.println(covInfoOutputPath + " does not exist!!");
        }else if (!linesOfInterestFile.exists()){
            System.out.println(linesOfInterestFilePath + "does not exist!!");
        }else{
            try{
                ArrayList<Pair<Integer, String>> mrIDsAndNames = readMRIDsAndNames(projBasePath);
                File[] origProgsForEachWorker = readOrigProgs(origProgName, projBasePath, numOfTestExecThreads);
                ArrayList<Integer> linesOfInterest = readLinesOfInterest(linesOfInterestFile);
                //allocate jobs
                int numOfMRs = mrIDsAndNames.size();
                int sizeOfJobs = (int)Math.ceil((double)numOfMRs/(double)(numOfTestExecThreads));

                ArrayList<Pair<Integer, String>>[] mrIDSets = new ArrayList[numOfTestExecThreads];
                for (int i = 0; i < numOfTestExecThreads; i ++){
                    int lowerBound = i * sizeOfJobs;
                    int upperBound = lowerBound + sizeOfJobs;
                    ArrayList<Pair<Integer, String>> current;

                    if (upperBound > numOfMRs){
                        current = new ArrayList<>(mrIDsAndNames.subList(lowerBound, mrIDsAndNames.size()));
                    }else{
                        current = new ArrayList<>(mrIDsAndNames.subList(lowerBound, upperBound));
                    }
                    mrIDSets[i] = current;
                }
                for (int i = 0; i < numOfTestExecThreads; i ++){
                    int threadID = i;
                    Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                execAndAnalyse(threadID, projBasePath, mrIDSets[threadID], origProgsForEachWorker[threadID], new HashSet<>(linesOfInterest), generator, covInfoOutputPath);
                            } catch (IOException | InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    Thread t = new Thread(r);
                    t.start();
                }
                System.out.println("Finish!");
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    public void execAndAnalyse(int threadID, String projBasePath, ArrayList<Pair<Integer, String>> mrIDsAndNames, File origProgUnderTest,
                               Set<Integer> linesOfInterest, GenCMDList generator, String outputFolderPath) throws IOException, InterruptedException{
        int currMrID;
        File inputFNames;
        FileInputStream fis;
        InputStreamReader isr;
        BufferedReader br;
        ArrayList<String> currFNames;

        ArrayList<String> genGcovCMDList = new ArrayList<>();
        genGcovCMDList.add("./2analyze_cov.sh");
        ArrayList<String> analyseCMDList = new ArrayList<>();
        analyseCMDList.add("./3extract_lines.sh");

        for(int i = 0; i < mrIDsAndNames.size(); i ++){
            long t1 = System.currentTimeMillis();
            currMrID = mrIDsAndNames.get(i).getKey();
            inputFNames = new File(projBasePath + "/mtgs/" + currMrID + "/inputFNames.txt");
            fis = new FileInputStream(inputFNames);
            isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);
            currFNames = new ArrayList<>();
            String currLine;
            while ((currLine = br.readLine()) !=null){
                currFNames.add(currLine);
            }
            String[] currResult = new ExecutionThread(currMrID, projBasePath, currFNames, linesOfInterest,
                    generator, origProgUnderTest, genGcovCMDList, analyseCMDList).call();
            FileWriter fw;
            BufferedWriter bw;
            File currMRResult = new File(outputFolderPath + File.separator + mrIDsAndNames.get(i).getValue() + ".txt");
            if (!currMRResult.exists()){
                currMRResult.createNewFile();
            }
            fw = new FileWriter(currMRResult, false);
            bw = new BufferedWriter(fw);
            for(String s: currResult) {
                bw.write(s);
                bw.write("\n");
            }
            bw.flush();
            bw.close();
            fw.close();
            long t2 = System.currentTimeMillis();
            long t3 = t2-t1;
            System.out.println(threadID + " MRID:" + currMrID + " finished! Time:"+t3);
        }
    }

    public File[] readOrigProgs(String origProgName, String projBasePath, int numOfTestExecThreads) throws IOException{
        String targetProgFolderName = "original_program";

        File folder = new File(projBasePath + "/" + targetProgFolderName);
        File[] childs = folder.listFiles(new FilenameFilter(){
            @Override
            public boolean accept(File parentPath, String fileName){
                if (fileName.startsWith("worker")){
                    return true;
                }else{
                    return false;
                }
            }
        });
        if (childs.length < numOfTestExecThreads){
            System.out.println("#workerFolders < #Exec threads!!");
            return null;
        }else{
            File[] workerFolders = new File[numOfTestExecThreads];
            ProcessBuilder b;
            Process p;
            InputStream in;
            InputStreamReader inr;
            int temp;
            for (int i = 0; i < numOfTestExecThreads; i ++){
                workerFolders[i] = new File(folder.getAbsolutePath() + File.separator + "worker" + i);
                b = new ProcessBuilder("./1compile_with_cov.sh");
                b.directory(new File(workerFolders[i].getAbsolutePath() + File.separator + "orig"));
                p = b.start();
                in = p.getInputStream();
                inr = new InputStreamReader(in);
                while ((temp = inr.read())!=-1){
                    //do nothing
                }
                inr.close();
                in.close();
                p.destroy();
            }

            File[] origProgForEachWorker = new File[workerFolders.length];
            for (int i = 0; i < workerFolders.length; i ++){
                origProgForEachWorker[i] = new File(workerFolders[i].getAbsolutePath() + File.separator + "orig", origProgName);
                if (! origProgForEachWorker[i].exists()){
                    System.out.println("Original program does not exist!");
                    System.exit(1);
                }
            }
            return origProgForEachWorker;
        }
    }

    public ArrayList<Integer> readLinesOfInterest(File f) throws IOException{
        FileInputStream fis = new FileInputStream(f);
        InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);
        ArrayList<Integer> result = new ArrayList<>();
        String currLine = "";
        while ((currLine = br.readLine())!= null){
            if (StringUtils.isNumeric(currLine)){
                result.add(Integer.parseInt(currLine));
            }else{
                System.out.println("Invalid line number!");
            }
        }
        return result;
    }

    public ArrayList<Pair<Integer, String>> readMRIDsAndNames(String projBasePath) throws IOException {
        //read MTGS
        ArrayList<Pair<Integer, String>> mrIDsAndNames = new ArrayList<>();
        File mappingFile = new File(projBasePath + "/" + "idAndMrMapping.txt");
        FileInputStream fis = new FileInputStream(mappingFile);
        InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);

        String currLine = "";
        currLine = br.readLine();
        String[] arrs = null;
        while (currLine != null) {
            arrs = currLine.split("\\|");
            if (arrs.length == 2) {
                mrIDsAndNames.add(new Pair<>(Integer.valueOf(arrs[0]), createValidFileName(arrs[1])));
            } else {
                System.out.println("Invalid id and MR mapping!!! " + currLine);
            }
            currLine = br.readLine();
        }
        br.close();
        isr.close();
        fis.close();
        //read MTGS finished;
        return mrIDsAndNames;
    }

    public String createValidFileName(String pairName){
        String[] a = pairName.split("<->");
        String[] b0 = a[0].split(":");
        String[] b1 = a[1].split(":");

        return b0[0] + "_" + b0[1] + "_" + b1[0] + "_" + b1[1];
    }
}
