package com.example.amelia.elidmx;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

public class SettingsActivity extends ActionBarActivity {
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private boolean blindRecord;
    private EditText urlEditText;
    private RadioGroup radioGroup;
    private String scenesToShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        urlEditText = (EditText) findViewById(R.id.editText);
        pref = getApplicationContext().getSharedPreferences(String.valueOf(R.string.sharedPref), MODE_PRIVATE);

        String basePath;
        basePath = pref.getString("base_url", null);

        if(basePath != null){
            urlEditText.setText(basePath);
        }

        blindRecord = pref.getBoolean("blind_record", false);
        Switch aSwitch = (Switch) findViewById(R.id.blind_record_default);
        aSwitch.setChecked(blindRecord);
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                blindRecord = isChecked;
            }
        });


        scenesToShow = pref.getString("scenes_to_show", "both");
        radioGroup = (RadioGroup) findViewById(R.id.to_view_rgroup);

        if(scenesToShow.equals("scenes")){
            radioGroup.check(R.id.scenes_only);
        }else if(scenesToShow.equals("channels")){
            radioGroup.check(R.id.channels_only);
        }else{
            radioGroup.check(R.id.both);
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // find which radio button is selected
                if (checkedId == R.id.scenes_only) {
                    scenesToShow = "scenes";
                } else if (checkedId == R.id.channels_only) {
                    scenesToShow = "channels";
                } else {
                    scenesToShow = "both";
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
       // getMenuInflater().inflate(R.menu.menu_settings, menu);
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

    public void savePrefs(View view){
        String basePath = urlEditText.getText().toString();
        editor = pref.edit();
        editor.putBoolean("blind_record", blindRecord);
        editor.putString("base_url", basePath);
        editor.putString("scenes_to_show", scenesToShow);
        editor.commit();
        Toast.makeText(getApplicationContext(), "Preferences Saved", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent("eli-dmx");
        intent.putExtra("message", "refresh");
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        NavUtils.navigateUpFromSameTask(this);
    }
}
