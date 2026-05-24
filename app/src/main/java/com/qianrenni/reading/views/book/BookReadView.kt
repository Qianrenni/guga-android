package com.qianrenni.reading.views.book

import android.app.Activity
import android.content.Context
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
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
import com.qianrenni.reading.components.ReadingSettings
import com.qianrenni.reading.data.model.ReadSettings
import com.qianrenni.reading.data.store.SettingsRepository
import com.qianrenni.reading.util.SystemBarUtils
import com.qianrenni.reading.viewmodels.book.BookReadViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    viewModel.loadBookAndCatalog(bookId, chapterId)
    // 控制系统栏显示/隐藏
    LaunchedEffect(uiState.isSystemBarsHidden) {
        if (uiState.isSystemBarsHidden) {
            SystemBarUtils.hideSystemBars(activity)
        } else {
            SystemBarUtils.showSystemBars(activity)
        }
    }

    var isAscending by remember { mutableStateOf(true) }
    var pageTextIndent by remember { mutableStateOf(BooleanArray(0)) }
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
    CommonPage(
        uiState = uiState
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // 显示章节内容
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.background)
            ) {
                AnimatedVisibility(uiState.catalog.isNotEmpty()) {
                    Text(
                        uiState.catalog[uiState.currentIndex].title,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = MaterialTheme.colorScheme.background)
                    )
                }
                HorizontalDivider(
                    Modifier.height(1.dp),
                    DividerDefaults.Thickness,
                    MaterialTheme.colorScheme.onBackground
                )
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    LaunchedEffect(uiState.chapterContent, readSettings) {
                        val availableHeight = uiState.availableHeight ?: maxHeight
                        val availableWidth = uiState.availableWidth ?: maxWidth
                        viewModel.updateScreen(availableWidth, availableHeight)
                        if (uiState.chapterContent.isNotEmpty()) {
                            withContext(Dispatchers.Default) {
                                val newPages = mutableListOf<List<String>>()
                                var startIndex = 0
                                val totalLength = uiState.chapterContent.length
                                val heightPx = with(density) { availableHeight.toPx() }
                                val widthPx = with(density) { availableWidth.toPx() }
                                val paddingPx = with(density) { readSettings.fontSize.dp.toPx() }
                                val tempIsIndent = MutableList(1) { true }
                                while (startIndex < totalLength) {
                                    // 初始猜测：从当前索引开始，向后移动约一页的字符数
                                    var low = startIndex
                                    var high = totalLength
                                    var bestFitIndex = low
                                    var textToMeasure: List<String>? = null
                                    var best: List<String> = emptyList()
                                    // 二分查找当前页能容纳的最大字符索引
                                    while (low <= high) {
                                        val mid = (low + high) / 2
                                        textToMeasure =
                                            uiState.chapterContent.substring(startIndex, mid)
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
                                pageTextIndent = tempIsIndent.toBooleanArray()
                                viewModel.updatePages(newPages)

                            }
                        }
                    }
                }
                if (uiState.pages.isNotEmpty()) {
                    val pagerState = rememberPagerState(
                        pageCount = { uiState.pages.size },
                        initialPage = uiState.currentPageIndex.coerceIn(
                            0,
                            maxOf(0, uiState.pages.size - 1)
                        )
                    )
                    // 同步页码到 ViewModel
                    LaunchedEffect(pagerState.currentPage) {
                        if (pagerState.currentPage != uiState.currentPageIndex) {
                            viewModel.setCurrentPage(pagerState.currentPage)
                        }
                    }
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { viewModel.toggleSystemBars() }
                    ) { page ->
                        ChapterPage(
                            content = uiState.pages[page],
                            settings = readSettings,
                            firstIndent = pageTextIndent[page],
                            modifier = Modifier.fillMaxSize()
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
                    .then(
                        if (uiState.showCatalog) {
                            Modifier.fillMaxHeight(0.8f)
                        } else {
                            Modifier
                        }
                    )
            ) {
                Column {
                    // 目录抽屉
                    AnimatedVisibility(
                        uiState.showCatalog,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
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
                        canGoPrevious = uiState.currentIndex > 0,
                        canGoNext = uiState.currentIndex < uiState.catalog.size - 1 && uiState.currentIndex >= 0,
                        onPreviousClick = { viewModel.goToPreviousChapter() },
                        onNextClick = { viewModel.goToNextChapter() },
                        onCatalogClick = {
                            viewModel.toggleCatalog()
                        },
                        onSettingsClick = { viewModel.toggleSettings() },
                        onDismiss = { viewModel.hideAllDialogs() }
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