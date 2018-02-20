package gameClasses;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by Ayham on 8/12/2017.
 */

public class MainCharacter extends GameCharacter {

    // index of rwo based on movement direction
//    private static final int ROW_TOP_TO_BOTTOM = 2;
//    private static final int ROW_RIGHT_TO_LEFT = 1;
//    private static final int ROW_LEFT_TO_RIGHT = 3;
//    private static final int ROW_BOTTOM_TO_TOP = 0;
    private static final int ROW_TOP_TO_BOTTOM = 0;
    private static final int ROW_RIGHT_TO_LEFT = 2;
    private static final int ROW_LEFT_TO_RIGHT = 1;

    // Row index of Image are being used.
    private int rowUsing = ROW_TOP_TO_BOTTOM;
    private int colUsing;
    // bitmap array for all direction
    private Bitmap[] leftToRights;
    private Bitmap[] rightToLefts;
    private Bitmap[] topToBottoms;
    private Bitmap[] bottomToTops;
    // Velocity of game character (percantage of pixel/millisecond)
    private float VELOCITY = 0.0008f;
    private float movingVectorX = 0f;
    private float movingVectorY = 0f;
    private long lastDrawMsTime = -1;
    private int counter = 0;

    private String type;
    private String myId;

    private GameSurface gameSurface;
    private int canvasHeight;
    private int canvasWidth;

    private int lives;
    private Paint dayingPaint;


    public MainCharacter(GameSurface gameSurface, Bitmap image, int x, int y, String type, String id) {
        super(image, 3/*number of rows */, 9/*number of columns*/, x, y);

        this.gameSurface = gameSurface;
        this.type = type;
        this.myId = id;
        this.canvasHeight = this.gameSurface.getHeight();
        this.canvasWidth = this.gameSurface.getWidth();


        this.lives = 2;
        dayingPaint = new Paint();
        dayingPaint.setAlpha(123);

        // for every direction , create the movement images and add them to the array of the movement
        this.topToBottoms = new Bitmap[colCount]; // 3
        this.rightToLefts = new Bitmap[colCount]; // 3
        this.leftToRights = new Bitmap[colCount]; // 3
//        this.bottomToTops = new Bitmap[colCount]; // 3
        for (int col = 0; col < this.colCount; col++) {
            this.topToBottoms[col] = this.createSubImageAt(ROW_TOP_TO_BOTTOM, col);
            this.rightToLefts[col] = this.createSubImageAt(ROW_RIGHT_TO_LEFT, col);
            this.leftToRights[col] = this.createSubImageAt(ROW_LEFT_TO_RIGHT, col);
//            this.bottomToTops[col] = this.createSubImageAt(ROW_BOTTOM_TO_TOP, col);
        }
    }

    /**
     * function to get the array of bitmaps for the current direction
     *
     * @return
     */
    public Bitmap[] getMoveBitmaps() {
        switch (rowUsing) {
//            case ROW_BOTTOM_TO_TOP:
//                return this.bottomToTops;
            case ROW_LEFT_TO_RIGHT:
                return this.leftToRights;
            case ROW_RIGHT_TO_LEFT:
                return this.rightToLefts;
            case ROW_TOP_TO_BOTTOM:
                return this.topToBottoms;
            default:
                return null;
        }
    }

    /**
     * function to get the current image from the array of current moving bitmaps
     *
     * @return
     */
    public Bitmap getCurrentMoveBitmap() {
        Bitmap[] bitmaps = this.getMoveBitmaps();
        return bitmaps[this.colUsing];
    }

    public void update() {
        // update moving images

        this.counter++;

        if (this.rowUsing == ROW_TOP_TO_BOTTOM) {
            // not moving ... slow animation
            if (this.counter % 5 == 0) {
                this.colUsing++;
                if (colUsing >= this.colCount) {
                    this.colUsing = 0;
                }
            }
        } else {
            // moving right and left ... fast animation
//            if (this.counter % 5 == 0) {
            this.colUsing++;
            if (colUsing >= this.colCount) {
                this.colUsing = 0;
            }
//            }
        }

        // Current time in millisecon
        long now = System.currentTimeMillis();
        // Never once did draw.
        if (lastDrawMsTime == -1) {
            lastDrawMsTime = now;
        }
        //  get delta time since last frame
        int deltaTime = 30;/*(int) ((now - lastDrawMsTime));*/
//        if (((now - lastDrawMsTime) / 1000000) > 100) {
// Distance moves

        float distance = VELOCITY * deltaTime;
        double movingVectorLength = Math.sqrt(movingVectorX * movingVectorX + movingVectorY * movingVectorY);
// Calculate the new position of the game character.

        // in every update there should be fixed ratio of pixels to move
        this.x = x + (int) (distance * movingVectorX / movingVectorLength);
        this.y = y + (int) (distance * movingVectorY / movingVectorLength);

        // Make sure character does not go out of the screen
        if (this.x < 0) {
            this.x = 0;
        } else if (this.x > this.canvasWidth - width) {
            this.x = this.canvasWidth - width;
        }

        if (this.y < 0) {
            this.y = 0;
        } else if (this.y > this.canvasHeight - height) {
            this.y = this.canvasHeight - height;
        }

        // rowUsing
        if (movingVectorX == 0) {
            this.rowUsing = ROW_TOP_TO_BOTTOM;
        } else if (movingVectorX > 0) {
            this.rowUsing = ROW_LEFT_TO_RIGHT;
        } else {
            this.rowUsing = ROW_RIGHT_TO_LEFT;
        }

        // keep time to find delta between frams
        lastDrawMsTime = System.currentTimeMillis();
    }


    public void draw(Canvas canvas) {
        // set character trancparency based on lives count
        Bitmap bitmap = this.getCurrentMoveBitmap();
        if (this.lives == 2) {
            canvas.drawBitmap(bitmap, x, y, null);
        } else {
            canvas.drawBitmap(bitmap, x, y, dayingPaint);
        }
    }

    /**
     * function to change the moving victor of the character in order to move or stop
     *
     * @param movingVectorX
     * @param movingVectorY
     */
    public void setMovingVector(float movingVectorX, float movingVectorY) {

        this.movingVectorX = movingVectorX * this.canvasWidth;
        this.movingVectorY = movingVectorY * this.canvasHeight;
    }


    public String getType() {
        return type;
    }

    public String getMyId() {
        return myId;
    }

    public int getLives() {
        return lives;
    }

    public void decrementLives() {
        this.lives -= 1;
    }

    public float getXPosition() {
        return (((float) this.x) / this.canvasWidth);
    }

    public void setXPosition(float xPercentage) {
        this.x = (int) (xPercentage * this.canvasWidth);
    }

    /**
     * To update the canvas dimensions after the canvas is created
     *
     * @param canvasHeight
     * @param canvasWidth
     */
    public void updateCanvuasDimensions(int canvasHeight, int canvasWidth) {
        this.canvasHeight = canvasHeight;
        this.canvasWidth = canvasWidth;
        // update character y position
        setY(canvasHeight);
        // velocity and moving speed depends on canvas width
        VELOCITY = VELOCITY * this.canvasWidth;
        movingVectorX = movingVectorX * this.canvasWidth;
    }
}
