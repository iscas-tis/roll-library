package roll.learner.sdfa;

import gnu.trove.iterator.TIntObjectIterator;
import roll.automata.DFA;
import roll.automata.SDFA;
import roll.learner.LearnerType;
import roll.learner.dfa.tree.LearnerDFATree;
import roll.learner.dfa.tree.ValueNode;
import roll.main.Options;
import roll.oracle.MembershipOracle;
import roll.table.ExprValue;
import roll.table.HashableValue;
import roll.tree.Node;
import roll.tree.TreeBinaryExpoterDOT;
import roll.tree.TreePrinterBoolean;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;
import roll.words.Alphabet;
import roll.words.Word;

public class LearnerSDFATree extends LearnerDFATree {

	private SDFA sdfa;

	public LearnerSDFATree(Options options, Alphabet alphabet, MembershipOracle<HashableValue> membershipOracle) {
		super(options, alphabet, membershipOracle);
	}

	@Override
	public LearnerType getLearnerType() {
		return LearnerType.SDFA_TREE;
	}

	protected DFA createConjecture() {
		this.sdfa = new SDFA(alphabet);
		return sdfa;
	}

	@Override
	protected void setRejecting(ValueNode state) {
		if (state.node.isRejecting()) {
			sdfa.setReject(state.id);
		}
	}

	// sift the word to one equivalence class
	
	// find partition (leaf node) for the given word
    private Partition findNodePartition(Word word) {
//    	System.out.println("Trying to find the word partition in tree: " + word.toString());
//    	System.out.println("Tree: " + TreePrinterBoolean.toString(tree));
        return findNodePartition(word, tree.getRoot());
    }
    
    private Partition findNodePartition(Word word, Node<ValueNode> nodeCurr) {
        while(! nodeCurr.isLeaf()) {
            HashableValue result = processMembershipQuery(word, nodeCurr.getLabel());
            Node<ValueNode> node = nodeCurr.getChild(result);
            if(node == null) {
                return new Partition(false, nodeCurr, result); // new leaf node
            }
            nodeCurr = node; // possibly we can not find that node
        }
        return new Partition(true, nodeCurr, null);
    }
	
    class Partition {
        boolean found ;
        Node<ValueNode> node;
        HashableValue branch;
        
        public Partition(boolean f, Node<ValueNode> n, HashableValue v) {
            this.found = f;
            this.node = n;
            this.branch = v;
        }
    }
    
    private Node<ValueNode> addNode(Node<ValueNode> parent, HashableValue branch, ExprValue nodeLabel) {
        Node<ValueNode> nodeLeaf = getValueNode(parent, branch, nodeLabel);
        ValueNode stateLeaf =  new ValueNode(states.size(), nodeLabel.get());
        stateLeaf.node = nodeLeaf;
        nodeLeaf.setValue(stateLeaf);
        states.add(stateLeaf); // add new state
        
        parent.addChild(branch, nodeLeaf);
        // test whether this node is accepting
        Word period = nodeLabel.get();
        HashableValue result = processMembershipQuery(period, tree.getRoot().getLabel());
        if (result.isAccepting())
			nodeLeaf.setAcceting();
		else if (result.isRejecting())
			nodeLeaf.setRejecting();

        updatePredecessors(stateLeaf.id, 0, alphabet.getLetterSize() - 1);
        return nodeLeaf;
    }
    
    // may add new leaf node during successor update
    @Override
    protected void updatePredecessors(int stateNr, int from, int to) {
        assert stateNr < states.size()
        && from >= 0
        && to < alphabet.getLetterSize();
        
        ValueNode state = states.get(stateNr);
        Word label = state.label;
        for(int letter = from; letter <= to; letter ++) {
            Word wordSucc = label.append(letter);
            Partition nodeSucc = findNodePartition(wordSucc);
            if(nodeSucc.found) {
                updateTransition(stateNr, letter, nodeSucc.node.getValue().id);
            }else {
                Node<ValueNode> node = addNode(nodeSucc.node, nodeSucc.branch, getExprValueWord(wordSucc));
                updateTransition(stateNr, letter, node.getValue().id);
            }
        }
    }
    
    @Override
    protected void updatePredecessors() {
        
        TIntObjectIterator<ISet> iterator = nodeToSplit.getValue().predecessors.iterator();
        Node<ValueNode> parent = nodeToSplit.getParent();
        ISet letterDeleted = UtilISet.newISet();
        while(iterator.hasNext()) {
            iterator.advance();
            int letter = iterator.key();
            ISet statePrevs = iterator.value().clone(); 
            ISet stateRemoved = UtilISet.newISet();
            // when addNode is called, statePrevs will not add states, 
            // but iterator.value() may add new states
            // since we do not care new added states, they are correct
            for(final int stateNr : statePrevs) {
                ValueNode statePrev = states.get(stateNr);
                Word wa = statePrev.label.append(letter);
                Partition partition = findNodePartition(wa, parent);
                if(partition.found) {
                    if (partition.node != nodeToSplit) {
                        updateTransition(stateNr, letter, partition.node.getValue().id);
                        stateRemoved.set(stateNr); // remove this predecessor
                    } // change to other leaf node
                }else {
                    Node<ValueNode> node = addNode(partition.node, partition.branch
                            , getExprValueWord(wa));
                    updateTransition(stateNr, letter, node.getValue().id);
                    stateRemoved.set(stateNr);  // remove this predecessor
                }
            }
            ISet temp = iterator.value().clone(); 
            temp.andNot(stateRemoved);
            if(temp.isEmpty()) {
                letterDeleted.set(letter);
            }else {
                iterator.setValue(temp);
            }
        }
        
        for(final int letter : letterDeleted) {
            nodeToSplit.getValue().predecessors.remove(letter);
        }
        
    }
    
    // we need to change the accepting/rejecting of nodes
    // if we change the previous leaf node to internal node,
    // we need to set the acceptance correctly
    @Override
	protected void setAcceptingNodes(Node<ValueNode> nodePrev, Node<ValueNode> nodePrevNew
	        , Node<ValueNode> nodeLeaf, boolean rootChanged) {
    	HashableValue mqLeaf = processMembershipQuery(nodeLeaf.getLabel().get()
    			, alphabet.getEmptyWord());
//    	System.out.println("Acc: " + nodeLeaf.getLabel().get() + ": " + mqLeaf);
    	if (mqLeaf.isAccepting()) {
    		nodeLeaf.setAcceting();
    	}else if (mqLeaf.isRejecting()) {
    		nodeLeaf.setRejecting();
    	}
    	if(nodePrev.isAccepting()) {
            nodePrevNew.setAcceting();
        }else if (nodePrev.isRejecting()) {
        	nodePrevNew.setRejecting();
        }
	}

	@Override
	protected CeAnalyzerTree getCeAnalyzerInstance(ExprValue exprValue, HashableValue result) {
		return new CeAnalyzerSDFATree(exprValue, result);
	}

	// analyze counterexample
	protected class CeAnalyzerSDFATree extends CeAnalyzerTree {

		public CeAnalyzerSDFATree(ExprValue exprValue, HashableValue result) {
			super(exprValue, result);
		}
		
		@Override
		public void analyze() {
			this.leafBranch = result;
			// only has one leaf
			if(tree.getRoot().isLeaf()) {
				this.wordExpr = getExprValueWord(alphabet.getEmptyWord());
				this.nodePrev = tree.getRoot();
				this.wordLeaf = getExprValueWord(getWordExperiment());
				this.nodePrevBranch = processMembershipQuery(alphabet.getEmptyWord(), wordExpr);
				return ;
			}
			// when root is not a terminal node
			CeAnalysisResult result = findBreakIndex();
			update(result);
		}
		
        @Override
        protected void update(CeAnalysisResult result) {
            super.update(result);
            this.nodePrevBranch = processMembershipQuery(getStateLabel(result.currState), wordExpr);
        }
	}
	
	@Override
	public String toString() {
		return TreePrinterBoolean.toString(tree);
	}

}
