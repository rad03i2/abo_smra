package com.abosmra.app.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

/** زبون (مدين أو غير مدين) */
@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val area: String = "",
    val notes: String = "",
    val openingDebt: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

/** معمل تعبئة الغاز */
@Entity(tableName = "factories")
data class Factory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val notes: String = "",
    val openingDebt: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

/** عملية بيع اسطوانات لزبون */
@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val date: Long,
    val qty: Int,
    val unitPrice: Double,
    val paid: Double,
    val emptyReturned: Int,
    val note: String = ""
) {
    fun total(): Double = qty * unitPrice
}

/** تسديد من زبون */
@Entity(tableName = "customer_payments")
data class CustomerPayment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val date: Long,
    val amount: Double,
    val note: String = ""
)

/** تعبئة اسطوانات فارغة في معمل */
@Entity(tableName = "refills")
data class Refill(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val factoryId: Long,
    val date: Long,
    val qty: Int,
    val unitCost: Double,
    val paid: Double,
    val note: String = ""
) {
    fun cost(): Double = qty * unitCost
}

/** دفعة للمعمل */
@Entity(tableName = "factory_payments")
data class FactoryPayment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val factoryId: Long,
    val date: Long,
    val amount: Double,
    val note: String = ""
)

/** الإعدادات (صف واحد) */
@Entity(tableName = "settings")
data class Settings(
    @PrimaryKey val id: Int = 1,
    val salePrice: Double = 10000.0,
    val refillCost: Double = 7000.0,
    val openingFull: Int = 0,
    val openingEmpty: Int = 0,
    val currency: String = "د.ع"
)

@Dao
interface AppDao {
    @Query("SELECT * FROM customers ORDER BY name")
    fun customers(): Flow<List<Customer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCustomer(c: Customer): Long

    @Query("DELETE FROM customers WHERE id = :id")
    suspend fun deleteCustomerById(id: Long)

    @Query("SELECT * FROM factories ORDER BY name")
    fun factories(): Flow<List<Factory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFactory(f: Factory): Long

    @Query("DELETE FROM factories WHERE id = :id")
    suspend fun deleteFactoryById(id: Long)

    @Query("SELECT * FROM sales ORDER BY date DESC, id DESC")
    fun sales(): Flow<List<Sale>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSale(s: Sale): Long

    @Query("DELETE FROM sales WHERE id = :id")
    suspend fun deleteSaleById(id: Long)

    @Query("DELETE FROM sales WHERE customerId = :customerId")
    suspend fun deleteSalesOf(customerId: Long)

    @Query("SELECT * FROM customer_payments ORDER BY date DESC, id DESC")
    fun payments(): Flow<List<CustomerPayment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPayment(p: CustomerPayment): Long

    @Query("DELETE FROM customer_payments WHERE id = :id")
    suspend fun deletePaymentById(id: Long)

    @Query("DELETE FROM customer_payments WHERE customerId = :customerId")
    suspend fun deletePaymentsOf(customerId: Long)

    @Query("SELECT * FROM refills ORDER BY date DESC, id DESC")
    fun refills(): Flow<List<Refill>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRefill(r: Refill): Long

    @Query("DELETE FROM refills WHERE id = :id")
    suspend fun deleteRefillById(id: Long)

    @Query("DELETE FROM refills WHERE factoryId = :factoryId")
    suspend fun deleteRefillsOf(factoryId: Long)

    @Query("SELECT * FROM factory_payments ORDER BY date DESC, id DESC")
    fun factoryPayments(): Flow<List<FactoryPayment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFactoryPayment(p: FactoryPayment): Long

    @Query("DELETE FROM factory_payments WHERE id = :id")
    suspend fun deleteFactoryPaymentById(id: Long)

    @Query("DELETE FROM factory_payments WHERE factoryId = :factoryId")
    suspend fun deleteFactoryPaymentsOf(factoryId: Long)

    @Query("SELECT * FROM settings WHERE id = 1")
    fun settings(): Flow<Settings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(s: Settings)

    // ---- استيراد / نسخ احتياطي ----
    @Query("DELETE FROM customers")
    suspend fun clearCustomers()

    @Query("DELETE FROM factories")
    suspend fun clearFactories()

    @Query("DELETE FROM sales")
    suspend fun clearSales()

    @Query("DELETE FROM customer_payments")
    suspend fun clearPayments()

    @Query("DELETE FROM refills")
    suspend fun clearRefills()

    @Query("DELETE FROM factory_payments")
    suspend fun clearFactoryPayments()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(list: List<Customer>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFactories(list: List<Factory>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSales(list: List<Sale>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(list: List<CustomerPayment>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRefills(list: List<Refill>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFactoryPayments(list: List<FactoryPayment>)
}

@Database(
    entities = [
        Customer::class, Factory::class, Sale::class, CustomerPayment::class,
        Refill::class, FactoryPayment::class, Settings::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): AppDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "abosmra.db"
                ).build().also { instance = it }
            }
    }
}
