package net.mathdoku.plus.grid;

import net.mathdoku.plus.storage.database.SolvingAttemptDatabaseAdapter;

import java.util.ArrayList;
import java.util.Arrays;

public class GridCage {
	// Each line in the GridFile which contains information about the cell
	// starts with an identifier. This identifier consists of a generic part and
	// the package revision number.
	private static final String SAVE_GAME_CAGE_LINE = "CAGE";

	public static final int ACTION_NONE = 0;
	public static final int ACTION_ADD = 1;
	public static final int ACTION_SUBTRACT = 2;
	public static final int ACTION_MULTIPLY = 3;
	public static final int ACTION_DIVIDE = 4;

	// Action for the cage
	public int mAction;

	// Number the action results in
	public int mResult;
	// Flag to indicate whether operator (+,-,x,/) is hidden.
	private boolean mHideOperator;

	// List of cage's cells
	public ArrayList<GridCell> mCells;
	// Id of the cage
	public int mId;
	// Enclosing context
	private Grid mGrid;

	// User math is correct
	public boolean mUserMathCorrect;

	// Cached list of numbers which satisfy the cage's arithmetic
	private ArrayList<int[]> mPossibles;

	/**
	 * Creates a new instance of {@link GridCage}.
	 * 
	 * @param grid
	 *            The grid in which this cage is defined.
	 */
	public GridCage(Grid grid) {
		initGridCage(grid);
	}

	/**
	 * Creates a new instance of {@link GridCage}.
	 * 
	 * @param grid
	 *            The grid in which the cage is defined.
	 * @param hideOperator
	 *            True in case the grid can be solved without using the
	 *            operators. False otherwise.
	 */
	public GridCage(Grid grid, boolean hideOperator) {
		initGridCage(grid);
		mHideOperator = hideOperator;
	}

	/**
	 * Initializes the cage variables.
	 * 
	 * @param grid
	 *            The grid in which this cage is defined.
	 */
	private void initGridCage(Grid grid) {
		this.mGrid = grid;
		mPossibles = null;
		mCells = new ArrayList<GridCell>();

		// Defaulting mUserMathCorrect to false result in setting all borders
		// when checking the cage math for the first time.
		mUserMathCorrect = false;
	}

	@Override
	public String toString() {
		String retStr = "";
		retStr += "Cage id: " + this.mId + ", Size: "
				+ (this.mCells == null ? 0 : this.mCells.size());
		retStr += ", Action: ";
		switch (this.mAction) {
		case ACTION_NONE:
			retStr += "None";
			break;
		case ACTION_ADD:
			retStr += "Add";
			break;
		case ACTION_SUBTRACT:
			retStr += "Subtract";
			break;
		case ACTION_MULTIPLY:
			retStr += "Multiply";
			break;
		case ACTION_DIVIDE:
			retStr += "Divide";
			break;
		}
		retStr += ", Result: " + this.mResult;
		retStr += ", cells: ";
		if (mCells != null) {
			for (GridCell cell : mCells)
				retStr += cell.getCellId() + ", ";
		}
		return retStr;
	}

	public boolean isOperatorHidden() {
		return mHideOperator;
	}

	public void revealOperator() {
		mHideOperator = false;
		setCageResults(mResult, mAction, false);
	}

	/**
	 * Set the result and operator for this cage.
	 * 
	 * @param resultValue
	 *            The resulting value of the cage when applying the given action
	 *            on the cell values in the cage.
	 * @param action
	 *            The action to be applied on the cell values in this cage.
	 * @param hideOperator
	 *            True in case the operator of this cage can be hidden but the
	 *            puzzle can still be solved.
	 */
	public void setCageResults(int resultValue, int action, boolean hideOperator) {
		// Store results in cage object
		mResult = resultValue;
		mAction = action;
		String operator = "";
		switch (mAction) {
		case ACTION_NONE:
			operator = "";
			break;
		case ACTION_ADD:
			operator = "+";
			break;
		case ACTION_SUBTRACT:
			operator = "-";
			break;
		case ACTION_MULTIPLY:
			operator = "x";
			break;
		case ACTION_DIVIDE:
			operator = "/";
			break;
		}
		mHideOperator = hideOperator;

		// Store cage outcome in top left cell of cage
		mCells.get(0).setCageText(mResult + (mHideOperator ? "" : operator));
	}

	/**
	 * Clears the cage result form the cage and the top left cell in the cage.
	 */
	public void clearCageResult() {
		mResult = 0;
		mAction = ACTION_NONE;

		// Remove outcome from top left cell of cage
		mCells.get(0).setCageText("");
	}

	/*
	 * Sets the cageId of the cage's cells.
	 */
	public void setCageId(int id) {
		this.mId = id;
		for (GridCell cell : this.mCells)
			cell.setCageId(this.mId);
	}

	boolean isAddMathsCorrect() {
		int total = 0;
		for (GridCell cell : this.mCells) {
			total += cell.getUserValue();
		}
		return (total == this.mResult);
	}

	boolean isMultiplyMathsCorrect() {
		int total = 1;
		for (GridCell cell : this.mCells) {
			total *= cell.getUserValue();
		}
		return (total == this.mResult);
	}

	boolean isDivideMathsCorrect() {
		if (this.mCells.size() != 2)
			return false;

		if (this.mCells.get(0).getUserValue() > this.mCells.get(1)
				.getUserValue())
			return this.mCells.get(0).getUserValue() == (this.mCells.get(1)
					.getUserValue() * this.mResult);
		else
			return this.mCells.get(1).getUserValue() == (this.mCells.get(0)
					.getUserValue() * this.mResult);
	}

	boolean isSubtractMathsCorrect() {
		if (this.mCells.size() != 2)
			return false;

		if (this.mCells.get(0).getUserValue() > this.mCells.get(1)
				.getUserValue())
			return (this.mCells.get(0).getUserValue() - this.mCells.get(1)
					.getUserValue()) == this.mResult;
		else
			return (this.mCells.get(1).getUserValue() - this.mCells.get(0)
					.getUserValue()) == this.mResult;
	}

	/**
	 * Checks whether the cage arithmetic is correct using the values the user
	 * has filled in. If needed the border of the cage will be updated to
	 * reflect a change of state. For single cell cages the math will never be
	 * incorrect.
	 * 
	 * @return True in case the user math does not contain an error or in case
	 *         not all cells in the cage have been filled in.
	 */
	public boolean checkUserMath() {
		// If cage has no cells, the maths are not wrong
		if (mCells == null || mCells.size() == 0) {
			return true;
		}

		boolean oldUserMathCorrect = mUserMathCorrect;
		if (allCellsFilledWithUserValue()) {
			if (this.mHideOperator) {
				mUserMathCorrect = isAddMathsCorrect()
						|| isMultiplyMathsCorrect() || isDivideMathsCorrect()
						|| isSubtractMathsCorrect();
			} else {
				switch (this.mAction) {
				case ACTION_ADD:
					mUserMathCorrect = isAddMathsCorrect();
					break;
				case ACTION_MULTIPLY:
					mUserMathCorrect = isMultiplyMathsCorrect();
					break;
				case ACTION_DIVIDE:
					mUserMathCorrect = isDivideMathsCorrect();
					break;
				case ACTION_SUBTRACT:
					mUserMathCorrect = isSubtractMathsCorrect();
					break;
				}
			}
		} else {
			// At least one cell has no user value. So math is not incorrect.
			mUserMathCorrect = true;
		}

		if (oldUserMathCorrect != mUserMathCorrect) {
			setBorders();
		}

		return mUserMathCorrect;
	}

	/**
	 * Set borders for all cells in this cage.
	 */
	public void setBorders() {
		for (GridCell cell2 : mCells) {
			cell2.setBorders();
		}
	}

	public ArrayList<int[]> getPossibleCombos() {
		if (mPossibles == null) {
			if (mHideOperator || (mAction == ACTION_NONE && mCells.size() > 1)) {
				mPossibles = setPossibleCombosHiddenOperator();
			} else {
				mPossibles = setPossibleCombosVisibleOperator();
			}
		}
		return mPossibles;
	}

	/**
	 * Get all permutations of cell values for this cage.
	 * 
	 * @return The list of all permutations of cell values which can be used for
	 *         this cage.
	 */
	private ArrayList<int[]> setPossibleCombosHiddenOperator() {
		ArrayList<int[]> resultCombos = new ArrayList<int[]>();

		// Single cell cages can only contain the value of the single cell.
		if (mCells.size() == 1) {
			int number[] = { mResult };
			resultCombos.add(number);
			return resultCombos;
		}

		// Cages of size two can contain any operation
		int gridSize = mGrid.getGridSize();
		if (mCells.size() == 2) {
			for (int i1 = 1; i1 <= gridSize; i1++) {
				for (int i2 = i1 + 1; i2 <= gridSize; i2++) {
					if (i2 - i1 == mResult || i1 - i2 == mResult
							|| mResult * i1 == i2 || mResult * i2 == i1
							|| i1 + i2 == mResult || i1 * i2 == mResult) {
						int numbers[] = { i1, i2 };
						resultCombos.add(numbers);
						numbers = new int[] { i2, i1 };
						resultCombos.add(numbers);
					}
				}
			}
			return resultCombos;
		}

		// Cages of size two and above can only contain an add or a multiply
		// operation
		resultCombos = getAllAddCombos(gridSize, mResult, mCells.size());
		ArrayList<int[]> multiplyCombos = getAllMultiplyCombos(gridSize,
				mResult, mCells.size());

		// Combine Add & Multiply result sets
		for (int[] multiplyCombo : multiplyCombos) {
			boolean newCombo = true;
			for (int[] resultCombo : resultCombos) {
				if (Arrays.equals(multiplyCombo, resultCombo)) {
					newCombo = false;
					break;
				}
			}
			if (newCombo) {
				resultCombos.add(multiplyCombo);
			}
		}

		return resultCombos;
	}

	/*
	 * Generates all combinations of numbers which satisfy the cage's arithmetic
	 * and MathDoku constraints i.e. a digit can only appear once in a
	 * column/row
	 */
	private ArrayList<int[]> setPossibleCombosVisibleOperator() {
		ArrayList<int[]> AllResults = new ArrayList<int[]>();

		int gridSize = mGrid.getGridSize();

		switch (this.mAction) {
		case ACTION_NONE:
			assert (mCells.size() == 1);
			int number[] = { mResult };
			AllResults.add(number);
			break;
		case ACTION_SUBTRACT:
			assert (mCells.size() == 2);
			for (int i1 = 1; i1 <= gridSize; i1++)
				for (int i2 = i1 + 1; i2 <= gridSize; i2++)
					if (i2 - i1 == mResult || i1 - i2 == mResult) {
						int numbers[] = { i1, i2 };
						AllResults.add(numbers);
						numbers = new int[] { i2, i1 };
						AllResults.add(numbers);
					}
			break;
		case ACTION_DIVIDE:
			assert (mCells.size() == 2);
			for (int i1 = 1; i1 <= gridSize; i1++)
				for (int i2 = i1 + 1; i2 <= gridSize; i2++)
					if (mResult * i1 == i2 || mResult * i2 == i1) {
						int numbers[] = { i1, i2 };
						AllResults.add(numbers);
						numbers = new int[] { i2, i1 };
						AllResults.add(numbers);
					}
			break;
		case ACTION_ADD:
			AllResults = getAllAddCombos(gridSize, mResult, mCells.size());
			break;
		case ACTION_MULTIPLY:
			AllResults = getAllMultiplyCombos(gridSize, mResult, mCells.size());
			break;
		}
		return AllResults;
	}

	// The following two variables are required by the recursive methods below.
	// They could be passed as parameters of the recursive methods, but this
	// reduces performance.
	private int[] getAllCombos_Numbers;
	private ArrayList<int[]> getAllCombos_ResultSet;

	private ArrayList<int[]> getAllAddCombos(int max_val, int target_sum,
			int n_cells) {
		getAllCombos_Numbers = new int[n_cells];
		getAllCombos_ResultSet = new ArrayList<int[]>();
		getAddCombos(max_val, target_sum, n_cells);
		return getAllCombos_ResultSet;
	}

	/*
	 * Recursive method to calculate all combinations of digits which add up to
	 * target
	 * 
	 * @param max_val maximum permitted value of digit (= dimension of grid)
	 * 
	 * @param target_sum the value which all the digits should add up to
	 * 
	 * @param n_cells number of digits still to select
	 */
	private void getAddCombos(int max_val, int target_sum, int n_cells) {
		for (int n = 1; n <= max_val; n++) {
			if (n_cells == 1) {
				if (n == target_sum) {
					getAllCombos_Numbers[0] = n;
					if (satisfiesConstraints(getAllCombos_Numbers))
						getAllCombos_ResultSet
								.add(getAllCombos_Numbers.clone());
				}
			} else {
				getAllCombos_Numbers[n_cells - 1] = n;
				getAddCombos(max_val, target_sum - n, n_cells - 1);
			}
		}
	}

	private ArrayList<int[]> getAllMultiplyCombos(int max_val, int target_sum,
			int n_cells) {
		getAllCombos_Numbers = new int[n_cells];
		getAllCombos_ResultSet = new ArrayList<int[]>();
		getMultiplyCombos(max_val, target_sum, n_cells);

		return getAllCombos_ResultSet;
	}

	/*
	 * Recursive method to calculate all combinations of digits which multiply
	 * up to target
	 * 
	 * @param max_val maximum permitted value of digit (= dimension of grid)
	 * 
	 * @param target_sum the value which all the digits should multiply up to
	 * 
	 * @param n_cells number of digits still to select
	 */
	private void getMultiplyCombos(int max_val, int target_sum, int n_cells) {
		for (int n = 1; n <= max_val; n++) {
			if (target_sum % n != 0)
				continue;

			if (n_cells == 1) {
				if (n == target_sum) {
					getAllCombos_Numbers[0] = n;
					if (satisfiesConstraints(getAllCombos_Numbers))
						getAllCombos_ResultSet
								.add(getAllCombos_Numbers.clone());
				}
			} else {
				getAllCombos_Numbers[n_cells - 1] = n;
				getMultiplyCombos(max_val, target_sum / n, n_cells - 1);
			}
		}
	}

	/*
	 * Check whether the set of numbers satisfies all constraints Looking for
	 * cases where a digit appears more than once in a column/row Constraints: 0
	 * -> (mGridSize * mGridSize)-1 = column constraints (each column must
	 * contain each digit) mGridSize * mGridSize -> 2*(mGridSize * mGridSize)-1
	 * = row constraints (each row must contain each digit)
	 */
	private boolean satisfiesConstraints(int[] possibles) {

		int gridSize = mGrid.getGridSize();

		boolean constraints[] = new boolean[gridSize * gridSize * 2];
		int constraint_num;
		for (int i = 0; i < this.mCells.size(); i++) {
			constraint_num = gridSize * (possibles[i] - 1)
					+ mCells.get(i).getColumn();
			if (constraints[constraint_num])
				return false;
			else
				constraints[constraint_num] = true;
			constraint_num = gridSize * gridSize + gridSize
					* (possibles[i] - 1) + mCells.get(i).getRow();
			if (constraints[constraint_num])
				return false;
			else
				constraints[constraint_num] = true;
		}
		return true;
	}

	/**
	 * Create a string representation of the Grid Cage which can be used to
	 * store a grid cage in a saved game.
	 * 
	 * @return A string representation of the grid cage.
	 */
	public String toStorageString() {
		String storageString = SAVE_GAME_CAGE_LINE
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1 + mId
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
				+ mAction
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
				+ mResult
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1;
		for (GridCell cell : mCells) {
			storageString += cell.getCellId()
					+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL2;
		}
		storageString += SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
				+ Boolean.toString(isOperatorHidden());

		return storageString;
	}

	/**
	 * Read cage information from or a storage string which was created with @
	 * GridCage#toStorageString()} before.
	 * 
	 * @param line
	 *            The line containing the cage information.
	 * @return True in case the given line contains cage information and is
	 *         processed correctly. False otherwise.
	 */
	public boolean fromStorageString(String line, int savedWithRevisionNumber) {
		String[] cageParts = line
				.split(SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1);

		// Only process the storage string if it starts with the correct
		// identifier.
		if (cageParts[0].equals(SAVE_GAME_CAGE_LINE) == false) {
			return false;
		}

		// When upgrading to MathDoku v2 the history is not converted. As of
		// revision 369 all logic for handling games stored with older versions
		// is removed.
		if (savedWithRevisionNumber <= 368) {
			return false;
		}

		// Process all parts
		int index = 1;
		mId = Integer.parseInt(cageParts[index++]);
		mAction = Integer.parseInt(cageParts[index++]);
		mResult = Integer.parseInt(cageParts[index++]);
		for (String cellId : cageParts[index++]
				.split(SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL2)) {
			GridCell c = mGrid.mCells.get(Integer.parseInt(cellId));
			c.setCageId(mId);
			mCells.add(c);
		}
		// noinspection UnusedAssignment
		mHideOperator = Boolean.parseBoolean(cageParts[index++]);

		return true;
	}

	/**
	 * Sets the reference to the grid to which this cage belongs.
	 * 
	 * @param grid
	 *            The grid to which the cage belongs.
	 */
	public void setGridReference(Grid grid) {
		mGrid = grid;
	}

	private boolean allCellsFilledWithUserValue() {
		if (mCells != null) {
			for (GridCell gridCell : mCells) {
				if (!gridCell.isUserValueSet()) {
					return false;
				}
			}
		}
		return true;
	}
}