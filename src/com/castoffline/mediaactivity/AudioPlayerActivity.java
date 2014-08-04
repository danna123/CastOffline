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
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
public class AudioPlayerActivity  extends Fragment 
{
	public ArrayList<Song> songList;	
	private ListView mediaView;
	MediaPlayer mediaPlayer;
	SurfaceView surfaceView;
	SurfaceHolder surfaceHolder;
	int mediatype;
	MediaPlayer player;
	Uri playableUri;
	Intent myIntent1=null;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
	    setHasOptionsMenu(true);
	    View rootView = inflater.inflate(R.layout.media_controller, container, false);
		mediaView = (ListView)rootView.findViewById(R.id.song_list);
		songList = new ArrayList<Song>();
		getSongList();
		Collections.sort(songList, new Comparator<Song>(){
				public int compare(Song a, Song b){
				return a.getTitle().compareTo(b.getTitle());
			    }
		});
		SongAdapter songAdt = new SongAdapter(getActivity(), songList);
		mediaView.setAdapter(songAdt);
		mediaView.setOnItemClickListener(new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				myIntent1 = new Intent(getActivity(),CastMedia.class);
				myIntent1.setType(songList.get(position).getMediatype());
				myIntent1.setFlags(position);
				myIntent1.putParcelableArrayListExtra("songlist",songList);	
				startActivity(myIntent1);
				}
		});	
	   return rootView;
		
	}
	
    public void getSongList() {
		ContentResolver musicResolver = getActivity().getContentResolver();
		Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
		if(musicCursor!=null && musicCursor.moveToFirst()){
			 //get columns
			 int titleColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
			 int idColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
			 int artistColumn = musicCursor.getColumnIndex (android.provider.MediaStore.Audio.Media.ARTIST);
			 int pathColumn = musicCursor.getColumnIndex (android.provider.MediaStore.Audio.Media.DATA);
			 int albumArt= musicCursor.getColumnIndex (android.provider.MediaStore.Audio.AlbumColumns.ALBUM_ART);
			 //add songs to list
			 do {
					 long thisId = musicCursor.getLong(idColumn);
					 String thisTitle = musicCursor.getString(titleColumn);
					 String thisArtist = musicCursor.getString(artistColumn);
					 String thisPath = musicCursor.getString(pathColumn);
					 String thisAlbumArt=musicCursor.getString(pathColumn);
					 songList.add(new Song(thisId, thisTitle, thisArtist,thisPath,thisAlbumArt));
				} while (musicCursor.moveToNext());
		}
	}
	
	
	public static class Song implements Parcelable{
		private long id;
		private String mediatype;
		private String albumart;
		private String title;
		private String artist;
		private String path;
		public Song(long songID, String songTitle, String songArtist,String songPath,String albumArt) {
			id=songID;
			mediatype="audio";
			title=songTitle;
			artist=songArtist;
			path=songPath;
			albumart=albumArt;
		}
		public Song(Parcel source) {
			 this.id = source.readLong();
			 this.mediatype=source.readString();
			 this.title = source.readString();
			 this.artist = source.readString();
			 this.path=source.readString();
			 this.albumart=source.readString();
		 }
		public long getID(){return id;}
		public String getMediatype(){return mediatype;}
		public String getTitle(){return title;}
		public String getArtist(){return artist;}
		public String getPath(){return path;}
		public String getAlbumArt(){return albumart;}
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
			dest.writeString(artist);
			dest.writeString(path);
			dest.writeString(albumart);
		}
		public static final Parcelable.Creator<Song> CREATOR = new Parcelable.Creator<Song>()  {
			public Song createFromParcel(Parcel source) {
					return new Song(source);
			}

	    public Song[] newArray(int size) {
	         return new Song[size];
	    }
	  };
	}
	
	public class SongAdapter extends BaseAdapter {
		private ArrayList<Song> songs;
		private LayoutInflater songInf;
		public SongAdapter(Context c, ArrayList<Song> theSongs){
			songs=theSongs;
			songInf=LayoutInflater.from(c);
		}
		@Override
		public int getCount() {
			 return songs.size();
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
				//map to song layout
				RelativeLayout songLay = (RelativeLayout)songInf.inflate(R.layout.song, parent, false);
				//get title and artist views
			    TextView songView = (TextView)songLay.findViewById(R.id.song_title);
			    TextView artistView = (TextView)songLay.findViewById(R.id.song_artist);
			    ImageView imageView = (ImageView)songLay.findViewById(R.id.album_image);
				Song currSong = songs.get(position);
				//get title and artist strings
				songView.setText(currSong.getTitle());
				artistView.setText(currSong.getArtist());
				//imageView.getResources((R.drawable.ic_launcher));
				//set position as tag
				songLay.setTag(position);
				return songLay;
		}
							 
	}
					
}

