package com.mariospatsis.unipismartalert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.sip.SipSession;
import android.util.Log;
import android.widget.Toast;

interface OnUsbServiseStatusListener
{
    public void onStatusChanged(boolean newStatus);
}
public class UsbService extends BroadcastReceiver {
    //H Κλαση USBService λαμβανει events οταν αλλαζει η κατασταση του USB
    String usbStateChangeAction = "android.hardware.usb.action.USB_STATE";
    public Boolean isConnected;
    private OnUsbServiseStatusListener listener;
    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("USB SERVICE");
        String action = intent.getAction();
        if(action.equalsIgnoreCase(usbStateChangeAction)) { //Check if change in USB state
            if(intent.getExtras().getBoolean("connected")) {
                // USB was connected
                System.out.println("connected");
                Toast.makeText(context.getApplicationContext(),"CONNECTED",Toast.LENGTH_SHORT).show();
                //isConnected = true;
                setConnected(true);
            } else {
                // USB was disconnected
                Toast.makeText(context.getApplicationContext(),"DISCONNECTED",Toast.LENGTH_SHORT).show();
                System.out.println("disconnected!!!");
                //isConnected = false;
                setConnected(false);
            }
        }
    }

    public Boolean getConnected() {
        return isConnected;
    }

    public void setConnected(Boolean connected) {
        isConnected = connected;

        if(listener !=null){
            listener.onStatusChanged(connected);
        }
    }

    public void setOnUsbServiceStatusListener(OnUsbServiseStatusListener listener){
        this.listener = listener;
    }


}
