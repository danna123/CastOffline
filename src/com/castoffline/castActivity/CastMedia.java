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
* Reference: Android sender for Chromecast: https://developers.google.com/cast/docs/android_sender
*            Webserver NanoHTTPD: https://github.com/NanoHttpd/nanohttpd/tree/nanohttpd-for-java1.1
*            Media Playback Messages: https://developers.google.com/cast/docs/reference/messages
*
*/
package com.castoffline.castActivity;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
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
import com.google.android.gms.common.images.WebImage;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
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
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
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
	public String videoTitle,audioTitle,videoArtist,audioArtist,mimetype,mediaart;
	public String path,dateString;
	Intent extras;
	public static final String TAG = CastMedia.class.getSimpleName();
	boolean change;
	String ipdevice;
	Long date;
	

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    actionBar = getSupportActionBar();
	    setContentView(R.layout.castmedia);
	    mediatype=this.getIntent().getType();
	    extras = this.getIntent();
	    
	    //Get the ip of the device running NanoHTTPD server
	    WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
	    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
	    int ipAddress = wifiInfo.getIpAddress();
	    ipdevice=String.format("http://%d.%d.%d.%d:8080",(ipAddress & 0xff),(ipAddress >> 8 & 0xff),(ipAddress >> 16 & 0xff),(ipAddress >> 24 & 0xff));
	   
	    // start the webserver
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
	   // create an instance of MediaRouterCallback
	    mMediaRouterCallback = new MyMediaRouterCallback();
	    mediaplay();	    
}
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 * To provide cast button according to Google Cast UX Guidelines
	 * Using the MediaRouter ActionBar provider: android.support.v7.app.MediaRouteActionProvider
	 */
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
      super.onCreateOptionsMenu(menu);
      getMenuInflater().inflate(R.menu.main, menu);
      MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);
      MediaRouteActionProvider mediaRouteActionProvider=(MediaRouteActionProvider) MenuItemCompat.getActionProvider(mediaRouteMenuItem);
      mediaRouteActionProvider.setRouteSelector(mMediaRouteSelector);
      return true;
    }	
	
	/*
	 * Identifies the mediatype that user has selected and gets the file path and other metadata details
	 */
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
	
	/*
	 * By default the video list is display in navigation list layout and hence up button is provided to traverse back to the home screen
	 * Gets the corresponding video details that user wants to display through Chromecast 
	 */
	@SuppressLint("SimpleDateFormat")
	public void playVideoCast(){
		//navigation up enabled and gos back to the home/main activity screen
		actionBar.setDisplayHomeAsUpEnabled(true);
		videoPosn=this.getIntent().getFlags();
		Video playVideo=Videos.get(videoPosn);
		videoTitle=playVideo.getTitle();
		Log.d(videoTitle,"videoTitle");
		videoArtist=playVideo.getArtist();
		mimetype=playVideo.getMimetype();
	    path=playVideo.getPath();
		setvideoController();
		videoview.setVideoPath(path);
		videoview.seekTo(100);
		Toast.makeText(CastMedia.this, "Connect to chromecast device",Toast.LENGTH_LONG).show();
	 	videoview.requestFocus();  
	 	
	}
	
	/*
	 * Gets the corresponding audio details that user wants to display through Chromecast 
	 * 
	 */
	public void playAudioCast(){
		  audioPosn=this.getIntent().getFlags();
		  Song playSong=Audios.get(audioPosn);
		  audioTitle=playSong.getTitle();
		  audioArtist=playSong.getArtist();
		  mimetype=playSong.getMimeType();
		  path=playSong.getPath();
		  if(playSong.getAlbumArt()!=null)
		  {
			  	mediaart="/audio/albumart/"+playSong.getID();
			  	Log.d(mediaart,"album");
		  }
		  setvideoController();
		  videoview.setVideoPath(path);
		  Toast.makeText(CastMedia.this, "Connect to chromecast device",Toast.LENGTH_LONG).show();
	 	  videoview.requestFocus();		 
	}
	
	/*
	 * Gets the corresponding image details that user wants to display through Chromecast 
	 * 
	 */
	public void playPhotoCast(){
		  photoPosn=this.getIntent().getFlags();
		  Image playPhoto=Photos.get(photoPosn);
		  path=playPhoto.getPath();
		  mimetype=playPhoto.getmimetype();
		  date=playPhoto.getDate(); 
		  java.text.DateFormat formatter = new SimpleDateFormat("MMMM dd, yyyy h:mmaa"); 
		  dateString = formatter.format(date);
		  TextView cast_title = (TextView)findViewById(R.id.cast_title);
		  TextView cast_artist = (TextView)findViewById(R.id.cast_artist); 
		  cast_title.setText("Connect to Chromecast to View Image");
		  cast_artist.setText("Date Taken: "+dateString);
		  Toast.makeText(CastMedia.this, "Connect to chromecast device", Toast.LENGTH_LONG).show();	 
	}
	
	
	/*
	 * MediaController for the video view in case user selects audio/video
	 */
	public class VideoController extends MediaController {
	    public VideoController(Context c){
			 super(c);
		}
		public void hide(){}
	}
	public void setvideoController(){
		videocontroller = new VideoController(this);	
		videocontroller.setMediaPlayer(this);
		videoview=(CustomVideoView)findViewById(R.id.videoView1);
		videocontroller.setAnchorView(videoview);
		videoview.setMediaController(videocontroller);
		videocontroller.setEnabled(true);	
	}
	
	
	 public class MyMediaRouterCallback extends MediaRouter.Callback {
	  	  @Override
	  	  public void onRouteSelected(MediaRouter router, RouteInfo info) {
	  	    mSelectedDevice = CastDevice.getFromBundle(info.getExtras());
	  	    if((mediatype=="video" )|| (mediatype == "audio"))
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
	  
	  /*
		 * Send the file path that user has chosen to the web server
	     *  Reference :https://github.com/NanoHttpd/nanohttpd/tree/nanohttpd-for-java1.1
	     * 
	  */
	  public class webserver extends NanoHTTPD {	
		FileInputStream fileInputStream;
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
		  case "photo":
			 mediasend="image/jpeg";
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
	
	/*
	 * Callback to handle the receiver application in chromecast
	 */
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
	   @Override
	   public void onConnected(Bundle connectionHint) { Log.d(TAG, "onConnected");
		 if (mApiClient == null) { return;}
		 try {
				if (mWaitingForReconnect) {
				mWaitingForReconnect = false;
			    // Check if the receiver app is still running
				 	if ((connectionHint != null)&& connectionHint.getBoolean(Cast.EXTRA_APP_NO_LONGER_RUNNING)) {
				 		Log.d(TAG, "App is no longer running");
				 		teardown();
				 		}
				 	else{ // Re-create the custom message channel
					      try {
								  Cast.CastApi.setMessageReceivedCallbacks(mApiClient,mRemoteMediaPlayer.getNamespace(), mRemoteMediaPlayer);
							   } catch (IOException e) {
									              Log.e(TAG, "Exception while creating channel", e);
						       }
					    }
			    } 
				else {
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
							/*
							 * Identify the mediatype and send the metadata details to media info		
							 */
							switch(mediatype)	
							{	
								case "audio" :  mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK);
										   		mediaMetadata.putString(MediaMetadata.KEY_TITLE, "My MUSIC TRACK"+":  "+audioTitle);
										   		mediaMetadata.putString(MediaMetadata.KEY_ARTIST,audioArtist);
										   		mediaMetadata.addImage(new WebImage(Uri.parse("https://www.googledrive.com/host/0B61ekPEN_94sZ21mcnQtbVU2RHM/media.png")));
										   		mediaInfo = new MediaInfo.Builder(ipdevice).setContentType(mimetype).setStreamType(MediaInfo.STREAM_TYPE_BUFFERED).setMetadata(mediaMetadata).build();
								break;
								case "video" :  mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
										        mediaMetadata.addImage(new WebImage(Uri.parse("https://www.googledrive.com/host/0B61ekPEN_94sZ21mcnQtbVU2RHM/film_reel.png")));
											 	mediaMetadata.putString(MediaMetadata.KEY_TITLE, "My MOVIE"+":  "+videoTitle);
											 	mediaInfo = new MediaInfo.Builder(ipdevice).setContentType(mimetype).setStreamType(MediaInfo.STREAM_TYPE_BUFFERED).setMetadata(mediaMetadata).build();
								break;
								case "photo" :	mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_PHOTO);
											  	mediaMetadata.putString(MediaMetadata.KEY_TITLE, "My PHOTO"+":  ");
											  	mediaInfo = new MediaInfo.Builder(ipdevice).setContentType(mimetype).setStreamType(MediaInfo.STREAM_TYPE_BUFFERED).setMetadata(mediaMetadata).build();
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
								    }});
									/*
									 * checks if the video is playing or if it is paused and according it will be played/paused in the receiver
									 */
									videoview.setPlayPauseListener(new CustomVideoView.PlayPauseListener() {
									AudioManager amanager=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
									@Override
									public void onPlay() {
										playbackPaused=false;  //videoView is playing
										if(mSelectedDevice!=null && mApiClient != null && mRemoteMediaPlayer != null){
											//volume is set to mute if media is casting in Chromecast
											amanager.setStreamMute(AudioManager.STREAM_MUSIC, true);
											sendMediaControl(playbackPaused,false);
										}else{
												 amanager.setStreamVolume(AudioManager.STREAM_MUSIC, 3,1);
											  }
										
									 }
								     @Override
									 public void onPause(){
										playbackPaused=true; //videoView is paused
										if (mSelectedDevice != null && mApiClient != null && mRemoteMediaPlayer != null){
											amanager.setStreamMute(AudioManager.STREAM_MUSIC, false);
											sendMediaControl(playbackPaused,false);
										}else{
											amanager.setStreamVolume(AudioManager.STREAM_MUSIC, 3,1); }
									 }
								     /* Currently Seek function is not working for the media playback while casting
								      * (non-Javadoc)
								      * @see com.castoffline.castActivity.CustomVideoView.PlayPauseListener#onSeekChanged(int)
								      */
									@Override
									 public void onSeekChanged(int pos){
									     Log.d(String.valueOf(videoview.getCurrentPosition()),"seekinsie");
										 seek(videoview.getCurrentPosition());
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
	private void sendMediaControl(final boolean playbackPaused,final boolean change)
	{
		if (mApiClient != null && mRemoteMediaPlayer != null){
		    mRemoteMediaPlayer.requestStatus(mApiClient).setResultCallback( new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
            @Override 
            public void onResult(RemoteMediaPlayer.MediaChannelResult mediaChannelResult) {           	
            		if(playbackPaused ==true){
        				mRemoteMediaPlayer.pause(mApiClient); 
        			}else{
        				mRemoteMediaPlayer.play(mApiClient);
        			}           	
            }
        });
		}
	}
	/*
	* Currently seek function is not working
	*/
	public void seek(final int position) 
	{
		Log.d("seek","seek");
		if (mApiClient != null && mRemoteMediaPlayer != null){
			
			videoview.pause();
			
			mRemoteMediaPlayer.seek(mApiClient,position).setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>(){
	        	  @Override
	        		    public void onResult(MediaChannelResult result) {
	              if (result.getStatus().isSuccess()) {
	                Log.d(String.valueOf(result.getStatus().getStatusCode()), "No seek");
	              }
	            }
			});
			mRemoteMediaPlayer.setOnStatusUpdatedListener(new RemoteMediaPlayer.OnStatusUpdatedListener(){
				@Override
				  public void onStatusUpdated() {
					@SuppressWarnings("unused")
					MediaStatus mediaStatus = mRemoteMediaPlayer.getMediaStatus();
				    
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
			return false;
		
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

    
 


