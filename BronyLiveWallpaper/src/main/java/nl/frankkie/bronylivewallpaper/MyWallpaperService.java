package nl.frankkie.bronylivewallpaper;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.graphics.Paint;
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
    public static int frameDelay = 50;

    @Override
    public Engine onCreateEngine() {
        return new MyEngine();
    }

    public class MyEngine extends Engine {
        Handler handler = new Handler();
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

        public void init(){
            ponies.clear();
            if (paint == null){
                paint = new Paint();
            }
            initPony("Applejack");
            initPony("Princess Twilight Sparkle");
        }

        public void initPony(String name){
            ponies.add(new Pony(MyWallpaperService.this,name));
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            this.visible = visible;
            if (visible) {
                handler.post(drawRunner);
            } else {
                handler.removeCallbacks(drawRunner);
            }
        }

        private void draw() {
            SurfaceHolder surfaceHolder = getSurfaceHolder();
            Canvas canvas = null;
            try {
                canvas = surfaceHolder.lockCanvas();
                if (canvas != null) {
                    //Draw stuff
                    drawStuff(canvas);
                }
            } finally {
                if (canvas != null) {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
            handler.removeCallbacks(drawRunner);
            if (visible) {
                handler.postDelayed(drawRunner, frameDelay);
            }
        }

        public void drawStuff(Canvas canvas){
            paint.setColor(Color.BLACK);
            canvas.drawRect(0,0,canvas.getWidth(),canvas.getHeight(),paint);
            for (Pony pony : ponies){
                pony.draw(canvas, paint);
            }
        }

    }
}
