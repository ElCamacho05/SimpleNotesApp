package db_classes;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "note")
public class Note implements Comparable<Note>{

    @PrimaryKey (autoGenerate = true)
    public Integer id;
    public String title;
    public String content;
    public String date;

    public String lastModified;

    public Note(String title, String content, String date) {
        this.title = title;
        this.content = content;
        this.date = date;
        this.lastModified = date;
    }

    public int compareTo(Note note){
        return Integer.compare(this.id, note.id);
    }
}
