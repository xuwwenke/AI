<template>
  <dl class="menu-classify">
    <dt>标签分类</dt>
    <template v-for="(item, index) in tList">
      <dd :key="index" :class="{ isSelected: curIndex === index }" @click="toggleSelect(index)">
        {{ item.tag }}
      </dd>
      <el-divider :key="index" v-if="index !== tList.length - 1" direction="vertical"></el-divider>
    </template>
  </dl>
</template>

<script>
export default {
  name: 'menu-classify',
  props: {
    list: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      firstTag: {
        name: '',
        id: '',
        tag: '全部',
        isSelected: false
      },
      curIndex: 0
    };
  },
  computed: {
    tList() {
      if (this.list.length) {
        const hash = {};
        const newArray = this.list.reduce((item, next) => {
          hash[next.tag] ? '' : (hash[next.tag] = true && item.push(next));
          return item;
        }, []);
        newArray.unshift(this.firstTag);
        return newArray;
      } else {
        return [];
      }
    }
  },
  methods: {
    toggleSelect(i) {
      // const item_all = this.data[0]
      // if (selectedItem.key === item_all.key) {
      // 	if (selectedItem.isSelected) {
      // 		this.data.forEach((item) => {
      // 			this.$set(item, 'isSelected', false)
      // 		})
      // 	} else {
      // 		this.data.forEach((item) => {
      // 			this.$set(item, 'isSelected', true)
      // 		})
      // 	}
      // } else {
      // 	this.data.forEach((item) => {
      // 		if (selectedItem.key === item.key) {
      // 			this.$set(item, 'isSelected', !item.isSelected)
      // 		}
      // 	})
      // 	const isSelectAlled = this.data.slice(1).every((item) => {
      // 		return item.isSelected === true
      // 	})
      // 	if (isSelectAlled) {
      // 		this.$set(this.data[0], 'isSelected', true)
      // 	} else {
      // 		this.$set(this.data[0], 'isSelected', false)
      // 	}
      // }
      this.curIndex = i;
      this.tList[i].isSelected = !this.tList[i].isSelected;
      this.$emit('filterMebuByTag', this.tList[i]);
    }
  }
};
</script>

<style scoped lang="scss">
@import 'variable.scss';
.menu-classify {
  margin-top: $distance-15;
  margin-bottom: $distance-15;
  margin-left: -($distance-15);
  margin-right: -($distance-15);
  line-height: 2.4;
  background: $color-gray1;
  border-top: 1px solid $boder-color1;
  border-bottom: 1px solid $boder-color1;
  dt,
  dd {
    display: inline-block;
  }
  dt {
    margin: 0 $distance-15 0 $distance-15 * 2;
    font-weight: bold;
  }
  dd {
    min-width: 6em;
    padding: 0 2em;
    text-align: center;
    border: 1px solid $color-gray1;
    cursor: pointer;
    box-sizing: border-box;
    &:hover {
      background-color: $color-gray1;
    }
    &.isSelected {
      background-color: #e1effe;
      border-bottom: 1px solid $color-theme;
      color: $color-theme;
      font-weight: 700;
    }
  }
}
</style>
