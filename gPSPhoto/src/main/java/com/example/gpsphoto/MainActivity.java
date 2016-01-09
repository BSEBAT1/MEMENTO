package com.example.gpsphoto;

import java.io.IOException;
import java.util.ArrayList;















import java.util.List;
import java.util.Locale;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.gpsphoto.LoginActivity.MessageHandler;
import com.gpsphoto.library.DatabaseManager;
import com.gpsphoto.library.ImageListAdapter;








import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


@SuppressLint("HandlerLeak")
//GPS Location Listener, MainActivity
public class MainActivity extends Activity implements LocationListener,View.OnClickListener {

	ListView mainImageList;
	ImageView btnProfile, btnAdd;
	ImageView btnLogout;
	TextView txtCity;
	
	String countryName = null, cityName = null, stateName = null;
	private Integer isHomePointRecorded = -1;
	public static double latitude = -1;
	public static double longitude = -1;
	private LocationManager locationManager;
	public static int width, heigth;
	
	//Photo List item Click Events
	Handler handler = new MessageHandler();
	public class MessageHandler extends Handler {
        
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case Global.IMAGE_LIST_ADAPTER:		//If user click area on photo lists
            	if( latitude == -1 ){
            		Toast.makeText(MainActivity.this, "Wait for connection to gps", Toast.LENGTH_SHORT).show();
            		break;
            	}
            	Intent intent = new Intent(MainActivity.this, ExpressActivity.class);
            	startActivity(intent);
              break;
            case Global.USER_LIST_ADAPTER:	//If user click username on photo lists
            	if( latitude == -1 ){
            		Toast.makeText(MainActivity.this, "Wait for connection to gps", Toast.LENGTH_SHORT).show();
            		break;
            	}
            	Global.temp_user_id = Integer.parseInt(Global.selPhotoItem.userid);
            	Intent intent1 = new Intent(MainActivity.this, ProfileActivity.class);
            	startActivity(intent1);
            	break;
            }
           
        }
    }
	
	
	//Every 3 minutes, Location Information will update.
	private final int interval = 180000; // 3 minutes
   	private Handler timerHandler = new Handler();
	private Runnable runnable = new Runnable(){
   	    public void run() {
   	    	Criteria criteria = new Criteria();
		     criteria.setAccuracy(Criteria.ACCURACY_FINE);
		        
		      for (String provider : locationManager.getProviders(criteria, true))
		      {
		          if(provider.contains("gps"))
		          {
		          	locationManager.requestSingleUpdate(provider,MainActivity.this,null);
		           	return;
		          }
		      }
		     startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));

   	   }
   	};
   	////////////////////////////////////////////////////////////
   	
   	Connection connection;
	Handler serverhandler = new MessageHandler1();
	public class MessageHandler1 extends Handler {
        
        @Override
        public void handleMessage(Message msg) {
        	switch(msg.what){
        	
        	case Global.CONNECTION_OK:		//If app get all pictures from databases on server, app will check distance.
        									//If photo location place on radius=3miles, app will show this photo,
        									//If photo location don't place on radius=3miles, app will hide this photo.
        		JSONObject obj = connection.getResult();
        		String res = null;
        		try {
					res = obj.getString("count");
					JSONArray data = obj.getJSONArray("data");
					
					ArrayList<PhotoItem> resArray = new ArrayList<PhotoItem>();
					for(int i = 0; i < data.length(); i++){
						JSONObject objItem = data.getJSONObject(i);
						//Get Photo Information from JSON Format
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
						//Calculate Distance
						float[] distance = new float[1];
						Location.distanceBetween(latitude,longitude, Double.parseDouble(item.latitude),
			         			Double.parseDouble(item.longitude), distance);
			         	double min = distance[0];
			         	
			         	min = min / 1609.344;
			         	if( min <= 3.0 ){
			         		resArray.add(item);	
			         	}
						
					}
					if( resArray.size() > 0 ){
						//Photo Count is bigger than 0, app will set PhotoListAdapter to show photo on listview.
				       	ImageListAdapter objAdapter = new ImageListAdapter(MainActivity.this, R.layout.image_item,resArray, handler);
						mainImageList.setAdapter(objAdapter);
				    }else{
				    	Toast.makeText(MainActivity.this, "There is no Photos in this place", Toast.LENGTH_LONG).show();
				    }
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		
        		break;
        	case Global.CONNECTION_CANCEL:	//API is Fail
        		MessageBox("Server Connection Fail!!!");
        		break;
        	}
        }
        
        public void MessageBox(String str){
    		new AlertDialog.Builder(MainActivity.this).setTitle("Message")
         		.setMessage(str).setNeutralButton("Close", null).show();
    	}
    }
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getActionBar().hide();
		
		mainImageList = (ListView)findViewById(R.id.mainImageList);
		txtCity = (TextView)findViewById(R.id.txtCity);
		
		btnProfile = (ImageView)findViewById(R.id.btnProfile);
		btnProfile.setOnClickListener(this);
		
		btnAdd = (ImageView)findViewById(R.id.btnAdd);
		btnAdd.setOnClickListener(this);
		
		btnLogout = (ImageView)findViewById(R.id.btnLogout);
		btnLogout.setOnClickListener(this);
		
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		//Phone Device Width, Height, Pixel
			Display display = getWindowManager().getDefaultDisplay(); 
			Global.nDeviceWidth = display.getWidth();  // deprecated
			Global.nDeviceHeight = display.getHeight(); 
	        Global.nDevicePixcel = display.getPixelFormat();
	         Criteria criteria = new Criteria();
		     criteria.setAccuracy(Criteria.ACCURACY_FINE);
	     ///////////////////////////////////////////////////////////////
	    
	     //If Location is disabled on phone, APP will show Location Setting.
		      for (String provider : locationManager.getProviders(criteria, true))
		      {
		          if(provider.contains("gps"))
		          {
		          	locationManager.requestSingleUpdate(provider,this,null);
		           	return;
		          }
		      }
		      startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
	      ///////////////////////////////////////////////////////////////
	}

	@Override
	public void onResume() {
		// Inflate the menu; this adds items to the action bar if it is present.
		super.onResume();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("type", "all"));
        
        //Get All Pictures from databases on Server.
        //Context, Server URL, Parameters, serverhandler is to process API results.
        	connection = new Connection(MainActivity.this, Global.SERVER + Global.GETPHOTO_URL, params, serverhandler);
        	connection.execute();
        ///////////////////////////////////////////////////////////////
	
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
		if( id == R.id.btnAdd ){
			if( latitude == -1 ){
				Toast.makeText(MainActivity.this, "Wait for connection to gps", Toast.LENGTH_SHORT).show();
				return;
			}
			//Go to the Phone Device Camera Activity
				Intent intent = new Intent(MainActivity.this, ActivityCamera.class);
				intent.putExtra("state", "new");
				startActivity(intent);
			///////////////////////////////////////////////////////////////
		}else if( id == R.id.btnProfile ){
			if( latitude == -1 ){
				Toast.makeText(MainActivity.this, "Wait for connection to gps", Toast.LENGTH_SHORT).show();
				return;
			}
			//Set the user information with logined user information
				Global.temp_user_id = Global.login_user_id;
			//Go to the User Profile Screens. UserProfileActivity will show temp_user_id informations.
				Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
				startActivity(intent);
			///////////////////////////////////////////////////////////////
		}else if( id == R.id.btnLogout ){
			//Set the values that user log out in last time.
			//So, If user run app again, app will not login automatically.
				Global.ed.putString("flag", "false");
	            Global.ed.commit();
				setResult(RESULT_CANCELED);
				finish();
			///////////////////////////////////////////////////////////////
		}
		
	}//////
	
	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
			latitude = location.getLatitude();
			longitude = location.getLongitude();
			
			/////////////////////////////////////////////////////////////////
			//GPS Location ChangeListener.
			//If GPS Location Information change, app will store Longitude, Latitude, cityName, countryName, stateName.
				cityName=null;
				countryName = null;
				Geocoder gcd = new Geocoder(getBaseContext(),   
				Locale.getDefault());               
			      List<Address>  addresses;    
			      try
			      {    
			    	  addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);    
			    	      
			    	  cityName=addresses.get(0).getLocality();
			    	  countryName = addresses.get(0).getCountryName();
			    	  stateName = addresses.get(0).getAdminArea();
			      } 
			      catch (IOException e) {
			    	  Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
			    	  e.printStackTrace();    
			      }
			      if( cityName != null && countryName != null)
			      {
			    	  if( stateName != null )
			    		  txtCity.setText(cityName + " " + stateName + " " + countryName);
			    	  else
			    		  txtCity.setText(cityName + " " + countryName);
			      }
			      Global.cityName = cityName;
			      Global.countryName = countryName;
			      Global.stateName = stateName;
		      ///////////////////////////////////////////////////////////////////
		    
		     //GPS Location Update Every 3 minutes.
		     //This Code will call runnable After interval seconds.
				timerHandler.postAtTime(runnable, System.currentTimeMillis()+interval);
	   	    	timerHandler.postDelayed(runnable, interval);
	   	     /////////////////////////////////////////////////////////////////
		     
   	    	
   	    	//Get All pictures from databases on Server.
	   	    	List<NameValuePair> params = new ArrayList<NameValuePair>();
	   	        params.add(new BasicNameValuePair("type", "all"));
	   	        connection = new Connection(MainActivity.this, Global.SERVER + Global.GETPHOTO_URL, params, serverhandler);
	   			connection.execute();
	   		/////////////////////////////////////////////////////////////////
	      /*   */
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}
}
