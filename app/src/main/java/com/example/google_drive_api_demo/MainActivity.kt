package com.example.google_drive_api_demo

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.google_drive_api_demo.api.DriveAPIProvider
import com.example.google_drive_api_demo.ui.folder.FolderActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.FileList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Collections

class MainActivity : AppCompatActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient

    // Googleアカウントへのログイン（認証）
    private val handleLoginResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK && it.data != null) {
            //Toast.makeText(this, "ログイン成功！", Toast.LENGTH_LONG).show()
            connectDrive(it.data!!)
        } else {
            Toast.makeText(this, "ログイン失敗...", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val gso = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        setContentView(R.layout.activity_main)
        setupUI()
    }

    private fun setupUI() {
        findViewById<SignInButton>(R.id.button).setOnClickListener { signInWithGoogle() }
    }

    private fun signInWithGoogle() {
        handleLoginResult.launch(googleSignInClient.signInIntent)
    }

    private fun connectDrive(intent: Intent) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                DriveAPIProvider.connectDrive(intent)
                val intent = Intent(this@MainActivity, FolderActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, e.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }
}