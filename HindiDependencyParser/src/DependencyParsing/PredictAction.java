package DependencyParsing;
import java.util.Stack;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.propagation.PropagationManager;
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
		// TODO Auto-generated constructor stub
	}

	public GuideUserAction setAction(DependencyStructure inputGraph, GuideUserHistory history, ParserConfiguration config, int transition) throws MaltChainedException {
		NivreConfig nivreConfig = (NivreConfig)config;
		DependencyNode stackPeek = nivreConfig.getStack().peek();
		int stackPeekIndex = stackPeek.getIndex();
		int inputPeekIndex = nivreConfig.getInput().peek().getIndex();
		if(transition == 1) {
			return this.updateActionContainers(history, transition, (LabelSet)null);
		} else if(transition == 3) {
			return this.updateActionContainers(history, transition, inputGraph.getTokenNode(stackPeekIndex).getHeadEdge().getLabelSet());
		}else{
			return this.updateActionContainers(history, transition, inputGraph.getTokenNode(inputPeekIndex).getHeadEdge().getLabelSet());
		}
	}
	
	public void applyTransition(int transition, ParserConfiguration config) throws MaltChainedException {
		NivreConfig nivreConfig = (NivreConfig)config;
		Stack<DependencyNode> stack = nivreConfig.getStack();
		Stack<DependencyNode> input = nivreConfig.getInput();
		Edge e = null;
		switch (transition) {
		case LEFTARC:
			System.out.println("left head before:"+ stack.peek().getHead().getIndex());
			e = nivreConfig.getDependencyStructure().addDependencyEdge(input.peek().getIndex(), stack.peek().getIndex());
			addEdgeLabels(e);
			System.out.println("left head after:"+ stack.peek().getHead().getIndex());
			stack.pop();
			break;
		case RIGHTARC:
			System.out.println("right head before:"+ input.peek().getHead().getIndex());
			e = nivreConfig.getDependencyStructure().addDependencyEdge(stack.peek().getIndex(), input.peek().getIndex());
			addEdgeLabels(e);
			System.out.println("right head after:"+ input.peek().getHead().getIndex());
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
