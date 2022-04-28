package kr.co.uxn.agms.ui.connect;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import kr.co.uxn.agms.R;

public class BleListAdapter extends BaseAdapter {

    private  static final int TYPE_ITEM=1;
    private  static  final int TYPE_EMPTY=2;

    private  final List<AdapterItem> mDevices=new ArrayList<>();

    public void update(final AdapterItem result){
        final AdapterItem device=findDevice(result);
        if (device == null) {
            mDevices.add(new AdapterItem(result.getRssi(),result.getName(),result.getAddress()));
        } else {
            device.setRssi(result.getRssi());
        }
        notifyDataSetChanged();
    }
    private  AdapterItem findDevice(final AdapterItem result){
        for(final AdapterItem device:mDevices)
            if(device.matches(result.getAddress()))
                return device;
            return null;
    }

    void clearDevices() {
        mDevices.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mDevices.size();
    }

    @Override
    public Object getItem(int position) { return mDevices.get(position); }

    @Override
    public int getViewTypeCount() { return 2; }

    @Override
    public boolean areAllItemsEnabled() { return false; }

    @Override
    public boolean isEnabled(int position) { return getItemViewType(position)==TYPE_ITEM; }

    @Override
    public int getItemViewType(int position) {
        if(position==getCount()&&mDevices.isEmpty())
            return  TYPE_EMPTY;
        return  TYPE_ITEM;
    }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View oldView, ViewGroup parent) {
       final LayoutInflater inflater=LayoutInflater.from(parent.getContext());
       final int type=getItemViewType(position);
       View view=oldView;
       switch (type){
           case TYPE_EMPTY:
               if(view==null){
                   view=inflater.inflate(R.layout.layout_bt_empty,parent,false);
               }

               break;
           default:
               if(view==null){
                   view=inflater.inflate(R.layout.ble_item,parent,false);
                   final  ViewHolder holder=new ViewHolder();
                   holder.name=view.findViewById(R.id.tv_name);
                   holder.address=view.findViewById(R.id.tv_address);
                   holder.rssi=view.findViewById(R.id.img_rssi);
                   view.setTag(holder);
               }
               final AdapterItem device=(AdapterItem) getItem(position);
               final ViewHolder holder =(ViewHolder) view.getTag();

               final String name=device.getName();
               holder.name.setText(name !=null ? name: parent.getContext().getString(R.string.not_available));
               holder.address.setText(device.getAddress());
               final int rssiPercent=(int) (100.0f * (127.0f + device.getRssi()) / (127.0f + 20.0f));
               holder.rssi.setImageLevel(rssiPercent);
               holder.rssi.setVisibility(View.VISIBLE);

               break;
       }
       return view;
    }

    private class ViewHolder {
        private TextView name;
        private TextView address;
        private ImageView rssi;
    }

}
