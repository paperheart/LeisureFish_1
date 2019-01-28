package com.blossom.leisurefish;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.temporal.IsoFields;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

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
    private ProgressBar progressBar;
    private int pre = 0,now = 0;
    private Handler handlerPlay = new Handler();
    private Handler handlerPause = new Handler();

    private final int NORMAL = 0;
    private final int PLAYING = 1;
    private final int PAUSING = 2;
    private final int STOPING = 3;
    private TimerTask task = null;
    private boolean touchAble = true;
    private int state = NORMAL;
    private boolean isStopUpdatingProgress = false;
    private boolean isFirstTime = true;
    private ImageButton love;
    private Intent intent;
    private  Timer mtimer = new Timer();

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
        progressBar = findViewById(R.id.pb_1);
        love = findViewById(R.id.ib_love);

        ib_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ib_play.setVisibility(INVISIBLE);
                if(isFirstTime){
                    isFirstTime = false;
                    progressBar.setVisibility(VISIBLE);
                }
                touchAble = true;
                start();
            }
        });
        ib_pausing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ib_pausing.setVisibility(INVISIBLE);
                ib_play.setVisibility(VISIBLE);
                touchAble = true;
                pause();
            }
        });
        surfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                pre = now;
                String timeStamp = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());

                timeStamp = timeStamp.substring(8,16);
                now = Integer.valueOf(timeStamp);
                if(Math.abs(now - pre) <= 50)
                {
                    mtimer.cancel();
                    task.cancel();
                    mtimer = new Timer();
                    love.setX(event.getX()-100);
                    love.setY(event.getY()-100);
                    love.setVisibility(VISIBLE);
                    new Handler().postDelayed(new Runnable(){
                        public void run() {
                            love.setVisibility(INVISIBLE);
                        }
                    },2000);

                }
                else
                {

                        task = new TimerTask() {
                            public void run() {
                                if(state == PLAYING){
                                    if(ib_pausing.getVisibility() == INVISIBLE){

                                        handlerPause.post(runnableToPause);
                                        handlerPause.postDelayed(runnablePause,2000);
                                    }
                                    else{
                                        handlerPause.post(runnablePause);
                                    }
                                }
                                else{
                                    if(ib_play.getVisibility() == INVISIBLE){
                                        handlerPlay.post(runnableToPlay);
                                        handlerPlay.postDelayed(runnablePlay,2000);
                                    }
                                    else{
                                        handlerPlay.post(runnablePlay);
                                    }

                                }
                            }
                        };

                    mtimer = new Timer();
                    mtimer.schedule(task, 200);
                }
                return false;
            }
        });

        intent = getIntent();
        user_id.setText(user_id.getText().toString() + intent.getStringExtra( "USER_ID"));
        user_name.setText(user_name.getText().toString() +
                intent.getStringExtra( "USER_NAME"));


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


            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    progressBar.setVisibility(INVISIBLE);
                }
            });
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
        //Toast.makeText(this,"Finished,play again",Toast.LENGTH_SHORT).show();
        state = PLAYING;
        mediaPlayer.start();
    }


    Runnable runnablePause = new Runnable() {
        @Override
        public void run() {
            ib_pausing.setVisibility(INVISIBLE);
        }
    };
    Runnable runnableToPause = new Runnable() {
        @Override
        public void run() {
            ib_pausing.setVisibility(VISIBLE);
        }
    };

    Runnable runnablePlay = new Runnable() {
        @Override
        public void run() {
            ib_play.setVisibility(INVISIBLE);
        }
    };
    Runnable runnableToPlay = new Runnable() {
        @Override
        public void run() {
            ib_play.setVisibility(VISIBLE);
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