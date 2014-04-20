package net.mathdoku.plus.gridgenerating;

import android.app.Activity;

import net.mathdoku.plus.puzzle.grid.Grid;
import net.mathdoku.plus.storage.database.DatabaseHelper;
import net.mathdoku.plus.util.Util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import robolectric.RobolectricGradleTestRunner;
import testHelper.GridCreator;
import testHelper.GridCreator2x2;
import testHelper.GridCreator4x4;
import testHelper.GridCreator4x4HiddenOperators;
import testHelper.GridCreator5x5;
import testHelper.GridCreator9x9;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * NOTE: the first test which is executed will take significant longer than the
 * other tests as the database structure will be created during this test.
 */

@RunWith(RobolectricGradleTestRunner.class)
public class GridGeneratorTest {
	GridGeneratorListener gridGeneratorListener;

	private static class GridGeneratorListener implements
			GridGenerator.Listener {
		@Override
		public boolean isCancelled() {
			return false;
		}

		@Override
		public void updateProgressHighLevel(String text) {
			// Swallow
		}

		@Override
		public void updateProgressDetailLevel(String text) {
			// Swallow
		}

		@Override
		public void signalSlowGridGeneration() {
			// Swallow
		}
	}

	@Before
	public void setup() {
		Activity activity = new Activity();
		new Util(activity);
		DatabaseHelper.getInstance(activity);

		gridGeneratorListener = new GridGeneratorListener();
	}

	@After
	public void tearDown() {
		// Close the database helper. This ensure that the next test will use a
		// new DatabaseHelper instance with a new SQLite database connection. In
		// this way it is possible to generate the same grid multiple times in
		// one test cyclus. Without this tear down, a grid can only be generated
		// once because the grid generator checks whether the generated grid
		// definition is unique.
		DatabaseHelper.getInstance().close();
	}

	@Test
	public void createGrid_Grid2x2_RegeneratedGridHasSameGridDefinition()
			throws Exception {
		assertRegenerateGrid(GridCreator2x2.createEmpty());
	}

	@Test
	public void createGrid_Grid4x4_RegeneratedGridHasSameGridDefinition()
			throws Exception {
		assertRegenerateGrid(GridCreator4x4.createEmpty());
	}

	@Test
	public void createGrid_Grid4x4HiddenOperators_RegeneratedGridHasSameGridDefinition()
			throws Exception {
		assertRegenerateGrid(GridCreator4x4HiddenOperators.createEmpty());
	}

	@Test
	public void createGrid_Grid5x5_RegeneratedGridHasSameGridDefinition()
			throws Exception {
		assertRegenerateGrid(GridCreator5x5.createEmpty());
	}

	@Test
	public void createGrid_Grid9x9_RegeneratedGridHasSameGridDefinition()
			throws Exception {
		assertRegenerateGrid(GridCreator9x9.createEmpty());
	}

	private void assertRegenerateGrid(GridCreator gridCreator) {
		assertThat(
				"This GridCreator subclass should not be used to test whether the grid can be regenerated.",
				gridCreator.canBeRegenerated(), is(true));

		Grid generatedGrid = new GridGenerator(gridGeneratorListener)
				.createGrid(gridCreator.getGridGeneratingParameters());
		assertThat(generatedGrid.getGridGeneratingParameters(),
				is(gridCreator.getGridGeneratingParameters()));
		assertThat(
				"This grid cannot be regenerated. However first check if this is\n"
						+ "caused by any of following:\n"
						+ " 1. A change in the grid generating algorithm, If so, first check some puzzles manually.\n"
						+ "    Then regenerate the GridCreator test helper class which fails.\n"
						+ " 2. If a newly generated GridCreator test helper class fails on the first run of the\n"
						+ "    unit tests, than this is most likely caused by generating the code based on a puzzle\n"
						+ "    which was generated on a non empty database in which the exact same puzzle was\n"
						+ "    already generated before. If suc a thing happens when generating a puzzle with the\n"
						+ "    the (real) app, another puzzle will be generated automatically. This however will\n"
						+ "    never occur when running the unit tetst as this will be executed on an empty database\n"
						+ "    by default\n", generatedGrid.getDefinition(),
				is(gridCreator.getGrid().getDefinition()));
	}
}
