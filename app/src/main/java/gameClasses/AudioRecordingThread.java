package gameClasses;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.game.firewater.FireGameActivity;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Ayham on 8/30/2017.
 */

public class AudioRecordingThread extends Thread {
    private boolean stopped = false;

    private FireGameActivity activity;

    /**
     * Give the thread high priority so that it's not canceled unexpectedly, and start it
     */
    public AudioRecordingThread(FireGameActivity activity) {
        this.activity = activity;
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        start();
    }

    @Override
    public void run() {
        Log.i("AudioRecordingThread", "Running AudioRecordingThread Thread");
        AudioRecord recorder = null;
//        AudioTrack track = null;
//        short[][] buffers = new short[256][160];
        short[] buffer/* = new short[160]*/;
        int ix = 0;
        int SAMPLE_RATE = 44100/*44100*//*8000*/;

        /*
         * Initialize buffer to hold continuously recorded audio data, start recording, and start
         * playback.
         */
        try {
            int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
                bufferSize = SAMPLE_RATE * 2;
            }

            // constrain by google play
            if (bufferSize > 1150) {
                bufferSize = 1150;
            }
            buffer = new short[bufferSize / 2/*160*/];
            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize /** 10*/);
//            track = new AudioTrack(AudioManager.STREAM_MUSIC, 8000,
//                    AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, N * 10, AudioTrack.MODE_STREAM);

            if (recorder.getState() != AudioRecord.STATE_INITIALIZED) {
                Log.e("AudioRecordingThread", "AudioRecordingThread Record can't initialize!");
                return;
            }
            recorder.startRecording();
//            track.play();
            /*
             * Loops until something outside of this thread stops it.
             * Reads the data from the recorder and writes it to the audio track for playback.
             */
            long shortsRead = 0;
            while (!stopped) {
                Log.i("Map", "Writing new data to buffer");
//                bufferSize = recorder.read(buffer, 0, buffer.length);
                int numberOfShort = recorder.read(buffer, 0, buffer.length);
                shortsRead += numberOfShort;
                // Do something with the buffer
                // convert


                // to turn shorts back to bytes.
                byte[] bytes2 = new byte[buffer.length * 2];
                ByteBuffer.wrap(bytes2).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(buffer);

                this.activity.sendVoice(bytes2);
                // to turn bytes to shorts as either big endian or little endian.
//                short[] shorts = new short[bytes2.length/2];
//                ByteBuffer.wrap(bytes2).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);


                // play
//                track.write(shorts, 0, shorts.length);
//
            }
        } catch (Throwable x) {
            Log.w("AudioRecordingThread", "Error reading voice audio", x);
        }
        /*
         * Frees the thread's resources after the loop completes so that it can be run again
         */ finally {
            recorder.stop();
            recorder.release();
//            track.stop();
//            track.release();
        }
    }

    /**
     * Called from outside of the thread in order to stop the recording/playback loop
     */
    public void close() {
        stopped = true;
    }

}
