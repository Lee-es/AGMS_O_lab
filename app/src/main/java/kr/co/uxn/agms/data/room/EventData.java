package kr.co.uxn.agms.data.room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = RoomConstants.TABLE_EVENT_DATA)
public class EventData {
    @PrimaryKey(autoGenerate = true)
    private long uid;
    @ColumnInfo(name = "patient_name")
    private String patientName;

    @ColumnInfo(name = "patient_number")
    private long patientNumber;

    @ColumnInfo(name = "event_date")
    private long eventDate;

    @ColumnInfo(name = "event_content")
    private String eventContent;

    @ColumnInfo(name = "event_glucose")
    private float eventGlucose;

    public EventData(long uid, String patientName, long patientNumber, long eventDate, String eventContent, float eventGlucose) {
        this.uid = uid;
        this.patientName = patientName;
        this.patientNumber = patientNumber;
        this.eventDate = eventDate;
        this.eventContent = eventContent;
        this.eventGlucose = eventGlucose;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public long getPatientNumber() {
        return patientNumber;
    }

    public void setPatientNumber(long patientNumber) {
        this.patientNumber = patientNumber;
    }

    public long getEventDate() {
        return eventDate;
    }

    public void setEventDate(long eventDate) {
        this.eventDate = eventDate;
    }

    public String getEventContent() {
        return eventContent;
    }

    public void setEventContent(String eventContent) {
        this.eventContent = eventContent;
    }

    public float getEventGlucose() {
        return eventGlucose;
    }

    public void setEventGlucose(float eventGlucose) {
        this.eventGlucose = eventGlucose;
    }
}
