package com.example.google_drive_api_demo.ui.image

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.google_drive_api_demo.R
import com.example.google_drive_api_demo.api.DriveAPIProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ImageFragment : Fragment() {

    companion object {
        private const val FILE_ID_KEY = "FILE_ID"

        fun createBundle(fileId: String) = Bundle().also {
            it.putString(FILE_ID_KEY, fileId)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fileId = requireArguments().getString(FILE_ID_KEY) ?: ""
        val imageView = view.findViewById<ImageView>(R.id.downloaded_image)
        fetchImage(fileId, imageView)
    }

    private fun fetchImage(fileId: String, imageView: ImageView) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val bitmap = DriveAPIProvider.downloadImage(fileId)
                imageView.setImageBitmap(bitmap)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), e.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }
}