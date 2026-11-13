<template>
  <el-card>
    <el-form
      v-if="!needRefresh"
      :model="dataForm"
      :rules="dataRule"
      ref="dataForm"
      label-width="auto"
      :disabled="!editing"
      :validate-on-rule-change="false"
    >
      <el-form-item :label="dataForm.type == 1 ? '上级菜单' : '上级菜单或资源'" prop="parentId">
        <tree-select
          v-model="dataForm.parentId"
          :props="{ value: 'id', label: 'name', children: 'children' }"
          :options="optionData"
        />
      </el-form-item>
      <template v-if="dataForm.type == 1">
        <el-form-item label="菜单名称" prop="name">
          <el-input v-model.trim="dataForm.name" placeholder="菜单名称"></el-input>
        </el-form-item>
        <el-form-item label="菜单图标" prop="icon">
          <icon-select
            v-model="dataForm.icon"
            placeholder="菜单图标"
            :disabled="!editing"
          />
        </el-form-item>
        <el-form-item label="显示序号" prop="seq">
          <el-input v-model="dataForm.seq" placeholder="请输入数字"></el-input>
        </el-form-item>
        <el-form-item prop="url">
          <span slot="label">
            菜单链接
            <el-tooltip placement="right">
              <i class="el-icon-question"></i>
              <div slot="content">
                基础示例：/sys/user
                <br />动态地址: /sys/user/:id?
                <br />基于ip端口的绝对地址: http://${ip}:8080/#/sys/user
                <br />基于域名的绝对地址: http://xxx-front.${DOMAIN}/#/sys/user
              </div>
            </el-tooltip>
          </span>
          <el-input
            v-model="dataForm.url"
            placeholder="也将用于动态生成前端路由，示例：/sys/user"
          ></el-input>
        </el-form-item>
        <el-form-item size="mini" prop="hidden">
          <span slot="label">
            菜单状态
            <el-tooltip placement="right">
              <i class="el-icon-question"></i>
              <div slot="content">
                菜单隐藏时，仍可用于生成前端路由
              </div>
            </el-tooltip>
          </span>
          <el-radio-group v-model="dataForm.hidden">
            <el-radio :label="0">显示</el-radio>
            <el-radio :label="1">隐藏</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="打开方式" prop="openMode">
          <el-radio-group v-model="dataForm.openMode">
            <el-radio :label="undefined">默认</el-radio>
            <el-radio label="_blank">新窗口</el-radio>
            <el-radio label="multiple">
              重复打开
              <el-tooltip placement="right">
                <i class="el-icon-question"></i>
                <div slot="content">在使用标签形式打开页面时，可重复打开多次</div>
              </el-tooltip>
            </el-radio>
          </el-radio-group>
        </el-form-item>
      </template>
      <template v-else>
        <el-form-item label="资源类型" prop="type">
          <el-radio-group v-model="dataForm.type">
            <el-radio :label="2">按钮权限</el-radio>
            <el-radio :label="3">通用资源</el-radio>
            <el-radio :label="4">数据字段脱敏</el-radio>
            <el-radio :label="5">数据字段范围权限</el-radio>
          </el-radio-group>
        </el-form-item>
        <template v-if="dataForm.type == 2">
          <el-form-item label="权限名称" prop="name">
            <el-input
              v-model.trim="dataForm.name"
              placeholder="权限名称"
            ></el-input>
          </el-form-item>
          <el-form-item label="权限图标" prop="icon">
            <icon-select
              v-model="dataForm.icon"
              placeholder="权限图标"
              :disabled="!editing"
            />
          </el-form-item>
          <el-form-item label="显示序号" prop="seq">
            <el-input v-model="dataForm.seq" placeholder="请输入数字"></el-input>
          </el-form-item>
          <el-form-item label="权限关键字" prop="permissionKeyword">
            <el-input
              v-model="dataForm.permissionKeyword"
              placeholder="权限关键字，采用模块名加功能名进行命名，示例：sys.user.add"
            ></el-input>
          </el-form-item>
          <el-form-item label="记录操作日志" size="mini" prop="isLog">
            <el-radio-group v-model="dataForm.isLog">
              <el-radio :label="1">是</el-radio>
              <el-radio :label="0">否</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item
            prop="url"
            :class="{ 'is-required': editing && dataForm.isLog == 1 }"
          >
            <span slot="label">
              访问请求
              <el-tooltip placement="right">
                <i class="el-icon-question"></i>
                <div slot="content">
                  请不要为“查看”权限设置“记录操作日志”和“访问请求url”
                  <br />以免该权限未被勾选时，影响用到同一“访问请求url”的其它页面
                  <br />目前“查看”权限仅用于勾选（避免授权时，因为不勾选任何权限，造成菜单无法选择）
                </div>
              </el-tooltip>
            </span>
            <urls-setting
              ref="urlsSetting"
              :dataForm="dataForm"
              :disabled="!editing"
              :needRules="dataForm.isLog == 1"
            />
          </el-form-item>
        </template>
        <template v-else-if="dataForm.type == 3">
          <el-form-item label="资源子类型" prop="subtype">
            <el-select
              v-model="dataForm.subtype"
              placeholder="请选择资源子类型"
            >
              <el-option
                v-for="subtype in subtypeList"
                :key="subtype.key"
                :value="subtype.key"
                :label="subtype.value"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="资源关系" prop="relation">
            <el-select v-model="dataForm.relation" placeholder="请选择资源关系">
              <el-option
                v-for="relation in relationList"
                :key="relation.key"
                :value="relation.key"
                :label="relation.value"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="资源名称" prop="name">
            <el-input v-model.trim="dataForm.name" placeholder="资源名称"></el-input>
          </el-form-item>
          <el-form-item label="资源图标" prop="icon">
            <icon-select
              v-model="dataForm.icon"
              placeholder="资源图标"
              :disabled="!editing"
            />
          </el-form-item>
          <el-form-item label="显示序号" prop="seq">
            <el-input v-model="dataForm.seq" placeholder="请输入数字"></el-input>
          </el-form-item>
          <el-form-item label="资源关键字" prop="permissionKeyword">
            <el-input
              v-model="dataForm.permissionKeyword"
              placeholder="资源关键字，采用模块名加功能名进行命名，示例：sys.user.add"
            ></el-input>
          </el-form-item>
          <el-form-item label="资源扩展信息" prop="other">
            <el-input
              type="textarea"
              :rows="3"
              v-model.trim="dataForm.other"
              placeholder="请输入资源扩展信息（建议使用json的形式存储）"
            ></el-input>
          </el-form-item>
        </template>
        <template v-else-if="dataForm.type == 4">
          <el-form-item class="is-required" style="margin-bottom: 0;">
            <span slot="label">
              访问请求
              <el-tooltip placement="right">
                <i class="el-icon-question"></i>
                <div slot="content">
                  针对于后台的访问地址，返回的数据字段进行权限控制
                </div>
              </el-tooltip>
            </span>
            <div class="el-form el-form--inline">
              <el-row :gutter="10">
                <el-col :md="10">
                  <el-form-item prop="url">
                    <el-input
                      v-model="dataForm.url"
                      placeholder="访问的URL，与swagger上的地址保持一致"
                    ></el-input>
                  </el-form-item>
                </el-col>
                <el-col :md="4">
                  <el-form-item prop="httpMethod">
                    <el-select
                      v-model="dataForm.httpMethod"
                      placeholder="请求方式"
                    >
                      <el-option value="GET"></el-option>
                      <el-option value="PUT"></el-option>
                      <el-option value="POST"></el-option>
                      <el-option value="DELETE"></el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :md="10">
                  <el-form-item prop="remark">
                    <el-input
                      v-model="dataForm.remark"
                      placeholder="URL的描述，记录操作日志时使用该描述"
                    ></el-input>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </el-form-item>
          <el-form-item label="字段权限名称" prop="name">
            <el-input
              v-model.trim="dataForm.name"
              placeholder="数据字段的权限名称，例如：查看用户手机号"
            ></el-input>
          </el-form-item>
          <el-form-item label="字段表达式" prop="permissionKeyword">
            <el-input
              v-model.trim="dataForm.permissionKeyword"
              placeholder="数据字段的表达式，即json的key值"
            ></el-input>
          </el-form-item>
        </template>
        <template v-else-if="dataForm.type == 5">
          <el-alert
            style="margin: 0 0 20px 40px;width: calc(100% - 40px);"
            title="数据范围权限：适合对某一字段的枚举值进行条件筛选，通过权限表达式动态拼接SQL实现数据行范围控制"
            :closable="false"
            type="info">
          </el-alert>
          <el-form-item label="权限名称" prop="name">
            <el-input
              v-model.trim="dataForm.name"
              placeholder="权限名称，例如：查看用户手机号"
            ></el-input>
          </el-form-item>
          <el-form-item label="权限图标" prop="icon">
            <icon-select
              v-model="dataForm.icon"
              placeholder="权限图标"
              :disabled="!editing"
            />
          </el-form-item>
          <el-form-item label="显示序号" prop="seq">
            <el-input
              v-model="dataForm.seq"
              placeholder="请输入数字"
            ></el-input>
          </el-form-item>
          <el-form-item class="is-required" style="margin-bottom: 0;">
            <span slot="label">
              访问请求
              <el-tooltip placement="right">
                <i class="el-icon-question"></i>
                <div slot="content">
                  针对于后台的访问地址，返回的数据字段进行权限控制
                </div>
              </el-tooltip>
            </span>
            <div class="el-form el-form--inline">
              <el-row :gutter="10">
                <el-col :md="10">
                  <el-form-item prop="url">
                    <el-input
                      v-model="dataForm.url"
                      placeholder="访问的URL，与swagger上的地址保持一致"
                    ></el-input>
                  </el-form-item>
                </el-col>
                <el-col :md="4">
                  <el-form-item prop="httpMethod">
                    <el-select
                      v-model="dataForm.httpMethod"
                      placeholder="请求方式"
                    >
                      <el-option value="GET"></el-option>
                      <el-option value="PUT"></el-option>
                      <el-option value="POST"></el-option>
                      <el-option value="DELETE"></el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :md="10">
                  <el-form-item prop="remark">
                    <el-input
                      v-model="dataForm.remark"
                      placeholder="URL的描述，记录操作日志时使用该描述"
                    ></el-input>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </el-form-item>
          <el-form-item label="权限关键字" prop="permissionKeyword">
            <el-input
              v-model.trim="dataForm.permissionKeyword"
              placeholder="权限关键字，需唯一，通常代表该字段的枚举值"
            ></el-input>
          </el-form-item>
          <el-form-item label="权限表达式" prop="other">
            <el-input
              v-model.trim="dataForm.other"
              placeholder="权限表达式，主要用来声明如何动态控制SQL，也可以在此处直接配置SQL"
            ></el-input>
          </el-form-item>
        </template>
      </template>
      <el-form-item v-show="editing">
        <el-button type="primary" :loading="submitLoading" @click="onSubmit">保存</el-button>
        <el-button @click="onCancel">取消</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script>
import api_menu from "@/api/sys/menu";
import IconSelect from "@/components/icon-select";
import { getCfgDic } from "@/utils/cfgDic";
import UrlsSetting from "./urls-setting";
import TreeSelect from "@/components/tree-select";

export default {
  props: ["appKey", "optionData"],
  components: {
    IconSelect,
    UrlsSetting,
    TreeSelect
  },
  created() {
    getCfgDic("resSubtype", data => {
      this.subtypeList = data;
    });
    getCfgDic("resRelation", data => {
      this.relationList = data;
    });
  },
  data() {
    return {
      needRefresh: false,
      // labelWidth: "80px",
      editing: false,
      submitLoading: false,
      currentNodeBak: null,
      dataForm: {
        name: null,
        type: 1,
        openMode: undefined
      },
      // 一级菜单校验
      menuRule: {
        name: [
          { required: true, message: "菜单名称不能为空", trigger: "blur" }
        ],
        seq: [{ required: true, message: "显示序号不能为空", trigger: "blur" }],
      },
      // 子菜单校验
      subMenuRule: {
        name: [
          { required: true, message: "菜单名称不能为空", trigger: "blur" }
        ],
        seq: [{ required: true, message: "显示序号不能为空", trigger: "blur" }],
        url: [{ required: true, message: "菜单链接不能为空", trigger: "blur" }],
      },
      // 权限校验
      permissionRule: {
        name: [{ required: true, message: "名称不能为空", trigger: "blur" }],
        permissionKeyword: [
          { required: true, message: "关键字不能为空", trigger: "blur" }
        ],
        isLog: [
          { required: true, message: "记录操作日志不能为空", trigger: "blur" }
        ]
      },
      // 数据字段权限校验
      fieldPermissionRule: {
        name: [{ required: true, message: "名称不能为空", trigger: "blur" }],
        url: [{ required: true, message: "菜单链接不能为空", trigger: "blur" }],
        httpMethod: [
          { required: true, message: "菜单链接不能为空", trigger: "change" }
        ],
        permissionKeyword: [
          { required: true, message: "关键字不能为空", trigger: "blur" }
        ]
      },
      // 数据字段范围权限校验
      fieldRangePermissionRule: {
        name: [{ required: true, message: "名称不能为空", trigger: "blur" }],
        url: [{ required: true, message: "菜单链接不能为空", trigger: "blur" }],
        httpMethod: [
          { required: true, message: "菜单链接不能为空", trigger: "change" }
        ],
        permissionKeyword: [
          { required: true, message: "关键字不能为空", trigger: "blur" }
        ],
        other: [
          { required: true, message: "表达式不能为空", trigger: "blur" }
        ]
      },
      subtypeList: [],
      relationList: []
    };
  },
  computed: {
    dataRule({ editing, dataForm }) {
      if (editing) {
        switch (dataForm.type) {
          case 1:
            if (dataForm.parentId == undefined) {
              return this.menuRule;
            } else {
              return this.subMenuRule;
            }
          case 4:
            return this.fieldPermissionRule;
          case 5:
            return this.fieldRangePermissionRule;
          default:
            return this.permissionRule;
        }
      } else {
        return null;
      }
    }
  },
  watch: {
    "dataForm.type": function(val) {
      this.clearValidate();

      // this.labelWidth = val == 1 ? "80px" : "110px";

      // 刷新表单，用来解决从按钮权限切换到通用资源时，表单中部分控件校验失效问题
      this.needRefresh = true;
      this.$nextTick(() => {
        this.needRefresh = false;
      });
    },
    "dataForm.isLog": function(val) {
      const urlsSetting = this.$refs.urlsSetting;
      if (urlsSetting) urlsSetting.clearValidate();
    }
  },
  methods: {
    reset() {
      this.dataForm = {
        type: 1
      };
    },
    // 清除表单校验状态
    clearValidate() {
      this.$refs.dataForm.clearValidate();
      const urlsSetting = this.$refs.urlsSetting;
      if (urlsSetting) urlsSetting.clearValidate();
    },
    updateFormData(currentNode) {
      this.confirmWithoutSave(() => {
        this.currentNodeBak = currentNode;
        let tmp = { ...currentNode };
        if (tmp.parent) {
          tmp.parentId = tmp.parent.id;
          tmp.parentName = tmp.parent.name;
          delete tmp.parent;
        }
        delete tmp.children;
        this.dataForm = tmp;
        this.clearValidate();
      });
    },
    onAdd(type, root = false, currentNode) {
      this.confirmWithoutSave(() => {
        this.clearValidate();
        this.dataForm = {
          type,
          hidden: type === 1 ? 0 : undefined,
          isLog: type === 2 ? 1 : undefined,
          parentId: root ? undefined : currentNode.id,
          parentName: root ? undefined : currentNode.name
        };

        this.editing = true;
      });
    },
    onEdit() {
      this.editing = true;
    },
    // 取消新增 / 修改
    onCancel(reset = true) {
      this.editing = false;
      this.submitLoading = false;
      this.clearValidate();
      reset && this.currentNodeBak && this.updateFormData(this.currentNodeBak);
    },
    // 新增 / 修改
    onSubmit() {
      this.$refs.dataForm.validate(valid => {
        const urlsSetting = this.$refs.urlsSetting;
        if (urlsSetting && !urlsSetting.validate()) return;

        if (valid) {
          this.submitLoading = true;

          const urlInfo = urlsSetting ? urlsSetting.getUrlInfo() : {};

          let tmp = { ...this.dataForm, ...urlInfo };
          tmp.appKey = this.appKey;

          const opt = this.dataForm.id == null ? "insert" : "update";

          api_menu[opt](tmp)
            .then(() => {
              this.onCancel(false);
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
    confirmWithoutSave(func) {
      if (this.editing) {
        this.$confirm("表单未保存, 是否离开?", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning"
        })
          .then(() => {
            this.editing = false;
            // func();
            // 加上nextTick，避免从菜单编辑切到新增资源时，校验出问题
            this.$nextTick(func);
          })
          .catch(() => {
            this.$emit("onConfirmCancel");
            return false;
          });
      } else {
        func();
      }
    }
  }
};
</script>
