package com.smartnurse.app;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import android.Manifest;

public class MainActivity extends Activity {

    private EditText etName, etInstructions, etPhone;
    private ImageView ivPhoto;
    private Button btnSelectPhoto, btnRecord, btnPlay, btnSave, btnPickTime;
    private TextView tvTime;
    private ListView lvMedications;
    private MedicationAdapter adapter;
    private List<Medication> medicationList;

    private DatabaseHelper dbHelper;
    private RecordingHelper recordingHelper;
    private String selectedPhotoPath = "";
    private String selectedAudioPath = "";
    private long editingId = -1;
    private boolean isRecording = false;

    private static final int REQUEST_CODE_PERMISSIONS = 100;
    private static final int REQUEST_IMAGE_PICK = 200;
    private static final int REQUEST_EXACT_ALARM = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        recordingHelper = new RecordingHelper();

        // اتصال view ها
        etName = findViewById(R.id.etName);
        etInstructions = findViewById(R.id.etInstructions);
        etPhone = findViewById(R.id.etPhone);
        ivPhoto = findViewById(R.id.ivPhoto);
        btnSelectPhoto = findViewById(R.id.btnSelectPhoto);
        btnRecord = findViewById(R.id.btnRecord);
        btnPlay = findViewById(R.id.btnPlay);
        btnSave = findViewById(R.id.btnSave);
        btnPickTime = findViewById(R.id.btnPickTime);
        tvTime = findViewById(R.id.tvTime);
        lvMedications = findViewById(R.id.lvMedications);

        // نمایش لیست
        loadMedicationList();

        // انتخاب عکس
        btnSelectPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_IMAGE_PICK);
            }
        });

        // انتخاب زمان
        btnPickTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                TimePickerDialog dialog = new TimePickerDialog(MainActivity.this,
                        (view, hourOfDay, minute) -> {
                            String time = String.format("%02d:%02d", hourOfDay, minute);
                            tvTime.setText(time);
                        },
                        cal.get(Calendar.HOUR_OF_DAY),
                        cal.get(Calendar.MINUTE),
                        true);
                dialog.show();
            }
        });

        // ضبط صدا
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRecording) {
                    recordingHelper.startRecording(MainActivity.this, System.currentTimeMillis());
                    isRecording = true;
                    btnRecord.setText("توقف ضبط");
                } else {
                    recordingHelper.stopRecording();
                    isRecording = false;
                    btnRecord.setText("ضبط پیام صوتی");
                    File audioFile = recordingHelper.getAudioFile();
                    if (audioFile != null) {
                        selectedAudioPath = audioFile.getAbsolutePath();
                        Toast.makeText(MainActivity.this, "صدا ذخیره شد", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // پخش صدای ضبط‌شده
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedAudioPath != null && !selectedAudioPath.isEmpty()) {
                    File audioFile = new File(selectedAudioPath);
                    if (audioFile.exists()) {
                        try {
                            android.media.MediaPlayer player = new android.media.MediaPlayer();
                            player.setDataSource(audioFile.getAbsolutePath());
                            player.prepare();
                            player.start();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "خطا در پخش صدا", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "فایل صوتی وجود ندارد", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "ابتدا صدا را ضبط کنید", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // ذخیره
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMedication();
            }
        });

        // بررسی مجوزها
        checkPermissions();
    }

    public void loadMedicationList() {
        medicationList = dbHelper.getAllMedications();
        adapter = new MedicationAdapter(this, medicationList);
        lvMedications.setAdapter(adapter);
    }

    private void saveMedication() {
        String name = etName.getText().toString().trim();
        String instructions = etInstructions.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String time = tvTime.getText().toString().trim();

        if (name.isEmpty() || instructions.isEmpty() || phone.isEmpty() || time.equals("انتخاب زمان")) {
            Toast.makeText(this, "لطفاً همه فیلدها را پر کنید", Toast.LENGTH_SHORT).show();
            return;
        }

        Medication med;
        if (editingId != -1) {
            med = new Medication(editingId, name, instructions, selectedPhotoPath, time, phone, selectedAudioPath);
            dbHelper.updateMedication(med);
            cancelAlarm(editingId);
        } else {
            long newId = dbHelper.insertMedication(new Medication(0, name, instructions, selectedPhotoPath, time, phone, selectedAudioPath));
            med = dbHelper.getMedication(newId);
        }

        // تنظیم آلارم
        scheduleAlarm(med);

        // پاک کردن فرم
        clearForm();
        loadMedicationList();
        Toast.makeText(this, "ذخیره شد", Toast.LENGTH_SHORT).show();
    }

    private void scheduleAlarm(Medication med) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra(AlarmReceiver.EXTRA_MED_ID, med.getId());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                (int) med.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String[] parts = med.getTime().split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(), pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(), pendingIntent);
            }
        } catch (SecurityException e) {
            alarmManager.set(AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(), pendingIntent);
        }
    }

    public void cancelAlarm(long medId) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                (int) medId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
    }

    private void clearForm() {
        etName.setText("");
        etInstructions.setText("");
        etPhone.setText("");
        ivPhoto.setImageDrawable(null);
        selectedPhotoPath = "";
        selectedAudioPath = "";
        tvTime.setText("انتخاب زمان");
        editingId = -1;
        isRecording = false;
        btnRecord.setText("ضبط پیام صوتی");
    }

    public void editMedication(Medication med) {
        editingId = med.getId();
        etName.setText(med.getName());
        etInstructions.setText(med.getInstructions());
        etPhone.setText(med.getPhone());
        tvTime.setText(med.getTime());
        selectedPhotoPath = med.getPhotoPath();
        selectedAudioPath = med.getAudioPath();

        if (med.getPhotoPath() != null && !med.getPhotoPath().isEmpty()) {
            File imgFile = new File(med.getPhotoPath());
            if (imgFile.exists()) {
                ivPhoto.setImageURI(Uri.fromFile(imgFile));
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                selectedPhotoPath = copyImageToInternalStorage(imageUri);
                if (selectedPhotoPath != null) {
                    ivPhoto.setImageURI(Uri.fromFile(new File(selectedPhotoPath)));
                }
            }
        }
    }

    private String copyImageToInternalStorage(Uri imageUri) {
        try {
            InputStream input = getContentResolver().openInputStream(imageUri);
            File dir = getFilesDir();
            File imageFile = new File(dir, "photo_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream output = new FileOutputStream(imageFile);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = input.read(buffer)) != -1) {
                output.write(buffer, 0, len);
            }
            output.close();
            input.close();
            return imageFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void checkPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.RECORD_AUDIO);
            }
            if (checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.SEND_SMS);
            }
            if (Build.VERSION.SDK_INT >= 33) {
                if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    permissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS);
                }
            }
            if (!permissionsNeeded.isEmpty()) {
                requestPermissions(permissionsNeeded.toArray(new String[0]), REQUEST_CODE_PERMISSIONS);
            }
        }
        // برای exact alarm در اندروید ۱۲ به بالا
        if (Build.VERSION.SDK_INT >= 31) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivityForResult(intent, REQUEST_EXACT_ALARM);
            }
        }
    }
}
