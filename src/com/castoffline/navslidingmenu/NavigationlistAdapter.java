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

package com.castoffline.navslidingmenu;

	 
import java.util.ArrayList;
import com.castoffline.*;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
	 
	public class NavigationlistAdapter extends BaseAdapter {
	     
	    private Context context;
	    private ArrayList<Navigationmenu> navDrawerItems;
	     
	    public NavigationlistAdapter(Context context, ArrayList<Navigationmenu> navDrawerItems){
	        this.context = context;
	        this.navDrawerItems = navDrawerItems;
	    }
	 
	    @Override
	    public int getCount() {
	        return navDrawerItems.size();
	    }
	 
	    @Override
	    public Object getItem(int position) {       
	        return navDrawerItems.get(position);
	    }
	 
	    @Override
	    public long getItemId(int position) {
	        return position;
	    }
	 
	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	        if (convertView == null) {
	            LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
	            convertView = mInflater.inflate(R.layout.drawer_list_item, null);
	        }
	          
	        ImageView imgIcon = (ImageView) convertView.findViewById(R.id.icon);
	        TextView txtTitle = (TextView) convertView.findViewById(R.id.title);
	        TextView txtCount = (TextView) convertView.findViewById(R.id.counter);
	          
	        imgIcon.setImageResource(navDrawerItems.get(position).getIcon());        
	        txtTitle.setText(navDrawerItems.get(position).getTitle());
	         
	        // displaying count
	        // check whether it set visible or not
	        if(navDrawerItems.get(position).getCounterVisibility()){
	            txtCount.setText(navDrawerItems.get(position).getCount());
	        }else{
	            // hide the counter view
	            txtCount.setVisibility(View.GONE);
	        }
	         
	        return convertView;
	    }
	 
	

}
