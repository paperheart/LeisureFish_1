package com.blossom.leisurefish;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import java.io.IOException;
import java.util.logging.Logger;

public class Detail extends AppCompatActivity implements MediaPlayer.OnCompletionListener {

    private ImageButton ib_pausing;
    private ImageButton ib_play;
    private TextView user_name;
    private TextView user_id;
    private TextView currentTime;
    private TextView totalTime;
    private SurfaceHolder holder;
    private MediaPlayer mediaPlayer = null;
    private SeekBar seekBar;


    private Handler handlerPlay = new Handler();
    private Handler handlerPause = new Handler();

    private final int NORMAL = 0;
    private final int PLAYING = 1;
    private final int PAUSING = 2;
    private final int STOPING = 3;

    private boolean touchAble = true;
    private int state = NORMAL;
    private boolean isStopUpdatingProgress = false;

    private Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        user_name = findViewById(R.id.tv_name);
        user_id   = findViewById(R.id.tv_id);
        ib_pausing = findViewById(R.id.ib_pause);
        ib_play = findViewById(R.id.ib_play);
        SurfaceView surfaceView = findViewById(R.id.sv);
        holder = surfaceView.getHolder();
        seekBar = findViewById(R.id.sb_1);


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

        intent = getIntent();
        user_id.setText(user_id.getText().toString() + intent.getStringExtra( "USER_ID"));
        user_name.setText(user_name.getText().toString() +
                intent.getStringExtra(user_name.getText().toString() + "USER_NAME"));


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
        String url = intent.getStringExtra("VIDEO_URL");
        //<TODO>二次建对象mediaPlayer是什么操作...
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDisplay(holder);

            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();
            mediaPlayer.start();

            mediaPlayer.setOnCompletionListener(this);

            state = PLAYING;

            int duration = mediaPlayer.getDuration();
            seekBar.setMax(duration);
            seekBar.setEnabled(false);
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


}