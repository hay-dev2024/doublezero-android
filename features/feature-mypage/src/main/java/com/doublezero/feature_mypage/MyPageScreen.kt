package com.doublezero.feature_mypage

import android.app.Activity
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.ApiException

@Composable
fun MyPageScreen(
    viewModel: MyPageViewModel = hiltViewModel(),
    onNavigateToHome: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onSearchClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val TAG = "MyPageScreen"

    // dialog state for sign-in failures
    val showSignInFailed = remember { mutableStateOf(false) }

    // Log state changes
    LaunchedEffect(uiState.isLoggedIn) {
        android.util.Log.d(TAG, "LaunchedEffect: isLoggedIn=${uiState.isLoggedIn}")
        if (uiState.isLoggedIn) {
            Toast.makeText(context, "Login state: SIGNED IN", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Login state: SIGNED OUT", Toast.LENGTH_SHORT).show()
        }
    }

    // Try silentSignIn on screen enter to restore cached credentials without user interaction
    LaunchedEffect(Unit) {
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.WEB_CLIENT_ID)
                .requestEmail()
                .build()
            val client = GoogleSignIn.getClient(context, gso)
            Log.d(TAG, "LaunchedEffect(Unit): attempting initial silentSignIn")
            client.silentSignIn().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    try {
                        val acct = task.result
                        Log.d(TAG, "LaunchedEffect: silentSignIn success account=$acct")
                        val idToken = acct?.idToken
                        if (!idToken.isNullOrEmpty()) {
                            viewModel.onGoogleLoginSuccess(idToken)
                        } else {
                            Log.d(TAG, "LaunchedEffect: silentSignIn returned no idToken")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "LaunchedEffect: silentSignIn processing error", e)
                    }
                } else {
                    Log.d(TAG, "LaunchedEffect: silentSignIn not available or failed: ${task.exception}")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "LaunchedEffect: silentSignIn threw", e)
        }
    }

    // 로그인 결과 처리 런처
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(TAG, "googleSignInLauncher: resultCode=${result.resultCode}")
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                Log.d(TAG, "googleSignInLauncher: account=$account")
                val idToken = account?.idToken
                Log.d(TAG, "googleSignInLauncher: idToken present=${!idToken.isNullOrEmpty()}")
                if (!idToken.isNullOrEmpty()) {
                    viewModel.onGoogleLoginSuccess(idToken)
                } else {
                    Toast.makeText(context, "Failed to get idToken. Check WEB_CLIENT_ID config.", Toast.LENGTH_LONG).show()
                }
            } catch (e: ApiException) {
                Log.e(TAG, "googleSignInLauncher: ApiException", e)
                Toast.makeText(context, "Google Sign-In failed: ${e.statusCode}", Toast.LENGTH_LONG).show()
            }
        } else {
            Log.d(TAG, "googleSignInLauncher: non-OK resultCode=${result.resultCode}")
            showSignInFailed.value = true
            // Additional debug: log Intent data and extras when result is non-OK
            val data = result.data
            if (data == null) {
                Log.w(TAG, "googleSignInLauncher: result.data is null")
            } else {
                try {
                    Log.d(TAG, "googleSignInLauncher: result.data.toString=${data.toString()}")
                    val uri = data.toUri(0)
                    Log.d(TAG, "googleSignInLauncher: result.data.toUri=${uri}")
                } catch (e: Exception) {
                    Log.w(TAG, "googleSignInLauncher: failed to toUri()", e)
                }

                val extras = data.extras
                if (extras == null) {
                    Log.d(TAG, "googleSignInLauncher: data.extras is null")
                } else {
                    for (key in extras.keySet()) {
                        try {
                            Log.d(TAG, "googleSignInLauncher: extra[$key]=${extras.get(key)}")
                        } catch (e: Exception) {
                            Log.d(TAG, "googleSignInLauncher: extra[$key]=<error>", e)
                        }
                    }
                }
            }
            // Show user-facing message when sign-in is cancelled or fails to return a token
            Toast.makeText(
                context,
                "Google Sign-In was cancelled or failed. Ensure your AVD has Google Play and an account, or use Dev login.",
                Toast.LENGTH_LONG
            ).show()

            // Attempt silentSignIn as a fallback (may succeed if credentials are cached)
            try {
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(BuildConfig.WEB_CLIENT_ID)
                    .requestEmail()
                    .build()
                val client = GoogleSignIn.getClient(context, gso)
                Log.d(TAG, "googleSignInLauncher: attempting silentSignIn fallback")
                client.silentSignIn().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        try {
                            val acct = task.result
                            Log.d(TAG, "googleSignInLauncher: silentSignIn success, account=$acct")
                            val idToken = acct?.idToken
                            if (!idToken.isNullOrEmpty()) {
                                viewModel.onGoogleLoginSuccess(idToken)
                            } else {
                                Log.d(TAG, "googleSignInLauncher: silentSignIn returned no idToken")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "googleSignInLauncher: silentSignIn processing error", e)
                        }
                    } else {
                        Log.d(TAG, "googleSignInLauncher: silentSignIn failed or no cached credentials: ${task.exception}")
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "googleSignInLauncher: silentSignIn fallback threw", e)
            }
        }
    }

    // 로그인 트리거 함수
    val onLoginClick = remember {
        {
            Log.d(TAG, "onLoginClick: WEB_CLIENT_ID=${BuildConfig.WEB_CLIENT_ID}")
            // Check Google Play Services availability
            val playStatus = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
            Log.d(TAG, "onLoginClick: GooglePlayServices status=$playStatus (ConnectionResult.SUCCESS=${ConnectionResult.SUCCESS})")
            if (playStatus != ConnectionResult.SUCCESS) {
                Toast.makeText(context, "Google Play Services not available on this device/AVD. Dev login available.", Toast.LENGTH_LONG).show()
                return@remember
            }

            // Log last signed-in account (if any)
            val lastAccount = GoogleSignIn.getLastSignedInAccount(context)
            Log.d(TAG, "onLoginClick: lastSignedInAccount=$lastAccount")
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.WEB_CLIENT_ID)
                .requestEmail()
                .build()

            val client = GoogleSignIn.getClient(context, gso)
            Log.d(TAG, "onLoginClick: launching signInIntent with client=$client")
            googleSignInLauncher.launch(client.signInIntent)
        }
    }

    if (uiState.isLoggedIn) {
        SignedInMyPageScreen(
            userProfile = uiState.userProfile,
            onLogout = viewModel::onLogout,
            onNavigateToHistory = onNavigateToHistory,
            onNavigateToSettings = onNavigateToSettings
        )
    } else {
        SignedOutMyPageScreen(
            onLoginClick = onLoginClick,
            onDevLoginClick = { viewModel.onDevLogin() }
        )
    }

    if (showSignInFailed.value) {
        AlertDialog(onDismissRequest = { showSignInFailed.value = false },
            title = { Text("Google Sign-In failed") },
            text = { Text("Sign-in was cancelled or failed. You can retry or open device account settings.") },
            confirmButton = {
                Button(onClick = {
                    showSignInFailed.value = false
                    onLoginClick()
                }) { Text("Retry") }
            },
            dismissButton = {
                Button(onClick = {
                    // Open account settings to let user add/select Google account
                    try {
                        val intent = Intent(Settings.ACTION_SYNC_SETTINGS)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Log.w(TAG, "failed to open account settings", e)
                    }
                    showSignInFailed.value = false
                }) { Text("Open Accounts") }
            }
        )
    }
}
