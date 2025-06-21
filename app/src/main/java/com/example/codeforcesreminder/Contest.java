package com.example.codeforcesreminder;

public class Contest {
    private int id;
    private String name;
    private long startTime;
    private String platform; // "codeforces" or "codechef"

    public Contest(int id, String name, long startTime, String platform) {
        this.id = id;
        this.name = name;
        this.startTime = startTime;
        this.platform = platform;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public long getStartTime() { return startTime; }
    public String getPlatform() { return platform; }
}
