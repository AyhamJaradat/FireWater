package gameClasses;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import utils.Constants;

/**
 * Created by Ayham on 8/13/2017.
 */

public class FallingCharacter extends GameCharacter implements Constants {


    // one rwo based on movement direction from top to butom
    private static final int ROW_TOP_TO_BOTTOM = 0;
    // Velocity of game character ( percentage of pixel/millisecond)
    public float VELOCITY = 0.0002f;
    // Row index of Image are being used.
    private int rowUsing = ROW_TOP_TO_BOTTOM;
    private int colUsing;
    // bitmap array for all direction
    private Bitmap[] topToBottoms;
    private int movingVectorX = 0;
    private float movingVectorY = 0.01f;
    private int canvasHeight;
    private long lastDrawMsTime = -1;
    private String type;
    private int counter = 0;
    private boolean isDone = false;
    private GameSurface gameSurface;
    private long fallingObjectTime = -1;
    private boolean amIFire;
    private int difficultyLevel;
    private boolean isMultiplayer = false;


    public FallingCharacter(GameSurface gameSurface, Bitmap image, int rowCount, int colCount, int x, int y, String type, boolean amIFire, int diffLevel, boolean isMultiplayer) {
        super(image, rowCount, colCount, x, y);

        this.gameSurface = gameSurface;
        this.type = type;
        this.amIFire = amIFire;
        this.difficultyLevel = diffLevel;
        this.isMultiplayer = isMultiplayer;
        this.canvasHeight = this.gameSurface.getHeight();
        if (this.difficultyLevel == 1 || this.difficultyLevel == 2 || this.difficultyLevel == 3) {
            VELOCITY = VELOCITY * this.canvasHeight;
            movingVectorY = movingVectorY * this.canvasHeight;
        } else if (this.difficultyLevel == 4) {
            VELOCITY = 0.0004f * this.canvasHeight;
            movingVectorY = 0.01f * this.canvasHeight;
        } else {
            VELOCITY = 0.0006f * this.canvasHeight;
            movingVectorY = 0.01f * this.canvasHeight;
        }
        // for every direction , create the movement images and add them to the array of the movement
        this.topToBottoms = new Bitmap[colCount]; // 3
        for (int col = 0; col < this.colCount; col++) {
            this.topToBottoms[col] = this.createSubImageAt(ROW_TOP_TO_BOTTOM, col);
        }
    }

    /**
     * function to get the array of bitmaps for the current direction
     *
     * @return
     */
    public Bitmap[] getMoveBitmaps() {
        switch (rowUsing) {
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
        if (this.counter % 10 == 0) {
            this.colUsing++;
            if (colUsing >= this.colCount) {
                this.colUsing = 0;
            }
        }

        // Current time in Millisseconds
        long now = System.currentTimeMillis();

        // Never once did draw.
        if (lastDrawMsTime == -1) {
            lastDrawMsTime = now;
        }
        // get delta time since last frame
        int deltaTime = (int) ((now - lastDrawMsTime));

        // Distance moves
        float distance = VELOCITY * deltaTime;
        double movingVectorLength = Math.sqrt(movingVectorX * movingVectorX + movingVectorY * movingVectorY);
        // Calculate the new position of the game character.
        this.x = x + (int) (distance * movingVectorX / movingVectorLength);
        this.y = y + (int) (distance * movingVectorY / movingVectorLength);
        // Make sure character does not go out of the screen
        if (this.x < 0) {
            this.x = 0;
        } else if (this.x > this.gameSurface.getWidth() - width) {
            this.x = this.gameSurface.getWidth() - width;
        }
        if (this.y <= 0) {
            this.y = 0;
            isDone = true;
        } else if (this.y > this.canvasHeight - height) {
            this.y = this.canvasHeight - height;
            isDone = true; // character reached ground
        }
        // keep time to find delta between frams
        lastDrawMsTime = System.currentTimeMillis();
    }


    public void draw(Canvas canvas) {
        if (fallingObjectTime == -1) {
            fallingObjectTime = System.currentTimeMillis();
        }
        // change trancperancy based on time passed
        int trancparancy = 255;
        long now = System.currentTimeMillis();
        if (this.isMultiplayer) {
            if (!this.amIFire && this.type.equals(FIRE_TYPE) || this.amIFire && this.type.equals(WATER_TYPE)) {
                float yDestancePerc = (float) y / this.canvasHeight;

//            trancparancy = getTrancparancyBasedOnLevel(this.difficultyLevel, yDestancePerc);
                if (yDestancePerc > 0.15) {
                    trancparancy = 0;
                }

            }
        }

        Bitmap bitmap = this.getCurrentMoveBitmap();
        Paint paint = new Paint();
        paint.setAlpha(trancparancy);
        canvas.drawBitmap(bitmap, x, y, paint);
    }

    private int getTrancparancyBasedOnLevel(int difficultyLevel, float yDestancePerc) {
        int tranc = 255;
        switch (difficultyLevel) {
            case 1: // always visible
                tranc = 255;
                break;
            case 2:
                if (yDestancePerc > 0.6) {
                    tranc = 80;
                } else if (yDestancePerc > 0.4) {
                    tranc = 140;
                }
                break;
            case 3:
                if (yDestancePerc > 0.5) {
                    tranc = 0;
                } else if (yDestancePerc > 0.35) {
                    tranc = 100;
                }
                break;
            case 4:
            case 5:
                if (yDestancePerc > 0.4) {
                    tranc = 0;
                } else if (yDestancePerc > 0.3) {
                    tranc = 100;
                }
                break;

        }

        return tranc;

    }


    public boolean isReachGround() {
        return isDone;
    }

    public String getType() {
        return type;
    }


}
