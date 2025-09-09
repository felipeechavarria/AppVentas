package com.example.appventas.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.concurrent.Executors
import kotlinx.coroutines.runBlocking
import androidx.room.TypeConverters

@Database(entities = [Producto::class, User::class, InventoryItem::class, Cliente::class, Venta::class, VentaItem::class, Abono::class], version = 10, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productoDao(): ProductoDao
    abstract fun userDao(): UserDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun clienteDao(): ClienteDao
    abstract fun ventaDao(): VentaDao
    abstract fun abonoDao(): AbonoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(databaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private fun databaseCallback(context: Context) = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                val instance = getDatabase(context)
                Executors.newSingleThreadExecutor().execute {
                    // Creamos el objeto Supervisor aquí
                    val supervisor =
                        User(username = "supervisor", password = "1234", role = "Supervisor")

                    // Usamos una corutina para llamar a la función suspendida del DAO
                    runBlocking {
                        instance.userDao().insert(supervisor)
                    }
                }
            }
        }
    }
}