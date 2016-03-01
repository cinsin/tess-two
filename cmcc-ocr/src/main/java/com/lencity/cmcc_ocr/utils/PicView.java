package com.lencity.cmcc_ocr.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class PicView extends View {  
  
    private static final long ANIMATION_DELAY = 10L;  
    private Paint paint;
    private Paint mAreaPaint;
    private int linePos = 0; // 移动的扫描线当前位置（自上而下，Y坐标）
  
    private int canvasWidth = 0;  
    private int canvasHeight = 0;  
  
    public PicView(Context context, AttributeSet attrs) {  
        super(context, attrs);  
        paint = new Paint(Paint.ANTI_ALIAS_FLAG); // 开启反锯齿  
        paint.setColor(Color.GREEN);
    }  
  
    @Override  
    public void onDraw(Canvas canvas) {  
        canvasWidth = canvas.getWidth();  
        canvasHeight = canvas.getHeight();  
  
        drawLine(canvas);
  
        postInvalidateDelayed(ANIMATION_DELAY, 0, 0, canvasWidth, canvasHeight);  
  
    }  

    // 纵向从左到右扫描
    /*private void drawLine(Canvas canvas) {
        // 获取屏幕的宽和高  
        int iLineBegin = canvasWidth / 5;
        int iLineEnd = canvasWidth * 4 / 5;
        int iFrameHigh = canvasHeight;  
        if (++linePos == iLineEnd)
            linePos = iLineBegin;

        canvas.drawRect(linePos, 0, linePos + 1, iFrameHigh, paint);
    }*/

    // 横线从上到下扫描
    private void drawLine(Canvas canvas) {
        int iLineBegin = canvasHeight / 7 * 2; // 矩形上部距离屏幕顶部
        int iLineEnd = canvasHeight / 7 * 3; // 矩形高度
        int lineWidth = canvasWidth / 6 * 5; // 矩形宽度与屏幕宽度占比
        int gapToBroder = canvasWidth / 6 / 2;

        if (linePos < iLineBegin || linePos > iLineEnd) {
            linePos = iLineBegin;
        }
        linePos += 1; // 扫描速度



        paint.setColor(Color.GREEN);
        int startX = gapToBroder;
        int startY = linePos;
        int stopX = gapToBroder + lineWidth;
        int stopY = linePos;
        // startX, startY, stopX, stopY, paint
        canvas.drawLine(startX, startY, stopX, stopY, paint);


        paint.setStyle(Paint.Style.STROKE); // 空心
        paint.setColor(Color.WHITE);
        int left = startX;
        int top = iLineBegin;
        int right = stopX;
        int bottom = iLineEnd;
        //left, top, right, bottom, paint
        canvas.drawRect(left, top, right, bottom, paint);

        //绘制四周阴影区域
        mAreaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mAreaPaint.setColor(Color.GRAY);
        //mAreaPaint.setColor(Color.argb(200,200,200,200));
        mAreaPaint.setStyle(Paint.Style.FILL);
        mAreaPaint.setAlpha(180);
        canvas.drawRect(0, 0, left, canvasHeight, mAreaPaint);
        canvas.drawRect(left, 0, right, top, mAreaPaint);
        canvas.drawRect(left, bottom, right, canvasHeight, mAreaPaint);
        canvas.drawRect(right, 0, canvasWidth, canvasHeight, mAreaPaint);

    }

}  