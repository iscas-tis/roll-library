package roll.table;

public class ExprValueInt implements ExprValue {
	
	private final int value;
	
	public ExprValueInt(int value) {
		this.value = value;
	}
	
	@Override
	public int compareTo(ExprValue o) {
		Integer rValue = o.get(); 
		return value - rValue.intValue();
	}

	@Override
	public boolean valueEqual(ExprValue cvalue) {
		Integer rValue = cvalue.get(); 
		return value == rValue.intValue();
	}

	@Override
	public Integer get() {
		// TODO Auto-enerated method stub
		return value;
	}

	@Override
	public boolean isPair() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public <T> T getLeft() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T getRight() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int hashCode() {
		return value;
	}
	
	@Override
	public String toString() {
		if (value > 0)
			return "+";
		else if(value < 0)
			return "-";
		else return "?";
	}

}
