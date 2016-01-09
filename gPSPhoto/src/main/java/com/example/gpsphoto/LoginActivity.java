package com.example.gpsphoto;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.gpsphoto.SignUpActivity.MessageHandler;
import com.gpsphoto.library.DatabaseManager;

import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.os.Build;

public class LoginActivity extends Activity implements OnClickListener, OnTouchListener {

	EditText username, password;
	TextView txtSignUp;
	Button btnLogin;
	
	PhotoUpload photoUpload;
	Connection connection;
	Handler signinhandler = new MessageHandler();
	public class MessageHandler extends Handler {
        
        @Override
        public void handleMessage(Message msg) {
        	switch(msg.what){
        	
        	case Global.CONNECTION_OK:		//Login API results is Success
        		JSONObject obj = connection.getResult();		//Get API results into JSON Formats.
        		String res = null, content = null;
        		try {
					res = obj.getString("result");
					content = obj.getString("message");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		if( res != null && Integer.valueOf(res) != -1 ){
        			Global.login_user_id = Integer.valueOf(res);
            		
            		Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            		startActivityForResult(intent, 0);
            		Global.ed.putString("flag", "true");
            		Global.ed.putInt("userid", Global.login_user_id);
                    Global.ed.commit();
        		}else{
        			MessageBox("Retype UserName/Password!!!");
        		}
        		break;
        	case Global.CONNECTION_CANCEL:		//Login API results is Fail
        		MessageBox("Server Connection Fail!!!");
        		break;
        	}
        }
        
        public void MessageBox(String str){
    		new AlertDialog.Builder(LoginActivity.this).setTitle("Message")
         		.setMessage(str).setNeutralButton("Close", null).show();
    	}
    }
    @Override
	public void onResume(){
		super.onResume();
		password.setCursorVisible(false);
		username.setCursorVisible(false);
	}
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	ActionBar actionBar = getActionBar();
    	actionBar.hide();
    	
    	 Calendar cal = Calendar.getInstance();    
    	 int nMonth = cal.get(cal.MONTH);
    	 int nDay = cal.get(cal.DAY_OF_MONTH);
    	 
    	
    	setContentView(R.layout.login_main);
    	
    	username = (EditText)findViewById(R.id.username);
    	password = (EditText)findViewById(R.id.password);
    	
    	username.setOnTouchListener(this);;
    	password.setOnTouchListener(this);
    	username.setOnClickListener(this);
    	password.setOnClickListener(this);
    	txtSignUp = (TextView)findViewById(R.id.txtSignUp);
    	btnLogin = (Button)findViewById(R.id.btnLogin);
    	txtSignUp.setOnClickListener(this);
    	btnLogin.setOnClickListener(this);
    	
    	
    	RestoreLogin();	//Check If user login in last time.
     }
    void RestoreLogin(){		//If User is logined already, APP will go to the MainActivity, Automatically.
    	Global.sp = getSharedPreferences("userinfo", 1);
        Global.ed = Global.sp.edit();
        Global.ed.putString("flag", "");
	    if("true".equals(Global.sp.getString("flag", "")))
	     {
	    	Global.login_user_id = Global.sp.getInt("userid", -1);
	    	Intent intent = new Intent(LoginActivity.this, MainActivity.class);
    		startActivityForResult(intent, 0);
	      }
    }
    @Override
	public void onClick(View v) {
    	if( v.getId() == R.id.username){
    		username.setCursorVisible(true);
			password.setCursorVisible(false);
    	}else if(v.getId() == R.id.password){
    		username.setCursorVisible(false);
			password.setCursorVisible(true);
		}
    	int id = v.getId();
    	if( id == R.id.btnLogin ){
    		String txtUser = username.getText().toString();
    		String txtPass = password.getText().toString();
    		if( txtUser.isEmpty() || txtPass.isEmpty() ){
    			new AlertDialog.Builder(LoginActivity.this).setTitle("Notification")
       	      		.setMessage("Input UserName/Password!!!").setNeutralButton("Close", null).show(); 
    			return;
    		}
    		
    		
    		
    		///Login Parameters
    		List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("type", "login"));
            params.add(new BasicNameValuePair("username", txtUser));
            params.add(new BasicNameValuePair("password", txtPass));
            
            //Context, Login API URL, Parameters, Result processing Handler.
            //After complete API, API will call Handler. So, we can control API results on singninhandler.
    		connection = new Connection(LoginActivity.this, Global.SERVER + Global.LOGIN_URL, params, signinhandler);
    		connection.execute();
    		
    		
    	}else if( id == R.id.txtSignUp ){
    		//Go to the SignUp Activity
    		Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
    		startActivity(intent);
    	}
    }
    @Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
    	if( v.getId() == R.id.username ){
    		username.setHint("");
			username.setCursorVisible(true);
			password.setCursorVisible(false);
    	}else if( v.getId() == R.id.password ){
    		password.setHint("");
			username.setCursorVisible(false);
			password.setCursorVisible(true);
    	}
		return false;
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    
       @Override
   public void onDestroy(){
	   super.onDestroy();
	  
   }
   
    //Phone Device Keyboard Show and Hide Function
	    private void showSoftkeyboard(){
	 	   InputMethodManager imm = (InputMethodManager) getSystemService( Context.INPUT_METHOD_SERVICE );
	 	   imm.showSoftInput(username, 0 );
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
	 /////////////////////////////////////////////
}