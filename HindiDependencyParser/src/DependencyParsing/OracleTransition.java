package DependencyParsing;
import java.io.File;
import java.util.Stack;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;

import Features.CreateFeatures;
import de.bwaldvogel.liblinear.*;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.helper.HashMap;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.io.dataformat.DataFormatSpecification;
import org.maltparser.core.propagation.PropagationManager;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.symbol.TableHandler;
import org.maltparser.core.symbol.hash.HashSymbolTableHandler;
import org.maltparser.core.syntaxgraph.DependencyGraph;
import org.maltparser.core.syntaxgraph.node.DependencyNode;
import org.maltparser.core.syntaxgraph.reader.SyntaxGraphReader;
import org.maltparser.core.syntaxgraph.reader.TabReader;
import org.maltparser.ml.liblinear.Liblinear;
import org.maltparser.parser.SingleMalt;
import org.maltparser.parser.algorithm.nivre.ArcStandard;
import org.maltparser.parser.algorithm.nivre.ArcStandardOracle;
import org.maltparser.parser.algorithm.nivre.NivreConfig;
import org.maltparser.parser.history.History;
import org.maltparser.parser.history.action.GuideUserAction;


public class OracleTransition {
    private DependencyGraph inputGraph;
    private SyntaxGraphReader tabReader;
    private SymbolTableHandler symbolTables = null;
    private static BufferedWriter bw = null;
    private static FileWriter fw = null;
    private CreateFeatures createFeatures;

    /**
     * Function initializes the necessary variables for data processing
     * @param dataFormatFileName
     * @param outfilename
     * @throws MaltChainedException
     */
    public OracleTransition(String dataFormatFileName,String outfilename,CreateFeatures createFeatures) throws MaltChainedException {

        this.createFeatures = createFeatures;

        try {
            fw = new FileWriter(outfilename);
            bw = new BufferedWriter(fw);
        } catch(IOException e){
            System.out.println("Error: "+e.getMessage());
        }

        // Creates a symbol table handler
        symbolTables = new HashSymbolTableHandler();

        // Initialize data format instance of the CoNLL data format from conllx.xml (conllx.xml located in same directory)
        DataFormatSpecification dataFormat = new DataFormatSpecification();
        dataFormat.parseDataFormatXMLfile(dataFormatFileName);
        DataFormatInstance dataFormatInstance = dataFormat.createDataFormatInstance(symbolTables, "none");

        // Creates a dependency graph
        inputGraph = new DependencyGraph(symbolTables);
        // Creates a tabular reader with the CoNLL data format
        tabReader = new TabReader();
        tabReader.setDataFormatInstance(dataFormatInstance);
    }

    /**
     * Function loops over sentences in a CoNLL format to create dependency graph
     * @param inFile
     * @param charSet
     * @throws MaltChainedException
     */
    public void run(String inFile, String charSet,String step) throws MaltChainedException {

        // Opens the input and output file with a character encoding set
        tabReader.open(inFile, charSet);
        boolean moreInput = true;
        // Reads Sentences until moreInput is false
        while (moreInput) {
            moreInput = tabReader.readSentence(inputGraph);
            if (inputGraph.hasTokens()) {
                createConfiguration(inputGraph,step);
                try {
                    bw.write("\n");
                } catch(IOException e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        }

        // Closes the reader
        tabReader.close();
    }

    /**
     * Function extracts features from a configuration of transition based parsing.
     * @param inputGraph
     * @param transition
     * @param st
     * @param q
     */
    public String createFeatures(DependencyGraph inputGraph, String transition, Stack<DependencyNode> st, Stack<DependencyNode> q){
        StringBuffer sb = new StringBuffer();
        sb.append(transition+" ");
        DependencyNode s1;
        DependencyNode s2;
        if(st.size() <= 1 ) {
            s1 = null;
            s2 = null;
        }
        else {
            s1 = st.peek();
            st.pop();
            if(st.size() <= 1)
                s2 = null;
            else
                s2 = st.peek();
            st.push(s1);
        }

        DependencyNode b1;
        if(q.empty())
            b1 = null;
        else
            b1 = q.peek();

        try {
            SymbolTable form = inputGraph.getSymbolTables().getSymbolTable("FORM");
            SymbolTable tag = inputGraph.getSymbolTables().getSymbolTable("POSTAG");
            DependencyNode s1_rc = s1 != null?s1.getRightmostDependent():null;
            DependencyNode s2_rc = s2 != null?s1.getRightmostDependent():null;
            DependencyNode s1_lc = s1 != null?s1.getLeftmostDependent():null;
            DependencyNode s2_lc = s2 != null?s1.getLeftmostDependent():null;
            String s1_word = s1 != null?s1.getLabelSymbol(form):"ROOT";
            String s1_tag = s1 != null?s1.getLabelSymbol(tag):"ROOT";
            String s2_word = s2 != null?s2.getLabelSymbol(form):s1 != null?"ROOT":"";
            String s2_tag = s2 != null?s2.getLabelSymbol(tag):s1 != null?"ROOT":"";
            String s1_rc_tag = s1_rc != null?s1_rc.getLabelSymbol(tag):"";
            String s1_lc_tag = s1_lc != null?s1_lc.getLabelSymbol(tag):"";
            String s2_rc_tag = s2_rc != null?s2_rc.getLabelSymbol(tag):"";
            String s2_lc_tag = s2_lc!=null?s2_lc.getLabelSymbol(tag):"";
            String b1_word = b1 != null?b1.getLabelSymbol(form):"";
            String b1_tag = b1 != null?b1.getLabelSymbol(tag):"";


            sb.append(s1_word+" "+s1_tag+" "+s1_word+s1_tag+" "+s2_word+" "+s2_tag+" "+s2_word+s2_tag+" "
                    +b1_word+" "+b1_tag+" "+b1_word+b1_tag+" "+s1_word+s1_tag+s2_word+s2_tag+" "+s1_word+s1_tag+s2_word
                    +" "+s1_word+s1_tag+s2_tag+" "+s1_word+s2_word+s2_tag+" "+s1_tag+s2_word+s2_tag
                    +" "+s1_word+s2_word+s1_tag+s2_tag+" "+s1_tag+b1_tag+" "+s2_tag+s1_tag+b1_tag
                    +" "+s2_tag+s1_tag+s1_lc_tag+" "+s2_tag+s1_tag+s1_rc_tag+" "+s2_tag+s1_tag+s2_lc_tag
                    +" "+s2_tag+s1_tag+s2_rc_tag+" "+s2_tag+s1_word+s2_rc_tag+" "+s2_tag+s1_word+s1_lc_tag
                    +" "+s2_tag+s1_word+b1_tag+"\n");
            try {
                bw.write(sb.toString());
            } catch(IOException e){
                System.out.println("Error: "+e.getMessage());
            }

        }catch(MaltChainedException e){
            System.out.println("Error:"+e.getMessage());
        }

        return sb.toString();
    }

    /**
     * Function creates configuration of each sentence using Oracle and Parser of ArcStandard algorithm
     * @param inputGraph
     * @throws MaltChainedException
     */
    public void createConfiguration(DependencyGraph inputGraph,String step) throws MaltChainedException {
        //Create configuration
        NivreConfig config = new NivreConfig(false, false, false);

        //Initialize variable for table handler
        String _decisionSettings = "T.TRANS+A.DEPREL";
        String _separator = "~";
        int _kBestSize = -1;

        //Create an ArcStandard object and initialiaze transition system
        ArcStandard as = new ArcStandard(new PropagationManager());
        as.initTableHandlers(_decisionSettings, symbolTables);

        HashMap<String,TableHandler> tableHandlers = as.getTableHandlers();

        History history = new History(_decisionSettings, _separator, tableHandlers, _kBestSize);
        as.initTransitionSystem(history);

        //Initialize Oracle object
        SingleMalt sm = new SingleMalt();
        ArcStandardOracle asc = new ArcStandardOracle(sm, history );
        config.setDependencyGraph(inputGraph);

        config.initialize();

        if(step == "train")
            TrainConfiguration(config,inputGraph,asc,as);
        else{
            TestConfiguration(config,inputGraph,asc,as);
        }
    }

    public void TestConfiguration(NivreConfig config,DependencyGraph inputGraph,ArcStandardOracle asc,ArcStandard as) throws MaltChainedException{


        //Predict and apply transitions to get configuration in each step of transition based parsing
        GuideUserAction action = asc.predict(inputGraph, config);

        String features,label;
        while(config.getInput().size()> 0) {
            if(config.getInput().size() != 0) {
                features = createFeatures(inputGraph, as.getActionString(action), config.getStack(), config.getInput());
                label = this.createFeatures.predict(features);
            }

            as.apply(action, config);


            if (config.getInput().size()> 0)
                action = asc.predict(inputGraph, config);
        }
    }

    public void TrainConfiguration(NivreConfig config,DependencyGraph inputGraph,ArcStandardOracle asc,ArcStandard as) throws MaltChainedException{


        //Predict and apply transitions to get configuration in each step of transition based parsing
        GuideUserAction action = asc.predict(inputGraph, config);

        while(config.getInput().size()> 0) {
            if(config.getInput().size() != 0)
                createFeatures(inputGraph,as.getActionString(action),config.getStack(),config.getInput());

            as.apply(action, config);


            if (config.getInput().size()> 0)
                action = asc.predict(inputGraph, config);
        }
    }


    public void closehandles(){
        try {
            if (bw != null)
                bw.close();

            if (fw != null)
                fw.close();

        } catch (IOException ex) {

            ex.printStackTrace();

        }
    }

}
