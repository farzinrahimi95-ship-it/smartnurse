package com.smartnurse.app;

public class Medication {
    private long id;
    private String name;
    private String instructions;
    private String photoPath;
    private String time; // format "HH:mm"
    private String phone;
    private String audioPath;

    // سازنده
    public Medication(long id, String name, String instructions, String photoPath,
                      String time, String phone, String audioPath) {
        this.id = id;
        this.name = name;
        this.instructions = instructions;
        this.photoPath = photoPath;
        this.time = time;
        this.phone = phone;
        this.audioPath = audioPath;
    }

    // getter و setter
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAudioPath() { return audioPath; }
    public void setAudioPath(String audioPath) { this.audioPath = audioPath; }
}
