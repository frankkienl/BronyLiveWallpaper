package nl.frankkie.bronylivewallpaper;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

/**
 * Created by FrankkieNL on 31-7-13.
 */
public class MyPreferencesActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();
    }

    protected void initUI() {
        setContentView(R.layout.settings);
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup viewGroup = (ViewGroup) findViewById(R.id.settings_container);
        try {
            String[] list = getAssets().list("");
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            for (final String s : list) {
                if (s.equals("images") || s.equals("kioskmode") || s.equals("sounds") || s.equals("webkit")) {
                    continue;
                }
                //included ponies are default on
                ViewGroup row = (ViewGroup) inflater.inflate(R.layout.settings_row, viewGroup, false);
                CheckBox cb = (CheckBox) row.findViewById(R.id.settings_row_cb);
                cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        prefs.edit().putBoolean(s, b).commit();
                        reinitPonies();
                    }
                });
                cb.setChecked(prefs.getBoolean(s, true));
                TextView tv = (TextView) row.findViewById(R.id.settings_row_tv);
                tv.setText(s);
                viewGroup.addView(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reinitPonies() {
        if (MyWallpaperService.instance != null) {
            MyWallpaperService.instance.initPonies();
        }
    }
}
