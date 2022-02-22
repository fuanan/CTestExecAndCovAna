package CMDListGenerator;

import java.io.File;
import java.util.ArrayList;

public interface GenCMDList {
    public void initialize(ArrayList<String> cmdList);
    public ArrayList<String> genCMDList(String pathOfProgramUnderTest, File execBaseDir, String additionalParaFileName);
}
