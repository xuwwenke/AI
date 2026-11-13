const GOTTY_CONFIG = "gotty_config";
class Storage {
  constructor() {
    this.init();
  }

  init() {
    const config = localStorage.getItem(GOTTY_CONFIG);
    this.data = config ? JSON.parse(config) : {};
  }

  getItem(key) {
    return this.data[key];
  }

  setItem(key, value) {
    this.data[key] = value;
    localStorage.setItem(GOTTY_CONFIG, JSON.stringify(this.data));
  }
}

export default new Storage();
