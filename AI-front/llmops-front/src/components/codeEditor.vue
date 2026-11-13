<template>
  <div style="line-height:initial">
    <CodeEditor ref="codeEditor" v-model="content" :options="cmOptions" />
    <div class="text-color-3" v-if="showTip">
      <span>处于编辑状态时按下F11可进入全屏编辑,按下ESC可退出全屏编辑</span>
      <!-- <span>Ctrl+F可进行搜索、Ctrl+R可进行替换</span> -->
    </div>
  </div>
</template>

<script>
import Vue from "vue";
import { codemirror } from "vue-codemirror";
import "codemirror/lib/codemirror.css";

import "codemirror/mode/css/css";
import "codemirror/theme/blackboard.css"; //编辑器主题样式
import "codemirror/theme/monokai.css"; //编辑器主题样式

import "codemirror/mode/javascript/javascript.js";
import "codemirror/mode/yaml/yaml.js";
import "codemirror/mode/properties/properties.js";
import "codemirror/mode/htmlmixed/htmlmixed.js";
import "codemirror/mode/xml/xml.js";
import "codemirror/mode/dockerfile/dockerfile.js";
import "codemirror/mode/htmlmixed/htmlmixed.js";

import "codemirror/addon/selection/active-line"; //光标行背景高亮，配置里面也需要styleActiveLine设置为true
import "codemirror/keymap/sublime"; //sublime编辑器效果
import "codemirror/addon/display/autorefresh"; //及时自动更新，配置里面也需要设置autoRefresh为true

import "codemirror/addon/display/fullscreen.js";
import "codemirror/addon/display/fullscreen.css";

import "codemirror/addon/dialog/dialog.js";
import "codemirror/addon/search/search.js";
import "codemirror/addon/search/searchcursor.js";
import "codemirror/addon/scroll/annotatescrollbar.js";
import "codemirror/addon/search/matchesonscrollbar.js";
import "codemirror/addon/search/jump-to-line.js";

import "codemirror/addon/dialog/dialog.css";
import "codemirror/addon/search/matchesonscrollbar.css"
export default {
  components: {
    CodeEditor: codemirror
  },

  props: {
    options: Object,
    value: String,
    fullSeccernShowSave: Boolean,
    showTip: {
      type: Boolean,
      default: true
    },
    showSearch: {
      type: Boolean,
      default: true
    }
  },

  model: {
    prop: "value",
    event: "change"
  },

  watch: {
    value: {
      handler(val = "") {
        this.content = val;
      },
      immediate: true
    },
    content: {
      handler(val) {
        this.$emit("change", val);
      }
    }
  },

  computed: {
    codemirror() {
      return this.$refs.codeEditor && this.$refs.codeEditor.codemirror;
    },

    cmOptions() {
      return {
        ...this.oriOptions,
        ...this.options
      };
    }
  },

  data() {
    return {
      content: "",
      oriOptions: {
        tabSize: 4,
        theme: "blackboard",
        lineNumbers: true,
        line: true,
        lineWrapping: true,
        gutters: [],
        styleActiveLine: true,
        autoRefresh: true,
        extraKeys: {
          F11: cm => {
            const flag = !cm.getOption("fullScreen");
            cm.setOption("fullScreen", flag);
            this.bottomWrapperSetData({
              showSaveBtn: flag && this.fullSeccernShowSave
            });
          },
          Esc: this.handleOutFullScreen,
          "Ctrl-F": this.showSearchMode
        }
        // mode: null
      },
      bottomWrapper: null,
      replaceMode: null,
      showSave: true
    };
  },

  methods: {
    find(options) {
      this.codemirror.findReplaceOptions = options;
      this.codemirror.execCommand("findPersistent");
    },
    findAll(options, callback) {
      this.codemirror.findReplaceOptions = options;
      this.codemirror.execCommand("findPersistent");
      callback &&
        callback(this.codemirror.state.search.annotate.matches.length);
    },
    findPrevious(options) {
      this.codemirror.findReplaceOptions = options;
      this.codemirror.execCommand("findPrev");
    },
    findNext(options) {
      this.codemirror.findReplaceOptions = options;
      this.codemirror.execCommand("findNext");
    },
    replaceOne(options) {
      this.codemirror.findReplaceOptions = options;
      this.codemirror.execCommand("replace");
    },
    replaceAll(options) {
      this.codemirror.findReplaceOptions = options;
      this.codemirror.execCommand("replaceAll");
    },
    clear() {
      if (this.codemirror.findReplaceOptions) {
        delete this.codemirror.findReplaceOptions;
      }
      this.codemirror.execCommand("clearSearch");
    },
    refresh() {
      this.codemirror.refresh();
    },

    injectReplcaeMode() {
      const replaceWrapper = document.createElement("DIV");
      replaceWrapper.id = "replaceWrapper";
      const node = document.createElement("DIV");
      node.id = "replaceContainer";
      replaceWrapper.appendChild(node);
      this.wrapper.appendChild(replaceWrapper);

      // this.$nextTick(() => {
      //   if (!this.showSearch) return;
      //   this.replaceMode = new Vue({
      //     render(h) {
      //       return (
      //         <FindReplace
      //           on-find={this.find}
      //           on-findAll={this.findAll}
      //           on-findPrevious={this.findPrevious}
      //           on-findNext={this.findNext}
      //           on-replaceOne={this.replaceOne}
      //           on-replaceAll={this.replaceAll}
      //           on-clear={this.clear}
      //           ref="replaceMode"
      //         ></FindReplace>
      //       );
      //     },
      //     methods: {
      //       find: this.find,
      //       findAll: this.findAll,
      //       findPrevious: this.findPrevious,
      //       findNext: this.findNext,
      //       replaceOne: this.replaceOne,
      //       replaceAll: this.replaceAll,
      //       clear: this.clear,
      //       showFind(flag) {
      //         this.$refs.replaceMode.showFind = flag;
      //       }
      //     }
      //   }).$mount("#replaceContainer");
      // });
    },

    injectBottomMode() {
      const replaceWrapper = document.createElement("DIV");
      replaceWrapper.id = "bottomWrapper";
      const node = document.createElement("DIV");
      node.id = "bottomContainer";
      replaceWrapper.appendChild(node);
      this.wrapper.appendChild(replaceWrapper);

      this.$nextTick(() => {
        this.bottomWrapper = new Vue({
          data() {
            return {
              showSaveBtn: false
            };
          },
          render(h) {
            return (
              <div>
                {this.showSaveBtn ? (
                  <el-button type="primary" on-click={this.handleSave}>
                    保存
                  </el-button>
                ) : null}
              </div>
            );
          },
          methods: {
            handleSave: this.handleSave
          }
        }).$mount("#bottomContainer");
      });
    },

    handleOutFullScreen(e) {
      if (this.codemirror.getOption("fullScreen"))
        this.codemirror.setOption("fullScreen", false);

      this.bottomWrapperSetData({ showSaveBtn: false });
    },

    bottomWrapperSetData(data = {}) {
      if (!this.bottomWrapper) return;
      for (const [key, value] of Object.entries(data)) {
        this.bottomWrapper.$set(this.bottomWrapper, key, value);
      }
    },

    showSearchMode() {
      this.replaceMode.showFind(true);
    },

    handleSave() {
      this.handleOutFullScreen();
      this.$emit("save");
    }
  },
  mounted() {
    this.wrapper = this.codemirror.getWrapperElement();
    this.injectReplcaeMode();
    this.injectBottomMode();
  }
};
</script>

<style>
#replaceWrapper {
  position: absolute;
  top: 0;
  right: 10px;
  z-index: 10;
}

#bottomWrapper {
  position: absolute;
  bottom: 20px;
  right: 10px;
  z-index: 999;
}

.CodeMirror-selected {
  background: #eee !important;
}

.CodeMirror.CodeMirror-fullscreen {
  z-index: 999999;
  height: 100vh !important;
}
</style>
