package com.example.deepsource

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.deepsource.data.LocalStore
import com.example.deepsource.domain.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class StoreTest {
    @Test fun accountIsolationAndOwnershipAreEnforced() {
        LocalStore(ApplicationProvider.getApplicationContext()).use {store ->
            val a=Session(UUID.randomUUID().toString(),"Cuenta A","a@example.test")
            val b=Session(UUID.randomUUID().toString(),"Cuenta B","b@example.test")
            store.createProject(a,"Proyecto de A","Cliente",8)
            val project=store.projects(a).single()
            val entry=TimeEntry(uid=a.uid,userName=a.name,projectId=project.id,day=LocalDate.now(),seconds=60,task="Tarea de prueba")
            val id=store.saveEntry(a,entry)
            assertTrue(store.projects(b).isEmpty());assertTrue(store.entries(b).isEmpty())
            assertTrue(runCatching {store.saveEntry(b,entry)}.isFailure)
            assertTrue(runCatching {store.deleteEntry(b,id)}.isFailure)
            assertTrue(runCatching {store.entries(a,true)}.isFailure)
            assertEquals(1,store.entries(a).size)
        }
    }
    @Test fun timerCompletionIsAtomicAndOnlyCountsActiveSeconds() {
        LocalStore(ApplicationProvider.getApplicationContext()).use {store ->
            val user=Session(UUID.randomUUID().toString(),"Prueba","test@example.test")
            store.createProject(user,"Proyecto prueba","Cliente",8)
            val project=store.projects(user).single()
            store.saveTimer(user,RunningTimer(project.id,"Tarea del cronómetro",5000))
            val saved=store.finishTimer(user,5)
            assertEquals(5,saved.seconds);assertNull(store.timer(user))
            assertTrue(runCatching {store.finishTimer(user,5)}.isFailure)
            assertEquals(1,store.entries(user).size)
        }
    }
    @Test fun dailyTotalCannotExceedTwentyFourHours() {
        LocalStore(ApplicationProvider.getApplicationContext()).use {store ->
            val user=Session(UUID.randomUUID().toString(),"Prueba","test@example.test")
            store.createProject(user,"Proyecto prueba","Cliente",8)
            val project=store.projects(user).single()
            val e=TimeEntry(uid=user.uid,userName=user.name,projectId=project.id,day=LocalDate.now(),seconds=86400,task="Tarea de prueba")
            store.saveEntry(user,e)
            assertTrue(runCatching {store.saveEntry(user,e.copy(seconds=1))}.isFailure)
        }
    }
}
