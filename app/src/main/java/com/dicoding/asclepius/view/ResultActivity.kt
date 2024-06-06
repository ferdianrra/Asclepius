package com.dicoding.asclepius.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.net.toUri
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.asclepius.R
import com.dicoding.asclepius.data.response.ArticlesItem
import com.dicoding.asclepius.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding
    private val resultViewModel by viewModels<ResultViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val layoutManager = LinearLayoutManager(this)
        binding.newsRvView.layoutManager = layoutManager
        val itemDecoration = DividerItemDecoration(this, layoutManager.orientation)
        binding.newsRvView.addItemDecoration(itemDecoration)

        resultViewModel.listNews.observe(this) {ReviewNews ->
            setReviewNews(ReviewNews)
        }

        resultViewModel.isLoading.observe(this) {loading ->
            showLoading(loading)
        }

        // TODO: Menampilkan hasil gambar, prediksi, dan confidence score.
        val uri = intent.getStringExtra(EXTRA_URI)?.toUri()
        val  result = intent.getStringExtra(EXTRA_RESULT)
        binding.resultImage.setImageURI(uri)
        binding.resultText.text = result
    }

    private fun showLoading(loading: Boolean) {
        binding.progressBars.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun setReviewNews(reviewNews: List<ArticlesItem>) {
        val adapter = NewsAdapter()
        adapter.submitList(reviewNews)
        binding.newsRvView.adapter = adapter
    }

    companion object {
        const val EXTRA_RESULT = "extra_result"
        const val EXTRA_URI = "extra_uri"
    }
}