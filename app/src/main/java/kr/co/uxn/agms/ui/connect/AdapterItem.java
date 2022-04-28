package kr.co.uxn.agms.ui.connect;

import androidx.annotation.Nullable;

public class AdapterItem {
   private final String address;
   private final String name;
   private  int rssi;

   public AdapterItem(int rssi, String name, String address) {
        this.name = name;
        this.address = address;
        this.rssi = rssi;
    }
    public boolean matches(final String address){
        return this.address.equals(address);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof AdapterItem){
            final AdapterItem data=(AdapterItem) obj;
            return this.address.equals(data.address);
        }
        return  super.equals(obj);
    }

    public String getAddress() {
        return this.address;
    }

    public int getRssi() {
        return this.rssi;
    }

    public String getName() {
        return this.name;
    }

    public void setRssi(int rssi) {
        this.rssi=rssi;
    }
}
