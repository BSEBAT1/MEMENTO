package com.gpsphoto.library;

import java.util.HashMap;
import java.util.List;







import com.example.gpsphoto.PlaceItem;
import com.example.gpsphoto.R;


import com.nostra13.universalimageloader.core.ImageLoader;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class PlaceListAdapter extends ArrayAdapter<PlaceItem> {
	
	private Activity activity;
	private List<PlaceItem> itemsAllEvents;
	private int row;
	public ImageLoader imageLoader; 
	 
	
	public PlaceListAdapter(Activity act, int resource, List<PlaceItem> arrayList) {
		super(act, resource, arrayList);
		this.activity = act;
		this.row = resource;
		this.itemsAllEvents = arrayList;
		
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View view = convertView;
		ViewHolder holder;
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) activity
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(row, null);

			holder = new ViewHolder();
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

		if ((itemsAllEvents == null) || ((position + 1) > itemsAllEvents.size()))
			return view;
	
		PlaceItem item = itemsAllEvents.get(position);
		holder.txtPlaceName = (TextView)view.findViewById(R.id.txtPlaceName);
		holder.txtDistance = (TextView)view.findViewById(R.id.txtDistance);
		holder.txtPlaceName.setText(item.name);
		holder.txtDistance.setText(item.length);
		
		return view;
		
	}

	public class ViewHolder {
	 	public TextView txtPlaceName;
	 	public TextView txtDistance;
	}

}
