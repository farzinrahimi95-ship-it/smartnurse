package com.smartnurse.app;

import android.content.Context;
import android.media.MediaRecorder;
import java.io.File;

public class RecordingHelper {
    private MediaRecorder recorder;
    private File audioFile;

    // شروع ضبط
    public void startRecording(Context context, long medId) {
        try {
            audioFile = new File(context.getFilesDir(), "audio_" + medId + ".m4a");
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setOutputFile(audioFile.getAbsolutePath());
            recorder.prepare();
            recorder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // توقف ضبط
    public void stopRecording() {
        try {
            if (recorder != null) {
                recorder.stop();
                recorder.release();
                recorder = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public File getAudioFile() {
        return audioFile;
    }
}
