package roll.translator;

import roll.automata.NBA;
import roll.learner.nba.ldollar.LearnerLDollar;
import roll.query.Query;
import roll.query.QuerySimple;
import roll.util.Timer;

public class TranslatorBuechi extends TranslatorSimple<NBA> {

	
	protected LearnerLDollar learnerLDollar;

	public TranslatorBuechi(LearnerLDollar learner) {
		super(learner);
		this.learnerLDollar = learner;
	}

	@Override
	public boolean canRefine() {
		if(super.canRefine()) {
			return true;
		}
		
		Timer timer = new Timer();
		timer.start();
		// check whether we can still use current counter example 
//		FiniteAutomaton buechi = UtilAutomaton.convertToRabitAutomaton(learnerOmegaBuechi.getBuechi());
//		//System.out.println(learnerOmegaBuechi.getBuechi().toDot());
//		List<String> prefix = new ArrayList<String>();
//		List<String> suffix = new ArrayList<String>();
//		
//		for(int letterNr = 0; letterNr < ceQuery.getPrefix().length(); letterNr ++) {
//			prefix.add(contextWord.letterToString(ceQuery.getPrefix().getLetter(letterNr)));
//		}
//		
//		for(int letterNr = 0; letterNr < ceQuery.getSuffix().length(); letterNr ++) {
//			suffix.add(contextWord.letterToString(ceQuery.getSuffix().getLetter(letterNr)));
//		}
//
//		//System.out.println(prefix);
//		//System.out.println(suffix);
//		boolean accepted = BuechiRunner.isAccepting(buechi, prefix, suffix);

		// (u, v) is in target 
		boolean result = false ;
//		if (ceQuery.getQueryAnswer().isCeInTarget){
//			result = ! accepted;
//		}//else
//		else {
//			result = accepted;
//		}
		
		timer.stop();
		options.stats.timeOfTranslator  += timer.getTimeElapsed();
		
		return result;
	}

	@Override
	public Query<Boolean> translate() {
		Query<Boolean> query = new QuerySimple<>(ceQuery.getPrefix(), 
				ceQuery.getSuffix());
//		query.answerQuery(ceQuery.getQueryAnswer().isCeInTarget);
		return query;
	}

}
