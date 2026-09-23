package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.CurrencyFormatter

data class PricePoint(
    val label: String,
    val price: Double
)

@Composable
fun PriceHistoryChart(
    ingredientName: String,
    unit: String,
    currencySymbol: String,
    history: List<PricePoint>
) {
    if (history.isEmpty()) return

    val minPrice = history.minOf { it.price }
    val maxPrice = history.maxOf { it.price }
    val priceRange = if (maxPrice > minPrice) maxPrice - minPrice else 1.0

    val firstPrice = history.first().price
    val lastPrice = history.last().price
    val priceDiff = lastPrice - firstPrice
    val percentChange = if (firstPrice > 0) (priceDiff / firstPrice) * 100.0 else 0.0

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Price Trend 📈 ($ingredientName)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Rate per $unit",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .background(
                            if (priceDiff > 0) Color(0xFFFEF2F2) else Color(0xFFECFDF5),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${if (priceDiff >= 0) "+" else ""}${CurrencyFormatter.formatPercent(percentChange)}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (priceDiff > 0) Color(0xFFB91C1C) else Color(0xFF047857)
                        )
                    )
                }
            }

            // Visual Canvas Graph
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .border(1.dp, Color.LightGray.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                val lineColor = if (priceDiff > 0) Color(0xFFEF4444) else Color(0xFF10B981)

                Canvas(modifier = Modifier.matchParentSize()) {
                    val w = size.width
                    val h = size.height
                    val pointsCount = history.size

                    if (pointsCount == 1) {
                        drawCircle(
                            color = lineColor,
                            radius = 6f,
                            center = Offset(w / 2, h / 2)
                        )
                    } else {
                        val path = Path()
                        history.forEachIndexed { i, pt ->
                            val x = i * (w / (pointsCount - 1))
                            val normalizedY = ((pt.price - minPrice) / priceRange).toFloat()
                            val y = h - (normalizedY * (h - 20f) + 10f)

                            if (i == 0) {
                                path.moveTo(x, y)
                            } else {
                                path.lineTo(x, y)
                            }

                            drawCircle(
                                color = lineColor,
                                radius = 4f,
                                center = Offset(x, y)
                            )
                        }

                        drawPath(
                            path = path,
                            color = lineColor,
                            style = Stroke(width = 3.dp.toPx())
                        )
                    }
                }
            }

            // Timeline Price Values
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                history.forEach { pt ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = CurrencyFormatter.format(pt.price, currencySymbol),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = pt.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
