package com.example.nommit.core.database

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performInTransactionSuspending
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Double
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class SearchCacheDao_Impl(
  __db: RoomDatabase,
) : SearchCacheDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfCachedSearchEntity: EntityInsertAdapter<CachedSearchEntity>

  private val __insertAdapterOfCachedPlaceEntity: EntityInsertAdapter<CachedPlaceEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfCachedSearchEntity = object : EntityInsertAdapter<CachedSearchEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `cached_search` (`cacheKey`,`fetchedAt`) VALUES (?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: CachedSearchEntity) {
        statement.bindText(1, entity.cacheKey)
        statement.bindLong(2, entity.fetchedAt)
      }
    }
    this.__insertAdapterOfCachedPlaceEntity = object : EntityInsertAdapter<CachedPlaceEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `cached_place` (`cacheKey`,`placeId`,`name`,`address`,`latitude`,`longitude`,`types`,`primaryType`,`primaryTypeDisplayName`,`priceLevel`,`rating`,`userRatingCount`,`openNow`,`photoName`,`position`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: CachedPlaceEntity) {
        statement.bindText(1, entity.cacheKey)
        statement.bindText(2, entity.placeId)
        statement.bindText(3, entity.name)
        val _tmpAddress: String? = entity.address
        if (_tmpAddress == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpAddress)
        }
        statement.bindDouble(5, entity.latitude)
        statement.bindDouble(6, entity.longitude)
        statement.bindText(7, entity.types)
        val _tmpPrimaryType: String? = entity.primaryType
        if (_tmpPrimaryType == null) {
          statement.bindNull(8)
        } else {
          statement.bindText(8, _tmpPrimaryType)
        }
        val _tmpPrimaryTypeDisplayName: String? = entity.primaryTypeDisplayName
        if (_tmpPrimaryTypeDisplayName == null) {
          statement.bindNull(9)
        } else {
          statement.bindText(9, _tmpPrimaryTypeDisplayName)
        }
        val _tmpPriceLevel: String? = entity.priceLevel
        if (_tmpPriceLevel == null) {
          statement.bindNull(10)
        } else {
          statement.bindText(10, _tmpPriceLevel)
        }
        val _tmpRating: Double? = entity.rating
        if (_tmpRating == null) {
          statement.bindNull(11)
        } else {
          statement.bindDouble(11, _tmpRating)
        }
        val _tmpUserRatingCount: Int? = entity.userRatingCount
        if (_tmpUserRatingCount == null) {
          statement.bindNull(12)
        } else {
          statement.bindLong(12, _tmpUserRatingCount.toLong())
        }
        val _tmpOpenNow: Boolean? = entity.openNow
        val _tmp: Int? = _tmpOpenNow?.let { if (it) 1 else 0 }
        if (_tmp == null) {
          statement.bindNull(13)
        } else {
          statement.bindLong(13, _tmp.toLong())
        }
        val _tmpPhotoName: String? = entity.photoName
        if (_tmpPhotoName == null) {
          statement.bindNull(14)
        } else {
          statement.bindText(14, _tmpPhotoName)
        }
        statement.bindLong(15, entity.position.toLong())
      }
    }
  }

  public override suspend fun upsertSearch(search: CachedSearchEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfCachedSearchEntity.insert(_connection, search)
  }

  public override suspend fun upsertPlaces(places: List<CachedPlaceEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfCachedPlaceEntity.insert(_connection, places)
  }

  public override suspend fun findFresh(cacheKey: String, oldestAcceptable: Long): List<CachedPlaceEntity>? = performInTransactionSuspending(__db) {
    super@SearchCacheDao_Impl.findFresh(cacheKey, oldestAcceptable)
  }

  public override suspend fun cache(
    cacheKey: String,
    fetchedAt: Long,
    places: List<CachedPlaceEntity>,
  ): Unit = performInTransactionSuspending(__db) {
    super@SearchCacheDao_Impl.cache(cacheKey, fetchedAt, places)
  }

  public override suspend fun findSearch(cacheKey: String): CachedSearchEntity? {
    val _sql: String = "SELECT * FROM cached_search WHERE cacheKey = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, cacheKey)
        val _columnIndexOfCacheKey: Int = getColumnIndexOrThrow(_stmt, "cacheKey")
        val _columnIndexOfFetchedAt: Int = getColumnIndexOrThrow(_stmt, "fetchedAt")
        val _result: CachedSearchEntity?
        if (_stmt.step()) {
          val _tmpCacheKey: String
          _tmpCacheKey = _stmt.getText(_columnIndexOfCacheKey)
          val _tmpFetchedAt: Long
          _tmpFetchedAt = _stmt.getLong(_columnIndexOfFetchedAt)
          _result = CachedSearchEntity(_tmpCacheKey,_tmpFetchedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun placesFor(cacheKey: String): List<CachedPlaceEntity> {
    val _sql: String = "SELECT * FROM cached_place WHERE cacheKey = ? ORDER BY position ASC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, cacheKey)
        val _columnIndexOfCacheKey: Int = getColumnIndexOrThrow(_stmt, "cacheKey")
        val _columnIndexOfPlaceId: Int = getColumnIndexOrThrow(_stmt, "placeId")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfAddress: Int = getColumnIndexOrThrow(_stmt, "address")
        val _columnIndexOfLatitude: Int = getColumnIndexOrThrow(_stmt, "latitude")
        val _columnIndexOfLongitude: Int = getColumnIndexOrThrow(_stmt, "longitude")
        val _columnIndexOfTypes: Int = getColumnIndexOrThrow(_stmt, "types")
        val _columnIndexOfPrimaryType: Int = getColumnIndexOrThrow(_stmt, "primaryType")
        val _columnIndexOfPrimaryTypeDisplayName: Int = getColumnIndexOrThrow(_stmt, "primaryTypeDisplayName")
        val _columnIndexOfPriceLevel: Int = getColumnIndexOrThrow(_stmt, "priceLevel")
        val _columnIndexOfRating: Int = getColumnIndexOrThrow(_stmt, "rating")
        val _columnIndexOfUserRatingCount: Int = getColumnIndexOrThrow(_stmt, "userRatingCount")
        val _columnIndexOfOpenNow: Int = getColumnIndexOrThrow(_stmt, "openNow")
        val _columnIndexOfPhotoName: Int = getColumnIndexOrThrow(_stmt, "photoName")
        val _columnIndexOfPosition: Int = getColumnIndexOrThrow(_stmt, "position")
        val _result: MutableList<CachedPlaceEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: CachedPlaceEntity
          val _tmpCacheKey: String
          _tmpCacheKey = _stmt.getText(_columnIndexOfCacheKey)
          val _tmpPlaceId: String
          _tmpPlaceId = _stmt.getText(_columnIndexOfPlaceId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpAddress: String?
          if (_stmt.isNull(_columnIndexOfAddress)) {
            _tmpAddress = null
          } else {
            _tmpAddress = _stmt.getText(_columnIndexOfAddress)
          }
          val _tmpLatitude: Double
          _tmpLatitude = _stmt.getDouble(_columnIndexOfLatitude)
          val _tmpLongitude: Double
          _tmpLongitude = _stmt.getDouble(_columnIndexOfLongitude)
          val _tmpTypes: String
          _tmpTypes = _stmt.getText(_columnIndexOfTypes)
          val _tmpPrimaryType: String?
          if (_stmt.isNull(_columnIndexOfPrimaryType)) {
            _tmpPrimaryType = null
          } else {
            _tmpPrimaryType = _stmt.getText(_columnIndexOfPrimaryType)
          }
          val _tmpPrimaryTypeDisplayName: String?
          if (_stmt.isNull(_columnIndexOfPrimaryTypeDisplayName)) {
            _tmpPrimaryTypeDisplayName = null
          } else {
            _tmpPrimaryTypeDisplayName = _stmt.getText(_columnIndexOfPrimaryTypeDisplayName)
          }
          val _tmpPriceLevel: String?
          if (_stmt.isNull(_columnIndexOfPriceLevel)) {
            _tmpPriceLevel = null
          } else {
            _tmpPriceLevel = _stmt.getText(_columnIndexOfPriceLevel)
          }
          val _tmpRating: Double?
          if (_stmt.isNull(_columnIndexOfRating)) {
            _tmpRating = null
          } else {
            _tmpRating = _stmt.getDouble(_columnIndexOfRating)
          }
          val _tmpUserRatingCount: Int?
          if (_stmt.isNull(_columnIndexOfUserRatingCount)) {
            _tmpUserRatingCount = null
          } else {
            _tmpUserRatingCount = _stmt.getLong(_columnIndexOfUserRatingCount).toInt()
          }
          val _tmpOpenNow: Boolean?
          val _tmp: Int?
          if (_stmt.isNull(_columnIndexOfOpenNow)) {
            _tmp = null
          } else {
            _tmp = _stmt.getLong(_columnIndexOfOpenNow).toInt()
          }
          _tmpOpenNow = _tmp?.let { it != 0 }
          val _tmpPhotoName: String?
          if (_stmt.isNull(_columnIndexOfPhotoName)) {
            _tmpPhotoName = null
          } else {
            _tmpPhotoName = _stmt.getText(_columnIndexOfPhotoName)
          }
          val _tmpPosition: Int
          _tmpPosition = _stmt.getLong(_columnIndexOfPosition).toInt()
          _item = CachedPlaceEntity(_tmpCacheKey,_tmpPlaceId,_tmpName,_tmpAddress,_tmpLatitude,_tmpLongitude,_tmpTypes,_tmpPrimaryType,_tmpPrimaryTypeDisplayName,_tmpPriceLevel,_tmpRating,_tmpUserRatingCount,_tmpOpenNow,_tmpPhotoName,_tmpPosition)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearPlaces(cacheKey: String) {
    val _sql: String = "DELETE FROM cached_place WHERE cacheKey = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, cacheKey)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteExpired(oldestAcceptable: Long) {
    val _sql: String = "DELETE FROM cached_search WHERE fetchedAt < ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, oldestAcceptable)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
