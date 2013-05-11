/** A ProbChar stores a character, as well as the percentage of individuals of the current population that it appears in.
 * It keeps track of that percentage by keeping track of the number of times it appears and the denominator of that percentage fraction - in this case, the total number of Individuals in the population.
 * It also stores the average fitness of all the individuals in which the character appears.
 * ProbChar.java 
 * @author William Richard willster3021@gmail.com
 *
 */
public class ProbChar {
	
	private char character;
	private int timesAppears; 
	private int denotminator;
	private double avgFitness;

	/** Makes a ProbChar with the passed information.
	 * @param _character - the character of this ProbChar
	 * @param _denominator - the denominator in the percentage calculation.
	 */
	public ProbChar(char _character, int _denominator) {
		character = _character;
		timesAppears = 0;
		denotminator = _denominator;
		avgFitness = 0.0;
	}
	
	/** Makes a copy of the passed ProbChar.
	 * @param original
	 */
	public ProbChar(ProbChar original) {
		character = original.character;
		timesAppears = original.timesAppears;
		denotminator = original.denotminator;
		avgFitness = original.avgFitness;
	}

	/**
	 * @return the character
	 */
	public char getCharacter() {
		return character;
	}	
	
	public double getProbability() {
		return ((double)timesAppears)/denotminator;
	}
	
	/**
	 * @return the avgFitness
	 */
	public double getAvgFitness() {
		return avgFitness;
	}

	/**
	 * @return the timesAppears
	 */
	public int getTimesAppears() {
		return timesAppears;
	}

	/** Adds an apperance to this ProbChar.
	 * This meas the average fitness of this ProbChar and the times it appears need to be adjusted.
	 * @param fitness
	 */
	public void addAppearance(double fitness) {
		avgFitness = ((avgFitness*timesAppears) + (double)fitness) / (timesAppears + 1);
		timesAppears++;
	}
	
	/** Resets this ProbChar so that it doesn't have any useful information in it.
	 */
	public void reset() {
		timesAppears = 0;
		avgFitness = 0.0;
	}
	
	/** Gets the probability of a character from an array of ProbChars.
	 * Assumes that the array of ProbChars has only one occurance of each character in a ProbChar.
	 * @param array
	 * @param c
	 * @return - the ProbChar that has the passed character, or null if it cannot be found.
	 */
	public static ProbChar getProbCharFromArrar(ProbChar[] array, char c) {
		for(ProbChar f : array) {
			if(c == f.getCharacter()) {
				return f;
			}
		}
		return null;
	}
	
	/** Sees if this ProbChar holds a wild char
	 * @return if this ProbChar has a Wild char.
	 */
	public boolean isWild() {
		return character == Solver.WILD_CHAR;
	}
	
	public String toString() {
		return character + ":" + getProbability() + " - " + getAvgFitness();
	}
	
	
}
