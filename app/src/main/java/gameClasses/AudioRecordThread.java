package gameClasses;

//
//import android.media.AudioFormat;
//import android.media.AudioRecord;
//import android.media.MediaRecorder;
//import android.nfc.Tag;
//import android.util.Log;
//
//import java.nio.Buffer;
//import java.nio.ShortBuffer;
//
///**
// * Created by Ayham on 8/29/2017.
// */
//
public class AudioRecordThread implements Runnable {
    //
//    String Tag = "AudioRecordThread";
//    boolean isRecorderStart=false;
//    MediaRecorder recorder;
//    public AudioRecordThread(){
//        recorder = new MediaRecorder();
//    }
//
    @Override
    public void run() {
//        int bufferLength = 0;
//        int bufferSize;
//        short[] audioData;
//        int bufferReadResult;
//        int sampleAudioBitRate = 1000;
//
//        try {
//            bufferSize = AudioRecord.getMinBufferSize(sampleAudioBitRate,
//                    AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
//
//            if (bufferSize <= 2048) {
//                bufferLength = 2048;
//            } else if (bufferSize <= 4096) {
//                bufferLength = 4096;
//            }
//
//                /* set audio recorder parameters, and start recording */
//            AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleAudioBitRate,
//                    AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferLength);
//            audioData = new short[bufferLength];
//            audioRecord.startRecording();
//            Log.d(Tag, "audioRecord.startRecording()");
//
//           boolean isAudioRecording = true;
//
//                /* ffmpeg_audio encoding loop */
//            while (isAudioRecording) {
//                bufferReadResult = audioRecord.read(audioData, 0, audioData.length);
//
//                if (bufferReadResult == 1024 && isRecorderStart) {
//                    Buffer realAudioData1024 = ShortBuffer.wrap(audioData, 0, 1024);
//
//
//                    recorder.record(realAudioData1024);
//
//
//                } else if (bufferReadResult == 2048 && isRecorderStart) {
//                    Buffer realAudioData2048_1 = ShortBuffer.wrap(audioData, 0, 1024);
//                    Buffer realAudioData2048_2 = ShortBuffer.wrap(audioData, 1024, 1024);
//                    for (int i = 0; i < 2; i++) {
//                        if (i == 0) {
//
//                            recorder.record(realAudioData2048_1);
//
//
//                        } else if (i == 1) {
//
//                            recorder.record(realAudioData2048_2);
//
//
//
//                        }
//                    }
//                }
//            }
//
//                /* encoding finish, release recorder */
//            if (audioRecord != null) {
//                try {
//                    audioRecord.stop();
//                    audioRecord.release();
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                audioRecord = null;
//            }
//
//            if (recorder != null && isRecorderStart) {
//                try {
//                    recorder.stop();
//                    recorder.release();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                recorder = null;
//            }
//        } catch (Exception e) {
//            Log.e(Tag, "get audio data failed:" + e.getMessage() + e.getCause() + e.toString());
//        }
//
    }
//
//
}
