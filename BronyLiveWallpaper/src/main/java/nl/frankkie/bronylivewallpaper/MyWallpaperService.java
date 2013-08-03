package nl.frankkie.bronylivewallpaper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by FrankkieNL on 31-7-13.
 * <p/>
 * Thanks to: http://www.vogella.com/articles/AndroidLiveWallpaper/article.html
 */
public class MyWallpaperService extends WallpaperService {

    ArrayList<Pony> ponies = new ArrayList<Pony>();
    Paint paint;
    public static Rect screen = new Rect(0,0,240,320); //smallest possible

    @Override
    public Engine onCreateEngine() {
        return new MyEngine();
    }

    public class MyEngine extends Engine {
        long lastFpsTime = 0;
        int showFps = 0;
        int fps = 0;
        double showDelta = 0;
        Handler handler = new Handler();
        Runnable drawRunner = new Runnable() {
            @Override
            public void run() {
                draw();
            }
        };
        boolean visible = true;
        Bitmap backgroundImage;

        public MyEngine() {
            init();
        }

        public void init() {
            ponies.clear();
            if (paint == null) {
                paint = new Paint();
            }
            backgroundImage = BitmapFactory.decodeResource(getResources(),R.drawable.background);
            initPony("Applejack");
            initPony("Princess Twilight Sparkle");
            //
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    gameLoop();
                }
            });
            t.start();
        }

        public void initPony(String name) {
            ponies.add(new Pony(MyWallpaperService.this, name));
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            this.visible = visible;
//            if (visible) {
//                handler.post(drawRunner);
//            } else {
//                handler.removeCallbacks(drawRunner);
//            }
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    gameLoop();
                }
            });
            t.start();
        }

        private void draw() {
            SurfaceHolder surfaceHolder = getSurfaceHolder();
            Canvas canvas = null;
            try {
                canvas = surfaceHolder.lockCanvas();
                if (canvas != null) {
                    //Draw stuff
                    screen = new Rect(0,0,canvas.getWidth(),canvas.getHeight());
                    drawStuff(canvas);
                }
            } finally {
                if (canvas != null) {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
//            handler.removeCallbacks(drawRunner);
//            if (visible) {
//                handler.postDelayed(drawRunner, frameDelay);
//            }
        }

        public void drawStuff(Canvas canvas) {
            //paint.setColor(Color.BLACK);
            //canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint);
            canvas.drawBitmap(backgroundImage,0,0,paint);
            for (Pony pony : ponies) {
                pony.draw(canvas, paint);
            }
        }

        void gameLoop() {
            long lastLoopTime = System.nanoTime();
            final int TARGET_FPS = 30;
            final long OPTIMAL_TIME = 1000000000 / TARGET_FPS;

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
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        draw();
                    }
                });

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
        }

        public void doGameUpdates(double delta) {
            for (Pony p : ponies) {
                p.updateTick();
            }
        }

    }
}
