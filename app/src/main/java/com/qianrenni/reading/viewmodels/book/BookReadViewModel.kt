package com.qianrenni.reading.viewmodels.book

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qianrenni.reading.data.api.BookService
import com.qianrenni.reading.data.api.ReportService
import com.qianrenni.reading.data.model.Book
import com.qianrenni.reading.data.model.Catalog
import com.qianrenni.reading.data.model.ReadEvent
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
    val isLoading: Boolean = false,
    val error: String? = null,
    val showCatalog: Boolean = false,
    val showSettings: Boolean = false,
    val showBottomControls: Boolean = false
)

class BookReadViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(BookReadUiState())
    val uiState: StateFlow<BookReadUiState> = _uiState.asStateFlow()

    private var heartbeatJob: Job? = null

    fun loadBookAndCatalog(bookId: Int, initialChapterId: Int) {
        if (_uiState.value.isLoading || _uiState.value.book != null) {
            return
        }
        _uiState.update { it.copy(isLoading = true) }
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
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun loadChapter(chapterId: Int) {
        val bookId = _uiState.value.book?.id ?: return
        if (_uiState.value.currentChapterId == chapterId) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
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
                        isLoading = false
                    )
                }
                // 上报进入新章节
                reportChapterRead(chapterId, "enter")
                startHeartbeat(chapterId)
            }.onFailure { _, _, _ ->
                _uiState.update { it.copy(isLoading = false) }
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
                showCatalog = !it.showCatalog,
                showSettings = false,
                showBottomControls = false
            )
        }
    }

    fun toggleSettings() {
        _uiState.update {
            it.copy(
                showSettings = !it.showSettings,
                showCatalog = false,
                showBottomControls = false
            )
        }
    }

    fun toggleBottomControls() {
        _uiState.update {
            it.copy(
                showBottomControls = !it.showBottomControls,
                showCatalog = false,
                showSettings = false
            )
        }
    }

    fun hideAllDialogs() {
        _uiState.update {
            it.copy(
                showCatalog = false,
                showSettings = false,
                showBottomControls = false
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
