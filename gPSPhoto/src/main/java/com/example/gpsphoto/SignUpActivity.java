package com.example.gpsphoto;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.gpsphoto.ProfileActivity.onMenuItemClick;
import com.gpsphoto.library.DatabaseManager;
import com.gpsphoto.library.ImageListAdapter;
import com.gpsphoto.library.QMainMenu;
import com.pkmmte.circularimageview.CircularImageView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Bitmap.CompressFormat;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class SignUpActivity extends Activity implements OnClickListener, OnTouchListener {

	CircularImageView btnUserPicture;
	EditText txtUserName;
	EditText txtPassword;
	EditText txtRetypePassword;
	Button btnRegister;
	
	
	
	QMainMenu mainDialog;
	
	private static int RESULT_LOAD_IMAGE = 1;
	private static final int PICK_FROM_GALLERY = 2;
	private static final int REQUEST_CAMERA     =   10;
	 
	String mCurrentPhotoPath;
	File photoFile;
	String imgFilePath = null;
	
	PhotoUpload photoUpload;
	Connection connection;
	Handler signupHandler = new MessageHandler();
	public class MessageHandler extends Handler {
        
        @Override
        public void handleMessage(Message msg) {
        	switch(msg.what){
        	case Global.UPLOADPICTURE_OK:
        		String txtUser = txtUserName.getText().toString();
        		String txtPass = txtPassword.getText().toString();
        		String path = Global.SERVER + "uploads/logo/" + photoUpload.getFileName();
        		
        		List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("type", "signup"));
                params.add(new BasicNameValuePair("username", txtUser));
                params.add(new BasicNameValuePair("password", txtPass));
                params.add(new BasicNameValuePair("picture", path));
                
        		connection = new Connection(SignUpActivity.this, Global.SERVER + Global.LOGIN_URL, params, this);
        		connection.execute();
        		break;
        	case Global.UPLOADPICTURE_CANCEL:
        		MessageBox("Photo Upload Fail!!!");
        		break;
        	case Global.CONNECTION_OK:
        		JSONObject obj = connection.getResult();
        		String res = null, content = null;
        		try {
					res = obj.getString("result");
					content = obj.getString("message");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		if( res.equals("true") ){
        			new AlertDialog.Builder(SignUpActivity.this).setTitle("Notification")
		   	      		.setMessage(content).setNeutralButton("Close", new DialogInterface.OnClickListener() {
						    @Override
						    public void onClick(DialogInterface dialog, int which) {
						        SignUpActivity.this.finish();
						    }
		   	      		}).show();
        		}else{
        			MessageBox(content);
        		}
        		break;
        	case Global.CONNECTION_CANCEL:
        		MessageBox("Server Connection Fail!!!");
        		break;
        	}
        }
        
        public void MessageBox(String str){
    		new AlertDialog.Builder(SignUpActivity.this).setTitle("Message")
         		.setMessage(str).setNeutralButton("Close", null).show();
    	}
    }
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.signup_main);
		getActionBar().hide();
		
		btnUserPicture = (CircularImageView) findViewById(R.id.btnUser);
		txtUserName = (EditText) findViewById(R.id.txtUserName);
		txtPassword = (EditText) findViewById(R.id.txtPassword);
		txtRetypePassword = (EditText)findViewById(R.id.txtRetypePassword);
		btnRegister = (Button)findViewById(R.id.btnRegister);
		
		txtUserName.setOnTouchListener(this);
		txtPassword.setOnTouchListener(this);
		txtRetypePassword.setOnTouchListener(this);
		
		
		mainDialog=new QMainMenu(this); 
        mainDialog.setOnItemClickListener(new onMenuItemClick());
        
        btnUserPicture.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mainDialog.show();
			}
			
		});
        
        btnRegister.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String txtUser = txtUserName.getText().toString();
				if( txtUser.isEmpty() ){
					new AlertDialog.Builder(SignUpActivity.this).setTitle("Notification")
	   	      		.setMessage("Input UserName!!!").setNeutralButton("Close", null).show();
					return;
				}
				
				String txtPass = txtPassword.getText().toString();
				if( txtPass.isEmpty() ){
					new AlertDialog.Builder(SignUpActivity.this).setTitle("Notification")
	   	      		.setMessage("Input Password!!!").setNeutralButton("Close", null).show();
					return;
				}
				
				String txtRetypePass = txtRetypePassword.getText().toString();
				if( txtRetypePass.isEmpty() ){
					new AlertDialog.Builder(SignUpActivity.this).setTitle("Notification")
	   	      		.setMessage("Retype Password!!!").setNeutralButton("Close", null).show();
					return;
				}
				
				if( txtPass.equals(txtRetypePass) == false ){
					new AlertDialog.Builder(SignUpActivity.this).setTitle("Notification")
	   	      		.setMessage("Password InCorrect!!!").setNeutralButton("Close", null).show();
					return;
				}
				
				if( imgFilePath == null ){
					new AlertDialog.Builder(SignUpActivity.this).setTitle("Notification")
	   	      		.setMessage("Select User Image").setNeutralButton("Close", null).show();
					return;
				}
				
				photoUpload = new PhotoUpload(SignUpActivity.this, Global.SERVER + Global.UPLOADLOGO_URL, imgFilePath, signupHandler);
				photoUpload.execute(imgFilePath);
				
				
			}
        	
        });
		
	}
	
	

    

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {  

	    super.onActivityResult(requestCode, resultCode, data);     

	     if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data){

	         Uri selectedImage = data.getData();
	         
	            String[] filePathColumn = { MediaStore.Images.Media.DATA };
	            Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
	            cursor.moveToFirst();
	            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
	            
	            String mimeType = getContentResolver().getType(selectedImage);
	            String type = mimeType.substring(0, 5);
	            if( type.equals("image") )
	            {
	            	btnUserPicture.setImageBitmap(Global.setImageScale(this, selectedImage));
	            	imgFilePath = cursor.getString(columnIndex);
	            }

	     }else {
	    	 if( requestCode == REQUEST_CAMERA && resultCode== Activity.RESULT_OK){
	     
	             if (photoFile!=null && photoFile.exists()) {
	            	 Uri mUriPhoto = Uri.fromFile(photoFile);
	                 imgFilePath = getRealPathFromURI(SignUpActivity.this, mUriPhoto);
	                  
	                
					
	                 File file = new File(imgFilePath);
	                 
	                 int angle = Global.neededRotation(file);
	                		 
	                 Bitmap bitmap = Global.setImageScale(this, mUriPhoto);
	                 if( angle != 0 )
	                	 bitmap = Global.RotateBitmap(bitmap, angle);
	                 
	                	 
                     Global.writeExternalToCache(bitmap, file);
                     
	                 btnUserPicture.setImageBitmap(bitmap);
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
        File dir = new File(storageDir + "/gps_photo");
        boolean bres = dir.mkdir();
        dir = new File(storageDir + "/gps_photo/logo");
        bres = dir.mkdir();
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





	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		if( v.getId() == R.id.txtUserName ){
    		txtUserName.setHint("");
    		
    	}else if( v.getId() == R.id.txtPassword ){
    		txtPassword.setHint("");
			
    	}else if( v.getId() == R.id.txtRetypePassword ){
    		txtRetypePassword.setHint("");
    	}
		return false;
	}





	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
}
