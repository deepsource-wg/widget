package com.example.deepsource.data

import android.content.Context
import android.content.MutableContextWrapper
import androidx.credentials.*
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.deepsource.BuildConfig
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

object GoogleAuth {
    val available=true
    suspend fun token(activity:Context):String {
        try {
            val option=GetSignInWithGoogleOption.Builder(BuildConfig.GOOGLE_WEB_CLIENT_ID).build()
            val result=CredentialManager.create(activity).getCredential(MutableContextWrapper(activity),GetCredentialRequest.Builder().addCredentialOption(option).build())
            val credential=result.credential
            require(credential is CustomCredential && credential.type==GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)
            return GoogleIdTokenCredential.createFrom(credential.data).idToken
        } catch(_:GetCredentialCancellationException) {throw AuthCancelled()}
    }
    suspend fun clear(context:Context) {CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())}
}
