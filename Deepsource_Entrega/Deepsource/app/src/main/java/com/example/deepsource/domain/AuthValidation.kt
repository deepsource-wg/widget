package com.example.deepsource.domain

import android.util.Patterns

internal fun validateEmail(email: String): String? =
    if (Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) null else "Ingresa un correo válido"

internal data class LoginErrors(val email: String?, val password: String?) {
    val valid get() = email == null && password == null
}

internal fun validateLogin(email: String, password: String): LoginErrors = LoginErrors(
    email = validateEmail(email),
    password = if (password.isBlank()) "Ingresa tu contraseña" else null
)

internal data class RegistrationErrors(
    val name: String?,
    val email: String?,
    val password: String?,
    val confirmation: String?
) {
    val valid get() = name == null && email == null && password == null && confirmation == null
}

internal fun validateRegistration(
    name: String,
    email: String,
    password: String,
    confirmation: String
): RegistrationErrors = RegistrationErrors(
    name = validateName(name),
    email = validateEmail(email),
    password = Passwords.validate(password),
    confirmation = validateConfirmation(password, confirmation)
)

internal data class PasswordChangeErrors(
    val current: String?,
    val next: String?,
    val confirmation: String?
) {
    val valid get() = current == null && next == null && confirmation == null
}

internal fun validatePasswordChange(
    current: String,
    next: String,
    confirmation: String
): PasswordChangeErrors = PasswordChangeErrors(
    current = if (current.isBlank()) "Ingresa tu contraseña actual" else null,
    next = Passwords.validate(next),
    confirmation = validateConfirmation(next, confirmation)
)

private fun validateConfirmation(password: String, confirmation: String): String? = when {
    confirmation.isEmpty() -> "Repite la contraseña"
    confirmation != password -> "Las contraseñas no coinciden"
    else -> null
}
