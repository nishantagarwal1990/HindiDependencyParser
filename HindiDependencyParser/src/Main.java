import DependencyParsing.OracleTransition;
import Features.CreateFeatures;
import org.maltparser.core.exception.MaltChainedException;

import java.io.File;
import java.io.IOException;

public class Main {
    //public OracleTransition oracleTransition;
    private CreateFeatures createFeatures;

    public Main(){
        createFeatures = new CreateFeatures();
    }

    public void run(String fileformat,String outfilename,String inDir,String step, String outDir) throws IOException{

        File f = new File(inDir);
        File[] listofFiles = f.listFiles();

        String inFile = null;
        String charSet = "UTF-8";

        OracleTransition oracle = null;
        try {
        	if(step.equalsIgnoreCase("train"))
        		oracle = new OracleTransition(fileformat,outfilename,createFeatures);
        	else
        		oracle = new OracleTransition(fileformat,"",createFeatures);
        } catch (MaltChainedException e) {
            System.err.println("MaltParser exception : " + e.getMessage());
        }

        for (int i = 0; i < listofFiles.length; i++) {
            if (listofFiles[i].isFile() && !listofFiles[i].getName().startsWith(".")) {
                inFile = listofFiles[i].getName();
                try {
                	System.out.println(inDir+"/"+inFile);
                    oracle.run(inDir+"/"+inFile, charSet, step, outDir+"/"+inFile);
//                	oracle.run(inFile, charSet,step);
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
//        String inTrainDir = "docs/train";
        String outTrainfilename = "feature_train.txt";
//        String inTestDir = "data/Testing";
        String inTestDir = "docs/test";
//        String outTestfilename = "feature_test.txt";
//        String outDependencyFile = "dependency.txt";


        Main m = new Main();

        try {
			m.run(fileformat,outTrainfilename,inTrainDir,"train","");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        String binaryOutTrainFile = "train.txt";
//        String binaryOutTestFile = "test.txt";
        
        File trainfile = new File(binaryOutTrainFile);
        m.createFeatures.run(outTrainfilename,binaryOutTrainFile);
        m.createFeatures.Train(trainfile);
        
        String outDir = "out";
        try {
			m.run(fileformat,"",inTestDir,"test", outDir);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
