package com.example.gpsphoto;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import com.gpsphoto.library.DatabaseManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Bitmap.CompressFormat;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;

public class Global {
	
	public static int EXPRESS_ALL = 0;
	public static int EXPRESS_USER = 1;
	public static int EXPRESS_PLACE = 2;
	
	public static final int IMAGE_LIST_ADAPTER = 1;
	public static final int USER_LIST_ADAPTER = 2;
	
	public static  android.content.SharedPreferences.Editor ed;
    public static SharedPreferences sp;
    public static DatabaseManager dbManager;
    
    public static int login_user_id;
    
    public static int temp_user_id;
    public static String username;
    public static String placename;
    public static String placeid;
    public static String imgPath;
    
    public static int nDeviceWidth;
    public static int nDeviceHeight;
    public static int nDevicePixcel;
    
    public static int nExpressType = EXPRESS_ALL;
    
    public static final int UPLOADPICTURE_OK 	= 1;
    public static final int UPLOADPICTURE_CANCEL = 2;
    public static final int CONNECTION_OK = 3;
    public static final int CONNECTION_CANCEL = 4;


    public static String SERVER = "http://app.bapmanfilms.com/gpsphoto/";
    public static String LOGIN_URL = "userapi.php";
    public static String GETPHOTO_URL = "getphotoapi.php";
    public static String GETPLACE_URL = "getplaceapi.php";
    public static String UPLOADPHOTO_URL = "uploadphoto.php";
    public static String UPLOADLOGO_URL = "uploadlogo.php";
    public static String ADDPHOGO_URL = "addpictureapi.php";
    
    public static PhotoItem selPhotoItem;
    public static PlaceItem selPlaceItem;
    public static List<PlaceItem> allPlaces;
    
    public static String cityName;
    public static String stateName;
    public static String countryName;
    
    public static String getRealPathFromURI(Context act, Uri contentURI)  //that is get Real path in Phone....
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

    public static Bitmap setImageScale(Context activity,Uri imageUri)  //Set Image Scale for Bitmap
    {
        String path = getRealPathFromURI(activity,imageUri);
        File file = new File(path);
        Bitmap bitmap = decodeFile(file);
        Bitmap bt=Bitmap.createScaledBitmap(bitmap, 400, 300, false);
        return bt;
    }

    public static Bitmap decodeFile(File f)  //Decode File
    {
        Bitmap b = null;
        int IMAGE_MAX_WIDTH = 800;
        int IMAGE_MAX_HEIGHT = 600;
        //Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        FileInputStream fis;
        try
        {
            //fis = new FileInputStream(f);
            try
            {
                BitmapFactory.decodeFile(f.getAbsolutePath(), o);

                //fis.close();
                //fis = null;
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            int scale = 1;

            if (o.outHeight > IMAGE_MAX_WIDTH || o.outWidth > IMAGE_MAX_WIDTH)
            {
                int maxwh = Math.max(o.outWidth,o.outHeight);
                while(maxwh / scale > IMAGE_MAX_WIDTH)
                    scale *= 2;
            }


            //Log.d("twinklestar.containerrecog", "width: " + o.outWidth + "height: " + o.outHeight + "scale:" + scale);
            //Decode with inSampleSize

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            o2.inJustDecodeBounds = false;
            o2.inPurgeable = true;

            //fis = new FileInputStream(f);

            try
            {
                b = BitmapFactory.decodeFile(f.getAbsolutePath(), o2);

                //fis.close();
                //fis = null;
            }
            catch (Exception e)
            {
                //	TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return b;
    }
    
    
    public static int neededRotation(File ff)
    {
    try
        {

        ExifInterface exif = new ExifInterface(ff.getAbsolutePath());
        int orientation = exif.getAttributeInt(
           ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        if (orientation == ExifInterface.ORIENTATION_ROTATE_270)
            { return 270; }
        if (orientation == ExifInterface.ORIENTATION_ROTATE_180)
            { return 180; }
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90)
            { return 90; }
        return 0;

        } catch (FileNotFoundException e)
        {
        e.printStackTrace();
        } catch (IOException e)
        {
        e.printStackTrace();
        }
    return 0;
    }
    
    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
          Matrix matrix = new Matrix();
          matrix.postRotate(angle);
          return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
    public static  void writeExternalToCache(Bitmap bitmap, File file) {
        try {
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            final BufferedOutputStream bos = new BufferedOutputStream(fos, 1024*8);
            bitmap.compress(CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {

        }

    }
}
