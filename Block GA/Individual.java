import java.util.Random;
import java.util.Vector;

/** An individual in the population.
 * Basically, a candidate solution. In this case, that means a String of data
 * and the Individual's fitness.
 * We also store e^fitness, for easy Boltzmann selection.
 * Individual.java 
 * @author William Richard willster3021@gmail.com
 *
 */
public class Individual {
	
	private String data;
	private int fitness = Integer.MIN_VALUE;
	private double expFitness;
	
	
	//Make an Individual randomly
	public Individual(Random r, Solver s) {
		//basically, make a random string made of characters from the alphabet of the required length
		data = "";
		for(int i = 0; i < s.TARGET_STRING.length(); i++) {
			int nextAlphaIndex = r.nextInt(Solver.ALPHABET.length());
			data += Solver.ALPHABET.charAt(nextAlphaIndex);
		}
		
		//now, determine how fit the string is
		fitness = calcFitness(s.TARGET_STRING);
		expFitness = Math.exp(fitness);
	}
	
	//Make an Individuals using the passed Blocks
	//pass it both the organized and unorganized blocks so that we can choose randomly from each
	public Individual(Vector<Block> unorganizedBlocks, Vector<Block>[] byStart, Vector<Block>[] byEnd, Random r, Solver s) {
		char[] newData = new char[s.TARGET_STRING.length()];
		//first choose a block randomly from the whole set
		Block randomBlock = unorganizedBlocks.get(r.nextInt(unorganizedBlocks.size()));
		//put the random block into the char array
		randomBlock.getData().getChars(0, randomBlock.getData().length(), newData, randomBlock.getStartIndex());
		//keep track of where we need to build from in the array
		int firstFilledIndex = randomBlock.getStartIndex();
		int lastFilledIndex = randomBlock.getEndIndex();
		
		//now, build to the end of the string, as much as we can
		//i.e. look for blocks to fill it starting at after the last filled index
		while(lastFilledIndex < s.TARGET_STRING.length()-1) {
			//we haven't finished filling in
			//see if we have a block that starts where we want it to
			if(byStart[lastFilledIndex + 1].size() > 0) {
				//we have at least one block - choose one randomly and add it to the array
				//get a new random block and starts where we want it to
				randomBlock = byStart[lastFilledIndex + 1].get(r.nextInt(byStart[lastFilledIndex+1].size()));
				//add it to the array
				randomBlock.getData().getChars(0, randomBlock.getData().length(), newData, randomBlock.getStartIndex());
				//update our what the last filled index is
				lastFilledIndex = randomBlock.getEndIndex();
			} else {
				//we don't have a block to put in
				//add a character randomly
				lastFilledIndex++;
				newData[lastFilledIndex] = Solver.ALPHABET.charAt(r.nextInt(Solver.ALPHABET.length()));
			}
		}
		
		//we have now filled to the end of the array
		//start filling from the block we randomly placed to the start of the array
		
		while(firstFilledIndex > 0) {
			//we haven't finished filling to the beginning of the array
			//see if we have a block that ends where we want it to
			//i.e. ends where our first unfillend index is
			if(byEnd[firstFilledIndex - 1].size() > 0) {
				//we have at least one block that ends where we want it to
				//randomly choose and add it to the array
				randomBlock = byEnd[firstFilledIndex - 1].get(r.nextInt(byEnd[firstFilledIndex-1].size()));
				//add it to the array
				randomBlock.getData().getChars(0, randomBlock.getData().length(), newData, randomBlock.getStartIndex());
				//update what the first filled index is
				firstFilledIndex = randomBlock.getStartIndex();
			} else {
				//we don't have a block to put in
				//add a character randomly
				firstFilledIndex--;
				newData[firstFilledIndex] = Solver.ALPHABET.charAt(r.nextInt(Solver.ALPHABET.length()));
			}
		}
		
		//we have finished filling the array
		//convert it to a string and store it
		data = new String(newData);
		
		//now, determine how fit the string is
		fitness = calcFitness(s.TARGET_STRING);
		expFitness = Math.exp(fitness);
		
		
	}
	
	/**
	 * Basically, the fitness of a String is how many characters it shares with the target String
	 * @param target: the target String
	 * @return the fitness of this Individual when compared to the passed target
	 */
	private int calcFitness(String target) {
		assert target != null;
		assert target.length() == data.length();
		
		int totalFitness = 0;
		
		for(int i = 0; i < target.length(); i++) {
			if(target.charAt(i) == data.charAt(i)) {
				totalFitness++;
			}
		}
		
		return totalFitness;
	}

	/**
	 * @return the data
	 */
	public String getData() {
		return data;
	}
	
	public char getCharAt(int index) {
		return data.charAt(index);
	}

	/**
	 * @return the fitness
	 */
	public int getFitness() {
		return fitness;
	}

	/**
	 * @return the expFitness
	 */
	public double getExpFitness() {
		return expFitness;
	}
	
	public void mutate(Random r, double mutateProb, String alphabet, String target) {
		//see if we're going to mutate each character
		char[] dataArray = data.toCharArray();
		for(int i = 0; i < dataArray.length; i++) {
			if(r.nextDouble() <= mutateProb) {
				dataArray[i] = alphabet.charAt(r.nextInt(alphabet.length()));
			}
		}
		//set data with the new chars
		data = new String(dataArray);
		
		//recalculate the fitness and exp-fitness
		fitness = calcFitness(target);
		expFitness = Math.exp(fitness);
	}
	
	
	public String toString() {
		return "'" + data + "' fit = " + getFitness();
	}
	

}
