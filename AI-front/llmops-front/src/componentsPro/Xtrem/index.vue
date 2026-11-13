<template>
  <div class="w100p h100p df flex-column" v-if="args">
    <div v-if="type !== 'ssh'" class="h50 df align-center justify-between shrink-0 pl20 pr20"
      style="background: rgb(58, 51, 51)">
      <div class="df align-center">
        <el-popover placement="bottom">
          <div>
            <h4>调整字体大小</h4>
            <el-input-number v-model.number="fontSize" :step="1" :min="12" :max="24"
              @change="setFontSize"></el-input-number>
          </div>
          <el-button slot="reference" type="primary">设置字体大小</el-button>
        </el-popover>
        <!-- 查找 -->
        <el-popover width="300" placement="bottom" v-model="searchOptiosn.visible" trigger="manual">
          <div>
            <el-input placeholder="请输入查找信息" v-model="searchOptiosn.text" clearable
              @keyup.enter.native="handleSearch"></el-input>
            <div class="df align-center justify-between mt10">
              <div class="df align-center">
                <el-tooltip content="向上查找" placement="bottom" effect="light">
                  <span :class="{
                    'seacrh-option-icon': true,
                    'text-primary': !searchOptiosn.findNext
                  }" @click="searchOptiosn.findNext = !searchOptiosn.findNext">
                    <span class="el-icon-back"></span>
                  </span>
                </el-tooltip>

                <el-tooltip content="向下查找" placement="bottom" effect="light">
                  <span :class="{
                    'seacrh-option-icon': true,
                    'text-primary': searchOptiosn.findNext
                  }" @click="searchOptiosn.findNext = !searchOptiosn.findNext">
                    <span class="el-icon-right"></span>
                  </span>
                </el-tooltip>

                <el-tooltip content="正则表达式" placement="bottom" effect="light">
                  <span :class="{
                    'seacrh-option-icon': true,
                    'text-primary': searchOptiosn.options.regex
                  }" @click="
                    searchOptiosn.options.regex = !searchOptiosn.options.regex
                    ">
                    <span>.*</span>
                  </span>
                </el-tooltip>

                <el-tooltip content="大小写区分" placement="bottom" effect="light">
                  <span :class="{
                    'seacrh-option-icon': true,
                    'text-primary': searchOptiosn.options.caseSensitive
                  }" @click="
                    searchOptiosn.options.caseSensitive = !searchOptiosn
                      .options.caseSensitive
                    ">
                    <span>Aa</span>
                  </span>
                </el-tooltip>

                <el-tooltip content="全字匹配" placement="bottom" effect="light">
                  <span :class="{
                    'seacrh-option-icon': true,
                    'text-primary': searchOptiosn.options.wholeWord
                  }" @click="
                    searchOptiosn.options.wholeWord = !searchOptiosn.options
                      .wholeWord
                    ">
                    <span style="text-decoration: underline">W</span>
                  </span>
                </el-tooltip>
              </div>
              <div class="df align-center">
                <el-tooltip content="是否命中" placement="bottom" effect="light">
                  <span :class="{
                    'seacrh-option-icon': true,
                    'text-primary': searchOptiosn.find
                  }">
                    <span class="el-icon-aim"></span>
                  </span>
                </el-tooltip>

                <el-button @click="handleSearch" type="primary">
                  查找
                </el-button>
                <el-button @click="handleCancelSearch"> 取消 </el-button>
              </div>
            </div>
          </div>
          <el-button class="ml10" @click="searchOptiosn.visible = true" slot="reference" icon="el-icon-search">
            查找
          </el-button>
        </el-popover>
        <!-- 前景色 -->
        <el-popover placement="bottom">
          <div class="df align-center">
            <span class="fz12 mr10">修改前景色，以适应不同的光线环境</span>
            <el-color-picker v-model="foreground" @change="handleChangeForeground"></el-color-picker>
          </div>
          <el-button class="ml10" slot="reference" type="primary">
            设置前景色
          </el-button>
        </el-popover>
        <!-- 清空 -->
        <el-button class="ml10" @click="handleClear" v-if="type === 'console'">
          清 空
        </el-button>
        <el-button class="ml10" @click="handleToWebSSH"> 切换容器 </el-button>

        <el-input v-if="type === 'log'" class="ml10 w250" v-model="filterWord" @clear="handleFilter"
          placeholder="请输入关键字" clearable @keydown.native.enter="handleFilter">
          <el-button type="primary" slot="append" @click="handleFilter">
            过滤输出
          </el-button>
        </el-input>
      </div>
      <div class="df align-center">
        <span class="text-white fz20 mr100">{{ currentPod }}
          <span class="ml10 fz18">{{
            type === "console" ? "终端" : "日志"
          }}</span></span>
        <el-button class="ml10" v-if="type === 'console'" @click="() => hanleChangeType('log')">
          切换至日志
        </el-button>
        <el-button class="ml20" v-if="type === 'log'" @click="() => hanleChangeType('console')">
          切换至终端
        </el-button>

        <span class="ml10">
          <span class="text-danger" v-if="close">已断开</span>
          <span class="text-success" v-else>已连接</span>
        </span>
      </div>
    </div>

    <div class="flex-1 overflow">
      <div ref="container" class="w100p h100p"></div>
    </div>
    <div class="h50 df align-center shrink-0 pl20 pr20 bg-white" v-if="type === 'log'">
      <span class="fz14 mr10 line-height-1">初始行数</span>
      <el-select v-model="lineNum" @change="handleChangeLineNum" class="w150 mr10">
        <el-option :value="100"></el-option>
        <el-option :value="500"></el-option>
        <el-option :value="1000"></el-option>
        <el-option :value="5000"></el-option>
      </el-select>

      <el-switch class="mr10" v-model="track" active-text="自动追踪" inactive-text="暂停追踪" @change="handleChangeTrack">
      </el-switch>

      <el-button @click="handleClear">清 空</el-button>
    </div>
    <FilterDialog ref="filterDialog" />
  </div>
</template>

<script>
import { initTerm } from "./src/main";
import Storage from "./src/utils/storage";
import isEqual from "lodash/isEqual";
import * as k8s_api from "@/api/app/k8s";
import cloneDeep from "lodash/cloneDeep";
import choosPodToWebssh from "@/utils/choosPodToWebssh";
import FilterDialog from "./components/filterDialog";

const searchOptiosn = {
  text: "",
  options: {
    regex: false,
    wholeWord: false,
    caseSensitive: false //大小写
  },
  findNext: true,
  find: false,
  visible: false
};

export default {
  components: { FilterDialog },

  data() {
    const fontSize = Storage.getItem("fontSize") || 12;
    const foreground = Storage.getItem("foreground") || "";
    return {
      wt: null,
      fontSize,
      foreground,
      track: true,
      args: "",
      pods: [],
      currentPod: "",
      searchOptiosn: cloneDeep(searchOptiosn),
      close: false,
      lineNum: 500,
      filterWord: ""
    };
  },

  computed: {
    hasFilterWord() {
      return !!this.filterWord.trim();
    }
  },

  props: {
    type: String
  },

  mounted() {
    this.init();
  },

  watch: {
    "$route.params": {
      handler(val, old) {
        if (isEqual(val, old)) return;
        this.init();
      },
      deep: true
    },
    "$route.query.lineNum": {
      handler() {
        this.init();
      }
    }
  },

  methods: {
    setFontSize(fontSize) {
      Storage.setItem("fontSize", fontSize);
      this.wt.setFontSize(fontSize);
    },
    handleChangeForeground(color) {
      Storage.setItem("foreground", color);
      this.wt.setForeground(color);
    },

    handleChangeTrack(value) {
      if (value) {
        this.wt.startPushLog();
      } else {
        this.wt.stopPushLog();
      }
    },
    handleClear() {
      this.wt.clear();
    },

    handleFilter() {
      if (!this.wt.term.filterWord && !this.filterWord) return;
      this.init();
    },

    // 通过弹窗展示
    // handleFilter() {
    //   if (!this.hasFilterWord) return;

    //   this.wt.term.term.selectAll();
    //   let str = this.wt.term.term.getSelection();
    //   const filterWord = this.filterWord.replace(/(\W)/g, "\\$1");
    //   const reg = new RegExp(filterWord, "gi");
    //   str = str.replace(/(.*\s?)/gim, ($0, $1) => {
    //     if (reg.test($1))
    //       return (
    //         $1.replace(
    //           reg,
    //           `<span style='color:red'>${this.filterWord}</span>`
    //         ) + "<br/><br/>"
    //       );
    //     return "";
    //   });

    //   this.wt.term.term.clearSelection();
    //   this.$refs.filterDialog.show(str);
    // },

    init() {
      if (this.type !== "ssh") {
        this.loadPodList();
      }
      this.searchOptiosn = cloneDeep(searchOptiosn);
      this.args = "";
      this.close = false;
      this.$nextTick(() => {
        const httpsEnabled = window.location.protocol == "https:";
        const url =
          (httpsEnabled ? "wss://" : "ws://") +
          location.host +
          (window.location.pathname === "/"
            ? "/webssh"
            : window.location.pathname + "webssh");
        const {
          params: { pod_name, namespace },
          query: { lineNum = 500, clusterName }
        } = this.$route;
        this.lineNum = Number(lineNum);

        let args = "?";

        switch (this.type) {
          case "log":
            args = `?arg=-c&arg=kubectl --context ${clusterName} logs -f --tail ${lineNum} ${pod_name} -n ${namespace}`;
            break;
          case "console":
            args = `?arg=-c&arg=kubectl --context ${clusterName} exec -it ${pod_name} -n ${namespace} -- bash`;
            break;
          case "sh":
            args = `?arg=-c&arg=kubectl --context ${clusterName} exec -it ${pod_name} -n ${namespace} -- sh`;
            break;
          case "bash":
            args = `?arg=-c&arg=kubectl --context ${clusterName} exec -it ${pod_name} -n ${namespace} -- bash`;
            break;
          case "cmd":
            args = `?arg=-c&arg=kubectl --context ${clusterName} exec -it ${pod_name} -n ${namespace} -- cmd`;
            break;
          case "powershell":
            args = `?arg=-c&arg=kubectl --context ${clusterName} exec -it ${pod_name} -n ${namespace} -- powershell`;
            break;
          case "ssh":
            args = `?`;
            break;
        }
        // kubectl config use-context default

        this.args = args;
        this.$nextTick(() => {
          this.wt = initTerm({
            elem: this.$refs.container,
            url,
            args: this.args,
            options: {
              fontSize: this.fontSize * 1
            },
            closeCB: () => {
              this.close = true;
            },
            openCB:
              this.type === "ssh"
                ? connection => {
                  connection.send(
                    `1alias kubectl='kubectl --context=${clusterName}' && clear && history -c\n`
                  );
                }
                : undefined,
            filterWord: this.filterWord
          });
          this.wt.setForeground(this.foreground);
          window.term = this.wt.term.term;
        });
      });
    },
    async loadPodList() {
      const {
        params: { pod_name, namespace }
      } = this.$route;

      const res = await k8s_api.getContainerGroup(
        namespace,
        undefined,
        `app=debug-${this.getServiceName()}`
      );
      this.pods = res.items.map(i => i.metadata.name);
      this.currentPod = pod_name;
    },

    getServiceName() {
      const {
        params: { pod_name }
      } = this.$route;
      const names = pod_name.split("-");
      return names.slice(0, names.length - 2).join("-");
    },

    hanleChangeType(type) {
      const {
        params: { pod_name, namespace },
        query
      } = this.$route;
      const base = pod_name.replace(/^(run-|debug-)/, '');
      let nextPodName = pod_name;
      if (type === 'log') {
        nextPodName = `run-${base}`;
      } else if (type === 'console') {
        nextPodName = `debug-${base}`;
      }
      this.$router.push({
        path: `/webssh/${type}/${namespace}/${nextPodName}`,
        query: { ...query }
      });
    },

    handleChangeLineNum(lineNum) {
      const {
        params: { pod_name, namespace, type },
        query
      } = this.$route;
      this.$router.push({
        path: `/webssh/${type}/${namespace}/${pod_name}`,
        query: { ...query, lineNum }
      });
    },

    handleSearch() {
      const { text, options, findNext } = this.searchOptiosn;

      const fn = findNext
        ? this.wt.term.searchAddon.findNext
        : this.wt.term.searchAddon.findPrevious;

      this.searchOptiosn.find = fn.call(
        this.wt.term.searchAddon,
        text,
        options
      );
    },
    handleCancelSearch() {
      this.searchOptiosn = cloneDeep(searchOptiosn);
    },
    handleToWebSSH() {
      const {
        params: { namespace }
      } = this.$route;

      const openMethod = (type, namespace, podName) => {
        this.$router.push({
          path: `/webssh/${type}/${namespace}/${podName}`,
          query: {
            proName: namespace,
            clusterName: this.$store.state.project.currentCluster.name
          }
        });
      };

      choosPodToWebssh(
        { serviceName: this.getServiceName(), namespace, openMethod },
        true
      );
    }
  }
};
</script>

<style lang="scss">
.terminal {
  font-family: "DejaVu Sans Mono", "Everson Mono", FreeMono, Menlo, Terminal,
    monospace, "Apple Symbols";
  height: 100%;
}

// 通用布局类
.w100p {
  width: 100%;
}

.h100p {
  height: 100%;
}

.df {
  display: flex;
}

.flex-column {
  flex-direction: column;
}

.flex-1 {
  flex: 1;
}

.shrink-0 {
  flex-shrink: 0;
}

.align-center {
  align-items: center;
}

.justify-between {
  justify-content: space-between;
}

.overflow {
  overflow: hidden;
}

// 间距类
.ml10 {
  margin-left: 10px;
}

.ml20 {
  margin-left: 20px;
}

.mr10 {
  margin-right: 10px;
}

.mr100 {
  margin-right: 100px;
}

.mt10 {
  margin-top: 10px;
}

.pl20 {
  padding-left: 20px;
}

.pr20 {
  padding-right: 20px;
}

// 尺寸类
.h50 {
  height: 50px;
}

.w150 {
  width: 150px;
}

.w250 {
  width: 250px;
}

// 字体与颜色
.fz12 {
  font-size: 12px;
}

.fz14 {
  font-size: 14px;
}

.fz18 {
  font-size: 18px;
}

.fz20 {
  font-size: 20px;
}

.text-white {
  color: #ffffff;
}

.text-success {
  color: #67C23A;
}

.text-danger {
  color: #F56C6C;
}

.line-height-1 {
  line-height: 1;
}

.bg-white {
  background: #ffffff;
}

.xterm-overlay {
  font-family: "DejaVu Sans Mono", "Everson Mono", FreeMono, Menlo, Terminal,
    monospace, "Apple Symbols";
  border-radius: 15px;
  font-size: xx-large;
  color: black;
  background: white;
  opacity: 0.75;
  padding: 0.2em 0.5em 0.2em 0.5em;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  user-select: none;
  transition: opacity 180ms ease-in;
}

.seacrh-option-icon {
  border: 1px solid currentColor;
  height: 22px;
  width: 22px;
  line-height: 22px;
  margin-right: 2px;
  text-align: center;
  cursor: pointer;

  &.text-primary {
    color: #409EFF;
  }
}
</style>
