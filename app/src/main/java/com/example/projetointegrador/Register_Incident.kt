package com.example.projetointegrador

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import br.com.alura.orgs.model.Incident
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class Register_Incident : AppCompatActivity() {

    private val PICK_IMAGE = 1
    private val TAKE_PHOTO = 2

    private var selectedImageUri: android.net.Uri? = null
    private var capturedBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register_incident)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register_incident)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Botão "View Events" - navega para outra activity
        val btnViewEvents = findViewById<Button>(R.id.btnViewEvents)
        btnViewEvents.setOnClickListener {
            val intent = Intent(this, ViewEventsActivity::class.java)
            startActivity(intent)
        }

        // Botão "Click to Upload" - câmera ou galeria
        val btnUpload = findViewById<Button>(R.id.btnUploadFile)
        btnUpload.setOnClickListener {
            showImagePickerDialog()
        }

        val btnRegisterEvent = findViewById<Button>(R.id.btnRegisterEvent)
        btnRegisterEvent.setOnClickListener {
            val title = findViewById<EditText>(R.id.etTitle).text.toString().trim()
            val description = findViewById<EditText>(R.id.etDescription).text.toString().trim()

            if (title.isEmpty() || description.isEmpty()) {
                AlertDialog.Builder(this)
                    .setTitle("Validation Error")
                    .setMessage("Please fill in both title and description.")
                    .setPositiveButton("OK", null)
                    .show()
                return@setOnClickListener
            }

            if (capturedBitmap != null) {
                uploadImageAndSaveIncident(title, description, capturedBitmap!!)
            } else {
                saveIncidentToFirestore(title, description, null)
            }
        }
    }

    private fun uploadImageAndSaveIncident(title: String, description: String, bitmap: Bitmap) {
        val storage = FirebaseStorage.getInstance()
        val imageRef = storage.reference.child("incident_images/${System.currentTimeMillis()}.jpg")

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageData = baos.toByteArray()

        imageRef.putBytes(imageData)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    saveIncidentToFirestore(title, description, imageUrl)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Register_Incident", "Error uploading image", e)
            }
    }


    private fun saveIncidentToFirestore(title: String, description: String, imageUrl: String?) {
        val db = FirebaseFirestore.getInstance()

        val incidentData = hashMapOf(
            "title" to title,
            "description" to description,
            "imageUrl" to imageUrl,
            "createdAt" to com.google.firebase.Timestamp.now()
        )

        db.collection("incidents")
            .add(incidentData)
            .addOnSuccessListener { documentReference ->
                Log.d("Register_Incident", "Incident saved with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.e("Register_Incident", "Error saving incident", e)
            }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Image")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> {
                    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(takePictureIntent, TAKE_PHOTO)
                }
                1 -> {
                    val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(pickPhoto, PICK_IMAGE)
                }
            }
        }
        builder.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val btnUpload = findViewById<Button>(R.id.btnUploadFile)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE -> {
                    selectedImageUri = data?.data
                    capturedBitmap = null

                    selectedImageUri?.let { uri ->
                        val inputStream = contentResolver.openInputStream(uri)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        capturedBitmap = bitmap
                        inputStream?.close()
                    }

                    btnUpload.text = "Image Selected"
                }

                TAKE_PHOTO -> {
                    capturedBitmap = data?.extras?.get("data") as? Bitmap
                    selectedImageUri = null

                    capturedBitmap?.let {
                        btnUpload.text = "Image Selected"
                    }
                }
            }
        }
    }
}
