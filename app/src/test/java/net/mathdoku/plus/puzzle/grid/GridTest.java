package net.mathdoku.plus.puzzle.grid;

import android.app.Activity;
import android.content.Context;

import net.mathdoku.plus.Preferences;
import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.enums.GridType;
import net.mathdoku.plus.enums.PuzzleComplexity;
import net.mathdoku.plus.gridgenerating.GridGeneratingParameters;
import net.mathdoku.plus.puzzle.InvalidGridException;
import net.mathdoku.plus.puzzle.cage.Cage;
import net.mathdoku.plus.puzzle.cell.Cell;
import net.mathdoku.plus.puzzle.cellchange.CellChange;
import net.mathdoku.plus.statistics.GridStatistics;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import robolectric.RobolectricGradleTestRunner;
import testHelper.GridCreator;
import testHelper.GridCreator4x4;
import testHelper.GridCreator4x4HiddenOperators;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class GridTest {
	private Activity mActivity;
	private Preferences mPreferencesMock;
	private GridBuilderStub mGridBuilderStub;

	/**
	 * The GridBuilderStub is used set up a default grid builder for each test
	 * case. It has some additional methods for tweaking the setup for specific
	 * test cases.
	 */
	private class GridBuilderStub extends GridBuilder {
		/*
		 * By default create a new mock for each each cell in the grid.
		 */
		private boolean mUseSameMockForAllCells = false;
		private boolean mUseSameMockForAllCages = false;

		/*
		 * Variables below refer to the last created grid cell mock and the grid
		 * cage mock. In case the same mock has to be used for all cells, the
		 * variables below are use for each such mock.
		 */
		public Cell mAnyCellMockOfDefaultSetup = null;
		public Cell mCellMockOfDefaultSetup[] = null;
		public Cage mAnyCageOfDefaultSetup = null;
		public Cage mCageMockOfDefaultSetup[] = null;

		private boolean mHideOperators = false;

		public GridGeneratingParameters mGridGeneratingParametersDefaultSetup;

		public GridBuilderStub useSameMockForAllCells() {
			mUseSameMockForAllCells = true;

			return this;
		}

		public GridBuilderStub useSameMockForAllCages() {
			mUseSameMockForAllCages = true;

			return this;
		}

		public GridBuilderStub setHiddenOperators() {
			mHideOperators = true;

			return this;
		}

		public GridBuilderStub setupDefaultWhichDoesNotThrowErrorsOnBuild() {
			GridType gridType = GridType.GRID_4X4;
			setGridSize(gridType.getGridSize());

			// Insert exact number of cells needed with this grid size. A
			// reference to the last created grid cell mock is kept for tests
			// which need just a cell in the default grid.
			int numberOfCells = gridType.getNumberOfCells();
			List<Cell> cells = mGridObjectsCreator.createArrayListOfCells();
			mCellMockOfDefaultSetup = new Cell[numberOfCells];
			for (int i = 0; i < numberOfCells; i++) {
				if (!mUseSameMockForAllCells || i == 0) {
					mCellMockOfDefaultSetup[i] = mock(Cell.class);
				} else {
					mCellMockOfDefaultSetup[i] = mCellMockOfDefaultSetup[0];
				}
				cells.add(mCellMockOfDefaultSetup[i]);
			}
			super.setCells(cells);
			mAnyCellMockOfDefaultSetup = mCellMockOfDefaultSetup[numberOfCells - 1];

			// Insert an arbitrary number of cages (at least 1). A
			// reference to the last created grid cage mock is kept for tests
			// which need just a cage in the default grid.
			int numberOfCages = 3;
			List<Cage> cages = mGridObjectsCreator.createArrayListOfCages();
			mCageMockOfDefaultSetup = new Cage[numberOfCages];
			for (int i = 0; i < numberOfCages; i++) {
				if (!mUseSameMockForAllCages || i == 0) {
					mCageMockOfDefaultSetup[i] = mock(Cage.class);
				} else {
					mCageMockOfDefaultSetup[i] = mCageMockOfDefaultSetup[0];
				}
				cages.add(mCageMockOfDefaultSetup[i]);
			}
			super.setCages(cages);
			mAnyCageOfDefaultSetup = mCageMockOfDefaultSetup[numberOfCages - 1];

			super.setGridStatistics(mGridObjectsCreator.createGridStatistics());

			mGridGeneratingParametersDefaultSetup = mGridObjectsCreator
					.createGridGeneratingParameters(gridType, mHideOperators,
							PuzzleComplexity.EASY, 598);
			super
					.setGridGeneratingParameters(mGridGeneratingParametersDefaultSetup);

			return this;
		}

		/**
		 * Initializes the list of cells of the GridBuilder with the given grid
		 * cells.
		 */
		public GridBuilderStub setCellsInitializedWith(Cell... cell) {
			// Default setup is no longer valid as the list of cells is
			// replaced.
			mAnyCellMockOfDefaultSetup = null;

			List<Cell> cells = new ArrayList<Cell>();

			for (int i = 0; i < cell.length; i++) {
				cells.add(cell[i]);
			}
			super.setCells(cells);

			return this;
		}

		/**
		 * Initializes the list of cages of the GridBuilder with the given grid
		 * cages.
		 */
		public GridBuilderStub setCagesInitializedWith(Cage... cage) {
			List<Cage> cages = new ArrayList<Cage>();

			for (int i = 0; i < cage.length; i++) {
				cages.add(cage[i]);
			}
			super.setCages(cages);

			return this;
		}

		/**
		 * Initializes the list of cell changess of the GridBuilder with the
		 * given cell changes.
		 */
		public GridBuilderStub setCellChangesInitializedWith(
				CellChange... cellChange) {
			List<CellChange> cellChanges = new ArrayList<CellChange>();

			for (int i = 0; i < cellChange.length; i++) {
				cellChanges.add(cellChange[i]);
			}
			super.setCellChanges(cellChanges);

			return this;
		}

		public GridBuilderStub setCellMockAsSelectedCell(int idSelectedCell) {
			// Set the selected cell before building the grid
			when(mCellMockOfDefaultSetup[idSelectedCell].isSelected())
					.thenReturn(true);

			return this;
		}

		private GridBuilderStub setCellMockWithEnteredValue(int id, int row,
				int column, int enteredValue) {
			// Define row and column of the selected cell
			when(mCellMockOfDefaultSetup[id].getRow()).thenReturn(row);
			when(mCellMockOfDefaultSetup[id].getColumn()).thenReturn(column);
			// Set user value
			when(mCellMockOfDefaultSetup[id].getEnteredValue()).thenReturn(
					enteredValue);

			return this;
		}

		private GridBuilderStub setCellMockWithMaybeValues(int id, int row,
				int column, int maybeValues[]) {
			when(mCellMockOfDefaultSetup[id].getRow()).thenReturn(row);
			when(mCellMockOfDefaultSetup[id].getColumn()).thenReturn(column);
			for (int maybeValue : maybeValues) {
				when(mCellMockOfDefaultSetup[id].hasPossible(maybeValue))
						.thenReturn(true);
			}

			return this;
		}
	}

	// Mocks used by the GridObjectsCreatorStub when creating new objects for
	// the Grid.
	private CellChange mCellChangeMock = mock(CellChange.class);
	private GridStatistics mGridStatisticsMock = mock(GridStatistics.class);
	private GridSaver mGridSaverMock = mock(GridSaver.class);

	private class GridTestObjectsCreator extends Grid.ObjectsCreator {
		// Unreveal the array list of cell changes as it is hidden in the Grid
		// Object.
		public List<CellChange> mArrayListOfCellChanges = null;

		@Override
		public GridStatistics createGridStatistics() {
			return mGridStatisticsMock;
		}

		@Override
		public List<CellChange> createArrayListOfCellChanges() {
			mArrayListOfCellChanges = super.createArrayListOfCellChanges();
			return mArrayListOfCellChanges;
		}

		@Override
		public GridSaver createGridSaver() {
			return mGridSaverMock;
		}

		@Override
		public CellChange createCellChange(Cell selectedCell) {
			return mCellChangeMock;
		}
	}

	private GridTestObjectsCreator mGridTestObjectsCreator;

	@Before
	public void setUp() {
		mActivity = new Activity();

		Preferences.ObjectsCreator preferencesObjectsCreator = new Preferences.ObjectsCreator() {
			@Override
			public Preferences createPreferences(Context context) {
				return mock(Preferences.class);
			}
		};

		// Create the Preference Instance with the Singleton Creator which uses
		// a mocked Preferences object
		mPreferencesMock = Preferences.getInstance(mActivity,
				preferencesObjectsCreator);

		mGridBuilderStub = new GridBuilderStub();
		mGridTestObjectsCreator = new GridTestObjectsCreator();
		mGridBuilderStub.setGridObjectsCreator(mGridTestObjectsCreator);
		mGridBuilderStub.setupDefaultWhichDoesNotThrowErrorsOnBuild();
	}

	@Test
	public void getSelectedCage_SelectedCellIsNull_NullCage() {
		Grid grid = mGridBuilderStub.build();
		assertThat("Selected cage", grid.getSelectedCage(), is(nullValue()));
	}

	@Test
	public void getSelectedCage_SelectedCellIsNotNull_CageSelected() {
		GridCreator testGrid = GridCreator4x4.createEmpty();
		Grid grid = testGrid.getGrid();
		int idSelectedCell = 3;
		int idSelectedCage = testGrid.getCageIdOfCell(idSelectedCell);
		Cell cell = grid.getCell(idSelectedCell);
		grid.setSelectedCell(cell);

		assertThat("Selected cage", grid.getSelectedCage(),
				is(grid.getCage(idSelectedCage)));
	}

	@Test
	public void clearCells_GridWithMultipleMovesCleared_AllMovesCleared()
			throws Exception {
		mGridBuilderStub.setCellChangesInitializedWith(mCellChangeMock,
				mock(CellChange.class), mock(CellChange.class),
				mock(CellChange.class));
		Grid grid = mGridBuilderStub.build();
		assertThat("Number of cell changes before clear", grid.countMoves(),
				is(mGridBuilderStub.mCellChanges.size()));

		grid.clearCells();

		assertThat("Number of moves for grid after clear", grid.countMoves(),
				is(0));
	}

	@Test
	public void clearCells_GridWithMultipleCellsCleared_AllCellsCleared()
			throws Exception {
		Grid grid = mGridBuilderStub
				.useSameMockForAllCells()
				.setupDefaultWhichDoesNotThrowErrorsOnBuild()
				.build();

		grid.clearCells();

		// Note: currently a cell is always cleared even in case it does not
		// contain a user values nor any maybe values.
		verify(mGridBuilderStub.mAnyCellMockOfDefaultSetup,
				times(grid.getCells().size())).clearValue();
	}

	@Test
	public void clearCells_GridWithMultipleCellsCleared_GridStatisticsUpdated()
			throws Exception {
		mGridBuilderStub
				.useSameMockForAllCells()
				.setupDefaultWhichDoesNotThrowErrorsOnBuild();
		when(mGridBuilderStub.mAnyCellMockOfDefaultSetup.getEnteredValue())
				.thenReturn(0, 1, 2, 0);
		Grid grid = mGridBuilderStub.build();

		grid.clearCells();

		// Clearing a second time won't change the statistics as no cells are
		// left to be cleared
		when(mGridBuilderStub.mAnyCellMockOfDefaultSetup.getEnteredValue())
				.thenReturn(0, 0, 0, 0);
		grid.clearCells();

		verify(mGridStatisticsMock).increaseCounter(
				GridStatistics.StatisticsCounterType.ACTION_CLEAR_GRID);
	}

	@Test
	public void clearCells_AnyGrid_UserMathOfAllCagesIsChecked()
			throws Exception {
		Grid grid = mGridBuilderStub.build();
		// Method checkMathOnEnteredValues is already called while instantiating
		// the new
		// grid.
		verify(mGridBuilderStub.mAnyCageOfDefaultSetup)
				.checkMathOnEnteredValues();

		grid.clearCells();

		verify(mGridBuilderStub.mAnyCageOfDefaultSetup, times(2))
				.checkMathOnEnteredValues();
	}

	private void assertThatCellDoesNotExist(int gridSize, int row, int col) {
		mGridBuilderStub.setGridSize(gridSize);
		Grid grid = mGridBuilderStub.build();

		assertThat("Cell found for row and column", grid.getCellAt(row, col),
				is(nullValue()));
	}

	@Test
	public void getCellAt_NegativeRowNumber_CellDoesNotExist() throws Exception {
		int gridSize = 4;
		int row = -1;
		int col = 1;

		assertThatCellDoesNotExist(gridSize, row, col);
	}

	@Test
	public void getCellAt_NegativeColNumber_CellDoesNotExist() throws Exception {
		int gridSize = 4;
		int row = 1;
		int col = -1;

		assertThatCellDoesNotExist(gridSize, row, col);
	}

	@Test
	public void getCellAt_RowNumberGreaterOrEqualsToGridSize_CellDoesNotExist()
			throws Exception {
		int gridSize = 4;
		int row = gridSize;
		int col = 1;

		assertThatCellDoesNotExist(gridSize, row, col);
	}

	@Test
	public void getCellAt_ColNumberGreaterOrEqualsToGridSize_CellDoesNotExist()
			throws Exception {
		int gridSize = 4;
		int row = 1;
		int col = gridSize;

		assertThatCellDoesNotExist(gridSize, row, col);
	}

	@Test
	public void getCellAt_ValidRowAndColNumber_NotNull() throws Exception {
		Grid grid = mGridBuilderStub.build();

		assertThat("Cell retrieved from grid",
				grid.getCellAt(mGridBuilderStub.mGridSize - 1,
						mGridBuilderStub.mGridSize - 1),
				is(sameInstance(mGridBuilderStub.mAnyCellMockOfDefaultSetup)));
	}

	@Test
	public void revealSolution_NonEmptyCellListWith2IncorrectCells_TwoCellsRevealed()
			throws Exception {
		mGridBuilderStub
				.useSameMockForAllCells()
				.setupDefaultWhichDoesNotThrowErrorsOnBuild();
		when(
				mGridBuilderStub.mAnyCellMockOfDefaultSetup
						.isEnteredValueIncorrect()).thenReturn(false, true,
				false, true, false);
		Grid grid = mGridBuilderStub.build();

		grid.revealSolution();

		// Check if test isn't flawed with incorrect number of cells in list.
		verify(mGridBuilderStub.mAnyCellMockOfDefaultSetup,
				times(mGridBuilderStub.mCells.size()))
				.isEnteredValueIncorrect();

		// Check whether the correct number of cells has been revealed.
		int expectedNumberOfCellsRevealed = 2; // Number of cells with value
												// false
		verify(mGridBuilderStub.mAnyCellMockOfDefaultSetup,
				times(expectedNumberOfCellsRevealed)).revealCorrectValue();
	}

	@Test
	public void revealSolution_GridWithHiddenOperators_OperatorsRevealed()
			throws Exception {
		mGridBuilderStub
				.useSameMockForAllCells()
				.useSameMockForAllCages()
				.setHiddenOperators()
				.setupDefaultWhichDoesNotThrowErrorsOnBuild();
		Grid grid = mGridBuilderStub.build();
		// During setup of default grid the method setCageText is already called
		// once for each cage.
		verify(mGridBuilderStub.mAnyCellMockOfDefaultSetup,
				times(mGridBuilderStub.mCages.size())).setCageText(anyString());

		grid.revealSolution();

		verify(mGridBuilderStub.mAnyCageOfDefaultSetup,
				times(mGridBuilderStub.mCages.size())).revealOperator();
		// Check if setCageText is called a second time for each cage
		verify(mGridBuilderStub.mAnyCellMockOfDefaultSetup,
				times(2 * mGridBuilderStub.mCages.size())).setCageText(
				anyString());

	}

	@Test
	public void revealSolution_NonEmptyCellList_GridStatisticsUpdated()
			throws Exception {
		Grid grid = mGridBuilderStub.build();
		grid.revealSolution();

		verify(mGridStatisticsMock).solutionRevealed();
	}

	@Test
	public void unrevealSolution() throws Exception {
		Grid grid = mGridBuilderStub.build();
		Config.AppMode actualAppMode = Config.mAppMode;
		Config.AppMode expectedAppMode = Config.AppMode.DEVELOPMENT;

		assertThat("Development mode", actualAppMode, is(expectedAppMode));
		assertThat("Grid is unrevealed", grid.isSolutionRevealed(), is(false));
	}

	@Test
	public void isSolved_UnSolvedGridIsChecked_False() throws Exception {
		mGridBuilderStub
				.useSameMockForAllCells()
				.setupDefaultWhichDoesNotThrowErrorsOnBuild();
		when(
				mGridBuilderStub.mAnyCellMockOfDefaultSetup
						.isEnteredValueIncorrect()).thenReturn(false, false,
				true, false);
		Grid grid = mGridBuilderStub.build();

		assertThat("Grid is solved", grid.isSolved(), is(false));

		// Check if test isn't flawed with incorrect number of cells in list.
		verify(mGridBuilderStub.mAnyCellMockOfDefaultSetup, atLeast(1))
				.isEnteredValueIncorrect();
	}

	@Test
	public void isSolved_SolvedGridIsChecked_True() throws Exception {
		mGridBuilderStub
				.useSameMockForAllCells()
				.setupDefaultWhichDoesNotThrowErrorsOnBuild();
		when(
				mGridBuilderStub.mAnyCellMockOfDefaultSetup
						.isEnteredValueIncorrect()).thenReturn(false);
		Grid grid = mGridBuilderStub.build();

		assertThat("Grid is solved", grid.isSolved(), is(true));

		// Check if test isn't flawed with incorrect number of cells in list.
		verify(mGridBuilderStub.mAnyCellMockOfDefaultSetup,
				times(mGridBuilderStub.mCells.size()))
				.isEnteredValueIncorrect();
	}

	@Test
	public void setSolved_SolvedGrid_GridStatisticsUpdated() throws Exception {
		Grid grid = mGridBuilderStub.build();
		grid.setSolved();

		verify(mGridStatisticsMock).solved();
	}

	@Test
	public void setSolved_SolvedGrid_OnSolvedListenerCalled() throws Exception {
		Grid grid = mGridBuilderStub.build();
		Grid.OnSolvedListener gridOnSolvedListener = mock(Grid.OnSolvedListener.class);
		grid.setSolvedHandler(gridOnSolvedListener);

		// Solve it
		grid.setSolved();

		verify(gridOnSolvedListener).puzzleSolved();
	}

	@Test
	public void isSolutionValidSoFar_AllCellsEmpty_True() throws Exception {
		mGridBuilderStub
				.useSameMockForAllCells()
				.setupDefaultWhichDoesNotThrowErrorsOnBuild();
		when(mGridBuilderStub.mAnyCellMockOfDefaultSetup.hasEnteredValue())
				.thenReturn(false);
		Grid grid = mGridBuilderStub.build();

		assertThat("Grid is valid so far", grid.isSolutionValidSoFar(),
				is(true));
	}

	@Test
	public void isSolutionValidSoFar_FilledCellIsValid_True() throws Exception {
		when(mGridBuilderStub.mAnyCellMockOfDefaultSetup.hasEnteredValue())
				.thenReturn(true);
		when(mGridBuilderStub.mAnyCellMockOfDefaultSetup.getEnteredValue())
				.thenReturn(2);
		when(
				mGridBuilderStub.mAnyCellMockOfDefaultSetup
						.isEnteredValueIncorrect()).thenReturn(false);
		Grid grid = mGridBuilderStub.build();

		assertThat("Grid is valid so far", grid.isSolutionValidSoFar(),
				is(true));
	}

	@Test
	public void isSolutionValidSoFar_FilledCellIsInvalid_False()
			throws Exception {
		when(mGridBuilderStub.mAnyCellMockOfDefaultSetup.hasEnteredValue())
				.thenReturn(true);
		when(mGridBuilderStub.mAnyCellMockOfDefaultSetup.getEnteredValue())
				.thenReturn(2);
		when(
				mGridBuilderStub.mAnyCellMockOfDefaultSetup
						.isEnteredValueIncorrect()).thenReturn(true);
		Grid grid = mGridBuilderStub.build();

		assertThat("Grid is valid so far", grid.isSolutionValidSoFar(),
				is(false));
	}

	@Test
	public void isSolutionValidSoFar_MultipleCellsIncludingAnInvalid_False()
			throws Exception {
		int cellIdToBeFilledWithCorrectValue = 4;
		Grid grid = GridCreator4x4
				.createEmpty()
				.setCorrectEnteredValueInCell(cellIdToBeFilledWithCorrectValue)
				.setCorrectEnteredValueInCell(
						cellIdToBeFilledWithCorrectValue + 1)
				.setIncorrectEnteredValueInCell(
						cellIdToBeFilledWithCorrectValue + 2)
				.getGrid();

		assertThat("Grid is valid so far", grid.isSolutionValidSoFar(),
				is(false));
	}

	@Test
	public void addMove_FirstMoveAddedToNullList_True() throws Exception {
		mGridBuilderStub.mCellChanges = null;
		Grid grid = mGridBuilderStub.build();
		grid.addMove(mCellChangeMock);

		assertThat("Number of cell changes",
				mGridTestObjectsCreator.mArrayListOfCellChanges.size(), is(1));
	}

	@Test
	public void addMove_FirstMoveAddedToEmptyList_True() throws Exception {
		Grid grid = mGridBuilderStub.build();
		grid.addMove(mCellChangeMock);

		assertThat("Number of cell changes",
				mGridTestObjectsCreator.mArrayListOfCellChanges.size(), is(1));
	}

	@Test
	public void addMove_AddMultipleDifferentMoves_True() throws Exception {
		Grid grid = mGridBuilderStub.build();
		grid.addMove(mCellChangeMock);
		// Add another mock as moves may not be identical for this test
		grid.addMove(mock(CellChange.class));

		assertThat("Number of cell changes",
				mGridTestObjectsCreator.mArrayListOfCellChanges.size(), is(2));
	}

	@Test
	public void addMove_AddMultipleIdenticalMoves_True() throws Exception {
		Grid grid = mGridBuilderStub.build();
		// Add same mock twice as we need identical moves for this test
		grid.addMove(mCellChangeMock);
		grid.addMove(mCellChangeMock);

		assertThat("Number of cell changes",
				mGridTestObjectsCreator.mArrayListOfCellChanges.size(), is(1));
	}

	@Test
	public void countMoves_MovesListIsNull_ZeroMoves() throws Exception {
		mGridBuilderStub.mCellChanges = null;
		Grid grid = mGridBuilderStub.build();

		int actualNumberOfCellChanges = grid.countMoves();
		assertThat("Number of moves in a Grid with an null moves list",
				actualNumberOfCellChanges, is(0));
	}

	@Test
	public void countMoves_MovesListIsNotEmpty_MovesCountedCorrectly()
			throws Exception {
		mGridBuilderStub.setCellChangesInitializedWith(mock(CellChange.class),
				mock(CellChange.class), mock(CellChange.class),
				mock(CellChange.class), mock(CellChange.class),
				mock(CellChange.class));
		Grid grid = mGridBuilderStub.build();

		assertThat("Number of moves in a Grid with a non-empty moves list",
				grid.countMoves(), is(mGridBuilderStub.mCellChanges.size()));
	}

	@Test
	public void countMoves_MovesListIsEmpty_ZeroMoves() throws Exception {
		Grid grid = mGridBuilderStub.build();
		int actualNumberOfCellChanges = grid.countMoves();
		assertThat("Number of moves in a Grid with an empty moves list",
				actualNumberOfCellChanges, is(0));
	}

	@Test
	public void undoLastMove_NullMovesList_False() throws Exception {
		mGridBuilderStub.setCellChanges(null);
		Grid grid = mGridBuilderStub.build();

		assertThat("Undo last move", grid.undoLastMove(), is(false));
	}

	@Test
	public void undoLastMove_EmptyMovesList_False() throws Exception {
		Grid grid = mGridBuilderStub.build();
		assertThat("Undo last move", grid.undoLastMove(), is(false));
	}

	@Test
	public void undoLastMove_RestoreCellAndCellChangeAreEmptyOrContainMaybes_MoveIsRestored()
			throws Exception {
		// This test simulates following situations:
		// 1) Before undo the cell is empty. After undo the cell is filled with
		// one or more maybe values.
		// 2) Before undo the cell contains one or more maybe values. After undo
		// the cell contains another set of maybe values (including 0 maybe
		// values).
		// In both cases the user value equals 0 before and after the actual
		// undo, indicating that the cell does not contain a user value.
		when(mGridBuilderStub.mAnyCellMockOfDefaultSetup.getEnteredValue())
				.thenReturn( //
						0 /*
						 * value before actual undo
						 */,
						//
						0 /*
						 * value after actual undo
						 */);
		when(mCellChangeMock.getCell()).thenReturn(
				mGridBuilderStub.mAnyCellMockOfDefaultSetup);
		mGridBuilderStub.setCellChangesInitializedWith(mCellChangeMock);
		int numberOfMovesBeforeUndo = mGridBuilderStub.mCellChanges.size();
		Grid grid = mGridBuilderStub.build();
		assertThat(grid.countMoves(), is(numberOfMovesBeforeUndo));

		grid.undoLastMove();

		verify(mCellChangeMock).restore();
		assertThat(grid.countMoves(), is(numberOfMovesBeforeUndo - 1));
		verify(mGridStatisticsMock).increaseCounter(
				GridStatistics.StatisticsCounterType.ACTION_UNDO_MOVE);
		assertThat("Selected cell", grid.getSelectedCell(),
				is(sameInstance(mGridBuilderStub.mAnyCellMockOfDefaultSetup)));
	}

	@Test
	public void undoLastMove_RestoreCellOrCellChangeAreEmptyOrContainEnteredValue_MoveIsRestored()
			throws Exception {
		// This test simulates following:
		// 1) Before undo the cell is empty. After undo the cell is filled with
		// a user value.
		// 2) Before undo the cell is filled with a user value. After undo the
		// cell is filled with another user value.
		// 3) Before undo the cell is filled with a user value. After undo the
		// cell does not contain a user value; the cell can contain a maybe
		// value.
		when(mGridBuilderStub.mAnyCellMockOfDefaultSetup.getEnteredValue())
				.thenReturn( //
						0 /* value before actual undo */,
						//
						1 /* value after actual undo */);
		// This unit test checks whether UndoLastMove correctly calls methods
		// markDuplicateValuesInSameRowAndColumn and checkMathOnEnteredValues.
		// Both those
		// methods are called when instantiating the new grid via the
		// GridBuilder.build().
		int cageIdAffectedCellInCellChange = 2;
		when(mGridBuilderStub.mAnyCellMockOfDefaultSetup.getCageId())
				.thenReturn(cageIdAffectedCellInCellChange);
		when(mCellChangeMock.getCell()).thenReturn(
				mGridBuilderStub.mAnyCellMockOfDefaultSetup);
		mGridBuilderStub.setCellChangesInitializedWith(mCellChangeMock);
		// Check if setup is correct
		int numberOfMovesBeforeUndo = mGridBuilderStub.mCellChanges.size();
		Grid grid = mGridBuilderStub.build();
		assertThat(grid.countMoves(), is(numberOfMovesBeforeUndo));
		// Check if setDuplicateHighlight and checkMathOnEnteredValues are
		// called during build
		verify(mGridBuilderStub.mAnyCellMockOfDefaultSetup)
				.setDuplicateHighlight(anyBoolean());
		verify(
				mGridBuilderStub.mCageMockOfDefaultSetup[cageIdAffectedCellInCellChange])
				.checkMathOnEnteredValues();

		grid.undoLastMove();

		verify(mCellChangeMock).restore();
		assertThat(grid.countMoves(), is(numberOfMovesBeforeUndo - 1));
		verify(mGridStatisticsMock).increaseCounter(
				GridStatistics.StatisticsCounterType.ACTION_UNDO_MOVE);
		assertThat("Selected cell", grid.getSelectedCell(),
				is(sameInstance(mGridBuilderStub.mAnyCellMockOfDefaultSetup)));
		// Check if setDuplicateHighlight and checkMathOnEnteredValues are
		// called as second time
		verify(mGridBuilderStub.mAnyCellMockOfDefaultSetup, times(2))
				.setDuplicateHighlight(anyBoolean());
		verify(
				mGridBuilderStub.mCageMockOfDefaultSetup[cageIdAffectedCellInCellChange],
				times(2)).checkMathOnEnteredValues();
	}

	@Test
	public void deselectSelectedCell_CellIsSelected_CellIsDeselected()
			throws Exception {
		Grid grid = GridCreator4x4.createEmptyGrid();
		int idSelectedCell = 3;
		Cell cell = grid.getCell(idSelectedCell);
		grid.setSelectedCell(cell);

		grid.deselectSelectedCell();

		assertThat("Selected cell", grid.getSelectedCell(), is(nullValue()));
	}

	@Test
	public void setSelectedCell_SelectNullCell_Null() throws Exception {
		Grid grid = mGridBuilderStub.build();
		assertThat("Selected cell", grid.setSelectedCell((Cell) null),
				is(nullValue()));
	}

	@Test
	public void setSelectedCell_NoCellCurrentlySelectedInGrid_BordersOfNewCageSetAndSelectedCellReturned()
			throws Exception {
		int cageIdOfSelectedCell = 1;
		when(mGridBuilderStub.mAnyCellMockOfDefaultSetup.getCageId())
				.thenReturn(cageIdOfSelectedCell);
		Grid grid = mGridBuilderStub.build();

		assertThat(
				"Selected cell",
				grid
						.setSelectedCell(mGridBuilderStub.mAnyCellMockOfDefaultSetup),
				is(sameInstance(mGridBuilderStub.mAnyCellMockOfDefaultSetup)));

		verify(mGridBuilderStub.mCageMockOfDefaultSetup[cageIdOfSelectedCell])
				.invalidateBordersOfAllCells();
	}

	@Test
	public void setSelectedCell_SelectAnotherCellInTheCurrentlySelectedCage_OldSelectedCellIsDeselected()
			throws Exception {
		Cell otherCellMock = mock(Cell.class);
		Grid grid = mGridBuilderStub.build();

		// Select the cells in given order
		grid.setSelectedCell(mGridBuilderStub.mAnyCellMockOfDefaultSetup);
		grid.setSelectedCell(otherCellMock);

		verify(mGridBuilderStub.mAnyCellMockOfDefaultSetup).setSelected(false);
	}

	@Test
	public void setSelectedCell_CurrentlySelectedCellInGridIsSelectedAgain_NoBordersReset()
			throws Exception {
		int cageIdOfSelectedCell = 1;
		when(mGridBuilderStub.mAnyCellMockOfDefaultSetup.getCageId())
				.thenReturn(cageIdOfSelectedCell);
		Grid grid = mGridBuilderStub.build();

		// Select the grid cell
		grid.setSelectedCell(mGridBuilderStub.mAnyCellMockOfDefaultSetup);
		verify(mGridBuilderStub.mCageMockOfDefaultSetup[cageIdOfSelectedCell],
				times(1)).invalidateBordersOfAllCells();

		// Select the same cell one more. The borders may not be reset again.
		grid.setSelectedCell(mGridBuilderStub.mAnyCellMockOfDefaultSetup);
		verify(mGridBuilderStub.mCageMockOfDefaultSetup[cageIdOfSelectedCell],
				times(1)).invalidateBordersOfAllCells();
	}

	@Test
	public void setSelectedCell_SelectAnotherCellInTheCurrentlySelectedCage_NoBordersReset()
			throws Exception {
		Grid grid = mGridBuilderStub.build();

		int cellId1 = 3;
		int cellId2 = 4;
		int selectedCageId = 1;
		when(mGridBuilderStub.mCellMockOfDefaultSetup[cellId1].getCageId())
				.thenReturn(selectedCageId);
		when(mGridBuilderStub.mCellMockOfDefaultSetup[cellId2].getCageId())
				.thenReturn(selectedCageId);

		// Select grid cell stub 1
		grid.setSelectedCell(mGridBuilderStub.mCellMockOfDefaultSetup[cellId1]);
		verify(mGridBuilderStub.mCageMockOfDefaultSetup[selectedCageId],
				times(1)).invalidateBordersOfAllCells();

		// Select the other cell in the same cage. The borders may not be reset
		// again.
		grid.setSelectedCell(mGridBuilderStub.mCellMockOfDefaultSetup[cellId2]);
		verify(mGridBuilderStub.mCageMockOfDefaultSetup[selectedCageId],
				times(1)).invalidateBordersOfAllCells();
	}

	@Test
	public void setSelectedCell_SelectCellInAnotherCage_NoBordersReset()
			throws Exception {
		Grid grid = mGridBuilderStub.build();

		int cellId1 = 3;
		int cageId1 = 0;
		when(mGridBuilderStub.mCellMockOfDefaultSetup[cellId1].getCageId())
				.thenReturn(cageId1);

		int cellId2 = 4;
		int cageId2 = 1;
		when(mGridBuilderStub.mCellMockOfDefaultSetup[cellId2].getCageId())
				.thenReturn(cageId2);

		// Select grid cell stub 1
		grid.setSelectedCell(mGridBuilderStub.mCellMockOfDefaultSetup[cellId1]);
		verify(mGridBuilderStub.mCageMockOfDefaultSetup[cageId1], times(1))
				.invalidateBordersOfAllCells();
		verify(mGridBuilderStub.mCageMockOfDefaultSetup[cageId2], times(0))
				.invalidateBordersOfAllCells();

		// Select the other cell in the same cage. The borders may not be reset
		// again. Border of both cages need to be set.
		grid.setSelectedCell(mGridBuilderStub.mCellMockOfDefaultSetup[cellId2]);
		verify(mGridBuilderStub.mCageMockOfDefaultSetup[cageId1], times(2))
				.invalidateBordersOfAllCells();
		verify(mGridBuilderStub.mCageMockOfDefaultSetup[cageId2], times(1))
				.invalidateBordersOfAllCells();
	}

	@Test
	public void clearRedundantPossiblesInSameRowOrColumn_AnotherCellInSameRowContainsTheRedundantPossibleValueButNoOtherPossibleValues_TheRedundantPossibleValueIsCleared()
			throws Exception {
		// Set up a grid with a for which the user value of the selected is
		// revealed and for which the same value os also used as a possible
		// value in another cell on the same row as the selected cell.
		int idSelectedCell = 14;
		int valueSelectedCell = 1;
		int rowSelectedCell = 3;
		int columnSelectedCell = 2;
		int idOtherCellOnSameRow = idSelectedCell - 1;
		Grid grid = mGridBuilderStub
				.setCellMockAsSelectedCell(idSelectedCell)
				.setCellMockWithEnteredValue(idSelectedCell, rowSelectedCell,
						columnSelectedCell, valueSelectedCell)
				.setCellMockWithMaybeValues(idOtherCellOnSameRow,
						rowSelectedCell, columnSelectedCell - 1,
						new int[] { valueSelectedCell })
				.build();

		grid.clearRedundantPossiblesInSameRowOrColumn(mock(CellChange.class));

		verify(mGridBuilderStub.mCellMockOfDefaultSetup[idOtherCellOnSameRow])
				.hasPossible(valueSelectedCell);
		verify(mGridBuilderStub.mCellMockOfDefaultSetup[idOtherCellOnSameRow])
				.removePossible(valueSelectedCell);
	}

	@Test
	public void clearRedundantPossiblesInSameRowOrColumn_AnotherCellInSameRowContainsMultiplePossibleValuesIncludingTheRedundantValue_TheRedundantPossibleValueIsCleared()
			throws Exception {
		// Set up a grid with a for which the user value of the selected is
		// revealed and for which the same value os also used as a possible
		// value in another cell on the same row as the selected cell.
		int idSelectedCell = 14;
		int valueSelectedCell = 2;
		int rowSelectedCell = 3;
		int columnSelectedCell = 2;
		int idOtherCellOnSameRow = idSelectedCell - 1;
		Grid grid = mGridBuilderStub
				.setCellMockAsSelectedCell(idSelectedCell)
				.setCellMockWithEnteredValue(idSelectedCell, rowSelectedCell,
						columnSelectedCell, valueSelectedCell)
				.setCellMockWithMaybeValues(
						idOtherCellOnSameRow,
						rowSelectedCell,
						columnSelectedCell - 1,
						new int[] { valueSelectedCell - 1, valueSelectedCell,
								valueSelectedCell + 1 })
				.build();

		grid.clearRedundantPossiblesInSameRowOrColumn(mock(CellChange.class));

		verify(mGridBuilderStub.mCellMockOfDefaultSetup[idOtherCellOnSameRow])
				.hasPossible(valueSelectedCell);
		verify(mGridBuilderStub.mCellMockOfDefaultSetup[idOtherCellOnSameRow])
				.removePossible(valueSelectedCell);
		verify(mGridBuilderStub.mCellMockOfDefaultSetup[idOtherCellOnSameRow],
				never()).removePossible(valueSelectedCell - 1);
		verify(mGridBuilderStub.mCellMockOfDefaultSetup[idOtherCellOnSameRow],
				never()).removePossible(valueSelectedCell + 1);
	}

	@Test
	public void clearRedundantPossiblesInSameRowOrColumn_AnotherCellInSameColumnContainsTheRedundantPossibleValueButNoOtherPossibleValues_TheRedundantPossibleValueIsCleared()
			throws Exception {
		// Set up a grid with a for which the user value of the selected is
		// revealed and for which the same value is also used as a possible
		// value in another cell in the same column as the selected cell.
		int idSelectedCell = 14;
		int valueSelectedCell = 1;
		int rowSelectedCell = 3;
		int columnSelectedCell = 2;
		int idOtherCellInSameColumn = idSelectedCell - 1;
		Grid grid = mGridBuilderStub
				.setCellMockAsSelectedCell(idSelectedCell)
				.setCellMockWithEnteredValue(idSelectedCell, rowSelectedCell,
						columnSelectedCell, valueSelectedCell)
				.setCellMockWithMaybeValues(idOtherCellInSameColumn,
						rowSelectedCell - 1, columnSelectedCell,
						new int[] { valueSelectedCell })
				.build();

		grid.clearRedundantPossiblesInSameRowOrColumn(mock(CellChange.class));

		verify(
				mGridBuilderStub.mCellMockOfDefaultSetup[idOtherCellInSameColumn])
				.hasPossible(valueSelectedCell);
		verify(
				mGridBuilderStub.mCellMockOfDefaultSetup[idOtherCellInSameColumn])
				.removePossible(valueSelectedCell);
	}

	@Test
	public void clearRedundantPossiblesInSameRowOrColumn_AnotherCellInSameColumnContainsMultiplePossibleValuesIncludingTheRedundantValue_TheRedundantPossibleValueIsCleared()
			throws Exception {
		// Set up a grid with a for which the user value of the selected is
		// revealed and for which the same value os also used as a possible
		// value in another cell on the same row as the selected cell.
		int idSelectedCell = 14;
		int valueSelectedCell = 1;
		int rowSelectedCell = 3;
		int columnSelectedCell = 2;
		int idOtherCellInSameColumn = idSelectedCell - 1;
		Grid grid = mGridBuilderStub
				.setCellMockAsSelectedCell(idSelectedCell)
				.setCellMockWithEnteredValue(idSelectedCell, rowSelectedCell,
						columnSelectedCell, valueSelectedCell)
				.setCellMockWithMaybeValues(idOtherCellInSameColumn,
						rowSelectedCell - 1, columnSelectedCell,
						new int[] { valueSelectedCell })
				.build();

		grid.clearRedundantPossiblesInSameRowOrColumn(mock(CellChange.class));

		verify(
				mGridBuilderStub.mCellMockOfDefaultSetup[idOtherCellInSameColumn])
				.hasPossible(valueSelectedCell);
		verify(
				mGridBuilderStub.mCellMockOfDefaultSetup[idOtherCellInSameColumn])
				.removePossible(valueSelectedCell);
		verify(
				mGridBuilderStub.mCellMockOfDefaultSetup[idOtherCellInSameColumn],
				never()).removePossible(valueSelectedCell - 1);
		verify(
				mGridBuilderStub.mCellMockOfDefaultSetup[idOtherCellInSameColumn],
				never()).removePossible(valueSelectedCell + 1);
	}

	@Test(expected = InvalidGridException.class)
	public void gridCreateWithGridBuilder_CellsListIsNull_InvalidParameterException()
			throws Exception {
		mGridBuilderStub.setCells(null);
		assertThat(mGridBuilderStub.build(), is(nullValue()));
	}

	@Test(expected = InvalidGridException.class)
	public void gridCreateWithGridBuilder_CellsListIsEmpty_InvalidParameterException()
			throws Exception {
		mGridBuilderStub.setCells(new ArrayList<Cell>());
		assertThat(mGridBuilderStub.build(), is(nullValue()));
	}

	@Test(expected = InvalidGridException.class)
	public void gridCreateWithGridBuilder_CellsHasTooLittleCells_InvalidParameterException()
			throws Exception {
		mGridBuilderStub.setGridSize(2);
		mGridBuilderStub.setCellsInitializedWith(
				mGridBuilderStub.mAnyCellMockOfDefaultSetup,
				mGridBuilderStub.mAnyCellMockOfDefaultSetup,
				mGridBuilderStub.mAnyCellMockOfDefaultSetup);
		assertThat(mGridBuilderStub.build(), is(nullValue()));
	}

	@Test(expected = InvalidGridException.class)
	public void gridCreateWithGridBuilder_CellsHasTooManyCells_InvalidParameterException()
			throws Exception {
		mGridBuilderStub.setGridSize(1);
		mGridBuilderStub.setCellsInitializedWith(
				mGridBuilderStub.mAnyCellMockOfDefaultSetup,
				mGridBuilderStub.mAnyCellMockOfDefaultSetup);
		assertThat(mGridBuilderStub.build(), is(nullValue()));
	}

	@Test(expected = InvalidGridException.class)
	public void gridCreateWithGridBuilder_CagesListIsNull_InvalidParameterException()
			throws Exception {
		mGridBuilderStub.setCages(null);
		assertThat(mGridBuilderStub.build(), is(nullValue()));
	}

	@Test(expected = InvalidGridException.class)
	public void gridCreateWithGridBuilder_CagesListIsEmpty_InvalidParameterException()
			throws Exception {
		mGridBuilderStub.setCages(new ArrayList<Cage>());
		assertThat(mGridBuilderStub.build(), is(nullValue()));
	}

	@Test(expected = InvalidGridException.class)
	public void gridCreateWithGridBuilder_GridGeneratingParametersIsNull_InvalidParameterException()
			throws Exception {
		mGridBuilderStub.setGridGeneratingParameters(null);
		assertThat(mGridBuilderStub.build(), is(nullValue()));
	}

	@Test
	public void gridCreateWithGridBuilder_ValidParameters_GridBuilderParamsUsed()
			throws Exception {
		mGridBuilderStub
				.useSameMockForAllCells()
				.useSameMockForAllCages()
				.setupDefaultWhichDoesNotThrowErrorsOnBuild();
		long dateCreated = System.currentTimeMillis();
		mGridBuilderStub.setDateCreated(dateCreated);
		mGridBuilderStub.setActive(false);
		Grid grid = mGridBuilderStub.build();
		assertThat("Selected grid cell", grid.getSelectedCell(),
				is(nullValue()));
		assertThat("Solution revealed", grid.isSolutionRevealed(), is(false));
		assertThat("Solving attempt id", grid.getSolvingAttemptId(),
				is(equalTo(-1)));
		assertThat("Row id", grid.getRowId(), is(equalTo(-1)));
		assertThat("Grid statistics", grid.getGridStatistics(),
				is(sameInstance(mGridStatisticsMock)));
		assertThat("Date created filled with system time",
				grid.getDateCreated(), is(dateCreated));
		assertThat("Date updated filled with system time", grid.getDateSaved(),
				is(dateCreated));
		assertThat("Grid size", grid.getGridSize(),
				is(mGridBuilderStub.mGridSize));
		assertThat("Cells", grid.getCells(), is(mGridBuilderStub.mCells));
		assertThat("Cages", grid.getCages(), is(mGridBuilderStub.mCages));
		assertThat("Is active", grid.isActive(), is(mGridBuilderStub.mActive));
		verify(mGridBuilderStub.mAnyCageOfDefaultSetup,
				times(mGridBuilderStub.mCages.size())).setGridReference(
				any(Grid.class));
		verify(mGridBuilderStub.mAnyCellMockOfDefaultSetup,
				times(mGridBuilderStub.mCells.size())).setGridReference(
				any(Grid.class));
		verify(mGridBuilderStub.mAnyCellMockOfDefaultSetup,
				times(mGridBuilderStub.mCages.size())).setCageText(anyString());
		verify(mGridBuilderStub.mAnyCageOfDefaultSetup,
				times(mGridBuilderStub.mCages.size()))
				.checkMathOnEnteredValues();
		verify(mGridBuilderStub.mAnyCellMockOfDefaultSetup, never())
				.setDuplicateHighlight(true);
	}

	@Test
	public void save_NoErrorsOnSave_GridIdSet() throws Exception {
		Grid grid = mGridBuilderStub.build();
		int rowId = 34;
		when(mGridSaverMock.save(grid)).thenReturn(true);
		when(mGridSaverMock.getRowId()).thenReturn(rowId);

		assertThat(grid.save(), is(true));
		assertThat(grid.getRowId(), is(rowId));
	}

	@Test
	public void save_NoErrorsOnSave_SolvingAttemptIdSet() throws Exception {
		Grid grid = mGridBuilderStub.build();
		int solvingAttemptId = 35;
		when(mGridSaverMock.save(grid)).thenReturn(true);
		when(mGridSaverMock.getSolvingAttemptId()).thenReturn(solvingAttemptId);

		assertThat(grid.save(), is(true));
		assertThat(grid.getSolvingAttemptId(), is(solvingAttemptId));
	}

	@Test
	public void save_NoErrorsOnSave_StatisticsSet() throws Exception {
		Grid grid = mGridBuilderStub.build();
		GridStatistics gridStatistics = mock(GridStatistics.class);
		when(mGridSaverMock.save(grid)).thenReturn(true);
		when(mGridSaverMock.getGridStatistics()).thenReturn(gridStatistics);

		assertThat(grid.save(), is(true));
		assertThat(grid.getGridStatistics(), is(gridStatistics));
	}

	@Test
	public void save_ErrorOnSave_False() throws Exception {
		Grid grid = mGridBuilderStub.build();
		when(mGridSaverMock.save(grid)).thenReturn(false);

		assertThat(grid.save(), is(false));
		verify(mGridSaverMock, never()).getRowId();
		verify(mGridSaverMock, never()).getSolvingAttemptId();
		verify(mGridSaverMock, never()).getGridStatistics();
	}

	@Test
	public void saveOnAppUpgrade_NoErrorsOnSaveOnAppUpgrade_GridIdSet()
			throws Exception {
		Grid grid = mGridBuilderStub.build();
		int rowId = 34;
		when(mGridSaverMock.saveOnAppUpgrade(grid)).thenReturn(true);
		when(mGridSaverMock.getRowId()).thenReturn(rowId);

		assertThat(grid.saveOnAppUpgrade(), is(true));
		assertThat(grid.getRowId(), is(rowId));
	}

	@Test
	public void saveOnAppUpgrade_NoErrorsOnSaveOnAppUpgrade_SolvingAttemptIdSet()
			throws Exception {
		Grid grid = mGridBuilderStub.build();
		int solvingAttemptId = 35;
		when(mGridSaverMock.saveOnAppUpgrade(grid)).thenReturn(true);
		when(mGridSaverMock.getSolvingAttemptId()).thenReturn(solvingAttemptId);

		assertThat(grid.saveOnAppUpgrade(), is(true));
		assertThat(grid.getSolvingAttemptId(), is(solvingAttemptId));
	}

	@Test
	public void saveOnAppUpgrade_NoErrorsOnSaveOnAppUpgrade_StatisticsSet()
			throws Exception {
		Grid grid = mGridBuilderStub.build();
		GridStatistics gridStatistics = mock(GridStatistics.class);
		when(mGridSaverMock.saveOnAppUpgrade(grid)).thenReturn(true);
		when(mGridSaverMock.getGridStatistics()).thenReturn(gridStatistics);

		assertThat(grid.saveOnAppUpgrade(), is(true));
		assertThat(grid.getGridStatistics(), is(gridStatistics));
	}

	@Test
	public void saveOnAppUpgrade_ErrorOnSaveOnAppUpgrade_False()
			throws Exception {
		Grid grid = mGridBuilderStub.build();
		when(mGridSaverMock.saveOnAppUpgrade(grid)).thenReturn(false);

		assertThat(grid.saveOnAppUpgrade(), is(false));
		verify(mGridSaverMock, never()).getRowId();
		verify(mGridSaverMock, never()).getSolvingAttemptId();
		verify(mGridSaverMock, never()).getGridStatistics();
	}

	@Test
	public void containsNoEnteredValues_GridHasNoEnteredValuesAndNoMaybeValues_True()
			throws Exception {
		Grid grid = mGridBuilderStub.build();

		assertThat(grid.containsNoEnteredValues(), is(true));
	}

	@Test
	public void containsNoEnteredValues_GridHasOneEnteredValueButNoMaybeValues_False()
			throws Exception {
		when(mGridBuilderStub.mAnyCellMockOfDefaultSetup.hasEnteredValue())
				.thenReturn(true);
		when(mGridBuilderStub.mAnyCellMockOfDefaultSetup.getEnteredValue())
				.thenReturn(2);
		Grid grid = mGridBuilderStub.build();

		assertThat(grid.containsNoEnteredValues(), is(false));
	}

	@Test
	public void containsNoEnteredValues_GridHasOneMaybeValueButNoEnteredValues_True()
			throws Exception {
		when(mGridBuilderStub.mAnyCellMockOfDefaultSetup.countPossibles())
				.thenReturn(1);
		Grid grid = mGridBuilderStub.build();

		assertThat(grid.containsNoEnteredValues(), is(true));
	}

	@Test
	public void isEmpty_GridHasNoEnteredValuesAndNoMaybeValues_True()
			throws Exception {
		Grid grid = mGridBuilderStub.build();

		assertThat(grid.isEmpty(), is(true));
	}

	@Test
	public void isEmpty_GridHasOneEnteredValueButNoMaybeValues_False()
			throws Exception {
		when(mGridBuilderStub.mAnyCellMockOfDefaultSetup.hasEnteredValue())
				.thenReturn(true);
		when(mGridBuilderStub.mAnyCellMockOfDefaultSetup.getEnteredValue())
				.thenReturn(2);
		Grid grid = mGridBuilderStub.build();

		assertThat(grid.isEmpty(), is(false));
	}

	@Test
	public void isEmpty_GridHasOneMaybeValueButNoEnteredValues_False()
			throws Exception {
		when(mGridBuilderStub.mAnyCellMockOfDefaultSetup.countPossibles())
				.thenReturn(1);
		Grid grid = mGridBuilderStub.build();

		assertThat(grid.isEmpty(), is(false));
	}

	@Test
	public void createNewGridForReplay_ContainsCellWithDuplicateValueHighlight_DuplicateHighLightIsCleared()
			throws Exception {
		// Use a grid without mocks for this test.
		Grid originalGrid = GridCreator4x4.createEmptyGrid();
		// Set only one property of one cell which should not be copied to the
		// new grid.
		int idOfCell_1 = 5;
		originalGrid.getCell(idOfCell_1).setDuplicateHighlight(true);

		Grid newGrid = originalGrid.createNewGridForReplay();

		assertThat(newGrid, is(notNullValue()));
		assertThat(newGrid.getCell(idOfCell_1),
				is(not(originalGrid.getCell(idOfCell_1))));
	}

	@Test
	public void createNewGridForReplay_ContainsCellWithEnteredValue_EnteredValueIsCleared()
			throws Exception {
		// Use a grid without mocks for this test.
		Grid originalGrid = GridCreator4x4.createEmptyGrid();
		// Set only one property of one cell which should not be copied to the
		// new grid.
		int idOfCell_1 = 5;
		originalGrid.getCell(idOfCell_1).setEnteredValue(3);

		Grid newGrid = originalGrid.createNewGridForReplay();

		assertThat(newGrid, is(notNullValue()));
		assertThat(newGrid.getCell(idOfCell_1).getEnteredValue(),
				is(not(originalGrid.getCell(idOfCell_1).getEnteredValue())));
	}

	@Test
	public void createNewGridForReplay_ContainsCellWithMaybeValue_PossiblesIsCleared()
			throws Exception {
		// Use a grid without mocks for this test.
		Grid originalGrid = GridCreator4x4.createEmptyGrid();
		// Set only one property of one cell which should not be copied to the
		// new grid.
		int idOfCell_1 = 5;
		originalGrid.getCell(idOfCell_1).addPossible(3);

		Grid newGrid = originalGrid.createNewGridForReplay();

		assertThat(newGrid, is(notNullValue()));
		assertThat(newGrid.getCell(idOfCell_1).getPossibles(),
				is(not(originalGrid.getCell(idOfCell_1).getPossibles())));
	}

	@Test
	public void createNewGridForReplay_ContainsRevealedCell_IsRevealedIsCleared()
			throws Exception {
		// Use a grid without mocks for this test.
		Grid originalGrid = GridCreator4x4.createEmptyGrid();
		// Set only one property of one cell which should not be copied to the
		// new grid.
		int idOfCell_1 = 5;
		originalGrid.getCell(idOfCell_1).revealCorrectValue();

		Grid newGrid = originalGrid.createNewGridForReplay();

		assertThat(newGrid, is(notNullValue()));
		assertThat(newGrid.getCell(idOfCell_1).isRevealed(),
				is(not(originalGrid.getCell(idOfCell_1).isRevealed())));
	}

	@Test
	public void createNewGridForReplay_InvalidUserHighlight_InvalidHighlightIsCleared()
			throws Exception {
		// Use a grid without mocks for this test.
		Grid originalGrid = GridCreator4x4.createEmptyGrid();
		// Set only one property of one cell which should not be copied to the
		// new grid.
		int idOfCell_1 = 5;
		originalGrid.getCell(idOfCell_1).setInvalidHighlight();

		Grid newGrid = originalGrid.createNewGridForReplay();

		assertThat(newGrid, is(notNullValue()));
		assertThat(newGrid.getCell(idOfCell_1),
				is(not(originalGrid.getCell(idOfCell_1))));
	}

	@Test
	public void createNewGridForReplay_HasSelectedCell_SelectedCellIsCleared()
			throws Exception {
		// Use a grid without mocks for this test.
		Grid originalGrid = GridCreator4x4.createEmptyGrid();
		// Set only one property of one cell which should not be copied to the
		// new grid.
		int idOfCell_1 = 5;
		originalGrid.getCell(idOfCell_1).setSelected(true);

		Grid newGrid = originalGrid.createNewGridForReplay();

		assertThat(newGrid, is(notNullValue()));
		assertThat(newGrid.getCell(idOfCell_1).isSelected(),
				is(not(originalGrid.getCell(idOfCell_1).isSelected())));
	}

	@Test
	public void createNewGridForReplay_RevealedCageOperator_RevealedCageOperatorIsHidden()
			throws Exception {
		// Use a grid without mocks for this test.
		GridCreator4x4HiddenOperators gridCreator = GridCreator4x4HiddenOperators
				.createEmpty();
		Grid originalGrid = gridCreator.getGrid();
		// Reveal the operator of a cage with an unrevealed cage operator. For
		// this a cell in such a cage has to be selected after which the cage
		// operator can be unrevealed. Deselect the cell again as after creating
		// the new grid, no cell is selected.
		int idOfCell_1 = gridCreator
				.getIdOfUpperLeftCellOfCageWithMultipleCellsAndAnUnrevealedCageOperator();
		Cell cell_1 = originalGrid.getCell(idOfCell_1);
		originalGrid.setSelectedCell(cell_1);
		originalGrid.revealOperatorSelectedCage();
		originalGrid.deselectSelectedCell();

		Grid newGrid = originalGrid.createNewGridForReplay();

		assertThat(newGrid, is(notNullValue()));
		assertThat(newGrid.getCell(idOfCell_1).getCageText(),
				is(not(originalGrid.getCell(idOfCell_1).getCageText())));
	}

	@Test
	public void markInvalidChoices_GridHasNoInvalidValues_GridStatisticsAreUpdated()
			throws Exception {
		Grid grid = mGridBuilderStub.build();

		assertThat(grid.markInvalidChoices(), is(0));

		verify(mGridStatisticsMock).increaseCounter(
				GridStatistics.StatisticsCounterType.ACTION_CHECK_PROGRESS);
	}

	@Test
	public void markInvalidChoices_GridHasOneCellWithInvalidValue_CellWithInvalidValueIsHighlighted()
			throws Exception {
		int cellId = 7;
		GridCreator testGridCreator = GridCreator4x4
				.createEmpty()
				.setIncorrectEnteredValueInCell(cellId);
		Grid grid = testGridCreator.getGrid();

		assertThat(grid.markInvalidChoices(), is(1));
		assertThat(grid.getCell(7).hasInvalidValueHighlight(), is(true));
	}

	@Test
	public void markInvalidChoices_GridHasTwoCellsWithInvalidValue_StatisticsOfInvalidCellsFoundUpdated()
			throws Exception {
		int cellId_1 = 7;
		int cellId_2 = 0;
		GridCreator testGridCreator = GridCreator4x4
				.createEmpty()
				.setIncorrectEnteredValueInCell(cellId_1)
				.setIncorrectEnteredValueInCell(cellId_2);
		Grid grid = testGridCreator.getGrid();

		assertThat(grid.markInvalidChoices(), is(2));
		assertThat(grid.getGridStatistics().mCheckProgressInvalidCellsFound,
				is(2));
	}

	@Test
	public void isReplay_GridStatisticsReplayCounterIs0_False()
			throws Exception {
		Grid grid = mGridBuilderStub.build();
		when(mGridStatisticsMock.getReplayCount()).thenReturn(0);

		assertThat(grid.isReplay(), is(false));
	}

	@Test
	public void isReplay_GridStatisticsReplayCounterIs1_True() throws Exception {
		Grid grid = mGridBuilderStub.build();
		when(mGridStatisticsMock.getReplayCount()).thenReturn(1);

		assertThat(grid.isReplay(), is(true));
	}

	@Test
	public void revealSelectedCell_CellIsSelected_UndoInformationForSelectedCellIsSaved()
			throws Exception {
		int idSelectedCell = 14;
		Grid grid = mGridBuilderStub
				.setCellMockAsSelectedCell(idSelectedCell)
				.build();

		assertThat(grid.revealSelectedCell(), is(true));
		assertThat(grid.countMoves(), is(1));
	}

	@Test
	public void revealSelectedCell_CellIsSelected_SelectedCellIsRevealed()
			throws Exception {
		int idSelectedCell = 14;
		Grid grid = mGridBuilderStub
				.setCellMockAsSelectedCell(idSelectedCell)
				.build();

		assertThat(grid.revealSelectedCell(), is(true));
		verify(mGridBuilderStub.mCellMockOfDefaultSetup[idSelectedCell])
				.revealCorrectValue();
	}

	@Test
	public void revealSelectedCell_CellIsSelected_SelectedCellContainsCorrectEnteredValue()
			throws Exception {
		int idSelectedCell = 14;
		Grid grid = mGridBuilderStub
				.setCellMockAsSelectedCell(idSelectedCell)
				.build();
		int correctValue = 3;
		when(
				mGridBuilderStub.mCellMockOfDefaultSetup[idSelectedCell]
						.getCorrectValue()).thenReturn(correctValue);

		assertThat(grid.revealSelectedCell(), is(true));
		verify(mGridBuilderStub.mCellMockOfDefaultSetup[idSelectedCell])
				.revealCorrectValue();
	}

	@Test
	public void revealSelectedCell_RevealedValueIsUsedAsPossibleInOtherCellOnSameRow_RedundantPossibleValueIsRemoved()
			throws Exception {
		// Set up a grid with a for which the user value of the selected is
		// revealed and for which the same value os also used as a possible
		// value in another cell on the same row as the selected cell.
		int idSelectedCell = 14;
		int rowSelectedCell = 3;
		int columnSelectedCell = 2;
		int idOtherCellOnSameRow = idSelectedCell - 1;
		int valueRevealed = 1;
		Grid grid = mGridBuilderStub
				.setCellMockAsSelectedCell(idSelectedCell)
				.setCellMockWithEnteredValue(idSelectedCell, rowSelectedCell,
						columnSelectedCell, valueRevealed)
				.setCellMockWithMaybeValues(idOtherCellOnSameRow,
						rowSelectedCell, columnSelectedCell - 1,
						new int[] { valueRevealed })
				.build();
		// Enable puzzle setting to clear maybes.
		when(mPreferencesMock.isPuzzleSettingClearMaybesEnabled()).thenReturn(
				true);

		// See unit tests for method clearRedundantPossiblesInSameRowOrColumn
		// for other scenario's. Just one scenario is tested for method
		// revealSelectedCell in order to test whether this method is invoked.
		assertThat(grid.revealSelectedCell(), is(true));
		verify(mGridBuilderStub.mCellMockOfDefaultSetup[idOtherCellOnSameRow])
				.removePossible(valueRevealed);
	}

	@Test
	public void revealSelectedCell_CellIsSelected_StatisticsUpdated()
			throws Exception {
		int idSelectedCell = 14;
		Grid grid = mGridBuilderStub
				.setCellMockAsSelectedCell(idSelectedCell)
				.build();

		assertThat(grid.revealSelectedCell(), is(true));
		verify(mGridBuilderStub.mGridStatistics).increaseCounter(
				GridStatistics.StatisticsCounterType.ACTION_REVEAL_CELL);
	}

	@Test
	public void revealSelectedCell_NoCellIsSelected_False() throws Exception {
		Grid grid = mGridBuilderStub.build();

		assertThat(grid.revealSelectedCell(), is(false));
	}

	@Test
	public void revealOperatorSelectedCage_CageSelectedForCageWithHiddenOperator_OperatorRevealed()
			throws Exception {
		int idSelectedCell = 6;
		GridCreator gridCreator = GridCreator4x4HiddenOperators
				.createEmptyWithSelectedCell(idSelectedCell);
		int idOfUpperLeftCellOfSelectedCage = gridCreator
				.getIdTopLeftCellOfCageContainingCellWithId(idSelectedCell);
		Grid grid = gridCreator.getGrid();
		String cageTextBeforeReveal = grid.getCell(
				idOfUpperLeftCellOfSelectedCage).getCageText();

		assertThat(grid.revealOperatorSelectedCage(), is(true));
		assertThat(grid.getCell(idOfUpperLeftCellOfSelectedCage).getCageText(),
				is(not(cageTextBeforeReveal)));
	}

	@Test
	public void revealOperatorSelectedCage_CageSelectedForCageWithVisibleOperator_OperatorRevealed()
			throws Exception {
		int idOfSelectedCell = 14;
		Grid grid = mGridBuilderStub
				.setCellMockAsSelectedCell(idOfSelectedCell)
				.build();
		when(mGridBuilderStub.mAnyCageOfDefaultSetup.isOperatorHidden())
				.thenReturn(false);

		assertThat(grid.revealOperatorSelectedCage(), is(false));
		verify(mGridBuilderStub.mAnyCageOfDefaultSetup, never())
				.revealOperator();
	}

	@Test
	public void revealOperatorSelectedCage_NoCageSelectedForCageWithVisibleOperator_OperatorRevealed()
			throws Exception {
		Grid grid = mGridBuilderStub.build();

		assertThat(grid.revealOperatorSelectedCage(), is(false));
		verify(mGridBuilderStub.mAnyCageOfDefaultSetup, never())
				.revealOperator();
	}

	@Test
	public void getEnteredValuesForCells_CellsIsNull_EmptyListReturned()
			throws Exception {
		Grid grid = mGridBuilderStub.build();

		assertThat(grid.getEnteredValuesForCells(null).size(), is(0));
	}

	@Test
	public void getEnteredValuesForCells_CellWithInvalidId_EmptyListReturned()
			throws Exception {
		int idOfCell = -1; // Value should not be in range 0 to gridsize *
							// gridsize
		Grid grid = mGridBuilderStub.build();

		assertThat(
				grid.getEnteredValuesForCells(new int[] { idOfCell }).size(),
				is(0));
	}

	@Test
	public void getEnteredValuesForCells_GetValueForOneCellHavingAEnteredValue_EnteredValueReturned()
			throws Exception {
		int idOfCell = 4;
		int valueOfCell = 3;
		when(
				mGridBuilderStub.mCellMockOfDefaultSetup[idOfCell]
						.hasEnteredValue()).thenReturn(true);
		when(
				mGridBuilderStub.mCellMockOfDefaultSetup[idOfCell]
						.getEnteredValue()).thenReturn(valueOfCell);
		Grid grid = mGridBuilderStub.build();

		List<Integer> expectedEnteredValues = new ArrayList<Integer>();
		expectedEnteredValues.add(valueOfCell);
		assertThat(grid.getEnteredValuesForCells(new int[] { idOfCell }),
				is(expectedEnteredValues));
	}

	@Test
	public void getEnteredValuesForCells_GetValueForOneCellNotHavingAEnteredValue_NoEnteredValuesReturned()
			throws Exception {
		int idOfCell = 4;
		when(
				mGridBuilderStub.mCellMockOfDefaultSetup[idOfCell]
						.hasEnteredValue()).thenReturn(false);
		Grid grid = mGridBuilderStub.build();

		List<Integer> expectedEnteredValues = new ArrayList<Integer>();
		assertThat(grid.getEnteredValuesForCells(new int[] { idOfCell }),
				is(expectedEnteredValues));
	}

	@Test
	public void getEnteredValuesForCells_GetValueForMultipleCellsAllHavingAEnteredValue_EnteredValuesReturned()
			throws Exception {
		int idOfCell_1 = 4;
		int valueOfCell_1 = 3;
		when(
				mGridBuilderStub.mCellMockOfDefaultSetup[idOfCell_1]
						.hasEnteredValue()).thenReturn(true);
		when(
				mGridBuilderStub.mCellMockOfDefaultSetup[idOfCell_1]
						.getEnteredValue()).thenReturn(valueOfCell_1);
		int idOfCell_2 = 8;
		int valueOfCell_2 = 2;
		when(
				mGridBuilderStub.mCellMockOfDefaultSetup[idOfCell_2]
						.hasEnteredValue()).thenReturn(true);
		when(
				mGridBuilderStub.mCellMockOfDefaultSetup[idOfCell_2]
						.getEnteredValue()).thenReturn(valueOfCell_2);
		Grid grid = mGridBuilderStub.build();

		List<Integer> expectedEnteredValues = new ArrayList<Integer>();
		expectedEnteredValues.add(valueOfCell_1);
		expectedEnteredValues.add(valueOfCell_2);
		assertThat(
				grid.getEnteredValuesForCells(new int[] { idOfCell_1,
						idOfCell_2 }), is(expectedEnteredValues));
	}

	@Test
	public void getEnteredValuesForCells_GetValueForMultipleCellsNotAllHavingAEnteredValue_EnteredValuesReturned()
			throws Exception {
		int idOfCell_1 = 4;
		int valueOfCell_1 = 3;
		when(
				mGridBuilderStub.mCellMockOfDefaultSetup[idOfCell_1]
						.hasEnteredValue()).thenReturn(true);
		when(
				mGridBuilderStub.mCellMockOfDefaultSetup[idOfCell_1]
						.getEnteredValue()).thenReturn(valueOfCell_1);
		int idOfCell_2 = 8;
		int valueOfCell_2 = 2;
		when(
				mGridBuilderStub.mCellMockOfDefaultSetup[idOfCell_2]
						.hasEnteredValue()).thenReturn(true);
		when(
				mGridBuilderStub.mCellMockOfDefaultSetup[idOfCell_2]
						.getEnteredValue()).thenReturn(valueOfCell_2);
		int idOfCell_3 = 9;
		when(
				mGridBuilderStub.mCellMockOfDefaultSetup[idOfCell_3]
						.hasEnteredValue()).thenReturn(false);
		Grid grid = mGridBuilderStub.build();

		List<Integer> expectedEnteredValues = new ArrayList<Integer>();
		expectedEnteredValues.add(valueOfCell_1);
		expectedEnteredValues.add(valueOfCell_2);
		assertThat(
				grid.getEnteredValuesForCells(new int[] { idOfCell_1,
						idOfCell_2, idOfCell_3 }), is(expectedEnteredValues));
	}

	@Test
	public void invalidateBordersOfAllCells_MultipleCells_BorderAreSet()
			throws Exception {
		Grid grid = mGridBuilderStub.useSameMockForAllCells().build();

		grid.invalidateBordersOfAllCells();

		verify(mGridBuilderStub.mAnyCellMockOfDefaultSetup).invalidateBorders();
	}

	@Test
	public void getCells_CellsIsNull_EmptyListReturned() throws Exception {
		Grid grid = mGridBuilderStub.build();

		assertThat(grid.getCells(null).size(), is(0));
	}

	@Test
	public void getCells_InvalidCellId_EmptyListReturned() throws Exception {
		int idOfCell = -1; // Value should not be in range 0 to gridsize *
							// gridsize
		Grid grid = mGridBuilderStub.build();

		assertThat(grid.getCells(new int[] { idOfCell }).size(), is(0));
	}

	@Test
	public void getCells_GetSingleCell_CellReturned() throws Exception {
		int idOfCell = 4;
		Grid grid = mGridBuilderStub.build();

		List<Cell> expectedCells = new ArrayList<Cell>();
		expectedCells.add(mGridBuilderStub.mCellMockOfDefaultSetup[idOfCell]);
		assertThat(grid.getCells(new int[] { idOfCell }), is(expectedCells));
	}

	@Test
	public void getCells_GetMultipleCells_CellsReturned() throws Exception {
		int idOfCell_1 = 4;
		int idOfCell_2 = 8;
		Grid grid = mGridBuilderStub.build();

		List<Cell> expectedCells = new ArrayList<Cell>();
		expectedCells.add(mGridBuilderStub.mCellMockOfDefaultSetup[idOfCell_1]);
		expectedCells.add(mGridBuilderStub.mCellMockOfDefaultSetup[idOfCell_2]);
		assertThat(grid.getCells(new int[] { idOfCell_1, idOfCell_2 }),
				is(expectedCells));
	}

	@Test
	public void markDuplicateValues_EmptyGrid_NoDuplicatesFounds()
			throws Exception {
		Grid grid = mGridBuilderStub.build();

		assertThat(grid.markDuplicateValues(), is(false));
	}

	@Test
	public void markDuplicateValues_GridWithEnteredValuesButNoDuplicates_NoDuplicatesFounds()
			throws Exception {
		Grid grid = GridCreator4x4
				.create()
				.setCorrectEnteredValueToAllCells()
				.getGrid();

		assertThat(grid.markDuplicateValues(), is(false));
	}

	@Test
	public void markDuplicateValues_GridWithDuplicateEnteredValuesOnSameRow_DuplicatesFounds()
			throws Exception {
		Grid grid = GridCreator4x4.create().setEmptyGrid().getGrid();
		int row = 1;
		int col1 = 2;
		int col2 = col1 + 1;
		int enteredValue = 2;
		grid.getCellAt(row, col1).setEnteredValue(enteredValue);
		grid.getCellAt(row, col2).setEnteredValue(enteredValue);

		assertThat(grid.markDuplicateValues(), is(true));
	}

	@Test
	public void markDuplicateValues_GridWithDuplicateEnteredValuesInSameColumn_DuplicatesFounds()
			throws Exception {
		Grid grid = GridCreator4x4.create().setEmptyGrid().getGrid();
		int row1 = 1;
		int row2 = row1 + 1;
		int col = 2;
		int enteredValue = 2;
		grid.getCellAt(row1, col).setEnteredValue(enteredValue);
		grid.getCellAt(row2, col).setEnteredValue(enteredValue);

		assertThat(grid.markDuplicateValues(), is(true));
	}
}
