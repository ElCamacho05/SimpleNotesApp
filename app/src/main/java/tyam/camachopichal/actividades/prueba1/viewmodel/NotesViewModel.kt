package tyam.camachopichal.actividades.prueba1.viewmodel

import androidx.lifecycle.ViewModel

class NotasViewModel : ViewModel() {
    val notas = mutableListOf<String>()

    fun agregarNota(nota: String) {
        notas.add((cantidadNotas()+1).toString()+". "+nota)
    }

    fun limpiarNotas() {
        notas.clear()
    }

    fun cantidadNotas(): Int = notas.size

    fun listadoNotas(): String = if (notas.isEmpty()) "Sin notas" else notas.joinToString("\n")
}
