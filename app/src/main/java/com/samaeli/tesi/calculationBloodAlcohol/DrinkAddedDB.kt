package com.samaeli.tesi.calculationBloodAlcohol

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.text.Editable
import com.samaeli.tesi.models.Drink
import com.samaeli.tesi.models.DrinkAdded
/*
    Classe che ha il compito di eseguery le query per aggiungere ed eliminare i dati dal database locale
 */
class DrinkAddedDB (){
    var context:Context?=null

    companion object {
        const val DB_NAME = "drink.db"
        const val DB_VERSION = 1

        const val DRINK_ADDED_TABLE = "drink_added"

        const val DRINK_ADDED_ID = "_id"
        const val DRINK_ADDED_ID_COL = 0

        const val DRINK_ADDED_NAME = "drink_name"
        const val DRINK_ADDED_NAME_COL = 1

        const val DRINK_ADDED_VOLUME = "drink_volume"
        const val DRINK_ADDED_VOLUME_COL = 2

        const val DRINK_ADDED_ALCOHOL_CONTENT = "drink_alcohol_content"
        const val DRINK_ADDED_ALCOHOL_CONTENT_COL = 3

        const val DRINK_ADDED_IMAGE_URL = "drink_image"
        const val DRINK_ADDED_IMAGE_URL_COL = 4

        const val DRINK_ADDED_QUANTITY = "drink_quantity"
        const val DRINK_ADDED_QUANTITY_COL = 5

        const val DRINK_ADDED_HOUR = "drink_hour"
        const val DRINK_ADDED_HOUR_COL = 6

        const val DRINK_ADDED_MINUTE = "drink_minute"
        const val DRINK_ADDED_MINUTE_COL = 7

        const val CREATE_DRINK_ADDED_TABLE =
                "CREATE TABLE " + DRINK_ADDED_TABLE + " (" +
                        DRINK_ADDED_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        DRINK_ADDED_NAME + " TEXT, " +
                        DRINK_ADDED_VOLUME + " TEXT, " +
                        DRINK_ADDED_ALCOHOL_CONTENT + " TEXT, " +
                        DRINK_ADDED_IMAGE_URL + " TEXT, " +
                        DRINK_ADDED_QUANTITY + " TEXT, " +
                        DRINK_ADDED_HOUR + " TEXT, " +
                        DRINK_ADDED_MINUTE + " TEXT)"

        const val DROP_DRINK_ADDED_TABLE =
                "DROP TABLE IF EXISTS " + DRINK_ADDED_TABLE
    }

    private class DBHelper(context:Context) : SQLiteOpenHelper(context, DB_NAME,null, DB_VERSION) {

        override fun onCreate(db: SQLiteDatabase?) {
            db!!.execSQL(CREATE_DRINK_ADDED_TABLE)
        }

        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
            db!!.execSQL(DROP_DRINK_ADDED_TABLE)
            onCreate(db)
        }

    }

    private var db : SQLiteDatabase? = null
    private var dbHelper : DBHelper? = null

    constructor(context:Context) : this() {
        this.context = context
        dbHelper = DBHelper(context)
    }

    private fun openReadableDB(){ db = dbHelper!!.readableDatabase }

    private fun openWriteableDB(){ db = dbHelper!!.writableDatabase }

    private fun closeDB(){
        if(db != null){
            db!!.close()
        }
    }

    // Metodo che ritorna un ArrayList contenete tutti i drink che ha inserito l'utente
    fun getDrinksAdded(): ArrayList<DrinkAdded>{
        var drinksAdded = ArrayList<DrinkAdded>()
        openReadableDB()
        val cursor : Cursor = db!!.query(DRINK_ADDED_TABLE,null,null,null,null,null,null)
        while (cursor.moveToNext()){
            getDrinkAddedFromCursor(cursor)?.let { drinksAdded.add(it) }
        }
        cursor.close()
        closeDB()
        return drinksAdded
    }

    // Metodo che ritorna un Drink passando come parametro il cursore
    private fun getDrinkAddedFromCursor(cursor:Cursor):DrinkAdded?{
        if(cursor.count == 0){
            return null
        }else{
            try {
                val drinkAdded = DrinkAdded(
                        cursor.getInt(DRINK_ADDED_ID_COL).toLong(),
                        Drink(
                                cursor.getString(DRINK_ADDED_NAME_COL),
                                cursor.getString(DRINK_ADDED_VOLUME_COL).toInt(),
                                cursor.getString(DRINK_ADDED_ALCOHOL_CONTENT_COL).toDouble(),
                                cursor.getString(DRINK_ADDED_IMAGE_URL_COL)
                        ),
                        cursor.getString(DRINK_ADDED_QUANTITY_COL).toInt(),
                        cursor.getString(DRINK_ADDED_HOUR_COL).toInt(),
                        cursor.getString(DRINK_ADDED_MINUTE_COL).toInt()
                )
                return drinkAdded
            }catch (e:Exception){
                return null
            }
        }
    }

    // Metodo per inserire un nuovo drink nel database
    fun insertDrinkAdded(drinkAdded : DrinkAdded):Long{
        val cv = ContentValues()
        cv.put(DRINK_ADDED_NAME, drinkAdded.drink!!.name)
        cv.put(DRINK_ADDED_VOLUME, drinkAdded.drink!!.volume)
        cv.put(DRINK_ADDED_ALCOHOL_CONTENT, drinkAdded.drink!!.alcoholContent)
        cv.put(DRINK_ADDED_IMAGE_URL, drinkAdded.drink!!.imageUrl)
        cv.put(DRINK_ADDED_QUANTITY, drinkAdded.quantity)
        cv.put(DRINK_ADDED_HOUR, drinkAdded.hour)
        cv.put(DRINK_ADDED_MINUTE, drinkAdded.minute)

        openWriteableDB()
        val rowId = db!!.insert(DRINK_ADDED_TABLE,null,cv)
        closeDB()

        return rowId
    }

    // Metodo che permette di eliminare un drink dal db sapendo il suo id
    fun deleteDrinkAdded(id:Long):Int{
        val where = DRINK_ADDED_ID + "= ?"
        val whereArgs = arrayOf(id.toString())

        openWriteableDB()
        val rowCount = db!!.delete(DRINK_ADDED_TABLE,where,whereArgs)
        closeDB()

        return rowCount
    }

    // Metodo che permette di cancellare tutti i drink dal db
    fun deleteDrinksAdded():Int{
        openWriteableDB()
        val rowCount = db!!.delete(DRINK_ADDED_TABLE,null,null)
        closeDB()

        return rowCount
    }

}