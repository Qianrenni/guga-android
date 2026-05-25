package com.qianrenni.reading.viewmodels.book

import android.app.Application
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qianrenni.reading.common.CommonPageStatus
import com.qianrenni.reading.common.CommonUiState
import com.qianrenni.reading.data.api.BookService
import com.qianrenni.reading.data.api.ReadingProgressService
import com.qianrenni.reading.data.api.ReportService
import com.qianrenni.reading.data.model.Book
import com.qianrenni.reading.data.model.Catalog
import com.qianrenni.reading.data.model.ReadEvent
import com.qianrenni.reading.data.model.UpdateProgressRequest
import com.qianrenni.reading.util.indexToCN
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BookReadUiState(
    val book: Book? = null,
    val catalog: List<Catalog> = emptyList(),
    val currentChapterId: Int = -1,
    val chapterContent: String = "",
    val pages: List<List<String>> = emptyList(),
    val showCatalog: Boolean = false,
    val showSettings: Boolean = false,
    val showBottomControls: Boolean = false,
    val currentIndex: Int = 0,
    val currentPageIndex: Int = 0,
    val isSystemBarsHidden: Boolean = true,
    val availableWidth: Dp? = null,
    val availableHeight: Dp? = null,
    override val pageStatus: CommonPageStatus = CommonPageStatus()
) : CommonUiState

class BookReadViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(BookReadUiState())
    val uiState: StateFlow<BookReadUiState> = _uiState.asStateFlow()

    private var heartbeatJob: Job? = null

    fun updatePages(pages: List<List<String>>) {
        _uiState.update { it.copy(pages = pages) }
    }

    fun updateScreen(width: Dp, height: Dp) {
        if (uiState.value.availableHeight == null) {
            _uiState.update { it.copy(availableWidth = width, availableHeight = height) }
        }

    }

    fun loadBookAndCatalog(bookId: Int, initialChapterId: Int) {
        val currentState = _uiState.value
        if (currentState.pageStatus.isLoading || (currentState.book != null && currentState.book.id == bookId)) {
            return
        }
        _uiState.update { it.copy(pageStatus = it.pageStatus.loading()) }
        viewModelScope.launch {
            val bookJob = async { BookService.getBookById(bookId) }
            val catalogJob = async { BookService.getCatalog(bookId) }
            val bookResult = bookJob.await()
            val catalogResult = catalogJob.await()
            bookResult.onSuccess { data ->
                _uiState.update { it.copy(book = data) }
            }
            catalogResult.onSuccess { data ->
                val catalogList = data.toList()
                    .mapIndexed { index, it -> it.copy(title = "第${indexToCN(index + 1)}章 ${it.title}") }
                _uiState.update { it.copy(catalog = catalogList) }
                val chapterIdToLoad = if (initialChapterId > 0) {
                    initialChapterId
                } else if (catalogList.isNotEmpty()) {
                    catalogList.first().id
                } else {
                    -1
                }
                if (chapterIdToLoad > 0) {
                    // 加载章节内容
                    loadChapter(chapterIdToLoad)
                }
            }
            _uiState.update { it.copy(pageStatus = it.pageStatus.down()) }
        }
    }

    fun loadChapter(chapterId: Int) {
        val bookId = _uiState.value.book?.id ?: return
        if (_uiState.value.currentChapterId == chapterId) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    pageStatus = it.pageStatus.loading(),
                    pages = emptyList(),
                    currentPageIndex = 0
                )
            }
            // 上报离开当前章节
            if (_uiState.value.currentChapterId > 0) {
                reportChapterRead(_uiState.value.currentChapterId, "exit")
                stopHeartbeat()
            }

            // 获取章节内容
            val result = BookService.getChapter(chapterId, bookId)
            result.onSuccess { data ->
                _uiState.update {
                    it.copy(
                        chapterContent = data,
                        currentChapterId = chapterId,
                        pageStatus = it.pageStatus.down(),
                        currentIndex = it.catalog.indexOfFirst { item -> item.id == chapterId },
                    )
                }
                // 触发分页计算 (在 ViewModel 中通过 StateFlow 驱动 UI 重新计算，或者在 UI 层计算)
                // 为了性能，我们通常在 UI 层根据测量结果分页，但这里我们先更新原始内容
                this.launch {
                    ReadingProgressService.updateReadingProgress(
                        UpdateProgressRequest(
                            bookId,
                            chapterId
                        )
                    )
                }
                reportChapterRead(chapterId, "enter")
                startHeartbeat(chapterId)
            }
            result.onFailure { _, _, _ ->
                _uiState.update { it.copy(pageStatus = it.pageStatus.down()) }
            }
        }
    }

    fun goToPreviousChapter() {
        val currentState = _uiState.value
        val currentIndex =
            currentState.catalog.indexOfFirst { it.id == currentState.currentChapterId }

        if (currentIndex > 0) {
            loadChapter(currentState.catalog[currentIndex - 1].id)
        }
    }

    fun goToNextChapter() {
        val currentState = _uiState.value
        val currentIndex =
            currentState.catalog.indexOfFirst { it.id == currentState.currentChapterId }

        if (currentIndex < currentState.catalog.size - 1 && currentIndex >= 0) {
            loadChapter(currentState.catalog[currentIndex + 1].id)
        }
    }

    fun toggleCatalog() {
        _uiState.update {
            it.copy(
                showSettings = false,
                showCatalog = !it.showCatalog,
            )
        }
    }

    fun toggleSettings() {
        _uiState.update {
            it.copy(
                showSettings = !it.showSettings,
                showCatalog = false,
            )
        }
    }


    fun hideAllDialogs() {
        _uiState.update {
            it.copy(
                showCatalog = false,
                showSettings = false,
                showBottomControls = false,
                isSystemBarsHidden = true,
            )
        }
    }

    fun toggleSystemBars() {
        _uiState.update {
            it.copy(
                isSystemBarsHidden = !it.isSystemBarsHidden,
                showBottomControls = !it.showBottomControls,
                showCatalog = false,
                showSettings = false
            )
        }
    }

    fun setCurrentPage(index: Int) {
        _uiState.update { it.copy(currentPageIndex = index) }
    }

    private fun reportChapterRead(chapterId: Int, eventType: String) {
        viewModelScope.launch {
            uiState.value.book?.let { book ->
                ReportService.reportChapterRead(
                    ReadEvent(
                        book_id = book.id,
                        chapter_id = chapterId,
                        event_type = eventType
                    )
                )
            }
        }
    }

    private fun startHeartbeat(chapterId: Int) {
        stopHeartbeat()
        heartbeatJob = viewModelScope.launch {
            while (true) {
                delay(10000) // 每10秒上报一次
                reportChapterRead(chapterId, "heartbeat")
            }
        }
    }

    private fun stopHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = null
    }

    override fun onCleared() {
        super.onCleared()
        // 上报离开当前章节
        if (_uiState.value.currentChapterId > 0) {
            reportChapterRead(_uiState.value.currentChapterId, "exit")
        }
        stopHeartbeat()
    }
}
