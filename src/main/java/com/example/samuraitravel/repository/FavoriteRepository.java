package com.example.samuraitravel.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.samuraitravel.entity.Favorite;
import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.entity.User;

public interface FavoriteRepository extends JpaRepository<Favorite, Integer>{
	
	//特定のユーザーに対する最新のお気に入り6件を取得し、リスト形式で出力する。
	public Page<Favorite> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
	
	//特定の民宿とユーザーに紐づくお気に入りを取得し、該当エンティティを出力する。
	public Favorite findByHouseAndUser(House house, User user);

}
