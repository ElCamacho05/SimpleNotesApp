package tyam.camachopichal.actividades.prueba1.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Note.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract DAO_Note noteDao();

    private static volatile AppDatabase INSTANCE;

    // singleton para que no se pueda lleagr a llenar la memoria por instanciacion multiple
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "simple_notes_db")
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries() // <-- AÑADIDO: Permite la inicialización en el hilo principal.
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
