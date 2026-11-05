package com.example.deepsource.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.example.deepsource.domain.Session
import org.json.JSONObject
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Tokens are encrypted with an Android Keystore key; passwords are never persisted. */
class SessionVault(context: Context) {
    private val prefs = context.getSharedPreferences("session_vault",Context.MODE_PRIVATE)
    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        return (store.getKey("deepsource_session",null) as? SecretKey) ?: KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore").run {
            init(KeyGenParameterSpec.Builder("deepsource_session",KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
            generateKey()
        }
    }
    fun save(s: Session?) {
        if(s==null) { check(prefs.edit().clear().commit()); return }
        val json = JSONObject().put("uid",s.uid).put("name",s.name).put("email",s.email).put("role",s.role)
            .put("demo",s.demo).put("provider",s.provider).put("token",s.token).put("refresh",s.refreshToken).put("expires",s.expiresAt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE,key()) }
        val encrypted = cipher.doFinal(json.toString().toByteArray(Charsets.UTF_8))
        check(prefs.edit().putString("data",Base64.encodeToString(encrypted,Base64.NO_WRAP))
            .putString("iv",Base64.encodeToString(cipher.iv,Base64.NO_WRAP)).commit()) { "No pudimos guardar la sesión" }
    }
    fun read(): Session? = try {
        val data = prefs.getString("data",null)
        if(data==null) null else {
            val iv = Base64.decode(prefs.getString("iv",null),Base64.NO_WRAP)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.DECRYPT_MODE,key(),GCMParameterSpec(128,iv)) }
            val j = JSONObject(String(cipher.doFinal(Base64.decode(data,Base64.NO_WRAP)),Charsets.UTF_8))
            Session(j.getString("uid"),j.getString("name"),j.getString("email"),j.getString("role"),
                j.getBoolean("demo"),j.getString("provider"),j.getString("token"),j.getString("refresh"),j.getLong("expires"))
        }
    } catch(_: Exception) { prefs.edit().clear().commit(); null }
}
