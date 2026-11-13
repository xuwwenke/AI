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
      <el-form-item label="用户名" prop="userName">
        <el-input
          v-model.trim="dataForm.userName"
          :disabled="dataForm.id != null"
          placeholder="用户名"
        ></el-input>
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input
          v-model.trim="dataForm.password"
          show-password
          autocomplete="new-password"
          :placeholder="
            dataForm.id == null
              ? '密码（留空表示使用默认密码）'
              : '密码（留空表示不修改密码）'
          "
        ></el-input>
      </el-form-item>
      <el-form-item label="真实姓名" prop="realName">
        <el-input
          v-model.trim="dataForm.realName"
          placeholder="真实姓名"
        ></el-input>
      </el-form-item>
      <el-form-item label="手机号" prop="tel">
        <el-input v-model.trim="dataForm.tel" placeholder="手机号"></el-input>
      </el-form-item>
      <el-form-item label="电子邮件" prop="email">
        <el-input v-model.trim="dataForm.email" placeholder="邮箱"></el-input>
      </el-form-item>
      <el-form-item label="岗位" prop="jobs">
        <el-select v-model="dataForm.jobs" placeholder="请选择岗位" filterable clearable>
          <el-option
            v-for="item in jobList"
            :key="item.key"
            :value="item.key"
            :label="item.value"
          ></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="职级" prop="rank">
        <el-select v-model="dataForm.rank" placeholder="请选择职级" filterable clearable>
          <el-option
            v-for="item in rankList"
            :key="item.key"
            :value="item.key"
            :label="item.value"
          ></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="状态" size="mini" prop="isValid">
        <el-radio-group v-model="dataForm.isValid">
          <el-radio v-for="s in statusList" :key="s.value" :label="s.value">{{
            s.label
          }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="所属组织" prop="orgIds">
        <el-select
          v-model="dataForm.orgIds"
          placeholder="请选择所属组织"
          multiple
          filterable
          class="fw"
          :disabled="btnDisabled"
        >
          <el-option
            v-for="org in orgList"
            :key="org.id"
            :value="org.id"
            :label="org.alias"
          ></el-option>
        </el-select>
      </el-form-item>
      <el-form-item size="mini" prop="roleIds">
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
        <el-checkbox-group v-model="dataForm.roleIds" :disabled="btnDisabled">
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
      <el-form-item label="用户归属" prop="comcode">
        <el-select
          v-model="dataForm.comcode"
          placeholder="请选择用户归属"
          filterable
          clearable
        >
          <el-option
            v-for="comcode in comcodeList"
            :key="comcode.key"
            :value="comcode.key"
            :label="comcode.value"
          ></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="失效时间" prop="expirationDate">
        <span slot="label">
          失效时间
          <el-tooltip placement="right">
            <i class="el-icon-question"></i>
            <div slot="content">
              控制账号的失效日期，账号失效后将无法登陆，不填写代表长期有效
            </div>
          </el-tooltip>
        </span>
        <el-date-picker
          v-model="dataForm.expirationDate"
          type="datetime"
          placeholder="请选择失效时间"
          value-format="timestamp"
        >
        </el-date-picker>
      </el-form-item>
      <el-form-item label="直属领导" prop="leaderLeve1">
        <user-select v-model="dataForm.leaderLeve1" single />
      </el-form-item>
      <el-form-item label="分管领导" prop="leaderLeve2">
        <user-select v-model="dataForm.leaderLeve2" single />
      </el-form-item>
      <el-form-item label="主管领导" prop="leaderLeve3">
        <user-select v-model="dataForm.leaderLeve3" single />
      </el-form-item>
      <el-form-item label="用户描述" prop="userDesc">
        <el-input
          type="textarea"
          :rows="3"
          v-model="dataForm.userDesc"
          placeholder="请输入描述"
        ></el-input>
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
import api_user from "@/api/sys/user";
import api_org from "@/api/sys/org";
import api_role from "@/api/sys/role";
import api_auth from "@/api/auth";
import api_app from "@/api/sys/app";
import { password, mobile } from "@/utils/validate";
import { getCfgDicPromise } from "@/utils/cfgDic";
import RSAUtils from "@/utils/rsaUtil";
import UserSelect from "@/components/user-select-system";

export default {
  components: { UserSelect },
  props: {
    statusList: Array,
  },
  data() {
    var validatePass = (rule, value, callback) => {
      if (value && !password(value)) {
        callback(
          new Error(
            "密码长度10~32位，必须包含“大写字母、小写字母、数字、特殊字符”中的三类"
          )
        );
      } else {
        callback();
      }
    };
    var validateMobile = (rule, value, callback) => {
      if (value && !mobile(value)) {
        callback(new Error("手机号格式错误"));
      } else {
        callback();
      }
    };
    return {
      visible: false,
      loading: false,
      submitLoading: false,
      orgList: [],
      roleList: [], // 当前操作人可见的角色列表
      jobList: [], // 岗位列表
      rankList: [], // 职级列表
      disabledRoleList: [], // 处于当前操作人可见的角色列表之外，但被修改用户拥有的角色
      comcodeList: [],
      dataForm: {
        userName: null,
        password: null,
        realName: null,
        tel: null,
        email: null,
        isValid: 0,
        orgIds: [],
        roleIds: [],
        jobs: null,
        rank: null,
        comcode: null,
        expirationDate: null,
        leaderLeve1: null,
        leaderLeve2: null,
        leaderLeve3: null,
        userDesc: null,
      },
      dataRule: {
        userName: [
          { required: true, message: "用户名不能为空", trigger: "blur" },
        ],
        password: [{ validator: validatePass, trigger: "blur" }],
        tel: [
          // { required: true, message: "手机号不能为空", trigger: "blur" },
          { validator: validateMobile, trigger: "blur" },
        ],
        email: [{ type: "email", message: "邮箱格式错误", trigger: "blur" }],
        isValid: [{ required: true, message: "状态不能为空", trigger: "blur" }],
        orgIds: [
          { required: true, message: "所属组织不能为空", trigger: "blur" },
        ],
        roleIds: [{ required: true, message: "角色不能为空", trigger: "blur" }],
      },
    };
  },
  computed: {
    btnDisabled() {
      // 不能修改自己的组织机构和角色（admin除外）
      if (!window.globalCache.userInfo) return false;
      const userName = window.globalCache.userInfo.userName;
      return userName !== "admin" && userName === this.dataForm.userName;
    },
  },
  methods: {
    init(row, comcodeList) {
      this.visible = true;
      this.comcodeList = comcodeList;
      // 修复焦点在带校验的项按回车提交后，再次打开对话框，该项会带有校验提示的问题
      // 而 resetFields 仍然放在关闭对话框时执行，以避免下次打开时，带动画的控件（如checkbox）会闪现之前的结果
      this.$nextTick(() => {
        this.$refs.dataForm.clearValidate();
      });

      this.loading = true;

      this.dataForm.id = row.id;
      Promise.all([
        api_org
          .getList({ /* bindCurrentUser: false, */ maxResult: 1000 })
          .catch(() => null),
        api_role.getList().catch(() => null),
        getCfgDicPromise("jobs").catch(() => null),
        getCfgDicPromise("rank").catch(() => null),
      ])
        .then((res) => {
          this.orgList = res && res[0] ? res[0].resultData : [];
          this.roleList = res && res[1] ? res[1].resultData : [];
          this.jobList = res && res[2] ? res[2] : [];
          this.rankList = res && res[3] ? res[3] : [];
          if (row.roles) {
            this.disabledRoleList = row.roles.filter((x) => {
              for (let y of this.roleList) {
                if (x.id == y.id) {
                  return false;
                }
              }
              return true;
            });
          }

          if (this.dataForm.id != null) {
            // 配合系统管理接口修改，防止丢失数据
            // const dataForm = JSON.parse(JSON.stringify(row));
            // dataForm.orgIds = row.orgs ? row.orgs.map((item) => item.id) : [];
            // dataForm.roleIds = row.roles
            //   ? row.roles.map((item) => item.id)
            //   : [];
            // delete dataForm.orgs;
            // delete dataForm.org;
            // delete dataForm.roles;
            // this.dataForm = dataForm;

            // 改成常规写法，避免密码输入框无法输入的问题（点修改，取消修改，再点新增，密码输入框无法输入）
            this.dataForm = {
              id: row.id,
              userName: row.userName,
              password: row.password,
              realName: row.realName,
              tel: row.tel,
              email: row.email,
              isValid: row.isValid,
              orgIds: row.orgs ? row.orgs.map((item) => item.id) : [],
              roleIds: row.roles ? row.roles.map((item) => item.id) : [],
              jobs: row.jobs,
              rank: row.rank,
              comcode: row.comcode,
              expirationDate: row.expirationDate,
              leaderLeve1: row.leaderLeve1,
              leaderLeve2: row.leaderLeve2,
              leaderLeve3: row.leaderLeve3,
              userDesc: row.userDesc,
            };
          }
        })
        .finally(() => {
          this.loading = false;
        });
    },
    reset() {
      this.$refs.dataForm.resetFields();
      this.roleList = [];
      this.disabledRoleList = [];
    },
    // 表单提交
    async dataFormSubmit() {
      if (window.globalConfig.verifyAuth) {
        //加授权权限校验
        try{
          let data = await api_auth.verifyAuth();
          if(!data) return this.$message.warning('license未授权或已过期，请联系系统管理员');
        } catch(err) {
          return false;
        }
      }
      this.$refs.dataForm.validate(async (valid) => {
        if (valid) {
          this.submitLoading = true;

          const tmp = { ...this.dataForm };

          // 当密码不为空时，对密码加密
          if (this.dataForm.password) {
            try {
              const resData = await api_auth.getKeyPair();
              const publicKey = RSAUtils.getKeyPair(
                resData.exponent,
                "",
                resData.modulus
              );

              tmp.password = RSAUtils.encryptedString(
                publicKey,
                this.dataForm.password
              );
            } catch (e) {
              console.log(e);
            }
          }

          tmp.orgId = tmp.orgIds.join(",");
          tmp.roleIds = tmp.roleIds.join(",");
          delete tmp.orgIds;

          const opt = this.dataForm.id == null ? "insert" : "update";

          api_user[opt](tmp)
            .then(() => {
              this.visible = false;
              this.submitLoading = false;
              this.$emit("onSubmit");
              this.$message({
                message: "操作成功",
                type: "success",
                duration: 1500,
              });
            })
            .catch(() => {
              this.submitLoading = false;
            });
        }
      });
    },
  },
};
</script>
