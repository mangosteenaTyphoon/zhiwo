<template>
  <div class="shanzhu-tag-select">
    <a-select
        class="tag-select"
        :value="value"
        :loading="loading"
        :placeholder="placeholder"
        mode="multiple"
        show-search
        allow-clear
        :filter-option="false"
        @search="handleSearch"
        @update:value="handleChange"
        @dropdown-visible-change="handleDropdownVisibleChange"
    >
      <a-select-option v-for="tag in tagOptions" :key="tag.id" :value="tag.id">
        <span class="tag-option">
          <span class="tag-color" :style="{ backgroundColor: tag.color || '#1677ff' }"></span>
          <span>{{ tag.name }}</span>
        </span>
      </a-select-option>
    </a-select>

    <a-button
        v-if="showCreateButton"
        class="create-tag-button"
        size="small"
        type="link"
        :loading="creating"
        @click="createTag"
    >
      新增标签：{{ searchKeyword }}
    </a-button>
  </div>
</template>

<script setup lang="ts">
import {computed, onMounted, ref} from "vue";
import {addTag, searchTag} from "@/api/shanzhu/tag/Tag.ts";
import type {ShanzhuTag} from "@/api/shanzhu/tag/type/Tag.ts";

const props = withDefaults(defineProps<{
  value?: string[];
  tagType?: string;
  placeholder?: string;
}>(), {
  value: () => [],
  placeholder: "请选择标签"
});

const emits = defineEmits<{
  "update:value": [value: string[]];
  change: [value: string[], tags: ShanzhuTag[]];
}>();

const loading = ref(false);
const creating = ref(false);
const searchKeyword = ref("");
const tagOptions = ref<ShanzhuTag[]>([]);

const showCreateButton = computed(() => {
  const keyword = searchKeyword.value.trim();
  if (!keyword) {
    return false;
  }
  return !tagOptions.value.some(tag => tag.name === keyword);
});

const loadTags = async (keyword?: string) => {
  loading.value = true;
  try {
    const response = await searchTag({
      keyword,
      tagType: props.tagType
    });
    tagOptions.value = response.data || [];
  } finally {
    loading.value = false;
  }
};

const handleSearch = (keyword: string) => {
  searchKeyword.value = keyword;
  loadTags(keyword);
};

const handleChange = (selectedValue: string[]) => {
  const selectedTags = tagOptions.value.filter(tag => selectedValue.includes(tag.id || ""));
  emits("update:value", selectedValue);
  emits("change", selectedValue, selectedTags);
};

const handleDropdownVisibleChange = (open: boolean) => {
  if (open && tagOptions.value.length === 0) {
    loadTags();
  }
};

const createTag = async () => {
  const tagName = searchKeyword.value.trim();
  if (!tagName) {
    return;
  }

  creating.value = true;
  try {
    const response = await addTag({
      name: tagName,
      tagType: props.tagType
    });
    await loadTags(tagName);

    const tagId = response.data;
    if (tagId && !props.value.includes(tagId)) {
      handleChange([...props.value, tagId]);
    }
    searchKeyword.value = "";
  } finally {
    creating.value = false;
  }
};

onMounted(() => {
  loadTags();
});
</script>

<style scoped>
.shanzhu-tag-select {
  width: 100%;
}

.tag-select {
  width: 100%;
}

.tag-option {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.tag-color {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex: 0 0 auto;
}

.create-tag-button {
  height: auto;
  margin-top: 4px;
  padding: 0;
  font-size: 12px;
}
</style>
