package testHelper;

import net.mathdoku.plus.enums.CageOperator;
import net.mathdoku.plus.enums.GridType;
import net.mathdoku.plus.enums.PuzzleComplexity;
import net.mathdoku.plus.puzzle.grid.Grid;

/**
 * The test grid in this class has been generated with the specified generator
 * version of the app. In case the grid is recreated with the same grid
 * generating parameters as specified by this class, the result should be equal
 * to the grid itself.
 * 
 * All data in the methods below should be kept in sync with the specified
 * version of the generator.
 * 
 * As the data below was generated with hidden operators, this grid can be used
 * with visible and with hidden operator as well.
 */
public class GridCreator5x5 extends GridCreator {

	public static GridCreator5x5 create() {
		return new GridCreator5x5();
	}

	public static GridCreator5x5 createEmpty() {
		GridCreator5x5 gridCreator = new GridCreator5x5();
		gridCreator.setEmptyGrid();
		return gridCreator;
	}

	public static Grid createEmptyGrid() {
		return createEmpty().getGrid();
	}

	protected GridCreator5x5() {
		super();
	}

	/**
	 * This code is generated based on a grid which is generated by the app. To
	 * generate the code, use the app in Development Mode. Choose menu option
	 * Development Options -> Generate code for test helper.
	 * 
	 * DO NOT REMOVE THIS TEXT.
	 */
	@Override
	protected long getGameSeed() {
		return -3965130064001655244L;
	}

	protected GridType getGridType() {
		return GridType.GRID_5X5;
	}

	protected boolean getHideOperator() {
		return false;
	}

	protected PuzzleComplexity getPuzzleComplexity() {
		return PuzzleComplexity.VERY_DIFFICULT;
	}

	protected int getGeneratorVersionNumber() {
		return 598;
	}

	protected int getMaxCageResult() {
		return 99999;
	}

	protected int getMaxCageSize() {
		return 6;
	}

	protected int[] getCorrectValuePerCell() {
		return new int[] {
				// Row 0
				2, 3, 5, 4, 1,
				// Row 1
				4, 1, 3, 2, 5,
				// Row 2
				1, 5, 2, 3, 4,
				// Row 3
				5, 2, 4, 1, 3,
				// Row 4
				3, 4, 1, 5, 2, };
	}

	protected int[] getCageIdPerCell() {
		return new int[] {
				// Row 0
				1, 2, 2, 3, 3,
				// Row 1
				1, 2, 4, 4, 3,
				// Row 2
				1, 2, 5, 4, 4,
				// Row 3
				1, 0, 5, 4, 0,
				// Row 4
				6, 0, 0, 0, 0, };
	}

	protected int[] getResultPerCage() {
		return new int[] {
				// Cage 0
				240,
				// Cage 1
				40,
				// Cage 2
				75,
				// Cage 3
				10,
				// Cage 4
				72,
				// Cage 5
				6,
				// Cage 6
				3, };
	}

	protected CageOperator[] getCageOperatorPerCage() {
		return new CageOperator[] {
				// Cage 0
				CageOperator.MULTIPLY,
				// Cage 1
				CageOperator.MULTIPLY,
				// Cage 2
				CageOperator.MULTIPLY,
				// Cage 3
				CageOperator.ADD,
				// Cage 4
				CageOperator.MULTIPLY,
				// Cage 5
				CageOperator.ADD,
				// Cage 6
				CageOperator.NONE, };
	}

	@Override
	public String getGridDefinition() {
		return new StringBuilder() //
				// PuzzleComplexity id
				.append("5")
				.append(FIELD_SEPARATOR_GRID_DEFINITION_PART)
				// Cage ids for cells on row 0
				.append("0102020303")
				// Cage ids for cells on row 1
				.append("0102040403")
				// Cage ids for cells on row 2
				.append("0102050404")
				// Cage ids for cells on row 3
				.append("0100050400")
				// Cage ids for cells on row 4
				.append("0600000000")
				// Definition for cage id 0
				.append(FIELD_SEPARATOR_GRID_DEFINITION_PART)
				.append("0,240,3")
				// Definition for cage id 1
				.append(FIELD_SEPARATOR_GRID_DEFINITION_PART)
				.append("1,40,3")
				// Definition for cage id 2
				.append(FIELD_SEPARATOR_GRID_DEFINITION_PART)
				.append("2,75,3")
				// Definition for cage id 3
				.append(FIELD_SEPARATOR_GRID_DEFINITION_PART)
				.append("3,10,1")
				// Definition for cage id 4
				.append(FIELD_SEPARATOR_GRID_DEFINITION_PART)
				.append("4,72,3")
				// Definition for cage id 5
				.append(FIELD_SEPARATOR_GRID_DEFINITION_PART)
				.append("5,6,1")
				// Definition for cage id 6
				.append(FIELD_SEPARATOR_GRID_DEFINITION_PART)
				.append("6,3,0")
				.toString();
	}
}
