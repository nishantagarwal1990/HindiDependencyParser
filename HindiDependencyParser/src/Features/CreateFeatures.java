package Features;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.InvalidInputDataException;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.SolverType;


public class CreateFeatures {
    public Map<String,Integer> labels_map = new HashMap<String,Integer>();
    public Map<String,Integer> feature_map;
    public Map<Integer,String> reverse_map;

    public ArrayList<ArrayList<String>> trainfilecontent;
    public ArrayList<ArrayList<String>> testfilecontent;

    private Problem problem = null;
    private Model model;
    private File modelFile;

    public CreateFeatures(){
        labels_map = new HashMap<String,Integer>();
        feature_map = new HashMap<String,Integer>();
        reverse_map = new HashMap<>();
        trainfilecontent = new ArrayList<>();
        testfilecontent = new ArrayList<>();
    }

    public void createFeatureMap(boolean labelled){
        int k = 0;
        int j = 1;
        for(ArrayList<String> strlist : trainfilecontent){
            if(strlist.size() != 0) {
                for(int i = 0;i<strlist.size();i++){
                    String val = strlist.get(i);
                    if(i == 0 && !labelled){
                        int ndx = val.indexOf('+');
                        if(ndx  == -1) {
                            if(!labels_map.containsKey(val)) {
                                labels_map.put(val, k++);
                                reverse_map.put(labels_map.get(val),val);
                            }
                        }
                        else{
                            String v = val.substring(0,val.indexOf('+'));
                            if(!labels_map.containsKey(v)) {
                                labels_map.put(v, k++);
                                reverse_map.put(labels_map.get(v),v);
                            }
                        }
                    }
                    else if(i == 0 && labelled){
                        if(!labels_map.containsKey(val)) {
                            labels_map.put(val, k++);
                            reverse_map.put(labels_map.get(val),val);
                        }
                    }
                    else{
                        if(!val.isEmpty() && !feature_map.containsKey(val)){
                            feature_map.put(strlist.get(i),j++);
                        }
                    }
                }
            }
        }
        feature_map.put("UNDEFINED",j);


        //feature_map.put("PHI",j+1);
        //feature_map.put("OMEGA",j+2);

        //System.out.println("No. of Features: "+feature_map.size());
        //System.out.println("No. of Labels: "+labels_map.size());
        //System.out.println(labels_map);
        //System.out.println(reverse_map);
    }

    public String createFeatures(ArrayList<String> temp, String step,boolean labelled){
        HashSet<Integer> s = new HashSet<>();
        StringBuffer sb = new StringBuffer();
        for (int j = 0; j < temp.size(); j++) {
            if (j == 0 && step == "train") {
                String val = temp.get(j);
                if (!labelled){
                    if (val.indexOf('+') == -1)
                        sb.append(Integer.toString(labels_map.get(val)) + " ");
                    else {
                        String v = val.substring(0, val.indexOf('+'));
                        sb.append(Integer.toString(labels_map.get(v)) + " ");
                    }
                }
                else{
                    sb.append(Integer.toString(labels_map.get(val)) + " ");
                }
            }
            else{
                if(!temp.get(j).isEmpty()) {
                    if (!feature_map.containsKey(temp.get(j))) {
                        s.add(feature_map.get("UNDEFINED"));
                    } else {
                        s.add(feature_map.get(temp.get(j)));
                    }
                }
            }
            /*
            if (i == 0 || filecontent.get(i - 1).size() == 0){
                s.add(feature_map.get("PHI"));
            }
            if(i == filecontent.size()-1 || filecontent.get(i + 1).size() == 0 ){
                s.add(feature_map.get("OMEGA"));
            }
            */
        }
        List<Integer> sortedList = new ArrayList<Integer>(s);
        Collections.sort(sortedList);

        for(Integer a: sortedList){
            sb.append(a.toString()+":1 ");
        }
        sb.append("\n");
        return sb.toString();
    }
    public void writeFeatures(String outfilename,String type,boolean labelled){
        ArrayList<ArrayList<String>> filecontent = null;

        if(type == "train")
            filecontent = trainfilecontent;
        else
            filecontent = testfilecontent;
        try {
            // FileReader reads text files in the default encoding.
            FileWriter fileWriter =
                    new FileWriter(outfilename);

            // Always wrap FileReader in BufferedReader.
            BufferedWriter bufferedWriter =
                    new BufferedWriter(fileWriter);

            for(int i = 0;i<filecontent.size();i++) {
                ArrayList<String> temp = filecontent.get(i);
                if (temp.size() != 0) {
                    bufferedWriter.write(createFeatures(temp,type,labelled));
                }
            }

            // Always close files.
            bufferedWriter.close();
            fileWriter.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println(
                    "Unable to open file '" +
                            outfilename + "'");
        }
        catch(IOException ex) {
            System.out.println(
                    "Error reading file '"
                            + outfilename + "'");
            // Or we could just do this:
            // ex.printStackTrace();
        }

    }


    public void readFileContent(String filename){
        String line;

        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader =
                    new FileReader(filename);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader =
                    new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
                if(!line.isEmpty())
                    trainfilecontent.add(new ArrayList<String>(Arrays.asList(line.trim().split(" "))));
            }

            // Always close files.
            bufferedReader.close();
            fileReader.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" +filename + "'");
        }
        catch(IOException ex) {
            System.out.println("Error reading file '"+ filename + "'");
        }
    }

    public void Train(File trainfile){
        try {
            problem = Problem.readFromFile(trainfile, 0.0);
        }catch(InvalidInputDataException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }

        SolverType solver = SolverType.L2R_LR; // -s 0
//        SolverType solver = SolverType.L2R_L2LOSS_SVC_DUAL; // -s 1
//        SolverType solver = SolverType.L2R_L2LOSS_SVC; //-s 2
//        SolverType solver = SolverType.L2R_L1LOSS_S VC_DUAL; //-s 3
//        SolverType solver = SolverType.MCSVM_CS; //-s 4 // OutOfMemoryError: Java heap space
//        SolverType solver = SolverType.L1R_L2LOSS_SVC; // -s 5
//        SolverType solver = SolverType.L1R_LR; // -s 6
//        SolverType solver = SolverType.L2R_LR_DUAL; //-s 7
       
//        For below models are for Regression
//        SolverType solver = SolverType.L2R_L2LOSS_SVR; // -s 11
//        SolverType solver = SolverType.L2R_L2LOSS_SVR_DUAL; // -s 12
//        SolverType solver = SolverType.L2R_L1LOSS_SVR_DUAL; //-s 13
        
        double C = 1.0;    // cost of constraints violation
        double eps = 0.01; // stopping criteria

        Parameter parameter = new Parameter(solver, C, eps);
        try {
            model = Linear.train(problem, parameter);
            modelFile = new File("output/Interim_Files/model");

            try {
                model.save(modelFile);
//            	model = Model.load(modelFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }catch(NullPointerException e){
            e.printStackTrace();
        }

    }

    public String predict(String line,boolean labelled){

        String features = createFeatures(new ArrayList<String>(Arrays.asList(line.trim().split(" "))),"test",labelled);

        ArrayList<String> feats= new ArrayList<String>(Arrays.asList(features.trim().split(" ")));

        Feature[] instance = new Feature[feats.size()-1];

        for(int i=1;i<feats.size();i++){
            String temp = feats.get(i);
            int ndx = temp.indexOf(':');
            int a = Integer.parseInt(temp.substring(0,ndx));
            int b = Integer.parseInt(temp.substring(ndx+1));
            instance[i-1] = new FeatureNode(a,b);
        }

        double prediction = Linear.predict(model, instance);

        return reverse_map.get((int)prediction);
    }

    public void run(String trainfile,String trainoutfile,boolean labelled) {
        String trainfilename = trainfile;
        String trainoutfilename = trainoutfile;
        readFileContent(trainfilename);
        //m.testfilecontent = m.readFileContent(testfilename);
        createFeatureMap(labelled);

        writeFeatures(trainoutfilename,"train",labelled);
        //m.writeFeatures(testoutfilename,"test");
    }

}
