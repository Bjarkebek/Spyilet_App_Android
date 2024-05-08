package bb.tec.spyiletapp;

import static android.app.PendingIntent.getActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.Manifest.*;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import bb.tec.spyiletapp.Model.ToiletLocation;


public class MainActivity extends AppCompatActivity {

    public static List<ToiletLocation> toilets;
    FusedLocationProviderClient flpc;
    Location location;
    TextView txt_long, txt_lat, txt_alt, txt_dir, viewDetect;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initGui();

        // to ask for permissions
        permissionsList = new ArrayList<>();
        permissionsList.addAll(Arrays.asList(permissionsStr));
        askForPermissions(permissionsList);


        // init the buttons
        toilets = new ArrayList<>();
        Button btn_reg = findViewById(R.id.btn_register);


    }

    private void initGui() {
        txt_lat = findViewById(R.id.txt_lat);
        txt_long = findViewById(R.id.txt_long);
        txt_alt = findViewById(R.id.txt_alt);
        txt_dir = findViewById(R.id.txt_dir);
        viewDetect = findViewById(R.id.viewDetect);

        findViewById(R.id.btn_register).setOnClickListener(v -> {
            if (location != null) {
                saveToiletLocation(new ToiletLocation(
                        location.getLatitude(),
                        location.getLongitude(),
                        location.getAltitude(),
                        location.getBearing()
                ));

//                new ToiletLocation().registerToiletLocation(location);
//                Toast.makeText(this, "Added toilet location", Toast.LENGTH_LONG).show();
            }
        });
    }


    // Method to save location to SharedPreferences
    public void saveToiletLocation(ToiletLocation location) {
        SharedPreferences sharedPreferences = this.getSharedPreferences("toilets", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Retrieve the existing list of ToiletLocations from SharedPreferences
        List<ToiletLocation> existingLocations = getToiletLocations();

        // Append the new location to the existing list
        existingLocations.add(location);

        // Convert list of locations to JSON string
        Gson gson = new Gson();
        String updatedLocationsJson = gson.toJson(existingLocations);

        // Save the location string
        editor.putString("toilet", updatedLocationsJson);
        editor.apply();
    }

    // Method to retrieve list of locations from SharedPreferences
    public List<ToiletLocation> getToiletLocations() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("toilets", Context.MODE_PRIVATE);

        // Retrieve the JSON string of locations from SharedPreferences
        String locationsJson = sharedPreferences.getString("toilet", null);

        // If locationsJson is not null, parse it and return the list of locations
        if (locationsJson != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<ToiletLocation>>() {
            }.getType();
            return gson.fromJson(locationsJson, type);
        } else {
            // If the data is null or empty, return an empty list
            return new ArrayList<>();
        }
    }


    private void checkToilets() {
        List<ToiletLocation> toiletLocation = getToiletLocations();
        if (toiletLocation != null) {
            for (ToiletLocation tl : toiletLocation) {
                if (tl.getLatitude() - 0.00002 <= location.getLatitude() && location.getLatitude() <= tl.getLatitude() + 0.00002
                        && tl.getLongitude() - 0.00002 <= location.getLongitude() && location.getLongitude() <= tl.getLongitude() + 0.00002
                ) {
                    viewDetect.setText("TOILET DETECTED");
                    viewDetect.setBackgroundResource(R.color.red);
                } else {
                    viewDetect.setText("");
                    viewDetect.setBackgroundResource(0);
                }
            }
        }
    }

    private void getUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this, permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted, handle it here or request permission again
            return;
        }

        flpc = LocationServices.getFusedLocationProviderClient(this);
        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 1000).build();

        flpc.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                location = locationResult.getLastLocation();

                txt_lat.setText("Latitude: " + location.getLatitude());
                txt_long.setText("Longitude: " + location.getLongitude());
                txt_alt.setText("Altitude: " + location.getAltitude());
                txt_dir.setText("Direction: " + location.getBearing());
                checkToilets();
            }
        }, Looper.myLooper());
    }


    //region Permission
    ArrayList<String> permissionsList;
    String[] permissionsStr = {
            permission.ACCESS_FINE_LOCATION,
            permission.ACCESS_COARSE_LOCATION,
            permission.CAMERA,
//            permission.ACCESS_BACKGROUND_LOCATION,
            permission.RECORD_AUDIO
    };
    int permissionsCount = 0;
    ActivityResultLauncher<String[]> permissionsLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onActivityResult(Map<String, Boolean> result) {
            ArrayList<Boolean> list = new ArrayList<>(result.values());
            permissionsList = new ArrayList<>();
            permissionsCount = 0;
            for (int i = 0; i < list.size(); i++) {
                if (shouldShowRequestPermissionRationale(permissionsStr[i])) {
                    permissionsList.add(permissionsStr[i]);
                } else if (!hasPermission(MainActivity.this, permissionsStr[i])) {
                    permissionsCount++;
                }
            }
            if (permissionsList.size() > 0) {
                //Some permissions are denied and can be asked again.
                askForPermissions(permissionsList);
            } else if (permissionsCount > 0) {
                //Show alert dialog
                showPermissionDialog();
            } else {
                //All permissions granted. Do your stuff 🤞
                getUpdates();
            }
        }
    });

    private boolean hasPermission(Context context, String permissionStr) {
        return ContextCompat.checkSelfPermission(context, permissionStr) == PackageManager.PERMISSION_GRANTED;
    }

    private void askForPermissions(ArrayList<String> permissionsList) {
        String[] newPermissionStr = new String[permissionsList.size()];
        for (int i = 0; i < newPermissionStr.length; i++) {
            newPermissionStr[i] = permissionsList.get(i);
        }
        if (newPermissionStr.length > 0) {
            permissionsLauncher.launch(newPermissionStr);
        } else {
        /* User has pressed 'Deny & Don't ask again' so we have to show the enable permissions dialog
        which will lead them to app details page to enable permissions from there. */
            showPermissionDialog();
        }
    }

    AlertDialog alertDialog;

    private void showPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permission required").setMessage("Some permissions are need to be allowed to use this app without any problems.").setPositiveButton("Continue", (dialog, which) -> {
            dialog.dismiss();
        });
        if (alertDialog == null) {
            alertDialog = builder.create();
            if (!alertDialog.isShowing()) {
                alertDialog.show();
            }
        }
    }
//endregion
}