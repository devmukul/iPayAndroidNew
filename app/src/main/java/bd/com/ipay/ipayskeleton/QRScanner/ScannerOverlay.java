package bd.com.ipay.ipayskeleton.QRScanner;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.ViewGroup;

import bd.com.ipay.ipayskeleton.R;

/**
 * Created by ravi on 04/05/17.
 */

public class ScannerOverlay extends ViewGroup {
    private float left, top, endY;
    private int rectWidth, rectHeight;
    private int frames;
    private boolean revAnimation;
    private int lineColor, lineWidth;
    private int border_length;

    public ScannerOverlay(Context context) {
        super(context);
    }

    public ScannerOverlay(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScannerOverlay(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ScannerOverlay,
                0, 0);
        rectWidth = a.getInteger(R.styleable.ScannerOverlay_square_width, getResources().getInteger(R.integer.scanner_rect_width));
        rectHeight = a.getInteger(R.styleable.ScannerOverlay_square_height, getResources().getInteger(R.integer.scanner_rect_height));
        lineColor = a.getColor(R.styleable.ScannerOverlay_line_color, ContextCompat.getColor(context, R.color.scanner_line));
        lineWidth = a.getInteger(R.styleable.ScannerOverlay_line_width, getResources().getInteger(R.integer.line_width));
        frames = a.getInteger(R.styleable.ScannerOverlay_line_speed, getResources().getInteger(R.integer.line_width));
        border_length = 30;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        left = (w - dpToPx(rectWidth)) / 2;
        top = (h - dpToPx(rectHeight)) / 2;
        endY = top;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // draw transparent rect
        int cornerRadius = 0;
        Paint eraser = new Paint();
        eraser.setAntiAlias(true);
        eraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));


        RectF rect = new RectF(left, top, dpToPx(rectWidth) + left, dpToPx(rectHeight) + top);
        canvas.drawRoundRect(rect, (float) cornerRadius, (float) cornerRadius, eraser);

        // draw horizontal line
        Paint line = new Paint();
        line.setColor(lineColor);
        line.setStrokeWidth(Float.valueOf(lineWidth));

        canvas.drawLine(left, top, left + dpToPx(border_length), top, line);
        canvas.drawLine(left, top, left, top + dpToPx(border_length), line);

        canvas.drawLine(left + dpToPx(rectWidth) - dpToPx(border_length), top, left + dpToPx(rectWidth), top, line);
        canvas.drawLine(left + dpToPx(rectWidth), top, left + dpToPx(rectWidth), top + dpToPx(border_length), line);

        canvas.drawLine(left, top + dpToPx(rectHeight), left + dpToPx(border_length), top + dpToPx(rectHeight), line);
        canvas.drawLine(left, top + dpToPx(rectHeight), left, top + dpToPx(rectHeight) - dpToPx(border_length), line);

        canvas.drawLine(left + dpToPx(rectWidth) - dpToPx(border_length), top + dpToPx(rectHeight), left + dpToPx(rectWidth), top + dpToPx(rectHeight), line);
        canvas.drawLine(left + dpToPx(rectWidth), top + dpToPx(rectHeight), left + dpToPx(rectWidth), top + dpToPx(rectHeight) - dpToPx(border_length), line);

        canvas.drawPoint(left, top, line);
        canvas.drawPoint(left + dpToPx(rectWidth), top, line);
        canvas.drawPoint(left, top + dpToPx(rectHeight), line);
        canvas.drawPoint(left + dpToPx(rectWidth), top + dpToPx(rectHeight), line);

        String qr_code_text = getContext().getString(R.string.qr_code_helper_text);
        String[] str = qr_code_text.split(",");

        TextPaint textPaint = new TextPaint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(dpToPx(16));
        textPaint.setTextAlign(Paint.Align.CENTER);
        float textHeight = -textPaint.ascent();

        RectF bounds = new RectF(0, 0, getWidth(), getHeight());
        for (int i = str.length - 1; i >= 0; i--) {
            //Center text here
            canvas.drawText(str[i], bounds.centerX(), bounds.centerY() + ((i + 3) * textHeight) + dpToPx(150), textPaint);
        }
        invalidate();
    }
}