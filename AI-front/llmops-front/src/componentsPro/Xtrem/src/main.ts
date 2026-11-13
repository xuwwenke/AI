import { Xterm } from "./xterm";
import { WebTTY, protocols } from "./webtty";
import { ConnectionFactory } from "./websocket";
import "xterm/css/xterm.css";

declare global {
  interface Window {
    wt: any;
  }
}

export function initTerm({
  elem,
  url,
  args,
  options,
  closeCB,
  filterWord,
  openCB
}: {
  elem: HTMLElement;
  url: string;
  args: string;
  options: any;
  closeCB?: Function;
  filterWord?: string;
  openCB?: Function;
}) {
  const term = new Xterm(elem, options, filterWord);

  // const url = (httpsEnabled ? 'wss://' : 'ws://') + '127.0.0.1:20002/ws';

  const factory = new ConnectionFactory(url, protocols);

  // let args = getArgs();

  let wt = new WebTTY(term, factory, args, "", closeCB, openCB);
  window.wt = wt;
  let closer = window.wt.open();

  wt.close = () => {
    closer();
    term.close();
    closeCB && closeCB();
  };
  // window.wt =wt
  window.addEventListener("unload", () => {
    wt.close();
  });
  return wt;
}
