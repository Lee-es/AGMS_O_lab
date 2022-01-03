package kr.co.uxn.agms.data.room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = RoomConstants.TABLE_ADMIN_DATA)
public class AdminData {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "uid")
    private long uid;

    @ColumnInfo(name = "admin_id")
    private String adminId;

    @ColumnInfo(name = "password")
    private String password;

    @ColumnInfo(name = "password_hint")
    private String passwordHint;

    @ColumnInfo(name = "create_time")
    private long createTime;

    @ColumnInfo(name = "event_time")
    private long eventTime;

    @ColumnInfo(name = "login_time")
    private long loginTime;

    public AdminData(long uid, String adminId, String password, String passwordHint, long createTime, long eventTime, long loginTime) {
        this.uid = uid;
        this.adminId = adminId;
        this.password = password;
        this.passwordHint = passwordHint;
        this.createTime = createTime;
        this.eventTime = eventTime;
        this.loginTime = loginTime;
    }

    public long getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(long loginTime) {
        this.loginTime = loginTime;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordHint() {
        return passwordHint;
    }

    public void setPasswordHint(String passwordHint) {
        this.passwordHint = passwordHint;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getEventTime() {
        return eventTime;
    }

    public void setEventTime(long eventTime) {
        this.eventTime = eventTime;
    }
}
