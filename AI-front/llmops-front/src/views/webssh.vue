<template>
  <div class="w100p h100p ">
    <Xtrem ref="xterm" :type="router_params.type" />
  </div>
</template>

<script>
import Xtrem from "@/componentsPro/Xtrem";
export default {
  components: {
    Xtrem
  },

  computed: {
    router_params() {
      return this.$route.params;
    }
  },

  data() {
    return {};
  },

  watch: {},

  methods: {}
};
</script>

<style></style>
