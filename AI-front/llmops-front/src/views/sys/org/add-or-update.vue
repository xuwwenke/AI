<template>
  <el-dialog :title="dataForm.id == null ? '新增' : '修改'" :close-on-click-modal="false"
    :visible.sync="visible" @closed="reset">
    <el-form v-loading="loading" :model="dataForm" :rules="dataRule" ref="dataForm" label-width="120px">
      <el-form-item label="上级组织机构" prop="parentId">
        <el-select v-model="fixedParentId" placeholder="请选择" filterable clearable
          :disabled="dataForm.id != null && (dataForm.parentId == null || !parentMatching)">
          <el-option v-for="org in orgList" :key="org.id" :value="org.id" :label="org.name">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="组织机构名称" prop="name">
        <el-input v-model.trim="dataForm.name" placeholder="组织机构名称"></el-input>
      </el-form-item>
      <el-form-item label="组织机构编码" prop="code">
        <el-input v-model.trim="dataForm.code" placeholder="组织机构编码"></el-input>
      </el-form-item>
      <el-form-item label="组织机构描述" prop="desc">
        <el-input type="textarea" :rows="3" v-model="dataForm.desc" placeholder="请输入描述">
        </el-input>
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :disabled="loading" :loading="submitLoading"
        @click="dataFormSubmit">确定</el-button>
    </span>
  </el-dialog>
</template>

<script>
import api_org from '@/api/sys/org';

export default {
  data() {
    return {
      visible: false,
      loading: false,
      submitLoading: false,
      orgList: [],
      parentName: null,
      dataForm: {
        parentId: null,
        name: null
      },
      dataRule: {
        name: [{ required: true, message: '组织名称不能为空', trigger: 'blur' }]
      }
    };
  },
  computed: {
    parentMatching() {
      const parentId = this.dataForm.parentId;
      if (parentId != null && parentId != '') {
        return Boolean(this.orgList.find(item => item.id == parentId));
      }
      return false;
    },
    // 上级组织机构可能处于当前操作人可见的组织机构列表之外，这时直接显示 parent.name
    fixedParentId: {
      get() {
        if (!this.parentMatching) return this.parentName;
        return this.dataForm.parentId;
      },
      set(v) {
        this.dataForm.parentId = v;
      }
    }
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
        api_org.getInfo(this.dataForm.id).then(data => {
          if (data) {
            this.parentName = data.parent && data.parent.name;

            this.dataForm = {
              id: data.id,
              parentId: data.parent && data.parent.id,
              name: data.name,
              code: data.code,
              desc: data.desc
            };

            api_org
              .getList({
                /* bindCurrentUser: false, */
                maxResult: 1000,
                notStyle: data.style
              })
              .then(data => {
                this.loading = false;
                this.orgList = data ? data.resultData : [];
              });
          }
        });
      } else {
        api_org
          .getList({ /* bindCurrentUser: false, */ maxResult: 1000 })
          .then(data => {
            this.loading = false;
            this.orgList = data ? data.resultData : [];
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
            .insertOrUpdate(this.dataForm)
            .then(() => {
              this.visible = false;
              this.submitLoading = false;
              this.$emit('onSubmit');
              this.$message({
                message: '操作成功',
                type: 'success',
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
