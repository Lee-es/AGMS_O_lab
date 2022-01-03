package kr.co.uxn.agms.ui.admin;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.lifecycle.LiveData;
import androidx.preference.PreferenceManager;

import kr.co.uxn.agms.CommonConstant;
import kr.co.uxn.agms.R;
import kr.co.uxn.agms.data.room.AdminData;
import kr.co.uxn.agms.data.room.SensorRepository;
import kr.co.uxn.agms.util.PasswordUtil;

public class LoginAdminActivity extends AppCompatActivity {



    private EditText idEditText;
    private EditText passwordEditText;

    private AppCompatButton buttonContinue;

    private RelativeLayout loadingWrap;

    private SensorRepository mRepository;
    private String mAdminId;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_admin_login);

        setResult(RESULT_OK);
        finish();
        View parentView = findViewById(R.id.container);
        parentView.setOnClickListener(view -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            View tmp = getCurrentFocus();
            if (tmp == null) {
                tmp = new View(LoginAdminActivity.this);
            }
            imm.hideSoftInputFromWindow(tmp.getWindowToken(), 0);
        });
        mRepository = new SensorRepository(getApplication());

        idEditText = findViewById(R.id.admin_id);
        passwordEditText = findViewById(R.id.password);

        buttonContinue = findViewById(R.id.buttonContinue);
        loadingWrap = findViewById(R.id.loading_wrap);

        AppCompatButton changePasswordButton = findViewById(R.id.changePasswordButton);
        changePasswordButton.setOnClickListener(view -> {
            view.setEnabled(false);
            view.postDelayed(() -> view.setEnabled(true),1000);
            changePassword();
        });

        final TextWatcher afterTextChangedListener = new TextWatcher() {
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
                buttonContinue.setEnabled(!TextUtils.isEmpty(idEditText.getText()) &&
                        !TextUtils.isEmpty(passwordEditText.getText()));
            }
        };
        idEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);


        passwordEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                login();
            }
            return false;
        });

        buttonContinue.setOnClickListener(v -> {
            v.setEnabled(false);
            v.postDelayed(() -> v.setEnabled(true),1000);

            login();
        });
        loadLastAdmin();
    }
    public void loadLastAdmin(){
        LiveData<AdminData> data = mRepository.getLastLiveAdminData();
        data.observe(this, adminData -> {
            if(adminData!=null){
                mAdminId = adminData.getAdminId();

                idEditText.setText(adminData.getAdminId());
            } else {
                Toast.makeText(LoginAdminActivity.this, R.string.error_no_admin_data_create, Toast.LENGTH_SHORT).show();
                Intent intent =new Intent(LoginAdminActivity.this, CreateAdminActivity.class);
                startActivity(intent);
            }
        });

    }

    public void changePassword(){
        Intent intent = new Intent(this,ChangePasswordActivity.class);
        intent.putExtra(ChangePasswordActivity.ARGS_ID, mAdminId);
        startActivityForResult(intent, CommonConstant.REQUEST_CODE_CHANGE_PASSWORD);

    }
    public void login(){
        if(TextUtils.isEmpty(idEditText.getText())){
            idEditText.setError(getString(R.string.error_empty_admin_id));
            idEditText.requestFocus();
            return;
        }
        if(TextUtils.isEmpty(passwordEditText.getText())){
            passwordEditText.setError(getString(R.string.error_empty_admin_password));
            passwordEditText.requestFocus();
            return;
        }


        if(passwordEditText.getText().length()<CommonConstant.ADMIN_MIN_PASSWORD_LENGTH){
            passwordEditText.setError(getString(R.string.error_empty_admin_password_length, CommonConstant.ADMIN_MIN_PASSWORD_LENGTH));
            passwordEditText.requestFocus();
        }



        new QueryAsyncTask(mRepository).execute(idEditText.getText().toString(),
                passwordEditText.getText().toString());
    }

    class QueryAsyncTask extends AsyncTask<String,Void, Integer> {
        SensorRepository repository;

        public QueryAsyncTask(SensorRepository repository) {
            this.repository = repository;

        }

        @Override
        protected Integer doInBackground(String... param) {
            if(param == null || param.length !=2){
                return -1;
            }
            String id = param[0];
            String password = param[1];


            AdminData data = repository.getAdminData(id);
            if(data == null || TextUtils.isEmpty(data.getAdminId())){
                return -1;
            }
            boolean result = PasswordUtil.verify(password, data.getPassword());
            if(!result){
                return -2;
            }
            data.setLoginTime(System.currentTimeMillis());
            repository.updateAdminData(data);

            PreferenceManager.getDefaultSharedPreferences(LoginAdminActivity.this)
                    .edit()
                    .putString(CommonConstant.PREF_CURRENT_ADMIN_ID, id)
                    .apply();

            return 1;
        }

        @Override
        protected void onPostExecute(Integer code) {
            repository = null;
            loadingWrap.setVisibility(View.GONE);

            if(code == -2){

                new AlertDialog.Builder(LoginAdminActivity.this)
                        .setTitle(R.string.alert_title)
                        .setMessage(R.string.error_passowrd_not_equal)
                        .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                            dialogInterface.dismiss();
                            passwordEditText.requestFocus();
                        })
                        .show();
                return;
            }
            if(code == -1){
                buttonContinue.setEnabled(false);
                new AlertDialog.Builder(LoginAdminActivity.this)
                        .setTitle(R.string.alert_title)
                        .setMessage(R.string.error_no_admin_data)
                        .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> dialogInterface.dismiss())
                        .show();

            } else {
                setResult(RESULT_OK);
                finish();
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==CommonConstant.REQUEST_CODE_CHANGE_PASSWORD && resultCode==RESULT_OK){
            setResult(RESULT_OK);
            finish();
        }
    }
}