package gameClasses;

/**
 * Created by Ayham on 8/12/2017.
 */

import android.graphics.Bitmap;
import android.graphics.Canvas;

import utils.Constants;

public class Explosion extends GameCharacter implements Constants{

    private int rowIndex = 0;
    private int colIndex = -1;
    private boolean finish = false;
    private GameSurface gameSurface;
    private String type;

    public Explosion(GameSurface GameSurface, Bitmap image, int x, int y, String characterType, int rowCount, int colCount) {
        super(image, rowCount, colCount, x, y);
        this.type = characterType;
        this.gameSurface = GameSurface;

        // for stars image rows does not change
        if (this.type.equalsIgnoreCase(WATER_STAR_TYPE)) {
            this.rowIndex = 1;
        } else if (this.type.equalsIgnoreCase(FIRE_STAR_TYPE)) {
            this.rowIndex = 0;
        }
    }

    public void update() {
        // increment column to next frame
        this.colIndex++;
        // Play sound explosion.wav.
        if (this.colIndex == 0 && this.rowIndex == 0) {
            this.gameSurface.playSoundExplosion(this.type);
        }
        // once reach last column
        if (this.colIndex >= this.colCount) {
            // go back to first column
            this.colIndex = 0;
            // increment row if more than one row exists
            if (this.type.equalsIgnoreCase(WATER_STAR_TYPE) ||this.type.equalsIgnoreCase(FIRE_STAR_TYPE) ) {
//                starts are one row only
                this.finish = true;
            } else {
                this.rowIndex++;
                if (this.rowIndex >= this.rowCount) {
                    this.finish = true;
                }
            }
        }
    }

    public void draw(Canvas canvas) {
        if (!finish) {
            Bitmap bitmap = this.createSubImageAt(rowIndex, colIndex);
            canvas.drawBitmap(bitmap, this.x, this.y, null);
        }
    }

    public boolean isFinish() {
        return finish;
    }

}