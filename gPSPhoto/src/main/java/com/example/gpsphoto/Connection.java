package com.example.gpsphoto;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

/*
 * API Connection Function.
 */
public class Connection extends AsyncTask<String, String, String> {
	 
    /**
     * Before starting background thread Show Progress Dialog
     * */
	Activity act;
	String server_url;
	List<NameValuePair> params;
	private ProgressDialog pDialog;
	JSONObject json;
	JSONParser jParser;
	Handler mHandler;
	
	public int nConnectionType;
	//Context, ServerURL, API parameter, Result Process Handler.
	public Connection(Activity act, String url, List<NameValuePair> p, Handler handler){
		this.act = act;
		this.server_url = url;
		this.params = p;
		this.mHandler = handler;
		json = null;
		jParser = new JSONParser();
		nConnectionType = 0;
		
	}
    @Override
    protected void onPreExecute() {				//Before request API
        super.onPreExecute();
        pDialog = new ProgressDialog(act);
        pDialog.setMessage("Please wait...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
    }

    /**
     * getting All products from url
     * */
    protected String doInBackground(String... args) {	//Processing API
        json = jParser.makeHttpRequest(server_url, "GET", params);
        return null;
    }

    /**
     * After completing background task Dismiss the progress dialog
     * **/
    protected void onPostExecute(String file_url) {	//After API is completed
        pDialog.dismiss();
        Message msg = new Message();
        if( json != null )
        	msg.what = Global.CONNECTION_OK;
        else
        	msg.what = Global.CONNECTION_CANCEL;
        mHandler.sendMessage(msg);
    }
    
    public JSONObject getResult(){
    	return json;
    }

}