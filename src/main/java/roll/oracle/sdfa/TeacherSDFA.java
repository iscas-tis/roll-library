package roll.oracle.sdfa;

import java.util.LinkedList;
import java.util.List;

import roll.automata.SDFA;
import roll.learner.dfa.tree.NodeImpl;
import roll.learner.dfa.tree.TreeImpl;
import roll.learner.dfa.tree.ValueNode;
import roll.main.Options;
import roll.oracle.TeacherAbstract;
import roll.query.Query;
import roll.query.QuerySimple;
import roll.table.ExprValue;
import roll.table.ExprValueInt;
import roll.table.HashableValue;
import roll.table.HashableValueBoolean;
import roll.table.HashableValueEnum;
import roll.table.HashableValueInt;
import roll.tree.Node;
import roll.words.Alphabet;
import roll.words.Word;

public class TeacherSDFA extends TeacherAbstract<SDFA> {
	
	private TreeImpl tree;
    protected List<ValueNode> states;
    private Alphabet alphabet;
    LinkedList<Word> positives;
    LinkedList<Word> negatives;
    LinkedList<Word> dontcares;
    
	public TeacherSDFA(Options options, Alphabet alphabet, 
		LinkedList<Word> positives, LinkedList<Word> negatives,
		LinkedList<Word> dontcares) {
		super(options);
		states = new LinkedList<>();
		this.alphabet = alphabet;
		this.positives = positives;
		this.negatives = negatives;
		this.dontcares = dontcares;
		Node<ValueNode> root = new NodeImpl(null, null, getExprValue(0));
		tree = new TreeImpl(root);
		
		constructPrefixTree();
	}
	
	private Node<ValueNode> searchNode(Word word, int mqResult, boolean query) {
		if (word.isEmpty()) {
			return tree.getRoot();
		}
//		System.out.println("Search node: " + word.toString());
		Node<ValueNode> nodeCurr = tree.getRoot();
		int val = 0;
		
		int currIndex = 0;
		while(true) {
			HashableValueInt branch = new HashableValueInt(word.getLetter(currIndex));
//			System.out.println("Current branch: " + branch);
			Node<ValueNode> child = nodeCurr.getChild(branch);
			if (currIndex + 1 == word.length()) {
				val = mqResult;
			}
			if (child == null) {
				if (query) {
					return null;
				}else {
					child = new NodeImpl(nodeCurr, branch, getExprValue(val));
					nodeCurr.addChild(branch, child);
				}
			}
			++ currIndex;
			if (currIndex >= word.length()) {
				return child;
			}
			nodeCurr = child;
		}
	}
    
    private void constructPrefixTree() {
    	for (Word word : positives) {
    		searchNode(word, 1, false);
    	}
    	for (Word word : negatives) {
    		searchNode(word, -1, false);
    	}
//		System.out.println(tree.toString());
    	LinkedList<Node<ValueNode>> visited = new LinkedList<>();
    	visited.add(tree.getRoot());
    	int num = 0;
    	while(!visited.isEmpty()) {
    		Node<ValueNode> node = visited.pollFirst();
//    		System.out.println("Node: " + node.toString());
    		num ++;
    		for (Node<ValueNode> child : node.getChildren()) {
    			visited.add(child);
    		}
    	}
    	System.out.println("#Nodes: " + num);
	}

	private ExprValue getExprValue(int value) {
    	return new ExprValueInt(value);
    }
	
	protected ValueNode createNode() {
		ValueNode valueNode = new ValueNode(states.size(), alphabet.getEmptyWord());
		return valueNode;
	}

	@Override
	protected HashableValue checkMembership(Query<HashableValue> query) {
		Word word = query.getQueriedWord();
//		System.out.println("Query word: " + word.toString());
		Node<ValueNode> node = searchNode(word, 0, true);
//		System.out.println("node = " + (node == null? "null" : node.getLabel().toString()));
		if (node == null) return new HashableValueEnum(0);
//		System.out.println("node label: " + node.getLabel().get());
		return new HashableValueEnum(node.getLabel().get());
	}

	@Override
	protected Query<HashableValue> checkEquivalence(SDFA hypothesis) {
		Word cex = alphabet.getEmptyWord();
		Query<HashableValue> ceQuery = null;
		for (Word word : positives) {
    		int state = hypothesis.getSuccessor(word);
    		if (!hypothesis.isFinal(state)) {
    			ceQuery = new QuerySimple<>(word);
    	        ceQuery.answerQuery(new HashableValueBoolean(false));
    	        return ceQuery;
    		}
    	}
    	for (Word word : negatives) {
    		int state = hypothesis.getSuccessor(word);
    		if (!hypothesis.isReject(state)) {
    			ceQuery = new QuerySimple<>(word);
    	        ceQuery.answerQuery(new HashableValueBoolean(false));
    	        return ceQuery;
    		}
    	}
    	for (Word word : dontcares) {
    		int state = hypothesis.getSuccessor(word);
    		if (hypothesis.isReject(state)
    			|| hypothesis.isFinal(state)) {
    			ceQuery = new QuerySimple<>(word);
    	        ceQuery.answerQuery(new HashableValueBoolean(false));
    	        return ceQuery;
    		}
    	}
    	ceQuery = new QuerySimple<>(cex);
        ceQuery.answerQuery(new HashableValueBoolean(true));
        return ceQuery;
	} 

}
