import javafx.util.Pair;

import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecThreadPool {
    private ExecutorService pool = null;
    private CompletionService <Pair<Integer, String[]>> completionService;

    public ExecThreadPool (int threadNum){
        this.pool = Executors.newFixedThreadPool(threadNum);
        this.completionService = new ExecutorCompletionService(pool);
    }

    public CompletionService getInstance(){
        return this.completionService;
    }

    public void close(){
        if (!this.pool.isShutdown()){
            this.pool.shutdown();
        }
        this.completionService = null;
    }
}
