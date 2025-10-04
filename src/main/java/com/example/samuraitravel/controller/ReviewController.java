package com.example.samuraitravel.controller;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.entity.Review;
import com.example.samuraitravel.entity.User;
import com.example.samuraitravel.form.ReviewEditForm;
import com.example.samuraitravel.form.ReviewRegisterForm;
import com.example.samuraitravel.security.UserDetailsImpl;
import com.example.samuraitravel.service.HouseService;
import com.example.samuraitravel.service.ReviewService;

@Controller
//RequestMapping リクエストのURLやHTTPメソッドに応じて、どのコントローラーメソッドが処理を行うかをマッピングするためのアノテーション
@RequestMapping("/houses/{houseId}/reviews")
public class ReviewController {
	
	//レビュー更新機能　依存性の注入 Houseも依存性注入
	private final HouseService houseService;
	private final ReviewService reviewService;
	
	public ReviewController(HouseService houseService, ReviewService reviewService) {
		this.houseService = houseService;
		this.reviewService = reviewService;
	}
	
	//1 findReviewsByHouseOrderByCreatedAtDesc()メソッドを用いて、指定された民宿のレビューをページングされた状態で取得し、民宿詳細ページへ受け渡して表示する。
	@GetMapping
	//@PathVariable によって RequestMappingメソッドの引数 houseId として渡される
	public String index(@PathVariable(name = "houseId") Integer houseId,
						@PageableDefault(page = 0, size = 10, sort = "id") Pageable pageable,
						RedirectAttributes redirectAttributes,
						Model model)
	{	
		//Optionalは値がNullの時に対応しやすい
		Optional<House> optionalHouse = houseService.findHouseById(houseId);
		
		if (optionalHouse.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "民宿が存在しません。");
			
			return "redirect:/houses";
		}
		
		House house = optionalHouse.get();
		Page<Review> reviewPage = reviewService.findReviewsByHouseOrderByCreatedAtDesc(house, pageable);
		
		model.addAttribute("house", house);
		model.addAttribute("reviewPage", reviewPage);
		
		return "reviews/index";
	}
	
	//2 レビュー投稿ページを表示する。レビュー投稿ページ用フォームクラスのインスタンスを生成し、指定された民宿のデータとともに受け渡す。
	//Getで良い
	@GetMapping("/register")
	public String register(@PathVariable(name = "houseId") Integer houseId,
							RedirectAttributes redirectAttributes,
							Model model) 
	{
		Optional<House> optionalHouse = houseService.findHouseById(houseId);
		
		if(optionalHouse.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "民宿が存在しません。");
			
			return "redirect:/houses";
		}
		
		House house = optionalHouse.get();
		
		model.addAttribute("house",house);
		//レビュー投稿ページ用フォームインスタンスの生成
		model.addAttribute("reviewRegisterForm", new ReviewRegisterForm());
		
		return "reviews/register";
	}
	
	
	//3 createReview()メソッドを用いて、新しいレビューを登録する。フォームのバリデーションNGの場合は、レビュー投稿ページを再表示。
	//Postで投げる
	@PostMapping("/create")
	//HouseId取得
	public String create(@PathVariable(name = "houseId") Integer houseId,
						@ModelAttribute @Validated ReviewRegisterForm reviewRegisterForm,
						//バリデーションチェックのため必要
						BindingResult bindingResult,
						@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
						RedirectAttributes redirectAttributes,
						Model model) 
	{
		Optional<House> optionalHouse = houseService.findHouseById(houseId);
		
		if(optionalHouse.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "民宿が存在しません。");
			
			return "redirect:/houses";
		}
		
		House house = optionalHouse.get();
		
		//バリデーションチェック
		if (bindingResult.hasErrors()) {
			//modelに入れてregisterに返す
			model.addAttribute("house", house);
			model.addAttribute("reviewRegisterForm", reviewRegisterForm);
			
			return "reviews/register";
		}
		
		User user = userDetailsImpl.getUser();
		//ここでようやくcreatReview()メソッドを使う。
		reviewService.createReview(reviewRegisterForm, house, user);
		redirectAttributes.addFlashAttribute("successMessage", "レビューを投稿しました。");
		
		return "redirect:/houses/{houseId}";
	}
	
	//4 レビュー編集ページを表示する。レビュー編集ページ用フォームクラスのインスタンスを生成し、指定された民宿・レビューのデータとともに受け渡す。
	//reviewIdを導入する
	@GetMapping("/{reviewId}/edit")
	public String edit(@PathVariable(name = "houseId") Integer houseId,
						@PathVariable(name = "reviewId") Integer reviewId,
						//User認証が必要
						@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
						RedirectAttributes redirectAttributes,
						Model model) 
	{
		Optional<House> optionalHouse = houseService.findHouseById(houseId);
		Optional<Review> optionalReview = reviewService.findReviewById(reviewId); 

		if(optionalHouse.isEmpty() || optionalReview.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "指定されたページが見つかりません。");

			return "redirect:/houses";
		}

		House house = optionalHouse.get();
		Review review = optionalReview.get();
		User user = userDetailsImpl.getUser();
		
		//違うユーザーが編集することを防ぐ必要がある。もう一段階認証が必要。
		if (!review.getHouse().getId().equals(house.getId()) || !review.getUser().getId().equals(user.getId())) {
			redirectAttributes.addFlashAttribute("errorMessage", "不正なアクセスです。");
			
			return "redirect:/houses/{houseId}";
		}
		
		//レビュー更新ページ用フォームインスタンス生成
		ReviewEditForm reviewEditForm = new ReviewEditForm(review.getScore(),review.getContent());
		//モデルに入れる
		model.addAttribute("house",house);
		model.addAttribute("review", review);
		model.addAttribute("reviewEditForm", reviewEditForm);

		return "reviews/edit";
	}		
		
	//5 updateReview()メソッドを用いて、指定されたレビューを更新する。フォームのバリデーションNGの場合は、レビュー編集ページを再表示。
	
	@PostMapping("/{reviewId}/update")
	public String update(@PathVariable(name = "houseId") Integer houseId,
						@PathVariable(name = "reviewId") Integer reviewId,
						@ModelAttribute @Validated ReviewEditForm reviewEditForm,
						BindingResult bindingResult,
						@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
						RedirectAttributes redirectAttributes,
						Model model) 
	{
		Optional<House> optionalHouse = houseService.findHouseById(houseId);
		Optional<Review> optionalReview = reviewService.findReviewById(reviewId);
		
		if(optionalHouse.isEmpty() || optionalReview.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "指定されたページが見つかりません。");
			
			return "redirect:/houses";
		}
		
		House house = optionalHouse.get();
		Review review = optionalReview.get();
		User user = userDetailsImpl.getUser();
		
		if(!review.getHouse().getId().equals(house.getId()) || !review.getUser().getId().equals(user.getId())) {
			redirectAttributes.addFlashAttribute("errorMessage", "不正なアクセスです。");
			
			return "redirect:/houses/{houseId}";
		}
		
		
		if(bindingResult.hasErrors()) {
			//modelに入れてeditに返す。
			model.addAttribute("house", house);
			model.addAttribute("review", review);
			model.addAttribute("reviewEditForm" ,reviewEditForm);
			
			return "reviews/edit";
		}
		
		
		reviewService.updateReview(reviewEditForm, review);
		redirectAttributes.addFlashAttribute("successMessage", "レビューを編集しました。");
		
		return "redirect:/houses/{houseId}";
	}
	
	//6 deleteReview()メソッドを用いて、指定されたレビューを削除する。
	@PostMapping("/{reviewId}/delete")
	public String delete(@PathVariable(name = "houseId") Integer houseId,
						@PathVariable(name = "reviewId") Integer reviewId,
						@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
						RedirectAttributes redirectAttributes)
	{
		Optional<House> optionalHouse = houseService.findHouseById(houseId);
		Optional<Review> optionalReview = reviewService.findReviewById(reviewId);
		
		if(optionalHouse.isEmpty() || optionalReview.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "指定されたページが見つかりません。");
			
			return "redirect:/houses";
		}
		
		House house = optionalHouse.get();
		Review review = optionalReview.get();
		User user = userDetailsImpl.getUser();
		if(!review.getHouse().getId().equals(house.getId()) || !review.getUser().getId().equals(user.getId())) {
			redirectAttributes.addFlashAttribute("errorMessage", "不正なアクセスです。");
			
			return "redirect:/houses/{houseId}";
		}
		
		reviewService.deleteReview(review);
		redirectAttributes.addFlashAttribute("successMessage", "レビューを削除しました。");
		
		return "redirect:/houses/{houseId}";
	}
}
