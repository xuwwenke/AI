<template>
  <div class="chart-section" ref="containerRef">
    <!-- <div class="chart-header">{{ title }}</div> -->
    <div 
      ref="chartRef" 
      class="chart-box" 
      :style="{ height: height }"
      v-loading="loading"
      element-loading-text="图表加载中..."
      element-loading-background="rgba(249, 250, 251, 0.8)"
    ></div>
  </div>
</template>

<script>
import echarts from 'echarts'

export default {
  name: 'EChartItem',
  props: {
    title: {
      type: String,
      default: '图表'
    },
    option: {
      type: Object,
      required: true
    },
    height: {
      type: String,
      default: '300px'
    }
  },
  data() {
    return {
      chart: null,
      resizeObserver: null,
      intersectionObserver: null,
      isInitialized: false,
      loading: true
    };
  },
  watch: {
    option: {
      handler(newVal) {
        if (this.chart) {
          this.chart.setOption(newVal, true);
        }
      },
      deep: true
    }
  },
  mounted() {
    this.setupIntersectionObserver();
  },
  beforeDestroy() {
    this.destroyIntersectionObserver();
    this.destroyResizeObserver();
    if (this.chart) {
      this.chart.dispose();
      this.chart = null;
    }
  },
  methods: {
    setupIntersectionObserver() {
      if (!window.IntersectionObserver) {
        this.initChart();
        return;
      }

      this.intersectionObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
          if (entry.isIntersecting && !this.isInitialized) {
            this.initChart();
            this.isInitialized = true;
            // 初始化后停止观察
            this.intersectionObserver.unobserve(this.$refs.containerRef);
          }
        });
      }, {
        rootMargin: '100px', // 提前 100px 开始加载
        threshold: 0.01
      });

      if (this.$refs.containerRef) {
        this.intersectionObserver.observe(this.$refs.containerRef);
      }
    },
    destroyIntersectionObserver() {
      if (this.intersectionObserver) {
        this.intersectionObserver.disconnect();
        this.intersectionObserver = null;
      }
    },
    initChart() {
      if (!this.$refs.chartRef || this.chart) return;
      
      this.loading = true;
      // 延迟一小会儿初始化，给 UI 渲染留出空间
      setTimeout(() => {
        try {
          this.chart = echarts.init(this.$refs.chartRef);
          this.chart.setOption(this.option);
          this.setupResizeObserver();
          this.loading = false;
          
          this.$nextTick(() => {
            this.handleResize();
          });
        } catch (error) {
          console.error('Chart initialization failed:', error);
          this.loading = false;
        }
      }, 100);
    },
    setupResizeObserver() {
      if (window.ResizeObserver && this.$refs.chartRef) {
        this.resizeObserver = new ResizeObserver(() => {
          this.handleResize();
        });
        this.resizeObserver.observe(this.$refs.chartRef);
      } else {
        window.addEventListener('resize', this.handleResize);
      }
    },
    destroyResizeObserver() {
      if (this.resizeObserver) {
        this.resizeObserver.disconnect();
        this.resizeObserver = null;
      } else {
        window.removeEventListener('resize', this.handleResize);
      }
    },
    handleResize() {
      if (this.chart) {
        this.chart.resize();
      }
    }
  }
};
</script>

<style lang="scss" scoped>
.chart-section {
  background-color: #f9fafb;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #f3f4f6;

  .chart-header {
    font-size: 14px;
    font-weight: 600;
    color: #374151;
    margin-bottom: 16px;
    padding-left: 8px;
    border-left: 3px solid #6366f1;
  }

  .chart-box {
    width: 100%;
  }
}
</style>
