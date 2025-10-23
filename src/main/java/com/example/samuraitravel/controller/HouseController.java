package com.example.samuraitravel.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;            // ★追加
import org.springframework.data.domain.PageRequest;      // ★追加
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat; // ★追加
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.samuraitravel.entity.Favorite;
import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.entity.Review;
import com.example.samuraitravel.entity.User;
import com.example.samuraitravel.form.ReservationInputForm;
import com.example.samuraitravel.security.UserDetailsImpl;
import com.example.samuraitravel.service.FavoriteService;
import com.example.samuraitravel.service.HouseService;
import com.example.samuraitravel.service.ReservationService; // ★追加
import com.example.samuraitravel.service.ReviewService;

//59 会員用の民宿一覧ページ
@Controller
@RequestMapping("/houses")
public class HouseController {
    private final HouseService houseService;
    private final ReviewService reviewService;
    private final FavoriteService favoriteService;
    private final ReservationService reservationService; // ★追加

    // 依存性の注入
    public HouseController(HouseService houseService,
                           ReviewService reviewService,
                           FavoriteService favoriteService,
                           ReservationService reservationService // ★追加
    ) {
        this.houseService = houseService;
        this.reviewService = reviewService;
        this.favoriteService = favoriteService;
        this.reservationService = reservationService; // ★追加
    }

    @GetMapping
    public String index(@RequestParam(name = "keyword", required = false) String keyword,
                        @RequestParam(name = "area", required = false) String area,
                        @RequestParam(name = "price", required = false) Integer price,
                        @RequestParam(name = "order", required = false) String order,
                        // ★追加: 日程絞り込み（ISO 形式で受ける）
                        @RequestParam(name = "checkinDate", required = false)
                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkinDate,
                        @RequestParam(name = "checkoutDate", required = false)
                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkoutDate,
                        @PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable,
                        Model model) {

        // まずは「既存条件のみ」で Page を作る（従来どおり）
        Page<House> basePage;
        if (keyword != null && !keyword.isEmpty()) {
            if ("priceAsc".equals(order)) {
                basePage = houseService.findHousesByNameLikeOrAddressLikeOrderByPriceAsc(keyword, keyword, pageable);
            } else {
                basePage = houseService.findHousesByNameLikeOrAddressLikeOrderByCreatedAtDesc(keyword, keyword, pageable);
            }
        } else if (area != null && !area.isEmpty()) {
            if ("priceAsc".equals(order)) {
                basePage = houseService.findHousesByAddressLikeOrderByPriceAsc(area, pageable);
            } else {
                basePage = houseService.findHousesByAddressLikeOrderByCreatedAtDesc(area, pageable);
            }
        } else if (price != null) {
            if ("priceAsc".equals(order)) {
                basePage = houseService.findHousesByPriceLessThanEqualOrderByPriceAsc(price, pageable);
            } else {
                basePage = houseService.findHousesByPriceLessThanEqualOrderByCreatedAtDesc(price, pageable);
            }
        } else {
            if ("priceAsc".equals(order)) {
                basePage = houseService.findAllHousesByOrderByPriceAsc(pageable);
            } else {
                basePage = houseService.findAllHousesByOrderByCreatedAtDesc(pageable);
            }
        }

        // ★追加: 日程が指定されていなければ従来の結果をそのまま表示
        boolean hasDateRange = (checkinDate != null && checkoutDate != null && checkinDate.isBefore(checkoutDate));
        if (!hasDateRange) {
            model.addAttribute("housePage", basePage);
            model.addAttribute("keyword", keyword);
            model.addAttribute("area", area);
            model.addAttribute("price", price);
            model.addAttribute("order", order);
            // テンプレート側で引き継げるよう日程も渡しておく（null 可）
            model.addAttribute("checkinDate", checkinDate);
            model.addAttribute("checkoutDate", checkoutDate);
            return "houses/index";
        }

        // ★追加: 日程が指定されている場合は “全件” を同条件で取得 → 空室判定で絞り込み → 手動でページング
        // 既存のサービスは Pageable 前提なので、十分大きな PageRequest で 1 ページ取得して全件相当を確保します。
        // （件数が非常に多い場合は専用のリポジトリ/クエリを用意するのがベター）
        int BIG_SIZE = 10_000; // 安全側の上限
        PageRequest big = PageRequest.of(0, BIG_SIZE, pageable.getSort());

        List<House> allByBaseCondition;
        if (keyword != null && !keyword.isEmpty()) {
            if ("priceAsc".equals(order)) {
                allByBaseCondition = houseService
                        .findHousesByNameLikeOrAddressLikeOrderByPriceAsc(keyword, keyword, big)
                        .getContent();
            } else {
                allByBaseCondition = houseService
                        .findHousesByNameLikeOrAddressLikeOrderByCreatedAtDesc(keyword, keyword, big)
                        .getContent();
            }
        } else if (area != null && !area.isEmpty()) {
            if ("priceAsc".equals(order)) {
                allByBaseCondition = houseService
                        .findHousesByAddressLikeOrderByPriceAsc(area, big)
                        .getContent();
            } else {
                allByBaseCondition = houseService
                        .findHousesByAddressLikeOrderByCreatedAtDesc(area, big)
                        .getContent();
            }
        } else if (price != null) {
            if ("priceAsc".equals(order)) {
                allByBaseCondition = houseService
                        .findHousesByPriceLessThanEqualOrderByPriceAsc(price, big)
                        .getContent();
            } else {
                allByBaseCondition = houseService
                        .findHousesByPriceLessThanEqualOrderByCreatedAtDesc(price, big)
                        .getContent();
            }
        } else {
            if ("priceAsc".equals(order)) {
                allByBaseCondition = houseService
                        .findAllHousesByOrderByPriceAsc(big)
                        .getContent();
            } else {
                allByBaseCondition = houseService
                        .findAllHousesByOrderByCreatedAtDesc(big)
                        .getContent();
            }
        }

        // ★追加: 空室チェックで絞り込み
        List<House> available = new ArrayList<>();
        for (House h : allByBaseCondition) {
            if (reservationService.isAvailable(h.getId(), checkinDate, checkoutDate)) {
                available.add(h);
            }
        }

        // ★追加: 手動でページング（現在のページ番号・サイズを尊重）
        int pageNumber = pageable.getPageNumber();
        int pageSize   = pageable.getPageSize();
        int fromIndex  = Math.min(pageNumber * pageSize, available.size());
        int toIndex    = Math.min(fromIndex + pageSize, available.size());
        List<House> pageContent = available.subList(fromIndex, toIndex);

        Page<House> filteredPage = new PageImpl<>(pageContent, pageable, available.size());

        // ★追加: 従来と同じモデル名で戻す（画面はそのまま）
        model.addAttribute("housePage", filteredPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("area", area);
        model.addAttribute("price", price);
        model.addAttribute("order", order);
        model.addAttribute("checkinDate", checkinDate);
        model.addAttribute("checkoutDate", checkoutDate);

        return "houses/index";
    }

    @GetMapping("/{id}")
    public String show(@PathVariable(name = "id") Integer id,
                       @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
                       RedirectAttributes redirectAttributes,
                       Model model) {
        Optional<House> optionalHouse = houseService.findHouseById(id);
        if (optionalHouse.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "民宿が存在しません。");
            return "redirect:/houses";
        }

        House house = optionalHouse.get();

        boolean hasUserAlreadyReviewed = false;
        Favorite favorite = null;
        boolean isFavorite = false;

        if (userDetailsImpl != null) {
            User user = userDetailsImpl.getUser();
            hasUserAlreadyReviewed = reviewService.hasUserAlreadyReviewed(house, user);
            isFavorite = favoriteService.isFavorite(house, user);
            if (isFavorite) {
                favorite = favoriteService.findFavoriteByHouseAndUser(house, user);
            }
        }

        List<Review> newReviews = reviewService.findTop6ReviewsByHouseOrderByCreatedAtDesc(house);
        long totalReviewCount = reviewService.countReviewsByHouse(house);

        model.addAttribute("house", house);
        model.addAttribute("reservationInputForm", new ReservationInputForm());
        model.addAttribute("hasUserAlreadyReviewed", hasUserAlreadyReviewed);
        model.addAttribute("newReviews", newReviews);
        model.addAttribute("totalReviewCount", totalReviewCount);
        model.addAttribute("favorite", favorite);
        model.addAttribute("isFavorite", isFavorite);

        return "houses/show";
    }
}
