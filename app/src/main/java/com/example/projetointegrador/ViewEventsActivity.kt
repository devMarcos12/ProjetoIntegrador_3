package com.example.projetointegrador

import android.os.Bundle
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

class ViewEventsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_events)

        window.statusBarColor = ContextCompat.getColor(this, R.color.button_enabled)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register_incident)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnAddEvent = findViewById<Button>(R.id.btnAddEvent)
        btnAddEvent.setOnClickListener {
            finish()
        }

        // Dados mockados
        val mockEvents = listOf(
            Event("Broken Projector", "Projector in room 103 not working", R.drawable.projetor),
            Event("Loose Cable", "Cable near entrance is loose", R.drawable.projetor),
            Event("No Board Marker", "No marker available in lab 204", R.drawable.projetor)
        )

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerEvents)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = EventAdapter(
            mockEvents,
            onEditClick = { event ->
                val dialog = EditEventDialogFragment(event) { updatedEvent ->
                    Toast.makeText(this, "Updated: ${updatedEvent.title}", Toast.LENGTH_SHORT).show()
                    // Aqui você atualizaria a lista e notificaria o adapter se necessário
                }
                dialog.show(supportFragmentManager, "editEvent")
            },
            onDeleteClick = { event ->
                Toast.makeText(this, "Delete: ${event.title}", Toast.LENGTH_SHORT).show()
            }
        )

        val tvTotal = findViewById<TextView>(R.id.tvTotal)
        tvTotal.text = "Total: ${mockEvents.size} Events"
    }
}
