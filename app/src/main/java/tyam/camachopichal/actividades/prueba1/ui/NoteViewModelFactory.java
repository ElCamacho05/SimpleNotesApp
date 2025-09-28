package tyam.camachopichal.actividades.prueba1.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class NoteViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;

    public NoteViewModelFactory(Application application) {
        this.application = application;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        // SOLUCIÓN: Usar la Factoría de forma limpia.
        if (modelClass.isAssignableFrom(NoteViewModel.class)) {
            // Llama directamente al constructor del NoteViewModel, pasando el objeto Application.
            // Esto evita problemas de Reflection y es la forma estándar de hacerlo.
            return (T) new NoteViewModel(application);
        }
        throw new IllegalArgumentException("Clase de ViewModel desconocida: " + modelClass.getName());
    }
}
