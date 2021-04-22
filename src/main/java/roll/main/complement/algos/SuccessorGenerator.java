package roll.main.complement.algos;

import roll.main.Options;
import roll.util.sets.ISet;
import roll.util.sets.PowerSet;

public class SuccessorGenerator {
	
	private boolean isCurrBEmpty;
	private final NCSB succNcsb;
	
	private ISet minusFSuccs;
	private ISet interFSuccs;
	
	private ISet fSet;       // so far all final states
	
	private ISet nPrime;  // d(N)\F\B'\S'
	private ISet vPrime;  // d(C) \/ (d(N) /\ F)
	private ISet mustIn;  // must in states in C or B
	private ISet sPrime;  // d(S)
	private ISet bPrime;  // d(B)
	
	private PowerSet ps;	
	
	private Options options;
		
	public SuccessorGenerator(Options options, boolean isBEmpty, NCSB succ, ISet minusFSuccs, ISet interFSuccs, ISet f) {
		this.options = options;
		
		this.isCurrBEmpty = isBEmpty;
		this.succNcsb = succ;
				
		this.minusFSuccs = minusFSuccs;
		this.interFSuccs = interFSuccs;
		this.fSet = f;
		
		// initialization
		initialize();
	}
	
	private void initialize() {
		
		// N'
		nPrime =  this.succNcsb.copyNSet();
		nPrime.andNot(fSet);                    // remove final states
		nPrime.andNot(succNcsb.getCSet());   // remove successors of C, the final states of NSuccs are in CSuccs 
		nPrime.andNot(succNcsb.getSSet());   // remove successors of S
		
		// V' = d(C) \/ (d(N)/\F)
		vPrime =  succNcsb.copyCSet();
		ISet nInterFSuccs =  succNcsb.copyNSet();
		nInterFSuccs.and(fSet);           // (d(N) /\ F)
		vPrime.or(nInterFSuccs);       // d(C) \/ (d(N) /\ F)
		
		// S successors
		sPrime =  succNcsb.getSSet();
		
		// B successors
		bPrime =  succNcsb.getBSet();
		
		// compute must in (C/B) states
		// in order not to mess up the code with the description 
		// some lines may repeat in different situation
		if(options.lazyS) {
			// lazy NCSB initialization
			if(isCurrBEmpty) {
				interFSuccs = succNcsb.copyCSet(); // set to d(C)
				// must in states computation
				mustIn = succNcsb.copyCSet();
				mustIn.and(fSet);                  // d(C) /\ F
				mustIn.or(nInterFSuccs);         // C_under = d(C\/N) /\F
			}else {
				mustIn = interFSuccs.clone(); // d(B/\F)
				mustIn.and(fSet);                // d(B/\F) /\F
				mustIn.or(minusFSuccs);       // B_under = d(B\F) \/ (d(B/\F) /\F)
			}
		}else {
			// original NCSB
			mustIn = interFSuccs.clone(); // d(C/\F)
			mustIn.and(fSet);                // d(C/\F) /\F
			mustIn.or(minusFSuccs);       // d(C\F) \/ (d(C/\F) /\F)
			mustIn.or(nInterFSuccs);       // C_under = d(C\F) \/ (d(C/\F) /\F) \/ (d(N)\/ F)
		}
		
		// compute nondeterministic states from interFSuccs
		interFSuccs.andNot(minusFSuccs);     // remove must-in C (B) states
		interFSuccs.andNot(sPrime);          // remove must in S states
		interFSuccs.andNot(fSet);               // remove final states 

		ps = new PowerSet(interFSuccs);
		
	}
	
	public boolean hasNext() {
		return ps.hasNext();
	}
	
	public NCSB next() {
		ISet toS = ps.next(); // extra states to be added into S'
		ISet left = interFSuccs.clone();
		left.andNot(toS);
		// this is implementation for NCSB 
		ISet NP = nPrime;
		ISet CP =  null;
		ISet SP =  sPrime.clone();
		ISet BP = null;
		
		if(options.lazyS) {
			SP.or(toS); // S'=d(S)\/M'
			if(isCurrBEmpty) {
				// as usual S and C
				CP = mustIn.clone();
				CP.or(left); // C' get extra
				if(! options.lazyB) {
					BP = CP;
				}else {
					// following is d(C) /\ C'
					BP = succNcsb.copyCSet(); 
					BP.and(CP);   // B'= d(C) /\ C'
				}
			}else {
				// B is not empty
				BP = mustIn.clone();
				BP.or(left); // B'=d(B)\M'
				CP = vPrime.clone();
				CP.andNot(SP); // C'= V'\S'
			}
			
			assert !SP.overlap(fSet) && !BP.overlap(SP) : "S:" + SP.toString() + " B:" + BP.toString();

		}else {
			// original NCSB
			CP = mustIn.clone();
			CP.or(left);
			SP.or(toS);
			if(isCurrBEmpty) {
				if(! options.lazyB ) {
					BP = CP;
				}else {
					BP = succNcsb.copyCSet();
					BP.and(CP);
				}
			}else {
				BP =  bPrime.clone();
				BP.and(CP);
			}
			
			assert !SP.overlap(fSet) && !CP.overlap(SP) : "S:" + SP.toString() + " C:" + CP.toString();
		}

		return new NCSB(NP, CP, SP, BP);
	}
	
	
	

}

