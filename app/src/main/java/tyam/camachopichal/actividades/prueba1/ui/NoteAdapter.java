package tyam.camachopichal.actividades.prueba1.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import tyam.camachopichal.actividades.prueba1.R;
import tyam.camachopichal.actividades.prueba1.db.Note;

public class NoteAdapter extends ListAdapter<Note, NoteAdapter.NoteViewHolder> {

    private final NoteItemListener listener;
    private boolean isSelectionMode = false;

    // Interfaz para manejar los clics y el doble clic
    public interface NoteItemListener {
        void onNoteDoubleClick(Note note);
        void onNoteClick(Note note, boolean isChecked);
        void onNoteLongClick(Note note);
    }

    public NoteAdapter(@NonNull NoteItemListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    public void setSelectionMode(boolean isSelectionMode) {
        this.isSelectionMode = isSelectionMode;
        notifyDataSetChanged(); // Forzar la actualización para mostrar/ocultar CheckBox
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note currentNote = getItem(position);
        holder.bind(currentNote, listener, isSelectionMode);
    }

    // Clase ViewHolder
    static class NoteViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleTextView;
        private final TextView timestampTextView;
        private final CheckBox selectCheckBox;
        private final CardView noteCard;

        private long lastClickTime = 0;
        private static final long DOUBLE_CLICK_TIME_DELTA = 300; // 300ms

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            // Referencias a las vistas del item_note.xml
            titleTextView = itemView.findViewById(R.id.text_view_title);
            timestampTextView = itemView.findViewById(R.id.text_view_timestamp);
            selectCheckBox = itemView.findViewById(R.id.checkbox_select);
            noteCard = itemView.findViewById(R.id.card_note);
        }

        public void bind(Note note, NoteItemListener listener, boolean isSelectionMode) {
            titleTextView.setText(note.title);
            timestampTextView.setText("Modificado: " + formatTimestamp(note.lastModified));

            // Lógica del modo selección
            selectCheckBox.setVisibility(isSelectionMode ? View.VISIBLE : View.GONE);
            // selectCheckBox.setChecked(false); // Resetear estado

            // --- Manejo de Eventos (Click, Double Click, Long Click) ---

            noteCard.setOnClickListener(v -> {
                long clickTime = System.currentTimeMillis();

                if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                    // ** Requisito: Doble Clic (para editar) **
                    listener.onNoteDoubleClick(note);
                    lastClickTime = 0; // Resetear
                } else {
                    // ** Requisito: Clic normal (para seleccionar/eliminar) **
                    if (isSelectionMode) {
                        selectCheckBox.setChecked(!selectCheckBox.isChecked());
                        listener.onNoteClick(note, selectCheckBox.isChecked());
                    }
                    lastClickTime = clickTime;
                }
            });

            noteCard.setOnLongClickListener(v -> {
                // ** Requisito: Long Clic (para entrar en modo selección) **
                if (!isSelectionMode) {
                    listener.onNoteLongClick(note);
                    selectCheckBox.setChecked(true); // Seleccionar inmediatamente
                }
                return true; // Consumir el evento
            });

            selectCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                // Solo notificar si estamos en modo selección
                if (isSelectionMode) {
                    listener.onNoteClick(note, isChecked);
                }
            });
        }

        private String formatTimestamp(String rawTimestamp) {
            try {
                SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date date = originalFormat.parse(rawTimestamp);

                SimpleDateFormat targetFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
                return targetFormat.format(date);
            } catch (ParseException e) {
                return rawTimestamp;
            }
        }
    }

    // Callback para optimizar la actualización de la lista
    private static final DiffUtil.ItemCallback<Note> DIFF_CALLBACK = new DiffUtil.ItemCallback<Note>() {
        @Override
        public boolean areItemsTheSame(@NonNull Note oldItem, @NonNull Note newItem) {
            return oldItem.id.equals(newItem.id);
        }

        @Override
        public boolean areContentsTheSame(@NonNull Note oldItem, @NonNull Note newItem) {
            return oldItem.title.equals(newItem.title) &&
                    oldItem.content.equals(newItem.content) &&
                    oldItem.lastModified.equals(newItem.lastModified);
        }
    };
}