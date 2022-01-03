package kr.co.uxn.agms.ui.admin;

import android.app.AlertDialog;
import android.content.Context;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.preference.PreferenceManager;

import kr.co.uxn.agms.CommonConstant;
import kr.co.uxn.agms.R;
import kr.co.uxn.agms.data.room.AdminData;
import kr.co.uxn.agms.data.room.SensorRepository;
import kr.co.uxn.agms.util.PasswordUtil;

public class ChangePasswordActivity extends AppCompatActivity {

    public static final String ARGS_ID = "args_id";



    private EditText passwordEditText;
    private EditText passwordCheckEditText;
    private EditText passwordHintEditText;

    private AppCompatButton buttonContinue;
    private RelativeLayout loadingWrap;

    private SensorRepository mRepository;

    private String mAdminId;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_change_password);
        View parentView = findViewById(R.id.container);
        parentView.setOnClickListener(view -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            View tmp = getCurrentFocus();
            if (tmp == null) {
                tmp = new View(ChangePasswordActivity.this);
            }
            imm.hideSoftInputFromWindow(tmp.getWindowToken(), 0);
        });
        mRepository = new SensorRepository(getApplication());

        mAdminId = getIntent().getStringExtra(ARGS_ID);
        passwordEditText = findViewById(R.id.password);
        passwordCheckEditText = findViewById(R.id.password_check);
        passwordHintEditText = findViewById(R.id.password_hint);

        buttonContinue = findViewById(R.id.buttonContinue);
        loadingWrap = findViewById(R.id.loading_wrap);



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
                buttonContinue.setEnabled(!TextUtils.isEmpty(passwordEditText.getText()) &&
                        !TextUtils.isEmpty(passwordCheckEditText.getText()) &&
                        !TextUtils.isEmpty(passwordHintEditText.getText()));
            }
        };

        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordCheckEditText.addTextChangedListener(afterTextChangedListener);
        passwordHintEditText.addTextChangedListener(afterTextChangedListener);


        passwordCheckEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                changePassword();
            }
            return false;
        });

        buttonContinue.setOnClickListener(v -> {
            v.setEnabled(false);
            v.postDelayed(new Runnable() {
                @Override
                public void run() {
                    v.setEnabled(true);
                }
            },1000);
            changePassword();
        });
    }

    public void changePassword(){

        if(TextUtils.isEmpty(passwordEditText.getText())){
            passwordEditText.setError(getString(R.string.error_empty_admin_password));
            passwordEditText.requestFocus();
            return;
        }
        if(TextUtils.isEmpty(passwordCheckEditText.getText())){
            passwordCheckEditText.setError(getString(R.string.error_empty_admin_password_check));
            passwordCheckEditText.requestFocus();
            return;
        }
        if(TextUtils.isEmpty(passwordHintEditText.getText())){
            passwordHintEditText.setError(getString(R.string.error_empty_admin_password_hint));
            passwordHintEditText.requestFocus();
            return;
        }

        if(passwordEditText.getText().length()<CommonConstant.ADMIN_MIN_PASSWORD_LENGTH){
            passwordEditText.setError(getString(R.string.error_empty_admin_password_length, CommonConstant.ADMIN_MIN_PASSWORD_LENGTH));
            passwordEditText.requestFocus();
            return;
        }
        if(passwordCheckEditText.getText().length()<CommonConstant.ADMIN_MIN_PASSWORD_LENGTH){
            passwordHintEditText.setError(getString(R.string.error_empty_admin_password_length, CommonConstant.ADMIN_MIN_PASSWORD_LENGTH));
            passwordHintEditText.requestFocus();
            return;
        }
        if(!passwordCheckEditText.getText().toString().equals(passwordEditText.getText().toString())){
            passwordEditText.setError(getString(R.string.error_empty_admin_password_not_equal));
            passwordEditText.requestFocus();
            return;
        }
        if(TextUtils.isEmpty(mAdminId)){
            new AlertDialog.Builder(this)
                    .setTitle(R.string.alert_title)
                    .setMessage(R.string.message_id_empty)
                    .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        finish();
                    })
                    .show();
            return;
        }

        new QueryAsyncTask(mRepository).execute(mAdminId,
                passwordEditText.getText().toString(),
                passwordHintEditText.getText().toString());
    }

    class QueryAsyncTask extends AsyncTask<String,Void, Integer> {
        SensorRepository repository;

        public QueryAsyncTask(SensorRepository repository) {
            this.repository = repository;

        }

        @Override
        protected Integer doInBackground(String... param) {
            if(param == null || param.length !=3){
                return -1;
            }
            String id = param[0];
            String password = param[1];
            String hint = param[2];

            AdminData data = repository.getAdminData(id);
            if(data == null || TextUtils.isEmpty(data.getAdminId())){
                return -1;
            }
            if(!data.getPasswordHint().equals(hint)){
                return -2;
            }
            data.setPassword(PasswordUtil.getEncrypt(password));
            data.setEventTime(System.currentTimeMillis());
            repository.updateAdminData(data);
            Toast.makeText(ChangePasswordActivity.this, R.string.message_pasword_chagned, Toast.LENGTH_SHORT).show();
            PreferenceManager.getDefaultSharedPreferences(ChangePasswordActivity.this)
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
                buttonContinue.setEnabled(false);
                new AlertDialog.Builder(ChangePasswordActivity.this)
                        .setTitle(R.string.alert_title)
                        .setMessage(R.string.error_password_hint_not_equal)
                        .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> dialogInterface.dismiss())
                        .show();
                return;
            }
            if(code == -1){
                buttonContinue.setEnabled(false);
                new AlertDialog.Builder(ChangePasswordActivity.this)
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


}