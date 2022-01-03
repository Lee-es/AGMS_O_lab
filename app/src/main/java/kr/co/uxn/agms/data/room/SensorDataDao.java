package kr.co.uxn.agms.data.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SensorDataDao {
    @Query("SELECT * from sensor_data where address = :deviceAddress order by sensor_time desc limit 1")
    LiveData<SensorData> getLastData(String deviceAddress);

    @Query("SELECT * from sensor_data where address = :deviceAddress and sensor_time >= :startTime and sensor_time <= :endTime order by sensor_time asc")
    LiveData<List<SensorData>> getDataList(String deviceAddress, long startTime, long endTime);

    @Query("SELECT * from sensor_data where address = :deviceAddress and sensor_time >= :startTime and sensor_time <= :endTime order by sensor_time asc")
    List<SensorData> queryDataList(String deviceAddress, long startTime, long endTime);

    @Query("SELECT * from sensor_data where address = :deviceAddress order by sensor_time asc")
    LiveData<List<SensorData>> getAllDataList(String deviceAddress);

    @Insert
    void insert(SensorData dat);

    @Update
    void update(SensorData data);

    @Delete
    void delete(SensorData data);
}
