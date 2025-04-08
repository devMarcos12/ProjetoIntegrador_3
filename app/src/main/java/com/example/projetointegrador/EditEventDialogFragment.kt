package com.example.projetointegrador

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class EditEventDialogFragment(
    private val event: Event,
    private val onSave: (Event) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_event, null)
        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etDescription = view.findViewById<EditText>(R.id.etDescription)
        val btnSave = view.findViewById<Button>(R.id.btnSave)

        etTitle.setText(event.title)
        etDescription.setText(event.description)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .create()

        btnSave.setOnClickListener {
            val updatedEvent = event.copy(
                title = etTitle.text.toString(),
                description = etDescription.text.toString()
            )
            onSave(updatedEvent)
            dialog.dismiss()
        }

        return dialog
    }
}
