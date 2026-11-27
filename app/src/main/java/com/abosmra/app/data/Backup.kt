package com.abosmra.app.data

import org.json.JSONArray
import org.json.JSONObject

data class Snapshot(
    val customers: List<Customer>,
    val factories: List<Factory>,
    val sales: List<Sale>,
    val payments: List<CustomerPayment>,
    val refills: List<Refill>,
    val factoryPayments: List<FactoryPayment>,
    val settings: Settings
)

/** تحويل كل البيانات إلى JSON وبالعكس (نسخة احتياطية) */
object Backup {

    fun toJson(s: Snapshot): String {
        val root = JSONObject()
        root.put("app", "abosmra")
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        root.put("customers", JSONArray().apply {
            s.customers.forEach {
                put(
                    JSONObject().put("id", it.id).put("name", it.name).put("phone", it.phone)
                        .put("area", it.area).put("notes", it.notes)
                        .put("openingDebt", it.openingDebt).put("createdAt", it.createdAt)
                )
            }
        })
        root.put("factories", JSONArray().apply {
            s.factories.forEach {
                put(
                    JSONObject().put("id", it.id).put("name", it.name).put("phone", it.phone)
                        .put("notes", it.notes).put("openingDebt", it.openingDebt)
                        .put("createdAt", it.createdAt)
                )
            }
        })
        root.put("sales", JSONArray().apply {
            s.sales.forEach {
                put(
                    JSONObject().put("id", it.id).put("customerId", it.customerId)
                        .put("date", it.date).put("qty", it.qty).put("unitPrice", it.unitPrice)
                        .put("paid", it.paid).put("emptyReturned", it.emptyReturned)
                        .put("note", it.note)
                )
            }
        })
        root.put("payments", JSONArray().apply {
            s.payments.forEach {
                put(
                    JSONObject().put("id", it.id).put("customerId", it.customerId)
                        .put("date", it.date).put("amount", it.amount).put("note", it.note)
                )
            }
        })
        root.put("refills", JSONArray().apply {
            s.refills.forEach {
                put(
                    JSONObject().put("id", it.id).put("factoryId", it.factoryId)
                        .put("date", it.date).put("qty", it.qty).put("unitCost", it.unitCost)
                        .put("paid", it.paid).put("note", it.note)
                )
            }
        })
        root.put("factoryPayments", JSONArray().apply {
            s.factoryPayments.forEach {
                put(
                    JSONObject().put("id", it.id).put("factoryId", it.factoryId)
                        .put("date", it.date).put("amount", it.amount).put("note", it.note)
                )
            }
        })
        root.put(
            "settings",
            JSONObject().put("salePrice", s.settings.salePrice)
                .put("refillCost", s.settings.refillCost)
                .put("openingFull", s.settings.openingFull)
                .put("openingEmpty", s.settings.openingEmpty)
                .put("currency", s.settings.currency)
        )
        return root.toString()
    }

    fun fromJson(text: String): Snapshot {
        val root = JSONObject(text)
        require(root.optString("app") == "abosmra") { "ملف غير صالح" }

        fun <T> JSONArray?.mapObjects(f: (JSONObject) -> T): List<T> {
            if (this == null) return emptyList()
            return (0 until length()).map { f(getJSONObject(it)) }
        }

        val st = root.optJSONObject("settings")
        return Snapshot(
            customers = root.optJSONArray("customers").mapObjects {
                Customer(
                    id = it.getLong("id"), name = it.getString("name"),
                    phone = it.optString("phone", ""), area = it.optString("area", ""),
                    notes = it.optString("notes", ""),
                    openingDebt = it.optDouble("openingDebt", 0.0),
                    createdAt = it.optLong("createdAt", System.currentTimeMillis())
                )
            },
            factories = root.optJSONArray("factories").mapObjects {
                Factory(
                    id = it.getLong("id"), name = it.getString("name"),
                    phone = it.optString("phone", ""), notes = it.optString("notes", ""),
                    openingDebt = it.optDouble("openingDebt", 0.0),
                    createdAt = it.optLong("createdAt", System.currentTimeMillis())
                )
            },
            sales = root.optJSONArray("sales").mapObjects {
                Sale(
                    id = it.getLong("id"), customerId = it.getLong("customerId"),
                    date = it.getLong("date"), qty = it.getInt("qty"),
                    unitPrice = it.getDouble("unitPrice"), paid = it.getDouble("paid"),
                    emptyReturned = it.optInt("emptyReturned", 0),
                    note = it.optString("note", "")
                )
            },
            payments = root.optJSONArray("payments").mapObjects {
                CustomerPayment(
                    id = it.getLong("id"), customerId = it.getLong("customerId"),
                    date = it.getLong("date"), amount = it.getDouble("amount"),
                    note = it.optString("note", "")
                )
            },
            refills = root.optJSONArray("refills").mapObjects {
                Refill(
                    id = it.getLong("id"), factoryId = it.getLong("factoryId"),
                    date = it.getLong("date"), qty = it.getInt("qty"),
                    unitCost = it.getDouble("unitCost"), paid = it.getDouble("paid"),
                    note = it.optString("note", "")
                )
            },
            factoryPayments = root.optJSONArray("factoryPayments").mapObjects {
                FactoryPayment(
                    id = it.getLong("id"), factoryId = it.getLong("factoryId"),
                    date = it.getLong("date"), amount = it.getDouble("amount"),
                    note = it.optString("note", "")
                )
            },
            settings = if (st == null) Settings() else Settings(
                salePrice = st.optDouble("salePrice", 10000.0),
                refillCost = st.optDouble("refillCost", 7000.0),
                openingFull = st.optInt("openingFull", 0),
                openingEmpty = st.optInt("openingEmpty", 0),
                currency = st.optString("currency", "د.ع")
            )
        )
    }
}
