# evervue

## [查看官方文档](http://evervue-doc.evervue.cloud.everoffice.cn/)

### 说明

- node 版本为 12.18.1
- 添加页面，先在 views 中开发页面，然后在系统管理 -> 菜单管理中配置菜单
- 目前 路由配置中， /page 是一个内部特殊路由，用来指向动态生成页
- 浏览器打开了一直没关，这时，服务器上删掉了旧的代码，提交了新的代码，浏览器旧代码请求旧资源时找不到文件。  
  解决方式：1. 提交新代码时，不马上删除旧代码。 2. 提示用户刷新页面

### 贡献

欢迎大家 Fork 代码，开发功能或修复问题，最后发起 Merge Request。  
由于 gitlab 的 Fork 权限被禁，大家可以申请开发权限，从 develop 拉分支，再发起 Merge Request。
注：Merge Request 指向 `develop` 分支。

### node-sass 替换为 sass

因 node-sass 容易导致流水线执行失败
evervue 已经将 node-sass 替换为 sass，同时顺便修复了编译时的 caniuse-lite is outdated 警告
当现有前端工程遇到类似问题难以解决时，可用最新的 evervue 覆盖升级
或者自行升级 sass，步骤如下：
- 卸载 node-sass：npm uninstall node-sass
- 安装 sass：npm install sass --save-dev
- 全局搜索替换：/deep/ 替换为 ::v-deep
- 删除根目录下不必要的 .npmrc
- 删除 package.json 中的 optionalDependencies 部分

### vscode vue 格式化代码设置

为了方便大家代码风格统一，强烈建议使用相同的 vscode 相关配置规则，相关步骤如下，需按照步骤依次执行。

#### 一、安装 VScode 插件

- 安装 ESlint
- 安装 Vetur
- 安装 Prettier - Code formatter

#### 二、修改 vscode 设置

通过文件 - 首选项 - 设置，打开 vscode 设置，点击右上角小图标进入 json 编辑模式（settings.json）
使用以下 json 覆盖现有设置，或者与现有配置合并

```json
{
  // 保存时格式化
  "editor.codeActionsOnSave": {
    "source.fixAll.eslint": true
  },
  // 显示 markdown 中英文切换时产生的特殊字符
  "editor.renderControlCharacters": true,
  // tab 大小为2个空格
  "editor.tabSize": 2,
  // 100 列后换行
  "editor.wordWrapColumn": 100,
  "eslint.enable": false,
  // eslint 检测文件类型
  "eslint.validate": ["html", "vue", "javascript", "javascriptreact"],
  // prettier 设置语句末尾不加分号
  "prettier.semi": false,
  // prettier 设置强制单引号
  "prettier.singleQuote": true,
  // 选择 vue 文件中 template 的格式化工具
  "vetur.format.defaultFormatter.html": "js-beautify-html",
  "vetur.format.defaultFormatter.js": "prettier-eslint",
  "vetur.format.defaultFormatter.css": "prettier",
  "vetur.format.defaultFormatter.less": "prettier",
  "vetur.format.defaultFormatter.postcss": "prettier",
  "vetur.format.defaultFormatter.pug": "prettier",
  "vetur.format.defaultFormatter.sass": "sass-formatter",
  "vetur.format.defaultFormatter.scss": "prettier",
  "vetur.format.defaultFormatter.stylus": "none",
  "vetur.format.defaultFormatter.ts": "prettier-tslint",
  // vetur 的自定义设置
  "vetur.format.defaultFormatterOptions": {
    "prettier": {
      "singleQuote": true, // true 为使用单引号
      "semi": true // 去掉分号
    },
    "js-beautify-html": {
      "wrap_attributes": "auto",
      "wrap_attributes_mode": "auto",
      "wrap-line-length": 100,
      "wrapped_attributes_per_line": "multiple",
      "wrapped_attributes_indent": "auto",
      "wrapped_attributes_end": "auto"
    }
  },
  "[jsonc]": {
    "editor.defaultFormatter": "esbenp.prettier-vscode"
  },
  "[vue]": {
    "editor.defaultFormatter": "octref.vetur" // 使用 vetur 格式化规则
  },
  "[javascript]": {
    "editor.defaultFormatter": "vscode.typescript-language-features"
  },
  // 开启 vscode 文件路径导航
  "breadcrumbs.enabled": true,
  // 拖拽移动文件时是否确认提示
  "explorer.confirmDragAndDrop": false,
  // 让函数(名)和后面的括号之间加个空格
  "javascript.format.insertSpaceBeforeFunctionParenthesis": false,
  "javascript.preferences.quoteStyle": "single",
  "typescript.preferences.quoteStyle": "single"
}
```
