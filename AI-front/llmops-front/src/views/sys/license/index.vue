<template>
  <div>
    <el-alert
      title="当前产品未授权或者已过期, 请授权！"
      type="warning"
      show-icon
      class="mb10"
      v-if="dataForm.status != 'inEffect'"
    ></el-alert>
    <page-title />
    <div class="title">授权许可摘要</div>
    <el-form
      :inline="true"
      :model="dataForm"
      ref="queryForm"
      label-position="left"
      label-width="70"
    >
      <el-row :gutter="50">
        <el-col :sm="12" :lg="8">
          <el-form-item label="项目编码：" prop="name">
            {{ dataForm.name || "——" }}
          </el-form-item>
        </el-col>
        <el-col :sm="12" :lg="8">
          <el-form-item label="授权状态：" prop="status">
            <el-tag size="mini" v-if="dataForm.status">{{
              statusMap[dataForm.status] || "——"
            }}</el-tag>
            <el-link type="text" class="ml10 f-sm" @click="showCode"
              >更新授权码></el-link
            >
          </el-form-item>
        </el-col>
        <el-col :sm="12" :lg="8">
          <el-form-item label="授权日期：" prop="issued">
            {{ formatTime(dataForm.issued) || "——" }}
          </el-form-item>
        </el-col>
        <el-col :sm="24" :lg="24">
          <el-form-item label="硬件码：" prop="mac">
            {{ dataForm.mac ? dataForm.mac.join("，") : "——" }}
          </el-form-item>
        </el-col>
        <el-col :sm="24" :lg="24">
          <el-form-item label="授权IP：">
            {{ dataForm.ip ? dataForm.ip.join("，") : "——" }}
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <el-divider></el-divider>

    <div class="title">授权许可明细</div>
    <el-table
      v-loading="isAuthDetailLoading"
      :data="authDetailList"
      stripe
      style="width: 100%;"
    >
      <el-table-column prop="name" align="left" label="功能模块">
      </el-table-column>
      <el-table-column prop="version" align="left" label="模块版本" width="120">
      </el-table-column>
      <el-table-column prop="total" align="left" label="授权方式" width="150">
        <template slot-scope="scope">
          {{ authType.label[scope.row.total ? "1" : "0"] }}
        </template>
      </el-table-column>
      <el-table-column prop="total" align="left" label="授权许可" width="260">
        <template slot-scope="scope">
          {{ licenseName(scope.row) }}
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增、编辑 -->
    <el-dialog
      title="下发授权"
      @close="cancel('dealForm')"
      :visible="isShow"
      :close-on-click-modal="false"
      width="56%"
    >
      <el-form ref="dealForm" class="deal-form" label-width="100px">
        <el-form-item label="设备信息" class="device-info">
          <el-input
            type="textarea"
            :readonly="true"
            :rows="5"
            v-model="dataForm.licenseCheckModel"
          ></el-input>
          <el-link
            class="copy-btn"
            icon="el-icon-document-copy"
            :underline="false"
            v-clipboard:copy="dataForm.licenseCheckModel"
            v-clipboard:success="onCopy"
            v-clipboard:error="onError"
          ></el-link>
          <el-link
            class="copy-btn"
            icon="el-icon-download"
            @click="handleDownload"
            :underline="false"
          ></el-link>
          <div class="c-gray f-sm" style="color: #606266">
            请复制设备信息发送给客户经理，申请产品授权文件
          </div>
        </el-form-item>
        <el-form-item label="授权码">
          <el-upload
            action
            :file-list="fileList"
            :http-request="fileUpload"
            accept=".lic"
            :before-upload="beforeUpload"
            :on-remove="handleRemove"
            :on-change="handleChange"
            list-type="text"
            :limit="1"
            class="fl ml10"
          >
            <el-button type="primary">上传文件</el-button>
            <div slot="tip" class="el-upload__tip">
              请上传授权文件，文件扩展名为LIC
            </div>
          </el-upload>
        </el-form-item>
        <el-form-item class="mt20">
          <el-button
            type="success"
            @click="activation"
            :loading="submitLoading"
            >确 认</el-button
          >
          <el-button @click="cancel()">取 消</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>

<script>
import api_license from "@/api/sys/license";
import { downloadFile } from "@/utils/download";
import { formatTime } from "@/utils/time";

// 授权类型定义
const authType = {
  label: {
    0: "按有效期",
    1: "按使用次数"
  },
  desc: {
    0: "有效期至",
    1: "使用次数上限"
  },
  options: [
    { label: "按有效期", value: 0 },
    { label: "按使用次数", value: 1 }
  ]
};

export default {
  data() {
    return {
      dataForm: {
        name: null,
        issued: null,
        version: null,
        hardwareCode: null,
        ip: null,
        mack: null,
        activationCode: null,
        status: "inEffect",
        type: null,
        startDate: null,
        endDate: null,
        licenseCheckModel: ""
      },
      dealData: {},
      isload: false,
      isShow: false,
      submitLoading: false,
      fileList: [],
      file: "",
      isUpload: 0,
      statusMap: {
        unauthorized: "未授权",
        inEffect: "已授权",
        expired: "已过期"
      },
      typeMap: {
        temporary: "试用版",
        permanent: "正式版"
      },
      authDetailList: [],
      isAuthDetailLoading: false,
      authType: authType,
      formatTime: formatTime
    };
  },
  computed: {},
  created() {
    this.getProductInfo();
  },
  methods: {
    showCode() {
      this.isShow = true;
      this.getServerInfos();
    },
    getProductInfo() {
      this.isload = true;
      return api_license
        .getProductInfo()
        .then(res => {
          this.isload = false;
          this.dataForm.name = res.extra.project.projectNum;
          this.dataForm.issued = res.issued;
          this.dataForm.ip = res.extra.hardware.ip;
          this.dataForm.mac = res.extra.hardware.mac;

          this.authDetailList = res.extra.spu
            ? Object.values(res.extra.spu)
            : [];
          return true;
        })
        .catch(() => {
          this.isload = false;
          return false;
        });
    },
    getServerInfos() {
      api_license
        .getServerInfos()
        .then(res => {
          this.isload = false;
          this.$nextTick(() => {
            this.$set(this.dataForm, "licenseCheckModel", JSON.stringify(res));
          });
        })
        .catch(() => {
          this.isload = false;
        });
    },
    licenseName(it) {
      let key = it.total ? "1" : "0";
      let prefix = authType.desc[key];
      return key == "1"
        ? `${prefix}：${it.total}次`
        : `${prefix}：${formatTime(it.expiryDate)}`;
    },
    // 移除文件
    handleRemove(file, fileList) {
      this.file = file;
      this.fileList = fileList;
    },
    // 文件上传
    fileUpload(param) {
      this.file = param.file;
      let fileFormData = new FormData();
      this.uploadApi(this.file, "", fileFormData, "file");
    },
    beforeUpload(file) {
      const isLt1M = file.size / 1024 / 1024 < 1;
      if (!isLt1M) {
        this.$message.error("上传文件大小不能超过 1MB!");
      }
      return isLt1M;
    },
    uploadApi(file, param, formData, type) {
      let data = {
        fileLic: file
      };
      Object.keys(data).forEach((item, index) => {
        formData.get(item) ? formData.delete(item) : "";
        formData.append(item, data[item]);
      });
      this.dealData = formData;
    },
    handleChange(file, fileList) {
      this.file = file;
      this.fileList = fileList;
    },
    onCopy: function(e) {
      this.$message({ message: "复制成功", type: "success", duration: "1000" });
    },
    onError: function(e) {
      this.$message({ message: "复制失败", type: "error" });
    },
    activation() {
      if (this.fileList.length == 0) {
        this.$message({ message: "请先上传LIC文件！", type: "error" });
        return;
      }
      this.submitLoading = true;
      api_license
        .uploadFile(this.dealData, "post", "")
        .then(async res => {
          if (res.code && res.code != 200) {
            this.$message({ message: "授权失败，请稍后重试！", type: "error" });
            return;
          }
          this.isShow = false;
          this.submitLoading = false;
          let flag = await this.getProductInfo();
          flag && this.$message({ message: "授权成功！", type: "success" });
        })
        .catch(() => {
          this.submitLoading = false;
        });
    },
    cancel() {
      this.isShow = false;
    },
    handleDownload() {
      let blob = new Blob([this.dataForm.licenseCheckModel], {
        type: "application/json"
      });
      downloadFile(blob, "license.txt");
    }
  }
};
</script>

<style lang="scss" scoped>
.deal-form {
  .el-input,
  .el-select,
  .el-textarea {
    width: 500px;
  }
  .copy-btn {
    vertical-align: top;
    margin-left: 10px;
  }
}
.title {
  font-weight: 600;
  margin-bottom: 20px;
}
::v-deep .el-form.el-form--inline .el-col .el-form-item__content > span {
  width: auto;
}
::v-deep .device-info {
  .el-textarea__inner {
    font-size: 12px;
    background: rgb(49, 48, 48) !important;
    color: #fff !important;
  }
}
</style>
