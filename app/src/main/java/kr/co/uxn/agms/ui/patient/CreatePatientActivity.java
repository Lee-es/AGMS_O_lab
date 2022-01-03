package kr.co.uxn.agms.ui.patient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import kr.co.uxn.agms.CommonConstant;
import kr.co.uxn.agms.R;
import kr.co.uxn.agms.data.room.PatientData;
import kr.co.uxn.agms.data.room.SensorRepository;
import kr.co.uxn.agms.util.StepHelper;

public class CreatePatientActivity extends AppCompatActivity {

    EditText usernameEditText;
    EditText numberEditText ;
    private SensorRepository mRepository;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_create_patient);
        mRepository = new SensorRepository(getApplication());
        usernameEditText = findViewById(R.id.username);
        numberEditText = findViewById(R.id.patient_number);

        final Button loginButton = findViewById(R.id.create);

        usernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(TextUtils.isEmpty(usernameEditText.getText())){
                    usernameEditText.setError(getString(R.string.error_empty_patient_name));
                } else {
                    usernameEditText.setError(null);
                }
            }
        });
        numberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(TextUtils.isEmpty(numberEditText.getText())){
                    numberEditText.setError(getString(R.string.error_empty_patient_number));
                } else {
                    if(TextUtils.isDigitsOnly(numberEditText.getText())){
                        numberEditText.setError(null);
                        loginButton.setEnabled(true);
                    } else {
                        numberEditText.setError(getString(R.string.error_not_number_error_patient_number));
                    }
                }
            }
        });

        numberEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                createPatient();
            }
            return false;
        });

        loginButton.setOnClickListener(v -> createPatient());
    }

    public void createPatient(){
        if(TextUtils.isEmpty(usernameEditText.getText())){
            usernameEditText.setError(getString(R.string.error_empty_patient_name));
            usernameEditText.requestFocus();
            return;
        }
        if(TextUtils.isEmpty(numberEditText.getText())){
            numberEditText.setError(getString(R.string.error_empty_patient_number));
            numberEditText.requestFocus();
            return;
        } else {
            if(TextUtils.isDigitsOnly(numberEditText.getText())){
                numberEditText.setError(null);
            } else {
                numberEditText.setError(getString(R.string.error_not_number_error_patient_number));
                numberEditText.requestFocus();
                return;
            }
        }
        long number = 0;
        String name = usernameEditText.getText().toString();
        try{
            number = Long.valueOf(numberEditText.getText().toString());
        }catch (Exception e){
            number = 0;
        }
        if(number<1){
            numberEditText.setError(getString(R.string.error_not_number_error_patient_number));
            numberEditText.requestFocus();
            return;
        }
        StringBuilder sb= new StringBuilder();
        sb.append(getString(R.string.text_patient_name, name));
        sb.append("\n");
        sb.append(getString(R.string.text_patient_number, String.valueOf(number)));
        sb.append("\n");
        sb.append("\n");
        sb.append(getString(R.string.dialog_create_patient_confirm));

        final String patientName = name;
        final long patientNumber = number;
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm)
                .setMessage(sb.toString())
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                    new QueryAsyncTask(mRepository,patientName,patientNumber).execute();
                })
                .setNegativeButton(android.R.string.cancel,null)
                .show();

    }

    class QueryAsyncTask extends AsyncTask<Long,Void,PatientData>{
        SensorRepository repository;
        String name;
        long number;
        public QueryAsyncTask(SensorRepository repository, String name, long number) {
            this.repository = repository;
            this.name = name;
            this.number = number;
        }

        @Override
        protected PatientData doInBackground(Long... longs) {
            return repository.getPatientData(number);
        }

        @Override
        protected void onPostExecute(PatientData patientData) {
            super.onPostExecute(patientData);
            if(patientData!=null){
                new AlertDialog.Builder(CreatePatientActivity.this)
                        .setTitle(R.string.alert_title)
                        .setMessage(R.string.error_duplicate_patient_number)
                        .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                            dialogInterface.dismiss();
                            numberEditText.requestFocus();
                        })
                        .show();
            } else {


                PreferenceManager.getDefaultSharedPreferences(CreatePatientActivity.this)
                        .edit()
                        .putLong(CommonConstant.PREF_CURRENT_PATIENT_NUMBER,number)
                        .putString(CommonConstant.PREF_CURRENT_PATIENT_NAME,name )
                        .putString(CommonConstant.PREF_USERNAME, name)
                        .apply();

                mRepository.createPatient(name,number);

                setResult(Activity.RESULT_OK);

                Intent intent = StepHelper.checkNextState(CreatePatientActivity.this, StepHelper.ScreenStep.LOGIN);

                if(intent!=null){
                    startActivity(intent);
                }
                finish();
            }
            repository = null;
        }
    }


}