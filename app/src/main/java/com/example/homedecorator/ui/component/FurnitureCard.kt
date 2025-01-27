package com.example.homedecorator.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.homedecorator.data.model.FurnitureItem

@Composable
fun FurnitureCard(
    item: FurnitureItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false
) {
    Card(
        modifier = modifier
            .width(160.dp)
            .height(200.dp)
            .border(
                BorderStroke(2.dp, if (isSelected) Color.Blue else Color.Transparent),
                shape = MaterialTheme.shapes.medium
            ),
        onClick = onClick
    ) {
        Column {
            Image(
                painter = painterResource(id = item.thumbnailResId),
                contentDescription = item.name,
                modifier = Modifier.height(140.dp).fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}
