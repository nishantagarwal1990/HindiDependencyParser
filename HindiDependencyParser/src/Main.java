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

    public void run(String fileformat,String outfilename,String inDir,String step, String outDir,boolean labelled,
                    OracleTransition oracle) throws IOException{

        File f = new File(inDir);
        File[] listofFiles = f.listFiles();

        String inFile = null;
        String charSet = "UTF-8";


        for (int i = 0; i < listofFiles.length; i++) {
            if (listofFiles[i].isFile() && !listofFiles[i].getName().startsWith(".")) {
                inFile = listofFiles[i].getName();
                try {
//                	System.out.println(inDir+"/"+inFile);
                    oracle.run(inDir+"/"+inFile, charSet, step, outDir+"/"+inFile,labelled);
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
        String outTrainfilename = "output/Interim_Files/feature_train.txt";
        String inTestDir = "data/Testing";

        boolean labelled = true;

        Main m = new Main();

        File directory = new File("output/Interim_Files");

        if(!directory.exists())
            directory.mkdirs();

        directory = new File("output/Test");

        if(!directory.exists())
            directory.mkdirs();

        OracleTransition oracle = null;
        try {
            oracle = new OracleTransition(fileformat,outTrainfilename,m.createFeatures);

        } catch (MaltChainedException e) {
            System.err.println("MaltParser exception : " + e.getMessage());
        }

        try {
			m.run(fileformat,outTrainfilename,inTrainDir,"train","",labelled,oracle);
		} catch (IOException e) {
			e.printStackTrace();
		}

        String binaryOutTrainFile = "output/Interim_Files/train.txt";

        File trainfile = new File(binaryOutTrainFile);
        m.createFeatures.run(outTrainfilename,binaryOutTrainFile,labelled);
        m.createFeatures.Train(trainfile);
        
        String outDir = "output/Test";
        try {
			m.run(fileformat,"",inTestDir,"test", outDir,labelled,oracle);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
