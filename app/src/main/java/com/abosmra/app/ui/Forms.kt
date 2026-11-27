@file:OptIn(ExperimentalMaterial3Api::class)

package com.abosmra.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.abosmra.app.AppState
import com.abosmra.app.AppViewModel
import com.abosmra.app.data.Customer
import com.abosmra.app.data.CustomerPayment
import com.abosmra.app.data.Factory
import com.abosmra.app.data.FactoryPayment
import com.abosmra.app.data.Refill
import com.abosmra.app.data.Sale
import com.abosmra.app.fmt
import com.abosmra.app.isSettled

sealed interface Form {
    data class NewSale(val customerId: Long? = null) : Form
    data class NewPayment(val customerId: Long? = null) : Form
    data class NewRefill(val factoryId: Long? = null) : Form
    data class NewFactoryPayment(val factoryId: Long? = null) : Form
    data class EditCustomer(val customer: Customer?) : Form
    data class EditFactory(val factory: Factory?) : Form
}

@Composable
fun FormHost(form: Form?, state: AppState, vm: AppViewModel, onClose: () -> Unit) {
    when (form) {
        null -> Unit
        is Form.NewSale -> SaleDialog(state, vm, form.customerId, onClose)
        is Form.NewPayment -> PaymentDialog(state, vm, form.customerId, onClose)
        is Form.NewRefill -> RefillDialog(state, vm, form.factoryId, onClose)
        is Form.NewFactoryPayment -> FactoryPayDialog(state, vm, form.factoryId, onClose)
        is Form.EditCustomer -> CustomerFormDialog(state, vm, form.customer, {}, onClose)
        is Form.EditFactory -> FactoryFormDialog(state, vm, form.factory, {}, onClose)
    }
}

// ------------------------------------------------------------------
// عناصر مساعدة
// ------------------------------------------------------------------

@Composable
fun SelectChip(text: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) },
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun TotalBox(rows: List<Triple<String, String, Color>>) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BlueSoft.copy(alpha = 0.55f))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        rows.forEach { (l, v, c) -> InfoRow(l, v, c) }
    }
}

@Composable
private fun WarnText(text: String) {
    Text(
        text,
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AmberSoft)
            .padding(10.dp),
        color = Color(0xFF7A4B00),
        style = MaterialTheme.typography.bodySmall
    )
}

class PickItem(val id: Long, val title: String, val subtitle: String, val trailing: String?)

@Composable
fun ListPickerDialog(
    title: String,
    items: List<PickItem>,
    addLabel: String,
    onPick: (Long) -> Unit,
    onAddNew: () -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val q = query.trim()
    val list = items.filter {
        q.isEmpty() || it.title.contains(q, ignoreCase = true) || it.subtitle.contains(q, ignoreCase = true)
    }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 620.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = InkDark)
                Spacer(Modifier.height(10.dp))
                AppField("بحث", query, { query = it })
                Spacer(Modifier.height(8.dp))
                LazyColumn(Modifier.weight(1f, fill = false)) {
                    items(list, key = { it.id }) { item ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onPick(item.id) }
                                .padding(horizontal = 6.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Avatar(item.title, 40.dp)
                            Column(
                                Modifier
                                    .weight(1f)
                                    .padding(horizontal = 12.dp)
                            ) {
                                Text(item.title, style = MaterialTheme.typography.titleSmall, color = InkDark)
                                if (item.subtitle.isNotBlank()) {
                                    Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = InkMid)
                                }
                            }
                            if (item.trailing != null) {
                                Text(item.trailing, style = MaterialTheme.typography.labelMedium, color = Red)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = onAddNew,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BlueSoft, contentColor = Navy)
                ) {
                    Text(addLabel, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun CustomerPicker(
    state: AppState,
    vm: AppViewModel,
    selectedId: Long?,
    onSelect: (Long) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }
    val sel = selectedId?.let { state.customer(it) }
    PickerField("الزبون", sel?.customer?.name ?: "", "اختر الزبون") { showPicker = true }
    if (showPicker) {
        val cur = state.settings.currency
        ListPickerDialog(
            title = "اختر الزبون",
            items = state.customers.sortedBy { it.customer.name }.map {
                PickItem(
                    it.customer.id, it.customer.name, it.customer.area,
                    if (it.debt > 0.5) "${it.debt.fmt()} $cur" else null
                )
            },
            addLabel = "＋ زبون جديد",
            onPick = { onSelect(it); showPicker = false },
            onAddNew = { showPicker = false; showNew = true },
            onDismiss = { showPicker = false }
        )
    }
    if (showNew) {
        CustomerFormDialog(state, vm, null, { onSelect(it) }, { showNew = false })
    }
}

@Composable
private fun FactoryPicker(
    state: AppState,
    vm: AppViewModel,
    selectedId: Long?,
    onSelect: (Long) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }
    val sel = selectedId?.let { state.factory(it) }
    PickerField("المعمل", sel?.factory?.name ?: "", "اختر المعمل") { showPicker = true }
    if (showPicker) {
        val cur = state.settings.currency
        ListPickerDialog(
            title = "اختر المعمل",
            items = state.factories.sortedBy { it.factory.name }.map {
                PickItem(
                    it.factory.id, it.factory.name, it.factory.phone,
                    if (it.debt > 0.5) "${it.debt.fmt()} $cur" else null
                )
            },
            addLabel = "＋ معمل جديد",
            onPick = { onSelect(it); showPicker = false },
            onAddNew = { showPicker = false; showNew = true },
            onDismiss = { showPicker = false }
        )
    }
    if (showNew) {
        FactoryFormDialog(state, vm, null, { onSelect(it) }, { showNew = false })
    }
}

// ------------------------------------------------------------------
// بيع
// ------------------------------------------------------------------

@Composable
fun SaleDialog(state: AppState, vm: AppViewModel, initialCustomerId: Long?, onDismiss: () -> Unit) {
    val cur = state.settings.currency
    var customerId by remember { mutableStateOf(initialCustomerId) }
    var qty by remember { mutableIntStateOf(1) }
    var priceText by remember { mutableStateOf(state.settings.salePrice.toLong().toString()) }
    var emptyInput by remember { mutableStateOf<Int?>(null) }
    var paidInput by remember { mutableStateOf<String?>(null) }
    var date by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var note by remember { mutableStateOf("") }

    val price = priceText.toDoubleOrNull() ?: 0.0
    val total = qty * price
    val paid = (paidInput ?: total.toLong().toString()).toDoubleOrNull() ?: 0.0
    val empty = emptyInput ?: qty
    val debt = total - paid
    val customer = customerId?.let { state.customer(it) }
    val valid = customer != null && qty > 0 && price > 0 && paid <= total + 0.5

    FormDialog(
        title = "بيع جديد",
        confirmText = "حفظ البيع",
        onDismiss = onDismiss,
        confirmEnabled = valid,
        onConfirm = {
            vm.addSale(
                Sale(
                    customerId = customer!!.customer.id,
                    date = date,
                    qty = qty,
                    unitPrice = price,
                    paid = paid,
                    emptyReturned = empty,
                    note = note.trim()
                )
            )
            onDismiss()
        }
    ) {
        CustomerPicker(state, vm, customerId) { customerId = it }
        if (customer != null && customer.debt > 0.5) {
            WarnText("على هذا الزبون دين سابق: ${customer.debt.fmt()} $cur")
        }
        Stepper("عدد الاسطوانات", qty, { qty = it }, min = 1)
        MoneyField("سعر الاسطوانة", priceText, { priceText = it }, cur)
        Stepper("فارغات مستلمة من الزبون", empty, { emptyInput = it })
        TotalBox(listOf(Triple("المجموع", "${total.fmt()} $cur", Navy)))

        Text("طريقة الدفع", style = MaterialTheme.typography.labelLarge, color = InkMid)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SelectChip("نقدًا بالكامل", total > 0 && paid >= total - 0.5) { paidInput = null }
            SelectChip("نصف المبلغ", total > 0 && paid > 0.5 && paid < total - 0.5) {
                paidInput = (total / 2).toLong().toString()
            }
            SelectChip("آجل (دين)", paid < 0.5) { paidInput = "0" }
        }
        MoneyField(
            "المبلغ المدفوع الآن",
            paidInput ?: total.toLong().toString(),
            { paidInput = it },
            cur,
            isError = paid > total + 0.5
        )
        if (debt > 0.5) {
            TotalBox(listOf(Triple("يُسجَّل دينًا على الزبون", "${debt.fmt()} $cur", Red)))
        } else {
            TotalBox(listOf(Triple("الحالة", "مدفوع بالكامل", Green)))
        }
        if (qty > state.stockFull) {
            WarnText("تنبيه: المخزون الممتلئ الحالي ${state.stockFull} فقط. تأكد من الكمية أو أضف تعبئة.")
        }
        DateField("تاريخ البيع", date) { date = it }
        AppField("ملاحظة (اختياري)", note, { note = it })
    }
}

// ------------------------------------------------------------------
// تسديد من زبون
// ------------------------------------------------------------------

@Composable
fun PaymentDialog(state: AppState, vm: AppViewModel, initialCustomerId: Long?, onDismiss: () -> Unit) {
    val cur = state.settings.currency
    var customerId by remember { mutableStateOf(initialCustomerId) }
    var amountText by remember { mutableStateOf("") }
    var date by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var note by remember { mutableStateOf("") }

    val customer = customerId?.let { state.customer(it) }
    val amount = amountText.toDoubleOrNull() ?: 0.0
    val valid = customer != null && amount > 0

    FormDialog(
        title = "تسديد من زبون",
        confirmText = "حفظ التسديد",
        onDismiss = onDismiss,
        confirmEnabled = valid,
        onConfirm = {
            vm.addPayment(
                CustomerPayment(
                    customerId = customer!!.customer.id,
                    date = date,
                    amount = amount,
                    note = note.trim()
                )
            )
            onDismiss()
        }
    ) {
        CustomerPicker(state, vm, customerId) { customerId = it }
        if (customer != null) {
            TotalBox(
                listOf(
                    Triple(
                        "الدين الحالي",
                        if (isSettled(customer.debt)) "لا يوجد" else "${customer.debt.fmt()} $cur",
                        if (customer.debt > 0.5) Red else Green
                    )
                )
            )
            if (customer.debt > 0.5) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SelectChip("تسديد كامل الدين", amount == customer.debt) {
                        amountText = customer.debt.toLong().toString()
                    }
                }
            }
        }
        MoneyField("المبلغ المقبوض", amountText, { amountText = it }, cur)
        if (customer != null && amount > 0) {
            val left = customer.debt - amount
            TotalBox(
                listOf(
                    Triple(
                        if (left >= 0) "المتبقي بعد التسديد" else "زيادة (رصيد للزبون)",
                        "${kotlin.math.abs(left).fmt()} $cur",
                        if (left > 0.5) Red else Green
                    )
                )
            )
        }
        DateField("تاريخ التسديد", date) { date = it }
        AppField("ملاحظة (اختياري)", note, { note = it })
    }
}

// ------------------------------------------------------------------
// تعبئة من معمل
// ------------------------------------------------------------------

@Composable
fun RefillDialog(state: AppState, vm: AppViewModel, initialFactoryId: Long?, onDismiss: () -> Unit) {
    val cur = state.settings.currency
    var factoryId by remember { mutableStateOf(initialFactoryId) }
    var qty by remember { mutableIntStateOf(10) }
    var costText by remember { mutableStateOf(state.settings.refillCost.toLong().toString()) }
    var paidInput by remember { mutableStateOf<String?>("0") }
    var date by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var note by remember { mutableStateOf("") }

    val cost = costText.toDoubleOrNull() ?: 0.0
    val total = qty * cost
    val paid = (paidInput ?: total.toLong().toString()).toDoubleOrNull() ?: 0.0
    val debt = total - paid
    val factory = factoryId?.let { state.factory(it) }
    val valid = factory != null && qty > 0 && cost >= 0 && paid <= total + 0.5

    FormDialog(
        title = "تعبئة جديدة من معمل",
        confirmText = "حفظ التعبئة",
        onDismiss = onDismiss,
        confirmEnabled = valid,
        onConfirm = {
            vm.addRefill(
                Refill(
                    factoryId = factory!!.factory.id,
                    date = date,
                    qty = qty,
                    unitCost = cost,
                    paid = paid,
                    note = note.trim()
                )
            )
            onDismiss()
        }
    ) {
        FactoryPicker(state, vm, factoryId) { factoryId = it }
        Stepper("عدد الاسطوانات المعبّأة", qty, { qty = it }, min = 1)
        MoneyField("سعر التعبئة للاسطوانة", costText, { costText = it }, cur)
        TotalBox(listOf(Triple("تكلفة التعبئة", "${total.fmt()} $cur", Navy)))

        Text("طريقة الدفع للمعمل", style = MaterialTheme.typography.labelLarge, color = InkMid)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SelectChip("نقدًا بالكامل", total > 0 && paid >= total - 0.5) { paidInput = null }
            SelectChip("آجل (دين)", paid < 0.5) { paidInput = "0" }
        }
        MoneyField(
            "المبلغ المدفوع الآن",
            paidInput ?: total.toLong().toString(),
            { paidInput = it },
            cur,
            isError = paid > total + 0.5
        )
        if (debt > 0.5) {
            TotalBox(listOf(Triple("يُسجَّل دينًا للمعمل", "${debt.fmt()} $cur", Red)))
        } else {
            TotalBox(listOf(Triple("الحالة", "مدفوع بالكامل", Green)))
        }
        if (qty > state.stockEmpty) {
            WarnText("تنبيه: عندك ${state.stockEmpty} اسطوانة فارغة فقط حسب السجل.")
        }
        DateField("تاريخ التعبئة", date) { date = it }
        AppField("ملاحظة (اختياري)", note, { note = it })
    }
}

// ------------------------------------------------------------------
// دفعة لمعمل
// ------------------------------------------------------------------

@Composable
fun FactoryPayDialog(state: AppState, vm: AppViewModel, initialFactoryId: Long?, onDismiss: () -> Unit) {
    val cur = state.settings.currency
    var factoryId by remember { mutableStateOf(initialFactoryId) }
    var amountText by remember { mutableStateOf("") }
    var date by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var note by remember { mutableStateOf("") }

    val factory = factoryId?.let { state.factory(it) }
    val amount = amountText.toDoubleOrNull() ?: 0.0
    val valid = factory != null && amount > 0

    FormDialog(
        title = "دفعة لمعمل",
        confirmText = "حفظ الدفعة",
        onDismiss = onDismiss,
        confirmEnabled = valid,
        onConfirm = {
            vm.addFactoryPayment(
                FactoryPayment(
                    factoryId = factory!!.factory.id,
                    date = date,
                    amount = amount,
                    note = note.trim()
                )
            )
            onDismiss()
        }
    ) {
        FactoryPicker(state, vm, factoryId) { factoryId = it }
        if (factory != null) {
            TotalBox(
                listOf(
                    Triple(
                        "المستحق للمعمل",
                        if (isSettled(factory.debt)) "لا يوجد" else "${factory.debt.fmt()} $cur",
                        if (factory.debt > 0.5) Red else Green
                    )
                )
            )
            if (factory.debt > 0.5) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SelectChip("دفع كامل المستحق", amount == factory.debt) {
                        amountText = factory.debt.toLong().toString()
                    }
                }
            }
        }
        MoneyField("المبلغ المدفوع", amountText, { amountText = it }, cur)
        DateField("تاريخ الدفع", date) { date = it }
        AppField("ملاحظة (اختياري)", note, { note = it })
    }
}

// ------------------------------------------------------------------
// زبون / معمل
// ------------------------------------------------------------------

@Composable
fun CustomerFormDialog(
    state: AppState,
    vm: AppViewModel,
    existing: Customer?,
    onSaved: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val cur = state.settings.currency
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var phone by remember { mutableStateOf(existing?.phone ?: "") }
    var area by remember { mutableStateOf(existing?.area ?: "") }
    var debtText by remember {
        mutableStateOf(if ((existing?.openingDebt ?: 0.0) != 0.0) existing!!.openingDebt.toLong().toString() else "")
    }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }

    FormDialog(
        title = if (existing == null) "زبون جديد" else "تعديل بيانات الزبون",
        confirmText = "حفظ",
        onDismiss = onDismiss,
        confirmEnabled = name.isNotBlank(),
        onConfirm = {
            val c = (existing ?: Customer(name = "")).copy(
                name = name.trim(),
                phone = phone.trim(),
                area = area.trim(),
                notes = notes.trim(),
                openingDebt = debtText.toDoubleOrNull() ?: 0.0
            )
            vm.saveCustomer(c) { id -> onSaved(id) }
            onDismiss()
        }
    ) {
        AppField("اسم الزبون", name, { name = it })
        AppField("رقم الهاتف", phone, { phone = it }, keyboardType = KeyboardType.Phone)
        AppField("المنطقة / القرية", area, { area = it })
        if (state.areas.isNotEmpty()) {
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.areas.forEach { a -> SelectChip(a, area.trim() == a) { area = a } }
            }
        }
        MoneyField("دين سابق (إن وجد)", debtText, { debtText = it }, cur)
        AppField("ملاحظات", notes, { notes = it }, singleLine = false)
    }
}

@Composable
fun FactoryFormDialog(
    state: AppState,
    vm: AppViewModel,
    existing: Factory?,
    onSaved: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val cur = state.settings.currency
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var phone by remember { mutableStateOf(existing?.phone ?: "") }
    var debtText by remember {
        mutableStateOf(if ((existing?.openingDebt ?: 0.0) != 0.0) existing!!.openingDebt.toLong().toString() else "")
    }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }

    FormDialog(
        title = if (existing == null) "معمل جديد" else "تعديل بيانات المعمل",
        confirmText = "حفظ",
        onDismiss = onDismiss,
        confirmEnabled = name.isNotBlank(),
        onConfirm = {
            val f = (existing ?: Factory(name = "")).copy(
                name = name.trim(),
                phone = phone.trim(),
                notes = notes.trim(),
                openingDebt = debtText.toDoubleOrNull() ?: 0.0
            )
            vm.saveFactory(f) { id -> onSaved(id) }
            onDismiss()
        }
    ) {
        AppField("اسم المعمل (مثال: معمل الرحمة)", name, { name = it })
        AppField("رقم الهاتف", phone, { phone = it }, keyboardType = KeyboardType.Phone)
        MoneyField("مستحق سابق للمعمل (إن وجد)", debtText, { debtText = it }, cur)
        AppField("ملاحظات", notes, { notes = it }, singleLine = false)
    }
}
