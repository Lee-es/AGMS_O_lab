package kr.co.uxn.agms.data.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DeviceDataDao {
    @Query("SELECT * from device_data where address = :deviceAddress order by uid desc limit 1")
    LiveData<DeviceData> getLiveData(String deviceAddress);

    @Query("SELECT * from device_data where address = :deviceAddress order by uid desc limit 1")
    DeviceData getData(String deviceAddress);

    @Insert
    void insert(DeviceData dat);

    @Update
    void update(DeviceData data);

    @Delete
    void delete(DeviceData data);
}
