package com.abosmra.app

import com.abosmra.app.data.Customer
import com.abosmra.app.data.CustomerPayment
import com.abosmra.app.data.Factory
import com.abosmra.app.data.FactoryPayment
import com.abosmra.app.data.Refill
import com.abosmra.app.data.Sale
import com.abosmra.app.data.Settings
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

// ------------------------------------------------------------------
// تنسيق
// ------------------------------------------------------------------

private val latinArabic: Locale = Locale.forLanguageTag("ar-IQ-u-nu-latn")

fun Double.fmt(): String =
    NumberFormat.getIntegerInstance(Locale.US).format(Math.round(this))

fun Int.fmt(): String = NumberFormat.getIntegerInstance(Locale.US).format(this)

fun money(v: Double, cur: String): String = "${v.fmt()} $cur"

fun fmtDate(ms: Long): String = SimpleDateFormat("d MMMM yyyy", latinArabic).format(Date(ms))

fun fmtDay(ms: Long): String = SimpleDateFormat("EEEE d MMMM", latinArabic).format(Date(ms))

fun fmtWeekday(ms: Long): String = SimpleDateFormat("EEE", latinArabic).format(Date(ms))

fun fmtDayMonth(ms: Long): String = SimpleDateFormat("d/M", latinArabic).format(Date(ms))

/** أرقام لاتينية فقط (يحوّل الأرقام العربية الهندية إن كتبها المستخدم) */
fun digitsOnly(s: String): String = buildString {
    for (c in s) {
        val d = Character.digit(c, 10)
        if (d >= 0) append(d)
    }
}

fun cylWord(n: Int): String = when {
    n == 1 -> "اسطوانة واحدة"
    n == 2 -> "اسطوانتان"
    n in 3..10 -> "$n اسطوانات"
    else -> "$n اسطوانة"
}

fun startOfDay(ms: Long): Long {
    val c = Calendar.getInstance()
    c.timeInMillis = ms
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    return c.timeInMillis
}

fun addDays(ms: Long, days: Int): Long {
    val c = Calendar.getInstance()
    c.timeInMillis = ms
    c.add(Calendar.DAY_OF_YEAR, days)
    return c.timeInMillis
}

// ------------------------------------------------------------------
// الحالة العامة للتطبيق
// ------------------------------------------------------------------

data class CustomerSummary(
    val customer: Customer,
    val debt: Double,
    val qtyBought: Int,
    val salesCount: Int,
    val totalBought: Double,
    val totalPaid: Double,
    val emptiesHeld: Int,
    val lastActivity: Long
)

data class FactorySummary(
    val factory: Factory,
    val debt: Double,
    val qtyRefilled: Int,
    val refillCount: Int,
    val totalCost: Double,
    val totalPaid: Double,
    val lastActivity: Long
)

data class AppState(
    val loaded: Boolean = false,
    val customers: List<CustomerSummary> = emptyList(),
    val factories: List<FactorySummary> = emptyList(),
    val sales: List<Sale> = emptyList(),
    val payments: List<CustomerPayment> = emptyList(),
    val refills: List<Refill> = emptyList(),
    val factoryPayments: List<FactoryPayment> = emptyList(),
    val settings: Settings = Settings(),
    val stockFull: Int = 0,
    val stockEmpty: Int = 0,
    val emptiesAtCustomers: Int = 0,
    val totalCustomerDebt: Double = 0.0,
    val totalFactoryDebt: Double = 0.0,
    val debtorsCount: Int = 0,
    val totalSalesRevenue: Double = 0.0
) {
    fun customer(id: Long): CustomerSummary? = customers.firstOrNull { it.customer.id == id }
    fun factory(id: Long): FactorySummary? = factories.firstOrNull { it.factory.id == id }
    val areas: List<String>
        get() = customers.map { it.customer.area.trim() }.filter { it.isNotEmpty() }.distinct().sorted()
}

fun buildState(
    customers: List<Customer>,
    sales: List<Sale>,
    payments: List<CustomerPayment>,
    factories: List<Factory>,
    refills: List<Refill>,
    factoryPayments: List<FactoryPayment>,
    settings: Settings
): AppState {
    val salesBy = sales.groupBy { it.customerId }
    val payBy = payments.groupBy { it.customerId }
    val cs = customers.map { c ->
        val s = salesBy[c.id].orEmpty()
        val p = payBy[c.id].orEmpty()
        val debt = c.openingDebt + s.sumOf { it.total() - it.paid } - p.sumOf { it.amount }
        val last = maxOf(
            c.createdAt,
            s.maxOfOrNull { it.date } ?: 0L,
            p.maxOfOrNull { it.date } ?: 0L
        )
        CustomerSummary(
            customer = c,
            debt = debt,
            qtyBought = s.sumOf { it.qty },
            salesCount = s.size,
            totalBought = s.sumOf { it.total() },
            totalPaid = s.sumOf { it.paid } + p.sumOf { it.amount },
            emptiesHeld = s.sumOf { it.qty - it.emptyReturned },
            lastActivity = last
        )
    }

    val refBy = refills.groupBy { it.factoryId }
    val fpBy = factoryPayments.groupBy { it.factoryId }
    val fs = factories.map { f ->
        val r = refBy[f.id].orEmpty()
        val p = fpBy[f.id].orEmpty()
        val debt = f.openingDebt + r.sumOf { it.cost() - it.paid } - p.sumOf { it.amount }
        val last = maxOf(
            f.createdAt,
            r.maxOfOrNull { it.date } ?: 0L,
            p.maxOfOrNull { it.date } ?: 0L
        )
        FactorySummary(
            factory = f,
            debt = debt,
            qtyRefilled = r.sumOf { it.qty },
            refillCount = r.size,
            totalCost = r.sumOf { it.cost() },
            totalPaid = r.sumOf { it.paid } + p.sumOf { it.amount },
            lastActivity = last
        )
    }

    val refilledQty = refills.sumOf { it.qty }
    val soldQty = sales.sumOf { it.qty }
    return AppState(
        loaded = true,
        customers = cs,
        factories = fs,
        sales = sales,
        payments = payments,
        refills = refills,
        factoryPayments = factoryPayments,
        settings = settings,
        stockFull = settings.openingFull + refilledQty - soldQty,
        stockEmpty = settings.openingEmpty - refilledQty + sales.sumOf { it.emptyReturned },
        emptiesAtCustomers = sales.sumOf { it.qty - it.emptyReturned },
        totalCustomerDebt = cs.filter { it.debt > 0.5 }.sumOf { it.debt },
        totalFactoryDebt = fs.filter { it.debt > 0.5 }.sumOf { it.debt },
        debtorsCount = cs.count { it.debt > 0.5 },
        totalSalesRevenue = sales.sumOf { it.total() }
    )
}

// ------------------------------------------------------------------
// كشف الحساب
// ------------------------------------------------------------------

enum class EntryKind { OPENING, SALE, PAYMENT, REFILL, FACTORY_PAYMENT }

data class LedgerEntry(
    val kind: EntryKind,
    val refId: Long,
    val date: Long,
    val title: String,
    val detail: String,
    val delta: Double,
    val balance: Double
)

private class RawEntry(
    val kind: EntryKind,
    val refId: Long,
    val date: Long,
    val title: String,
    val detail: String,
    val delta: Double
)

private fun runBalances(opening: RawEntry?, rest: List<RawEntry>): List<LedgerEntry> {
    val sorted = rest.sortedWith(compareBy({ it.date }, { it.kind.ordinal }, { it.refId }))
    val all = listOfNotNull(opening) + sorted
    var bal = 0.0
    val out = all.map {
        bal += it.delta
        LedgerEntry(it.kind, it.refId, it.date, it.title, it.detail, it.delta, bal)
    }
    return out.reversed()
}

fun customerLedger(
    c: Customer,
    sales: List<Sale>,
    payments: List<CustomerPayment>,
    cur: String
): List<LedgerEntry> {
    val opening = if (c.openingDebt != 0.0)
        RawEntry(EntryKind.OPENING, 0, c.createdAt, "رصيد سابق", "دين قبل استخدام التطبيق", c.openingDebt)
    else null
    val rest = ArrayList<RawEntry>()
    sales.forEach {
        val debt = it.total() - it.paid
        val parts = ArrayList<String>()
        parts += "السعر ${it.unitPrice.fmt()}"
        parts += "المجموع ${it.total().fmt()}"
        parts += if (debt > 0.5) "مدفوع ${it.paid.fmt()}" else "مدفوع بالكامل"
        if (it.emptyReturned > 0) parts += "فارغ مستلم ${it.emptyReturned}"
        if (it.note.isNotBlank()) parts += it.note
        rest += RawEntry(EntryKind.SALE, it.id, it.date, "بيع ${cylWord(it.qty)}", parts.joinToString(" • "), debt)
    }
    payments.forEach {
        rest += RawEntry(
            EntryKind.PAYMENT, it.id, it.date, "تسديد ${money(it.amount, cur)}",
            it.note.ifBlank { "دفعة من الزبون" }, -it.amount
        )
    }
    return runBalances(opening, rest)
}

fun factoryLedger(
    f: Factory,
    refills: List<Refill>,
    payments: List<FactoryPayment>,
    cur: String
): List<LedgerEntry> {
    val opening = if (f.openingDebt != 0.0)
        RawEntry(EntryKind.OPENING, 0, f.createdAt, "رصيد سابق", "مستحق للمعمل قبل استخدام التطبيق", f.openingDebt)
    else null
    val rest = ArrayList<RawEntry>()
    refills.forEach {
        val debt = it.cost() - it.paid
        val parts = ArrayList<String>()
        parts += "السعر ${it.unitCost.fmt()}"
        parts += "التكلفة ${it.cost().fmt()}"
        parts += if (debt > 0.5) "مدفوع ${it.paid.fmt()}" else "مدفوع بالكامل"
        if (it.note.isNotBlank()) parts += it.note
        rest += RawEntry(EntryKind.REFILL, it.id, it.date, "تعبئة ${cylWord(it.qty)}", parts.joinToString(" • "), debt)
    }
    payments.forEach {
        rest += RawEntry(
            EntryKind.FACTORY_PAYMENT, it.id, it.date, "دفعة ${money(it.amount, cur)}",
            it.note.ifBlank { "دفعة للمعمل" }, -it.amount
        )
    }
    return runBalances(opening, rest)
}

// ------------------------------------------------------------------
// النشاط الأخير
// ------------------------------------------------------------------

data class Activity(
    val kind: EntryKind,
    val time: Long,
    val title: String,
    val subtitle: String,
    val amount: Double
)

fun recentActivity(state: AppState, limit: Int): List<Activity> {
    val cur = state.settings.currency
    val list = ArrayList<Activity>()
    state.sales.take(limit).forEach {
        val name = state.customer(it.customerId)?.customer?.name ?: "زبون محذوف"
        list += Activity(EntryKind.SALE, it.date, "بيع ${cylWord(it.qty)}", name, it.total())
    }
    state.payments.take(limit).forEach {
        val name = state.customer(it.customerId)?.customer?.name ?: "زبون محذوف"
        list += Activity(EntryKind.PAYMENT, it.date, "تسديد من $name", "قبض ${money(it.amount, cur)}", it.amount)
    }
    state.refills.take(limit).forEach {
        val name = state.factory(it.factoryId)?.factory?.name ?: "معمل محذوف"
        list += Activity(EntryKind.REFILL, it.date, "تعبئة ${cylWord(it.qty)}", name, it.cost())
    }
    state.factoryPayments.take(limit).forEach {
        val name = state.factory(it.factoryId)?.factory?.name ?: "معمل محذوف"
        list += Activity(EntryKind.FACTORY_PAYMENT, it.date, "دفعة لمعمل $name", money(it.amount, cur), it.amount)
    }
    return list.sortedByDescending { it.time }.take(limit)
}

// ------------------------------------------------------------------
// التقارير
// ------------------------------------------------------------------

enum class ReportRange(val label: String) {
    TODAY("اليوم"), WEEK("٧ أيام"), MONTH("٣٠ يومًا"), ALL("الكل")
}

data class AreaStat(val area: String, val qty: Int, val revenue: Double, val debt: Double)
data class TopCustomer(val name: String, val qty: Int, val total: Double)

data class Report(
    val salesCount: Int,
    val qtySold: Int,
    val revenue: Double,
    val paidAtSale: Double,
    val debtAdded: Double,
    val paymentsIn: Double,
    val refillCount: Int,
    val qtyRefilled: Int,
    val refillCost: Double,
    val paidToFactories: Double,
    val profit: Double,
    val cashRatio: Double,
    val debtRatio: Double,
    val collectionRatio: Double,
    val byArea: List<AreaStat>,
    val topCustomers: List<TopCustomer>
)

fun rangeStart(range: ReportRange, now: Long = System.currentTimeMillis()): Long = when (range) {
    ReportRange.TODAY -> startOfDay(now)
    ReportRange.WEEK -> startOfDay(addDays(now, -6))
    ReportRange.MONTH -> startOfDay(addDays(now, -29))
    ReportRange.ALL -> 0L
}

fun buildReport(state: AppState, range: ReportRange): Report {
    val from = rangeStart(range)
    val sales = state.sales.filter { it.date >= from }
    val pays = state.payments.filter { it.date >= from }
    val refills = state.refills.filter { it.date >= from }
    val fpays = state.factoryPayments.filter { it.date >= from }

    val qtySold = sales.sumOf { it.qty }
    val revenue = sales.sumOf { it.total() }
    val paidAtSale = sales.sumOf { it.paid }
    val debtAdded = revenue - paidAtSale
    val paymentsIn = pays.sumOf { it.amount }

    val allRefilledQty = state.refills.sumOf { it.qty }
    val avgCost = if (allRefilledQty > 0) state.refills.sumOf { it.cost() } / allRefilledQty
    else state.settings.refillCost

    val areaOf = state.customers.associate { it.customer.id to it.customer.area.trim() }
    val nameOf = state.customers.associate { it.customer.id to it.customer.name }
    val byArea = sales.groupBy { areaOf[it.customerId].orEmpty().ifBlank { "بدون منطقة" } }
        .map { (area, list) ->
            AreaStat(area, list.sumOf { it.qty }, list.sumOf { it.total() }, list.sumOf { it.total() - it.paid })
        }
        .sortedByDescending { it.qty }
    val top = sales.groupBy { it.customerId }
        .map { (id, list) ->
            TopCustomer(nameOf[id] ?: "زبون محذوف", list.sumOf { it.qty }, list.sumOf { it.total() })
        }
        .sortedByDescending { it.qty }
        .take(5)

    return Report(
        salesCount = sales.size,
        qtySold = qtySold,
        revenue = revenue,
        paidAtSale = paidAtSale,
        debtAdded = debtAdded,
        paymentsIn = paymentsIn,
        refillCount = refills.size,
        qtyRefilled = refills.sumOf { it.qty },
        refillCost = refills.sumOf { it.cost() },
        paidToFactories = refills.sumOf { it.paid } + fpays.sumOf { it.amount },
        profit = revenue - qtySold * avgCost,
        cashRatio = if (revenue > 0) (paidAtSale / revenue).coerceIn(0.0, 1.0) else 0.0,
        debtRatio = if (revenue > 0) (debtAdded / revenue).coerceIn(0.0, 1.0) else 0.0,
        collectionRatio = if (revenue > 0) ((paidAtSale + paymentsIn) / revenue).coerceIn(0.0, 1.0) else 0.0,
        byArea = byArea,
        topCustomers = top
    )
}

/** عدد الاسطوانات المباعة لكل يوم خلال آخر [days] يومًا (الأقدم أولًا) */
fun dailyQty(sales: List<Sale>, days: Int, now: Long = System.currentTimeMillis()): List<Pair<Long, Int>> {
    val today = startOfDay(now)
    return (days - 1 downTo 0).map { back ->
        val start = startOfDay(addDays(today, -back))
        val end = addDays(start, 1)
        start to sales.filter { it.date in start until end }.sumOf { it.qty }
    }
}

fun isSettled(debt: Double): Boolean = abs(debt) < 0.5
