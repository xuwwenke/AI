<template>
  <el-dialog
    title="版本详情"
    width="80%"
    top="10vh"
    :close-on-click-modal="false"
    :visible.sync="visible"
    append-to-body
  >
    <el-form @keyup.native.enter="search" label-width="auto">
      <el-form-item>
        <el-row>
          <el-col :span="10">
            <el-input
              v-model.trim="dataForm.searchVal"
              :placeholder="`请输入${getLabelByValue()}`"
              clearable
              maxlength="64"
            >
              <el-select
                v-model="dataForm.type"
                placeholder="请选择"
                slot="prepend"
                style="width: 100px;"
              >
                <el-option
                  v-for="item in options"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                >
                </el-option>
              </el-select>
              <el-button
                size="medium"
                @click="search"
                slot="append"
                icon="el-icon-search"
              ></el-button>
            </el-input>
          </el-col>
        </el-row>
      </el-form-item>
    </el-form>

    <el-table
      v-loading="isLoading"
      :data="versionList"
      stripe
      border
      style="width: 100%;"
      height="55vh"
    >
      <el-table-column prop="name" align="center" label="服务标识">
        <template slot-scope="scope">
          <el-tooltip
            :disabled="scope.row.name != '——'"
            effect="dark"
            placement="top"
            :content="tooltipContent"
          >
            <span class="pointer">{{ scope.row.name }}</span>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column prop="description" align="center" label="服务描述">
        <template slot-scope="scope">
          <el-tooltip
            :disabled="scope.row.description != '——'"
            effect="dark"
            placement="top"
            :content="tooltipContent"
          >
            <span class="pointer">{{ scope.row.description }}</span>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column
        prop="version"
        align="center"
        label="版本名称"
        width="210"
      >
        <template slot-scope="scope">
          <el-tooltip
            :disabled="scope.row.version != '——'"
            effect="dark"
            placement="top"
            :content="tooltipContent"
          >
            <span class="pointer">{{ scope.row.version }}</span>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column
        prop="build"
        align="center"
        label="构建日期"
        width="140"
        :formatter="timeFormatter"
      >
        <template slot-scope="scope">
          <el-tooltip
            :disabled="scope.row.build != '——'"
            effect="dark"
            placement="top"
            :content="tooltipContent"
          >
            <span class="pointer">{{ scope.row.build }}</span>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column
        prop="serviceType"
        align="center"
        label="服务类型"
        width="80"
      >
        <template slot-scope="scope">
          <el-tooltip
            :disabled="scope.row.serviceType != '——'"
            effect="dark"
            placement="top"
            :content="tooltipContent"
          >
            <span class="pointer">{{ scope.row.serviceType }}</span>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column prop="submit" align="center" label="提交号">
        <template slot-scope="scope">
          <el-tooltip
            :disabled="scope.row.submit != '——'"
            effect="dark"
            placement="top"
            :content="tooltipContent"
          >
            <span class="pointer">{{ scope.row.submit }}</span>
          </el-tooltip>
        </template>
      </el-table-column>
    </el-table>
  </el-dialog>
</template>

<script>
import api_version from "@/api/sys/version";
import { formatTime } from "@/utils/time";

export default {
  components: {},
  data() {
    return {
      visible: false,
      versionList: [],
      dataForm: {
        searchVal: "",
        type: "serName"
      },
      options: [
        { label: "服务标识", value: "serName" },
        { label: "服务类型", value: "serviceType" }
      ],
      isLoading: false,
      tooltipContent: "未识别"
    };
  },
  created() {

  },
  methods: {
    init() {
      this.visible = true;
      this.getList();
    },
    getList() {
      this.isLoading = true;
      let { type, searchVal } = this.dataForm;
      api_version
        .getVersionList({
          [type]: searchVal
        })
        .then(res => {
          this.isLoading = false;
          this.versionList = res.data || res
        })
        .catch(err => {
          this.isLoading = false;
        });
    },
    search() {
      this.getList();
    },
    getLabelByValue() {
      let item = this.options.find(it => it.value == this.dataForm.type);
      return item ? item.label : "";
    },
    timeFormatter(row, column, cellValue, index) {
      return formatTime(cellValue);
    }
  }
};
</script>
<style lang="scss" scoped>
.pointer {
  cursor: pointer;
}
</style>
