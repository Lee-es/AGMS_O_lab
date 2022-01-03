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

public class GraphCycleDialog extends Dialog
{
    private EditText et_time;
    private TextView tv_nowtime;
    private Button btn_ok;
    private Button btn_cancle;



    long time;

    GraphUtil GraphUtildata;

    public GraphCycleDialog(@NonNull Context context){super(context);}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GraphUtildata=GraphUtil.getInstance();


        time=GraphUtildata.getGraph_TargetTime();

        WindowManager.LayoutParams layoutParams=new WindowManager.LayoutParams();
        layoutParams.flags=WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount=0.8f;
        getWindow().setAttributes(layoutParams);

        setContentView(R.layout.dialog_time_change);

        tv_nowtime=findViewById(R.id.now_time);
        tv_nowtime.setText(getContext().getString(R.string.current_cycle_s,time/1000));

        btn_ok=findViewById(R.id.button_ok);
        btn_ok.setOnClickListener(view -> {
            btn_ok.setEnabled(false);
            btn_ok.postDelayed(() -> btn_ok.setEnabled(true), 1000);
            changeTime();
        });
        btn_cancle=findViewById(R.id.button_cancel);
        btn_cancle.setOnClickListener(view -> {
            btn_cancle.setEnabled(false);
            btn_cancle.postDelayed(() -> btn_cancle.setEnabled(true), 1000);
            this.dismiss();
        });

        et_time=findViewById(R.id.et_time);


    }

    void changeTime(){
            try{
                int changeTime=Integer.parseInt(et_time.getText().toString());
                GraphUtildata.setGraph_TargetTime(changeTime);
                    this.dismiss();
            }catch (Exception e){
                Toast.makeText(getContext(), "숫자를 입력해주세요.", Toast.LENGTH_SHORT).show();
            }

        }

    }

