package kr.co.uxn.agms.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import kr.co.uxn.agms.R;
import kr.co.uxn.agms.util.GraphUtil;


public class CurrentRangeDialog extends Dialog {

    private EditText et_min;
    private EditText et_max;
    private TextView tv_min;
    private TextView tv_max;
    private Button btn_ok;
    private Button btn_cancle;



    int min;
    int max;

    GraphUtil GraphUtildata;


    public CurrentRangeDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GraphUtildata=GraphUtil.getInstance();


        min=GraphUtildata.getGraph_Current_MIN();
        max=GraphUtildata.getGraph_Current_MAX();
        WindowManager.LayoutParams layoutParams=new WindowManager.LayoutParams();
        layoutParams.flags=WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount=0.8f;
        getWindow().setAttributes(layoutParams);

        setContentView(R.layout.dialog_rang_change);

        tv_max=findViewById(R.id.max);
        tv_min=findViewById(R.id.min);

        tv_max.setText(getContext().getString(R.string.max,Integer.toString(GraphUtildata.getGraph_Current_MAX())));
        tv_min.setText(getContext().getString(R.string.min,Integer.toString(GraphUtildata.getGraph_Current_MIN())));

        btn_ok=findViewById(R.id.button_ok);
        btn_ok.setOnClickListener(view -> {
            btn_ok.setEnabled(false);
            btn_ok.postDelayed(() -> btn_ok.setEnabled(true), 1000);
            changeRange();
        });
        btn_cancle=findViewById(R.id.button_cancel);
        btn_cancle.setOnClickListener(view -> {
            btn_cancle.setEnabled(false);
            btn_cancle.postDelayed(() -> btn_cancle.setEnabled(true), 1000);
            this.dismiss();
        });

        et_min=findViewById(R.id.et_min);

        et_max=findViewById(R.id.et_max);

    }

    void changeRange() {

        try {
            min=Integer.parseInt(et_min.getText().toString());
            max=Integer.parseInt(et_max.getText().toString());

            if (max <= min) {
                Toast.makeText(getContext(), "숫자 범위를 잘못 지정 하셨습니다. 다시 설정해주세요", Toast.LENGTH_SHORT).show();
            } else {
                GraphUtildata.setGraph_Current_MAX(max);
                GraphUtildata.setGraph_Current_MIN(min);
                this.dismiss();
            }


        }catch (Exception e){
            Toast.makeText(getContext(), "숫자를 입력해주세요.", Toast.LENGTH_SHORT).show();
        }
    }

}
