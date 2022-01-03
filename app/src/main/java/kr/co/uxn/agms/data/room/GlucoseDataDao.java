package kr.co.uxn.agms.data.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface GlucoseDataDao {
    @Query("SELECT * from glucose_data order by data_date desc limit 1")
    LiveData<GlucoseData> getLastData();
    @Query("SELECT * from glucose_data where device_address = :deviceAddress order by data_date desc limit 1")
    LiveData<GlucoseData> getLastData(String deviceAddress);

    @Query("SELECT * from glucose_data where device_address = :deviceAddress and data_date >= :startTime and data_date <= :endTime order by data_date asc")
    LiveData<List<GlucoseData>> getDataList(String deviceAddress, long startTime, long endTime);
    @Query("SELECT * from glucose_data where patient_number = :patientNumber and data_date >= :startTime and data_date <= :endTime order by data_date asc")
    LiveData<List<GlucoseData>> getDataList(long patientNumber, long startTime, long endTime);

    @Query("SELECT * from glucose_data where device_address = :deviceAddress and data_date >= :startTime and data_date <= :endTime order by data_date asc")
    List<GlucoseData> queryDataList(String deviceAddress, long startTime, long endTime);

    @Query("SELECT * from glucose_data where device_address = :deviceAddress order by data_date asc")
    LiveData<List<GlucoseData>> getAllDataList(String deviceAddress);

    @Query("SELECT * from glucose_data order by data_date asc")
    LiveData<List<GlucoseData>> getAllDataList();

    @Query("SELECT * from glucose_data where patient_number = :patientNumber order by data_date asc")
    LiveData<List<GlucoseData>> getAllDataList(long patientNumber);

    @Query("SELECT * from glucose_data where patient_number = :patientNumber order by data_date asc")
    List<GlucoseData> getGlucoseAllDataArrayList(long patientNumber);

    @Insert
    void insert(GlucoseData dat);

    @Update
    void update(GlucoseData data);

    @Delete
    void delete(GlucoseData data);
}
