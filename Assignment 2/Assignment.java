import java.util.Vector;

/**
 * Assignment.java 
 * @author William Richard willster3021@gmail.com
 * Stores a representation of an assignment.
 */
public class Assignment {
	
	// The assignment, as an array of booleans.
	// the 1st index of the array stores the value of the 1st variable in the ordering
	// the 0th index is a dummy index.
	private boolean[] assignment;
	// The probability of the assignment succeeding.
	private Double probability;
	
	/**
	 * Constructor.
	 * @param variables - the variables to use to make an assignment
	 * @param prob - the probability that it succeeds.
	 */
	public Assignment(Vector<Variable> variables, double prob) {
		//make sure the probability is legal
		assert prob <= 1.0 && prob >= 0.0;
		
		//make the array
		assignment = new boolean[variables.size()];
		//fill the array
		for(int i = 1; i < variables.size(); i++) {
			//assume assignments are true unless explicitly set to false
			assignment[i] = ! (variables.get(i).getAssignment() == Variable.FALSE);
		}

		//store the probability.
		probability = prob;
	}

	/**
	 * @return the assignment array
	 */
	public boolean[] getAssignment() {
		return assignment;
	}

	/**
	 * @return the probability
	 */
	public double getProbability() {
		return probability;
	}
	
	/**
	 * Adjusts the probability of success by multiplying the probability by
	 * the passed adjustment factor.
	 * Used when a chance variable is in the assignment to adjust the probability
	 * based on that chance variable's value.
	 * @param adjustmentFactor
	 */
	public void adjustProbability(double adjustmentFactor) {
		//make sure the probailities stay nicely rounded
		probability = probability * adjustmentFactor;
		probability = (double) Math.round(probability*1000)/1000.0;
	}
	
	/**
	 * @param assignments - a collection of Assignments
	 * @return the total probability.  This essentially gives you the probability
	 * that the passed set of assignments has of suceeding.
	 */
	public static double getTotalProbability(Vector<Assignment> assignments) {
		double total = 0.0;
		for(Assignment a : assignments)
			total += a.getProbability();
		return total;
	}
	
	/**
	 * Returns a human-readable representation of the assignment.
	 * Has the variable's number by itself it is is stored as being positive
	 * or with a negative sign (-) in front if it is stored as being negative.
	 */
	public String toString() {
		String returnString = "";
		//go to length-1, since 
		for(int i = 1; i < assignment.length; i++) {
			if(assignment[i]) {
				returnString = returnString.concat(" " + i + "\t");
			} else {
				returnString = returnString.concat("-" + i + "\t");
			}
		}
		
		returnString = returnString.concat("\t\t" + getProbability());
		
		return returnString;
	}

}
