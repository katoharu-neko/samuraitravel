//74 会員用の民宿詳細ページに予約フォームを作る
//３ヶ月後の日付を代入
let maxDate = new Date();
maxDate = maxDate.setMonth(maxDate.getMonth() + 3);
//id属性に"fromCheckinDateToCheckoutDate"が設定されたHTML要素（入力フォーム）に対し、Flatpickrのインスタンスを生成
flatpickr('#fromCheckinDateToCheckoutDate', {
	//カレンダーのモード
	mode: "range",
	//カレンダーの言語
	locale: 'ja',
	//最小の日付
	minDate: 'today',
	//最大の日付
	maxDate: maxDate,
	//onClose カレンダーを閉じたときの処理
	onClose: function(selectedDates, dateStr, instance) {
		//文字列をチェックイン日とアウト日で分割
		const dates = dateStr.split(" から ");
		if (dates.length === 2) {
			document.querySelector("input[name='checkinDate']").value = dates[0];
			document.querySelector("input[name='checkoutDate']").value = dates[1];
		} else {
			document.querySelector("input[name='checkinDate']").value = '';
			document.querySelector("input[name='checkoutDate']").value = '';
		}
	}
});
