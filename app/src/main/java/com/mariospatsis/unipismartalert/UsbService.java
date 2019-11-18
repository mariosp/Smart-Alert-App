package com.mariospatsis.unipismartalert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.sip.SipSession;
import android.util.Log;
import android.widget.Toast;

/*
 * Interface  OnUsbServiseStatusListener
 * με μεθοδο onStatusChanged
 * Γινεται override η μεθοδος στην main Activity και χρησιμοποιειται σαν listener οταν υπαρχει αλλαγη καταστασης USB
 * */
interface OnUsbServiseStatusListener
{
    public void onStatusChanged(boolean newStatus);
}

/*
    H Κλαση USBService λαμβανει events οταν αλλαζει η κατασταση του USB
 */
public class UsbService extends BroadcastReceiver {
    String usbStateChangeAction = "android.hardware.usb.action.USB_STATE";
    public Boolean isConnected;
    private OnUsbServiseStatusListener listener;

    /*
    *  onReceive BroadcastReceiver @override
    *  Επιστρεφει αμα η συσκευη ειναι συνδεδεμενη με Usb καλωδιο
    *  */
    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("USB SERVICE");
        String action = intent.getAction();
        if(action.equalsIgnoreCase(usbStateChangeAction)) {
            if(intent.getExtras().getBoolean("connected")) {
                // USB was connected
                System.out.println("USB connected");
                Toast.makeText(context.getApplicationContext(),"USB CONNECTED",Toast.LENGTH_SHORT).show();
                setConnected(true);
            } else {
                // USB was disconnected
                Toast.makeText(context.getApplicationContext(),"USB DISCONNECTED",Toast.LENGTH_SHORT).show();
                System.out.println("USB disconnected!!!");
                setConnected(false);
            }
        }
    }

    public Boolean getConnected() {
        return isConnected;
    }

    /* setConnected
     * καλει την μεθοδο onStatusChanged που εχει υλοποιηθει στην MainActivity
     */
    public void setConnected(Boolean connected) {
        isConnected = connected;

        if(listener !=null){
            listener.onStatusChanged(connected);
        }
    }

    /* setOnUsbServiceStatusListener
     * Αποθηκευση του OnUsbServiseStatusListener instance  (MainActivity) στα properties του αντικειμενου
     * για να γινει χρηστη του απο την setConnected
     */
    public void setOnUsbServiceStatusListener(OnUsbServiseStatusListener listener){
        this.listener = listener;
    }


}
