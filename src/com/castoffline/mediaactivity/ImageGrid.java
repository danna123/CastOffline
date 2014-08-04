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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.castoffline.castActivity.CastMedia;
import com.castoffline.R;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

public class ImageGrid extends Fragment{
	int imageID;
	Cursor imagecursor1,cursor;
	ImageAdapter imageAdapter;
	LayoutInflater mInflater;
	private ArrayList<Image> imageList;
	GridView gridview1;
	Intent myintent2;
	
	public ImageGrid(){}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		 View rootView = inflater.inflate(R.layout.gridphoto, container, false);
		 gridview1= (GridView)rootView.findViewById(R.id.gridview);
	     gridview1.setNumColumns(GridView.AUTO_FIT);
	     gridview1.setHorizontalSpacing(40);
         gridview1.setVerticalSpacing(40);
         gridview1.setPadding(20, 20, 20, 0);
         gridview1.setFastScrollEnabled(true);
         gridview1.setStretchMode(GridView.STRETCH_COLUMN_WIDTH); 
		 imageList= new ArrayList<Image>();
		 getImageList();
		 Log.d("here","image");
		 ImageAdapter imageAdt = new ImageAdapter(getActivity(), imageList);
		 gridview1.setAdapter(imageAdt);
		 gridview1.setOnItemClickListener(new OnItemClickListener() {
	    	 public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	        	Image currImage1=imageList.get(position);
	        	long longdate=currImage1.getDate();
				Date d = new Date(longdate); 
				java.text.DateFormat formatter = new SimpleDateFormat("MMMM dd, yyyy h:mmaa"); 
				String dateString = formatter.format(d); 
	            Toast.makeText(getActivity(), " " + position+" "+currImage1.getPath()+"Date:"+dateString, Toast.LENGTH_LONG).show();
	            myintent2 = new Intent(getActivity(),CastMedia.class);
				myintent2.setType(imageList.get(position).getMediatype());
				myintent2.setFlags(position);
				myintent2.putParcelableArrayListExtra("imagelist",imageList);	
				startActivity(myintent2);
	        }
	    });
	     return rootView;	   
	}
	
	public void getImageList() {
		final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
		ContentResolver imageResolver = getActivity().getContentResolver();
		Uri imageUri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		Cursor imageCursor = imageResolver.query(imageUri, null, null, null, orderBy + " DESC");
		if(imageCursor!=null && imageCursor.moveToFirst()){
			 int titleColumn = imageCursor.getColumnIndex(android.provider.MediaStore.Images.Media.TITLE);
			 int idColumn = imageCursor.getColumnIndex(android.provider.MediaStore.Images.Media._ID);
			 int dateColumn = imageCursor.getColumnIndex (android.provider.MediaStore.Images.Media.DATE_TAKEN);
			 int pathColumn = imageCursor.getColumnIndex (android.provider.MediaStore.Images.Media.DATA);
			 do {
					 long thisId =imageCursor.getLong(idColumn);
					 String thisTitle = imageCursor.getString(titleColumn);
					 long thisdate = imageCursor.getLong(dateColumn);
					 String thisPath = imageCursor.getString(pathColumn);
					 imageList.add(new Image(thisId, thisTitle, thisdate,thisPath));
				} while (imageCursor.moveToNext());
		}
	}
	public static class Image implements Parcelable {
		public long id;
		public String mediatype;
		public String title;
		public long date;
		public String path;
		public Image(long imageID, String imageTitle, long imageDate,String imagePath) {
			id=imageID;
			mediatype="image";
			title=imageTitle;
			date=imageDate;
			path=imagePath;
		}
		public Image(Parcel source) {
			 this.id = source.readLong();
			 this.mediatype=source.readString();
			 this.title = source.readString();
			 this.date = source.readLong();
			 this.path=source.readString();
		}
		public long getID(){return id;}
		public String getMediatype(){return mediatype;}
		public String getTitle(){return title;}
		public long getDate(){return date;}
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
			dest.writeLong(date);
			dest.writeString(path);
			
		}
		public static final Parcelable.Creator<Image> CREATOR = new Parcelable.Creator<Image>()  {
			public Image createFromParcel(Parcel source) {
					return new Image(source);
			}

	    public Image[] newArray(int size) {
	         return new Image[size];
	    }
	  };
		
	}
	public class ImageAdapter extends BaseAdapter {
	   
	    public LayoutInflater imageInf;
	    ArrayList<Image> images;
	    Context mcontext;
	    public ImageAdapter(Context context, ArrayList<Image> theimages) {
	    	images = theimages;
	    	mcontext=context;
	    }
	    public int getCount() {
	    	return images.size();
	    }
	    public Object getItem(int position) {
	        return null;
	    }
	    public long getItemId(int position) {
	        return position;
	    }
	    public View getView(int position, View convertView, ViewGroup parent) {
	        ImageView imageView;
	        Bitmap bm;
	        Image currImage = images.get(position);;
	        if (convertView == null) {  
	            imageView = new ImageView(mcontext);
	            imageView.setLayoutParams(new GridView.LayoutParams(350, 350));
	            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
	            imageView.setPadding(8, 8, 8, 8);
	        } else {
	            imageView = (ImageView)convertView;
	        }
	        bm = decodeSampledBitmapFromUri(currImage.getPath(), 250, 250);
	        imageView.setImageBitmap(bm);
			return imageView;
	    }
	    public Bitmap decodeSampledBitmapFromUri(String path, int reqWidth, int reqHeight) { 
            Bitmap bm = null;
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            options.inJustDecodeBounds = false;
            bm = BitmapFactory.decodeFile(path, options);
            return bm;  
        }
        public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;
     
            if (height > reqHeight || width > reqWidth) {
            	final int halfHeight = height / 4;
                final int halfWidth = width / 4;
                while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                      inSampleSize *= 2;
                }
            }
            return inSampleSize;   
        }  
    }
}
	 
