package com.castoffline.castActivity;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import com.castoffline.mediaactivity.AudioPlayerActivity.Song;
import com.castoffline.mediaactivity.ImageGrid.Image;
import com.castoffline.mediaactivity.VideoPlayerActivity.Video;
import com.castoffline.R;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.Cast.ApplicationConnectionResult;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.gms.cast.RemoteMediaPlayer.MediaChannelResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;

import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.support.v7.media.MediaRouter.RouteInfo;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.Toast;
import android.widget.VideoView;

public class CastMedia extends ActionBarActivity implements MediaPlayerControl{

	String mediatype;
	MediaRouter mMediaRouter;
	MediaMetadata mediaMetadata;
	public MediaRouteSelector mMediaRouteSelector;
	public MediaRouter.Callback mMediaRouterCallback;
	public Cast.Listener mCastListener;
	public webserver mediaserver;
	public GoogleApiClient mApiClient;
	public ConnectionCallbacks mConnectionCallbacks;
	public ConnectionFailedListener mConnectionFailedListener;
	public boolean mApplicationStarted;
	public boolean mWaitingForReconnect;
	ActionBar actionBar;
	private boolean playbackPaused=false;
	public String mSessionId;
	CustomVideoView videoview;
	ImageView imageview,imageView2;
	VideoView remoteview;
	MediaInfo mediaInfo;
	MediaPlayer mMediaPlayer;
	MediaStatus mediaStatus;
	RemoteMediaPlayer mRemoteMediaPlayer;
	public CastDevice mSelectedDevice;
	VideoController videocontroller,controller;
	public ArrayList<Video> Videos;
	public ArrayList<Song> Audios;
	public ArrayList<Image> Photos;
	public int videoPosn,audioPosn,photoPosn;
	public String videoTitle,audioTitle;
	public String path;
	Intent extras;
	public static final String TAG = CastMedia.class.getSimpleName();
	ImageView imageView1;

	

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    actionBar = getSupportActionBar();
	    setContentView(R.layout.castmedia);
	    mediatype=this.getIntent().getType();
	    extras = this.getIntent();
	     
	    mediaserver = new webserver();
	    try {
	        mediaserver.start();
	    } catch(IOException ioe) {
	        Log.d("Httpd", "The server could not start.");
	    }
	  //The application needs to obtain an instance of the MediaRouter and needs to hold onto that instance for the lifetime of the sender application
	    mMediaRouter = MediaRouter.getInstance(getApplicationContext());
	    //The MediaRouter needs to filter discovery for Cast devices that can launch the receiver application associated with the sender app. For that a MediaRouteSelector is created by calling MediaRouteSelector.Builder
	    mMediaRouteSelector = new MediaRouteSelector.Builder().addControlCategory(CastMediaControlIntent.categoryForCast(getString(R.string.app_id))).build();
	    mMediaRouterCallback = new MyMediaRouterCallback();
	    mediaplay();	    
}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
      super.onCreateOptionsMenu(menu);
      getMenuInflater().inflate(R.menu.main, menu);
      MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);
      MediaRouteActionProvider mediaRouteActionProvider=(MediaRouteActionProvider) MenuItemCompat.getActionProvider(mediaRouteMenuItem);
      mediaRouteActionProvider.setRouteSelector(mMediaRouteSelector);
      return true;
    }	
	
	public void mediaplay()
	{
		switch(mediatype){ 
			case "video" :
	    					Videos  =  extras.getParcelableArrayListExtra("videolist"); 
	    					playVideoCast();
	    	break;
			case "audio" :
	    					Audios  =  extras.getParcelableArrayListExtra("songlist"); 
	    					playAudioCast();
	    	break;
			case "photo":
	    					Photos =  extras.getParcelableArrayListExtra("imagelist"); 
	    					playPhotoCast();
	    	break;
	    	default:
	    		
		}
	}
	
	public void playVideoCast(){
		//navigation up enabled and gos back to the home/main activity screen
		actionBar.setDisplayHomeAsUpEnabled(true);
		videoPosn=this.getIntent().getFlags();
		Video playVideo=Videos.get(videoPosn);
		videoTitle=playVideo.getTitle();
	    path=playVideo.getPath();
		setvideoController();
		videoview.setVideoPath(path);				
	 	videoview.requestFocus();  
	 	
	}
	public void playAudioCast(){
		  audioPosn=this.getIntent().getFlags();
		  Song playSong=Audios.get(audioPosn);
		  audioTitle=playSong.getTitle();
		  path=playSong.getPath();
		  setvideoController();
		  videoview.setVideoPath(path);
	 	  videoview.requestFocus();		 
	}
	public void playPhotoCast(){
		  photoPosn=this.getIntent().getFlags();
		  Image playPhoto=Photos.get(photoPosn);
		  path=playPhoto.getPath();
		  //imageView1.setImageBitmap(BitmapFactory.decodeFile(path));
		  Toast.makeText(CastMedia.this, "Connect to chromecast device", Toast.LENGTH_SHORT).show();	 
	}
	
	public class VideoController extends MediaController {
	    public VideoController(Context c){
			 super(c);
		}
		public void hide(){}
	}
	public void setvideoController(){
		videocontroller = new VideoController(this);	
		videocontroller.setPrevNextListeners(new View.OnClickListener() {
		@Override
		 public void onClick(View v) {
			playNext();
		 } }, new View.OnClickListener() {
		 @Override
		 public void onClick(View v) {
			playPrev();
		 }
	});
		videocontroller.setMediaPlayer(this);
		videoview=(CustomVideoView)findViewById(R.id.videoView1);
		videocontroller.setAnchorView(videoview);
		videoview.setMediaController(videocontroller);
		videocontroller.setEnabled(true);	
	}
	//play next
	private void playNext(){
		if(playbackPaused){
			  setvideoController();
			  playbackPaused=false;
		}
	    videocontroller.show(0);
    }
    //play previous
    private void playPrev(){
		if(playbackPaused){
			 setvideoController();
			 playbackPaused=false;
		}
	    videocontroller.show(0);
    }
	public class webserver extends NanoHTTPD {	
       public webserver(){
			super(8080);
	   }
	   @Override
	   public Response serve(String uri, Method method, Map<String, String> header,Map<String, String> parameters, Map<String, String> files) {
			String mediasend=" ";
			FileInputStream fis = null;
			try { 
			          fis = new FileInputStream(path); 	
			} catch (FileNotFoundException e) {  
			    e.printStackTrace();
			} 
			switch(mediatype){
				case "image":
			        		mediasend="image/*";
			        		break;
				case "audio":
			        	mediasend="audio/mp3";
			        	break;
				case "video":
			        	mediasend="video/mp4"; 
			        	break;
			}
			    return new NanoHTTPD.Response(com.castoffline.castActivity.NanoHTTPD.Response.Status.OK,mediasend,fis);
		}
	}
	@Override
	public void onDestroy()
	{
	        super.onDestroy();
	        if (mediaserver != null)
	            mediaserver.stop();
	 }
	 public class MyMediaRouterCallback extends MediaRouter.Callback {
	  	  @Override
	  	  public void onRouteSelected(MediaRouter router, RouteInfo info) {
	  	    mSelectedDevice = CastDevice.getFromBundle(info.getExtras());
	  	    videoview.pause();
	  	    launchReceiver();
	  	  }
	  	  @Override
	  	  public void onRouteUnselected(MediaRouter router, RouteInfo info) {
	  	    teardown();
	  	    mSelectedDevice = null;
	  	  }
	 }
	 @Override
	 protected void onResume() {
	      super.onResume();
	      mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback,MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);
	 }
	 @Override
	 protected void onPause() {
	 if (isFinishing()) {
	        mMediaRouter.removeCallback(mMediaRouterCallback);
	        }
	      super.onPause();
	  }
	  @Override
	  protected void onStart() {
	        super.onStart();
	        mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback,MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);
	  }
	  @Override
	  protected void onStop() {
	        mMediaRouter.removeCallback(mMediaRouterCallback);
	        super.onStop();
	  }
	  public void launchReceiver() {
	  try {
			 mCastListener = new Cast.Listener() {
			 @Override
			 public void onApplicationStatusChanged() {
				if (mApiClient != null) {
					 Log.d(TAG, "onApplicationStatusChanged:"+ Cast.CastApi.getApplicationStatus(mApiClient));
				 }
			 }
             @Override
			 public void onVolumeChanged() {
				 if (mApiClient != null) {
						Log.d(TAG, "onVolumeChanged: " + Cast.CastApi.getVolume(mApiClient));
				  }
			 }
			 @Override
			 public void onApplicationDisconnected(int errorCode) {
				  Log.d(TAG, "application has stopped");
				  teardown();
			 }
	         };
			 // Connect to Google Play services
			 mConnectionCallbacks = new ConnectionCallbacks();
			 mConnectionFailedListener = new ConnectionFailedListener();
			 Cast.CastOptions.Builder apiOptionsBuilder = Cast.CastOptions.builder(mSelectedDevice, mCastListener);
			 mApiClient = new GoogleApiClient.Builder(this).addApi(Cast.API, apiOptionsBuilder.build()).addConnectionCallbacks(mConnectionCallbacks).addOnConnectionFailedListener(mConnectionFailedListener).build();
			 mApiClient.connect();
		} catch (Exception e) {Log.e(TAG, "Failed launchReceiver", e);
	 }
	}
	private class ConnectionCallbacks implements GoogleApiClient.ConnectionCallbacks {
	   boolean isPlaying;
	   private static final String ipdevice = "http://192.168.1.102:8080";
	   @Override
	   public void onConnected(Bundle connectionHint) {  Log.d(TAG, "onConnected");
			if (mApiClient == null) { return;}
			try {
				   if (mWaitingForReconnect) {
						mWaitingForReconnect = false;
						// Check if the receiver app is still running
						if ((connectionHint != null)&& connectionHint.getBoolean(Cast.EXTRA_APP_NO_LONGER_RUNNING)) {
							Log.d(TAG, "App is no longer running");
							teardown();
						 } else { // Re-create the custom message channel
							      try {
									     Cast.CastApi.setMessageReceivedCallbacks(mApiClient,mRemoteMediaPlayer.getNamespace(), mRemoteMediaPlayer);
								       } catch (IOException e) {
									              Log.e(TAG, "Exception while creating channel", e);
										}
						 }
					 } else {
							   Cast.CastApi.launchApplication(mApiClient,getString(R.string.app_id),false).setResultCallback(new ResultCallback<Cast.ApplicationConnectionResult>() {
								@Override
								public void onResult(ApplicationConnectionResult result) {
								Status status = result.getStatus();
								if (status.isSuccess()) {
									ApplicationMetadata applicationMetadata = result.getApplicationMetadata();
									mSessionId = result.getSessionId();
									String applicationStatus = result.getApplicationStatus();
									boolean wasLaunched = result.getWasLaunched();
									Log.d(TAG,"application name: "+ applicationMetadata.getName()+ ", status: "+ applicationStatus+ ", sessionId: "+ mSessionId+ ", wasLaunched: "+ wasLaunched);
									mApplicationStarted = true;
									mRemoteMediaPlayer = new RemoteMediaPlayer();
									switch(mediatype)	
									{	
										case "audio" :  mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK);
										   				mediaMetadata.putString(MediaMetadata.KEY_TITLE, "My MUSIC TRACK");
										   				mediaInfo = new MediaInfo.Builder(ipdevice).setContentType("audio/mp3").setStreamType(MediaInfo.STREAM_TYPE_BUFFERED).setMetadata(mediaMetadata).build();
										break;
										case "video" :  mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
											 			mediaMetadata.putString(MediaMetadata.KEY_TITLE, "My MOVIE");
											 			mediaInfo = new MediaInfo.Builder(ipdevice).setContentType("video/mp4").setStreamType(MediaInfo.STREAM_TYPE_BUFFERED).setMetadata(mediaMetadata).build();
										break;
										case "photo" :	mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_PHOTO);
											  			mediaMetadata.putString(MediaMetadata.KEY_TITLE, "My PHOTO");
											  			mediaInfo = new MediaInfo.Builder(ipdevice).setContentType("image/*").setStreamType(MediaInfo.STREAM_TYPE_BUFFERED).setMetadata(mediaMetadata).build();
										break;
										default:
									}
									try {
												Cast.CastApi.setMessageReceivedCallbacks(mApiClient,mRemoteMediaPlayer.getNamespace(), mRemoteMediaPlayer);
									} catch (IOException e) {
													Log.d(TAG, "Exception while creating media channel", e);
									}
									try {
											mRemoteMediaPlayer.load(mApiClient, mediaInfo, false).setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
								    		@Override
								    		public void onResult(MediaChannelResult result) {
								    			if (result.getStatus().isSuccess()) {
								    						Log.d(TAG, "Media loaded successfully");
								    			 }
								    		 }
								             });
											 videoview.setPlayPauseListener(new CustomVideoView.PlayPauseListener() {
												 AudioManager amanager=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
												 @Override
												 public void onPlay() {
												    playbackPaused=false;  //videoView is playing
												    if(mSelectedDevice!=null && mApiClient != null && mRemoteMediaPlayer != null){
												        amanager.setStreamMute(AudioManager.STREAM_MUSIC, true);
												        sendMediaControl(playbackPaused);
												     }else{
												             amanager.setStreamVolume(AudioManager.STREAM_MUSIC, 1,1);
												           }
												    }
												    @Override
												    public void onPause(){
												    	playbackPaused=true; //videoView is paused
												    	Log.d("remotepause","remotepause");
												    	if (mSelectedDevice != null && mApiClient != null && mRemoteMediaPlayer != null){
												             amanager.setStreamMute(AudioManager.STREAM_MUSIC, false);
												    		 //videoview.setBackgroundResource(R.drawable.default_video);
												    		 sendMediaControl(playbackPaused);
												    	}else{
												    		amanager.setStreamVolume(AudioManager.STREAM_MUSIC, 1,1); }
												    }
												});
											} catch (IllegalStateException e) {
																		Log.d(TAG, "Problem occurred with media during loading", e);
										  } catch (Exception e) {
													     Log.d(TAG, "Problem opening media during loading", e);}
									} else {
											  Log.e(TAG,"application could not launch");
											  teardown();
											}
									}
								});
							  }
							} catch (Exception e) {
													Log.e(TAG, "Failed to launch application", e);}
						}
			            @Override
						public void onConnectionSuspended(int cause) {
											mWaitingForReconnect = true;
									}
		}	
	// Google Play services callbacks
		private class ConnectionFailedListener implements GoogleApiClient.OnConnectionFailedListener {
		 @Override
		 public void onConnectionFailed(ConnectionResult result) {
							teardown();
		  }
		}
		
	//Tear down the connection to the receiver
	private void teardown() {
		Log.d(TAG, "teardown");
		if (mApiClient != null) {
			if (mApplicationStarted) {
				if (mApiClient.isConnected()) {
				 try {
						Cast.CastApi.stopApplication(mApiClient, mSessionId);
						if (mRemoteMediaPlayer != null) {
								Cast.CastApi.removeMessageReceivedCallbacks(mApiClient,mRemoteMediaPlayer.getNamespace());
								mRemoteMediaPlayer = null;
						 }
					   } catch (IOException e) {
							Log.e(TAG, "Exception while removing channel", e);
					   }
						mApiClient.disconnect();
					  }
					   mApplicationStarted = false;
					 }
						mApiClient = null;
				}
				mSelectedDevice = null;
				mWaitingForReconnect = false;
				mSessionId = null;
		}
	private void sendMediaControl(final boolean playbackPaused)
	{
		if (mApiClient != null && mRemoteMediaPlayer != null){
		    mRemoteMediaPlayer.requestStatus(mApiClient).setResultCallback( new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
            @Override 
            public void onResult(RemoteMediaPlayer.MediaChannelResult mediaChannelResult) {
                if(playbackPaused ==true)
                	  mRemoteMediaPlayer.pause(mApiClient);     
                else
                	  mRemoteMediaPlayer.play(mApiClient);
            }
        });
		}
	}
	
	//MediaPlayerControl Methods
	@Override
	public void start() {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void pause() {
		// TODO Auto-generated method stub
	}
	@Override
	public int getDuration() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public int getCurrentPosition() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void seekTo(int pos) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean isPlaying() {
		// TODO Auto-generated method stub
		if(mMediaPlayer.isPlaying())
		
			Log.d(String.valueOf(mMediaPlayer.isPlaying()),"control");
			return true;
		
	}
	@Override
	public int getBufferPercentage() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public boolean canPause() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean canSeekBackward() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean canSeekForward() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public int getAudioSessionId() {
		// TODO Auto-generated method stub
		return 0;
	}


}

    
 


