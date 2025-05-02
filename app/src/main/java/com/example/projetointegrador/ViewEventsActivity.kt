package com.example.projetointegrador

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ViewEventsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val incidentes = mutableListOf<Event>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvTotal: TextView
    private lateinit var adapter: EventAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_events)

        window.statusBarColor = ContextCompat.getColor(this, R.color.button_enabled)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register_incident)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnAddEvent = findViewById<Button>(R.id.btnAddEvent)
        btnAddEvent.setOnClickListener {
            finish()
        }

        tvTotal = findViewById<TextView>(R.id.tvTotal)
        recyclerView = findViewById(R.id.recyclerEvents)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = EventAdapter(
            incidentes,
            onEditClick = { event ->
                val dialog = EditEventDialogFragment(event) { updatedEvent ->
                    atualizarDocIncidente(updatedEvent)
                }
                dialog.show(supportFragmentManager, "editEvent")
            },
            onDeleteClick = { event ->
                deletarDocIncidente(event)
            }
        )

        recyclerView.adapter = adapter

        preencherListaIncidentes()
    }

    private fun deletarDocIncidente(incidente: Event) {
        val idUsuarioLogado = auth.currentUser?.uid

        if (idUsuarioLogado != null) {
            val referenciaIncidente = db
                .collection("Usuarios")
                .document(idUsuarioLogado)
                .collection("Incidentes")
                .document(incidente.docId)

            referenciaIncidente.delete()
                .addOnSuccessListener {
                    Log.d("Firestore", "Documento excluído com sucesso!")
                    Toast.makeText(this, "Documento excluído com sucesso!", Toast.LENGTH_SHORT)
                        .show()
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Erro ao excluir documento: ${e.message}", e)
                    Toast.makeText(this, "Erro ao excluir documento", Toast.LENGTH_SHORT)
                        .show()
                }
        }
    }

    private fun atualizarDocIncidente(incidente: Event) {
        val idUsuarioLogado = auth.currentUser?.uid

        if (idUsuarioLogado != null) {
            val referenciaIncidente = db
                .collection("Usuarios")
                .document(idUsuarioLogado)
                .collection("Incidentes")
                .document(incidente.docId)

            if (incidente.title.isEmpty() && incidente.description.isEmpty()) {
                return
            }

            val updates = mutableMapOf<String, Any>()

            if (incidente.title.isNotEmpty()) {
                updates["titulo"] = incidente.title
            }

            if (incidente.description.isNotEmpty()) {
                updates["descricao"] = incidente.description
            }

            referenciaIncidente.update(updates)
                .addOnSuccessListener {
                    Log.d("Firestore", "Documento atualizado com sucesso!")
                    Toast.makeText(
                        this,
                        "Documento atualizado com sucesso!",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Erro ao atualizar documento: ${e.message}", e)
                    Toast.makeText(this, "Erro ao atualizar documento", Toast.LENGTH_SHORT)
                        .show()
                }
        }
    }

    private fun preencherListaIncidentes() {
        val idUsuarioLogado = auth.currentUser?.uid

        if (idUsuarioLogado != null) {

            val referenciaIncidentes = db
                .collection("Usuarios")
                .document(idUsuarioLogado)
                .collection("Incidentes")

            referenciaIncidentes.addSnapshotListener { querySnapshot, erro ->
                val docsIncidentes = querySnapshot?.documents

                incidentes.clear()

                docsIncidentes?.forEach { documentSnapshot ->
                    val docIncidente = documentSnapshot?.data
                    val idDocIncidente = documentSnapshot?.id

                    if (docIncidente != null && idDocIncidente != null) {
                        val titulo = docIncidente["titulo"] as? String ?: ""
                        val descricao = docIncidente["descricao"] as? String ?: ""
                        val imagePath = R.drawable.projetor

                        incidentes.add(
                            Event(
                                idDocIncidente, titulo, descricao, imagePath
                            )
                        )
                    }
                }

                adapter.notifyDataSetChanged()

                tvTotal.text = "Total: ${incidentes.size} Events"
            }
        }
    }
}
