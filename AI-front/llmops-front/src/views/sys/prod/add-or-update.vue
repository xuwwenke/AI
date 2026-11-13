<template>
  <el-dialog :title="dataForm.id == null ? '新增' : '修改'" :close-on-click-modal="false" :visible.sync="visible"
    @closed="reset">
    <el-form :model="dataForm" :rules="dataRule" ref="dataForm" label-width="auto">
      <el-form-item label="产线名称" prop="name">
        <el-input v-model.trim="dataForm.name" placeholder="产线名称"></el-input>
      </el-form-item>
      <el-form-item label="产线关键字" prop="key">
        <el-input v-model.trim="dataForm.key" :disabled="dataForm.id != null" placeholder="产线关键字（key）"></el-input>
      </el-form-item>
      <el-form-item label="图标" prop="icon">
        <icon-select v-model="dataForm.icon" placeholder="点击选择图标，或直接输入图片地址，也可点击下方按钮上传图片（将转为 base64）" />
        <icon-upload-base64 v-model="dataForm.icon" class="mt10" />
      </el-form-item>
      <el-form-item label="显示序号" prop="seq">
        <el-input v-model="dataForm.seq" placeholder="请输入数字"></el-input>
      </el-form-item>
      <el-form-item label="呈现在导览页" prop="seq">
        <el-switch v-model="dataForm.showInHome" :active-value="1" :inactive-value="0"></el-switch>
      </el-form-item>
      <el-form-item label="是否已部署" prop="deploy">
        <el-radio-group v-model="dataForm.deploy">
          <el-radio :label="1">已部署</el-radio>
          <el-radio :label="0">未部署</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="产线地址" prop="url">
        <span slot="label">产线地址<el-tooltip placement="right" class="ml5">
            <div slot="content">
              将优先使用关联的应用进行展示，没有关联任何应用时，才会使用该地址
            </div>
            <i class="el-icon-question"></i>
          </el-tooltip>
        </span>
        <el-input v-model="dataForm.url" placeholder="产线地址（非必填），没有关联任何应用时，才会使用该地址"></el-input>
      </el-form-item>
      <el-form-item label="产线打开方式" prop="urlTarget">
        <el-radio-group v-model="dataForm.urlTarget">
          <el-radio :label="undefined">
            默认
            <el-tooltip placement="right">
              <i class="el-icon-question"></i>
              <div slot="content">
                由内部逻辑根据所处的环境自动决定，例如：
                <br />产线切换时，会切换至该产线下的第一个应用，如果没有关联任何应用，则直接打开产线地址
                <br />与当前登录的应用同域时，会被认为是同一套前端，在切换时直接切换菜单和路由
                <br />与当前登录的应用不同域时，在切换时会把应用地址作为前缀，与菜单地址拼接为绝对地址，然后以 iframe 打开
              </div>
            </el-tooltip>
          </el-radio>
          <el-radio label="_blank">新窗口</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="产线宣传门户" prop="introUrl">
        <el-input v-model="dataForm.introUrl" placeholder="产线宣传页面的链接地址"></el-input>
      </el-form-item>
      <el-form-item label="门户打开方式" prop="introUrlTarget">
        <el-radio-group v-model="dataForm.introUrlTarget">
          <el-radio :label="undefined">默认</el-radio>
          <el-radio label="dialog" disabled>对话框</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="产线描述" prop="desc">
        <el-input type="textarea" :rows="3" v-model="dataForm.desc" placeholder="请输入描述"></el-input>
      </el-form-item>
      <el-form-item label="关联应用" prop="appKeys">
        <el-select
          v-model="dataForm.appKeys"
          placeholder="请选择关联的应用"
          :loading="appListLoading"
          multiple
          filterable
        >
          <el-option
            v-for="app in appList"
            :key="app.key"
            :value="app.key"
            :label="app.name + ' - ' + app.key"
          ></el-option>
        </el-select>
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitLoading" @click="dataFormSubmit">确定</el-button>
    </span>
  </el-dialog>
</template>

<script>
import api_prod from "@/api/sys/prod";
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
    // var validateUrl = (rule, value, callback) => {
    //   if (!value) {
    //     callback(new Error("产线地址不能为空"));
    //   } else if (!/^(http[s]?:)?\/\/.*/.test(value)) {
    //     callback(new Error("产线地址不符合要求，通常以 http:// 作为前缀"));
    //   } else {
    //     callback();
    //   }
    // };

    return {
      visible: false,
      submitLoading: false,
      appListLoading: false,
      appList: [],
      dataForm: {
        name: null,
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
        appKeys: [],
      },
      dataRule: {
        name: [
          { required: true, message: "产线名称不能为空", trigger: "blur" },
        ],
        key: [
          { required: true, message: "产线关键字不能为空", trigger: "blur" },
        ],
        // url: [{ required: true, validator: validateUrl, trigger: "blur" }],
        deploy: [
          { required: true, message: "点击跳转方式不能为空", trigger: "blur" },
        ],
      },
    };
  },
  methods: {
    init(row) {
      this.visible = true;

      this.$nextTick(() => {
        // 修复焦点在带校验的项按回车提交后，再次打开对话框，该项会带有校验提示的问题
        // 而 resetFields 仍然放在关闭对话框时执行，以避免下次打开时，带动画的控件（如checkbox）会闪现之前的结果
        this.$refs.dataForm.clearValidate();

        // 对dataForm的赋值，需要滞后，以免变成默认值，reset时出问题
        this.dataForm.id = row.id;
        if (this.dataForm.id != null) {
          this.dataForm = {
            id: row.id,
            name: row.name,
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
            appKeys: row.appKeyArray || [],
          };
        }
      });

      this.appListLoading = true;

      api_app
        .getList({
          maxResult: 1000,
        })
        .then(data => {
          this.appListLoading = false;
          this.appList = data ? data.resultData : [];
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
      this.$refs.dataForm.validate((valid) => {
        if (valid) {
          this.submitLoading = true;
          const tmp = { ...this.dataForm };
          tmp.appKeys = tmp.appKeys.join(',');

          const opt = this.dataForm.id == null ? "insert" : "update";
          api_prod[opt](tmp)
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
  },
};
</script>
