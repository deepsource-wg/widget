package com.example.deepsource.data

import android.content.Context
import com.example.deepsource.domain.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(private val context: Context) {
    suspend fun signIn(email: String, password: String): Session = withContext(Dispatchers.IO) {
        LocalAccounts(context).use { it.signIn(email, password) }
    }

    suspend fun register(name: String, email: String, password: String): Session = withContext(Dispatchers.IO) {
        LocalAccounts(context).use { it.register(name, email, password) }
    }

    suspend fun updateProfile(session: Session, name: String): Session {
        if (!session.demo) {
            withContext(Dispatchers.IO) {
                LocalAccounts(context).use { it.updateName(session, name) }
            }
        }
        return session.copy(name = name.trim())
    }

    suspend fun changePassword(session: Session, current: String, next: String) {
        require(!session.demo) { "Esta función está disponible para cuentas locales" }
        withContext(Dispatchers.IO) {
            LocalAccounts(context).use { it.changePassword(session, current, next) }
        }
    }
}
