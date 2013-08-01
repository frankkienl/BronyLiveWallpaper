package nl.frankkie.bronylivewallpaper;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

import au.com.bytecode.opencsv.CSVReader;
import jp.tomorrowkey.android.gifplayer.GifDecoder;

/**
 * Created by FrankkieNL on 31-7-13.
 */
public class Pony {

    public static final int DIR_LEFT = 0;
    public static final int DIR_UP = 270;
    public static final int DIR_RIGHT = 180;
    public static final int DIR_DOWN = 90;
    //TODO FIX THESE
    public static final int DIR_LEFT_UP = 135;
    public static final int DIR_LEFT_DOWN = 225;
    public static final int DIR_RIGHT_UP = 45;
    public static final int DIR_RIGHT_DOWN = 325;

    float positionX = 50; //default
    float positionY = 150; //default
    int width = 130; //default
    int height = 96; //default
    String name = "Unnamed Pony"; //default
    boolean outsideWrap = false;
    boolean limitAtEdge = true;
    Context context;
    ArrayList<Behaviour> behaviours = new ArrayList<Behaviour>();
    Behaviour currentBehaviour = null;
    float velocity = 0f;
    float direction = 0f;
    Rect screen;
    long timeToChangeBehaviour = 0;
    boolean imageRight = true;
    GifDecoder gifDecoder;

    public Pony(Context context, String name) {
        this.name = name;
        this.context = context;
        init();
    }

    public void setCurrentBehaviour(Behaviour behaviour) {
        currentBehaviour = behaviour;
        //set stuff!
        Random random = new Random();
        //change behaviour time
        timeToChangeBehaviour = System.currentTimeMillis() + (long) (currentBehaviour.maxDuration * 1000);
        //timeToChangeBehaviour += (long) random.nextInt((int) ((currentBehaviour.maxDuration - currentBehaviour.minDuration) * 1000));
        //velocity and direction
        velocity = currentBehaviour.movementSpeed;
        if (currentBehaviour.movementsAllowed.equalsIgnoreCase("None")) {
            direction = (random.nextBoolean()) ? DIR_LEFT : DIR_RIGHT; //just for image!
        } else if (currentBehaviour.movementsAllowed.equalsIgnoreCase("All")) {
            direction = random.nextFloat();
        } else if (currentBehaviour.movementsAllowed.equalsIgnoreCase("horizontal_only")) {
            direction = (random.nextBoolean()) ? DIR_LEFT : DIR_RIGHT;
        } else if (currentBehaviour.movementsAllowed.equalsIgnoreCase("vertical_only")) {
            direction = (random.nextBoolean()) ? DIR_UP : DIR_DOWN;
        } else if (currentBehaviour.movementsAllowed.equalsIgnoreCase("horizontal_vertical")) {
            switch (random.nextInt(4)) {
                case 0: {
                    direction = DIR_LEFT;
                    break;
                }
                case 1: {
                    direction = DIR_UP;
                    break;
                }
                case 2: {
                    direction = DIR_RIGHT;
                    break;
                }
                case 3: {
                    direction = DIR_DOWN;
                    break;
                }
            }
        } else if (currentBehaviour.movementsAllowed.equalsIgnoreCase("diagonal_only")) {
            switch (random.nextInt(4)) {
                case 0: {
                    direction = DIR_LEFT_UP;
                    break;
                }
                case 1: {
                    direction = DIR_LEFT_DOWN;
                    break;
                }
                case 2: {
                    direction = DIR_RIGHT_UP;
                    break;
                }
                case 3: {
                    direction = DIR_RIGHT_DOWN;
                    break;
                }
            }
        } else if (currentBehaviour.movementsAllowed.equalsIgnoreCase("diagonal_horizontal")
                || currentBehaviour.movementsAllowed.equalsIgnoreCase("diagonal_vertical")) {
            //just go with it. Trust me i'm a developer.
            direction = random.nextFloat();
        }
        //
        if (direction > DIR_UP && direction < DIR_DOWN) { //right
            imageRight = true;
        } else {
            imageRight = false;
        }
        //mMovie = null; //get new image at draw time!
        gifDecoder = null;
    }

    public void init() {
        behaviours.clear();
        AssetManager assets = context.getAssets();
        try {
            InputStream inputStream = assets.open(name + "/pony.ini");
            CSVReader reader = new CSVReader(new InputStreamReader(inputStream));
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                if (nextLine[0].equalsIgnoreCase("behavior")) {
                    behaviours.add(new Behaviour(nextLine));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //
        Log.e("BronyLiveWallpaper", "Loaded " + name);
        setCurrentBehaviour(getRandomBehaviour());

        positionY = (float) (50 + (Math.random() * 600));
        positionX = (float) (50 + (Math.random() * 400));
    }

    public void updateTick() {
        if (System.currentTimeMillis() > timeToChangeBehaviour) {
            setCurrentBehaviour(getRandomBehaviour());
        }
        move(1.0);
    }

    public void move(double delta) {
        if (screen == null){
            Log.e("BronyLiveWallpaper", "Screen == null, making default..");
            screen = new Rect(0,0,480,800);
        }
        //velocity -= Math.abs((float) (friction * delta));
//        if (velocity < 0) { //limit
//            velocity = 0;
//        }Ben
//            s.position += s.velocity * delta;
        positionY += (float) ((Math.sin((-direction) * (Math.PI / 180))) * velocity) * delta;
        positionX += (float) ((Math.cos((-direction) * (Math.PI / 180))) * velocity) * delta;

        /*
        if (outsideWrap && !limitAtEdge) {
            if (positionY > screen.height()) {
                positionY = 1;
            }
            if (positionY < 0) {
                positionY = screen.height() - 1;
            }
            if (positionX > screen.width()) {
                positionX = 1;
            }
            if (positionX < 0) {
                positionX = screen.width() - 1;
            }
        }*/

        /* NOT LIMIT, CHANGE DIRECTION!
        if (limitAtEdge && !outsideWrap) {
            if (positionY > screen.height() - height) {
                positionY = (screen.height() - height) - 1;
            }
            if (positionY < 0) {
                positionY = 1;
            }
            if (positionX > screen.width() - width) {
                positionX = (screen.width() - width) - 1;
            }
            if (positionX < 0) {
                positionX = 1;
            }
        }*/

        if (limitAtEdge && !outsideWrap) {
            gifDecoder = null; //change image
            if (positionY > screen.height() - height) {
                positionY = (screen.height() - height) - 1;
                direction += 180;
                direction = direction % 360;
                Log.e("BornyLiveWallpaper", "Changed Direction ! " + direction);
            }
            if (positionY < 0) {
                positionY = 1;
                direction += 180;
                direction = direction % 360;
                Log.e("BornyLiveWallpaper", "Changed Direction ! " + direction);
            }
            if (positionX > screen.width() - width) {
                positionX = (screen.width() - width) - 1;
                direction += 180;
                direction = direction % 360;
                Log.e("BornyLiveWallpaper", "Changed Direction ! " + direction);
            }
            if (positionX < 0) {
                positionX = 1;
                direction += 180;
                direction = direction % 360;
                Log.e("BornyLiveWallpaper", "Changed Direction ! " + direction);
            }
        }
    }

    public Behaviour findBehaviourByName(String name) {
        for (Behaviour behaviour : behaviours) {
            if (behaviour.name.equalsIgnoreCase(name)) {
                return behaviour;
            }
        }
        return null;
    }

    /**
     * TODO fix die stuff
     */
    public Behaviour getRandomBehaviour() {
        Behaviour b = behaviours.get((int) (Math.random() * behaviours.size()));
        Log.e("BronyLiveWallpaper","Changing behaviour of " + name);
        return b;
    }

    int currentFrame = 0;
    public void draw(Canvas canvas, Paint paint) {
        if (screen == null) {
            screen = new Rect(0, 0, canvas.getWidth(), canvas.getHeight());
        }
        updateTick();
        //drawTemp(canvas, paint);
        if (gifDecoder == null) {
            gifDecoder = new GifDecoder();
            try {
                if (imageRight) {
                    gifDecoder.read(context.getAssets().open(name + "/" + currentBehaviour.imageRight));
                } else {
                    gifDecoder.read(context.getAssets().open(name + "/" + currentBehaviour.imageLeft));
                }
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        Bitmap bitmap = gifDecoder.getFrame(currentFrame);
        currentFrame++;
        if (currentFrame > gifDecoder.getFrameCount()){
            currentFrame = 0;
        }
        canvas.drawBitmap(bitmap,positionX,positionY,paint);
    }


    Movie mMovie;
    long mMovieStart = 0;

    /**
     * Will be replaced by better method soon(tm)
     *
     * @param canvas
     * @param paint
     */
    public void drawTemp(Canvas canvas, Paint paint) {
        long now = android.os.SystemClock.uptimeMillis();
        if (mMovieStart == 0) {   // first time
            mMovieStart = now;
        }
        if (mMovie != null) {
            int dur = mMovie.duration();
            if (dur == 0) {
                dur = 600;
            }
            int relTime = (int) ((now - mMovieStart) % dur);
            mMovie.setTime(relTime);
            mMovie.draw(canvas, positionX, positionY);
        } else {
            InputStream is = context.getResources().openRawResource(R.raw.aj_gallop_right);
            mMovie = Movie.decodeStream(is);
        }
    }

    public class Behaviour {

        String name;
        String[] line;
        float probability; //0.1 - 1.0
        float maxDuration;
        float minDuration;
        int movementSpeed; //pixels per 100ms, so calculate for current refresh rate!
        String imageRight;
        String imageLeft;
        String movementsAllowed;

        public Behaviour(String[] line) {
            this.line = line;
            name = line[1];
            probability = Float.parseFloat(line[2]);
            maxDuration = Float.parseFloat(line[3]);
            minDuration = Float.parseFloat(line[4]);
            movementSpeed = Integer.parseInt(line[5]);
            imageRight = line[6];
            imageLeft = line[7];
            movementsAllowed = line[8];
        }
    }
}
