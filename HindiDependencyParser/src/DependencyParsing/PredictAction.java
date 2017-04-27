package DependencyParsing;
import java.util.Stack;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.propagation.PropagationManager;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.LabelSet;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.DependencyNode;
import org.maltparser.parser.ParserConfiguration;
import org.maltparser.parser.algorithm.nivre.ArcStandard;
import org.maltparser.parser.algorithm.nivre.NivreConfig;
import org.maltparser.parser.history.GuideUserHistory;
import org.maltparser.parser.history.action.GuideUserAction;

public class PredictAction  extends ArcStandard{

	public PredictAction(PropagationManager propagationManager) throws MaltChainedException {
		super(propagationManager);
	}

	public GuideUserAction setAction(DependencyStructure inputGraph, GuideUserHistory history,
									 ParserConfiguration config, int transition,String deprel) throws MaltChainedException {
		NivreConfig nivreConfig = (NivreConfig)config;
		DependencyNode stackPeek = nivreConfig.getStack().peek();
		int stackPeekIndex = stackPeek.getIndex();
		int inputPeekIndex = nivreConfig.getInput().peek().getIndex();
		if(transition == 1) {
			return this.updateActionContainers(history, transition, (LabelSet) null);
//		} else if(transition == 3) {
//			return this.updateActionContainers(history, transition, inputGraph.getTokenNode(stackPeekIndex).getHeadEdge().getLabelSet());
//		}else{
//			return this.updateActionContainers(history, transition, inputGraph.getTokenNode(inputPeekIndex).getHeadEdge().getLabelSet());
//		}
		}else{
			SymbolTable deprelSymTable = inputGraph.getSymbolTables().getSymbolTable("DEPREL");
			LabelSet labelSet = new LabelSet();
			labelSet.put(deprelSymTable, deprelSymTable.getSymbolStringToCode(deprel));
			return this.updateActionContainers(history, transition, labelSet);
		}
	}

	public void applyTransition(int transition, ParserConfiguration config) throws MaltChainedException {
		NivreConfig nivreConfig = (NivreConfig)config;
		Stack<DependencyNode> stack = nivreConfig.getStack();
		Stack<DependencyNode> input = nivreConfig.getInput();
		Edge e = null;
		switch (transition) {
		case LEFTARC:
			e = nivreConfig.getDependencyStructure().addDependencyEdge(input.peek().getIndex(), stack.peek().getIndex());
			addEdgeLabels(e);
			stack.pop();
			break;
		case RIGHTARC:
			e = nivreConfig.getDependencyStructure().addDependencyEdge(stack.peek().getIndex(), input.peek().getIndex());
			addEdgeLabels(e);
			input.pop();
			if (!stack.peek().isRoot()) {
				input.push(stack.pop());	
			}
			break;
		default:
			stack.push(input.pop()); // SHIFT
			break;
		}
	}
}
