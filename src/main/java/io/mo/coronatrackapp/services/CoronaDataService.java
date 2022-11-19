package io.mo.coronatrackapp.services;

import io.mo.coronatrackapp.models.ApiSportsStats;
import io.mo.coronatrackapp.models.Country;
import io.mo.coronatrackapp.models.LocationStats;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class CoronaDataService {

    private static String VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";

    private List<LocationStats> allStats = new ArrayList<>();

    public List<LocationStats> getAllStats() {
        return allStats;
    }

    @PostConstruct
    @Scheduled(cron = "* * 1 * * *")
    public void getVirusData() {
        List<LocationStats> newStats = new ArrayList<>();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(VIRUS_DATA_URL))
                .build();
        try {
            HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());

            StringReader csvBodyReader = new StringReader(httpResponse.body());
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);

            for (CSVRecord record : records) {
                LocationStats locationStat = new LocationStats();
                locationStat.setState(record.get("Province/State"));
                locationStat.setCountry(record.get("Country/Region"));
                try {
                    int latestCases = Integer.parseInt(record.get(record.size() - 1));
                    int prevDayCases = Integer.parseInt(record.get(record.size() - 2));
                    locationStat.setLatestTotalCases(latestCases);
                    locationStat.setDiffFromPrevDay(latestCases - prevDayCases);
                } catch (NumberFormatException e) {
                    locationStat.setLatestTotalCases(0);
                }

                newStats.add(locationStat);
            }
            this.allStats = newStats;
        } catch (IOException|InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }

    public ApiSportsStats getSecondApiData() {
        //get today's overall data
        HttpRequest requestTotalStats = HttpRequest.newBuilder()
                .uri(URI.create("https://covid-193.p.rapidapi.com/statistics?country=all"))
                .header("X-RapidAPI-Key", "your-rapid-api-key")
                .header("X-RapidAPI-Host", "covid-193.p.rapidapi.com")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();

        //get today's data for every country
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://covid-193.p.rapidapi.com/statistics"))
                .header("X-RapidAPI-Key", "your-rapid-api-key")
                .header("X-RapidAPI-Host", "covid-193.p.rapidapi.com")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();

        //creating object of ApisportsStats class
        ApiSportsStats ApiSportsData = new ApiSportsStats();

        //getting overall data
        try {
            HttpResponse<String> responseTotalStats =
                    HttpClient.newHttpClient().send(requestTotalStats, HttpResponse.BodyHandlers.ofString());
            String toBeRaplaced = responseTotalStats.body().substring(0, responseTotalStats.body().indexOf(":[{") + 1);
            String responseStr = responseTotalStats.body().replace(toBeRaplaced, "");
            JSONArray responseArray = new JSONArray(responseStr);

            for (int i = 0; i < responseArray.length(); i++) {
                //getting all cases
                JSONObject obj = responseArray.getJSONObject(i);
                String allCasesTmp = obj.getString("cases");
                int startIndex = allCasesTmp.indexOf("total") + 7;
                int endIndex = allCasesTmp.indexOf("cri") - 2;
                String allCases = allCasesTmp.substring(startIndex, endIndex).replace("\"","");
                ApiSportsData.setTotalCases(allCases);

                //getting new cases
                String newCasesTmp = obj.getString("cases");
                int startIndex2 = newCasesTmp.indexOf("new") + 5;
                int endIndex2 = newCasesTmp.indexOf("rec") - 2;
                String newCases = newCasesTmp.substring(startIndex2, endIndex2).replace("\"","");
                ApiSportsData.setNewCases(newCases);
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("There was a problem with getting the second api data. Error message: " + e.getMessage());
        } catch (JSONException e) {
            System.out.println("JSON Exception ----- " + e.getMessage());
        }

        //getting todays data
        try {
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            String toBeRaplaced = response.body().substring(0, response.body().indexOf(":[{") + 1);
            String responseStr = response.body().replace(toBeRaplaced, "");
            JSONArray responseArray = new JSONArray(responseStr);
            List<Country> countries = new ArrayList<>();

            for (int i = 0; i < responseArray.length(); i++) {
                JSONObject obj = responseArray.getJSONObject(i);
                Country country = new Country();

                //getting country name
                country.setName(obj.getString("country"));

                //getting total cases
                String allCasesTmp = obj.getString("cases");
                int startIndex = allCasesTmp.indexOf("total") + 7;
                int endIndex = allCasesTmp.indexOf("cri") - 2;
                country.setTotalCases(allCasesTmp.substring(startIndex, endIndex).replace("\"",""));

                //getting new cases
                String newCasesTmp = obj.getString("cases");
                int startIndex2 = newCasesTmp.indexOf("new") + 5;
                int endIndex2 = newCasesTmp.indexOf("rec") - 2;
                String newCases = newCasesTmp.substring(startIndex2, endIndex2).replace("\"","");

                if (newCases == "null") {
                    country.setNewCases("0");
                } else {
                    country.setNewCases(newCases);
                }

                countries.add(country);
            }
            countries.sort(Comparator.comparing(Country::getName));
            ApiSportsData.setCountries(countries);
        } catch (IOException | InterruptedException e) {
            System.out.println("There was a problem with getting the second api data. Error message: " + e.getMessage());
        } catch (JSONException e) {
            System.out.println("JSON Exception ----- " + e.getMessage());
        }

        return ApiSportsData;
    }
}
