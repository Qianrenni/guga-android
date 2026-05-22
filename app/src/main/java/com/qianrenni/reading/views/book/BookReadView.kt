package com.qianrenni.reading.views.book

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.qianrenni.reading.R
import com.qianrenni.reading.components.BottomControlBar
import com.qianrenni.reading.components.CatalogDrawer
import com.qianrenni.reading.components.ReadingSettings
import com.qianrenni.reading.data.model.ReadSettings
import com.qianrenni.reading.data.store.SettingsRepository
import com.qianrenni.reading.viewmodels.book.BookReadViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookReadView(
    context: Context,
    navController: NavController,
    bookId: Int,
    chapterId: Int = -1,
    viewModel: BookReadViewModel = viewModel()
) {
    viewModel.loadBookAndCatalog(bookId, chapterId)
    val uiState by viewModel.uiState.collectAsState()
    var isAscending by remember { mutableStateOf(true) }
    val lazyListState = rememberLazyListState()
    val settingsRepository =
        remember { SettingsRepository(context) }
    var readSettings by remember { mutableStateOf(ReadSettings()) }

    // 收集阅读设置
    LaunchedEffect(Unit) {
        settingsRepository.readSettings.collectLatest { settings ->
            readSettings = settings
        }
    }
    Surface() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(readSettings.backgroundColor.toColorInt()))
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (uiState.error != null) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "加载失败: ${uiState.error}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                    Button(onClick = { /* TODO: 重试 */ }) {
                        Text("重试")
                    }
                }
            } else {
                // 显示章节内容
                ChapterContent(
                    content = uiState.chapterContent,
                    settings = readSettings,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { viewModel.toggleBottomControls() }
                )
                if (uiState.showBottomControls) {
                    val currentIndex =
                        uiState.catalog.indexOfFirst { it.id == uiState.currentChapterId }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)  //  让这个 Box 在父 Box 中靠底部
                            .fillMaxWidth()
                    ) {
                        // 阅读设置对话框
                        Column() {
                            AnimatedVisibility(uiState.showSettings) {
                                ReadingSettings(
                                    settings = readSettings,
                                    onSettingsChange = { newSettings ->
                                        viewModel.viewModelScope.launch {
                                            settingsRepository.updateSettings(newSettings)
                                        }
                                    },
                                )
                            }
                            BottomControlBar(
                                canGoPrevious = currentIndex > 0,
                                canGoNext = currentIndex < uiState.catalog.size - 1 && currentIndex >= 0,
                                onPreviousClick = { viewModel.goToPreviousChapter() },
                                onNextClick = { viewModel.goToNextChapter() },
                                onCatalogClick = {
                                    viewModel.toggleCatalog()
                                },
                                onSettingsClick = { viewModel.toggleSettings() },
                                onBookDetailClick = {
                                    viewModel.hideAllDialogs()
                                    navController.navigate("book/${uiState.book?.id}")
                                },
                                onDismiss = { viewModel.hideAllDialogs() }
                            )
                        }
                    }
                }
                val shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomEnd = 0.dp,
                    bottomStart = 0.dp
                )
                // 目录抽屉
                AnimatedVisibility(
                    uiState.showCatalog,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.75f)
                        .align(Alignment.BottomCenter)
                        .background(
                            color = MaterialTheme.colorScheme.background,
                            shape = shape,
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)

                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    model = uiState.book?.cover,
                                    // 可选：添加占位/错误状态
                                    placeholder = painterResource(R.drawable.skeleton),
                                    error = painterResource(R.drawable.skeleton)
                                ),
                                contentDescription = uiState.book?.name,
                                modifier = Modifier
                                    .height(60.dp),
                                contentScale = ContentScale.FillHeight
                            )
                            Column(modifier = Modifier.padding(start = 8.dp)) {
                                Text(
                                    uiState.book?.name ?: "",
                                    style = MaterialTheme.typography.titleLarge,
                                    maxLines = 1
                                )
                                Text(
                                    uiState.book?.author ?: "",
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1
                                )
                            }
                            IconButton(onClick = { navController.navigate("book/${uiState.book?.id}") }) {
                                Icon(Icons.Default.ChevronRight, "goToDetail")
                            }

                        }
                        CatalogDrawer(
                            modifier = Modifier
                                .weight(1f),
                            isAscending = isAscending,
                            state = lazyListState,
                            catalog = uiState.catalog,
                            currentChapterId = uiState.currentChapterId,
                            onChapterSelected = { chapterId ->
                                viewModel.loadChapter(
                                    chapterId
                                )
                            },
                            onDismiss = { viewModel.toggleCatalog() },
                            onReverseCatalog = { isAscending = !isAscending }
                        )
                    }

                }

            }
        }
    }
}

@Composable
private fun ChapterContent(
    content: List<String>,
    settings: ReadSettings,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.background(
            MaterialTheme.colorScheme.surface
        )
    ) {
        items(content) {
            Text(
                text = it,
                modifier = Modifier.padding(
                    horizontal = 8.dp,
                    vertical = (settings.fontSize / 2).dp
                ),
                style = TextStyle(
                    fontSize = settings.fontSize.sp,
                    lineHeight = settings.lineHeight.sp,
                    letterSpacing = settings.letterSpacing.sp,
                    textIndent = TextIndent(firstLine = (settings.fontSize + settings.fontSize).sp),
                    fontFamily = settings.fontFamily
                )
            )
        }
    }
}