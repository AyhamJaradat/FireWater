package gameClasses;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import java.nio.ShortBuffer;

/**
 * Created by Ayham on 8/30/2017.
 */

public class AudioPlay extends Thread {

    private boolean mShouldContinue = true;
    private String TAG = "AudioPlay";
    private int SAMPLE_RATE = 44100/*44100*/;
    private  short[] shorts;
    private int mNumSamples;
    private ShortBuffer mSamples;

    private short[] BUFFER;
    private boolean isNewData;
    private int dataLength;

    public AudioPlay() {
//        this.shorts = shorts;
//        this.mSamples = ShortBuffer.wrap(shorts);
//        this.mNumSamples = shorts.length;
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        start();
    }

    @Override
    public void run() {
        int bufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        if (bufferSize == AudioTrack.ERROR || bufferSize == AudioTrack.ERROR_BAD_VALUE /*|| bufferSize > shorts.length*/) {
            bufferSize = SAMPLE_RATE * 2;
        }

        BUFFER = new short[bufferSize];
        AudioTrack track = null;
        try {
            track = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE/*8000*/,
                    AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM /*AudioTrack.MODE_STREAM*/);

            if (track.getState() != AudioTrack.STATE_INITIALIZED) {
                Log.e("AudioRecordingThread", "AudioRecordingThread Record can't initialize!");
//                return;
            }
            track.play();


            while (mShouldContinue) {

                if (isNewData) {
//                    track.write(shorts, 0, shorts.length);
                    track.write(BUFFER, 0, dataLength);
                    isNewData = false;
                }

            }


        } catch (Throwable x) {
            Log.w("AudioRecordingThread", "Error reading voice audio", x);
        } finally {
            track.stop();
            track.release();
        }
    }

    public void close() {
        mShouldContinue = true;
    }

    public void writeToBuffer(short[] shorts) {

        if (isNewData) {
            // data in buffer is not read yet
            // concatinate on it
            System.arraycopy(shorts, 0, BUFFER, dataLength, shorts.length);
            isNewData = true;
            dataLength = shorts.length;
        } else {
            // data in buffer is read ,, clear it
            System.arraycopy(shorts, 0, BUFFER, 0, shorts.length);
            isNewData = true;
            dataLength = shorts.length;
        }


    }

}
