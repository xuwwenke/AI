<template>
  <el-dialog
    :title="dataForm.id == null ? '新增' : '修改'"
    :close-on-click-modal="false"
    :visible.sync="visible"
    @closed="reset"
  >
    <el-form
      v-loading="loading"
      :model="dataForm"
      :rules="dataRule"
      ref="dataForm"
      label-width="100px"
    >
      <el-form-item label="用户组名称" prop="name">
        <el-input v-model.trim="dataForm.name" placeholder="组名称"></el-input>
      </el-form-item>
      <el-form-item label="用户组类型" prop="type">
        <el-select
          v-model="dataForm.type"
          placeholder="请选择用户组类型"
          filterable
          class="fw"
        >
          <el-option
            v-for="item of groupTypeList"
            :key="item.key"
            :value="item.key"
            :label="item.value"
          ></el-option>
        </el-select>
      </el-form-item>
      <!-- <el-form-item label="所属组织" prop="org.id">
        <el-select
          v-model="dataForm.org.id"
          placeholder="请选择所属组织"
          filterable
          class="fw"
          :disabled="dataForm.id != null"
        >
          <el-option
            v-for="org in orgList"
            :key="org.id"
            :value="org.id"
            :label="org.name"
          ></el-option>
        </el-select>
      </el-form-item> -->
      <el-form-item
        :label="'管理员' + (adminVisibleCount > 1 ? '1' : '')"
        prop="administrator1"
      >
        <div class="row">
          <user-select v-model="dataForm.administrator1" single />
          <el-button
            icon="el-icon-plus"
            class="icon-btn ml5"
            :disabled="adminVisibleCount > 2"
            :title="adminVisibleCount > 2 ? '最多支持3位管理员' : ''"
            @click="addAdmin()"
          ></el-button>
        </div>
      </el-form-item>
      <el-form-item
        v-if="adminVisibleCount > 1"
        label="管理员2"
        prop="administrator2"
      >
        <div class="row">
          <user-select v-model="dataForm.administrator2" single />
          <el-button
            icon="el-icon-minus"
            class="icon-btn ml5"
            @click="removeAdmin(1)"
          ></el-button>
        </div>
      </el-form-item>
      <el-form-item
        v-if="adminVisibleCount > 2"
        label="管理员3"
        prop="administrator3"
      >
        <div class="row">
          <user-select v-model="dataForm.administrator3" single />
          <el-button
            icon="el-icon-minus"
            class="icon-btn ml5"
            @click="removeAdmin(2)"
          ></el-button>
        </div>
      </el-form-item>
      <el-form-item label="用户组描述" prop="desc">
        <el-input
          type="textarea"
          :rows="3"
          v-model="dataForm.desc"
          placeholder="请输入描述"
        ></el-input>
      </el-form-item>
      <el-form-item v-if="!dataForm.id" prop="joinMyself">
        <el-checkbox v-model="dataForm.joinMyself"
          >是否将我加入用户组</el-checkbox
        >
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">取消</el-button>
      <el-button
        type="primary"
        :disabled="loading"
        :loading="submitLoading"
        @click="dataFormSubmit"
        >确定</el-button
      >
    </span>
  </el-dialog>
</template>

<script>
import { cloneDeep } from "lodash";
import api_group from "@/api/sys/group";
import api_org from "@/api/sys/org";
import UserSelect from "@/components/user-select-system";
import { getCfgDicPromise } from "@/utils/cfgDic";
const defaultValues = {
  id: null,
  name: "",
  type: null,
  desc: "",
  administrator1: null,
  administrator2: null,
  administrator3: null,
  org: { id: null },
  joinMyself: null
};
const adminFields = ["administrator1", "administrator2", "administrator3"];
export default {
  components: { UserSelect },
  data() {
    const validateAdmin = ({ field }, value, callback) => {
      for (let name of adminFields) {
        if (value && field !== name && value === this.dataForm[name]) {
          callback(new Error("请勿重复设置管理员"));
          return;
        }
      }
      callback();
    };
    return {
      visible: false,
      loading: false,
      submitLoading: false,
      orgList: [],
      comcodeList: [],
      groupTypeList: [],
      dataForm: cloneDeep(defaultValues),
      dataRule: {
        name: [{ required: true, message: "用户组名称不能为空", trigger: "blur" }],
        ["org.id"]: [
          { required: true, message: "所属部门不能为空", trigger: "change" }
        ],
        administrator1: [{ validator: validateAdmin }],
        administrator2: [{ validator: validateAdmin }],
        administrator3: [{ validator: validateAdmin }]
      },
      adminVisibleCount: 1
    };
  },
  watch: {
    "dataForm.administrator1"() {
      this.validateAdmin("administrator1");
    },
    "dataForm.administrator2"() {
      this.validateAdmin("administrator2");
    },
    "dataForm.administrator3"() {
      this.validateAdmin("administrator3");
    }
  },
  created() {
    getCfgDicPromise("groupType").then(data => {
      this.groupTypeList = data || [];
    });
  },
  methods: {
    init(row) {
      this.visible = true;
      // 修复焦点在带校验的项按回车提交后，再次打开对话框，该项会带有校验提示的问题
      // 而 resetFields 仍然放在关闭对话框时执行，以避免下次打开时，带动画的控件（如checkbox）会闪现之前的结果
      this.$nextTick(() => {
        this.$refs.dataForm.clearValidate();
      });

      this.loading = true;

      this.dataForm.id = row.id;
      const pArr = [
        api_org
          .getList({ /* bindCurrentUser: false, */ maxResult: 1000 })
          .then(data => {
            this.orgList = (data && data.resultData) || [];
          })
          .catch()
      ];
      if (Number.isFinite(row.id)) {
        pArr.push(
          api_group
            .getDetail(row.id)
            .then(data => {
              this.dataForm = Object.assign(cloneDeep(defaultValues), data);
              this.adminVisibleCount = this.dataForm.administrator3
                ? 3
                : this.dataForm.administrator2
                ? 2
                : 1;
            })
            .catch(() => {
              this.dataForm = cloneDeep(defaultValues);
            })
        );
      }
      Promise.all(pArr).finally(() => {
        this.loading = false;
      });
    },
    reset() {
      this.dataForm = cloneDeep(defaultValues);
    },
    // 表单提交
    dataFormSubmit() {
      this.$refs.dataForm.validate(valid => {
        if (valid) {
          this.submitLoading = true;

          const {
            id,
            name,
            type,
            desc,
            org,
            administrator1,
            administrator2,
            administrator3,
            joinMyself
          } = this.dataForm;
          const opt = this.dataForm.id == null ? "insert" : "update";
          const tmp = {
            id,
            name,
            type,
            desc,
            administrator1,
            administrator2,
            administrator3,
            joinMyself
          };
          tmp["org.id"] = org && org.id;
          api_group[opt](tmp)
            .then(() => {
              this.visible = false;
              this.submitLoading = false;
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
    validateAdmin(self) {
      if (!this.$refs.dataForm) {
        return;
      }
      adminFields.forEach(f => {
        if (!self || self !== f) {
          this.$refs.dataForm.validateField(f);
        }
      });
    },
    addAdmin() {
      if (this.adminVisibleCount < adminFields.length) {
        this.adminVisibleCount++;
      }
    },
    removeAdmin(index) {
      const form = this.dataForm;
      for (let i = index; i < adminFields.length; i++) {
        if (i + 1 < adminFields.length) {
          form[adminFields[i]] = form[adminFields[i + 1]];
        } else {
          form[adminFields[i]] = null;
        }
      }
      if (this.adminVisibleCount > 1) {
        this.adminVisibleCount--;
      }
    }
  }
};
</script>
<style lang="scss" scoped>
.row {
  display: flex;
  flex-direction: row;
}
</style>
