package roll.main.complement.algos;

import java.util.TreeSet;

import roll.main.inclusion.congr.IntBoolTriple;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;

public class CongruenceClass {
	
	protected ISet guess;
	protected TreeSet<IntBoolTriple> level;
	protected boolean isSet;
	
	public CongruenceClass(ISet set) {
		this.guess = set;
		this.level = new TreeSet<>();
		this.isSet = true;
	}
	
	public CongruenceClass(TreeSet<IntBoolTriple> level) {
		this.level = level;
		this.guess = UtilISet.newISet();
		this.isSet = false;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(!(obj instanceof CongruenceClass)) {
			return false;
		}
		CongruenceClass other = (CongruenceClass)obj;
		return  contentEqual(other);
	}
	
	protected boolean contentEqual(CongruenceClass other) {
		if( this.isSet != other.isSet
		|| ! guess.contentEq(other.guess)
		|| ! level.equals(other.level)) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		if(! this.isSet) {
			return level.toString();
		}else {
			return guess.toString();
		}
	}
	
	protected boolean hasCode = false;
    protected int hcValue ;
    @Override
    public int hashCode() {
        if(hasCode) {
            return hcValue;
        }else {
        	hasCode = true;
            hcValue = guess.hashCode();
            hcValue = hcValue + (isSet? 1 : 0);
            for(IntBoolTriple triple : level) {
            	hcValue = hcValue * 31 + triple.hashCode();
            }
            return hcValue;            
        }
    }
    
    public boolean isAccepted() {
    	if(this.isSet ) {
    		return guess.isEmpty();
    	}else {
    		if(this.level.isEmpty()) {
        		return false;
        	}else {
        		ISet pre = UtilISet.newISet();
        		ISet post = UtilISet.newISet();
        		for(IntBoolTriple triple : this.level) {
        			pre.set(triple.getLeft());
        			post.set(triple.getRight());
        		}
        		// first, pre needs to be the SAME
        		if(!pre.contentEq(post)) {
        			return false;
        		}
        		// we need it to be accepted by complement language
        		return ! UtilCongruence.decideAcceptance(pre, level);
        	}
    	}
    }

}
