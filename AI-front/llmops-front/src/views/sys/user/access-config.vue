<template>
  <el-form :inline="true" :model="accessObj" :rules="rules" ref="form" style="width: auto">
    <el-row :gutter="10">
      <el-col :md="3">
        <el-form-item prop="accessType">
          <el-select v-model="accessObj.accessType" placeholder="允许 / 禁止">
            <el-option label="允许" value="ALLOW"></el-option>
            <el-option label="禁止" value="DENY"></el-option>
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :md="7">
        <el-form-item prop="startIpStr">
          <el-input v-model="accessObj.startIpStr" placeholder="起始IP地址"></el-input>
        </el-form-item>
      </el-col>
      <el-col :md="7">
        <el-form-item prop="endIpStr">
          <el-input v-model="accessObj.endIpStr" placeholder="结束IP地址"></el-input>
        </el-form-item>
      </el-col>
      <el-col :md="7">
        <el-form-item prop="expirationDate">
          <el-date-picker v-model="accessObj.expirationDate" type="datetime" value-format="timestamp" placeholder="截止有效期"></el-date-picker>
        </el-form-item>
      </el-col>
    </el-row>
  </el-form>
</template>

<script>
import { ip } from "@/utils/validate";

export default {
  props: ["accessObj"],
  data() {
    var validateIp = (rule, value, callback) => {
      if (value && !ip(value)) {
        callback(new Error("IP地址格式错误"));
      } else {
        callback();
      }
    };
    return {
      rules: {
        accessType: [
          { required: true, message: "控制类型不能为空", trigger: "change" },
        ],
        startIpStr: [
          { required: true, message: "起始IP地址不能为空", trigger: "blur" },
          { validator: validateIp, trigger: "blur" }
        ],
        endIpStr: [
          { required: true, message: "结束IP地址不能为空", trigger: "blur" },
          { validator: validateIp, trigger: "blur" }
        ],
      },
    };
  },
  methods: {
    validate() {
      let result = false;
      this.$refs.form.validate((valid) => {
        if (valid) {
          result = true;
        }
      });
      return result;
    },
    // 清除表单校验状态
    clearValidate() {
      this.$refs.form.clearValidate();
    },
  },
};
</script>
