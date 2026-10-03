package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.B20Asset
import com.example.data.model.PaymentInvoice
import com.example.data.model.PrivateTransaction
import com.example.data.model.Stablecoin
import com.example.data.model.WalletTransaction

@Database(
    entities = [
        PaymentInvoice::class,
        B20Asset::class,
        Stablecoin::class,
        PrivateTransaction::class,
        WalletTransaction::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BaseDatabase : RoomDatabase() {
    abstract fun invoiceDao(): InvoiceDao
    abstract fun assetDao(): AssetDao
    abstract fun stablecoinDao(): StablecoinDao
    abstract fun privateTxDao(): PrivateTxDao
    abstract fun walletTxDao(): WalletTxDao

    companion object {
        @Volatile
        private var INSTANCE: BaseDatabase? = null

        fun getDatabase(context: Context): BaseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BaseDatabase::class.java,
                    "base_hub_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
