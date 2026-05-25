package com.qianrenni.reading.viewmodels.book

import android.app.Application
import android.util.Log
import androidx.collection.LruCache
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qianrenni.reading.common.CommonPageStatus
import com.qianrenni.reading.common.CommonUiState
import com.qianrenni.reading.data.api.BookService
import com.qianrenni.reading.data.api.ReportService
import com.qianrenni.reading.data.model.Book
import com.qianrenni.reading.data.model.Catalog
import com.qianrenni.reading.data.model.ReadEvent
import com.qianrenni.reading.util.indexToCN
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BookChapter(val chapterId: Int, val chapterContent: String)
data class PageChapterItem(
    val contents: List<String>,
    val firstLineIndent: Boolean
)

data class BookReadUiState(
    val book: Book? = null,
    val catalog: List<Catalog> = emptyList(),
    val pages: List<PageChapterItem> = emptyList(),
    val showCatalog: Boolean = false,
    val showSettings: Boolean = false,
    val showBottomControls: Boolean = false,
    val currentIndex: Int = -1,
    val isSystemBarsHidden: Boolean = true,
    override val pageStatus: CommonPageStatus = CommonPageStatus()
) : CommonUiState

private val TAG = "BookReadViewModel"

class BookReadViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(BookReadUiState())
    val uiState: StateFlow<BookReadUiState> = _uiState.asStateFlow()
    private val chaptersCache = LruCache<Int, List<PageChapterItem>>(5)
    val bookChapterChannel = Channel<BookChapter>()
    private val PAGE_SIZE = 3
    private var heartbeatJob: Job? = null
    var currentChapterPageIndex: Int = 0
    var currentPageIndex: Int = 0
    fun clear() {
        chaptersCache.evictAll()
        _uiState.update { it.copy(pages = emptyList()) }
    }

    fun catalogIndexToLoad(index: Int): List<Int> {
        val catalog = uiState.value.catalog
        return listOf((index - 1 + catalog.size) % catalog.size, index, (index + 1) % catalog.size)
    }

    fun refreshPages(step: Int = 0, currentPage: Int = currentPageIndex) {
        val indexArr = IntArray(PAGE_SIZE)
        val catalog = uiState.value.catalog
        val indexes = catalogIndexToLoad(uiState.value.currentIndex)
        val items = indexes.flatMapIndexed { index, it ->
            val item = chaptersCache[catalog[it].id]!!
            indexArr[index] = item.size
            item
        }
        currentChapterPageIndex += step
        val targetIndex = indexArr[0] + currentChapterPageIndex
        var updateCurrentIndex = uiState.value.currentIndex
        if (currentChapterPageIndex < 0) {
            updateCurrentIndex = (updateCurrentIndex - 1 + catalog.size) % catalog.size
            currentChapterPageIndex = indexArr[0] - 1
        } else if (currentChapterPageIndex == indexArr[1]) {
            updateCurrentIndex = (updateCurrentIndex + 1) % catalog.size
            currentChapterPageIndex = 0
        }
        catalogIndexToLoad(updateCurrentIndex).forEach { loadChapter(catalog[it].id) }

        val updateItems = listOf(targetIndex, targetIndex - 1, targetIndex + 1).map { items[it] }
        val pagesOrder = listOf(
            currentPage,
            (currentPage - 1 + PAGE_SIZE) % PAGE_SIZE,
            (currentPage + 1) % PAGE_SIZE
        )
        _uiState.update { state ->
            state.copy(
                pages = pagesOrder.zip(updateItems).sortedBy { it.first }.map { it.second },
                currentIndex = updateCurrentIndex
            )
        }
        Log.d(TAG, "refreshPages: ${uiState.value.pages}")
        currentPageIndex = currentPage
    }

    fun addPages(chapterId: Int, indents: List<Boolean>, contents: List<List<String>>) {
        val pageChapterItem = contents.mapIndexed { index, strings ->
            PageChapterItem(
                firstLineIndent = indents[index],
                contents = strings
            )
        }
        chaptersCache.put(chapterId, pageChapterItem)
        if (chaptersCache.size() == PAGE_SIZE && uiState.value.pages.isEmpty()) {
            refreshPages()
        }
    }

    fun loadBookAndCatalog(bookId: Int, initialChapterId: Int) {
        clear()
        val currentState = _uiState.value
        if (currentState.pageStatus.isLoading
            || (currentState.book != null && currentState.book.id == bookId)
        ) {
            return
        }
        if (currentState.catalog.isNotEmpty() && currentState.currentIndex != -1 && currentState.catalog[currentState.currentIndex].id == initialChapterId) {
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
                    _uiState.update {
                        it.copy(
                            currentIndex = it.catalog.indexOfFirst { item -> item.id == chapterIdToLoad },
                        )
                    }
                    val catalog = uiState.value.catalog
                    catalogIndexToLoad(uiState.value.currentIndex).forEach {
                        loadChapter(catalog[it].id)
                    }
                }
            }
            _uiState.update { it.copy(pageStatus = it.pageStatus.down()) }
        }
    }

    fun loadChapter(chapterId: Int) {
        chaptersCache.get(chapterId)?.let { return }
        val bookId = _uiState.value.book?.id ?: return
        viewModelScope.launch {
            // 获取章节内容
            val result = BookService.getChapter(chapterId, bookId)
            result.onSuccess { data ->
                this.launch {
                    bookChapterChannel.send(
                        BookChapter(
                            chapterId = chapterId,
                            chapterContent = data
                        )
                    )
                }
                _uiState.update {
                    it.copy(
                        pageStatus = it.pageStatus.down()
                    )
                }
            }
        }
    }

    fun goToPreviousChapter() {
        val currentIndex = uiState.value.currentIndex
        val catalog = uiState.value.catalog
        val updateCurrentIndex = (currentIndex - 1 + catalog.size) % catalog.size
        loadChapter(catalog[updateCurrentIndex].id)
        _uiState.update { it.copy(currentIndex = updateCurrentIndex) }
        currentChapterPageIndex = 0
        refreshPages()
    }

    fun goToNextChapter() {
        val currentIndex = uiState.value.currentIndex
        val catalog = uiState.value.catalog
        val updateCurrentIndex = (currentIndex + 1) % catalog.size
        loadChapter(catalog[updateCurrentIndex].id)
        _uiState.update { it.copy(currentIndex = updateCurrentIndex) }
        currentChapterPageIndex = 0
        refreshPages()
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
        if (uiState.value.currentIndex > 0) {
            reportChapterRead(uiState.value.catalog[uiState.value.currentIndex].id, "exit")
        }
        stopHeartbeat()
    }
}
