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
            <RiseOutlined style="margin-right: 4px;"/> 任务中心
          </a-button>
          <a-button type="primary" @click="openCreateModal">
            <template #icon><PlusOutlined/></template>
            新建目标
          </a-button>
        </a-space>
      </div>
    </div>

    <!-- 指标统计条 -->
    <div class="goal-stats-bar">
      <div class="goal-stat-card">
        <span class="goal-stat-num goal-stat-blue">{{ goalOverview.total }}</span>
        <span class="goal-stat-lbl">全部目标</span>
      </div>
      <div class="goal-stat-card">
        <span class="goal-stat-num">{{ goalOverview.inProgress }}</span>
        <span class="goal-stat-lbl">进行中</span>
      </div>
      <div class="goal-stat-card">
        <span class="goal-stat-num goal-stat-green">{{ goalOverview.completed }}</span>
        <span class="goal-stat-lbl">已完成</span>
      </div>
      <div class="goal-stat-card">
        <span class="goal-stat-num">{{ goalOverview.avgProgress }}<small>%</small></span>
        <span class="goal-stat-lbl">平均进度</span>
      </div>
    </div>

    <!-- 操作栏：标签页 + 搜索 + 筛选 -->
    <div class="goal-action-bar">
      <div class="goal-tabs">
        <button
            v-for="tab in statusTabs"
            :key="tab.value || 'all'"
            class="goal-tab"
            :class="{ 'goal-tab-active': activeStatus === tab.value }"
            @click="handleStatusChange(tab.value)"
        >
          {{ tab.label }}
          <span v-if="tab.count" class="goal-tab-count">{{ tab.count }}</span>
        </button>
      </div>
      <div class="goal-action-right">
        <div class="goal-search-wrap">
          <a-input
              v-model:value="goalQuery.keyword"
              placeholder="搜索..."
              allow-clear
              @press-enter="queryPage"
              @change="handleSearchChange"
          >
            <template #prefix>
              <SearchOutlined style="color: rgba(0,0,0,0.25);"/>
            </template>
          </a-input>
        </div>
        <a-button class="goal-filter-btn" @click="filterExpanded = !filterExpanded">
          <FilterOutlined/>
          <span v-if="activeFilterCount > 0" class="goal-filter-dot"></span>
        </a-button>
      </div>
    </div>

    <!-- 折叠筛选面板 -->
    <div v-show="filterExpanded" class="goal-filter-panel">
      <div class="goal-filter-inner">
        <div class="goal-filter-item">
          <span class="goal-filter-label">分类</span>
          <shanzhu-category-select v-model:value="goalQuery.categoryId"/>
        </div>
        <div class="goal-filter-item">
          <span class="goal-filter-label">标签</span>
          <shanzhu-tag-select v-model:value="goalQuery.tagIds" tag-type="goal"/>
        </div>
        <div class="goal-filter-actions">
          <a-button type="primary" size="small" @click="queryPage" :loading="tableLoading">查询</a-button>
          <a-button size="small" @click="resetPage" :loading="tableLoading">重置</a-button>
        </div>
      </div>
    </div>

    <!-- 目标卡片网格 -->
    <a-spin :spinning="tableLoading">
      <div v-if="!tableLoading && goalList.length === 0" class="goal-empty">
        <div class="goal-empty-icon">
          <AimOutlined/>
        </div>
        <h3 class="goal-empty-title">还没有目标</h3>
        <p class="goal-empty-desc">设定一个清晰的目标，让每一步行动都有方向</p>
        <a-button type="primary" shape="round" size="large" @click="openCreateModal">
          <template #icon><PlusOutlined/></template>
          创建第一个目标
        </a-button>
      </div>

      <TransitionGroup v-else-if="goalList.length > 0" name="goal-card-anim" tag="div" class="goal-grid">
        <div
            v-for="(goal, index) in goalList"
            :key="goal.id"
            class="goal-card"
            :class="{
              'goal-card-done': goal.status === 'completed',
              'goal-card-high': goal.priority === 3 && goal.status !== 'completed'
            }"
            :style="{ '--anim-order': index }"
            @click="openDetailPage(goal.id)"
        >
          <!-- 卡片顶部：状态 + 操作 -->
          <div class="goal-card-top">
            <span class="goal-card-status" :class="'gc-status-' + goal.status">
              <component :is="getStatusIcon(goal.status)" style="font-size: 11px;"/>
              {{ getGoalStatusOption(goal.status).label }}
            </span>
            <div class="goal-card-actions" @click.stop>
              <a-dropdown>
                <button class="goal-card-more"><MoreOutlined/></button>
                <template #overlay>
                  <a-menu>
                    <a-menu-item @click="openDetailPage(goal.id)">查看详情</a-menu-item>
                    <a-menu-item @click="openEditModal(goal.id)">编辑目标</a-menu-item>
                    <a-menu-divider/>
                    <a-menu-item danger @click="confirmDeleteGoal(goal)">删除</a-menu-item>
                  </a-menu>
                </template>
              </a-dropdown>
            </div>
          </div>

          <!-- 卡片主体 -->
          <div class="goal-card-body">
            <div class="goal-card-title">{{ goal.title }}</div>
            <div class="goal-card-desc">{{ goal.description || '暂无描述' }}</div>
          </div>

          <!-- 进度区域 -->
          <div class="goal-card-progress">
            <div class="goal-card-progress-info">
              <span class="goal-card-progress-label">进度</span>
              <span class="goal-card-progress-value">{{ goal.progress || 0 }}%</span>
            </div>
            <div class="goal-card-bar">
              <div class="goal-card-bar-fill" :style="{ width: (goal.progress || 0) + '%' }"></div>
            </div>
          </div>

          <!-- 卡片底部：标签 + 日期 -->
          <div class="goal-card-footer">
            <div class="goal-card-tags">
              <span class="gc-tag gc-tag-cat">{{ goal.categoryName || '未分类' }}</span>
              <span v-for="tag in goal.tags.slice(0, 1)" :key="tag.id" class="gc-tag gc-tag-custom">{{ tag.name }}</span>
              <span v-if="goal.tags.length > 1" class="gc-tag gc-tag-more">+{{ goal.tags.length - 1 }}</span>
            </div>
            <span v-if="goal.deadline" class="goal-card-deadline">
              <ClockCircleOutlined/> {{ goal.deadline }}
            </span>
          </div>

          <!-- 高优标记 -->
          <div v-if="goal.priority === 3 && goal.status !== 'completed'" class="goal-card-priority">
            <FireOutlined/>
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
import {
  AimOutlined,
  CheckOutlined,
  ClockCircleOutlined,
  FilterOutlined,
  FireOutlined,
  MinusOutlined,
  MoreOutlined,
  PauseOutlined,
  PlusOutlined,
  RedoOutlined,
  RiseOutlined,
  SearchOutlined,
  StopOutlined
} from "@ant-design/icons-vue";
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
const filterExpanded = ref(false);

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

const getStatusIcon = (status?: string) => {
  switch (status) {
    case 'completed':
      return CheckOutlined;
    case 'in_progress':
      return RiseOutlined;
    case 'paused':
      return PauseOutlined;
    case 'cancelled':
      return StopOutlined;
    case 'not_started':
      return MinusOutlined;
    default:
      return MinusOutlined;
  }
};

const activeFilterCount = computed(() => {
  let count = 0;
  if (goalQuery.value.categoryId) count++;
  if (goalQuery.value.tagIds && goalQuery.value.tagIds.length > 0) count++;
  return count;
});

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
/* ===== 页面容器 ===== */
.shanzhu-goal-page {
  max-width: 1200px;
  min-height: calc(100vh - 120px);
  margin: 0 auto;
  padding: 28px 40px 56px;
  overflow-x: hidden;
}

/* ===== Header ===== */
.goal-header {
  position: relative;
  z-index: 2;
  padding: 20px 24px;
  margin-bottom: 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.goal-header-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.goal-eyebrow {
  margin-bottom: 4px;
  color: rgba(0, 0, 0, 0.28);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 1.2px;
  text-transform: uppercase;
}

.goal-page-title {
  margin: 0;
  color: rgba(0, 0, 0, 0.88);
  font-size: 24px;
  font-weight: 800;
  line-height: 1.3;
  letter-spacing: -0.5px;
}

.goal-page-desc {
  margin: 4px 0 0;
  color: rgba(0, 0, 0, 0.40);
  font-size: 13px;
}

.goal-secondary-btn {
  height: 36px;
  border-color: rgba(0, 0, 0, 0.08);
  border-radius: 8px;
  background: #fff;
  color: rgba(0, 0, 0, 0.65);
  font-size: 13px;
  font-weight: 600;
}

.goal-header-top :deep(.ant-btn-primary) {
  height: 36px;
  border-radius: 8px;
  box-shadow: none;
  font-weight: 600;
}

/* ===== 指标统计条 ===== */
.goal-stats-bar {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.goal-stat-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 16px 12px;
  border: 1px solid rgba(0, 0, 0, 0.05);
  border-radius: 12px;
  background: #fff;
}

.goal-stat-num {
  color: rgba(0, 0, 0, 0.82);
  font-size: 28px;
  font-weight: 800;
  font-feature-settings: "tnum";
  line-height: 1;
}

.goal-stat-num small {
  font-size: 16px;
  font-weight: 700;
  color: rgba(0, 0, 0, 0.35);
}

.goal-stat-blue { color: #1677ff; }
.goal-stat-green { color: #52c41a; }

.goal-stat-lbl {
  color: rgba(0, 0, 0, 0.40);
  font-size: 12px;
  font-weight: 600;
}

/* ===== 操作栏 ===== */
.goal-action-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.goal-tabs {
  display: flex;
  gap: 2px;
  overflow-x: auto;
}

.goal-tab {
  display: flex;
  align-items: center;
  gap: 5px;
  min-height: 32px;
  padding: 6px 12px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: rgba(0, 0, 0, 0.50);
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
  cursor: pointer;
  transition: all 0.15s ease;
}

.goal-tab:hover {
  background: rgba(0, 0, 0, 0.04);
  color: rgba(0, 0, 0, 0.75);
}

.goal-tab-active {
  background: rgba(0, 0, 0, 0.06);
  color: rgba(0, 0, 0, 0.88);
}

.goal-tab-count {
  min-width: 18px;
  padding: 1px 6px;
  border-radius: 999px;
  background: rgba(0, 0, 0, 0.06);
  font-size: 11px;
  font-weight: 700;
  text-align: center;
}

.goal-tab-active .goal-tab-count {
  background: rgba(0, 0, 0, 0.10);
}

.goal-action-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.goal-search-wrap {
  width: 180px;
}

.goal-search-wrap :deep(.ant-input-affix-wrapper) {
  height: 32px;
  border-color: rgba(0, 0, 0, 0.08);
  border-radius: 8px;
  background: #fff;
  font-size: 13px;
}

.goal-filter-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  width: 32px;
  height: 32px;
  padding: 0;
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 8px;
  background: #fff;
  color: rgba(0, 0, 0, 0.50);
  font-size: 14px;
  cursor: pointer;
}

.goal-filter-btn:hover {
  border-color: rgba(0, 0, 0, 0.15);
  color: rgba(0, 0, 0, 0.78);
}

.goal-filter-dot {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #ff4d4f;
}

/* ===== 筛选面板 ===== */
.goal-filter-panel {
  margin-bottom: 16px;
  overflow: hidden;
  animation: goalSlideDown 0.2s ease;
}

.goal-filter-inner {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  padding: 12px 16px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-radius: 10px;
  background: #fafafa;
}

.goal-filter-item {
  min-width: 0;
  flex: 1;
}

.goal-filter-label {
  display: block;
  margin-bottom: 4px;
  color: rgba(0, 0, 0, 0.40);
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.goal-filter-item :deep(.ant-select-selector) {
  border-radius: 8px;
}

.goal-filter-actions {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
}

/* ===== 空状态 ===== */
.goal-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80px 20px;
  text-align: center;
}

.goal-empty-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  margin-bottom: 16px;
  border-radius: 16px;
  background: rgba(0, 0, 0, 0.03);
  color: rgba(0, 0, 0, 0.18);
  font-size: 28px;
}

.goal-empty-title {
  margin: 0 0 6px;
  color: rgba(0, 0, 0, 0.72);
  font-size: 16px;
  font-weight: 700;
}

.goal-empty-desc {
  margin: 0 0 20px;
  color: rgba(0, 0, 0, 0.38);
  font-size: 13px;
}

/* ===== 卡片网格 ===== */
.goal-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.goal-card {
  position: relative;
  display: flex;
  flex-direction: column;
  padding: 18px 20px 16px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-radius: 12px;
  background: #fff;
  cursor: pointer;
  transition: border-color 0.15s, box-shadow 0.2s, transform 0.15s;
  animation: goalFadeUp 0.3s ease both;
  animation-delay: calc(var(--anim-order) * 0.03s);
}

.goal-card:hover {
  border-color: rgba(0, 0, 0, 0.10);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  transform: translateY(-1px);
}

.goal-card-done {
  background: #fcfff8;
}

.goal-card-high {
  border-left: 3px solid #ff4d4f;
}

/* 卡片顶部 */
.goal-card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.goal-card-status {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 8px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 700;
}

.gc-status-completed {
  background: rgba(82, 196, 26, 0.10);
  color: #389e0d;
}

.gc-status-in_progress {
  background: rgba(22, 119, 255, 0.08);
  color: #1677ff;
}

.gc-status-not_started {
  background: rgba(0, 0, 0, 0.04);
  color: rgba(0, 0, 0, 0.45);
}

.gc-status-paused {
  background: rgba(250, 173, 20, 0.10);
  color: #d48806;
}

.gc-status-cancelled {
  background: rgba(255, 77, 79, 0.08);
  color: #cf1322;
}

.goal-card-actions {
  opacity: 0;
  transition: opacity 0.15s;
}

.goal-card:hover .goal-card-actions {
  opacity: 1;
}

.goal-card-more {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  padding: 0;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: rgba(0, 0, 0, 0.35);
  font-size: 14px;
  cursor: pointer;
}

.goal-card-more:hover {
  background: rgba(0, 0, 0, 0.04);
  color: rgba(0, 0, 0, 0.65);
}

/* 卡片主体 */
.goal-card-body {
  flex: 1;
  min-height: 0;
}

.goal-card-title {
  display: -webkit-box;
  overflow: hidden;
  margin-bottom: 4px;
  color: rgba(0, 0, 0, 0.85);
  font-size: 15px;
  font-weight: 700;
  line-height: 1.4;
  word-break: break-word;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.goal-card-desc {
  display: -webkit-box;
  overflow: hidden;
  margin-bottom: 14px;
  color: rgba(0, 0, 0, 0.38);
  font-size: 12px;
  line-height: 1.5;
  word-break: break-word;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

/* 进度区域 */
.goal-card-progress {
  margin-bottom: 14px;
}

.goal-card-progress-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}

.goal-card-progress-label {
  color: rgba(0, 0, 0, 0.35);
  font-size: 11px;
  font-weight: 600;
}

.goal-card-progress-value {
  color: rgba(0, 0, 0, 0.72);
  font-size: 12px;
  font-weight: 800;
  font-feature-settings: "tnum";
}

.goal-card-bar {
  height: 4px;
  overflow: hidden;
  border-radius: 999px;
  background: rgba(0, 0, 0, 0.04);
}

.goal-card-bar-fill {
  height: 100%;
  min-width: 4px;
  border-radius: 999px;
  background: #1677ff;
  transition: width 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.goal-card-done .goal-card-bar-fill {
  background: #52c41a;
}

.goal-card-high .goal-card-bar-fill {
  background: #ff4d4f;
}

/* 卡片底部 */
.goal-card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.goal-card-tags {
  display: flex;
  gap: 4px;
  overflow: hidden;
}

.gc-tag {
  display: inline-flex;
  align-items: center;
  max-width: 100px;
  padding: 2px 8px;
  overflow: hidden;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.gc-tag-cat {
  background: rgba(22, 119, 255, 0.06);
  color: #4096ff;
}

.gc-tag-custom {
  background: rgba(114, 46, 209, 0.06);
  color: #9254de;
}

.gc-tag-more {
  background: rgba(0, 0, 0, 0.03);
  color: rgba(0, 0, 0, 0.30);
}

.goal-card-deadline {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  color: rgba(0, 0, 0, 0.30);
  font-size: 11px;
  font-weight: 600;
  white-space: nowrap;
}

/* 高优标记 */
.goal-card-priority {
  position: absolute;
  top: 10px;
  right: 10px;
  color: #ff4d4f;
  font-size: 12px;
}

.goal-card:hover .goal-card-priority {
  display: none;
}

/* 分页 */
.goal-pagination {
  display: flex;
  justify-content: center;
  padding: 20px 0 8px;
}

/* ===== 动画 ===== */
.goal-card-anim-enter-active,
.goal-card-anim-leave-active {
  transition: all 0.2s ease;
}

.goal-card-anim-enter-from,
.goal-card-anim-leave-to {
  opacity: 0;
  transform: translateY(8px);
}

.goal-card-anim-move {
  transition: transform 0.2s ease;
}

@keyframes goalFadeUp {
  from {
    opacity: 0;
    transform: translateY(6px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes goalSlideDown {
  from {
    opacity: 0;
    transform: translateY(-4px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ===== Responsive ===== */
@media (max-width: 960px) {
  .shanzhu-goal-page {
    padding: 20px 20px 40px;
  }
  .goal-header-top {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  .goal-stats-bar {
    grid-template-columns: repeat(2, 1fr);
  }
  .goal-action-bar {
    flex-direction: column;
    align-items: stretch;
  }
  .goal-action-right {
    justify-content: space-between;
  }
  .goal-search-wrap {
    flex: 1;
  }
}

@media (max-width: 640px) {
  .shanzhu-goal-page {
    padding: 16px 12px 32px;
  }
  .goal-grid {
    grid-template-columns: 1fr;
    gap: 8px;
  }
  .goal-stats-bar {
    grid-template-columns: repeat(2, 1fr);
    gap: 8px;
  }
  .goal-stat-num {
    font-size: 22px;
  }
  .goal-card-title {
    font-size: 14px;
  }
}
</style>
