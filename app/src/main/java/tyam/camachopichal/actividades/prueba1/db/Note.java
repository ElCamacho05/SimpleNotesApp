package tyam.camachopichal.actividades.prueba1.db;

import androidx.room.PrimaryKey;
import androidx.room.Entity;

@Entity(tableName = "notes")
public class Note implements Comparable<Note>{
    @PrimaryKey(autoGenerate = true)
    public Integer id;
    public String title;
    public String content;
    public String date;
    public String lastModified;

    @Override
    public int compareTo (Note n) {
        return Integer.compare (n.id, this.id);
    }

    public Note() {
    }

}
