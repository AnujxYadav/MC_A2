package com.example.weatherappq1;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    Button searchButton;
    EditText cityName, inputDate;
    TextView max_temperature, min_temperature, windSpeed, dialogMaxTemperature, dialogMinTemperature;
    ImageView weatherIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchButton = findViewById(R.id.SearchButton);
        cityName = findViewById(R.id.CityName);
        inputDate = findViewById(R.id.editTextDate);
        max_temperature = findViewById(R.id.max_temperature);
        min_temperature = findViewById(R.id.min_temperature);
        windSpeed = findViewById(R.id.windSpeed);
        weatherIcon = findViewById(R.id.svgIcon);
        dialogMinTemperature = findViewById(R.id.dialogMinTemperature);
        dialogMaxTemperature = findViewById(R.id.dialogMaxTemperature);

        reset_fetched_data();

        searchButton.setOnClickListener(v -> {
//            String demoCity = "New Delhi";
//            String demoDate = "2020-10-01";
//            fetch_data(demoCity, demoDate);
            reset_fetched_data();
            String city = cityName.getText().toString();
            String date = inputDate.getText().toString();
            // checking the city is empty or not
            boolean flagged = false;
            if(city.isEmpty()){
                cityName.setError("City Name is required");
                flagged = true;
            }

            // checking the date is correct or not
            String res = date_checker(date);
            if(res.equals("PASS") && !flagged){
//                System.out.println("City: " + city + " Date: " + date);
                fetch_data(city, date);
//                min_temperature.setText("78.9");
//                max_temperature.setText("89.8");
//                windSpeed.setText("Clear conditions throughout the day.");
//                String icon = "partly-cloudy-day";
//                icon = icon.replace("-", "_");
//                weatherIcon.setImageResource(getResources().getIdentifier(icon, "drawable", getPackageName()));
            }
            else{
                // show the error message
                inputDate.setError(res);
            }
        });
    }

    private void fetch_data(String city, String date) {
        // Instantiate the RequestQueue.

        // change the format from dd:mm:yyyy to yyyy-mm-dd
        String[] dateArray = date.split("-");
        String newDate = dateArray[2] + "-" + dateArray[1] + "-" + dateArray[0];

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/" + city + "/" + newDate + "/" + newDate + "?key=R8S3X48KHHVQQA3HBEG3HS4VU";

//        String url = "https://httpbin.org/get";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    // Parse the response here
                    ArrayList<String> data = new ArrayList<>();
                    // print the response
                    System.out.println(response);
                    // parsing Json response
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(response);
                        JSONArray daysArray = jsonObject.getJSONArray("days");
                        JSONObject dayObject = daysArray.getJSONObject(0);
                        double tempmin = dayObject.getDouble("tempmin");
                        double tempmax = dayObject.getDouble("tempmax");
                        // feranheit to celsius
                        tempmin = (tempmin - 32) * 5 / 9;
                        tempmax = (tempmax - 32) * 5 / 9;
                        tempmin = Math.round(tempmin * 10.0) / 10.0;
                        tempmax = Math.round(tempmax * 10.0) / 10.0;
                        String windspeed = dayObject.getString("description");
                        String icon = dayObject.getString("icon");
                        icon = icon.replace("-", "_");
                        dialogMinTemperature.setVisibility(TextView.VISIBLE);
                        dialogMaxTemperature.setVisibility(TextView.VISIBLE);
                        min_temperature.setText(String.valueOf(tempmin));
                        max_temperature.setText(String.valueOf(tempmax));
                        System.out.println("Min Temp: " + tempmin + " Max Temp: " + tempmax + " Wind Speed: " + windspeed + " Icon: " + icon);
                        windSpeed.setText(windspeed);
                        weatherIcon.setImageResource(getResources().getIdentifier(icon, "drawable", getPackageName()));
                        System.out.println("min_temperature: " + min_temperature.getText() + " max_temperature: " + max_temperature.getText() + " windSpeed: " + windSpeed.getText() + " weatherIcon: " + weatherIcon.getDrawable());
                    } catch (JSONException e) {
                        Toast.makeText(this, "Data not found", Toast.LENGTH_SHORT).show();
                    }


                    // Update UI elements here
//                    max_temperature.setText(data.get(0));
//                    min_temperature.setText(data.get(1));
//                    windSpeed.setText(data.get(2));
//                    weatherIcon.setImageResource(getResources().getIdentifier(data.get(3), "drawable", getPackageName()));
                }, error -> {
            // Handle error here

            if(error == null || error.getMessage() == null){
                Toast.makeText(this, "Data not found", Toast.LENGTH_SHORT).show();
                return;
            }

            String errorText = error.getMessage();

            if(errorText.contains("No address associated with hostname")){
                Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "Data not found", Toast.LENGTH_SHORT).show();
            }

            error.printStackTrace();
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void reset_fetched_data(){
        max_temperature.setText("");
        min_temperature.setText("");
        windSpeed.setText("");
        weatherIcon.setImageResource(0);
        // how to hide text
        dialogMinTemperature.setVisibility(TextView.INVISIBLE);
        dialogMaxTemperature.setVisibility(TextView.INVISIBLE);
    }

    private String date_checker(String date){
        // get current date
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        // check if date is in correct format
        if(date.length() != 10 || date.charAt(2) != '-' || date.charAt(5) != '-'){
            return "Incorrect Date Format";
        }
        // date should be less than or equal to current date
        int currentYear = Integer.parseInt(currentDate.substring(6));
        int currentMonth = Integer.parseInt(currentDate.substring(3, 5));
        int currentDay = Integer.parseInt(currentDate.substring(0, 2));
        int passedYear = Integer.parseInt(date.substring(6));
        int passedMonth = Integer.parseInt(date.substring(3, 5));
        int passedDay = Integer.parseInt(date.substring(0, 2));

        if(passedYear > currentYear){
            return "Year should be less than or equal to current year";
        }
        else if(passedYear == currentYear){
            if(passedMonth > currentMonth){
                return "Month should be less than or equal to current month";
            }
            else if(passedMonth == currentMonth){
                if(passedDay > currentDay){
                    return "Day should be less than or equal to current day";
                }
            }
        }

        if(passedYear < 1975){
            return "Year should be greater than 1975";
        }

        if(passedMonth > 12 || passedMonth < 1){
            return "Month should be between 1 to 12";
        }

        if(passedMonth == 1 || passedMonth == 3 || passedMonth == 5 || passedMonth == 7 || passedMonth == 8 || passedMonth == 10 || passedMonth == 12){
            if(passedDay > 31 || passedDay < 1){
                return "Day should be between 1 to 31";
            }
        }
        else if(passedMonth == 4 || passedMonth == 6 || passedMonth == 9 || passedMonth == 11){
            if(passedDay > 30 || passedDay < 1){
                return "Day should be between 1 to 30";
            }
        }
        else{
            if(passedYear % 4 == 0){
                if(passedDay > 29 || passedDay < 1){
                    return "Day should be between 1 to 29";
                }
            }
            else{
                if(passedDay > 28 || passedDay < 1){
                    return "Day should be between 1 to 28";
                }
            }
        }

        return "PASS";
    }
}