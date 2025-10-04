package com.example.samuraitravel.entity;

import java.sql.Timestamp;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.ToString;

@Entity
@Table(name="houses")
//ゲッター、セッターなどを自動生成する
@Data
//69 カスケード削除を可能にする
//オブジェクトの状態を文字列に変換するメソッド　リレーションによる循環参照StackOverFlowErrorが発生してしまうことを防ぐ
@ToString(exclude = {"reservations", "reviews", "favorites"})

public class House {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "image_name")
	private String imageName;
	
	@Column(name = "description")
	private String description;
	
	@Column(name = "price")
	private Integer price;
	
	@Column(name = "capacity")
	private Integer capacity;
	
	@Column(name = "postal_code")
	private String postalCode;
	
	@Column(name = "address")
	private String address;
	
	@Column(name = "phone_number")
	private String phoneNumber;
	
	@Column(name = "created_at", insertable = false, updatable = false)
	private Timestamp createdAt;
	
	@Column(name = "updated_at", insertable = false, updatable = false)
	private Timestamp updatedAt;
	
	//69 カスケード削除
	//一対多のフィールドに@OneToManyアノテーションをつける。mappedBy属性を設定して双方向の参照ができるようにする。cascade属性を使い削除の操作が紐づいたエンティティにも適用される　
	//fetch属性を設定して相手エンティティの取得タイミングを取得する
	@OneToMany(mappedBy = "house", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
	private List<Reservation> reservations;
	
	//93 カスケード削除　レビューの削除
	@OneToMany(mappedBy = "house", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
	private List<Review> reviews;
	
	//94 カスケード削除　お気に入りの削除
	@OneToMany(mappedBy = "house", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
	private List<Favorite> favorites;
	
	

}
