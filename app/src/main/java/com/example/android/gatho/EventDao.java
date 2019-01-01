package com.example.android.gatho;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface EventDao {
    @Query("SELECT * FROM events")
    List<Event> getAll();

    @Query("SELECT * FROM events WHERE id IN (:event_ids)")
    List<Event> loadAllByIds(int[] event_ids);

    @Query("SELECT * FROM events WHERE name LIKE :title LIMIT 1")
    Event findByTitle(String title);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Event... events);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Event event);

    @Delete
    void delete(Event event);

    @Query("DELETE FROM events")
    void deleteAll();

    @Update
    public void update(Event... events);

    @Query("UPDATE events SET sid = :sid WHERE id = :id ")
    public void updateSid(Integer id,Integer sid);



}
