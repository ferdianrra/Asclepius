package com.dicoding.asclepius.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper
import com.yalantis.ucrop.UCrop
import org.tensorflow.lite.task.vision.classifier.Classifications
import java.io.File
import java.text.NumberFormat

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var imageClassifierHelper: ImageClassifierHelper
    private var myResult: String = ""

    private var currentImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.galleryButton.setOnClickListener { startGallery() }
        binding.analyzeButton.setOnClickListener { if (currentImageUri != null ) {
            moveToResult()
            }   else{
                showToast("Harap Masukkan foto terlebih dahulu!")
            }
        }
        binding.previewImageView.setImageURI(currentImageUri)
        binding.previewImageView.setOnClickListener {
            showImage()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri = UCrop.getOutput(data!!)
            currentImageUri = resultUri
            binding.previewImageView.setImageURI(currentImageUri)
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!).toString()
            showToast(cropError)
        }
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun showImage() {
       currentImageUri?.let {
           Log.d("Image URI", "showImage: $it")
           val destinationUri = Uri.fromFile(File(cacheDir, "cropped_image.jpg"))
           UCrop.of(it, destinationUri)
               .withAspectRatio(16f, 9f)
               .withMaxResultSize(1000, 1000)
               .start(this);
           binding.previewImageView.setImageURI(it)

       }
    }

    private fun analyzeImage() {
        // TODO: Menganalisa gambar yang berhasil ditampilkan.
        imageClassifierHelper = ImageClassifierHelper(
            context = this,
            classifierListener = object : ImageClassifierHelper.ClassifierListener {
                override fun onError(error: String) {
                    showToast(error)
                }

                override fun onResult(result: List<Classifications>?) {
                    result?.let { it ->
                        if (it.isNotEmpty() && it[0].categories.isNotEmpty()) {
                            println(it)
                            val categories = it[0].categories.sortedByDescending { it?.score }
                            val displayResult = categories.joinToString("\n") {
                                "Hasil Analisis: ${it.label} " + "\nTingkat Akurasi: " +NumberFormat.getPercentInstance()
                                    .format(it.score).trim()
                            }
                            myResult = displayResult

                        }
                    }
                }
            })
        currentImageUri?.let { imageClassifierHelper.classifyStaticImage(it) }
    }

    private fun moveToResult() {
        analyzeImage()
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra(EXTRA_URI, currentImageUri.toString())
        intent.putExtra(EXTRA_RESULT, myResult)
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showImage()
        } else {
            Log.d("Photo Picker", "Tidak ada media yang dipilih")
        }
    }

    companion object {
        const val EXTRA_RESULT = "extra_result"
        const val EXTRA_URI = "extra_uri"
    }
}