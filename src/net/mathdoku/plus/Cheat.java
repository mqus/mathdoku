package net.mathdoku.plus;

import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.config.Config.AppMode;
import android.content.Context;
import android.content.res.Resources;

public class Cheat {

	public enum CheatType {
		CELL_REVEALED, OPERATOR_REVEALED, SOLUTION_REVEALED, CHECK_PROGRESS_USED
	}

	private final Resources mResources;

	// Constants to convert milisecond to calendar units
	private final long MILIS_PER_DAY = 24 * 60 * 60 * 1000;
	private final long MILIS_PER_HOUR = 60 * 60 * 1000;
	private final long MILIS_PER_MINUTE = 60 * 1000;
	private final long MILIS_PER_SECOND = 1000;

	// The type of cheat
	private final CheatType mCheatType;

	// The penalty consists of a base penalty and an optionally a penalty per
	// occurrence of a certain condition relevant for the cheat.
	private final int mConditionalOccurrences;

	// Penalty time in mili seconds
	private final long mPenaltyTimeMilisBase;
	private final long mPenaltyTimeMilisPerOccurrence;

	// Title and text to be used in tip dialogs.
	private String mTipTitle;
	private String mTipText;

	/**
	 * Creates a new instance of {@link Cheat} which only consist of a base
	 * penalty.
	 * 
	 * @param context
	 *            The context in which the cheat is created.
	 * @param cheatType
	 *            The type of cheat to be created.
	 */
	public Cheat(Context context, CheatType cheatType) {
		mResources = context.getResources();
		mCheatType = cheatType;
		mPenaltyTimeMilisPerOccurrence = 0;
		mConditionalOccurrences = 0;
		switch (mCheatType) {
		case CELL_REVEALED:
			mPenaltyTimeMilisBase = 60 * MILIS_PER_SECOND;
			mTipTitle = mResources
					.getString(R.string.dialog_tip_cheat_reveal_value_title);
			mTipText = mResources.getString(
					R.string.dialog_tip_cheat_reveal_value_text,
					getPenaltyTimeText(mPenaltyTimeMilisBase));
			break;
		case OPERATOR_REVEALED:
			mPenaltyTimeMilisBase = 30 * MILIS_PER_SECOND;
			mTipTitle = mResources
					.getString(R.string.dialog_tip_cheat_reveal_operator_title);
			mTipText = mResources.getString(
					R.string.dialog_tip_cheat_reveal_operator_text,
					getPenaltyTimeText(mPenaltyTimeMilisBase));
			break;
		case SOLUTION_REVEALED:
			mPenaltyTimeMilisBase = MILIS_PER_DAY;
			mTipTitle = mResources
					.getString(R.string.dialog_tip_cheat_reveal_solution_title);
			mTipText = mResources.getString(
					R.string.dialog_tip_cheat_reveal_solution_text,
					getPenaltyTimeText(mPenaltyTimeMilisBase));
			break;
		default:
			mPenaltyTimeMilisBase = 0;
			mTipTitle = "";
			mTipText = "";
			if (Config.mAppMode == AppMode.DEVELOPMENT) {
				throw new RuntimeException(
						"Invalid value for parameter cheatType used in call to method Cheat(Context, CheatType).");
			}
			break;
		}
	}

	/**
	 * Creates a new instance of {@link Cheat} which consist of a base and a
	 * conditional penalty.
	 * 
	 * @param context
	 *            The context in which the cheat is created.
	 * @param occurrencesConditionalPenalty
	 *            The number of occurrences of the conditional penalty.
	 * @param cheatType
	 *            The type of cheat to be created.
	 */
	@SuppressWarnings("SameParameterValue")
	public Cheat(Context context, CheatType cheatType,
			int occurrencesConditionalPenalty) {
		mResources = context.getResources();
		mCheatType = cheatType;

		switch (mCheatType) {
		case CHECK_PROGRESS_USED:
			mPenaltyTimeMilisBase = 20 * MILIS_PER_SECOND;
			mPenaltyTimeMilisPerOccurrence = 15 * MILIS_PER_SECOND;
			mConditionalOccurrences = occurrencesConditionalPenalty;
			mTipTitle = mResources
					.getString(R.string.dialog_tip_cheat_check_progress_title);
			mTipText = mResources.getString(
					R.string.dialog_tip_cheat_check_progress_text,
					getPenaltyTimeText(mPenaltyTimeMilisBase),
					getPenaltyTimeText(mPenaltyTimeMilisPerOccurrence));
			break;
		default:
			mPenaltyTimeMilisBase = 0;
			mPenaltyTimeMilisPerOccurrence = 0;
			mConditionalOccurrences = 0;
			if (Config.mAppMode == AppMode.DEVELOPMENT) {
				throw new RuntimeException(
						"Invalid value for parameter cheatType used in call to method Cheat(Context, CheatType).");
			}
			break;
		}
	}

	/**
	 * Get the penalty time for this cheat.
	 * 
	 * @return The penalty time in mili seconds for this cheat.
	 */
	public long getPenaltyTimeMilis() {
		return mPenaltyTimeMilisBase
				+ (mConditionalOccurrences * mPenaltyTimeMilisPerOccurrence);
	}

	/**
	 * Get the title to be displayed in the tip cheat dialog.
	 * 
	 * @return The title to be displayed in the tip cheat dialog.
	 */
	public String getTipTitle() {
		return mTipTitle;
	}

	/**
	 * Get the text to be displayed in the tip cheat dialog.
	 * 
	 * @return The text to be displayed in the tip cheat dialog.
	 */
	public String getTipText() {
		return mTipText;
	}

	/**
	 * Get the type of the cheat.
	 * 
	 * @return The type of the cheat.
	 */
	public CheatType getType() {
		return mCheatType;
	}

	/**
	 * Converts a given penalty time from mili seconds to a formatted text
	 * string.
	 * 
	 * @param penaltyTime
	 *            The penalty time in mili seconds.
	 * @return A formatted text.
	 */
	private String getPenaltyTimeText(long penaltyTime) {
		String penaltyTimeText = "";
		String and = " "
				+ mResources.getString(R.string.connector_last_two_elements)
				+ " ";

		// Determine number of days
		long days = penaltyTime / MILIS_PER_DAY;
		if (days > 1) {
			penaltyTimeText = Long.toString(days) + " "
					+ mResources.getString(R.string.time_unit_days_plural);
		} else if (days == 1) {
			penaltyTimeText = "1 "
					+ mResources.getString(R.string.time_unit_days_singular);
		} else {
			penaltyTimeText = "";
		}
		penaltyTime -= (days * MILIS_PER_DAY);

		if (penaltyTime > 0) {
			// Determine number of hours
			long hours = penaltyTime / MILIS_PER_HOUR;
			if (hours > 1) {
				penaltyTimeText += (days > 0 ? and : "") + hours + " "
						+ mResources.getString(R.string.time_unit_hours_plural);
			} else if (hours == 1) {
				penaltyTimeText += (days > 0 ? and : "")
						+ "1 "
						+ mResources
								.getString(R.string.time_unit_hours_singular);
			} else {
				penaltyTimeText += "";
			}
			penaltyTime -= (hours * MILIS_PER_HOUR);

			// Determine number of minutes
			if (penaltyTime > 0) {
				long minutes = penaltyTime / MILIS_PER_MINUTE;
				if (minutes > 1) {
					penaltyTimeText += ((days + hours) > 0 ? and : "")
							+ minutes
							+ " "
							+ mResources
									.getString(R.string.time_unit_minutes_plural);
				} else if (minutes == 1) {
					penaltyTimeText += ((days + hours) > 0 ? and : "")
							+ "1 "
							+ mResources
									.getString(R.string.time_unit_minutes_singular);
				} else {
					penaltyTimeText += "";
				}
				penaltyTime -= (minutes * MILIS_PER_MINUTE);

				// Determine number of seconds
				if (penaltyTime > 0) {
					long seconds = penaltyTime / MILIS_PER_SECOND;
					if (seconds > 1) {
						penaltyTimeText += ((days + hours + minutes) > 0 ? and
								: "")
								+ seconds
								+ " "
								+ mResources
										.getString(R.string.time_unit_seconds_plural);
					} else if (seconds == 1) {
						penaltyTimeText += ((days + hours + minutes) > 0 ? and
								: "")
								+ seconds
								+ " "
								+ mResources
										.getString(R.string.time_unit_seconds_singular);
					}
				}
			}
		}
		return penaltyTimeText;
	}
}