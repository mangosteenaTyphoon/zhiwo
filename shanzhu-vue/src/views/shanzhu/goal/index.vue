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
            <CheckCircleOutlined style="margin-right: 4px;"/> 任务中心
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
    </div>

    <div class="goal-content">
      <div class="goal-main">
        <div class="goal-body">
          <!-- 折叠式筛选区 -->
          <div class="goal-filter-bar">
            <a-button class="goal-filter-toggle" @click="filterExpanded = !filterExpanded">
              <template #icon><FilterOutlined/></template>
              筛选
              <span v-if="activeFilterCount > 0" class="goal-filter-badge">{{ activeFilterCount }}</span>
            </a-button>
            <div class="goal-search-float">
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

          <div v-show="filterExpanded" class="goal-filter-panel">
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
          </div>

          <a-spin :spinning="tableLoading">
            <div v-if="!tableLoading && goalList.length === 0" class="goal-empty">
              <div class="goal-empty-illustration">
                <svg width="160" height="120" viewBox="0 0 160 120" fill="none">
                  <circle cx="80" cy="60" r="40" fill="#f0f5ff" stroke="#d6e4ff" stroke-width="2"/>
                  <circle cx="80" cy="60" r="28" fill="#e6f0ff" stroke="#b7d5ff" stroke-width="1.5"/>
                  <path d="M68 60L76 68L92 52" stroke="#1677ff" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
                  <circle cx="55" cy="35" r="6" fill="#ffd666" opacity="0.6"/>
                  <circle cx="115" cy="45" r="4" fill="#95de64" opacity="0.5"/>
                  <circle cx="45" cy="80" r="5" fill="#ff9c6e" opacity="0.4"/>
                  <circle cx="125" cy="85" r="7" fill="#b37feb" opacity="0.3"/>
                </svg>
              </div>
              <h3 class="goal-empty-title">还没有目标</h3>
              <p class="goal-empty-desc">设定一个清晰的目标，让每一步行动都有方向</p>
              <a-button type="primary" shape="round" size="large" @click="openCreateModal">
                <template #icon><PlusOutlined/></template>
                创建第一个目标
              </a-button>
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
                <!-- 左侧状态色带 -->
                <div class="goal-status-strip" :class="'goal-strip-' + goal.status"></div>

                <div class="goal-item-inner">
                  <!-- 环形进度图 -->
                  <div class="goal-ring-wrap" @click="openDetailPage(goal.id)">
                    <svg class="goal-ring-svg" viewBox="0 0 64 64">
                      <defs>
                        <linearGradient id="goalRingGradient" x1="0%" y1="0%" x2="100%" y2="0%">
                          <stop offset="0%" stop-color="#1677ff"/>
                          <stop offset="100%" stop-color="#52c41a"/>
                        </linearGradient>
                      </defs>
                      <circle class="goal-ring-track" cx="32" cy="32" r="26"/>
                      <circle
                          class="goal-ring-fill"
                          cx="32" cy="32" r="26"
                          :stroke-dasharray="`${163.36} ${163.36}`"
                          :stroke-dashoffset="`${163.36 - (163.36 * (goal.progress || 0) / 100)}`"
                      />
                    </svg>
                    <div class="goal-ring-text">
                      <span class="goal-ring-percent">{{ goal.progress || 0 }}</span>
                      <span class="goal-ring-unit">%</span>
                    </div>
                  </div>

                  <!-- 内容区 -->
                  <div class="goal-item-body" @click="openDetailPage(goal.id)">
                    <div class="goal-item-header">
                      <div class="goal-item-title">{{ goal.title }}</div>
                      <div class="goal-item-meta">
                        <span class="goal-status-badge" :class="'goal-badge-' + goal.status">
                          <component :is="getStatusIcon(goal.status)"/>
                          {{ getGoalStatusOption(goal.status).label }}
                        </span>
                        <span v-if="goal.priority === 3" class="goal-priority-flag">
                          <FireOutlined/> 高优
                        </span>
                      </div>
                    </div>

                    <div class="goal-item-desc">{{ goal.description || '暂无目标描述' }}</div>

                    <!-- 分段式进度轨道 -->
                    <div class="goal-track-bar">
                      <div class="goal-track-fill" :style="{ width: (goal.progress || 0) + '%' }"></div>
                    </div>

                    <div class="goal-item-footer">
                      <div class="goal-item-tags">
                        <span class="goal-tag goal-tag-category">
                          {{ goal.categoryName || '未分类' }}
                        </span>
                        <span v-for="tag in goal.tags.slice(0, 2)" :key="tag.id" class="goal-tag goal-tag-custom">
                          {{ tag.name }}
                        </span>
                        <span v-if="goal.tags.length > 2" class="goal-tag goal-tag-more">+{{ goal.tags.length - 2 }}</span>
                      </div>
                      <div class="goal-item-dates">
                        <span v-if="goal.deadline" class="goal-deadline">
                          <ClockCircleOutlined style="margin-right: 3px;"/> {{ goal.deadline }}
                        </span>
                      </div>
                    </div>
                  </div>

                  <!-- 操作区 -->
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
        <!-- 数据可视化面板 -->
        <div class="goal-sidebar-card goal-sidebar-anim">
          <h4 class="goal-sidebar-title"><BarChartOutlined style="margin-right: 6px;"/> 目标分布</h4>
          <div class="goal-donut-chart">
            <svg viewBox="0 0 120 120" class="goal-donut-svg">
              <!-- 背景圆环 -->
              <circle cx="60" cy="60" r="44" fill="none" stroke="#f0f0f0" stroke-width="16"/>
              <!-- 已完成 -->
              <circle
                  v-if="goalOverview.completed > 0"
                  cx="60" cy="60" r="44" fill="none"
                  stroke="#52c41a" stroke-width="16"
                  stroke-linecap="round"
                  :stroke-dasharray="`${goalOverview.completed / Math.max(goalOverview.total, 1) * 276.46} 276.46`"
                  stroke-dashoffset="0"
                  transform="rotate(-90 60 60)"
              />
              <!-- 进行中 -->
              <circle
                  v-if="goalOverview.inProgress > 0"
                  cx="60" cy="60" r="44" fill="none"
                  stroke="#1677ff" stroke-width="16"
                  stroke-linecap="round"
                  :stroke-dasharray="`${goalOverview.inProgress / Math.max(goalOverview.total, 1) * 276.46} 276.46`"
                  :stroke-dashoffset="`-${goalOverview.completed / Math.max(goalOverview.total, 1) * 276.46}`"
                  transform="rotate(-90 60 60)"
              />
              <!-- 未开始 -->
              <circle
                  v-if="goalOverview.total - goalOverview.completed - goalOverview.inProgress > 0"
                  cx="60" cy="60" r="44" fill="none"
                  stroke="#d9d9d9" stroke-width="16"
                  stroke-linecap="round"
                  :stroke-dasharray="`${(goalOverview.total - goalOverview.completed - goalOverview.inProgress) / Math.max(goalOverview.total, 1) * 276.46} 276.46`"
                  :stroke-dashoffset="`-${(goalOverview.completed + goalOverview.inProgress) / Math.max(goalOverview.total, 1) * 276.46}`"
                  transform="rotate(-90 60 60)"
              />
            </svg>
            <div class="goal-donut-center">
              <span class="goal-donut-number">{{ goalOverview.total }}</span>
              <span class="goal-donut-label">目标总数</span>
            </div>
          </div>
          <div class="goal-donut-legend">
            <div class="goal-legend-item">
              <span class="goal-legend-dot" style="background: #52c41a;"></span>
              <span class="goal-legend-label">已完成</span>
              <span class="goal-legend-value">{{ goalOverview.completed }}</span>
            </div>
            <div class="goal-legend-item">
              <span class="goal-legend-dot" style="background: #1677ff;"></span>
              <span class="goal-legend-label">进行中</span>
              <span class="goal-legend-value">{{ goalOverview.inProgress }}</span>
            </div>
            <div class="goal-legend-item">
              <span class="goal-legend-dot" style="background: #d9d9d9;"></span>
              <span class="goal-legend-label">其他</span>
              <span class="goal-legend-value">{{ goalOverview.total - goalOverview.completed - goalOverview.inProgress }}</span>
            </div>
          </div>
        </div>

        <!-- 进度概览 -->
        <div class="goal-sidebar-card goal-sidebar-anim">
          <h4 class="goal-sidebar-title"><RiseOutlined style="margin-right: 6px;"/> 进度概览</h4>
          <div class="goal-progress-overview">
            <div class="goal-progress-big">
              <span class="goal-progress-number">{{ goalOverview.avgProgress }}</span>
              <span class="goal-progress-unit">%</span>
            </div>
            <div class="goal-progress-label">平均完成度</div>
            <div class="goal-progress-track">
              <div class="goal-progress-track-fill" :style="{ width: goalOverview.avgProgress + '%' }"></div>
            </div>
          </div>
          <div class="goal-stat-divider"></div>
          <div class="goal-sidebar-actions">
            <a-button block @click="router.push('/shanzhu/task')">
              <template #icon><CheckCircleOutlined/></template>
              任务中心
            </a-button>
            <a-button block @click="router.push('/shanzhu/today-work')">
              <template #icon><DesktopOutlined/></template>
              今日工作台
            </a-button>
            <a-button block @click="router.push('/shanzhu/review')">
              <template #icon><FileTextOutlined/></template>
              复盘中心
            </a-button>
          </div>
        </div>

        <div class="goal-sidebar-card goal-sidebar-card-tip goal-sidebar-anim">
          <h4 class="goal-sidebar-title"><BulbOutlined style="margin-right: 6px;"/> 目标提示</h4>
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
import {
  BarChartOutlined,
  BulbOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  DesktopOutlined,
  FileTextOutlined,
  FilterOutlined,
  FireOutlined,
  MoreOutlined,
  PlusOutlined,
  RedoOutlined,
  RiseOutlined,
  SearchOutlined,
  ThunderboltOutlined,
  CheckOutlined,
  PauseOutlined,
  StopOutlined,
  MinusOutlined,
  CloseOutlined
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
  max-width: 1360px;
  min-height: calc(100vh - 120px);
  margin: 0 auto;
  padding: 36px 48px 56px;
  overflow-x: hidden;
}

/* ===== Header ===== */
.goal-header {
  position: relative;
  z-index: 2;
  padding: 20px 28px;
  margin-bottom: 12px;
  overflow: hidden;
  border: 1px solid rgba(22, 119, 255, 0.08);
  border-radius: 22px;
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
  font-size: 26px;
  font-weight: 850;
  line-height: 1.2;
  letter-spacing: -0.5px;
}

.goal-page-desc {
  margin: 5px 0 0;
  color: rgba(0, 0, 0, 0.48);
  font-size: 13px;
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

/* ===== Toolbar ===== */
.goal-toolbar {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 10px;
  padding: 6px 8px;
  border: 1px solid rgba(0, 0, 0, 0.04);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.76);
  box-shadow: 0 6px 18px rgba(15, 35, 80, 0.03);
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

/* ===== 折叠式筛选区 ===== */
.goal-filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  border-bottom: 1px solid rgba(15, 35, 80, 0.04);
}

.goal-filter-toggle {
  display: flex;
  align-items: center;
  gap: 6px;
  height: 34px;
  padding: 0 14px;
  border: 1px solid rgba(15, 35, 80, 0.08);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.8);
  color: rgba(0, 0, 0, 0.65);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.goal-filter-toggle:hover {
  border-color: rgba(22, 119, 255, 0.25);
  background: #fff;
  color: #1677ff;
}

.goal-filter-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 999px;
  background: #ff4d4f;
  color: #fff;
  font-size: 11px;
  font-weight: 700;
}

.goal-search-float {
  width: 200px;
  flex-shrink: 0;
  margin-left: auto;
}

.goal-search-float :deep(.ant-input-affix-wrapper) {
  height: 36px;
  border-color: transparent;
  border-radius: 12px;
  background: rgba(247, 248, 250, 0.92);
}

.goal-search-float :deep(.ant-input-affix-wrapper:hover),
.goal-search-float :deep(.ant-input-affix-wrapper-focused) {
  border-color: rgba(22, 119, 255, 0.20);
  background: #fff;
  box-shadow: 0 6px 16px rgba(22, 119, 255, 0.08);
}

.goal-filter-panel {
  overflow: hidden;
  animation: goalSlideDown 0.25s ease;
}

.goal-filter-strip {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  padding: 14px 18px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.92), rgba(248, 251, 255, 0.78));
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

/* ===== Content ===== */
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

/* ===== 空状态 ===== */
.goal-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 72px 20px;
  text-align: center;
}

.goal-empty-illustration {
  margin-bottom: 20px;
}

.goal-empty-title {
  margin: 0 0 8px;
  color: rgba(0, 0, 0, 0.78);
  font-size: 18px;
  font-weight: 750;
}

.goal-empty-desc {
  margin: 0 0 24px;
  color: rgba(0, 0, 0, 0.45);
  font-size: 14px;
}

/* ===== 列表项 - 卡片式 + 左侧色带 ===== */
.goal-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 10px 10px;
}

.goal-item {
  position: relative;
  display: flex;
  padding: 0;
  overflow: hidden;
  border: 1px solid rgba(15, 35, 80, 0.05);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 2px 8px rgba(15, 35, 80, 0.04);
  transition: background-color 0.18s ease, box-shadow 0.22s ease, transform 0.18s ease, border-color 0.18s ease;
}

.goal-item:hover {
  z-index: 2;
  border-color: rgba(22, 119, 255, 0.12);
  background: #fff;
  box-shadow: 0 10px 32px rgba(15, 35, 80, 0.10);
  transform: translateY(-2px);
}

.goal-item-done {
  background: linear-gradient(135deg, rgba(246, 255, 237, 0.7), rgba(240, 249, 235, 0.5));
}

.goal-item-done:hover {
  background: linear-gradient(135deg, rgba(246, 255, 237, 0.9), rgba(240, 249, 235, 0.7));
}

/* 左侧状态色带 */
.goal-status-strip {
  width: 5px;
  flex-shrink: 0;
  border-radius: 999px 0 0 999px;
  background: transparent;
}

.goal-strip-completed {
  background: linear-gradient(180deg, #52c41a, #73d13d);
}

.goal-strip-in_progress {
  background: linear-gradient(180deg, #1677ff, #4096ff);
}

.goal-strip-not_started {
  background: linear-gradient(180deg, #bfbfbf, #d9d9d9);
}

.goal-strip-paused {
  background: linear-gradient(180deg, #faad14, #ffc53d);
}

.goal-strip-cancelled {
  background: linear-gradient(180deg, #ff4d4f, #ff7875);
}

.goal-item-high .goal-status-strip {
  background: linear-gradient(180deg, #ff4d4f, #ff7875);
}

/* 卡片内部布局 */
.goal-item-inner {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 22px 24px;
  flex: 1;
  min-width: 0;
}

/* ===== 环形进度图 ===== */
.goal-ring-wrap {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 66px;
  height: 66px;
  flex-shrink: 0;
  border-radius: 50%;
  background: radial-gradient(circle at 50% 50%, rgba(22, 119, 255, 0.04), transparent 70%);
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.goal-ring-wrap:hover {
  transform: scale(1.08);
  box-shadow: 0 6px 20px rgba(22, 119, 255, 0.12);
}

.goal-ring-svg {
  width: 66px;
  height: 66px;
  transform: rotate(-90deg);
}

.goal-ring-track {
  fill: none;
  stroke: rgba(15, 35, 80, 0.08);
  stroke-width: 6;
  stroke-linecap: round;
}

.goal-ring-fill {
  fill: none;
  stroke: url(#goalRingGradient);
  stroke-width: 6;
  stroke-linecap: round;
  transition: stroke-dashoffset 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  filter: drop-shadow(0 2px 6px rgba(22, 119, 255, 0.25));
}

.goal-ring-text {
  position: absolute;
  display: flex;
  align-items: baseline;
  gap: 1px;
}

.goal-ring-percent {
  color: rgba(0, 0, 0, 0.82);
  font-size: 16px;
  font-weight: 900;
  font-feature-settings: "tnum";
}

.goal-ring-unit {
  color: rgba(0, 0, 0, 0.38);
  font-size: 10px;
  font-weight: 700;
}

.goal-item-done .goal-ring-wrap {
  background: radial-gradient(circle at 50% 50%, rgba(82, 196, 26, 0.06), transparent 70%);
}

.goal-item-done .goal-ring-wrap:hover {
  box-shadow: 0 6px 20px rgba(82, 196, 26, 0.15);
}

.goal-item-done .goal-ring-fill {
  stroke: #52c41a;
  filter: drop-shadow(0 2px 6px rgba(82, 196, 26, 0.25));
}

.goal-item-done .goal-ring-percent {
  color: #389e0d;
}

/* ===== 内容区 ===== */
.goal-item-body {
  min-width: 0;
  flex: 1;
  cursor: pointer;
}

.goal-item-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 4px;
}

.goal-item-title {
  display: -webkit-box;
  overflow: hidden;
  color: rgba(0, 0, 0, 0.86);
  font-size: 15px;
  font-weight: 750;
  line-height: 22px;
  word-break: break-word;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
}

.goal-item-desc {
  display: -webkit-box;
  min-height: 18px;
  margin-bottom: 12px;
  overflow: hidden;
  color: rgba(0, 0, 0, 0.42);
  font-size: 13px;
  line-height: 20px;
  word-break: break-word;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
}

/* 状态徽章 */
.goal-item-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.goal-status-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
}

.goal-badge-completed {
  background: rgba(82, 196, 26, 0.12);
  color: #389e0d;
}

.goal-badge-in_progress {
  background: rgba(22, 119, 255, 0.10);
  color: #1677ff;
}

.goal-badge-not_started {
  background: rgba(0, 0, 0, 0.05);
  color: rgba(0, 0, 0, 0.52);
}

.goal-badge-paused {
  background: rgba(250, 173, 20, 0.13);
  color: #d48806;
}

.goal-badge-cancelled {
  background: rgba(255, 77, 79, 0.10);
  color: #cf1322;
}

.goal-priority-flag {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 3px 8px;
  border-radius: 999px;
  background: rgba(255, 77, 79, 0.10);
  color: #cf1322;
  font-size: 11px;
  font-weight: 700;
}

/* ===== 分段式进度轨道 ===== */
.goal-track-bar {
  position: relative;
  height: 7px;
  margin-bottom: 14px;
  overflow: hidden;
  border-radius: 999px;
  background: rgba(15, 35, 80, 0.06);
}

.goal-track-fill {
  min-width: 7px;
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, #1677ff, #52c41a);
  box-shadow: 0 1px 4px rgba(22, 119, 255, 0.25);
  transition: width 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.goal-item-done .goal-track-fill {
  background: linear-gradient(90deg, #52c41a, #73d13d);
}

.goal-item-high .goal-track-fill {
  background: linear-gradient(90deg, #ff4d4f, #ff7875);
}

/* ===== 底部标签和日期 ===== */
.goal-item-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.goal-item-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  overflow: hidden;
}

.goal-tag {
  display: inline-flex;
  align-items: center;
  max-width: 140px;
  min-height: 24px;
  padding: 3px 10px;
  overflow: hidden;
  border-radius: 999px;
  background: rgba(15, 35, 80, 0.055);
  color: rgba(0, 0, 0, 0.56);
  font-size: 11px;
  font-weight: 700;
  line-height: 18px;
  text-overflow: ellipsis;
  white-space: nowrap;
  letter-spacing: 0.2px;
}

.goal-tag-category {
  background: rgba(22, 119, 255, 0.10);
  color: #1677ff;
}

.goal-tag-custom {
  background: rgba(114, 46, 209, 0.09);
  color: #722ed1;
}

.goal-tag-more {
  background: rgba(0, 0, 0, 0.04);
  color: rgba(0, 0, 0, 0.38);
  font-weight: 600;
}

.goal-item-dates {
  flex-shrink: 0;
}

.goal-deadline {
  display: inline-flex;
  align-items: center;
  color: rgba(0, 0, 0, 0.38);
  font-size: 11px;
  font-weight: 600;
}

/* ===== 操作按钮 ===== */
.goal-item-actions {
  display: flex;
  align-items: center;
  gap: 2px;
  flex-shrink: 0;
  opacity: 0;
  transition: opacity 0.18s ease;
}

.goal-item:hover .goal-item-actions {
  opacity: 1;
}

.goal-detail-btn,
.goal-action-btn {
  border-radius: 999px;
  color: rgba(0, 0, 0, 0.45);
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

/* ===== Sidebar ===== */
.goal-sidebar {
  width: 300px;
  flex-shrink: 0;
}

.goal-sidebar-card {
  margin-bottom: 16px;
  padding: 20px;
  border: 1px solid rgba(15, 35, 80, 0.06);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 14px 34px rgba(15, 35, 80, 0.055);
  backdrop-filter: blur(12px);
}

.goal-sidebar-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 16px;
  color: rgba(0, 0, 0, 0.82);
  font-size: 15px;
  font-weight: 800;
}

/* 环形图面板 */
.goal-donut-chart {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 140px;
  height: 140px;
  margin: 0 auto 16px;
}

.goal-donut-svg {
  width: 140px;
  height: 140px;
  transform: rotate(-90deg);
}

.goal-donut-center {
  position: absolute;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.goal-donut-number {
  color: rgba(0, 0, 0, 0.82);
  font-size: 26px;
  font-weight: 850;
  line-height: 1;
}

.goal-donut-label {
  margin-top: 2px;
  color: rgba(0, 0, 0, 0.42);
  font-size: 11px;
  font-weight: 600;
}

.goal-donut-legend {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.goal-legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.goal-legend-dot {
  width: 8px;
  height: 8px;
  flex-shrink: 0;
  border-radius: 999px;
}

.goal-legend-label {
  flex: 1;
  color: rgba(0, 0, 0, 0.52);
  font-size: 12px;
  font-weight: 600;
}

.goal-legend-value {
  color: rgba(0, 0, 0, 0.72);
  font-size: 13px;
  font-weight: 750;
}

/* 进度概览 */
.goal-progress-overview {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 0 16px;
}

.goal-progress-big {
  display: flex;
  align-items: baseline;
  gap: 2px;
}

.goal-progress-number {
  color: #1677ff;
  font-size: 36px;
  font-weight: 850;
  line-height: 1;
}

.goal-progress-unit {
  color: rgba(0, 0, 0, 0.35);
  font-size: 16px;
  font-weight: 700;
}

.goal-progress-label {
  margin-top: 6px;
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
  font-weight: 600;
}

.goal-progress-track {
  width: 100%;
  height: 6px;
  margin-top: 12px;
  overflow: hidden;
  border-radius: 999px;
  background: rgba(15, 35, 80, 0.06);
}

.goal-progress-track-fill {
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, #1677ff, #52c41a);
  transition: width 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.goal-stat-divider {
  height: 1px;
  margin: 4px 0 12px;
  background: rgba(15, 35, 80, 0.07);
}

.goal-sidebar-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
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

/* ===== Animations ===== */
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

@keyframes goalSlideDown {
  from {
    opacity: 0;
    transform: translateY(-6px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ===== Responsive ===== */
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
  .goal-search-float {
    width: 100%;
  }
  .goal-sidebar {
    width: 100%;
  }
  .goal-item-inner {
    gap: 14px;
    padding: 16px;
  }
  .goal-ring-wrap {
    width: 52px;
    height: 52px;
  }
  .goal-ring-svg {
    width: 52px;
    height: 52px;
  }
  .goal-ring-percent {
    font-size: 13px;
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
  .goal-item-inner {
    gap: 12px;
    padding: 14px 12px;
  }
  .goal-ring-wrap {
    width: 48px;
    height: 48px;
  }
  .goal-ring-svg {
    width: 48px;
    height: 48px;
  }
  .goal-item-header {
    flex-direction: column;
    gap: 6px;
  }
  .goal-item-actions {
    position: absolute;
    right: 8px;
    bottom: 8px;
  }
  .goal-detail-btn {
    display: none;
  }
  .goal-item-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
  }
}
</style>
