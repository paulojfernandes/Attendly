package pt.attendly.attendly;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;


import pt.attendly.attendly.firebase.manageData;
import pt.attendly.attendly.model.Card;
import pt.attendly.attendly.model.Classroom;
import pt.attendly.attendly.model.Schedule;
import pt.attendly.attendly.model.Subject;
import pt.attendly.attendly.model.User;

import pt.attendly.attendly.model.Log;

public class MainActivity extends AppCompatActivity implements BeaconConsumer {

    protected static final String TAG = "MonitoringActivity";
    protected static final String TAG2 = "MonitoringActivity2";
    private BeaconManager beaconManager;
    private String BluetoothMacAddressUser, beaconSala;
    private boolean presença, tempo, classOpen;
    static boolean read = false;

    static ArrayList<Card> cards = new ArrayList<>();
    static TextView aula;
    TextView sala;
    CardView cardView;

    static boolean subjectExists = false, noClass = false;

    public static String userId = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        aula = findViewById(R.id.txtClass);
        cardView = findViewById(R.id.cardView);

        manageData.getMainActivityData(userId);
//        card();

        beaconManager = BeaconManager.getInstanceForApplication(this);
        // To detect proprietary beacons, you must add a line like below corresponding to your beacon
        // type.  Do a web search for "setBeaconLayout" to get the proper expression.
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);


    }



//    private void card() {
//        currentCard("3SGi1vnVujY7y4xsHc07JmBhS9U2", new OnGetDataListener() {
//
//
//            @Override
//            public void onStart() {
//                android.util.Log.d("aca1", "aca");
//                cardView.setVisibility(View.INVISIBLE);
//            }
//
//            @Override
//            public void onSuccess(DataSnapshot data) {
//                android.util.Log.d("aca", "aca");
//                aula.setText(cards.get(0).getSubjectName());
//                cardView.setVisibility(View.VISIBLE);
//            }
//
//            @Override
//            public void onFailed(DatabaseError databaseError) {
//
//            }
//        });
//    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    @Override
    public void onBeaconServiceConnect() {

        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                android.util.Log.d(TAG, "BEACONS" + beacons);
                if (beacons.size() > 0) {

                    if (cards.size() > 0) {
                        android.util.Log.d("card", String.valueOf(cards.get(0).getSubjectName()));
                    }
                    for (Beacon beacon : beacons) {
                        // Obter Beacons que estão a menos de 2.5m
                        if (beacon.getDistance() <= 2.5) {
                            // Obter o macaddress do beacon ; verificar qual é o mac address da sala da proxima aula ; comparar os dos mac address
                            // verificar se o beacon detetado corresponde com o da sala
                            if (beacons.iterator().next().getBluetoothAddress() == beaconSala) {
                                // verificar se prof abriu a aula

//                                Log l= new Log(String id, String id_user, String id_bluetooth, int id_subject, String date, int day_week, int presence, int id_classroom);

                                //manageData.write("Log", );
//                                if(classOpen== true){
//
//
//
//                                    // se coincidirem  verificar se já tiver falta não pode marcar presença
//                                    if(presença== false){
//
//                                        // caso não tenha falta verificar se está dentro do tempo para marcar presença, caso contrário não faz nada
//                                        if (tempo   == true ){
//
//                                            // registar na base de dados a presença
//                                            // alterar a card principal
//                                        }
//                                    }
//                                }
                            }
                            android.util.Log.i(TAG2, "The first beacon I see is about " + beacons.iterator().next().getDistance() + " meters away.");
                            android.util.Log.i(TAG, "The beacon " + beacons.iterator().next().getBluetoothAddress() + " bluetooth");
                        }
                    }
                    android.util.Log.i(TAG, "BT Device" + getBluetoothMacAddress());
                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
        }
    }


    public void enterLog(View view) {
        Intent intent = new Intent(this, MainTeacherActivity.class);
        intent.putExtra("teste", "teste");
        startActivity(intent);
    }

    public void openHistoryActivity(View view) {
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
    }

    public void openProfile(View view)
    {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    private String getBluetoothMacAddress() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        String bluetoothMacAddress = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            try {
                Field mServiceField = bluetoothAdapter.getClass().getDeclaredField("mService");
                mServiceField.setAccessible(true);

                Object btManagerService = mServiceField.get(bluetoothAdapter);

                if (btManagerService != null) {
                    bluetoothMacAddress = (String) btManagerService.getClass().getMethod("getAddress").invoke(btManagerService);
                }
            } catch (NoSuchFieldException e) {

            } catch (NoSuchMethodException e) {

            } catch (IllegalAccessException e) {

            } catch (InvocationTargetException e) {

            }
        } else {
            bluetoothMacAddress = bluetoothAdapter.getAddress();
        }
        return bluetoothMacAddress;
    }

    public static void currentCard(User currentUser) {
        final ArrayList<User> users = manageData.users;
        final ArrayList<Schedule> schedules = manageData.schedules;
        final ArrayList<Subject> subjects = manageData.subjects;
        final ArrayList<Classroom> classrooms = manageData.classrooms;

        // Get the current date (day, hour and minute)
        Date date = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(date);

        int day = c.get(Calendar.DAY_OF_WEEK);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);


        // Get the schedules of the current user
        ArrayList<Integer> userSubjects = currentUser.getSubjects();
        ArrayList<Integer> userSchedulesID = new ArrayList<>();

        for (int i = 0; i < subjects.size(); i++) {
            Subject tempSubject = subjects.get(i);
            int tempSubjectID = subjects.get(i).getId();

            for (int j = 0; j < userSubjects.size(); j++) {
                int tempUserSubjectID = userSubjects.get(j);

                if (tempSubjectID == tempUserSubjectID) {
                    for (int k = 0; k < tempSubject.getSchedules().size(); k++) {
                        int tempScheduleID = tempSubject.getSchedules().get(k);
                        userSchedulesID.add(tempScheduleID);
                    }
                }
            }
        }

        ArrayList<Schedule> userSchedules = new ArrayList<>();
        for (int i = 0; i < userSchedulesID.size(); i++) {

            for (int j = 0; j < schedules.size(); j++) {
                if (schedules.get(j).getId() == userSchedulesID.get(i)) {
                    userSchedules.add(schedules.get(j));

                }
            }
        }


        String subjectBeginning = "";
        String subjectEnding = "";
        String subjectClassroom = "";
        String subjectName = "";
        String subjectBeacon = "";
        String subjectCourse = "";


        int totalClasses = 0, pastClasses = 0;
        noClass = false;


        for (int i = 0; i < userSchedules.size(); i++) {

            // NESTE DIA TEM AULAS
            android.util.Log.d("day", String.valueOf(userSchedules.size()));
            if (day == userSchedules.get(i).getDay_week()) {
                totalClasses++;
                Date currentDate = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");

                subjectBeginning = userSchedules.get(i).getBeginning();
                subjectEnding = userSchedules.get(i).getEnding();

                int tempClassroomID = userSchedules.get(i).getClassroom();

                for (int j = 0; j < classrooms.size(); j++) {
                    if (classrooms.get(j).getId() == tempClassroomID) {
                        subjectClassroom = classrooms.get(j).getName();
                        subjectBeacon = classrooms.get(j).getId_beacon();
                    }
                }

                int tempScheduleID = userSchedules.get(i).getId();

                for (int j = 0; j < subjects.size(); j++) {
                    //android.util.Log.d("XPTO", String.valueOf(subjects.size()));

                    for (int k = 0; k < subjects.get(j).getSchedules().size(); k++) {
                        int tempID = subjects.get(j).getSchedules().get(k);

                        if (tempID == tempScheduleID) {
                            subjectName = subjects.get(j).getName();
                            subjectCourse = subjects.get(j).getCourse();
                        }
                    }
                }

                Date subjectDate = new Date();
                String[] tempArray = subjectEnding.split(":");
                subjectDate.setHours(Integer.parseInt(tempArray[0]));
                subjectDate.setMinutes(Integer.parseInt(tempArray[1]));
                subjectDate.setSeconds(0);


                if (subjectDate.before(currentDate)) {
                    pastClasses++;
                }

//               VERIFICAR HORA DA AULA - SE A AULA NÃO TIVER PASSADO (HORA FINAL DA AULA)
                if (subjectDate.after(currentDate)) {

                    subjectExists = true;
                    android.util.Log.d("XPTO", subjectBeginning);
                    android.util.Log.d("XPTO", subjectEnding);
                    android.util.Log.d("XPTO", subjectClassroom);
                    android.util.Log.d("XPTO", subjectName);
                    android.util.Log.d("XPTO", subjectBeacon);
                    android.util.Log.d("XPTO", subjectCourse);
                    break;
                }
            }
        }
        if (totalClasses == pastClasses) {
            android.util.Log.d("count", String.valueOf(pastClasses));
            android.util.Log.d("count1", String.valueOf(totalClasses));
            android.util.Log.d("count", "No more classes");
        }
        read = true;
        android.util.Log.d("card2", String.valueOf(read));
        cards.clear();
        Card card = new Card(subjectBeginning, subjectEnding, subjectClassroom, subjectName, subjectBeacon, subjectCourse);
        cards.add(card);
        aula.setText(cards.get(0).getSubjectName());

    }

//    public static void currentCard(String ID, final OnGetDataListener listener) {
//        listener.onStart();
//        final String userID = ID;
//        final ArrayList<User> users = new ArrayList<>();
//        final ArrayList<Schedule> schedules = new ArrayList<>();
//        final ArrayList<Subject> subjects = new ArrayList<>();
//        final ArrayList<Classroom> classrooms = new ArrayList<>();
//
//        //GET USERS
//        DatabaseReference User_ref = FirebaseDatabase.getInstance().getReference("User");
//        User_ref.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
//                for (DataSnapshot child : children) {
//                    User user = child.getValue(User.class);
//                    users.add(user);
//                }
//                //GET SCHEDULES
//                DatabaseReference Schedule_ref = FirebaseDatabase.getInstance().getReference("Schedule");
//                Schedule_ref.addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        Iterable<DataSnapshot> children = dataSnapshot.getChildren();
//                        schedules.clear();
//                        for (DataSnapshot child : children) {
//                            Schedule schedule = child.getValue(Schedule.class);
//                            schedules.add(schedule);
//                        }
//                        //GET SUBJECTS
//                        DatabaseReference Subject_ref = FirebaseDatabase.getInstance().getReference("Subject");
//                        Subject_ref.addValueEventListener(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(DataSnapshot dataSnapshot) {
//                                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
//                                subjects.clear();
//                                for (DataSnapshot child : children) {
//                                    Subject subject = child.getValue(Subject.class);
//                                    subjects.add(subject);
//                                }
//                                //GET CLASSROOMS
//                                DatabaseReference Classroom_ref = FirebaseDatabase.getInstance().getReference("Classroom");
//                                Classroom_ref.addValueEventListener(new ValueEventListener() {
//                                    @Override
//                                    public void onDataChange(DataSnapshot dataSnapshot) {
//                                        Iterable<DataSnapshot> children = dataSnapshot.getChildren();
//                                        classrooms.clear();
//                                        for (DataSnapshot child : children) {
//                                            Classroom classroom = child.getValue(Classroom.class);
//                                            classrooms.add(classroom);
//                                        }
//
//
//                                        // Get the current user
//                                        User currentUser = new User();
//                                        for (int i = 0; i < users.size(); i++) {
//                                            String tempUserID = users.get(i).getId();
//                                            if (userID.equals(tempUserID)) {
//                                                currentUser = users.get(i);
//                                            }
//                                        }
//
//                                        // Get the current date (day, hour and minute)
//                                        Date date = new Date();
//                                        Calendar c = Calendar.getInstance();
//                                        c.setTime(date);
//
//                                        int day = c.get(Calendar.DAY_OF_WEEK);
//                                        int hour = c.get(Calendar.HOUR_OF_DAY);
//                                        int minute = c.get(Calendar.MINUTE);
//
//
//                                        // Get the schedules of the current user
//                                        ArrayList<Integer> userSubjects = currentUser.getSubjects();
//                                        ArrayList<Integer> userSchedulesID = new ArrayList<>();
//
//                                        for (int i = 0; i < subjects.size(); i++) {
//                                            Subject tempSubject = subjects.get(i);
//                                            int tempSubjectID = subjects.get(i).getId();
//
//                                            for (int j = 0; j < userSubjects.size(); j++) {
//                                                int tempUserSubjectID = userSubjects.get(j);
//
//                                                if (tempSubjectID == tempUserSubjectID) {
//                                                    for (int k = 0; k < tempSubject.getSchedules().size(); k++) {
//                                                        int tempScheduleID = tempSubject.getSchedules().get(k);
//                                                        userSchedulesID.add(tempScheduleID);
//                                                    }
//                                                }
//                                            }
//                                        }
//
//                                        ArrayList<Schedule> userSchedules = new ArrayList<>();
//                                        for (int i = 0; i < userSchedulesID.size(); i++) {
//
//                                            for (int j = 0; j < schedules.size(); j++) {
//                                                if (schedules.get(j).getId() == userSchedulesID.get(i)) {
//                                                    userSchedules.add(schedules.get(j));
//
//                                                }
//                                            }
//                                        }
//
//
//                                        String subjectBeginning = "";
//                                        String subjectEnding = "";
//                                        String subjectClassroom = "";
//                                        String subjectName = "";
//                                        String subjectBeacon = "";
//                                        String subjectCourse = "";
//
//
//                                        int totalClasses = 0, pastClasses = 0;
//                                        noClass = false;
//
//
//                                        for (int i = 0; i < userSchedules.size(); i++) {
//
//                                            // NESTE DIA TEM AULAS
//                                            android.util.Log.d("day", String.valueOf(userSchedules.size()));
//                                            if (day == userSchedules.get(i).getDay_week()) {
//                                                totalClasses++;
//                                                Date currentDate = new Date();
//                                                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
//
//                                                subjectBeginning = userSchedules.get(i).getBeginning();
//                                                subjectEnding = userSchedules.get(i).getEnding();
//
//                                                int tempClassroomID = userSchedules.get(i).getClassroom();
//
//                                                for (int j = 0; j < classrooms.size(); j++) {
//                                                    if (classrooms.get(j).getId() == tempClassroomID) {
//                                                        subjectClassroom = classrooms.get(j).getName();
//                                                        subjectBeacon = classrooms.get(j).getId_beacon();
//                                                    }
//                                                }
//
//                                                int tempScheduleID = userSchedules.get(i).getId();
//
//                                                for (int j = 0; j < subjects.size(); j++) {
//                                                    //android.util.Log.d("XPTO", String.valueOf(subjects.size()));
//
//                                                    for (int k = 0; k < subjects.get(j).getSchedules().size(); k++) {
//                                                        int tempID = subjects.get(j).getSchedules().get(k);
//
//                                                        if (tempID == tempScheduleID) {
//                                                            subjectName = subjects.get(j).getName();
//                                                            subjectCourse = subjects.get(j).getCourse();
//                                                        }
//                                                    }
//                                                }
//
//                                                Date subjectDate = new Date();
//                                                String[] tempArray = subjectEnding.split(":");
//                                                subjectDate.setHours(Integer.parseInt(tempArray[0]));
//                                                subjectDate.setMinutes(Integer.parseInt(tempArray[1]));
//                                                subjectDate.setSeconds(0);
//
//
//                                                if (subjectDate.before(currentDate)) {
//                                                    pastClasses++;
//                                                }
//
////                                              VERIFICAR HORA DA AULA - SE A AULA NÃO TIVER PASSADO (HORA FINAL DA AULA)
//                                                if (subjectDate.after(currentDate)) {
//
//                                                    subjectExists = true;
//                                                    android.util.Log.d("XPTO", subjectBeginning);
//                                                    android.util.Log.d("XPTO", subjectEnding);
//                                                    android.util.Log.d("XPTO", subjectClassroom);
//                                                    android.util.Log.d("XPTO", subjectName);
//                                                    android.util.Log.d("XPTO", subjectBeacon);
//                                                    android.util.Log.d("XPTO", subjectCourse);
//                                                    break;
//                                                }
//                                            }
//                                        }
//                                        if (totalClasses == pastClasses) {
//                                            android.util.Log.d("count", String.valueOf(pastClasses));
//                                            android.util.Log.d("count1", String.valueOf(totalClasses));
//                                            android.util.Log.d("count", "No more classes");
////                                                    if (subjectExists == true) {
////                                                        break;
////                                                    } else {
////
////                                                        i = 0;
////                                                        totalClasses = 0;
////                                                        pastClasses = 0;
////                                                        day++;
////                                                        if (day > 6) {
////                                                            day = 0;
//////
////                                                        }
////                                                    }
//                                        }
////        android.util.Log.d("XPTO", String.valueOf(subjectExists));
//                                        // ArrayList<card> cards = new ArrayList<>();
//                                        read = true;
//                                        android.util.Log.d("card2", String.valueOf(read));
//                                        cards.clear();
//                                        card card = new card(subjectBeginning, subjectEnding, subjectClassroom, subjectName, subjectBeacon, subjectCourse);
//                                        cards.add(card);
//                                        //android.util.Log.d("card",Integer.toString(cards.size()));
//                                        //for (int l = 0; l < cards.size(); l++) {
//                                        //android.util.Log.d("card",cards.get(l).getSubjectName());
//                                        //}
//
////                                        return cards;
//                                        listener.onSuccess(dataSnapshot);
//
//                                    }
//
//                                    @Override
//                                    public void onCancelled(DatabaseError databaseError) {
//                                        listener.onFailed(databaseError);
//                                    }
//                                });
//
//                            }
//
//                            @Override
//                            public void onCancelled(DatabaseError databaseError) {
//
//                            }
//                        });
//
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//
//
//        });
//
//
////        // Hardcode models (replace by firebase data)
////        ArrayList<User> users = new ArrayList<>();
////        int[] exampleSubjects = {0};
////        User u1 = new User("3SGi1vnVujY7y4xsHc07JmBhS9U2", 0, "João", "url", "TSIW", exampleSubjects);
////        User u2 = new User("7ygXxTdPxpNlAiuUE1Dce7naFet1", 0, "Paulo", "url", "TSIW", exampleSubjects);
////        User u3 = new User("BsT8jtyt7HWwtDu6Jq2xcvJZvW02", 0, "Daniel", "url", "TSIW", exampleSubjects);
////        users.add(u1);
////        users.add(u2);
////        users.add(u3);
////        ArrayList<Schedule> schedules = new ArrayList<>();
////        Schedule s1 = new Schedule(0, "11:00", "13:00", 3, 0);
////        Schedule s2 = new Schedule(1, "11:00", "13:00", 4, 0);
////        Schedule s3 = new Schedule(2, "14:00", "18:00", 4, 0);
////        schedules.add(s1);
////        schedules.add(s2);
////        schedules.add(s3);
////        int[] exampleSchedules = {0, 1, 2};
////        int[] exampleTeachers = {3};
////        ArrayList<Subject> subjects = new ArrayList<>();
////        Subject sb1 = new Subject(0, "PDM", "TSIW", exampleSchedules , exampleTeachers);
////        subjects.add(sb1);
////
////        ArrayList<Classroom> classrooms = new ArrayList<>();
////        Classroom c1 = new Classroom(0, "B206", "IDBEACONXPTO");
////        classrooms.add(c1);
////
////
//        // Get the current user
//
//
//    }


}

