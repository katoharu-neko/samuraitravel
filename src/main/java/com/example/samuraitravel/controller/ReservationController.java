package com.example.samuraitravel.controller;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.samuraitravel.dto.ReservationDTO;
import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.entity.Reservation;
import com.example.samuraitravel.entity.User;
import com.example.samuraitravel.form.ReservationInputForm;
import com.example.samuraitravel.security.UserDetailsImpl;
import com.example.samuraitravel.service.HouseService;
import com.example.samuraitravel.service.ReservationService;
import com.example.samuraitravel.service.StripeService;

import jakarta.servlet.http.HttpSession;

//会員用の予約一覧ページを作ろう

@Controller
public class ReservationController {
	private final ReservationService reservationService;
	private final HouseService houseService;
	private final StripeService stripeService;
	
	public ReservationController(ReservationService reservationService, HouseService houseService, StripeService stripeService) {
		this.reservationService = reservationService;
		this.houseService = houseService;
		this.stripeService = stripeService;
	}
	
	@GetMapping("/reservations")
	//@AuthenticationPrincipalアノテーションで現在ログイン中のユーザー
	public String index(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
						@PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable,
						Model model)
	{
		User user = userDetailsImpl.getUser();
		Page<Reservation> reservationPage = reservationService.findReservationsByUserOrderByCreatedAtDesc(user, pageable);
		
		model.addAttribute("reservationPage", reservationPage);
		
		return "reservations/index";
	}
	
	//76 予約内容の確認ページのコントローラー
	@PostMapping("/houses/{id}/reservations/input")
	public String input(@PathVariable(name = "id") Integer id,
						@ModelAttribute @Validated ReservationInputForm reservationInputForm,
						BindingResult bindingResult,
						RedirectAttributes redirectAttributes,
						HttpSession httpSession,
						Model model)
	{
		Optional<House> optionalHouse = houseService.findHouseById(id);
		
		if(optionalHouse.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "民宿が存在しません。");
			
			return "redirect:/houses";
		}
		
		//チェックイン日とチェックアウト日を取得する
		LocalDate checkinDate = reservationInputForm.getCheckinDate();
		LocalDate checkoutDate = reservationInputForm.getCheckoutDate();
		
		House house = optionalHouse.get();
		
		//宿泊人数と民宿の定員を取得する。
		Integer numberOfPeople = reservationInputForm.getNumberOfPeople();
		Integer capacity = house.getCapacity();
		
		//チェックイン日はチェックアウト日よりも前の日付が選択されているか。
		if (checkinDate != null && checkoutDate != null && !reservationService.isCheckinBeforeCheckout(checkinDate, checkoutDate)) {
			FieldError fieldError = new FieldError(bindingResult.getObjectName(), "checkinDate", "チェックイン日はチェックアウト日よりも前の日付を選択してください。");
			bindingResult.addError(fieldError);
		}
		
		//宿泊人数が定員を超えていないか
		if (numberOfPeople != null && !reservationService.isWithinCapacity(numberOfPeople, capacity)) {
			FieldError fieldError = new FieldError(bindingResult.getObjectName(), "numberOfPeople", "宿泊人数が定員を超えています。");
			bindingResult.addError(fieldError);
		}
		
		// 2次開発　空き状況チェック
		if (checkinDate != null && checkoutDate != null) {
		    boolean available = reservationService.isAvailable(house.getId(), checkinDate, checkoutDate);
		    if (!available) {
		        // カレンダーから来ていない場合も想定して、フォーム側でもガード
		        FieldError fieldError = new FieldError(bindingResult.getObjectName(),
		                "checkinDate", "この期間はすでに予約があります。別の日程を選択してください。");
		        bindingResult.addError(fieldError);
		    }
		}		
		
		if (bindingResult.hasErrors()) {
			String previousDates = reservationService.getPreviousDates(checkinDate, checkoutDate, bindingResult);
			
			model.addAttribute("house", house);
			model.addAttribute("reservationInputForm", reservationInputForm);
			model.addAttribute("previousDates", previousDates);
			model.addAttribute("errorMessage", "予約内容に不備があります。");
			
			return "houses/show";
		}
		
		//宿泊料金を計算する
		Integer price = house.getPrice();
		Integer amount = reservationService.calculateAmount(checkinDate, checkoutDate, price);
		
		ReservationDTO reservationDTO = new ReservationDTO(house.getId(), checkinDate, checkoutDate, numberOfPeople, amount);
		
		//セッションにDTOを保存する
		//セッションとは複数のHTTPリクエストにわたって一時的にデータを保存する仕組み
		//setAttribute()メソッドを使い、インスタンス化したReservationDTOオブジェクトを"reservationDTO"というキー名でセッションに保存しています。
		httpSession.setAttribute("reservationDTO", reservationDTO);
		
		return "redirect:/reservations/confirm";
	}
	
	@GetMapping("/reservations/confirm")
	//public String confirm(RedirectAttributes redirectAttributes, HttpSession httpSession, Model model) {
	public String confirm(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
						RedirectAttributes redirectAttributes,
						HttpSession httpSession,
						Model model)
	{
		//セッションからDTOを取得する
		//(ReservationDTO)をつけて型変換しているのはgetAttribute()メソッドの戻り値がObject型だからです。
		ReservationDTO reservationDTO = (ReservationDTO)httpSession.getAttribute("reservationDTO");
		
		//セッションにReservationDTOオブジェクトが存在しない場合はエラー
		if ( reservationDTO == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "セッションがタイムアウトしました。もう一度予約内容を入力してください。");
			
			return "redirect:/houses";
		}
		
		User user = userDetailsImpl.getUser();
		
		String sessionId = stripeService.createStripeSession(reservationDTO, user);
		
		model.addAttribute("reservationDTO", reservationDTO);
		model.addAttribute("sessionId", sessionId);
		
		return "reservations/confirm";
	}

	/*
	createメソッドはもう使わないのでコメントアウト
	
	//79 予約情報の登録処理
	@PostMapping("/reservations/create")
	public String create(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, RedirectAttributes redirectAttributes, HttpSession httpSession) {
		//セッションからDTOを取得する
		ReservationDTO reservationDTO = (ReservationDTO)httpSession.getAttribute("reservationDTO");
		
		if (reservationDTO ==null) {
			//存在しない場合は民宿一覧ページにリダイレクト
			redirectAttributes.addFlashAttribute("errorMessage", "セッションがタイムアウトしました。もう一度予約内容を入力してください。");
			
			return "redirect:/houses";
		}
		
		//ReservationDTOオブジェクトをログイン中のユーザー情報とともにcreateResevation()メソッドに渡し、予約情報をデータベースに登録しています。
		User user = userDetailsImpl.getUser();
		reservationService.createReservation(reservationDTO, user);
		
		//セッションからDTOを削除する
		httpSession.removeAttribute("reservationDTO");
		return "redirect:/reservations?reserved";
		
	}
	*/

}
