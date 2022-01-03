package kr.co.uxn.agms.data.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface SensorLogDao {
    @Query("SELECT * from sensor_log where address = :deviceAddress order by uid desc limit 1")
    LiveData<SensorLog> getCurrentState(String deviceAddress);

    @Query("SELECT * from sensor_log order by uid desc limit 1")
    LiveData<SensorLog> getLastData();

    @Insert
    void insert(SensorLog log);

    @Update
    void update(SensorLog log);

    @Delete
    void delete(SensorLog log);
}
