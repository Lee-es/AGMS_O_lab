package kr.co.uxn.agms.ui.patient;

import android.app.Activity;
import android.app.AlertDialog;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import kr.co.uxn.agms.CommonConstant;
import kr.co.uxn.agms.R;
import kr.co.uxn.agms.data.room.PatientData;
import kr.co.uxn.agms.data.room.SensorRepository;
import kr.co.uxn.agms.util.StepHelper;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CreatePatientActivity extends AppCompatActivity {

    EditText usernameEditText;
    EditText numberEditText ;
    EditText experiment_researcher_EditText;
    private SensorRepository mRepository;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_create_patient);
        mRepository = new SensorRepository(getApplication());
        usernameEditText = findViewById(R.id.username);
        numberEditText = findViewById(R.id.patient_number);
        experiment_researcher_EditText=findViewById(R.id.experiment_researcher);

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


        experiment_researcher_EditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(TextUtils.isEmpty(experiment_researcher_EditText.getText())){
                    experiment_researcher_EditText.setError(getString(R.string.error_experiment_researcher));
                } else {
                    experiment_researcher_EditText.setError(null);
                }

            }
        });

        loginButton.setOnClickListener(v -> createPatient());
    }

    public void createPatient(){
        if(TextUtils.isEmpty(usernameEditText.getText())){
            usernameEditText.setError(getString(R.string.error_empty_patient_name));
            usernameEditText.requestFocus();
            return;
        }

        if(TextUtils.isEmpty(experiment_researcher_EditText.getText())){
            experiment_researcher_EditText.setError(getString(R.string.error_experiment_researcher));
            experiment_researcher_EditText.requestFocus();
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
        String researcher=experiment_researcher_EditText.getText().toString();
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
        sb.append(getString(R.string.text_experiment_researcher, researcher));
        sb.append("\n");
        sb.append(getString(R.string.text_patient_name, name));
        sb.append("\n");
        sb.append(getString(R.string.text_patient_number, String.valueOf(number)));
        sb.append("\n");
        sb.append("\n");
        sb.append(getString(R.string.dialog_create_patient_confirm));

        final String patientName = name;
        final long patientNumber =number;
        final String experiment_researcher=researcher;
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm)
                .setMessage(sb.toString())
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                    new Insert_Experiment_ID(patientName,patientNumber,experiment_researcher).execute();
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

    class Insert_Experiment_ID extends AsyncTask<Void,Void, JSONObject>{

        String patient_id;
        String experiment_researcher;
        long    patient_number;


        public Insert_Experiment_ID(String patient_id,long patient_number,String experiment_researcher)
        {
            this.patient_id=patient_id;
            this.patient_number=patient_number;
            this.experiment_researcher=experiment_researcher;
        }

        @Override
        protected JSONObject doInBackground(Void... voids) {

            try{
                HttpUrl httpUrl=new HttpUrl.Builder()
                        .scheme("http")
                        .host(CommonConstant.HOST)
                        .port(CommonConstant.PORT)
                        .addPathSegment("insert_experiment_id")
                        .build();

                OkHttpClient client  =new OkHttpClient();
                JSONObject jsonInput =new JSONObject();

                jsonInput.put("patient_id",patient_id);
                jsonInput.put("patient_number",patient_number);
                jsonInput.put("experiment_researcher",experiment_researcher);

                RequestBody reqBody =RequestBody.create(
                        MediaType.parse("application/json; charset=utf-8"),
                        jsonInput.toString()
                );
                Request request=new Request.Builder()
                        .post(reqBody)
                        .url(httpUrl)
                        .build();

                Response responses = null;
                responses = client.newCall(request).execute();
                JSONObject data=new JSONObject(responses.body().string());
                System.out.println(data);

                return data;

            } catch (JSONException | IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);

            if(jsonObject!=null)
            {
                try {
                    boolean success=jsonObject.getBoolean("success");

                    if(success){
                        new QueryAsyncTask(mRepository,this.patient_id,this.patient_number).execute();
                    }
                    else{
                        new AlertDialog.Builder(CreatePatientActivity.this)
                                .setTitle(R.string.error)
                                .setMessage("서버에 등록된 아이디 입니다.\n다시작성해주세요")
                                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                                })
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }else{
                new AlertDialog.Builder(CreatePatientActivity.this)
                        .setTitle(R.string.error)
                        .setMessage("서버에 등록된 아이디 입니다.\n다시작성해주세요 ")
                        .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                        })
                        .show();
                new Insert_Experiment_ID(this.patient_id,this.patient_number,this.experiment_researcher).execute();
            }
        }
    }


}