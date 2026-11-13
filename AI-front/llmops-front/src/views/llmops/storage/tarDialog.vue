<template>
  <el-dialog
    top="5vh"
    :visible.sync="dialogVisible"
    :close-on-click-modal="false"
    @close="close"
    title="打包成tar文件"
    width="1350px"
    append-to-body
  >
    <div v-loading="loading" element-loading-text="打包中...">
      <div class="layout-row stretch h650">
        <el-card header="待打包的文件/文件夹" style="width: 50%" class="mr10">
          <div :key="item.path" v-for="item in form.selection" class="mb5">
            <el-tag>{{ item.path }}</el-tag>
          </div>
        </el-card>
        <el-card header="tar命令选项" style="width: 50%">
          <el-form :model="form" label-width="100px">
            <el-form-item label="是否压缩">
              <el-switch v-model="form.compress"></el-switch>
            </el-form-item>
            <el-form-item label="所在路径">
              <span>{{ form.currentPath }}</span>
            </el-form-item>
            <el-form-item label="tar包文件名">
              <el-input v-model="form.tarName" placeholder=""></el-input>
            </el-form-item>
            <el-form-item label="tar命令">
              <div class="tarCmd">{{ tarCmd }}</div>
            </el-form-item>
          </el-form>
        </el-card>
      </div>
      <div class="layout-row end">
        <el-button type="info" class="mb10" @click="close"> 取消 </el-button>
        <el-button type="success" class="mb10" @click="tar"> 打包 </el-button>
      </div>
    </div>
  </el-dialog>
</template>
<script>
import k8s_api from "@/api/app/k8s-container.js";

export default {
  data() {
    return {
      dialogVisible: false,
      podName: null,
      loading: false,
      form: {
        selection: [],
        compress: true,
        currentPath: null,
        tarName: `archive.tar.gz`
      }
    };
  },
  computed: {
    tarCmd() {
      let cmd = `cd ${this.form.currentPath} && tar ${
        this.form.compress ? "-cvzf" : "-cvf"
      } ${this.form.tarName}`;
      this.form.selection.forEach(file => {
        cmd = cmd + " " + file.fileName;
      });
      return cmd;
    }
  },
  methods: {
    async tar() {
      this.loading = true;
      await k8s_api.executeCommand(this.podName, this.tarCmd).catch(err => {
        this.loading = false;
      });
      this.loading = false;
      this.$message.success("打包成功");
      this.$emit("update");
    },
    close() {
      this.loading = false;
      this.dialogVisible = false;
    },
    init(podName, selection, currentPath) {
      this.podName = podName;
      this.form.currentPath = currentPath;
      this.form.selection = selection;
      this.dialogVisible = true;
    }
  }
};
</script>

<style lang="scss" scoped>
.tarCmd {
  width: 100%;
  height: 100px;
  border: 1px solid white;
  padding: 4px;
  color: white;
  border-radius: 8px;
  background-color: black;
  overflow-y: scroll;
}
</style>
