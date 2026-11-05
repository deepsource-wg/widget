package com.example.deepsource.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.deepsource.AppViewModel
import com.example.deepsource.BuildConfig
import com.example.deepsource.domain.validateName
import com.example.deepsource.domain.validatePasswordChange
import com.example.deepsource.ui.theme.Mint
import com.example.deepsource.ui.theme.Muted

@Composable fun ProfileScreen(vm:AppViewModel) {
    val s=vm.session!!
    var name by rememberSaveable(s.uid) {mutableStateOf(s.name)}
    var submitted by rememberSaveable(s.uid) { mutableStateOf(false) }
    val nameError = validateName(name)
    var logout by remember {mutableStateOf(false)}
    var changePassword by remember {mutableStateOf(false)}
    Column(Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(22.dp)) {
        DemoBanner(vm)
        Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(16.dp)) {
            Avatar(s, size = 64, photo = vm.profilePhoto)
            Column(Modifier.weight(1f)) {
                Text(s.name, style = MaterialTheme.typography.titleLarge)
                Text("Tu cuenta de Deepsource", color = Muted)
            }
        }
        ProfilePhotoPanel(vm, s.uid)
        Field(name, { name = it }, "Nombre completo", leading = "person",
            error = if (submitted) nameError else null)
        Field(s.email,{},"Correo electrónico",readOnly=true,leading="mail",trailing={Glyph("lock",tint=Muted)})
        Text("El correo identifica tu cuenta y se mantiene durante la sesión.",color=Muted,style=MaterialTheme.typography.bodySmall)
        Field(s.role,{},"Rol · Solo lectura",readOnly=true,leading="lock")
        Primary("Guardar cambios", "check", !vm.busy && name.trim() != s.name) {
            submitted = true
            if (nameError == null) vm.profile(name)
        }
        PanelCard {Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {Glyph("lock",tint=Mint);Column {Text("Sesión protegida",fontWeight=FontWeight.SemiBold);Text("Método de acceso: ${s.provider}",color=Muted,style=MaterialTheme.typography.bodySmall)}}}
        if(!s.demo) Secondary("Cambiar contraseña","lock",!vm.busy) {changePassword=true}
        Secondary("Cerrar sesión","logout",!vm.busy && !vm.photoBusy) {logout=true}
        Text("Deepsource · ${BuildConfig.VERSION_NAME}\nTus registros se guardan en este dispositivo.",Modifier.fillMaxWidth(),color=Muted,style=MaterialTheme.typography.bodySmall,textAlign=androidx.compose.ui.text.style.TextAlign.Center)
    }
    if(changePassword) ChangePasswordDialog(vm) {changePassword=false}
    if(logout) AlertDialog(onDismissRequest={logout=false},title={Text("¿Cerrar sesión?")},text={Text("Tus registros se conservarán. Si el cronómetro está en marcha, quedará pausado.")},confirmButton={TextButton({logout=false;vm.logout()}) {Text("Cerrar sesión")}},dismissButton={TextButton({logout=false}) {Text("Cancelar")}})
}

@Composable private fun ChangePasswordDialog(vm:AppViewModel,onDismiss:()->Unit) {
    var current by remember { mutableStateOf("") }
    var next by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    var currentPasswordError by remember { mutableStateOf<String?>(null) }
    val errors = validatePasswordChange(current, next, confirm)

    AlertDialog(
        onDismissRequest = { if (!vm.busy) onDismiss() },
        title = { Text("Cambiar contraseña") },
        text = {
            Column(
                Modifier.imePadding().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Field(current, {
                    current = it.take(128)
                    currentPasswordError = null
                }, "Contraseña actual", keyboard = KeyboardType.Password, readOnly = vm.busy,
                    visual = PasswordVisualTransformation(),
                    error = if (submitted) errors.current ?: currentPasswordError else null)
                Field(next, { next = it.take(128) }, "Nueva contraseña", keyboard = KeyboardType.Password,
                    readOnly = vm.busy, visual = PasswordVisualTransformation(),
                    error = if (submitted) errors.next else null)
                Field(confirm, { confirm = it.take(128) }, "Repite la nueva contraseña",
                    keyboard = KeyboardType.Password, readOnly = vm.busy, visual = PasswordVisualTransformation(),
                    error = if (submitted) errors.confirmation else null)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                submitted = true
                if (validatePasswordChange(current, next, confirm).valid) {
                    currentPasswordError = null
                    vm.changePassword(current, next, onDismiss) { currentPasswordError = it }
                }
            }, enabled = !vm.busy) { Text(if (vm.busy) "Guardando…" else "Guardar") }
        },
        dismissButton = { TextButton(onDismiss, enabled = !vm.busy) { Text("Cancelar") } }
    )
}

