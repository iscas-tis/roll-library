package roll.table;


public class HashableValueEnum implements HashableValue {
	
	public static enum EValue {
		Q,
		T,
        F;
    }
	
	private EValue value;
	
	public HashableValueEnum(int value) {
		if (value > 0) {
			this.value = EValue.T;
		}else if (value < 0) {
			this.value = EValue.F;
		}else {
			this.value = EValue.Q;
		}
	}
	
	@Override
	public boolean valueEqual(HashableValue rvalue) {
		if(this == rvalue) return true;
	    if( rvalue instanceof HashableValueEnum) {
	    	HashableValueEnum other = (HashableValueEnum)rvalue;
	        return other.value == value;
	    }
		return false;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null) return false;
		if (this == o) return true;
		if (o instanceof HashableValueEnum) {
			HashableValueEnum other = (HashableValueEnum)o;
			return this.valueEqual(other);
		}
		return false;
	}

	@Override
	public <T> T get() {
		return null;
	}

	@Override
	public boolean isPair() {
		return false;
	}

	@Override
	public <T> T getLeft() {
		return null;
	}

	@Override
	public <T> T getRight() {
		return null;
	}
	
	@Override
	public int hashCode() {
	    switch(value) {
	    case T:
	        return 1;
	    case F:
	        return -1;
	    case Q:
	        return 0;
	    default:
	            throw new UnsupportedOperationException("No such value for right component");
	    }
	}

	
	@Override
	public String toString() {
	    switch(value) {
	    case T:
	        return "+";
	    case F:
	        return "-";
	    case Q:
	        return "?";
	    default:
	            throw new UnsupportedOperationException("No such value for right component");
	    }
	}
	
	@Override
	public boolean isAccepting() {
		return value == EValue.T;
	}
	
	@Override
	public boolean isRejecting() {
		return value == EValue.F;
	}

}
