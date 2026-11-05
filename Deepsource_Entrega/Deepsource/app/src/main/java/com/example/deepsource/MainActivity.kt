package com.example.deepsource

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.deepsource.ui.*
import com.example.deepsource.ui.theme.*

class MainActivity : ComponentActivity() {
    private lateinit var vm: AppViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        vm=ViewModelProvider(this)[AppViewModel::class.java]
        if (savedInstanceState == null) {
            acceptRoute(intent)
        } else if (vm.loading) {
            if (savedInstanceState.getBoolean("profile_open")) vm.route("profile") else acceptRoute(intent)
        }
        setContent { DeepsourceTheme { DeepsourceApp(vm) } }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("profile_open", vm.screen == "profile")
    }
    override fun onNewIntent(intent: Intent) { super.onNewIntent(intent); setIntent(intent); acceptRoute(intent) }
    override fun onResume() { super.onResume(); if(::vm.isInitialized && !vm.loading && vm.session!=null) vm.refresh() }
    private fun acceptRoute(intent: Intent?) {
        intent?.getStringExtra("project")?.let { vm.selectedProject=it }
        intent?.getStringExtra("route")?.takeIf { it in listOf("entry","timer","home","widget") }?.let { vm.route(it) }
    }
}

@Composable fun DeepsourceApp(vm:AppViewModel) {
    val snackbar=remember { SnackbarHostState() }
    LaunchedEffect(vm.message) { vm.message?.let { val value=it; vm.message=null; snackbar.showSnackbar(value) } }
    BackHandler(enabled=vm.session!=null && vm.screen!="home") { vm.screen="home" }
    val nav=listOf(Triple("home","Inicio","home"),Triple("summary","Resumen","chart"),
        Triple("history","Registros","history"),Triple("widget","Widget","widget"),Triple("profile","Perfil","person"))
    val showNav=vm.session!=null && vm.screen in nav.map { it.first }
    Surface(Modifier.fillMaxSize(),color=Ink) {
        Box(Modifier.fillMaxSize(),contentAlignment=Alignment.TopCenter) {
            Scaffold(Modifier.widthIn(max=640.dp).fillMaxSize(),containerColor=Ink,
                contentWindowInsets=WindowInsets(0,0,0,0),snackbarHost={SnackbarHost(snackbar)},
                topBar={ if(vm.session!=null) {
                    Row(Modifier.fillMaxWidth().background(Ink).statusBarsPadding().heightIn(min=66.dp).padding(horizontal=20.dp, vertical=4.dp),verticalAlignment=Alignment.CenterVertically) {
                        if(vm.screen=="home") Brand(true) else {
                            if(!showNav) IconButton({vm.screen="home"}, Modifier.semantics { contentDescription = "Volver al inicio" }) { Glyph("back") }
                            Text(when(vm.screen) { "summary"->"Resumen de horas"; "history"->"Mis registros"; "widget"->"Tu widget"; "profile"->"Mi perfil"; "entry"->if(vm.editing==null) "Registrar horas" else "Editar registro"; "timer"->"Cronómetro"; "team"->"Resumen del equipo"; "project"->"Detalle del proyecto"; else->"Deepsource" },style=MaterialTheme.typography.titleLarge)
                        }
                        Spacer(Modifier.weight(1f))
                        if(vm.screen=="home") IconButton({vm.screen="profile"}, Modifier.semantics { contentDescription = "Abrir perfil" }) { Avatar(vm.session!!, 36, vm.profilePhoto) }
                    }
                } },
                bottomBar={ if(showNav) NavigationBar(containerColor=Ink,tonalElevation=0.dp,windowInsets=WindowInsets.navigationBars) {
                    nav.forEach { (route,label,icon) -> NavigationBarItem(selected=vm.screen==route,onClick={vm.screen=route},
                        icon={Glyph(icon)},label={Text(label)},colors=NavigationBarItemDefaults.colors(selectedIconColor=Mint,selectedTextColor=Mint,indicatorColor=Mint.copy(alpha=.1f),unselectedIconColor=Muted,unselectedTextColor=Muted)) }
                } }
            ) { inner ->
                Box(Modifier.fillMaxSize().padding(inner)) {
                    when {
                        vm.loading -> Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center) { CircularProgressIndicator(color=Mint) }
                        vm.session==null -> LoginScreen(vm)
                        else -> when(vm.screen) {
                            "home" -> HomeScreen(vm)
                            "summary" -> SummaryScreen(vm,false)
                            "team" -> if(vm.session!!.role=="LEADER") SummaryScreen(vm,true) else HomeScreen(vm)
                            "history" -> HistoryScreen(vm)
                            "entry" -> EntryScreen(vm)
                            "timer" -> TimerScreen(vm)
                            "success" -> SuccessScreen(vm)
                            "profile" -> ProfileScreen(vm)
                            "widget" -> WidgetScreen(vm)
                            "project" -> ProjectScreen(vm)
                            else -> HomeScreen(vm)
                        }
                    }
                    if(vm.busy) LinearProgressIndicator(Modifier.fillMaxWidth().align(Alignment.TopCenter),color=Mint,trackColor=Color.Transparent)
                }
            }
        }
    }
}
