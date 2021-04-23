package roll.main.complement;

import roll.automata.NBA;
import roll.main.Options;

public abstract class Complement extends NBA {
	
    protected final NBA operand;
	protected Options options;

	public Complement(Options options, NBA operand) {
		super(operand.getAlphabet());
		this.operand = operand;
		this.options = options;
		computeInitialState();
	}
	
    public Options getOptions() {
    	return this.options;
    }
	
    protected abstract void computeInitialState();
	
	public NBA getOperand() {
		return this.operand;
	}
	
	public NBA getResult() {
		return this;
	}

}
