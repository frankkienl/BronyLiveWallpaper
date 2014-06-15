/*
 * Copyright (C) 2013 FrankkieNL
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.frankkie.bronylivewallpaper;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.*;
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
        ((TextView) findViewById(R.id.settings_note)).setTextColor(Color.BLACK);
        findViewById(R.id.settings_backgroundimage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                //intent.setAction(Intent.ACTION_PICK);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                //http://stackoverflow.com/questions/2507898/how-to-pick-an-image-from-gallery-sd-card-for-my-app-in-android
                startActivityForResult(intent, 2507898);
            }
        });
        initPonySettings();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 2507898) {
            if (resultCode == RESULT_OK) {
                prefs.edit().putString("backgroundImage", data.getDataString()).commit();
            } else {
                prefs.edit().putString("backgroundImage", "null").commit();
            }
            reinitBackground();
        }
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
                    //ignore non-pony folders
                    //uhm maybe its better to ignore folders that dont have an ini file
                    if (s.equals("images") || s.equals("kioskmode") || s.equals("sounds") || s.equals("webkitsec")
                            || s.equals("webkit") || s.equals("fonts") || s.equals("interactions.ini")) {
                        continue;
                    }                   
                    ponySettings.add(new PonySetting(s, Util.LOCATION_ASSETS));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            //Get Ponies in Addon
            //TODO: ponies in Addon
            File folder = new File(Environment.getExternalStorageDirectory().getPath() + "Ponies");
            if (folder.exists()) {
                String[] list = folder.list();
                for (final String s : list) {
                    if (s.equalsIgnoreCase("interactions.ini")) {
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
                    //reinitPonies();
                    reinitPony(ponySetting.name, ponySetting.location, b);
                }
            });
            //default = (isAsset);
            //DefaultOn when inMane6 && not external
            boolean defaultOn = Util.isInMane6(ponySetting.name) && ponySetting.location.equalsIgnoreCase(Util.LOCATION_ASSETS);
            cb.setChecked(prefs.getBoolean(ponySetting.name, defaultOn));
            TextView tv = (TextView) convertView.findViewById(R.id.settings_row_tv);
            tv.setTextColor(Color.BLACK);
            tv.setText(ponySetting.name);

            //RESET IMAGE
            ((ImageView) convertView.findViewById(R.id.settings_row_pic)).setImageResource(R.drawable.ic_launcher);
            //
            InputStream inputStream = null;
            try {
                if (ponySetting.location.equals(Util.LOCATION_ASSETS)) {
                    inputStream = assetManager.open(ponySetting.name + "/pony.ini");
                } else if (ponySetting.location.equalsIgnoreCase(Util.LOCATION_SDCARD)) {
                    inputStream = new FileInputStream(new File(Environment.getExternalStorageDirectory().getPath() + "Ponies/" + ponySetting.name + "/pony.ini"));
                }
                CSVReader reader = new CSVReader(new InputStreamReader(inputStream));
                String[] nextLine;
                    /*
                    Not all ponies have behaviours name stand, idle or walk
                    So we use A behavior as last resort.
                     */
                String[] lastResort = null;
                while ((nextLine = reader.readNext()) != null) {
                    if (nextLine[0].equalsIgnoreCase("behavior")) {
//                            behaviours.add(new Behaviour(nextLine));
                        lastResort = nextLine;
                        if (nextLine[1].equalsIgnoreCase("stand") || nextLine[1].equalsIgnoreCase("idle") || nextLine[1].equalsIgnoreCase("walk")) {
                            String imageName = nextLine[7];
                            LoadImageAsyncTask task = new LoadImageAsyncTask();
                            if (ponySetting.location.equals(Util.LOCATION_ASSETS)) {
                                task.filename = ponySetting.name + "/" + imageName;
                            } else if (ponySetting.location.equalsIgnoreCase(Util.LOCATION_SDCARD)) {
                                task.filename = Environment.getExternalStorageDirectory().getPath() + "Ponies/" + ponySetting.name + "/" + imageName;
                            }
                            task.external = false;
                            task.imageView = ((ImageView) convertView.findViewById(R.id.settings_row_pic));
                            task.execute();
                            lastResort = null; //remove, images has been found
                            break;
                        }
                    }
                }
                if (lastResort != null) {
                    String imageName = lastResort[7];
                    LoadImageAsyncTask task = new LoadImageAsyncTask();
                    task.filename = ponySetting.name + "/" + imageName;
                    task.external = false;
                    task.imageView = ((ImageView) convertView.findViewById(R.id.settings_row_pic));
                    task.execute();
                }
            } catch (Exception e) {
                //ignore, no pic for you
            }

            return convertView;
        }
    }

    public class LoadImageAsyncTask extends AsyncTask<Void, Void, Drawable> {

        public ImageView imageView;
        public String filename;
        public boolean external;

        @Override
        protected Drawable doInBackground(Void... voidz) {
            Drawable d;
            try {
                if (external) {
                    d = Drawable.createFromStream(new FileInputStream(new File(filename)), null);
                } else {
                    d = Drawable.createFromStream(getAssets().open(filename), null);
                }
                return d;
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Drawable drawable) {
            if (imageView != null) {
                if (drawable != null) {
                    imageView.setImageDrawable(drawable);
                } else {
                    imageView.setImageResource(R.drawable.ic_launcher);
                }
            }
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

    /**
     * Dont waste CPU Cycles
     * by reloading ALL ponies
     */
    @Deprecated
    public void reinitPonies() {
        if (MyWallpaperService.instance != null) {
            MyWallpaperService.instance.initPonies();
        }
    }

    public void reinitPony(String name, String location, boolean init) {
        if (MyWallpaperService.instance != null) {
            if (init) {
                MyWallpaperService.instance.initPony(name, location);
            } else {
                MyWallpaperService.instance.deInitPony(name);
            }
        }
    }

    public void reinitBackground() {
        if (MyWallpaperService.instance != null) {
            MyWallpaperService.instance.loadBackground();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0,0,0,getString(R.string.credits));
        menu.add(0,1,0,getString(R.string.website));
        return true;
//        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case 0: {
                showCredits();
                return true;
            }
            case 1: {
                showWebsite();
                return true;
            }
        }
        return false;
    }

    public void showCredits(){
        Intent i = new Intent();
        i.setClass(this, CreditsActivity.class);
        startActivity(i);
    }

    public void showWebsite(){
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://frankkie.nl/"));
        try {
            startActivity(i);
        } catch (Exception e){
            Toast.makeText(this,"Browser could not be started.\nPlease go to:\nwww.frankkie.nl",Toast.LENGTH_LONG).show();
        }

    }
}
