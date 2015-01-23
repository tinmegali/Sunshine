/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tupigames.sunshine;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import com.tupigames.sunshine.data.WeatherContract.LocationEntry;
import com.tupigames.sunshine.data.WeatherContract.WeatherEntry;
import com.tupigames.sunshine.data.WeatherDbHelper;

import java.util.Map;
import java.util.Set;


public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    public void testCreateDb() throws Throwable {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new WeatherDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

    public void testInsertReadDb() {

        // Test data we're going to insert into the DB to see if it works.
        String testLocationSetting = "99705";
        String testCityName = "North Pole";
        double testLatitude = 64.7488;
        double testLongitude = -147.353;

        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(LocationEntry.COLUMN_LOCATION_SETTING, testLocationSetting);
        values.put(LocationEntry.COLUMN_CITY_NAME, testCityName);
        values.put(LocationEntry.COLUMN_COORD_LAT, testLatitude);
        values.put(LocationEntry.COLUMN_COORD_LONG, testLongitude);

        long locationRowId;
        locationRowId = db.insert(LocationEntry.TABLE_NAME, null, values);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // Specify which columns you want.
        String[] columns = {
                LocationEntry._ID,
                LocationEntry.COLUMN_LOCATION_SETTING,
                LocationEntry.COLUMN_CITY_NAME,
                LocationEntry.COLUMN_COORD_LAT,
                LocationEntry.COLUMN_COORD_LONG
        };

        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                LocationEntry.TABLE_NAME,  // Table to Query
                columns,
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // If possible, move to the first row of the query results.
        if (cursor.moveToFirst()) {
            // Get the value in each column by finding the appropriate column index.
            int locationIndex = cursor.getColumnIndex(LocationEntry.COLUMN_LOCATION_SETTING);
            String location = cursor.getString(locationIndex);

            int nameIndex = cursor.getColumnIndex((LocationEntry.COLUMN_CITY_NAME));
            String name = cursor.getString(nameIndex);

            int latIndex = cursor.getColumnIndex((LocationEntry.COLUMN_COORD_LAT));
            double latitude = cursor.getDouble(latIndex);

            int longIndex = cursor.getColumnIndex((LocationEntry.COLUMN_COORD_LONG));
            double longitude = cursor.getDouble(longIndex);

            // Hooray, data was returned!  Assert that it's the right data, and that the database
            // creation code is working as intended.
            // Then take a break.  We both know that wasn't easy.
            assertEquals(testCityName, name);
            assertEquals(testLocationSetting, location);
            assertEquals(testLatitude, latitude);
            assertEquals(testLongitude, longitude);

            // Fantastic.  Now that we have a location, add some weather!
        } else {
            // That's weird, it works on MY machine...
            fail("No values returned :(");
        }


        String testDate     = "20141205";
        double testDegrees  = 1.1;
        double testHumidity = 1.2;
        double testPressure = 1.3;
        double testMax      = 75;
        double testMin      = 60;
        String testDesc     = "Asteroids";
        double testWind     = 5.5;
        double testWID      = 321;

        // Fantastic.  Now that we have a location, add some weather!
        ContentValues weatherValues = new ContentValues();
        weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationRowId);
        weatherValues.put(WeatherEntry.COLUMN_DATETEXT, testDate);
        weatherValues.put(WeatherEntry.COLUMN_DEGREES, testDegrees);
        weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, testHumidity);
        weatherValues.put(WeatherEntry.COLUMN_PRESSURE, testPressure);
        weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, testMax);
        weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, testMin);
        weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, testDesc);
        weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, testWind);
        weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, testWID);

        /**
         * TODO YOUR CODE BELOW HERE FOR QUIZ
         * QUIZ - 4a - InsertReadDbTest
         * https://www.udacity.com/course/viewer#!/c-ud853/l-1639338560/e-1633698604/m-1633698605
         **/

        long weatherRowID;
        weatherRowID =  db.insert(WeatherEntry.TABLE_NAME, null, weatherValues);
        assertTrue(weatherRowID != -1);

        String[] columnsWeather = {
                WeatherEntry.COLUMN_DATETEXT,
                WeatherEntry.COLUMN_DEGREES,
                WeatherEntry.COLUMN_HUMIDITY,
                WeatherEntry.COLUMN_LOC_KEY,
                WeatherEntry.COLUMN_MAX_TEMP,
                WeatherEntry.COLUMN_MIN_TEMP,
                WeatherEntry.COLUMN_PRESSURE,
                WeatherEntry.COLUMN_WEATHER_ID,
                WeatherEntry.COLUMN_WIND_SPEED,
                WeatherEntry.COLUMN_SHORT_DESC
        };

        // A cursor is your primary interface to the query results.
        Cursor cursorWeather = db.query(
                WeatherEntry.TABLE_NAME,  // Table to Query
                columnsWeather,
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // If possible, move to the first row of the query results.
        if (cursorWeather.moveToFirst()) {
            // Get the value in each column by finding the appropriate column index.
            int dateIndex = cursorWeather.getColumnIndex(WeatherEntry.COLUMN_DATETEXT);
            String date = cursorWeather.getString(dateIndex);

            int descIndex = cursorWeather.getColumnIndex(WeatherEntry.COLUMN_SHORT_DESC);
            String description = cursorWeather.getString(descIndex);

            int minIndex = cursorWeather.getColumnIndex(WeatherEntry.COLUMN_MIN_TEMP);
            double min = cursorWeather.getDouble(minIndex);

            int maxIndex = cursorWeather.getColumnIndex(WeatherEntry.COLUMN_MAX_TEMP);
            double max = cursorWeather.getDouble(maxIndex);

            int humidityIndex = cursorWeather.getColumnIndex(WeatherEntry.COLUMN_HUMIDITY);
            double humidity = cursorWeather.getDouble(humidityIndex);

            int pressureIndex = cursorWeather.getColumnIndex(WeatherEntry.COLUMN_PRESSURE);
            double pressure = cursorWeather.getDouble(pressureIndex);

            int windspeedIndex = cursorWeather.getColumnIndex(WeatherEntry.COLUMN_WIND_SPEED);
            double windspeed = cursorWeather.getDouble(windspeedIndex);


            assertEquals(testDate, date);
            assertEquals(testDesc, description);
            assertEquals(testPressure, pressure);
            assertEquals(testHumidity, humidity);
            assertEquals(testWind, windspeed);
            assertEquals(testMax, max);
            assertEquals(testMin, min);

            // Fantastic.  Now that we have a location, add some weather!
        } else {
            // That's weird, it works on MY machine...
            fail("No values returned :(");
        }

        cursorWeather.close();

        dbHelper.close();
    }

    static ContentValues createWeatherValues(long locationRowId) {
        ContentValues weatherValues = new ContentValues();
        weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationRowId);
        weatherValues.put(WeatherEntry.COLUMN_DATETEXT, "20141205");
        weatherValues.put(WeatherEntry.COLUMN_DEGREES, 1.1);
        weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, 1.2);
        weatherValues.put(WeatherEntry.COLUMN_PRESSURE, 1.3);
        weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, 75);
        weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, 65);
        weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, "Asteroids");
        weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, 5.5);
        weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, 321);

        return weatherValues;
    }

    static ContentValues createNorthPoleLocationValues() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(LocationEntry.COLUMN_LOCATION_SETTING, "99705");
        testValues.put(LocationEntry.COLUMN_CITY_NAME, "North Pole");
        testValues.put(LocationEntry.COLUMN_COORD_LAT, 64.7488);
        testValues.put(LocationEntry.COLUMN_COORD_LONG, -147.353);

        return testValues;
    }

    static void validateCursor(Cursor valueCursor, ContentValues expectedValues) {

        assertTrue(valueCursor.moveToFirst());

        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse(idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals(expectedValue, valueCursor.getString(idx));
        }
        valueCursor.close();
    }

    static final String TEST_LOCATION = "99705";
    static final String TEST_DATE = "20141205";
}