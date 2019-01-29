package com.blossom.leisurefish;

import android.content.Intent;
import android.gesture.GestureOverlayView;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;
import android.view.GestureDetector.OnGestureListener;

import com.blossom.leisurefish.newtork.IMiniDouyinService;
import com.blossom.leisurefish.utils.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import beans.PostVideoResponse;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.blossom.leisurefish.CameraActivity.MEDIA_TYPE_IMAGE;
import static com.blossom.leisurefish.CameraActivity.getOutputMediaFile;

public class VideoPlay extends AppCompatActivity implements MediaPlayer.OnCompletionListener, View.OnTouchListener,OnGestureListener {
    private String name,id;
    private ImageButton ib_pausing;
    private ImageButton ib_play;
    private SurfaceHolder holder;
    private MediaPlayer mediaPlayer = null;
    private SeekBar seekBar;
    private Uri mSelectedVideo;
    public Uri mSelectedImage;
    private Handler handlerPlay = new Handler();
    private Handler handlerPause = new Handler();
    private ProgressBar progressBar;
    private final int NORMAL = 0;
    private final int PLAYING = 1;
    private final int PAUSING = 2;
    private final int STOPING = 3;
    private GestureDetector mGestureDetector;
    private boolean touchAble = true;
    private int state = NORMAL;
    private boolean isStopUpdatingProgress = false;
    public Intent intent;
    public String url;
    private RelativeLayout relativeLayout;
    private boolean Flingable = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_video);
        ib_pausing = findViewById(R.id.ib_pause);
        ib_play = findViewById(R.id.ib_play);
        SurfaceView surfaceView = findViewById(R.id.sv);
        holder = surfaceView.getHolder();
        seekBar = findViewById(R.id.sb_1);
        intent=getIntent();
        progressBar = findViewById(R.id.progress_circular);

        url = intent.getStringExtra("URLforVideo");
        name = getIntent().getStringExtra("USER_NAME");
        id = getIntent().getStringExtra("USER_ID");

        mSelectedVideo = Uri.parse("file://"+url);

        ib_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ib_play.setVisibility(View.INVISIBLE);
                touchAble = true;
                start();
            }
        });
        ib_pausing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ib_pausing.setVisibility(View.INVISIBLE);
                ib_play.setVisibility(View.VISIBLE);
                touchAble = true;
                pause();
            }
        });
        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(state == PLAYING){
                    if(ib_pausing.getVisibility() == View.INVISIBLE){
                        ib_pausing.setVisibility(View.VISIBLE);
                        handlerPause.postDelayed(runnablePause,2000);
                    }
                    else{
                        ib_pausing.setVisibility(View.INVISIBLE);
                    }
                }
                else{
                    if(ib_play.getVisibility() == View.INVISIBLE){
                        ib_play.setVisibility(View.VISIBLE);
                        handlerPlay.postDelayed(runnablePlay,2000);
                    }
                    else{
                        ib_play.setVisibility(View.INVISIBLE);
                    }

                }
            }
        });
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        relativeLayout = findViewById(R.id.RL);
        mGestureDetector = new GestureDetector(this);
        surfaceView.setOnTouchListener(this);
        surfaceView.setLongClickable(true);

    }

    public void start(){
        if(mediaPlayer != null){
            isStopUpdatingProgress = false;
            mediaPlayer.start();
            state = PLAYING;
            return;
        }
        isStopUpdatingProgress = false;
        play();
    }


    public void play(){

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDisplay(holder);

            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();
            mediaPlayer.start();

            mediaPlayer.setOnCompletionListener(this);

            state = PLAYING;

            seekBar.setMax(mediaPlayer.getDuration());
            isStopUpdatingProgress = false;
            new Thread(new UpdateProgressRunnable()).start();
        } catch (IOException e) {

            e.printStackTrace();
        }

    }

    public void pause(){
        if(mediaPlayer != null && state == PLAYING){
            mediaPlayer.pause();
            state = PAUSING;

            isStopUpdatingProgress = true;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        state = PLAYING;
        mediaPlayer.start();
    }


    Runnable runnablePause = new Runnable() {
        @Override
        public void run() {
            ib_pausing.setVisibility(View.INVISIBLE);
        }
    };

    Runnable runnablePlay = new Runnable() {
        @Override
        public void run() {
            ib_play.setVisibility(View.INVISIBLE);
        }
    };

    @Override
    protected void onDestroy() {
        // 在activity结束的时候回收资源
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        isStopUpdatingProgress = true;
        super.onDestroy();
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        Log.d("Text","Touched");
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if(velocityY < -5000 && Math.abs(velocityX)<6000 && Flingable)
        {
            Flingable = false;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.VISIBLE);
                }
            });
            MediaMetadataRetriever media = new MediaMetadataRetriever();
            media.setDataSource(url);
            Bitmap bitmap = media.getFrameAtTime();
            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            try {
                FileOutputStream out = new FileOutputStream(pictureFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 50, out);
                out.flush();
                out.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mSelectedImage = Uri.fromFile(pictureFile);
            postVideo();
        }
        Log.d("Text",String.valueOf(velocityX)+" "+String.valueOf(velocityY));
        return false;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d("Text","touched on onTouch");
        return mGestureDetector.onTouchEvent(event);
    }

    class UpdateProgressRunnable implements Runnable {

        @Override
        public void run() {
            while (!isStopUpdatingProgress) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                seekBar.setProgress(currentPosition);
                SystemClock.sleep(100);
            }

        }

    }
    private void postVideo() {

        Retrofit retrofit =new Retrofit.Builder()
                .baseUrl("http://10.108.10.39:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        final IMiniDouyinService iMiniDouyinService = retrofit.create(IMiniDouyinService.class);
        Log.d("aaa","2");
        new Thread(){
            @Override public void run(){

                Uri myuri = mSelectedVideo;
                Call<PostVideoResponse> call = iMiniDouyinService.creatVideo(id,name,
                        getMultipartFromUri("cover_image",mSelectedImage),
                        getMultipartFromUri("video",mSelectedVideo));

                call.enqueue(new Callback<PostVideoResponse>() {

                    @Override
                    public void onResponse(Call<PostVideoResponse> call, Response<PostVideoResponse> response) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(VideoPlay.this, "Video "+name+" "+id, Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.INVISIBLE);
                                Flingable = true;
                            }
                        });

                    }

                    @Override
                    public void onFailure(Call<PostVideoResponse> call, Throwable t) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(VideoPlay.this, "上传失败", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.INVISIBLE);
                                Flingable = true;
                            }
                        });

                    }
                });
            }

        }.start();



    }
    private MultipartBody.Part getMultipartFromUri(String name, Uri uri) {
        // if NullPointerException thrown, try to allow storage permission in system settings
        File f = new File(ResourceUtils.getRealPath(VideoPlay.this, uri));
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), f);
        Log.d("aaa","on");
        return MultipartBody.Part.createFormData(name, f.getName(), requestFile);
    }

}