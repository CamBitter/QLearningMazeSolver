import java.util.ArrayList;
import java.io.FileNotFoundException; 
import java.util.*;
import java.io.*;

/*

Please email me with any questions -- I really wanted 
to get the emoji maze to work so that's what makes the code so complex

*/


class QLearning {

	private final double a = 0.1;   // learning rate
	private final double y = 0.9;   // discount factor
	private final double e = 0.3;   // exploration rate
	private final double p = 0.05;   // move penalty

	private final int reward = 100;
	private final int mistake = -10;
	private final int generations = 1000;

	private int numStates;
	private int mapWidth;
	private int mapHeight;

	private ArrayList<Integer> validStartingState;
	private int winState;
	private int failState;
	private int startState;

	private char[] map;
	private int[][] RMatrix;
	private double[][] QMatrix;

	private final String MapFileName = "map.txt";
	private String stateStringPrinted;


	/*-—-----------------------State------------*                        
	|   				 0 1 2 3 4 5 6 7 8      |               
	|        left   (0)                         |              *-------*-------*
	| Action right  (1)                         |              | 0 1 2 | 0 0 0 |
	|        up     (2)                         |              | 3 4 5 | 0 0 0 |
	|        down   (3)                         |              | 6 7 8 | X 0 F |
	*-------------------------------------------*              *-------*-------*          */


	public static void main(String[] args) {
		QLearning q = new QLearning();
		q.run();
		q.export();
	}


	public QLearning() {

		validStartingState = new ArrayList<Integer>();
		startState = -1;
		
		readMap();
		RMatrix = new int[4][numStates];
		QMatrix = new double[4][numStates];

		initRMatrix();
		initQMatrix();
	}


	// Read map.txt into array
	private void readMap() {

		// mapstring used to calculate maze dimensions
		// stateTracker keeps track of state number

		stateStringPrinted = "";
		String mapString = "";
		int stateTracker = 0;
		mapWidth = 0;
		stateStringPrinted += "\n    States:\n";

		// open the file
		File file = new File(MapFileName);        
		try (FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader reader = new BufferedReader(isr)) {

			
            String s;
            while ((s = reader.readLine()) != null) {

            	// this top line is to copy and paste the emojis from, not a part of the maze
            	// so we ignore it
            	if (s.equals("🔪🧀⬛🐁⬜") || s.equals("")) {
            		continue;
            	}

            	// we need to convert from our emojis to a simple text string
				// this is because once we convert from String to char, all emojis
				// become the same value so we lose the ability to compare them

            	String[] sArr = extractEmojis(s);

            	String convertedString = "";
            	for (String m: sArr) {
            		if (m.equals("🧀")) {
						convertedString += "F";
					}
					else if (m.equals("🔪")) {
						convertedString += "X";
					}
					else if (m.equals("⬛")) {
						convertedString += "*";
					}
					else if (m.equals("🐁")) {
						convertedString += "S";
					}
					else {
						convertedString += "0";
					}
            	}

            	// add our string to the overall map string
				mapString += convertedString;
			
				// update width of maze to widest line
				if ((sArr.length) > mapWidth) {
					mapWidth = (sArr.length);
				}

				// printing out states and updating which state is a mistake,
				// win, etc.
				for (char c: convertedString.toCharArray()) {

					// goal
					if (c == 'F') {
						stateStringPrinted += String.format("%4s", "F");
						winState = stateTracker;
					}

					// penalty
					else if (c == 'X') {
						stateStringPrinted += String.format("%4s", "X");
						failState = stateTracker;
					}

					// wall
					else if (c == '*') {
						stateStringPrinted += String.format("%4s", "*");
					}

					else if (c == 'S') {
						stateStringPrinted += String.format("%4s", "S");
						startState = stateTracker;
						validStartingState.add(stateTracker);
					} 

					// open space
					else if (c == '0') {
						validStartingState.add(stateTracker);
						stateStringPrinted += String.format("%4d", stateTracker);
					}

					// increment state tracker
					stateTracker++;
				}
				stateStringPrinted += "\n";
			}
			reader.close();

			System.out.println("Valid starting states:" + validStartingState);

        } catch (IOException e) {
            e.printStackTrace();
        }

	
		System.out.println(stateStringPrinted);

		// calculate dimensions of maze
		mapHeight = mapString.length() / mapWidth;
		numStates = mapString.length();



		map = mapString.toCharArray();
	}


	private void initRMatrix() {

		for (int move = 0; move < RMatrix.length; move++) {
			for (int fromState = 0; fromState < RMatrix[move].length; fromState++) {

				// first index is the action, second is the state
				// 0 = left, 1 = right, 2 = up, 3 = down
				
				int toState = -1;

				// determine the state to which one would travel to
				// based on the current state and action

				switch(move) {
					case 0: // left
						toState = fromState - 1;
						break;
					case 1: // right
						toState = fromState + 1;
						break;
					case 2: // up
						toState = fromState - mapWidth;
						break;
					case 3: // down
						toState = fromState + mapWidth;
						break;
				}

				// decide whether move is valid (possible)

				// out of the bounds
				if (toState < 0 || toState >= numStates) {
					RMatrix[move][fromState] = -1;
				}

				// if it's a different line
				else if (Math.abs(fromState - toState) == 1 && fromState / mapWidth != toState / mapWidth) {
					RMatrix[move][fromState] = -1;
				}

				else {

					// get character at that state
					char c = map[toState];

					// mistake
					if (c == 'X' || c == '🟥' || c == '🔪') {
						RMatrix[move][fromState] = mistake;
					}

					// if the move leads to a reward
					else if (c == 'F' || c == '🧀' || c == '🟩') {
						RMatrix[move][fromState] = reward;
					}

					// different characters can be used for walls
					else if (c == '*' || c == '|' || c == '—' || c == '-' || c == '⬛') {
						RMatrix[move][fromState] = -1;
					}

					// regular move
					else {
						RMatrix[move][fromState] = 0;
					}
				}
			}
		}
		return;
	}


	private void initQMatrix() {

		// Initialize all values according to the weight penalty factor
		for (int i = 0; i < QMatrix.length; i++) {
			for (int j = 0; j < QMatrix[i].length; j++) {
				QMatrix[i][j] = 0;

				if (p > 0.0) {
					QMatrix[i][j] = -(10 * p);
				}
			}
		}

		//printMatrix(QMatrix);
	}

	
	public void run() {

		// this array is used to calculate the state which one would travel to
		// given an action corresponding to the index, e.g. index 0, or moving left,
		// would decrease the state by 1
		int[] moves = {-1, 1, -mapWidth, mapWidth}; 

		Random rand = new Random();

		for (int i = 0; i < generations; i++) {

			// begin at a random state

			int currentState;

			if (startState > 0) {
				currentState = startState;
			}
			else {
				currentState = rand.nextInt(numStates);
				while (!validStartingState.contains(currentState)) {
					currentState = rand.nextInt(numStates);
				}
			}
			

			// keep track of states already visited in the run
			ArrayList<Integer> alreadyVisited = new ArrayList<Integer>();

			// while not at the end state, or the mistake state, or a state already visited, continue
			while (validStartingState.contains(currentState) /*&& !alreadyVisited.contains(currentState)*/) {

//				System.out.println("------START-----");
//				printMatrix(QMatrix);

				int randSeed = rand.nextInt(100);
				int move = -1;
				double maxQ = -1;

				// e% of the time, make a completely random move
				if (100 * e > randSeed) {
					
					move = rand.nextInt(4);
					while(!isValidMove(move, currentState)) {
						move = rand.nextInt(4);
					}
				}

				// otherwise find move with highest Q-value
				else {

//					System.out.println("Finding MaxQ Value... ");
					

					// j = all possible actions from the current state

					for (int j = 0; j < 4; j++) {

						// if its a possible move
						if (isValidMove(j, currentState)) {
							
							// find the state that would be reached given action j
							int futureState = currentState + moves[j];
							double futureStateMax = -1;

							// find the max Q value for the future state
							// by looping through all actions of k from the future state

							for (int k = 0; k < 4; k++) {
								if (QMatrix[k][futureState] > futureStateMax && isValidMove(k, futureState)) {
									futureStateMax = QMatrix[k][futureState];
								}
							}

							// update maxQ if future state reached by j had highest
							if (futureStateMax > maxQ) {

								maxQ = futureStateMax;
								move = j;
							}
						}
					}

					// if maxQ is all the same just pick a random move

					if (maxQ <= 0) {
//						System.out.println("Couldn't find MaxQ, finding random move...");
						move = rand.nextInt(4);
						while(!isValidMove(move, currentState)) {
							move = rand.nextInt(4);
						}
						maxQ = QMatrix[move][currentState];
					}
				}


//				System.out.println("Current State: " + currentState + " \nMove: " + move +" \nMax Q: " + maxQ);
//				System.out.println("QValue before: " + QMatrix[move][currentState]);


				// update Q Value
				QMatrix[move][currentState] = QMatrix[move][currentState]+(a * ((RMatrix[move][currentState] + (y * maxQ) - QMatrix[move][currentState])));

//				System.out.println("QValue after: " + QMatrix[move][currentState]);

				// update state
				switch(move) {
					case 0: 
						currentState = currentState - 1;
						break;
					case 1: 
						currentState = currentState + 1;
						break;
					case 2: 
						currentState = currentState - mapWidth;
						break;
					case 3: 
						currentState = currentState + mapWidth;
						break;
				}

//				System.out.println("New state = " + currentState);
//				alreadyVisited.add(currentState);
			}



//			System.out.println("------END-----");
		}
	}


	public void export() {

		try {
			
			PrintWriter writer = new PrintWriter("QLearning_Output.txt", "UTF-8");	

			// print out map, QMatrix, and RMatrix
			writer.println(stateStringPrinted);
			writer.println(printMatrix(QMatrix));
			writer.println(printRMatrix());

			writer.println("Solution: ");
			writer.println(solutionFound());
			System.out.println(solutionFound());

			writer.close();
		} 

		catch (IOException e) {
			System.exit(1);
		}
	}


	

	// determines whether a solution to the maze has been found
	// by checking whether 2 states keeping looping to each other
	private String solutionFound() {

		// if there is no defined start point, 
		// then every run-through is a solution
		if (startState < 0) {
			
			String policy = "-- POLICY --\n";
			String[] moves = new String[] {"No move found", "Left", "Right", "Up", "Down"};

			for (int i = 0; i < numStates; i++) {
				double maxQ = -mistake * p;
				int move = 0;
				for (int j = 0; j < 4; j++) {
					if (QMatrix[j][i] > maxQ) {
						maxQ = QMatrix[j][i];
						move = j + 1;
					}
				}

				policy += "From state " + i + ", move " + moves[move] + "\n";
			}

			return policy;
		}
		
		String result = "";
		int state = startState;
		int prevState = -1;
		int prevPrevState = -1;
		int[] moves = {-1, 1, -mapWidth, mapWidth};
		
		// path holds the states along the successful path
		// pathMoves holds the moves that get to those states from the previous one

		ArrayList<Integer> path = new ArrayList<Integer>();
		ArrayList<Integer> pathMoves = new ArrayList<Integer>();

		while (state != winState) {
			
			int move = -1;
			double maxQ = -1;

			// loop through all possible actions, find maxQ and its associated move
			for (int j = 0; j < 4; j++) {
				if (QMatrix[j][state] > maxQ && isValidMove(j, state)) {
					maxQ = QMatrix[j][state];
					move = j;
				}
			}

			// just for a nicer final output
			String moveWord = "";
			switch (move) {
				case 0: 
					moveWord = "left";
					break;
				case 1: 
					moveWord = "right";
					break;
				case 2: 
					moveWord = "up";
					break;
				case 3: 
					moveWord = "down";
					break;
			}


			result += ("Move " + moveWord + " from state " + state + " to state " + (state + moves[move]) + "\n");
			

			prevPrevState = prevState;
			prevState = state;
			state += moves[move];

			path.add(state);
			pathMoves.add(move);

			// if we just went back to the x2 prev state, that means we
			// are in an infinite loop and no solution was found

			if (prevPrevState == state) {
				return "No solution found";
			}
		}

		// remove the win state from path
		path.remove(path.size()-1);
		path.add(0, startState);

		for (int i = 0; i < numStates; i++) {
			
			// add new line
			if (i % mapWidth == 0) {
				result += "\n";
			}

			if (path.contains(i) && i != startState) {

				// change emoji based on moves array
				switch (pathMoves.get(path.indexOf(i))) {
					case 0:
						result += String.format("%2s", "←");
						break;
					case 1:
						result += String.format("%2s", "→");
						break;
					case 2:
						result += String.format("%2s", "↑");
						break;
					case 3:
						result += String.format("%2s", "↓");
						break;
				}
			}
			else {

				if (map[i] == '0') {
					result += String.format("%2s", " ");
				} else {
					result += String.format("%2c", map[i]);
				}

				
			}

		}

		return result;

	}


	// check is an action / state combination is possible
	private boolean isValidMove(int action, int state) {
		if (state > numStates || state < 0) {
			return false;
		}
		return (RMatrix[action][state] != -1);
	}

	// extract the emojies from a string and return them as
	// an array, I had to do it this way as some emojies use 
	// 2 16x unicode characters so string.length for that emoji
	// would return 2 which messes everything up
	public static String[] extractEmojis(String input) {
        List<String> emojiList = new ArrayList<>();
        int offset = 0;
        while (offset < input.length()) {
            int codePoint = input.codePointAt(offset);
            int charCount = Character.charCount(codePoint);
            String emoji = input.substring(offset, offset + charCount);
            emojiList.add(emoji);
            offset += charCount;
        }
        return emojiList.toArray(new String[0]);
    }


	// print out RMatrix
	private String printRMatrix() {

		// printQMatrix takes a double array so we must convert before printing
		double[][] temp = new double[RMatrix.length][RMatrix[0].length];
		for (int i = 0; i < RMatrix.length; i++) {
			for (int j = 0; j < RMatrix[i].length; j++) {
				temp[i][j] = (double) RMatrix[i][j];
			}
		}

		return printMatrix(temp);
	}


	// Prints out Matrix
	private String printMatrix(double[][] matrix) {

		String result = "";

		// top "State" text label
		for (int i = 0; i < matrix[0].length * 3; i++) {
			result += " ";
		}
		result += "          State\n      ";


		// border between "State" and table
		for (int i = 0; i < matrix[0].length; i++) {
			result += String.format("%6d ", i);
		}

		// top line of "--"
		result += "\n      *";
		for (int i = 0; i < matrix[0].length * 7; i++) {
			result += "-";
		}
		result += "*\n";

		// the actual data
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				if (j == 0) {
					if (i == 0) {
						result += "LEFT  |";
					}
					else if (i == 1) {
						result += "RIGHT |";
					}
					else if (i == 2) {
						result += "UP    |";
					}
					else if (i == 3) {
						result += "DOWN  |";
					}
				}

				result += String.format("%6.1f ", matrix[i][j]);
				if (j == matrix[i].length - 1) {
					result += "|";
				}
			}
			result += "\n";
		}

		// bottom line
		result += "      *";
		for (int i = 0; i < matrix[0].length * 7; i++) {
			result += "-";
		}
		result += "*\n";

		if (numStates < 26) System.out.println(result);
		return result;
	}





}