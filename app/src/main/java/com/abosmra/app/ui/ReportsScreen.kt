@file:OptIn(ExperimentalMaterial3Api::class)

package com.abosmra.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.LocalGasStation
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Sell
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abosmra.app.AppState
import com.abosmra.app.ReportRange
import com.abosmra.app.buildReport
import com.abosmra.app.dailyQty
import com.abosmra.app.fmt
import com.abosmra.app.fmtDayMonth
import com.abosmra.app.fmtWeekday
import com.abosmra.app.money

@Composable
private fun RangeTabs(selected: ReportRange, onChange: (ReportRange) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ReportRange.values().forEach { r ->
            val sel = r == selected
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (sel) Color.White else Color.Transparent)
                    .clickable { onChange(r) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    r.label,
                    color = if (sel) Navy else Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun ReportsScreen(state: AppState) {
    var range by remember { mutableStateOf(ReportRange.WEEK) }
    val cur = state.settings.currency
    val r = buildReport(state, range)
    val pct = { v: Double -> "${(v * 100).toInt()}%" }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ScreenHeader(title = "التقارير والإحصائيات", subtitle = "تحليل المبيعات والديون والتعبئة") {
            Spacer(Modifier.height(14.dp))
            RangeTabs(range) { range = it }
            Spacer(Modifier.height(16.dp))
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.12f))
                    .padding(16.dp)
            ) {
                Text(
                    "إجمالي المبيعات",
                    color = Color.White.copy(alpha = 0.82f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    money(r.revenue, cur),
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "الربح التقديري: ${money(r.profit, cur)}",
                    color = FlameLight,
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatTile("عمليات البيع", r.salesCount.toString(), Icons.Rounded.Sell, Flame, FlameSoft, Modifier.weight(1f))
            StatTile("اسطوانات مباعة", r.qtySold.toString(), Icons.Rounded.Inventory2, Blue, BlueSoft, Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatTile("مقبوض عند البيع ($cur)", r.paidAtSale.fmt(), Icons.Rounded.Payments, Green, GreenSoft, Modifier.weight(1f))
            StatTile("ديون جديدة ($cur)", r.debtAdded.fmt(), Icons.Rounded.Warning, Red, RedSoft, Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatTile("تسديدات مستلمة ($cur)", r.paymentsIn.fmt(), Icons.Rounded.AccountBalanceWallet, Teal, TealSoft, Modifier.weight(1f))
            StatTile(
                "تعبئات (${r.refillCount})",
                r.qtyRefilled.toString(),
                Icons.Rounded.LocalGasStation,
                Navy,
                BlueSoft,
                Modifier.weight(1f),
                sub = "اسطوانة معبّأة"
            )
        }

        SectionTitle("نسب البيع والديون")
        AppCard(
            Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                RatioBar("نسبة البيع النقدي", r.cashRatio, Green, pct(r.cashRatio))
                RatioBar("نسبة البيع بالدين", r.debtRatio, Red, pct(r.debtRatio))
                RatioBar("نسبة التحصيل (نقد + تسديدات)", r.collectionRatio, Blue, pct(r.collectionRatio))
            }
        }

        SectionTitle(if (range == ReportRange.MONTH || range == ReportRange.ALL) "مبيعات آخر ١٤ يومًا" else "مبيعات آخر ٧ أيام")
        AppCard(
            Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        ) {
            val days = if (range == ReportRange.MONTH || range == ReportRange.ALL) 14 else 7
            val daily = dailyQty(state.sales, days)
            Column(Modifier.padding(16.dp)) {
                BarChart(
                    values = daily.map { it.second },
                    labels = daily.map { if (days > 7) fmtDayMonth(it.first) else fmtWeekday(it.first) },
                    barWidth = if (days > 7) 12.dp else 22.dp
                )
            }
        }

        SectionTitle("المبيعات حسب المنطقة")
        AppCard(
            Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        ) {
            if (r.byArea.isEmpty()) {
                Text(
                    "لا توجد مبيعات في هذه الفترة.",
                    Modifier.padding(16.dp),
                    color = InkMid,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                val maxQty = r.byArea.maxOf { it.qty }.coerceAtLeast(1)
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    r.byArea.forEach { a ->
                        Column {
                            RatioBar(a.area, a.qty.toDouble() / maxQty, Flame, "${a.qty} اسطوانة")
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "مبيعات ${a.revenue.fmt()} $cur • دين ${a.debt.fmt()} $cur",
                                style = MaterialTheme.typography.labelSmall,
                                color = InkMid
                            )
                        }
                    }
                }
            }
        }

        SectionTitle("أكثر الزبائن شراءً")
        AppCard(
            Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        ) {
            if (r.topCustomers.isEmpty()) {
                Text(
                    "لا توجد مبيعات في هذه الفترة.",
                    Modifier.padding(16.dp),
                    color = InkMid,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                r.topCustomers.forEachIndexed { i, t ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(if (i == 0) FlameSoft else Color(0xFFE9EEF6)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                (i + 1).toString(),
                                color = if (i == 0) Flame else InkMid,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Column(
                            Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp)
                        ) {
                            Text(t.name, style = MaterialTheme.typography.titleSmall, color = InkDark)
                            Text(
                                "${t.qty} اسطوانة",
                                style = MaterialTheme.typography.bodySmall,
                                color = InkMid
                            )
                        }
                        Text(
                            "${t.total.fmt()} $cur",
                            style = MaterialTheme.typography.labelLarge,
                            color = Navy
                        )
                    }
                }
            }
        }

        SectionTitle("المعامل")
        AppCard(
            Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                InfoRow("اسطوانات معبّأة في الفترة", "${r.qtyRefilled}")
                InfoRow("تكلفة التعبئة", money(r.refillCost, cur))
                InfoRow("المدفوع للمعامل", money(r.paidToFactories, cur), Green)
                InfoRow("إجمالي المستحق للمعامل حاليًا", money(state.totalFactoryDebt, cur), Red)
            }
        }

        Spacer(Modifier.height(28.dp))
    }
}
