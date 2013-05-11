import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Vector;

/**
 * SsatSolver.java 
 * @author William Richard willster3021@gmail.com
 * Solves a SSAT problem.
 */
public class SsatSolver {

	//Stores the variables that are in this problem
	private static Vector<Variable> variables;
	//stores the clauses that are in this problem
	private static Vector<Clause> clauses;

	//a debug flag.  If set, it will print out debug information.
	public static final boolean DEBUG = false;


	/**
	 * @param args
	 */
	public static void main(String[] args) {		
		//make sure we're passed the correct arguments
		if(args.length != 1) { 
			System.out.println("Incorrect number of arguments - given " + args.length);
			System.out.println("Correct usage: java SsatSolver <ssat file>");
			System.out.print("Given : '");
			for(String s : args) System.out.print(s + " ");
			System.out.println("'");
			System.exit(0);
		}

		//start timing
		long startTime = System.currentTimeMillis();

		//read the SSAT formula
		readFormula(args[0]);

		//check if we start out with any unit clauses
		for(Clause c : clauses) {
			c.checkUnitness();
		}

		if(DEBUG) {
			//print out the clauses for debug purposes
			System.out.println("Read in the formula with " + clauses.size() + " clauses:");
			for(Clause c : clauses) {
				System.out.println(c);
			}
			System.out.println("");

			//print out the variables in order
			for(int i = 1; i < variables.size(); i++) {
				System.out.println(variables.get(i).getName() + "\t" + variables.get(i).getChanceTrue());
			}
		}

		//solve the formula
		Vector<Assignment> satAssignments = DPLL();

		//stop timing
		long stopTime = System.currentTimeMillis();

		//calculate and print out time taken
		double totalTime = (stopTime - startTime) / 1000.0;
		System.out.println("Time Taken = " + totalTime + " seconds.");

		//print out the assignment.
		printAssignments(satAssignments);
		//print out the total probablily of this plan, or if we don't have satisfaction
		double overallProb = Assignment.getTotalProbability(satAssignments);
		if(overallProb == 0.0) {
			System.out.println("No Satisfaction :-(");
		} else {
			System.out.println("Success Probability = " + overallProb);
		}

	}


	public static void readFormula(String fileLocation) {
		//open up the file
		Scanner reader = null;
		try {
			reader = new Scanner(new File(fileLocation));
		} catch (FileNotFoundException e) {
			System.out.println("File not found: '" + fileLocation + "'");
			System.exit(1);
		}

		assert reader != null;

		//read in the comments and problem lines
		//once we have read in the problem line, go onto the clauses
		while(reader.hasNextLine()) {
			String nextLine = reader.nextLine();

			if(nextLine.substring(0, 1).equals("c")) {
				//just print out the comment lines
				System.out.println(nextLine.substring(1));
			} else if(nextLine.substring(0,1).equals("p")) {
				//read in the problem line
				//should look like this
				//"p cnf <num variables> <num clauses>"

				//set up a scanner to read the line
				Scanner problemScanner = new Scanner(nextLine);

				//advance the scanner to the ints
				while(!problemScanner.hasNextInt()) problemScanner.next();

				//read in the number of variables and set up the variables Vector
				int numVars = problemScanner.nextInt();
				variables = new Vector<Variable>(numVars+1);
				//so we don't have off by one errors, put a null in the 0th index
				for(int i = 0; i <= numVars; i++) {
					Variable nextVar = new Variable(i);
					variables.add(i, nextVar);
					if(DEBUG) {
						System.out.println("Added variable " + variables.get(i).getName());
					}
				}
				variables.set(0, null);

				//get the number of clauses, and use it to set up the clauses Vector
				int numClauses = problemScanner.nextInt();
				clauses = new Vector<Clause>(numClauses);

				if(DEBUG) System.out.println("read that we have " + numVars + " variables and " + numClauses + " clauses.");

				//done reading in the problem line, so clauses should come next
				//so break out of the while loop
				break;
			} else {
				System.out.println("Error: got unexpected line in file. '" + nextLine + "'");
				System.exit(2);
			}
		}		

		//start reading in clauses
		for(int i = 0; i < clauses.capacity(); i++) {
			Clause nextClause = new Clause();
			int nextVarNum = reader.nextInt();
			Variable nextVar;
			while(nextVarNum != 0) {
				if(DEBUG) {
					System.out.println("read variable " + nextVarNum + " in clause " + i);
				}

				nextVar = variables.get(Math.abs(nextVarNum));

				if(DEBUG) {
					System.out.println("Got variable " + nextVar.getName() + " from array");
				}


				nextClause.addVariable(nextVar, nextVarNum > 0);

				nextVarNum = reader.nextInt();
				if(DEBUG) System.out.println("\n");
			}

			clauses.add(nextClause);
		}

		//done reading in clauses - read in the variable probabilities

		//get to the next int
		while(! reader.hasNextInt()) reader.next();

		//start reading in the variable information.
		for(int i = 1; i < variables.size(); i++) {
			int variableName = reader.nextInt();
			double variableValue = reader.nextDouble();

			if(DEBUG) 
				System.out.println("Read in variable " + variableName + " with value " + variableValue);

			variables.get(variableName).setValue(variableValue);	
		}

		if(DEBUG) {
			System.out.println("");
			for(int i = 1; i < variables.size(); i++){
				Variable v = variables.get(i);
				System.out.println("Variable " + v.getName() + " is a choice variable? " + v.isChoice());
				System.out.println("Variable " + v.getName() + " is a chance variable? " + v.isChance() + "\n");
			}
		}

	}


	/**
	 * Solves the current configuration of vaiables and clauses in the static Vectors.
	 * Does so using a variation of DPLL.
	 * First, it looks for unit clauses, and assigns them if possible using tryAssign.
	 * Then, it tries to find and assign choice pure variable.
	 * If it can't do that, it tries both assignments for the next unassigned variable
	 * and depending on if it is a chance or choice variable, returns the either the
	 * best plan it can find, or both plans.
	 * 
	 * @return a Vector of assignments in the plan that it finds.
	 */
	public static Vector<Assignment> DPLL() {

		if(DEBUG) System.out.println("\nStarting DPLL()"); 

		//see if the formula is satisfied
		if(Clause.isFormulaSAT(clauses)) {
			if(DEBUG) System.out.println("Formula is SAT - returning the assignment");
			//make sure we have assigned all the chance variables, so we know the probability of each possible plan
			return assignNextChance();
		}

		//see if the formula is un-satisfied
		if(Clause.isFormulaUnSAT(clauses)) {
			if(DEBUG) System.out.println("Formula is UNSAT - returning the assignment");
			//make sure we have assigned all the chance variables, so we know the probability of each possible plan
			return assignNextChance();
		}

		//try to find a variable to assign.

		//first, look for a unit clause
		for(int i = 1; i < variables.size(); i++) {
			Variable v = variables.get(i);

			//only consider variables that are unassigned
			if(v.getAssignment() == Variable.UNASSIGNED) {

				//see if it is unit
				if(v.isUnit()) {
					if(SsatSolver.DEBUG) System.out.println("Variable " + v.getName() + " is unit");
					Clause unitClause = v.getFirstUnitClause();
					assert unitClause != null;
					Literal unitLiteral = unitClause.getLiteral(v);

					//try the assignment, based on if it appears positively or negatively unit.
					//return different vectors of assignments if it is choice or chance.
					Vector<Assignment> unitAssignments;
					if(unitLiteral.getSign()) {
						if(SsatSolver.DEBUG) System.out.println("Assigning var " + v.getName() + " to true");
						unitAssignments = tryAssign(unitLiteral.getVariable(), Variable.TRUE);
						//depending on if v is choice or chance, we may need to adjust things
						if(v.isChoice()) {
							//v is choice, so we don't need to adjust the probability of success.
							return unitAssignments;
						} else {
							//v is chance, so we need to adjust the probability of success for all
							//the assignments we got back.
							assert v.isChance();
							for(Assignment a : unitAssignments) {
								a.adjustProbability(v.getChanceTrue());
							}
							return unitAssignments;
						}
						//Do the same, but with assigning the variable to FALSE
					} else {
						if(SsatSolver.DEBUG) System.out.println("Assigning var " + v.getName() + " to false");
						unitAssignments = tryAssign(unitLiteral.getVariable(), Variable.FALSE);
						//depending on if v is choice or chance, we may need to adjust things
						if(v.isChoice()) {
							return unitAssignments;
						} else {
							assert v.isChance();
							for(Assignment a : unitAssignments) {
								a.adjustProbability(v.getChanceFalse());
							}
							return unitAssignments;
						}
					}
				}
			}
		}

		//Next, look for a pure variable
		for(int i = 1; i < variables.size(); i++) {
			Variable v = variables.get(i);
			//only consider variables that are choice variables and unassigned
			if(v.isChoice() && v.getAssignment() == Variable.UNASSIGNED) {
				//purity will be positive if v only appears positively,
				//negative if v only appears negatively, or 0 otherwise.
				int purity = v.isPure();
				if(purity != 0) {
					if(SsatSolver.DEBUG) System.out.println("Variable " + v.getName() + " is pure");
					//we have a pure variable
					//assign it accordingly
					Vector<Assignment> pureAssignments;
					if(purity > 0) {
						if(SsatSolver.DEBUG) System.out.println("Assigning it true");
						pureAssignments = tryAssign(v, Variable.TRUE);
						return pureAssignments;
					}
					else { 
						if(SsatSolver.DEBUG) System.out.println("Assigning it false");
						pureAssignments = tryAssign(v, Variable.FALSE);
						return pureAssignments;
					}
				}
			}
		}

		//We weren't able to find a pure or unit variable, so go to the next unassigned variable
		Variable nextAssignee = null;
		for(int i = 1; i < variables.size(); i++) {
			Variable v = variables.get(i);
			if(v.getAssignment() == Variable.UNASSIGNED) {
				nextAssignee = v;
				break;
			}
		}

		assert nextAssignee != null;

		if(SsatSolver.DEBUG) System.out.println("No pure or unit variables - trying to assign variable " + nextAssignee.getName());

		//try assigning the variable to true and to false
		if(SsatSolver.DEBUG) System.out.println("Trying variable " + nextAssignee.getName() + " assigned to TRUE");
		Vector<Assignment> trueAssignments = tryAssign(nextAssignee, Variable.TRUE);
		if(SsatSolver.DEBUG) System.out.println("Trying variable " + nextAssignee.getName() + " assigned to FALSE");
		Vector<Assignment> falseAssignments = tryAssign(nextAssignee, Variable.FALSE); 

		//depending on if the variable is chance or choice, do different things
		if(nextAssignee.isChoice()) {
			//for a choice variable, return the set of assignments with the higher probability of sucess
			//since we get to choose the assignment of choice variables to maximize success.
			if(Assignment.getTotalProbability(trueAssignments) >= Assignment.getTotalProbability(falseAssignments))
				return trueAssignments;
			else 
				return falseAssignments;
		} else {
			//it is a chance variable
			assert nextAssignee.isChance();

			//adjust the probabilities of the true assignments by the chance that the variable is true
			//and adjust the probabilities of the false assignmetns by the chance that the varible is false.
			for(Assignment a : trueAssignments) {
				a.adjustProbability(nextAssignee.getChanceTrue());
			}

			for(Assignment a : falseAssignments) {
				a.adjustProbability(nextAssignee.getChanceFalse());
			}

			//now, return a Vector containing the false and true assignments.
			Vector<Assignment> allAssignmetns = new Vector<Assignment>(trueAssignments.size() + falseAssignments.size());
			allAssignmetns.addAll(trueAssignments);
			allAssignmetns.addAll(falseAssignments);

			return allAssignmetns;
		}
	}

	/**
	 * When we find that the formula is SAT or UNSAT, we still need to assign all the chance variables
	 * so that we know the probability of success for each possible assignment.
	 * So, when that situation comes up, this method is called to makes sure we have 
	 * calculated all of those assignments.
	 * @return The assignments with all the chance variables assigned and probabilities calculated.
	 */
	public static Vector<Assignment> assignNextChance() {
		if(DEBUG) System.out.println("Making sure we have assigned all chance variables");
		//get the next chance variable to assign
		Variable nextChance = null;
		for(int i = 1; i < variables.size(); i++) {
			Variable v = variables.get(i);
			if(v.isChance() && v.getAssignment() == Variable.UNASSIGNED) {
				nextChance = v;
				break;
			}
		}

		//if we cannot find a chance variable to assign, so they have all been assign.
		//return the current assignment
		if(nextChance == null) {
			if(Clause.isFormulaSAT(clauses)) {		
				//make an Assignment, and return it with a probably 1, since we are SAT
				Vector<Assignment> SATAssignment = new Vector<Assignment>();
				SATAssignment.add(new Assignment(variables, 1.0));
				return SATAssignment;
			} else if(Clause.isFormulaUnSAT(clauses)) {
				//make an assignment with probability 0.0 (since we are UNSAT)
				//and return it.
				Vector<Assignment> UNSATAssignment = new Vector<Assignment>();
				UNSATAssignment.add(new Assignment(variables, 0.0));
				return UNSATAssignment;
			} else {
				//we are neither SAT or UNSAT, yet there are no more chance variables to assign - this shouldn't happen
				//FREAK OUT!!!!
				System.out.println("Tried to assign all the chance variables after finding out we were SAT/UNSAT, but now we are not SAT or UNSAT!!!!");
				printFormulaInfo();
				System.exit(5);
			}
		}

		//we have found a chance variable to assign
		//assign it to true and false, adjust the resulting probabilities based on the variable's 
		//chance that it is true or false
		//and return all the resulting assignments
		Vector<Assignment> trueAssignments = tryAssign(nextChance, Variable.TRUE);
		for(Assignment a : trueAssignments)
			a.adjustProbability(nextChance.getChanceTrue());
		Vector<Assignment> falseAssignmetns = tryAssign(nextChance, Variable.FALSE);
		for(Assignment a : falseAssignmetns)
			a.adjustProbability(nextChance.getChanceFalse());

		//return all of the assignments if it is true and false.
		Vector<Assignment> allAssignments = new Vector<Assignment>();
		allAssignments.addAll(trueAssignments);
		allAssignments.addAll(falseAssignmetns);
		return allAssignments;
	}

	/**
	 * Tries to assigned the passed variable to the passed assignment,
	 * gets the resulting assignments, and then unassign the variable
	 * @param assignedVar - the variable to assign
	 * @param varSign - how we should try assigning it
	 * @return the assignments we get back from assigning the variable as requested.
	 */
	public static Vector<Assignment> tryAssign(Variable assignedVar, int varSign) {

		if(SsatSolver.DEBUG) System.out.println("Assigning variable " + assignedVar.getName() + " to " + (varSign==Variable.TRUE?"True":"False"));
		//assign the variable
		assignedVar.setAssignment(varSign);
		//make sure all the clauses update the stats of the variables
		for(Clause c : clauses) {
			c.variableAssigned(assignedVar);
		}

		//if we're debugging, make sure everything is sane in the stored informaiton about variables.
		if(DEBUG) checkVariableStats();

		//solve the formula with the assigned variable
		//hold onto the assignments we get back
		Vector<Assignment> assignments = DPLL();

		if(SsatSolver.DEBUG) System.out.println("When assiging variable " + assignedVar.getName() + " to " + (varSign==Variable.TRUE?"True":"False") + " got " + assignments.size() + " possible assignments back.");

		if(SsatSolver.DEBUG) System.out.println("Unassigning variable " + assignedVar.getName());

		//unassign the variable
		assignedVar.setAssignment(Variable.UNASSIGNED);
		//make sure the clauses update the stats of the variables
		for(Clause c : clauses) {
			c.variableUnassigned(assignedVar, varSign);
		}

		if(DEBUG) checkVariableStats();

		//return the assignments we got back
		return assignments;
	}

	/**
	 * Prints out all of the passed assignments
	 * @param assignments - the assignments to print out.
	 */
	public static void printAssignments(Vector<Assignment> assignments) {
		System.out.println("Assignments with non-zero chance of sucess:");
		for(Assignment a : assignments) {
			System.out.println(a);
		}
		System.out.println("");
	}

	/** 
	 * Print the information about the current formula.
	 * Only really used for debugging.
	 */
	public static void printFormulaInfo() {
		System.out.println("*********************************\nVariable Information");

		//print out the variables
		for(int i = 1; i < variables.size(); i++) {
			Variable v = variables.get(i);
			System.out.println(v.getName() + 
					"\tAssignment:" + (v.getAssignment()==Variable.UNASSIGNED?"Unassign":(v.getAssignment()==Variable.TRUE?"True    ":"False   ")) +
					"\tTimes pos: " + v.getTimesPositive() + 
					"\tTimes neg: " + v.getTimesNegative() +
					"\tUnit clauses: '" + v.getUnitClauses().toString() + "'");
		}
		//print out the clauses
		System.out.println("\nLiterals in Clauses: < # > means variable '#' has been assigned");
		for(int i = 0; i < clauses.size(); i++) {
			Clause c = clauses.get(i);
			System.out.println("Clause " + i + " is " + (c.isSatisfied()?"Satisfied":"Unsatisfied(yet)"));
			for(Literal l : c.getLiterals()) {
				if(l.getVariable().getAssignment() == Variable.UNASSIGNED)
					System.out.print(l + " ");
				else
					System.out.print("<" + l + "> ");
			}
			System.out.println("");
		}
		System.out.println("");
	}

	/**
	 * Check all the statistics for all the variables and make sure they match
	 * what is true in the formula.
	 * 
	 * Only used for debugging.
	 */
	public static void checkVariableStats() {
		System.out.println("Checking variable stats");
		for(int i = 1; i < variables.size(); i++) {
			//gather information about the variable
			int timesPos = 0;
			int timesNeg = 0;
			boolean isUnit = false;
			Variable v = variables.get(i);
			for(Clause c : clauses) {
				if(c.isSatisfied()) continue;

				Literal l = c.getLiteral(v);
				if(l == null) continue;

				if(l.getSign()) timesPos++;
				else timesNeg++;

				//count up the number of unassigned literals
				int numUnassignedLits = 0;
				for(Literal lit : c.getLiterals()) {
					if(lit.getVariable().getAssignment() == Variable.UNASSIGNED)
						numUnassignedLits++;
				}

				if(numUnassignedLits == 1 && v.getAssignment() == Variable.UNASSIGNED) 
					isUnit = true;
			}
			//make sure they match what the variable knows
			if(v.getTimesPositive() != timesPos || v.getTimesNegative() != timesNeg) {
				System.out.println("Variable " + v.getName() + " has wrong times pos/neg stats");
				printFormulaInfo();
				System.exit(4);
			} else if(isUnit && v.getUnitClauses().size() == 0) {
				System.out.println("Variable " + v.getName() + " thinks it is not unit, but is actually unit");
				printFormulaInfo();
				System.exit(4);
			} else if(!isUnit && v.getUnitClauses().size() > 0) {
				System.out.println("Variblae " + v.getName() + " thinks it is unit, but actually is not unit");
				printFormulaInfo();
				System.exit(4);
			}
			//check purity
			if(v.isPure() == 0) {
				//thinks it is not pure
				if(timesPos == 0 || timesNeg == 0) {
					System.out.println("Variable " + v.getName() + " thinks it is not pure, but actually is");
					printFormulaInfo();
					System.exit(4);
				}
			} else {
				//thinks it is pure
				if(timesPos > 0 && timesNeg > 0) {
					System.out.println("Variable " + v.getName() + " thinks it is pure, but actually is not");
					printFormulaInfo();
					System.exit(4);
				}
			}
		}
	}

}
