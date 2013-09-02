package nl.frankkie.bronylivewallpaper;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import au.com.bytecode.opencsv.CSVReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by FrankkieNL on 31-7-13.
 */
public class MyPreferencesActivity extends ListActivity {

    ArrayList<PonySetting> ponySettings = new ArrayList<PonySetting>();
    MyAdapter adapter;
    LayoutInflater layoutInflater;
    SharedPreferences prefs;
    AssetManager assetManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();
    }

    protected void initUI() {
        setContentView(R.layout.settings);
        layoutInflater = getLayoutInflater();
        assetManager = getAssets();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        findViewById(android.R.id.empty).setVisibility(View.VISIBLE);
        if (isAppInstalled("nl.frankkie.bronylivewallpaperaddon")) {
            findViewById(R.id.btn_want_more).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setAction("nl.frankkie.bronylivewallpaperaddon.MAIN");
                    intent.addCategory("android.intent.category.DEFAULT");
                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(MyPreferencesActivity.this, "Cannot start Addon Package..\nI'll try starting Google Play instead", Toast.LENGTH_LONG).show();
                        Intent intent2 = new Intent();
                        intent2.setData(Uri.parse("market://details?id=nl.frankkie.bronylivewallpaperaddon"));
                        try {
                            startActivity(intent2);
                        } catch (Exception e2) {
                            Toast.makeText(MyPreferencesActivity.this, "Cannot start Google Play\nTry searching for: 'Brony Live Wallpaper Addon' on Google Play", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });
        } else {
            findViewById(R.id.btn_want_more).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setData(Uri.parse("market://details?id=nl.frankkie.bronylivewallpaperaddon"));
                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(MyPreferencesActivity.this, "Cannot start Google Play\nTry searching for: 'Brony Live Wallpaper Addon' on Google Play", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        initPonySettings();
    }

    private boolean isAppInstalled(String uri) {
        //http://stackoverflow.com/questions/11392183/how-to-check-if-the-application-is-installed-or-not-in-android-programmatically
        PackageManager pm = getPackageManager();
        boolean app_installed = false;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }

    public void initPonySettings() {
        adapter = new MyAdapter();
        MyInitPonySettingsTask task = new MyInitPonySettingsTask();
        task.execute();
    }

    public class MyInitPonySettingsTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            //Get Ponies in Assets
            try {
                String[] list = getAssets().list("");
                for (final String s : list) {
                    if (s.equals("images") || s.equals("kioskmode") || s.equals("sounds") || s.equals("webkit")) {
                        continue;
                    }
                    ponySettings.add(new PonySetting(s, Util.LOCATION_ASSETS));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            //Get Ponies in Addon
            //TODO: ponies in Addon
            File folder = new File("/sdcard/Ponies");
            if (folder.exists()){
                String[] list = folder.list();
                for (final String s : list) {
                    if (s.equalsIgnoreCase("interactions.ini")){
                        continue;
                    }
                    ponySettings.add(new PonySetting(s, Util.LOCATION_SDCARD));
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            ListView listView = getListView();
            adapter = new MyAdapter();
            listView.setAdapter(adapter);
        }
    }


    public class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return ponySettings.size();
        }

        @Override
        public Object getItem(int i) {
            return ponySettings.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.settings_row, viewGroup, false);
            }
            CheckBox cb = (CheckBox) convertView.findViewById(R.id.settings_row_cb);
            final PonySetting ponySetting = (PonySetting) getItem(i);
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    prefs.edit().putBoolean(ponySetting.name, b).commit();
                    reinitPonies();
                }
            });
            //default = (isAsset);
            cb.setChecked(prefs.getBoolean(ponySetting.name, ponySetting.location.equalsIgnoreCase(Util.LOCATION_ASSETS)));
            TextView tv = (TextView) convertView.findViewById(R.id.settings_row_tv);
            tv.setText(ponySetting.name);

            if (ponySetting.location.equals(Util.LOCATION_ASSETS)) {
                try {
                    InputStream inputStream = assetManager.open(ponySetting.name + "/pony.ini");
                    CSVReader reader = new CSVReader(new InputStreamReader(inputStream));
                    String[] nextLine;
                    while ((nextLine = reader.readNext()) != null) {
                        if (nextLine[0].equalsIgnoreCase("behavior")) {
//                            behaviours.add(new Behaviour(nextLine));
                            if (nextLine[1].equalsIgnoreCase("stand")){
                                String imageName = nextLine[7];
                                Drawable d = Drawable.createFromStream(getAssets().open(ponySetting.name + "/" + imageName), null);
                                ((ImageView)convertView.findViewById(R.id.settings_row_pic)).setImageDrawable(d);
                            }
                        }
                    }
                } catch (Exception e) {
                    //ignore, no pic for you
                }
            } else if (ponySetting.location.equalsIgnoreCase(Util.LOCATION_SDCARD)){
                try {
                    InputStream inputStream = new FileInputStream(new File("/sdcard/Ponies/" + ponySetting.name + "/pony.ini"));
                    CSVReader reader = new CSVReader(new InputStreamReader(inputStream));
                    String[] nextLine;
                    while ((nextLine = reader.readNext()) != null) {
                        if (nextLine[0].equalsIgnoreCase("behavior")) {
//                            behaviours.add(new Behaviour(nextLine));
                            if (nextLine[1].equalsIgnoreCase("stand")){
                                String imageName = nextLine[7];
                                Drawable d = Drawable.createFromStream(new FileInputStream(new File("/sdcard/Ponies/" +ponySetting.name + "/" + imageName)),null);
                                ((ImageView)convertView.findViewById(R.id.settings_row_pic)).setImageDrawable(d);
                            }
                        }
                    }
                } catch (Exception e) {
                    //ignore, no pic for you
                }
            }
            return convertView;
        }
    }

    public class PonySetting {
        public PonySetting(String name, String location) {
            this.name = name;
            this.location = location;
        }

        String name;
        String location;
    }

    /*
    protected void initUI_OLD() {
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
    */

    public void reinitPonies() {
        if (MyWallpaperService.instance != null) {
            MyWallpaperService.instance.initPonies();
        }
    }
}
