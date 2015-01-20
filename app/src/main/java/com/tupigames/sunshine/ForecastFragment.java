package com.tupigames.sunshine;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ForecastFragment extends Fragment {

    ListView    listView;
    ArrayAdapter<String> arrayAdapter;

    SharedPreferences sharedPreferences;
    String unitSelected;

    public ForecastFragment() {
    }

    @Override
    public void onStart()
    {
        super.onStart();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //TODO receber mensagem enviada via bundle de Activity

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        refresh();

        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        String forecast[] = {
                "Buscando informações"};

        List<String> weekForecast = new ArrayList<String>(Arrays.asList(forecast));

        //buscando dados do servidor

        arrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, weekForecast);


        listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = ((TextView)view).getText().toString();

                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, item);
                startActivity(intent);
            }
        });
        refresh();

       //FetchWeatherTask fetchWeatherTask = new FetchWeatherTask().execute( );

        return rootView;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch ( item.getItemId() )
        {
            case R.id.action_refresh:
                refresh();
                return true;

            case R.id.action_showmap:
                Log.v("ForecastFragment", "ShowMap - " + sharedPreferences.getString(SettingsActivity.PREF_KEY_GPSLOCAL, "") );
                String myLocationStr = sharedPreferences.getString(SettingsActivity.PREF_KEY_GPSLOCAL, "");

                String myLocationEncoded;
                try {
                    myLocationEncoded = URLEncoder.encode(myLocationStr, "utf-8");
                    showMap( Uri.parse("geo:0,0?q="+myLocationEncoded) );
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    public void showMap(Uri geoLocation)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);

        intent.setData(geoLocation);
        if ( intent.resolveActivity( getActivity().getPackageManager()) != null )
        {
            startActivity(intent);
        }

    }

    public void refresh()
    {

        readPreferences();



        String localizationByGPS   = sharedPreferences.getString(SettingsActivity.PREF_KEY_GPSLOCAL, "");
        Log.v("ForecastFragment", "localizationByGPS=> "+ localizationByGPS);

        String localizationSelected= sharedPreferences.getString(SettingsActivity.PREF_KEY_LOCALIZATION, "");
        Log.v("ForecastFragment", "localizationSelected=> "+ localizationSelected);


        //TODO criar possibilidade do usuário incluir outras cidades
        //TODO considerar alteração no campo localização para fazer busca
        //TODO iniciar sempre o campo busca com a localização definida pelo GPS

        unitSelected        = sharedPreferences.getString(SettingsActivity.PREF_KEY_UNITS, "metric");
        boolean useGPS      = sharedPreferences.getBoolean(SettingsActivity.PREF_KEY_USEGPS, true);

        String localization;
        if ( useGPS )
        {
            localization = localizationByGPS;
        } else {
            localization = localizationSelected;
        }

        //String myQueryURL = queryURL(localization, "json", units, "7");
        String myQueryURL = queryURL(localization, "json", "metric", "7");

        new FetchWeatherTask().execute(myQueryURL);

    }



    private void readPreferences()
    {

    }

    private void makeToast ( String message )
    {
        Context context     = getActivity().getApplicationContext();
        CharSequence msg    = message;
        int duration        = Toast.LENGTH_SHORT;
        Toast toast         = Toast.makeText(context, msg, duration);
        toast.show();
    }

    private String queryURL( String postalCode, String mode, String units, String cnt )
    {
        Uri.Builder builder = new Uri.Builder();

            builder.scheme("http")
                    .authority("api.openweathermap.org")
                    .appendPath("data")
                    .appendPath("2.5")
                    .appendPath("forecast")
                    .appendPath("daily")
                    .appendQueryParameter("q", postalCode)
                    .appendQueryParameter("mode", mode)
                    .appendQueryParameter("units", units)
                    .appendQueryParameter("cnt", cnt);


        return builder.build().toString();


    }

    private String[] getWeathrFromJson(String forecastJsonStr)
            throws JSONException
    {
        final String OWM_LIST       = "list";
        final String OWM_WEATHER    = "weather";
        final String OWM_TEMPERATURE= "temp";
        final String OWM_MAX        = "max";
        final String OWM_MIN        = "min";
        final String OWM_DATETIME   = "dt";
        final String OWM_DESCRIPTION= "main";

        JSONObject forecastJson     = new JSONObject(forecastJsonStr);
        JSONArray weatherArray      = forecastJson.getJSONArray(OWM_LIST);

        int numDays         = weatherArray.length();
        String[] resultStr  = new String[numDays];

        for ( int i=0; i < numDays; i++ )
        {
            String date;
            String description;
            String hightAndLow;

            JSONObject dayForecastWeather   = weatherArray.getJSONObject(i);
            JSONObject dayTemperature       = dayForecastWeather.getJSONObject(OWM_TEMPERATURE);
            JSONArray  weatherDay           = dayForecastWeather.getJSONArray(OWM_WEATHER);

            date        = dateToHumanReadable(dayForecastWeather.getLong(OWM_DATETIME));
            description = weatherDay.getJSONObject(0).getString(OWM_DESCRIPTION);

            double min  = dayTemperature.getDouble(OWM_MIN);
            double max  = dayTemperature.getDouble(OWM_MAX);

            Log.v("ForecastFragment", "unitSelected - " + unitSelected);
            if (unitSelected.equals("imperial"))
            {
                Log.v("ForecastFragment", "Convertendo para imperial");
                min *= 33.8;
                max *= 33.8;
            }

            //TODO atualizar settings de unidades quando for alterada pelo usuário
            //TODO colocar no summary de unidades a unidade selecionada
            DecimalFormat decimalFormat = new DecimalFormat("#.0");
            hightAndLow = "Min " + decimalFormat.format(min) + " / Max " + decimalFormat.format(max);
            resultStr[i]    = date + " - " + description + " - " + hightAndLow;

        }

        return  resultStr;

    }

    private String dateToHumanReadable( long time )
    {
        Date date   = new Date( time * 1000 );
        SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
        return format.format(date).toString();
    }

    private class FetchWeatherTask extends AsyncTask<String, Void, String[]>
    {
        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected String[] doInBackground(String... params)
        {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            String myQueryURL = params[0];


            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are available at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                URL url = new URL( myQueryURL );

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    forecastJsonStr = null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    forecastJsonStr = null;
                }
                forecastJsonStr = buffer.toString();

                String[] formatedForecastString;

                try {
                    return formatedForecastString = getWeathrFromJson(forecastJsonStr);
                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                }
            } catch (IOException e) {
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                forecastJsonStr = null;
                //return forecastJsonStr;
                return null;

            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);

            if (result != null) {
                arrayAdapter.clear();
                for ( String dayForecastStr : result )
                {
                    arrayAdapter.add(dayForecastStr);
                }


            }
        }
    }

}
