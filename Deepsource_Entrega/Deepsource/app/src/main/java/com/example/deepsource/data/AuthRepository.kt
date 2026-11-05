package com.example.deepsource.data

import android.content.Context
import com.example.deepsource.BuildConfig
import com.example.deepsource.domain.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URI
import java.net.URLEncoder

class AuthRepository(private val context: Context) {
    val isLocal get() = !BuildConfig.USE_FIREBASE
    val configured get() = !isLocal && BuildConfig.FIREBASE_API_KEY.isNotBlank()
    val googleConfigured get() = GoogleAuth.available && configured && BuildConfig.GOOGLE_WEB_CLIENT_ID.isNotBlank()

    suspend fun signIn(email: String, password: String): Session {
        if(isLocal) return withContext(Dispatchers.IO) {LocalAccounts(context).use {it.signIn(email,password)}}
        require(configured) { "Configura Firebase para usar una cuenta real, o entra en la demostración" }
        val result = api("signInWithPassword",JSONObject().put("email",email.trim()).put("password",password).put("returnSecureToken",true))
        return toSession(result)
    }

    suspend fun google(activity: Context): Session {
        require(googleConfigured) { "El acceso con Google requiere configurar el cliente OAuth y Firebase. Revisa INSTRUCCIONES.md" }
        val idToken = GoogleAuth.token(activity)
        // Firebase validates the Google ID token server-side before a session is accepted.
        return toSession(api("signInWithIdp",JSONObject()
            .put("postBody","id_token=${encode(idToken)}&providerId=google.com")
            .put("requestUri","http://localhost").put("returnSecureToken",true)),"Google")
    }

    private fun toSession(j: JSONObject, provider: String = "Correo") = Session(
        uid=j.getString("localId"),name=j.optString("displayName").ifBlank { j.getString("email").substringBefore('@') },
        email=j.getString("email"),provider=provider,token=j.getString("idToken"),refreshToken=j.getString("refreshToken"),
        expiresAt=System.currentTimeMillis()+j.optLong("expiresIn",3600)*1000)

    suspend fun resetPassword(email: String) {
        require(configured) { "La recuperación de contraseña requiere configurar Firebase" }
        api("sendOobCode",JSONObject().put("requestType","PASSWORD_RESET").put("email",email.trim()))
    }

    suspend fun updateProfile(session: Session, name: String): Session {
        if(session.demo) return session.copy(name=name.trim())
        if(isLocal) {withContext(Dispatchers.IO) {LocalAccounts(context).use {it.updateName(session,name)}};return session.copy(name=name.trim())}
        val current = refresh(session)
        val result = api("update",JSONObject().put("idToken",current.token).put("displayName",name.trim()).put("returnSecureToken",true))
        return current.copy(name=name.trim(),token=result.optString("idToken",current.token),
            refreshToken=result.optString("refreshToken",current.refreshToken),expiresAt=System.currentTimeMillis()+3600000)
    }

    private suspend fun refresh(s: Session): Session {
        if(s.expiresAt > System.currentTimeMillis()+60000) return s
        val j = post("https://securetoken.googleapis.com/v1/token?key=${BuildConfig.FIREBASE_API_KEY}",
            "grant_type=refresh_token&refresh_token=${encode(s.refreshToken)}","application/x-www-form-urlencoded")
        return s.copy(token=j.getString("id_token"),refreshToken=j.getString("refresh_token"),
            expiresAt=System.currentTimeMillis()+j.optLong("expires_in",3600)*1000)
    }

    suspend fun register(name:String,email:String,password:String):Session {
        require(isLocal) {"Las cuentas de empresa se habilitan por el administrador"}
        return withContext(Dispatchers.IO) {LocalAccounts(context).use {it.register(name,email,password)}}
    }
    suspend fun changePassword(session:Session,current:String,next:String) {
        require(isLocal && !session.demo) {"Esta función está disponible para cuentas locales"}
        withContext(Dispatchers.IO) {LocalAccounts(context).use {it.changePassword(session,current,next)}}
    }
    suspend fun signOut() { if(GoogleAuth.available) runCatching {GoogleAuth.clear(context)} }

    private suspend fun api(action: String, body: JSONObject): JSONObject = post(
        "https://identitytoolkit.googleapis.com/v1/accounts:$action?key=${BuildConfig.FIREBASE_API_KEY}",body.toString(),"application/json")

    private suspend fun post(url: String, body: String, type: String): JSONObject = withContext(Dispatchers.IO) {
        val connection = URI(url).toURL().openConnection() as HttpURLConnection
        try {
            connection.requestMethod="POST"; connection.connectTimeout=15000; connection.readTimeout=15000
            connection.doOutput=true; connection.setRequestProperty("Content-Type",type)
            connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            val success = connection.responseCode in 200..299
            val text = (if(success) connection.inputStream else connection.errorStream)?.bufferedReader()?.use { it.readText() }.orEmpty()
            val json = JSONObject(text.ifBlank { "{}" })
            if(!success) {
                val code = json.optJSONObject("error")?.optString("message").orEmpty()
                error(when {
                    code.contains("INVALID_LOGIN_CREDENTIALS") || code.contains("INVALID_PASSWORD") || code.contains("EMAIL_NOT_FOUND") -> "El correo o la contraseña no son correctos"
                    code.contains("TOO_MANY_ATTEMPTS") -> "Demasiados intentos. Vuelve a intentarlo más tarde"
                    code.contains("USER_DISABLED") -> "La cuenta está deshabilitada"
                    code.contains("OPERATION_NOT_ALLOWED") -> "Activa este método de acceso en Firebase Authentication"
                    code.contains("TOKEN") -> "Tu sesión caducó. Cierra sesión y vuelve a ingresar"
                    else -> "No pudimos completar el acceso. Revisa la conexión y la configuración de Firebase"
                })
            }
            json
        } catch(e: java.io.IOException) { error("No hay conexión. Vuelve a intentarlo cuando tengas internet") }
        finally { connection.disconnect() }
    }
    private fun encode(value: String) = URLEncoder.encode(value,"UTF-8")
}

class AuthCancelled:Exception()
