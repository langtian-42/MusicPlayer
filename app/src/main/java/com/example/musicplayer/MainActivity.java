package com.example.musicplayer;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Button btn_play,btn_pause,btn_continue_play,btn_exit;
    private TextView song_name;
    private ImageView iv_disk;
    private static SeekBar musicProgressBar;
    private static TextView currentTv;
    private static TextView totalTv;
    private ObjectAnimator animator;
    private MusicService.MusicControl control;


    private ServiceConnection conn=new ServiceConnection() {
        private Object service;
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            control=(MusicService.MusicControl) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

    }

    public void init(){
        btn_play=(Button)findViewById(R.id.btn_play);
        btn_pause=(Button) findViewById(R.id.btn_pause);
        btn_continue_play=(Button) findViewById(R.id.btn_continue);
        btn_exit=(Button)findViewById(R.id.btn_exit);
        iv_disk=findViewById(R.id.iv_music);
        musicProgressBar=findViewById(R.id.sb);
        currentTv=findViewById(R.id.tv_progress);
        totalTv=findViewById(R.id.tv_total);

       OnClick onClick=new OnClick();

        btn_play.setOnClickListener(onClick);
        btn_pause.setOnClickListener(onClick);
        btn_continue_play.setOnClickListener(onClick);
        btn_exit.setOnClickListener(onClick);
        animator=ObjectAnimator.ofFloat(iv_disk,"rotation",0f,360.0f);
        animator.setDuration(10000);//动画旋转一周的时间为10秒
        animator.setInterpolator(new LinearInterpolator());//匀速
        animator.setRepeatCount(-1);//-1表示设置动画无限循环


        Intent intent=new Intent(getApplicationContext(),MusicService.class);
        bindService(intent,conn,BIND_AUTO_CREATE);

        musicProgressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress==seekBar.getMax()){
                    animator.pause();
                }if(fromUser){
                    control.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                control.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                control.resume();
            }
        });
    }

    class OnClick implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btn_play:


                    control.play();
                    animator.start();
                    break;
                case R.id.btn_pause:
                    control.pause();
                    animator.pause();
                    break;
                case R.id.btn_continue:
                    control.resume();
                    animator.resume();

                    break;
                case R.id.btn_exit:
                    finish();
                    break;
            }
        }
    }


    @Override
    protected void onDestroy() {
        control.stop();
        unbindService(conn);
        super.onDestroy();
    }


    //handler机制，线程间的通信，获取到一个信息，然后把这个信息返回
    public static Handler handler=new Handler(){//创建消息处理器对象
        //在主线程中处理从子线程发送过来的消息
        @Override
        public void handleMessage(@NonNull Message msg){
            super.handleMessage(msg);
            Bundle bundle=msg.getData();//获取从子线程发送过来的音乐播放进度
            //获取当前进度currentPosition和总时长duration
            int duration=bundle.getInt("duration");
            int currentPosition=bundle.getInt("currentPosition");
            //对进度条进行设置
            musicProgressBar.setMax(duration);
            musicProgressBar.setProgress(currentPosition);
            String totalTime=msToMinSec(duration);
            String currentTime=msToMinSec(currentPosition);
            totalTv.setText(totalTime);
            currentTv.setText(currentTime);
        }
    };

    @NonNull
    public static String msToMinSec(int ms) {
        int sec = ms / 1000;
        int min = sec / 60;
        sec -= min * 60;
        return String.format("%02d:%02d", min, sec);
    }
//文字滚动
    public class MyTextView extends androidx.appcompat.widget.AppCompatTextView {
        public MyTextView(Context context) {
            super(context);
        }

        public MyTextView(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
        }

        public MyTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        public boolean isFocused() {
            return true;
        }
    }
}
