package com.app.tintuccongnghe.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.tintuccongnghe.domain.models.LoginRequest
import com.app.tintuccongnghe.domain.models.RegisterRequest
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import androidx.credentials.exceptions.GetCredentialException
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import androidx.activity.compose.rememberLauncherForActivityResult
import com.facebook.login.LoginResult
import com.facebook.login.LoginManager
import com.facebook.FacebookException
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.credentials.CustomCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.crashlytics.FirebaseCrashlytics

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    githubCode: String? = null,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = CredentialManager.create(context)
    val callbackManager = remember { CallbackManager.Factory.create() }

    LaunchedEffect(githubCode) {
        if (githubCode != null) {
            viewModel.socialLogin(
                provider = "GitHub",
                idToken = githubCode,
                email = "",
                username = "GitHub User"
            )
        }
    }

    val facebookLauncher = rememberLauncherForActivityResult(
        contract = LoginManager.getInstance().createLogInActivityResultContract(callbackManager),
        onResult = {}
    )

    DisposableEffect(Unit) {
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                viewModel.socialLogin(
                    provider = "Facebook",
                    idToken = result.accessToken.token,
                    email = "", // You might need Graph API for email
                    username = "Facebook User"
                )
            }
            override fun onCancel() {}
            override fun onError(error: FacebookException) {}
        })
        onDispose {
            LoginManager.getInstance().unregisterCallback(callbackManager)
        }
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var usernameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Login", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { 
                username = it
                usernameError = null
            },
            label = { Text("UserName") },
            modifier = Modifier.fillMaxWidth(),
            isError = usernameError != null,
            supportingText = usernameError?.let { { Text(it) } }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { 
                password = it
                passwordError = null
            },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = passwordError != null,
            supportingText = passwordError?.let { { Text(it) } },
            trailingIcon = {
                val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = null)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    var hasError = false
                    if (username.isBlank()) {
                        usernameError = "Username cannot be empty"
                        hasError = true
                    }
                    if (password.isBlank()) {
                        passwordError = "Password cannot be empty"
                        hasError = true
                    }

                    if (!hasError) {
                        viewModel.login(LoginRequest(username, password))
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login")
            }
        }

        uiState.error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error)
            FirebaseCrashlytics.getInstance().recordException(Exception(it))
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text(text = "Or login with", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedButton(
                onClick = {
                    val googleIdOption = GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId("760271124450-021s80ip4isqho9svsb5924ilookcv93.apps.googleusercontent.com")
                        .setAutoSelectEnabled(true)
                        .build()

                    val request = GetCredentialRequest.Builder()
                        .addCredentialOption(googleIdOption)
                        .build()

                    scope.launch {
                        try {
                            val result = credentialManager.getCredential(
                                context = context,
                                request = request
                            )
                            val credential = result.credential
                            when (credential) {
                                is CustomCredential -> {
                                    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                        try {
                                            // Use googleIdTokenCredential and extract the ID for server-side validation.
                                            val googleIdTokenCredential = GoogleIdTokenCredential
                                                .createFrom(credential.data)

                                            viewModel.socialLogin(
                                                provider = "Google",
                                                idToken = googleIdTokenCredential.idToken,
                                                email = googleIdTokenCredential.id ?: "",
                                                username = googleIdTokenCredential.id ?: "Google User"
                                            )

                                        } catch (e: GoogleIdTokenParsingException) {
                                        }
                                    } else {
                                        // Catch any unrecognized credential type here.
                                    }
                                }

                                else -> {
                                }
                            }
                            FirebaseCrashlytics.getInstance().log("LoginScreen: Login with Google successfully")
                        } catch (e: GetCredentialException) {
                            // Handle error
                            FirebaseCrashlytics.getInstance().recordException(e)
                        }
                    }
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF4285F4)
                )
            ) {
                Text("Google")
            }
            Button(
                onClick = {
                    facebookLauncher.launch(listOf("email", "public_profile"))
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1877F2),
                    contentColor = Color.White
                )
            ) {
                Text("Facebook")
            }
            Button(
                onClick = {
                    val clientId = "YOUR_GITHUB_CLIENT_ID"
                    val redirectUri = "reply://github-auth"
                    val url = "https://github.com/login/oauth/authorize?client_id=$clientId&scope=user:email&redirect_uri=$redirectUri"
                    val customTabsIntent = CustomTabsIntent.Builder().build()
                    customTabsIntent.launchUrl(context, Uri.parse(url))
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF24292E),
                    contentColor = Color.White
                )
            ) {
                Text("GitHub")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = {
            onNavigateToRegister()
            FirebaseCrashlytics.getInstance().log("LoginScreen: User clicked on Register button")
        }) {
            Text("Don't have an account? Register")
        }
    }
}

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf<String?>(null) }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onRegisterSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Register", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { 
                username = it
                usernameError = null
            },
            label = { Text("UserName") },
            modifier = Modifier.fillMaxWidth(),
            isError = usernameError != null,
            supportingText = usernameError?.let { { Text(it) } }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { 
                password = it
                passwordError = null
            },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = passwordError != null,
            supportingText = passwordError?.let { { Text(it) } },
            trailingIcon = {
                val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = null)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    var hasError = false
                    if (email.isBlank()) {
                        emailError = "Email cannot be empty"
                        hasError = true
                    }
                    if (username.isBlank()) {
                        usernameError = "Username cannot be empty"
                        hasError = true
                    }
                    if (password.isBlank()) {
                        passwordError = "Password cannot be empty"
                        hasError = true
                    }

                    if (!hasError) {
                        viewModel.register(
                            RegisterRequest(
                                AppId = 0,
                                Email = email,
                                UserName = username,
                                Password = password
                            )
                        )
                        FirebaseCrashlytics.getInstance().log("RegisterScreen: User clicked on Register button")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Register")
            }
        }

        uiState.error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error)
            FirebaseCrashlytics.getInstance().recordException(Exception(it))
        }

        TextButton(onClick = {
            onNavigateToLogin()
            FirebaseCrashlytics.getInstance().log("RegisterScreen: User clicked on Login button")
        }) {
            Text("Already have an account? Login")
        }
    }
}
