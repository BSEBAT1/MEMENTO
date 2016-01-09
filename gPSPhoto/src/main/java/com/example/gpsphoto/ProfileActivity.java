package com.example.gpsphoto;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.gpsphoto.MainActivity.MessageHandler1;
import com.example.gpsphoto.SignUpActivity.MessageHandler;
import com.gpsphoto.library.DatabaseManager;
import com.gpsphoto.library.ImageListAdapter;
import com.gpsphoto.library.ProfileImageListAdapter;
import com.gpsphoto.library.QMainMenu;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.pkmmte.circularimageview.CircularImageView;





































import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ProfileActivity extends Activity {

	CircularImageView btnUser;
	EditText txtUserName;
	TextView txtUserNameShow;
	ListView lstUserImageList;
	DatabaseManager db;
	QMainMenu mainDialog;
	 private static int RESULT_LOAD_IMAGE = 1;
	 private static final int PICK_FROM_GALLERY = 2;
	 private static final int REQUEST_CAMERA     =   10;
	 
	 String mCurrentPhotoPath;
	    File photoFile;
	    
	    Connection connection;
		Handler serverhandler = new MessageHandler();
		public class MessageHandler extends Handler {
	        
	        @Override
	        public void handleMessage(Message msg) {
	        	switch(msg.what){
	        	case Global.CONNECTION_OK:		//API is success, API is to get username and profile picture.
	        		JSONObject obj = connection.getResult();
	        		String res = null;
	        		String username = null, picture = null;
	        		try {
						res = obj.getString("result");
						username = obj.getString("username");
						picture = obj.getString("picture");
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        		if( res.equals("true") ){
	        			ShowUserInfo(username, picture);
	        		}else{
	        			MessageBox("UserInfo Getting Fail!!!");
	        		}
	        		break;
	        	case Global.CONNECTION_CANCEL:	//API is fail, API is to get username and profile picture.
	        		MessageBox("Server Connection Fail!!!");
	        		break;
	        	}
	        }
	        
	        public void MessageBox(String str){
	    		new AlertDialog.Builder(ProfileActivity.this).setTitle("Message")
	         		.setMessage(str).setNeutralButton("Close", null).show();
	    	}
	    }
		Handler photoHandler = new MessageHandler1();
		public class MessageHandler1 extends Handler {
	        
	        @Override
	        public void handleMessage(Message msg) {
	        	switch(msg.what){
	        	
	        	case Global.CONNECTION_OK:		//API is success, API is to get all pictures taken by user.
	        		JSONObject obj = connection.getResult();
	        		String res = null;
	        		try {
						res = obj.getString("count");
						JSONArray data = obj.getJSONArray("data");
						
						ArrayList<String> resArray = new ArrayList<String>();
						for(int i = 0; i < data.length(); i++){
							JSONObject objItem = data.getJSONObject(i);
							resArray.add(objItem.getString("picture"));
						}
						 
				        if( resArray.size() > 0 ){
				        	ProfileImageListAdapter objAdapter = new ProfileImageListAdapter(ProfileActivity.this, R.layout.profile_image_item,
				        			resArray);
				        	lstUserImageList.setAdapter(objAdapter);
				        }
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        		
	        		break;
	        	case Global.CONNECTION_CANCEL:	//API is fail, API is to get all pictures taken by user.
	        		MessageBox("Server Connection Fail!!!");
	        		break;
	        	}
	        }
	        
	        public void MessageBox(String str){
	    		new AlertDialog.Builder(ProfileActivity.this).setTitle("Message")
	         		.setMessage(str).setNeutralButton("Close", null).show();
	    	}
	    }
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile_main);
		getActionBar().hide();
		btnUser = (CircularImageView) findViewById(R.id.btnUser);
		txtUserName = (EditText) findViewById(R.id.txtUserName);
		lstUserImageList = (ListView) findViewById(R.id.profileImageList);
		txtUserNameShow = (TextView) findViewById(R.id.txtUserNameShow);
		
		txtUserName.setVisibility(View.GONE);
		txtUserNameShow.setVisibility(View.VISIBLE);
		
		mainDialog=new QMainMenu(this); 
        mainDialog.setOnItemClickListener(new onMenuItemClick());
        
		txtUserNameShow.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				txtUserName.setVisibility(View.VISIBLE);
				txtUserNameShow.setVisibility(View.GONE);
				showSoftkeyboard();
				hideSoftInputWindow(txtUserName, true);
				return false;
			}
			
		});
		
		txtUserName.setOnKeyListener(new OnKeyListener(){

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				if( keyCode == 66 ){
					txtUserNameShow.setText(txtUserName.getText().toString());
					txtUserName.setVisibility(View.GONE);
					txtUserNameShow.setVisibility(View.VISIBLE);
					
					hideSoftInputWindow(txtUserName, false);
					
				}
				return false;
			}
			
		});
		
		//Getting User Information
			//Making Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();
	        params.add(new BasicNameValuePair("type", "get"));
	        params.add(new BasicNameValuePair("userid", String.valueOf(Global.temp_user_id)));
	        
	        //Call API
	        //Context, ServerAPI Url, Parameters, Result Handler
	        connection = new Connection(ProfileActivity.this, Global.SERVER + Global.LOGIN_URL, params, serverhandler);
	        connection.execute();
		
		
        btnUser.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mainDialog.show();
				
			}
			
		});
		
	}
	public void ShowUserInfo(String username, String picture){
		txtUserName.setText(username);
		txtUserNameShow.setText(username);
		
		////////////
		//Image Load from Server.
		//Parameter is Image URL.
			ImageLoader imageLoder = ImageLoader.getInstance();
			if(!imageLoder.isInited())
				imageLoder.init(ImageLoaderConfiguration.createDefault(ProfileActivity.this));
				
	        DisplayImageOptions options = new DisplayImageOptions.Builder()
			.imageScaleType(ImageScaleType.EXACTLY).cacheInMemory()
			.cacheOnDisc().cacheOnDisc().build();
			imageLoder.displayImage(picture, btnUser, options);
		//////////////////////////////////////////////////////
		
		//Getting User Information from Server
			List<NameValuePair> params = new ArrayList<NameValuePair>();
	        params.add(new BasicNameValuePair("type", "userid"));
	        params.add(new BasicNameValuePair("userid", String.valueOf(Global.temp_user_id)));
	        connection = new Connection(ProfileActivity.this, Global.SERVER + Global.GETPHOTO_URL, params, photoHandler);
			connection.execute();
		////////////////////////////////////////
		
	}
	public void onResume(){
		super.onResume();
		
		
	}
	//Phone Device Keyboard Show function
	private void showSoftkeyboard(){
	 	   InputMethodManager imm = (InputMethodManager) getSystemService( Context.INPUT_METHOD_SERVICE );
	 	   imm.showSoftInput(txtUserName, 0 );
	 	}
	    public boolean hideSoftInputWindow(View edit_view, boolean bState) {
	        
	        InputMethodManager imm = (InputMethodManager) getSystemService
	                (Context.INPUT_METHOD_SERVICE);
	         
	        if ( bState )
	            return imm.showSoftInput(edit_view, 0);
	        else
	            return imm.hideSoftInputFromWindow
	                    (edit_view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	             
	    }
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {  

	    super.onActivityResult(requestCode, resultCode, data);     

	    //If User click gallery to change profile picture
	     if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data){
	    	 
	    	 	//Getting Picture File from Phone Device
		         	Uri selectedImage = data.getData();
		         
		            String[] filePathColumn = { MediaStore.Images.Media.DATA };
		            Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
		            cursor.moveToFirst();
		            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
		            String picturePath = cursor.getString(columnIndex);
		            cursor.close();
		            String mimeType = getContentResolver().getType(selectedImage);
		            String type = mimeType.substring(0, 5);
		            if( type.equals("image") )
		            {
		            	btnUser.setImageBitmap(Global.setImageScale(this, selectedImage));
		            	//Update Information on Phone Device Databases.
		            	db.UpdateUserInfo(Global.temp_user_id, null, null, picturePath);
		            }
	     }else {	//if User take a photo to change profile picture
	    	 if( requestCode == REQUEST_CAMERA && resultCode== Activity.RESULT_OK){
	     
	             if (photoFile!=null && photoFile.exists()) {
	            	 Uri mUriPhoto = Uri.fromFile(photoFile);
	                 String mQuestionPhotoPath = getRealPathFromURI(ProfileActivity.this, mUriPhoto);
	                 //Update Information on Phone Device Databases
	                 db.UpdateUserInfo(Global.temp_user_id, null, null, mQuestionPhotoPath);
	 	             btnUser.setImageBitmap(Global.setImageScale(this, mUriPhoto));
	                 try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	             }
	    	 }
	     }
	}
	public static String getRealPathFromURI(Activity act, Uri contentURI)
    {
        Cursor cursor = act.getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            return contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(idx);
        }
    }
	class onMenuItemClick implements OnItemClickListener
    {
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
			mainDialog.close();			
			if(arg2==0)
			{ 
				 Intent in = new   Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                 startActivityForResult(in, RESULT_LOAD_IMAGE);
			} 
			else if(arg2==1)
			{	
				Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		        // Ensure that there's a camera activity to handle the intent
		        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
		            // Create the File where the photo should go
		            photoFile = null;
		            try {
		                photoFile = createImageFile();
		            } catch (IOException ex) {
		                // Error occurred while creating the File
		                //...
		            }
		            // Continue only if the File was successfully created
		            if (photoFile != null) {
		                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
		                        Uri.fromFile(photoFile));
		                startActivityForResult(takePictureIntent, REQUEST_CAMERA);
		            }
		        }
			}			
			
			
		}
    }
	private File createImageFile() throws IOException {
        // Create an image file name
        File storageDir = Environment.getExternalStorageDirectory();
        File dir = new File(storageDir + "/gps_photo/logo");
        dir.mkdir();
        String time = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File image = new File(dir.getAbsolutePath(), time + ".jpg");
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
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
