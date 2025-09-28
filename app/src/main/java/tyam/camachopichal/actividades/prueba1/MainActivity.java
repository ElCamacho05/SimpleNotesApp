package tyam.camachopichal.actividades.prueba1;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import tyam.camachopichal.actividades.prueba1.databinding.ActivityMainBinding;
import tyam.camachopichal.actividades.prueba1.db.AppDatabase;
import tyam.camachopichal.actividades.prueba1.db.Note;
import tyam.camachopichal.actividades.prueba1.ui.NoteViewModel;
import tyam.camachopichal.actividades.prueba1.ui.NoteViewModelFactory;
import tyam.camachopichal.actividades.prueba1.ui.NoteAdapter;

public class MainActivity extends AppCompatActivity implements NoteAdapter.NoteItemListener {
    private ActivityMainBinding binding;
    private NoteAdapter noteAdapter;
    private NoteViewModel noteViewModel;

    // estado para manejar la selección de notas
    private boolean isSelectionMode = false;
    private List<Note> selectedNotes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // data binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbarMain);

        // configuracio del view model
        NoteViewModelFactory factory = new NoteViewModelFactory(getApplication());
        noteViewModel = new ViewModelProvider(this, factory).get(NoteViewModel.class);

        setupRecyclerView();

        noteViewModel.getAllNotes().observe(this, notes -> {
            noteAdapter.submitList(notes);
            updateEmptyState(notes);
        });

        binding.fabAddNote.setOnClickListener(v -> {
            if (!isSelectionMode) {
                openNoteDetailActivity(null);
            } else {
                Toast.makeText(this, "Termina la eliminación primero.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView() {
        noteAdapter = new NoteAdapter(this);
        binding.recyclerViewNotes.setAdapter(noteAdapter);
    }

    private void updateEmptyState(List<Note> notes) {
        if (notes == null || notes.isEmpty()) {
            binding.textViewEmptyState.setVisibility(View.VISIBLE);
            binding.recyclerViewNotes.setVisibility(View.GONE);
        } else {
            binding.textViewEmptyState.setVisibility(View.GONE);
            binding.recyclerViewNotes.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_delete_notes) {
            handleDeleteAction();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onNoteDoubleClick(Note note) {
        if (!isSelectionMode) {
            openNoteDetailActivity(note);
        }
    }

    @Override
    public void onNoteClick(Note note, boolean isChecked) {
        if (isSelectionMode) {
            if (isChecked) {
                selectedNotes.add(note);
            } else {
                selectedNotes.remove(note);
            }
        }
    }

    @Override
    public void onNoteLongClick(Note note) {
        if (!isSelectionMode) {
            enterSelectionMode();
            selectedNotes.add(note);
        }
    }

    private void openNoteDetailActivity(Note note) {
        Intent intent = new Intent(this, NoteDetailActivity.class);
        if (note != null) {
            intent.putExtra("NOTE_ID", note.id);
        }
        startActivity(intent);
    }

    private void enterSelectionMode() {
        isSelectionMode = true;
        selectedNotes.clear();
        noteAdapter.setSelectionMode(true);
        getSupportActionBar().setTitle("0 Notas Seleccionadas");
    }

    private void exitSelectionMode() {
        isSelectionMode = false;
        selectedNotes.clear();
        noteAdapter.setSelectionMode(false);
        getSupportActionBar().setTitle(R.string.app_name_title);
    }

    private void handleDeleteAction() {
        if (!isSelectionMode || selectedNotes.isEmpty()) {
            Toast.makeText(this, "Selecciona una o más notas para eliminar.", Toast.LENGTH_SHORT).show();
            return;
        }

        Snackbar.make(binding.getRoot(),
                        "¿Estás seguro de eliminar " + selectedNotes.size() + " nota(s)?",
                        Snackbar.LENGTH_LONG)
                .setAction("CONFIRMAR", v -> confirmDeletion())
                .show();
    }

    private void confirmDeletion() {
        CompletableFuture.runAsync(() -> {
            for (Note note : selectedNotes) {
                AppDatabase.getDatabase(this).noteDao().delete(note);
            }
        }).thenRun(() -> {
            runOnUiThread(() -> {
                Toast.makeText(this, "Nota(s) eliminada(s).", Toast.LENGTH_SHORT).show();
                exitSelectionMode();
            });
        });
    }
}
