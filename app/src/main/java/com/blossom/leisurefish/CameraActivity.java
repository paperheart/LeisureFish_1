package com.blossom.leisurefish;

import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static android.widget.Toast.LENGTH_LONG;

public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private int CAMERA_TYPE = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private String name;
    private String id;
    private Camera mCamera;
    private int rotationDegree;
    private SurfaceView mSurfaceView;
    private Boolean isRecording = false;
    private MediaRecorder mMediaRecorder;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private int testdegree = 270;
    private ImageButton video_shoot ;
    private ImageButton picture_shoot;
    private ImageButton change_button;
    private Toast mtoast = null;
    private ProgressBar progressBar;
    Intent intent = null;
    //private com.blossom.leisurefish.CircleButtonView circleButtonView;
    Intent intent_1;
    boolean islong = false;
    private CountDownTimer mTimer;
    private SeekBar timebar;
    private File file_video;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.camera_activity);

        intent_1 = getIntent();
        name = intent_1.getStringExtra("USER_NAME");
        id = intent_1.getStringExtra("USER_ID");
        mCamera = getCamera(CAMERA_TYPE);
        video_shoot = findViewById(R.id.video_shoot);
        picture_shoot = findViewById(R.id.picture_shoot);
        mSurfaceView = findViewById(R.id.surView);
        change_button = findViewById(R.id.change);
        timebar = findViewById(R.id.timebar);
        progressBar = findViewById(R.id.cir);
        SurfaceHolder holder = mSurfaceView.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(this);



        findViewById(R.id.imageButton).setOnClickListener(v -> {
            CAMERA_TYPE = 1 - CAMERA_TYPE;
            releaseCameraAndPreview();
            mCamera = getCamera(CAMERA_TYPE);
            try {
                cameraResize();
                mCamera.setPreviewDisplay(mSurfaceView.getHolder());
                mCamera.startPreview();

            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        findViewById(R.id.video_shoot).setOnClickListener(v -> {
            if (isRecording) {
                video_shoot.setBackgroundResource(R.drawable.start);
                isRecording = false;
                change_button.setClickable(true);
                releaseMediaRecorder();;
                mCamera.unlock();
                mCamera.lock();
                timebar.setProgress(0);
                mTimer.cancel();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.VISIBLE);
                        progressBar.setPressed(true);
                    }
                });

                //Toast.makeText(CameraActivity.this, "Camera  "+name+" "+id, Toast.LENGTH_SHORT).show();
                intent = new Intent(CameraActivity.this,VideoPlay.class);
                intent.putExtra("URLforVideo",file_video.getAbsolutePath().toString());
                intent.putExtra("USER_NAME",name);
                intent.putExtra("USER_ID",id);
                startActivity(intent);
                finish();

                if(mtoast != null) mtoast.cancel();
                mtoast = Toast.makeText(CameraActivity.this, "over!", Toast.LENGTH_LONG);
                mtoast.show();
            } else {
                if(mtoast != null) mtoast.cancel();
                mtoast = Toast.makeText(CameraActivity.this, "start!", Toast.LENGTH_LONG);
                mtoast.show();
                video_shoot.setBackgroundResource(R.drawable.stop);
                mTimer.start();
                change_button.setClickable(false);
                mMediaRecorder = new MediaRecorder();
                //if (CAMERA_TYPE == 1)
                //mMediaRecorder.setOrientationHint(testdegree);
                mCamera.lock();
                mCamera.unlock();
                mMediaRecorder.setCamera(mCamera);
                //mMediaRecorder.setOrientationHint(testdegree);
                mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
                mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
                file_video = getOutputMediaFile(MEDIA_TYPE_VIDEO);
                mMediaRecorder.setOutputFile(file_video.toString());
                mMediaRecorder.setPreviewDisplay( mSurfaceView.getHolder().getSurface());



                switch (this.getResources().getConfiguration().orientation)
                {
                    case 2:
                        switch (CAMERA_TYPE)
                        {
                            case 1:mMediaRecorder.setOrientationHint(180);break;
                            case 0:mMediaRecorder.setOrientationHint(0);break;
                        }break;
                    case 1:
                        switch (CAMERA_TYPE)
                        {
                            case 1:mMediaRecorder.setOrientationHint(270);break;
                            case 0:mMediaRecorder.setOrientationHint(90);break;
                        }break;
                }

                isRecording = true;
                try {
                    mMediaRecorder.prepare();
                    mMediaRecorder.start();
                } catch (IOException e) {
                    releaseMediaRecorder();
                    e.printStackTrace();
                }
            }
        });
        findViewById(R.id.change).setOnClickListener(v -> {
            if(video_shoot.getVisibility() == View.VISIBLE) {
                video_shoot.setVisibility(View.INVISIBLE);
                picture_shoot.setVisibility(View.VISIBLE);
                change_button.setBackgroundResource(R.drawable.videoswitch);
            }
            else{
                video_shoot.setVisibility(View.VISIBLE);
                picture_shoot.setVisibility(View.INVISIBLE);
                change_button.setBackgroundResource(R.drawable.camera);
            }
        });

        findViewById(R.id.picture_shoot).setOnClickListener(v -> {
            mCamera.takePicture(null,null,mPicture) ;

        });

        findViewById(R.id.surView).setOnClickListener(v -> {
            auto();
            if(mtoast != null) mtoast.cancel();
            mtoast = Toast.makeText(CameraActivity.this,"auto!",Toast.LENGTH_LONG);
            mtoast.show();
        });
        initView();
    }
    private void initView() {
        if (mTimer == null) {
            mTimer = new CountDownTimer((long) (10 * 1000), 100) {

                @Override
                public void onTick(long millisUntilFinished) {
                    if (!CameraActivity.this.isFinishing()) {
                        double remainTime =  (millisUntilFinished / 100L);

                        timebar.setProgress((int)(remainTime+2));
                        //Toast.makeText(CameraActivity.this,String.valueOf((int)(remainTime*10)),Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFinish() {
                    if(mtoast != null) mtoast.cancel();
                    mtoast = Toast.makeText(CameraActivity.this, "over!", Toast.LENGTH_LONG);
                    mtoast.show();
                    video_shoot.setBackgroundResource(R.drawable.start);
                    isRecording = false;
                    change_button.setClickable(true);
                    releaseMediaRecorder();;
                    mCamera.unlock();
                    mCamera.lock();
                    timebar.setProgress(0);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.VISIBLE);
                            progressBar.setPressed(true);
                        }
                    });
                    intent = new Intent(CameraActivity.this,VideoPlay.class);
                    intent.putExtra("URLforVideo",file_video.getAbsolutePath().toString());
                    intent.putExtra("USER_NAME",name);
                    intent.putExtra("USER_ID",id);
                    startActivity(intent);
                    finish();
                }
            };

        }
    }
    private Camera.PictureCallback mPicture = (data, camera) -> {
        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
        if (pictureFile == null) {
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();
        } catch (IOException e) {
            Log.d("mPicture", "Error accessing file: " + e.getMessage());
        }
        mCamera.startPreview();
        //MediaStore.Images.Media.insertImage(getContentResolver(), BitmapFactory.decodeFile(pictureFile.getAbsolutePath()) , "title", "description");
    };
    private void startPreview(SurfaceHolder holder) {
        try {
            timebar.setClickable(false);
            cameraResize();
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    private int getCameraDisplayOrientation(int cameraId) {
        Camera.CameraInfo info =
                new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
            default:
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }
    private void releaseCameraAndPreview() {
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }
    public Camera getCamera(int position) {
        CAMERA_TYPE = position;
        if (mCamera != null) {
            releaseCameraAndPreview();
        }
        Camera cam = null;
        try {
            cam = Camera.open(position);
            rotationDegree = getCameraDisplayOrientation(position);
            cam.setDisplayOrientation(rotationDegree);

        }catch (Exception e)
        {
            if(mtoast != null) mtoast.cancel();
            mtoast = Toast.makeText(CameraActivity.this, "RuntimeError!", Toast.LENGTH_LONG);
            mtoast.show();
        }

        return cam;
    }
    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        double ASPECT_TOLERANCE = 1;
        double targetRatio = (double) h / w;
        if(h>=w) targetRatio = (double) h / w;
        else targetRatio = (double) w / h;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
       // double minDiff = (double)100;

        int targetmin = Math.min(w, h);

        for (Camera.Size size : sizes) {
            double ratio;
            int realmin = Math.min(size.height,size.width);
            if(size.width >= size.height) ratio = (double) size.width/size.height;
            else ratio = (double )size.height/size.width;
            if(realmin>=targetmin && Math.abs(ratio - targetRatio)<ASPECT_TOLERANCE)
            {
                optimalSize = size;
                ASPECT_TOLERANCE = Math.abs(ratio - targetRatio);
            }
        }
        return optimalSize;
    }
    private void cameraResize()
    {
        mSurfaceView.post(new Runnable() {
            @Override
            public void run() {

                Camera.Size size = getOptimalPreviewSize(mCamera.getParameters().getSupportedPreviewSizes(),mSurfaceView.getWidth(),mSurfaceView.getHeight());
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setPreviewSize(size.width, size.height);
                mCamera.setParameters(parameters);
                //自动调焦
                auto();
            }
        });
    }
    private void auto(){
        Camera.Parameters params = mCamera.getParameters();
        if (params.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }
        mCamera.setParameters(params);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        startPreview(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    public static File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "CameraData");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    private void releaseMediaRecorder() {
        mMediaRecorder.stop();
        mMediaRecorder.reset();
        mMediaRecorder.release();
        mMediaRecorder = null;
    }


}
