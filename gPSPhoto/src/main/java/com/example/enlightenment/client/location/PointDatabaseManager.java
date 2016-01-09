/*
 * Copyright 2011 Greg Milette and Adam Stroud
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.enlightenment.client.location;

import java.util.Calendar;
import java.util.TimeZone;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;

/**
 * The interface to the point database that can retrieve point information and manage
 * the database's data.
 * 
 * @author Adam Stroud &#60;<a href="mailto:adam.stroud@gmail.com">adam.stroud@gmail.com</a>&#62;
 */
public class PointDatabaseManager
{
    private static final String TAG = "PointDatabaseManager";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "LocationTracking.sqlite";
    private static final String TABLE_POINTS = "points";
    
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_ACCURACY = "accuracy";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_PROVIDER = "provider";
    
    private static PointDatabaseManager instance;
    
    private DatabaseHelper databaseHelper;
    private SQLiteDatabase database;
    
    private PointDatabaseManager(Context context)
    {
        databaseHelper = new DatabaseHelper(context.getApplicationContext());
        database = databaseHelper.getWritableDatabase();
    }

    public static synchronized PointDatabaseManager getInstance(Context context)
    {
        if (instance == null)
        {
            instance = new PointDatabaseManager(context.getApplicationContext());
        }
        
        return instance;
    }

    public long insertPoint(Location location)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_LATITUDE, location.getLatitude());
        contentValues.put(COLUMN_LONGITUDE, location.getLongitude());
        contentValues.put(COLUMN_ACCURACY, location.getAccuracy());
        contentValues.put(COLUMN_TIME, location.getTime());
        contentValues.put(COLUMN_PROVIDER, location.getProvider());
        
        return database.insert(TABLE_POINTS, null, contentValues);
    }

    public Cursor getPointCursor(String[] columns, String selection, String[] selectionArgs, String orderBy)
    {
        return database.query(TABLE_POINTS, columns, selection, selectionArgs, null, null, orderBy);
    }
    private long latestDeltaTime	=	3*60000;
    private long twoPointDeltaTime	=	5*60000;
    public Point[] retrieveLatestM10Point(){
    	final String[] columns = {COLUMN_LATITUDE,
                COLUMN_LONGITUDE,
                COLUMN_ACCURACY,
                COLUMN_TIME,
                COLUMN_PROVIDER};
        
        Cursor cursor = database.query(TABLE_POINTS, columns, null, null, null, null, "time DESC", "10");
        Point[]	points = null;
        
        try
        {
        	int count	=	cursor.getCount();
            if (count >= 2)
            {
            	points	=	new Point[count];
            	cursor.moveToFirst();
            	for(int i=0; i<count; i++){
            		points[i]	=	new Point();
            		
            		points[i].setLatitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE)));
                    points[i].setLongitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE)));
                    points[i].setTime(cursor.getLong(cursor.getColumnIndex(COLUMN_TIME)));
                    points[i].setAccuracy(cursor.getFloat(cursor.getColumnIndex(COLUMN_ACCURACY)));
                    
                    cursor.moveToNext();
            	}
            }
            else
            {
            	points = null;
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, "Caught exception, e");
            points = null;
        }
        finally
        {
            cursor.close();
        }
        
        return points;
    }
    public Point[] retrievePreferred2Point(){
    	final String[] columns = {COLUMN_LATITUDE,
                COLUMN_LONGITUDE,
                COLUMN_ACCURACY,
                COLUMN_TIME,
                COLUMN_PROVIDER};
        
        Cursor cursor = database.query(TABLE_POINTS, columns, null, null, null, null, "time DESC", "10");
        Point[]	points = new Point[2];
        
        try
        {
            if (cursor.getCount() >= 2)
            {
            	points[0]	=	new Point();
            	points[1]	=	new Point();
            	cursor.moveToFirst();
            	
    			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
	    		long currentTime 		=	cal.getTimeInMillis();
	    		long latestTime			=	cursor.getLong(cursor.getColumnIndex(COLUMN_TIME));
            	long deltaTime			=	currentTime	-	latestTime;
            	
            	if(deltaTime < latestDeltaTime)
            	{
            		double firstLat		=	cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE));
            		double firstLon		=	cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE));
            		float firstAcc		=	cursor.getFloat(cursor.getColumnIndex(COLUMN_ACCURACY));
            		long firstTime		=	cursor.getLong(cursor.getColumnIndex(COLUMN_TIME));
                    
                    cursor.moveToNext();
                    boolean gotTwoPoint	=	false;
                    while(!cursor.isAfterLast())
                	{
                    	double secondLat		=	cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE));
                    	double secondLon		=	cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE));
                    	float secondAcc		=	cursor.getFloat(cursor.getColumnIndex(COLUMN_ACCURACY));
                    	long secondTime			=	cursor.getLong(cursor.getColumnIndex(COLUMN_TIME));
                        
                        float[] distance = new float[1];
    					Location.distanceBetween(firstLat,firstLon,secondLat,secondLon,distance);
    					
                        if(distance[0] > firstAcc + secondAcc)
                        {
                        	double betTime	=	firstTime - secondTime;
                        	
                        	if(betTime < 5*twoPointDeltaTime)
                        	{
                        		// match condition
                        		points[0].setLatitude	(firstLat);
                                points[0].setLongitude	(firstLon);
                                points[0].setTime		(firstTime);
                                points[0].setAccuracy	(firstAcc);
                                
                        		points[1].setLatitude	(secondLat);
                                points[1].setLongitude	(secondLon);
                                points[1].setTime		(secondTime);
                                points[1].setAccuracy	(secondAcc);
                                
                                gotTwoPoint	=	true;
                                break;
                        	}
                        }
                    	cursor.moveToNext();
                	}
                    
                    if(gotTwoPoint == false)
                    {
                    	points	=	null;
                    }
            	}
            	else
            		points = null;
            }
            else
            {
            	points = null;
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, "Caught exception, e");
            points = null;
        }
        finally
        {
            cursor.close();
        }
        
        return points;
    }
    public Point[] retrieveLatest2Point()
    {
        final String[] columns = {COLUMN_LATITUDE,
                COLUMN_LONGITUDE,
                COLUMN_ACCURACY,
                COLUMN_TIME,
                COLUMN_PROVIDER};
        
        Cursor cursor = database.query(TABLE_POINTS, columns, null, null, null, null, "time DESC", "2");
        Point[]	points = new Point[2];
        
        try
        {
            if (cursor.getCount() >= 2)
            {
            	points[0]	=	new Point();
            	points[1]	=	new Point();
                cursor.moveToFirst();
                
                points[0].setLatitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE)));
                points[0].setLongitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE)));
                points[0].setTime(cursor.getLong(cursor.getColumnIndex(COLUMN_TIME)));
                points[0].setAccuracy(cursor.getFloat(cursor.getColumnIndex(COLUMN_ACCURACY)));
                points[0].setProvider(cursor.getString(cursor.getColumnIndex(COLUMN_PROVIDER)));
                
                cursor.moveToNext();
                
                points[1].setLatitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE)));
                points[1].setLongitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE)));
                points[1].setTime(cursor.getLong(cursor.getColumnIndex(COLUMN_TIME)));
                points[1].setAccuracy(cursor.getFloat(cursor.getColumnIndex(COLUMN_ACCURACY)));
                points[1].setProvider(cursor.getString(cursor.getColumnIndex(COLUMN_PROVIDER)));
            }
            else
            {
            	points = null;
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, "Caught exception, e");
            points = null;
        }
        finally
        {
            cursor.close();
        }
        
        return points;
    }
    public Point retrieveLatestPoint()
    {
        final String[] columns = {COLUMN_LATITUDE,
                COLUMN_LONGITUDE,
                COLUMN_ACCURACY,
                COLUMN_TIME,
                COLUMN_PROVIDER};
        
        Cursor cursor = database.query(TABLE_POINTS, columns, null, null, null, null, "time DESC", "1");
        Point point;
        
        try
        {
            if (cursor.getCount() > 0)
            {
                cursor.moveToFirst();
                
                point = new Point();
                point.setLatitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE)));
                point.setLongitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE)));
                point.setTime(cursor.getLong(cursor.getColumnIndex(COLUMN_TIME)));
                point.setAccuracy(cursor.getFloat(cursor.getColumnIndex(COLUMN_ACCURACY)));
                point.setProvider(cursor.getString(cursor.getColumnIndex(COLUMN_PROVIDER)));
            }
            else
            {
                point = null;
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, "Caught exception, e");
            point = null;
        }
        finally
        {
            cursor.close();
        }
        
        return point;
    }

    public void deletePoints()
    {
        database.delete(TABLE_POINTS, null, null);
    }

    public void close()
    {
        synchronized (PointDatabaseManager.class)
        {
            databaseHelper.close();
            instance = null;
            database = null;
        }
    }

    private static final class DatabaseHelper extends SQLiteOpenHelper
    {
        public DatabaseHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            db.execSQL("CREATE TABLE " +
            		"points (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            		"latitude REAL, " +
            		"longitude REAL, " +
            		"accuracy REAL, " +
            		"time INTEGER, " +
            		"provider TEXT);");

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            db.execSQL("DROP TABLE IF EXISTS points;");
        }
    }
}
