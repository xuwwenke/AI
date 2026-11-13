<template>
  <iframe-loader
    :src="$route.meta.iframeUrl"
    :needLoading="false"
    :isPage="true"
    @onUpdateMain="$emit('onUpdateMain', $event)"
  />
</template>

<script>
import IframeLoader from "@/components/iframe-loader";

export default {
  components: {
    IframeLoader,
  },
};
</script>
