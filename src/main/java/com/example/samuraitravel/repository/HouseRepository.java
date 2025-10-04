package com.example.samuraitravel.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.samuraitravel.entity.House;

public interface HouseRepository extends JpaRepository<House, Integer>{
	public Page<House> findByNameLike(String keyword, Pageable pageable);
	public House findFirstByOrderByIdDesc();
	
	//59会員用の民宿一覧ページを作る
	//public Page<House> findByNameLikeOrAddressLike(String nameKeyword, String addressKeyword, Pageable pageable);
	//public Page<House> findByAddressLike(String area, Pageable pageable);
	//public Page<House> findByPriceLessThanEqual(Integer price, Pageable pageable);
	
	//62 会員用の民宿一覧ページ並べ替え機能　リポジトリを変更
	//OrderByを使う
	//~~Ascは昇順
	public Page<House> findByNameLikeOrAddressLikeOrderByCreatedAtDesc(String nameKeyword, String addressKeyword, Pageable pageable);
	//~~Descは降順
	public Page<House> findByNameLikeOrAddressLikeOrderByPriceAsc(String nameKeyword, String addressKeyword, Pageable pageable);
	public Page<House> findByAddressLikeOrderByCreatedAtDesc(String area, Pageable pageable);
	public Page<House> findByAddressLikeOrderByPriceAsc(String area, Pageable pageable);
	public Page<House> findByPriceLessThanEqualOrderByCreatedAtDesc(Integer price, Pageable pageable);
	public Page<House> findByPriceLessThanEqualOrderByPriceAsc(Integer price, Pageable pageable);
	public Page<House> findAllByOrderByCreatedAtDesc(Pageable pageable);
	public Page<House> findAllByOrderByPriceAsc(Pageable pageable);
	
	//63トップページを作成しよう
	public List<House> findTop8ByOrderByCreatedAtDesc();//Top~~でLimitと同じ
	
	//89 デザインを整えよう（バックエンド）
	//QueryアノテーションJPQL文を渡すことでキーワードを使っても実現できない複雑なクエリを実現できる。
	@Query("SELECT h FROM House h LEFT JOIN h.reservations r GROUP BY h.id ORDER BY COUNT(r) DESC")
	List<House> findAllByOrderByReservationCountDesc(Pageable pageable);
	

}
