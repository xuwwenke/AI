const UglifyJsPlugin = require("uglifyjs-webpack-plugin");
const dynamicProxy = require('./vue-config/dynamic.proxy');
const path = require('path');
const express = require('express');
const knowledgeInvoke = require('./vue-config/knowledge.invoke');

module.exports = {
  publicPath: "",
  // lintOnSave: process.env.NODE_ENV !== "production",
  lintOnSave: false,
  productionSourceMap: false,
  // filenameHashing: false,
  devServer: {
    before(app) {
      app.use('/knowledge-invoke', knowledgeInvoke.guardKnowledgeInvoke);
      app.use('/ls-templates', express.static(
        path.resolve(__dirname, '../label-studio/label_studio/core/static/templates')
      ));
    },
    proxy: {
      // URL 自带项目和知识库 ID，仅允许检索，不提供新增/修改/删除入口。
      '/knowledge-invoke': {
        target: process.env.KNOWLEDGE_SERVICE_TARGET || 'http://192.168.98.137:8000',
        changeOrigin: true,
        ws: false,
        pathRewrite: knowledgeInvoke.rewriteKnowledgeInvoke,
        onProxyReq: knowledgeInvoke.setKnowledgeInvokeHeaders
      },
      // 注意：这里的顺序就是匹配优先级。vue-cli 的 prepareProxy 会把 context 当「非锚定」正则
      // 来匹配（pathname.match(context)），只要路径里出现 /api 就会命中下面那条 /api 规则。
      // 所以这两个前缀必须放在 /api 之前，否则 /labelstudio/api/... 会被转发到 /api 的后端。
      // 数据标注聚合服务（data-labeling-service，FastAPI）
      // 目标可用环境变量 LABELING_SERVICE_TARGET 覆盖，默认指向本地 uvicorn 启动的地址
      '/labeling-service': {
        target: process.env.LABELING_SERVICE_TARGET || 'http://127.0.0.1:8000',
        changeOrigin: true,
        ws: true,
        pathRewrite: {
          "^/labeling-service": "" // rewrite path
        },
      },
      '/labelstudio': {
        target: 'http://192.168.98.129:8000',
        changeOrigin: true,
        ws: true,
        pathRewrite: {
          "^/labelstudio": "" // rewrite path
        },
      },
      // 知识库服务。通过同源前缀转发，避免浏览器携带 namespace 等请求头时触发跨域拦截。
      '/knowledge-api': {
        target: process.env.KNOWLEDGE_SERVICE_TARGET || 'http://192.168.98.137:8000',
        changeOrigin: true,
        ws: false,
        pathRewrite: {
          '^/knowledge-api': ''
        }
      },
      "/ollama": {
        target: "http://192.168.224.248:11434",
        changeOrigin: true,
        pathRewrite: {
          "^/ollama": ""
        },
        proxyTimeout: 0,
        timeout: 0
      },
      "/service": {
        target: "这里配置无效", // 为了支持代理地址热切换（无需重启），已转到 vue_config/dynamic.proxy.list.json 中配置，并通过 active 激活代理（保持只有一个处于 active 状态）
        changeOrigin: true,
        ws: true,
        pathRewrite: {
          "^/service": "" // rewrite path
        },
        router: function (req) { // 代理地址热切换相关
          return dynamicProxy();
        },
        // EverCloud 武汉环境不需要下面这段，但北京环境需要
        onProxyRes: function (proxyRes, req, res) {
          var cookies = proxyRes.headers['set-cookie'];
          var cookieRegex = /Domain=\.?beijing.everdevcloud.com/i; // 返回的 cookie 中提取 domain
          // 修改 cookie Path
          if (cookies) {
            var newCookie = cookies.map(function (cookie) {
              if (cookieRegex.test(cookie)) {
                // 将 domain 置空
                return cookie.replace(cookieRegex, '');
              }
              return cookie;
            });
            delete proxyRes.headers['set-cookie'];
            proxyRes.headers['set-cookie'] = newCookie;
          }
        }
      },
      "/api": {
        target: "http://192.168.224.187:3001",
        changeOrigin: true,
        ws: false,
        // 关键：SSE 流式建议关掉代理超时（可选）
        proxyTimeout: 0,
        timeout: 0
        // 不要 pathRewrite，后端本来就是 /api/chat /api/chat/stream
      },
      "/webssh": {
        target: "http://ailabx-kube-webssh.llmops-dev.cloud.everoffice.cn",
        changeOrigin: true,
        ws: true,
        pathRewrite: {
          "^/webssh": "/ws"
        }
      },
      '/pvcviewer': {
        target: 'http://192.168.75.239:30088',
        changeOrigin: true,
        ws: true,
        pathRewrite: {
          "^/pvcviewer": "" // rewrite path
        },
      }
      // eslint 警告和错误将直接出现在页面上
      /* overlay: {
        warnings: true,
        errors: true
      } */
    }
  },
  configureWebpack: config => {
    // 支持 VSCode 中调试
    config.devtool = config.mode === 'production' ? false : 'source-map';
    // 入口文件
    config.entry.app = ["./src/main.js"];

    config.optimization = {
      splitChunks: {
        maxAsyncRequests: 30,
        maxInitialRequests: 10,
        name: false
      }
    };

    if (process.env.NODE_ENV === "production") {
      // 生产环境下
      config.plugins = [
        ...config.plugins,
        new UglifyJsPlugin({
          parallel: true,
          uglifyOptions: {
            output: {
              comments: false   // 删除注释
            },
            compress: {
              drop_console: true  // 删除 console
            }
          }
        })
      ];
    }
  },

  chainWebpack: config => {
    // 让 webpack 把 .fbx 当静态文件处理，import 时返回 URL
    config.plugins.delete('preload');
    config.plugins.delete('prefetch');
    // 从 public 复制到 dist 的时候，忽略掉版本控制相关文件夹
    config
      .plugin('copy')
      .tap(args => {
        const newIgnore = ['**/.svn/**', '**/.git/**'];
        args[0][0].ignore = args[0][0].ignore.concat(newIgnore);
        return args;
      });
  }
};
