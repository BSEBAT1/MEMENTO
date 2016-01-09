package com.example.gpsphoto;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;







import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Photo Upload API
 */
public class PhotoUpload extends AsyncTask<String, Void, Boolean> {
    String responseString = "";
    boolean recording = false;
    Context mContext;
    Handler mHandler;
    String imgPath;
    String uploadFile;
    String serverFilePath;
    JSONParser jParser = new JSONParser();
    public PhotoUpload(Context context,String uploadURL,String p_imgPath,  Handler handler){
        mContext    =   context;
        mHandler    =   handler;
        imgPath = p_imgPath;
        uploadFile = uploadURL;
    }
    @Override
    protected void onPreExecute() {

        super.onPreExecute();

    }
    public boolean uploadFile(String sourceFileUri) {
        int serverResponseCode = 0;

        String fileName = imgPath;

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);

        if (!sourceFile.isFile()) {

            Toast.makeText(mContext, "File does not exist", Toast.LENGTH_SHORT).show();
            return false;
        }
        else
        {
            String serverResponseMessage = null;
            String response = null;
            try {
                String filenameArray[] = sourceFileUri.split("\\.");
                String extension = filenameArray[filenameArray.length-1];
                //fileName = fileName + "." +extension;
                
                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(uploadFile);

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);
                dos = new DataOutputStream(conn.getOutputStream());
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + fileName + "\"" + lineEnd);

                dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                serverResponseMessage = conn.getResponseMessage();

                InputStream is = conn.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] byteBuffer = new byte[1024];
                byte[] byteData = null;
                int nLength = 0;
                while((nLength = is.read(byteBuffer, 0, byteBuffer.length)) != -1) {
                    baos.write(byteBuffer, 0, nLength);
                }
                byteData = baos.toByteArray();

                response = new String(byteData);


                if(serverResponseCode == 200){

                }

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {

                ex.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
                fileName    =   e.toString();
            }

            if(response!=null && response.equals("success")) {
            	serverFilePath = sourceFile.getName();
                return true;
            }
            else
                return false;

        } // End else block
    }
   
    @Override
    protected Boolean doInBackground(String... urls) {
        boolean success = false;
        String sourceUri    =   urls[0];
        return uploadFile(sourceUri);
    }
    @Override
    protected void onPostExecute(Boolean isSuccess) {
    	Message msg = new Message();
        if(isSuccess){
            msg.what = Global.UPLOADPICTURE_OK;
        }
        else{
        	msg.what = Global.UPLOADPICTURE_CANCEL;
        }
        mHandler.sendMessage(msg);
    }
    
    public String getFileName(){
    	return serverFilePath;
    }
}
