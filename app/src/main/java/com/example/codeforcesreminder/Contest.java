package com.example.codeforcesreminder;

public class Contest {
    private int id;
    private String name;
    private long startTime;

    public Contest(int id, String name, long startTime) {
        this.id = id;
        this.name = name;
        this.startTime = startTime;
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
}
