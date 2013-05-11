import java.util.Vector;

/**
 * Variable.java 
 * @author William Richard willster3021@gmail.com
 * Stores a Variable in a SSAT formula.
 * This constitutes it's "name" i.e. integer value, it's current assignment 
 * as defined by the constants in this class, it's value, how many times it
 * appears positively in non-satisfied clauses, how many times it appears 
 * negatively in non-satisfied clauses, and what clauses it appears as unit in.
 */
public class Variable {
	
	//Constants to be used in assignments
	public static final int FALSE = 0;
	public static final int TRUE = 1;
	public static final int UNASSIGNED = 2;

	//the "name" of the variable, i.e. it's integer value
	private int name;
	
	//how the variable has been assigned, or if it is not yet assigned
	private int assignment;
	
	//The "value" of the variable is it's probability of being true if it is chance
	//or -1 if it is a choice variable
	private double value;
	
	//holds how many times the variable appears in unsatisfied clauses positively
	private int timesPositive;
	//holds how many times the variable appears in unsatisfied clauses negatively
	private int timesNegative;
	
	//if the variable is unit in any of it's unsatisfied clauses,
	//this stores the clause that it is unit in
	private Vector<Clause> unitClauses;
	
	/** Default constructor
	 * Sets instance variables with default values
	 */
	public Variable(int _name){
		//assign it's name
		name = _name;
		
		//it starts UNASSIGNED
		assignment = UNASSIGNED;
		
		//assume variables are choice
		value = -1.0;		
		
		timesPositive = 0;
		timesNegative = 0;
		//assumes it does not appear unit in any clauses.
		unitClauses = new Vector<Clause>();
	}

	/**
	 * @return the assignment
	 */
	public int getAssignment() {
		return assignment;
	}

	/**
	 * @param assignment the assignment to set
	 */
	public void setAssignment(int assignment) {
		this.assignment = assignment;
	}

	/**
	 * @return the value
	 */
	public double getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(double value) {
		this.value = value;
	}
	
	/**
	 * @return if the variable is a choice variable.
	 */
	public boolean isChoice() {
		return value < 0;
	}
	
	/**
	 * @return if this variable is a chance variable.
	 */
	public boolean isChance() {
		return !isChoice();
	}
	
	/**
	 * @return the chance that this variable is true.
	 * If it is not a chance variable, return -1.0
	 */
	public double getChanceTrue() {
		if(isChoice()) return -1.0;
		return value;
	}
	
	/**
	 * @return the chance that this variable is false.
	 * If it is not a chance variable, return -1.0
	 */
	public double getChanceFalse() {
		if(isChoice()) return -1.0;
		return 1.0-value;
	}
	

	/**
	 * @return the timesPositive
	 */
	public int getTimesPositive() {
		return timesPositive;
	}
	
	/**
	 * @param timesPositive the timesPositive to set
	 */
	protected void setTimesPositive(int timesPositive) {
		this.timesPositive = timesPositive;
	}

	/** Decrement the number of times this variable appears positively in unsatisfied clauses
	 * 
	 */
	public void decTimesPositive(){
		timesPositive--;
		
		assert timesPositive >= 0;
		
	}
	
	/** Increment the number of times this variable appears positively in unsatisfied clauses
	 * 
	 */
	public void incTimesPositive(){
		timesPositive++;
	}
	

	/**
	 * @return the timesNegative
	 */
	public int getTimesNegative() {
		return timesNegative;
	}
	
	/**
	 * @param timesNegative the timesNegative to set
	 */
	protected void setTimesNegative(int timesNegative) {
		this.timesNegative = timesNegative;
	}

	/** Decrement the number of times this variable appears negatively in unsatisfied clauses
	 * 
	 */
	public void decTimesNegative(){
		timesNegative--;
		
		assert timesNegative >= 0;
	}
	
	/** Increment the number of times this variable appears negatively in unsatisfied clauses
	 * 
	 */
	public void incTimesNegative(){
		timesNegative++;
	}

	/**
	 * @return the unitClauses of this variable
	 */
	public Vector<Clause> getUnitClauses() {
		return unitClauses;
	}

	/**
	 * Adds a unit clause to this Variable's list of unit clauses.
	 * @param newUnitClause - the new unit clause to add.
	 */
	public void addUnitClause(Clause newUnitClause) {
		assert newUnitClause != null;
		assert ! unitClauses.contains(newUnitClause);
		unitClauses.add(newUnitClause);
	}
	
	/**
	 * Remove a unit clause to this Variable's list of unit clauses.
	 * @param oldUnitClause - the clause to remove from this variable's list of unit clauses.
	 */
	public void removeUnitClause(Clause oldUnitClause) {
		assert oldUnitClause != null;
		assert unitClauses.contains(oldUnitClause);
		unitClauses.remove(oldUnitClause);
	}
	
	/**
	 * @param hasClause the clause we want to check.
	 * @return if this variable has the passed cause as a unit clause.
	 */
	public boolean hasUnitClause(Clause hasClause) {
		assert hasClause != null;
		return unitClauses.contains(hasClause);
	}
	
	/**
	 * @return the first unit clause this variable has stored.
	 */
	public Clause getFirstUnitClause() {
		return unitClauses.firstElement();
	}
		
	/**
	 * @return the name
	 */
	public int getName() {
		return name;
	}

	/**
	 * @return if this variable appears in a unit clause
	 */
	public boolean isUnit() {
		return unitClauses.size() > 0;
	}
	
	/** 
	 * Determines if the variable is pure,
	 * According to the times it appears positively and negatively.
	 * @return a positive number if this variable only appears positively,
	 * 			a negative number if it only appears negatively, or 0 otherwise.
	 */
	public int isPure() {
		if(timesPositive > 0 && timesNegative > 0) return 0;
		//catch the situation where both timesPositive and timesNegative are 0
		else if(timesPositive == 0 && timesNegative == 0) return 1;
		else return timesPositive - timesNegative;
		
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 * 2 variables are equal if they have the same name.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Variable))
			return false;
		Variable other = (Variable) obj;
		if (name != other.name)
			return false;
		return true;
	}
}
