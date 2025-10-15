// window.fullcalendar.js
document.addEventListener('DOMContentLoaded', function () {
  // houseId はテンプレで埋め込み済み
  const calendarEl = document.getElementById('calendar');
  if (!calendarEl) return;

  const form = document.getElementById('reservationForm');
  const display = document.getElementById('fromCheckinDateToCheckoutDate');

  const calendar = new FullCalendar.Calendar(calendarEl, {
    locale: 'ja',
    initialView: 'dayGridMonth',
    height: 'auto',
    firstDay: 0,
    selectable: true,
    selectLongPressDelay: 0,
    selectMirror: true,

    // 予約イベント
    events: `/houses/${houseId}/calendar-events`,

    // 満室見た目＆クリック不可
    eventClassNames: (arg) => (arg.event.extendedProps?.type === 'booked' ? ['booked-event'] : []),
    eventContent: (arg) => (arg.event.extendedProps?.type === 'booked'
      ? { html: '<div class="booked-label">満室</div>' }
      : { domNodes: [] }),
    eventClick: (info) => {
      if (info.event.extendedProps?.type === 'booked') {
        info.jsEvent.preventDefault();
        return false;
      }
    },

    // 予約済みと重なる選択は不可
    selectAllow: function (selectInfo) {
      const selStart = selectInfo.start;
      const selEnd = selectInfo.end;
      for (const ev of calendar.getEvents()) {
        if (ev.extendedProps?.type === 'booked') {
          if (selStart < ev.end && selEnd > ev.start) return false;
        }
      }
      return true;
    },

    // 範囲選択時の動作
    select: function (info) {
      const checkin = info.startStr;  // 2025-10-20
      const checkout = info.endStr;   // (排他的) → そのままサーバに渡す

      // 先に人数チェック
      const people = form.querySelector('input[name="numberOfPeople"]');
      if (!people || !people.value) {
        alert('先に人数を入力してください。');
        calendar.unselect();
        return;
      }

      // hiddenへ反映
      form.querySelector('input[name="checkinDate"]').value = checkin;
      form.querySelector('input[name="checkoutDate"]').value = checkout;
      if (display) display.value = `${checkin} 〜 ${checkout}`;

      // 旧flatpickr相当の挙動：確認してそのまま送信
      if (confirm(`${checkin} から ${checkout} の日程で予約しますか？`)) {
        form.submit();
      } else {
        calendar.unselect();
      }
    }
  });

  calendar.render();
});
