// 自分の予約一覧ページ（/reservations）用：表示専用カレンダー
document.addEventListener('DOMContentLoaded', function () {
  const el = document.getElementById('myReservationCalendar');
  if (!el) return;

  const calendar = new FullCalendar.Calendar(el, {
    locale: 'ja',
    initialView: 'dayGridMonth',
    height: 'auto',
    firstDay: 0,          // 日曜はじまり（必要なら変更）
    selectable: false,    // 表示専用
    editable: false,      // ドラッグ等も不可
    eventStartEditable: false,
    eventDurationEditable: false,
    dayMaxEvents: true,   // 枠溢れ時の「+n件」

    // 予約イベント（自分の予約のみ）
    events: '/reservations/calendar-events',

    // すべてのイベントにクラスを付けて“ピル表示”のCSSを当てる
    eventClassNames: function () {
      return ['mine-event'];
    },

    // ブロック表示にする（高さが確保され、テキストと帯が重ならない）
    eventDisplay: 'block',

    // クリックしても何もしない
    eventClick: function (info) {
      info.jsEvent.preventDefault();
      return false;
    }
  });

  calendar.render();
});
