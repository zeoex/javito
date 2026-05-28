package com.javito.shoplist.data

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupManager {

    fun export(
        context: Context,
        uri: Uri,
        shoppingItems: List<ShoppingItem>,
        templateItems: List<ShoppingItem>,
        gastos: List<Gasto>,
        ingresos: List<Ingreso>
    ) {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportDate", SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()))

        // Shopping items + template items
        val itemsArr = JSONArray()
        shoppingItems.forEach { item ->
            itemsArr.put(JSONObject().apply {
                put("id", item.id)
                put("name", item.name)
                put("quantity", item.quantity)
                put("unit", item.unit)
                put("category", item.category)
                put("isChecked", item.isChecked)
                put("isTemplate", false)
                put("createdAt", item.createdAt)
            })
        }
        templateItems.forEach { item ->
            itemsArr.put(JSONObject().apply {
                put("id", item.id)
                put("name", item.name)
                put("quantity", item.quantity)
                put("unit", item.unit)
                put("category", item.category)
                put("isChecked", item.isChecked)
                put("isTemplate", true)
                put("createdAt", item.createdAt)
            })
        }
        root.put("shoppingItems", itemsArr)

        // Gastos
        val gastosArr = JSONArray()
        gastos.forEach { g ->
            gastosArr.put(JSONObject().apply {
                put("id", g.id)
                put("description", g.description)
                put("category", g.category)
                put("amount", g.amount)
                put("paymentMethod", g.paymentMethod)
                put("gastoDate", g.gastoDate)
                put("gastoMonth", g.gastoMonth)
                put("notes", g.notes)
                put("createdAt", g.createdAt)
            })
        }
        root.put("gastos", gastosArr)

        // Ingresos
        val ingresosArr = JSONArray()
        ingresos.forEach { i ->
            ingresosArr.put(JSONObject().apply {
                put("id", i.id)
                put("description", i.description)
                put("source", i.source)
                put("amount", i.amount)
                put("paymentMethod", i.paymentMethod)
                put("ingresoDate", i.ingresoDate)
                put("ingresoMonth", i.ingresoMonth)
                put("notes", i.notes)
                put("createdAt", i.createdAt)
            })
        }
        root.put("ingresos", ingresosArr)

        context.contentResolver.openOutputStream(uri)?.use { out ->
            out.write(root.toString(2).toByteArray(Charsets.UTF_8))
        }
    }

    data class BackupData(
        val shoppingItems: List<ShoppingItem>,
        val gastos: List<Gasto>,
        val ingresos: List<Ingreso>
    )

    fun import(context: Context, uri: Uri): BackupData {
        val json = context.contentResolver.openInputStream(uri)?.use { input ->
            input.bufferedReader(Charsets.UTF_8).readText()
        } ?: throw IllegalStateException("No se pudo leer el archivo")

        val root = JSONObject(json)

        // Parse shopping items (includes templates via isTemplate flag)
        val shoppingItems = mutableListOf<ShoppingItem>()
        val itemsArr = root.optJSONArray("shoppingItems") ?: JSONArray()
        for (i in 0 until itemsArr.length()) {
            val o = itemsArr.getJSONObject(i)
            shoppingItems.add(ShoppingItem(
                id = 0, // new id on insert
                name = o.getString("name"),
                quantity = o.getDouble("quantity"),
                unit = o.getString("unit"),
                category = o.getString("category"),
                isChecked = o.optBoolean("isChecked", false),
                isTemplate = o.optBoolean("isTemplate", false),
                createdAt = o.optLong("createdAt", System.currentTimeMillis())
            ))
        }

        // Parse gastos
        val gastos = mutableListOf<Gasto>()
        val gastosArr = root.optJSONArray("gastos") ?: JSONArray()
        for (i in 0 until gastosArr.length()) {
            val o = gastosArr.getJSONObject(i)
            gastos.add(Gasto(
                id = 0,
                description = o.getString("description"),
                category = o.getString("category"),
                amount = o.getDouble("amount"),
                paymentMethod = o.getString("paymentMethod"),
                gastoDate = o.getString("gastoDate"),
                gastoMonth = o.getString("gastoMonth"),
                notes = o.optString("notes", ""),
                createdAt = o.optLong("createdAt", System.currentTimeMillis())
            ))
        }

        // Parse ingresos
        val ingresos = mutableListOf<Ingreso>()
        val ingresosArr = root.optJSONArray("ingresos") ?: JSONArray()
        for (i in 0 until ingresosArr.length()) {
            val o = ingresosArr.getJSONObject(i)
            ingresos.add(Ingreso(
                id = 0,
                description = o.getString("description"),
                source = o.getString("source"),
                amount = o.getDouble("amount"),
                paymentMethod = o.getString("paymentMethod"),
                ingresoDate = o.getString("ingresoDate"),
                ingresoMonth = o.getString("ingresoMonth"),
                notes = o.optString("notes", ""),
                createdAt = o.optLong("createdAt", System.currentTimeMillis())
            ))
        }

        return BackupData(shoppingItems, gastos, ingresos)
    }
}
