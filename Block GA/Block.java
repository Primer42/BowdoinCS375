import java.text.DecimalFormat;
import java.util.Vector;

/** Stores a Block.
 * A block is start index, specifying a start index in an Individual, as well as some known characters that hopefully make individuals with that substring fit.
 * A block has it's own measure of fitness, which is the average fitness of all the Individuals in the generation in which it was made that include the characters that make it up.
 * Block.java 
 * @author William Richard willster3021@gmail.com
 *
 */
public class Block {
	
	private String data; //the substring values
	private int startIndex; //the start index in an Individual that this block would start
	private double avgFitness; //this block's fitness
	private int numInd; //the number of individuals that this block appears in - allows us to update the average fitness when a new individual is found.

	/** Makes a new, empty block.
	 * @param _startIndex the index that this blocks starts at.
	 */
	public Block(int _startIndex) {
		startIndex = _startIndex;
		data = "";
		avgFitness = 0.0;
		numInd = 0;
	}
	
	/** Basically constructs a copy of the passed block.
	 * @param old
	 */
	public Block(Block old) {
		startIndex = old.startIndex;
		data = old.data;
		avgFitness = old.avgFitness;
		numInd = old.numInd;
	}
	
	/** Adds a character to this block.
	 * Also, updates the fitness of this block.
	 * The average fitness of a block is really the average fitness of the individuals that all have the characters that this block is made up of.
	 * Which is different than the individuals that have this block completely.
	 * An individual may have a character of the block, but not the whole block.
	 * This isn't technically correct, but it seems to work.
	 * Allows us to incrementally make a Block.
	 * @param newChar - the new character to add.
	 */
	public void addCharacter(ProbChar newChar) {
		avgFitness = ((avgFitness * numInd) + (newChar.getAvgFitness() * newChar.getTimesAppears())) / (numInd + newChar.getTimesAppears());
		data = data.concat(String.valueOf(newChar.getCharacter()));
		numInd += newChar.getTimesAppears();
	}

	/*********************
	 * GETTERS AND SETTERS
	 *********************/
	
	
	/**
	 * @return the characters
	 */
	public String getData() {
		return data;
	}

	/**
	 * @return the startIndex
	 */
	public int getStartIndex() {
		return startIndex;
	}
	
	public int getEndIndex() {
		return startIndex + data.length() - 1;
	}
	
	/**
	 * @return the fitness
	 */
	public double getFitness() {
		return avgFitness;
	}
	
	/** toString
	 * Just returns a string with all of the block's information in nice human readable format.
	 */
	public String toString() {
		DecimalFormat df = new DecimalFormat(".###");
		return "Starting at " + startIndex + " with fitness " + df.format(avgFitness) + "  \t'" + data + "'";
	}

	/** Tests if 2 Blocks are "equal"
	 * 2 blocks are equal if they have the same data and the same fitness.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Block))
			return false;
		Block other = (Block) obj;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		if (Double.doubleToLongBits(avgFitness) != Double
				.doubleToLongBits(other.avgFitness))
			return false;
		if (startIndex != other.startIndex)
			return false;
		return true;
	}

	/**
	 * Organizes the passed Vector of blocks into an array of Vectors.
	 * All blocks in the Vector at index i of the array of Vectors will have a start index of i.
	 * @param unorganized
	 * @param s
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Vector<Block>[] organizeBlocksByStartIndex(Vector<Block> unorganized, Solver s) {
		//set up the array of vectors
		Vector<Block> organized[] = (Vector<Block>[]) new Vector<?>[s.TARGET_STRING.length()];
		//initialize all the vectors in the array
		for(int i = 0; i < organized.length; i++) {
			organized[i] = new Vector<Block>();
		}
		
		//fill them up
		for(Block b : unorganized) {
			//add b into the last slot of the column for it's starting index
			organized[b.getStartIndex()].add(b);
		}
		
		return organized;
	}
	
	/**
	 * Organizes the passed Vector of blocks into an array of Vectors.
	 * All blocks in the Vector at index i of the array of Vectors will have an end index of i.
	 * @param unorganized
	 * @param s
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Vector<Block>[] organizeBlocksByEndIndex(Vector<Block> unorganized, Solver s) {
		//set up the array of vectors
		Vector<Block>[] organized = (Vector<Block>[]) new Vector<?>[s.TARGET_STRING.length()];
		//initialize all the vectors
		for(int i = 0; i < organized.length; i++) {
			organized[i] = new Vector<Block>();
		}
		
		for(Block b : unorganized) {
			//add b into the last slot of the column for it's end index
			organized[b.getEndIndex()].add(b);
		}
		
		return organized;	
	}

	

}
