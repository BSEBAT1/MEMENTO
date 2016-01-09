package com.example.gpsphoto;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.gpsphoto.SignUpActivity.MessageHandler;
import com.gpsphoto.library.DatabaseManager;



public class ActivityCamera extends Activity implements  View.OnClickListener{

    public static final String TAG = ActivityCamera.class.getName();

    public ActivityCamera() {
    }

    private ImageView mQuestionPhoto;
    //private Button mActionSend;
    private TextView mQuestionText;
    private LinearLayout progressView;
    
    //private Button btnTranslate;
    public static final int REQUEST_CAMERA     =   10;
    public static final int SELECT_FILE         =   11;

    private int     mColor;
    private int     mId = -1;
    private boolean bNewState = false;
    String placeName, longitude, latitude, placeid;
    
    private boolean bSotreFile = false;
    
    PhotoUpload photoUpload;
	Connection connection;
	Handler serverhandler = new MessageHandler();
	public class MessageHandler extends Handler {
        
        @Override
        public void handleMessage(Message msg) {
        	switch(msg.what){
        	case Global.UPLOADPICTURE_OK:
        		
        		String path = Global.SERVER + "uploads/photo/" + photoUpload.getFileName();
        		StoreData(path);
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
        			new AlertDialog.Builder(ActivityCamera.this).setTitle("Notification")
		   	      		.setMessage(content).setNeutralButton("Close", new DialogInterface.OnClickListener() {
						    @Override
						    public void onClick(DialogInterface dialog, int which) {
						    	setResult(RESULT_CANCELED);
				                ActivityCamera.this.finish();
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
    		new AlertDialog.Builder(ActivityCamera.this).setTitle("Message")
         		.setMessage(str).setNeutralButton("Close", null).show();
    	}
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String state = bundle.getString("state");
        if( state != null && state.equals("new") ){
        	bNewState = true;
        }else{
        	
        	placeid = Global.placeid;
        	placeName = Global.placename;
        	
        }
        mColor  =   getResources().getColor(R.color.nature_category_color);
        findBindView();
        dispatchTakePictureIntent();
    }

    public void onResume(){
        super.onResume();


    }
   
   
    private void findBindView(){

        mQuestionPhoto    = (ImageView) findViewById(R.id.question_photo);
        mQuestionText   = (TextView) findViewById(R.id.question_text);
        progressView    = (LinearLayout) findViewById(R.id.linlaHeaderProgress);

        //mQuestionText.setText("");
        mQuestionPhoto.setOnClickListener(this);
    }

  


    private String getUTCDatetime(){

        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        fmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date date = new Date();
        String localTime = fmt.format(date);

        return localTime;
    }
    private String getUniqueId(){

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        long currentTime 		=	cal.getTimeInMillis();

        return String.valueOf(currentTime);
    }
    //PhotoUpload photoUpload=null;

    /**
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();
     
    }
    private boolean mProgressShown;
    private Integer sampleWidth;
    private Integer sampleHeight;

    private Uri mUriPhoto;
    private String mQuestionPhotoPath;
    private Integer mQuestionPhotoOrientation=0;
    private boolean mQuestionPhotoMirror = false;
    private Integer mQuestionSource=0;
    private Integer mOrientation;
    
    //Menu
    private void selectImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Library",
                "Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    dispatchTakePictureIntent();
                } else if (items[item].equals("Choose from Library")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(
                            Intent.createChooser(intent, "Select File"),
                            SELECT_FILE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }
   
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case REQUEST_CAMERA:			//Phone Device Camera Activity Calls
                if(resultCode== Activity.RESULT_OK){
                    if (photoFile!=null && photoFile.exists()) {
                        
                    	//Camera Photo file Getting 
	                        mUriPhoto = Uri.fromFile(photoFile);
	
	                        mQuestionPhotoPath = getRealPathFromURI(ActivityCamera.this, mUriPhoto);
	                        
	                        mQuestionPhotoOrientation   = 0;
	                        mQuestionPhotoMirror        = false;
	                        mQuestionSource             =   requestCode;
	                        mOrientation                =   90;
	
	                        File file = new File(mQuestionPhotoPath);
	                        int angle = Global.neededRotation(file);
	               		 
	   	                    Bitmap bitmap = Global.setImageScale(this, mUriPhoto);
	   	                    if( angle != 0 )
	   	                	   bitmap = Global.RotateBitmap(bitmap, angle);
	   	                 
	   	                	 
	                        Global.writeExternalToCache(bitmap, file);
                        //Save Photo on Databases
                        StoreFile(mQuestionPhotoPath);
                    }
                }else{
                   
                	setResult(RESULT_CANCELED);
                    finish();
                }
                break;	
            case SELECT_FILE:			//If User Click Gallery, This method will process results
                if(resultCode==Activity.RESULT_OK){
                	//Selected Photo File Getting
	                    Uri selectedImageUri = data.getData();
	
	                    mQuestionPhotoPath = getRealPathFromURI(ActivityCamera.this, selectedImageUri);
	                    mQuestionPhotoOrientation   = 0;
	                    mQuestionPhotoMirror        = false;
	                    mQuestionSource             =   requestCode;
	                    mOrientation                =   90;
	                   
	                    try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	                //Save Photo on Databases
                    StoreFile(mQuestionPhotoPath);
                }
                break;
        }
    }
    //Upload Photo on Server
     public void StoreFile(final String path){
    	 if( bSotreFile )	return;
    	 bSotreFile = true;
    	 //Call Photo Upload API
    	 //Context, ServerAPI URL, parameter, Result Handler
	    	 photoUpload = new PhotoUpload(ActivityCamera.this, Global.SERVER + Global.UPLOADPHOTO_URL, path, serverhandler);
	    	 photoUpload.execute(path);
	}
    public void StoreData(final String path){
    	//Save Photo Information into Server Databases 
    	if( bNewState == true ){		//If Photo is new, that is, There is no Current GPS Location on server databases.
    		//Making Parameters
	    		latitude = String.valueOf(MainActivity.latitude);
	    		longitude = String.valueOf(MainActivity.longitude);
	    		Date date = new Date();
	    		String strPlace = date.toString();
	    		List<NameValuePair> params = new ArrayList<NameValuePair>();
	            params.add(new BasicNameValuePair("type", "new"));
	            params.add(new BasicNameValuePair("placeid", strPlace));
	            params.add(new BasicNameValuePair("city", Global.cityName));
	            params.add(new BasicNameValuePair("state", Global.stateName));
	            params.add(new BasicNameValuePair("country", Global.countryName));
	            params.add(new BasicNameValuePair("latitude", latitude));
	            params.add(new BasicNameValuePair("longitude", longitude));
	            params.add(new BasicNameValuePair("picture", path));
	            params.add(new BasicNameValuePair("userid", String.valueOf(Global.login_user_id)));
            
	        //Photo Information Saving API
	            connection = new Connection(ActivityCamera.this, Global.SERVER + Global.ADDPHOGO_URL, params, serverhandler);
	            connection.execute();
        }
        else	//If Photo is on DB, that is, There is Current GPS Location on server databases.
        {
        	//Making parameters
	        	List<NameValuePair> params = new ArrayList<NameValuePair>();
	            params.add(new BasicNameValuePair("type", "old"));
	            params.add(new BasicNameValuePair("placeid", Global.placeid));
	            params.add(new BasicNameValuePair("picture", path));
	            params.add(new BasicNameValuePair("userid", String.valueOf(Global.login_user_id)));
            //Photo Information Saving API
	            connection = new Connection(ActivityCamera.this, Global.SERVER + Global.ADDPHOGO_URL, params, serverhandler);
	            connection.execute();
        }
    }
    
    //Check if there is same name by 3 miles.
    private boolean isNameNearByMile(String newName){
    	 List<PlaceItem> res = Global.allPlaces;
    	 if( res == null )	return false;
    	 float[] distance = new float[1];
         for(int i = 0; i < res.size(); i++){
         	PlaceItem a = res.get(i);
         	Location.distanceBetween(Double.parseDouble(latitude),Double.parseDouble(longitude), Double.parseDouble(a.latitude),
         			Double.parseDouble(a.longitude), distance);
         	double min = distance[0];
         	
         	min = min / 1609.344;
         	if( min <= 3.0 ){
         		if( a.name.equals(newName) == true )
         			return true;
         	}
         }
         return false;
    }
    
    String mCurrentPhotoPath;
    File photoFile;
    //Create ImageFile with random file name.
    private File createImageFile() throws IOException {
        // Create an image file name
        File storageDir = Environment.getExternalStorageDirectory();
        File dir = new File(storageDir + "/gps_photo");
        dir.mkdir();
        String time = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File image = new File(dir.getAbsolutePath(), time + ".jpg");
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }
    static final int REQUEST_TAKE_PHOTO = 1;

    private void dispatchTakePictureIntent() {
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

    
}
