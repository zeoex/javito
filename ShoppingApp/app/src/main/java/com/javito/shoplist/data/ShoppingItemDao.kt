package com.javito.shoplist.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingItemDao {

    @Query("SELECT * FROM shopping_items WHERE isTemplate = 0 ORDER BY isChecked ASC, createdAt DESC")
    fun getAllItems(): Flow<List<ShoppingItem>>

    @Query("SELECT * FROM shopping_items WHERE isTemplate = 1 ORDER BY name ASC")
    fun getTemplateItems(): Flow<List<ShoppingItem>>

    @Query("DELETE FROM shopping_items WHERE isTemplate = 1")
    suspend fun deleteAllTemplateItems()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ShoppingItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ShoppingItem): Long

    @Update
    suspend fun update(item: ShoppingItem)

    @Delete
    suspend fun delete(item: ShoppingItem)

    @Query("DELETE FROM shopping_items WHERE isChecked = 1")
    suspend fun deleteCheckedItems()

    @Query("SELECT * FROM shopping_items WHERE isTemplate = 0")
    suspend fun getAllItemsOnce(): List<ShoppingItem>

    @Query("SELECT * FROM shopping_items WHERE isTemplate = 1")
    suspend fun getAllTemplateItemsOnce(): List<ShoppingItem>
}
