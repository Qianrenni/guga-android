package com.qianrenni.reading.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.qianrenni.reading.data.model.Catalog

@Composable
fun CatalogDrawer(
    bookName: String,
    catalog: List<Catalog>,
    currentChapterId: Int,
    onChapterSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var isAscending by remember { mutableStateOf(true) }

    val sortedCatalog = remember(catalog, isAscending) {
        if (isAscending) catalog else catalog.reversed()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        // 书籍名称
        Text(
            text = bookName,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        // 目录标题和排序按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "目录 (${catalog.size}章)",
                style = MaterialTheme.typography.titleMedium
            )

            TextButton(onClick = { isAscending = !isAscending }) {
                Text(if (isAscending) "升序" else "降序")
                Icon(
                    imageVector = Icons.Default.SwapVert,
                    contentDescription = "切换排序",
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // 章节列表
        LazyColumn(
            modifier = Modifier
                .weight(1f)
        ) {
            items(sortedCatalog, key = { it.id }) { item ->
                val isSelected = item.id == currentChapterId
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onChapterSelected(item.id)
                            onDismiss()
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surface
                    )
                ) {
                    Text(
                        text = item.title,
                        style = if (isSelected)
                            MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        else
                            MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
