package kr.co.uxn.agms.data.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PatientDataDao {
    @Query("SELECT * from patient_data where patient_number = :patientNumber order by uid desc limit 1")
    PatientData getData(long patientNumber);

    @Query("SELECT * from patient_data where patient_number = :patientNumber order by uid desc limit 1")
    LiveData<PatientData> getLiveData(long patientNumber);

    @Query("SELECT * from patient_data order by uid asc")
    LiveData<List<PatientData>> getAll();

    @Query("SELECT * from patient_data order by uid asc")
    List<PatientData> getAllList();

    @Query("SELECT * from patient_data order by uid desc limit 1")
    LiveData<PatientData> getLastUser();

    @Query("SELECT * from patient_data where name = :patientName order by uid asc")
    List<PatientData> queryPatient(String patientName);




    @Insert
    void insert(PatientData dat);

    @Update
    void update(PatientData data);

    @Delete
    void delete(PatientData data);
}
