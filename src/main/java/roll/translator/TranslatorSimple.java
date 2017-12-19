package roll.translator;

import roll.automata.Acceptor;
import roll.learner.LearnerBase;
import roll.main.Options;
import roll.query.Query;
import roll.query.QuerySimple;
import roll.words.Alphabet;

public class TranslatorSimple<M extends Acceptor> implements Translator {

	protected boolean called;
	protected Query<Boolean> ceQuery = null;
	protected Alphabet alphabet;
	protected Options options;
	
	public TranslatorSimple(LearnerBase<M> learner) {
		this.called = false;
		this.alphabet = learner.getHypothesis().getAlphabet();
		this.options = learner.getOptions();
	}
	
	@Override
	public boolean canRefine() {
		assert ceQuery != null;
		if(! called) {
			called = true;
			return true;
		}
		return false;
	}

	@Override
	public Query<Boolean> translate() {
		return new QuerySimple<>(ceQuery.getPrefix(), ceQuery.getSuffix());
	}

	@Override
	public void setQuery(Query<Boolean> query) {	
		this.ceQuery = query;
	}

    @Override
    public Alphabet getAlphabet() {
        return alphabet;
    }

    @Override
    public Options getOptions() {
        return options;
    }
}
