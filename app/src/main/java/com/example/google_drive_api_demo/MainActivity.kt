package com.example.google_drive_api_demo

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
    private var drive: Drive? = null

    // Googleアカウントへのログイン（認証）
    private val handleLoginResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK && it.data != null) {
            //Toast.makeText(this, "ログイン成功！", Toast.LENGTH_LONG).show()
            connectDrive(it.data!!)
        } else {
            Toast.makeText(this, "ログイン失敗...", Toast.LENGTH_LONG).show()
        }
    }

    // Googleアカウントからの情報取得の許可（認可）
    private val handleRecoverableResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "認可完了", Toast.LENGTH_LONG).show()
            queryFiles()
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
        GoogleSignIn.getSignedInAccountFromIntent(intent)
            .addOnSuccessListener {
                val credential = GoogleAccountCredential.usingOAuth2(
                    this, Collections.singleton(DriveScopes.DRIVE_READONLY)
                )
                credential.selectedAccount = it.account
                drive = Drive.Builder(
                    NetHttpTransport(),
                    GsonFactory(),
                    credential
                ).setApplicationName(getString(R.string.app_name)).build()
                queryFiles()
            }
            .addOnFailureListener {
                Toast.makeText(this, "アクセス失敗だよ〜", Toast.LENGTH_LONG).show()
            }
    }

    private fun queryFiles() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val fileList = execute()
                val builder = StringBuilder()
                fileList.files.forEach {
                    builder.append(it.name).append("\n")
                }
                Toast.makeText(this@MainActivity, builder.toString(), Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                when (e) {
                    is UserRecoverableAuthIOException -> { // Driveアクセスの認可がない
                        handleRecoverableResult.launch(e.intent)
                    }
                    else -> Toast.makeText(this@MainActivity, e.toString(), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // rootはマイドライブのルートフォルダIDのエイリアス
    // https://developers.google.com/drive/api/guides/search-files
    private suspend fun execute(): FileList = withContext(Dispatchers.IO) {
        drive?.let {
            return@withContext it.files().list().setSpaces("drive").setQ("'root' in parents").execute()
        } ?: run { throw IllegalAccessException() }
    }
}