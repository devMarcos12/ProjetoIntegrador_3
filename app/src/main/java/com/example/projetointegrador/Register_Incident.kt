package com.example.projetointegrador

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.util.Base64
import java.io.ByteArrayOutputStream
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import java.util.*


class Register_Incident : AppCompatActivity() {

    private val PICK_IMAGE = 1
    private val TAKE_PHOTO = 2

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var selectedImageUri: String? = null
    private var imageBitmap: Bitmap? = null
    private var currentLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_incident)

        window.statusBarColor = ContextCompat.getColor(this, R.color.button_enabled)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        getCurrentLocation()

        val btnViewEvents = findViewById<Button>(R.id.btnViewEvents)
        btnViewEvents.setOnClickListener {
            val intent = Intent(this, ViewEventsActivity::class.java)
            startActivity(intent)
        }

        val btnUpload = findViewById<Button>(R.id.btnUploadFile)
        btnUpload.setOnClickListener {
            showImagePickerDialog()
        }

        val btnRegisterEvent = findViewById<Button>(R.id.btnRegisterEvent)
        btnRegisterEvent.setOnClickListener {
            registerIncident()
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
                    val uri = data?.data
                    val inputStream = contentResolver.openInputStream(uri!!)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    imageBitmap = bitmap
                    selectedImageUri = bitmapToBase64(bitmap) // Converte para Base64
                    btnUpload.text = "Image Selected"
                }
                TAKE_PHOTO -> {
                    val bitmap = data?.extras?.get("data") as? Bitmap
                    if (bitmap != null) {
                        imageBitmap = bitmap
                        selectedImageUri = bitmapToBase64(bitmap) // Converte para Base64
                        btnUpload.text = "Image Selected"
                    }
                }
            }
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Solicita a localização do Ususario quando entra na página
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    currentLocation = location
                } else {
                    Toast.makeText(this, "Não foi possível obter a localização", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao obter localização: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun registerIncident() {
        val titleField = findViewById<EditText>(R.id.etTitle)
        val descriptionField = findViewById<EditText>(R.id.etDescription)

        val title = titleField.text.toString().trim()
        val description = descriptionField.text.toString().trim()

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        val incidentId = UUID.randomUUID().toString()

        if (currentLocation == null) {
            Toast.makeText(this, "Localização não disponível. Tente novamente.", Toast.LENGTH_SHORT).show()
            return
        }

        val geoPoint = GeoPoint(currentLocation!!.latitude, currentLocation!!.longitude)

        saveIncidentToFirestore(userId, incidentId, title, description, geoPoint)
    }

    private fun saveIncidentToFirestore(userId: String, incidentId: String, title: String, description: String, geoPoint: GeoPoint) {
        val incident = hashMapOf(
            "titulo" to title,
            "descricao" to description,
            "imageBase64" to selectedImageUri, // Salva a imagem como Base64
            "timestamp" to System.currentTimeMillis(),
            "localizacao" to geoPoint
        )

        db.collection("Usuarios").document(userId).collection("Incidentes").document(incidentId)
            .set(incident)
            .addOnSuccessListener {
                Toast.makeText(this, "Incidente registrado com sucesso!", Toast.LENGTH_SHORT).show()
                clearFields() // Limpar os campos após o registro
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao salvar incidente: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearFields() {
        findViewById<EditText>(R.id.etTitle).text.clear()
        findViewById<EditText>(R.id.etDescription).text.clear()
        findViewById<Button>(R.id.btnUploadFile).text = "Click to Upload"
        selectedImageUri = null
        imageBitmap = null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation()
        } else {
            Toast.makeText(this, "Permissão de localização negada", Toast.LENGTH_SHORT).show()
        }
    }
}