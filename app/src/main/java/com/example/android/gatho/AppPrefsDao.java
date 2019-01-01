package com.example.android.gatho;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface AppPrefsDao {
    @Query("SELECT * FROM app_prefs")
    List<AppPrefs> getAll();

    @Query("SELECT * FROM app_prefs WHERE keya LIKE :key LIMIT 1")
    AppPrefs getByKey(String key);

    @Insert
    void insertAll(AppPrefs... pref);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(AppPrefs pref);

    @Delete
    void delete(AppPrefs pref);


}
