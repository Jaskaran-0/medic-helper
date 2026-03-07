package data_models;

import java.time.LocalDateTime;

public class Note {
    private int noteId;
    private final String title;
    private final String noteContent;
    private String createdAt;
    private String image;

    public Note(int noteId, String title, String noteContent, String createdAt, String image) {
        this.noteId = noteId;
        this.title = title;
        this.noteContent = noteContent;
        this.createdAt = createdAt;
        this.image = image;
    }

    public Note(String title, String noteContent, String image) {
        this.title = title;
        this.noteContent = noteContent;
        this.image = image;
    }

    public Note(String title, String noteContent) {
        this.title = title;
        this.noteContent = noteContent;
    }

    public int getNoteId() {
        return noteId;
    }

    public String getTitle() {
        return title;
    }

    public String getNoteContent() {
        return noteContent;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getImage() {
        return image;
    }

    @Override
    public String toString() {
        return "Note{" +
                "noteId=" + noteId +
                ", title='" + title + '\'' +
                ", noteContent='" + noteContent + '\'' +
                ", createdAt=" + createdAt +
                ", image='" + image + '\'' +
                '}';
    }
}
