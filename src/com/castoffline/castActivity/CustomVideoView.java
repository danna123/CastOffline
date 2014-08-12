/*Copyright 2014 Divya Anna Marcus

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/

/*
* Reference: http://stackoverflow.com/questions/7934556/event-for-videoview-playback-state-or-mediacontroller-play-pause/8046523#8046523
*
*/

/* Identifying the event in videoview - playback state */
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
