package com.qianrenni.reading.viewmodels.book

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qianrenni.reading.data.api.BookService
import com.qianrenni.reading.data.model.Book
import com.qianrenni.reading.data.model.Catalog
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BookInfoViewModel : ViewModel() {

    data class UiState(
        val book: Book? = null,
        val catalog: List<Catalog> = emptyList(),
        val relatedBooks: List<Book> = emptyList(),
        val isLoading: Boolean = false,
        val isError: Boolean = false,
        val errorMessage: String = "",
        val selectedTabIndex: Int = 0,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun loadBookInfo(bookId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isError = false) }

            // 并行加载书籍信息和目录
            val bookJob = async { BookService.getBookById(bookId) }
            val catalogJob = async { BookService.getCatalog(bookId) }
            val bookResult = bookJob.await()
            val catalogResult = catalogJob.await()
            bookResult.onSuccess { book ->
                _uiState.update { state ->
                    state.copy(
                        book = book,
                        isLoading = false
                    )
                }

                // 加载相关推荐
                loadRecommendations(book.tags)
            }

            catalogResult.onSuccess { catalogList ->
                // Catalog 返回的是数组
                _uiState.update { state ->
                    state.copy(
                        catalog = catalogList.toList(),
                        isLoading = false
                    )
                }
            }

            bookResult.onFailure { message, _, _ ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isError = true,
                        errorMessage = message
                    )
                }
                Log.e("BookInfoVM", "Load book failed: $message")
            }

            catalogResult.onFailure { message, _, _ ->
                Log.e("BookInfoVM", "Load catalog failed: $message")
            }
        }
    }

    private fun loadRecommendations(tags: String) {
        viewModelScope.launch {
            if (tags.isEmpty()) return@launch

            val result = BookService.getRecommendations(tags)
            result.onSuccess { books ->
                val currentBookId = _uiState.value.book?.id
                val filtered = books.filter { it.id != currentBookId }.toList()
                _uiState.update {
                    it.copy(relatedBooks = filtered)
                }
            }
            result.onFailure { message, _, _ ->
                Log.e("BookInfoVM", "Load recommendations failed: $message")
            }
        }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTabIndex = index) }
    }
}
