package com.castoffline.castActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.util.AttributeSet;
import android.widget.VideoView;

public class CustomVideoView extends VideoView {

    private PlayPauseListener mListener;

    public CustomVideoView(Context context) {
        super(context);
    }

    public CustomVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    public void setPlayPauseListener(PlayPauseListener listener) {
        mListener = listener;
    }
    
    

    @Override
    public void pause() {
        super.pause();
        if (mListener != null) {
            mListener.onPause();
        }
    }

    @Override
    public void start() {
        super.start();
        if (mListener != null) {
            mListener.onPlay();
        }
    }
    
    
    @Override
    public void seekTo(int pos)
    {
        super.seekTo(pos);

        if (mListener != null)
        {
        	mListener.onSeekChanged(pos);
        }
    }
    public static interface PlayPauseListener {
        void onPlay();
        //void onResume();
        void onSeekChanged(int currentTime);
		void onPause();
    }
	


}