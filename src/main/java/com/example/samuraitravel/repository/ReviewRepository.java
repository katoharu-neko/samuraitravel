package com.example.samuraitravel.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.entity.Review;
import com.example.samuraitravel.entity.User;

public interface ReviewRepository extends JpaRepository<Review, Integer>{
	
	//特定の民宿に対する最新のレビュー6件を取得し、リスト形式で出力する。
	public List<Review> findTop6ByHouseOrderByCreatedAtDesc(House house); //特定の民宿に関してなのでHouseを引数に受け取る必要がある。
	
	//特定の民宿とユーザーに紐づくレビューを取得し、該当エンティティを出力する。
	public Review findByHouseAndUser(House house, User user);//Review型HouseとUserを引数に受け取る必要がある。
	
	//特定の民宿に対するレビュー総数をカウントし、その結果を出力する。
	public long countByHouse(House house);//Long型で十分　Hosueを引数で受け取る必要がある。
	
	//特定の民宿に関するレビューを作成日時の降順で取得し、ページネーション形式で出力する。
	public Page<Review> findByHouseOrderByCreatedAtDesc(House house, Pageable pageable); //特定の民宿なのでHouseを引数で受け取る
	

}
