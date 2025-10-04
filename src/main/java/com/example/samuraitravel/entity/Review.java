package com.example.samuraitravel.entity;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "reviews")
//ゲッターセッターの生成
@Data
public class Review {
	//フィールド
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;
	
	//reviewエンティティはレビュー視点で見るため、レビューは一つの民宿に紐づく
	@ManyToOne
	@JoinColumn(name = "house_id")
	//House型
	private House house;
	
	//reviewエンティティはレビュー視点で見るため、レビューは一つのユーザーに紐づく
	@ManyToOne
	//User型
	@JoinColumn(name = "user_id")
	private User user;

	@Column(name = "content")
	private String content;
	
	@Column(name = "score")
	private Integer score;
	
	@Column(name = "created_at", insertable = false, updatable = false )
	private Timestamp createdAt;
	
	@Column(name = "updated_at", insertable = false, updatable = false )
	private Timestamp updatedAt;

}
