package com.lencity.cmcc_ocr.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Menu;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.lencity.cmcc_ocr.R;
import com.lencity.cmcc_ocr.bo.CameraPreview;
import com.lencity.cmcc_ocr.bo.ImageHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;

public class MainActivity extends Activity implements SurfaceHolder.Callback {
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    Camera mCamera;
    private CameraPreview mPreview;
    Camera.Parameters parameters;

    // The first rear facing camera
    int mDefaultCameraId;
    int mNumberOfCameras;
    int mCameraCurrentlyLocked;

    Bitmap bitmapSrc;
    Bitmap bitmapRes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        surfaceHolder =  surfaceView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.setKeepScreenOn(true);
        surfaceView.setFocusable(true);
        surfaceView.setBackgroundColor(TRIM_MEMORY_BACKGROUND);
        surfaceHolder.addCallback(this);

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this);

        // Source video frame
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);

        // 将相机预览图加入帧布局里面
        preview.addView(mPreview, 0);

        // 得到默认的相机ID
        mDefaultCameraId = getDefaultCameraId();
        mCameraCurrentlyLocked = mDefaultCameraId;

        mCamera = getCameraInstance(mCameraCurrentlyLocked);

        // Capture picture and send to api
       /* new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("LOG_TAG", "*** thread thread ***");
                while (true) {
                    try {
                        Thread.sleep(5000l);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // Identification interval
                    mCamera.takePicture(null, null, mPicture);
                    Log.d("LOG_TAG", "Picture took...");
                }
            }
        }).start();*/

    }

    /*@Override
    protected void onResume() {
        Log.d(Constant.LOG_TAG, "onResume");
        super.onResume();

        // Open the default i.e. the first rear facing camera.
        mCamera = getCameraInstance(mCameraCurrentlyLocked);

        mPreview.setCamera(mCamera);
    }

    @Override
    protected void onPause() {
        Log.d(Constant.LOG_TAG, "onPause");
        super.onPause();

        // Because the Camera object is a shared resource, it's very
        // important to release it when the activity is paused.
        if (mCamera != null) {
            mPreview.setCamera(null);
            Log.d(Constant.LOG_TAG, "onPause --> Realease camera");
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(Constant.LOG_TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Log.d(Constant.LOG_TAG, "onWindowFocusChanged, checking camera status...");
        *//*if (isSwitchOn) {
            Log.d(Constant.LOG_TAG, "Camera is on, will be released immediately...");
            startCaptureButton.performClick(); // perform click
        }*//*
    }*/

    /**
     * Callback on picture taken
     */
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d("MainActivity", "onPictureTaken");

            // Captured picture is in landscape mode, need to transfer to portrait mode
            // byte[] -> BitMap -> landscape to portrait -> byte[]
            if (data != null) {
                bitmapSrc = BitmapFactory.decodeByteArray(data, 0, data.length);//data是字节数据，将其解析成位图
                // Start preview after picture taken
                mCamera.startPreview();
            }
            bitmapSrc = ImageHandler.getRotateBitmap(bitmapSrc, 90.0f);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmapSrc.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            data = stream.toByteArray();

            // Save taken picture to phone/sd card storage
            /*File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null) {
                Log.d(Constant.LOG_TAG, "Error creating media file, check storage permissions: ");
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(Constant.LOG_TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(Constant.LOG_TAG, "Error accessing file: " + e.getMessage());
            }*/

            TessBaseAPI baseApi = new TessBaseAPI();
            // DATA_PATH = Path to the storage
            // lang = for which the language data exists, usually "eng"
            //baseApi.init(DATA_PATH, lang);
            baseApi.init("/tesseract/tessdata/eng.traineddata", "eng");
            baseApi.setImage(bitmapSrc);
            String recognizedText = baseApi.getUTF8Text();
            Log.i("#RESULT#", recognizedText);
            baseApi.end();
        }
    };

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (null == mCamera) {
            mCamera = Camera.open();
            try {
                mCamera.setPreviewDisplay(surfaceHolder);
                initCamera();
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //实现自动对焦
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if (success) {
                    initCamera();//实现相机的参数初始化
                    camera.cancelAutoFocus();//只有加上了这一句，才会自动对焦。
                }
            }

        });
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            mCamera.stopPreview();
        }
        mCamera.release();
        mCamera = null;
    }

    //相机参数的初始化设置
    private void initCamera()
    {
        parameters= mCamera.getParameters();
        parameters.setPictureFormat(PixelFormat.JPEG);
        //parameters.setPictureSize(surfaceView.getWidth(), surfaceView.getHeight());  // 部分定制手机，无法正常识别该方法。
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);//1连续对焦
        setDispaly(parameters, mCamera);
        mCamera.setParameters(parameters);
        mCamera.startPreview();
        mCamera.cancelAutoFocus();// 2如果要实现连续的自动对焦，这一句必须加上

    }

    //控制图像的正确显示方向
    private void setDispaly(Camera.Parameters parameters,Camera camera)
    {
        if (Integer.parseInt(Build.VERSION.SDK) >= 8){
            setDisplayOrientation(camera,90);
        }
        else{
            parameters.setRotation(90);
        }

    }

    //实现的图像的正确显示
    private void setDisplayOrientation(Camera camera, int i) {
        Method downPolymorphic;
        try{
            downPolymorphic=camera.getClass().getMethod("setDisplayOrientation", new Class[]{int.class});
            if(downPolymorphic!=null) {
                downPolymorphic.invoke(camera, new Object[]{i});
            }
        }
        catch(Exception e){
            Log.e("Came_e", "图像出错");
        }
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance(int cameraId) {
        Log.d("LOG", "getCameraInstance");
        Camera c = null;
        try {
            c = Camera.open(cameraId); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            e.printStackTrace();
            Log.e("LOG", "Camera is not available");
        }
        return c; // returns null if camera is unavailable
    }

    /**
     * 得到默认相机的ID
     *
     * @return
     */
    private int getDefaultCameraId() {
        Log.d("LOG_TAG", "getDefaultCameraId");
        int defaultId = -1;

        // Find the total number of cameras available
        mNumberOfCameras = Camera.getNumberOfCameras();

        // Find the ID of the default camera
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < mNumberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            Log.d("LOG_TAG", "camera info: " + cameraInfo.orientation);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                defaultId = i;
            }
        }
        if (-1 == defaultId) {
            if (mNumberOfCameras > 0) {
                // 如果没有后向摄像头
                defaultId = 0;
            } else {
                // 没有摄像头
                Toast.makeText(getApplicationContext(), R.string.no_camera, Toast.LENGTH_LONG).show();
            }
        }
        return defaultId;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

}
