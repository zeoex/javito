package com.javito.shoplist.data

import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val shoppingItemDao: ShoppingItemDao,
    private val gastoDao: GastoDao,
    private val ingresoDao: IngresoDao
) {

    val allShoppingItems: Flow<List<ShoppingItem>> = shoppingItemDao.getAllItems()
    val templateItems: Flow<List<ShoppingItem>> = shoppingItemDao.getTemplateItems()

    suspend fun insertShoppingItem(item: ShoppingItem) = shoppingItemDao.insert(item)
    suspend fun updateShoppingItem(item: ShoppingItem) = shoppingItemDao.update(item)
    suspend fun deleteShoppingItem(item: ShoppingItem) = shoppingItemDao.delete(item)
    suspend fun deleteCheckedItems() = shoppingItemDao.deleteCheckedItems()
    suspend fun deleteAllTemplateItems() = shoppingItemDao.deleteAllTemplateItems()
    suspend fun insertAllShoppingItems(items: List<ShoppingItem>) = shoppingItemDao.insertAll(items)

    suspend fun saveGasto(gasto: Gasto) = gastoDao.insert(gasto)
    suspend fun deleteGasto(gasto: Gasto) = gastoDao.delete(gasto)
    fun getGastosByMonth(month: String): Flow<List<Gasto>> = gastoDao.getByMonth(month)
    fun getGastoTotalByMonth(month: String): Flow<Double?> = gastoDao.getTotalByMonth(month)

    suspend fun saveIngreso(ingreso: Ingreso) = ingresoDao.insert(ingreso)
    suspend fun deleteIngreso(ingreso: Ingreso) = ingresoDao.delete(ingreso)
    fun getIngresosByMonth(month: String): Flow<List<Ingreso>> = ingresoDao.getByMonth(month)
    fun getIngresoTotalByMonth(month: String): Flow<Double?> = ingresoDao.getTotalByMonth(month)
    fun getIngresoEfectivoByMonth(month: String) = ingresoDao.getTotalByMonthAndMethod(month, "Efectivo")
    fun getIngresoDigitalByMonth(month: String) = ingresoDao.getTotalByMonthAndMethod(month, "Digital")

    // Backup/restore
    suspend fun getAllItemsForBackup(): List<ShoppingItem> = shoppingItemDao.getAllItemsOnce()
    suspend fun getAllTemplateItemsForBackup(): List<ShoppingItem> = shoppingItemDao.getAllTemplateItemsOnce()
    suspend fun getAllGastosForBackup(): List<Gasto> = gastoDao.getAllGastosOnce()
    suspend fun getAllIngresosForBackup(): List<Ingreso> = ingresoDao.getAllIngresosOnce()

    suspend fun restoreGastos(gastos: List<Gasto>) = gastoDao.insertAll(gastos)
    suspend fun restoreIngresos(ingresos: List<Ingreso>) = ingresoDao.insertAll(ingresos)
    suspend fun restoreShoppingItems(items: List<ShoppingItem>) = shoppingItemDao.insertAll(items)
}
