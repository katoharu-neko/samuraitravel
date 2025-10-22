// fullcalendar.js（置換版）
document.addEventListener('DOMContentLoaded', function () {
  const calendarEl = document.getElementById('calendar');
  if (!calendarEl) return;

  const form    = document.getElementById('reservationForm');
  const display = document.getElementById('fromCheckinDateToCheckoutDate');

  const calendar = new FullCalendar.Calendar(calendarEl, {
    locale: 'ja',
    initialView: 'dayGridMonth',
    height: 'auto',
    firstDay: 0,
    selectable: true,
    selectLongPressDelay: 0,
    selectMirror: true,

    // ★予約イベント取得（サーバー側で "type": booked|mine を付与）
    events: `/houses/${houseId}/calendar-events`,

    // ★見た目クラス付与
    eventClassNames: function (arg) {
      const t = arg.event.extendedProps && arg.event.extendedProps.type;
      if (t === 'booked') return ['st-booked-event']; // 満室（他人）
      if (t === 'mine')   return ['st-mine-event'];   // 自分の予約
      return [];
    },

    // ★各日コマにラベルを必ず描く（連泊でも毎日「満室/予約済」を表示）
    //   FullCalendar v6 は dayGrid の各セグメントごとに eventContent が呼ばれるので、
    //   isStart/isEnd を見ず、常に文字を返す。
    eventContent: function (arg) {
      const t = arg.event.extendedProps && arg.event.extendedProps.type;
      if (t === 'booked') {
        // 満室（他人）：日コマ全体を覆う帯の中央に「満室」
        return { html: '<div class="st-fullcell-label">満室</div>' };
      }
      if (t === 'mine') {
        // 自分の予約：標準ブルー系に「予約済」
        return { html: '<div class="st-minecell-label">予約済</div>' };
      }
      return { domNodes: [] };
    },

    // ★「満室」イベントはクリック不可（自分の予約はクリックしても何もしない）
    eventClick: function (info) {
      const t = info.event.extendedProps && info.event.extendedProps.type;
      if (t === 'booked' || t === 'mine') {
        info.jsEvent.preventDefault();
        return false;
      }
    },

    // ★重なり選択の禁止（他人予約・自分予約どちらとも）
    selectAllow: function (selectInfo) {
      const selStart = selectInfo.start;
      const selEnd   = selectInfo.end;
      const events   = calendar.getEvents();

      for (const ev of events) {
        const t = ev.extendedProps && ev.extendedProps.type;
        if (t === 'booked' || t === 'mine') {
          // overlap: selStart < ev.end && selEnd > ev.start
          if (selStart < ev.end && selEnd > ev.start) return false;
        }
      }
      return true;
    },

    // ★選択→既存フォームへ日付を入れて送信（旧flatpickrと同じ挙動）
    select: function (info) {
      const checkin  = info.startStr; // YYYY-MM-DD
      const checkout = info.endStr;   // （排他的）

      // 人数が未入力なら先に入力を促す
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

      form.submit(); // POST /houses/{id}/reservations/input
    }
  });

  calendar.render();
});
