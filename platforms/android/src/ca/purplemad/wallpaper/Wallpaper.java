package ca.purplemad.wallpaper;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URISyntaxException;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.OnScanCompletedListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.graphics.Matrix;
import android.util.Base64;
import android.util.DisplayMetrics;


public class Wallpaper extends CordovaPlugin 
{
	public static final String SET_WALLPAPER = "setwallpaper";
	public static final String SAVE_WALLPAPER = "savewallpaper";

    public JSONObject params;
    public CallbackContext callbackContext;
    public String format;
    public String imageData;
    public String imageDataType;

    public static final String IMAGE_DATA_TYPE_BASE64 = "base64Image";
    public static final String IMAGE_DATA_TYPE_URL = "urlImage";
    public static final String RESIZE_TYPE_FACTOR = "factorResize";
    public static final String RESIZE_TYPE_MIN_PIXEL = "minPixelResize";
    public static final String RESIZE_TYPE_MAX_PIXEL = "maxPixelResize";
    public static final String RETURN_BASE64 = "returnBase64";
    public static final String RETURN_URI = "returnUri";
    public static final String FORMAT_JPG = "jpg";
    public static final String FORMAT_PNG = "png";
    public static final String DEFAULT_FORMAT = "jpg";
    public static final String DEFAULT_IMAGE_DATA_TYPE = IMAGE_DATA_TYPE_BASE64;
    public static final String DEFAULT_RESIZE_TYPE = RESIZE_TYPE_FACTOR;

	URL url;
	@Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        try {
        	JSONObject arg_object = args.getJSONObject(0);
        	String path = arg_object.getString("imagePath");
            String imageTitle = arg_object.getString("imageTitle");
            String folderName = arg_object.getString("folderName");
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(this.cordova.getActivity().getApplicationContext());
            File direct = new File(Environment.getExternalStorageDirectory()+"/"+folderName);
            if(!direct.exists()) {
            	direct.mkdir();
            }
            if(path.contains("https://") || path.contains("http://")){
            	ConnectivityManager connec = (ConnectivityManager) this.cordova.getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo ni = connec.getActiveNetworkInfo();
                if (ni == null) {
                	callbackContext.error("Internet not Available.");
    	            return false;
                }
                else{
                	if (SET_WALLPAPER.equals(action)) {
	                	
                		if(saveRemoteImage(direct,path,imageTitle)){
                			Bitmap setAsWallpaper = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/" + folderName + "/" + imageTitle + ".jpeg");
    	                    wallpaperManager.setBitmap(setAsWallpaper);
    	                    callbackContext.success();
    	    	            return true;
                		}
                	}
                	else if(SAVE_WALLPAPER.equals(action)){
                		if(saveRemoteImage(direct,path,imageTitle)){
    	                    callbackContext.success();
    	    	            return true;
                		}
                	}
                }
            }
            else{
            	if (SET_WALLPAPER.equals(action)) {
	            	InputStream ins = this.cordova.getActivity().getApplicationContext().getAssets().open(path);	            	
	            	wallpaperManager.setStream(ins);
	            	if(saveLocalImage(direct,ins,imageTitle)){
	            		callbackContext.success();
			            return true;
	            	}    
            	}
            	else if(SAVE_WALLPAPER.equals(action)){   	
                    InputStream ins = this.cordova.getActivity().getApplicationContext().getAssets().open(path);
                    if(saveLocalImage(direct,ins,imageTitle)){
                    	callbackContext.success();
                        return true;
                    }
            	}
            } 
		callbackContext.error("Invalid action");
		return false;    
        } catch(Exception e) {
            System.err.println("Exception: " + e.getMessage());
            callbackContext.error(e.getMessage());
            return false;
        } 
    }
	
	public void refresh_gallery(File file){
		MediaScannerConnection.scanFile(								
    		this.cordova.getActivity().getApplicationContext(), 
    	    new String[]{file.getAbsolutePath()}, 
    	    null, 
    	    new OnScanCompletedListener() {
    	       @Override
    	       public void onScanCompleted(String filepath, Uri uri) {
    	          System.out.println("file: " + filepath + " was scanned successfully: " + uri);
    	       }
    	});
	}
	public boolean saveLocalImage(File direct, InputStream ins, String imageTitle){
		try{
			OutputStream fos = null;
	        File file = new File(direct, imageTitle + ".jpeg");
	        Bitmap bm =BitmapFactory.decodeStream(ins);
	        fos = new FileOutputStream(file);
	        BufferedOutputStream bos = new BufferedOutputStream(fos);
	        bm.compress(Bitmap.CompressFormat.JPEG, 50, bos);
	        refresh_gallery(file);               
	        bos.flush();
	        bos.close();
	        return true;
		}catch(Exception e){
			System.err.println("Exception: " + e.getMessage());
			return false;
		}
	}
	public boolean saveRemoteImage(File direct,String path,String imageTitle){
		try{
		URL url = new URL(path); 
		String fname = imageTitle + ".jpeg";
		File file = new File (direct, fname);
		if (file.exists ()) file.delete (); 
		
		URLConnection ucon = url.openConnection();
		InputStream inputStream = null;
		HttpURLConnection httpConn = (HttpURLConnection)ucon;
		httpConn.setRequestMethod("GET");
		httpConn.connect();

		if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
			inputStream = httpConn.getInputStream();
		}

        FileOutputStream fos = new FileOutputStream(file);
        int size = 1024*1024;
        int bytesDownloaded = 0;
        byte[] buf = new byte[size];
        int byteRead;
        while (((byteRead = inputStream.read(buf)) != -1)) {
            fos.write(buf, 0, byteRead);
            bytesDownloaded += byteRead;
        }
        fos.close();
        refresh_gallery(file);
        return true;
		}catch(Exception e){
			System.err.println("Exception: " + e.getMessage());
			return false;
		}
	}

    public Bitmap getBitmap(String imageData, String imageDataType, BitmapFactory.Options options) throws IOException, URISyntaxException {
        Bitmap bmp;
        if (imageDataType.equals(IMAGE_DATA_TYPE_BASE64)) {
            byte[] blob = Base64.decode(imageData, Base64.DEFAULT);
            bmp = BitmapFactory.decodeByteArray(blob, 0, blob.length, options);
        } else {
            URL url = new URL(imageData);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            bmp = BitmapFactory.decodeStream(input, null, options);
        }
        return bmp;
    }

    public void resizeImage(JSONObject params, CallbackContext callbackContext) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            getBitmap(imageData, imageDataType, options);
            float[] sizes = calculateFactors(params, options.outWidth, options.outHeight);
            float reqWidth = options.outWidth * sizes[0];
            float reqHeight = options.outHeight * sizes[1];
            int inSampleSize = calculateInSampleSize(options, (int)reqWidth, (int)reqHeight);
    
            options = new BitmapFactory.Options();
            options.inSampleSize = inSampleSize;
            Bitmap bmp = getBitmap(imageData, imageDataType, options);
            if (bmp == null) {
                throw new IOException("The image file could not be opened.");
            }
            
            sizes = calculateFactors(params, options.outWidth, options.outHeight);
            bmp = getResizedBitmap(bmp, sizes[0], sizes[1]);
                    
            if (params.getBoolean("storeImage")) {
                // storeImage(params, format, bmp, callbackContext);
            } else {
                int quality = params.getInt("quality");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                if (format.equals(FORMAT_PNG)) {
                    bmp.compress(Bitmap.CompressFormat.PNG, quality, baos);
                } else {
                    bmp.compress(Bitmap.CompressFormat.JPEG, quality, baos);
                }
                byte[] b = baos.toByteArray();
                String returnString = Base64.encodeToString(b, Base64.DEFAULT);
                // return object
                JSONObject res = new JSONObject();
                res.put("imageData", returnString);
                res.put("width", bmp.getWidth());
                res.put("height", bmp.getHeight());
                callbackContext.success(res);
            }
        } catch (JSONException e) {
            Log.d("PLUGIN", e.getMessage());
            callbackContext.error(e.getMessage());
        } catch (IOException e) {
            Log.d("PLUGIN", e.getMessage());
            callbackContext.error(e.getMessage());
        } catch (URISyntaxException e) {
            Log.d("PLUGIN", e.getMessage());
            callbackContext.error(e.getMessage());
        }
    }

    public Bitmap getResizedBitmap(Bitmap bm, float widthFactor, float heightFactor) {
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
    
    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
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
    
    public float[] calculateFactors(JSONObject params, int width, int height) throws JSONException {
        float widthFactor;
        float heightFactor;
        String resizeType = params.getString("resizeType");
        float desiredWidth = (float)params.getDouble("width");
        float desiredHeight = (float)params.getDouble("height");
        
        if (resizeType.equals(RESIZE_TYPE_MIN_PIXEL)) {
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
        } else if (resizeType.equals(RESIZE_TYPE_MAX_PIXEL)) {
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
        
        if (params.getBoolean("pixelDensity")) {
            DisplayMetrics metrics = cordova.getActivity().getResources().getDisplayMetrics();
            if (metrics.density > 1) {
                if (widthFactor * metrics.density < 1.0 && heightFactor * metrics.density < 1.0) {
                    widthFactor *= metrics.density;
                    heightFactor *= metrics.density;
                } else {
                    widthFactor = 1.0f;
                    heightFactor = 1.0f;
                }
            }
        }
        
        float[] sizes = {widthFactor, heightFactor};
        return sizes;
    }
}

