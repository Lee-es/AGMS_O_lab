package kr.co.uxn.agms.ui.connect;


import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import kr.co.uxn.agms.CommonConstant;
import kr.co.uxn.agms.R;
import kr.co.uxn.agms.service.BluetoothService;
import kr.co.uxn.agms.ui.BleActivity;

public class ConnectActivity extends BleActivity {




    Button connect;

    private BleListAdapter mAdapter;
    private AdapterItem mdevice;
    private int mPrevPosition=-1;
    private ListView listView ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_connect);

        connect = findViewById(R.id.connect);
        connect.setEnabled(false);



        connect.setOnClickListener(view -> {
            connect.setEnabled(false);
            listView.setEnabled(false);
            if(mdevice!=null&&mdevice.getAddress()!=getTryToConnectAddress()){
                doDisConncet();
            }
             runDeviceConnect();
            mAdapter.clearDevices();
        });



        listView=findViewById(R.id.list);
        final LayoutInflater inflater = LayoutInflater.from(listView.getContext());
        listView.setEmptyView(inflater.inflate(R.layout.layout_bt_empty,listView,false));
        listView.setAdapter(mAdapter=new BleListAdapter());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                stopScan();
                if(position!=mPrevPosition){
                    connect.setEnabled(false);

                }
                connect.setEnabled(true);
                mPrevPosition=position;

                view.setSelected(true);
                 mdevice=(AdapterItem) mAdapter.getItem(position);
            }
        });

        TextView btn_scan=findViewById(R.id.btn_scan);
        btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScan();
            }
        });

        startScan();
    }
    private boolean runDeviceConnect(){
        if(TextUtils.isEmpty(mdevice.getAddress())){
            Toast.makeText(this,"Invalid address",Toast.LENGTH_SHORT).show();
            return false;
        }
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit().putLong(CommonConstant.PREF_WARM_UP_START_DATE,0)
                .putLong(CommonConstant.PREF_DEVICE_NEW_SENSOR_DATE,System.currentTimeMillis())
                .putLong(CommonConstant.PREF_DEVICE_FIRST_PAIRING_DATE, System.currentTimeMillis())
                .apply();
        boolean result = false;
        try {
            result = doDeviceClicked(true,mdevice.getAddress());

        }catch (Exception e){}
        if(result){

        }
        return result;
    }
    private void startScan(){
        doDisConncet();
        mPrevPosition=-1;
        connect.setEnabled(false);
        mAdapter.clearDevices();
        doScan();
        listView.setEnabled(true);
    }

    @Override
    public void doWhenConnectFail() {
        if(!isFinishing()){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    connect.setEnabled(true);
                }
            });
        }

    }

    @Override
    public void doWhenDeviceFound(Intent intent) {
        BluetoothDevice device = intent.getParcelableExtra(BluetoothService.EXTRA_DEVICE);


        if(device!=null){
            int rssi = intent.getIntExtra(BluetoothService.EXTRA_RSSI,0);
            Log.e("check","device:"+device.getName()+"/"+device.getAddress());
            AdapterItem item = new AdapterItem(rssi,device.getName(), device.getAddress());
            mAdapter.update(item);
        }
    }

    @Override
    public void doWhenDeviceConnected() {
        checkWarmup();
    }

    public void checkWarmup(){
        if(mBluetoothService!=null && mBluetoothService.isDeviceConnected()){

            StringBuilder sb= new StringBuilder();
            sb.append("디바이스: "+mdevice.getAddress());
            sb.append("\n");
            sb.append("\n");
            sb.append("연결이 되었습니다.");

            new AlertDialog.Builder(this)
                    .setTitle(R.string.confirm)
                    .setMessage(sb.toString())
                    .setNegativeButton(R.string.button_cancel,(dialogInterface, i) -> startScan())
                    .setPositiveButton(R.string.done,(dialogInterface,i)->doGoHome())
                    .show();
        } else {

            Toast.makeText(ConnectActivity.this,R.string.alert_message_device_connect_error,Toast.LENGTH_SHORT);
        }
    }

    @Override
    public void doWhenDeviceDisconnected() {
        checkWarmup();
    }

    private void doGoHome(){
        PreferenceManager.getDefaultSharedPreferences(ConnectActivity.this)
                .edit()
                .putLong(CommonConstant.PREF_DEVICE_NEW_SENSOR_DATE, System.currentTimeMillis())
                .putLong(CommonConstant.PREF_WARM_UP_START_DATE, System.currentTimeMillis())
                .apply();

        goHome();
    }


}