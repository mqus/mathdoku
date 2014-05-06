package net.mathdoku.plus.storage.selector;

import android.app.Activity;

import net.mathdoku.plus.enums.GridTypeFilter;
import net.mathdoku.plus.enums.SolvingAttemptStatus;
import net.mathdoku.plus.puzzle.grid.Grid;
import net.mathdoku.plus.storage.databaseadapter.DatabaseHelper;
import net.mathdoku.plus.storage.databaseadapter.GridDatabaseAdapter;
import net.mathdoku.plus.util.Util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import robolectric.RobolectricGradleTestRunner;
import testHelper.GridCreator4x4;
import testHelper.GridCreator4x4HiddenOperators;
import testHelper.GridCreator5x5;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class AvailableStatusFiltersSelectorTest {
	private static List<GridDatabaseAdapter.StatusFilter> statusFilterListAllSizes;
	private static List<GridDatabaseAdapter.StatusFilter> statusFilterListWithSize4;
	private static List<GridDatabaseAdapter.StatusFilter> statusFilterListWithSize5;

	@Before
	public void setup() {
		Activity activity = new Activity();
		new Util(activity);
		DatabaseHelper.getInstance(activity);

		statusFilterListAllSizes = createAndInitializeNewStatusFilterList();
		statusFilterListWithSize4 = createAndInitializeNewStatusFilterList();
		statusFilterListWithSize5 = createAndInitializeNewStatusFilterList();
		createAndSaveGrid(GridCreator4x4
				.create()
				.setCorrectEnteredValueToAllCells()
				.getGrid());
		createAndSaveGrid(GridCreator5x5
				.create()
				.setCorrectEnteredValueToAllCells()
				.getGrid());
		createAndSaveGrid(GridCreator4x4HiddenOperators
				.create()
				.setEmptyGrid()
				.getGrid());
	}

	private List<GridDatabaseAdapter.StatusFilter> createAndInitializeNewStatusFilterList() {
		List<GridDatabaseAdapter.StatusFilter> statusFilterList = new ArrayList<GridDatabaseAdapter.StatusFilter>();

		statusFilterList.add(GridDatabaseAdapter.StatusFilter.ALL);

		return statusFilterList;
	}

	@After
	public void tearDown() {
		// Close the database helper. This ensure that the next test will use a
		// new DatabaseHelper instance with a new SQLite database connection.
		DatabaseHelper.getInstance().close();
	}

	private void createAndSaveGrid(Grid grid) {
		assertThat(grid.save(), is(true));

		SolvingAttemptStatus solvingAttemptStatus = SolvingAttemptStatus
				.getDerivedStatus(grid.isSolutionRevealed(), grid.isActive(),
						grid.isEmpty());

		GridDatabaseAdapter.StatusFilter statusFilter = solvingAttemptStatus
				.getAttachedToStatusFilter();
		if (!statusFilterListAllSizes.contains(statusFilter)) {
			statusFilterListAllSizes.add(statusFilter);
		}
		if (grid.getGridSize() == 4) {
			statusFilterListWithSize4.add(statusFilter);
		}
		if (grid.getGridSize() == 5) {
			statusFilterListWithSize5.add(statusFilter);
		}
	}

	@Test
	public void getAvailableStatusFilters_GridAllSizes() throws Exception {
		assertThatStatusFilterList(GridTypeFilter.ALL, statusFilterListAllSizes);
	}

	private void assertThatStatusFilterList(GridTypeFilter gridTypeFilter,
			List<GridDatabaseAdapter.StatusFilter> expectedStatusFilterList) {
		List<GridDatabaseAdapter.StatusFilter> resultStatusFilterList = new AvailableStatusFiltersSelector(
				gridTypeFilter).getAvailableStatusFilters();
		Collections.sort(resultStatusFilterList);
		Collections.sort(expectedStatusFilterList);
		assertThat(resultStatusFilterList, is(expectedStatusFilterList));
	}

	@Test
	public void getAvailableStatusFilters_GridSize4() throws Exception {
		assertThatStatusFilterList(GridTypeFilter.GRID_4X4,
				statusFilterListWithSize4);
	}

	@Test
	public void getAvailableStatusFilters_GridSize5() throws Exception {
		assertThatStatusFilterList(GridTypeFilter.GRID_5X5,
				statusFilterListWithSize5);
	}
}
