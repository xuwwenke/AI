<template>
  <el-upload drag class="avatar-uploader" action :show-file-list="false"
    :before-upload="beforeAvatarUpload" :accept="imgFiletypes.join(',')" :http-request="onUpload">
    <template v-if="field">
      <el-image v-if="/\/|\./.test(field)" fit="contain" :src="field" />
      <!-- <i v-else :class="'fa fa-' + field"></i> -->
      <Icon v-else :icon="field" />
      <i class="del-btn el-icon-close" @click.stop="onDel"></i>
      <!-- <div class="el-upload__tip" slot="tip">
        <label>点击或拖入可再次上传，覆盖之前的文件</label>
      </div> -->
    </template>
    <template v-else>
      <i class="el-icon-plus"></i>
      <!-- <div class="el-upload__text">
        将文件拖到此处，或<em>点击上传</em>
      </div> -->
      <!-- <div class="el-upload__tip" slot="tip">
        <label
          >支持 <i class="fa fa-file-image-o"></i>
          {{ imgFiletypes.join("/") }}</label
        >
      </div> -->
    </template>
  </el-upload>
</template>

<script>
import Icon from '@/components/icon';

export default {
  components: {
    Icon,
  },
  props: {
    value: String,
    sizeLimit: {
      type: Number,
      default: 100,
    },
  },
  data() {
    return {
      field: null,
      imgFiletypes: ['.jpg', '.jpeg', '.png', '.bmp', '.svg'],
    };
  },
  watch: {
    value: {
      handler(val) {
        this.field = val;
      },
      immediate: true,
    },
    field(val) {
      this.$emit('input', val);
    },
  },
  methods: {
    async beforeAvatarUpload(file) {
      console.log('beforeAvatarUpload', file);
      return await new Promise(async (resolve, reject) => {
        let fileType = file.name
          .substring(file.name.lastIndexOf('.'))
          .toLowerCase();
        // image/jpeg包含 jpe jpeg jpg
        const isJPG =
          file.type === 'image/jpeg' || this.imgFiletypes.includes(fileType);
        const isLt2M = file.size / 1024 <= this.sizeLimit;

        if (!isJPG) {
          this.$message.error(
            `上传图片只能是 ${this.imgFiletypes.join('、')} 格式!`
          );
          return reject();
        }
        if (!isLt2M) {
          this.$message.error('上传图片大小不能超过' + this.sizeLimit + 'KB!');
          return reject();
        }
        resolve();
      });
    },
    onUpload(options) {
      const file = options.file;

      if (!file) {
        this.field = null;
        return;
      }

      //读取文件
      let reader = new FileReader();
      reader.onload = (e) => {
        let dataURL = e.target.result;

        this.field = dataURL;
      };
      reader.readAsDataURL(file);
    },
    onDel() {
      this.field = null;
    },
  },
};
</script>

<style lang="scss" scoped>
.avatar-uploader ::v-deep {
  width: 100px;
  height: 100px;
  .el-upload {
    width: 100%;
    height: 100%;
    .el-upload-dragger {
      width: 100%;
      height: 100%;
      .el-image {
        width: 100%;
        height: 100%;
      }

      i:not(.del-btn),
      svg {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        font-size: 50px;
      }
      .el-icon-plus {
        font-size: 30px;
        color: #8c939d
      }
    }
  }
}

.del-btn {
  position: absolute;
  top: 5px;
  right: 5px;
  cursor: pointer;
  opacity: 0.75;
  color: #606266;
  &:hover {
    opacity: 1;
  }
}
</style>
