<template>
  <div class="dib">
    <el-upload
      v-bind="$attrs"
      :drag="isDrag"
      ref="upload"
      action="/"
      :http-request="chooseFile"
      :before-upload="_beforeUpload"
      :on-remove="handleRemove"
      :show-file-list="false"
    >
      <slot name="controller">
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
      </slot>
      <slot name="tip"> </slot>
    </el-upload>
    <slot name="progress">
      <div class="file-list" v-if="showFileName">
        <span class="file-name">{{ showFileName }}</span>
        <span>
          <span class="el-icon-loading file-status" v-if="loading"></span>
          <span
            class="text-success el-icon-circle-check file-status"
            v-else
          ></span>
          <span class="el-icon-close" @click="handleRemove"></span>
        </span>
      </div>

      <el-progress
        class="dib text-left"
        v-if="fileName && !filePath"
        style="width: 360px"
        :percentage="percentage"
      ></el-progress>
    </slot>
  </div>
</template>

<script>
import SparkMD5 from "spark-md5";

const chunkSize = 5 * 1024 * 1024; // Read in chunks of 5MB;
const BYTES_PER_SLICE = 1024 * 1024 * 5;

const statusMap = {
  READ_STREAM: "READ_STREAM",
  UPLOADING: "UPLOADING",
  UPLOAD_END: "UPLOAD_END",
  MERGING: "MERGING",
  SUCCESS: "SUCCESS",
  FAILED: "FAILED"
};

export default {
  props: {
    isAutoEnd: {
      type: Boolean,
      default: true
    },
    isDrag: {
      type: Boolean,
      default: true
    },

    beforeUpload: Function,
    begin: Function,
    upload: Function,
    end: Function,
    existFileName: String
  },

  data() {
    return {
      loading: false,
      fileName: null,
      filePath: null,
      percentage: 0,
      hasSendNum: 0,
      currentSliceIndex: 1,
      totalSlices: null, //总切片
      totalSize: null,
      code: null,
      BYTES_PER_SLICE: 1024 * 1024 * 5,
      status: ""
    };
  },

  watch: {
    percentage(val) {
      this.$emit("onPercentage", val);
    },
    status(val) {
      this.$emit("onStatus", val);
    }
  },

  computed: {
    showFileName() {
      return this.fileName || this.existFileName;
    }
  },

  beforeDestroy() {
    this.clearFiles();
  },

  methods: {
    getFileName() {
      return this.showFileName;
    },
    handleCancel() {
      this.cancel && this.cancel();
      this.cancel = null;
    },

    async _beforeUpload(...prop) {
      try {
        let customFlag = true;
        if (this.beforeUpload) {
          customFlag = await this.beforeUpload(...prop);
        }

        const flag = await this.checkExistFile();
        if (!flag || !customFlag) {
          this.clearFiles();
        }

        return flag && customFlag
          ? Promise.resolve(true)
          : Promise.reject(false);
      } catch (error) {
        this.clearFiles();
        return Promise.reject(error);
      }
    },

    async checkExistFile() {
      if (!this.showFileName) return true;
      try {
        await this.$confirm("已存在文件，是否继续上传覆盖当前文件", "提示", {
          type: "warning"
        });
        this.clearFiles();
        return true;
      } catch (error) {
        return false;
      }
    },

    clearFiles() {
      this.loading = false;
      this.handleCancel();

      this.code = "";
      this.percentage = 0;
      this.hasSendNum = 0;
      this.currentSliceIndex = 1;
      this.fileName = null;
      this.filePath = null;
      this.totalSlices = null;
      this.totalSize = null;
      this.$refs.upload && this.$refs.upload.clearFiles();
      this.$emit("clear");
    },

    chooseFile(data) {
      this.loading = true;
      const file = data.file;
      this.percentage = 0;
      this.status = statusMap.READ_STREAM;

      this.fileName = file.name;
      let blobSlice =
        File.prototype.slice ||
        File.prototype.mozSlice ||
        File.prototype.webkitSlice;

      let chunks = Math.ceil(file.size / chunkSize);
      let currentChunk = 0;
      let spark = new SparkMD5.ArrayBuffer();
      let fileReader = new FileReader();

      function loadNext() {
        let start = currentChunk * chunkSize;
        let end =
          start + chunkSize >= file.size ? file.size : start + chunkSize;
        fileReader.readAsArrayBuffer(blobSlice.call(file, start, end));
      }

      fileReader.onload = async e => {
        spark.append(e.target.result);
        currentChunk++;
        if (currentChunk < chunks) {
          loadNext();
        } else {
          this.code = spark.end();
          try {
            const res = await this.begin({
              fileMd5: this.code,
              fileSize: file.size,
              fileName: this.fileName,
              chunkSize: BYTES_PER_SLICE,
              cancel: cancel => (this.cancel = cancel)
            });

            if (res) {
              this.totalSlices = res.chunkCount;
              this.currentSliceIndex = res.uploadedChunk;

              if (this.totalSlices === this.currentSliceIndex) {
                this.status = statusMap.UPLOAD_END;
                this.percentage = 100;

                return this.isAutoEnd && this.uploadEnd();
              }

              this.commit(file);
              return;
            }

            this.clearFiles();
            return;
          } catch (error) {
            this.status = statusMap.FAILED;
            console.log("error:", error);
          }
        }
      };

      fileReader.onerror = function () {
        console.warn("fileReader, something was wrong.");
      };

      loadNext();
    },

    async commit(file) {
      if (!file) {
        this.$message({
          type: "warning",
          message: "请先选择文件!"
        });
        return;
      }

      if (file.size == 0) {
        this.$message({
          message: "文件为空无法上传，请重新选择文件",
          type: "warning",
          duration: 1500
        });
        return;
      }
      // 分片的开始、结束索引
      var start, end;
      this.status = statusMap.UPLOADING;

      while (this.status === statusMap.UPLOADING) {
        if (this.currentSliceIndex < this.totalSlices) {
          start = this.currentSliceIndex * BYTES_PER_SLICE;
          end = start + BYTES_PER_SLICE;
          var slice = file.slice(start, end); //切割文件
          await this.uploadFile(slice, this.currentSliceIndex++, this.fileName);
        }
      }
    },

    async uploadFile(slice, index, fileName) {
      var formData = new FormData();
      formData.append("fileMd5", this.code);
      formData.append("fileName", fileName);
      formData.append("chunkIndex", index);
      formData.append("file", slice);

      try {
        await this.upload({
          formData,
          percentage: this.percentage,
          cancel: cancel => {
            this.cancel = cancel;
          }
        });
        this.percentage = Math.floor(((index + 1) / this.totalSlices) * 100);
        this.percentage > 100 ? 100 : this.percentage;
        if (index === this.totalSlices - 1) {
          this.status = statusMap.UPLOAD_END;
          this.percentage = 100;

          if (this.isAutoEnd) {
            this.uploadEnd();
          }
        }
      } catch (error) {
        console.log("error:", error);
        this.status = statusMap.FAILED;
      }
    },
    // 合并分片
    async uploadEnd() {
      try {
        if (!this.fileName) return;
        this.status = statusMap.MERGING;

        const res = await this.end({
          fileName: this.fileName,
          fileMd5: this.code,
          cancel: cancel => (this.cancel = cancel)
        });

        this.status = statusMap.SUCCESS;
        this.$message.success("上传成功");
        this.$emit("success", res);
      } catch (error) {
        console.log("error:", error);
        this.status = statusMap.FAILED;
      }
      this.clearFiles();
    },
    handleRemove() {
      this.status = "";
      this.clearFiles();
      this.$emit("remove");
    }
  }
};
</script>

<style lang="scss" scoped>
.dib {
  display: inline-block;
}

.file-list {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #606266;

  .el-icon-close {
    display: none;
  }

  .file-status {
    display: inline-block;
  }

  &:hover {
    background-color: #f5f7fa;
    cursor: pointer;

    .file-name {
      color: #409eff;
    }

    .el-icon-close {
      display: inline-block;
    }

    .file-status {
      display: none;
    }
  }
}
</style>
