package com.abosmra.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.abosmra.app.data.AppDatabase
import com.abosmra.app.data.Backup
import com.abosmra.app.data.Customer
import com.abosmra.app.data.CustomerPayment
import com.abosmra.app.data.Factory
import com.abosmra.app.data.FactoryPayment
import com.abosmra.app.data.Refill
import com.abosmra.app.data.Sale
import com.abosmra.app.data.Settings
import com.abosmra.app.data.Snapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private class PartB(
    val factories: List<Factory>,
    val refills: List<Refill>,
    val factoryPayments: List<FactoryPayment>,
    val settings: Settings?
)

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.get(app)
    private val dao = db.dao()

    val state: StateFlow<AppState> = combine(
        combine(dao.customers(), dao.sales(), dao.payments()) { c, s, p -> Triple(c, s, p) },
        combine(dao.factories(), dao.refills(), dao.factoryPayments(), dao.settings()) { f, r, fp, st ->
            PartB(f, r, fp, st)
        }
    ) { a, b ->
        buildState(a.first, a.second, a.third, b.factories, b.refills, b.factoryPayments, b.settings ?: Settings())
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppState())

    // ---- زبائن ----
    fun saveCustomer(c: Customer, onDone: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = dao.upsertCustomer(c)
            onDone(if (c.id != 0L) c.id else id)
        }
    }

    fun deleteCustomer(id: Long) {
        viewModelScope.launch {
            db.withTransaction {
                dao.deleteSalesOf(id)
                dao.deletePaymentsOf(id)
                dao.deleteCustomerById(id)
            }
        }
    }

    // ---- معامل ----
    fun saveFactory(f: Factory, onDone: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = dao.upsertFactory(f)
            onDone(if (f.id != 0L) f.id else id)
        }
    }

    fun deleteFactory(id: Long) {
        viewModelScope.launch {
            db.withTransaction {
                dao.deleteRefillsOf(id)
                dao.deleteFactoryPaymentsOf(id)
                dao.deleteFactoryById(id)
            }
        }
    }

    // ---- عمليات ----
    fun addSale(s: Sale) {
        viewModelScope.launch { dao.upsertSale(s) }
    }

    fun deleteSale(id: Long) {
        viewModelScope.launch { dao.deleteSaleById(id) }
    }

    fun addPayment(p: CustomerPayment) {
        viewModelScope.launch { dao.upsertPayment(p) }
    }

    fun deletePayment(id: Long) {
        viewModelScope.launch { dao.deletePaymentById(id) }
    }

    fun addRefill(r: Refill) {
        viewModelScope.launch { dao.upsertRefill(r) }
    }

    fun deleteRefill(id: Long) {
        viewModelScope.launch { dao.deleteRefillById(id) }
    }

    fun addFactoryPayment(p: FactoryPayment) {
        viewModelScope.launch { dao.upsertFactoryPayment(p) }
    }

    fun deleteFactoryPayment(id: Long) {
        viewModelScope.launch { dao.deleteFactoryPaymentById(id) }
    }

    // ---- الإعدادات ----
    fun saveSettings(s: Settings) {
        viewModelScope.launch { dao.saveSettings(s.copy(id = 1)) }
    }

    // ---- النسخ الاحتياطي ----
    suspend fun exportJson(): String {
        val snap = Snapshot(
            customers = dao.customers().first(),
            factories = dao.factories().first(),
            sales = dao.sales().first(),
            payments = dao.payments().first(),
            refills = dao.refills().first(),
            factoryPayments = dao.factoryPayments().first(),
            settings = dao.settings().first() ?: Settings()
        )
        return Backup.toJson(snap)
    }

    /** يستبدل كل البيانات الحالية بمحتوى النسخة الاحتياطية. يرجع true عند النجاح. */
    suspend fun importJson(text: String): Boolean {
        return try {
            val snap = Backup.fromJson(text)
            db.withTransaction {
                dao.clearSales()
                dao.clearPayments()
                dao.clearRefills()
                dao.clearFactoryPayments()
                dao.clearCustomers()
                dao.clearFactories()
                dao.insertCustomers(snap.customers)
                dao.insertFactories(snap.factories)
                dao.insertSales(snap.sales)
                dao.insertPayments(snap.payments)
                dao.insertRefills(snap.refills)
                dao.insertFactoryPayments(snap.factoryPayments)
                dao.saveSettings(snap.settings.copy(id = 1))
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
