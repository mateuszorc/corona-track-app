package io.mo.coronatrackapp.models;

import java.util.List;

public class ApiSportsStats {
    private String allTotalCases;
    private String allNewCases;
    private List<String> countries;

    public String getAllTotalCases() {
        return allTotalCases;
    }

    public void setAllTotalCases(String allTotalCases) {
        this.allTotalCases = allTotalCases;
    }

    public String getAllNewCases() {
        return allNewCases;
    }

    public void setAllNewCases(String allNewCases) {
        this.allNewCases = allNewCases;
    }

    public List<String> getCountries() {
        return countries;
    }

    public void setCountries(List<String> countries) {
        this.countries = countries;
    }
}
