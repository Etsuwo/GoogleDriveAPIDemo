package com.example.google_drive_api_demo.api

import android.content.Intent
import android.widget.Toast
import com.example.google_drive_api_demo.R
import com.example.google_drive_api_demo.application.GoogleDriveDemoApplication
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.auth.openidconnect.IdTokenResponse.execute
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
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object DriveAPIProvider {

    private lateinit var drive: Drive

    suspend fun connectDrive(intent: Intent) = suspendCoroutine<Unit> { continuation ->
        GoogleSignIn.getSignedInAccountFromIntent(intent)
            .addOnSuccessListener {
                val credential = GoogleAccountCredential.usingOAuth2(
                    GoogleDriveDemoApplication.app.applicationContext, Collections.singleton(DriveScopes.DRIVE_READONLY)
                )
                credential.selectedAccount = it.account
                drive = Drive.Builder(
                    NetHttpTransport(),
                    GsonFactory(),
                    credential
                ).setApplicationName(GoogleDriveDemoApplication.app.applicationContext.getString(R.string.app_name)).build()
                continuation.resume(Unit)
            }
            .addOnFailureListener {
                continuation.resumeWithException(FailedToConnectException())
            }
    }

    // rootはマイドライブのルートフォルダIDのエイリアス
    // https://developers.google.com/drive/api/guides/search-files
    suspend fun queryFiles(): FileList = withContext(Dispatchers.IO) {
        drive.let {
            return@withContext it.files().list().setSpaces("drive").setQ("'root' in parents").execute()
        }
    }
}