package gameClasses;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import utils.Constants;

/**
 * Created by Ayham on 3/17/2018.
 */

public class GoToAnimiation extends GameCharacter implements Constants {

    private int rowIndex = 0;
    private int colIndex = -1;
    private boolean finish = false;
    private int animCounter = 0;

    public GoToAnimiation( Bitmap image, int x, int y, int rowCount, int colCount, String type) {
        super(image, rowCount, colCount, x, y);

        switch (type) {
            case "rightFire":
//                this.rowIndex = 0;
                this.rowIndex = 4;
                break;
            case "leftFire":
//              this.rowIndex = 1;
                this.rowIndex = 5;
                break;
            case "rightWater":
//                this.rowIndex = 0;
                this.rowIndex = 2;
                break;
            case "leftWater":
//                this.rowIndex = 1;
                this.rowIndex = 3;
                break;
        }

    }

    public void update() {
        // increment column to next frame
        this.colIndex++;

        // once reach last column
        if (this.colIndex >= this.colCount) {
            // go back to first column
            this.colIndex = 0;
            this.animCounter++;
            // increment row if more than one row exists
            if (this.animCounter == 4) {
//                starts are one row only
                this.finish = true;
                this.animCounter = 0;
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
