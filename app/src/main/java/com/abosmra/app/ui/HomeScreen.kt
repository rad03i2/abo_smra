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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LocalGasStation
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Sell
import androidx.compose.material.icons.rounded.TrendingUp
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abosmra.app.AppState
import com.abosmra.app.AppViewModel
import com.abosmra.app.EntryKind
import com.abosmra.app.dailyQty
import com.abosmra.app.fmt
import com.abosmra.app.fmtDay
import com.abosmra.app.fmtDayMonth
import com.abosmra.app.fmtWeekday
import com.abosmra.app.money
import com.abosmra.app.recentActivity
import com.abosmra.app.startOfDay

fun kindIcon(kind: EntryKind): ImageVector = when (kind) {
    EntryKind.OPENING -> Icons.Rounded.History
    EntryKind.SALE -> Icons.Rounded.Sell
    EntryKind.PAYMENT -> Icons.Rounded.Payments
    EntryKind.REFILL -> Icons.Rounded.LocalGasStation
    EntryKind.FACTORY_PAYMENT -> Icons.Rounded.AccountBalanceWallet
}

fun kindTint(kind: EntryKind): Color = when (kind) {
    EntryKind.OPENING -> InkMid
    EntryKind.SALE -> Flame
    EntryKind.PAYMENT -> Green
    EntryKind.REFILL -> Blue
    EntryKind.FACTORY_PAYMENT -> Teal
}

fun kindBg(kind: EntryKind): Color = when (kind) {
    EntryKind.OPENING -> Color(0xFFE9EEF6)
    EntryKind.SALE -> FlameSoft
    EntryKind.PAYMENT -> GreenSoft
    EntryKind.REFILL -> BlueSoft
    EntryKind.FACTORY_PAYMENT -> TealSoft
}

@Composable
private fun MiniStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, color = Color.White.copy(alpha = 0.72f), style = MaterialTheme.typography.labelSmall)
        Text(value, color = Color.White, style = MaterialTheme.typography.titleSmall, maxLines = 1)
    }
}

@Composable
private fun StockItem(label: String, count: Int, color: Color, fill: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CylinderIcon(color, fill, Modifier.size(68.dp))
        Text(
            count.toString(),
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
        Text(label, style = MaterialTheme.typography.bodySmall, color = InkMid)
    }
}

@Composable
fun HomeScreen(
    state: AppState,
    vm: AppViewModel,
    onOpenCustomer: (Long) -> Unit,
    goCustomers: () -> Unit,
    goReports: () -> Unit
) {
    var form by remember { mutableStateOf<Form?>(null) }
    FormHost(form, state, vm) { form = null }

    val cur = state.settings.currency
    val now = System.currentTimeMillis()
    val todayStart = startOfDay(now)
    val todaySales = state.sales.filter { it.date >= todayStart }
    val todayPays = state.payments.filter { it.date >= todayStart }
    val todayQty = todaySales.sumOf { it.qty }
    val todayCollected = todaySales.sumOf { it.paid } + todayPays.sumOf { it.amount }
    val todayDebt = todaySales.sumOf { it.total() - it.paid }
    val debtRatio =
        if (state.totalSalesRevenue > 0) (state.totalCustomerDebt / state.totalSalesRevenue).coerceIn(0.0, 1.0) else 0.0

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ScreenHeader(
            title = "أبو سمرة",
            subtitle = "وكيل توزيع الغاز • ${fmtDay(now)}",
            actions = {
                Box(
                    Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    CylinderIcon(Color.White, 0.75f, Modifier.size(32.dp))
                }
            }
        ) {
            Spacer(Modifier.height(16.dp))
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.12f))
                    .padding(16.dp)
            ) {
                Text(
                    "إجمالي الديون على الزبائن",
                    color = Color.White.copy(alpha = 0.82f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    money(state.totalCustomerDebt, cur),
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth()) {
                    MiniStat("عدد المديونين", state.debtorsCount.toString(), Modifier.weight(1f))
                    MiniStat("ديون المعامل", money(state.totalFactoryDebt, cur), Modifier.weight(1.4f))
                    MiniStat("نسبة الدين", "${(debtRatio * 100).toInt()}%", Modifier.weight(0.8f))
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        AppCard(
            Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp)
            ) {
                QuickAction(Icons.Rounded.Sell, "بيع", Flame, FlameSoft, Modifier.weight(1f)) {
                    form = Form.NewSale()
                }
                QuickAction(Icons.Rounded.Payments, "تسديد", Green, GreenSoft, Modifier.weight(1f)) {
                    form = Form.NewPayment()
                }
                QuickAction(Icons.Rounded.LocalGasStation, "تعبئة", Blue, BlueSoft, Modifier.weight(1f)) {
                    form = Form.NewRefill()
                }
                QuickAction(Icons.Rounded.PersonAdd, "زبون جديد", Teal, TealSoft, Modifier.weight(1f)) {
                    form = Form.EditCustomer(null)
                }
            }
        }

        SectionTitle("مخزون الاسطوانات")
        AppCard(
            Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        ) {
            val total = (state.stockFull.coerceAtLeast(0) + state.stockEmpty.coerceAtLeast(0) +
                    state.emptiesAtCustomers.coerceAtLeast(0)).coerceAtLeast(1)
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StockItem("ممتلئة", state.stockFull, Blue, state.stockFull.coerceAtLeast(0).toFloat() / total)
                StockItem("فارغة", state.stockEmpty, Flame, state.stockEmpty.coerceAtLeast(0).toFloat() / total)
                StockItem(
                    "عند الزبائن",
                    state.emptiesAtCustomers,
                    Teal,
                    state.emptiesAtCustomers.coerceAtLeast(0).toFloat() / total
                )
            }
            if (state.stockFull <= 0 && state.refills.isEmpty() && state.settings.openingFull == 0) {
                Text(
                    "اضبط المخزون الافتتاحي من الإعدادات ليبدأ الحساب الدقيق للاسطوانات.",
                    Modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = InkMid
                )
            }
        }

        SectionTitle("اليوم")
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatTile("اسطوانات مباعة", todayQty.toString(), Icons.Rounded.TrendingUp, Flame, FlameSoft, Modifier.weight(1f))
            StatTile("مقبوض ($cur)", todayCollected.fmt(), Icons.Rounded.Payments, Green, GreenSoft, Modifier.weight(1f))
            StatTile("دين جديد ($cur)", todayDebt.fmt(), Icons.Rounded.Warning, Red, RedSoft, Modifier.weight(1f))
        }

        SectionTitle("مبيعات آخر ٧ أيام", "التقارير", goReports)
        AppCard(
            Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        ) {
            val daily = dailyQty(state.sales, 7)
            Column(Modifier.padding(16.dp)) {
                BarChart(
                    values = daily.map { it.second },
                    labels = daily.map { fmtWeekday(it.first) }
                )
            }
        }

        SectionTitle("أعلى المديونين", "كل الزبائن", goCustomers)
        AppCard(
            Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        ) {
            val top = state.customers.filter { it.debt > 0.5 }.sortedByDescending { it.debt }.take(5)
            if (top.isEmpty()) {
                EmptyState(Icons.Rounded.CheckCircle, "لا توجد ديون", "كل الزبائن مسدّدون. ممتاز!")
            } else {
                top.forEach { s ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onOpenCustomer(s.customer.id) }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Avatar(s.customer.name, 42.dp)
                        Column(
                            Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp)
                        ) {
                            Text(s.customer.name, style = MaterialTheme.typography.titleSmall, color = InkDark)
                            val sub = s.customer.area.ifBlank { "بدون منطقة" }
                            Text(sub, style = MaterialTheme.typography.bodySmall, color = InkMid)
                        }
                        DebtLabel(s.debt, cur)
                    }
                }
            }
        }

        SectionTitle("آخر العمليات")
        AppCard(
            Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        ) {
            val recent = recentActivity(state, 8)
            if (recent.isEmpty()) {
                EmptyState(
                    Icons.Rounded.History,
                    "لا توجد عمليات بعد",
                    "ابدأ بإضافة زبون ثم سجّل أول عملية بيع."
                )
            } else {
                recent.forEach { a ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconBadge(kindIcon(a.kind), kindTint(a.kind), kindBg(a.kind), 40.dp)
                        Column(
                            Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp)
                        ) {
                            Text(a.title, style = MaterialTheme.typography.titleSmall, color = InkDark, maxLines = 1)
                            Text(a.subtitle, style = MaterialTheme.typography.bodySmall, color = InkMid, maxLines = 1)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "${a.amount.fmt()} $cur",
                                style = MaterialTheme.typography.labelLarge,
                                color = kindTint(a.kind)
                            )
                            Text(fmtDayMonth(a.time), style = MaterialTheme.typography.labelSmall, color = InkMid)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(28.dp))
    }
}
