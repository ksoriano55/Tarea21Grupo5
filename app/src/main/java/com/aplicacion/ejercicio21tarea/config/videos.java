package com.aplicacion.ejercicio21tarea.config;

public class videos {
    private String id;
    private String videos;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVideos() {
        return videos;
    }

    public void setVideos(String videos) {
        this.videos = videos;
    }

    public videos(String id, String videos) {
        this.id = id;
        this.videos = videos;
    }

    public videos() {
    }
}
