package com.gpsphoto.library;

	import java.util.ArrayList;

import android.content.Context;
	import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


	public class CAdapterHomePage extends PagerAdapter {
		private ArrayList<View> imgList;
		//private ArrayList<ImageView> imgList;

		public CAdapterHomePage(ArrayList<View> imgList) {
			this.imgList = imgList;
		}

		@Override
		public int getCount() {
			//return imgList != null && !imgList.isEmpty() ? Integer.MAX_VALUE : 0;
			return imgList.size();
		}
		

		@Override
		  public int getItemPosition (Object object)
		  {
		    int index = imgList.indexOf (object);
		    if (index == -1)
		      return POSITION_NONE;
		    else
		      return index;
		  }


		 
		 
		
		
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			//imgList.get(position % imgList.size()).		
			//container.removeView(imgList.get(position % imgList.size()));
			//container.removeView(imgList.get(position % imgList.size()));
	
			container.addView(imgList.get(position % imgList.size()));	
		
			return imgList.get(position % imgList.size());
			
			 
			    
		}

		@Override
		public boolean isViewFromObject(View view, Object obj) {
			return view == obj;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			//container.removeView(imgList.get(position % imgList.size()));
			((ViewPager)container).removeView (imgList.get (position % imgList.size()));
		}
		
	
	}
