package kr.co.uxn.agms.data.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface UserDataDao {

    @Query("SELECT * from user_data where email = :email and password = :password order by uid desc limit 1")
    UserData getUserWithPassword(String email, String password);

    @Query("SELECT * from user_data where email = :email order by uid desc limit 1")
    UserData getUser(String email);

    @Insert
    void insert(UserData dat);

    @Update
    void update(UserData data);

    @Delete
    void delete(UserData data);
}
