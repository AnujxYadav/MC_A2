# MC_A2_Q1

## Application Logic

1. **Initialization**: In the `onCreate` method, all the necessary UI elements are initialized. This includes buttons, text views, and image views.

2. **Resetting Data**: The `reset_fetched_data` method is called to clear any previously fetched weather data from the UI.

3. **Data Fetching**: When the search button is clicked, the `fetch_data` method is called. This method sends a GET request to the Visual Crossing Weather API to fetch weather data for the specified city and date. The date is formatted from `dd-mm-yyyy` to `yyyy-mm-dd` before being sent in the API request.

4. **API Interaction**: The `fetch_data_from_API` method fetches data from the API. It uses the Volley library to make a GET request to the API. The response from the API is parsed and added to an ArrayList. If the data is successfully fetched from the API, it is written into the database using the `writeIntoDatabase` method and displayed using the `showData` method.

5. **Data Parsing**: The response from the API is a JSON object. This object is parsed to extract the minimum and maximum temperatures, wind speed, and weather icon. The temperatures are converted from Fahrenheit to Celsius.

6. **UI Updating**: The parsed data is then used to update the UI elements. The minimum and maximum temperatures are displayed in the respective text views, the wind speed is displayed in its text view, and the weather icon is set in the image view.

7. **Error Handling**: If there's an error in the API request, an error message is displayed to the user. The error message depends on the type of error that occurred. There is method to check the input date and city name to validate the user input.



# MC_A2_Q2

This application fetches weather data for a given city and date. The main logic is implemented in the `MainActivity.java` file.

## Main Logic

1. **Initialization**: In the `onCreate` method, all the necessary UI elements are initialized. This includes buttons, text views, and image views.

2. **Resetting Data**: The `reset_fetched_data` method is called to clear any previously fetched weather data from the UI.

3. **Data Fetching**: The `fetchData` method is responsible for fetching the weather data. It first tries to fetch the data from the local database. If the data is not found in the database, it fetches the data from an API.

4. **Database Interaction**: The `fetch_data_from_dataBase` method fetches data from the local database using the `DatabaseHelper` class. If the data is successfully fetched, it is displayed using the `showData` method. If the data is not found in the database, the `fetch_data_from_API` method is called.

5. **API Interaction**: The `fetch_data_from_API` method fetches data from the API. It uses the Volley library to make a GET request to the API. The response from the API is parsed and added to an ArrayList. If the data is successfully fetched from the API, it is written into the database using the `writeIntoDatabase` method and displayed using the `showData` method.

6. **Error Handling**: If there is an error while fetching data from the API, the error is handled in the `onError` method of the `DataFetchCallback` interface. The error message is printed to the console.

7. **Date Checking**: The `date_checker` method checks if the input date is in the correct format and is a valid date. The `isFutureDate` method checks if the input date is a future date. If future date is entered, it will fetch the last 10 years data for the same date, it also handles for the leap year.


## Database Table

| Column Name | Data Type | Description |
|-------------|-----------|-------------|
| id          | INTEGER   | The unique identifier for each record. This is the primary key. |
| date        | TEXT      | The date for which the weather data is recorded. |
| location    | TEXT      | The city for which the weather data is recorded. |
| isFuture    | BOOLEAN   | A flag indicating whether the date is in the future. |
| minTemp     | TEXT      | The minimum temperature recorded on the date. |
| maxTemp     | TEXT      | The maximum temperature recorded on the date. |
| description | TEXT      | A description of the weather on the date. |
| icon        | TEXT      | An icon representing the weather on the date. |

Each row in the `weatherData` table represents the weather data for a specific date and location.
