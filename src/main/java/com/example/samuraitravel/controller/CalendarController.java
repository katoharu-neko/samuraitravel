package com.example.samuraitravel.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.samuraitravel.entity.Reservation;
import com.example.samuraitravel.service.ReservationService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class CalendarController {

    private final ReservationService reservationService;

    // カレンダービュー
    @GetMapping("/calendar/{houseId}")
    public String showCalendar(@PathVariable Integer houseId, Model model) {
        model.addAttribute("houseId", houseId);
        return "calendar";
    }

    // FullCalendar向けイベントJSON
    @GetMapping("/calendar/events/{houseId}")
    @ResponseBody
    public List<CalendarEvent> events(@PathVariable Integer houseId) {
        List<Reservation> list = reservationService.findByHouseId(houseId);
        return list.stream()
                .map(r -> new CalendarEvent("予約済み",
                        r.getCheckinDate().toString(),
                        r.getCheckoutDate().toString()))
                .collect(Collectors.toList());
    }

    // DTO（FullCalendarの期待形式）
    public record CalendarEvent(String title, String start, String end) {}
}
