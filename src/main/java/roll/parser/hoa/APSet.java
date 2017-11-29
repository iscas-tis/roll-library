package roll.parser.hoa;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * represents a set of atomic propositions
 * */
public class APSet implements Iterable<String> {
	
	private List<String> aps;
	
	public APSet() {
		aps = new ArrayList<>();
	}
	
	public APSet(List<String> aps) {
		this.aps = new ArrayList<>();
		for(String ap : aps) {
			addAP(ap);
		}
	}
	
	public int addAP(String ap) {
		int i = aps.indexOf(ap);
		if(i == -1) {
			aps.add(ap);
			i = aps.size() - 1;
		}
		return i;
	}
	
	public String getAP(int i) {
		return aps.get(i);
	}
	
	public int indexOf(String ap) {
		return aps.indexOf(ap);
	}
	
	public int size() {
		return aps.size();
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{");
		for(int i = 0; i < aps.size(); i ++) {
			builder.append(aps.get(i));
			if(i != aps.size() - 1) {
				builder.append(", ");
			}
		}
		builder.append("}");
		return builder.toString();
	}
	
	@Override
	public Iterator<String> iterator() {
		return aps.iterator();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof APSet) {
			APSet set = (APSet)obj;
			return this.aps.equals(set.aps);
		}
		return false;
	}

}
