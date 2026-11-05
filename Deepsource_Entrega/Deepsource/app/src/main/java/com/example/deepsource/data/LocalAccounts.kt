package com.example.deepsource.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.deepsource.domain.*
import java.util.Locale
import java.util.UUID

internal class LocalAuthenticationException(message: String) : IllegalStateException(message)
internal class LocalEmailInUseException : IllegalStateException("Ya existe una cuenta local con ese correo")

/** Device accounts for the working local prototype. Never accepts an arbitrary email. */
class LocalAccounts(context:Context):SQLiteOpenHelper(context,"local_accounts.db",null,1) {
    override fun onCreate(db:SQLiteDatabase) {
        db.execSQL("CREATE TABLE accounts(uid TEXT PRIMARY KEY,email TEXT UNIQUE NOT NULL,name TEXT NOT NULL,role TEXT NOT NULL,salt BLOB NOT NULL,hash BLOB NOT NULL,iterations INTEGER NOT NULL,attempts INTEGER NOT NULL DEFAULT 0,locked_until INTEGER NOT NULL DEFAULT 0)")
    }
    override fun onUpgrade(db:SQLiteDatabase,oldVersion:Int,newVersion:Int)=Unit
    fun register(name:String,email:String,password:String):Session {
        val nameError = validateName(name)
        require(nameError == null) { nameError.orEmpty() }
        val normalized=normalize(email)
        val emailError = validateEmail(normalized)
        require(emailError == null) { emailError.orEmpty() }
        val digest=Passwords.create(password)
        val uid="local_"+UUID.randomUUID()
        try {
            writableDatabase.insertOrThrow("accounts",null,ContentValues().apply {
                put("uid",uid);put("email",normalized);put("name",name.trim());put("role","DEV")
                put("salt",digest.salt);put("hash",digest.hash);put("iterations",digest.iterations)
            })
        } catch (_: android.database.sqlite.SQLiteConstraintException) {
            throw LocalEmailInUseException()
        }
        return Session(uid,name.trim(),normalized,provider="Correo local")
    }
    fun signIn(email:String,password:String):Session {
        val invalidCredentials = "El correo o la contraseña no son correctos"
        if (password.length > 128) throw LocalAuthenticationException(invalidCredentials)
        val row=readableDatabase.query("accounts",null,"email=?",arrayOf(normalize(email)),null,null,null).use {c ->
            if(!c.moveToFirst()) null else Account(c.getString(0),c.getString(1),c.getString(2),c.getString(3),
                PasswordDigest(c.getBlob(4),c.getBlob(5),c.getInt(6)),c.getInt(7),c.getLong(8))
        } ?: run { // Run a derivation for unknown accounts, too.
            Passwords.verify(password,PasswordDigest(ByteArray(16),ByteArray(32),Passwords.ITERATIONS))
            throw LocalAuthenticationException(invalidCredentials)
        }
        if (row.lockedUntil > System.currentTimeMillis()) {
            throw LocalAuthenticationException("Demasiados intentos. Espera un minuto antes de volver a ingresar")
        }
        if(!Passwords.verify(password,row.digest)) {
            val attempts=if(row.lockedUntil>0) 1 else row.attempts+1
            writableDatabase.update("accounts",ContentValues().apply {
                put("attempts",attempts);put("locked_until",if(attempts>=5) System.currentTimeMillis()+60000 else 0)
            },"uid=?",arrayOf(row.uid))
            throw LocalAuthenticationException(invalidCredentials)
        }
        writableDatabase.update("accounts",ContentValues().apply {put("attempts",0);put("locked_until",0)},"uid=?",arrayOf(row.uid))
        return Session(row.uid,row.name,row.email,row.role,provider="Correo local")
    }
    fun updateName(session:Session,name:String) {
        require(writableDatabase.update("accounts",ContentValues().apply {put("name",name.trim())},"uid=?",arrayOf(session.uid))==1)
    }
    fun changePassword(session:Session,current:String,next:String) {
        val verified=signIn(session.email,current)
        require(verified.uid==session.uid)
        val digest=Passwords.create(next)
        writableDatabase.update("accounts",ContentValues().apply {put("salt",digest.salt);put("hash",digest.hash);put("iterations",digest.iterations)},"uid=?",arrayOf(session.uid))
    }
    private fun normalize(email:String)=email.trim().lowercase(Locale.ROOT)
    private data class Account(val uid:String,val email:String,val name:String,val role:String,val digest:PasswordDigest,val attempts:Int,val lockedUntil:Long)
}
