// @ts-ignore
import { lib } from "./hterm_module";

// @ts-ignore
import { Terminal } from "xterm";
import { WebLinksAddon } from "xterm-addon-web-links";
import { SearchAddon } from "xterm-addon-search";
import { FitAddon } from "xterm-addon-fit";

export class Xterm {
  elem: HTMLElement;
  term: Terminal;
  fitAddon: FitAddon;
  searchAddon: SearchAddon;
  resizeListener: () => void;
  decoder: lib.UTF8Decoder;

  message: HTMLElement;
  messageTimeout: number;
  messageTimer: any;
  streamFilterCache: any;
  filterWord: String;

  constructor(elem: HTMLElement, options: any, filterWord: any) {
    this.elem = elem;
    this.filterWord = filterWord;
    this.fitAddon = new FitAddon();
    this.searchAddon = new SearchAddon();
    this.term = new Terminal({
      rendererType: "canvas",
      scrollback: 5000,
      ...options
    });

    this.term.loadAddon(new WebLinksAddon());
    this.term.loadAddon(this.searchAddon);
    this.term.loadAddon(this.fitAddon);

    const doc = elem.ownerDocument || document;
    this.message = doc.createElement("div");
    this.message.className = "xterm-overlay";
    this.messageTimeout = 2000;

    this.resizeListener = () => {
      this.fitAddon.fit();
      this.term.scrollToBottom();
      this.showMessage(
        String(this.term.cols) + "x" + String(this.term.rows),
        this.messageTimeout
      );
    };

    this.streamFilterCache = [];

    this.term.open(elem);

    this.resizeListener();
    window.addEventListener("resize", () => {
      this.resizeListener();
    });

    this.decoder = new lib.UTF8Decoder();
  }

  setFontSize(fontSize: number | string) {
    this.term.setOption("fontSize", fontSize);
    this.reDraw();
  }

  reDraw() {
    try {
      this.term.refresh(0, this.term.rows - 1);

      // var ev = document.createEvent("Event");
      // ev.initEvent("resize", true, true);
      // window.dispatchEvent(ev);
      this.resizeListener();
    } catch (e) {}
  }

  setForeground(color: string) {
    this.term.setOption("theme", {
      foreground: color,
      selection: "#e38d9590"
    });
    this.reDraw();
  }

  getFontSize() {
    return this.term.getOption("fontSize") || 12;
  }

  info(): { columns: number; rows: number } {
    return { columns: this.term.cols, rows: this.term.rows };
  }

  filterOutput(data: string) {
    const infoList = data.replace("\r\n", "\r\n|%split%|").split("|%split%|");
    for (const str of infoList) {
      this.pushCache(str);
    }
  }

  pushCache(data: string) {
    this.streamFilterCache.push(data);
    if (data.endsWith("\r\n")) {
      let str = this.streamFilterCache.join("");

      const filterWord = this.filterWord.replace(/(\W)/g, "\\$1");
      const reg = new RegExp(`(${filterWord})`, "gi");

      if (reg.test(str)) {
        str = str.replace(reg, "\x1b[31m$1\x1b[0m");
        this.term.write(str);
      }
      this.streamFilterCache.length = 0;
    }
  }

  output(data: string) {
    data = this.decoder.decode(data);
    if (this.filterWord) {
      this.filterOutput(data);
      return;
    }
    this.term.write(data);
  }

  showMessage(message: string, timeout: number) {
    this.message.textContent = message;
    this.elem.appendChild(this.message);

    if (this.messageTimer) {
      clearTimeout(this.messageTimer);
    }
    if (timeout > 0) {
      this.messageTimer = setTimeout(() => {
        // @ts-ignore
        this.elem.removeChild(this.message);
      }, timeout);
    }
  }

  removeMessage(): void {
    if (this.message.parentNode == this.elem) {
      this.elem.removeChild(this.message);
    }
  }

  setWindowTitle(title: string) {
    document.title = title;
  }

  setPreferences(value: object) {}

  onInput(callback: (input: string) => void) {
    this.term.onData(data => callback(data));
  }

  onResize(callback: (colmuns: number, rows: number) => void) {
    this.term.onResize(e => callback(e.cols, e.rows));
  }

  deactivate(): void {
    this.term.blur();
  }

  reset(): void {
    this.removeMessage();
    this.term.clear();
  }

  close(): void {
    this.term.dispose();
  }

  clear(): void {
    this.term.clear();
  }
}
