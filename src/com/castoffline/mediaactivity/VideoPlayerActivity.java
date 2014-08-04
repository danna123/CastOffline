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





package com.castoffline.mediaactivity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import com.castoffline.castActivity.CastMedia;
import com.castoffline.R;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.ActionBarDrawerToggle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;
import android.widget.AdapterView.OnItemClickListener;
 
public class VideoPlayerActivity extends Fragment{
	
	public  ArrayList<Video> videoList;
	private ListView mediaView1;
	MediaPlayer mediaPlayer;
	MainActivity mainactivity;
	private ActionBarDrawerToggle mDrawerToggle;
	SurfaceView surfaceView;
	SurfaceHolder surfaceHolder;
	int mediatype;
	MediaPlayer player;
	Uri playableUri;
	VideoView videoview;
	Intent myIntent1=null;
    
    public VideoPlayerActivity(){}
     
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
    	    setHasOptionsMenu(true);
    	    
            View rootView = inflater.inflate(R.layout.media_controller, container, false);
		    mediaView1= (ListView)rootView.findViewById(R.id.song_list);
		    videoList= new ArrayList<Video>();
		    getVideoList();
				Collections.sort(videoList, new Comparator<Video>(){
					public int compare(Video a, Video b){
					return a.getTitle().compareTo(b.getTitle());
				    }
			});	
			VideoAdapter videoAdt = new VideoAdapter(getActivity(), videoList);
			mediaView1.setAdapter(videoAdt);
			mediaView1.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
					
				//videoPicked(view);
				 myIntent1 = new Intent(getActivity(),CastMedia.class);
				 myIntent1.setType(videoList.get(position).mediatype);
				 myIntent1.setFlags(position);
				 myIntent1.putParcelableArrayListExtra("videolist",videoList);	
				 startActivity(myIntent1);
			}
		});	
		return rootView;	
    }  
	
	public void getVideoList() {
		ContentResolver musicResolver = getActivity().getContentResolver();
		Uri musicUri = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
		Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
		if(musicCursor!=null && musicCursor.moveToFirst()){
			 int titleColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Video.Media.TITLE);
			 int idColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Video.Media._ID);
			 int artistColumn = musicCursor.getColumnIndex (android.provider.MediaStore.Video.Media.ARTIST);
			 int pathColumn = musicCursor.getColumnIndex (android.provider.MediaStore.Video.Media.DATA);
			 do {
					 long thisId = musicCursor.getLong(idColumn);
					 String thisTitle = musicCursor.getString(titleColumn);
					 String thisArtist = musicCursor.getString(artistColumn);
					 String thisPath = musicCursor.getString(pathColumn);
					 videoList.add(new Video(thisId, thisTitle, thisArtist,thisPath));
				} while (musicCursor.moveToNext());
		}
	}
	public static class Video implements Parcelable {
		public long id;
		public String title;
		public String artist;
		public String path;
		private String mediatype;
		public Video(long videoID, String videoTitle, String videoArtist,String videoPath) {
			id=videoID;
			mediatype="video";
			title=videoTitle;
			artist=videoArtist;
			path=videoPath;
		}
		public Video(Parcel source) {
			 this.id = source.readLong();
			 this.mediatype=source.readString();
			 this.title = source.readString();
			 this.artist = source.readString();
			 this.path=source.readString();
		 }
		public long getID(){return id;}
		public String getMediatype(){return mediatype;}
		public String getTitle(){return title;}
		public String getArtist(){return artist;}
		public String getPath(){return path;}
		@Override
		public int describeContents() {
			// TODO Auto-generated method stub
			return 0;
		}
		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeLong(id);
			dest.writeString(mediatype);
			dest.writeString(title);
			dest.writeString( artist);
			dest.writeString(path);
			
		}
		public static final Parcelable.Creator<Video> CREATOR = new Parcelable.Creator<Video>()  {
			public Video createFromParcel(Parcel source) {
					return new Video(source);
			}

	    public Video[] newArray(int size) {
	         return new Video[size];
	    }
	  };
		
	}
	public class VideoAdapter extends BaseAdapter {
		public ArrayList<Video> Videos;
		public LayoutInflater videoInf;	
		public VideoAdapter(Context c, ArrayList<Video> theVideos){
			Videos=theVideos;
			videoInf=LayoutInflater.from(c);
		}
		@Override
		public int getCount() {
			 return Videos.size();
		}
	    @Override
		public Object getItem(int arg0) {
				// TODO Auto-generated method stub
			 return null;
		}
	    @Override
	    public long getItemId(int arg0) {
				// TODO Auto-generated method stub
				 return 0;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
				//map to video layout
				RelativeLayout videoLay = (RelativeLayout)videoInf.inflate(R.layout.video, parent, false);
				//get title and artist views
			    TextView videoView = (TextView)videoLay.findViewById(R.id.video_title);
			    TextView artistView = (TextView)videoLay.findViewById(R.id.video_artist); 
				Video currSong = Videos.get(position);
				//get title and artist strings
				videoView.setText(currSong.getTitle());
				artistView.setText(currSong.getArtist());
				//set position as tag
				videoLay.setTag(position);
				return videoLay;
		}						 
	}					
}
