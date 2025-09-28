package tyam.camachopichal.actividades.prueba1.ui;

import android.app.Application;

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

    public NoteViewModel(@NonNull Application application) {
        super(application);
        // Obtener la base de datos y el DAO
        AppDatabase db = AppDatabase.getDatabase(application);
        noteDao = db.noteDao();
        // Inicializar LiveData para observar todas las notas
        allNotes = noteDao.getAllNotes();
    }

    // Método para obtener la lista de notas que se observará en MainActivity
    public LiveData<List<Note>> getAllNotes() {
        return allNotes;
    }

    // Aquí irían métodos para insertar/eliminar si no usáramos CompletableFuture en la Activity.
    // Aunque es mejor práctica, por simplicidad para la actividad, lo manejamos en la Activity.
}
