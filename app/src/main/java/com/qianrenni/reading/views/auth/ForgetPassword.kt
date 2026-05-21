package com.qianrenni.reading.views.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.qianrenni.reading.util.SnackBarManager
import com.qianrenni.reading.viewmodels.auth.ForgetPasswordViewModel
import kotlinx.coroutines.launch

@Composable
fun ForgetPasswordView(
    navController: NavController,
    viewModel: ForgetPasswordViewModel = viewModel()
) {
    val forgetPasswordState by viewModel.forgetPasswordState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    LaunchedEffect(forgetPasswordState.pageStatus.errorMessage) {
        forgetPasswordState.pageStatus.errorMessage?.let { error ->
            SnackBarManager.showMessage(error)
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("忘记密码", style = MaterialTheme.typography.headlineMedium)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = forgetPasswordState.email,
                    onValueChange = { viewModel.onEmailChange(it) },
                    label = { Text("邮箱") },
                    placeholder = { Text("请输入邮箱") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )
                Button(
                    onClick = {
                        viewModel.sendVerificationCode(
                            onSuccess = {
                                scope.launch { SnackBarManager.showMessage("验证码已发送到邮箱") }
                            }
                        )
                    },
                    enabled = !forgetPasswordState.isSendingCode,
                ) {
                    if (forgetPasswordState.isSendingCode) {
                        CircularProgressIndicator(modifier = Modifier.width(20.dp))
                    } else {
                        Text("验证邮箱")
                    }
                }
            }

            OutlinedTextField(
                value = forgetPasswordState.captcha,
                onValueChange = { viewModel.onCaptchaChange(it) },
                label = { Text("验证码") },
                placeholder = { Text("请输入邮箱验证码") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = forgetPasswordState.password,
                onValueChange = { viewModel.onPasswordChange(it) },
                label = { Text("新密码") },
                placeholder = { Text("请输入新密码") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = forgetPasswordState.confirmPassword,
                onValueChange = { viewModel.onConfirmPasswordChange(it) },
                label = { Text("确认密码") },
                placeholder = { Text("请再次输入新密码") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    viewModel.resetPassword(
                        onSuccess = {
                            scope.launch { SnackBarManager.showMessage("密码重置成功，请登录") }
                            navController.navigate("login") {
                                popUpTo("forget-password") { inclusive = true }
                            }
                        }
                    )
                },
                enabled = !forgetPasswordState.pageStatus.isLoading,
            ) {
                if (forgetPasswordState.pageStatus.isLoading) {
                    CircularProgressIndicator()
                } else {
                    Text("重置密码")
                }
            }

            TextButton(onClick = {
                navController.navigate("login") {
                    popUpTo("forget-password") { inclusive = true }
                }
            }) {
                Text(text = "返回登录")
            }
        }
    }
}
