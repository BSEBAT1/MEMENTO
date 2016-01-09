package com.gpsphoto.library;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
 
public class DatabaseManager extends SQLiteOpenHelper {
 
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;
 
    // Database Name
    private static final String DATABASE_NAME = "db_gpsphoto";
 
    private static final String TABLE_USERINFO = "userTable";
    private static final String TABLE_PLACE = "placeTable";
    private static final String TABLE_PHOTO = "photoTable";
 
    private static final String USER_ID = "userId";
    private static final String USER_NAME = "userName";
    private static final String USER_PICTURE = "userPicture";
    private static final String USER_PASSWORD = "userPassword";
    
    private static final String PLACE_ID = "id";
    private static final String PLACE_NAME = "placeName";
    private static final String PLACE_X = "xPosition";
    private static final String PLACE_Y = "yPOsition";
    
    private static final String PHOTO_ID = "id";
    private static final String PHOTO_PATH = "picture";
    private static final String PLACE_INFO = "placeId";
    private static final String USER_INFO = "userId";
    private static final String TAKE_TIME = "takeTime";
    
    public DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
 
    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_USERINFO + "("
        		+ USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                + USER_NAME + " TEXT,"
                + USER_PICTURE + " TEXT,"  
                + USER_PASSWORD + " TEXT" + ")";
        db.execSQL(CREATE_USER_TABLE);
        
        String CREATE_PLACE_TABLE = "CREATE TABLE " + TABLE_PLACE + "("
                + PLACE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                + PLACE_NAME + " TEXT,"
                + PLACE_X + " TEXT,"
                + PLACE_Y + " TEXT"  + ")";
        db.execSQL(CREATE_PLACE_TABLE);
        
        String CREATE_PHOTO_TABLE = "CREATE TABLE " + TABLE_PHOTO + "("
        		+ PHOTO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                + PHOTO_PATH + " TEXT,"
                + PLACE_INFO + " INTEGER,"
                + USER_INFO + " INTEGER,"
                + TAKE_TIME + " TEXT"  + ")";
        db.execSQL(CREATE_PHOTO_TABLE);
    }
 
    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PHOTO);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLACE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERINFO);
        // Create tables again
        onCreate(db);
    }
 
    public boolean IsExitUser(String username){
    	String selectQuery = "SELECT * FROM " + TABLE_USERINFO + " WHERE " + USER_NAME + "='" + username + "'";
    	SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        if( cursor.getCount() > 0 ) return true;
        return false;
    }
    public int getUserId(String username, String password){
    	String selectQuery = "SELECT " + USER_ID + " FROM " + TABLE_USERINFO + " WHERE " + USER_NAME + "='" + username + "' and " +
    				USER_PASSWORD + "='" + password + "'";
    	SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        if( !cursor.isAfterLast() ){
        	return cursor.getInt(0);
        }
        return -1;
    }
    public int getUserId(String username){
    	String selectQuery = "SELECT " + USER_ID + " FROM " + TABLE_USERINFO + " WHERE " + USER_NAME + "='" + username + "'";
    	SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        if( !cursor.isAfterLast() ){
        	return cursor.getInt(0);
        }
        return -1;
    }

    public void AddUserInfo(String name, String pass, String imgPath){
   	 
        SQLiteDatabase db = this.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put(USER_NAME, name);
        values.put(USER_PASSWORD, pass); 
        values.put(USER_PICTURE, imgPath);
        db.insert(TABLE_USERINFO, null, values);
        db.close();
        
    }
    public void UpdateUserInfo(int user_id, String userName, String pass, String imgPath){
    	SQLiteDatabase db = this.getWritableDatabase();
    	ContentValues values = new ContentValues();
    	if( userName != null )
    		values.put(USER_NAME, userName);
    	if( pass != null )
    		values.put(USER_PASSWORD, pass);
    	if( imgPath != null )
    		values.put(USER_PICTURE, imgPath);
    	db.update(TABLE_USERINFO, values, USER_ID + " = ?", new String[]{String.valueOf(user_id)});
    }
    public HashMap<String, String> getUserInfo(int user_id){
   	 String selectQuery = "SELECT " + USER_NAME + "," + USER_PICTURE + " FROM " + TABLE_USERINFO + " WHERE " + USER_ID + "='" + String.valueOf(user_id) + "'";
        
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        HashMap<String, String> res = new HashMap<String, String>();
        cursor.moveToFirst();
        if( !cursor.isAfterLast() )
        {
        	res.put("username",  cursor.getString(0));
       	 	res.put("path",  cursor.getString(1));
        	cursor.moveToNext();
        }
        return res;
    }
    public String getUserName(int user_id){
    	
    	String selectQuery = "SELECT " + USER_NAME + " FROM " + TABLE_USERINFO + " WHERE " + USER_ID + "='" + String.valueOf(user_id) + "'";
        
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        HashMap<String, String> res = new HashMap<String, String>();
        cursor.moveToFirst();
        if( cursor.getCount() == 0 )	return null;
        return cursor.getString(0);
    }
    
 
    public ArrayList<String> getAllPhotos(){
        
        String selectQuery = "SELECT " + PHOTO_PATH + " FROM " + TABLE_PHOTO +  " ORDER BY " + TAKE_TIME + " DESC";
          
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        ArrayList<String> resArray = new ArrayList<String>();
        // Move to first row
        cursor.moveToFirst();
        while( !cursor.isAfterLast() )
        {
        	
        	resArray.add(cursor.getString(0));
        	cursor.moveToNext();
        }
        return resArray;
    }
 
    
  public HashMap<String, String> getUserandPlaceFromImage(String imgPath){
	  String selectQuery = "SELECT " + USER_INFO + "," + PLACE_INFO + " FROM " + TABLE_PHOTO + " WHERE " +
			  			PHOTO_PATH + "='" + imgPath + "'";
	  SQLiteDatabase db = this.getReadableDatabase();
	     Cursor cursor = db.rawQuery(selectQuery, null);
	     ArrayList<String> resArray = new ArrayList<String>();
	     // Move to first row
	     cursor.moveToFirst();
	     int user_id = cursor.getInt(0);
	     int place_id = cursor.getInt(1);
	     
	     HashMap<String, String> res = new HashMap<String, String>();
	     res.put("user", getUserName(user_id));
	     res.put("placename",  getPlaceName(place_id));
	     res.put("placeid",  String.valueOf(place_id));
	     return res;
  }
  public ArrayList<String> getAllPhotosFromPlace(int placeId){
     
     String selectQuery = "SELECT " + PHOTO_PATH + " FROM " + TABLE_PHOTO +  " WHERE " + PLACE_INFO + "='" + placeId + "'";
       
     SQLiteDatabase db = this.getReadableDatabase();
     Cursor cursor = db.rawQuery(selectQuery, null);
     ArrayList<String> resArray = new ArrayList<String>();
     // Move to first row
     cursor.moveToFirst();
     while( !cursor.isAfterLast() )
     {
        resArray.add(cursor.getString(0));
    	cursor.moveToNext();
     }
     return resArray;
 }
public ArrayList<String> getAllPhotosFromUser(int user_id){
     
     //String selectQuery = "SELECT " + PHOTO_PATH + ", max(" + TAKE_TIME + ")" +  " FROM " + TABLE_PHOTO +  " group by " + PLACE_INFO ;
	 String selectQuery = "SELECT " + PHOTO_PATH +   " FROM " + TABLE_PHOTO +  " WHERE " + USER_INFO + "='" + String.valueOf(user_id) + "'" ;       
     SQLiteDatabase db = this.getReadableDatabase();
     Cursor cursor = db.rawQuery(selectQuery, null);
     ArrayList<String> resArray = new ArrayList<String>();
     cursor.moveToFirst();
     while( !cursor.isAfterLast() )
     {
     	resArray.add(cursor.getString(0));
     	cursor.moveToNext();
     }
     return resArray;
 }




public String getPlaceName(int place_id){
	
	String selectQuery = "SELECT " + PLACE_NAME + " FROM " + TABLE_PLACE + " WHERE " + PLACE_ID + "='" + String.valueOf(place_id) + "'";
    
    SQLiteDatabase db = this.getReadableDatabase();
    Cursor cursor = db.rawQuery(selectQuery, null);
    // Move to first row
    HashMap<String, String> res = new HashMap<String, String>();
    cursor.moveToFirst();
    if( cursor.getCount() == 0 )	return null;
    return cursor.getString(0);
}
public HashMap<String, String> getPlaceInfo(int place_id){
	
	String selectQuery = "SELECT " + PLACE_NAME + "," + PLACE_X + "," + PLACE_Y + " FROM " + TABLE_PLACE + " WHERE " + PLACE_ID + "='" + String.valueOf(place_id) + "'";
    
    SQLiteDatabase db = this.getReadableDatabase();
    Cursor cursor = db.rawQuery(selectQuery, null);
    // Move to first row
    HashMap<String, String> res = new HashMap<String, String>();
    cursor.moveToFirst();
    if( cursor.getCount() == 0 )	return null;
    res.put("placename", cursor.getString(0));
    res.put("latitude",  cursor.getString(1));
    res.put("longitude",  cursor.getString(2));
    return res;
}
 public ArrayList<HashMap<String, String>> getAllPlaces(String placeName){
     
     String selectQuery = "SELECT "+ PLACE_NAME + "," + PLACE_X + "," + PLACE_Y+ "," + PLACE_ID + " FROM " + TABLE_PLACE +  " WHERE " + PLACE_NAME + " LIKE '%" + placeName + "%';";
       
     SQLiteDatabase db = this.getReadableDatabase();
     Cursor cursor = db.rawQuery(selectQuery, null);
     ArrayList<HashMap<String, String>> resArray = new ArrayList<HashMap<String, String>>();
     // Move to first row
     cursor.moveToFirst();
     while( !cursor.isAfterLast() )
     {
    	 HashMap<String, String> item = new HashMap<String, String>();
    	 item.put("name", cursor.getString(0));
    	 item.put("latitude",  cursor.getString(1));
    	 item.put("longitude",  cursor.getString(2));
    	 item.put("placeid", String.valueOf(cursor.getInt(3)));
    	 resArray.add(item);
     	 cursor.moveToNext();
     }
     return resArray;
 }
public ArrayList<HashMap<String, String>> getAllPlaces(){
     
     String selectQuery = "SELECT "+ PLACE_NAME + "," + PLACE_X + "," + PLACE_Y+ "," + PLACE_ID + " FROM " + TABLE_PLACE;
       
     SQLiteDatabase db = this.getReadableDatabase();
     Cursor cursor = db.rawQuery(selectQuery, null);
     ArrayList<HashMap<String, String>> resArray = new ArrayList<HashMap<String, String>>();
     // Move to first row
     cursor.moveToFirst();
     while( !cursor.isAfterLast() )
     {
    	 HashMap<String, String> item = new HashMap<String, String>();
    	 item.put("name", cursor.getString(0));
    	 item.put("latitude",  cursor.getString(1));
    	 item.put("longitude",  cursor.getString(2));
    	 item.put("placeid", String.valueOf(cursor.getInt(3)));
    	 resArray.add(item);
     	 cursor.moveToNext();
     }
     return resArray;
 }




 public boolean AddPhoto(String placeName, String placeX, String placeY, String photo, int userid, boolean bNewPlace){
	 SQLiteDatabase db = this.getWritableDatabase();
	 int place_id = 0;
	 String time = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	 if( bNewPlace == true ){
		 ContentValues values = new ContentValues();
	     values.put(PLACE_NAME, placeName);
	     values.put(PLACE_X, placeX); 
	     values.put(PLACE_Y, placeY); 
	          // Inserting Row
	     db.insert(TABLE_PLACE, null, values);
	     db.close();
	     
	     String query = "SELECT " + PLACE_ID + " FROM " + TABLE_PLACE + " ORDER BY " + PLACE_ID + " DESC";
		 db = this.getReadableDatabase();
		 Cursor cursor = db.rawQuery(query, null);
		 cursor.moveToFirst();
		 place_id = cursor.getInt(0);
		 db.close();
	 }
	 else
	 {
		 place_id = Integer.parseInt(placeName);
		  
	 }
	 ContentValues values1 = new ContentValues();
	 values1.put(PHOTO_PATH, photo);
	 values1.put(PLACE_INFO, place_id);
	 values1.put(TAKE_TIME, time);
	 values1.put(USER_INFO,  userid);
	 db = this.getWritableDatabase();
	 db.insert(TABLE_PHOTO, null, values1);
	 db.close();
	 return true;
 }
}