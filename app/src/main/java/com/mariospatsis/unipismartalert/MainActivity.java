package com.mariospatsis.unipismartalert;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    static Activity mainActivity;
    UsbService mUsbService = new UsbService();
    FirebaseService mFirebaseService;
    String type;
    Button sosBtn;
    Button abortBtn;
    public TextView mainTitle;
    public TextView sosTitle;
    CountDownTimer countDownTimer;
    CountDownTimer countDownSOS;
    boolean countDownTimerIsRunning = false;
    boolean sosStatus = false;
    private FallDetectionHandler falldetection;
    private SeismicDetectionHandler seismicdetection;

    private final static int REQUESTCODE = 325;
    LocationManager mLocationManager;
    Uri notification;
    Ringtone r;
    private LocationListener locationService;
    private Boolean prevStatus;

    /* onCreate
    * - Αρχικοποιηση αντικειμενων UI της Αρχικης οθονης (Main Activity)
    * - Αρχικοποιηση επαφων για αποστολη με sms (οι οποιες αποθηκευονται σε sharedPreference)
    * - Δημιουργια FirebaseService οπου θα χρησιμοποιηθει το ιδιο instance στην εφαρμογη
    * - Δημιουργια device token συσκευης απο Firebase Messaging
    * - Location Manager και δημιουργια αντικειμενου LocationService για την χρηση τοποθεσιας
    * - Ελεγχος για permissions συσκευης
    * - Εγγραφη του UsbService για την αναγνωριση της καταστασης USB
    * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivity = this;
        mFirebaseService = FirebaseService.getInstance();
        mFirebaseService.getFCMToken(); // Γινεται generate FCM token απο την Firebase Messaging
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationService = new LocationService();

        sosBtn = findViewById(R.id.btn_sos);
        abortBtn = findViewById(R.id.btn_abort);
        mainTitle = findViewById(R.id.main_title);
        sosTitle = findViewById(R.id.sos_text);
        sosBtn.setOnClickListener(this);
        abortBtn.setOnClickListener(this);
        checkPermissions(); //ελεγχος για permission συσκευης
        try { //Δημιουργεια αντικειμενο Ringtone, επιλεγοντας το Notification ringtone της συσκευης
            notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*
         Κανουμε register to UsbService broadcast ωστε
         να λαμβανουμε τα events οταν υπαρχει αλλαγη στην κατασταση του USB.
         Σε καθε αλλαγη καταστασης επιστρεφεται απο την UsbService ενα boolean οπου
         -true ειναι οτι η συσκευη ειναι συνδεδεμενη οποτε η λειτουργια που θα ακολουθησει θα ειναι η earthquakeDetection (εντοπισμος δονησεων)
         -false η συσκευη δεν ειναι συνδεδεμενη οποτε η λειτουργια που θα ακολουθησει ειναι fallDetection (εντοπισμος πτωσεων χρηστη)
        */
        this.registerReceiver(mUsbService,new IntentFilter("android.hardware.usb.action.USB_STATE"));
        mUsbService.setOnUsbServiceStatusListener(new OnUsbServiseStatusListener() {
            @Override
            public void onStatusChanged(boolean newStatus) {
                if(newStatus){
                    //κατασταση εντοπισμος σεισμου
                    if(prevStatus == null || prevStatus != newStatus ) { //Ελεγχος της προηγουμενης καταστασης ετσι ωστε να μην γινει εγγραφη σε listener 2 φορες
                        prevStatus = newStatus;
                        type = "earthquakeDetection";
                        if (falldetection != null && FallDetectionHandler.getListenerStatus()) { // ελεγχος οτι αν υπαρχει ενεργος listener της αλλης λειτουργιας
                            falldetection.unregisterListener(); // αμα υπαρχει γινεται unregister του listener ωστε να ειναι ενεργος μονο ενας listener καθε φορα αναλογα την λειτουργια
                        }
                        mainTitle.setText(R.string.main_title2); //αλλαγη του τιτλου της οθονης αναλογα την λειουτγια που εκτελειται
                        setupEarthquakeDetection(); // Γινεται αρχικοποιηση της λειτουργιας EarthquakeDetection
                    }
                }else {
                    //κατασταση εντοπισμος πτωσεων χρηστη
                    if(prevStatus == null || prevStatus != newStatus ) {
                        prevStatus = newStatus;
                        type = "fallDetection";
                        if (seismicdetection != null && SeismicDetectionHandler.getListenerStatus()) {
                            System.out.println(seismicdetection);
                            seismicdetection.unregisterListener();
                        }
                        mainTitle.setText(R.string.main_title1);
                        setupFallDetection(); // Γινεται αρχικοποιηση της λειτουργιας FallDetection
                    }
                }
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() { // Κανουμε unregister τον broadcaster οταν φευγουμε απο το activity
        super.onDestroy();
    }

    /*
    * Click listener για τα κουμπια SOS και abort
    * */
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_sos: // κουμπι SOS
                sosStatus = true;
                sosTitle.setText(R.string.sos_title);
                countDownSOS =  new CountDownTimer(300000, 1000) { //timer στα 5 λεπτα ωστε να μπορει ο χρηστης να στειλει μηνυμα ακυρωσης μεσα σε αυτα τα λεπτα
                    public void onTick(long millisUntilFinished) {
                    }
                    public void onFinish() {
                        sosTitle.setText("");
                        sosStatus = false;
                    }

                }.start();
                handleEvent("SOS");
                break;
            case R.id.btn_abort: // κουμπι Abort
                if(type == "fallDetection" && countDownTimerIsRunning) { // σε περιπτωση που ο timer του fallDetection ειναι ενεργος τοτε το κουμπι abort ακυρωνει τον συναγερμο πτωσης
                    cancelTimer();
                    Toast.makeText(this, "Aborted", Toast.LENGTH_LONG).show();
                    mainTitle.setText(R.string.main_title1);
                    falldetection.registerListener();
                } else if(sosStatus){ // αμα το sosStatus ειναι ενεργο δηλαδη εχει πατηθει το SOS button και δεν εχουν περασει τα 5 λεπτα που εχει ο χρηστης για να κανει ακυρωση
                    cancelSOSTimer();
                    handleEvent("AbortSOS");
                }
                break;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.topbar, menu);
        return true;
    }

    /*
    * Clck listener απο το μενου της μπαρας
    *  */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_statistics:
                Intent goToStatistics = new Intent(this,Statistics.class);
                startActivity(goToStatistics);  // Νεο acitvity Statistics
                return true;
            case R.id.menu_contacts:
                Intent goToContacts = new Intent(this,ContactsActivity.class);
                startActivity(goToContacts);  // Νεο acitvity Contacts
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    /*
    *  Η μεθοδος checkPermission αμα υπαρχουν τα απαραιτητα permissions που χρειαζεται η εφαρμογη
    *  Η εφαρμογη χρειαζεται δυο permission για την τοποθεσια και για τη αποστολη SMS
    *  Αμα δεν υπαρχει το permission τοτε το ζηταει απο τον χρηστη οταν ανοιγει την εφαρμογη
    *  */
    private void checkPermissions() {
        List<String> PERMISSIONS = new ArrayList<>();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
                PERMISSIONS.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }else{
            System.out.println("GPS ENABLED");
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                    locationService);
        }
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS) !=
                PackageManager.PERMISSION_GRANTED) {
            PERMISSIONS.add(Manifest.permission.SEND_SMS);
        }

       if(!PERMISSIONS.isEmpty()){
           String[] array = PERMISSIONS.toArray(new String[PERMISSIONS.size()]);
           ActivityCompat.requestPermissions(this,
                   array,
                   REQUESTCODE);
       }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission)
                    == PackageManager.PERMISSION_GRANTED) {
                if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                            locationService);

                }

            }
        }
    }


    /* setupFallDetection
    *  Δημιουργια αντικειμενου και listener (επιταχυνσιόμετρο συσκευης) οταν ειναι σε κατασταση fallDetection
    *  Οταν το status ειναι true τοτε υπαρχει πτωση του χρηστη και γινεται απενεργοποιηση του listener
    *  και ενεργοποιηση ενος CountDownTimer που δινει στον χρηστη 30 δευτερολεπτα για να κανει ακυρωση του event
    *    */
    private void setupFallDetection() {
        falldetection = new FallDetectionHandler(this);
        falldetection.setFallDetectionListener(new FallDetectionListener() {
            @Override
            public void onStatusChanged(boolean fallDetectionStatus) {
                if(fallDetectionStatus) {
                    falldetection.unregisterListener();
                    countDownTimerIsRunning = true;
                    countDownTimer =  new CountDownTimer(30000, 1000) {

                        public void onTick(long millisUntilFinished) { // καθε δευτερολεπτο αλλαζουμε το UI για να εμφανιζεται η αντιστροφη μετρηση
                            r.play();
                            mainTitle.setText(Long.toString(millisUntilFinished / 1000));
                        }

                        public void onFinish() { // οταν τελειωσει ο timer ξανακανουμε register τον listener και γινεται διαχεριση του event
                            countDownTimerIsRunning = false;
                            r.stop();
                            mainTitle.setText(R.string.main_title1);
                            falldetection.registerListener();
                            handleEvent("fallDetection");
                        }

                    }.start();
                }
            }
        });
    }

    private void cancelTimer(){ //ακυρωση timer για το fall detection
        countDownTimer.cancel();
        r.stop();
    }

    private void cancelSOSTimer(){ //ακυρωση timer για το SOS button
        countDownSOS.onFinish();
        countDownSOS.cancel();
    }

    /* setupEarthquakeDetection
     *  Δημιουργια αντικειμενου και listener (επιταχυνσιόμετρο συσκευης) οταν ειναι σε κατασταση seismicDetection
     *  Οταν το status ειναι true τοτε υπαρχει εντοπισμος δονησης και γινεται απενεργοποιηση του listener
     *    */
    private void setupEarthquakeDetection() {
        seismicdetection = new SeismicDetectionHandler(this);
        seismicdetection.setSeismicDetectionListener(new SeismicDetectionListener() {
            @Override
            public void onStatusChanged(boolean seismicDetectionStatus) {
                if(seismicDetectionStatus) {
                    seismicdetection.unregisterListener(); // Κανουμε unregistrer τον listener μεχρι να γινει η καταγραφη στην βαση και να δουμε αν ειναι οντως σεισμος
                    handleEvent("earthquakeDetection"); //καταγραφουμε στην βαση με type earthquakeDetection ωστε να κανουμε αναζητηση και σε αλλους χρηστες με το ιδιο type
                }
            }
        });
    }

    /* handleEvent
    * Διαχειριση των events για εγγραφη στην βαση και αποστολη sms
    * Αναλογα το ειδος του event γινεται η καταλληλη ενεργεια */
    private void handleEvent( String type){
        String eventType = type;
        final double latd = LocationService.latitude;
        final double lond = LocationService.latitude;
        String lat = Double.toString(latd);
        String lon = Double.toString(lond);
        final long timestamp = System.currentTimeMillis();
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timestamp);
        String date = DateFormat.format("dd-MM-yyyy HH:mm", cal).toString();

        mFirebaseService.insertEvent(new EventModel(eventType, latd,lond,timestamp)); // Εγγραφη στην Firebase Database
        if((eventType != "earthquakeDetection") && (eventType != "earthquake")) { //Στελνουμε μηνυμα σε καθε περιπτωση εκτος απο την περιπτωση της ανιχνευσης σεισμου
            Notification notification = new Notification(mainActivity);
            notification.sendNotification(type, lat, lon, date); // αποστολη SMS
        }

        if(eventType == "earthquakeDetection"){ // Στην περιπτωση που εχουμε ανιχνευση σεισμου, γινεται ελεγχος της βασης για να βρεθει και αλλος χρηστης σε κοντινη αποσταση που ειχε ιδιο event
            mFirebaseService.getEvents();
            mFirebaseService.setFirebaseListener(new FirebaseListener() {
                @Override
                public void onStatusChanged(String newStatus) { // οταν η getEvents() ολοκληρωθει και εχει φερει ολα τα events τοτε το newStatus θα ειναι allEvents.
                    if(newStatus.equals("allEvents")){
                        List<EventModel> events = EventModel.filterEarthquakeDetectionEvents(mFirebaseService.eventsList); //φιλτρουμε απο ολα τα events μονο τα earthquakedetection
                        boolean seismicStatus = seismicdetection.seismicStatus(events, timestamp,latd,lond);
                        if(seismicStatus){
                            handleEvent("earthquake"); // εγγραφη του event στην βαση
                            new AlertDialog.Builder(MainActivity.mainActivity) // ειδοποιση χρηστη και και ενεργοποιηση του listener otan πατησει το οκ
                                    .setTitle("Earthquake")
                                    .setMessage("An Earthquake has been detected")
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            if( FallDetectionHandler.getListenerStatus() == null || FallDetectionHandler.getListenerStatus() ==false ){
                                                seismicdetection.registerListener();
                                            }
                                        }
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                        }else {
                            //αμα δεν υπαρχει αλλος κοντινος χρηστης τοτε δεν γινεται event earthquake
                            if(FallDetectionHandler.getListenerStatus() == null || FallDetectionHandler.getListenerStatus() ==false ){
                                seismicdetection.registerListener();
                            }

                        }
                    }
                }
            });
        }
    }
}
