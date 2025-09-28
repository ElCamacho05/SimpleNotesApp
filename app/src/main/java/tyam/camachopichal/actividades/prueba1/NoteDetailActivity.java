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

        // La línea que requiere que el archivo XML se llame activity_note_detail.xml
        binding = ActivityNoteDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = AppDatabase.getDatabase(this);

        setSupportActionBar(binding.toolbarDetail);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        if (getIntent().hasExtra("NOTE_ID")) {
            noteIdToEdit = getIntent().getIntExtra("NOTE_ID", -1);
            if (noteIdToEdit != -1) {
                loadNoteForEditing(noteIdToEdit);
                binding.toolbarDetail.setTitle("Editar Nota");
            }
        } else {
            binding.toolbarDetail.setTitle("Nueva Nota");
        }

        binding.buttonSaveNote.setOnClickListener(v -> saveNote());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void loadNoteForEditing(int noteId) {
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
                })
                .exceptionally(e -> {
                    Log.e(TAG, "Error cargando nota para edición: " + e.getMessage());
                    return null;
                });
    }

    private void saveNote() {
        String title = binding.editTextNoteTitle.getText().toString().trim();
        String content = binding.editTextNoteContent.getText().toString().trim();
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        if (content.isEmpty()) {
            Toast.makeText(this, "El cuerpo de la nota no puede estar vacío.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (title.isEmpty()) {
            title = "Nota sin Título (" + now + ")";
        }

        Note note = new Note();
        note.title = title;
        note.content = content;

        if (noteIdToEdit != -1) {
            note.id = noteIdToEdit;
        }

        if (noteIdToEdit == -1) {
            note.date = now;
        }

        note.lastModified = now;

        CompletableFuture.runAsync(() -> {
            try {
                if (noteIdToEdit != -1) {
                    db.noteDao().update(note);
                } else {
                    db.noteDao().insert(note);
                }
                runOnUiThread(() -> {
                    finish();
                });
            } catch (Exception e) {
                Log.e(TAG, "Error al guardar/actualizar nota", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error al guardar la nota.", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
