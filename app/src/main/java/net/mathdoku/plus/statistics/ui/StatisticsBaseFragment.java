package net.mathdoku.plus.statistics.ui;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.mathdoku.plus.R;
import net.mathdoku.plus.util.Util;

import org.achartengine.GraphicalView;
import org.achartengine.renderer.SimpleSeriesRenderer;

/**
 * A base fragment representing the statistics for a game or a grid size.
 */
public class StatisticsBaseFragment extends android.support.v4.app.Fragment {
    @SuppressWarnings("unused")
    private static final String TAG = StatisticsBaseFragment.class.getName();

    private LinearLayout mChartsLayout;

    // Text size for body text
    protected int mDefaultTextSize;

    // The inflater for this activity.
    private LayoutInflater mLayoutInflater;

    private boolean mDisplayStatisticDescription;

    // Green colors will be used at things which are positive
    protected static final int COLOR_GREEN = 0xFF80FF00;

    // Grey colors will be used at things which are neutral
    protected static final int COLOR_GREY = 0xFFD4D4D4;

    protected static final int COLOR_PINK = 0xFFFF00FF;
    protected static final int COLOR_PURPLE = 0xFF8000FF;
    protected static final int COLOR_BLUE = 0xFF0000FF;

    // Green colors will be used at things which are negative
    protected static final int COLOR_RED_1 = 0xFFFF0000;
    protected static final int COLOR_RED_2 = 0xFFFF3300;
    protected static final int COLOR_RED_3 = 0xFFB22400;
    protected static final int COLOR_RED_4 = 0xFFFECCBF;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return onCreateView(inflater, R.layout.statistics_fragment, container, savedInstanceState);
    }

    protected View onCreateView(LayoutInflater inflater, int layout, ViewGroup container, @SuppressWarnings(
            "UnusedParameters") Bundle savedInstanceState) {
        // Get default sizes for text
        mDefaultTextSize = getResources().getDimensionPixelSize(R.dimen.text_size_default);

        // Chart description will be displayed by default.
        mDisplayStatisticDescription = true;

        // Get inflater and return view
        mLayoutInflater = inflater;

        View rootView = mLayoutInflater.inflate(layout, container, false);

        mChartsLayout = (LinearLayout) rootView.findViewById(R.id.chartLayouts);

        return rootView;
    }

    /**
     * Creates a new simple series renderer for the given color.
     *
     * @param color
     *         The color for the new simple series renderer.
     * @return A new simple series renderer for the given color.
     */
    protected SimpleSeriesRenderer createSimpleSeriesRenderer(int color) {
        SimpleSeriesRenderer simpleSeriesRenderer = new SimpleSeriesRenderer();
        simpleSeriesRenderer.setColor(color);

        return simpleSeriesRenderer;
    }

    /**
     * Add a statistics section to the activity.
     * @param title
     *         The title of this section. Null in case no title has to be displayed.
     * @param chart
     *         The chart view. Null in case no chart has to be displayed.
     * @param explanation
     */
    protected View addChartToStatisticsSection(String title, GraphicalView chart, String explanation) {
        // Inflate a new view for this statistics section
        View sectionView = mLayoutInflater.inflate(R.layout.statistics_section, null);
        if (sectionView == null) {
            return null;
        }

        // Set title. The chart title of achartengine is not used.
        int titleHeightDIP = 0;
        if (!Util.isNullOrEmpty(title)) {
            TextView textView = (TextView) sectionView.findViewById(R.id.statistics_section_title);
            if (textView != null) {
                titleHeightDIP = textView.getPaddingTop() + (int) textView.getTextSize() +
                        textView.getPaddingBottom();
                textView.setText(title);
                textView.setVisibility(View.VISIBLE);
            }
        }

        // Add chart
        if (chart != null) {
            LinearLayout linearLayout = (LinearLayout) sectionView.findViewById(R.id.statistics_section_chart);
            if (linearLayout != null) {
                int paddingChartDIP = linearLayout.getPaddingTop() + linearLayout.getPaddingBottom();

                // The height of the achartengine view has to be set explicitly
                // else it won't be displayed.
                chart.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                                                       getMaxChartHeight(titleHeightDIP, paddingChartDIP)));

                linearLayout.addView(chart);
                linearLayout.setVisibility(View.VISIBLE);
            }
        }

        // Add body text for explaining the chart
        if (explanation != null && !explanation.isEmpty() && mDisplayStatisticDescription) {
            TextView textView = (TextView) sectionView.findViewById(R.id.statistics_section_explanation);
            if (textView != null) {
                textView.setText(explanation);
                textView.setVisibility(View.VISIBLE);
            }
        }

        // Add the section to the general charts layout
        mChartsLayout.addView(sectionView);

        return sectionView;
    }

    protected void addViewToStatisticsSection(View view) {
        mChartsLayout.addView(view);
    }


    /**
     * Sets whether the chart descriptions have to be displayed.
     *
     * @param display
     *         True in case the chart descriptions have to be displayed.
     */
    protected void setDisplayChartDescription(boolean display) {
        mDisplayStatisticDescription = display;
    }

    /**
     * Determine the height to be used for the charts. The title and the chart should be entirely visible without
     * scrolling.
     *
     * @param titleHeightPixels
     *         The height needed to display the title inclusive top and bottom padding.
     * @param paddingChartPixels
     *         The height of the top and bottom padding set on the layout to which the chart is added.
     * @return The height to be set on the chart.
     */
    protected int getMaxContentHeight(int titleHeightPixels, int paddingChartPixels) {
        // Get size of display
        DisplayMetrics displayMetrics = getActivity().getResources()
                .getDisplayMetrics();
        int maxContentHeight = displayMetrics.heightPixels;

        // Get height of the notification bar
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            maxContentHeight -= getResources().getDimensionPixelSize(resourceId);
        }

        // Calculate ActionBar height
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getActivity().getTheme();
        if (theme != null && theme.resolveAttribute(android.R.attr.actionBarSize, typedValue, true)) {
            maxContentHeight -= TypedValue.complexToDimensionPixelSize(typedValue.data, displayMetrics);
        }

        // Subtract height (inclusive padding) of chart title and padding of the
        // chart itself
        maxContentHeight -= titleHeightPixels + paddingChartPixels;

        return maxContentHeight;
    }

    /**
     * Determine the height to be used for the charts. The title and the chart should be entirely visible without
     * scrolling.
     *
     * @param titleHeightPixels
     *         The height needed to display the title inclusive top and bottom padding.
     * @param paddingChartPixels
     *         The height of the top and bottom padding set on the layout to which the chart is added.
     * @return The height to be set on the chart.
     */
    private int getMaxChartHeight(int titleHeightPixels, int paddingChartPixels) {
        // Determine an acceptable height / width ratio for the chart dependent
        // on the orientation of the device
        Configuration configuration = getActivity().getResources()
                .getConfiguration();
        float ratio = configuration.orientation == Configuration.ORIENTATION_PORTRAIT ? 2f / 3f : 1f / 2f;

        // The actual height of the chart is preferably equal to the ratio of
        // the width but it may never exceeds the maximum content height as the
        // title and chart must be viewable without scrolling.
        return Math.min((int) (getActivity().getResources()
                                .getDisplayMetrics().widthPixels * ratio),
                        getMaxContentHeight(titleHeightPixels, paddingChartPixels));
    }

    protected void removeAllCharts() {
        mChartsLayout.removeAllViewsInLayout();
    }
}