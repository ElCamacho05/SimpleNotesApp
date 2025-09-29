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
import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;

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

        // 3. Configurar RecyclerView
        setupRecyclerView();

        // Observar cambios en la lista de notas de la BD (Room + LiveData)
        noteViewModel.getAllNotes().observe(this, notes -> {
            noteAdapter.submitList(notes);
            updateEmptyState(notes);
        });

        // 4. Configurar el FAB (Agregar Nota)
        binding.fabAddNote.setOnClickListener(v -> {
            if (!isSelectionMode) {
                openNoteDetailActivity(null); // Abre la actividad para una nota nueva
            } else {
                // Si está en modo selección, el FAB puede tener otra acción o simplemente ignorarse
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

    // --- Lógica del Menú (Toolbar) ---

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflar el menú que contendrá la acción de "Eliminar"
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

    // --- Implementación de NoteItemListener ---

    // Maneja el DOBLE CLIC (para Editar)
    @Override
    public void onNoteDoubleClick(Note note) {
        if (!isSelectionMode) {
            openNoteDetailActivity(note);
        }
    }

    // Maneja el CLICK NORMAL (para la Selección/Eliminación)
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

    // Maneja el LONG CLICK (para entrar en Modo Selección)
    @Override
    public void onNoteLongClick(Note note) {
        if (!isSelectionMode) {
            enterSelectionMode();
            // Asegurarse de que el elemento que recibió el long click quede seleccionado
            selectedNotes.add(note);
        }
    }

    // --- Flujos de Navegación y Acciones ---

    private void openNoteDetailActivity(Note note) {
        Intent intent = new Intent(this, NoteDetailActivity.class);
        if (note != null) {
            // Pasar ID si es para editar
            intent.putExtra("NOTE_ID", note.id);
        }
        startActivity(intent);
    }

    private void enterSelectionMode() {
        isSelectionMode = true;
        selectedNotes.clear();
        noteAdapter.setSelectionMode(true);
        updateSelectionTitle(); // Muestra el título inicial
    }

    private void exitSelectionMode() {
        isSelectionMode = false;
        selectedNotes.clear();
        noteAdapter.setSelectionMode(false);
        getSupportActionBar().setTitle(R.string.app_name_title);
    }

    // FUNCIÓN PARA ACTUALIZAR EL TÍTULO DEL CONTADOR
    private void updateSelectionTitle() {
        int count = selectedNotes.size();
        if (count == 0) {
            getSupportActionBar().setTitle("Seleccionar notas");
        } else {
            getSupportActionBar().setTitle(count + " nota(s) seleccionada(s)");
        }
    }

    // FUNCIÓN PARA MANEJAR EL BOTÓN DE RETROCESO
    private void setupOnBackPressed() {
        OnBackPressedDispatcher dispatcher = getOnBackPressedDispatcher();
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Si estamos en modo de selección, lo cancelamos
                if (isSelectionMode) {
                    exitSelectionMode();
                } else {
                    // Si no, permitimos el comportamiento normal (cerrar la app)
                    setEnabled(false); // Desactiva este callback
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        };
        dispatcher.addCallback(this, callback);
    }


    private void handleDeleteAction() {
        if (!isSelectionMode || selectedNotes.isEmpty()) {
            // Requisito: Si no hay selección, no hacer nada.
            Toast.makeText(this, "Selecciona una o más notas para eliminar.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Requisito: Preguntar al usuario si está seguro (usando Snackbar)
        Snackbar.make(binding.getRoot(),
                        "¿Estás seguro de eliminar " + selectedNotes.size() + " nota(s)?",
                        Snackbar.LENGTH_LONG)
                .setAction("CONFIRMAR", v -> confirmDeletion())
                .show();
    }

    private void confirmDeletion() {
        // Ejecutar la eliminación en un hilo secundario
        CompletableFuture.runAsync(() -> {
            for (Note note : selectedNotes) {
                AppDatabase.getDatabase(this).noteDao().delete(note);
            }
        }).thenRun(() -> {
            // Requisito: Actualizar la lista (LiveData lo hace automáticamente), pero salir del modo selección.
            runOnUiThread(() -> {
                Toast.makeText(this, "Nota(s) eliminada(s).", Toast.LENGTH_SHORT).show();
                exitSelectionMode();
            });
        });
    }
}
