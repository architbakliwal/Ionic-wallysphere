package com.ionicframework.myapp627805;

import java.io.IOException;
import java.util.Random;
import android.graphics.BitmapFactory;
import java.io.ByteArrayOutputStream;
import android.util.Base64;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.URISyntaxException;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.DisplayMetrics;
import java.net.HttpURLConnection;
import java.io.FileOutputStream;
import android.net.Uri;
import java.io.OutputStream;
import android.media.MediaScannerConnection.OnScanCompletedListener;
import android.media.MediaScannerConnection;
import java.net.URL;
import java.io.File;
import java.io.InputStream;
import android.os.Environment;
import android.view.WindowManager;
import android.content.Context;
import android.app.Activity;


public class DownloadAndResize extends Activity{

    public JSONObject params = new JSONObject();

    private Bitmap getBitmap(String imageData, BitmapFactory.Options options) throws IOException, URISyntaxException {
        Bitmap bmp;
        
        URL url = new URL(imageData);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.connect();
        InputStream input = connection.getInputStream();
        bmp = BitmapFactory.decodeStream(input, null, options);
        input.close();
        connection.disconnect();
        
        return bmp;
    }
    
    private void storeImage(JSONObject params, Bitmap bmp) throws JSONException, IOException, URISyntaxException {
        
        int quality = 100;
        String filename = params.getString("filename");
        String folderName = params.getString("directory");
        File folder = new File(Environment.getExternalStorageDirectory()+"/"+folderName);
        if(!folder.exists()) {
            folder.mkdir();
        }
        File file = new File(folder, filename + ".jpeg");
        if (file.exists ()) file.delete (); 

        OutputStream outStream = new FileOutputStream(file);
        bmp.compress(Bitmap.CompressFormat.JPEG, quality, outStream);

        MediaScannerConnection.scanFile(                                
            CordovaApp.getApplicationContext(), 
            new String[]{file.getAbsolutePath()}, 
            null, 
            new OnScanCompletedListener() {
               @Override
               public void onScanCompleted(String filepath, Uri uri) {
                  System.out.println("file: " + filepath + " was scanned successfully: " + uri);
               }
        });

        outStream.flush();
        outStream.close();
    }

	public void download(String url, String filename) {
		try {

            params.put("filename", filename);
            params.put("directory", "Wallysphere");

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            getBitmap(url, options);
            float[] sizes = calculateFactors(options.outWidth, options.outHeight);
            float reqWidth = options.outWidth * sizes[0];
            float reqHeight = options.outHeight * sizes[1];
            int inSampleSize = calculateInSampleSize(options, (int)reqWidth, (int)reqHeight);
    
            options = new BitmapFactory.Options();
            options.inSampleSize = inSampleSize;
            Bitmap bmp = getBitmap(url, options);
            if (bmp == null) {
                throw new IOException("The image file could not be opened.");
            }
            
            sizes = calculateFactors(options.outWidth, options.outHeight);
            bmp = getResizedBitmap(bmp, sizes[0], sizes[1]);

            storeImage(params, bmp);
                    
        } catch (JSONException e) {
            Log.d("PLUGIN", e.getMessage());
            // callbackContext.error(e.getMessage());
        } catch (IOException e) {
            Log.d("PLUGIN", e.getMessage());
            // callbackContext.error(e.getMessage());
        } catch (URISyntaxException e) {
            Log.d("PLUGIN", e.getMessage());
            // callbackContext.error(e.getMessage());
        }
    }

    private Bitmap getResizedBitmap(Bitmap bm, float widthFactor, float heightFactor) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        // create a matrix for the manipulation
        Matrix matrix = new Matrix();
        // resize the bit map
        matrix.postScale(widthFactor, heightFactor);
        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
                matrix, false);
        return resizedBitmap;
    }
    
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
    
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
    
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
    
        return inSampleSize;
    }
    
    private float[] calculateFactors(int width, int height) throws JSONException {
        float widthFactor;
        float heightFactor;
        String resizeType = "maxPixelResize";
        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        float desiredWidth = (float)outMetrics .widthPixels;
        float desiredHeight = (float)outMetrics .heightPixels;

        System.out.println(desiredWidth + " x " + desiredHeight);
        
        if (resizeType.equals("minPixelResize")) {
            widthFactor = desiredWidth / (float)width;
            heightFactor = desiredHeight / (float)height;
            if (widthFactor > heightFactor && widthFactor <= 1.0) {
                heightFactor = widthFactor;
            } else if (heightFactor <= 1.0) {
                widthFactor = heightFactor;
            } else {
                widthFactor = 1.0f;
                heightFactor = 1.0f;
            }
        } else if (resizeType.equals("maxPixelResize")) {
            widthFactor = desiredWidth / (float)width;
            heightFactor = desiredHeight / (float)height;
            if (widthFactor == 0.0) {
                widthFactor = heightFactor;
            } else if (heightFactor == 0.0) {
                heightFactor = widthFactor;
            } else if (widthFactor > heightFactor) {
                widthFactor = heightFactor; // scale to fit height
            } else {
                heightFactor = widthFactor; // scale to fit width
            }
        } else {
            widthFactor = desiredWidth;
            heightFactor = desiredHeight;
        }

        //pixelDensity
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        if (metrics.density > 1) {
            if (widthFactor * metrics.density < 1.0 && heightFactor * metrics.density < 1.0) {
                widthFactor *= metrics.density;
                heightFactor *= metrics.density;
            } else {
                widthFactor = 1.0f;
                heightFactor = 1.0f;
            }
        }
        
        float[] sizes = {widthFactor, heightFactor};
        return sizes;
    }
}