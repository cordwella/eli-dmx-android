package com.example.amelia.elidmx;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.common.io.CharStreams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class EditSceneActivity extends ActionBarActivity {
    private String basePath;
    private ListView lightList;
    private Spinner categorySpinner;
    private Button saveSceneButton;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private ArrayList<Light> lights;
    private String prevLightValues = "";
    private String sceneName;
    private String categoryName;
    private LightAdapterEdit lightAdt;
    private ArrayList<String> categoryList;
    private ArrayAdapter<String> catAdt;
    private Boolean blindRecord = false;
    private EditText editText;
    private String id;
    private String sceneId;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_scene);
        pref = getApplicationContext().getSharedPreferences(String.valueOf(R.string.sharedPref), MODE_PRIVATE);
        basePath = pref.getString("base_url", null);

        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        new dataFromServer().execute(id);

        lightList = (ListView) findViewById(R.id.select_lights_scene);
        categorySpinner = (Spinner) findViewById(R.id.cat_spinner);
        categoryList = new ArrayList<>();

        editText = (EditText) findViewById(R.id.scenename);

        lights = new ArrayList<>();
        lightAdt = new LightAdapterEdit(getApplicationContext(), lights);

        Switch aSwitch = (Switch) findViewById(R.id.switch1);
        blindRecord = pref.getBoolean("blind_record", false);

        aSwitch.setChecked(blindRecord);
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                blindRecord = isChecked;
            }
        });

        sendEveryTime.start();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_edit_scene, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void saveScene(View view){
        String info = "editscenes.php?";
        int category = categorySpinner.getSelectedItemPosition()+1;
        String name = editText.getText().toString();
        name = name.replaceAll(" ", "%20");
        String values = "";
        for (int i = 0; i < lights.size(); i++) {
            int value = lights.get(i).value;
            String formatValue;
            if(value==0){
                formatValue = "NULL";
            }else{
                formatValue = Integer.toString(value);
            }
            if (values.length() == 0) {
                values = formatValue;
            } else {
                values = values +"," + formatValue;
            }
        }

        if(id!=null) {
            info = info + "id="+id+"&";
        }

        info = info + "channels="+values+"&";
        info = info + "name="+name+"&";
        info = info + "category="+category;
        new sendScene().execute(info);
        //Toast.makeText(getApplicationContext(), info, Toast.LENGTH_LONG).show();
    }

    private class dataFromServer extends AsyncTask<String, Void, String> {
        // This is almost identical to the one in openscreen
        // However it is nessavadr here incase the user wishes to refresh without restarting
        // Or if the basePath changes due to something within the preferences activity
        protected void onPreExecute(){
            Toast.makeText(getApplicationContext(), "Loading...", Toast.LENGTH_SHORT).show();
        }
        protected String doInBackground(String... strings) {
            URL url;
            HttpURLConnection connection;
            BufferedReader reader;
            String output = null;
            try {
                if(strings[0]!=null) {
                    url = new URL(basePath + "getdata.php?scene=" + strings[0]);
                }else{
                    url = new URL(basePath + "getdata.php");
                }

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
                listsFromJSON(output);
            }
        }
    }

    private class sendScene extends AsyncTask<String, Void, String> {
        protected void onPreExecute(){
            Toast.makeText(getApplicationContext(), "Loading...", Toast.LENGTH_LONG).show();
        }

        protected String doInBackground(String... strings) {
            URL url;
            HttpURLConnection connection;
            BufferedReader reader;
            String output = null;
            try {
                url = new URL(basePath+strings[0]);
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
          //      Toast.makeText(getApplicationContext(), output, Toast.LENGTH_LONG).show();
                Intent intent = new Intent("eli-dmx");
                intent.putExtra("message", "refresh");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                up();
            }
        }
    }


    private void listsFromJSON(String jsonCode){
       // NavUtils.navigateUpFromSameTask(this);
        JSONObject response = null;
        try {
            response = new JSONObject(jsonCode);
            JSONArray categories = response.getJSONArray("categories");

            for (int i = 0; i < categories.length(); i++) {
                JSONObject currCat = categories.getJSONObject(i);
                categoryList.add(currCat.getString("name"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        // We have two tries here because there are two possible sets of answers we could get depending on if we are creating a new scene or editing an old one
        try{
            JSONArray scenes = response.getJSONArray("scene");
            JSONObject scene = scenes.getJSONObject(0);
            // List of ints that represent the channel in the scene
            JSONArray sceneValues = scene.getJSONArray("channels");
            // List of objects
            JSONArray channels = response.getJSONArray("channels");
            for (int i = 0; i < channels.length(); i++) {
                JSONObject currentLight = channels.getJSONObject(i);
                if(sceneValues.isNull(i)) {
                    lights.add(new Light(currentLight.getString("name"), currentLight.getInt("id"), 0, currentLight.getString("category"), false));
                }else {
                    lights.add(new Light(currentLight.getString("name"), currentLight.getInt("id"), sceneValues.getInt(i), currentLight.getString("category"), false));
                }
            }

            sceneId = String.valueOf(scene.getInt("id"));
            sceneName = scene.getString("name");
            editText.setText(sceneName);
            categoryName = scene.getString("category");

        } catch (JSONException e) {
            e.printStackTrace();
            try {
                JSONArray channels = response.getJSONArray("channels");

                for (int i = 0; i < channels.length(); i++) {
                    JSONObject currentLight = channels.getJSONObject(i);
                    lights.add(new Light(currentLight.getString("name"), currentLight.getInt("id"), 0, currentLight.getString("category"), false));
                }
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        } catch (NullPointerException e){
            e.printStackTrace();
        }

        lightList = (ListView) findViewById(R.id.select_lights_scene);
        lightList.setAdapter(lightAdt);
        lightAdt.notifyDataSetChanged();

        catAdt = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryList);
        catAdt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(catAdt);
        int currentCat = catAdt.getPosition(categoryName);
        categorySpinner.setSelection(currentCat);
    }

    // okay so up() is nessacary becuaes it wont let me run this line of code from the async task
    // and yet if I chuck it in a function its all good
    // god if youre out there please explain this to me
    private void up(){
        NavUtils.navigateUpFromSameTask(this);
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

                if(!blindRecord) {
                    HttpURLConnection connection;
                    Reader reader = null;
                    URL url;

                    String channelValuesToSend = "";
                    String channelsToSend = "";

                    Boolean valuesHaveChanged = false;

                    for (int i = 0; i < lights.size(); i++) {
                        if (channelValuesToSend.length() == 0) {
                            channelValuesToSend = Integer.toString((int) (lights.get(i).value * 2.55));
                            channelsToSend = Integer.toString(lights.get(i).getId());
                        } else {
                            channelValuesToSend = channelValuesToSend + "," + Integer.toString((int) (lights.get(i).value * 2.6));
                            channelsToSend = channelsToSend + "," + Integer.toString(lights.get(i).getId());
                        }
                    }


                    if (!channelValuesToSend.equals(prevLightValues)) {
                        if (channelValuesToSend.length() != 0) {
                            try {
                                //url = new URL("http://192.168.1.4/olatesting/php/changetime.php");
                                url = new URL(basePath + "sendchanneldmx.php?channels=" + channelsToSend + "&values=" + channelValuesToSend);
                                connection = (HttpURLConnection) url.openConnection();
                                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                                String response = reader.toString();

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                    prevLightValues = channelValuesToSend;
                }
            }

        }
    });

}
