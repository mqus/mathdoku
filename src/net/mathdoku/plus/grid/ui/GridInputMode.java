package net.mathdoku.plus.grid.ui;

// The input mode in which the puzzle can be.
public enum GridInputMode {
	NORMAL, // Digits entered are handled as a new cell value
	MAYBE, // Digits entered are handled to toggle the possible value on/of
	COPY, // Digits are copied from another cell
}
