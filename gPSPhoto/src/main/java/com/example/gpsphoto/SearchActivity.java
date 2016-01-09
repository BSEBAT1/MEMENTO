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
import com.gpsphoto.library.DatabaseManager;
import com.gpsphoto.library.ImageListAdapter;
import com.gpsphoto.library.PlaceListAdapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

public class SearchActivity extends Activity {

	ImageView btnSearch;
	ListView placeList;
	double latitude;
	double longitude;
	
	boolean bSearch = false;
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
					
					ArrayList<PlaceItem> resArray = new ArrayList<PlaceItem>();
					for(int i = 0; i < data.length(); i++){
						JSONObject objItem = data.getJSONObject(i);
						PlaceItem item = new PlaceItem();
						item.placeid = objItem.getString("placeid");
						item.name = objItem.getString("name");
						item.city = objItem.getString("city");
						item.state = objItem.getString("state");
						item.country = objItem.getString("country");
						item.latitude = objItem.getString("latitude");
						item.longitude = objItem.getString("longitude");
						resArray.add(item);
					}
					if( data.length() > 0 ){
						AddListAdapter(resArray);
				    }else if( bSearch ){
				    	AddListAdapter(resArray);
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
    		new AlertDialog.Builder(SearchActivity.this).setTitle("Message")
         		.setMessage(str).setNeutralButton("Close", null).show();
    	}
    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_main);
		getActionBar().hide();
		
		latitude = MainActivity.latitude;
		longitude = MainActivity.longitude;
		
		placeList = (ListView)findViewById(R.id.placeList);
		btnSearch = (ImageView)findViewById(R.id.btnSearch);
		
		btnSearch.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
				builder.setTitle("Search Place");

				// Set up the input
				final EditText input = new EditText(SearchActivity.this);
				
				// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
				input.setInputType(InputType.TYPE_CLASS_TEXT);
				builder.setView(input);
				
				// Set up the buttons
				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { 
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				    	String strSearchPlace = input.getText().toString();
				    	if(!strSearchPlace.equals(""))
				    	{
				    		dialog.dismiss();
				    		SearchPlace(strSearchPlace);
				    	}
				    	
				    }
				});
				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				        dialog.cancel();
				    }
				});

				builder.show();
			}
			
		});
		
		SearchAllPlace();
	}
	public void SearchAllPlace(){
		bSearch = false;
		List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("type", "all"));
        connection = new Connection(SearchActivity.this, Global.SERVER + Global.GETPLACE_URL, params, serverhandler);
		connection.execute();
		
		
	}
	public void SearchPlace(String strKey){
		bSearch = true;
		List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("type", "name"));
        params.add(new BasicNameValuePair("name", strKey));
        connection = new Connection(SearchActivity.this, Global.SERVER + Global.GETPLACE_URL, params, serverhandler);
		connection.execute();
	}
	public void AddListAdapter(ArrayList<PlaceItem> res){
		float[] distance = new float[1];
		double min = 0;
		
        final ArrayList<PlaceItem> resArray = new ArrayList<PlaceItem>();
        for(int i = 0; i < res.size(); i++){
        	PlaceItem a = res.get(i);
        	
        	Location.distanceBetween(latitude,longitude, Double.parseDouble(a.latitude),
        			Double.parseDouble(a.longitude), distance);
        	min = distance[0];
        	int nIdx = i;
        	for(int j = i + 1; j < res.size(); j++){
        		PlaceItem b = res.get(j);
        		Location.distanceBetween(latitude,longitude, Double.parseDouble(b.latitude),
            			Double.parseDouble(b.longitude), distance);
        		if( distance[0] < min ){
        			min = distance[0];
        			nIdx = j;
        		}
        	}
        	
        	min = min / 1609.344;
        	String str = String.format("%.2f", min);
        	String strDistance = str + "miles from you";
        	
        	if( nIdx != i ){
        		PlaceItem c = res.set(nIdx,  res.get(i));
        		res.set(nIdx,  c);
        	}
        	PlaceItem c = res.get(i);
        	c.length = strDistance;
        	resArray.add(c);
        }
        
        if( resArray.size() > 0 ){
        	Global.allPlaces = resArray;
       	 PlaceListAdapter objAdapter = new PlaceListAdapter(SearchActivity.this, R.layout.search_item,
				resArray);
       	 	
			placeList.setAdapter(objAdapter);
			placeList.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					// TODO Auto-generated method stub
					PlaceItem item = resArray.get(position);
					Intent intent = new Intent(SearchActivity.this, ExpressActivity.class);
					Global.nExpressType = Global.EXPRESS_PLACE;
					Global.placeid = item.placeid;
					Global.placename = item.name;
					Global.selPlaceItem = item;
					Global.imgPath = null;
					startActivityForResult(intent, 0);
				}
				
			});
			
        }else{
        	AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
			builder.setTitle("Add Place");
			builder.setMessage("Do you want to add place?");
			
			// Set up the buttons
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { 
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
			    	dialog.dismiss();
			    	Intent intent = new Intent(SearchActivity.this, ActivityCamera.class);
			    	intent.putExtra("state", "new");
			    	startActivityForResult(intent, 0);
			    	//SearchActivity.this.finish();
			    }
			});
			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
			        dialog.cancel();
			    }
			});

			builder.show();
        }
	}
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode== Activity.RESULT_CANCELED){
        	this.finish();
        }
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
}
