package io.mo.coronatrackapp.controlers;

import io.mo.coronatrackapp.models.ApiSportsStats;
import io.mo.coronatrackapp.models.LocationStats;
import io.mo.coronatrackapp.services.CoronaDataService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    final CoronaDataService coronaDataService;

    public HomeController(CoronaDataService coronaDataService) {
        this.coronaDataService = coronaDataService;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<LocationStats> allStats = coronaDataService.getAllStats();
        int totalReportedCases = allStats.stream().mapToInt(LocationStats::getLatestTotalCases).sum();
        int totalNewCases = allStats.stream().mapToInt(stat -> stat.getDiffFromPrevDay()).sum();
        model.addAttribute("locationStats", allStats);
        model.addAttribute("totalReportedCases", totalReportedCases);
        model.addAttribute("totalNewCases", totalNewCases);
        return "home";
    }

    @GetMapping("/secondApi")
    public String secondApi(Model model) {
        ApiSportsStats apiSportsStats = coronaDataService.getSecondApiData();
        model.addAttribute("totalReportedCases", apiSportsStats.getTotalCases());
        model.addAttribute("totalNewCases", apiSportsStats.getNewCases());
        model.addAttribute("locationStats", apiSportsStats.getCountries());
        return "secondApi";
    }
}
