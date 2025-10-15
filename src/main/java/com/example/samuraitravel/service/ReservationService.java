package com.example.samuraitravel.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

//import com.example.samuraitravel.dto.ReservationDTO;
import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.entity.Reservation;
import com.example.samuraitravel.entity.User;
import com.example.samuraitravel.repository.HouseRepository;
import com.example.samuraitravel.repository.ReservationRepository;
import com.example.samuraitravel.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
//import jakarta.transaction.Transactional;

//70 会員用の予約一覧ページを作ろう
@Service
public class ReservationService {
	private final ReservationRepository reservationRepository;
	private final HouseRepository houseRepository;
    private final UserRepository userRepository;

	public ReservationService(ReservationRepository reservationRepository, HouseRepository houseRepository, UserRepository userRepository) {
		this.reservationRepository = reservationRepository;
		this.houseRepository = houseRepository;
		this.userRepository = userRepository;
	}
	
	//指定されたユーザーに紐づく予約を作成日時が新しい順に並べ替え、ページングされた状態で取得する
	public Page<Reservation> findReservationsByUserOrderByCreatedAtDesc(User user, Pageable pageable){
		return reservationRepository.findByUserOrderByCreatedAtDesc(user,pageable);
	}
	
	//75予約内容の確認ページを作ろう
	//チェックイン日がチェックアウト日よりも前の日付かどうかをチェックする
	public boolean isCheckinBeforeCheckout(LocalDate checkinDate, LocalDate checkoutDate) {
		return checkinDate.isBefore(checkoutDate);
	}
	
	//宿泊人数が定員以下かどうかをチェックする
	public boolean isWithinCapacity(Integer numberOfPeople, Integer capacity) {
		return numberOfPeople <= capacity;
	}
	
	//2次開発重複予約チェック
	
	
	//チェックイン・チェックアウト日の入力に不備がない場合は以前の入力値を取得する
	//houses/show.htmlファイルに渡す変数（previousDates）の値を取得するためのメソッド
	public String getPreviousDates(LocalDate checkinDate, LocalDate checkoutDate, BindingResult bindingResult) {
		//BindingResultクラスのhasFieldErrorsを使うことで特定のフィールドにエラーが存在するかを確かめる
		if (checkinDate != null && checkoutDate != null && !bindingResult.hasFieldErrors("checkinDate") && !bindingResult.hasFieldErrors("checkoutDate")) {
			DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			String formattedCheckinDate = checkinDate.format(dateTimeFormatter);
			String formattedCheckoutDate = checkoutDate.format(dateTimeFormatter);
			
			return formattedCheckinDate + " から " + formattedCheckoutDate;
		}
		//不適の場合は空文字をかえす。
		return "";
	}
	
	//宿泊料金を計算する
	public Integer calculateAmount(LocalDate checkinDate, LocalDate checkoutDate, Integer price) {
		long numberOfNights = ChronoUnit.DAYS.between(checkinDate, checkoutDate);
		int amount = price * (int)numberOfNights;
		
		return amount;
	}
	
	//80予約情報の登録処理のテスト
	//予約のレコード数を取得する
	public long countReservations() {
		return reservationRepository.count();
	}
	
	//idが最も大きい予約を取得する
	public Reservation findFirstReservationByOrderByIdDesc() {
		return reservationRepository.findFirstByOrderByIdDesc();
	}
	
	//79予約情報の登録処理を作ろう
	@Transactional
	//86 ReservationDTOオブジェクトやUserオブジェクトの代わりにsessionMetadataというMap型のオブジェクトを受け取るようにしています。
	//sessionMetadataは支払い情報のメタデータが含まれているオブジェクト、get()メソッドにメタデータ名を渡すことでその値を取得することができる
	
	public void createReservation(Map<String,String> sessionMetadata) {
		Reservation reservation = new Reservation();
		
		Integer houseId = Integer.valueOf(sessionMetadata.get("houseId"));
		Integer userId = Integer.valueOf(sessionMetadata.get("userId"));
		
		Optional<House> optionalHouse = houseRepository.findById(houseId);
		//EntityNotFoundExceptionクラスは、「エンティティにアクセスしたが、そのエンティティがデータベース上に存在しない」ことを表すための例外クラスです
		House house = optionalHouse.orElseThrow(() -> new EntityNotFoundException("指定されたIDの民宿が存在しません。"));
		
		Optional<User> optionalUser = userRepository.findById(userId);
		User user = optionalUser.orElseThrow(() -> new EntityNotFoundException("指定されたIDのユーザーが存在しません。"));
		
		LocalDate checkinDate = LocalDate.parse(sessionMetadata.get("checkinDate"));
		LocalDate checkoutDate = LocalDate.parse(sessionMetadata.get("checkoutDate"));
		Integer numberOfPeople = Integer.valueOf(sessionMetadata.get("numberOfPeople"));
		Integer amount = Integer.valueOf(sessionMetadata.get("amount"));
		
		
		reservation.setHouse(house);
		reservation.setUser(user);
		//reservation.setCheckinDate(reservationDTO.getCheckinDate());
		reservation.setCheckinDate(checkinDate);
		//reservation.setCheckoutDate(reservationDTO.getCheckoutDate());
		reservation.setCheckoutDate(checkoutDate);
		//reservation.setNumberOfPeople(reservationDTO.getNumberOfPeople());
		reservation.setNumberOfPeople(numberOfPeople);
		//reservation.setAmount(reservationDTO.getAmount());
		reservation.setAmount(amount);
		
		reservationRepository.save(reservation);
	}

}
