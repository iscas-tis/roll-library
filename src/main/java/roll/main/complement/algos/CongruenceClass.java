package roll.main.complement.algos;

import java.util.TreeSet;

import roll.main.inclusion.congr.IntBoolTriple;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;

public class CongruenceClass {
	
	protected ISet guess;
	protected TreeSet<IntBoolTriple> level;
	protected boolean isSet;
	protected boolean[][] fsim;
	protected boolean[][] bsim;
	
	public CongruenceClass(ISet set, boolean[][] fsim, boolean[][] bsim) {
		this.guess = set;
		this.level = new TreeSet<>();
		this.isSet = true;
		this.fsim = fsim;
		this.bsim = bsim;
	}
	
	public CongruenceClass(TreeSet<IntBoolTriple> level, boolean[][] fsim, boolean[][] bsim) {
		this.level = level;
		this.guess = UtilISet.newISet();
		this.isSet = false;
		this.fsim = fsim;
		this.bsim = bsim;
	}
	
	private ISet minimizeGuess(ISet set) {
		ISet result = UtilISet.newISet();
		for(int p : set) {
			int maxP = p;
			for(int q : set) {
				if(fsim[maxP][q]) {
					maxP = q;
				}
			}
			result.set(maxP);
		}
		return result;
	}
	
	public void minimize() {
		if(this.isSet) {
			// only keep the maximal one, if they are equivalent, then keep the larger one
//			this.guess = minimizeGuess(this.guess);
			ISet result = UtilISet.newISet();
			for(int p : this.guess) {
				int maxP = p;
				for(int q : this.guess) {
					// more strict computation
					if(fsim[maxP][q] && bsim[maxP][q]) {
						maxP = q;
					}
				}
				result.set(maxP);
			}
			this.guess = result;
		}else {
			// minimize the equivalence class in level
			TreeSet<IntBoolTriple> result = new TreeSet<>();
			for(IntBoolTriple left : this.level) {
				// find maximal for left
				IntBoolTriple maxTriple = left;
				System.out.println("Curr = " + maxTriple);
				for(IntBoolTriple right : this.level) {
					if(bsim[maxTriple.getLeft()][right.getLeft()]
					&& fsim[maxTriple.getRight()][right.getRight()]
					&& (!maxTriple.getBool() || right.getBool())) {
						maxTriple = right;
						System.out.println("updated to " + right);
					}
				}
				result.add(maxTriple);
			}
			this.level = result;
		}
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
	
	private boolean isGuessEqual(ISet left, ISet right) {
		for(int p : left) {
			boolean contained = false;
			for(int q : right) {
				if(fsim[p][q] && fsim[q][p]) {
					contained = true;
					break;
				}
			}
			if(! contained) {
				return false;
			}
		}
		for(int p : right) {
			boolean contained = false;
			for(int q : left) {
				if(fsim[p][q] && fsim[q][p]) {
					contained = true;
					break;
				}
			}
			if(! contained) {
				return false;
			}
		}
		return true;
	}
	
	private boolean isLevelEqual(TreeSet<IntBoolTriple> left, TreeSet<IntBoolTriple> right) {
		for(IntBoolTriple fst : left) {
			boolean contained = false;
			for(IntBoolTriple snd : right) {
				if(fst.getLeft() == snd.getLeft()
				&& fst.getBool() == snd.getBool()
				&& fsim[fst.getRight()][snd.getRight()] && fsim[snd.getRight()][fst.getRight()]) {
					contained = true;
					break;
				}
			}
			if(! contained) {
				return false;
			}
		}
		for(IntBoolTriple fst : right) {
			boolean contained = false;
			for(IntBoolTriple snd : left) {
				if(fst.getLeft() == snd.getLeft()
				&& fst.getBool() == snd.getBool()
				&& fsim[fst.getRight()][snd.getRight()] && fsim[snd.getRight()][fst.getRight()]) {
					contained = true;
					break;
				}
			}
			if(! contained) {
				return false;
			}
		}
		return true;
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
    
    public boolean isAccepted(ISet leadingSet) {
    	if(this.isSet ) {
    		return guess.isEmpty();
    	}else {
    		if(this.level.isEmpty()) {
        		return false;
        	}else {
        		ISet post = UtilISet.newISet();
        		for(IntBoolTriple triple : this.level) {
        			post.set(triple.getRight());
        		}
        		// first, pre needs to be the SAME/simulated same
        		ISet simulatedPost = this.minimizeGuess(post);
        		ISet simulatedInit = this.minimizeGuess(leadingSet);
//        		System.out.println("post = " + post + " sim = " + simulatedPost);
//        		System.out.println("lset = " + leadingSet + " sim = " + simulatedInit);
        		if(! simulatedInit.contentEq(simulatedPost)) {
        			return false;
        		}
        		// we need it to be accepted by complement language
        		return ! UtilCongruence.decideAcceptance(leadingSet, level, fsim);
        	}
    	}
    }

}
