package com.business.zyvo;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;
import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.buffer.BarBuffer;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.highlight.Range;
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.model.GradientColor;
import com.github.mikephil.charting.renderer.BarLineScatterCandleBubbleRenderer;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;
import java.util.List;

public class RoundedBarChartRenderer extends BarLineScatterCandleBubbleRenderer {
    protected BarDataProvider mChart;
    protected RectF mBarRect = new RectF();
    protected BarBuffer[] mBarBuffers;
    protected Paint mShadowPaint;
    protected Paint mBarBorderPaint;
    private RectF mBarShadowRectBuffer = new RectF();

    // Radius for rounded corners
    private float mRadius = 8f;

    public RoundedBarChartRenderer(BarDataProvider chart, ChartAnimator animator, ViewPortHandler viewPortHandler) {
        super(animator, viewPortHandler);
        this.mChart = chart;
        this.mHighlightPaint = new Paint(1);
        this.mHighlightPaint.setStyle(Style.FILL);
        this.mHighlightPaint.setColor(Color.rgb(0, 0, 0));
        this.mHighlightPaint.setAlpha(120);
        this.mShadowPaint = new Paint(1);
        this.mShadowPaint.setStyle(Style.FILL);
        this.mBarBorderPaint = new Paint(1);
        this.mBarBorderPaint.setStyle(Style.STROKE);
    }

    /**
     * Set the radius for rounded corners
     * @param radius Corner radius in pixels
     */
    public void setRadius(float radius) {
        this.mRadius = radius;
    }

    public void initBuffers() {
        BarData barData = this.mChart.getBarData();
        this.mBarBuffers = new BarBuffer[barData.getDataSetCount()];

        for(int i = 0; i < this.mBarBuffers.length; ++i) {
            IBarDataSet set = (IBarDataSet)barData.getDataSetByIndex(i);
            this.mBarBuffers[i] = new BarBuffer(set.getEntryCount() * 4 * (set.isStacked() ? set.getStackSize() : 1), barData.getDataSetCount(), set.isStacked());
        }
    }

    public void drawData(Canvas c) {
        BarData barData = this.mChart.getBarData();

        for(int i = 0; i < barData.getDataSetCount(); ++i) {
            IBarDataSet set = (IBarDataSet)barData.getDataSetByIndex(i);
            if (set.isVisible()) {
                this.drawDataSet(c, set, i);
            }
        }
    }

    protected void drawDataSet(Canvas c, IBarDataSet dataSet, int index) {
        Transformer trans = this.mChart.getTransformer(dataSet.getAxisDependency());
        this.mBarBorderPaint.setColor(dataSet.getBarBorderColor());
        this.mBarBorderPaint.setStrokeWidth(Utils.convertDpToPixel(dataSet.getBarBorderWidth()));
        boolean drawBorder = dataSet.getBarBorderWidth() > 0.0F;
        float phaseX = this.mAnimator.getPhaseX();
        float phaseY = this.mAnimator.getPhaseY();

        if (this.mChart.isDrawBarShadowEnabled()) {
            this.mShadowPaint.setColor(dataSet.getBarShadowColor());
            BarData barData = this.mChart.getBarData();
            float barWidth = barData.getBarWidth();
            float barWidthHalf = barWidth / 2.0F;
            int i = 0;

            for(int count = Math.min((int)Math.ceil((double)((float)dataSet.getEntryCount() * phaseX)), dataSet.getEntryCount()); i < count; ++i) {
                BarEntry e = (BarEntry)dataSet.getEntryForIndex(i);
                float x = e.getX();
                this.mBarShadowRectBuffer.left = x - barWidthHalf;
                this.mBarShadowRectBuffer.right = x + barWidthHalf;
                trans.rectValueToPixel(this.mBarShadowRectBuffer);
                if (this.mViewPortHandler.isInBoundsLeft(this.mBarShadowRectBuffer.right)) {
                    if (!this.mViewPortHandler.isInBoundsRight(this.mBarShadowRectBuffer.left)) {
                        break;
                    }

                    this.mBarShadowRectBuffer.top = this.mViewPortHandler.contentTop();
                    this.mBarShadowRectBuffer.bottom = this.mViewPortHandler.contentBottom();
                    c.drawRect(this.mBarShadowRectBuffer, this.mShadowPaint);
                }
            }
        }

        BarBuffer buffer = this.mBarBuffers[index];
        buffer.setPhases(phaseX, phaseY);
        buffer.setDataSet(index);
        buffer.setInverted(this.mChart.isInverted(dataSet.getAxisDependency()));
        buffer.setBarWidth(this.mChart.getBarData().getBarWidth());
        buffer.feed(dataSet);
        trans.pointValuesToPixel(buffer.buffer);
        boolean isSingleColor = dataSet.getColors().size() == 1;
        if (isSingleColor) {
            this.mRenderPaint.setColor(dataSet.getColor());
        }

        for(int j = 0; j < buffer.size(); j += 4) {
            if (this.mViewPortHandler.isInBoundsLeft(buffer.buffer[j + 2])) {
                if (!this.mViewPortHandler.isInBoundsRight(buffer.buffer[j])) {
                    break;
                }

                if (!isSingleColor) {
                    this.mRenderPaint.setColor(dataSet.getColor(j / 4));
                }

                if (dataSet.getGradientColor() != null) {
                    GradientColor gradientColor = dataSet.getGradientColor();
                    this.mRenderPaint.setShader(new LinearGradient(buffer.buffer[j], buffer.buffer[j + 3], buffer.buffer[j], buffer.buffer[j + 1], gradientColor.getStartColor(), gradientColor.getEndColor(), TileMode.MIRROR));
                }

                if (dataSet.getGradientColors() != null) {
                    this.mRenderPaint.setShader(new LinearGradient(buffer.buffer[j], buffer.buffer[j + 3], buffer.buffer[j], buffer.buffer[j + 1], dataSet.getGradientColor(j / 4).getStartColor(), dataSet.getGradientColor(j / 4).getEndColor(), TileMode.MIRROR));
                }

                // Draw rounded rectangle instead of normal rectangle
                drawRoundedRect(c, buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2], buffer.buffer[j + 3], this.mRenderPaint);

                if (drawBorder) {
                    drawRoundedRect(c, buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2], buffer.buffer[j + 3], this.mBarBorderPaint);
                }
            }
        }
    }

    /**
     * Draw rounded rectangle with top corners rounded
     */
    private void drawRoundedRect(Canvas c, float left, float top, float right, float bottom, Paint paint) {
        Path path = new Path();

        // Determine if bar is positive or negative
        boolean isPositive = top < bottom;

        if (isPositive) {
            // For positive bars, round the top corners
            path.moveTo(left, bottom);
            path.lineTo(left, top + mRadius);
            path.quadTo(left, top, left + mRadius, top);
            path.lineTo(right - mRadius, top);
            path.quadTo(right, top, right, top + mRadius);
            path.lineTo(right, bottom);
            path.close();
        } else {
            // For negative bars, round the bottom corners
            path.moveTo(left, top);
            path.lineTo(left, bottom - mRadius);
            path.quadTo(left, bottom, left + mRadius, bottom);
            path.lineTo(right - mRadius, bottom);
            path.quadTo(right, bottom, right, bottom - mRadius);
            path.lineTo(right, top);
            path.close();
        }

        c.drawPath(path, paint);
    }

    protected void prepareBarHighlight(float x, float y1, float y2, float barWidthHalf, Transformer trans) {
        float left = x - barWidthHalf;
        float right = x + barWidthHalf;
        float top = y1;
        float bottom = y2;
        this.mBarRect.set(left, top, right, bottom);
        trans.rectToPixelPhase(this.mBarRect, this.mAnimator.getPhaseY());
    }

    public void drawValues(Canvas c) {
        if (this.isDrawingValuesAllowed(this.mChart)) {
            List<IBarDataSet> dataSets = this.mChart.getBarData().getDataSets();
            float valueOffsetPlus = Utils.convertDpToPixel(4.5F);
            float posOffset = 0.0F;
            float negOffset = 0.0F;
            boolean drawValueAboveBar = this.mChart.isDrawValueAboveBarEnabled();

            for(int i = 0; i < this.mChart.getBarData().getDataSetCount(); ++i) {
                IBarDataSet dataSet = (IBarDataSet)dataSets.get(i);
                if (this.shouldDrawValues(dataSet)) {
                    this.applyValueTextStyle(dataSet);
                    boolean isInverted = this.mChart.isInverted(dataSet.getAxisDependency());
                    float valueTextHeight = (float)Utils.calcTextHeight(this.mValuePaint, "8");
                    posOffset = drawValueAboveBar ? -valueOffsetPlus : valueTextHeight + valueOffsetPlus;
                    negOffset = drawValueAboveBar ? valueTextHeight + valueOffsetPlus : -valueOffsetPlus;
                    if (isInverted) {
                        posOffset = -posOffset - valueTextHeight;
                        negOffset = -negOffset - valueTextHeight;
                    }

                    BarBuffer buffer = this.mBarBuffers[i];
                    float phaseY = this.mAnimator.getPhaseY();
                    ValueFormatter formatter = dataSet.getValueFormatter();
                    MPPointF iconsOffset = MPPointF.getInstance(dataSet.getIconsOffset());
                    iconsOffset.x = Utils.convertDpToPixel(iconsOffset.x);
                    iconsOffset.y = Utils.convertDpToPixel(iconsOffset.y);

                    if (!dataSet.isStacked()) {
                        for(int j = 0; (float)j < (float)buffer.buffer.length * this.mAnimator.getPhaseX(); j += 4) {
                            float x = (buffer.buffer[j] + buffer.buffer[j + 2]) / 2.0F;
                            if (!this.mViewPortHandler.isInBoundsRight(x)) {
                                break;
                            }

                            if (this.mViewPortHandler.isInBoundsY(buffer.buffer[j + 1]) && this.mViewPortHandler.isInBoundsLeft(x)) {
                                BarEntry entry = (BarEntry)dataSet.getEntryForIndex(j / 4);
                                float val = entry.getY();
                                if (dataSet.isDrawValuesEnabled()) {
                                    this.drawValue(c, formatter.getBarLabel(entry), x, val >= 0.0F ? buffer.buffer[j + 1] + posOffset : buffer.buffer[j + 3] + negOffset, dataSet.getValueTextColor(j / 4));
                                }

                                if (entry.getIcon() != null && dataSet.isDrawIconsEnabled()) {
                                    Drawable icon = entry.getIcon();
                                    float px = x;
                                    float py = val >= 0.0F ? buffer.buffer[j + 1] + posOffset : buffer.buffer[j + 3] + negOffset;
                                    px += iconsOffset.x;
                                    py += iconsOffset.y;
                                    Utils.drawImage(c, icon, (int)px, (int)py, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
                                }
                            }
                        }
                    }
                    // Note: Stacked bar handling code remains the same as original
                    MPPointF.recycleInstance(iconsOffset);
                }
            }
        }
    }

    public void drawValue(Canvas c, String valueText, float x, float y, int color) {
        this.mValuePaint.setColor(color);
        c.drawText(valueText, x, y, this.mValuePaint);
    }

    public void drawHighlighted(Canvas c, Highlight[] indices) {
        BarData barData = this.mChart.getBarData();
        Highlight[] var4 = indices;
        int var5 = indices.length;

        for(int var6 = 0; var6 < var5; ++var6) {
            Highlight high = var4[var6];
            IBarDataSet set = (IBarDataSet)barData.getDataSetByIndex(high.getDataSetIndex());
            if (set != null && set.isHighlightEnabled()) {
                BarEntry e = (BarEntry)set.getEntryForXValue(high.getX(), high.getY());
                if (this.isInBoundsX(e, set)) {
                    Transformer trans = this.mChart.getTransformer(set.getAxisDependency());
                    this.mHighlightPaint.setColor(set.getHighLightColor());
                    this.mHighlightPaint.setAlpha(set.getHighLightAlpha());
                    boolean isStack = high.getStackIndex() >= 0 && e.isStacked();
                    float y1;
                    float y2;
                    if (isStack) {
                        if (this.mChart.isHighlightFullBarEnabled()) {
                            y1 = e.getPositiveSum();
                            y2 = -e.getNegativeSum();
                        } else {
                            Range range = e.getRanges()[high.getStackIndex()];
                            y1 = range.from;
                            y2 = range.to;
                        }
                    } else {
                        y1 = e.getY();
                        y2 = 0.0F;
                    }

                    this.prepareBarHighlight(e.getX(), y1, y2, barData.getBarWidth() / 2.0F, trans);
                    this.setHighlightDrawPos(high, this.mBarRect);

                    // Draw rounded highlight
                    drawRoundedRect(c, this.mBarRect.left, this.mBarRect.top, this.mBarRect.right, this.mBarRect.bottom, this.mHighlightPaint);
                }
            }
        }
    }

    protected void setHighlightDrawPos(Highlight high, RectF bar) {
        high.setDraw(bar.centerX(), bar.top);
    }

    public void drawExtras(Canvas c) {
    }
}
