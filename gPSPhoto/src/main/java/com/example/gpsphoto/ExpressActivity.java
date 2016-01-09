package com.example.gpsphoto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.gpsphoto.MainActivity.MessageHandler1;
import com.gpsphoto.library.CAdapterHomePage;
import com.gpsphoto.library.DatabaseManager;
import com.gpsphoto.library.ImageListAdapter;







import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ExpressActivity extends Activity implements View.OnClickListener {

	private ViewPager imgPager;
	ImageView btnPost, btnCamera;
	TextView txtPosition;
	TextView txtType;
	TextView txtPlaceTitle;
	
	String placeName, latitude, longitude, placeid;
	private ArrayList<View> imgList;
	private int nPosition = 0;
	private int nImageCount = 0;
	
	Connection connection;
	Handler serverhandler = new MessageHandler1();
	public class MessageHandler1 extends Handler {
        
        @Override
        public void handleMessage(Message msg) {
        	switch(msg.what){
        	
        	case Global.CONNECTION_OK:
        		JSONObject obj = connection.getResult();
        		String res = null;
        		try {
					res = obj.getString("count");
					JSONArray data = obj.getJSONArray("data");
					
					ArrayList<PhotoItem> resArray = new ArrayList<PhotoItem>();
					ArrayList<String> resPath = new ArrayList<String>();
					for(int i = 0; i < data.length(); i++){
						JSONObject objItem = data.getJSONObject(i);
						PhotoItem item = new PhotoItem();
						item.picture = objItem.getString("picture");
						item.placeid = objItem.getString("placeid");
						item.userid = objItem.getString("userid");
						item.longitude = objItem.getString("longitude");
						item.latitude = objItem.getString("latitude");
						item.place_name = objItem.getString("place_name");
						item.place_city = objItem.getString("place_city");
						item.place_state = objItem.getString("place_state");
						item.place_country = objItem.getString("place_country");
						item.user_name = objItem.getString("user_name");
						
						resPath.add(item.picture);
						resArray.add(item);
					}
					if( data.length() > 0 ){
				       	 AddAdapter(resPath);
				    }
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		
        		break;
        	case Global.CONNECTION_CANCEL:
        		MessageBox("Server Connection Fail!!!");
        		break;
        	}
        }
        
        public void MessageBox(String str){
    		new AlertDialog.Builder(ExpressActivity.this).setTitle("Message")
         		.setMessage(str).setNeutralButton("Close", null).show();
    	}
    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.express_main);
		getActionBar().hide();
		
		
		txtPosition = (TextView)findViewById(R.id.txtPosition);
		txtType = (TextView)findViewById(R.id.txtType);
		txtPlaceTitle = (TextView)findViewById(R.id.txtPlaceTitle);
		
		btnPost = (ImageView)findViewById(R.id.btnPost);
		btnPost.setOnClickListener(this);
		btnCamera = (ImageView)findViewById(R.id.btnCamera);
		btnCamera.setOnClickListener(this);
		imgPager = (ViewPager)findViewById(R.id.viewPager);
		btnCamera.setVisibility(View.GONE);
		btnPost.setVisibility(View.GONE);
		
		
		if( Global.nExpressType == Global.EXPRESS_PLACE ){
			btnCamera.setVisibility(View.VISIBLE);
			btnPost.setVisibility(View.VISIBLE);
			placeName = Global.placename;
			placeid = Global.placeid;
			
			List<NameValuePair> params = new ArrayList<NameValuePair>();
	        params.add(new BasicNameValuePair("type", "placeid"));
	        params.add(new BasicNameValuePair("placeid", Global.placeid));
	        connection = new Connection(ExpressActivity.this, Global.SERVER + Global.GETPHOTO_URL, params, serverhandler);
			connection.execute();
			
			
		}
	}
	public void AddAdapter(ArrayList<String> res){
    	imgList = new ArrayList<View>();
		
        if( Global.imgPath != null )
        {
        	for(int i = 0; i < res.size(); i++)
        	{
        		if( res.get(i).equals(Global.imgPath) )
        		{
        			res.remove(i);
        			res.add(0, Global.imgPath);
        			break;
        		}
        	}
        }
        ImageLoader imageLoder = ImageLoader.getInstance();
		if(!imageLoder.isInited())
		imageLoder.init(ImageLoaderConfiguration.createDefault(ExpressActivity.this));
		
        for(int i = 0; i < res.size(); i++){
        	View view = new View(this);
	        view = LayoutInflater.from(this).inflate(R.layout.express_item, null);
	        ImageView imageView = ((ImageView)view.findViewById(R.id.img_item));
	        
	        DisplayImageOptions options = new DisplayImageOptions.Builder()
			.imageScaleType(ImageScaleType.EXACTLY).cacheInMemory()
			.cacheOnDisc().cacheOnDisc().build();
			imageLoder.displayImage(res.get(i),imageView,options);
			
	        imgList.add(view);
        }
        nImageCount = res.size();
        txtType.setText("Photo in " + placeName);
        txtPlaceTitle.setText(placeName);
	
        txtType.setVisibility(View.GONE);
	
        String temp = String.valueOf(nPosition + 1) + "/" + String.valueOf(nImageCount);
        txtPosition.setText(temp);
	
    
        CAdapterHomePage adapter = new CAdapterHomePage(imgList);
        imgPager.setAdapter(adapter);
    
        imgPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				nPosition = position;
				String temp = String.valueOf(nPosition + 1) + "/" + String.valueOf(nImageCount);
				txtPosition.setText(temp);
			}
	
			@Override
			public void onPageScrolled(int position, float arg1, int arg2) {
	
			}
	
			@Override
			public void onPageScrollStateChanged(int position) {
	
			}
        });
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		if( id == R.id.btnCamera ){
			Intent intent = new Intent(ExpressActivity.this, ActivityCamera.class);
			intent.putExtra("state", "add");
			startActivityForResult(intent, 0);
			//ExpressActivity.this.finish();
		}else if( id == R.id.btnPost ){
			
		}
	}
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode== Activity.RESULT_CANCELED){
        	setResult(RESULT_CANCELED);
        	this.finish();
        }
	}
}
