package gameClasses;

import android.graphics.Bitmap;

/**
 * Created by Ayham on 8/12/2017.
 */

public abstract class GameCharacter {

    protected Bitmap image;
    protected int rowCount;
    protected int colCount;
    protected int totalWidth;
    protected int totalHeight;
    protected int width;
    protected int height;
    protected int x;
    protected int y;

    public GameCharacter(Bitmap image, int rowCount, int colCount, int x, int y) {

        this.image = image;
        this.rowCount = rowCount;
        this.colCount = colCount;
        this.x = x;
        this.y = y;
        this.totalWidth = image.getWidth();
        this.totalHeight = image.getHeight();
        this.width = this.totalWidth / colCount;
        this.height = this.totalHeight / rowCount;
    }

    /**
     * method to creat a bitmap as sub image of the original image
     * @param row
     * @param col
     * @return
     */
    protected Bitmap createSubImageAt(int row, int col) {
        // createBitmap(bitmap, x, y, width, height).
        Bitmap subImage = Bitmap.createBitmap(image, col * width, row * height, width, height);
        return subImage;
    }


    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getHeight() {
        return height;
    }


    public int getWidth() {
        return width;
    }
}
