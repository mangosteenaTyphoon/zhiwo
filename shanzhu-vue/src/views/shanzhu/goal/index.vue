<template>
  <div class="shanzhu-goal-page">
    <!-- 顶部：恢复任务中心同款高级横幅 -->
    <div class="goal-header goal-anim-header">
      <div class="goal-header-top">
        <div>
          <div class="goal-eyebrow">Goal Studio</div>
          <h2 class="goal-page-title">目标管理</h2>
          <p class="goal-page-desc">把长期方向拆成可见进度，用目标牵引每日行动。</p>
        </div>
        <a-space>
          <a-button class="goal-secondary-btn" @click="router.push('/shanzhu/task')">
            <RiseOutlined style="margin-right: 4px;"/> 任务中心
          </a-button>
          <a-button type="primary" shape="round" size="large" @click="openCreateModal">
            <template #icon><PlusOutlined/></template>
            新建目标
          </a-button>
        </a-space>
      </div>
    </div>

    <!-- Tab 筛选 + 搜索 -->
    <div class="goal-toolbar goal-anim-toolbar">
      <div ref="goalTabsRef" class="goal-tabs">
        <span class="goal-tab-indicator" :style="goalTabIndicatorStyle"></span>
        <button
            v-for="(tab, index) in statusTabs"
            :key="tab.value || 'all'"
            ref="goalTabRefs"
            class="goal-tab"
            :class="{ 'goal-tab-active': activeStatus === tab.value }"
            @click="handleStatusChange(tab.value, index)"
        >
          {{ tab.label }}
          <span class="goal-tab-count">{{ tab.count }}</span>
        </button>
      </div>
      <div class="goal-toolbar-right">
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
        <a-button class="goal-filter-btn" @click="filterExpanded = !filterExpanded">
          <template #icon><FilterOutlined/></template>
          筛选
          <span v-if="activeFilterCount > 0" class="goal-filter-dot"></span>
        </a-button>
      </div>
    </div>

    <Transition name="goal-filter-expand">
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
    </Transition>

    <!-- 主体：目标列表 + 侧边栏 -->
    <div class="goal-content">
      <div class="goal-main">
        <div ref="goalBodyRef" class="goal-body">
          <a-spin :spinning="tableLoading">
            <!-- 列表表头 -->
            <div v-if="goalList.length > 0" class="goal-list-header">
              <span class="goal-list-count">共 <strong>{{ goalList.length }}</strong> 个目标</span>
              <a-dropdown>
                <a-button type="text" size="small" class="goal-sort-btn">
                  <SortAscendingOutlined style="margin-right: 4px;"/> 按截止日期
                  <DownOutlined style="font-size: 10px; margin-left: 2px;"/>
                </a-button>
                <template #overlay>
                  <a-menu>
                    <a-menu-item>按截止日期</a-menu-item>
                    <a-menu-item>按创建时间</a-menu-item>
                    <a-menu-item>按优先级</a-menu-item>
                    <a-menu-item>按进度</a-menu-item>
                  </a-menu>
                </template>
              </a-dropdown>
            </div>

            <div v-if="!tableLoading && goalList.length === 0" class="goal-empty">
              <div class="goal-empty-icon">
                <AimOutlined/>
              </div>
              <h3 class="goal-empty-title">还没有目标</h3>
              <p class="goal-empty-desc">设定一个清晰的目标，让每一步行动都有方向。</p>
              <a-button type="primary" shape="round" @click="openCreateModal">
                <template #icon><PlusOutlined/></template>
                创建第一个目标
              </a-button>
            </div>

            <div v-else-if="goalList.length > 0" class="goal-list">
              <div
                  v-for="(goal, index) in goalList"
                  :key="goal.id"
                  class="goal-item"
                  :class="{
                    'goal-item-done': goal.status === 'completed',
                    'goal-item-high': goal.priority === 3 && goal.status !== 'completed'
                  }"
              >
                <div class="goal-progress-ring" @click="openDetailPage(goal.id)">
                  <div class="goal-progress-number">{{ goal.progress || 0 }}</div>
                  <div class="goal-progress-unit">%</div>
                </div>

                <div class="goal-item-body" @click="openDetailPage(goal.id)">
                  <div class="goal-item-title">{{ goal.title }}</div>
                  <div class="goal-item-desc">{{ goal.description || '暂无目标描述' }}</div>
                  <div class="goal-item-track-row">
                    <div class="goal-item-track">
                      <div class="goal-item-track-fill" :style="{ width: (goal.progress || 0) + '%' }"></div>
                    </div>
                    <span class="goal-item-track-pct">{{ goal.progress || 0 }}%</span>
                  </div>
                  <div class="goal-item-tags">
                    <span v-if="goal.priority === 3" class="goal-tag goal-tag-red">高优</span>
                    <span class="goal-tag goal-tag-status" :class="'goal-tag-status-' + goal.status">
                      <component :is="getStatusIcon(goal.status)" style="font-size: 10px;"/>
                      {{ getGoalStatusOption(goal.status).label }}
                    </span>
                    <span class="goal-tag goal-tag-category">{{ goal.categoryName || '未分类' }}</span>
                    <span v-for="tag in goal.tags.slice(0, 2)" :key="tag.id" class="goal-tag goal-tag-custom">{{ tag.name }}</span>
                    <span v-if="goal.tags.length > 2" class="goal-tag goal-tag-more">+{{ goal.tags.length - 2 }}</span>
                  </div>
                </div>

                <div class="goal-item-right">
                  <div v-if="goal.deadline" class="goal-item-date-block">
                    <span class="goal-item-date-label">截止日期</span>
                    <span class="goal-item-date-value">
                      <CalendarOutlined style="margin-right: 4px;"/> {{ goal.deadline }}
                    </span>
                  </div>
                  <div class="goal-item-actions" @click.stop>
                    <a-button type="link" size="small" class="goal-detail-link" @click="openDetailPage(goal.id)">详情</a-button>
                    <a-dropdown>
                      <a-button type="text" size="small" class="goal-more-btn">
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
            </div>

            <div class="goal-pagination">
              <span class="goal-pagination-info">共 {{ goalList.length }} 条</span>
            </div>
          </a-spin>
        </div>
      </div>

      <div class="goal-sidebar">
        <div class="goal-sidebar-card goal-overview-card goal-anim-sidebar" style="--sidebar-order: 0;">
          <div class="goal-sidebar-title">
            <RiseOutlined/>
            目标概览
          </div>
          <div class="goal-overview-row">
            <span>全部目标</span>
            <strong>{{ goalOverview.total }}</strong>
          </div>
          <div class="goal-overview-row goal-overview-blue">
            <span>进行中</span>
            <strong>{{ goalOverview.inProgress }}</strong>
          </div>
          <div class="goal-overview-row goal-overview-green">
            <span>已完成</span>
            <strong>{{ goalOverview.completed }}</strong>
          </div>
          <div class="goal-overview-row">
            <span>平均进度</span>
            <strong>{{ goalOverview.avgProgress }}%</strong>
          </div>
          <div class="goal-overview-progress">
            <div class="goal-overview-progress-fill" :style="{ width: goalOverview.avgProgress + '%' }"></div>
          </div>
        </div>

        <div class="goal-sidebar-card goal-anim-sidebar" style="--sidebar-order: 1;">
          <div class="goal-sidebar-title">
            <ThunderboltOutlined/>
            快捷入口
          </div>
          <div class="goal-shortcut-list">
            <div class="goal-shortcut-item" @click="router.push('/shanzhu/task')">
              <UnorderedListOutlined class="goal-shortcut-icon"/>
              <span class="goal-shortcut-text">任务中心</span>
              <RightOutlined class="goal-shortcut-arrow"/>
            </div>
            <div class="goal-shortcut-item" @click="router.push('/shanzhu/today-work')">
              <DesktopOutlined class="goal-shortcut-icon"/>
              <span class="goal-shortcut-text">今日工作台</span>
              <RightOutlined class="goal-shortcut-arrow"/>
            </div>
            <div class="goal-shortcut-item" @click="router.push('/shanzhu/review')">
              <HistoryOutlined class="goal-shortcut-icon"/>
              <span class="goal-shortcut-text">复盘中心</span>
              <RightOutlined class="goal-shortcut-arrow"/>
            </div>
          </div>
        </div>

        <div class="goal-sidebar-card goal-tip-card goal-anim-sidebar" style="--sidebar-order: 2;">
          <div class="goal-sidebar-title">
            <BulbOutlined/>
            目标提示
          </div>
          <ul class="goal-tip-list">
            <li>目标要能拆成任务</li>
            <li>建议设置明确截止日期</li>
            <li>定期复盘目标完成情况</li>
            <li>优先维护正在推进的目标</li>
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
import {computed, nextTick, onMounted, reactive, ref, watch} from "vue";
import {useRouter} from "vue-router";
import type {FormInstance, Rule} from "ant-design-vue/es/form";
import {message, Modal} from "ant-design-vue";
import {
  AimOutlined,
  BulbOutlined,
  CalendarOutlined,
  CheckOutlined,
  ClockCircleOutlined,
  DesktopOutlined,
  DownOutlined,
  FilterOutlined,
  HistoryOutlined,
  MinusOutlined,
  MoreOutlined,
  PauseOutlined,
  PlusOutlined,
  RedoOutlined,
  RightOutlined,
  RiseOutlined,
  SearchOutlined,
  SortAscendingOutlined,
  StopOutlined,
  ThunderboltOutlined,
  UnorderedListOutlined
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
const allGoalList = ref<ShanzhuGoalVO[]>([]);
const tableLoading = ref(false);
const listTransitionDirection = ref<"left" | "right">("right");
const goalTabsRef = ref<HTMLElement>();
const goalTabRefs = ref<HTMLElement[]>([]);
const goalTabIndicatorStyle = reactive({
  left: "0px",
  width: "0px",
  opacity: 0
});
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

const goalList = computed(() => {
  const status = activeStatus.value;
  if (!status) return allGoalList.value;
  return allGoalList.value.filter(goal => goal.status === status);
});

const goalTotal = computed(() => allGoalList.value.length);

const statusTabs = computed(() => [
  {label: "全部", value: "", count: allGoalList.value.length},
  ...goalStatusOptions.map(item => ({
    ...item,
    count: allGoalList.value.filter(goal => goal.status === item.value).length
  }))
]);

const goalOverview = computed(() => {
  const allGoals = allGoalList.value;
  const inProgressCount = allGoals.filter(goal => goal.status === "in_progress").length;
  const completedCount = allGoals.filter(goal => goal.status === "completed").length;
  const totalProgress = allGoals.reduce((sum, goal) => sum + (goal.progress || 0), 0);
  const avgProgress = allGoals.length ? Math.round(totalProgress / allGoals.length) : 0;

  return {
    total: allGoals.length,
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

const activeTabIndex = computed(() => {
  const currentIndex = statusTabs.value.findIndex(tab => tab.value === activeStatus.value);
  return currentIndex >= 0 ? currentIndex : 0;
});

const updateGoalTabIndicator = async () => {
  await nextTick();

  const tabsElement = goalTabsRef.value;
  const activeTabElement = goalTabRefs.value[activeTabIndex.value];

  if (!tabsElement || !activeTabElement) {
    return;
  }

  const tabsRect = tabsElement.getBoundingClientRect();
  const activeTabRect = activeTabElement.getBoundingClientRect();

  const indicatorOffset = activeTabRect.left - tabsRect.left + tabsElement.scrollLeft;

  goalTabIndicatorStyle.left = `${indicatorOffset}px`;
  goalTabIndicatorStyle.width = `${activeTabRect.width}px`;
  goalTabIndicatorStyle.opacity = 1;
};

const goalBodyRef = ref<HTMLElement>();

const playGoalBodySwitchAnimation = () => {
  const goalBodyElement = goalBodyRef.value;
  if (!goalBodyElement) {
    return;
  }

  const startOffset = listTransitionDirection.value === "right" ? 18 : -18;
  const transformOrigin = listTransitionDirection.value === "right" ? "left center" : "right center";
  goalBodyElement.getAnimations().forEach(animation => animation.cancel());
  goalBodyElement.animate(
    [
      {
        transform: `translate3d(${startOffset}px, 0, 0) scaleX(0.982)`,
        transformOrigin
      },
      {
        transform: "translate3d(0, 0, 0) scaleX(1)",
        transformOrigin
      }
    ],
    {
      duration: 260,
      easing: "cubic-bezier(0.2, 0, 0.2, 1)"
    }
  );
};

const handleStatusChange = async (status: string, tabIndex?: number) => {
  if (typeof tabIndex === "number") {
    listTransitionDirection.value = tabIndex >= activeTabIndex.value ? "right" : "left";
  }

  goalQuery.value.status = status || undefined;

  if (typeof tabIndex === "number") {
    await nextTick();
    await updateGoalTabIndicator();
  }

  await nextTick();
  playGoalBodySwitchAnimation();
};

const handleSearchChange = async () => {
  if (!goalQuery.value.keyword) {
    await queryPage();
  }
};

const fetchAllGoals = async () => {
  tableLoading.value = true;
  try {
    const response = await queryGoalPage({
      ...goalQuery.value,
      status: undefined,
      pageNum: 1,
      pageSize: 100
    });
    if (response.code === 200) {
      allGoalList.value = response.data.records || [];
    } else {
      message.error(response.msg);
    }
  } finally {
    tableLoading.value = false;
  }
};

const initPage = async () => {
  await fetchAllGoals();
};

const queryPage = async () => {
  await fetchAllGoals();
};

const resetPage = async () => {
  goalQuery.value = defaultGoalQuery();
  await fetchAllGoals();
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

watch(activeTabIndex, () => {
  updateGoalTabIndicator();
});

onMounted(async () => {
  await initPage();
  await updateGoalTabIndicator();
});
</script>

<style scoped>
/* ===== Goal page refined layout ===== */
.shanzhu-goal-page {
  position: relative;
  max-width: 1360px;
  min-height: calc(100vh - 120px);
  margin: 0 auto;
  padding: 36px 48px 56px;
  overflow-x: hidden;
  isolation: isolate;
}

.goal-header {
  position: relative;
  z-index: 2;
  margin-bottom: 16px;
  padding: 24px 32px;
  overflow: hidden;
  border: 1px solid rgba(22, 119, 255, 0.06);
  border-radius: 24px;
  background:
    radial-gradient(circle at 10% 20%, rgba(22, 119, 255, 0.12), transparent 40%),
    radial-gradient(circle at 100% 0%, rgba(200, 160, 240, 0.14), transparent 40%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.98), rgba(245, 248, 255, 0.96));
  box-shadow: 0 14px 34px rgba(15, 35, 80, 0.052);
}

.goal-header-top {
  position: relative;
  z-index: 1;
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
  margin: 6px 0 0;
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
  position: relative;
  display: flex;
  gap: 6px;
  overflow: hidden;
  contain: layout paint;
  isolation: isolate;
}

.goal-tab-indicator {
  position: absolute;
  top: 0;
  bottom: 0;
  left: 0;
  z-index: 0;
  border-radius: 12px;
  background: #eaf3ff;
  box-shadow: inset 0 0 0 1px rgba(22, 119, 255, 0.06), 0 2px 8px rgba(22, 119, 255, 0.08);
  pointer-events: none;
  transition:
    left 0.28s cubic-bezier(0.23, 1, 0.32, 1),
    width 0.24s ease,
    opacity 0.2s ease;
  contain: paint;
}

.goal-tab {
  position: relative;
  z-index: 1;
  display: flex;
  min-height: 34px;
  align-items: center;
  gap: 6px;
  padding: 7px 14px;
  border: none;
  border-radius: 12px;
  background: transparent;
  color: rgba(0, 0, 0, 0.54);
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
  cursor: pointer;
  transition: color 0.22s ease, transform 0.2s ease;
}

.goal-tab:hover {
  color: rgba(0, 0, 0, 0.78);
}

.goal-tab-active {
  color: #1677ff;
}

.goal-tab-active .goal-tab-count {
  animation: goalCountBounce 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
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

.goal-toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.goal-search {
  width: 220px;
}

.goal-search :deep(.ant-input-affix-wrapper) {
  height: 34px;
  border-color: rgba(15, 35, 80, 0.07);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.38);
}

.goal-search :deep(.ant-input-affix-wrapper:hover),
.goal-search :deep(.ant-input-affix-wrapper-focused) {
  border-color: rgba(22, 119, 255, 0.20);
  background: #fff;
  box-shadow: 0 6px 16px rgba(22, 119, 255, 0.08);
}

.goal-toolbar .goal-filter-btn {
  position: relative;
  height: 34px;
  padding: 0 14px;
  border-color: rgba(15, 35, 80, 0.10);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.92);
  font-size: 13px;
  font-weight: 600;
  color: rgba(0, 0, 0, 0.62);
  display: flex;
  align-items: center;
  gap: 5px;
}

.goal-toolbar .goal-filter-btn:hover {
  border-color: rgba(22, 119, 255, 0.16);
  background: #fff;
  color: #1677ff;
  box-shadow: 0 6px 16px rgba(22, 119, 255, 0.08);
}

.goal-filter-dot {
  position: absolute;
  top: 7px;
  right: 7px;
  width: 7px;
  height: 7px;
  border: 2px solid #fff;
  border-radius: 50%;
  background: #ff4d4f;
}

.goal-filter-panel {
  position: relative;
  z-index: 2;
  margin: -4px 0 14px;
  padding: 14px;
  border: 1px solid rgba(0, 0, 0, 0.04);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.76);
  box-shadow: 0 10px 28px rgba(15, 35, 80, 0.04);
  backdrop-filter: blur(12px);
}

.goal-filter-inner {
  display: flex;
  align-items: flex-end;
  gap: 14px;
}

.goal-filter-item {
  min-width: 180px;
  flex: 1;
}

.goal-filter-label {
  display: block;
  margin-bottom: 6px;
  color: rgba(0, 0, 0, 0.46);
  font-size: 12px;
  font-weight: 650;
}

.goal-filter-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

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
  animation: goalFadeUp 0.4s cubic-bezier(0.23, 1, 0.32, 1) 0.1s both;
}

.goal-empty {
  padding: 88px 20px;
  text-align: center;
}

.goal-empty-icon {
  display: inline-flex;
  width: 58px;
  height: 58px;
  align-items: center;
  justify-content: center;
  margin-bottom: 14px;
  border-radius: 20px;
  background: rgba(22, 119, 255, 0.08);
  color: #1677ff;
  font-size: 28px;
}

.goal-empty-title {
  margin: 0;
  color: rgba(0, 0, 0, 0.78);
  font-size: 17px;
  font-weight: 750;
}

.goal-empty-desc {
  margin: 8px 0 18px;
  color: rgba(0, 0, 0, 0.42);
  font-size: 13px;
}

.goal-list {
  display: flex;
  flex-direction: column;
}

.goal-item {
  position: relative;
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 18px 22px;
  overflow: hidden;
  border-bottom: 1px solid rgba(15, 35, 80, 0.055);
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.88), rgba(248, 251, 255, 0.72));
  transition: background-color 0.22s ease, transform 0.22s cubic-bezier(0.34, 1.56, 0.64, 1), box-shadow 0.22s ease;
}

.goal-item:last-child {
  border-bottom: none;
}

.goal-item:hover {
  z-index: 1;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.98), rgba(247, 251, 255, 0.96));
  box-shadow: 0 12px 28px rgba(15, 35, 80, 0.065);
  transform: translateY(-2px);
}

.goal-item-done {
  opacity: 0.62;
  background: rgba(248, 250, 252, 0.88);
}

.goal-item-done:hover {
  background: rgba(248, 250, 252, 0.98);
}

.goal-item-high::before {
  content: "";
  position: absolute;
  top: 16px;
  bottom: 16px;
  left: 0;
  width: 3px;
  border-radius: 999px;
  background: #ff4d4f;
}

.goal-progress-ring {
  display: flex;
  width: 56px;
  height: 56px;
  flex-shrink: 0;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(82, 196, 26, 0.12);
  border-radius: 18px;
  background: rgba(82, 196, 26, 0.06);
  color: #36b37e;
  cursor: pointer;
  transition: transform 0.2s cubic-bezier(0.34, 1.56, 0.64, 1), box-shadow 0.2s ease;
}

.goal-progress-number {
  font-size: 22px;
  font-weight: 800;
  line-height: 1;
  font-feature-settings: "tnum";
}

.goal-progress-unit {
  margin-top: 2px;
  font-size: 11px;
  font-weight: 700;
  opacity: 0.62;
}

.goal-item-done .goal-progress-ring {
  border-color: rgba(82, 196, 26, 0.16);
  background: rgba(82, 196, 26, 0.08);
  color: #389e0d;
}

.goal-item-body {
  min-width: 0;
  flex: 1;
  cursor: pointer;
}

.goal-item-title {
  display: -webkit-box;
  overflow: hidden;
  color: rgba(0, 0, 0, 0.85);
  font-size: 14px;
  font-weight: 650;
  line-height: 22px;
  word-break: break-word;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.goal-item-desc {
  display: -webkit-box;
  margin-top: 4px;
  overflow: hidden;
  color: rgba(0, 0, 0, 0.42);
  font-size: 12px;
  line-height: 20px;
  word-break: break-word;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.goal-item-track-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 10px;
}

.goal-item-track {
  flex: 1;
  height: 6px;
  overflow: hidden;
  border-radius: 999px;
  background: rgba(15, 35, 80, 0.06);
}

.goal-item-track-fill {
  min-width: 6px;
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, #36d399, #52c41a);
  transform: translateZ(0);
  transition: width 0.32s ease;
}

.goal-item-high .goal-item-track-fill {
  background: linear-gradient(90deg, #ff7875, #ffa39e);
}

.goal-item-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
  min-width: 0;
  margin-top: 9px;
}

.goal-tag {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  max-width: 180px;
  min-height: 22px;
  padding: 2px 9px;
  overflow: hidden;
  border: 1px solid transparent;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 650;
  line-height: 16px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.goal-tag-red {
  border-color: rgba(255, 77, 79, 0.10);
  background: rgba(255, 77, 79, 0.10);
  color: #cf1322;
}

.goal-tag-category {
  border-color: rgba(250, 173, 20, 0.12);
  background: rgba(250, 173, 20, 0.13);
  color: #ad6800;
}

.goal-tag-custom {
  border-color: rgba(114, 46, 209, 0.08);
  background: rgba(114, 46, 209, 0.08);
  color: #531dab;
}

.goal-tag-more {
  background: rgba(0, 0, 0, 0.045);
  color: rgba(0, 0, 0, 0.42);
}

.goal-tag-status {
  border-color: rgba(0, 0, 0, 0.035);
  background: rgba(0, 0, 0, 0.045);
  color: rgba(0, 0, 0, 0.52);
}

.goal-tag-status-completed {
  background: rgba(82, 196, 26, 0.10);
  color: #389e0d;
}

.goal-tag-status-in_progress {
  border-color: rgba(22, 119, 255, 0.10);
  background: rgba(22, 119, 255, 0.10);
  color: #0958d9;
}

.goal-tag-status-paused {
  border-color: rgba(250, 219, 20, 0.14);
  background: rgba(250, 219, 20, 0.18);
  color: #ad6800;
}

.goal-tag-status-cancelled {
  border-color: rgba(255, 77, 79, 0.10);
  background: rgba(255, 77, 79, 0.10);
  color: #cf1322;
}

.goal-item-right {
  display: flex;
  width: 140px;
  flex-shrink: 0;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.goal-item-date-block {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
}

.goal-item-date-label {
  color: rgba(0, 0, 0, 0.36);
  font-size: 11px;
  font-weight: 600;
}

.goal-item-date-value {
  display: inline-flex;
  align-items: center;
  color: rgba(0, 0, 0, 0.72);
  font-size: 13px;
  font-weight: 650;
  font-feature-settings: "tnum";
  white-space: nowrap;
}

.goal-item-actions {
  display: flex;
  align-items: center;
  gap: 2px;
}

.goal-detail-link {
  color: #1677ff;
  font-size: 13px;
  font-weight: 600;
  padding: 0 4px;
}

.goal-detail-link:hover {
  color: #4096ff;
}

.goal-more-btn {
  color: rgba(0, 0, 0, 0.35);
}

.goal-more-btn:hover {
  color: #1677ff;
}

.goal-sidebar {
  position: sticky;
  top: 80px;
  display: flex;
  width: 300px;
  flex-shrink: 0;
  flex-direction: column;
  gap: 16px;
}

.goal-sidebar-card {
  position: relative;
  overflow: hidden;
  padding: 20px;
  border: 1px solid rgba(15, 35, 80, 0.06);
  border-radius: 22px;
  background:
    radial-gradient(circle at 12% 0%, rgba(22, 119, 255, 0.08), transparent 32%),
    rgba(255, 255, 255, 0.92);
  box-shadow: 0 16px 38px rgba(15, 35, 80, 0.065), 0 1px 2px rgba(15, 35, 80, 0.035);
  backdrop-filter: blur(12px);
}

.goal-sidebar-card::after {
  content: "";
  position: absolute;
  right: -34px;
  bottom: -34px;
  width: 88px;
  height: 88px;
  border-radius: 50%;
  background: rgba(22, 119, 255, 0.055);
  pointer-events: none;
}

.goal-sidebar-title {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 7px;
  margin: 0 0 16px;
  color: rgba(0, 0, 0, 0.72);
  font-size: 13px;
  font-weight: 800;
  letter-spacing: 0.5px;
}

.goal-overview-row {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 2px 0;
  color: rgba(0, 0, 0, 0.5);
  font-size: 13px;
}

.goal-overview-row:last-child {
  border-bottom: none;
}

.goal-overview-row strong {
  min-width: 28px;
  color: rgba(0, 0, 0, 0.8);
  font-size: 20px;
  font-weight: 700;
  font-feature-settings: "tnum";
  text-align: right;
}

.goal-overview-blue strong {
  color: #1677ff;
}

.goal-overview-green strong {
  color: #52c41a;
}

.goal-overview-progress {
  position: relative;
  z-index: 1;
  height: 8px;
  margin-top: 12px;
  overflow: hidden;
  border-radius: 999px;
  background: rgba(15, 35, 80, 0.06);
}

.goal-overview-progress-fill {
  min-width: 8px;
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, #4d8df7, #7bcf9a);
  transition: width 0.5s ease;
}

.goal-shortcut-list {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.goal-shortcut-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border: 1px solid rgba(15, 35, 80, 0.055);
  border-radius: 13px;
  background: rgba(255, 255, 255, 0.82);
  color: rgba(0, 0, 0, 0.68);
  font-size: 13px;
  font-weight: 620;
  cursor: pointer;
  transition: all 0.22s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.goal-shortcut-item:hover {
  border-color: rgba(22, 119, 255, 0.18);
  background: rgba(22, 119, 255, 0.05);
  color: #1677ff;
  box-shadow: 0 8px 18px rgba(22, 119, 255, 0.06);
  transform: translateY(-2px);
}

.goal-shortcut-item:active {
  transform: scale(0.97);
}

.goal-shortcut-icon {
  font-size: 15px;
  color: rgba(0, 0, 0, 0.36);
}

.goal-shortcut-item:hover .goal-shortcut-icon {
  color: #1677ff;
}

.goal-shortcut-text {
  flex: 1;
}

.goal-shortcut-arrow {
  font-size: 10px;
  color: rgba(0, 0, 0, 0.18);
  transition: transform 0.18s ease;
}

.goal-shortcut-item:hover .goal-shortcut-arrow {
  color: #1677ff;
  transform: translateX(2px);
}

.goal-tip-card {
  border-color: rgba(250, 173, 20, 0.10);
  background:
    radial-gradient(circle at 100% 0%, rgba(250, 173, 20, 0.12), transparent 32%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.96), rgba(250, 252, 255, 0.90));
}

.goal-tip-list {
  position: relative;
  z-index: 1;
  margin: 0;
  padding: 0 0 0 18px;
  color: rgba(0, 0, 0, 0.4);
  font-size: 12px;
  line-height: 24px;
  list-style: none;
}

.goal-tip-list li {
  position: relative;
}

.goal-tip-list li::before {
  content: "•";
  position: absolute;
  left: -14px;
  color: #faad14;
  font-size: 14px;
}

/* 列表表头 */
.goal-list-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 22px;
  border-bottom: 1px solid rgba(15, 35, 80, 0.045);
}

.goal-list-count {
  color: rgba(0, 0, 0, 0.48);
  font-size: 13px;
}

.goal-list-count strong {
  color: rgba(0, 0, 0, 0.78);
  font-weight: 700;
  font-feature-settings: "tnum";
}

.goal-sort-btn {
  color: rgba(0, 0, 0, 0.52);
  font-size: 13px;
  font-weight: 600;
}

.goal-sort-btn:hover {
  color: #1677ff;
}

/* 进度条百分比文字 */
.goal-item-track-pct {
  flex-shrink: 0;
  min-width: 36px;
  color: rgba(0, 0, 0, 0.42);
  font-size: 12px;
  font-weight: 650;
  font-feature-settings: "tnum";
  text-align: right;
}

/* 分页信息 */
.goal-pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 22px;
  border-top: 1px solid rgba(15, 35, 80, 0.045);
}

.goal-pagination-info {
  color: rgba(0, 0, 0, 0.38);
  font-size: 12px;
  font-feature-settings: "tnum";
}

/* ===== 入场动画 ===== */
@keyframes goalFadeUp {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes goalSlideDown {
  from {
    opacity: 0;
    transform: translateY(-18px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes goalFadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes goalSidebarSlide {
  from {
    opacity: 0;
    transform: translateX(16px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes goalPulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.08); opacity: 0.8; }
}

@keyframes goalCountBounce {
  0% { transform: scale(0.7); }
  50% { transform: scale(1.15); }
  100% { transform: scale(1); }
}

/* Header 入场 */
.goal-anim-header {
  animation: goalSlideDown 0.55s cubic-bezier(0.23, 1, 0.32, 1) both;
}

/* 工具栏延迟入场 */
.goal-anim-toolbar {
  animation: goalFadeUp 0.5s cubic-bezier(0.23, 1, 0.32, 1) 0.1s both;
}

/* 侧栏卡片交错入场 */
.goal-anim-sidebar {
  animation: goalSidebarSlide 0.5s cubic-bezier(0.23, 1, 0.32, 1) both;
  animation-delay: calc(0.18s + var(--sidebar-order, 0) * 0.1s);
}

/* 列表卡片入场 - 通过 .goal-body 已有定义，这里追加动画 */

/* 空状态图标呼吸 */
.goal-empty-icon {
  animation: goalPulse 2.4s ease-in-out infinite;
}

/* 进度条仅保留稳定的宽度过渡，避免 Tab 切换瞬间触发宽度重播造成渲染伪影 */
.goal-overview-progress-fill {
  transition: width 0.36s ease;
}

/* 筛选面板展开/收起 */
.goal-filter-expand-enter-active {
  transition: opacity 0.3s cubic-bezier(0.23, 1, 0.32, 1),
              transform 0.3s cubic-bezier(0.23, 1, 0.32, 1);
  overflow: hidden;
}

.goal-filter-expand-leave-active {
  transition: opacity 0.2s ease-in,
              transform 0.2s ease-in;
  overflow: hidden;
}

.goal-filter-expand-enter-from {
  opacity: 0;
  transform: translateY(-10px) scaleY(0.96);
}

.goal-filter-expand-leave-to {
  opacity: 0;
  transform: translateY(-6px) scaleY(0.98);
}

/* Tab 点击弹性效果 */
.goal-tab {
  transition: all 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.goal-tab:active {
  transform: scale(0.93);
}

.goal-tab-active {
  transition: all 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
}

/* 标签 hover 微上浮 */
.goal-tag {
  transition: all 0.15s ease;
}

.goal-tag:hover {
  transform: translateY(-1px);
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.06);
}

/* 进度块 hover 弹性 */
.goal-progress-ring:hover {
  transform: scale(1.08);
  box-shadow: 0 8px 20px rgba(82, 196, 26, 0.15);
}

.goal-progress-ring:active {
  transform: scale(0.96);
}

/* 新建按钮 hover 发光 */
.goal-header-top :deep(.ant-btn-primary) {
  transition: all 0.2s ease;
}

.goal-header-top :deep(.ant-btn-primary:hover) {
  box-shadow: 0 14px 32px rgba(22, 119, 255, 0.32);
  transform: translateY(-1px);
}

/* 快捷入口箭头动画已在 .goal-shortcut-item:hover 中定义 */

@media (max-width: 1200px) {
  .shanzhu-goal-page {
    padding: 28px 28px;
  }

  .goal-sidebar {
    width: 260px;
  }
}

@media (max-width: 900px) {
  .shanzhu-goal-page {
    padding: 20px 16px;
  }

  .goal-header-top,
  .goal-toolbar,
  .goal-filter-inner {
    flex-direction: column;
    align-items: stretch;
  }

  .goal-header-top :deep(.ant-space) {
    justify-content: flex-start;
  }

  .goal-search {
    width: 100%;
  }

  .goal-content {
    flex-direction: column;
  }

  .goal-sidebar {
    position: static;
    display: flex;
    width: 100%;
    flex-direction: row;
    flex-wrap: wrap;
  }

  .goal-sidebar-card {
    flex: 1;
    min-width: 220px;
  }
}

@media (max-width: 640px) {
  .goal-header {
    padding: 22px 20px;
    border-radius: 22px;
  }

  .goal-page-title {
    font-size: 26px;
  }

  .goal-header-top :deep(.ant-space) {
    width: 100%;
    flex-direction: column;
    align-items: stretch;
  }

  .goal-header-top :deep(.ant-space-item),
  .goal-header-top :deep(.ant-btn) {
    width: 100%;
  }

  .goal-tabs {
    padding-bottom: 2px;
  }

  .goal-item {
    flex-wrap: wrap;
    gap: 12px;
    padding: 16px 18px;
  }

  .goal-item-body {
    width: calc(100% - 62px);
    flex-basis: calc(100% - 62px);
  }

  .goal-item-right {
    width: 100%;
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
    padding-left: 62px;
  }

  .goal-item-actions {
    opacity: 1;
  }

  .goal-tag {
    max-width: 100%;
  }

  .goal-sidebar {
    flex-direction: column;
  }
}
</style>
