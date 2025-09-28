package tyam.camachopichal.actividades.prueba1.ui;

import android.app.Application; // <-- ¡ESTE ES EL IMPORT CRÍTICO!
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import tyam.camachopichal.actividades.prueba1.db.AppDatabase;
import tyam.camachopichal.actividades.prueba1.db.Note;
import tyam.camachopichal.actividades.prueba1.db.DAO_Note;

public class NoteViewModel extends AndroidViewModel {

    private final DAO_Note noteDao;
    private final LiveData<List<Note>> allNotes;

    // CONSTRUCTOR: El sistema llama a este constructor y le pasa la Application
    public NoteViewModel(@NonNull Application application) {
        super(application);

        AppDatabase db = AppDatabase.getDatabase(application);
        noteDao = db.noteDao();
        allNotes = noteDao.getAllNotes();
    }

    public LiveData<List<Note>> getAllNotes() {
        return allNotes;
    }
}

