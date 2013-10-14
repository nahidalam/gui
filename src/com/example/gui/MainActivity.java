package com.example.gui;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

	private static final String DEBUG_TAG = "DEMO";
	private TextView textView2;
	private final String DATA = "\"data\":\"";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		textView2 = (TextView) findViewById(R.id.textView1);
		myDownload();
		final Button buttonClick = (Button) findViewById(R.id.button1);
		
		buttonClick.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                
            	if(buttonClick.getText()=="OFF")
            	{
            		buttonClick.setText("ON");
            		buttonClick.setBackgroundColor(Color.RED);
            		postData ( "on" );
            	}
            	
            	else
            	{
            		buttonClick.setText("OFF");
            		buttonClick.setBackgroundColor(Color.GRAY);
            		postData ( "off" );
            	}
            }
        });	
	}
	
	public void myDownload()
	{
		TimerTask doAsynchronousTask;
	    final Handler handler = new Handler();
	    Timer timer = new Timer();

        // Gets the URL from the UI's text field.
        final String stringUrl = "http://alpha-api.elasticbeanstalk.com/v1/groups/" +
        		"1655c124-c3a7-4303-b28f-081594545eb4/records?" +
        		"appKey=4e6f08f3-6b11-4fd3-bea5-f76081820b8c" +
        		"&deviceId=0a894a89-63fd-4687-b096-efe67991da84&channel=FSR1";
        ConnectivityManager connMgr = (ConnectivityManager) 
            getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

	    doAsynchronousTask = new TimerTask() {
	        @Override
	        public void run() {
	            handler.post(new Runnable() {
	                public void run() {
	                     if(networkInfo != null && networkInfo.isConnected()){// check net connection
	                    	 new DownloadWebpageTask().execute(stringUrl);
	                    	 //System.out.println("read it\n");
	                    	 Log.d(DEBUG_TAG, "reading");
	                    }
	                     else {
	                         //textView.setText("No network connection available.");
	                     }
	                }
	            });
	        }
	    };

	    timer.schedule(doAsynchronousTask, 0, 3000);// execute in every 10 s
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void postData ( String ledState ) {
		new AsyncTask<String, Void, String>() {		
			@Override
			protected String doInBackground(String... params) {
				// Create a new HttpClient and Post Header
			    HttpClient  httpclient = new DefaultHttpClient();
			    HttpPost httppost = new HttpPost("http://alpha-api.elasticbeanstalk.com/v1/groups/" +
			    		"1655c124-c3a7-4303-b28f-081594545eb4/records?appKey=4e6f08f3-6b11-4fd3-bea5-f76081820b8c");
			    
			    try {
			        // Add your data
			    	httppost.setHeader("Content-type", "application/json");
			        httppost.setEntity( new StringEntity("{\"records\": [{\"deviceId\" : \"0a894a89-63fd-4687-b096-efe67991da84\"," +
			        		" \"channel\":\"LED1\", \"data\":\"" + params[0] +"\"}]}"));

			        // Execute HTTP Post Request
			        HttpResponse response = httpclient.execute(httppost);
			        return response.getStatusLine().toString();
			        
			    } catch (ClientProtocolException e) {
			        // TODO Auto-generated catch block
			    } catch (IOException e) {
			        // TODO Auto-generated catch block
			    }

				return null;
			}
			@Override
	        protected void onPostExecute(String result) {
	            //
	       }
		}.execute( ledState );
	} 

	
	private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
              
            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            //TextView.setText(result);
        	 Log.d(DEBUG_TAG, result);
        	 int startIndex = result.indexOf ( DATA );
        	 if ( startIndex > -1 ) {
        		 startIndex += DATA.length ( );
        		 textView2.setText( result.substring ( 
        				 					startIndex, result.indexOf ( "\"", startIndex ) ) 
        				 			+ " %" );
        	 }
       }
        
        public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
            Reader reader = null;
            reader = new InputStreamReader(stream, "UTF-8");        
            char[] buffer = new char[len];
            reader.read(buffer);
            return new String(buffer);
        }
        
        private String downloadUrl(String myurl) throws IOException {
            InputStream is = null;
            // Only display the first 500 characters of the retrieved
            // web page content.
            int len = 500;
                
            try {
                URL url = new URL(myurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                int response = conn.getResponseCode();
                //Log.d(DEBUG_TAG, "The response is: " + response);
                is = conn.getInputStream();

                // Convert the InputStream into a string
                String contentAsString = readIt(is, len);
                return contentAsString;
                
            // Makes sure that the InputStream is closed after the app is
            // finished using it.
            } finally {
                if (is != null) {
                    is.close();
                } 
            }
        }
    }

}

