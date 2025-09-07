package tyam.camachopichal.actividades.prueba1

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.lifecycle.ViewModelProvider


import tyam.camachopichal.actividades.prueba1.databinding.ActivityMainBinding
import tyam.camachopichal.actividades.prueba1.viewmodel.NotasViewModel

class MainActivity : AppCompatActivity() {
    private val notasPrefs: String = "NotasPrefs"
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: NotasViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this)[NotasViewModel::class.java]

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        // Cargar notas guardadas de SharedPreferences al iniciar
        loadPreferences()
        updateUI()

        // Botón Guardar
        binding.buttonGuardar.setOnClickListener {
            val nota = binding.editTextNota.text.toString()
            if (nota.isNotBlank()) {
                viewModel.agregarNota(nota)
                binding.editTextNota.text.clear()
                updateUI()
            }
        }

        // Botón Limpiar
        binding.buttonLimpiar.setOnClickListener {
            viewModel.limpiarNotas()
            updateUI()
        }
    }

    override fun onPause() {
        super.onPause()
        // keep notes on SharedPreferences when activiry is paused
        saveOnSharedPreferences()
    }

    private fun updateUI() {
        binding.textViewCantNotas.text = viewModel.cantidadNotas().toString()
        binding.textViewListadoNotas.text = viewModel.listadoNotas()
    }

    private fun saveOnSharedPreferences() {
        val prefs = getSharedPreferences(notasPrefs, Context.MODE_PRIVATE)
        prefs.edit {
            putStringSet("notas", viewModel.notas.toSet())
            apply()
        }
    }

    private fun loadPreferences() {
        val prefs = getSharedPreferences(notasPrefs, Context.MODE_PRIVATE)
        val savedNotes = prefs.getStringSet("notas", emptySet()) ?: emptySet()
        savedNotes.sortedBy { it.substringBefore('.') }.forEach { nota -> viewModel.notas.add(nota) }
        viewModel.notas.clear()
        viewModel.notas.addAll(savedNotes.sorted())
    }
}
