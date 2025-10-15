package com.example.samuraitravel.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.samuraitravel.entity.Reservation;
import com.example.samuraitravel.entity.User;

public interface ReservationRepository extends JpaRepository<Reservation, Integer>{
	public Page<Reservation> findByUserOrderByCreatedAtDesc(User user, Pageable pageabel);
	//2次開発
    List<Reservation> findByHouseIdOrderByCheckinDateAsc(Integer houseId);
	public Reservation findFirstByOrderByIdDesc();
	
	//2次開発　houseごとの予約一覧取得
    @Query("SELECT r FROM Reservation r WHERE r.house.id = :houseId ORDER BY r.checkinDate ASC")
    List<Reservation> findByHouseId(@Param("houseId") Integer houseId);
    
    //2次開発　期間重複の検出（[start,end) で管理：checkoutは宿泊しない日）
    @Query("""
            SELECT r FROM Reservation r
            WHERE r.house.id = :houseId
              AND (r.checkinDate < :checkout AND r.checkoutDate > :checkin)
            """)
     List<Reservation> findOverlap(
         @Param("houseId") Integer houseId,
         @Param("checkin") LocalDate checkin,
         @Param("checkout") LocalDate checkout
     );    
    
}
