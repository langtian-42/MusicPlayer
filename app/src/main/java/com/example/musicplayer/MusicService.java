package com.example.musicplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;

import java.util.Timer;
import java.util.TimerTask;

public class MusicService extends Service {
    private MediaPlayer mediaPlayer;
    private Timer timer;//时钟对象
    public   MusicService(){ }
    @Override
    public IBinder onBind(Intent intent) {

        return new MusicControl();//绑定服务，把音乐控制类实例化
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer=new MediaPlayer();//创建音乐播放器对象
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mediaPlayer==null)return;
        if(mediaPlayer.isPlaying())mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer=null;
    }

    //添加计时器用于设置音乐播放器中的播放进度条
    public void addTimer(){
        //如果timer不存在，也就是没有引用实例
        if(timer==null){
            //创建计时器对象
            timer=new Timer();
            TimerTask task=new TimerTask() {
                @Override
                public void run() {
                    int duration=mediaPlayer.getDuration();//获取歌曲总时长
                    int currentPosition=mediaPlayer.getCurrentPosition();//获取播放进度
                    Message msg=MainActivity.handler.obtainMessage();//创建消息对象
                    //将音乐的总时长和播放进度封装至bundle中
                    Bundle bundle=new Bundle();
                    bundle.putInt("duration",duration);
                    bundle.putInt("currentPosition",currentPosition);
                    //再将bundle封装到msg消息对象中
                    msg.setData(bundle);
                    //最后将消息发送到主线程的消息队列
                  MainActivity.handler.sendMessage(msg);
                }
            };
            //开始计时任务后的5毫秒，第一次执行task任务，以后每500毫秒（0.5s）执行一次
            timer.schedule(task,5,500);
        }
    }
    class MusicControl extends Binder {
        public void play() {
            mediaPlayer.reset();
            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.one );
            mediaPlayer.start();
            addTimer();
        }

        public void pause() {
            mediaPlayer.pause();
        }
        public void resume() {
            mediaPlayer.start();
        }
        public void stop() {
            mediaPlayer.stop();
            mediaPlayer.release();
            try{timer.cancel();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        //打带
        public void seekTo(int ms) {
            mediaPlayer.seekTo(ms);
        }
    }
}

