package com.venomdevelopment.sunwise;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;
import androidx.core.content.res.ResourcesCompat;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import java.lang.reflect.Field;

public class GraphViewUtils {

    private static final String TAG = "GraphViewUtils";

    public static void setLabelTypeface(Context context, GraphView graphView, int fontResId) {
        try {
            // 1. Create a custom typeface
            Typeface customTypeface = ResourcesCompat.getFont(context, fontResId);

            // 2. Get the GridLabelRenderer
            GridLabelRenderer gridLabelRenderer = graphView.getGridLabelRenderer();

            // 3. Use reflection to access the mPaintLabel object
            Field paintField = GridLabelRenderer.class.getDeclaredField("mPaintLabel");
            paintField.setAccessible(true);
            Paint paint = (Paint) paintField.get(gridLabelRenderer);

            // 4. Set the custom typeface
            paint.setTypeface(customTypeface);

            // Invalidate the graph to redraw with the new typeface
            graphView.invalidate();
            Log.d("font", "font set");

        } catch (Exception e) {
            Log.e(TAG, "Error setting label typeface: " + e.getMessage());
        }
    }
    public static void setTitleTypeface(Context context, GraphView graphView, int fontResId) {
        try {
            // 1. Create a custom typeface
            Typeface customTypeface = ResourcesCompat.getFont(context, fontResId);

            // 2. Use reflection to access the mTitlePaint object
            Field titlePaintField = GraphView.class.getDeclaredField("mPaintTitle");
            titlePaintField.setAccessible(true);
            Paint titlePaint = (Paint) titlePaintField.get(graphView);

            // 3. Set the custom typeface
            titlePaint.setTypeface(customTypeface);

            // Invalidate the graph to redraw with the new typeface
            graphView.invalidate();
            Log.d("font", "title font set");

        } catch (Exception e) {
            Log.e(TAG, "Error setting title typeface: " + e.getMessage());
        }
    }
}