package com.ionicframework.myapp627805;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import android.os.AsyncTask;
import java.io.BufferedReader;
import java.net.URL;
import org.json.JSONObject;
import org.json.JSONTokener;
import java.io.InputStream;
import android.util.Log;

public class DownloadFile extends AsyncTask<String, Void, JSONObject>
{
    Exception mException = null;

    public void test() {
        System.out.println("ttest****************8");
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
        this.mException = null;
    }

    @Override
    protected JSONObject doInBackground(String... params)
    {
        StringBuilder urlString = new StringBuilder();
        urlString.append("https://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=2962d4c497bd4b64a58dbdfb31e0da27&tags=night%2C+sky%2C+moon%2C+stars&text=night+sky+stars+moon&sort=interestingness-desc&content_type=1&media=photos&extras=url_o&per_page=10&page=1&format=json&nojsoncallback=1");

        System.out.println(urlString.toString());

        HttpURLConnection urlConnection = null;
        URL url = null;
        JSONObject object = null;

        try
        {
            url = new URL(urlString.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.connect();
            InputStream inStream = null;
            inStream = urlConnection.getInputStream();
            BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));
            String temp, response = "";
            while ((temp = bReader.readLine()) != null)
                response += temp;
            bReader.close();
            inStream.close();
            urlConnection.disconnect();
            object = (JSONObject) new JSONTokener(response).nextValue();
            System.out.println(object);
        }
        catch (Exception e)
        {
            this.mException = e;
        }

        return (object);
    }

    @Override
    protected void onPostExecute(JSONObject result)
    {
        super.onPostExecute(result);
    }
}