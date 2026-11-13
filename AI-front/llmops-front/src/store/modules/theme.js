let name = "default";
if (window.globalConfig.theme) name = window.globalConfig.theme;
if (window.globalConfig.showChangeTheme) {
  const tmp = localStorage.getItem("theme");
  if (themeExist(tmp)) {
    name = tmp;
  }
}

function themeExist(name) {
  if (!name) return false;
  return window.globalConfig.themes.some(item => item.name == name);
}

export default {
  namespaced: true,
  state: {
    name,
    siteMenuMode: localStorage.getItem("siteMenuMode") || window.globalConfig.siteMenuMode || "vertical" // vertical / horizontal
  },
  mutations: {
    setTheme(state, payload) {
      if (!themeExist(payload)) return;

      state.name = payload;
      document
        .getElementById("theme-css")
        .setAttribute("href", "themes/" + payload + "/index.css");
    },
    switchSiteMenuMode(state) {
      state.siteMenuMode =
        state.siteMenuMode == "vertical" ? "horizontal" : "vertical";
      localStorage.setItem("siteMenuMode", state.siteMenuMode);
    }
  }
};
