<template>
  <el-dialog top="5vh" :visible.sync="dialogVisible" :close-on-click-modal="false" @close="close" width="70%"
    append-to-body class="file-view-dialog">
    <div slot="title" class="dialog-title">
      <div>
        <span class="title-text">文件浏览器{{ store.state.project.currentProject.name + "/" + podName }}</span>
      </div>
      <span class="user-info">
        容器内用户身份：<el-tag type="primary">{{ user }}</el-tag>
      </span>
    </div>
    <div class="dialog-body" v-loading="loading">
      <el-card class="dir">
        <div class="tree-container">
          <el-tree ref="fileTree" node-key="path" :data="treeData" :props="defaultProps" @node-click="handleNodeClick"
            default-expand-all>
            <span slot-scope="{ node, data }" class="customize-tree-p">
              <i v-if="data.label == '/mnt'" class="el-icon-s-home" style="margin-right:5px" />
              <i v-else class="el-icon-folder" style="margin-right:5px" />
              <span>{{ data.label }}</span>
            </span>
          </el-tree>
        </div>
      </el-card>
      <el-card class="list">
        <div class="breadcrumb-row">
          <div :key="p" v-for="(p, index) in currentPath" class="breadcrumb-item">
            <i v-if="index > 0" class="el-icon-arrow-right" style="margin-right:5px"></i>
            <el-button @click="() => clickPath(p)" v-if="index == 0" icon="el-icon-s-home">/</el-button>
            <el-button v-else-if="index == currentPath.length - 1" type="primary" icon="el-icon-refresh"
              @click="() => clickPath(p)">{{ p.split("/").pop() }}</el-button>
            <el-button @click="() => clickPath(p)" v-else>{{ p.split("/").pop() }}</el-button>
          </div>
        </div>
        <div v-if="!showFile" class="toolbar-row">
          <div class="filter-area">
            <el-input v-model="form.fileName" placeholder="当前文件夹内名称过滤" style="width:350px;margin-right:10px" clearable />
            <el-checkbox v-model="form.showHidden" @change="changeShowHidden">显示隐藏</el-checkbox>
          </div>
          <div class="action-area">
            <FileSliceUpload v-if="dialogVisible" :isDrag="false" :beforeUpload="beforeUpload" :begin="begin"
              :upload="upload" :end="end" @success="onUploadSuccess">
              <template v-slot:controller>
                <el-button type="success">
                  <span v-if="percentage > 0">
                    <i class="el-icon-loading" style="margin-right:5px" />{{ percentage + "%" }}
                  </span>
                  <span v-else>上传文件</span>
                </el-button>
              </template>
              <template v-slot:progress><span></span></template>
            </FileSliceUpload>
            <el-button :disabled="selection.length == 0" type="primary" @click="showTarDialog" style="margin-left:10px">
              打包
            </el-button>
          </div>
        </div>
        <el-table ref="table" v-if="!showFile" :data="data.filter(row => row.fileName.includes(form.fileName))"
          @selection-change="syncSelection" row-key="path" max-height="590" style="width:100%" border>
          <el-table-column align="center" type="selection" width="70" reserve-selection />
          <el-table-column header-align="center" prop="fileName" label="文件夹/文件" sortable min-width="25%">
            <template slot-scope="scope">
              <p v-if="scope.row.permission.startsWith('d')" class="fileName" @click="toTheFlolder(scope.row.path)">
                <i class="el-icon-folder" style="margin-right:5px" />
                {{ scope.row.fileName }}
              </p>
              <p v-else>
                <i class="el-icon-document" style="margin-right:5px" />
                {{ scope.row.fileName }}
              </p>
            </template>
          </el-table-column>
          <el-table-column align="center" prop="permission" label="属性" min-width="10%" show-overflow-tooltip />
          <el-table-column align="center" prop="owner" label="Owner" min-width="10%" show-overflow-tooltip />
          <el-table-column align="center" prop="group" label="Group" min-width="10%" show-overflow-tooltip />
          <el-table-column align="center" prop="fileSize" label="文件大小" min-width="10%">
            <template slot-scope="scope">{{ Math.ceil(scope.row.fileSize / 1024) }}KB</template>
          </el-table-column>
          <el-table-column min-width="17%" align="center" prop="updateTime" sortable label="更新时间">
            <template slot-scope="scope">{{ timeConvert(scope.row.updateTime) }}</template>
          </el-table-column>
          <el-table-column min-width="10%" align="center" label="操作">
            <template slot-scope="scope">
              <el-button v-if="scope.row.permission.startsWith('-')" type="text"
                @click="showFileView(scope.row.path, scope.row.fileName, scope.row.permission, scope.row.viewMode)">查看</el-button>
              <el-button v-if="scope.row.permission.startsWith('-')" type="text"
                @click="download(scope.row.path)">下载</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div v-else>
          <el-button type="info" style="margin-bottom:10px" @click="closeFileContent">取消</el-button>
          <el-button type="success" style="margin-bottom:10px"
            @click="refreshFile(currentPath[currentPath.length - 1] + '/' + currentFileName, currentFileName)">
            重新加载
          </el-button>
          <CodeEditor v-model="cuttentFileContent" :options="getCmOptions(currentFileName)" />
        </div>
      </el-card>
      <TarDialog ref="tarDialog" @update="() => getFiles(currentPath[currentPath.length - 1])" />
    </div>
  </el-dialog>
</template>
<script>
import CodeEditor from "@/components/codeEditor";
import k8s_api from "@/api/app/k8s-container.js";
import store from "@/store";
import moment from "moment";
import TarDialog from "./tarDialog";
import FileSliceUpload from "@/utils/@eversec/multipart-upload"
import {
  PART_SIZE,
  initProjectFileUpload,
  getProjectFilePresignedUrl,
  completeProjectFileUpload,
  notifyProjectFilePartComplete,
  uploadProjectFilePartToS3,
  uploadProjectSmallFile,
  getProjectFileDownloadUrl
} from '@/api/llmops/storage-file'

export default {
  components: { CodeEditor, TarDialog, FileSliceUpload },
  data() {
    return {
      moment,
      store,
      dialogVisible: false,
      loading: false,
      podName: null,
      form: {
        fileName: "",
        showHidden: false
      },
      showFile: false,
      currentFileName: "",
      cuttentFileContent: "",
      selection: [],
      user: "root",
      currentPath: ["/"],
      mimeType: {
        properties: "text/x-properties",
        yaml: "text/x-yaml",
        html: "text/html",
        json: "application/json",
        xml: "text/xml"
      },
      data: [],
      treeData: [],
      defaultProps: {
        children: "children",
        label: "label"
      },
      percentage: 0,
      currentUploadId: '',
      currentObjectKey: '',
      uploadedParts: [],
      isSmallFile: false,
      currentProjectName: '',
      currentSafeName: '',
      currentTargetFilePath: ''
    };
  },
  computed: {
    timeConvert() {
      return time => {
        let date = moment(
          time,
          "YYYY-MM-DD HH:mm:ss.SSSSSSSSS Z"
        ).toISOString();
        return moment(date).format("YYYY-MM-DD HH:mm:ss");
      };
    }
  },
  methods: {
    beforeUpload(file) {
      const size = window.globalConfig.outlineExportPackageSize || 2;
      if (file.size > size * 1024 * 1024 * 1024) {
        this.$message({
          type: "error",
          message: `上传文件不能超过${size}Gi`
        });
        return false;
      }
      return true;
    },
    onUploadSuccess() {
      this.percentage = 0;
      this.getFiles(this.currentPath[this.currentPath.length - 1]);
    },
    async begin({ cancel, ...option }) {
      const projectName = (this.store && this.store.state && this.store.state.project && this.store.state.project.currentProject && this.store.state.project.currentProject.name) ? String(this.store.state.project.currentProject.name) : 'default';
      this.currentProjectName = projectName;
      
      const safeName = (option.fileName || 'file').replace(/\s+/g, '-').replace(/[^a-zA-Z0-9.\-_]/g, '') || 'file';
      this.currentSafeName = safeName;
      
      const pathPrefix = this.currentPath[this.currentPath.length - 1] === '/' ? '' : this.currentPath[this.currentPath.length - 1];
      this.currentTargetFilePath = `${pathPrefix}/${safeName}`;
      
      this.isSmallFile = option.fileSize < PART_SIZE;
      
      if (this.isSmallFile) {
        this.currentObjectKey = '';
        this.currentUploadId = '';
        this.uploadedParts = [];
        return {
          chunkCount: 1,
          uploadedChunk: 0
        };
      }
      
      const initRes = await initProjectFileUpload({ fileName: safeName }, { 'X-Project-Name': projectName });
      if (!initRes || !initRes.uploadId) {
        throw new Error('初始化分片上传失败');
      }
      this.currentObjectKey = initRes.objectKey;
      this.currentUploadId = initRes.uploadId;
      this.uploadedParts = [];

      return {
        chunkCount: Math.ceil(option.fileSize / option.chunkSize),
        uploadedChunk: 0
      };
    },
    async upload({ formData, percentage, cancel }) {
      this.$set(this, "percentage", percentage);
      
      const fileBlob = formData.get("file");
      
      if (this.isSmallFile) {
        const uploadData = new FormData();
        uploadData.append('file', new File([fileBlob], this.currentSafeName));
        const res = await uploadProjectSmallFile(uploadData, { 'X-Project-Name': this.currentProjectName });
        
        const downloadRes = await getProjectFileDownloadUrl({ objectKey: res.objectKey });
        await k8s_api.downloadFromUrl(this.podName, downloadRes.url, this.currentTargetFilePath);
        return;
      }
      
      const chunkIndex = parseInt(formData.get("chunkIndex"), 10);
      const partNumber = chunkIndex + 1;
      
      const presignedRes = await getProjectFilePresignedUrl({
        objectKey: this.currentObjectKey,
        uploadId: this.currentUploadId,
        partNumber: partNumber
      });
      if (!presignedRes || !presignedRes.url) {
        throw new Error(`获取预签名 URL 失败，分片 ${partNumber}`);
      }
      
      const putRes = await uploadProjectFilePartToS3(presignedRes.url, fileBlob);
      const headers = putRes.headers || {};
      const etag = (headers.get ? (headers.get('etag') || headers.get('ETag')) : (headers['etag'] || headers['ETag']) || '').replace(/"/g, '');
      if (!etag) {
        throw new Error(`分片 ${partNumber} 未返回 ETag`);
      }
      
      await notifyProjectFilePartComplete({
        uploadId: this.currentUploadId,
        partNumber: partNumber,
        etag: etag,
        partSize: fileBlob.size
      });
      
      this.uploadedParts.push({ partNumber, etag });
    },
    async end({ cancel, ...option }) {
      if (this.isSmallFile) {
        this.isSmallFile = false;
        return;
      }
      
      this.uploadedParts.sort((a, b) => a.partNumber - b.partNumber);
      const completeRes = await completeProjectFileUpload({
        objectKey: this.currentObjectKey,
        uploadId: this.currentUploadId,
        parts: this.uploadedParts
      });
      if (!completeRes) {
        throw new Error('合并分片失败');
      }
      
      const downloadRes = await getProjectFileDownloadUrl({ objectKey: this.currentObjectKey });
      await k8s_api.downloadFromUrl(this.podName, downloadRes.url, this.currentTargetFilePath);
      
      // 上传完毕后清空状态
      this.currentObjectKey = '';
      this.currentUploadId = '';
      this.uploadedParts = [];
    },
    changeShowHidden(e) {
      const path = this.currentPath[this.currentPath.length - 1];
      const targetNode = this.$refs.fileTree.getNode(path);
      this.getFiles(path, targetNode);
    },
    syncSelection(selection) {
      this.selection = selection;
    },
    async download(filePath) {
      try {
        const res = await k8s_api.downloadFile(this.podName, filePath)
        const blob = new Blob([res.data])
        const url = URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.href = url
        a.download = filePath.split('/').pop()
        a.click()
        URL.revokeObjectURL(url)
      } catch (e) {
        this.$message.error('下载失败')
      }
    },
    showTarDialog() {
      this.$refs.tarDialog.init(
        this.podName,
        this.selection,
        this.currentPath[this.currentPath.length - 1]
      );
    },
    clickPath(path) {
      const targetNode = this.$refs.fileTree.getNode(path);
      this.getFiles(path, targetNode.data);
    },
    async executeCommand(cmd) {
      const res = await k8s_api.executeCommand(this.podName, cmd);
      return res;
    },
    async getFiles(
      path,
      targetNodeData = this.$refs.fileTree.getNode(path).data
    ) {
      // 清空选择
      this.$refs.table && this.$refs.table.clearSelection();
      // 如果是文件浏览模式就关闭掉
      if (this.showFile) {
        this.showFile = false;
      }
      // 更新当前路径
      this.currentPath = ["/"];
      if (path !== "/") {
        const pathes = path.split("/");
        pathes.forEach((item, index) => {
          if (index > 0) {
            this.currentPath.push(
              (this.currentPath[index - 1] == "/"
                ? "/"
                : `${this.currentPath[index - 1]}/`) + item
            );
          }
        });
      }
      this.loading = true;
      const result = await k8s_api.getFiles(
        this.podName,
        path,
        this.form.showHidden
      );
      this.loading = false;
      targetNodeData.children = [];
      this.data = [];
      result.forEach(item => {
        if (item.permission.startsWith("d")) {
          // 构造当前节点的子树
          targetNodeData.children.push({
            path: (path == "/" ? "" : path) + "/" + item.fileName,
            label: item.fileName,
            children: [],
            data: item
          });
        }
        // 构造右侧文件列表
        this.data.push({
          path: (path == "/" ? "" : path) + "/" + item.fileName,
          fileName: item.fileName,
          permission: item.permission,
          owner: item.owner,
          group: item.group,
          fileSize: item.fileSize,
          updateTime: item.time,
          viewMode: item.viewMode
        });
      });
    },
    closeFileContent() {
      this.showFile = false;
    },
    getCmOptions(fileName) {
      let type = "properties";
      const file = fileName.split(".");
      if (file) {
        type = file.pop();
      }
      return {
        mode: this.mimeType[type],
        readOnly: this.readonly
      };
    },
    handleNodeClick(data, node) {
      this.getFiles(data.path, data);
    },
    async refreshFile(path, fileName) {
      this.cuttentFileContent = await k8s_api.getFileContent(
        this.podName,
        path
      );
      this.currentFileName = fileName;
      this.showFile = true;
    },
    toTheFlolder(path) {
      const targetNode = this.$refs.fileTree.getNode(path);
      targetNode && this.getFiles(path, targetNode.data);
    },
    showFileView(path, fileName, permission, viewMode) {
      switch (viewMode) {
        case 0:
          this.refreshFile(path, fileName);
          break;
        case 1:
          this.$message.warning("不支持的文件格式");
          break;
        case 2:
          this.$message.warning("文件大于1MB不能查看");
          break;
        default:
          this.$message.warning("该文件不能查看");
      }
    },
    close() {
      this.loading = false;
      this.form = {
        fileName: "",
        showHidden: false
      };
      this.showFile = false;
      this.selection = [];
      this.currentPath = ["/"];
      this.data = [];
      this.treeData = [];
      this.dialogVisible = false;
    },
    async init(podName) {
      this.podName = podName;
      // this.user = await this.executeCommand("whoami");
      const root = {
        path: "/mnt",
        label: "/mnt",
        children: [],
        data: {
          path: "/mnt",
          fileName: "/mnt",
          permission: "d",
          owner: "root",
          group: "root",
          fileSize: 0,
          updateTime: ""
        }
      };
      this.treeData = [];
      this.treeData.push(root);

      this.getFiles("/mnt", root);

      this.dialogVisible = true;
    }
  }
};
</script>
<style lang="scss">
.file-view-dialog .CodeMirror {
  height: 550px;
}
</style>
<style lang="scss" scoped>
.dialog-title {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
}

.title-text {
  font-size: 14px;
}

.user-info {
  font-size: 14px;
  margin-right: 50px;
}

.dialog-body {
  display: flex;
  flex-direction: row;
  align-items: stretch;
  height: 700px;
}

.breadcrumb-row {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  margin-bottom: 10px;
}

.breadcrumb-item {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.toolbar-row {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  margin-bottom: 10px;
}

.filter-area {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.action-area {
  display: flex;
  flex-direction: row;
  align-items: flex-end;
}

.dir {
  width: 370px;
  margin-right: 10px;
}

.list {
  width: 1030px;
}

.tree-container {
  width: 360px;
  height: 650px;
  overflow-y: scroll;
  overflow-x: scroll;
}

.fileName {
  cursor: pointer;
}

.lab-button {
  width: 80px;
  border: 1px solid #c2e7b0;
  border-radius: 5px;
  background-color: #f0f9eb;
}

.lab-button:focus,
.lab-button:hover {
  border-color: #c2e7b0;
  background-color: #67c23a;
}

#lab-file {
  font-size: 12px;
  padding: 10px 10px;
  line-height: 30px;
  color: #67c23a;
}

#lab-file:hover {
  color: white;
}

#file {
  display: none;
}
</style>
