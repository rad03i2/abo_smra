@file:OptIn(ExperimentalMaterial3Api::class)

package com.abosmra.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.abosmra.app.EntryKind
import com.abosmra.app.LedgerEntry
import com.abosmra.app.fmt
import com.abosmra.app.fmtDate
import kotlin.math.abs

/** قائمة كشف الحساب (للزبون أو للمعمل) */
@Composable
fun LedgerList(
    entries: List<LedgerEntry>,
    cur: String,
    onDelete: (LedgerEntry) -> Unit
) {
    var showAll by remember { mutableStateOf(false) }
    val shown = if (showAll) entries else entries.take(30)

    AppCard(
        Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        if (entries.isEmpty()) {
            EmptyState(Icons.Rounded.Description, "لا توجد عمليات", "ستظهر هنا كل العمليات مرتبة بالتاريخ.")
        } else {
            shown.forEach { e ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, end = 6.dp, top = 10.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconBadge(kindIcon(e.kind), kindTint(e.kind), kindBg(e.kind), 40.dp)
                    Column(
                        Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                    ) {
                        Text(e.title, style = MaterialTheme.typography.titleSmall, color = InkDark)
                        Text(
                            e.detail,
                            style = MaterialTheme.typography.bodySmall,
                            color = InkMid,
                            maxLines = 3
                        )
                        Text(fmtDate(e.date), style = MaterialTheme.typography.labelSmall, color = InkMid)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        val up = e.delta > 0.5
                        val zero = abs(e.delta) <= 0.5
                        Text(
                            when {
                                zero -> "0"
                                up -> "+${e.delta.fmt()}"
                                else -> "−${abs(e.delta).fmt()}"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            color = when {
                                zero -> Green
                                up -> Red
                                else -> Green
                            }
                        )
                        Text(
                            "الرصيد ${e.balance.fmt()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = InkMid
                        )
                    }
                    if (e.kind != EntryKind.OPENING) {
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onDelete(e) }
                                .padding(8.dp)
                        ) {
                            Icon(
                                Icons.Rounded.DeleteOutline,
                                "حذف",
                                tint = InkMid,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        Spacer(Modifier.height(1.dp))
                    }
                }
            }
            if (!showAll && entries.size > 30) {
                Text(
                    "عرض كل العمليات (${entries.size})",
                    Modifier
                        .fillMaxWidth()
                        .clickable { showAll = true }
                        .padding(14.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = Blue
                )
            }
        }
    }
}
