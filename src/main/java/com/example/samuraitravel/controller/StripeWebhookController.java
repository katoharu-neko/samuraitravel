package com.example.samuraitravel.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.example.samuraitravel.service.StripeService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;

//87 予約情報の登録処理を変更する　コントローラー
//@Controllerから変更
@RestController
public class StripeWebhookController {
	private final StripeService stripeService;
	
	@Value("${stripe.webhook-secret}")
	private String webhookSecret;
	
	public StripeWebhookController(StripeService stripeService) {
		this.stripeService = stripeService;
	}
	
	@PostMapping("/stripe/webhook")
	public ResponseEntity<String> webhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
		Event event = null;
		
		try {
			event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
		} catch (SignatureVerificationException e) {
			System.out.println("Webhookの署名シークレットが正しくありません。");
			
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
		//決済成功イベントを受信する。
		if ("checkout.session.completed".equals(event.getType())) {
			//StripeServiceクラスに定義したprocessSessionCompleted()メソッドを呼び出しています。
			stripeService.processSessionCompleted(event);
		}
		
		return new ResponseEntity<>("Success", HttpStatus.OK);
	}

}
