package com.example.codeforcesreminder;

public class Contest {
    private int id;
    private String name;
    private long startTime;
    private String phase;

    public Contest(int id, String name, long startTime, String phase) {
        this.id = id;
        this.name = name;
        this.startTime = startTime;
        this.phase = phase;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getStartTime() {
        return startTime;
    }

    public String getPhase() {
        return phase;
    }
}
