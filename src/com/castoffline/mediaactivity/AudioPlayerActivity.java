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



/*Reference : Adapter Class :  http://developer.android.com/reference/android/widget/Adapter.html
 * 			  Parcelable : http://developer.android.com/reference/android/os/Parcelable.html
 * 			  Audio Icon : https://www.iconfinder.com/iconsets/miniiconset */

package com.castoffline.mediaactivity;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import com.castoffline.castActivity.CastMedia;
import com.castoffline.R;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
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
	Bitmap bitmap;
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
		Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
		if(musicCursor!=null && musicCursor.moveToFirst()){
			 //get columns
			 int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
			 int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
			 int artistColumn = musicCursor.getColumnIndex (MediaStore.Audio.Media.ARTIST);
			 int pathColumn = musicCursor.getColumnIndex (MediaStore.Audio.Media.DATA);
			 int mimetype= musicCursor.getColumnIndex (MediaStore.Audio.Media.MIME_TYPE);
			 //add songs to list
			 do {
					 long thisId = musicCursor.getLong(idColumn);
					 String thisTitle = musicCursor.getString(titleColumn);
					 String thisArtist = musicCursor.getString(artistColumn);
					 String thisPath = musicCursor.getString(pathColumn);
					 String thismimetype=musicCursor.getString(mimetype);
					 songList.add(new Song(thisId, thisTitle, thisArtist,thisPath,thismimetype));
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
		private String mediamimetype;
		public Song(long songID, String songTitle, String songArtist,String songPath,String mimetype) {
			id=songID;
			mediatype="audio";
			title=songTitle;
			artist=songArtist;
			path=songPath;
			albumart=null;
			mediamimetype=mimetype;
		}
		public Song(Parcel source) {
			 this.id = source.readLong();
			 this.mediatype=source.readString();
			 this.title = source.readString();
			 this.artist = source.readString();
			 this.path=source.readString();
			 this.albumart=source.readString();
			 this.mediamimetype=source.readString();
		 }
		public long getID(){return id;}
		public String getMediatype(){return mediatype;}
		public String getTitle(){return title;}
		public String getArtist(){return artist;}
		public String getPath(){return path;}
		public String getAlbumArt(){return albumart;}
		public String getMimeType(){return mediamimetype;}
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
			dest.writeString(mediamimetype);
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
		Context mcontext;
		public SongAdapter(Context c, ArrayList<Song> theSongs){
			songs=theSongs;
			songInf=LayoutInflater.from(c);
			mcontext=c;
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
		@SuppressLint("ViewHolder")
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
				//get media art
				Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
                Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri,currSong.getID());
                bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(mcontext.getContentResolver(), albumArtUri);
                    bitmap = Bitmap.createScaledBitmap(bitmap,200,250, true);
                    currSong.albumart=albumArtUri.toString();
                } catch (FileNotFoundException exception) {
                    exception.printStackTrace();
                    bitmap = BitmapFactory.decodeResource(mcontext.getResources(),R.drawable.audio);
                    bitmap = Bitmap.createScaledBitmap(bitmap,200,250, true);
                    currSong.albumart=null;
                } catch (IOException e) {

                    e.printStackTrace();
                }
                imageView.setImageBitmap(bitmap);
				//set position as tag
				songLay.setTag(position);
				return songLay;
		}
							 
	}
					
}

