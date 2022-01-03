package kr.co.uxn.agms.data.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CalibrationDataDao {
    @Query("SELECT * from calibration where device_address = :deviceAddress order by uid desc limit 1")
    CalibrationData getLastData(String deviceAddress);

    @Query("SELECT * from calibration where device_address = :deviceAddress and calibration1_date >= :startTime and calibration2_date >= :endTime order by uid desc limit 1")
    CalibrationData getDataList(String deviceAddress, long startTime, long endTime);





    @Insert
    void insert(CalibrationData dat);

    @Update
    void update(CalibrationData data);

    @Delete
    void delete(CalibrationData data);
}
