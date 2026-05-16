<template>
  <a-select
      class="shanzhu-category-select"
      :value="value"
      :loading="loading"
      :placeholder="placeholder"
      allow-clear
      @update:value="handleChange"
  >
    <a-select-option v-for="category in categoryOptions" :key="category.id" :value="category.id">
      <span class="shanzhu-category-option">
        <span class="category-color" :style="{ backgroundColor: category.color || '#1677ff' }"></span>
        <span>{{ category.name }}</span>
      </span>
    </a-select-option>
  </a-select>
</template>

<script setup lang="ts">
import {onMounted, ref} from "vue";
import {queryCategoryList} from "@/api/shanzhu/category/Category.ts";
import type {ShanzhuCategory} from "@/api/shanzhu/category/type/Category.ts";

withDefaults(defineProps<{
  value?: string;
  placeholder?: string;
}>(), {
  placeholder: "请选择分类"
});

const emits = defineEmits<{
  "update:value": [value?: string];
  change: [value?: string, category?: ShanzhuCategory];
}>();

const loading = ref(false);
const categoryOptions = ref<ShanzhuCategory[]>([]);

const loadCategories = async () => {
  loading.value = true;
  try {
    const response = await queryCategoryList();
    categoryOptions.value = response.data || [];
  } finally {
    loading.value = false;
  }
};

const handleChange = (selectedValue?: string) => {
  const selectedCategory = categoryOptions.value.find(category => category.id === selectedValue);
  emits("update:value", selectedValue);
  emits("change", selectedValue, selectedCategory);
};

onMounted(() => {
  loadCategories();
});
</script>

<style scoped>
.shanzhu-category-select {
  width: 100%;
}

.shanzhu-category-option {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.category-color {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex: 0 0 auto;
}
</style>
