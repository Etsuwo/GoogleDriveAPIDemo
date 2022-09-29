package com.example.google_drive_api_demo.ui.folder

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.google_drive_api_demo.R
import com.example.google_drive_api_demo.api.DriveAPIProvider
import com.example.google_drive_api_demo.ui.image.ImageFragment
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FolderFragment : Fragment() {

    lateinit var recyclerView: RecyclerView

    private val handleRecoverableResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            Toast.makeText(requireContext(), "認可完了", Toast.LENGTH_LONG).show()
            fetchFiles()
        }
    }

    companion object {
        private const val FILE_MIME_TYPE = "image/jpeg"

        private const val FOLDER_ID_KEY = "FOLDER_ID"
        fun createBundle(folderId: String) = Bundle().also {
            it.putString(FOLDER_ID_KEY, folderId)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_folder, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recycler_view)

        fetchFiles()
    }

    private fun loadFolderId(): String {
        return try {
            requireArguments().getString(FOLDER_ID_KEY) ?: "root"
        } catch (e: Exception) {
            "root"
        }
    }

    private fun fetchFiles() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val folderId = loadFolderId()
                val adapter = RecyclerViewAdapter(emptyList()) {
                    if (it.mimeType == FILE_MIME_TYPE) {
                        val args = ImageFragment.createBundle(it.id)
                        findNavController().navigate(R.id.image_fragment, args)
                    } else {
                        val args = createBundle(it.id)
                        findNavController().navigate(R.id.folder_fragment, args)
                    }
                }
                recyclerView.adapter = adapter
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
                val fileList = DriveAPIProvider.queryFiles(folderId)
                val driveItemList = fileList.files.map { file ->
                    val thumbnail = if (file.hasThumbnail) file.thumbnailLink else file.iconLink
                    DriveItem(file.id, file.name, file.mimeType, thumbnail)
                }
                adapter.list = driveItemList
                adapter.notifyDataSetChanged()
            } catch (e: Exception) {
                when (e) {
                    is UserRecoverableAuthIOException -> { // Driveアクセスの認可がない
                        handleRecoverableResult.launch(e.intent)
                    }
                    else -> Toast.makeText(requireContext(), e.toString(), Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}