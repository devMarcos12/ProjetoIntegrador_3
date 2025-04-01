package com.example.projetointegrador

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Register_Incident : AppCompatActivity() {

    private val PICK_IMAGE = 1
    private val TAKE_PHOTO = 2

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

        val imageView = findViewById<ImageView>(R.id.imgIllustration)
        val btnUpload = findViewById<Button>(R.id.btnUploadFile)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE -> {
                    val selectedImage = data?.data

                    btnUpload.text = "Image Selected"
                }
                TAKE_PHOTO -> {
                    val imageBitmap = data?.extras?.get("data") as? Bitmap
                    imageBitmap?.let {

                        btnUpload.text = "Image Selected"
                    }
                }
            }
        }
    }}
