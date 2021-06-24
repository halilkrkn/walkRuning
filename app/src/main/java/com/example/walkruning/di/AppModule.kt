package com.example.walkruning.di
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import com.example.walkruning.db.RunningDatabase
import com.example.walkruning.other.Constants.KEY_FIRST_TIME_TOGGLE
import com.example.walkruning.other.Constants.KEY_NAME
import com.example.walkruning.other.Constants.KEY_WEIGHT
import com.example.walkruning.other.Constants.RUNNING_DATABASE_NAME
import com.example.walkruning.other.Constants.SHARED_PREFERENCES_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton


@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideRunningDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(
      context,
      RunningDatabase::class.java,
      RUNNING_DATABASE_NAME
    ).build()


    @Singleton
    @Provides
    fun provideRunDao(db:RunningDatabase) = db.getRunningDao()

    // TODO: 24.06.2021
    //  Kullanıcı İsim ve Ağırlığını almak için sharedReferences fonk. yararlanıp telefonun hafızasında kayıt etmiş olduk.
    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext app: Context) =
        app.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)

    // Bu fonk. yapısı ile yukarıdaki fonksiyonun yapısı aynıdır.
    // Yukarıdaki fonk. Single Expression Functions özelliğini kullanıyor. Arasındaki fark ise " = " operatörü sayesinde sadece bir işlemlik görevleri yapılıyor ve Derleyi hangi dönüş tipinde olduğunu otomatik kendisi  algılıyor ve " = " operatörü  de dönüş tipine göre return yapıyor. Yani "=" operatörü return işleminin yerine geçmiş oluyor.
    // Bu yüzden kod fazlalığından kurtulmuş ve okunurluluk artmış oluyor.

//    fun provideSharedPreferences(@ApplicationContext app: Context): SharedPreferences{
//        return app.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)
//
//    }

    @Singleton
    @Provides
    fun provideName(sharedPref: SharedPreferences): String {
       return sharedPref.getString(KEY_NAME, "") ?: ""
    }


    @Singleton
    @Provides
    fun provideWeight(sharedPref: SharedPreferences) = sharedPref.getFloat(KEY_WEIGHT, 80f)


    @Singleton
    @Provides
    fun provideFirstTimeToggle(sharedPref: SharedPreferences) = sharedPref.getBoolean(KEY_FIRST_TIME_TOGGLE, true)
}