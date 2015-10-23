package com.example.amelia.elidmx;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by amelia on 14/09/15.
 */
public class LightAdapterEdit extends BaseAdapter {
    private ArrayList<Light> lights;
    private Context context;
    private LayoutInflater lightInf;

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
    public View getView(int position, View convertView, ViewGroup parent) {
        // Layout base
        LinearLayout lightLay =(LinearLayout)lightInf.inflate(R.layout.edit_scene_light, parent, false);
        TextView nameView = (TextView)lightLay.findViewById(R.id.name);
        final Light currLight = lights.get(position);

        nameView.setText(currLight.getName());

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


        return lightLay;
    }

    public LightAdapterEdit(Context c, ArrayList<Light> theLights){
        lights = theLights;
        lightInf= LayoutInflater.from(c);
        context = c;

    }

}
