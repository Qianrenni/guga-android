package com.qianrenni.reading.viewmodels.book

import android.app.Application
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
    val chapterContent: List<String> = emptyList(),
    val showCatalog: Boolean = false,
    val showSettings: Boolean = false,
    val showBottomControls: Boolean = false,
    val currentIndex: Int = -1,
    val isSystemBarsHidden: Boolean = true,
    override val pageStatus: CommonPageStatus = CommonPageStatus()
) : CommonUiState

class BookReadViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(BookReadUiState())
    val uiState: StateFlow<BookReadUiState> = _uiState.asStateFlow()

    private var heartbeatJob: Job? = null

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
                    .mapIndexed { index, it -> it.copy(title = "第${index + 1}章 ${it.title}") }
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
            _uiState.update { it.copy(pageStatus = it.pageStatus.loading()) }
            // 上报离开当前章节
            if (_uiState.value.currentChapterId > 0) {
                reportChapterRead(_uiState.value.currentChapterId, "exit")
                stopHeartbeat()
            }

            // 获取章节内容
            val result = BookService.getChapter(chapterId, bookId)
            result.onSuccess { data ->
                val processedContent = processContent(data)
                _uiState.update {
                    it.copy(
                        chapterContent = processedContent,
                        currentChapterId = chapterId,
                        pageStatus = it.pageStatus.down(),
                        currentIndex = it.catalog.indexOfFirst { item -> item.id == chapterId }
                    )
                }
                // 上报进入新章节
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

    private fun processContent(rawContent: String): List<String> {
        // 简单处理：如果是纯文本，转换为段落格式
        return rawContent.split("\n").filter { it.isNotBlank() }
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
