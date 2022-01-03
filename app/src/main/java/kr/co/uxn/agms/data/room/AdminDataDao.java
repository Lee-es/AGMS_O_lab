package kr.co.uxn.agms.data.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AdminDataDao {
    @Query("SELECT * from admin_data where admin_id = :adminId order by create_time desc limit 1")
    LiveData<AdminData> getLiveData(String adminId);

    @Query("SELECT * from admin_data where admin_id = :adminId order by create_time desc limit 1")
    AdminData getData(String adminId);


    @Query("SELECT * from admin_data order by login_time desc limit 1")
    AdminData getLastUser();

    @Query("SELECT * from admin_data order by login_time desc limit 1")
    LiveData<AdminData> getLiveDataLastUser();


    @Insert
    void insert(AdminData data);

    @Update
    void update(AdminData data);

    @Delete
    void delete(AdminData data);
}
