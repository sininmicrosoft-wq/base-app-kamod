package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.B20Asset
import com.example.data.model.PaymentInvoice
import com.example.data.model.PrivateTransaction
import com.example.data.model.Stablecoin
import com.example.data.model.WalletTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM payment_invoices ORDER BY createdAt DESC")
    fun getAllInvoices(): Flow<List<PaymentInvoice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: PaymentInvoice): Long

    @Update
    suspend fun updateInvoice(invoice: PaymentInvoice)

    @Delete
    suspend fun deleteInvoice(invoice: PaymentInvoice)

    @Query("SELECT * FROM payment_invoices WHERE id = :id LIMIT 1")
    suspend fun getInvoiceById(id: Long): PaymentInvoice?
}

@Dao
interface AssetDao {
    @Query("SELECT * FROM tokenized_assets ORDER BY createdAt DESC")
    fun getAllAssets(): Flow<List<B20Asset>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: B20Asset): Long

    @Update
    suspend fun updateAsset(asset: B20Asset)

    @Delete
    suspend fun deleteAsset(asset: B20Asset)
}

@Dao
interface StablecoinDao {
    @Query("SELECT * FROM stablecoins ORDER BY createdAt DESC")
    fun getAllStablecoins(): Flow<List<Stablecoin>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStablecoin(stablecoin: Stablecoin): Long

    @Update
    suspend fun updateStablecoin(stablecoin: Stablecoin)

    @Delete
    suspend fun deleteStablecoin(stablecoin: Stablecoin)
}

@Dao
interface PrivateTxDao {
    @Query("SELECT * FROM private_transactions ORDER BY timestamp DESC")
    fun getAllPrivateTxs(): Flow<List<PrivateTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrivateTx(tx: PrivateTransaction): Long
}

@Dao
interface WalletTxDao {
    @Query("SELECT * FROM wallet_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<WalletTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(tx: WalletTransaction): Long
}
