package kr.co.uxn.agms.ui.event;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import kr.co.uxn.agms.R;
import kr.co.uxn.agms.data.room.EventData;
import kr.co.uxn.agms.data.room.PatientData;
import kr.co.uxn.agms.data.room.SensorRepository;

public class EventFragment extends Fragment {

    private SensorRepository mRepository;

    private EditText timeEditText;
    private EditText eventEditText;
    private EditText glucoseEditText;
    private AppCompatButton create;
    private long mEventTime = System.currentTimeMillis();
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private String mPatientName;
    private long mPatientNumber;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_event, container, false);
        root.setOnClickListener(view -> {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
             View tmp = getActivity().getCurrentFocus();
            if (tmp == null) {
                tmp = new View(getActivity());
            }
            imm.hideSoftInputFromWindow(tmp.getWindowToken(), 0);
        });
        timeEditText = root.findViewById(R.id.username);
        timeEditText.setOnClickListener(view -> {
            timeEditText.setClickable(false);
            timeEditText.postDelayed(() -> timeEditText.setClickable(true),1000);
            showTimeDialog();
        });
        eventEditText = root.findViewById(R.id.patient_number);
        glucoseEditText = root.findViewById(R.id.event);
        mRepository = new SensorRepository(getActivity().getApplication());
        create = root.findViewById(R.id.create);

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                create.setEnabled(!TextUtils.isEmpty(eventEditText.getText()) || !TextUtils.isEmpty(glucoseEditText.getText()));
            }
        };
        eventEditText.addTextChangedListener(watcher);
        glucoseEditText.addTextChangedListener(watcher);

        eventEditText.setOnEditorActionListener((textView, i, keyEvent) -> {
            glucoseEditText.requestFocus();
            return false;
        });
        glucoseEditText.setOnEditorActionListener((textView, i, keyEvent) -> {
            createEvent();
            return false;
        });

        create.setOnClickListener(view -> {
            create.setClickable(false);
            create.postDelayed(() -> create.setClickable(true),1000);
            createEvent();
        });
        init();
        return root;
    }

    private void showTimeDialog(){
        showDatePicker();
    }

    private void init(){
        timeEditText.setText(null);
        eventEditText.setText(null);
        glucoseEditText.setText(null);

        eventEditText.setEnabled(false);
        glucoseEditText.setEnabled(false);

        create.setEnabled(false);
        mEventTime = System.currentTimeMillis();

        LiveData<PatientData> data = mRepository.getLastUser();
        data.observe(getViewLifecycleOwner(), patientData -> {
            if(patientData!=null){
                mPatientName = patientData.getName();
                mPatientNumber = patientData.getPatientNumber();
            }

        });

    }



    private void createEvent(){
        long time = mEventTime;
        if(time < 1 || TextUtils.isEmpty(timeEditText.getText())){
            timeEditText.setError(getString(R.string.error_invalid_time) );
            return;
        }
        String event = "";
        if(!TextUtils.isEmpty(eventEditText.getText())){
            event = eventEditText.getText().toString();
        }
        float glucose = 0;
        try {
            glucose = Float.valueOf(glucoseEditText.getText().toString());
        }catch (Exception e){
            glucose = 0;
        }

        if(TextUtils.isEmpty(event) && glucose == 0){
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.alert_title)
                    .setMessage(R.string.error_empty_event)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            return;
        }

        final EventData data = new EventData(0,
                 mPatientName, mPatientNumber,  time,event, glucose);
        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.text_event_time, format.format(new Date(time))));
        sb.append("\n");
        sb.append("\n");
        sb.append(getString(R.string.text_event_content, event==null?"":event));
        sb.append("\n");
        sb.append("\n");
        DecimalFormat decimalFormat = new DecimalFormat("0.######");
        decimalFormat.setRoundingMode(RoundingMode.DOWN);
        sb.append(getString(R.string.text_event_glucose,decimalFormat.format(glucose) ));

        sb.append("\n");
        sb.append("\n");
        sb.append(getString(R.string.text_confirm_event));

        new AlertDialog.Builder(getContext())
                .setTitle(R.string.confirm)
                .setMessage(sb.toString())
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                    mRepository.createEventData(data);
                    Toast.makeText(getActivity(), R.string.toast_save_event,Toast.LENGTH_SHORT).show();
                    init();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();



    }
    private void showDatePicker(){
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.setTime(mEventTime);
        newFragment.setListener((datePicker, i, i1, i2) -> {
            Log.e("check",String.format("date : %d,%d,%d", i, i1, i2));
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(mEventTime);
            calendar.set(Calendar.YEAR,i);
            calendar.set(Calendar.MONTH, i1);
            calendar.set(Calendar.DAY_OF_MONTH, i2);
            mEventTime = calendar.getTimeInMillis();
            showTimePicker();
        });
        newFragment.show(getChildFragmentManager(), "datePicker");
    }
    private void showTimePicker(){
        TimePickerFragment newFragment = new TimePickerFragment();
        newFragment.setListener((timePicker, i, i1) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(mEventTime);
            calendar.set(Calendar.HOUR_OF_DAY, i);
            calendar.set(Calendar.MINUTE,i1);
            calendar.set(Calendar.SECOND,0);
            calendar.set(Calendar.MILLISECOND,0);

            long tmp = calendar.getTimeInMillis();
            if(tmp<System.currentTimeMillis()){
                mEventTime = tmp;
                timeEditText.setText(format.format(calendar.getTime()));

                eventEditText.setEnabled(true);
                glucoseEditText.setEnabled(true);
            } else {
                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.alert_title)
                        .setMessage(R.string.dialog_no_future)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            }

        });
        newFragment.setTime(mEventTime);
        newFragment.show(getChildFragmentManager(), "timePicker");
    }
    public static class DatePickerFragment extends DialogFragment {

        DatePickerDialog.OnDateSetListener mListener;
        private long mTime = System.currentTimeMillis();
        public void setTime(long time){
            this.mTime = time;
        }
        public void setListener(DatePickerDialog.OnDateSetListener listener){
            mListener = listener;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            c.setTimeInMillis(mTime);
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog dialog = new DatePickerDialog(getActivity(), mListener, year, month, day);
            dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000 * 60*60*24*10);
            return dialog;
        }

    }
    public static class TimePickerFragment extends DialogFragment {
        TimePickerDialog.OnTimeSetListener mListener;
        long mTime = System.currentTimeMillis();
        public void setTime(long millisecond){
            mTime = millisecond;
        }

        public void setListener(TimePickerDialog.OnTimeSetListener mListener) {
            this.mListener = mListener;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            c.setTimeInMillis(mTime);
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            return new TimePickerDialog(getActivity(), this.mListener, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));

        }

    }
}
