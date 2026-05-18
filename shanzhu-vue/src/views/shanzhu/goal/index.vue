<template>
  <div class="shanzhu-goal-page">
    <a-flex :gap="16" vertical>
      <a-card :bordered="false" class="goal-filter-card">
        <a-form :colon="false">
          <a-row :gutter="[16, 12]">
            <a-col :xs="24" :sm="12" :md="8" :lg="6">
              <a-form-item label="关键词">
                <a-input v-model:value="goalQuery.keyword" placeholder="搜索目标名称" allow-clear/>
              </a-form-item>
            </a-col>
            <a-col :xs="24" :sm="12" :md="8" :lg="6">
              <a-form-item label="分类">
                <shanzhu-category-select v-model:value="goalQuery.categoryId"/>
              </a-form-item>
            </a-col>
            <a-col :xs="24" :sm="12" :md="8" :lg="6">
              <a-form-item label="状态">
                <a-select v-model:value="goalQuery.status" placeholder="请选择状态" allow-clear>
                  <a-select-option v-for="item in goalStatusOptions" :key="item.value" :value="item.value">
                    {{ item.label }}
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :xs="24" :sm="12" :md="8" :lg="6">
              <a-form-item>
                <a-space size="small">
                  <a-button type="primary" :loading="tableLoading" @click="queryPage">
                    <template #icon>
                      <SearchOutlined/>
                    </template>
                    查 询
                  </a-button>
                  <a-button :loading="tableLoading" @click="resetPage">
                    <template #icon>
                      <RedoOutlined/>
                    </template>
                    重 置
                  </a-button>
                </a-space>
              </a-form-item>
            </a-col>
          </a-row>
        </a-form>
      </a-card>

      <a-card :bordered="false" class="goal-list-card">
        <template #title>
          <a-flex justify="space-between" align="center" wrap="wrap" :gap="12">
            <div>
              <a-typography-title :level="4" class="goal-list-title">目标管理</a-typography-title>
              <a-typography-text type="secondary">把长期方向拆成可跟踪、可复盘的目标数据</a-typography-text>
            </div>
            <a-button type="primary" @click="openCreateModal">
              <template #icon>
                <PlusOutlined/>
              </template>
              新建目标
            </a-button>
          </a-flex>
        </template>

        <a-spin :spinning="tableLoading">
          <a-empty v-if="goalList.length === 0" description="暂无目标，先创建一个目标吧"/>
          <a-row v-else :gutter="[16, 16]">
            <a-col v-for="goal in goalList" :key="goal.id" :xs="24" :lg="12" :xl="8">
              <a-card class="goal-card" hoverable>
                <a-flex vertical :gap="12">
                  <a-flex justify="space-between" align="flex-start" :gap="12">
                    <div class="goal-main-info">
                      <a-tag :color="goal.categoryColor || 'blue'">{{ goal.categoryName || '未分类' }}</a-tag>
                      <a-typography-title :level="5" class="goal-title">{{ goal.title }}</a-typography-title>
                    </div>
                    <a-tag :color="getGoalStatusOption(goal.status).color">
                      {{ getGoalStatusOption(goal.status).label }}
                    </a-tag>
                  </a-flex>

                  <a-typography-paragraph class="goal-description" :ellipsis="{ rows: 2 }">
                    {{ goal.description || '暂无目标描述' }}
                  </a-typography-paragraph>

                  <a-progress :percent="goal.progress || 0" size="small"/>

                  <a-flex justify="space-between" align="center">
                    <a-space size="small" wrap>
                      <a-tag v-for="tag in goal.tags" :key="tag.id" :color="tag.color || 'blue'">
                        {{ tag.name }}
                      </a-tag>
                      <a-tag v-if="!goal.tags || goal.tags.length === 0">暂无标签</a-tag>
                    </a-space>
                    <a-space size="small">
                      <a-button type="link" size="small" @click="openDetailPage(goal.id)">详情</a-button>
                      <a-button type="link" size="small" @click="openEditModal(goal.id)">编辑</a-button>
                    </a-space>
                  </a-flex>

                  <a-divider class="goal-card-divider"/>

                  <a-flex justify="space-between" class="goal-meta">
                    <span>开始：{{ goal.startDate || '-' }}</span>
                    <span>截止：{{ goal.deadline || '-' }}</span>
                  </a-flex>
                </a-flex>
              </a-card>
            </a-col>
          </a-row>
        </a-spin>

        <template #footer>
          <a-flex justify="flex-end">
            <a-pagination
                v-model:current="goalQuery.pageNum"
                v-model:page-size="goalQuery.pageSize"
                show-size-changer
                :total="goalTotal"
                :show-total="(total:number) => `共 ${total} 条`"
                @change="initPage"
            />
          </a-flex>
        </template>
      </a-card>
    </a-flex>

    <a-modal
        v-model:open="modalActive.open"
        :confirm-loading="modalActive.saveLoading"
        :title="modalActive.title"
        width="720px"
        ok-text="保 存"
        cancel-text="取 消"
        @ok="handleSaveGoal"
    >
      <a-form ref="goalFormRef" :colon="false" :model="goalForm" :rules="goalRules" :label-col="{ span: 4 }">
        <a-form-item label="目标名称" name="title">
          <a-input v-model:value="goalForm.title" placeholder="例如：完成个人成长系统一期建设" :maxlength="120" show-count allow-clear/>
        </a-form-item>
        <a-form-item label="分类" name="categoryId">
          <shanzhu-category-select v-model:value="goalForm.categoryId"/>
        </a-form-item>
        <a-form-item label="状态">
          <a-radio-group v-model:value="goalForm.status">
            <a-radio v-for="item in goalStatusOptions" :key="item.value" :value="item.value">{{ item.label }}</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-row>
          <a-col :span="12">
            <a-form-item label="优先级" :label-col="{ span: 8 }">
              <a-select v-model:value="goalForm.priority" placeholder="请选择优先级">
                <a-select-option v-for="item in goalPriorityOptions" :key="item.value" :value="item.value">
                  {{ item.label }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="进度" :label-col="{ span: 8 }">
              <a-input-number v-model:value="goalForm.progress" :min="0" :max="100" addon-after="%" style="width: 100%"/>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row>
          <a-col :span="12">
            <a-form-item label="开始日期" :label-col="{ span: 8 }">
              <a-date-picker v-model:value="goalForm.startDate" value-format="YYYY-MM-DD" style="width: 100%" placeholder="请选择开始日期"/>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="截止日期" :label-col="{ span: 8 }">
              <a-date-picker v-model:value="goalForm.deadline" value-format="YYYY-MM-DD" style="width: 100%" placeholder="请选择截止日期"/>
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="标签">
          <shanzhu-tag-select v-model:value="goalForm.tagIds" tag-type="goal"/>
        </a-form-item>
        <a-form-item label="目标描述">
          <a-textarea v-model:value="goalForm.description" placeholder="描述这个目标为什么重要，以及希望达成的结果" :rows="4" :maxlength="500" show-count allow-clear/>
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model:value="goalForm.remark" placeholder="补充说明" :rows="3" :maxlength="300" show-count allow-clear/>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import {onMounted, reactive, ref} from "vue";
import {useRouter} from "vue-router";
import type {FormInstance, Rule} from "ant-design-vue/es/form";
import {message} from "ant-design-vue";
import {PlusOutlined, RedoOutlined, SearchOutlined} from "@ant-design/icons-vue";
import ShanzhuCategorySelect from "@/components/shanzhu-category-select/index.vue";
import ShanzhuTagSelect from "@/components/shanzhu-tag-select/index.vue";
import {queryGoalById, queryGoalPage, saveGoal} from "@/api/shanzhu/goal/Goal.ts";
import type {
  GoalPriorityOption,
  GoalStatusOption,
  ShanzhuGoal,
  ShanzhuGoalQuery,
  ShanzhuGoalVO
} from "@/api/shanzhu/goal/type/Goal.ts";
import type {BaseModalActiveType} from "@/api/global/Type.ts";

const router = useRouter();

const goalStatusOptions: GoalStatusOption[] = [
  {label: "未开始", value: "not_started", color: "default"},
  {label: "进行中", value: "in_progress", color: "processing"},
  {label: "已完成", value: "completed", color: "success"},
  {label: "已暂停", value: "paused", color: "warning"},
  {label: "已取消", value: "cancelled", color: "error"}
];

const goalPriorityOptions: GoalPriorityOption[] = [
  {label: "低", value: 1, color: "blue"},
  {label: "中", value: 2, color: "orange"},
  {label: "高", value: 3, color: "red"}
];

const defaultGoalQuery = (): ShanzhuGoalQuery => ({
  pageNum: 1,
  pageSize: 10
});

const defaultGoalForm = (): ShanzhuGoal => ({
  status: "not_started",
  goalType: "normal",
  progressMode: "manual",
  priority: 2,
  progress: 0,
  tagIds: []
});

const goalQuery = ref<ShanzhuGoalQuery>(defaultGoalQuery());
const goalList = ref<ShanzhuGoalVO[]>([]);
const goalTotal = ref<number>(0);
const tableLoading = ref(false);
const goalFormRef = ref<FormInstance>();
const goalForm = ref<ShanzhuGoal>(defaultGoalForm());

const modalActive = reactive<BaseModalActiveType>({
  open: false,
  saveLoading: false,
  title: "新建目标"
});

const goalRules: Record<string, Rule[]> = {
  title: [{required: true, message: "请输入目标名称", trigger: "blur"}],
  categoryId: [{required: true, message: "请选择目标分类", trigger: "change"}]
};

const getGoalStatusOption = (status?: string) => {
  return goalStatusOptions.find(item => item.value === status) || goalStatusOptions[0];
};

const initPage = async () => {
  tableLoading.value = true;
  try {
    const response = await queryGoalPage(goalQuery.value);
    if (response.code === 200) {
      goalList.value = response.data.records || [];
      goalTotal.value = response.data.total || 0;
    } else {
      message.error(response.msg);
    }
  } finally {
    tableLoading.value = false;
  }
};

const queryPage = async () => {
  goalQuery.value.pageNum = 1;
  await initPage();
};

const resetPage = async () => {
  goalQuery.value = defaultGoalQuery();
  await initPage();
};

const openCreateModal = () => {
  goalForm.value = defaultGoalForm();
  modalActive.title = "新建目标";
  modalActive.open = true;
};

const openDetailPage = (goalId?: string) => {
  if (!goalId) {
    return;
  }
  router.push(`/shanzhu/goal/detail/${goalId}`);
};

const openEditModal = async (goalId?: string) => {
  if (!goalId) {
    return;
  }

  const response = await queryGoalById(goalId);
  if (response.code !== 200 || !response.data) {
    message.error(response.msg || "目标不存在");
    return;
  }

  goalForm.value = {
    ...response.data,
    tagIds: response.data.tags?.map(tag => tag.id || "").filter(Boolean) || []
  };
  modalActive.title = "编辑目标";
  modalActive.open = true;
};

const handleSaveGoal = async () => {
  await goalFormRef.value?.validate();

  modalActive.saveLoading = true;
  try {
    const response = await saveGoal(goalForm.value);
    if (response.code === 200) {
      message.success("保存成功");
      modalActive.open = false;
      await queryPage();
    } else {
      message.error(response.msg);
    }
  } finally {
    modalActive.saveLoading = false;
  }
};

onMounted(() => {
  initPage();
});
</script>

<style scoped>
.shanzhu-goal-page {
  width: 100%;
}

.goal-filter-card :deep(.ant-card-body) {
  padding-bottom: 0;
}

.goal-list-title {
  margin-bottom: 4px;
}

.goal-card {
  height: 100%;
  border-radius: var(--lihua-radius-md);
}

.goal-main-info {
  min-width: 0;
}

.goal-title {
  margin-top: 8px;
  margin-bottom: 0;
}

.goal-description {
  min-height: 44px;
  margin-bottom: 0;
  color: var(--lihua-text-color-secondary);
}

.goal-card-divider {
  margin: 0;
}

.goal-meta {
  color: var(--lihua-text-color-secondary);
  font-size: var(--lihua-font-size-sm);
}
</style>
