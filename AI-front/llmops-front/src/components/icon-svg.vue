<template>
  <svg class="icon" aria-hidden="true">
    <use :xlink:href="'#icon-' + fixIcon" />
  </svg>
</template>

<script>
export default {
  props: {
    icon: {
      type: String,
      default: "",
    },
  },
  computed: {
    fixIcon() {
      return this.icon.startsWith('icon-') ? this.icon.slice(5) : this.icon
    }
  }
};
</script>
