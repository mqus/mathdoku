package net.mathdoku.plus.gridGenerating;

import java.util.ArrayList;
import java.util.Random;

import android.util.Log;

public class CageTypeGenerator {
	private static final String TAG = "MathDoku.CageTypeGenerator";

	/**
	 * Size of largest cages to be generated. Be careful with adjusting this
	 * value as the number of cage types growth exponentially. Generating ALL
	 * cage types for a given size can take long.
	 * 
	 * <pre>
	 * 	MaxSize    #cage_types     #cumulative
	 *        1              1               1
	 *        2              2               3 
	 *        3              6               6
	 *        4             19              29
	 *        5             63              92
	 *        6            216             308
	 *        7            760           1,068
	 *        8          2,725           3,793
	 *        9          9,910          13,703
	 * </pre>
	 */
	public static final int MAX_CAGE_SIZE = 5;

	// Singleton reference for the cage type generator.
	private static CageTypeGenerator mCageTypeGeneratorSingletonInstance = null;

	// All available cage types.
	private GridCageType mSingleCellCageType;
	private ArrayList<GridCageType>[] mCageTypes;

	/**
	 * Get an instance to the singleton instance of the cage type generator.
	 * 
	 * The {@link CageTypeGenerator} containing cage types having minimum 1 and
	 * maximum {{@value #MAX_CAGE_SIZE} cells. Cages of bigger size have to
	 * generated with { {@link #getRandomCageType(int, Random)}.
	 * 
	 * @return The singleton instance for the cage type generator.
	 */
	public static CageTypeGenerator getInstance() {
		if (mCageTypeGeneratorSingletonInstance == null) {
			mCageTypeGeneratorSingletonInstance = new CageTypeGenerator();
		}
		return mCageTypeGeneratorSingletonInstance;
	}

	/**
	 * Creates a new instance of {@link CageTypeGenerator} containing cage types
	 * having minimum 1 and maximum {{@value #MAX_CAGE_SIZE} cells. Cages of
	 * bigger size have to generated with {
	 * {@link #getRandomCageType(int, Random)}.
	 */
	private CageTypeGenerator() {
		// Initialize all cage type array lists
		mCageTypes = new ArrayList[MAX_CAGE_SIZE];
		for (int i = 0; i < MAX_CAGE_SIZE; i++) {
			mCageTypes[i] = new ArrayList<GridCageType>();
		}

		// Start with a cage consisting of a single cell.
		boolean singleCageTypeMatrix[][] = new boolean[1][1];
		singleCageTypeMatrix[0][0] = true;
		if (addCageTypeIfNotExists(singleCageTypeMatrix)) {
			mSingleCellCageType = mCageTypes[0].get(0);
		}

		// The other cage types (size n) can be generated by adding one
		// additional cell to any cage having one cell less (size n-1).
		for (int smallerCageSize = 1; smallerCageSize < MAX_CAGE_SIZE; smallerCageSize++) {
			for (GridCageType cageType : mCageTypes[smallerCageSize - 1]) {
				// Add a cell to each existing cage type having just one cell
				// less than size of the grid type that we need to be create
				// now.
				boolean newCageTypeMatrix[][] = cageType
						.getExtendedCageTypeMatrix();
				for (int row = 0; row < newCageTypeMatrix.length; row++) {
					for (int col = 0; col < newCageTypeMatrix[row].length; col++) {
						if (newCageTypeMatrix[row][col]) {
							// This cell was already occupied in the
							// original grid. Fill each free cell above,
							// right, below or left to create a new cage
							// type.

							// Because the ExtendedCageType has a free
							// border of emtpy cells around the cage there
							// is no need to check indexes.

							// Check above current occupied cell
							if (!newCageTypeMatrix[row - 1][col]) {
								newCageTypeMatrix[row - 1][col] = true;
								addCageTypeIfNotExists(newCageTypeMatrix);
								newCageTypeMatrix[row - 1][col] = false;
							}

							// Check to right of current occupied cell
							if (!newCageTypeMatrix[row][col + 1]) {
								newCageTypeMatrix[row][col + 1] = true;
								addCageTypeIfNotExists(newCageTypeMatrix);
								newCageTypeMatrix[row][col + 1] = false;
							}
							// Check below current occupied cell
							if (!newCageTypeMatrix[row + 1][col]) {
								newCageTypeMatrix[row + 1][col] = true;
								addCageTypeIfNotExists(newCageTypeMatrix);
								newCageTypeMatrix[row + 1][col] = false;
							}

							// Check to left of current occupied cell
							if (!newCageTypeMatrix[row][col - 1]) {
								newCageTypeMatrix[row][col - 1] = true;
								addCageTypeIfNotExists(newCageTypeMatrix);
								newCageTypeMatrix[row][col - 1] = false;
							}
						}
					}
				}
			}
		}

		// Report number of cage types found
		if (GridGenerator.DEBUG_GRID_GENERATOR_FULL) {
			for (int i = 0; i < mCageTypes.length; i++) {
				if (mCageTypes[i] != null) {
					Log.i(TAG, "Number of cage type with " + (i + 1)
							+ " cells: " + mCageTypes[i].size());
				}
			}
		}
	}

	/**
	 * Adds a new cage type to the list of cage types in case it is not yet on
	 * this list.
	 * 
	 * @param newCageTypeMatrix
	 *            The cage type matrix which has to be added to the list of
	 *            available cage types.
	 * @return True in case the cage type is added. False otherwise.
	 */
	private boolean addCageTypeIfNotExists(boolean[][] newCageTypeMatrix) {
		GridCageType newPossibleCageType = new GridCageType();
		newPossibleCageType.setMatrix(newCageTypeMatrix);

		int sizeCageType = newPossibleCageType.cellsUsed();

		// Check if this cage type was not yet defined.
		if (mCageTypes[sizeCageType - 1].contains(newPossibleCageType)) {
			return false;
		}

		// This cage type does not yet exist.
		mCageTypes[sizeCageType - 1].add(newPossibleCageType);

		if (GridGenerator.DEBUG_GRID_GENERATOR_FULL) {
			Log.i(TAG,
					"Found a new cage type:\n" + newPossibleCageType.toString());
		}

		return true;
	}

	/**
	 * Gets the cage type for a single cell.
	 * 
	 * @return Cage type representing a single cell.
	 */
	public GridCageType getSingleCellCageType() {
		return this.mSingleCellCageType;
	}

	/**
	 * Get the cage type with given index.
	 * 
	 * @param cageTypeIndex
	 *            The index of the cage type to be retrieved.
	 * 
	 * @return The cage type which was requested. Null in case of an error.
	 */
	public GridCageType getCageType(int cageTypeIndex) {
		// Find cage type array in which the cage type is defined.
		for (int i = 0; i < mCageTypes.length; i++) {
			if (mCageTypes[i] != null) {
				// This cage type array does contain elements. Check if it
				// contains enough elements to find the given index.
				if (cageTypeIndex < mCageTypes[i].size()) {
					return mCageTypes[i].get(cageTypeIndex);
				}

				// Descrease index for search in next array.
				cageTypeIndex -= mCageTypes[i].size();
			}
		}

		// Index not found
		return null;
	}

	/**
	 * Get the number of generated cage types for the given size.
	 * 
	 * @return the number of available cage types for the given size.
	 */
	public int size(int maxCageSize) {
		int totalCageTypes = 0;
		maxCageSize = (maxCageSize > MAX_CAGE_SIZE ? MAX_CAGE_SIZE
				: maxCageSize);
		for (int i = 0; i < maxCageSize; i++) {
			totalCageTypes += mCageTypes[i].size();
		}
		return totalCageTypes;
	}

	/**
	 * Get a random cage type of the requested size. It is possible to retrieve
	 * a cage type of a bigger size than is pre-generated.
	 * 
	 * @param cellsUsed
	 *            The number of cells the cage consists of.
	 * @param maxWidth
	 *            The maximum width 0f the cell. Use 0 in case width does not
	 *            matter.
	 * @param maxHeight
	 *            The maximum height 0f the cell. Use 0 in case width does not
	 *            matter.
	 * @param random
	 *            The random generator to use for randomized decisions.
	 * @return A cage type.
	 */
	public GridCageType getRandomCageType(int cellsUsed, int maxWidth,
			int maxHeight, Random random) {
		assert (cellsUsed > 0);
		assert (maxWidth >= 0);
		assert (maxHeight >= 0);
		if (maxWidth > 0 && maxHeight > 0 && maxWidth * maxHeight < cellsUsed) {
			// No such grid cage type can exist as more cells are requested than
			// the maximum space can contain.
			return null;
		}

		// Determine which cageTypeArray to use
		ArrayList<GridCageType> gridCageTypes = (cellsUsed - 1 < mCageTypes.length ? mCageTypes[cellsUsed - 1]
				: mCageTypes[mCageTypes.length - 1]);
		assert (gridCageTypes != null);

		// Choose a random cage type in the array which fits within the given
		// maximum dimensions
		GridCageType gridCageType;
		do {
			gridCageType = gridCageTypes.get(random.nextInt(gridCageTypes
					.size()));
		} while ((maxWidth > 0 && gridCageType.getWidth() > maxWidth)
				|| (maxHeight > 0 && gridCageType.getHeight() > maxHeight));

		if (cellsUsed - 1 < mCageTypes.length || gridCageType == null) {
			// Return the already defined grid cage type.
			return gridCageType;
		} else {
			// A cage type of this size does not yet exist. Use the selected
			// cage type to generate a cage type of the requested size.
			return deriveNewCageType(gridCageType,
					cellsUsed - gridCageType.cellsUsed(), maxWidth, maxHeight,
					random);
		}
	}

	/**
	 * Derives a new cage type by adding the given number of cells to the given
	 * base cage type. The new cells will be placed adjacent to an already
	 * occupied cell. The created cage type will not be added to the list of
	 * cage types.
	 * 
	 * @param cageType
	 *            The cage type to be used as base to generate a new cage type.
	 * @param extendWithCells
	 *            The number of cells with which the given cage type will be
	 *            extended.
	 * @param maxWidth
	 *            The maximum width 0f the cell. Use 0 in case width does not
	 *            matter.
	 * @param maxHeight
	 *            The maximum height 0f the cell. Use 0 in case width does not
	 *            matter.
	 * @param random
	 *            The randomize which should be used for random decisions.
	 * @return The new cage type.
	 */
	private GridCageType deriveNewCageType(GridCageType cageType,
			int extendWithCells, int maxWidth, int maxHeight, Random random) {
		boolean newCageTypeMatrix[][] = cageType.getExtendedCageTypeMatrix();

		// Determine which used cells can be extended.
		int numberOfCellsPerRow = newCageTypeMatrix[0].length;
		ArrayList<Integer> extendIndexes = new ArrayList<Integer>();
		for (int row = 0; row < newCageTypeMatrix.length; row++) {
			for (int col = 0; col < newCageTypeMatrix[row].length; col++) {
				if (newCageTypeMatrix[row][col]) {
					// This cell was already occupied in the
					// original grid. Check which cells can be extended.

					// Check above and below current occupied cell in case the
					// base cage type has not yet reached its maximum height.
					if (cageType.getHeight() < maxHeight) {
						// Check above
						if (!newCageTypeMatrix[row - 1][col]) {
							int cellnumber = ((row - 1) * numberOfCellsPerRow)
									+ col;
							if (!extendIndexes.contains(cellnumber)) {
								extendIndexes.add(cellnumber);
							}
						}
						// Check below
						if (cageType.getHeight() < maxHeight
								&& !newCageTypeMatrix[row + 1][col]) {
							int cellnumber = ((row + 1) * numberOfCellsPerRow)
									+ col;
							if (!extendIndexes.contains(cellnumber)) {
								extendIndexes.add(cellnumber);
							}
						}
					}

					// Check to right and left of current occupied cell in case
					// the base cage type has not yet reached its maximum width.
					if (cageType.getWidth() < maxWidth) {
						// Check to right
						if (!newCageTypeMatrix[row][col + 1]) {
							int cellnumber = (row * numberOfCellsPerRow)
									+ (col + 1);
							if (!extendIndexes.contains(cellnumber)) {
								extendIndexes.add(cellnumber);
							}
						}

						// Check to left
						if (!newCageTypeMatrix[row][col - 1]) {
							int cellnumber = (row * numberOfCellsPerRow)
									+ (col - 1);
							if (!extendIndexes.contains(cellnumber)) {
								extendIndexes.add(cellnumber);
							}
						}
					}
				}
			}
		}

		// Select one of the cell index numbers to which the given cage type can
		// be extended
		int extendToCellnumber = extendIndexes.get(random.nextInt(extendIndexes
				.size()));
		int col = extendToCellnumber % numberOfCellsPerRow;
		int row = extendToCellnumber / numberOfCellsPerRow;
		newCageTypeMatrix[row][col] = true;

		GridCageType newCageType = new GridCageType();
		newCageType.setMatrix(newCageTypeMatrix);

		if (extendWithCells == 1) {
			return newCageType;
		} else {
			// An extra level of extension is needed.
			return deriveNewCageType(newCageType, extendWithCells - 1,
					maxWidth, maxHeight, random);
		}
	}
}