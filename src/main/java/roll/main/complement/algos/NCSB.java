package roll.main.complement.algos;

import roll.util.sets.ISet;
import roll.util.sets.UtilISet;

/**
 * NCSB tuple 
 * TODO: in order to make it unmodifiable
 * */
public class NCSB {
	
	protected ISet nSet;
	protected ISet cSet;
	protected ISet sSet;
	protected ISet bSet;
	
	public NCSB(ISet N, ISet C, ISet S, ISet B) {
		this.nSet = N;
		this.cSet = C;
		this.sSet = S;
		this.bSet = B;
	}
	
	public NCSB() {
		this.nSet = UtilISet.newISet();
		this.cSet = UtilISet.newISet();
		this.sSet = UtilISet.newISet();
		this.bSet = UtilISet.newISet();
	}
	
	// be aware that we use the same object
	//CLONE object to make modification
	public ISet getNSet() {
		return  nSet;
	}
	
	public ISet getCSet() {
		return  cSet;
	}
	
	public ISet getSSet() {
		return  sSet;
	}
	
	public ISet getBSet() {
		return  bSet;
	}
	
	// Safe operations for (N, C, S, B)
	public ISet copyNSet() {
		return  nSet.clone();
	}
	
	public ISet copyCSet() {
		return  cSet.clone();
	}
	
	public ISet copySSet() {
		return  sSet.clone();
	}
	
	public ISet copyBSet() {
		return  bSet.clone();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(!(obj instanceof NCSB)) {
			return false;
		}
		NCSB ncsb = (NCSB)obj;
		return  contentEqual(ncsb);
	}
	
	protected boolean contentEqual(NCSB ncsb) {
		if(! nSet.equals(ncsb.nSet)
		|| ! cSet.equals(ncsb.cSet)
		|| ! sSet.equals(ncsb.sSet)
		|| ! bSet.equals(ncsb.bSet)) {
			return false;
		}
		return true;
	}
	

	public boolean coveredBy(NCSB other) {
	    boolean lazys = true;
	    if(lazys && !other.bSet.subsetOf(bSet)) {
            return false;
        }
		if(! other.nSet.subsetOf(nSet)
		|| ! other.cSet.subsetOf(cSet)
		|| ! other.sSet.subsetOf(sSet)) {
			return false;
		}

		return true;
	}
	
	// this.N >= other.N & this.C >= other.C & this.S >= other.S & this.B >= other.B
	public boolean strictlyCoveredBy(NCSB other) {
		if(! other.nSet.subsetOf(nSet)
		|| ! other.cSet.subsetOf(cSet)
		|| ! other.sSet.subsetOf(sSet)
		|| ! other.bSet.subsetOf(bSet)) {
			return false;
		}

		return true;
	}
	
	private ISet allSets = null; 
	
	private void initializeAllSets() {
	    allSets = copyNSet();
	    allSets.or(cSet);
	    allSets.or(sSet);
	}
	
	public boolean subsetOf(NCSB other) {
	    if(allSets == null) {
	        initializeAllSets();
	    }
	    if(other.allSets == null) {
	        other.initializeAllSets();
	    }
        return allSets.subsetOf(other.allSets);
	}
	
	@Override
	public NCSB clone() {
		return new NCSB(nSet.clone(), cSet.clone(), sSet.clone(), bSet.clone());
	}
	
	@Override
	public String toString() {
		return "(" + nSet.toString() + "," 
		           + cSet.toString() + ","
		           + sSet.toString() + ","
		           + bSet.toString() + ")";
	}
	
    protected int hashCode;
    protected boolean hasCode = false;
	
	@Override
	public int hashCode() {
		if(hasCode) return hashCode;
		else {
			hasCode = true;
			hashCode = 1;
			final int prime = 31;
			hashCode= prime * hashCode + nSet.hashCode();
			hashCode= prime * hashCode + cSet.hashCode();
			hashCode= prime * hashCode + sSet.hashCode();
			hashCode= prime * hashCode + bSet.hashCode();
			return hashCode;
		}
	}

}

