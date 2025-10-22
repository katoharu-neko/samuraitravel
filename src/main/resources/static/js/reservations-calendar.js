// 予約一覧ページの FullCalendar（閲覧専用）
document.addEventListener('DOMContentLoaded', function () {
  const calEl = document.getElementById('myReservationsCalendar');
  if (!calEl) return;

  const calendar = new FullCalendar.Calendar(calEl, {
    locale: 'ja',
    initialView: 'dayGridMonth',
    height: 'auto',
    firstDay: 0,
    selectable: false,
    editable: false,
    eventStartEditable: false,
    eventDurationEditable: false,
    dragScroll: false,

    // 自分の予約だけを返すエンドポイント（◎ご要望③）
    events: `/reservations/calendar-events`,

    eventClassNames: function (arg) {
      // 自分の予約を「mine」表示で流用
      return ['st-mine-event'];
    },

    // 連泊でも各日セルにラベルを描く。タイトルは「民宿名（n人）」を表示。
    eventContent: function (arg) {
      const title = arg.event.title || '予約';
      return { html: `<div class="st-minecell-label">${title}</div>` };
    },

    // 完全閲覧専用
    eventClick: function (info) {
      info.jsEvent.preventDefault();
      return false;
    }
  });

  calendar.render();
});
