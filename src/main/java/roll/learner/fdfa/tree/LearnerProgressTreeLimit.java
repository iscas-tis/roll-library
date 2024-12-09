/* Copyright (c) 2016, 2017                                               */
/*       Institute of Software, Chinese Academy of Sciences               */
/* This file is part of ROLL, a Regular Omega Language Learning library.  */
/* ROLL is free software: you can redistribute it and/or modify           */
/* it under the terms of the GNU General Public License as published by   */
/* the Free Software Foundation, either version 3 of the License, or      */
/* (at your option) any later version.                                    */

/* This program is distributed in the hope that it will be useful,        */
/* but WITHOUT ANY WARRANTY; without even the implied warranty of         */
/* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          */
/* GNU General Public License for more details.                           */

/* You should have received a copy of the GNU General Public License      */
/* along with this program.  If not, see <http://www.gnu.org/licenses/>.  */

package roll.learner.fdfa.tree;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import roll.automata.DFA;
import roll.learner.LearnerType;
import roll.learner.dfa.tree.ValueNode;
import roll.learner.fdfa.LearnerLeading;
import roll.learner.fdfa.LearnerProgressLimit;
import roll.main.Options;
import roll.oracle.MembershipOracle;
import roll.query.Query;
import roll.query.QuerySimple;
import roll.table.HashableValue;
import roll.table.HashableValueBoolean;
import roll.tree.Node;
import roll.util.Pair;
import roll.words.Alphabet;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class LearnerProgressTreeLimit extends LearnerProgressTree implements LearnerProgressLimit {

public LearnerProgressTreeLimit(Options options, Alphabet alphabet,
            MembershipOracle<HashableValue> membershipOracle, LearnerLeading learnerLeading, int state) {
        super(options, alphabet, membershipOracle, learnerLeading, state);
    }

    @Override
    public LearnerType getLearnerType() {
        return LearnerType.FDFA_LIMIT_TREE;
    }
    
    @Override
    public void startLearning() {
        initialize();
    }
    
    @Override
    public HashableValue prepareRowHashableValue(boolean mqResult, Word x, Word e) {
        DFA leadDFA = getLearnerLeading().getHypothesis();
        int stateUX = leadDFA.getSuccessor(getLeadingState(), x);
        int stateUXE = leadDFA.getSuccessor(stateUX, e);
        boolean recur = stateUXE == getLeadingState();
        return getHashableValueBool(!recur || mqResult);
    }
    
	// we have to be careful about the initial tree with only
	// one terminal node of epsilon
	@Override
	public List<Query<HashableValue>> computeMark() {
		DFA leadDFA = getLearnerLeading().getHypothesis();
		// we need to traverse the tree to obtain the representatives and internal
		// experiments
		// we traverse the whole tree until we found the conflict or return
		int conflict = 0;
		Query<HashableValue> pos = null;
		Query<HashableValue> neg = null;
		HashableValue left = getHashableValueBool(false);
		HashableValue right = getHashableValueBool(true);

		LinkedList<Pair<Node<ValueNode>, Boolean>> stack = new LinkedList<>();
		Node<ValueNode> root = tree.getRoot();
		stack.addLast(new Pair<>(root, false));

		while (!stack.isEmpty() && conflict < 3) {
			Pair<Node<ValueNode>, Boolean> curr = stack.getLast();
			if (curr.getLeft().isLeaf()) {
				// we now check the list of experiments
				Word wordX = curr.getLeft().getLabel().get();
				for (Pair<Node<ValueNode>, Boolean> interNode : stack) {
					Word wordE = interNode.getLeft().getLabel().get();
					Word loop = wordX.concat(wordE);
					// ignore empty word or itself
					if (loop.isEmpty() || interNode == curr)
						continue;
					boolean recur = state == leadDFA.getSuccessor(state, wordX.concat(wordE));
					if (recur && interNode.getRight()) {
						pos = new QuerySimple<>(alphabet.getEmptyWord(), loop);
						pos.answerQuery(getHashableValueBool(true));
						conflict |= 2;
					} else if (recur && !interNode.getRight()) {
						neg = new QuerySimple<>(alphabet.getEmptyWord(), loop);
						neg.answerQuery(getHashableValueBool(false));
						conflict |= 1;
					}
				}
				// already processed the curr node
				stack.removeLast();
				// we already traversed the one branch
				// switch to another branch if one branch is finished
				while (!stack.isEmpty()) {
					Pair<Node<ValueNode>, Boolean> parent = stack.getLast();
					stack.removeLast();
					if (!parent.getRight()) {
						Pair<Node<ValueNode>, Boolean> copy = new Pair<>(parent.getLeft(), true);
						stack.addLast(copy);
						break;
					}
				}

			} else {
				if (!curr.getRight()) {
					// we try left branch
					Node<ValueNode> leftNode = curr.getLeft().getChild(left);
					if (leftNode != null) {
						stack.addLast(new Pair<>(leftNode, false));
					} else {
						// try right
						stack.removeLast();
						stack.addLast(new Pair<>(curr.getLeft(), true));
					}
				} else {
					Node<ValueNode> rightNode = curr.getLeft().getChild(right);
					if (rightNode != null) {
						stack.addLast(new Pair<>(rightNode, false));
					} else {
						// back trace to upper level
						stack.removeLast();
					}
				}
			}
		}
		List<Query<HashableValue>> res = new ArrayList<>();
		if (pos != null) {
			res.add(pos);
		}
		if (neg != null) {
			res.add(neg);
		}

		return res;
	}

}
