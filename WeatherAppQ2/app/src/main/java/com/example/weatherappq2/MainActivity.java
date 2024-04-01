package com.example.weatherappq2;

import androidx.appcompat.app.AppCompatActivity;

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
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    Button searchButton;
    EditText cityName, inputDate;
    TextView max_temperature, min_temperature, windSpeed, dialogMaxTemperature, dialogMinTemperature;
    ImageView weatherIcon;

    private static DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseHelper = new DatabaseHelper(this);
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
            boolean isFuture = isFutureDate(date);
            if(res.equals("PASS") && !flagged){
                fetchData(city, date, isFuture);
            }
            else{
                // show the error message
                inputDate.setError(res);
            }
        });
    }

    private void fetchData(String city, String date, boolean isFuture){
        ArrayList<String> backendData = fetch_data_from_dataBase(city, date, isFuture);
        if(backendData.isEmpty()){
            if(isFuture) {
                String[] dateArray = date.split("-");
                int multiplier = 1;
                // leap year
                if(Integer.parseInt(dateArray[2]) % 4 == 0 && Integer.parseInt(dateArray[1]) == 2 && Integer.parseInt(dateArray[0]) == 29){
                    multiplier = 4;
                }

                String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                // date should be less than or equal to current date
                int currentYear = Integer.parseInt(currentDate.substring(6));
                int currentMonth = Integer.parseInt(currentDate.substring(3, 5));
                int currentDay = Integer.parseInt(currentDate.substring(0, 2));
                int year = currentYear-1;
                if(Integer.parseInt(dateArray[2]) == currentYear){
                    year = currentYear - 1;
                } else if (Integer.parseInt(dateArray[2]) > currentYear) {
                    if(Integer.parseInt(dateArray[1]) > currentMonth){
                        year = currentYear - 1;
                    } else if (Integer.parseInt(dateArray[1]) == currentMonth) {
                        if(Integer.parseInt(dateArray[0]) > currentDay){
                            year = currentYear - 1;
                        } else {
                            year = currentYear;
                        }
                    } else {
                        year = currentYear;
                    }
                }

                boolean[] firstError = {false};

                for (int i = 0; i < 10; i++) {
                    String newDate = dateArray[0] + "-" + dateArray[1] + "-" + String.valueOf(year - multiplier * i);
                    int finalI = i;
                    fetch_data_from_API(city, newDate, new DataFetchCallback() {
                        @Override
                        public void onDataFetched(ArrayList<String> data) {
                            if(data.isEmpty()){
                                return;
                            }
                            double tempmin = Double.parseDouble(data.get(0));
                            double tempmax = Double.parseDouble(data.get(1));
                            String description = data.get(2);
                            String icon = data.get(3);
                            ArrayList<String> backendData = new ArrayList<>();
                            backendData.add(String.valueOf(tempmin));
                            backendData.add(String.valueOf(tempmax));
                            backendData.add(description);
                            backendData.add(icon);
                            writeIntoDatabase(city, date, backendData, isFuture);
                            showData(backendData);
                        }

                        @Override
                        public void onError(String errorMessage) {
                            // Handle the error case here
                            System.out.println("Error: " + errorMessage);
                            if(finalI == 0){
                                firstError[0] = true;
                            }
                        }
                    });

                    if(i == 0 && firstError[0]){
                        break;
                    }
                }
            }
            else{
                fetch_data_from_API(city, date, new DataFetchCallback() {
                    @Override
                    public void onDataFetched(ArrayList<String> data) {
                        if(data.isEmpty()){
                            System.out.println("Data is empty");
                            return;
                        }
                        double tempmin = Double.parseDouble(data.get(0));
                        double tempmax = Double.parseDouble(data.get(1));
                        String description = data.get(2);
                        String icon = data.get(3);
                        ArrayList<String> backendData = new ArrayList<>();
                        backendData.add(String.valueOf(tempmin));
                        backendData.add(String.valueOf(tempmax));
                        backendData.add(description);
                        backendData.add(icon);
                        writeIntoDatabase(city, date, backendData, isFuture);
                        showData(backendData);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        // Handle the error case here
                        System.out.println("Error: " + errorMessage);
                    }
                });
            }
        } else {
            showData(backendData);
        }
    }

    private void showData(ArrayList<String> data){
        dialogMinTemperature.setVisibility(TextView.VISIBLE);
        dialogMaxTemperature.setVisibility(TextView.VISIBLE);
        min_temperature.setText(data.get(0));
        max_temperature.setText(data.get(1));
        windSpeed.setText(data.get(2));
        String icon = data.get(3);
        icon = icon.replace("-", "_");
        weatherIcon.setImageResource(getResources().getIdentifier(icon, "drawable", getPackageName()));
    }

    private void reset_fetched_data(){
        max_temperature.setText("");
        min_temperature.setText("");
        windSpeed.setText("");
        weatherIcon.setImageResource(0);
        dialogMinTemperature.setVisibility(TextView.INVISIBLE);
        dialogMaxTemperature.setVisibility(TextView.INVISIBLE);
    }

    private ArrayList<String> fetch_data_from_dataBase(String city, String date, boolean isFuture){
        ArrayList<String> data = databaseHelper.getData(city, date, isFuture);
        System.out.println("Data from Database: ");
        for(String s : data){
            System.out.println(s);
        }
        return data;
    }

    private void writeIntoDatabase(String city, String date, ArrayList<String> data, boolean isFuture){
        databaseHelper.insertData(date, city, isFuture, data.get(0), data.get(1), data.get(3), data.get(2));
    }

    public interface DataFetchCallback {
        void onDataFetched(ArrayList<String> data);
        void onError(String errorMessage);
    }

    private void fetch_data_from_API(String city, String date,  DataFetchCallback callback) {

        // change the format from dd:mm:yyyy to yyyy-mm-dd
        String[] dateArray = date.split("-");
        String newDate = dateArray[2] + "-" + dateArray[1] + "-" + dateArray[0];

//        ArrayList<String> data = new ArrayList<>();

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/" + city + "/" + newDate + "/" + newDate + "?key=R8S3X48KHHVQQA3HBEG3HS4VU";

//        String url = "https://httpbin.org/get";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    // Parse the response here

                    // print the response
                    System.out.println(response);
                    // parsing Json response
                    JSONObject jsonObject = null;
                    try {
                        ArrayList<String> data = new ArrayList<>();
                        jsonObject = new JSONObject(response);
                        JSONArray daysArray = jsonObject.getJSONArray("days");
                        JSONObject dayObject = daysArray.getJSONObject(0);
                        double tempmin = dayObject.getDouble("tempmin");
                        double tempmax = dayObject.getDouble("tempmax");
                        tempmin = (tempmin - 32) * 5 / 9;
                        tempmax = (tempmax - 32) * 5 / 9;
                        tempmin = Math.round(tempmin * 10.0) / 10.0;
                        tempmax = Math.round(tempmax * 10.0) / 10.0;
                        String windspeed = dayObject.getString("description");
                        String icon = dayObject.getString("icon");
                        icon = icon.replace("-", "_");
                        data.add(String.valueOf(tempmin));
                        data.add(String.valueOf(tempmax));
                        data.add(windspeed);
                        data.add(icon);
                        callback.onDataFetched(data);
                    } catch (JSONException e) {
                        Toast.makeText(this, "Data not found", Toast.LENGTH_SHORT).show();
                        callback.onError("404");
                    }
                }, error -> {
            if(error == null || error.getMessage() == null){
                Toast.makeText(this, "Data not found", Toast.LENGTH_SHORT).show();
                callback.onError("404");
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
            callback.onError("404");
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private boolean isFutureDate(String date){
        // get current date
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        // date should be less than or equal to current date
        int currentYear = Integer.parseInt(currentDate.substring(6));
        int currentMonth = Integer.parseInt(currentDate.substring(3, 5));
        int currentDay = Integer.parseInt(currentDate.substring(0, 2));
        int passedYear = Integer.parseInt(date.substring(6));
        int passedMonth = Integer.parseInt(date.substring(3, 5));
        int passedDay = Integer.parseInt(date.substring(0, 2));

        if(passedYear > currentYear){
            return true;
        }
        else if(passedYear == currentYear){
            if(passedMonth > currentMonth){
                return true;
            }
            else if(passedMonth == currentMonth){
                if(passedDay > currentDay){
                    return true;
                }
            }
        }

        return false;
    }

    private String date_checker(String date){
        // check if date is in correct format
        if(date.length() != 10 || date.charAt(2) != '-' || date.charAt(5) != '-'){
            return "Incorrect Date Format";
        }
        // date should be less than or equal to current date
        int passedYear = Integer.parseInt(date.substring(6));
        int passedMonth = Integer.parseInt(date.substring(3, 5));
        int passedDay = Integer.parseInt(date.substring(0, 2));

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