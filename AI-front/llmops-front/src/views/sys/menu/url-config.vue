<template>
  <el-form
    :inline="true"
    :model="urlObj"
    :rules="needRules ? rules : undefined"
    ref="form"
    style="width: auto"
    :disabled="disabled"
  >
    <el-row :gutter="10">
      <el-col :md="10">
        <el-form-item prop="url">
          <el-input v-model="urlObj.url" placeholder="访问的URL，与swagger上的地址保持一致"></el-input>
        </el-form-item>
      </el-col>
      <el-col :md="4">
        <el-form-item prop="httpMethod">
          <el-select v-model="urlObj.httpMethod" placeholder="请求方式">
            <el-option value="GET"></el-option>
            <el-option value="PUT"></el-option>
            <el-option value="POST"></el-option>
            <el-option value="DELETE"></el-option>
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :md="10">
        <el-form-item prop="remark">
          <el-input v-model="urlObj.remark" placeholder="URL的描述，记录操作日志时使用该描述"></el-input>
        </el-form-item>
      </el-col>
    </el-row>
  </el-form>
</template>

<script>
export default {
  props: ["urlObj", "disabled", "needRules"],
  data() {
    return {
      rules: {
        url: [{ required: true, message: "URL不能为空", trigger: "blur" }],
        httpMethod: [
          { required: true, message: "请求方式不能为空", trigger: "blur" }
        ]
      }
    };
  },
  methods: {
    validate() {
      let result = false;
      this.$refs.form.validate(valid => {
        if (valid) {
          result = true;
        }
      });
      return result;
    },
    // 清除表单校验状态
    clearValidate() {
      this.$refs.form.clearValidate();
    }
  }
};
</script>
