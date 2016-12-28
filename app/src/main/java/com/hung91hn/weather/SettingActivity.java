package com.hung91hn.weather;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.ToggleButton;

import static com.hung91hn.weather.utils.Share.PREFERENCE_KEY;
import static com.hung91hn.weather.utils.Share.TEMP_UNIT;
import static com.hung91hn.weather.utils.Share.TIME_UPDATE;
import static com.hung91hn.weather.utils.Share.UNIT_CHANGED;
import static com.hung91hn.weather.utils.Share.UPDATE_CHANGED;

public class SettingActivity extends AppCompatActivity {
    private ToggleButton tgUnit;
    private NumberPicker npUpdate;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);


        sharedPreferences = getSharedPreferences(PREFERENCE_KEY, MODE_PRIVATE);
        editor = sharedPreferences.edit();

        addControls();


    }

    @Override
    protected void onPause() {
        super.onPause();
        editor.putInt(TIME_UPDATE, npUpdate.getValue());
        editor.putBoolean(TEMP_UNIT, tgUnit.isChecked());
        editor.commit();
    }


    private void addControls() {
        tgUnit = (ToggleButton) findViewById(R.id.tg_unit);
        npUpdate = (NumberPicker) findViewById(R.id.np_update);
        npUpdate.setMinValue(1);
        npUpdate.setMaxValue(12);


        npUpdate.setValue(sharedPreferences.getInt(TIME_UPDATE, 1));
        tgUnit.setChecked(sharedPreferences.getBoolean(TEMP_UNIT, false));

        npUpdate.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                UNIT_CHANGED=true;
            }
        });

        tgUnit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UPDATE_CHANGED=true;
            }
        });
    }
}
