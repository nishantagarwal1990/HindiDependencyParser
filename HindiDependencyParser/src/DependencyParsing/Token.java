package DependencyParsing;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.maltparser.core.syntaxgraph.node.DependencyNode;

public class Token {
	DependencyNode node;
	int head;

	public Token(DependencyNode d, int head) {
		this.head = head;
		this.node = d;
	}
//	public void sortToken(List<Token> list) {
//		Collections.sort(list, new Comparator<Token>() {
//			int compare(Token left, Token right)  {
//				return left.node.getIndex() - right.node.getIndex();
//			}
//		});
//	}
}
