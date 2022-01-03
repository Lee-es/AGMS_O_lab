package kr.co.uxn.agms.data.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface EventDataDao {
    @Query("SELECT * from event_data where patient_number = :patient_number order by uid asc")
    LiveData<List<EventData>> getLiveData(long patient_number);

    @Query("SELECT * from event_data where patient_number = :patient_number order by uid asc")
    List<EventData> getAllList(long patient_number);

    @Query("SELECT * from event_data order by uid asc")
    LiveData<List<EventData>> getAllLiveData();

    @Insert
    void insert(EventData dat);

    @Update
    void update(EventData data);

    @Delete
    void delete(EventData data);
}
