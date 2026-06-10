<template>
  <div class="shanzhu-goal-page">
    <div class="goal-header">
      <div class="goal-header-top">
        <div>
          <div class="goal-eyebrow">Goal Studio</div>
          <h2 class="goal-page-title">目标列表</h2>
          <p class="goal-page-desc">把长期方向拆成可见进度，持续跟踪每一个重要目标。</p>
        </div>
        <a-space>
          <a-button class="goal-secondary-btn" @click="router.push('/shanzhu/task')">
            ✅ 任务中心
          </a-button>
          <a-button type="primary" shape="round" size="large" @click="openCreateModal">
            <template #icon><PlusOutlined/></template>
            新建目标
          </a-button>
        </a-space>
      </div>
    </div>

    <div class="goal-toolbar">
      <div class="goal-tabs">
        <button
            v-for="tab in statusTabs"
            :key="tab.value || 'all'"
            class="goal-tab"
            :class="{ 'goal-tab-active': activeStatus === tab.value }"
            @click="handleStatusChange(tab.value)"
        >
          {{ tab.label }}
          <span class="goal-tab-count">{{ tab.count }}</span>
        </button>
      </div>
      <div class="goal-search">
        <a-input
            v-model:value="goalQuery.keyword"
            placeholder="搜索目标..."
            allow-clear
            @press-enter="queryPage"
            @change="handleSearchChange"
        >
          <template #prefix>
            <SearchOutlined style="color: rgba(0,0,0,0.25);"/>
          </template>
        </a-input>
      </div>
    </div>

    <div class="goal-content">
      <div class="goal-main">
        <div class="goal-body">
          <div class="goal-filter-strip">
            <div class="goal-filter-item">
              <span class="goal-filter-label">分类</span>
              <shanzhu-category-select v-model:value="goalQuery.categoryId"/>
            </div>
            <div class="goal-filter-item">
              <span class="goal-filter-label">标签</span>
              <shanzhu-tag-select v-model:value="goalQuery.tagIds" tag-type="goal"/>
            </div>
            <div class="goal-filter-actions">
              <a-button type="primary" shape="round" :loading="tableLoading" @click="queryPage">
                <template #icon><SearchOutlined/></template>
                筛选
              </a-button>
              <a-button shape="round" :loading="tableLoading" @click="resetPage">
                <template #icon><RedoOutlined/></template>
                重置
              </a-button>
            </div>
          </div>

          <a-spin :spinning="tableLoading">
            <div v-if="!tableLoading && goalList.length === 0" class="goal-empty">
              <a-empty description="暂无目标，先创建一个目标吧">
                <a-button type="primary" shape="round" @click="openCreateModal">
                  <template #icon><PlusOutlined/></template>
                  添加第一个目标
                </a-button>
              </a-empty>
            </div>
            <TransitionGroup v-else-if="goalList.length > 0" name="goal-list-anim" tag="div" class="goal-list">
              <div
                  v-for="(goal, index) in goalList"
                  :key="goal.id"
                  class="goal-item"
                  :class="{
                    'goal-item-done': goal.status === 'completed',
                    'goal-item-high': goal.priority === 3 && goal.status !== 'completed'
                  }"
                  :style="{ '--anim-order': index }"
              >
                <div class="goal-progress-orb" @click="openDetailPage(goal.id)">
                  <span>{{ goal.progress || 0 }}%</span>
                </div>

                <div class="goal-item-body" @click="openDetailPage(goal.id)">
                  <div class="goal-item-top">
                    <div class="goal-item-title-wrap">
                      <div class="goal-item-title">{{ goal.title }}</div>
                      <div class="goal-item-desc">{{ goal.description || '暂无目标描述' }}</div>
                    </div>
                    <span class="goal-status-chip" :class="'goal-status-' + goal.status">
                      {{ getGoalStatusOption(goal.status).label }}
                    </span>
                  </div>

                  <a-progress
                      class="goal-progress"
                      :percent="goal.progress || 0"
                      :show-info="false"
                      size="small"
                  />

                  <div class="goal-item-tags">
                    <span class="goal-tag goal-tag-category">
                      {{ goal.categoryName || '未分类' }}
                    </span>
                    <span class="goal-tag" :class="'goal-tag-priority-' + goal.priority">
                      {{ getGoalPriorityOption(goal.priority).label }}优先级
                    </span>
                    <span v-if="goal.startDate" class="goal-tag goal-tag-date">
                      起 {{ goal.startDate }}
                    </span>
                    <span v-if="goal.deadline" class="goal-tag goal-tag-date">
                      止 {{ goal.deadline }}
                    </span>
                    <span v-for="tag in goal.tags" :key="tag.id" class="goal-tag goal-tag-custom">
                      {{ tag.name }}
                    </span>
                  </div>
                </div>

                <div class="goal-item-actions">
                  <a-button type="text" size="small" class="goal-detail-btn" @click.stop="openDetailPage(goal.id)">
                    详情
                  </a-button>
                  <a-dropdown>
                    <a-button type="text" size="small" class="goal-action-btn" @click.stop>
                      <template #icon><MoreOutlined/></template>
                    </a-button>
                    <template #overlay>
                      <a-menu>
                        <a-menu-item @click="openDetailPage(goal.id)">查看详情</a-menu-item>
                        <a-menu-item @click="openEditModal(goal.id)">编辑目标</a-menu-item>
                        <a-menu-divider/>
                        <a-menu-item danger @click="confirmDeleteGoal(goal)">删除目标</a-menu-item>
                      </a-menu>
                    </template>
                  </a-dropdown>
                </div>
              </div>
            </TransitionGroup>

            <div v-if="goalTotal > goalQuery.pageSize" class="goal-pagination">
              <a-pagination
                  v-model:current="goalQuery.pageNum"
                  v-model:page-size="goalQuery.pageSize"
                  :total="goalTotal"
                  :page-size-options="['10', '20', '30', '50']"
                  size="small"
                  show-size-changer
                  @change="initPage"
              />
            </div>
          </a-spin>
        </div>
      </div>

      <div class="goal-sidebar">
        <div class="goal-sidebar-card goal-sidebar-anim">
          <h4 class="goal-sidebar-title">📊 概览</h4>
          <div class="goal-sidebar-stats">
            <div class="goal-stat-row">
              <span class="goal-stat-label">目标总数</span>
              <span class="goal-stat-value goal-stat-value-blue">{{ goalOverview.total }}</span>
            </div>
            <div class="goal-stat-row">
              <span class="goal-stat-label">进行中</span>
              <span class="goal-stat-value">{{ goalOverview.inProgress }}</span>
            </div>
            <div class="goal-stat-row">
              <span class="goal-stat-label">已完成</span>
              <span class="goal-stat-value goal-stat-value-green">{{ goalOverview.completed }}</span>
            </div>
            <div class="goal-stat-divider"></div>
            <div class="goal-stat-row">
              <span class="goal-stat-label">当前页均进度</span>
              <span class="goal-stat-value">{{ goalOverview.avgProgress }}%</span>
            </div>
          </div>
        </div>

        <div class="goal-sidebar-card goal-sidebar-anim">
          <h4 class="goal-sidebar-title">⚡ 快捷入口</h4>
          <div class="goal-sidebar-actions">
            <a-button block @click="router.push('/shanzhu/task')">任务中心</a-button>
            <a-button block @click="router.push('/shanzhu/today-work')">今日工作台</a-button>
            <a-button block @click="router.push('/shanzhu/review')">复盘中心</a-button>
          </div>
        </div>

        <div class="goal-sidebar-card goal-sidebar-card-tip goal-sidebar-anim">
          <h4 class="goal-sidebar-title">💡 目标提示</h4>
          <ul class="goal-sidebar-tips">
            <li>保持目标标题具体、可衡量</li>
            <li>用任务拆解下一步行动</li>
            <li>每周复盘一次目标进度</li>
          </ul>
        </div>
      </div>
    </div>

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
import {computed, onMounted, reactive, ref} from "vue";
import {useRouter} from "vue-router";
import type {FormInstance, Rule} from "ant-design-vue/es/form";
import {message, Modal} from "ant-design-vue";
import {MoreOutlined, PlusOutlined, RedoOutlined, SearchOutlined} from "@ant-design/icons-vue";
import ShanzhuCategorySelect from "@/components/shanzhu-category-select/index.vue";
import ShanzhuTagSelect from "@/components/shanzhu-tag-select/index.vue";
import {deleteGoal, queryGoalById, queryGoalPage, saveGoal} from "@/api/shanzhu/goal/Goal.ts";
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

const activeStatus = computed(() => goalQuery.value.status || "");

const statusTabs = computed(() => [
  {label: "全部", value: "", count: goalTotal.value},
  ...goalStatusOptions.map(item => ({
    ...item,
    count: goalList.value.filter(goal => goal.status === item.value).length
  }))
]);

const goalOverview = computed(() => {
  const inProgressCount = goalList.value.filter(goal => goal.status === "in_progress").length;
  const completedCount = goalList.value.filter(goal => goal.status === "completed").length;
  const totalProgress = goalList.value.reduce((sum, goal) => sum + (goal.progress || 0), 0);
  const avgProgress = goalList.value.length ? Math.round(totalProgress / goalList.value.length) : 0;

  return {
    total: goalTotal.value,
    inProgress: inProgressCount,
    completed: completedCount,
    avgProgress
  };
});

const getGoalStatusOption = (status?: string) => {
  return goalStatusOptions.find(item => item.value === status) || goalStatusOptions[0];
};

const getGoalPriorityOption = (priority?: number) => {
  return goalPriorityOptions.find(item => item.value === priority) || goalPriorityOptions[1];
};

const handleStatusChange = async (status: string) => {
  goalQuery.value.status = status || undefined;
  await queryPage();
};

const handleSearchChange = async () => {
  if (!goalQuery.value.keyword) {
    await queryPage();
  }
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

const confirmDeleteGoal = (goal: ShanzhuGoalVO) => {
  if (!goal.id) {
    return;
  }

  Modal.confirm({
    title: "确认删除目标？",
    content: `删除后，目标「${goal.title || '-'}」及其子目标、任务和进展记录将不再展示。`,
    okText: "确认删除",
    cancelText: "取消",
    okType: "danger",
    onOk: async () => {
      const response = await deleteGoal(goal.id || "");
      if (response.code === 200) {
        message.success("删除成功");
        await initPage();
      } else {
        message.error(response.msg || "删除失败");
      }
    }
  });
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
  max-width: 1360px;
  min-height: calc(100vh - 120px);
  margin: 0 auto;
  padding: 36px 48px 56px;
  overflow-x: hidden;
}

/* Header */
.goal-header {
  position: relative;
  z-index: 2;
  padding: 26px 28px;
  margin-bottom: 18px;
  overflow: hidden;
  border: 1px solid rgba(22, 119, 255, 0.08);
  border-radius: 26px;
  background:
    radial-gradient(circle at 16% 0%, rgba(22, 119, 255, 0.13), transparent 34%),
    radial-gradient(circle at 96% 18%, rgba(82, 196, 26, 0.10), transparent 28%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.98), rgba(247, 250, 255, 0.94));
  box-shadow: 0 18px 45px rgba(15, 35, 80, 0.06);
}

.goal-header-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.goal-eyebrow {
  margin-bottom: 8px;
  color: #1677ff;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.8px;
  text-transform: uppercase;
}

.goal-page-title {
  margin: 0;
  color: rgba(0, 0, 0, 0.88);
  font-size: 30px;
  font-weight: 850;
  line-height: 1.2;
  letter-spacing: -0.7px;
}

.goal-page-desc {
  margin: 8px 0 0;
  color: rgba(0, 0, 0, 0.48);
  font-size: 14px;
}

.goal-secondary-btn {
  height: 38px;
  border-color: rgba(15, 35, 80, 0.08);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.74);
  color: rgba(0, 0, 0, 0.68);
  font-weight: 600;
}

.goal-header-top :deep(.ant-btn-primary) {
  min-width: 122px;
  height: 42px;
  box-shadow: 0 10px 24px rgba(22, 119, 255, 0.22);
}

/* Toolbar */
.goal-toolbar {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
  padding: 8px;
  border: 1px solid rgba(0, 0, 0, 0.04);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.76);
  box-shadow: 0 10px 28px rgba(15, 35, 80, 0.04);
  backdrop-filter: blur(12px);
}

.goal-tabs {
  display: flex;
  gap: 6px;
  overflow-x: auto;
}

.goal-tab {
  display: flex;
  align-items: center;
  gap: 6px;
  min-height: 34px;
  padding: 7px 14px;
  border: none;
  border-radius: 12px;
  background: transparent;
  color: rgba(0, 0, 0, 0.54);
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
  cursor: pointer;
  transition: all 0.2s ease;
}

.goal-tab:hover {
  background: rgba(22, 119, 255, 0.06);
  color: rgba(0, 0, 0, 0.78);
}

.goal-tab-active {
  background: #eaf3ff;
  color: #1677ff;
  box-shadow: inset 0 0 0 1px rgba(22, 119, 255, 0.06);
}

.goal-tab-count {
  min-width: 20px;
  padding: 1px 7px;
  border-radius: 999px;
  background: rgba(0, 0, 0, 0.06);
  font-size: 11px;
  font-weight: 700;
  text-align: center;
}

.goal-tab-active .goal-tab-count {
  background: rgba(22, 119, 255, 0.14);
  color: #1677ff;
}

.goal-search {
  width: 220px;
  flex-shrink: 0;
}

.goal-search :deep(.ant-input-affix-wrapper) {
  height: 34px;
  border-color: transparent;
  border-radius: 12px;
  background: rgba(247, 248, 250, 0.92);
}

.goal-search :deep(.ant-input-affix-wrapper:hover),
.goal-search :deep(.ant-input-affix-wrapper-focused) {
  border-color: rgba(22, 119, 255, 0.20);
  background: #fff;
  box-shadow: 0 6px 16px rgba(22, 119, 255, 0.08);
}

/* Content */
.goal-content {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: flex-start;
  gap: 28px;
  overflow: visible;
}

.goal-main {
  min-width: 0;
  flex: 1;
}

.goal-body {
  min-height: 240px;
  overflow: hidden;
  border: 1px solid rgba(15, 35, 80, 0.06);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 18px 45px rgba(15, 35, 80, 0.07), 0 1px 2px rgba(15, 35, 80, 0.04);
}

.goal-filter-strip {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  padding: 16px 18px;
  border-bottom: 1px solid rgba(15, 35, 80, 0.055);
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.92), rgba(248, 251, 255, 0.78));
}

.goal-filter-item {
  min-width: 0;
  flex: 1;
}

.goal-filter-label {
  display: block;
  margin-bottom: 6px;
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
  font-weight: 700;
}

.goal-filter-item :deep(.ant-select-selector),
.goal-filter-item :deep(.ant-input-affix-wrapper) {
  border-radius: 12px;
}

.goal-filter-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.goal-empty {
  padding: 88px 20px;
}

.goal-list {
  display: flex;
  flex-direction: column;
}

.goal-item {
  position: relative;
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 20px 22px;
  overflow: hidden;
  border-bottom: 1px solid rgba(15, 35, 80, 0.055);
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.88), rgba(248, 251, 255, 0.72));
  transition: background-color 0.18s ease, transform 0.18s ease, box-shadow 0.18s ease;
}

.goal-item::before {
  position: absolute;
  top: 18px;
  bottom: 18px;
  left: 0;
  width: 3px;
  border-radius: 0 999px 999px 0;
  background: transparent;
  content: "";
}

.goal-item:hover {
  z-index: 2;
  background: #fff;
  box-shadow: 0 14px 30px rgba(15, 35, 80, 0.08);
  transform: translateY(-1px);
}

.goal-item-high::before {
  background: #ff4d4f;
}

.goal-item-done {
  background:
    linear-gradient(135deg, rgba(250, 255, 250, 0.88), rgba(246, 255, 237, 0.66));
}

.goal-progress-orb {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 54px;
  height: 54px;
  flex-shrink: 0;
  border-radius: 18px;
  background:
    radial-gradient(circle at 30% 20%, rgba(255, 255, 255, 0.88), transparent 34%),
    linear-gradient(135deg, rgba(22, 119, 255, 0.92), rgba(82, 196, 26, 0.82));
  box-shadow: 0 12px 24px rgba(22, 119, 255, 0.18);
  color: #fff;
  font-size: 13px;
  font-weight: 850;
  cursor: pointer;
}

.goal-item-body {
  min-width: 0;
  flex: 1;
  cursor: pointer;
}

.goal-item-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.goal-item-title-wrap {
  min-width: 0;
}

.goal-item-title {
  display: -webkit-box;
  overflow: hidden;
  color: rgba(0, 0, 0, 0.86);
  font-size: 16px;
  font-weight: 750;
  line-height: 24px;
  word-break: break-word;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.goal-item-desc {
  display: -webkit-box;
  min-height: 20px;
  margin-top: 5px;
  overflow: hidden;
  color: rgba(0, 0, 0, 0.46);
  font-size: 13px;
  line-height: 20px;
  word-break: break-word;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.goal-status-chip {
  flex-shrink: 0;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(0, 0, 0, 0.05);
  color: rgba(0, 0, 0, 0.56);
  font-size: 12px;
  font-weight: 700;
}

.goal-status-in_progress {
  background: rgba(22, 119, 255, 0.10);
  color: #1677ff;
}

.goal-status-completed {
  background: rgba(82, 196, 26, 0.12);
  color: #389e0d;
}

.goal-status-paused {
  background: rgba(250, 173, 20, 0.13);
  color: #d48806;
}

.goal-status-cancelled {
  background: rgba(255, 77, 79, 0.10);
  color: #cf1322;
}

.goal-progress {
  margin: 12px 0 8px;
}

.goal-progress :deep(.ant-progress-inner) {
  background: rgba(15, 35, 80, 0.06);
}

.goal-item-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  max-height: 58px;
  overflow: hidden;
}

.goal-tag {
  display: inline-flex;
  align-items: center;
  max-width: 180px;
  min-height: 24px;
  padding: 3px 9px;
  overflow: hidden;
  border-radius: 999px;
  background: rgba(15, 35, 80, 0.055);
  color: rgba(0, 0, 0, 0.54);
  font-size: 12px;
  font-weight: 650;
  line-height: 18px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.goal-tag-category {
  background: rgba(22, 119, 255, 0.08);
  color: #1677ff;
}

.goal-tag-priority-3 {
  background: rgba(255, 77, 79, 0.10);
  color: #cf1322;
}

.goal-tag-priority-2 {
  background: rgba(250, 173, 20, 0.12);
  color: #d48806;
}

.goal-tag-priority-1 {
  background: rgba(22, 119, 255, 0.08);
  color: #1677ff;
}

.goal-tag-date {
  background: rgba(0, 0, 0, 0.045);
  color: rgba(0, 0, 0, 0.50);
}

.goal-tag-custom {
  background: rgba(114, 46, 209, 0.08);
  color: #722ed1;
}

.goal-item-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.goal-detail-btn,
.goal-action-btn {
  border-radius: 999px;
  color: rgba(0, 0, 0, 0.48);
  font-weight: 650;
}

.goal-detail-btn:hover,
.goal-action-btn:hover {
  background: rgba(22, 119, 255, 0.08);
  color: #1677ff;
}

.goal-pagination {
  display: flex;
  justify-content: flex-end;
  padding: 16px 18px;
  border-top: 1px solid rgba(15, 35, 80, 0.055);
}

/* Sidebar */
.goal-sidebar {
  width: 286px;
  flex-shrink: 0;
}

.goal-sidebar-card {
  margin-bottom: 16px;
  padding: 18px;
  border: 1px solid rgba(15, 35, 80, 0.06);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 14px 34px rgba(15, 35, 80, 0.055);
  backdrop-filter: blur(12px);
}

.goal-sidebar-title {
  margin: 0 0 14px;
  color: rgba(0, 0, 0, 0.82);
  font-size: 15px;
  font-weight: 800;
}

.goal-sidebar-stats {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.goal-stat-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.goal-stat-label {
  color: rgba(0, 0, 0, 0.48);
  font-size: 13px;
  font-weight: 600;
}

.goal-stat-value {
  color: rgba(0, 0, 0, 0.78);
  font-size: 16px;
  font-weight: 850;
}

.goal-stat-value-blue {
  color: #1677ff;
}

.goal-stat-value-green {
  color: #389e0d;
}

.goal-stat-divider {
  height: 1px;
  margin: 2px 0;
  background: rgba(15, 35, 80, 0.07);
}

.goal-sidebar-actions {
  display: flex;
  flex-direction: column;
  gap: 9px;
}

.goal-sidebar-actions :deep(.ant-btn) {
  height: 38px;
  justify-content: flex-start;
  border-color: rgba(15, 35, 80, 0.08);
  border-radius: 12px;
  color: rgba(0, 0, 0, 0.68);
  font-weight: 650;
}

.goal-sidebar-card-tip {
  background:
    radial-gradient(circle at 12% 0%, rgba(22, 119, 255, 0.10), transparent 30%),
    rgba(255, 255, 255, 0.92);
}

.goal-sidebar-tips {
  padding-left: 18px;
  margin: 0;
  color: rgba(0, 0, 0, 0.52);
  font-size: 13px;
  line-height: 1.9;
}

.goal-sidebar-anim {
  animation: goalFadeUp 0.45s ease both;
}

.goal-list-anim-enter-active,
.goal-list-anim-leave-active {
  transition: all 0.22s ease;
}

.goal-list-anim-enter-from,
.goal-list-anim-leave-to {
  opacity: 0;
  transform: translateY(8px);
}

.goal-list-anim-move {
  transition: transform 0.22s ease;
}

.goal-item {
  animation: goalFadeUp 0.36s ease both;
  animation-delay: calc(var(--anim-order) * 0.025s);
}

@keyframes goalFadeUp {
  from {
    opacity: 0;
    transform: translateY(8px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Responsive */
@media (max-width: 1200px) {
  .shanzhu-goal-page {
    padding: 28px 28px 48px;
  }

  .goal-content {
    gap: 18px;
  }

  .goal-sidebar {
    width: 260px;
  }
}

@media (max-width: 960px) {
  .goal-header-top,
  .goal-toolbar,
  .goal-content,
  .goal-filter-strip {
    flex-direction: column;
    align-items: stretch;
  }

  .goal-header-top :deep(.ant-space) {
    justify-content: flex-start;
  }

  .goal-search {
    width: 100%;
  }

  .goal-sidebar {
    width: 100%;
  }
}

@media (max-width: 640px) {
  .shanzhu-goal-page {
    padding: 18px 14px 36px;
  }

  .goal-header {
    padding: 22px 20px;
    border-radius: 22px;
  }

  .goal-page-title {
    font-size: 26px;
  }

  .goal-tabs {
    width: 100%;
  }

  .goal-filter-actions {
    width: 100%;
  }

  .goal-filter-actions :deep(.ant-btn) {
    flex: 1;
  }

  .goal-item {
    gap: 12px;
    padding: 18px 16px;
  }

  .goal-progress-orb {
    width: 46px;
    height: 46px;
    border-radius: 16px;
    font-size: 12px;
  }

  .goal-item-top {
    flex-direction: column;
    gap: 8px;
  }

  .goal-item-actions {
    position: absolute;
    right: 12px;
    bottom: 12px;
  }

  .goal-detail-btn {
    display: none;
  }

  .goal-item-tags {
    padding-right: 34px;
  }
}
</style>
