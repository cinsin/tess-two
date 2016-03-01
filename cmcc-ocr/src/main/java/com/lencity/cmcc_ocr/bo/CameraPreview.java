package com.lencity.cmcc_ocr.bo;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


import com.lencity.cmcc_ocr.utils.Constant;

import java.io.IOException;
import java.util.List;

/**
 * A basic Camera preview class
 * Created by cinsin on 2015/9/15.
 *
 * 相机图片预览类
 *
 * @author
 *
 */
public class CameraPreview extends SurfaceView implements
        SurfaceHolder.Callback
{

    private SurfaceHolder mHolder;
    private Camera mCamera;
    Camera.Size mPreviewSize;
    List<Camera.Size> mSupportedPreviewSizes;

    public CameraPreview(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init();
    }

    public CameraPreview(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public CameraPreview(Context context)
    {
        super(context);
        init();
    }

    /**
     * 初始化工作
     *
     */
    private void init()
    {
        Log.d(Constant.LOG_TAG, "CameraPreview initialize");

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

    }

    public void setCamera(Camera camera)
    {

        mCamera = camera;
        if (mCamera != null)
        {
            mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
            requestLayout();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        Log.d(Constant.LOG_TAG, "surfaceCreated");
        // The Surface has been created, now tell the camera where to draw the
        // preview.
        try
        {
            if (null != mCamera)
            {
                mCamera.setPreviewDisplay(holder);
            }
        }
        catch (IOException e1)
        {
            e1.printStackTrace();

            Log.d(Constant.LOG_TAG, "Error setting camera preview display: " + e1.getMessage());
        }
        try
        {
            if (null != mCamera)
            {
                mCamera.startPreview();
            }

            Log.d(Constant.LOG_TAG, "surfaceCreated successfully! ");
        }
        catch (Exception e)
        {
            Log.d(Constant.LOG_TAG,
                    "Error setting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height)
    {

        Log.d(Constant.LOG_TAG, "surface changed");
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (null == mHolder.getSurface())
        {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try
        {
            if (null != mCamera)
            {
                mCamera.stopPreview();
            }
        }
        catch (Exception e)
        {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        if (null != mCamera)
        {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);

            requestLayout();

            mCamera.setParameters(parameters);
            mCamera.setDisplayOrientation(90);
            Log.d(Constant.LOG_TAG, "camera set parameters successfully!: "
                    + parameters);

        }
        // 这里可以用来设置尺寸

        // start preview with new settings
        try
        {
            if (null != mCamera)
            {

                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();
            }

        }
        catch (Exception e)
        {
            Log.d(Constant.LOG_TAG,
                    "Error starting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        Log.d(Constant.LOG_TAG, "surfaceDestroyed");

        if (null != mCamera)
        {
            mCamera.stopPreview();
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // We purposely disregard child measurements because act as a
        // wrapper to a SurfaceView that centers the camera preview instead
        // of stretching it.
        final int width = resolveSize(getSuggestedMinimumWidth(),
                widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(),
                heightMeasureSpec);
        setMeasuredDimension(width, height);

        if (mSupportedPreviewSizes != null)
        {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width,
                    height);
        }
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h)
    {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null)
            return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes)
        {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - targetHeight) < minDiff)
            {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null)
        {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes)
            {
                if (Math.abs(size.height - targetHeight) < minDiff)
                {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    /*private void initPreview(int width, int height) {
        if (mCamera != null && mHolder.getSurface() != null) {
            try {
                mCamera.setPreviewDisplay(mHolder);
            } catch (Throwable t) {
                Log.e("PreviewDemo-surfaceCallback",
                        "Exception in setPreviewDisplay()", t);
                Toast.makeText(getContext(), t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }

            try {
                Camera.Parameters parameters=mCamera.getParameters();

                Camera.Size size=getBestPreviewSize(width, height, parameters);
                Camera.Size pictureSize=getSmallestPictureSize(parameters);

                Display display = windowManager.getDefaultDisplay();
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) { // for 2.1 and before
                    if (isPortrait(display)) {
                        parameters.set(CAMERA_PARAM_ORIENTATION, CAMERA_PARAM_PORTRAIT);
                    } else {
                        parameters.set(CAMERA_PARAM_ORIENTATION, CAMERA_PARAM_LANDSCAPE);
                    }
                } else { // for 2.2 and later
                    switch (display.getRotation()) {
                        case Surface.ROTATION_0: // This is display orientation
                            if (size.height > size.width) parameters.setPreviewSize(size.height, size.width);
                            else parameters.setPreviewSize(size.width, size.height);
                            mCamera.setDisplayOrientation(90);
                            break;
                        case Surface.ROTATION_90:
                            if (size.height > size.width) parameters.setPreviewSize(size.height, size.width);
                            else parameters.setPreviewSize(size.width, size.height);
                            mCamera.setDisplayOrientation(0);
                            break;
                        case Surface.ROTATION_180:
                            if (size.height > size.width) parameters.setPreviewSize(size.height, size.width);
                            else parameters.setPreviewSize(size.width, size.height);
                            mCamera.setDisplayOrientation(270);
                            break;
                        case Surface.ROTATION_270:
                            if (size.height > size.width) parameters.setPreviewSize(size.height, size.width);
                            else parameters.setPreviewSize(size.width, size.height);
                            mCamera.setDisplayOrientation(180);
                            break;
                    }
                }

                parameters.setPictureSize(pictureSize.width, pictureSize.height);
                //parameters.setPictureFormat(ImageFormat.JPEG);
                mCamera.setParameters(parameters);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }*/

}
