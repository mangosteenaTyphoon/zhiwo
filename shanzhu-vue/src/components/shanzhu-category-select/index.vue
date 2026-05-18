<template>
  <div class="shanzhu-category-select">
    <a-select
        class="category-select"
        :value="value"
        :loading="loading"
        :placeholder="placeholder"
        show-search
        allow-clear
        :filter-option="filterCategoryOption"
        @search="handleSearch"
        @update:value="handleChange"
    >
      <a-select-option v-for="category in categoryOptions" :key="category.id" :value="category.id">
        <span class="shanzhu-category-option">
          <span class="category-color" :style="{ backgroundColor: category.color || '#1677ff' }"></span>
          <span>{{ category.name }}</span>
        </span>
      </a-select-option>
    </a-select>

    <a-button
        v-if="showCreateButton"
        class="create-category-button"
        size="small"
        type="link"
        :loading="creating"
        @click="createCategory"
    >
      新增分类：{{ searchKeyword }}
    </a-button>
  </div>
</template>

<script setup lang="ts">
import {computed, onMounted, ref} from "vue";
import {addCategory, queryCategoryList} from "@/api/shanzhu/category/Category.ts";
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
const creating = ref(false);
const searchKeyword = ref("");
const categoryOptions = ref<ShanzhuCategory[]>([]);

const showCreateButton = computed(() => {
  const keyword = searchKeyword.value.trim();
  if (!keyword) {
    return false;
  }
  return !categoryOptions.value.some(category => category.name === keyword);
});

const loadCategories = async () => {
  loading.value = true;
  try {
    const response = await queryCategoryList();
    categoryOptions.value = response.data || [];
  } finally {
    loading.value = false;
  }
};

const handleSearch = (keyword: string) => {
  searchKeyword.value = keyword;
};

const handleChange = (selectedValue?: string) => {
  const selectedCategory = categoryOptions.value.find(category => category.id === selectedValue);
  emits("update:value", selectedValue);
  emits("change", selectedValue, selectedCategory);
};

const createCategory = async () => {
  const categoryName = searchKeyword.value.trim();
  if (!categoryName) {
    return;
  }

  creating.value = true;
  try {
    const response = await addCategory({
      name: categoryName
    });
    await loadCategories();

    const categoryId = response.data;
    if (categoryId) {
      handleChange(categoryId);
    }
    searchKeyword.value = "";
  } finally {
    creating.value = false;
  }
};

const filterCategoryOption = (input: string, option: { label?: string; value?: string; children?: string }) => {
  const optionValue = String(option.value || "");
  const optionLabel = String(option.children || option.label || "");
  return optionValue.toLowerCase().includes(input.toLowerCase()) || optionLabel.toLowerCase().includes(input.toLowerCase());
};

onMounted(() => {
  loadCategories();
});
</script>

<style scoped>
.shanzhu-category-select {
  width: 100%;
}

.category-select {
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

.create-category-button {
  height: auto;
  margin-top: 4px;
  padding: 0;
  font-size: 12px;
}
</style>
