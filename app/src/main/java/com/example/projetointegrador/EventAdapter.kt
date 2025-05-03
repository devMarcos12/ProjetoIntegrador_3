package com.example.projetointegrador

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class EventAdapter(
    private val events: List<Event>,
    private val onEditClick: (Event) -> Unit,
    private val onDeleteClick: (Event) -> Unit
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgEvent: ImageView = itemView.findViewById(R.id.imgEvent)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]

        // Exibir o Bitmap no ImageView
        if (event.imageBitmap != null) {
            holder.imgEvent.setImageBitmap(event.imageBitmap)
        } else {
            // Exibir uma imagem padrão caso o Bitmap seja nulo
            holder.imgEvent.setImageResource(R.drawable.projetor)
        }
        // Configurar título e descrição
        holder.tvTitle.text = event.title
        holder.tvDescription.text = event.description

        // Configurar os botões de editar e excluir
        holder.btnEdit.setOnClickListener { onEditClick(event) }
        holder.btnDelete.setOnClickListener { onDeleteClick(event) }
    }

    override fun getItemCount(): Int = events.size
}