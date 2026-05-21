package com.qianrenni.reading.views.book

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.qianrenni.reading.data.model.ShelfItem
import com.qianrenni.reading.viewmodels.book.ShelfViewModel

@Composable
fun BookShelfView(
    navController: NavController,
    viewModel: ShelfViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CommonPage(uiState, refresh = { viewModel.loadShelf() }) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                count = uiState.books.size,
                key = { uiState.books[it].id }
            ) { index ->
                ShelfItemCard(
                    shelfItem = uiState.shelfItems[index],
                    book = uiState.books[index],
                    onClick = { bookId, chapterId ->
                        navController.navigate("read/$bookId/$chapterId")
                    },
                    onDelete = {
                        viewModel.removeFromShelf(it.book_id)
                    }
                )
            }
        }
    }
}

@Composable
fun ShelfItemCard(
    shelfItem: ShelfItem,
    book: Book,
    onClick: (Int, Int) -> Unit,
    onDelete: (ShelfItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(shelfItem.book_id, shelfItem.last_chapter_id ?: 0) }
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = book.cover,
                    placeholder = painterResource(R.drawable.skeleton),
                    error = painterResource(R.drawable.skeleton)
                ),
                contentDescription = book.name,
                modifier = Modifier
                    .width(96.dp)
                    .height(144.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
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
                shelfItem.last_read_at?.let {
                    Text(
                        text = "上次阅读: ${it.split("T")[0]}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { onClick(book.id, shelfItem.last_chapter_id ?: 0) },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("继续阅读")
                }
            }
        }
    }
}
