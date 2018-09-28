package gameClasses;

/**
 * Created by Ayham on 8/12/2017.
 */

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class GameThread extends Thread {

    private boolean isRunning;
    private GameSurface gameSurface;
    private SurfaceHolder surfaceHolder;

    public GameThread(GameSurface gameSurface, SurfaceHolder surfaceHolder) {
        this.gameSurface = gameSurface;
        this.surfaceHolder = surfaceHolder;
    }

    /**
     * do the rendering on the canvas
     */
    private void render() {
        Canvas canvas = null;
        try {
            // Get Canvas from Holder and lock it.
            canvas = this.surfaceHolder.lockCanvas();

            // Synchronized
            synchronized (canvas) {
                this.gameSurface.draw(canvas);
            }
        } catch (Exception e) {
            // Do nothing.
        } finally {
            if (canvas != null) {
                // Unlock Canvas.
                this.surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    @Override
    public void run() {
//        double lastUpdateTime = System.currentTimeMillis();
//        double lastRenderTime = System.currentTimeMillis();
//        double previous = System.currentTimeMillis();
//        double lag = 0.0;
        // if we want 60 frame per second = 60 FPS
        // then we need to draw a frame every 16 Ms = (1/60)*1000
        // the update time should be faster than the render time (faster than 60 FPS)
        // so lets set MS_PER_UPDATE shorter than 16
//        int MS_PER_UPDATE =30/*6*/;
//        double tempLeftTimePortion;


        // new solution
        int FRAME_PER_SECOND = 30 /*60*/;
        double MS_PER_UPDATE = 1000 / FRAME_PER_SECOND;
        int MAX_FRAMESKIP = 5;

        double next_game_tick = System.currentTimeMillis();
        int loops;
//        float interpolation;
        while (isRunning) {

            loops = 0;
            while (System.currentTimeMillis() > next_game_tick && loops < MAX_FRAMESKIP) {
                this.gameSurface.update();
                next_game_tick += MS_PER_UPDATE;
                loops++;
            }
//            interpolation = (float)( System.currentTimeMillis() + MS_PER_UPDATE - next_game_tick ) / (float)( MS_PER_UPDATE );
//          display_game( interpolation );

            render();


//            double current = System.currentTimeMillis();
//            double elapsed = current - previous;
//            previous = current;
//            lag += elapsed;
//            // update positions and states
//            while (lag >= MS_PER_UPDATE) {
//                if (lag - MS_PER_UPDATE < MS_PER_UPDATE) {
//
//
//                    // last update before  next rended
//                    tempLeftTimePortion = (lag - MS_PER_UPDATE) / MS_PER_UPDATE;
////                    tempLeftTimePortion = 1- tempLeftTimePortion;
//                    this.gameSurface.update(1 + tempLeftTimePortion);
//                } else {
//                    this.gameSurface.update(1);
//                }
//                double now = System.currentTimeMillis();
//                Log.d("updateTime Interval"," : " +(now-lastUpdateTime) );
//                lastUpdateTime = now;
//                lag -= MS_PER_UPDATE;
//            }
//            // render to canvas
//            render();

        }
    }

    public void setRunning(boolean running) {
        this.isRunning = running;
    }
}