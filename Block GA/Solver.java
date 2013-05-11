import java.util.Random;
import java.util.Scanner;
import java.util.Vector;


public class Solver {

	public static final boolean DEBUG = false;

	public static final Random RANDOM_NUMBER_GENERATOR = new Random();
	//MAKE SURE THESE DO NOT HAVE ANY OVERLAP
	public static final String ALPHABET = "01"; //<--- NEEDS TO BE IN THIS FORMAT
	public static final char WILD_CHAR = '2';

	public final int MAX_NUM_GENERATIONS;
	public final int INDIVIDUAL_POPULATION_SIZE;
	public final int BLOCK_POPULATION_SIZE;
	public final double MUTATION_PROB;
	public final double MIN_CHARACTER_PROB;

	//The target string we're shooting for
	public final String TARGET_STRING;	

	/**
	 * Constructor that takes all values that need to be set
	 * @param numGen
	 * @param indPop
	 * @param blockPop
	 * @param mutProb
	 * @param minCharProb
	 * @param target
	 */
	public Solver(int numGen, int indPop, int blockPop, double mutProb, double minCharProb, String target) {
		MAX_NUM_GENERATIONS = numGen;
		INDIVIDUAL_POPULATION_SIZE = indPop;
		BLOCK_POPULATION_SIZE = blockPop;
		MUTATION_PROB = mutProb;
		MIN_CHARACTER_PROB = minCharProb;

		TARGET_STRING = target;

		targetStringValid();
	}

	/**
	 * Constructor that extracts values from main method argument string.
	 * @param args
	 * @param target
	 */
	public Solver(String[] args, String target) {
		//MAKE SURE THE ORDER OF THESE CHANGE LATER!!!!
		MAX_NUM_GENERATIONS = Integer.parseInt(args[0]);
		MUTATION_PROB = Double.parseDouble(args[1]);
		INDIVIDUAL_POPULATION_SIZE = Integer.parseInt(args[2]);
		BLOCK_POPULATION_SIZE = Integer.parseInt(args[3]);
		MIN_CHARACTER_PROB = Double.parseDouble(args[4]);

		TARGET_STRING = target;

		targetStringValid();
	}

	/** Make sure that the target string is valid.
	 * A target string is valid if it only contains characters in the Alphabet.
	 * And especially that it does not contain the wild character.
	 */
	public void targetStringValid() {
		//make sure our target String doesn't have any character not in our alphabet
		//and it does not have the wild character in it
		if(TARGET_STRING.indexOf(WILD_CHAR) > 0) {
			System.out.println("Target String includes our wild character - no good");
			System.exit(0);
		}

		//replace all the characters in our alphabet with "".  See if there is anything left.
		//if there is, we have characters in our target string that are not in our alphabet
		String replacedString = TARGET_STRING;
		for(int i = 0; i < ALPHABET.length(); i++) {
			replacedString = replacedString.replaceAll(String.valueOf(ALPHABET.charAt(i)), "");
		}

		if(replacedString.length() > 0) {
			System.out.println("Target String includes characters that are not in our alphabet - no good");
			System.exit(0);
		}
	}

	/** Attempts to find the target string passed.
	 * It does this by first, creating a random population of individuals.
	 * Then it goes through a certain number of generations.
	 * At each generation, it does the following:
	 * 	1) Checks if the target string has been found in the individual population
	 * 	2) Selects the fittest individuals using Boltzmann Selection
	 * 	3) Determines the probability that any character may appear at any index in the fit individuals.
	 * 	4) It then processes those character probabilities, replacing ones that appear less often than the minimum charater probably by wild cards.
	 * 	5) All non-wild characters that appear in contiguous blocks are extracted and made into Blocks.
	 * 	6) Those blocks are added to the population of blocks
	 * 	7) Duplicate blocks are removed from the block population.
	 * 	8) Blocks are selected based on fitness, again using Boltzmann selection.
	 * 	9) The blocks are then used to create the next population of individuals.
	 * 	10) The next population of individuals are then mutated, based on the mutation probability set.
	 * @return The number of generations that needed to be executed to find the target string.
	 */
	public int solve() {

		//make the array to hold our population of individuals
		Individual[] individuals = new Individual[INDIVIDUAL_POPULATION_SIZE];

		//have a vector to hold our population of blocks
		Vector<Block> blocks = new Vector<Block>(); 

		//start off with making a random initial population
		for(int i = 0; i < individuals.length; i++) {
			individuals[i] = new Individual(RANDOM_NUMBER_GENERATOR, this);
		}

		if(DEBUG) {
			System.out.println("Random starting population:");
			for(int i = 0; i < individuals.length; i++) {
				System.out.println(individuals[i]);
			}
			System.out.println();
		}


		//start going through the generations
		for(int generationNumber = 0; generationNumber < MAX_NUM_GENERATIONS; generationNumber++) {
			if(DEBUG) {
				System.out.println("********************************\nStarting generation " + generationNumber + "\n********************************\n");
			}

			//check if we have the optimal solution
			if(foundOptimalSolution(individuals)) {
				return generationNumber;
			}			

			//we already have a population to work with.
			//Select on that population
			individuals = selectIndividuals(individuals);

			if(DEBUG) {
				System.out.println("Fit Individuals");
				for(Individual i : individuals) {
					System.out.println(i);
				}
				System.out.println();
			}

			//Figure out the most probable character at each index of all of the individuals
			ProbChar[] characterProbabilites = determineCharaterProbability(individuals);

			if(DEBUG) {
				System.out.println("Probabilities of characters");
				for(int i = 0; i < characterProbabilites.length; i++) {
					System.out.println("Index " + i + ":\t" + characterProbabilites[i]);
				}
				System.out.println();
			}

			//replace characters that do not have high enough probability with wild characters
			//because we don't "know" with enough certainty that those characters create fit individuals
			characterProbabilites = processCharacterProbabilites(characterProbabilites);

			if(DEBUG) {
				System.out.println("Processed Characters");
				for(int i = 0; i < characterProbabilites.length; i++) {
					System.out.println("Index " + i + ":\t" + characterProbabilites[i]);
				}
				System.out.println();
			}

			//pull out blocks
			//blocks are contiguous groups of character that are not wild.
			Vector<Block> newBlocks = extractBlocks(characterProbabilites);

			if(DEBUG) {
				System.out.println("Extracted blocks:");
				for(Block b : newBlocks) {
					System.out.println(b);
				}
				System.out.println();
			}

			/*TODO: ADD ALL COMBINATION OF NEW BLOCKS???  
			 * Would need to change Individual constructor as well to handle wilds in the middle.
			 */

			//add our new blocks to the Vector
			blocks.addAll(newBlocks);

			//see if we have any blocks at this point
			//if we don't just make a new, random population of individuals and try again
			if(blocks.size() == 0) {
				for(int i = 0; i < INDIVIDUAL_POPULATION_SIZE; i++) {
					individuals[i] = new Individual(RANDOM_NUMBER_GENERATOR, this);
				}
				continue;
			}

			if(DEBUG) {
				System.out.println("All blocks, before selection:");
				for(Block b : blocks) {
					System.out.println(b);
				}
				System.out.println();
			}

			//remove duplicate blocks
			//otherwise, if only a few blocks are extracted in the early generations
			//they dominate the block pool in subsequent generations
			blocks = removeDuplicateBlocks(blocks);

			if(DEBUG) {
				System.out.println("Without duplicates:");
				for(Block b : blocks) {
					System.out.println(b);
				}
				System.out.println();
			}			

			//select on our blocks to keep our population at constant size
			blocks = selectBlocks(blocks);

			if(DEBUG) {
				System.out.println("Selected blocks:");
				for(Block b : blocks) {
					System.out.println(b);
				}
				System.out.println();
			}

			//organize the blocks based on their starting and ending index
			//we need to do this to make creating new individuals easy.
			Vector<Block>[] organizedByStart = Block.organizeBlocksByStartIndex(blocks, this);
			Vector<Block>[] organizedByEnd = Block.organizeBlocksByEndIndex(blocks, this);

			//build the next population of individuals
			individuals = constructNextIndividualPopulation(blocks, organizedByStart, organizedByEnd);

			if(DEBUG) {
				System.out.println("Next Generation of individuals:");
				for(Individual i : individuals) {
					System.out.println(i);
				}
				System.out.println();
			}

			//mutate the individuals for more variety
			individuals = mutateIndividuals(individuals);

			if(DEBUG) {
				System.out.println("Mutated individuals:");
				for(Individual i : individuals) {
					System.out.println(i);
				}
				System.out.println();
			}
		}//end for loop where generations go from 0 to MAX_NUM_GENERATIONS
		return MAX_NUM_GENERATIONS;
	}

	/** Test to see if we have found the Optimum solution
	 * Go through each Individual and see if it matches the target string.
	 * @param candidates
	 * @return
	 */
	public boolean foundOptimalSolution(Individual[] candidates) {
		for(Individual i : candidates) {
			if(i.getData().equals(TARGET_STRING)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Select individuals from the passed population using Boltzmann selection.
	 * @param population the population to select from
	 * @return the population of fit individuals
	 */
	public Individual[] selectIndividuals(Individual[] population) {
		//make a new array to store the fit individuals
		Individual[] fitIndividuals = new Individual[population.length];

		//first, calculate the sum of e^(the fitness)
		double fitnessSum = 0.0;
		for(Individual i : population) {
			fitnessSum += i.getExpFitness();
		}

		//Second, figure out what maximum double value we would get to select each individual
		double[] maxSelectionValue = new double[population.length];
		maxSelectionValue[0] = (population[0].getExpFitness() / fitnessSum);
		for(int i = 1; i < population.length; i++) {
			maxSelectionValue[i] = maxSelectionValue[i-1] + (population[i].getExpFitness() / fitnessSum);
		}

		//now, select some of the individuals using Boltzmann selection
		double randDouble;

		for(int i = 0; i < population.length; i++) {
			//get a random double
			randDouble = RANDOM_NUMBER_GENERATOR.nextDouble();
			//check to see if that random number is less than the first max selection value
			//if it is, put the individual in the 0th index of population into the fit individuals
			if(randDouble < maxSelectionValue[0]) {
				fitIndividuals[i] = population[0];
				continue;
			}
			//if not, go through the rest of the selection values, and
			//and find the value that is less than the max value and greater than the value before
			//in that case, we want that individual, so take it.
			for(int j = 1; j < population.length; j++) {
				if(randDouble < maxSelectionValue[j] && randDouble > maxSelectionValue[j-1]) {
					fitIndividuals[i] = population[j];
					break;
				}
			}
		}
		//all done - return the fit individuals
		return fitIndividuals;
	}

	/**
	 * Figure out what the most probable character at each index is.
	 * This should be run on a population of fit individuals.
	 * Using this method in that way will allow us to (hopefully) figure out what characters make fit individuals fit.
	 * @param fitIndividuals
	 * @return An array of ProbChars, that store a character and the probability of individuals in which that character appears.
	 */
	public ProbChar[] determineCharaterProbability(Individual[] fitIndividuals) {

		//keep track of the most probable character in each index of the individuals
		ProbChar[] mostProbableChars = new ProbChar[TARGET_STRING.length()];

		//keep track of the fitness of each character that we might have in each index
		//changes for each array
		ProbChar[] allCharProbabilities = new ProbChar[ALPHABET.length()];
		//initialize the array
		for(int i = 0; i < ALPHABET.length(); i++) {
			allCharProbabilities[i] = new ProbChar(ALPHABET.charAt(i), INDIVIDUAL_POPULATION_SIZE);
		}

		//go through each index of the individuals
		for(int indIndex = 0; indIndex < TARGET_STRING.length(); indIndex++) {
			//reset all the ProbChars in allCharProbabilities
			for(ProbChar f : allCharProbabilities) {
				f.reset();
			}

			//adjust all the the FitChars in the array based on the current index of the fit individuals
			for(Individual curInd : fitIndividuals) {
				char curChar = curInd.getCharAt(indIndex);
				ProbChar curProbChar = ProbChar.getProbCharFromArrar(allCharProbabilities, curChar);
				curProbChar.addAppearance(curInd.getFitness());
			}

			//find the ProbChar with the highest probability of appearance
			ProbChar mostProbChar = new ProbChar('a', INDIVIDUAL_POPULATION_SIZE);
			for(ProbChar f : allCharProbabilities) {
				if(f.getProbability() > mostProbChar.getProbability())
					mostProbChar = new ProbChar(f);
			}

			//store the most probable ProbChar
			mostProbableChars[indIndex] = mostProbChar;
		}

		return mostProbableChars;
	}

	/**
	 * Takes an array of ProbChars and replaces those with a probability less than the minimum probability of characters
	 * with a ProbChar storing a wild.
	 * @param unprocessedArray
	 * @return
	 */
	public ProbChar[] processCharacterProbabilites(ProbChar[] unprocessedArray) {
		//make the wild ProbChar
		ProbChar wild = new ProbChar(WILD_CHAR, INDIVIDUAL_POPULATION_SIZE);
		//make the new arary
		ProbChar[] processedArray = new ProbChar[unprocessedArray.length];
		//go through the old array, and replace characters with too low probability with the wild.
		for(int i = 0; i < unprocessedArray.length; i++) {
			if(unprocessedArray[i].getProbability() < MIN_CHARACTER_PROB) {
				processedArray[i] = wild;
			} else {
				processedArray[i] = unprocessedArray[i];
			}
		}
		return processedArray;
	}

	/** Extract Blocks from the passed ProbChar array.
	 * Blocks are considered to be segments of the array without any wilds.
	 * @param probabilities
	 * @return
	 */
	public Vector<Block> extractBlocks(ProbChar[] probabilities) {
		//make a vector to hold all the new blocks
		Vector<Block> blocks = new Vector<Block>();

		//start filling the vector
		//go through the probabilities array, looking for blocks of non wilds
		Block currentBlock = null;
		for(int index = 0; index < probabilities.length; index++) {
			if(probabilities[index].isWild()) {
				if(currentBlock == null) {
					//we're not working on a block
					//keep looking
					continue;
				} else {
					//we've just finished a block
					//add it to the vector
					blocks.add(currentBlock);
					//set the currrent block to null so that it doesn't get added to
					currentBlock = null;
				}
			} else {
				//we're not looking at a wild character
				if(currentBlock == null) {
					//we haven't started a block yet  - start one
					currentBlock = new Block(index);
					currentBlock.addCharacter(probabilities[index]);
				} else {
					//we're in the middle of constructing a block
					//add the current character to it
					currentBlock.addCharacter(probabilities[index]);
				}
			}
		}
		//if we've finished the array but haven't finished a block, finish the block and add it to the vector
		if(currentBlock != null)
			blocks.add(currentBlock);

		//all done - return the blocks
		return blocks;
	}

	/**
	 * Remove duplicate blocks from passed vector.
	 * Blocks are considered equal using the Block.equals method.
	 * @param withDups
	 * @return
	 */
	public Vector<Block> removeDuplicateBlocks(Vector<Block> withDups) {
		Vector<Block> withoutDups = new Vector<Block>();

		//see if the current block is in the new Vector.
		//if it is not, add it - if it is in the Vector, do not add it.
		for(Block b : withDups) {
			if(! withoutDups.contains(b)) {
				withoutDups.add(b);
			}
		}

		return withoutDups;
	}


	/** Select "fit" Blocks using Boltzmann Selection.
	 * @param originalPopulation
	 * @return
	 */
	public Vector<Block> selectBlocks(Vector<Block> originalPopulation) {
		Vector<Block> fitBlocks = new Vector<Block>();

		//first, calculate the sum of e^(fitness of each of the blocks)
		double fitnessSum = 0.0;
		for(Block b : originalPopulation) {
			fitnessSum += Math.exp(b.getFitness());
		}

		//second figure out what the maximum double value we would get to select each block
		double maxSelectionValue[] = new double[originalPopulation.size()];
		maxSelectionValue[0] = Math.exp(originalPopulation.get(0).getFitness()) / fitnessSum;
		for(int i = 1; i < originalPopulation.size(); i++) {
			maxSelectionValue[i] = maxSelectionValue[i-1] + (Math.exp(originalPopulation.get(i).getFitness())/fitnessSum);
		}

		//now, select sum of the blocks using Boltzman selection
		double randDouble;
		for(int i = 0; i < BLOCK_POPULATION_SIZE; i++) {
			//get a random double
			randDouble = RANDOM_NUMBER_GENERATOR.nextDouble();
			//check to see if that random number is less than the first max selection value
			//if it is, take the block in the 0th index
			if(randDouble < maxSelectionValue[0]) {
				fitBlocks.add(new Block(originalPopulation.get(0)));
				continue;
			}
			//if not, go through the rest of the selection values and find
			//the value that is less than the current value but greater than the prev value
			for(int j = 1; j < maxSelectionValue.length; j++) {
				if(randDouble < maxSelectionValue[j] && randDouble > maxSelectionValue[j-1]) {
					fitBlocks.add(new Block(originalPopulation.get(j)));
					break;
				}
			}
		}

		return fitBlocks;
	}		

	/** Make the next population of individuals
	 * Do this using blocks, and the Individual constructor that takes the correct Vectors/Arrays
	 * @param unorganizedBlocks All the blocks in any order
	 * @param byStart The blocks in an array of Vectors, where each array index is the start index of the blocks in that array index's Vector.
	 * @param byEnd The blocks in an array of Vectors, where each array index is the end index of the blocks in that array index's Vector.
	 * @return the next population of Individuals
	 */
	public Individual[] constructNextIndividualPopulation(Vector<Block> unorganizedBlocks, Vector<Block>[] byStart, Vector<Block>[] byEnd) {
		//make the array we will return
		Individual[] newIndividuals = new Individual[INDIVIDUAL_POPULATION_SIZE];
		//fill it up
		for(int i = 0; i < newIndividuals.length; i++) {
			newIndividuals[i] = new Individual(unorganizedBlocks, byStart, byEnd, RANDOM_NUMBER_GENERATOR, this);
		}
		//return the array
		return newIndividuals;
	}

	/** Mutate Individuals.
	 * Go through each index of each Individual and ask it to mutate.
	 * @param individuals
	 * @return
	 */
	public Individual[] mutateIndividuals(Individual[] individuals) {
		for(int i = 0; i < individuals.length; i++) {
			Individual ind = individuals[i];
			ind.mutate(RANDOM_NUMBER_GENERATOR, MUTATION_PROB, ALPHABET, TARGET_STRING);
		}
		return individuals;

	}

	public static void main(String[] args) {
		//		//FOR DEBUGGING
		//		Solver debugSolver = new Solver();
		//		int numGenSolve = debugSolver.solve();
		//		System.out.println("Solved in " + numGenSolve + " generations");


		//FOR TESTING
		//		runTests();


		//make sure they pass the correct number of arguments
		if(args.length != 5) {
			System.out.println("Incorrect Usage.  Correct Usage:");
			System.out.println("Solver <max number of generations to run> <mutation probability> <individual population size> <block population size> <minimum character probability>");
			System.exit(0);
		}
		//ask for the solution string they want to work off of
		System.out.print("Target String: ");
		Scanner scan = new Scanner(System.in);
		String target = scan.nextLine();

		Solver ourSolver = new Solver(args, target);
		long startTime = System.currentTimeMillis();
		int numGenToSolve = ourSolver.solve();
		long stopTime = System.currentTimeMillis();
		System.out.println("Found optimum solution in " + (stopTime - startTime)/1000.0 + " seconds. Needed " + numGenToSolve + " generations to solve the problem.");
	}

	public static void runTests() {

		String filename = "./data/";

		for(int targetLength = 10; targetLength <= 50; targetLength += 10) {
			//make the target string
			String target = "";
			for(int i = 0; i < targetLength; i++) {
				target += "1";
			}
			for(int numGen = 20; numGen <= 1000; numGen += 20) {
				for(int indPop = 5; indPop <= 100; indPop += 5) {
					for(int blockPop = 5; blockPop <= 100; blockPop += 5) {
						for(double mutProb = .1; mutProb < .5; mutProb += .05) {
							for(double minCharProb = .5; minCharProb <= 1.0; minCharProb += .05) {
								//got all our parameters set away
								//want to run each set of parameters a few times
								//let's do 3 threads, and have each thread run it twice for 6 times total
								SolverRunnable runnable = new SolverRunnable(filename + targetLength+".txt", target, numGen, indPop, blockPop, mutProb, minCharProb);

								(new Thread(runnable)).start();
								(new Thread(runnable)).start();
								(new Thread(runnable)).start();
							}
						}
					}
				}
			}
		}
	}
}