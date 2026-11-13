<!--  -->
<template>
  <div class="projectAdd">
    <el-form
      :model="dataForm"
      ref="dataForm"
      :rules="dataFormRules"
      label-width="100px"
      
    >
      <el-form-item label="项目名称" prop="projectChName">
        <el-input v-model.trim="dataForm.projectChName" maxlength="30" :disabled="disabled"/>
      </el-form-item>
      <el-form-item label="管理员" prop="projectUserAdminInfos">
        <el-select
          v-model="listAdmin"
          @change="setUser()"
          placeholder="请选择"
          multiple
          filterable
          clearable
          style="width: 100%"
          :disabled="disabled"
        >
          <el-option
            v-for="item in admins"
            :key="`${item.id}-${dataForm.id||'admin'}`"
            :label="`${item.userName}${item.realName ? `-${item.realName}` : ''}`"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="普通用户" prop="projectUserAllInfos">
        <el-select
          v-model="listUser"
          @change="setUser()"
          placeholder="请选择"
          :disabled="disabled"
          multiple
          filterable
          clearable
          style="width: 100%"
        >
          <el-option
            v-for="item in users"
            :key="`${item.id}-${dataForm.id||'user'}`"
            :label="`${item.userName}${item.realName ? `-${item.realName}` : ''}`"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="信息描述">
        <el-input
          type="textarea"
          maxlength="200"
          rows="3"
          v-model.trim="dataForm.description"
          :disabled="disabled"
          show-word-limit
        />
      </el-form-item>

      <el-form-item v-if="!disabled">
        <el-button type="primary" @click="update" :loading="loading"
          >保存</el-button
        >
        <el-button @click="cancel">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import apiUser from "@/api/sys/user";
import api from "@/api/sys/project";
export default {
  name: 'projectAdd',
  mixins: [],
  components: {},
  props: ["dataForm", "disabled"],
  data () {
    return {
      dataFormRules: {
        appKeys: [{ required: true, message: "该字段必填" }],
        projectChName: [{ required: true, message: "该字段必填", trigger: 'blur' }],
        // projectUserAdminInfos: [{ required: true, message: "该字段必填"}],
        // projectUserAllInfos: [{ required: true, message: "该字段必填"}],
      },
      allUser: [],
      admins: [],
      users: [],
      loading: false,
    }
  },
  computed: {
    listUser: {
      get() {
        const list = this.dataForm.projectUserAllInfos;
        return Array.isArray(list)
          ? list.map((o) => {
              return o.userid || (o.id && String(o.id));
            })
          : [];
      },
      set(ids) {
        const users = ids.map((id) => {
          const user = this.allUser.find((o) => o.id == id);
          if (!user) return { userid: id };
          return {
            ...user,
            userid: user.id,
            username: user.userName,
          };
        });
        this.$set(this.dataForm, "projectUserAllInfos", users);
      },
    },
    listAdmin: {
      get() {
        const list = this.dataForm.projectUserAdminInfos;
        return Array.isArray(list)
          ? list.map((o) => {
              return  o.userid || (o.id && String(o.id));
            })
          : [];
      },
      set(ids) {
        const users = ids.map((id) => {
          const user = this.allUser.find((o) => o.id == id);
          if (!user) return { userid: id };
          return {
            ...user,
            userid: user.id,
            username: user.userName,
          };
        });
        this.$set(this.dataForm, "projectUserAdminInfos", users);
      },
    },
  },
  created () {
    this.getAllUser();
  },
  mounted () {},
  methods: {
    update() {
      this.$refs.dataForm.validate((valid) => {
        if (valid) {
          const operation = this.dataForm.id ? "updateProject" : "addProject";
          let adminMember = '';
          if (this.dataForm.projectUserAdminInfos) adminMember = this.dataForm.projectUserAdminInfos.map(item => item.id)
          let userMember = '';
          if (this.dataForm.projectUserAllInfos) userMember = this.dataForm.projectUserAllInfos.map(item => item.id)

          let formData=new FormData()
          formData.append("description",this.dataForm.description || '');
          formData.append("projectChName",this.dataForm.projectChName);
          formData.append("managerIds",adminMember.toString());
          formData.append("userIds",userMember.toString());
          if(this.dataForm.id) {
            formData.append("id",this.dataForm.id);
          }

          this.loading = true;
          api[operation](formData)
            .then((res) => {
              let type = this.dataForm.id ? 'updateProject' : 'addProject'
              this.$emit(type)
              this.loading = false;
              this.$emit("update:disabled", true);
            })
            .catch((err) => {
              this.loading = false;
            });
        }
      });
    },
    cancel() {
      this.$refs.dataForm.clearValidate();
      this.$emit('cancelChange')
    },
    getAllUser() {
      apiUser
        .getList({
          fetchOrg: true,
          fetchRole: true,
          startPosition: 0,
          maxResult: 1000,
        })
        .then((data) => {
          this.allUser = data.resultData && data.resultData.map(item => {
            return {
              ...item,
              id: String(item.id)
            }
          });
          this.setUser();
        });
    },
    // 获取管理员和用户
    setUser(parent) {
      this.admins = this.allUser.filter(
        (o) => !this.listUser.some((u) => u == o.id)
      );
      this.users = this.allUser.filter(
        (o) => !this.listAdmin.some((u) => u == o.id)
      );
    },
  }
}
</script>

<style scoped lang="scss"></style>
