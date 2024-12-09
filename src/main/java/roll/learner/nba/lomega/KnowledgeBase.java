package roll.learner.nba.lomega;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;

import roll.automata.NBA;
import roll.query.Query;
import roll.query.QuerySimple;
import roll.table.HashableValue;
import roll.table.HashableValueBoolean;

// currently we only support very limited knowledge base
// only support NBA, since equivalence query is expensive
public class KnowledgeBase {
	
	// maintain a priority queue of queries	
	private LinkedList<Data> kb;
	final int sizeLimit;
	
	public KnowledgeBase(int sizeLimit) {
		this.sizeLimit = sizeLimit;
		this.kb = new LinkedList<>();
		
	}
	
	public void addQuery(Query<HashableValue> query, boolean inTarget) {
		Data d = new Data(query, inTarget);
		Data e = contains(d);
		if (e == null) {
			d.count = 1;
			if (kb.size() >= sizeLimit
					&& !kb.isEmpty()) {
				kb.removeLast();
			}
			kb.addFirst(d);
		}else {
			// move to the front
			kb.remove(e);
			e.count ++;
			kb.addFirst(e);
		}
	}
	
	public Query<HashableValue> testCorrectness(NBA nba) {
		Iterator<Data> iter = kb.iterator();
		Data res = null;
		while (iter.hasNext()) {
			Data e = iter.next();
			// now test whether this automaton can use e
			boolean acc = nba.getAcc().accept(e.data.getPrefix(), e.data.getSuffix());
			// if acc is not equal to answer
			if (acc != e.data.getQueryAnswer().isAccepting()) {
				// we move 
				res = e;
				// remove current one
				iter.remove();
				break;
			}
		}
		if (res != null) {
			kb.addFirst(res);
			return res.data;
		}
		return null;
	}
	
	private Data contains(Data d) {
		Iterator<Data> iter = kb.iterator();
		while (iter.hasNext()) {
			Data e = iter.next();
			if (e.equals(d)) {
				return e;
			}
		}
		return null;
	}
	
	private class CompareQueries implements Comparator<Data> {
		
		// larger is better, should be on top
		@Override
		public int compare(Data o1, Data o2) {
			return o1.count - o2.count;
		}
	}
	
	private class Data {
		
		Query<HashableValue> data;
		int count;
		
		public Data(Query<HashableValue> query, boolean inTarget) {
			this.data = new QuerySimple<>(query.getPrefix(), query.getSuffix());
			this.data.answerQuery(new HashableValueBoolean(inTarget));
			this.count = 0;
		}
		
		@Override
		public boolean equals(Object o) {
			if (o instanceof Data) {
				Data d = (Data)o;
				if (d.data.getPrefix().equals(this.data.getPrefix())
				&& d.data.getSuffix().equals(this.data.getSuffix())) {
					return true;
				}
			}
			return false;
		}
		
		@Override
		public String toString() {
			return data.toString() + " : " + count;
		}
		
	}
	

}
