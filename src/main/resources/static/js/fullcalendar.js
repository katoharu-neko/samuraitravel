// 民宿詳細ページの FullCalendar（予約可）
document.addEventListener('DOMContentLoaded', function () {
  const calendarEl = document.getElementById('calendar');
  if (!calendarEl) return;

  const form    = document.getElementById('reservationForm');
  const display = document.getElementById('fromCheckinDateToCheckoutDate');

  // 当日(ローカル)の 00:00:00
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const tomorrow = new Date(today);
  tomorrow.setDate(tomorrow.getDate() + 1);

  const calendar = new FullCalendar.Calendar(calendarEl, {
    locale: 'ja',
    initialView: 'dayGridMonth',
    height: 'auto',
    firstDay: 0,
    selectable: true,
    selectLongPressDelay: 0,
    selectMirror: true,

    // 予約イベント取得（type: booked|mine をサーバで付与）
    events: `/houses/${houseId}/calendar-events`,

    // --- 表示クラス ---
    eventClassNames: function (arg) {
      const t = arg.event.extendedProps && arg.event.extendedProps.type;
      if (t === 'booked') return ['st-booked-event']; // 満室（他人）
      if (t === 'mine')   return ['st-mine-event'];   // 自分の予約
      return [];
    },

    // --- ラベル（連泊すべての日に表示） ---
    eventContent: function (arg) {
      const t = arg.event.extendedProps && arg.event.extendedProps.type;
      if (t === 'booked') return { html: '<div class="st-fullcell-label">満室</div>' };
      if (t === 'mine')   return { html: '<div class="st-minecell-label">予約済</div>' };
      return { domNodes: [] };
    },

    // --- 日セルの装飾：当日含む過去に陰影（◎ご要望①） ---
    dayCellClassNames: function (arg) {
      const cellDate = new Date(arg.date.getFullYear(), arg.date.getMonth(), arg.date.getDate());
      if (cellDate.getTime() <= today.getTime()) {
        return ['st-past-day'];
      }
      return [];
    },

    // --- イベントクリック抑止 ---
    eventClick: function (info) {
      const t = info.event.extendedProps && info.event.extendedProps.type;
      if (t === 'booked' || t === 'mine') {
        info.jsEvent.preventDefault();
        return false;
      }
    },

    // --- 選択可否：予約と重なる場合は不可、さらに過去/当日開始も不可 ---
    selectAllow: function (selectInfo) {
      const selStart = selectInfo.start;
      const selEnd   = selectInfo.end;
      const events   = calendar.getEvents();

      // 当日含む過去からの開始は不可
      if (selStart.getTime() < tomorrow.getTime()) return false;

      for (const ev of events) {
        const t = ev.extendedProps && ev.extendedProps.type;
        if (t === 'booked' || t === 'mine') {
          if (selStart < ev.end && selEnd > ev.start) return false;
        }
      }
      return true;
    },

    // --- 選択時の最終バリデーションと送信 ---
    select: function (info) {
      const checkin  = info.startStr; // YYYY-MM-DD
      const checkout = info.endStr;

      // ① 当日含む過去はバリデーションで弾く（ユーザーに明示）
      if (info.start.getTime() < tomorrow.getTime()) {
        alert('当日を含む過去の日程は選択できません。');
        calendar.unselect();
        return;
      }

      // 人数チェック（未入力なら促す）
      const peopleInput = form.querySelector('input[name="numberOfPeople"]');
      if (!peopleInput || !peopleInput.value) {
        alert('先に人数を入力してください。');
        calendar.unselect();
        return;
      }

      if (!confirm(`${checkin} から ${checkout} の日程で予約しますか？`)) {
        calendar.unselect();
        return;
      }

      form.querySelector('input[name="checkinDate"]').value  = checkin;
      form.querySelector('input[name="checkoutDate"]').value = checkout;
      if (display) display.value = `${checkin} 〜 ${checkout}`;

      form.submit(); // 既存の /houses/{id}/reservations/input
    }
  });

  calendar.render();
});
