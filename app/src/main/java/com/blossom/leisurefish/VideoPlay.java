package com.blossom.leisurefish;

import android.content.Intent;
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
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

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

public class VideoPlay extends AppCompatActivity implements MediaPlayer.OnCompletionListener {

    private ImageButton ib_pausing;
    private ImageButton ib_play;
    private SurfaceHolder holder;
    private MediaPlayer mediaPlayer = null;
    private SeekBar seekBar;
    private Uri mSelectedVideo;
    public Uri mSelectedImage;
    private Handler handlerPlay = new Handler();
    private Handler handlerPause = new Handler();

    private final int NORMAL = 0;
    private final int PLAYING = 1;
    private final int PAUSING = 2;
    private final int STOPING = 3;

    private boolean touchAble = true;
    private int state = NORMAL;
    private boolean isStopUpdatingProgress = false;
    private ImageButton post;
    public Intent intent;
    public String url;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_video);
        ib_pausing = findViewById(R.id.ib_pause);
        ib_play = findViewById(R.id.ib_play);
        SurfaceView surfaceView = findViewById(R.id.sv);
        holder = surfaceView.getHolder();
        seekBar = findViewById(R.id.sb_1);
        post = findViewById(R.id.post);
        intent=getIntent();
        url = intent.getStringExtra("URLforVideo");
        mSelectedVideo = Uri.parse("file://"+url);

        ib_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ib_play.setVisibility(View.INVISIBLE);
                touchAble = true;
                start();
            }
        });
        post.setOnClickListener(v -> {
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
        Toast.makeText(this,"Finished,play again",Toast.LENGTH_SHORT).show();
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

    class UpdateProgressRunnable implements Runnable {

        @Override
        public void run() {
            while (!isStopUpdatingProgress) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                seekBar.setProgress(currentPosition);
                SystemClock.sleep(1000);
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
                Call<PostVideoResponse> call = iMiniDouyinService.creatVideo("1120170646","王星煜",
                        getMultipartFromUri("cover_image",mSelectedImage),
                        getMultipartFromUri("video",mSelectedVideo));

                call.enqueue(new Callback<PostVideoResponse>() {

                    @Override
                    public void onResponse(Call<PostVideoResponse> call, Response<PostVideoResponse> response) {
                        Log.d("aaa","4");
                        runOnUiThread(
                                ()->Toast.makeText(VideoPlay.this, "上传成功", Toast.LENGTH_SHORT).show()
                        );

                    }

                    @Override
                    public void onFailure(Call<PostVideoResponse> call, Throwable t) {
                        runOnUiThread(
                                ()->Toast.makeText(VideoPlay.this, "上传失败", Toast.LENGTH_SHORT).show()
                        );
                        runOnUiThread(
                                ()-> Toast.makeText(VideoPlay.this,"shibai",Toast.LENGTH_SHORT).show()
                        );

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