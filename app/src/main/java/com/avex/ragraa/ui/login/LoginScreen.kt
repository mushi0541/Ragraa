package com.avex.ragraa.ui.login

import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.avex.ragraa.R
import com.avex.ragraa.data.Datasource
import com.avex.ragraa.network.CustomWebChromeClient
import com.avex.ragraa.network.CustomWebViewClient
import com.avex.ragraa.network.RagraaApi

@Composable
fun LoginScreen(
    viewModel: LoginViewModel
) {
    val uiState = viewModel.uiState.collectAsState().value
    val scrollState = rememberScrollState()

    BackHandler {
        if (Datasource.rollNo.isNotEmpty()) viewModel.navController.navigate("home")
    }

    if (uiState.isCompleted) {
        viewModel.resetData()
        viewModel.navController.navigate("home")
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(vertical = 16.dp)
    ) {
        //Logo
        Logo()

        Spacer(modifier = Modifier.height(24.dp))

        val focusManager = LocalFocusManager.current

        SemesterMenu(viewModel)

        //Username text field
        OutlinedTextField(
            value = uiState.rollNo,
            onValueChange = { viewModel.updateRollNo(it) },
            singleLine = true,
            leadingIcon = { Icon(imageVector = Icons.Filled.Person, contentDescription = null) },
            isError = uiState.isError,
            textStyle = MaterialTheme.typography.bodyLarge,
            keyboardActions = KeyboardActions {
                focusManager.moveFocus(FocusDirection.Next)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            label = {
                Text(
                    text = stringResource(R.string.roll_number),
                    style = MaterialTheme.typography.labelLarge
                )
            },
            shape = CutCornerShape(topEnd = 10.dp, bottomStart = 10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                unfocusedLabelColor = MaterialTheme.colorScheme.onBackground,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                focusedTextColor = MaterialTheme.colorScheme.primary,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            )
        )

        //Password text field
        OutlinedTextField(
            value = uiState.password,
            onValueChange = { viewModel.updatePassword(it) },
            textStyle = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            singleLine = true,
            visualTransformation = if (uiState.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            label = {
                Text(
                    text = stringResource(R.string.password),
                    style = MaterialTheme.typography.labelLarge
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            keyboardActions = KeyboardActions {
                focusManager.clearFocus()
            },
            leadingIcon = { Icon(imageVector = Icons.Filled.VpnKey, contentDescription = null) },
            trailingIcon = {
                ShowPasswordIcon(viewModel)
            },
            shape = CutCornerShape(topEnd = 10.dp, bottomStart = 10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                unfocusedLabelColor = MaterialTheme.colorScheme.onBackground,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                focusedTextColor = MaterialTheme.colorScheme.primary,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            )
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 11.dp)
        ) {
            Surface {
                AnimatedContent(uiState.rememberLogin, label = "") {
                    Checkbox(
                        checked = it,
                        onCheckedChange = { viewModel.updatePreference() }
                    )
                }
            }
            Text(
                text = stringResource(R.string.remember_login_info),
                modifier = Modifier.clickable { viewModel.updatePreference() },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!uiState.captchaSolved && !uiState.isCompleted) {
            val backgroundHex = if (Datasource.darkTheme) "#11140F" else "#F8FAF0"
            val backgroundColor = if (Datasource.darkTheme) android.graphics.Color.parseColor("#11140F") else android.graphics.Color.parseColor("#F8FAF0")
            val webViewClient = CustomWebViewClient { }
            val webChromeClient = CustomWebChromeClient { viewModel.updateCaptchaToken(it) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (Datasource.darkTheme) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSecondary)
            ) {
                // Flex now uses Cloudflare Turnstile. Base URL must be the Flex origin so the sitekey's hostname check passes.
                val captchaSnippet = """
                    <html>
                    <head>
                        <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
                        <script src="https://challenges.cloudflare.com/turnstile/v0/api.js?render=explicit&onload=onloadCallback" async defer></script>
                        <style>
                            body { margin: 0; padding: 0; display: flex; justify-content: center; align-items: flex-start; height: 100vh; background-color: $backgroundHex; }
                            #captcha { margin-top: 16px; }
                        </style>
                    </head>
                    <body>
                        <div id="captcha"></div>
                        <script type="text/javascript">
                            function onloadCallback() {
                                turnstile.render("#captcha", {
                                    sitekey: "${RagraaApi.TURNSTILE_SITE_KEY}",
                                    theme: "${if (Datasource.darkTheme) "dark" else "light"}",
                                    callback: function(token) { console.log("koubilgicaptchatoken:" + token) }
                                })
                            }
                        </script>
                    </body>
                    </html>
                """.trimIndent()

                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    // Load once in factory; loading in the update block reset the widget on every recomposition (e.g. typing)
                    factory = { context ->
                        WebView(context).apply {
                            this.webViewClient = webViewClient
                            this.webChromeClient = webChromeClient
                            settings.javaScriptEnabled = true
                            settings.blockNetworkImage = false
                            settings.domStorageEnabled = true
                            // Keep the WebView's real user agent: Turnstile fails when it doesn't match the browser fingerprint
                            setBackgroundColor(backgroundColor)
                            // Fix for "white box" on scroll
                            setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                            isNestedScrollingEnabled = false
                            loadDataWithBaseURL("https://flexstudent.nu.edu.pk/Login", captchaSnippet, "text/html", "UTF-8", null)
                        }
                    }
                )
            }
        }

        val statusList = uiState.status.substring(1, uiState.status.length - 1).split(", ")

        for (status in statusList) if (status.isNotEmpty()) Text(
            text = status,
            color = if (status.contains("successfully")) MaterialTheme.colorScheme.primary else if (status.contains(
                    "Error"
                )
            ) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(4.dp),
            textAlign = TextAlign.Center
        )

        if (uiState.captchaSolved && !uiState.isCompleted && uiState.showButtons) {
            Button(
                modifier = Modifier
                    .padding(horizontal = 32.dp, vertical = 16.dp)
                    .fillMaxWidth(),
                onClick = {
                    viewModel.login()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                elevation = ButtonDefaults.buttonElevation(dimensionResource(R.dimen.elevation))
            ) {
                Text(
                    stringResource(R.string.login),
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
