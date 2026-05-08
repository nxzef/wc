package com.nxzef.wc.presentation.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    onResetSuccess: () -> Unit,
    viewModel: ForgotPasswordViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is ForgotPasswordUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                ForgotPasswordUiEvent.NavigateToLogin -> onResetSuccess()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .widthIn(max = 440.dp)
                    .padding(24.dp),
                shape = MaterialTheme.shapes.large,
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }

                    Text(
                        text = if (state.step == 1) "Forgot Password" else "Reset Password",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (state.step == 1) {
                        EmailStep(state, viewModel)
                    } else {
                        ResetStep(state, viewModel)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmailStep(state: ForgotPasswordState, viewModel: ForgotPasswordViewModel) {
    Text(
        text = "Enter your email address and we'll send you a 6-digit code to reset your password.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = state.email,
        onValueChange = { viewModel.onEmailChange(it) },
        label = { Text("Email Address") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done)
    )

    Button(
        onClick = { viewModel.sendCode() },
        modifier = Modifier.fillMaxWidth().height(50.dp),
        enabled = !state.isLoading && state.email.isNotBlank(),
        shape = MaterialTheme.shapes.medium
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
        } else {
            Text("Send Reset Code")
        }
    }
}

@Composable
private fun ResetStep(state: ForgotPasswordState, viewModel: ForgotPasswordViewModel) {
    Text(
        text = "We sent a code to ${state.email}. Enter it below along with your new password.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    OutlinedTextField(
        value = state.code,
        onValueChange = { if (it.length <= 6) viewModel.onCodeChange(it) },
        label = { Text("6-digit Code") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
    )

    OutlinedTextField(
        value = state.newPassword,
        onValueChange = { viewModel.onNewPasswordChange(it) },
        label = { Text("New Password") },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next)
    )

    OutlinedTextField(
        value = state.confirmPassword,
        onValueChange = { viewModel.onConfirmPasswordChange(it) },
        label = { Text("Confirm New Password") },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
    )

    Button(
        onClick = { viewModel.resetPassword() },
        modifier = Modifier.fillMaxWidth().height(50.dp),
        enabled = !state.isLoading && state.code.length == 6 && state.newPassword.isNotBlank(),
        shape = MaterialTheme.shapes.medium
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
        } else {
            Text("Reset Password")
        }
    }

    TextButton(onClick = { viewModel.sendCode() }, enabled = !state.isLoading) {
        Text("Didn't receive code? Resend")
    }
}
