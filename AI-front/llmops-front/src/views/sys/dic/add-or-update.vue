<template>
  <el-dialog
    :title="dataForm.id == null ? '新增' : '修改'"
    :close-on-click-modal="false"
    :visible.sync="visible"
    @closed="reset"
  >
    <el-form
      :model="dataForm"
      :rules="dataRule"
      ref="dataForm"
      label-width="140px"
    >
      <el-form-item label="字典类型" prop="type">
        <el-select v-model="dataForm.type" placeholder="字典类型" :disabled="dataForm.id!=null">
          <el-option
            v-for="item in typeList"
            :key="item.itemValue"
            :value="parseInt(item.itemValue)"
            :label="item.itemName"
          ></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="字典编码" prop="code">
        <el-input v-model="dataForm.code" placeholder="字典编码" :disabled="dataForm.id!=null"></el-input>
      </el-form-item>
      <el-form-item label="字典名称" prop="name">
        <el-input v-model="dataForm.name" placeholder="字典名称"></el-input>
      </el-form-item>
      <template v-if="showExtDicForm">
        <el-form-item label="数据源" prop="dsId">
          <el-select v-model="dataForm.dsId" @change="getTables" filterable>
            <el-option
              v-for="ds in dsList"
              :key="ds.dsId"
              :label="ds.aliasName || ds.dsName"
              :value="ds.dsId"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="数据表" prop="tableName">
          <el-select
            v-model="dataForm.tableName"
            @change="onSelectTable"
            :disabled="dataForm.dsId == null"
            filterable
          >
            <el-option
              v-for="tbl in tblList"
              :key="tbl.tblName"
              :value="tbl.tblName"
              :label="tbl.aliasName + (tbl.tblDesc ? ' - ' + tbl.tblDesc : '')"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="字典项值字段" prop="valueColumn">
          <el-select v-model="dataForm.valueColumn" :disabled="dataForm.tableName == null" filterable>
            <el-option
              v-for="col in columns"
              :key="col.key"
              :value="col.key"
              :label="col.key + (col.label ? ' - ' + col.label : '')"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="字典项名称字段" prop="nameColumn">
          <el-select v-model="dataForm.nameColumn" :disabled="dataForm.tableName == null" filterable>
            <el-option
              v-for="col in columns"
              :key="col.key"
              :value="col.key"
              :label="col.key + (col.label ? ' - ' + col.label : '')"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="字典层级" prop="hierarchy">
          <el-input-number
            v-model="dataForm.hierarchy"
            :min="1"
            :max="10"
            :precision="0"
            placeholder="层级"
            :disabled="dataForm.id != null"
          ></el-input-number>
        </el-form-item>
        <el-form-item label="字典项ID字段" prop="idColumn" v-if="dataForm.hierarchy>1">
          <el-select v-model="dataForm.idColumn" :disabled="dataForm.tableName == null" filterable>
            <el-option
              v-for="col in columns"
              :key="col.key"
              :value="col.key"
              :label="col.key + (col.label ? ' - ' + col.label : '')"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="上级字典项ID字段" prop="pidColumn" v-if="dataForm.hierarchy>1">
          <el-select v-model="dataForm.pidColumn" :disabled="dataForm.tableName == null" filterable>
            <el-option
              v-for="col in columns"
              :key="col.key"
              :value="col.key"
              :label="col.key + (col.label ? ' - ' + col.label : '')"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="服务器缓存" prop="useCache">
          <el-switch v-model="dataForm.useCache" :active-value="1" :inactive-value="0"></el-switch>
        </el-form-item>
      </template>
      <el-form-item label="字典层级" prop="hierarchy" v-else>
        <el-input-number
          v-model="dataForm.hierarchy"
          :min="1"
          :max="4"
          :precision="0"
          placeholder="层级"
          :disabled="dataForm.id != null"
        ></el-input-number>
      </el-form-item>
      <el-form-item label="备注" prop="comment">
        <el-input type="textarea" :rows="3" placeholder="备注" v-model="dataForm.comment"></el-input>
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitLoading" @click="dataFormSubmit">确定</el-button>
    </span>
  </el-dialog>
</template>

<script>
import api_dic from "@/api/sys/dic";
// import api_ds from "@/api/datasource/datasource";
import { omitStr } from "@/utils/base";

export default {
  created() {
    if (window.globalConfig.useBiDic) {
      const dsApi = "/datasource/datasource";
      import("@/api" + dsApi).then(module => {
        // console.log(module.default);
        this.api_ds = module.default;
        this.getDataSources();
      });
    }
  },
  computed: {
    // 显示外部字典相关表单   对应code中的字典项：111 外部字典
    showExtDicForm() {
      return window.globalConfig.useBiDic && this.dataForm.type == 111;
    }
  },
  data() {
    return {
      api_ds: null,
      visible: false,
      submitLoading: false,
      typeList: [],
      dataForm: {
        type: 1,
        hierarchy: 1,
        tableName: null,
        valueColumn: null,
        nameColumn: null,
        idColumn: null,
        pidColumn: null,
        useCache: 0
      },
      dataRule: {
        type: [
          { required: true, message: "字典类型不能为空", trigger: "blur" }
        ],
        code: [
          { required: true, message: "字典编码不能为空", trigger: "blur" }
        ],
        name: [
          { required: true, message: "字典名称不能为空", trigger: "blur" }
        ],
        hierarchy: [
          { required: true, message: "字典层级不能为空", trigger: "blur" }
        ],
        dsId: [
          { required: true, message: "数据源不能为空", trigger: "change" }
        ],
        tableName: [
          { required: true, message: "数据表不能为空", trigger: "change" }
        ],
        valueColumn: [
          { required: true, message: "字典项值字段不能为空", trigger: "change" }
        ],
        nameColumn: [
          { required: true, message: "字典项名称字段不能为空", trigger: "change" }
        ],
        idColumn: [
          { required: true, message: "字典项ID字段不能为空", trigger: "change" }
        ],
        pidColumn: [
          { required: true, message: "上级字典项ID字段不能为空", trigger: "change" }
        ]
      },
      dsList: [],
      tblList: [],
      columns: [],
      needInit: false
    };
  },
  methods: {
    init(row) {
      this.visible = true;
      this.typeList = this.$parent.typeList;
      this.$nextTick(() => {
        this.$refs.dataForm.clearValidate();
        this.dataForm = {
          type: 1,
          hierarchy: 1,
          tableName: null,
          valueColumn: null,
          nameColumn: null,
          idColumn: null,
          pidColumn: null,
          ...row
        };

        if (this.dataForm.dsId != null) {
          this.getTables(this.dataForm.dsId, false);
          this.needInit = true;
        }
      });
    },
    reset() {
      this.$refs.dataForm.resetFields();
    },
    // 表单提交
    dataFormSubmit() {
      this.$refs.dataForm.validate(valid => {
        if (valid) {
          this.submitLoading = true;

          const type = this.dataForm.id == null ? "add" : "update";
          if (this.dataForm.tblId) this.dataForm.tableId = this.dataForm.tblId;
          api_dic[type](this.dataForm)
            .then(() => {
              this.visible = false;
              this.submitLoading = false;
              //子组件触发父组件事件
              this.$emit("onSubmit");
              this.$message({
                message: "操作成功",
                type: "success",
                duration: 1500
              });
            })
            .catch(() => {
              this.submitLoading = false;
            });
        }
      });
    },
    // 获取数据源列表
    getDataSources() {
      this.api_ds &&
        this.api_ds.getList().then(data => {
          this.dsList = data;
        });
    },
    getTables(dsId, reset = true) {
      this.tblList = [];
      // this.tablesLoading = true;
      if (reset) {
        this.dataForm.tableName = null;
        this.dataForm.valueColumn = null;
        this.dataForm.nameColumn = null;
        this.dataForm.idColumn = null;
        this.dataForm.pidColumn = null;
        this.columns = [];
      }
      this.api_ds &&
        this.api_ds
          .getTables(dsId)
          .then(data => {
            this.tblList = data;
            // this.tablesLoading = false;

            // 初始化
            if (this.needInit) {
              this.needInit = false;
              this.onSelectTable(this.dataForm.tableName, false);
            }
          })
          .catch(() => {
            // this.tablesLoading = false;
          });
    },
    onSelectTable(tblName, reset = true) {
      this.columns = [];
      const dsId = this.dataForm.dsId;
      if (reset) {
        this.dataForm.valueColumn = null;
        this.dataForm.nameColumn = null;
        this.dataForm.idColumn = null;
        this.dataForm.pidColumn = null;
      }
      const tbl = this.tblList.find(item => item.tblName == tblName);
      this.dataForm.tableType = tbl.tblType;
      this.dataForm.tblId = tbl.tblId;
      this.api_ds &&
        this.api_ds
          .getColumns(
            dsId,
            tblName,
            tbl.schema,
            tbl.catlog,
            tbl.tblType,
            tbl.tblId
          )
          .then(data => {
            this.columns = data.map(item => {
              return {
                label: omitStr(item.colDesc),
                key: item.colName
              };
            });
          });

      this.$refs.dataForm.validateField("tableName");
    }
  }
};
</script>
