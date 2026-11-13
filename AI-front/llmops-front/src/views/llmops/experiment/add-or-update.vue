<template>
  <el-dialog
    :title="!dataForm.id ? '创建实验' : '修改实验'"
    :close-on-click-modal="false"
    :visible.sync="visible"
    width="600px"
  >
    <el-form
      :model="dataForm"
      :rules="dataRule"
      ref="dataForm"
      @keyup.enter.native="dataFormSubmit()"
      label-position="top"
    >
      <el-form-item label="实验名称" prop="name">
        <el-input v-model="dataForm.name" placeholder="请输入实验名称"></el-input>
      </el-form-item>
      <!-- <el-form-item label="所属项目" prop="projectId">
        <el-select v-model="dataForm.projectId" placeholder="请选择项目" style="width: 100%">
          <el-option
            v-for="item in projectList"
            :key="item.id"
            :label="item.projectChName"
            :value="item.id"
          ></el-option>
        </el-select>
      </el-form-item> -->
      <el-form-item label="模型" prop="modelName">
        <el-input v-model="dataForm.modelName" placeholder="请输入模型名称"></el-input>
      </el-form-item>
      <el-form-item label="描述" prop="desc">
        <el-input
          type="textarea"
          :rows="3"
          v-model="dataForm.desc"
          placeholder="请输入实验描述"
        ></el-input>
      </el-form-item>
      <el-form-item label="配置" prop="config">
        <el-input
          type="textarea"
          :rows="4"
          v-model="dataForm.config"
          placeholder="请输入实验配置 (JSON格式)"
        ></el-input>
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="dataFormSubmit()" :loading="confirmLoading">
        {{ !dataForm.id ? '确定创建' : '确定修改' }}
      </el-button>
    </span>
  </el-dialog>
</template>

<script>
import { getExperimentDetail, createExperiment, updateExperiment } from '@/api/llmops/experiment'
import { getProjectList } from '@/api/llmops/project'

export default {
  data() {
    return {
      visible: false,
      confirmLoading: false,
      projectList: [],
      dataForm: {
        id: 0,
        name: '',
        // projectId: '',
        modelName: '',
        desc: '',
        config: ''
      },
      dataRule: {
        name: [{ required: true, message: '实验名称不能为空', trigger: 'blur' }],
        // projectId: [{ required: true, message: '所属项目不能为空', trigger: 'change' }],
        model: [{ required: true, message: '模型不能为空', trigger: 'blur' }],
        config: [
          {
            validator: (rule, value, callback) => {
              if (value) {
                try {
                  JSON.parse(value)
                  callback()
                } catch (e) {
                  callback(new Error('请输入正确的 JSON 格式'))
                }
              } else {
                callback()
              }
            },
            trigger: 'blur'
          }
        ]
      }
    }
  },
  methods: {
    init(id) {
      this.dataForm.id = id || 0
      this.visible = true
      this.$nextTick(() => {
        this.$refs['dataForm'].resetFields()
        this.getProjects()
        if (this.dataForm.id) {
          this.getInfo()
        }
      })
    },
    // 获取项目列表
    async getProjects() {
      try {
        const res = await getProjectList({ limit: 1000 })
        this.projectList = res.list || []
      } catch (error) {
        console.error('获取项目列表失败:', error)
      }
    },
    // 获取详情
    async getInfo() {
      try {
        const res = await getExperimentDetail(this.dataForm.id)
        if (res) {
          this.dataForm.name = res.name
          // this.dataForm.projectId = res.projectId
          this.dataForm.modelName = res.modelName
          this.dataForm.desc = res.desc
          this.dataForm.config = res.config ? JSON.parse(res.config) : ''
        }
      } catch (error) {
        console.error('获取实验详情失败:', error)
      }
    },
    // 表单提交
    dataFormSubmit() {
      this.$refs['dataForm'].validate(async (valid) => {
        if (valid) {
          this.confirmLoading = true
          try {
            const params = {
              ...this.dataForm,
              id: this.dataForm.id || undefined,
              config: this.dataForm.config ? JSON.stringify(this.dataForm.config) : undefined
            }
            const res = this.dataForm.id ? await updateExperiment(params) : await createExperiment(params)
            
            this.$message({
              message: '操作成功',
              type: 'success',
              duration: 1500,
              onClose: () => {
                this.visible = false
                this.$emit('refreshDataList')
              }
            })
          } catch (error) {
            console.error('提交失败:', error)
          } finally {
            this.confirmLoading = false
          }
        }
      })
    }
  }
}
</script>

<style scoped>
.dialog-footer {
  text-align: right;
}
</style>
