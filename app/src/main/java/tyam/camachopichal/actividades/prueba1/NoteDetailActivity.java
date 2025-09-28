package tyam.camachopichal.actividades.prueba1;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import tyam.camachopichal.actividades.prueba1.databinding.ActivityNoteDetailBinding;
import tyam.camachopichal.actividades.prueba1.db.AppDatabase;
import tyam.camachopichal.actividades.prueba1.db.Note;

public class NoteDetailActivity extends AppCompatActivity {
    private ActivityNoteDetailBinding binding;
    private AppDatabase db;
    private int noteIdToEdit = -1;
    private final String TAG = "NoteDetailActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Se asume que DataBinding está habilitado y el layout es activity_note_detail.xml
        binding = ActivityNoteDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = AppDatabase.getDatabase(this);

        // Configurar la Toolbar
        setSupportActionBar(binding.toolbarDetail);
        // Habilitar el botón de regreso (back/home)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // 1. Manejar la Edición de Notas (si hay un ID)
        if (getIntent().hasExtra("NOTE_ID")) {
            noteIdToEdit = getIntent().getIntExtra("NOTE_ID", -1);
            if (noteIdToEdit != -1) {
                loadNoteForEditing(noteIdToEdit);
                binding.toolbarDetail.setTitle("Editar Nota");
            }
        } else {
            binding.toolbarDetail.setTitle("Nueva Nota");
        }

        // 2. Manejar el botón de Guardar
        binding.buttonSaveNote.setOnClickListener(v -> saveNote());
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Regresa a la actividad anterior sin guardar
        onBackPressed();
        return true;
    }

    private void loadNoteForEditing(int noteId) {
        // Cargar la nota en un hilo secundario
        CompletableFuture.supplyAsync(() -> db.noteDao().getNoteById(noteId))
                .thenAccept(note -> {
                    if (note != null) {
                        runOnUiThread(() -> {
                            binding.editTextNoteTitle.setText(note.title);
                            binding.editTextNoteContent.setText(note.content);
                        });
                    } else {
                        Log.e(TAG, "Nota no encontrada con ID: " + noteId);
                    }
                });
    }

    private void saveNote() {
        String title = binding.editTextNoteTitle.getText().toString().trim();
        String content = binding.editTextNoteContent.getText().toString().trim();
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // validacion 1, el cuerpo de la nota no puede estar vacio
        if (content.isEmpty()) {
            Toast.makeText(this, "No puede haber una nota vacia.", Toast.LENGTH_SHORT).show();
            return;
        }

        // validacion 2, enerar titulo si esta vacio
        if (title.isEmpty()) {
            title = "Nota sin Título (" + now + ")";
        }

        Note note = new Note();
        note.title = title;
        note.content = content;
        note.date = now; // Usar el timestamp inicial como "date"
        note.lastModified = now;

        CompletableFuture.supplyAsync(() -> {
            try {
                if (noteIdToEdit != -1) {
                    // Actualizar
                    note.id = noteIdToEdit;
                    db.noteDao().update(note);
                } else {
                    // Insertar nuevo
                    db.noteDao().insert(note);
                }
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Error al guardar/actualizar nota", e);
                return false;
            }
        }).thenAccept(success -> {
            runOnUiThread(() -> {
                if (success) {
                    // Regresar a la pantalla principal después de guardar/actualizar
                    finish();
                } else {
                    Toast.makeText(this, "Error al guardar la nota.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}