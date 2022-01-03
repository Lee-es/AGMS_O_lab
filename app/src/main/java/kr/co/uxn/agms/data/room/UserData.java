package kr.co.uxn.agms.data.room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = RoomConstants.TABLE_USER_DATA)
public class UserData {
    @PrimaryKey(autoGenerate = true)
    private long uid;

    @ColumnInfo(name = "email")
    private String email;

    @ColumnInfo(name = "password")
    private String password;

    @ColumnInfo(name = "event_time")
    private long eventTime;

    public UserData(long uid, String email, String password, long eventTime) {
        this.uid = uid;
        this.email = email;
        this.password = password;
        this.eventTime = eventTime;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getEventTime() {
        return eventTime;
    }

    public void setEventTime(long eventTime) {
        this.eventTime = eventTime;
    }
}
