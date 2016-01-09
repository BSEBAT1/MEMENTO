package com.gpsphoto.library;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

























import com.example.gpsphoto.ExpressActivity;
import com.example.gpsphoto.Global;
import com.example.gpsphoto.MainActivity;
import com.example.gpsphoto.PhotoItem;
import com.example.gpsphoto.R;


import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;


public class ImageListAdapter extends ArrayAdapter<PhotoItem> {
	
	private Activity activity;
	private List<PhotoItem> itemsAllEvents;
	private int row;
	public ImageLoader imageLoder; 
	public Handler mHandler;
	
	public ImageListAdapter(Activity act, int resource, ArrayList<PhotoItem> arrayList, Handler handle) {
		super(act, resource, arrayList);
		this.activity = act;
		this.row = resource;
		this.itemsAllEvents = arrayList;
		this.mHandler = handle;
		
		this.imageLoder = ImageLoader.getInstance();
		if(!imageLoder.isInited())
		this.imageLoder.init(ImageLoaderConfiguration.createDefault(act));
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View view = convertView;
		final ViewHolder holder;
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

		
		final String bitmapImage = itemsAllEvents.get(position).picture;
		
		
		holder.photo_img = (ImageView)view.findViewById(R.id.img_item);
		holder.photo_place = (TextView)view.findViewById(R.id.txtPlaceName);
		holder.photo_user = (TextView)view.findViewById(R.id.txtUserName);
		
		
		String szPlace = itemsAllEvents.get(position).place_name;
		String szUser = "By " + itemsAllEvents.get(position).user_name;
		final String szPlaceId = itemsAllEvents.get(position).place_name;
		
		holder.photo_place.setText(szPlace);
		holder.photo_user.setText(szUser);
		

		holder.photo_place.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Global.placename = itemsAllEvents.get(position).place_name;
				Global.placeid = itemsAllEvents.get(position).placeid;
				Global.imgPath = itemsAllEvents.get(position).picture;
				Global.selPhotoItem = itemsAllEvents.get(position);
				
				Global.nExpressType = Global.EXPRESS_PLACE;
				Message msg =   new Message();
	            msg.what    =   Global.IMAGE_LIST_ADAPTER;
	            mHandler.sendMessage(msg);
			}
			
		});
		
		holder.photo_user.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Global.username = itemsAllEvents.get(position).user_name;
				Global.selPhotoItem = itemsAllEvents.get(position);
				Global.nExpressType = Global.EXPRESS_USER;
				Message msg =   new Message();
	            msg.what    =   Global.USER_LIST_ADAPTER;
	            mHandler.sendMessage(msg);
			}
			
		});
		
		
		
		DisplayImageOptions options = new DisplayImageOptions.Builder()
		.imageScaleType(ImageScaleType.EXACTLY).cacheInMemory()
		.cacheOnDisc().cacheOnDisc().build();
		imageLoder.displayImage(bitmapImage,holder.photo_img,options,new ImageLoadingListener() {
		
		@Override
		public void onLoadingStarted(String arg0, View arg1) {
		 	//holder.bar.setVisibility(View.VISIBLE);
			
			
		}
		
		@Override
		public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
			
			
		}
		
		@Override
		public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
		 	
			
		}
		
		@Override
		public void onLoadingCancelled(String arg0, View arg1) {
			
		
		}
	});
		
		LayoutParams params = holder.photo_img.getLayoutParams();
		int nWidth = Global.nDeviceWidth;
		holder.photo_img.setScaleType(ScaleType.FIT_XY);
		params.width = nWidth; //(int) (((double)nWidth / (double)origW) * origH);
		params.height = (int) (nWidth * 1.2);//nWidth;  (int) (((double)nWidth / (double)origW) * origH * 1.25);//
		return view;
	}

	public class ViewHolder {
	 	public ImageView photo_img;
	 	public TextView photo_place;
	 	public TextView photo_user;
	}

}
