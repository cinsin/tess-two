package com.lencity.cmcc_ocr.ui;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.lencity.cmcc_ocr.R;
import com.lencity.cmcc_ocr.bo.CameraPreview;
import com.lencity.cmcc_ocr.bo.ImageHandler;
import com.lencity.cmcc_ocr.utils.Constant;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see
 */
public class VideoIdentifyActivity extends Activity{

    private Camera mCamera;
    private CameraPreview mPreview;

    int mNumberOfCameras;
    int mCameraCurrentlyLocked;

    // The first rear facing camera
    int mDefaultCameraId;

    public static final int MEDIA_TYPE_IMAGE = 1;

    TextView identifyResultTextView;
    ImageView identifySrcImageView;
    ImageView identifyResultImageView;
    Bitmap bitmapSrc;
    Bitmap bitmapDst;

    boolean isSwitchOn = false;

    protected Button startCaptureButton;

    // TTS related
    public static final String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/cmcc-ocr/";
    private static final String TAG = "cmcc-ocr";
    public static final String lang = "eng";
    //public static final String lang = "chi_sim";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Constant.LOG_TAG = "VideoIdentifyActivity";
        Log.d(Constant.LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);

        //=============================================
        String[] paths = new String[] { DATA_PATH, DATA_PATH + "tessdata/" };

        for (String path : paths) {
            File dir = new File(path);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.v(TAG, "ERROR: Creation of directory " + path + " on sdcard failed");
                    return;
                } else {
                    Log.v(TAG, "Created directory " + path + " on sdcard");
                }
            }

        }

        // lang.traineddata file with the app (in assets folder)
        // You can get them at:
        // http://code.google.com/p/tesseract-ocr/downloads/list
        // This area needs work and optimization
        /*if (!(new File(DATA_PATH + "tessdata/" + lang + ".traineddata")).exists()) {
            try {

                AssetManager assetManager = getAssets();
                InputStream in = assetManager.open("tessdata/" + lang + ".traineddata");
                //GZIPInputStream gin = new GZIPInputStream(in);
                OutputStream out = new FileOutputStream(DATA_PATH
                        + "tessdata/" + lang + ".traineddata");

                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                //while ((lenf = gin.read(buff)) > 0) {
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                //gin.close();
                out.close();

                Log.v(TAG, "Copied " + lang + " traineddata");
            } catch (IOException e) {
                Log.e(TAG, "Was unable to copy " + lang + " traineddata " + e.toString());
            }
        }*/
        //=============================================



        // 无标题栏的窗口
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // 设置布局
        setContentView(R.layout.activity_video_identify);
        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this);

        // Video frame src
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);

        // 将相机预览图加入帧布局里面
        preview.addView(mPreview, 0);

        identifyResultTextView = (TextView) findViewById(R.id.identifyResultTxt);

        // Source picture taken
        identifyResultImageView = (ImageView) findViewById(R.id.identifyResultImageView);

        // Capture switch button and its listener
        startCaptureButton = (Button) findViewById(R.id.button_capture);
        startCaptureButton.setBackgroundColor(Color.argb(100, 100, 100, 100));
        startCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(Constant.LOG_TAG, "Start capture button clicked...");

                // Change button label
                isSwitchOn = !isSwitchOn;

                startCaptureButton.setEnabled(false);
                startCaptureButton.setTextColor(Color.RED);
                startCaptureButton.setText("处理中...");

                /*if (isSwitchOn) {
                    startCaptureButton.setText("停止");
                    startCaptureButton.setTextColor(Color.RED);
                    //tts.speak("识别开始", TextToSpeech.QUEUE_FLUSH, null);
                } else {
                    startCaptureButton.setText("启动");
                    startCaptureButton.setTextColor(Color.GREEN);
                    //tts.speak("识别结束", TextToSpeech.QUEUE_FLUSH, null);
                }*/

                // Identification interval
                mCamera.takePicture(null, null, mPicture);
                Log.d(Constant.LOG_TAG, "Picture taken...");

                // Capture picture and send to api
                /*new Thread(new Runnable() {
                    @Override
                    public void run() {

                        while (isSwitchOn) {
                            // Identification interval
                            mCamera.takePicture(null, null, mPicture);
                            Log.d(Constant.LOG_TAG, "Picture took...");
                            try {
                                Thread.sleep(1000l);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();*/
            }
        });

        // 得到默认的相机ID
        mDefaultCameraId = getDefaultCameraId();
        mCameraCurrentlyLocked = mDefaultCameraId;

    }


    /**
     * Callback on picture taken
     */
    private PictureCallback mPicture = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(Constant.LOG_TAG, "PictureCallback");

            // Captured picture is in landscape mode, need to transfer to portrait mode
            // byte[] -> BitMap -> landscape to portrait -> byte[]
            if (data != null) {
                bitmapSrc = BitmapFactory.decodeByteArray(data, 0, data.length);//data是字节数据，将其解析成位图
                // Stop preview
                mCamera.stopPreview();
                // Start preview after picture taken
                mCamera.startPreview();
            }
            bitmapSrc = ImageHandler.getRotateBitmap(bitmapSrc, 90.0f);

            // Save taken picture to phone/sd card storage
            File fileName = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            try {
                FileOutputStream fos = new FileOutputStream(fileName);
                Log.d(Constant.LOG_TAG, "File saved to dir: /storage/sdcard0/Pictures/cmcc-ocr/" + fileName);
                bitmapSrc.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(Constant.LOG_TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(Constant.LOG_TAG, "Error accessing file: " + e.getMessage());
            }

            /*
                屏幕大小与拍出来的照片大小不同，测试机屏幕H/W=1280/720，拍出来的照片大小H'/W'=640/480
                需先算出屏幕比例，按照比例计算图片中的扫描框大小
                比例计算方法: Xb=W/W', Yb=H/H'
                假设：
                屏幕大小中扫描框坐上角(X1,Y1)，右下角(X2，Y2)
                图片中标扫描框左上角(X1',Y1')，右下角(X2',Y2')
                则有：X1'=X1/Xb，Y1'=Y1/Yb，X2'=X2/Xb，Y2'=Y2/Yb
            */
            Constant.BITMAP_WIDTH = bitmapSrc.getWidth();
            Constant.BITMAP_HEIGHT = bitmapSrc.getHeight();
            Constant.SCAN_AREA_Xb = Constant.CANVAS_WIDTH / Constant.BITMAP_WIDTH;
            Constant.SCAN_AREA_Yb = Constant.CANVAS_HEIGHT / Constant.BITMAP_HEIGHT;

            Constant.BITMAP_SCAN_AREA_X = (int)(Constant.CANVAS_SCAN_AREA_X / Constant.SCAN_AREA_Xb);
            Constant.BITMAP_SCAN_AREA_Y = (int)(Constant.CANVAS_SCAN_AREA_Y / Constant.SCAN_AREA_Yb);
            Constant.BITMAP_SCAN_AREA_WIDTH = (int)(Constant.CANVAS_SCAN_AREA_WIDTH / Constant.SCAN_AREA_Xb);
            Constant.BITMAP_SCAN_AREA_HEIGHT = (int)(Constant.CANVAS_SCAN_AREA_HEIGHT / Constant.SCAN_AREA_Yb);

            // Crop bitmap image
            bitmapDst = Bitmap.createBitmap(bitmapSrc,
                    (int)Constant.BITMAP_SCAN_AREA_X + 36, (int)Constant.BITMAP_SCAN_AREA_Y,
                    (int)Constant.BITMAP_SCAN_AREA_WIDTH - 74, (int)Constant.BITMAP_SCAN_AREA_HEIGHT);

            // Enlarge bitmap
            Matrix matrix = new Matrix();
            matrix.postScale(2.0f,2.0f); //长和宽放大缩小的比例
            bitmapDst = Bitmap.createBitmap(bitmapDst,0,0,bitmapDst.getWidth(),bitmapDst.getHeight(),matrix,true);


            // Save taken picture to phone/sd card storage
            fileName = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            try {
                FileOutputStream fos = new FileOutputStream(fileName);
                Log.d(Constant.LOG_TAG, "File saved to dir: /storage/sdcard0/Pictures/cmcc-ocr/" + fileName);
                bitmapDst.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(Constant.LOG_TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(Constant.LOG_TAG, "Error accessing file: " + e.getMessage());
            }

            // Show the cropped part of the picture
            identifyResultImageView.setImageBitmap(bitmapDst);
            identifyResultImageView.setVisibility(View.VISIBLE);

            // OCR via TessApi
            TessBaseAPI baseApi = new TessBaseAPI();
            baseApi.setDebug(true);
            baseApi.init(DATA_PATH, lang);
            baseApi.setImage(bitmapDst);
            //baseApi.setVariable("classify_bln_numeric_mode", "1"); //set numeric-only mode
            String recognizedText = baseApi.getUTF8Text();
            Log.i("#RESULT#", recognizedText);
            baseApi.end();

            String keyText = prettyPrint(recognizedText);
            String displayText = "关键值：" + keyText + "\n原始值：" + recognizedText;

            // Show the recognize result
            identifyResultTextView.setText(displayText);

            // Reset take picture button
            startCaptureButton.setEnabled(true);
            startCaptureButton.setTextColor(Color.GREEN);
            startCaptureButton.setText("检测");
        }
    };

    /**
     * 如果识别的对象是标签，则取标签最后4位数字作为识别结果
     * @param str
     * @return
     */
    private String prettyPrint(String str) {

        if(str.contains("****")) {
            int index = str.indexOf("****") + 4;
            str = str.substring(index);
            if(str.length() >= 4) {
                str = str.substring(0,4);
            }
        }
        return str;
    }

    @Override
    protected void onResume() {
        Log.d(Constant.LOG_TAG, "onResume");
        super.onResume();

        // Open the default i.e. the first rear facing camera.
        mCamera = getCameraInstance(mCameraCurrentlyLocked);
        //set camera to continually auto-focus
        Camera.Parameters params = mCamera.getParameters();
        params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE); // camera auto focus

        /*Camera.Size bestPreviewSize = determineBestPreviewSize(params);
        Camera.Size bestPictureSize = determineBestPictureSize(params);
        params.setPreviewSize(bestPreviewSize.width, bestPreviewSize.height);
        params.setPictureSize(bestPictureSize.width, bestPictureSize.height);*/

        mCamera.setParameters(params);

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
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Log.d(Constant.LOG_TAG, "onWindowFocusChanged, checking camera status...");
        if (isSwitchOn) {
            Log.d(Constant.LOG_TAG, "Camera is on, will be released immediately...");
            if (mCamera != null) {
                mPreview.setCamera(null);
                Log.d(Constant.LOG_TAG, "onPause --> Realease camera");
                mCamera.release();
                mCamera = null;
            }
        }
    }

    /**
     * 得到默认相机的ID
     *
     * @return
     */
    private int getDefaultCameraId() {
        Log.d(Constant.LOG_TAG, "getDefaultCameraId");
        int defaultId = -1;

        // Find the total number of cameras available
        mNumberOfCameras = Camera.getNumberOfCameras();

        // Find the ID of the default camera
        CameraInfo cameraInfo = new CameraInfo();
        for (int i = 0; i < mNumberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            Log.d(Constant.LOG_TAG, "camera info: " + cameraInfo.orientation);
            if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
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

    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance(int cameraId) {
        Log.d(Constant.LOG_TAG, "getCameraInstance");
        Camera c = null;
        try {
            c = Camera.open(cameraId); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            e.printStackTrace();
            Log.e(Constant.LOG_TAG, "Camera is not available");
        }
        return c; // returns null if camera is unavailable
    }

    public static final int MEDIA_TYPE_VIDEO = 2;

    /**
     * Create a File for saving an image or video
     */
    private static File getOutputMediaFile(int type) {
        Log.d(Constant.LOG_TAG, "getOutputMediaFile");
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = null;
        try {
            // This location works best if you want the created images to be
            // shared
            // between applications and persist after your app has been
            // uninstalled.
            mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "cmcc-ocr");

            Log.d(Constant.LOG_TAG, "Successfully created mediaStorageDir: " + mediaStorageDir);

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(Constant.LOG_TAG, "Error in Creating mediaStorageDir: " + mediaStorageDir);
        }

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                // 在SD卡上创建文件夹需要权限：
                // <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
                Log.d(Constant.LOG_TAG, "failed to create directory, check if you have the WRITE_EXTERNAL_STORAGE permission");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        timeStamp = timeStamp + Math.random();
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    /**
     * Check if this device has a camera
     */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

}
