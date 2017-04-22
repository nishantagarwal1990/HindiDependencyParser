package DependencyParsing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;

import Features.CreateFeatures;
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
import org.maltparser.parser.SingleMalt;
import org.maltparser.parser.algorithm.nivre.ArcStandard;
import org.maltparser.parser.algorithm.nivre.ArcStandardOracle;
import org.maltparser.parser.algorithm.nivre.NivreConfig;
import org.maltparser.parser.history.History;
import org.maltparser.parser.history.action.ComplexDecisionAction;
import org.maltparser.parser.history.action.GuideUserAction;
import org.maltparser.parser.history.container.ActionContainer;


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
		if(!outfilename.equalsIgnoreCase("")) {
			try {
				fw = new FileWriter(outfilename);
				bw = new BufferedWriter(fw);
			} catch(IOException e){
				System.out.println("Error: "+e.getMessage());
			}
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
	 * @throws IOException 
	 */
	public void run(String inFile, String charSet,String step, String outFile) throws MaltChainedException, IOException {

		// Opens the input and output file with a character encoding set
		tabReader.open(inFile, charSet);
		boolean moreInput = true;
		FileWriter fw = null;
		BufferedWriter bw = null;
		if(step.equalsIgnoreCase("test")) {
			fw = new FileWriter(outFile);
			bw = new BufferedWriter(fw);
		}
		// Reads Sentences until moreInput is false
		while (moreInput) {
			moreInput = tabReader.readSentence(inputGraph);
			if (inputGraph.hasTokens()) {
				if(step.equalsIgnoreCase("test")) {
					createConfiguration(inputGraph,step, bw);
					try {
						bw.write("\n");
					} catch(IOException e) {
						System.out.println("Error: " + e.getMessage());
					}
				}else {
					createConfiguration(inputGraph,step, null);
				}
			}
		}
		if(step.equalsIgnoreCase("test")) {
			bw.flush();
			bw.close();
			fw.close();
		}
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
		if(transition != null)
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
	 * @throws IOException 
	 */
	public void createConfiguration(DependencyGraph inputGraph,String step, BufferedWriter bw) throws MaltChainedException, IOException {
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
			TestConfiguration(config,inputGraph,asc,as, bw);
		}
	}

	public void TestConfiguration(NivreConfig config,DependencyGraph inputGraph,ArcStandardOracle asc,ArcStandard as, BufferedWriter bw) throws MaltChainedException, IOException{
		List<Token> tokenList = new LinkedList<>();

		//Predict and apply transitions to get configuration in each step of transition based parsing
		//		GuideUserAction action = new ComplexDecisionAction(history);

		String features,label;
//		System.out.println("111");
		while(config.getInput().size() > 0) {
			Token t = null;
			System.out.println("222");
			if(config.getInput().size() != 0) {
				System.out.println("333");
				
				features = createFeatures(inputGraph, null, config.getStack(), config.getInput());
				System.out.println("features: "+ features);
				System.out.println("stack:"+config.getStack().size());
				System.out.println("stack top:"+ config.getStack().peek());
				System.out.println("buffer:"+config.getInput().size());
				System.out.println("buffer top:"+config.getInput().peek());
				
				label = this.createFeatures.predict(features);
				System.out.println("label:"+label);
				
				Stack<DependencyNode> st = config.getStack();
				DependencyNode s1 = null;
				DependencyNode s2 = null;
				if(config.getStack().size() > 1) {
					s1 = st.pop();
					if(config.getStack().size() > 1) {
						s2 = st.peek();
					}
				}
				if(label.equalsIgnoreCase("LA")) {
					t = new Token(s2, s1.getIndex());
				}else if(label.equalsIgnoreCase("RA")) {
					t = new Token(s1, s2.getIndex());
				}else {
					st.push(config.getInput().pop());
				}
			}

			//			as.apply(action, config);
			//
			//
			//			if (config.getInput().size()> 0)
			//				action = asc.predict(inputGraph, config);
			if(t != null)
				tokenList.add(t);
		}
		Collections.sort(tokenList, new Comparator<Token>() {
			public int compare(Token left, Token right)  {
				return left.node.getIndex() - right.node.getIndex();
			}
		});
		for(Token t: tokenList) {
			bw.write(createCoNllConfiguration(t, inputGraph));
		}
	}

	public List<SymbolTable> returnSymbolTableList(DependencyGraph inputGraph ){
		List<SymbolTable> symList = new LinkedList<>();
		try {
			symList.add(inputGraph.getSymbolTables().getSymbolTable("ID"));
			symList.add(inputGraph.getSymbolTables().getSymbolTable("FORM"));
			symList.add(inputGraph.getSymbolTables().getSymbolTable("LEMMA"));
			symList.add(inputGraph.getSymbolTables().getSymbolTable("CPOSTAG"));
			symList.add(inputGraph.getSymbolTables().getSymbolTable("POSTAG"));
			symList.add(inputGraph.getSymbolTables().getSymbolTable("FEATS"));
			//    	symList.add(inputGraph.getSymbolTables().getSymbolTable("HEAD"));
		}catch(MaltChainedException me) {
			me.printStackTrace();
		}
		return symList;
	}

	public String createCoNllConfiguration(Token t,DependencyGraph inputGraph ) {
		StringBuffer sb = new StringBuffer();
		List<SymbolTable> symList = returnSymbolTableList(inputGraph);
		int i = 0;
		try {
			for(SymbolTable st: symList) {
				sb.append(t.node.getLabelSymbol(symList.get(i)));
				sb.append(" ");
			}
			sb.append(String.valueOf(t.head) + " " + "_" + " " + "_" + " " + "_");
		}catch(MaltChainedException me) {
			me.printStackTrace();
		}
		return sb.toString();
	}

	//	public void writeDependencyNode(BufferedWriter bw, String st) throws IOException {
	//		bw.write(st);
	//	}

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
