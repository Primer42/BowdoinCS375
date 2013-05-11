/**
 * Literal.java 
 * @author William Richard willster3021@gmail.com
 * Stores a Literal in a Clause.  This consists of a Variable, and if it appears
 * positively or negativey.
 */
public class Literal {	
	
	//the variable that is in the literal
	private Variable variable;
	//it's sign i.e. if it is positive or negative
	private boolean sign;
	
	/**
	 * Basic constructor.
	 * @param v - variable for the clause
	 * @param s - it's sign - true for positive, false for negative.
	 */
	public Literal(Variable v, boolean s) {
		variable = v;
		sign = s;
	}

	/**
	 * @return the variable
	 */
	public Variable getVariable() {
		return variable;
	}

	/**
	 * @return the sign of the literal
	 */
	public boolean getSign() {
		return sign;
	}

	/** 
	 * Looks at the variable and figures out if this literal is satisfied
	 * @return if the literal is satisfied
	 */
	public boolean isSatisfied() {
		if(sign) {
			return variable.getAssignment() == Variable.TRUE;
		} else {
			return variable.getAssignment() == Variable.FALSE;
		}
	}
	
	/**
	 * @return a human readable representation of this Literal.
	 */
	public String toString() {
		String returnString = "";
		//put nothing for a positive variable, a "-" for a negative variable
		if(! getSign()) returnString = returnString.concat("-");
		
		//put the variable name
		returnString = returnString.concat("" + getVariable().getName());
		
		return returnString;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 * Two Literals are equal if they have the same variable with the same sign.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Literal))
			return false;
		Literal other = (Literal) obj;
		if (sign != other.sign)
			return false;
		if (variable == null) {
			if (other.variable != null)
				return false;
		} else if (!variable.equals(other.variable))
			return false;
		return true;
	}
}
