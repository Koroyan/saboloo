package com.example.chamegzavne.Activityes;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.chamegzavne.InfoClass.Post;
import com.example.chamegzavne.InfoClass.User;
import com.example.chamegzavne.R;
import com.example.chamegzavne.push_notification.MySingleton;
import com.facebook.login.LoginManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import android.support.v7.widget.Toolbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {
    private static final String TAG = "mylocation";
    //Intent Keys
    private static final String INTENT_INFO_KEY = "intent_info_key";
    private static final String INTENT_CHAT_KEY = "intent.chat.key";
    private static final String INTENT_NAV_BAR_KEY="intent.navbar.key";

    //push_Notification keys
    final private String FCM_API = "https://fcm.googleapis.com/fcm/send";
    final private String serverKey = "key=" + "AAAAflzg4vs:APA91bHpZOVe8GfSb-WoStbXaO3wbC_J8R8Ogumy2KBCt2flI1v8Xidpbg5ybg5wg6vHSPb3fiSB1h4QPhz8LGSyBSypTKF4vZn8fs5-UtfKDcU620B8V-m6yElCpPfPnbF06_WdCbDp";
    final private String contentType = "application/json";


    String NOTIFICATION_TITLE;
    String NOTIFICATION_MESSAGE;
    String TOPIC;

    //Firebase initialize.....
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRefUser = database.getReference("users/mylocation");
    DatabaseReference myPostRef = database.getReference("posts");
    DatabaseReference myMessagelistRef = database.getReference("messagelist");
    DatabaseReference myMessageRef = database.getReference("message");
    //Map for mapMarker
    private final Map<String, MarkerOptions> mMarkers = new ConcurrentHashMap<String, MarkerOptions>();
    private Map<String, Post> markerpost = new HashMap<>();

    //Location initialize.....
    private LocationManager locationManager;
    private static final int MINIMUM_TIME = 1000 * 0;
    private static final int MINIMUM_DISTANCE = 10;

    //myLocation
    public static Location mylocation;

    //USER
    private static List<User> users = new ArrayList<>();
    User user;
    public static String userName;
    public static String userID;
    public static Uri userProfilePhoto;
    // DatabaseReference myChatID=database.getReference(userID);
    //post
    Post post;
    private String pGel;
    private String pTitle;
    private String pComment;

    //Buttons
    FloatingActionButton add_marker;


    String key = null;

    FirebaseAuth firebaseAuth;
    private GoogleMap mMap;

    public Context mContext = (Context) getBaseContext();
    //
    public static boolean NOTIFICATION_SHOW_STATUS = true;


    BottomNavigationView bottomNavigationView;
    BottomNavigationView bottomNavigationViewListMap;
    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: skizb");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar=findViewById(R.id.main_toolbar_id);
        setSupportActionBar(toolbar);


        add_marker = findViewById(R.id.add_marker);
        firebaseAuth = FirebaseAuth.getInstance();
        add_marker.setOnClickListener(this);


        bottomNavigationViewListMap = findViewById(R.id.list_map_nav_bar);

        bottomNavigationViewListMap.getMenu().findItem(R.id.navigation_map).setChecked(true);
        bottomNavigationViewListMap.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Toast.makeText(MainActivity.this, "" + menuItem.getMenuInfo(), Toast.LENGTH_SHORT).show();
                switch (menuItem.getItemId()) {
                    case R.id.navigation_list_map:
                        Intent intent = new Intent(MainActivity.this, PostListActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);

                        return true;
                }
                return false;
            }
        });

        bottomNavigationView = findViewById(R.id.nav_bar);

        bottomNavigationView.getMenu().findItem(R.id.navigation_map).setChecked(true);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Toast.makeText(MainActivity.this, "" + menuItem.getMenuInfo(), Toast.LENGTH_SHORT).show();
                switch (menuItem.getItemId()) {
                    case R.id.navigation_messages:
                        Intent intent = new Intent(MainActivity.this, MessageListActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);

                        return true;
                }
                return false;
            }
        });

        locationInitializer();
        firebaseListener();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        Log.d(TAG, "onCreate: verj");


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_settings:
               Toast.makeText(this,item.getTitle().toString(),Toast.LENGTH_SHORT).show();
               return true;
            case R.id.menu_sign_out:
                Toast.makeText(this,item.getTitle().toString(),Toast.LENGTH_SHORT).show();
                LoginManager.getInstance().logOut();
                firebaseAuth.signOut();
                startActivity(new Intent(getBaseContext(), MainActivity.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: skizb");
        mMap = googleMap;


        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent intent = new Intent(MainActivity.this, DetailPostsActivity.class);
                ArrayList<String> puting = new ArrayList<>();
                Post post = markerpost.get(marker.getId());
                Log.d("mypost", "onInfoWindowClick: " + post.getpUserID() + " : " + userID);
                if (post.getpUserID().equals(userID)) {

                    Toast.makeText(MainActivity.this, "its your post", Toast.LENGTH_SHORT).show();
                }
                puting.add(userID.toString()); //[0] id for chat
                puting.add(userName);                             //[1]username for client
                puting.add(post.getpName());                      //[2]username server
                puting.add(post.getpTitle());                     //[3]title
                puting.add(post.getpComment());                   //[4]comment
                puting.add(post.getpGel());                       //[5]gel
                puting.add(post.getpUserID() + post.getpTitle().toString());             //[6]Post user ID for post
                puting.add(post.getpUserID());                                         //[7]post user id

                //intent.putExtra(INTENT_CHAT_KEY,userID+marker.getId().toString());
                intent.putStringArrayListExtra(INTENT_CHAT_KEY, puting);
                startActivity(intent);

            }
        });

        Log.d(TAG, "onMapReady: verj");
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_marker:
                Log.d(TAG, "onClick: button");
                if (mylocation == null) {
                    Toast.makeText(this, "GPS status is null", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(MainActivity.this, AddPostsActivity.class);

                startActivityForResult(intent, 100);
                return;



        }

        return;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("resultactivity", "onActivityResult: resultcode" + resultCode);
        if (RESULT_CANCELED == resultCode) {
            return;
        }
        if (requestCode == 100) {
            String[] result = data.getStringArrayExtra(INTENT_INFO_KEY);
            // ArrayList<String> result = data.getStringArrayListExtra(INTENT_INFO_KEY);
            // Log.d("putextra", "onActivityResult:" + result.get(0) + result.get(1) + result.get(2));
            Log.d("username", "onActivityResult: " + userName);
            post = new Post(userName, result[0], result[1], result[2],userProfilePhoto.toString(), userID, mylocation.getLatitude(), mylocation.getLongitude());
            myPostRef.child(userID + result[1]).setValue(post);

            TOPIC = "messages"; //topic has to match what the receiver subscribed to
            NOTIFICATION_TITLE = post.getpTitle();
            NOTIFICATION_MESSAGE = post.getpComment();

            JSONObject notification = new JSONObject();
            JSONObject notifcationBody = new JSONObject();
            try {
                notifcationBody.put("title", NOTIFICATION_TITLE);
                notifcationBody.put("message", NOTIFICATION_MESSAGE);
                notifcationBody.put("ID", userID);


                notification.put("to", FirebaseInstanceId.getInstance().getToken());
                notification.put("data", notifcationBody);

            } catch (JSONException e) {
                Log.e(TAG, "onCreate: " + e.getMessage());
            }
            sendNotification(notification);


        } else {

            Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
        }


    }


    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: skizb");
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            userProfilePhoto = currentUser.getProviderData().get(0).getPhotoUrl();
            userName = currentUser.getDisplayName();
            Log.d("username", "onStart: " + userName);
            userID = currentUser.getUid();
            if (userName == null) {
                finish();
            }
            Log.d("myfirebase", "USERID: " + userID);
            Log.d("myfirebase", "user name: " + currentUser.getDisplayName());
        }

        if (currentUser == null) {

            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        Log.d(TAG, "onStart: verj");
    }


    //locationManagerinit
    public void locationInitializer() {
        Log.d(TAG, "locationInitializer: skizb");
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Log.d(TAG, "locationInitializer: verj");
    }

    public void firebaseListener() {
        Log.d(TAG, "firebaseListener: skizb");

        myPostRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d("my", "onChildAdded: ");
                Post post = dataSnapshot.getValue(Post.class);

                assert post != null;
                if (post.getpUserID() == null) return;

                String key = add(post.getpName() + " : " + post.getpTitle(), new LatLng(post.getpLatitude(), post.getpLongitude()),Uri.parse(post.getpPhotoURL()));

                markerpost.put(key, post);
                if ((post.getpUserID().equals(userID))) {
                    //  showNotification(MainActivity.this,post.getpTitle(),post.getpComment(),new Intent(MainActivity.this,MainActivity.class   ));
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                remove(dataSnapshot.getValue(Post.class).getpName() + " : " +
                        dataSnapshot.getValue(Post.class).getpTitle());
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        Log.d(TAG, "firebaseListener: verj");

    }

    public void setMyLocation(Location location) {
        Log.d(TAG, "setMyLocation: skizb");
        mylocation = location;

        Log.d(TAG, "setMyLocation: verj");

    }


    //add Marker method
    private String add(final String name, final LatLng ll,Uri photoUri){
        final String[] markerID = new String[1];
        Log.d(TAG, "add: skizb");
        Log.d("photo", "add: " + userProfilePhoto.toString());




         Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                add_marker.setImageBitmap(getCircledBitmap(bitmap));
                final MarkerOptions marker = new MarkerOptions().position(ll).title(name).icon(BitmapDescriptorFactory.fromBitmap(getCircledBitmap(bitmap)));
                mMarkers.put(name, marker);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(ll));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        markerID[0] = mMap.addMarker(marker).getId();


                    }
                });
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {

            }


            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        };


        Picasso.get().load(photoUri).into(target);

        Log.d(TAG, "add: verj");
        return markerID[0];

    }


    //remove Marker method
    private void remove(String name) {
        Log.d(TAG, "remove: skizb");
        mMarkers.remove(name);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMap.clear();

                for (MarkerOptions item : mMarkers.values()) {
                    mMap.addMarker(item);
                }
            }
        });
        Log.d(TAG, "remove: verj");
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: skizb");
        super.onResume();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {


            userProfilePhoto = currentUser.getProviderData().get(0).getPhotoUrl();
            userName = currentUser.getDisplayName();
            if (userName == null) {
                finish();
            }
            Log.d("username", "onResume: " + userName);
            userID = currentUser.getUid();
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onResume: manifest error");
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            return;
        }

        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, MINIMUM_TIME, MINIMUM_DISTANCE,
                locationListener);

        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, MINIMUM_TIME, MINIMUM_DISTANCE,
                locationListener);
        Log.d(TAG, "onResume: verj");
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: skizb");
        super.onPause();
        locationManager.removeUpdates(locationListener);
        Log.d(TAG, "onPause: verj");
    }

    //LocationListener :)
    private LocationListener locationListener = new LocationListener() {


        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "onLocationChanged: skizb");
            if (location == null) {
                Toast.makeText(MainActivity.this, "location is null", Toast.LENGTH_SHORT).show();
                return;
            }
            setMyLocation(location);
            Log.d(TAG, "onLocationChanged: verj");
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d(TAG, "onProviderDisabled: ");
            Toast.makeText(MainActivity.this, "provider disabled", Toast.LENGTH_SHORT).show();
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

                return;
            }
            setMyLocation(locationManager.getLastKnownLocation(provider));
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d(TAG, "onProviderEnabled: skizb");
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                return;
            }
            if (locationManager.getLastKnownLocation(provider) == null) {
                return;
            }
            setMyLocation(locationManager.getLastKnownLocation(provider));

            Log.d(TAG, "onProviderEnabled: verj");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d(TAG, "onStatusChanged: ");

            Toast.makeText(MainActivity.this, "StatusChanged:" + status, Toast.LENGTH_SHORT).show();

        }
    };


    //show notification method
    public void showNotification(Context context, String title, String body, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationId = (int) System.currentTimeMillis();
        String channelId = "channel-01";
        String channelName = "Channel Name";
        int importance = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            importance = NotificationManager.IMPORTANCE_HIGH;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true);

        TaskStackBuilder stackBuilder = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            stackBuilder = TaskStackBuilder.create(context);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            stackBuilder.addNextIntent(intent);
        }
        PendingIntent resultPendingIntent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            resultPendingIntent = stackBuilder.getPendingIntent(
                    0,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );
        }
        mBuilder.setContentIntent(resultPendingIntent);

        notificationManager.notify(notificationId, mBuilder.build());
    }

    //send push-Notification method
    private void sendNotification(JSONObject notification) {


        // RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, FCM_API, notification,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {


                        Log.i(TAG, "onResponse: " + response);


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Request error", Toast.LENGTH_LONG).show();
                        Log.i(TAG, "onErrorResponse: Didn't work");
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", serverKey);
                params.put("Content-Type", contentType);
                return params;
            }

        };
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
        //requestQueue.add(jsonObjectRequest);

    }

       //getCircleBitmap
    public static Bitmap getCircledBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(bitmap.getWidth()/2f, bitmap.getHeight()/2f, bitmap.getWidth()/2f, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

}