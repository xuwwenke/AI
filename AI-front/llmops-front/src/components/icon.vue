<script>
import IconSvg from '@/components/icon-svg'
export default {
  components: {
    IconSvg
  },
  props: {
    icon: {
      type: String,
    },
    placeholder: {
      type: String,
    }
  },
  render() {
    if (!this.icon) {
      if (this.placeholder) {
        return <i class={'fa fa-' + this.placeholder}></i>
      }
      return null
    }
    if (this.icon.startsWith('icon-')) {
      return <IconSvg icon={this.icon} />
    }
    if (this.icon.startsWith('el-icon-')) {
      return <i class={this.icon}></i>
    }
    return <i class={'fa fa-' + this.icon}></i>
  }
}
</script>

<style lang="scss" scoped>

</style>
