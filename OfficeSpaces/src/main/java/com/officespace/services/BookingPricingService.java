package com.officespace.services;

import com.officespace.utils.BookingDateUtils;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
public class BookingPricingService {

    public double calculateTotal(double basePrice, String priceUnit, LocalDate start, LocalDate end, String startTime, String endTime) {
        String unit = priceUnit != null ? priceUnit.toUpperCase() : "MONTH";

        switch (unit) {
            case "HOUR":
                long hours = BookingDateUtils.calculateHours(startTime, endTime);
                return basePrice * hours;
            case "DAY":
                long days = BookingDateUtils.calculateDays(start, end);
                return basePrice * days;
            case "WEEK":
                long weeks = BookingDateUtils.calculateWeeks(start, end);
                return basePrice * weeks;
            case "MONTH":
            default:
                long months = BookingDateUtils.calculateMonths(start, end);
                return basePrice * months;
        }
    }

    public String calculateDuration(String priceUnit, LocalDate start, LocalDate end, String startTime, String endTime) {
        String unit = priceUnit != null ? priceUnit.toUpperCase() : "MONTH";

        switch (unit) {
            case "HOUR":
                long hours = BookingDateUtils.calculateHours(startTime, endTime);
                return hours + (hours == 1 ? " Hour" : " Hours");
            case "DAY":
                long days = BookingDateUtils.calculateDays(start, end);
                return days + (days == 1 ? " Day" : " Days");
            case "WEEK":
                long weeks = BookingDateUtils.calculateWeeks(start, end);
                return weeks + (weeks == 1 ? " Week" : " Weeks");
            case "MONTH":
            default:
                long months = BookingDateUtils.calculateMonths(start, end);
                return months + (months == 1 ? " Month" : " Months");
        }
    }
}
