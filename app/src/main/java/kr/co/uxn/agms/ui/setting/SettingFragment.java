package kr.co.uxn.agms.ui.setting;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.preference.PreferenceManager;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import kr.co.uxn.agms.CommonConstant;
import kr.co.uxn.agms.MainActivity;
import kr.co.uxn.agms.R;
import kr.co.uxn.agms.data.room.EventData;
import kr.co.uxn.agms.data.room.GlucoseData;
import kr.co.uxn.agms.data.room.PatientData;
import kr.co.uxn.agms.data.room.SensorRepository;
import kr.co.uxn.agms.ftp.FTPManager;
import kr.co.uxn.agms.ui.BaseFragment;
import kr.co.uxn.agms.ui.admin.LoginAdminActivity;
import kr.co.uxn.agms.ui.patient.CreatePatientActivity;

public class SettingFragment extends BaseFragment {

    final String  TAG=this.getClass().getName();
    Button buttonNewUser;
    Button buttonNewDevice;

    Button buttonAllSave;
    Button buttonCancel;
    Button buttonSend;
    Button buttonSave;
    long patientNumbler;


    final String FTP_ID="agms077";
    String DIRECTORY;
    String patient_id;
    Thread ftpThread;
    boolean ftpresult=false;
    boolean fileSaveNow=false;



    TextView deviceSerial;
    TextView deviceBattery;
    TextView deviceSensorDate;

    private SensorRepository mRepository;

    private void setAdminData() {

        String dataDeviceMac="";
        String dataDeviceBattery="";
        String dataSensorDate="";

        try {
            MainActivity activity=(MainActivity)getActivity();
            DIRECTORY=FTP_ID + "/";
            patient_id=activity.getPatient_ID();
            dataDeviceMac=activity.getDeviceMac();
            dataDeviceBattery=activity.getDeviceBattery() + "V";
            dataSensorDate=activity.getSensorDate();
        } catch (Exception e) {
        }


        if (deviceSerial != null) {
            deviceSerial.setText(getString(R.string.setting_data_device, dataDeviceMac));
        }
        if (deviceBattery != null) {
            deviceBattery.setText(getString(R.string.setting_data_device_battery, dataDeviceBattery));
        }
        if (deviceSensorDate != null) {
            deviceSensorDate.setText(getString(R.string.setting_data_sensor_date, dataSensorDate));
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        setAdminData();
        if (fileSavingNow.get()) {
            showProgress();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        dismissProgress();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRepository=new SensorRepository(getActivity().getApplication());
        View root=null;
        if (CommonConstant.MODE_IS_MEDICAL) {
            root=inflater.inflate(R.layout.fragment_admin_setting, container, false);
        } else {
            root=inflater.inflate(R.layout.fragment_new_setting, container, false);
        }

        deviceSerial=root.findViewById(R.id.device_serial);
        deviceBattery=root.findViewById(R.id.device_battery);
        deviceSensorDate=root.findViewById(R.id.device_sensor_date);


        buttonNewUser=root.findViewById(R.id.button_new_user);
        buttonNewDevice=root.findViewById(R.id.button_new_device);


        buttonAllSave=root.findViewById(R.id.button_allsave);
        buttonCancel=root.findViewById(R.id.button_cancel);
        buttonSend=root.findViewById(R.id.button_send);
        buttonSave=root.findViewById(R.id.button_save);


        buttonNewUser.setOnClickListener(view -> {
            buttonNewUser.setEnabled(false);
            buttonNewUser.postDelayed(() -> buttonNewUser.setEnabled(true), 1000);
            doNewUser();
        });
        buttonNewDevice.setOnClickListener(view -> {
            buttonNewDevice.setEnabled(false);
            buttonNewDevice.postDelayed(() -> buttonNewDevice.setEnabled(true), 1000);
            doNewDevice();
        });

        buttonAllSave.setOnClickListener(view -> {
            buttonAllSave.setEnabled(false);
            buttonAllSave.postDelayed(() -> buttonAllSave.setEnabled(true), 1000);
            doAllSave();
        });
        buttonSave.setOnClickListener(view -> {
            buttonSave.setEnabled(false);
            buttonSave.postDelayed(() -> buttonSave.setEnabled(true), 1000);
            doSave();
        });
        
        buttonCancel.setOnClickListener(view -> {
            buttonCancel.setEnabled(false);
            buttonCancel.postDelayed(() -> buttonCancel.setEnabled(true), 1000);
            doOff();
        });
        buttonSend.setOnClickListener(view -> {
            doSend();
            buttonSend.setEnabled(false);
            buttonSend.postDelayed(() -> sendCheck(), 3000);
            buttonSend.postDelayed(() -> buttonSend.setEnabled(true), 1000);

        });


        Intent intent;
        intent=new Intent(getContext(), LoginAdminActivity.class);
        startActivityForResult(intent, CommonConstant.REQUEST_CODE_CHECK_PASSWORD);



        return root;
    }

     private void doSave(){

        if (checkPermissions()) {



            File folder=new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "AGMS/");

            if (folder.exists()) {
                File[] prevFiles=folder.listFiles();
                if (prevFiles != null) {
                    for (File prevFile : prevFiles) {
                        boolean result=prevFile.delete();
                        if (!result) {
                            break;
                        }
                    }
                }
            }
            LiveData<PatientData> data = mRepository.getLastUser();
            data.observe(getViewLifecycleOwner(), checkdata -> {
                if(checkdata!=null){
                    PatientData patientData=checkdata;
                    SimpleDateFormat formatter=new SimpleDateFormat("yyyyMMdd_HHmm", Locale.KOREA);
                    String time=formatter.format(patientData.getCreateDate());
                    Thread saveThread=new Thread(()->{
                        createGlucosefile(patientData,time);
                        createEventfile(patientData,time);
                    });
                    saveThread.start();
                }
            });
            Toast.makeText(getActivity(), R.string.file_save_over, Toast.LENGTH_SHORT).show();


        } else {
            requestPermission();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CommonConstant.REQUEST_CODE_CHECK_PASSWORD) {
            if (resultCode != Activity.RESULT_OK) {
                try {
                    ((MainActivity)getActivity()).goTabHome();
                } catch (Exception e) {
                }
            } else if (requestCode == CommonConstant.REQUEST_CREATE_FILE_FOR_DATA) {
                if (resultCode == Activity.RESULT_OK) {
                    savePatientDataFile(data.getData());
                } else {
                    createPatientEventFile();
                }
            } else if (requestCode == CommonConstant.REQUEST_CREATE_FILE_FOR_EVENT) {

                if (resultCode == Activity.RESULT_OK) {
                    savePatientEventFile(data.getData());
                } else {
                    createPatientDataFile();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CommonConstant.REQUEST_CODE_PERMISSION_WRITE) {
            boolean writeGranted=false;
            if (grantResults.length == permissions.length) {
                for (int i=0; i < permissions.length; i++) {
                    int grantResult=grantResults[i];
                    String permission=permissions[i];
                    if (permission.equalsIgnoreCase(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        if (grantResult == PackageManager.PERMISSION_GRANTED) {
                            writeGranted=true;
                        }
                    }
                }
            }
            if (writeGranted) {
                doAllSave();
            } else {

                Toast.makeText(getContext(), R.string.write_permission_error_message, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void doAllSave() {

        if (checkPermissions()) {
            if (fileSavingNow.get()) {
                Toast.makeText(getContext(), R.string.file_save_now, Toast.LENGTH_SHORT).show();
                return;
            }
            fileSavingNow.set(true);


            File folder=new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "AGMS/");

            if (folder.exists()) {
                File[] prevFiles=folder.listFiles();
                if (prevFiles != null) {
                    for (File prevFile : prevFiles) {
                        boolean result=prevFile.delete();
                        if (!result) {
                            break;
                        }
                    }
                }
            }

            showProgress();
            new SaveAsyncTask(mRepository).execute();
        } else {
            requestPermission();
        }
    }


    private void showProgress() {
        try {
            ((MainActivity)getActivity()).showActivityProgress();
        } catch (Exception e) {
        }
    }

    private void dismissProgress() {
        try {
            ((MainActivity)getActivity()).dismissActivityProgress();
        } catch (Exception e) {
        }
    }


    private void doNewUser() {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.title_dialog_in_progress)
                .setMessage(R.string.dialog_message_new_user)
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    createNewUserRequested.set(true);
                    doAllSave();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void doNewDevice() {

        new AlertDialog.Builder(getContext())
                .setTitle(R.string.title_dialog)
                .setMessage(R.string.dialog_message_new_device)
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    resetAndGoNewDevice();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();


    }

    private void doAppOff() {
        PreferenceManager.getDefaultSharedPreferences(getContext())
                .edit()
                .putString(CommonConstant.PREF_LAST_CONNECT_DEVICE, null)
                .putLong(CommonConstant.PREF_WARM_UP_START_DATE, 0)
                .putLong(CommonConstant.PREF_DEVICE_FIRST_PAIRING_DATE, 0)
                .putLong(CommonConstant.PREF_DEVICE_NEW_SENSOR_DATE, 0)
                .putLong(CommonConstant.PREF_CURRENT_PATIENT_NUMBER, 0)
                .putString(CommonConstant.PREF_CURRENT_PATIENT_NAME, null)
                .apply();

        try {
            ((MainActivity)getActivity()).doOff();
        } catch (Exception e) {

        }
        getActivity().finishAffinity();


    }

    private void createNewUser() {
        PreferenceManager.getDefaultSharedPreferences(getContext())
                .edit()
                .putString(CommonConstant.PREF_LAST_CONNECT_DEVICE, null)
                .putLong(CommonConstant.PREF_WARM_UP_START_DATE, 0)
                .putLong(CommonConstant.PREF_DEVICE_FIRST_PAIRING_DATE, 0)
                .putLong(CommonConstant.PREF_DEVICE_NEW_SENSOR_DATE, 0)
                .putLong(CommonConstant.PREF_CURRENT_PATIENT_NUMBER, 0)
                .putString(CommonConstant.PREF_CURRENT_PATIENT_NAME, null)
                .apply();

        try {
            ((MainActivity)getActivity()).disconnectAndReset();
        } catch (Exception e) {
        }
        getActivity().finishAffinity();
        Intent intent;
        intent=new Intent(getContext(), CreatePatientActivity.class);
        startActivity(intent);


    }

    private void resetAndGoNewDevice() {


        try {
            ((MainActivity)getActivity()).disconnectAndReset();
        } catch (Exception e) {
        }


    }

    private void doOff() {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.alert_title)
                .setMessage(R.string.dialog_app_off_confirm)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                    appOffRequested.set(true);
                    doAllSave();
                }).show();


    }






    private void requestPermission() {
        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(getContext(), R.string.write_permission_error_message, Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(getContext(), R.string.permission_error_message, Toast.LENGTH_SHORT).show();
            // Request the permission. The result will be received in onRequestPermissionResult().
        }
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                CommonConstant.REQUEST_CODE_PERMISSION_WRITE);
    }

    private boolean checkPermissions() {

        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

    }


    final AtomicInteger glucoseSaveCount=new AtomicInteger(0);
    final AtomicInteger eventSaveCount=new AtomicInteger(0);
    final AtomicBoolean requestGlucoseSave=new AtomicBoolean(false);
    final AtomicBoolean requestEventSave=new AtomicBoolean(false);
    final AtomicBoolean fileSavingNow=new AtomicBoolean(false);
    final AtomicBoolean appOffRequested=new AtomicBoolean(false);
    final AtomicBoolean createNewUserRequested=new AtomicBoolean(false);
    SimpleDateFormat fullDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    @Override
    public boolean onBackPressed() {
        return super.onBackPressed();

    }



    private int mSaveIndex;
    List<PatientData> mPatientList;
    class SaveAsyncTask extends AsyncTask<Void, Void, Void> {
        List<PatientData> mPatientList;
        int index;
        final SensorRepository mRepository;

        public SaveAsyncTask(SensorRepository repository) {
            this.mRepository=repository;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mPatientList=mRepository.getAllPatientList();
            if (mPatientList == null || mPatientList.isEmpty()) {
                return null;
            }
            index=-1;

            File folder=new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "AGMS/");

            boolean success=true;
            if (!folder.exists()) {

                success=folder.mkdirs();
            }
            if (!success) {
                Log.e("FILE", "Directory not created");
                return null;
            }

            for (PatientData patientData : mPatientList) {
                index++;
                SimpleDateFormat formatter=new SimpleDateFormat("yyyyMMdd_HHmm", Locale.KOREA);
                String time=formatter.format(patientData.getCreateDate());
                publishProgress();
                createGlucosefile(patientData,time);
                createEventfile(patientData,time);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            fileSavingNow.set(false);
            if (getActivity() == null || isRemoving()) {
                return;
            }
            dismissProgress();

            if (mPatientList == null || mPatientList.isEmpty()) {

            } else if (index == -1) {
                Toast.makeText(getActivity(), R.string.file_save_error_directory_not_exist, Toast.LENGTH_SHORT).show();
            } else {

            }

            if (appOffRequested.get()) {
                doAppOff();
            }
            if (createNewUserRequested.get()) {
                createNewUser();
            }
            Toast.makeText(getActivity(), R.string.file_save_over, Toast.LENGTH_SHORT).show();
        }


    }

    private void createPatientEventFile() {

        SimpleDateFormat formatter=new SimpleDateFormat("yyyyMMdd_HHmm", Locale.KOREA);
        String time=formatter.format(new Date());
        PatientData patientData=mPatientList.get(mSaveIndex);
        String fileName="ID_" + patientData.getName() + "_" + patientData.getPatientNumber() + "_event_" + time + ".csv";
        createFile(null, fileName, CommonConstant.REQUEST_CREATE_FILE_FOR_EVENT);
    }

    private void createPatientDataFile() {

        mSaveIndex++;
        if (mPatientList == null || mPatientList.isEmpty() || (mSaveIndex >= mPatientList.size())) {
            doWhenSaveOver();
            return;
        }

        SimpleDateFormat formatter=new SimpleDateFormat("yyyyMMdd_HHmm", Locale.KOREA);
        String time=formatter.format(new Date());
        PatientData patientData=mPatientList.get(mSaveIndex);
        String fileName="ID_" + patientData.getName() + "_" + patientData.getPatientNumber() + "_glucose_" + time + ".csv";
        createFile(null, fileName, CommonConstant.REQUEST_CREATE_FILE_FOR_DATA);
    }

    private void savePatientDataFile(Uri uri) {

        // new SavePatientDataTask(uri).execute();
    }

    private void savePatientEventFile(Uri uri) {

        // new SavePatientEventTask(uri).execute();
    }

    private void doSend() {
        ftpThread=new Thread(() -> {
            FTPManager manager=FTPManager.getInstance();
            if (manager.ftpConnect(FTP_ID)) {
                Log.d("FTP", "연결 성공");
                if (manager.ftpUploadFile(DIRECTORY)) {
                    Log.d("FTP", "업로드 성공");
                    if (manager.ftpDisconnect()) {
                        ftpresult=true;
                        ftpThread.interrupt();
                    } else {
                        ftpresult=false;
                    }
                } else {
                    ftpresult=false;
                }
            } else {
                ftpresult=false;
            }
        });
        ftpThread.start();
    }

    private void sendCheck() {
        if (ftpresult) {
            ftpresult=false;
            Toast.makeText(getContext(), R.string.server_transfer_successful, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), R.string.server_transfer_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void doWhenSaveOver() {

        fileSavingNow.set(false);
        if (getActivity() == null || isRemoving()) {
            return;
        }
        dismissProgress();
        if (appOffRequested.get()) {
            doAppOff();
        }
        if (createNewUserRequested.get()) {
            createNewUser();
        }
    }


    private void createFile(Uri pickerInitialUri, String title, int request) {

        Intent intent=new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, title);
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);
        startActivityForResult(intent, request);
    }

    private void createGlucosefile(@NonNull PatientData patientData, String time) {
        List<GlucoseData> glucoseDataList=mRepository.getGlucoseAllDataArrayList(patientData.getPatientNumber());
        OutputStream file=null;
        OutputStreamWriter opStream=null;
        BufferedOutputStream buffer=null;
        try {
            file=new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + "/AGMS/" +
                    "ID_" + patientData.getName() + "_" + patientData.getPatientNumber() + "_glucose_" + time + ".csv");
            buffer=new BufferedOutputStream(file);
            opStream=new OutputStreamWriter(buffer);

            // Write File
            String COMMA="," +
                    "";
            String END="\n";
            opStream.write("patient_name" + COMMA + "patient_number" + COMMA + "device_address" + COMMA +
                    "date" + COMMA + "current" + COMMA + "battery_level" + COMMA + END);
            if (glucoseDataList != null && glucoseDataList.size() > 0) {
                for (int i=0; i < glucoseDataList.size(); i++) {
                    GlucoseData data=glucoseDataList.get(i);
                    opStream.write(data.getUser() + COMMA + data.getPatientNumber() + COMMA
                            + data.getDeviceAddress() + COMMA + fullDateFormat.format(new Date(data.getData_date())) + COMMA
                            + data.getWe_current() + COMMA + data.getBatteryLevel() + END);
                }
            }


        } catch (Exception e) {

        } finally {
            try {
                if (opStream != null) {
                    opStream.close();
                }
            } catch (Exception e) {
            }
            try {
                if (buffer != null) {
                    buffer.close();
                }

            } catch (Exception e) {
            }
            try {
                if (file != null) {
                    file.close();
                }
            } catch (Exception e) {
            }
        }
    }

    private void createEventfile(PatientData patientData, String time) {
        List<EventData> eventDataList=mRepository.getEventDataAllArrayList(patientData.getPatientNumber());
        OutputStream file2=null;
        BufferedOutputStream buffer2=null;
        OutputStreamWriter opStream2=null;
        try {

            file2=new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + "/AGMS/" +
                    "ID_" + patientData.getName() + "_" + patientData.getPatientNumber() + "_event_" + time + ".csv");
            buffer2=new BufferedOutputStream(file2);
            opStream2=new OutputStreamWriter(buffer2);

            // Write File
            String COMMA=",";
            String END="\n";
            opStream2.write("patient_name" + COMMA + "patient_number" + COMMA +
                    "date" + COMMA + "event" + COMMA + "glucose" + END);
            if (eventDataList != null && !eventDataList.isEmpty()) {
                for (int i=0; i < eventDataList.size(); i++) {
                    EventData data=eventDataList.get(i);
                    opStream2.write(data.getPatientName() + COMMA + data.getPatientNumber() + COMMA
                            + fullDateFormat.format(new Date(data.getEventDate())) + COMMA + data.getEventContent() + COMMA
                            + data.getEventGlucose() + END);
                }
            }

        } catch (Exception e) {
            Toast.makeText(getActivity(), R.string.file_error_event_not_found, Toast.LENGTH_SHORT).show();
            fileSavingNow.set(false);
            dismissProgress();
            if (createNewUserRequested.get()) {
                createNewUser();
            }
            if (appOffRequested.get()) {
                doAppOff();
            }
        } finally {
            try {
                if (opStream2 != null) {
                    opStream2.close();
                }
            } catch (Exception e) {
            }
            try {
                if (buffer2 != null) {
                    buffer2.close();
                }

            } catch (Exception e) {
            }
            try {
                if (file2 != null) {
                    file2.close();
                }
            } catch (Exception e) {
            }
        }
    }



}
