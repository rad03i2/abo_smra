@file:OptIn(ExperimentalMaterial3Api::class)

package com.abosmra.app.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Factory
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.LocalGasStation
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.abosmra.app.AppState
import com.abosmra.app.AppViewModel
import com.abosmra.app.EntryKind
import com.abosmra.app.FactorySummary
import com.abosmra.app.LedgerEntry
import com.abosmra.app.factoryLedger
import com.abosmra.app.fmt
import com.abosmra.app.fmtDate
import com.abosmra.app.isSettled
import com.abosmra.app.money
import kotlin.math.abs

@Composable
fun FactoriesScreen(state: AppState, vm: AppViewModel, onOpen: (Long) -> Unit) {
    var form by remember { mutableStateOf<Form?>(null) }
    FormHost(form, state, vm) { form = null }
    val cur = state.settings.currency

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader(
                title = "المعامل",
                subtitle = "${state.factories.size} معمل • المستحق لهم ${money(state.totalFactoryDebt, cur)}"
            ) {
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HeaderStat("إجمالي التعبئات", state.refills.size.toString(), Modifier.weight(1f))
                    HeaderStat("اسطوانات معبّأة", state.refills.sumOf { it.qty }.toString(), Modifier.weight(1f))
                    HeaderStat("إجمالي التكلفة", state.refills.sumOf { it.cost() }.fmt(), Modifier.weight(1.4f))
                }
            }

            if (state.factories.isEmpty()) {
                EmptyState(
                    Icons.Rounded.Factory,
                    "لا توجد معامل بعد",
                    "أضف معمل التعبئة (مثل معمل الرحمة أو معمل الهدى) لتسجّل التعبئات وحساباتها."
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.factories.sortedByDescending { it.debt }, key = { it.factory.id }) { s ->
                        AppCard(Modifier.fillMaxWidth(), onClick = { onOpen(s.factory.id) }) {
                            Row(
                                Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconBadge(Icons.Rounded.Factory, Blue, BlueSoft, 46.dp)
                                Column(
                                    Modifier
                                        .weight(1f)
                                        .padding(horizontal = 12.dp)
                                ) {
                                    Text(
                                        s.factory.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = InkDark,
                                        maxLines = 1
                                    )
                                    Text(
                                        "${s.refillCount} تعبئة • ${s.qtyRefilled} اسطوانة",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = InkMid
                                    )
                                }
                                DebtLabel(s.debt, cur)
                            }
                        }
                    }
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = { form = Form.EditFactory(null) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Flame,
            contentColor = Color.White,
            icon = { Icon(Icons.Rounded.Add, null) },
            text = { Text("معمل جديد") }
        )
    }
}

@Composable
private fun HeaderStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(value, color = Color.White, style = MaterialTheme.typography.titleMedium, maxLines = 1)
        Text(label, color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun FactoryDetailScreen(id: Long, state: AppState, vm: AppViewModel, onBack: () -> Unit) {
    val summary = state.factory(id)
    if (summary == null) {
        LaunchedEffect(state.loaded) { if (state.loaded) onBack() }
        Box(Modifier.fillMaxSize())
    } else {
        FactoryDetailContent(summary, state, vm, onBack)
    }
}

@Composable
private fun FactoryDetailContent(
    s: FactorySummary,
    state: AppState,
    vm: AppViewModel,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val cur = state.settings.currency
    var form by remember { mutableStateOf<Form?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }
    var entryToDelete by remember { mutableStateOf<LedgerEntry?>(null) }
    FormHost(form, state, vm) { form = null }

    val ledger = factoryLedger(
        s.factory,
        state.refills.filter { it.factoryId == s.factory.id },
        state.factoryPayments.filter { it.factoryId == s.factory.id },
        cur
    )

    if (confirmDelete) {
        ConfirmDialog(
            title = "حذف المعمل؟",
            text = "سيتم حذف ${s.factory.name} وكل تعبئاته ودفعاته نهائيًا. سيتغير حساب المخزون تبعًا لذلك.",
            onConfirm = {
                confirmDelete = false
                vm.deleteFactory(s.factory.id)
                onBack()
            },
            onDismiss = { confirmDelete = false }
        )
    }
    entryToDelete?.let { e ->
        ConfirmDialog(
            title = "حذف العملية؟",
            text = "${e.title} بتاريخ ${fmtDate(e.date)}",
            onConfirm = {
                when (e.kind) {
                    EntryKind.REFILL -> vm.deleteRefill(e.refId)
                    EntryKind.FACTORY_PAYMENT -> vm.deleteFactoryPayment(e.refId)
                    else -> Unit
                }
                entryToDelete = null
            },
            onDismiss = { entryToDelete = null }
        )
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = s.factory.name,
            subtitle = s.factory.phone.ifBlank { "معمل تعبئة" },
            onBack = onBack,
            actions = {
                IconButton(onClick = { form = Form.EditFactory(s.factory) }) {
                    Icon(Icons.Rounded.Edit, "تعديل", tint = Color.White)
                }
                IconButton(onClick = { confirmDelete = true }) {
                    Icon(Icons.Rounded.Delete, "حذف", tint = Color.White)
                }
            }
        ) {
            Spacer(Modifier.height(14.dp))
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.12f))
                    .padding(16.dp)
            ) {
                Text(
                    when {
                        isSettled(s.debt) -> "الحساب"
                        s.debt > 0 -> "المستحق للمعمل"
                        else -> "رصيد لك عند المعمل"
                    },
                    color = Color.White.copy(alpha = 0.82f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    if (isSettled(s.debt)) "مسدّد ✓" else money(abs(s.debt), cur),
                    color = if (s.debt > 0.5) FlameLight else Color.White,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
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
                    QuickAction(Icons.Rounded.LocalGasStation, "تعبئة", Blue, BlueSoft, Modifier.weight(1f)) {
                        form = Form.NewRefill(s.factory.id)
                    }
                    QuickAction(Icons.Rounded.Payments, "دفعة", Green, GreenSoft, Modifier.weight(1f)) {
                        form = Form.NewFactoryPayment(s.factory.id)
                    }
                    QuickAction(Icons.Rounded.Call, "اتصال", Flame, FlameSoft, Modifier.weight(1f)) {
                        if (s.factory.phone.isNotBlank()) {
                            ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${s.factory.phone}")))
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatTile("عدد التعبئات", s.refillCount.toString(), Icons.Rounded.LocalGasStation, Blue, BlueSoft, Modifier.weight(1f))
                StatTile("اسطوانات معبّأة", s.qtyRefilled.toString(), Icons.Rounded.Inventory2, Flame, FlameSoft, Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatTile("إجمالي التكلفة ($cur)", s.totalCost.fmt(), Icons.Rounded.Description, Teal, TealSoft, Modifier.weight(1f))
                StatTile("إجمالي المدفوع ($cur)", s.totalPaid.fmt(), Icons.Rounded.AccountBalanceWallet, Green, GreenSoft, Modifier.weight(1f))
            }
            if (s.factory.notes.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                AppCard(
                    Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        s.factory.notes,
                        Modifier.padding(14.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkMid
                    )
                }
            }

            SectionTitle("كشف حساب المعمل")
            LedgerList(ledger, cur) { entryToDelete = it }
            Spacer(Modifier.height(32.dp))
        }
    }
}
