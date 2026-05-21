package com.qianrenni.reading.viewmodels.book

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qianrenni.reading.common.CommonPageStatus
import com.qianrenni.reading.common.CommonUiState
import com.qianrenni.reading.data.api.BookService
import com.qianrenni.reading.data.api.ReadingProgressService
import com.qianrenni.reading.data.api.ShelfService
import com.qianrenni.reading.data.model.AddShelfRequest
import com.qianrenni.reading.data.model.Book
import com.qianrenni.reading.data.model.BookReadingProgress
import com.qianrenni.reading.util.SnackBarManager
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HistoryViewModel : ViewModel() {

    data class UiState(
        val historyItems: List<BookReadingProgress> = emptyList(),
        val shelfIds: Set<Int> = emptySet(),
        val books: List<Book> = emptyList<Book>(),
        override val pageStatus: CommonPageStatus = CommonPageStatus(),
    ) : CommonUiState

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun loadHistory() {
        if (_uiState.value.pageStatus.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(pageStatus = it.pageStatus.loading()) }
            val historyItemsJob = async { ReadingProgressService.getReadingProgress() }
            val shelfItemsJob = async { ShelfService.getShelf() }
            historyItemsJob.await().onSuccess { historyItems ->
                _uiState.update { it.copy(historyItems = historyItems.sortedBy { it.book_id }) }
                val bookIds = historyItems.map { it.book_id }
                bookIds
            }?.let { bookIds ->
                if (bookIds.isNotEmpty()) {
                    BookService.getBooksByIds(bookIds).onSuccess { books ->
                        _uiState.update { it.copy(books = books.toList().sortedBy { it.id }) }
                    }
                }
            }
            shelfItemsJob.await().onSuccess { shelfItems ->
                _uiState.update { state ->
                    state.copy(shelfIds = shelfItems.map { it.book_id }.toSet())
                }
            }
            _uiState.update { it.copy(pageStatus = it.pageStatus.down()) }
        }
    }

    fun deleteHistory(bookId: Int) {
        viewModelScope.launch {
            val result = ReadingProgressService.deleteReadingProgress(bookId)
            result.onSuccess {
                this.launch {
                    SnackBarManager.showMessage("删除成功")
                }
                _uiState.update { state ->
                    state.copy(historyItems = state.historyItems.filter { it.book_id != bookId })
                }
            }
            result.onFailure { msg, _, _ ->
                this.launch {
                    SnackBarManager.showMessage(msg)
                }
            }
        }
    }

    fun addToShelf(bookId: Int) {
        viewModelScope.launch {
            val result =
                ShelfService.addToShelf(AddShelfRequest(book_id = bookId))
            result.onEmpty {
                this.launch {
                    SnackBarManager.showMessage("添加成功")
                }
                _uiState.update { state ->
                    state.copy(shelfIds = state.shelfIds + bookId)
                }
            }
            result.onFailure { msg, _, _ ->
                this.launch {
                    SnackBarManager.showMessage(msg)
                }
            }
        }
    }
}
