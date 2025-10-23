// 民宿一覧ページ：サイドバーの「日程で探す」カレンダー
document.addEventListener('DOMContentLoaded', function () {
  const calEl = document.getElementById('availabilityCalendar');
  if (!calEl) return;

  // 送信用フォーム
  const form = document.getElementById('availabilitySearchForm');
  const inputCheckin = document.getElementById('avCheckin');
  const inputCheckout = document.getElementById('avCheckout');
  const msg = document.getElementById('availabilityMessage');

  const calendar = new FullCalendar.Calendar(calEl, {
    locale: 'ja',
    initialView: 'dayGridMonth',
    height: 'auto',
    firstDay: 0,
    selectable: true,
    selectMirror: true,
    selectLongPressDelay: 0,

    // 当日を含む過去の選択を禁止（UIレベル）
    selectAllow: function (selectInfo) {
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      const selStart = new Date(selectInfo.start.getTime());
      selStart.setHours(0, 0, 0, 0);
      return selStart > today;
    },

    // 選択したらそのまま /houses に GET 遷移（通常の結果描画へ委譲）
    select: function (info) {
      const checkin = info.startStr;  // 例: 2025-11-01
      const checkout = info.endStr;   // FullCalendar は end 排他

      // プレビュー表示
      if (msg) {
        msg.textContent = `選択中: ${checkin} 〜 ${checkout}`;
      }

      // hidden に詰めてサブミット
      if (inputCheckin && inputCheckout && form) {
        inputCheckin.value = checkin;
        inputCheckout.value = checkout;
        form.submit();
      }
    }
  });

  calendar.render();
});
