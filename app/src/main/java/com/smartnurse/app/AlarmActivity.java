package com.smartnurse.app;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;

public class AlarmActivity extends Activity {

    private MediaPlayer mediaPlayer;
    private TextView tvName, tvInstructions;
    private ImageView ivPhoto;
    private Button btnStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        tvName = findViewById(R.id.tvName);
        tvInstructions = findViewById(R.id.tvInstructions);
        ivPhoto = findViewById(R.id.ivPhoto);
        btnStop = findViewById(R.id.btnStop);

        long medId = getIntent().getLongExtra(AlarmReceiver.EXTRA_MED_ID, -1);
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        Medication med = dbHelper.getMedication(medId);

        if (med != null) {
            tvName.setText(med.getName());
            tvInstructions.setText(med.getInstructions());

            // نمایش عکس اگر وجود داشته باشد
            if (med.getPhotoPath() != null && !med.getPhotoPath().isEmpty()) {
                File imgFile = new File(med.getPhotoPath());
                if (imgFile.exists()) {
                    ivPhoto.setImageURI(android.net.Uri.fromFile(imgFile));
                }
            }

            // پخش پیام صوتی
            if (med.getAudioPath() != null && !med.getAudioPath().isEmpty()) {
                File audioFile = new File(med.getAudioPath());
                if (audioFile.exists()) {
                    try {
                        mediaPlayer = new MediaPlayer();
                        mediaPlayer.setDataSource(audioFile.getAbsolutePath());
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "خطا در پخش صدا", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
