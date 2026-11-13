<template>
  <div class="container">
    <div class="card add-btn" @click="addOrUpdateHandle()">
      <div class="icon el-icon-plus"></div>
      <div class="name">创建自定义应用</div>
    </div>
    <CustomAppCard v-for="item in filteredCustomAppList" :key="item.key" :item="item"
      :addOrUpdateHandle="addOrUpdateHandle" :deleteHandle="deleteHandle" class="card" />
    <CustomAppForm ref="customAppForm" @onSubmit="getCustomAppList" />
  </div>
</template>

<script>
import api_customApp from "@/api/customApp"
import CustomAppCard from './CustomAppCard'
import CustomAppForm from './CustomAppForm'

export default {
  inject: ['getSearchStr'],
  components: {
    CustomAppCard,
    CustomAppForm,
  },
  data() {
    return {
      customAppList: [],
    }
  },
  computed: {
    searchStr() {
      return this.getSearchStr()
    },
    filteredCustomAppList() {
      const reg = new RegExp(this.searchStr, 'i')
      return this.customAppList.filter(item => item.appName.search(reg) !== -1 || (item.appDesc && item.appDesc.search(reg) !== -1))
    },
  },
  created() {
    if (window.globalConfig.showCustomApp) {
      this.getCustomAppList()
    }
  },
  methods: {
    getCustomAppList() {
      api_customApp.getList({
        paging: 1,
        limit: 1000,
      }).then(data => {
        this.dataListLoading = false
        this.customAppList = data.list
      }).catch(e => {
        this.dataListLoading = false;
      })
    },
    addOrUpdateHandle(row) {
      this.$refs.customAppForm.init({ ...row })
    },
    // 删除
    deleteHandle(id) {
      this.$confirm(`确定进行[删除]操作?`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(() => {
          api_customApp.delete(id).then(() => {
            this.getCustomAppList();
            this.$message({
              message: "操作成功",
              type: "success",
              duration: 1500,
            });
          });
        })
        .catch(() => { });
    },
  }
}
</script>

<style lang="scss" scoped>
@import '../var.scss';

.container {
  margin-right: -50px;
}

.card {
  margin: 0 35px 26px 0;
  vertical-align: middle;
}

.add-btn {
  position: relative;
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 252px;
  height: 260px;
  padding: 20px;
  border: 1px solid rgba(52, 143, 234, 0.2);
  background: #fff;
  border-radius: 8px;
  transition: all 0.3s;
  cursor: pointer;

  &:hover {
    border-color: $primary-color;
    box-shadow: 0px 5px 6px rgba(67, 107, 217, 0.2);
  }

  .icon {
    font-size: 50px;
  }

  .name {}
}
</style>
