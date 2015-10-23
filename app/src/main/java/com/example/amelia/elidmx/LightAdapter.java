package com.example.amelia.elidmx;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.io.CharStreams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import static android.support.v4.app.ActivityCompat.startActivity;

/**
 * Created by amelia on 29/07/15.
 */
public class LightAdapter extends BaseAdapter {
    private ArrayList<Light> lights;
    private LayoutInflater lightInf;
    private Context context;
    private SharedPreferences pref;
    private String basePath;

    @Override
    public int getCount() {
        return lights.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        // Layout base
        RelativeLayout lightLay =(RelativeLayout)lightInf.inflate(R.layout.light, parent, false);
        TextView nameView = (TextView)lightLay.findViewById(R.id.name);
        TextView catView = (TextView)lightLay.findViewById(R.id.category);
        final Light currLight = lights.get(position);
        String name = currLight.getName();
        nameView.setText(name);
        String category = currLight.getCategory();
        catView.setText(category);

        ImageView menuPopup = (ImageView) lightLay.findViewById(R.id.popupMenuButton);
        if(!(currLight.isEditable())){
            //hide edit view
            menuPopup.setVisibility(View.INVISIBLE);
            // Set margins like -60
            menuPopup.setMaxWidth(2);
        }else{
            // Create menu or do this before hand
            menuPopup.setVisibility(View.VISIBLE);
            menuPopup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popup = new PopupMenu(context, v);
                    MenuInflater inflater = popup.getMenuInflater();
                    inflater.inflate(R.menu.menu_edit_light, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            int id = item.getItemId();

                            if (id == R.id.action_delete) {
                                AlertDialog dialog;
                                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                                builder.setMessage("Are you sure you want to do this?")
                                        .setTitle("Delete Scene: "+ currLight.getName());
                                builder.setPositiveButton("Yes (delete scene)", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        new sendData().execute("deletescene.php?scenes=" + currLight.getId());
                                        Intent intent = new Intent("eli-dmx");
                                        intent.putExtra("message", "refresh");
                                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                                    }
                                });
                                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // User cancelled the dialog

                                    }
                                });

                                dialog = builder.create();
                                dialog.show();
                                return true;
                            } else if (id == R.id.action_edit) {
                                Intent intent = new Intent("eli-dmx");
                                intent.putExtra("message", "edit-scene");
                                intent.putExtra("id", Integer.toString(currLight.getId()));
                                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                                return true;
                            }
                            return false;
                        }
                    });
                    popup.show();
                }
            });
        }


        final ImageView pressFull = (ImageView) lightLay.findViewById(R.id.sceneFullButton);
        pressFull.setOnTouchListener(new ImageView.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if(parent.getId() == R.id.light_list) {
                            String toExe = "sendchanneldmx.php?channels="+Integer.toString(currLight.getId())+"&values=255";
                            new sendData().execute(toExe);
                            //Toast.makeText(context, toExe, Toast.LENGTH_SHORT).show();
                        }else if(parent.getId() == R.id.scene_list) {
                            String toExe = "sendscene.php?scenes="+Integer.toString(currLight.getId())+"&values=255";
                            new sendData().execute(toExe);
                            //Toast.makeText(context, toExe, Toast.LENGTH_SHORT).show();
                        }
                        return false;

                    case MotionEvent.ACTION_UP:
                        // RELEASED
                        if(parent.getId() == R.id.light_list) {
                            String toExe = "sendchanneldmx.php?channels="+Integer.toString(currLight.getId())+"&values="+currLight.value;
                            new sendData().execute(toExe);
                            //Toast.makeText(context, toExe, Toast.LENGTH_SHORT).show();
                        }else if(parent.getId() == R.id.scene_list) {
                            String toExe = "sendscene.php?scenes="+Integer.toString(currLight.getId())+"&values="+currLight.value;
                            new sendData().execute(toExe);
                            //Toast.makeText(context, toExe, Toast.LENGTH_SHORT).show();
                        }
                        return false;
                }
                return false;
            }

        });


        int fill = currLight.value;
        SeekBar seekBar = (SeekBar)lightLay.findViewById(R.id.seekBar);
        seekBar.setProgress(fill);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO Auto-generated method stub
                currLight.value = progress;

            }
        });

        lightLay.setTag(currLight.getId());
        //currently all this sets is the name but yeah
        return lightLay;
    }

    public LightAdapter(Context c, ArrayList<Light> theLights){
        lights = theLights;
        lightInf=LayoutInflater.from(c);
        context = c;
        pref = context.getSharedPreferences(String.valueOf(R.string.sharedPref), 1);
        basePath = pref.getString("base_url", null);
    }

    private class sendData extends AsyncTask<String, Void, String> {
        // This is almost identical to the one in openscreen
        // However it is nessavadr here incase the user wishes to refresh without restarting
        // Or if the basePath changes due to something within the preferences activity
        protected void onPreExecute(){
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
                Toast.makeText(context, "Hey, we couldn't connect to the server. Check that the server is on, you are connected to the right Wi-Fi network, and that you typed in the server name right.", Toast.LENGTH_LONG).show();
            }
        }
    }


}
