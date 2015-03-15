package edu.my.rstetsenko.zenua.activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.VideoView;

import edu.my.rstetsenko.zenua.Constants;
import edu.my.rstetsenko.zenua.R;
import edu.my.rstetsenko.zenua.Utility;
import edu.my.rstetsenko.zenua.fragments.ExchangeRateFragment;


public class MainActivity extends ActionBarActivity {
//    private static final String VIDEO_POSITION = "video_position";
//    private static final String PLAYER_STATE = "player_state";

    private VideoView videoView;
    private MenuItem soundItem;
//    private int videoPosition = 0;
    private MediaPlayer mediaPlayer;
    private boolean isPlayerPrepared = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        videoView = (VideoView)findViewById(R.id.video_view);
        videoView.setVideoURI(getVideoUri());
        videoView.setOnPreparedListener(preparedListener);
        videoView.setOnCompletionListener(onCompletionListener);
        LogMessage("onCreate");//, videoView setVideoURI, listeners");
        mediaPlayer = MediaPlayer.create(this, R.raw.wawe49s);
        isPlayerPrepared = false;
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                isPlayerPrepared = true;
                if (Utility.isSoundOn()) {
                    mediaPlayer.start();
                }
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                isPlayerPrepared = true;
                if (Utility.isSoundOn()) {
                    mediaPlayer.start();
                }
            }
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ExchangeRateFragment())
                    .commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Utility.isSoundOn() && isPlayerPrepared && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
//        videoView.resume();
        LogMessage("onStart");//, videoView.resume()");
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
//        isPlayerPrepared = savedInstanceState.getBoolean(PLAYER_STATE, false);
//        videoPosition = savedInstanceState.getInt(VIDEO_POSITION, 0);
        LogMessage("onRestoreInstanceState");//, videoView.seekTo(videoPosition), videoPosition = " + videoPosition);
//        videoView.seekTo(videoPosition);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Utility.isSoundOn() && isPlayerPrepared && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
        LogMessage("onResume");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        soundItem = menu.findItem(R.id.sound);
        updateSoundItem();
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
//        videoPosition = videoView.getCurrentPosition();
        LogMessage("onPause");//, videoView.getCurrentPosition(), videoPosition = " + videoPosition);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
//        savedInstanceState.putInt(VIDEO_POSITION, videoPosition);
//        savedInstanceState.putBoolean(PLAYER_STATE, isPlayerPrepared);
        LogMessage("onSaveInstanceState");//, videoView.pause(), PUT videoPosition = "+ videoPosition + " current pos:" + videoView.getCurrentPosition());
//        videoView.pause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
//        if (videoView != null && videoView.isPlaying()) {
//            videoView.pause();
//        }
        LogMessage("onStop, videoView.pause()"); //videoView.getCurrentPosition() - will be 0 here
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (videoView != null){
            videoView.suspend();
            videoView = null;
        }
        LogMessage("onDestroy");
    }

    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
//            mp.reset();
//            videoView.setVideoURI(getVideoUri());
            videoView.start();
            LogMessage("onCompletion, videoView.start()");
        }
    };

    private MediaPlayer.OnPreparedListener preparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            videoView.start();
            LogMessage("onPrepared, videoView.start()");
        }
    };

    private Uri getVideoUri(){
        return Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.wawe1);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.sound:
                Utility.toggleSound();
                if (Utility.isSoundOn() && isPlayerPrepared && !mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                }
                if (!Utility.isSoundOn() && mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
                updateSoundItem();
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateSoundItem() {
        if (Utility.isSoundOn()){
            soundItem.setTitle(getString(R.string.sound_on));
            soundItem.setIcon(R.drawable.ic_volume_on);
        } else {
            soundItem.setTitle(getString(R.string.sound_off));
            soundItem.setIcon(R.drawable.ic_volume_off);
        }
    }

    private void LogMessage(String msg) {
        Log.d(Constants.LOG_TAG, msg);
    }
}
