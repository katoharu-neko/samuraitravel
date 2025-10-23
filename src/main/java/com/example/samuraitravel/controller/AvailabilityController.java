package com.example.samuraitravel.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.repository.HouseRepository;
import com.example.samuraitravel.service.ReservationService;

/**
 * 民宿一覧ページ用：日付範囲で空き民宿を返すAPI
 * 既存の検索機能を壊さないよう、追加のRESTエンドポイントで提供します。
 */
@RestController
public class AvailabilityController {

    private final HouseRepository houseRepository;
    private final ReservationService reservationService;

    public AvailabilityController(HouseRepository houseRepository,
                                  ReservationService reservationService) {
        this.houseRepository = houseRepository;
        this.reservationService = reservationService;
    }

    /**
     * GET /api/houses/available?start=YYYY-MM-DD&end=YYYY-MM-DD
     *  end は FullCalendar と同様「排他的（exclusive）」扱いでOKです。
     */
    @GetMapping("/api/houses/available")
    public List<Map<String, Object>> findAvailableHouses(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end")   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        List<Map<String, Object>> results = new ArrayList<>();

        // 念のためガード
        if (start == null || end == null || !start.isBefore(end)) {
            return results;
        }

        // ここでは全民宿から絞り込む（既存の複合条件検索を壊さないため）
        List<House> all = houseRepository.findAll();

        for (House h : all) {
            boolean ok = reservationService.isAvailable(h.getId(), start, end);
            if (ok) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", h.getId());
                row.put("name", h.getName());
                row.put("capacity", h.getCapacity());
                row.put("price", h.getPrice());
                row.put("imageName", h.getImageName()); // 画像がある場合に使えます
                results.add(row);
            }
        }
        return results;
    }
}
