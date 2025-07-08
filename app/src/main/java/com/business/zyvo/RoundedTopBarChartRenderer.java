package com.business.zyvo;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.buffer.BarBuffer;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.renderer.BarChartRenderer;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

public class RoundedTopBarChartRenderer extends BarChartRenderer {

    private float mRadius = 10f; // Default radius for rounded corners
    private Path mBarShadowPathBuffer = new Path();
    private RectF mBarRect = new RectF();

    public RoundedTopBarChartRenderer(BarDataProvider chart, ChartAnimator animator, ViewPortHandler viewPortHandler) {
        super(chart, animator, viewPortHandler);
    }

    /**
     * Sets the radius for rounded corners
     * @param radius corner radius in pixels
     */
    public void setRadius(float radius) {
        this.mRadius = radius;
    }

    @Override
    protected void drawDataSet(Canvas c, IBarDataSet dataSet, int index) {
        Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());

        // Configure paint
        mBarBorderPaint.setColor(dataSet.getBarBorderColor());
        mBarBorderPaint.setStrokeWidth(Utils.convertDpToPixel(dataSet.getBarBorderWidth()));
        final boolean drawBorder = dataSet.getBarBorderWidth() > 0f;

        // Get animation phases
        final float phaseX = mAnimator.getPhaseX();
        final float phaseY = mAnimator.getPhaseY();

        // Initialize the buffer
        BarBuffer buffer = mBarBuffers[index];
        buffer.setPhases(phaseX, phaseY);
        buffer.setDataSet(index);
        buffer.setInverted(mChart.isInverted(dataSet.getAxisDependency()));
        buffer.setBarWidth(mChart.getBarData().getBarWidth());
        buffer.feed(dataSet);

        // Transform values to pixels
        trans.pointValuesToPixel(buffer.buffer);

        // Single color vs multi-color handling
        final boolean isSingleColor = dataSet.getColors().size() == 1;
        if (isSingleColor) {
            mRenderPaint.setColor(dataSet.getColor());
        }

        // Draw each bar
        for (int j = 0; j < buffer.size(); j += 4) {
            if (!mViewPortHandler.isInBoundsLeft(buffer.buffer[j + 2])) {
                continue;
            }

            if (!mViewPortHandler.isInBoundsRight(buffer.buffer[j])) {
                break;
            }

            // Set color for multi-color bars
            if (!isSingleColor) {
                mRenderPaint.setColor(dataSet.getColor(j / 4));
            }

            // Create path for rounded top bar
            Path path = createRoundedTopBarPath(
                    buffer.buffer[j],     // left
                    buffer.buffer[j + 1], // top
                    buffer.buffer[j + 2], // right
                    buffer.buffer[j + 3]  // bottom
            );

            // Draw the bar
            c.drawPath(path, mRenderPaint);

            // Draw border if needed
            if (drawBorder) {
                c.drawPath(path, mBarBorderPaint);
            }
        }
    }

    /**
     * Creates a path for a bar with rounded top corners
     */
    private Path createRoundedTopBarPath(float left, float top, float right, float bottom) {
        Path path = new Path();
        mBarRect.set(left, top, right, bottom);

        // Determine actual bar top and bottom (accounting for negative values)
        float barTop = Math.min(top, bottom);
        float barBottom = Math.max(top, bottom);

        // Adjust radius if bar is too small
        float usedRadius = Math.min(mRadius, (barBottom - barTop) / 2f);

        // Build the path with rounded top corners
        path.moveTo(left, barBottom);
        path.lineTo(left, barTop + usedRadius);
        path.quadTo(left, barTop, left + usedRadius, barTop);
        path.lineTo(right - usedRadius, barTop);
        path.quadTo(right, barTop, right, barTop + usedRadius);
        path.lineTo(right, barBottom);
        path.close();

        return path;
    }

    @Override
    protected void prepareBarHighlight(float x, float y1, float y2, float barWidthHalf, Transformer trans) {
        // You may want to override this if you need custom highlight shapes
        super.prepareBarHighlight(x, y1, y2, barWidthHalf, trans);
    }
}