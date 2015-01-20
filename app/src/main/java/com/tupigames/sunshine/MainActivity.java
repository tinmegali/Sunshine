package com.tupigames.sunshine;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends ActionBarActivity
    implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener

{

    private GoogleApiClient mGoogleApiClient;

    private static final int REQUEST_RESOLVE_ERROR = 1001;
    private static final String DIALOG_ERROR = "dialog_error";
    private boolean mResolvingError = false;
    private static final String STATE_RESOLVING_ERROR = "resolving_error";

    private Location mLastLocation;
    public static String latitude;
    public static String longitude;

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mResolvingError = savedInstanceState != null && savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);

        buildGoogleApiClient();

        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ForecastFragment(), "forecastFragment")
                    .commit();
        }


    }

    @Override
    protected void onStart()
    {
        super.onStart();
        if (!mResolvingError)
        {
            mGoogleApiClient.connect();
        }



    }

    @Override
    protected void onStop()
    {
        mGoogleApiClient.disconnect();
        super.onStop();


    }

   

    protected synchronized void buildGoogleApiClient()
    {
        mGoogleApiClient   = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        //Conectado aos servviços GooglePlay
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if ( mLastLocation != null )
        {
            Log.v("GoogleAPI", "Latitude => "+ String.valueOf(mLastLocation.getLatitude()));
            Log.v("GoogleAPI", "Longitude => " + String.valueOf(mLastLocation.getLongitude()));

            //TODO Passar resultados latitude e longitude para as preferências
            latitude = String.valueOf(mLastLocation.getLatitude());
            longitude =  String.valueOf(mLastLocation.getLongitude());

            String gpsLocation = getLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            Log.v("GeoCoder", "gpsLocation => " + gpsLocation); // OK


            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putString(SettingsActivity.PREF_KEY_GPSLOCAL, gpsLocation);
            //editor.putString(SettingsActivity.PREF_KEY_LOCALIZATION, gpsLocation);

            editor.commit();

        }
    }

    private String getLocation(double lat, double lon)
    {
        //TODO descobrir cidade usando GEocoder e coordenadas
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try
        {
            List<Address> addresses = geocoder.getFromLocation(lat, lon , 1);
            if ( addresses.size() > 0 )
            {
                String city     = addresses.get(0).getAddressLine(1);
                String country  = addresses.get(0).getAddressLine(2);

                Log.v("GeoCoder", "Ok - City => " + city  + " | Contry => " + country); // OK
                return (city + ", " + country);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    @Override
    public void onConnectionSuspended(int i) {
        //Conexão com Google Play interronpida
        //TODO implementar
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //Lidando com erros do GooglePlay

        if (mResolvingError)
        {
            return;
        } else if (connectionResult.hasResolution() )
        {
            try {
                mResolvingError = true;
                connectionResult.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                mGoogleApiClient.connect();
            }
        } else {
            //mostra diálogo
            showErrorDialog(connectionResult.getErrorCode());
            mResolvingError = true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            mResolvingError = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!mGoogleApiClient.isConnecting() &&
                        !mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
            }
        }
    }

    /* Creates a dialog for an error message */
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "errordialog");
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        mResolvingError = false;
    }

    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() { }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GooglePlayServicesUtil.getErrorDialog(errorCode,
                    this.getActivity(), REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((MainActivity)getActivity()).onDialogDismissed();
        }
    }

}
