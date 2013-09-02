package nl.frankkie.bronylivewallpaper;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import java.io.File;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by FrankkieNL on 31-7-13.
 * <p/>
 * Thanks to: http://www.vogella.com/articles/AndroidLiveWallpaper/article.html
 */
public class MyWallpaperService extends WallpaperService {

    CopyOnWriteArrayList<Pony> ponies = new CopyOnWriteArrayList<Pony>();
    Paint paint;
    public static Rect screen = new Rect(0, 0, 240, 320); //smallest possible
    public Context context;
    public static MyEngine instance = null;
    Bitmap backgroundImage;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public Engine onCreateEngine() {
        context = this;
        //
        DisplayMetrics metrics = new DisplayMetrics();
        Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        display.getMetrics(metrics);
        int rotation = display.getOrientation();
        if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) { //vertical
            screen = new Rect(0, 0, metrics.widthPixels, metrics.heightPixels);
        } else {
            screen = new Rect(0, 0, metrics.heightPixels, metrics.widthPixels);
        }
        //
        if (instance != null) {
            instance.killMe();
            //kill old instance
            instance = null;
        }
        instance = new MyEngine();
        return instance;
    }

    public class MyEngine extends Engine {
        public int gameLoopRunning = 0;
        long lastFpsTime = 0;
        int showFps = 0;
        int fps = 0;
        double showDelta = 0;
        Handler handler = new Handler();
        float mOffset = 0;
        Runnable drawRunner = new Runnable() {
            @Override
            public void run() {
                draw();
            }
        };
        boolean visible = true;

        public MyEngine() {
            init();
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            handler.removeCallbacks(drawRunner);
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            screen = new Rect(0, 0, width, height);
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            visible = false;
            handler.removeCallbacks(drawRunner);
        }

        float xOffsetStep;

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset,
                                     float xStep, float yStep, int xPixels, int yPixels) {
            Log.e("LWP", "offsets: x" + xOffset + " y" + yOffset + " xs" + xStep + " ys" + yStep + " xp" + xPixels + " yp" + yPixels);
            mOffset = xOffset;
            xOffsetStep = xStep;
            handler.post(drawRunner);
        }

        public void killMe() {
            visible = false;
        }

        public void init() {
            new CLog(context, "BronyLiveWallpaper");
            ponies.clear();
            if (paint == null) {
                paint = new Paint();
            }

            if (Build.VERSION.SDK_INT >= 15) {
                setOffsetNotificationsEnabled(true);
            }

            LoadPoniesTask task = new LoadPoniesTask();
            task.execute();
            //
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    gameLoop();
                }
            });
            t.start();
        }

        public void initPonies() {
            ponies.clear();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyWallpaperService.this);
            //included ponies
            try {
                String[] list = getAssets().list("");
                for (String s : list) {
                    if (s.equals("images") || s.equals("kioskmode") || s.equals("sounds") || s.equals("webkit")) {
                        continue;
                    }
                    //included ponies are default on
                    if (prefs.getBoolean(s, true)) {
                        initPony(s, Util.LOCATION_ASSETS);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            //Check external ponies
            File folder = new File("/sdcard/Ponies");
            if (folder.exists()){
                String[] list = folder.list();
                for (final String s : list) {
                    if (s.equalsIgnoreCase("interactions.ini")){
                        continue;
                    }
                    if (prefs.getBoolean(s, false)){
                        initPony(s, Util.LOCATION_SDCARD);
                    }
                }
            }
        }

        public void initPony(String name, String location) {
            ponies.add(new Pony(MyWallpaperService.this, name, location));
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            this.visible = visible;
            CLog.e("Visibility: " + visible);
//            if (visible) {
//                handler.post(drawRunner);
//            } else {
//                handler.removeCallbacks(drawRunner);
//            }
            if (visible) {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        gameLoop();
                    }
                });
                t.start();
            } else {
                handler.removeCallbacks(drawRunner);
            }
        }

        private void draw() {
            if (gameLoopRunning <= 0) {
                if (visible) {
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            gameLoop();
                        }
                    });
                    t.start();
                }
            }
            final SurfaceHolder surfaceHolder = getSurfaceHolder();
            Canvas canvas = null;
            try {
                canvas = surfaceHolder.lockCanvas();
                if (canvas != null) {
                    //Draw stuff
                    drawStuff(canvas);
                } else {
                    CLog.e("Canvas == null");
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                } catch (Exception ee) {
                    e.printStackTrace();
                }
                //retry
                handler.postDelayed(drawRunner, 10);
            } finally {
                if (canvas != null) {
                    try {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    } catch (Exception e) {
                        e.printStackTrace();
                        //retry
                        handler.postDelayed(drawRunner, 10);
                    }
                } else {
                    CLog.e("Canvas == null !");
                }
            }
        }

        Rect backgroundImageRect = new Rect(0, 0, 800, 450);

        public void drawStuff(Canvas canvas) {
            if (backgroundImage != null) {
//                int numOfSteps = (int)(1/xOffsetStep )+1;
//                int newWidth = screen.height() * (800 / 450);
//                float x = map(mOffset, 0, 1, 1, 5) * (newWidth / numOfSteps);
                //canvas.drawBitmap(backgroundImage, 0, 0, paint);
                canvas.drawBitmap(backgroundImage, null, screen, paint);
            } else {
                paint.setColor(Color.BLACK);
                canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint);
                paint.setColor(Color.WHITE);
                paint.setTextSize(50);
                paint.setFakeBoldText(true);
                canvas.drawText("LOADING...", 50, 150, paint);
//                firstFrame = false;
                //return;
            }

            if (ponies.size() == 0) {
                paint.setFakeBoldText(true);
                canvas.drawText("Please Wait.. Loading Ponies...", 50, 210, paint);
            }

            for (Pony pony : ponies) {
                pony.draw(canvas, paint);
            }
        }

        void gameLoop() {
            long lastLoopTime = System.nanoTime();
            final int TARGET_FPS = 30;
            final long OPTIMAL_TIME = 1000000000 / TARGET_FPS;
            gameLoopRunning++;
            // keep looping round til the game ends
            while (visible) {
                // work out how long its been since the last update, this
                // will be used to calculate how far the entities should
                // move this loop
                long now = System.nanoTime();
                long updateLength = now - lastLoopTime;
                lastLoopTime = now;
                double delta = updateLength / ((double) OPTIMAL_TIME);

                // update the frame counter
                lastFpsTime += updateLength;
                fps++;

                // update our FPS counter if a second has passed since
                // we last recorded
                if (lastFpsTime >= 1000000000) {
                    //System.out.println("(FPS: " + fps + ")");
                    lastFpsTime = 0;
                    showFps = fps;
                    fps = 0;
                }

                // update the game logic
                doGameUpdates(delta);
                showDelta = delta;

                // draw everyting
                //postInvalidate();
                //render();
                handler.post(drawRunner);

//            Runtime.getRuntime().gc();

                // we want each frame to take 10 milliseconds, to do this
                // we've recorded when we started the frame. We add 10 milliseconds
                // to this and then factor in the current time to give
                // us our final value to wait for
                // remember this is in ms, whereas our lastLoopTime etc. vars are in ns.
                try {
                    Thread.sleep((lastLoopTime - System.nanoTime() + OPTIMAL_TIME) / 1000000);
                } catch (Exception e) {
                    //ignore
                }
            }
            gameLoopRunning--;
        }

        public void doGameUpdates(double delta) {
            for (Pony p : ponies) {
                p.updateTick();
            }
        }

        public class LoadPoniesTask extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                if (backgroundImage == null) {
                    Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.background);
//                    int newWidth = screen.height() * (800 / 450);
//                    if (screen.width() > screen.height()) {
//                        backgroundImage = Bitmap.createScaledBitmap(image, screen.width(), screen.height(), true);
//                    } else {
//                        backgroundImage = Bitmap.createScaledBitmap(image, screen.height(), screen.width(), true);
//                    }
                    backgroundImage = image;
                    Runtime.getRuntime().gc();
                }
                initPonies();
                return null;
            }
        }

        float map(float x, float in_min, float in_max, float out_min, float out_max) {
            return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
        }
    }

}
