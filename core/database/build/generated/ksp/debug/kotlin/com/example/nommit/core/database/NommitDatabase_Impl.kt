package com.example.nommit.core.database

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class NommitDatabase_Impl : NommitDatabase() {
  private val _searchCacheDao: Lazy<SearchCacheDao> = lazy {
    SearchCacheDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(3, "f327a4358b767470bab01d70f80be55c", "d03ba13413b04e64f181d01c76c3453e") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `cached_search` (`cacheKey` TEXT NOT NULL, `fetchedAt` INTEGER NOT NULL, PRIMARY KEY(`cacheKey`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `cached_place` (`cacheKey` TEXT NOT NULL, `placeId` TEXT NOT NULL, `name` TEXT NOT NULL, `address` TEXT, `latitude` REAL NOT NULL, `longitude` REAL NOT NULL, `types` TEXT NOT NULL, `primaryType` TEXT, `photoName` TEXT, `position` INTEGER NOT NULL, PRIMARY KEY(`cacheKey`, `placeId`), FOREIGN KEY(`cacheKey`) REFERENCES `cached_search`(`cacheKey`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_cached_place_cacheKey` ON `cached_place` (`cacheKey`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'f327a4358b767470bab01d70f80be55c')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `cached_search`")
        connection.execSQL("DROP TABLE IF EXISTS `cached_place`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        connection.execSQL("PRAGMA foreign_keys = ON")
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsCachedSearch: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsCachedSearch.put("cacheKey", TableInfo.Column("cacheKey", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCachedSearch.put("fetchedAt", TableInfo.Column("fetchedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysCachedSearch: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesCachedSearch: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoCachedSearch: TableInfo = TableInfo("cached_search", _columnsCachedSearch, _foreignKeysCachedSearch, _indicesCachedSearch)
        val _existingCachedSearch: TableInfo = read(connection, "cached_search")
        if (!_infoCachedSearch.equals(_existingCachedSearch)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |cached_search(com.example.nommit.core.database.CachedSearchEntity).
              | Expected:
              |""".trimMargin() + _infoCachedSearch + """
              |
              | Found:
              |""".trimMargin() + _existingCachedSearch)
        }
        val _columnsCachedPlace: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsCachedPlace.put("cacheKey", TableInfo.Column("cacheKey", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCachedPlace.put("placeId", TableInfo.Column("placeId", "TEXT", true, 2, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCachedPlace.put("name", TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCachedPlace.put("address", TableInfo.Column("address", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCachedPlace.put("latitude", TableInfo.Column("latitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCachedPlace.put("longitude", TableInfo.Column("longitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCachedPlace.put("types", TableInfo.Column("types", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCachedPlace.put("primaryType", TableInfo.Column("primaryType", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCachedPlace.put("photoName", TableInfo.Column("photoName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCachedPlace.put("position", TableInfo.Column("position", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysCachedPlace: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysCachedPlace.add(TableInfo.ForeignKey("cached_search", "CASCADE", "NO ACTION", listOf("cacheKey"), listOf("cacheKey")))
        val _indicesCachedPlace: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesCachedPlace.add(TableInfo.Index("index_cached_place_cacheKey", false, listOf("cacheKey"), listOf("ASC")))
        val _infoCachedPlace: TableInfo = TableInfo("cached_place", _columnsCachedPlace, _foreignKeysCachedPlace, _indicesCachedPlace)
        val _existingCachedPlace: TableInfo = read(connection, "cached_place")
        if (!_infoCachedPlace.equals(_existingCachedPlace)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |cached_place(com.example.nommit.core.database.CachedPlaceEntity).
              | Expected:
              |""".trimMargin() + _infoCachedPlace + """
              |
              | Found:
              |""".trimMargin() + _existingCachedPlace)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "cached_search", "cached_place")
  }

  public override fun clearAllTables() {
    super.performClear(true, "cached_search", "cached_place")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(SearchCacheDao::class, SearchCacheDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun searchCacheDao(): SearchCacheDao = _searchCacheDao.value
}
