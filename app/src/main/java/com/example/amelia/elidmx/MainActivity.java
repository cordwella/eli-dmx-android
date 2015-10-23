package com.example.amelia.elidmx;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.StrictMode;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.amelia.elidmx.Light;
import com.google.common.io.CharStreams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static android.widget.Toast.LENGTH_SHORT;


public class MainActivity extends ActionBarActivity {
    //public String jsonCode = "{\"scenes\":[{\"name\":\"All\",\"id\":0,\"value\":0},{\"name\":\"Stage\",\"id\":1,\"value\":0}],\"lights\":[{\"name\":\"Disco Ball\",\"id\":18,\"value\":0},{\"name\":\"Front-left White\",\"id\":1,\"value\":0},{\"name\":\"Blue\",\"id\":13,\"value\":0}]}";
    public String jsonCode = "[[\"General Wash\",{\"channel\":1,\"intensity\":1},{\"channel\":2,\"intensity\":1},{\"channel\":4,\"intensity\":1}],[\"Disco Ball\",{\"channel\":4,\"intensity\":1}]]";
    public String names;
    private ArrayList<Light> lightList;
    private List<Integer> lightValueList;
    private ArrayList<Light> sceneList;
    private List<Integer> sceneValueList;
    private LightAdapter sceneAdt;
    private ListView sceneView;
    private LightAdapter lightAdt;
    private ListView lightView;
    private boolean warmedUp = false;
    private String basePath;
    private AlertDialog dialog;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private SeekBar faderBar;
    private int fadeTime;
    private String scenesToShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pref = getApplicationContext().getSharedPreferences(String.valueOf(R.string.sharedPref), MODE_PRIVATE);
        basePath = pref.getString("base_url", null);
        scenesToShow = pref.getString("scenes_to_show", "both");
        editor = pref.edit();

        Intent intent = getIntent();
        String JSONfromIntent = intent.getStringExtra("JSON");

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("eli-dmx"));

        warmedUp = pref.getBoolean("warmed_up", false);

        fadeTime = 0;

        faderBar = (SeekBar) findViewById(R.id.fadeBar);
       // y = 0.02x^2
        //goes up to 110 points on the thing and therefore 4 minutes of fade

        faderBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                fadeTime = (int) ((Math.pow(2, 0.05*progress)-1)*1000); // (milliseconds)
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        if (JSONfromIntent != null) {
            listsAndViewsFromJSON(JSONfromIntent);
        } else {
            new dataFromServer().execute();
        }
        // This allows me to access the internet within the main thread
        // I'm using this for simplicity and also as this should be running on it's own internal thread
        //StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        //StrictMode.setThreadPolicy(policy);

        // Background of the fader bar
        //http://www.bridgecrossing.co.uk/viewpost.php?post=80
        //Get the width of the main view.
        Display display = getWindowManager().getDefaultDisplay();
        Point displaysize = new Point();
        display.getSize(displaysize);
        int width = (displaysize.x);

        //set the seekbar maximum (Must be a even number, having a remainder will cause undersirable results)
        //this variable will also determine the number of points on the scale.
        int seekbarmax = 80;
        float seekbarpoints = (width/seekbarmax)-(34/seekbarmax); //this will determine how many points on the scale there should be on the seekbar

        int[] pointsList = {0, 10, 16, 26, 35, 51, 60, 71, 81};
        String[] nameList = {"0s","1s", "2s", "5s", "10s", "30s", "1min", "2min", "4min"};


        //Set the seekbar to a max range of 10 - Nah bro we are using these to build our background however there should still be points inbetween
        //faderBar.setMax(seekbarmax);
        //Create a new bitmap that is the width of the screen
        Bitmap bitmap = Bitmap.createBitmap(width, 80, Bitmap.Config.ARGB_8888);
        //A new canvas to draw on.
        Canvas canvas = new Canvas(bitmap);


        //a new style of painting - colour and stoke thickness.
        Paint paint = new Paint();
        paint.setColor(Color.rgb(2,201,187)); //Set the colour to red
        paint.setStyle(Paint.Style.STROKE); //set the style
        paint.setStrokeWidth(2); //Stoke width

        Paint textpaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textpaint.setColor(Color.rgb(71, 71, 71));// text color RGB
        textpaint.setTextSize(24);// text size

        //this draws a box around the edge of the bitmap
        canvas.drawLine(17,0,width-17,0,paint);

        int point = 16; //initiate the point variable

        //Start a for loop that will loop seekbarpoints number of times.
        for (int i = 0; i < pointsList.length; i++  ){
            float currPoint = point + (pointsList[i]*seekbarpoints);
            if (i==pointsList.length-1) {
                canvas.drawText(nameList[i], currPoint - 13, 75, textpaint);
            }else if (nameList[i].length() > 3){
                canvas.drawText(nameList[i], currPoint - 25, 75, textpaint);
            }else {
                canvas.drawText(nameList[i], currPoint - 14, 75, textpaint);
            }
            //the modulus operator is make the long and short lines as shown in the image
            //if i can be divided without a remainder then it will draw a short line
            //long line
            canvas.drawLine(currPoint, 40, currPoint, 0, paint);

        }

        //Create a new Drawable
        Drawable d = new BitmapDrawable(getResources(),bitmap);


        //Set the seekbar widgets background to the above drawable.
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= 16){
            faderBar.setBackground(d);
        }else{
            faderBar.setBackgroundDrawable(d);
            // do something for phones running an SDK before lollipop
        }

        // Alert For warming up
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Do you want to warm up the lights?")
                .setTitle("Lights need warming up");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                new warmUpLights().execute();
            }
        });
        builder.setNegativeButton("Not Now", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        builder.setNeutralButton("Already warm", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                warmedUp = true;
                editor.putBoolean("warmed_up", true);
                editor.commit();
            }
        });

        dialog = builder.create();
        if(!warmedUp) {
            dialog.show();
        }
        sendEveryTime.start();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent sendIntent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(sendIntent);
            return true;
        } else if (id == R.id.action_refresh) {
            new dataFromServer().execute();
            return true;
        } else if (id == R.id.action_add_scene) {
            // Here is where I would put stuff had I actually put everything into the tasks and stuff I was meant to
            // TODO this
            Intent sendIntent = new Intent(getApplicationContext(), EditSceneActivity.class);
            startActivity(sendIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause(){
        dialog.dismiss();
        super.onPause();
    }


    private class warmUpLights extends AsyncTask<String, Void, String> {
        // This is almost identical to the one in openscreen
        // However it is nessavadr here incase the user wishes to refresh without restarting
        // Or if the basePath changes due to something within the preferences activity
        protected void onPreExecute() {
            Toast.makeText(getApplicationContext(), "Loading...", Toast.LENGTH_SHORT).show();
        }

        protected String doInBackground(String... strings) {
            URL url;
            HttpURLConnection connection;
            BufferedReader reader;
            String output = null;
            try {
                url = new URL(basePath + "warmup.php");
                connection = (HttpURLConnection) url.openConnection();
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                output = CharStreams.toString(reader);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return output;
        }

        protected void onPostExecute(String output) {
            if (output == null) {
                Toast.makeText(getApplicationContext(), "Hey, we couldn't warmup the lights.", Toast.LENGTH_LONG).show();
            } else {
                editor.putBoolean("warmed_up", true);
                editor.commit();
                warmedUp = true;
                Toast.makeText(getApplicationContext(), "The lights are now warming up, please wait 3 minutes before using the lights again.", Toast.LENGTH_LONG).show();
            }
        }
    }


    public void listsAndViewsFromJSON(String json) {
        names = "";
        lightList = new ArrayList<>();
        lightValueList = new ArrayList<>();
        lightAdt = new LightAdapter(this, lightList);
        lightView = (ListView) findViewById(R.id.light_list);
        lightView.setAdapter(lightAdt);

        sceneList = new ArrayList<>();
        sceneValueList = new ArrayList<>();
        sceneAdt = new LightAdapter(this, sceneList);
        sceneView = (ListView) findViewById(R.id.scene_list);
        sceneView.setAdapter(sceneAdt);

        try {
            JSONObject data = new JSONObject(json);
            JSONArray channels = data.getJSONArray("channels");
            JSONArray scenes = data.getJSONArray("scenes");

            if(!scenesToShow.equals("scenes")) {
                for (int i = 0; i < channels.length(); i++) {
                    JSONObject currentLight = channels.getJSONObject(i);
                    lightList.add(new Light(currentLight.getString("name"), currentLight.getInt("id"), 0, currentLight.getString("category"), false));
                    lightValueList.add(lightList.get(i).value);
                }
            }

            if(!scenesToShow.equals("channels")) {
                for (int i = 0; i < scenes.length(); i++) {
                    JSONObject currentLight = scenes.getJSONObject(i);
                    sceneList.add(new Light(currentLight.getString("name"), currentLight.getInt("id"), 0, currentLight.getString("category"), true));
                    sceneValueList.add(0);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "JSON issue", LENGTH_SHORT).show();
        }

        lightAdt.notifyDataSetChanged();
        sceneAdt.notifyDataSetChanged();
        TextView sceneListText = (TextView) findViewById(R.id.scene_list_header);
        TextView channelListText = (TextView) findViewById(R.id.channel_list_header);
        if(scenesToShow.equals("scenes")) {
            channelListText.setVisibility(View.INVISIBLE);
            sceneListText.setVisibility(View.VISIBLE);
        }else if(scenesToShow.equals("channels")) {
            sceneListText.setVisibility(View.INVISIBLE);
            channelListText.setVisibility(View.VISIBLE);
        }else{
            sceneListText.setVisibility(View.VISIBLE);
            channelListText.setVisibility(View.VISIBLE);
        }

        setListViewHeightBasedOnChildren(sceneView);
        setListViewHeightBasedOnChildren(lightView);
    }

    private class dataFromServer extends AsyncTask<String, Void, String> {
        // This is almost identical to the one in openscreen
        // However it is nessavadr here incase the user wishes to refresh without restarting
        // Or if the basePath changes due to something within the preferences activity
        protected void onPreExecute() {
            Toast.makeText(getApplicationContext(), "Loading...", Toast.LENGTH_SHORT).show();
        }

        protected String doInBackground(String... strings) {
            URL url;
            HttpURLConnection connection;
            BufferedReader reader;
            String output = null;
            try {
                url = new URL(basePath + "getdata.php");
                connection = (HttpURLConnection) url.openConnection();
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                output = CharStreams.toString(reader);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return output;
        }

        protected void onPostExecute(String output) {
            if (output == null) {
                Toast.makeText(getApplicationContext(), "Hey, we couldn't connect to the server. Check that the server is on, you are connected to the right Wi-Fi network, and that you typed in the server name right.", Toast.LENGTH_LONG).show();
            } else {
                //Create all of the lists and views
                listsAndViewsFromJSON(output);
            }
        }
    }

    final Thread sendEveryTime = new Thread(new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(250);
                } catch (Exception e) {
                    return;
                }


                HttpURLConnection connection;
                Reader reader = null;
                URL url;
                String sceneValuesToSend = "";
                String scenesToSend = "";

                String channelValuesToSend = "";
                String channelsToSend = "";

                if(sceneValueList != null){

                    for (int i = 0; i < sceneValueList.size(); i++) {
                        if (sceneValueList.get(i) != sceneList.get(i).value) {
                            if (sceneValuesToSend.length() == 0) {
                                sceneValuesToSend = Integer.toString(sceneList.get(i).value);
                                scenesToSend = Integer.toString(sceneList.get(i).getId());
                            } else {
                                sceneValuesToSend = sceneValuesToSend + "," + Integer.toString(sceneList.get(i).value);
                                scenesToSend = scenesToSend + "," + Integer.toString(sceneList.get(i).getId());
                            }
                        }
                        sceneValueList.set(i, sceneList.get(i).value);
                    }


                    for (int i = 0; i < lightValueList.size(); i++) {
                        if (lightValueList.get(i) != lightList.get(i).value) {
                            if (channelValuesToSend.length() == 0) {
                                channelValuesToSend = Integer.toString(lightList.get(i).value);
                                channelsToSend = Integer.toString(lightList.get(i).getId());
                            } else {
                                channelValuesToSend = channelValuesToSend + "," + Integer.toString(lightList.get(i).value);
                                channelsToSend = channelsToSend + "," + Integer.toString(lightList.get(i).getId());
                            }
                        }
                        lightValueList.set(i, lightList.get(i).value);
                    }


                    if (sceneValuesToSend.length() != 0 || channelValuesToSend.length() != 0) {
                        if (warmedUp) {
                            if (sceneValuesToSend.length() != 0) {
                                try {
                                    String strUrl = basePath + "sendscene.php?scenes=" + scenesToSend + "&values=" + sceneValuesToSend;
                                    if(fadeTime > 250){
                                        strUrl = strUrl + "&fade=" + fadeTime;
                                    }
                                    //url = new URL("http://192.168.1.4/olatesting/php/changetime.php");
                                    url = new URL(strUrl);
                                    connection = (HttpURLConnection) url.openConnection();
                                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                                    String response = reader.toString();

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (channelValuesToSend.length() != 0) {
                                try {
                                    String strUrl = basePath + "sendchanneldmx.php?channels=" + channelsToSend + "&values=" + channelValuesToSend;
                                    if(fadeTime > 250){
                                        strUrl = strUrl + "&fade=" + fadeTime;
                                    }
                                    //url = new URL("http://192.168.1.4/olatesting/php/changetime.php");
                                    url = new URL(strUrl);
                                    connection = (HttpURLConnection) url.openConnection();
                                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                                    String response = reader.toString();

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dialog.show();

                                }
                            });
                        }

                    }

                }
            }
        }
    });

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        // This allows me to have multiple listviews in the scroll view
        // SRC: http://stackoverflow.com/questions/3495890/how-can-i-put-a-listview-into-a-scrollview-without-it-collapsing/3495908#3495908
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = listView.getPaddingTop() + listView.getPaddingBottom();
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            if (listItem instanceof ViewGroup) {
                listItem.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");

            if(message.equals("refresh")){
                basePath = pref.getString("base_url", basePath);
                scenesToShow = pref.getString("scenes_to_show", "both");
                new dataFromServer().execute();
            }else if(message.equals("edit-scene")){
                Intent sendIntent = new Intent(getApplicationContext(), EditSceneActivity.class);
                String id = intent.getStringExtra("id");
                //Toast.makeText(getApplicationContext(), id, Toast.LENGTH_SHORT).show();
                sendIntent.putExtra("id", id);
                startActivity(sendIntent);
            }
        }
    };


}