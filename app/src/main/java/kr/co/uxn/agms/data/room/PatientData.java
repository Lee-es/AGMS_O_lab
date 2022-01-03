package kr.co.uxn.agms.data.room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = RoomConstants.TABLE_PATIENT_DATA)
public class PatientData {
    @PrimaryKey(autoGenerate = true)
    private long uid;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "patient_number")
    private long patientNumber;

    @ColumnInfo(name = "create_date")
    private long createDate;

    @ColumnInfo(name = "updateDate")
    private long updateDate;

    @ColumnInfo(name = "deleteDate")
    private long deleteDate;

    public PatientData(long uid, String name, long patientNumber, long createDate, long updateDate, long deleteDate) {
        this.uid = uid;
        this.name = name;
        this.patientNumber = patientNumber;
        this.createDate = createDate;
        this.updateDate = updateDate;
        this.deleteDate = deleteDate;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getPatientNumber() {
        return patientNumber;
    }

    public void setPatientNumber(long patientNumber) {
        this.patientNumber = patientNumber;
    }

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    public long getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(long updateDate) {
        this.updateDate = updateDate;
    }

    public long getDeleteDate() {
        return deleteDate;
    }

    public void setDeleteDate(long deleteDate) {
        this.deleteDate = deleteDate;
    }
}
