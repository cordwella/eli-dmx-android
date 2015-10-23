package com.example.amelia.elidmx;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.common.io.CharStreams;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class OpenScreen extends Activity {
    EditText mEdit;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    String baseURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Two things here, first check if the root URL is set and then swap into the main activity, or offer the user a chance to set up the thing themselves

        pref = getApplicationContext().getSharedPreferences(String.valueOf(R.string.sharedPref), MODE_PRIVATE);
        editor = pref.edit();
        editor.putBoolean("warmed_up", false);
        editor.apply();

        setContentView(R.layout.activity_open_screen);
        ImageView image = (ImageView) findViewById(R.id.logo);
        image.setImageResource(R.mipmap.eli_logo);
        mEdit = (EditText)findViewById(R.id.editText);
        baseURL = pref.getString("base_url", null);
        if(baseURL != null) {
            mEdit.setText(baseURL);
            new checkServer().execute(baseURL);
        }
    }



    public void onButtonPress(View view) {
        baseURL = mEdit.getText().toString();
        new checkServer().execute(baseURL);
    }

    private class checkServer extends AsyncTask<String, Void, String> {
        protected void onPreExecute(){
            Toast.makeText(getApplicationContext(), "Loading...", Toast.LENGTH_SHORT).show();
        }
        protected String doInBackground(String... strings) {
            URL url;
            HttpURLConnection connection;
            BufferedReader reader;
            String output = null;
            try {
                url = new URL(strings[0]+"getdata.php");
                connection = (HttpURLConnection) url.openConnection();
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                output = CharStreams.toString(reader);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return output;
        }



        protected void onPostExecute(String output) {
            if(output == null){
                Toast.makeText(getApplicationContext(), "Hey, we couldn't connect to the server. Check that the server is on, you are connected to the right Wi-Fi network, and that you typed in the server name right.", Toast.LENGTH_LONG).show();
            }else{
                //Jump out into next thing
                editor.putString("base_url", baseURL);
                editor.commit();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("JSON", output);
                startActivity(intent);
            }
        }
    }


}
