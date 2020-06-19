package edu.uci.ics.fabflixmobile;

public class Movie {
    private String name;
    private String id;
    private short year;

    public Movie(String id, String name, short year) {
        this.name = name;
        this.id = id;
        this.year = year;
    }

    public String getName() {
        return name;
    }

    public String getID() {
        return id;
    }

    public short getYear() {
        return year;
    }
}