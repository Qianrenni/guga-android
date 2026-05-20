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
import com.qianrenni.reading.data.model.ShelfItem
import com.qianrenni.reading.util.SnackBarManager
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class ShelfViewModel : ViewModel() {
    data class UiState(
        val shelfItems: List<ShelfItem> = emptyList(),
        val books: List<Book> = emptyList(),
        override val pageStatus: CommonPageStatus = CommonPageStatus(),
    ) : CommonUiState


    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadShelf()
    }

    fun loadShelf() {
        if (uiState.value.pageStatus.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(pageStatus = it.pageStatus.loading()) }

            val shelfItemsJob = async { ShelfService.getShelf() }
            val historyItemJob = async { ReadingProgressService.getReadingProgress() }
            val shelfItemsResult = shelfItemsJob.await()
            var shelfItems = emptyList<ShelfItem>()
            shelfItemsResult.onSuccess {
                shelfItems = it
            }
            shelfItems.map { it.book_id }.let { bookIds ->
                if (bookIds.isNotEmpty()) {
                    BookService.getBooksByIds(bookIds).onSuccess { books ->
                        _uiState.update { it.copy(books = books.toList()) }
                    }
                }
            }
            val historyItemsResult = historyItemJob.await()
            historyItemsResult.onSuccess { historyItems ->
                val historyItemsMap = historyItems.associateBy { it.book_id }
                shelfItems = shelfItems.map { shelfItem ->
                    historyItemsMap.get(shelfItem.book_id)?.let {
                        return@map shelfItem.copy(
                            last_chapter_id = it.last_chapter_id,
                            last_position = it.last_position,
                            last_read_at = it.last_read_at
                        )
                    }
                    return@map shelfItem
                }
            }
            _uiState.update {
                it.copy(
                    shelfItems = shelfItems,
                    pageStatus = it.pageStatus.down()
                )
            }

        }
    }

    fun addToShelf(bookId: Int) {
        viewModelScope.launch {
            val result = ShelfService.addToShelf(AddShelfRequest(bookId))
            result.onSuccess {
                this.launch {
                    SnackBarManager.showMessage("添加成功")
                }
                loadShelf()
            }
            result.onFailure { msg, _, _ ->
                this.launch {
                    SnackBarManager.showMessage(msg)
                }
            }
        }
    }

    fun removeFromShelf(bookId: Int) {
        viewModelScope.launch {
            val result = ShelfService.removeFromShelf(bookId)
            result.onSuccess {
                this.launch {
                    SnackBarManager.showMessage("删除成功")
                }
                _uiState.update { state ->
                    state.copy(shelfItems = state.shelfItems.filter { it.book_id != bookId })
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
