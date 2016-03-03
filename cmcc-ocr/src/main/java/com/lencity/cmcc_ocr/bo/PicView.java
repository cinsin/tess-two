package com.lencity.cmcc_ocr.bo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.lencity.cmcc_ocr.utils.Constant;

public class PicView extends View {  
  
    private static final long ANIMATION_DELAY = 10L;  
    private Paint paint;
    private Paint mAreaPaint;
    private int linePos = 0; // 移动的扫描线当前位置（自上而下，Y坐标）
  
    public PicView(Context context, AttributeSet attrs) {  
        super(context, attrs);  
        paint = new Paint(Paint.ANTI_ALIAS_FLAG); // 开启反锯齿  
        paint.setColor(Color.GREEN);
    }  
  
    @Override  
    public void onDraw(Canvas canvas) {  
        Constant.CANVAS_WIDTH = canvas.getWidth();  
        Constant.CANVAS_HEIGHT = canvas.getHeight();  
  
        drawLine(canvas);
  
        postInvalidateDelayed(ANIMATION_DELAY, 0, 0, (int)Constant.CANVAS_WIDTH, (int)Constant.CANVAS_HEIGHT);
  
    }  

    // 纵向从左到右扫描
    /*private void drawLine(Canvas canvas) {
        // 获取屏幕的宽和高  
        int iLineBegin = Constant.CANVAS_WIDTH / 5;
        int iLineEnd = Constant.CANVAS_WIDTH * 4 / 5;
        int iFrameHigh = Constant.CANVAS_HEIGHT;  
        if (++linePos == iLineEnd)
            linePos = iLineBegin;

        canvas.drawRect(linePos, 0, linePos + 1, iFrameHigh, paint);
    }*/

    // 横线从上到下扫描
    private void drawLine(Canvas canvas) {
        int iLineBegin = (int)(Constant.CANVAS_HEIGHT / 100 * 20); // 矩形顶部
        int iLineEnd = (int)(Constant.CANVAS_HEIGHT / 100 * 26); // 矩形底部
        int lineWidth = (int)(Constant.CANVAS_WIDTH / 10 * 6); // 矩形宽
        int gapToBroder = (int)(Constant.CANVAS_WIDTH / 10 * 2); // 矩形左右离边框的距离

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

        Constant.CANVAS_SCAN_AREA_X = left;
        Constant.CANVAS_SCAN_AREA_Y = top;
        Constant.CANVAS_SCAN_AREA_WIDTH = stopX - startX;
        Constant.CANVAS_SCAN_AREA_HEIGHT = iLineEnd - iLineBegin;

        //绘制四周阴影区域
        mAreaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        //mAreaPaint.setColor(Color.GRAY);
        mAreaPaint.setColor(Color.argb(100, 0, 0, 0)); // 透明0~255不透明
        mAreaPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, left, Constant.CANVAS_HEIGHT, mAreaPaint);
        canvas.drawRect(left, 0, right, top, mAreaPaint);
        canvas.drawRect(left, bottom, right, Constant.CANVAS_HEIGHT, mAreaPaint);
        canvas.drawRect(right, 0, Constant.CANVAS_WIDTH, Constant.CANVAS_HEIGHT, mAreaPaint);

    }

}  