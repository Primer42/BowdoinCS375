import java.util.Vector;

/**
 * Clause
 * @author William Richard willster3021@gmail.com
 * Stores a SAT clause as a Vector of Literals.
 * Also, updates the variable statistics when assignments are made
 * if variableAssigned or variableUnassigned are called.
 *
 */

public class Clause {

	//stores the unsatisfied literals in the clause
	//i.e. if a variable v is false in this clause, but then is assigned true overall,
	//it's literal would be removed from the vector 
	private Vector<Literal> literals;

	/** set up the Vectors to store the information
	 */
	public Clause() {
		literals = new Vector<Literal>();
	}

	/**
	 * Go through all of the literals and see if any are satisfied
	 * If any literal is satisfied, then the clause is satisfied.
	 * @return the satisfied
	 */
	public boolean isSatisfied() {
		return getNumSatisfiedLiterals() > 0;
	}

	/**
	 * Determine if the clause is unsatisfied.
	 * @return
	 */
	public boolean isUnsatisfied() {
		for(Literal l : literals) {
			//try to find at least 1 literal that is assigned to a true value or assigned yet
			//in other words, try to find a literal that is satisfied or the variable is unassigned
			//if we can find 1 such literal, this clause is not UNSAT
			if(l.isSatisfied() || l.getVariable().getAssignment() == Variable.UNASSIGNED) return false;
		}

		return true;
	}

	/**
	 * @return the number of satisfied literals in this clause.
	 */
	public int getNumSatisfiedLiterals() { 
		int numSatLiterals = 0;
		for(Literal l : literals) {
			if(l.isSatisfied()) numSatLiterals++;
		}
		return numSatLiterals;
	}
	
	/** 
	 * @return the total number of literals in this clause.
	 */
	public int getNumLiterals() {
		return literals.size();
	}
	
	/**
	 * @return the number of literals that are not yet satisfied.
	 * This is not the number of UNSAT literals, but the number of literals
	 * that are not SAT.
	 */
	public int getNumNonSatLiterals() {
		return getNumLiterals() - getNumSatisfiedLiterals();
	}


	/** add a new variable to the clause
	 * This is only used when setting up the clause
	 */
	public void addVariable(Variable v, boolean vSign) {
		//make a new literal
		Literal newLit = new Literal(v, vSign);

		if(SsatSolver.DEBUG) {
			System.out.println("Adding a literal: '" + newLit + "'");
		}

		//add it to the vectors
		literals.add(newLit);

		//make sure v has the number of times it appears positive or negative correct
		if(vSign) {
			v.incTimesPositive();
		} else {
			v.incTimesNegative();
		}
	}	

	/**
	 * @param v the variable you're interested in
	 * @return the literal in this clause that has the passed variable in it.
	 * If no such literal exists, return null.
	 */
	public Literal getLiteral(Variable v) {
		for(Literal l : literals) {
			if(l.getVariable().equals(v))
				return l;
		}
		return null;
	}
	
	/**
	 * @return the vector of all literals in this clause.
	 */
	public Vector<Literal> getLiterals() {
		return literals;
	}



	/**
	 * Check if this passed variable is in the clause
	 * @param v - the variable to check
	 * @return if v is in this clause
	 */
	public boolean variableInClause(Variable v) {
		return getLiteral(v) != null;
	}


	/**
	 * Called after a variable is assigned.
	 * Fixes all the statistics stored in all of this clauses variables based
	 * on the assignment.
	 * @param assignedVar
	 */
	public void variableAssigned(Variable assignedVar) {
		//see if the variable is in the clause
		if(! variableInClause(assignedVar)) return;

		//make sure the unitness stats are correct
		checkUnitness();
		
		//see if the clause is satisfied
		//we're going to do this by checking the number of satisfied literals directly
		//because we're going to need that number later
		int numSatLits = getNumSatisfiedLiterals();
		if(numSatLits == 1) {
			//make sure this variable is the satisfying variable
			//if it isn't, we don't need to do anything
			Literal assignedLit = getLiteral(assignedVar);
			if(! assignedLit.isSatisfied()) return;

			//Since this assignment satisfied the clause, we need to adjust
			//the statistics in all of the variables.
			for(Literal l : literals) {
				if(l.getSign()) {
					//the literal is positive, and the clause is SAT, 
					//so decrement the times the variable appears positive
					l.getVariable().decTimesPositive();
				} else  {
					//the literal is negative, and the clause is SAT,
					//so decrement the times the variable appears negative
					l.getVariable().decTimesNegative();
				}
			}
		}
		//otherwise, this assignment is guaranteed to not have satisfied the clause
		//Since that is the case, we don't need to adjust anything.
	}

	/** 
	 * Called after a variable is unassigned
	 * Fixes all the statistics stored in all of this clauses variables
	 * based on the old assignment.
	 */
	public void variableUnassigned(Variable unassignedVar, int oldAssignment) {
		//see if the clause is still satisfied.
		//if it is, nothing changes
		if(isSatisfied()) return;

		//make sure the variable is in this clause
		if(! variableInClause(unassignedVar)) return;

		//check if someone is unit who shouldn't be
		checkUnitness();
		
		//it is no longer satisfied, so see if it was satisfied before
		//if it was, we need to adjust the statistics in the variables
		//if it wasn't, we don't need to do anything
		Literal unassigedLit = getLiteral(unassignedVar);
		
		if(unassigedLit.getSign() && oldAssignment==Variable.FALSE) return;
		if((! unassigedLit.getSign()) && oldAssignment== Variable.TRUE) return;

		//we have established that the clause was satisfied before when the variable was assigned
		//thus, we need to adjust all the statistics in all the variables in this clause
		for(Literal l : literals) {
			if(l.getSign()) {
				//the literal appears positively
				//so we need to increment the number of times the variable appears positively.
				l.getVariable().incTimesPositive();
			} else {
				//the literal appears negatively
				//we need to increment the number of times the variable appears negatively.
				l.getVariable().incTimesNegative();
			}
		}
	}

	public void checkUnitness() {
		//if this clause is satisfied, then make sure no variable thinks it is unit in it
		if(isSatisfied()) {
			for(Literal l: literals) {
				Variable v = l.getVariable();
				//see if this variable thinks it is a unit clause in this clause
				//since this clause is SAT, it should not be thinking this
				if(v.hasUnitClause(this)) {
					v.removeUnitClause(this);
				}
			}
			return;
		}
		
		//get the number of un-assigned literals
		int numUnassignedLits = 0;
		for(Literal l : literals ) {
			if(l.getVariable().getAssignment() == Variable.UNASSIGNED)
				numUnassignedLits++;
		}
				
		//if we only have 1 unassigned literal, we have a unit clause, so let the variable know it is unit
		if(numUnassignedLits == 1) {
			for(Literal l : literals) {
				if(l.getVariable().getAssignment() == Variable.UNASSIGNED)
					l.getVariable().addUnitClause(this);
			}
			//otherwise, we don't have a unit clause - make sure all the clauses know that
			//we should never be in a situation where more than 1 variable is assigned at once
			//so we should never have the situation that a variable becomes unit in 2 different clauses
			//for 2 different reasons at once, so it is safe to reset the field
		} else {
			for(Literal l : literals) {
				Variable v = l.getVariable();
				//see if the variable thinks it is unit in this clause
				//if it does, tell the variable it is not unit
				if(v.hasUnitClause(this)){
					v.removeUnitClause(this);
				}
			}
		}
	}

	/**
	 * @return all the literals in a human readable format
	 */
	public String toString() {
		String returnString = "Clause has " + literals.size() + " literals\t";
		for(Literal l : literals) {
			returnString = returnString.concat(l + "\t");
		}
	
		return returnString;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 * Sees if 2 clauses are equal.
	 * 2 clauses are equal if they have all the same literals in the same order.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Clause))
			return false;
		Clause other = (Clause) obj;
		if (literals == null) {
			if (other.literals != null)
				return false;
		} else if (!literals.equals(other.literals))
			return false;
		return true;
	}

	/**
	 * @param clauses - a vector of clauses
	 * @return if that vector of clauses constitues a satisfied SAT formula.
	 */
	public static boolean isFormulaSAT(Vector<Clause> clauses) {
		if(SsatSolver.DEBUG) System.out.println("Seeing if formula is SAT");
		for(int i = 0; i < clauses.size(); i++) {
			Clause c = clauses.get(i);
			//try to find a clause that is not yet sat
			if(! c.isSatisfied()) {
				if(SsatSolver.DEBUG) System.out.println("Clause " + i + " is not satisfied");
				return false;
			}
		}
		//didn't find any clauses that weren't satisfied, so it is SAT
		return true;
	}

	/**
	 * @param clauses - a vector of clauses, essentially a forumla
	 * @return if the formula is UNSAT.
	 */
	public static boolean isFormulaUnSAT(Vector<Clause> clauses) {
		if(SsatSolver.DEBUG) System.out.println("Seeing if formula is UNSAT");
		for(int i = 0; i < clauses.size(); i++) {
			Clause c = clauses.get(i);
			//try to find one clause that is  UnSAT
			if(c.isUnsatisfied()) { 
				if(SsatSolver.DEBUG) System.out.println("Clause " + i + " is UNSAT");
				return true;
			}
		}
		return false;
	}

}