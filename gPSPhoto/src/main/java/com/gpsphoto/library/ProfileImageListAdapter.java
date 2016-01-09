package com.gpsphoto.library;

import java.util.HashMap;
import java.util.List;























import com.example.gpsphoto.ExpressActivity;
import com.example.gpsphoto.Global;
import com.example.gpsphoto.MainActivity;
import com.example.gpsphoto.ProfileActivity;
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


public class ProfileImageListAdapter extends ArrayAdapter<String> {
	
	private Activity activity;
	private List<String> itemsAllEvents;
	private int row;
	public ImageLoader imageLoder; 
	
	public ProfileImageListAdapter(Activity act, int resource, List<String> arrayList) {
		super(act, resource, arrayList);
		this.activity = act;
		this.row = resource;
		this.itemsAllEvents = arrayList;
		
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

		
		String bitmapImage = itemsAllEvents.get(position);
		
		holder.photo_img = (ImageView)view.findViewById(R.id.img_item);
			
        DisplayImageOptions options = new DisplayImageOptions.Builder()
		.imageScaleType(ImageScaleType.EXACTLY).cacheInMemory()
		.cacheOnDisc().cacheOnDisc().build();
		imageLoder.displayImage(bitmapImage, holder.photo_img, options);
	
		
		
		LayoutParams params = holder.photo_img.getLayoutParams();
		int nWidth = Global.nDeviceWidth;
		holder.photo_img.setScaleType(ScaleType.FIT_XY);
		params.width = nWidth;
		params.height = (int) (nWidth * 0.8);
		
		return view;
		
	}

	public class ViewHolder {
	 	public ImageView photo_img;

	}

}
