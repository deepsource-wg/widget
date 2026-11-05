package com.example.deepsource

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.deepsource.data.LocalAccounts
import com.example.deepsource.data.SessionVault
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class LocalLoginTest {
    @Test fun registrationSignInAndPasswordChangeKeepTheSameIdentity() {
        LocalAccounts(ApplicationProvider.getApplicationContext()).use {accounts ->
            val email="test-${UUID.randomUUID()}@example.test"
            val first=accounts.register("Usuario de prueba",email,"PrimeraClave2026!")
            val login=accounts.signIn(email.uppercase(),"PrimeraClave2026!")
            assertEquals(first.uid,login.uid);assertEquals("DEV",login.role)
            assertTrue(runCatching {accounts.signIn(email,"NoEsLaClave2026!")}.isFailure)
            assertTrue(runCatching {accounts.register("Otra persona",email,"PrimeraClave2026!")}.isFailure)
            accounts.changePassword(login,"PrimeraClave2026!","SegundaClave2026!")
            assertTrue(runCatching {accounts.signIn(email,"PrimeraClave2026!")}.isFailure)
            assertEquals(first.uid,accounts.signIn(email,"SegundaClave2026!").uid)
        }
    }
    @Test fun encryptedSessionRestoresTheAuthenticatedIdentityAndClearsOnLogout() {
        val context=ApplicationProvider.getApplicationContext<android.content.Context>()
        val vault=SessionVault(context)
        val previous=vault.read()
        try {
            val session=com.example.deepsource.domain.Session("test_uid","Prueba","prueba@example.test",provider="Correo local")
            vault.save(session);assertEquals(session,vault.read());vault.save(null);assertNull(vault.read())
        } finally {vault.save(previous)}
    }
}
