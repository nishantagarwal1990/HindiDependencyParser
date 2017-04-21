import DependencyParsing.OracleTransition;
import Features.CreateFeatures;
import org.maltparser.core.exception.MaltChainedException;

import java.io.File;

public class Main {
    //public OracleTransition oracleTransition;
    private CreateFeatures createFeatures;

    Main(){
        createFeatures = new CreateFeatures();
    }

    public void run(String fileformat,String outfilename,String inDir,String step){

        File f = new File(inDir);
        File[] listofFiles = f.listFiles();

        String inFile = null;
        String charSet = "UTF-8";

        OracleTransition oracle = null;
        try {
            oracle = new OracleTransition(fileformat,outfilename,createFeatures);
        } catch (MaltChainedException e) {
            System.err.println("MaltParser exception : " + e.getMessage());
        }

        for (int i = 0; i < listofFiles.length; i++) {
            if (listofFiles[i].isFile() && !listofFiles[i].getName().startsWith(".")) {
                inFile = listofFiles[i].getName();
                try {
                    oracle.run(inDir+"/"+inFile, charSet,step);
                } catch (MaltChainedException e) {
                    System.err.println("MaltParser exception : " + e.getMessage());
                }
            }
        }
        oracle.closehandles();
    }

    public static void main(String[] args) {
        String fileformat = "resources/conllx.xml";
        String inTrainDir = "data/Training";
        String outTrainfilename = "out/feature_train.txt";
        String inTestDir = "data/Testing";
        String outTestfilename = "out/feature_test.txt";


        Main m = new Main();

        m.run(fileformat,outTrainfilename,inTrainDir,"train");
        m.run(fileformat,outTestfilename,inTestDir,"test");

        String binaryOutTrainFile = "out/train.txt";
        String binaryOutTestFile = "out/test.txt";

        m.createFeatures.run(outTrainfilename,outTestfilename,binaryOutTrainFile,binaryOutTestFile);

        File trainfile = new File(binaryOutTrainFile);

        m.createFeatures.Train(trainfile);

    }
}
