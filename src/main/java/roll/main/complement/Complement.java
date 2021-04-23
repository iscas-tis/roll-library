package roll.main.complement;

import roll.automata.NBA;

public abstract class Complement extends NBA {
	
    protected final NBA operand;

	public Complement(NBA operand) {
		super(operand.getAlphabet());
		this.operand = operand;
		computeInitialState();
	}
	
    protected abstract void computeInitialState();
	
	public NBA getOperand() {
		return this.operand;
	}
	
	public NBA getResult() {
		return this;
	}

}
