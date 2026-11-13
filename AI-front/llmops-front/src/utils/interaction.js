// 一段时间无鼠标键盘交互事件
export class NoEvent {
  /**
   * 构造
   * @param {*} callback 回调
   * @param {*} millisecond 计时（毫秒）
   * @param {*} eventTarget 事件对象
   * @param {*} eventNames 监听事件
   */
  constructor(callback, millisecond, eventTarget = document, eventNames = ["mousemove", "scroll", "mousedown", "mouseup", "keydown", "keyup"]) {
    this.noEventTimer = null;
    this.callback = callback;
    this.millisecond = millisecond;
    this.eventTarget = eventTarget;
    this.eventNames = eventNames;
  }

  on() {
    this.addListenners();
    this.startTimer();
  }

  off() {
    this.removeListenners();
    this.stopTimer();
  }

  addListenners() {
    this.eventNames.forEach(eventName => this.eventTarget.addEventListener(eventName, this.restartTimer));
  }

  removeListenners() {
    this.eventNames.forEach(eventName => this.eventTarget.removeEventListener(eventName, this.restartTimer));
  }

  restartTimer = () => {
    this.stopTimer();
    this.startTimer();
  }

  startTimer() {
    this.noEventTimer = setTimeout(this.callback, this.millisecond);
  }

  stopTimer() {
    clearTimeout(this.noEventTimer);
    this.noEventTimer = null;
  }
}

