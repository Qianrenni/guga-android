package com.qianrenni.reading.views.book

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.qianrenni.reading.R
import com.qianrenni.reading.components.CommonPage
import com.qianrenni.reading.data.model.Book
import com.qianrenni.reading.data.model.BookReadingProgress
import com.qianrenni.reading.viewmodels.book.HistoryViewModel

@Composable
fun ReadingHistoryView(
    navController: NavController,
    viewModel: HistoryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CommonPage(uiState = uiState, refresh = { viewModel.loadHistory() }) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.books.size, key = { uiState.books[it].id }) { item ->
                HistoryItemCard(
                    historyItem = uiState.historyItems[item],
                    book = uiState.books[item],
                    isInShelf = uiState.shelfIds.contains(uiState.books[item].id),
                    onClick = { bookId, chapterId ->
                        navController.navigate("read/$bookId/$chapterId")
                    },
                    onDelete = {
                        viewModel.deleteHistory(it.book_id)
                    },
                    onAddToShelf = {
                        viewModel.addToShelf(it.id)
                    }
                )
            }
        }
    }
}

@Composable
fun HistoryItemCard(
    historyItem: BookReadingProgress,
    book: Book,
    isInShelf: Boolean,
    onClick: (Int, Int) -> Unit,
    onDelete: (BookReadingProgress) -> Unit,
    onAddToShelf: (Book) -> Unit
) {
    Row(
        modifier = Modifier
            .height(120.dp)
            .fillMaxWidth()
            .clickable(onClick = { onClick(book.id, historyItem.last_chapter_id) }),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(
                model = book.cover,
                placeholder = painterResource(R.drawable.skeleton),
                error = painterResource(R.drawable.skeleton)
            ),
            contentDescription = book.name,
            modifier = Modifier
                .fillMaxHeight(),
            contentScale = ContentScale.FillHeight
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = book.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = book.author,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "上次阅读: ${historyItem.last_read_at.split("T")[0]}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.align(Alignment.End)
            ) {
                TextButton(onClick = { onClick(book.id, historyItem.last_chapter_id) }) {
                    Text("继续阅读")
                }
                if (!isInShelf) {
                    TextButton(onClick = { onAddToShelf(book) }) {
                        Text("加入书架")
                    }
                }
            }
        }
    }
}
