<template>
  <el-dialog :title="dataForm.id == null ? '新增' : '修改'" :close-on-click-modal="false" :visible.sync="visible"
    @closed="reset">
    <el-form :model="dataForm" :rules="dataRule" ref="dataForm" label-width="auto">
      <el-form-item label="应用名称" prop="name">
        <el-input v-model.trim="dataForm.name" placeholder="应用名称"></el-input>
      </el-form-item>
      <el-form-item label="所属分类" prop="type">
        <el-select v-model="dataForm.type" placeholder="请选择所属分类" clearable>
          <el-option v-for="appType in appTypeList" :key="appType.key" :value="appType.key" :label="appType.value">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="应用关键字" prop="key">
        <el-input v-model.trim="dataForm.key" :disabled="dataForm.id != null" placeholder="应用关键字（key）" onkeyup="value=value.replace(/[^\x00-\xff]/g, '')"></el-input>
      </el-form-item>
      <el-form-item label="图标" prop="icon">
        <icon-select v-model="dataForm.icon" placeholder="点击选择图标，或直接输入图片地址，也可点击下方按钮上传图片（将转为 base64）" />
        <icon-upload-base64 v-model="dataForm.icon" class="mt10" />
      </el-form-item>
      <el-form-item label="显示序号" prop="seq">
        <el-input v-model="dataForm.seq" placeholder="请输入数字"></el-input>
      </el-form-item>
      <el-form-item label="呈现在导航" prop="showInHome">
        <el-switch v-model="dataForm.showInHome" :active-value="1" :inactive-value="0"></el-switch>
      </el-form-item>
      <el-form-item label="是否已部署" prop="deploy">
        <el-radio-group v-model="dataForm.deploy">
          <el-radio :label="1">已部署</el-radio>
          <el-radio :label="0">未部署</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="应用地址" prop="url">
        <span slot="label">应用地址<el-tooltip placement="right" class="ml5">
            <div slot="content">
              基础示例：http://127.0.0.1:8080，不推荐，不便于部署和迁移
              <br />高级示例：http://${ip}:${port}，ip 和 port 为IP地址和端口号，可独立使用，例如：http://${ip}:8080
              <br />域名示例：http://xxx-front.${DOMAIN}，DOMAIN 为域名的公共部分，详见全局配置
              <br />支持 // 开头的写法来同时支持 http 和 https 协议
            </div>
            <i class="el-icon-question"></i>
          </el-tooltip>
        </span>
        <el-input v-model="dataForm.url" v-fillWithPlaceholder placeholder="http://"></el-input>
      </el-form-item>
      <el-form-item label="应用打开方式" prop="urlTarget">
        <el-radio-group v-model="dataForm.urlTarget">
          <el-radio :label="undefined">
            默认
            <el-tooltip placement="right">
              <i class="el-icon-question"></i>
              <div slot="content">
                由内部逻辑根据所处的环境自动决定，例如：
                <br />与当前登录的应用同域时，会被认为是同一套前端，在切换时直接切换菜单和路由
                <br />与当前登录的应用不同域时，在切换时会把应用地址作为前缀，与菜单地址拼接为绝对地址，然后以 iframe 打开
              </div>
            </el-tooltip>
          </el-radio>
          <el-radio label="_blank">新窗口</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="应用宣传门户" prop="introUrl">
        <el-input v-model="dataForm.introUrl" placeholder="应用宣传页面的链接地址"></el-input>
      </el-form-item>
      <el-form-item label="门户打开方式" prop="introUrlTarget">
        <el-radio-group v-model="dataForm.introUrlTarget">
          <el-radio :label="undefined">默认</el-radio>
          <el-radio label="dialog" disabled>对话框</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="微服务名" prop="microServices">
        <span slot="label">微服务名<el-tooltip placement="right" class="ml5">
            <div slot="content">
              该应用的前端需要调用的后台微服务名称
            </div>
            <i class="el-icon-question"></i>
          </el-tooltip>
        </span>
        <el-tag
          :key="tag"
          v-for="tag in dynamicTags"
          closable
          :disable-transitions="false"
          @close="handleClose(tag)">
          {{tag}}
        </el-tag>
        <el-input
          :style="{marginLeft: dynamicTags.length ? '10px' : ''}"
          class="input-new-tag"
          v-if="inputVisible"
          v-model="inputValue"
          ref="saveTagInput"
          size="small"
          @keyup.enter.native="handleInputConfirm"
          @blur="handleInputConfirm"
        >
        </el-input>
        <el-button v-else :style="{marginLeft: dynamicTags.length ? '10px' : ''}" class="button-new-tag" size="small" @click="showInput">添加</el-button>
      </el-form-item>
      <el-form-item label="应用描述" prop="desc">
        <el-input type="textarea" :rows="3" v-model="dataForm.desc" placeholder="请输入描述"></el-input>
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitLoading" @click="dataFormSubmit">确定</el-button>
    </span>
  </el-dialog>
</template>

<script>
import api_app from "@/api/sys/app";
import api_auth from "@/api/auth";
import IconSelect from "@/components/icon-select";
import IconUploadBase64 from '@/components/icon-upload-base64';

export default {
  components: {
    IconSelect,
    IconUploadBase64,
  },
  data() {
    var validateUrl = (rule, value, callback) => {
      if (!value) {
        callback(new Error("应用地址不能为空"));
      } else if (!/^(http[s]?:)?\/\/.*/.test(value)) {
        callback(new Error("应用地址不符合要求"));
      } else {
        callback();
      }
    };

    return {
      visible: false,
      submitLoading: false,
      appTypeList: [],
      dataForm: {
        name: null,
        type: null,
        key: null,
        icon: null,
        url: null,
        urlTarget: undefined,
        introUrl: null,
        introUrlTarget: undefined,
        seq: null,
        showInHome: 1,
        desc: null,
        deploy: 1,
        microServices: null,
      },
      dataRule: {
        name: [
          { required: true, message: "应用名称不能为空", trigger: "blur" },
        ],
        type: [
          { required: true, message: "所属分类不能为空", trigger: "blur" },
        ],
        key: [
          { required: true, message: "应用关键字不能为空", trigger: "blur" },
        ],
        url: [{ required: true, validator: validateUrl, trigger: "blur" }],
        deploy: [
          { required: true, message: "点击跳转方式不能为空", trigger: "blur" },
        ],
      },
      dynamicTags: [],
      inputVisible: false,
      inputValue: ''
    };
  },
  methods: {
    init(row, appTypeList) {
      this.visible = true;
      this.appTypeList = appTypeList;
      this.dynamicTags =  []
      this.$nextTick(() => {
        // 修复焦点在带校验的项按回车提交后，再次打开对话框，该项会带有校验提示的问题
        // 而 resetFields 仍然放在关闭对话框时执行，以避免下次打开时，带动画的控件（如checkbox）会闪现之前的结果
        this.$refs.dataForm.clearValidate();

        // 对dataForm的赋值，需要滞后，以免变成默认值，reset时出问题
        this.dataForm.id = row.id;
        if (this.dataForm.id != null) {
          if(row.deploy == undefined) row.deploy = 1;
          this.dataForm = {
            id: row.id,
            name: row.name,
            type: row.type,
            key: row.key,
            icon: row.icon,
            url: row.url,
            urlTarget: row.urlTarget,
            introUrl: row.introUrl,
            introUrlTarget: row.introUrlTarget,
            seq: row.seq,
            showInHome: row.showInHome,
            desc: row.desc,
            deploy: row.deploy,
            microServices:  row.microServices,
          };
          if(this.dataForm.microServices && this.dataForm.microServices.length){
            this.dynamicTags =  this.dataForm.microServices.split(',')
          }
        }
      });
    },
    reset() {
      this.$refs.dataForm.resetFields();
    },
    // 表单提交
    async dataFormSubmit() {
      if (window.globalConfig.verifyAuth) {
        //加授权权限校验
        try {
          let data = await api_auth.verifyAuth();
          if (!data) return this.$message.warning('license未授权或已过期，请联系系统管理员');
        } catch (err) {
          return false;
        }
      }
      this.dataForm.microServices = this.dynamicTags.join(',')
      this.$refs.dataForm.validate((valid) => {
        if (valid) {
          this.submitLoading = true;
          const tmp = { ...this.dataForm };
          api_app
            .insertOrUpdate(tmp)
            .then(() => {
              this.visible = false;
              this.submitLoading = false;
              this.$emit("onSubmit");
              this.$message({
                message: "操作成功",
                type: "success",
                duration: 1500,
              });
            })
            .catch(() => {
              this.submitLoading = false;
            });
        }
      });
    },
    handleClose(tag) {
      this.dynamicTags.splice(this.dynamicTags.indexOf(tag), 1);
    },

    showInput() {
      this.inputVisible = true;
      this.$nextTick(_ => {
        this.$refs.saveTagInput.$refs.input.focus();
      });
    },

    handleInputConfirm() {
      let inputValue = this.inputValue;
      if (inputValue) {
        this.dynamicTags.push(inputValue);
      }
      this.inputVisible = false;
      this.inputValue = '';
    }
  },
};
</script>
<style lang="scss" scoped>
::v-deep .el-tag + .el-tag {
  margin-left: 10px;
}
.button-new-tag {
  height: 32px;
  line-height: 30px;
  padding-top: 0;
  padding-bottom: 0;
}
.input-new-tag {
  width: 90px;
  vertical-align: bottom;
}
</style>
