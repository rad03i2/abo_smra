@file:OptIn(ExperimentalMaterial3Api::class)

package com.abosmra.app.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Sell
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.abosmra.app.CustomerSummary
import com.abosmra.app.EntryKind
import com.abosmra.app.LedgerEntry
import com.abosmra.app.customerLedger
import com.abosmra.app.fmt
import com.abosmra.app.fmtDate
import com.abosmra.app.isSettled
import com.abosmra.app.money
import kotlin.math.abs

@Composable
fun SearchField(query: String, onChange: (String) -> Unit, placeholder: String) {
    OutlinedTextField(
        value = query,
        onValueChange = onChange,
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(Icons.Rounded.Search, null) },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = Flame,
            unfocusedBorderColor = Color.Transparent
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun CustomersScreen(state: AppState, vm: AppViewModel, onOpen: (Long) -> Unit) {
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableIntStateOf(0) }
    var area by remember { mutableStateOf<String?>(null) }
    var sort by remember { mutableIntStateOf(0) }
    var form by remember { mutableStateOf<Form?>(null) }
    FormHost(form, state, vm) { form = null }

    val cur = state.settings.currency
    val q = query.trim()
    var list = state.customers.filter { s ->
        (q.isEmpty() || s.customer.name.contains(q, true) || s.customer.area.contains(q, true) ||
                s.customer.phone.contains(q)) &&
                (area == null || s.customer.area.trim() == area) &&
                when (filter) {
                    1 -> s.debt > 0.5
                    2 -> isSettled(s.debt) || s.debt < 0
                    else -> true
                }
    }
    list = when (sort) {
        1 -> list.sortedBy { it.customer.name }
        2 -> list.sortedByDescending { it.lastActivity }
        else -> list.sortedByDescending { it.debt }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader(
                title = "الزبائن",
                subtitle = "${state.customers.size} زبون • مجموع الديون ${money(state.totalCustomerDebt, cur)}"
            ) {
                Spacer(Modifier.height(14.dp))
                SearchField(query, { query = it }, "ابحث بالاسم أو المنطقة أو الهاتف")
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SelectChip("الكل", filter == 0) { filter = 0 }
                SelectChip("مديونون", filter == 1) { filter = 1 }
                SelectChip("مسدّدون", filter == 2) { filter = 2 }
                Spacer(Modifier.padding(horizontal = 2.dp))
                SelectChip("الأعلى دينًا", sort == 0) { sort = 0 }
                SelectChip("الاسم", sort == 1) { sort = 1 }
                SelectChip("الأحدث", sort == 2) { sort = 2 }
            }
            if (state.areas.isNotEmpty()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(start = 16.dp, end = 16.dp, bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SelectChip("كل المناطق", area == null) { area = null }
                    state.areas.forEach { a -> SelectChip(a, area == a) { area = if (area == a) null else a } }
                }
            }

            if (list.isEmpty()) {
                EmptyState(
                    Icons.Rounded.Groups,
                    if (state.customers.isEmpty()) "لا يوجد زبائن بعد" else "لا نتائج",
                    if (state.customers.isEmpty()) "اضغط «زبون جديد» لإضافة أول زبون."
                    else "جرّب تغيير البحث أو الفلتر."
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(list, key = { it.customer.id }) { s ->
                        AppCard(Modifier.fillMaxWidth(), onClick = { onOpen(s.customer.id) }) {
                            Row(
                                Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Avatar(s.customer.name)
                                Column(
                                    Modifier
                                        .weight(1f)
                                        .padding(horizontal = 12.dp)
                                ) {
                                    Text(
                                        s.customer.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = InkDark,
                                        maxLines = 1
                                    )
                                    val sub = listOf(
                                        s.customer.area.ifBlank { "بدون منطقة" },
                                        "${s.qtyBought} اسطوانة"
                                    ).joinToString(" • ")
                                    Text(sub, style = MaterialTheme.typography.bodySmall, color = InkMid, maxLines = 1)
                                }
                                DebtLabel(s.debt, cur)
                            }
                        }
                    }
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = { form = Form.EditCustomer(null) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Flame,
            contentColor = Color.White,
            icon = { Icon(Icons.Rounded.PersonAdd, null) },
            text = { Text("زبون جديد") }
        )
    }
}

private fun statementText(s: CustomerSummary, ledger: List<LedgerEntry>, cur: String): String {
    val sb = StringBuilder()
    sb.appendLine("كشف حساب: ${s.customer.name}")
    sb.appendLine("التاريخ: ${fmtDate(System.currentTimeMillis())}")
    sb.appendLine()
    when {
        isSettled(s.debt) -> sb.appendLine("الحالة: لا يوجد دين، الحساب مسدّد.")
        s.debt > 0 -> sb.appendLine("الدين الحالي: ${s.debt.fmt()} $cur")
        else -> sb.appendLine("رصيد للزبون: ${abs(s.debt).fmt()} $cur")
    }
    sb.appendLine()
    sb.appendLine("آخر العمليات:")
    ledger.take(10).forEach {
        val sign = if (it.delta > 0.5) "+" else if (it.delta < -0.5) "−" else ""
        sb.appendLine("• ${fmtDate(it.date)}: ${it.title} (${sign}${abs(it.delta).fmt()})")
    }
    sb.appendLine()
    sb.append("أبو سمرة لتوزيع الغاز")
    return sb.toString()
}

@Composable
fun CustomerDetailScreen(id: Long, state: AppState, vm: AppViewModel, onBack: () -> Unit) {
    val summary = state.customer(id)
    if (summary == null) {
        LaunchedEffect(state.loaded) { if (state.loaded) onBack() }
        Box(Modifier.fillMaxSize())
    } else {
        CustomerDetailContent(summary, state, vm, onBack)
    }
}

@Composable
private fun CustomerDetailContent(
    s: CustomerSummary,
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

    val ledger = customerLedger(
        s.customer,
        state.sales.filter { it.customerId == s.customer.id },
        state.payments.filter { it.customerId == s.customer.id },
        cur
    )

    if (confirmDelete) {
        ConfirmDialog(
            title = "حذف الزبون؟",
            text = "سيتم حذف ${s.customer.name} وكل عملياته نهائيًا. لا يمكن التراجع.",
            onConfirm = {
                confirmDelete = false
                vm.deleteCustomer(s.customer.id)
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
                    EntryKind.SALE -> vm.deleteSale(e.refId)
                    EntryKind.PAYMENT -> vm.deletePayment(e.refId)
                    else -> Unit
                }
                entryToDelete = null
            },
            onDismiss = { entryToDelete = null }
        )
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = s.customer.name,
            subtitle = listOf(s.customer.area, s.customer.phone).filter { it.isNotBlank() }
                .joinToString(" • ").ifBlank { "زبون" },
            onBack = onBack,
            actions = {
                IconButton(onClick = { form = Form.EditCustomer(s.customer) }) {
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
                        s.debt > 0 -> "الدين الحالي"
                        else -> "رصيد للزبون"
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
                    QuickAction(Icons.Rounded.Sell, "بيع", Flame, FlameSoft, Modifier.weight(1f)) {
                        form = Form.NewSale(s.customer.id)
                    }
                    QuickAction(Icons.Rounded.Payments, "تسديد", Green, GreenSoft, Modifier.weight(1f)) {
                        form = Form.NewPayment(s.customer.id)
                    }
                    QuickAction(Icons.Rounded.Call, "اتصال", Blue, BlueSoft, Modifier.weight(1f)) {
                        if (s.customer.phone.isNotBlank()) {
                            ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${s.customer.phone}")))
                        }
                    }
                    QuickAction(Icons.Rounded.Share, "مشاركة", Teal, TealSoft, Modifier.weight(1f)) {
                        val send = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, statementText(s, ledger, cur))
                        }
                        ctx.startActivity(Intent.createChooser(send, "مشاركة كشف الحساب"))
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
                StatTile("مرات الشراء", s.salesCount.toString(), Icons.Rounded.ShoppingCart, Flame, FlameSoft, Modifier.weight(1f))
                StatTile("اسطوانات مشتراة", s.qtyBought.toString(), Icons.Rounded.Inventory2, Blue, BlueSoft, Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatTile("إجمالي المشتريات ($cur)", s.totalBought.fmt(), Icons.Rounded.Description, Teal, TealSoft, Modifier.weight(1f))
                StatTile("إجمالي المسدَّد ($cur)", s.totalPaid.fmt(), Icons.Rounded.AccountBalanceWallet, Green, GreenSoft, Modifier.weight(1f))
            }
            if (s.emptiesHeld > 0) {
                Spacer(Modifier.height(10.dp))
                AppCard(
                    Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        "عند الزبون ${s.emptiesHeld} اسطوانة (لم يُرجع فارغاتها بعد)",
                        Modifier.padding(14.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkDark
                    )
                }
            }
            if (s.customer.notes.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                AppCard(
                    Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        s.customer.notes,
                        Modifier.padding(14.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkMid
                    )
                }
            }

            SectionTitle("كشف الحساب")
            LedgerList(ledger, cur) { entryToDelete = it }
            Spacer(Modifier.height(32.dp))
        }
    }
}
