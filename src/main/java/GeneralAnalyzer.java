import CMDListGenerator.GenCMDList;
import javafx.util.Pair;
import sun.nio.cs.Surrogate;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.CompletionService;

public class GeneralAnalyzer {

    String origProgName;
    String projBasePath;
    GenCMDList generator;
    String covInfoOutputPath;
    int numOfTestExecThreads = 1;

    public GeneralAnalyzer(String origProgName, String projBasePath, GenCMDList generator, String covInfoOutputPath){
        this.origProgName = origProgName;
        this.projBasePath = projBasePath;
        this.generator = generator;
        this.covInfoOutputPath = covInfoOutputPath;
    }

    public void analyze(String projBasePath, String covInfoOutputPath){
        File basePath = new File(projBasePath);
        if ( !basePath.exists() || !basePath.isDirectory()){
            System.out.println(projBasePath + " does not exist!!");
        }else{
            try{
                String covOutputFolderPath = projBasePath + "/" + "code_cov_result";
                ArrayList<Pair<Integer, String>> mrIDsAndNames = readMRIDsAndNames(projBasePath);
                File[] origProgsForEachWorker = readOrigProgs(origProgName, projBasePath, numOfTestExecThreads);
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
                                execAndAnalyse(projBasePath, mrIDSets[threadID], origProgsForEachWorker[threadID], generator, covInfoOutputPath);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    r.run();
                }
                System.out.println("Finish!");
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    public void execAndAnalyse(String projBasePath, ArrayList<Pair<Integer, String>> mrIDsAndNames, File origProgUnderTest, GenCMDList generator,
                               String outputFolderPath) throws IOException{
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
            Pair <Integer, String[]> currResult = new ExecutionThread(currMrID, projBasePath, currFNames, generator, origProgUnderTest,
                    genGcovCMDList, analyseCMDList).call();
            FileWriter fw;
            BufferedWriter bw;
            File currMRResult = new File(outputFolderPath + File.separator + mrIDsAndNames.get(i).getValue() + ".txt");
            if (!currMRResult.exists()){
                currMRResult.createNewFile();
            }
            fw = new FileWriter(currMRResult, true);
            bw = new BufferedWriter(fw);
            for(String s: currResult.getValue()) {
                bw.write(s);
                bw.write("\n");
            }
            bw.flush();
            bw.close();
            fw.close();
        }
    }

    public File[] readOrigProgs(String origProgName, String projBasePath, int numOfTestExecThreads){
        String targetProgFolderName = "original_program";

        File folder = new File(projBasePath + "/" + targetProgFolderName);
        if (folder.listFiles().length < numOfTestExecThreads){
            System.out.println("#workerFolders < #Exec threads!!");
            return null;
        }else{
            File[] workerFolders = new File[numOfTestExecThreads];
            for (int i = 0; i < numOfTestExecThreads; i ++){
                workerFolders[i] = new File(folder.getAbsolutePath() + "/worker" + i);
            }

            File[] origProgForEachWorker = new File[workerFolders.length];
            for (int i = 0; i < workerFolders.length; i ++){
                origProgForEachWorker[i] = new File(workerFolders[i].getAbsolutePath() + "orig", origProgName);
                if (! origProgForEachWorker[i].exists()){
                    System.out.println("Original program does not exist!");
                    System.exit(1);
                }
            }
            return origProgForEachWorker;
        }
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
        while (currLine!=null){
            arrs = currLine.split("\\|");
            if (arrs.length == 2){
                mrIDsAndNames.add(new Pair<>(Integer.valueOf(arrs[0]), createValidFileName(arrs[1])));
            }else {
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
