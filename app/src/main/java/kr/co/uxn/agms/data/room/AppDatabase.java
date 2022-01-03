package kr.co.uxn.agms.data.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.atomic.AtomicReference;

@Database(entities = {UserData.class, SensorLog.class, SensorData.class, GlucoseData.class,
        CalibrationData.class, DeviceData.class, PatientData.class, EventData.class, AdminData.class}, version = RoomConstants.DATABASE_VERSION)
public abstract class AppDatabase extends RoomDatabase {

    public abstract SensorLogDao sensorLogDao();
    public abstract SensorDataDao sensorDataDao();
    public abstract UserDataDao userDataDao();
    public abstract GlucoseDataDao glucoseDataDao();
    public abstract CalibrationDataDao calibrationDataDao();
    public abstract PatientDataDao patientDataDao();
    public abstract DeviceDataDao deviceDataDao();
    public abstract EventDataDao eventDataDao();
    public abstract AdminDataDao adminDataDao();

    private static final AtomicReference<AppDatabase> INSTANCE = new AtomicReference<>();

    static AppDatabase getDatabase(final Context context){
        if(INSTANCE.get() == null){
            synchronized (AppDatabase.class){
                if(INSTANCE.get() == null){
                    INSTANCE.set(Room.databaseBuilder(context.getApplicationContext()
                            , AppDatabase.class, RoomConstants.DATABASE_NAME)
                            .fallbackToDestructiveMigration()
                            .build());
                }
            }
        }
        return INSTANCE.get();
    }

}
