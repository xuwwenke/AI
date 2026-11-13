<!-- 项目管理 -->
<template>
  <div class="projectIndex">
    <page-title />
    <el-row type="flex" justify="space-between" :gutter="10">
      <el-col :span="6">
        <el-card :body-style="{ height: 'calc(100vh - 200px)' }" style="overflow: auto;">
          <el-row type="flex" justify="space-between">
            <el-col :span="12">项目列表</el-col>
            <el-col :span="12" class="tar">
              <!-- v-if="isAuth('sys.project.add')" -->
              <i
                v-if="isAuth('sys.project.add')"
                class="el-icon-plus pointer"
                style="color: #409eff"
                @click="add"
              />
            </el-col>
          </el-row>
          <el-input
            v-model="filterText"
            class="mt10 mb10"
            placeholder="输入项目名称进行过滤"
            clearable
            @keyup.enter.native="filterProject()"
          >
            <i slot="suffix"
            class="el-input__icon el-icon-search pointer" @click="filterProject()"></i>
          </el-input>
          <ul
            v-if="projectList && projectList.length > 0"
            v-loading="dataListLoading"
            class="list-wrapper"
          >
            <li
              v-for="(item,index) in projectList"
              :key="`${item.id}-${index}`"
              :title="item.projectChName"
              :class="{
                active: addOrUpdate.id === item.id,
                'list-item': true,
              }"
              @click="change(item)"
            >
              {{ item.projectChName }}
              <i
                title="编辑"
                v-if="isAuth('sys.project.edit')"
                class="el-icon-edit icon-edit"
                style="color: #409eff;margin-left:10px;"
                @click.stop="edit(item)"
              />
              <i class="el-icon-delete icon-del" @click.stop="delProject(item)" v-if="isAuth('sys.project.del')" title="删除"></i>
            </li>
          </ul>
          <div v-else class="no-tenant">暂无数据</div>
          <el-pagination
            class="mt10"
            small
            :current-page="params.paging"
            :page-size="params.limit"
            :total="total"
            layout="total, prev, pager, next"
            @current-change="currentChangeHandle"
          />
        </el-card>
      </el-col>
      <el-col :span="18">
        <el-card :body-style="{ height: 'calc(100vh - 200px)' }">
          <addOrUpdate
            :dataForm="addOrUpdate"
            :disabled.sync="disabled"
            @updateProject="getProjectList(params)"
            @addProject="addProject"
            @cancelChange="cancelChange"
            ref="addOrUpdate"
            v-loading="loading"
          />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import api from "@/api/sys/project";
import addOrUpdate from "./addOrUpdate";
import cloneDeep from "lodash/cloneDeep";
export default {
  name: 'projectIndex',
  mixins: [],
  components: {
    addOrUpdate
  },
  props: {},
  data () {
    return {
      disabled: true,
      addOrUpdate: {
        projectChName: '',
        projectUserAdminInfos: [],
        projectUserAllInfos:[],
        description: ''
      },
      addOrUpdateInit: {},
      filterText: "",
      dataListLoading: false,
      addDialog: false,
      params: {
        paging: 1,
        limit: 10,
      },
      total: 0,
      projectList: [],
      loading: false
    };
  },
  computed: {},
  watch: {},
  async created () {
    await this.getList()
  },
  mounted () {},
  methods: {
    addProject () {
      this.params.paging = 1
      this.getList()
    },
    // 取消
    cancelChange () {
      this.disabled = true
      if (this.addDialog && this.projectList && this.projectList.length) {
        this.initTree(cloneDeep(this.projectList[0]))
      } else {
        this.addOrUpdate = cloneDeep(this.addOrUpdateInit)
      }
    },
    async getList () {
      this.params.paging = 1
      let params = cloneDeep(this.params)
      if(this.filterText) {
        params.projectChName = this.filterText
      }
      await this.getProjectList(params)
      if (this.projectList && this.projectList[0]) {
        this.initTree(this.projectList[0]);
      }
    },
    async getProjectList(params) {
      let paramsForm = cloneDeep(params)
      if(this.filterText) {
        paramsForm.projectChName = this.filterText
      }
      let res = await api.getProjectList(paramsForm)
      this.total = res.totalRecord
      this.projectList = res.resultData
    },
    // 切换选中项目
    async change(data) {
      this.disabled = true;
      await this.getClickData(data)
    },
    async getClickData(data) {
      try{
        this.loading =true
        let res = await api.checkProject(data.id)
        this.loading = false
        this.addOrUpdate = {
          ...res,
          projectUserAdminInfos: res.managers,
          projectUserAllInfos: res.users
        }
        this.addOrUpdateInit = cloneDeep(this.addOrUpdate)
        this.$nextTick(() => {
          this.$refs.addOrUpdate.setUser()
        });
      }catch{
        this.loading = false
      }

    },
    // 删除项目
    delProject(project) {
      console.log("projct", project);
      this.$confirm(`确认删除项目【${project.projectChName}】?`).then((o) => {
        api
          .delProject(project.id)
          .then(async (res) => {
            this.$message.success("操作成功");
            // 删除和新增后选中第一页第一个
            await this.getList()
          });
      });
    },
    initTree(project) {
      if (this.projectList.length) {
        this.$nextTick((o) => {
          const node = project.projectChName
            ? this.projectList.find(
                (o) => o.projectChName == project.projectChName
              ) || this.projectList[0]
            : this.projectList[0];
          this.change(node);
        });
      }
    },
    add() {
      this.disabled = false;
      this.addOrUpdate = {};
      this.addDialog = true
      this.$nextTick((o) => {
        this.$refs.addOrUpdate.$refs.dataForm.clearValidate();
        this.$refs.addOrUpdate.setUser()
      });
    },
    edit(data) {
      this.addDialog = false
      this.disabled = false
      this.getClickData(data)
    },
    async filterProject() {
      await this.getList()
    },
    currentChangeHandle (paging) {
      this.params.paging = paging
      this.getProjectList(this.params)
    },
  }
}
</script>

<style scoped lang="scss">
.pointer {
  cursor: pointer;
}
::v-deep {
  .el-tree {
    height: calc(100vh - 300px);
    overflow-y: auto;
  }
  > .el-tree-node {
    border-top: 1px solid #ebeef5;
  }
  .el-tree-node__content {
    padding: 6px;
  }
}
.container {
  overflow: hidden;
}
.list-wrapper {
  li {
    position: relative;
  }
  .list-item {
    padding-left: 20px;
    padding-right: 20px;
    line-height: 40px;
    // color: #333;
    cursor: pointer;
    border-radius: 4px;
    overflow: hidden;/*超出部分隐藏*/
    text-overflow:ellipsis;/* 超出部分显示省略号 */
    white-space: nowrap;/*规定段落中的文本不进行换行 */
    &:hover {
      background: var(--sub-hover-bg);
    }
    &.active {
      background: var(--sub-active-bg, #ecf5ff);
    }
  }
  .icon-edit {
    cursor: pointer;
    position: absolute;
    right: 26px;
    top: 10px;
  }

  .icon-del {
    color: red;
    cursor: pointer;
    position: absolute;
    right: 5px;
    top: 10px;
  }
}
</style>
