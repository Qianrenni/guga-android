package com.qianrenni.reading.views.book

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.qianrenni.reading.R
import com.qianrenni.reading.components.BottomControlBar
import com.qianrenni.reading.components.CatalogDrawer
import com.qianrenni.reading.components.CommonPage
import com.qianrenni.reading.components.InfiniteHorizontalPager
import com.qianrenni.reading.components.ReadingSettings
import com.qianrenni.reading.data.model.ReadSettings
import com.qianrenni.reading.data.store.SettingsRepository
import com.qianrenni.reading.util.SystemBarUtils
import com.qianrenni.reading.viewmodels.book.BookReadViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val TAG = "BOOK READ VIEW"
suspend fun measureText(
    content: String,
    density: Density,
    heightPx: Float,
    widthPx: Float,
    textMeasurer: TextMeasurer,
    readSettings: ReadSettings,
    onCallBack: (List<Boolean>, List<List<String>>) -> Unit
) {
    withContext(Dispatchers.Default) {
        val newPages = mutableListOf<List<String>>()
        var startIndex = 0
        val totalLength = content.length
        val paddingPx = with(density) { readSettings.fontSize.dp.toPx() }
        val tempIsIndent = MutableList(1) { true }
        while (startIndex < totalLength) {
            // 初始猜测：从当前索引开始，向后移动约一页的字符数
            var low = startIndex
            var high = totalLength
            var bestFitIndex = low
            var textToMeasure: List<String>?
            var best: List<String> = emptyList()
            // 二分查找当前页能容纳的最大字符索引
            while (low <= high) {
                val mid = (low + high) / 2
                textToMeasure =
                    content.substring(startIndex, mid)
                        .split("\n")
                        .filter { it.isNotEmpty() }
                val sumHeight = textToMeasure.mapIndexed { index, it ->
                    textMeasurer.measure(
                        text = it,
                        style = TextStyle(
                            fontSize = readSettings.fontSize.sp,
                            lineHeight = readSettings.lineHeight.sp,
                            letterSpacing = readSettings.letterSpacing.sp,
                            fontFamily = readSettings.fontFamily,
                            textIndent = if (index == 0) {
                                if (tempIsIndent.last()) {
                                    TextIndent(firstLine = (readSettings.fontSize * 2).sp)
                                } else {
                                    null
                                }
                            } else {
                                TextIndent(firstLine = (readSettings.fontSize * 2).sp)
                            },
                        ),
                        constraints = Constraints(
                            maxWidth = widthPx.toInt(),
                            maxHeight = Int.MAX_VALUE
                        )
                    )
                }.sumOf { it.size.height }
                // 使用 TextMeasurer 进行精确测量
                if ((sumHeight + (textToMeasure.size - 1) * paddingPx) <= heightPx) {
                    bestFitIndex = mid
                    low = mid + 1
                    best = textToMeasure
                } else {
                    high = mid - 1
                }
            }
            best.let {
                if (it.isNotEmpty()) {
                    newPages.add(best)
                    tempIsIndent.add(it.last().last() == '\n')
                }  // 避免添加空页
            }
            startIndex = bestFitIndex
        }
        onCallBack(tempIsIndent.toList(), newPages)
    }
}

@Composable
fun BookReadView(
    context: Context,
    navController: NavController,
    bookId: Int,
    chapterId: Int = -1,
    viewModel: BookReadViewModel = viewModel()
) {

    val activity = LocalContext.current as Activity
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.loadBookAndCatalog(bookId, chapterId)
    }
    // 控制系统栏显示/隐藏
    LaunchedEffect(uiState.isSystemBarsHidden) {
        if (uiState.isSystemBarsHidden) {
            SystemBarUtils.hideSystemBars(activity)
        } else {
            SystemBarUtils.showSystemBars(activity)
        }
    }

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


    // 当界面销毁时恢复系统栏显示
    DisposableEffect(Unit) {
        onDispose {
            SystemBarUtils.showSystemBars(activity)
        }
    }

    // 分页逻辑
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    var availableHeight by remember { mutableStateOf(0.dp) }
    CommonPage(
        uiState = uiState,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background)
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
            ) {

                LaunchedEffect(Unit) {
                    viewModel.viewModelScope.launch(Dispatchers.Default) {
                        availableHeight = maxHeight
                        val height = with(density) { maxHeight.toPx() }
                        val availableWidth = with(density) { maxWidth.toPx() }
                        while (true) {
                            val it = viewModel.bookChapterChannel.receive()
                            if (it.chapterId > 0) {
                                measureText(
                                    content = it.chapterContent,
                                    density = density,
                                    textMeasurer = textMeasurer,
                                    readSettings = readSettings,
                                    heightPx = height,
                                    widthPx = availableWidth,
                                    onCallBack = { indents, contents ->
                                        viewModel.addPages(
                                            it.chapterId,
                                            indents = indents,
                                            contents = contents
                                        )
                                    }
                                )
                            }
                        }
                    }

                }
            }
            if (uiState.pages.isNotEmpty()) {
                InfiniteHorizontalPager(
                    items = uiState.pages.indices.toList(),
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { viewModel.toggleSystemBars() },
                    onForward = {
                        viewModel.refreshPages(+1, it)
                        Log.d(TAG, "BookReadView: onForward $it")
                    },
                    onBack = {
                        viewModel.refreshPages(-1, it)
                        Log.d(TAG, "BookReadView: onBack $it")
                    }
                ) { page ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.statusBars)
                    ) {
                        Text(
                            uiState.catalog[uiState.currentIndex].title,
                            modifier = Modifier.fillMaxWidth(),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                        ChapterPage(
                            content = uiState.pages[page].contents,
                            settings = readSettings,
                            firstIndent = uiState.pages[page].firstLineIndent,
                            modifier = Modifier
                                .weight(1f)
                        )
                    }
                }
            }
            AnimatedVisibility(
                uiState.showBottomControls,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .background(color = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    // 目录抽屉
                    AnimatedVisibility(
                        uiState.showCatalog,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(availableHeight * 2 / 3)
                    ) {
                        Column(
                            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)
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
                                currentChapterId = uiState.catalog[uiState.currentIndex].id,
                                onChapterSelected = { chapterId ->
                                    viewModel.goChapterId(chapterId)
                                },
                                onDismiss = { viewModel.toggleCatalog() },
                                onReverseCatalog = { isAscending = !isAscending }
                            )
                        }

                    }
                    AnimatedVisibility(uiState.showSettings) {
                        ReadingSettings(
                            settings = readSettings,
                            onSettingsChange = { newSettings ->
                                viewModel.viewModelScope.launch(Dispatchers.IO) {
                                    settingsRepository.updateSettings(newSettings)
                                }
                            },
                        )
                    }
                    BottomControlBar(
                        onPreviousClick = { viewModel.goChapter(-1) },
                        onNextClick = { viewModel.goChapter(+1) },
                        onCatalogClick = {
                            viewModel.toggleCatalog()
                        },
                        onSettingsClick = { viewModel.toggleSettings() },
                        onDismiss = { viewModel.hideAllDialogs() },
                        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                    )
                }
            }


        }
    }
}

@Composable
private fun ChapterPage(
    modifier: Modifier = Modifier,
    content: List<String>,
    settings: ReadSettings,
    firstIndent: Boolean = false,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(settings.fontSize.dp)
    ) {
        content.forEachIndexed { index, paragraph ->
            Text(
                text = paragraph,
                modifier = Modifier.padding(horizontal = 8.dp),
                style = TextStyle(
                    fontSize = settings.fontSize.sp,
                    lineHeight = settings.lineHeight.sp,
                    letterSpacing = settings.letterSpacing.sp,
                    textIndent = if (index == 0) {
                        if (firstIndent) {
                            TextIndent(firstLine = (settings.fontSize * 2).sp)
                        } else {
                            null
                        }

                    } else TextIndent(firstLine = (settings.fontSize * 2).sp),
                    fontFamily = settings.fontFamily
                )
            )
        }
    }
}