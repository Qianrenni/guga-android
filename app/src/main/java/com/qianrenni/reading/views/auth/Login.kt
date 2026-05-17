package com.qianrenni.reading.views.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.qianrenni.reading.viewmodels.auth.LoginViewModel

@Composable
fun LoginView(
    onLoginSuccess: () -> Unit = {},
    onLoginError: (String) -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {},
    navController: NavController,
    viewModel: LoginViewModel = viewModel()
) {
    val username by viewModel.username
    val password by viewModel.password
    val captcha by viewModel.captcha
    val rememberMe by viewModel.rememberMe
    val isLoading by viewModel.isLoading
    val captchaBytes by viewModel.captchaBytes

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("用户登录", style = MaterialTheme.typography.headlineMedium)

            OutlinedTextField(
                value = username,
                onValueChange = { viewModel.username.value = it },
                label = { Text("邮箱") },
                placeholder = { Text("请输入用户邮箱") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { viewModel.password.value = it },
                label = { Text("密码") },
                placeholder = { Text("请输入密码") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp) // 输入框和图片之间的间距
            ) {
                OutlinedTextField(
                    value = captcha,
                    onValueChange = { viewModel.captcha.value = it },
                    label = { Text("验证码") },
                    placeholder = { Text("请输入验证码") },
                    singleLine = true,
                    // 让输入框占据剩余空间
                    modifier = Modifier.weight(1f)
                )
                Image(
                    painter = rememberAsyncImagePainter(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(captchaBytes) // Coil 直接支持 ByteArray
                            .size(Size.ORIGINAL)
                            .build(),
                    ),
                    contentDescription = "验证码",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .height(80.dp)
                        .clickable(onClick = {
                            viewModel.loadCaptcha()
                        }),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { viewModel.rememberMe.value = it },
                    )
                    Text("记住我")
                }
                TextButton(onClick = onForgotPasswordClick) {
                    Text(text = "忘记密码?")
                }
            }

            Button(
                onClick = {
                    viewModel.login(
                        onLoginSuccess = onLoginSuccess,
                        onLoginError = onLoginError
                    )
                },
                enabled = !isLoading,
                modifier = Modifier.width(180.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    Text("登录")
                }
            }

            TextButton(onClick = onRegisterClick) {
                Text(text = "没有账号? 立即注册")
            }
        }
    }
}