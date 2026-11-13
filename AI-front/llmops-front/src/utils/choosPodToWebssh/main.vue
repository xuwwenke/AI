<template>
  <el-dialog :visible.sync="visible" :close-on-click-modal="false" width="1000px" append-to-body title="容器组"
    @close="close">
    <el-table :data="list">
      <el-table-column align="center" label="名称" prop="name" min-width="120px" />
      <el-table-column v-if="showStatus" align="center" label="状态" min-width="90px">
        <template slot-scope="scope">
          {{ statusMap[scope.row.phase] || scope.row.phase }}
        </template>
      </el-table-column>
      <el-table-column label="就绪" prop="ready" align="center" min-width="90px">
        <template slot-scope="scope">
          <span> {{ scope.row.ready ? "是" : "否" }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="创建时间" prop="creationTimestamp" min-width="123px" />
      <el-table-column align="center" label="容器IP" prop="podIP" showOverflowTooltip min-width="150px" />
      <el-table-column align="center" label="主机IP" prop="hostIP" showOverflowTooltip min-width="150px" />
      <!-- <el-table-column align="center" label="原因" prop="reason" showOverflowTooltip min-width="120px" /> -->
      <el-table-column align="center" label="操作" min-width="120px">
        <template slot-scope="scope">
          <el-button type="text" :disabled="scope.row.phase !== 'Running'"
            @click="() => openWebssh('log', namespace, scope.row.name)">查看日志</el-button>
          <el-button type="text" :disabled="scope.row.phase !== 'Running'"
            @click="() => openWebssh('console', namespace, scope.row.name)">进入终端</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-dialog>
</template>

<script>
export default {
  data() {
    return {
      visible: true,
      statusMap: {
        Running: "运行中",
        Pending: "等待中",
        Deleting: "删除中",
        Failed: "失败",
        Succeeded: "已完成"
      },
      list: [],
      namespace: ""
    };
  },
  computed: {
    showStatus() {
      // 只要列表中有一个 Pod 还没完成（不是 Succeeded），就需要展示状态列来区分（运行中、失败等）
      // 如果全部都已经是 Succeeded 了，这一列就没有展示意义了，直接隐藏
      return this.list.some(item => item.phase !== 'Succeeded');
    }
  },
  methods: {}
};
</script>

<style></style>
