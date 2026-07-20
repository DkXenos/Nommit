package com.example.nommit.core.database;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class DatabaseModule_ProvideSearchCacheDaoFactory implements Factory<SearchCacheDao> {
  private final Provider<NommitDatabase> databaseProvider;

  private DatabaseModule_ProvideSearchCacheDaoFactory(Provider<NommitDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public SearchCacheDao get() {
    return provideSearchCacheDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideSearchCacheDaoFactory create(
      Provider<NommitDatabase> databaseProvider) {
    return new DatabaseModule_ProvideSearchCacheDaoFactory(databaseProvider);
  }

  public static SearchCacheDao provideSearchCacheDao(NommitDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideSearchCacheDao(database));
  }
}
