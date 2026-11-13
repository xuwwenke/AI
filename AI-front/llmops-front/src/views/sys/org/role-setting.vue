<template>
  <el-dialog
    title="授权角色"
    :close-on-click-modal="false"
    :visible.sync="visible"
    @closed="reset"
  >
    <el-form
      v-loading="loading"
      :model="dataForm"
      :rules="dataRule"
      ref="dataForm"
      label-width="120px"
    >
      <el-form-item size="mini" prop="roles">
        <span slot="label">
          角色
          <el-tooltip placement="right">
            <i class="el-icon-question"></i>
            <div slot="content">
              一般用户不能修改自己的角色和所属组织
              <br />仅能设置您拥有的角色，以及下级角色
            </div>
          </el-tooltip>
        </span>
        <el-checkbox-group v-model="dataForm.roles">
          <el-checkbox
            v-for="role in roleList"
            :key="role.id"
            :label="role.id"
            >{{ role.name }}</el-checkbox
          >
          <el-checkbox
            v-for="role in disabledRoleList"
            :key="role.id"
            :label="role.id"
            disabled
            >{{ role.name }}</el-checkbox
          >
        </el-checkbox-group>
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
import api_org from "@/api/sys/org";
import api_role from "@/api/sys/role";

export default {
  data() {
    return {
      visible: false,
      loading: false,
      submitLoading: false,
      roleList: [], // 当前操作人可见的角色列表
      disabledRoleList: [], // 处于当前操作人可见的角色列表之外，但被修改用户拥有的角色
      dataForm: {
        id: null,
        roles: []
      },
      dataRule: {}
    };
  },
  methods: {
    init(id) {
      this.visible = true;
      // 修复焦点在带校验的项按回车提交后，再次打开对话框，该项会带有校验提示的问题
      // 而 resetFields 仍然放在关闭对话框时执行，以避免下次打开时，带动画的控件（如checkbox）会闪现之前的结果
      this.$nextTick(() => {
        this.$refs.dataForm.clearValidate();
      });

      this.loading = true;

      this.dataForm.id = id;

      if (this.dataForm.id != null) {
        Promise.all([
          api_org.getInfo(this.dataForm.id).catch(() => null),
          api_role.getList().catch(() => null)
        ])
          .then(res => {
            this.dataForm.roles =
              res && res[0] && res[0].roles ? res[0].roles.map(r => r.id) : [];
            this.roleList = res && res[1] ? res[1].resultData : [];
            if (this.dataForm.roles.length) {
              this.disabledRoleList = this.dataForm.roles.filter(x => {
                for (let y of this.roleList) {
                  if (x.id == y.id) {
                    return false;
                  }
                }
                return true;
              });
            } else {
              this.disabledRoleList = [];
            }
            console.log(this.dataForm, this.roleList);
          })
          .finally(() => {
            this.loading = false;
          });
      }
    },
    reset() {
      this.$refs.dataForm.resetFields();
    },
    // 表单提交
    dataFormSubmit() {
      this.$refs.dataForm.validate(valid => {
        if (valid) {
          this.submitLoading = true;
          api_org
            .replaceRoles(this.dataForm.id, this.dataForm.roles)
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
    }
  }
};
</script>
