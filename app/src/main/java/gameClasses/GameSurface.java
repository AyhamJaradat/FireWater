package gameClasses;


import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.game.firewater.FireGameActivity;
import com.game.firewater.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

import utils.Constants;
import utils.MyUtils;

/**
 * Created by Ayham on 8/12/2017.
 */
public class GameSurface extends SurfaceView implements Constants, SurfaceHolder.Callback, CustomCountDownTimer.TimerTickListener {

    //    private MainCharacter characters;
    private final List<MainCharacter> mainCharacterList = new ArrayList<MainCharacter>();
    private final List<Explosion> explosionList = new ArrayList<Explosion>();
    private final List<FallingCharacter> fallingObjects = new ArrayList<FallingCharacter>();
    private GameThread gameThread;
    //    sounds ids
    private int soundIdBackground;
    private int soundIdFireRun;
    private int soundIdWaterRun;
    private int soundIdWaterHit;
    private int soundIdFireHit;
    private int streamIdFireRun;
    private int streamIdWaterRun;
    //    private int streamIdWaterHit;
//    private int streamIdFireHit;
    private boolean soundPoolLoaded;
    private SoundPool soundPool;
    private long lastFallingDrawingTime = -1;
    private int theSeed;
    private Random generator;
    private Context mainActivityContext = null;
    private boolean amIFire;
    private String timer;
    private boolean isGameplaying;
    private Bitmap bkBitmap, explosionBitmap, waterExplosionBitmap, waterDisappearBitmap, fireDisappearBitmap, startsBitmap, fireFallingCharBitmap, waterFallingCharBitmap, fireCharacterBtimap, waterCharacterBtimap;
    private Paint timerTextPaint, timertimePaint;
    private boolean isNextFallingFire = false;

    private int secondsScore = 0;
    private int secondLeft = 0;

    //    private CountDownTimer countDownTimer;
    private CustomCountDownTimer customCountDownTimer;

    private Dialog starterDialog;
    private Typeface custom_font;

    // Timer
    private long totalSeconds = 3 * 60 * 1000;
    private long intervalSeconds = 1 * 1000;
    private int lastfallingSecond = 3 * 60;

    public GameSurface(Context context, boolean amIFire) {
        super(context);

        this.amIFire = amIFire;
        this.mainActivityContext = context;
        // Make Game Surface focusable so it can handle events. .
        this.setFocusable(true);

        // Sét callback.
        this.getHolder().addCallback(this);

        this.initSoundPool();
        this.isGameplaying = true;
//        this.setBackgroundColor(getResources().getColor(R.color.transparent));
        initialization();
//        final long totalSeconds = 3 * 60 * 1000;
//        long intervalSeconds = 1 * 1000;

        customCountDownTimer = new CustomCountDownTimer(totalSeconds, intervalSeconds, this);

//        countDownTimer = new CountDownTimer(totalSeconds, intervalSeconds) {
//            int lastfallingSecond = 3 * 60;
//
//            public void onTick(long millisUntilFinished) {
//                if (isGameplaying) {
//                    int secondLeft = (int) (millisUntilFinished / 1000);
//                    // update second score
//                    if (isGameplaying)
//                        secondsScore = ((int) (totalSeconds / 1000)) - secondLeft;
//                    int minLeft = secondLeft / 60;
//                    String secondDisplay = "";
//                    if (secondLeft % 60 < 10) {
//                        secondDisplay = "0" + String.valueOf(secondLeft % 60);
//                    } else {
//                        secondDisplay = String.valueOf(secondLeft % 60);
//                    }
//
//                    timer = "0" + String.valueOf(minLeft) + ":" + secondDisplay;
//
//
//                    // hide starter animation
//                    if (secondLeft <= 3 * 59 && starterDialog.isShowing())
//                        hideStarterDialog();
//                    // game difficulty level based on time
//                    if (secondLeft <= 60) {
//                        // last minute ,, most difficult
//                        if (lastfallingSecond - secondLeft > 1) {
//                            isNextFallingFire = !isNextFallingFire;
//                            drawNewfallingCharacters(5, isNextFallingFire);
//                            lastfallingSecond = secondLeft;
//                        }
//                    } else if (secondLeft <= 2 * 60) {
//                        // second minute ,, middle difficulty
//                        if (lastfallingSecond - secondLeft > 1) {
//                            isNextFallingFire = !isNextFallingFire;
//                            drawNewfallingCharacters(4, isNextFallingFire);
//                            lastfallingSecond = secondLeft;
//                        }
//                    } else if (secondLeft <= 2.5 * 60) {
//                        // last 30 second of first minute,, normal
//                        if (lastfallingSecond - secondLeft > 2) {
//                            isNextFallingFire = !isNextFallingFire;
//                            drawNewfallingCharacters(3, isNextFallingFire);
//                            lastfallingSecond = secondLeft;
//                        }
//                    } else if (secondLeft <= (2.5 * 60) + 15) {
//                        // second 15 second ,, wimming up to end
//                        if (lastfallingSecond - secondLeft > 2) {
//                            isNextFallingFire = !isNextFallingFire;
//                            drawNewfallingCharacters(2, isNextFallingFire);
//                            lastfallingSecond = secondLeft;
//                        }
//
//                    } else if (secondLeft <= 3 * 58) {
//                        // first 15 second ,, easy ,, visible
//                        // start creating falling objects every 2 seconds
//                        if (lastfallingSecond - secondLeft > 2) {
//                            isNextFallingFire = !isNextFallingFire;
//                            drawNewfallingCharacters(1, isNextFallingFire);
//                            lastfallingSecond = secondLeft;
//                        }
//                    }
//                }
//            }
//
//            public void onFinish() {
//
//                endOfGame(true);
//            }
//        };

        // show starter dialog
        showStarterDialog();

//        countDownTimer.start();

//        new Timer().schedule(new TimerTask() {
//            @Override
//            public void run() {
//                // this code will be executed after  seconds
//                countDownTimer.start();
//            }
//        }, startAfterSeconds);

//        ((FireGameActivity) this.mainActivityContext).runOnUiThread(new Runnable() {
//
//            @Override
//            public void run() {
//                final Handler handler = new Handler();
//                handler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        //add your code here
//                        countDownTimer.start();
//                    }
//                }, startAfterSeconds);
//
//            }
//        });

//        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                //Do something here
//                countDownTimer.start();
//            }
//        }, startAfterSeconds);

//        new Handler().postDelayed(new Runnable() {
//                                      @Override
//                                      public void run() {
//                                          countDownTimer.start();
//                                      }
//                                  },
//                startAfterSeconds);


    }

    public GameSurface(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Log.d("Ayham1", "here1");
        this.setFocusable(true);
        getHolder().addCallback(this);
    }

    public GameSurface(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        Log.d("Ayham1", "here2");

        this.setFocusable(true);
        getHolder().addCallback(this);
    }

    @Override
    public void onTick(long millisUntilFinished) {
        if (isGameplaying) {
            secondLeft = (int) (millisUntilFinished / 1000);
            // update second score
            if (isGameplaying)
                secondsScore = ((int) (totalSeconds / 1000)) - secondLeft;
            int minLeft = secondLeft / 60;
            String secondDisplay = "";
            if (secondLeft % 60 < 10) {
                secondDisplay = "0" + String.valueOf(secondLeft % 60);
            } else {
                secondDisplay = String.valueOf(secondLeft % 60);
            }

            timer = "0" + String.valueOf(minLeft) + ":" + secondDisplay;


            // hide starter animation
            if (secondLeft <= 3 * 59 && starterDialog.isShowing())
                hideStarterDialog();
            // game difficulty level based on time
            if (secondLeft <= 60) {
                // last minute ,, most difficult
                if (lastfallingSecond - secondLeft > 1) {
                    isNextFallingFire = !isNextFallingFire;
                    drawNewfallingCharacters(5, isNextFallingFire);
                    lastfallingSecond = secondLeft;
                }
            } else if (secondLeft <= 2 * 60) {
                // second minute ,, middle difficulty
                if (lastfallingSecond - secondLeft > 1) {
                    isNextFallingFire = !isNextFallingFire;
                    drawNewfallingCharacters(4, isNextFallingFire);
                    lastfallingSecond = secondLeft;
                }
            } else if (secondLeft <= 2.5 * 60) {
                // last 30 second of first minute,, normal
                if (lastfallingSecond - secondLeft > 2) {
                    isNextFallingFire = !isNextFallingFire;
                    drawNewfallingCharacters(3, isNextFallingFire);
                    lastfallingSecond = secondLeft;
                }
            } else if (secondLeft <= (2.5 * 60) + 15) {
                // second 15 second ,, wimming up to end
                if (lastfallingSecond - secondLeft > 2) {
                    isNextFallingFire = !isNextFallingFire;
                    drawNewfallingCharacters(2, isNextFallingFire);
                    lastfallingSecond = secondLeft;
                }

            } else if (secondLeft <= 3 * 58) {
                // first 15 second ,, easy ,, visible
                // start creating falling objects every 2 seconds
                if (lastfallingSecond - secondLeft > 2) {
                    isNextFallingFire = !isNextFallingFire;
                    drawNewfallingCharacters(1, isNextFallingFire);
                    lastfallingSecond = secondLeft;
                }
            }
        }
    }

    @Override
    public void onFinish() {
        endOfGame(true);
    }

    @Override
    public void onCancel() {

    }

    private void showStarterDialog() {
        starterDialog = new Dialog(this.mainActivityContext);
        starterDialog.setCanceledOnTouchOutside(false);
        starterDialog.setCancelable(false);
        starterDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        starterDialog.setContentView(R.layout.message_dialog);
        starterDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        starterDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation; //style id
        // set the custom dialog components - text, image and button
        TextView text = (TextView) starterDialog.findViewById(R.id.textViewTitle);
        text.setTypeface(custom_font);

        TextView messageText = (TextView) starterDialog.findViewById(R.id.finalMessage);
        AnimationDrawable imageAnimation;
        ImageView characterImage = (ImageView) starterDialog.findViewById(R.id.imageViewCharacter);
        int color = 0;

        if (this.amIFire) {
            text.setText("Yor are Fire");
            color = getResources().getColor(R.color.FireBackgroundColor);
//            text.setTextColor(color);
            messageText.setText("Survive, and Help Water !");
//            messageText.setTextColor(color);
            characterImage.setBackgroundResource(R.drawable.moving_fire_animation);
            imageAnimation = (AnimationDrawable) characterImage.getBackground();
            imageAnimation.start();
        } else {
            text.setText("Yor are Water");
            color = getResources().getColor(R.color.ButtonTextColor);
//            text.setTextColor(color);
            messageText.setText("Survive, and Help Fire !");
//            messageText.setTextColor(color);
            characterImage.setBackgroundResource(R.drawable.moving_water_animation);
            imageAnimation = (AnimationDrawable) characterImage.getBackground();
            imageAnimation.start();
        }


        starterDialog.show();
    }

    private void hideStarterDialog() {
        starterDialog.dismiss();
    }

    private void initialization() {

        custom_font = Typeface.createFromAsset(mainActivityContext.getAssets(), "fonts/trajan_pro_bold.ttf");

        // prepare bitmaps
        explosionBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.explosion);
        waterExplosionBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.water_explosion);
        waterDisappearBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.water_disappear);
        fireDisappearBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.fire_disappear);
        startsBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.coins1);
        fireFallingCharBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.falling_fire);
        waterFallingCharBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.falling_water);
        fireCharacterBtimap = BitmapFactory.decodeResource(this.getResources(), R.drawable.fire_character);
        waterCharacterBtimap = BitmapFactory.decodeResource(this.getResources(), R.drawable.water_animation_small_size);


        // style for word Time left
        timerTextPaint = new Paint();
        timerTextPaint.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/Asimov.otf"));
        timerTextPaint.setARGB(255, 255, 217, 159);
        timerTextPaint.setTextSize(30);
        timerTextPaint.setAntiAlias(true);
        timerTextPaint.setFakeBoldText(true);
        // style of actual time
        timertimePaint = new Paint();
        timertimePaint.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/AsimovOuIt.otf"));
        timertimePaint.setARGB(255, 230, 230, 230);
        timertimePaint.setTextSize(50);
        timertimePaint.setAntiAlias(true);
        timertimePaint.setShadowLayer(1.5f, 0, 0.5f, 0xFFFFFFFF);
        timertimePaint.setStrokeWidth(1.5f);
        timertimePaint.setStyle(Paint.Style.FILL);
    }

    private void initSoundPool() {
        // With Android API >= 21.
        if (Build.VERSION.SDK_INT >= 21) {

            AudioAttributes audioAttrib = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            SoundPool.Builder builder = new SoundPool.Builder();
            builder.setAudioAttributes(audioAttrib).setMaxStreams(MAX_STREAMS);

            this.soundPool = builder.build();
        }
        // With Android API < 21
        else {
            // SoundPool(int maxStreams, int streamType, int srcQuality)
            this.soundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
        }

        // When SoundPool load complete.
        this.soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                soundPoolLoaded = true;

//                if(status ==0){
//                    switch (sampleId){
//
//                    }
//                }

                // Playing char running sounds.
                startCharRunningSounds();
//                playSoundBackground();
            }
        });

        // Load the sound background.mp3 into SoundPool
//        this.soundIdBackground = this.soundPool.load(this.getContext(), R.raw.background, 1);

        // Load the sound explosion.wav into SoundPool
//        this.soundIdExplosion = this.soundPool.load(this.getContext(), R.raw.explosion, 1);

        this.soundIdFireRun = this.soundPool.load(this.getContext(), R.raw.fire_run, 1);
        this.soundIdWaterRun = this.soundPool.load(this.getContext(), R.raw.humping, 1);
        this.soundIdFireHit = this.soundPool.load(this.getContext(), R.raw.fire_hit, 1);
        this.soundIdWaterHit = this.soundPool.load(this.getContext(), R.raw.water_hit, 1);


    }

    public void playSoundExplosion(String type) {
        if (this.soundPoolLoaded) {
            float leftVolumn = 0.2f;
            float rightVolumn = 0.2f;
            // Play sound explosion.wav
//            int streamId = this.soundPool.play(this.soundIdExplosion, leftVolumn, rightVolumn, 1, 0, 1f);
            if (type.equalsIgnoreCase(FIRE_TYPE)) {
                int streamfireId = this.soundPool.play(this.soundIdFireHit, leftVolumn, rightVolumn, 1, 0, 1f);
            } else if (type.equalsIgnoreCase(WATER_TYPE)) {
                int streamwaterId = this.soundPool.play(this.soundIdWaterHit, leftVolumn, rightVolumn, 1, 0, 1f);
            }
        }
    }

    /**
     * update timer time to synchronize with other player
     * @param time
     */
    public void updateMyTimerToSynchronize(long time){
        customCountDownTimer.updateTime(time);
    }

    public void playSoundCharRunning(boolean isFireChar) {


        if (this.soundPoolLoaded) {
            // Play sound background.mp3
//            int streamId = this.soundPool.play(this.soundIdBackground, leftVolumn, rightVolumn, 1, -1, 1f);
            if (isFireChar) {
                this.soundPool.resume(this.streamIdFireRun);
            } else {
                this.soundPool.resume(this.streamIdWaterRun);
            }
        }
    }

    public void stopSoundCharRunning(boolean isFireChar) {
        if (this.soundPoolLoaded) {
            if (isFireChar) {
                this.soundPool.pause(this.streamIdFireRun);
//                this.soundPool.stop(this.soundIdFireRun);
            } else {
                this.soundPool.pause(this.streamIdWaterRun);
//                this.soundPool.stop(this.soundIdWaterRun);
            }
        }
    }

    public void startCharRunningSounds() {
        if (this.soundPoolLoaded) {
            float leftVolumn = 0.05f;
            float rightVolumn = 0.05f;
            // run sounds
            this.streamIdWaterRun = this.soundPool.play(this.soundIdWaterRun, leftVolumn, rightVolumn, 1, -1, 1f);
            this.streamIdFireRun = this.soundPool.play(this.soundIdFireRun, leftVolumn, rightVolumn, 1, -1, 1f);

            // pause sounds
            this.soundPool.pause(this.streamIdFireRun);
            this.soundPool.pause(this.streamIdWaterRun);

        }
    }

    public void playSoundBackground() {
        if (this.soundPoolLoaded) {
            float leftVolumn = 0.8f;
            float rightVolumn = 0.8f;
            // Play sound background.mp3
            int streamId = this.soundPool.play(this.soundIdBackground, leftVolumn, rightVolumn, 1, -1, 1f);
        }
    }

    public void stopSounds() {
        if (this.soundPoolLoaded) {
            // stop sound background.mp3
//            this.soundPool.stop(this.soundIdBackground);
            // Stop sound explosion.wav
//            this.soundPool.stop(this.soundIdExplosion);
            this.soundPool.stop(this.soundIdFireHit);
            this.soundPool.stop(this.soundIdFireRun);
            this.soundPool.stop(this.soundIdWaterHit);
            this.soundPool.stop(this.soundIdWaterRun);

            // release sound pool
            this.soundPool.release();
            this.soundPool = null;
            this.soundPoolLoaded = false;
        }
    }

    public void update() {


        // update main characters
        for (MainCharacter chibi : mainCharacterList) {
            chibi.update();
        }
        // update explosion objects
        for (Explosion explosion : this.explosionList) {
            explosion.update();
        }
        // update falling object positions
        for (FallingCharacter fallingCharacter : this.fallingObjects) {
            fallingCharacter.update();
        }
        // remove falling objects when reach ground
        Iterator<FallingCharacter> fallingIterator = this.fallingObjects.iterator();
        while (fallingIterator.hasNext()) {
            FallingCharacter fallingCharacter = fallingIterator.next();
            if (fallingCharacter.isReachGround()) {
                // add ground hit animation
                Explosion explosion;
                if (fallingCharacter.getType().equalsIgnoreCase(WATER_TYPE) && !amIFire) {
                    explosion = new Explosion(this, waterDisappearBitmap, fallingCharacter.getX() - 20 - (fallingCharacter.getWidth() / 2), fallingCharacter.getY() + 8 - fallingCharacter.getHeight(), WATER_DISAPPEAR_TYPE, 3, 3);
                    this.explosionList.add(explosion);
                } else if (fallingCharacter.getType().equalsIgnoreCase(FIRE_TYPE) && amIFire) {
                    explosion = new Explosion(this, fireDisappearBitmap, fallingCharacter.getX() - 10 - (fallingCharacter.getWidth() / 2), fallingCharacter.getY() + 35 - fallingCharacter.getHeight(), FIRE_DISAPPEAR_TYPE, 4, 5);
                    this.explosionList.add(explosion);
                }

                // falling object reached ground, Remove the current element from the iterator & list.
                fallingIterator.remove();

                continue;
            }
        }
        // remove explosion object if they finish
        Iterator<Explosion> iterator = this.explosionList.iterator();
        while (iterator.hasNext()) {
            Explosion explosion = iterator.next();
            if (explosion.isFinish()) {
                // If explosion finish, Remove the current element from the iterator & list.
                iterator.remove();
                continue;
            }
        }
    }


    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

//        if (this.isGameplaying) {
        // background

        canvas.drawBitmap(bkBitmap, 0, 0, null);
//            canvas.drawColor(Color.TRANSPARENT/*Color.WHITE*/);
        int canvasWidth = this.getWidth();

        drawTimeLeft(canvas, canvasWidth);


//        Color.TRANSPARENT


        for (MainCharacter chibi : mainCharacterList) {
            chibi.draw(canvas);
        }


        for (FallingCharacter fallingCharacter : this.fallingObjects) {
            fallingCharacter.draw(canvas);
        }

        // check for collisions and death
        if (this.isGameplaying) {
            checkForCollision();
        }
        for (Explosion explosion : this.explosionList) {
            explosion.draw(canvas);
        }

        if (this.isGameplaying) {
            // check end of game
            if (mainCharacterList.size() < 2) {
                // game Ended
                endOfGame(false);
            }
        }
//        }
    }

    private void drawNewfallingCharacters(int difficultyLevel, boolean isNextFallingFire) {

        if (this.isGameplaying) {
            if (difficultyLevel == 4 || difficultyLevel == 5 || difficultyLevel == 3) {
                float xRandomPerc = generator.nextInt(101);
                int XRandomPixcel = (int) ((xRandomPerc / 100) * this.getWidth());
                FallingCharacter fallingCharacter = new FallingCharacter(this, fireFallingCharBitmap, 1, 3, XRandomPixcel, 50, FIRE_TYPE, this.amIFire, difficultyLevel);
                this.fallingObjects.add(fallingCharacter);
                float x1RandomPerc = generator.nextInt(101); //20%
                int X1RandomPixcel = (int) ((x1RandomPerc / 100) * this.getWidth());
                FallingCharacter fallingCharacter1 = new FallingCharacter(this, waterFallingCharBitmap, 1, 4, X1RandomPixcel, 50, WATER_TYPE, this.amIFire, difficultyLevel);
                this.fallingObjects.add(fallingCharacter1);
            } else if (isNextFallingFire) {
                float xRandomPerc = generator.nextInt(101);
                int XRandomPixcel = (int) ((xRandomPerc / 100) * this.getWidth());
                FallingCharacter fallingCharacter = new FallingCharacter(this, fireFallingCharBitmap, 1, 3, XRandomPixcel, 50, FIRE_TYPE, this.amIFire, difficultyLevel);
                this.fallingObjects.add(fallingCharacter);
            } else {

                float x1RandomPerc = generator.nextInt(101); //20%
                int X1RandomPixcel = (int) ((x1RandomPerc / 100) * this.getWidth());
                FallingCharacter fallingCharacter1 = new FallingCharacter(this, waterFallingCharBitmap, 1, 4, X1RandomPixcel, 50, WATER_TYPE, this.amIFire, difficultyLevel);
                this.fallingObjects.add(fallingCharacter1);
            }
            lastFallingDrawingTime = System.nanoTime();
        }

    }

    /**
     * function to be called when game ends , by loosing or by timer
     *
     * @param isWinner
     */
    private void endOfGame(boolean isWinner) {
        if (this.isGameplaying) {
            // game finish
            this.isGameplaying = false;


            if (isWinner) {
                // win case

                ((FireGameActivity) this.mainActivityContext).showGameFinishScreen(true, secondsScore);

            } else {
                // lose case
                ((FireGameActivity) this.mainActivityContext).showGameFinishScreen(false, secondsScore);

            }


        }
    }

    /**
     * function to draw the left time of the game
     *
     * @param canvas
     * @param canvasWidth
     */
    private void drawTimeLeft(Canvas canvas, int canvasWidth) {
        // rectangle to draw time inside
        RectF rectF = new RectF();
        Paint paint = new Paint();
        paint.setARGB(128, 0, 0, 0);
        int rectWidth = 150;
        int rectHeight = 100;
        int margin = 15;
        int timeMargin = 9;
        // set rectangle dimensions and position
        rectF.set(canvasWidth - rectWidth - margin, margin, canvasWidth - margin, rectHeight + margin);
        canvas.drawRect(rectF, paint);
        canvas.drawText("Time left", canvasWidth - rectWidth + 3, 50, timerTextPaint);
        canvas.drawText(timer, canvasWidth - rectWidth - margin + timeMargin, 100, timertimePaint);
    }

    public void startGame(String myId, String PartecepentId, final boolean amIFire, int seeds) {
        this.amIFire = amIFire;

        if (this.mainCharacterList.size() == 0) {

            MainCharacter fireCharacter;
            MainCharacter waterCharacter;
            if (amIFire) {
                fireCharacter = new MainCharacter(this, fireCharacterBtimap, 100, this.getHeight(), FIRE_TYPE, myId);
                waterCharacter = new MainCharacter(this, waterCharacterBtimap, 300, this.getHeight(), WATER_TYPE, PartecepentId);
            } else {
                fireCharacter = new MainCharacter(this, fireCharacterBtimap, 100, this.getHeight(), FIRE_TYPE, PartecepentId);
                waterCharacter = new MainCharacter(this, waterCharacterBtimap, 300, this.getHeight(), WATER_TYPE, myId);
            }
            this.mainCharacterList.add(fireCharacter);
            this.mainCharacterList.add(waterCharacter);
            theSeed = seeds;
            if (seeds != 0) {
                generator = new Random(seeds);
            } else {
                generator = new Random();
            }
            Log.d("Generator", "seed sets");

            // start timer
//            countDownTimer.start();
            final SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss:SS");
            dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
            new Handler().postDelayed(new Runnable() {
                                          @Override
                                          public void run() {
                                              Log.d("timeme Starting", dateFormatGmt.format(new Date()
                                              ) + "");
                                              customCountDownTimer.start();
                                              if (amIFire) {
                                                  ((FireGameActivity) mainActivityContext).sendTimeSynchroMessages();
                                              }
//                                              countDownTimer.start();
                                          }
                                      },
                    getTimeToStartAfter());
        }
    }

    private long getTimeToStartAfter() {

        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss:SS");
        dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date nowDate = new Date();
        String gmtTime = dateFormatGmt.format(nowDate);
        Date GMTDate = null;
        try {
            GMTDate = dateFormatGmt.parse(gmtTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(GMTDate);
        cal.set(Calendar.MILLISECOND, 0);
        int seconds = cal.get(Calendar.SECOND);
//        if (seconds % 5 == 0) {
//            seconds++;
//        }
        while (seconds % 5 != 0) {
            seconds++;
        }
        cal.set(Calendar.SECOND, seconds);
        long millSecDiff = cal.getTime().getTime()/*getTimeInMillis()*/ - GMTDate.getTime();

        Log.d("timeme millSecDef", millSecDiff + "");
        Log.d("timeme timeToStart", dateFormatGmt.format(cal.getTime().getTime() /*cal.getTimeInMillis()*/) + "");
        return millSecDiff < 0 ? 0 : millSecDiff;

    }

    /**
     * called when a message to show star for a character is recieved from other player
     *
     * @param characterType
     */
    public void showStarForCharacter(String characterType) {
        Iterator<MainCharacter> iterator = this.mainCharacterList.iterator();
        while (iterator.hasNext()) {
            MainCharacter chibi = iterator.next();
            if (chibi.getType().equalsIgnoreCase(characterType)) {
                // collision detected
                // show star
                // add score
                // Create star object.
                Explosion explosion;
                if (chibi.getType().equalsIgnoreCase(WATER_TYPE)) {
                    explosion = new Explosion(this, startsBitmap, chibi.getX(), chibi.getY() - 5, WATER_STAR_TYPE, 2, 6);
                } else {
                    explosion = new Explosion(this, startsBitmap, chibi.getX(), chibi.getY() - 5, FIRE_STAR_TYPE, 2, 6);
                }
                this.explosionList.add(explosion);
            }
        }
    }

    /**
     * called when a message to kill a character is recieved from other player
     *
     * @param characterType
     */
    public void killCharacter(String characterType) {
        Iterator<MainCharacter> iterator = this.mainCharacterList.iterator();
        while (iterator.hasNext()) {
            MainCharacter chibi = iterator.next();
            if (chibi.getType().equalsIgnoreCase(characterType)) {
                // collision detected
                // decrement lives
                chibi.decrementLives();
                // check if he need to be killed
                if (chibi.getLives() <= 0) {
                    // kill this damn character
                    iterator.remove();
                }
                // Create Explosion object.
                Explosion explosion;
                if (characterType.equalsIgnoreCase(WATER_TYPE)) {
                    explosion = new Explosion(this, explosionBitmap, chibi.getX(), chibi.getY(), characterType, 5, 5);
                } else {
                    explosion = new Explosion(this, waterExplosionBitmap, chibi.getX() - 200, chibi.getY() - 200, chibi.getType(), 4, 5);
                }
                this.explosionList.add(explosion);
//                }
            }
        }
    }

    private void checkForCollision() {
        Iterator<MainCharacter> iterator = this.mainCharacterList.iterator();
        while (iterator.hasNext()) {
            MainCharacter chibi = iterator.next();
            FallingCharacter fallingCharacter;
            // Water character in Water player OR fire character on Fire player
            for (int i = 0; i < this.fallingObjects.size(); i++) {
                fallingCharacter = this.fallingObjects.get(i);
                int x = (int) fallingCharacter.getX();
                int y = (int) (fallingCharacter.getY() + fallingCharacter.getHeight());
                if (chibi.getX() < x && x < chibi.getX() + chibi.getWidth()
                        && chibi.getY() < y && y < chibi.getY() + chibi.getHeight()) {
                    // collision detected
                    if (chibi.getType().equalsIgnoreCase(FIRE_TYPE) && amIFire ||
                            chibi.getType().equalsIgnoreCase(WATER_TYPE) && !amIFire) {
                        // I am fire and checking for fire character collision to eat something
                        // or I am water and checking for water character if eat something
                        if (chibi.getType().equalsIgnoreCase(fallingCharacter.getType())) {
                            // eat some .. add score

                            // send msg to other screen
                            ((FireGameActivity) this.mainActivityContext).showStarForCharacterOnParticipantScreen(chibi.getType());
                            ((FireGameActivity) this.mainActivityContext).incrementMyScore();
                            Explosion explosion;
                            if (chibi.getType().equalsIgnoreCase(WATER_TYPE)) {
                                explosion = new Explosion(this, startsBitmap, fallingCharacter.getX() - 5, fallingCharacter.getY() + 5, WATER_STAR_TYPE, 2, 6);
                            } else {
                                explosion = new Explosion(this, startsBitmap, fallingCharacter.getX(), fallingCharacter.getY() + 5, FIRE_STAR_TYPE, 2, 6);
                            }
                            this.explosionList.add(explosion);
                            // remove falling object
                            this.fallingObjects.remove(i);
                            i--;
                        } else {
                            // DO nth
                        }


                    } else {
                        // I am fire and checking if water character hit fire object
                        // or I am water and checking if fire character hit water object
                        if (chibi.getType().equalsIgnoreCase(fallingCharacter.getType())) {
                            // do nth
                        } else {
                            // this character should die ,, kill him here and send msg to other player
                            // collision detected
                            chibi.decrementLives();
                            // remove falling object
                            this.fallingObjects.remove(i);
                            i--;
                            // send msg to other screen
                            ((FireGameActivity) this.mainActivityContext).distroyCharacterOnParticipantScreen(chibi.getType()/*, chibi.getLives()*/);

                            if (chibi.getLives() <= 0) {
                                // remove character
                                iterator.remove();
                            }
                            // draw explosion effect
                            // Create Explosion object.
                            Explosion explosion;
                            if (chibi.getType().equalsIgnoreCase(WATER_TYPE)) {
                                explosion = new Explosion(this, explosionBitmap, chibi.getX(), chibi.getY(), chibi.getType(), 5, 5);
                            } else {
                                explosion = new Explosion(this, waterExplosionBitmap, chibi.getX() - 200, chibi.getY() - 200, chibi.getType(), 4, 5);
                            }
                            this.explosionList.add(explosion);
                        }

                    }
                }


            }

        }
    }

    // Implements method of SurfaceHolder.Callback
    @Override
    public void surfaceCreated(SurfaceHolder holder) {


        Log.d("Ayham1", "created");
        // create background bitmap
//        Bitmap tempBmp;
        bkBitmap = MyUtils.decodeSampledBitmapFromResource(getResources(), R.drawable.bk_good, this.getWidth(), this.getHeight());
//        if (this.amIFire)
//            bkBitmap = MyUtils.decodeSampledBitmapFromResource(getResources(), R.drawable.fire_background_xhdpi, this.getWidth(), this.getHeight());
//        else
//            bkBitmap = MyUtils.decodeSampledBitmapFromResource(getResources(), R.drawable.water_background_xhdpi, this.getWidth(), this.getHeight());


        // update chibi posotions and canvuas dimensions
        for (MainCharacter chibi : mainCharacterList) {
//            chibi.setY(this.getHeight());
            chibi.updateCanvuasDimensions(this.getHeight(), this.getWidth());
        }
        lastFallingDrawingTime = System.nanoTime();
        this.gameThread = new GameThread(this, holder);
        this.gameThread.setRunning(true);
        this.gameThread.start();

    }

    // Implements method of SurfaceHolder.Callback
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d("Ayham1", "changed");
    }

    // Implements method of SurfaceHolder.Callback
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("Ayham1", "destroyed");
        boolean retry = true;
        stopSounds();
        this.isGameplaying = false;
        while (retry) {
            try {
                this.gameThread.setRunning(false);

                // Parent thread must wait until the end of GameThread.
                this.gameThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            retry = false;
        }
        //recycle bitmaps
        bkBitmap.recycle();
        explosionBitmap.recycle();
        waterExplosionBitmap.recycle();
        waterDisappearBitmap.recycle();
        fireDisappearBitmap.recycle();
        startsBitmap.recycle();
        fireFallingCharBitmap.recycle();
        waterFallingCharBitmap.recycle();
        fireCharacterBtimap.recycle();
        waterCharacterBtimap.recycle();
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        if (event.getAction() == MotionEvent.ACTION_DOWN) {
//
//            int x = (int) event.getX();
//            int y = (int) event.getY();
//
//            Iterator<MainCharacter> iterator = this.mainCharacterList.iterator();
//
//            while (iterator.hasNext()) {
//                MainCharacter chibi = iterator.next();
//                if (chibi.getX() < x && x < chibi.getX() + chibi.getWidth()
//                        && chibi.getY() < y && y < chibi.getY() + chibi.getHeight()) {
//                    // Remove the current element from the iterator and the list.
//                    iterator.remove();
//
//                    // Create Explosion object.
//                    Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.explosion);
//                    Explosion explosion = new Explosion(this, bitmap, chibi.getX(), chibi.getY());
//
//                    this.explosionList.add(explosion);
//                }
//            }
//
//
//            for (MainCharacter chibi : mainCharacterList) {
//                int movingVectorX = x - chibi.getX();
//                int movingVectorY = y - chibi.getY();
//                chibi.setMovingVector(movingVectorX, movingVectorY);
//            }
//            return true;
//        }
//        return false;
//
//
////        if (event.getAction() == MotionEvent.ACTION_DOWN) {
////            int x = (int) event.getX();
////            int y = (int) event.getY();
////
////            int movingVectorX = x - this.chibi1.getX();
////            int movingVectorY = y - this.chibi1.getY();
////
////            this.chibi1.setMovingVector(movingVectorX, movingVectorY);
////            return true;
////        }
////        return false;
//    }

    public void movePlayerTo(String id, float direction) {

        for (MainCharacter chibi : mainCharacterList) {

            float movingVectorX = direction;
            int movingVectorY = 0;
            if (id == null) {
                // single player
                // move all now
                chibi.setMovingVector(movingVectorX, movingVectorY);
            } else if (id.equalsIgnoreCase(chibi.getMyId())) {
                // multiplayer
                // only move my player
                chibi.setMovingVector(movingVectorX, movingVectorY);
            }
        }
    }

    /**
     * function to get the x position of a character in order to send it to other participiant
     *
     * @param mMyId
     * @return XPosition
     */
    public float getMyPlayerXPosition(String mMyId) {
        float XPosition = -1;
        for (MainCharacter chibi : mainCharacterList) {
            if (mMyId == null) {
                // single player
//               do nth
            } else if (mMyId.equalsIgnoreCase(chibi.getMyId())) {
                // multiplayer
                // get my player xPosition
                return chibi.getXPosition();
            }
        }
        return XPosition;
    }

    /**
     * function to update the x position of player once a stop moving message is recieved
     *
     * @param id
     * @param xPosition
     */
    public void updatePlayerXPosition(String id, float xPosition) {

        for (MainCharacter chibi : mainCharacterList) {
            if (id == null) {
                // single player
//               do nth
            } else if (id.equalsIgnoreCase(chibi.getMyId())) {
                // multiplayer
                // update player xPosition
                chibi.setXPosition(xPosition);
            }
        }
    }


    // freez the game because one player has leaved or is disconnected
    public void freezGame() {
        this.isGameplaying = false;
    }

    public int getTimeScore() {
        return secondsScore;
    }


    public int getLeftTime() {
        return secondLeft;
    }
}