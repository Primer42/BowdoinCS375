import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

/** Just a runnable to allow Threading when testing.
 * Basically, just a run method that solves the problem twice and writes the resulting information to a file.
 * SolverRunnable.java 
 * @author William Richard willster3021@gmail.com
 *
 */
public class SolverRunnable implements Runnable {

	String outputFileLoc;
	Solver solver;
	DecimalFormat df;

	public SolverRunnable(String _outputLoc, String _target, int _numGen, int _indPop, int _blockPop, double _mutProb, double _minCharProb) {
		outputFileLoc = _outputLoc;
		solver = new Solver(_numGen, _indPop, _blockPop, _mutProb, _minCharProb, _target);
		df = new DecimalFormat("#.###");
	}

	public void run() {
		//solve it twice, then write the information to the file twice 
		long firstStartTime = System.currentTimeMillis();
		int firstNumGenToSolve = solver.solve();
		long firstStopTime = System.currentTimeMillis();

		long firstTotalTime = firstStopTime - firstStartTime;

		long secondStartTime = System.currentTimeMillis();
		int secondNumGenToSolve = solver.solve();
		long secondStopTime = System.currentTimeMillis();

		long secondTotalTime = secondStopTime - secondStartTime;

		try {
			BufferedWriter outputFile = new BufferedWriter(new FileWriter(outputFileLoc, true));
			String outputMessage = firstTotalTime + " " + firstNumGenToSolve + " " + solver.TARGET_STRING.length() + " " + solver.INDIVIDUAL_POPULATION_SIZE + " " + solver.BLOCK_POPULATION_SIZE + " " + df.format(solver.MUTATION_PROB) + " " + df.format(solver.MIN_CHARACTER_PROB) + "\n";
			outputFile.write(outputMessage);
			outputMessage = secondTotalTime + " " + secondNumGenToSolve + " " + solver.TARGET_STRING.length() + " " + solver.INDIVIDUAL_POPULATION_SIZE + " " + solver.BLOCK_POPULATION_SIZE + " " + df.format(solver.MUTATION_PROB) + " " + df.format(solver.MIN_CHARACTER_PROB) + "\n";
			outputFile.write(outputMessage);
			outputFile.flush();
			outputFile.close();
		} catch (IOException e) {
			System.out.println("ERROR opening file: " + e);
			System.out.println("maybe thread issue?");
			System.exit(0);
		}
	}
}
