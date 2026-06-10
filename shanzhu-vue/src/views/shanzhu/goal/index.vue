<template>
  <div class="shanzhu-goal-page">
    <!-- 顶部：恢复任务中心同款高级横幅 -->
    <div class="goal-header">
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
          <span v-if="activeFilterCount > 0" class="goal-filter-dot"></span>
        </a-button>
      </div>
    </div>

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

    <!-- 主体：目标列表 + 侧边栏 -->
    <div class="goal-content">
      <div class="goal-main">
        <div class="goal-body">
          <a-spin :spinning="tableLoading">
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
                <div class="goal-progress-ring" @click="openDetailPage(goal.id)">
                  <div class="goal-progress-number">{{ goal.progress || 0 }}</div>
                  <div class="goal-progress-unit">%</div>
                </div>

                <div class="goal-item-body" @click="openDetailPage(goal.id)">
                  <div class="goal-item-title">{{ goal.title }}</div>
                  <div class="goal-item-desc">{{ goal.description || '暂无目标描述' }}</div>
                  <div class="goal-item-track">
                    <div class="goal-item-track-fill" :style="{ width: (goal.progress || 0) + '%' }"></div>
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
                  <span v-if="goal.deadline" class="goal-item-date">
                    <ClockCircleOutlined/> {{ goal.deadline }}
                  </span>
                  <div class="goal-item-actions" @click.stop>
                    <a-button type="text" size="small" @click="openDetailPage(goal.id)">详情</a-button>
                    <a-dropdown>
                      <a-button type="text" size="small">
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
        <div class="goal-sidebar-card goal-overview-card">
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

        <div class="goal-sidebar-card">
          <div class="goal-sidebar-title">
            <AimOutlined/>
            快捷入口
          </div>
          <div class="goal-sidebar-actions">
            <a-button block @click="router.push('/shanzhu/task')">任务中心</a-button>
            <a-button block @click="router.push('/shanzhu/today-work')">今日工作台</a-button>
            <a-button block @click="router.push('/shanzhu/review')">复盘中心</a-button>
          </div>
        </div>

        <div class="goal-sidebar-card goal-tip-card">
          <div class="goal-sidebar-title">
            <FireOutlined/>
            提醒
          </div>
          <ul class="goal-tip-list">
            <li>目标保持具体、可衡量。</li>
            <li>每个目标至少拆解一个行动项。</li>
            <li>每周复盘一次真实进展。</li>
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
/* ===== Goal page refined layout ===== */
.shanzhu-goal-page {
  position: relative;
  max-width: 1280px;
  min-height: calc(100vh - 120px);
  margin: 0 auto;
  padding: 28px 36px 56px;
  overflow-x: hidden;
  border-radius: 28px;
  background:
    radial-gradient(circle at 18% 0%, rgba(22, 119, 255, 0.05), transparent 28%),
    linear-gradient(180deg, #f7f9fc 0%, #ffffff 42%);
  isolation: isolate;
}

.shanzhu-goal-page::before {
  content: "";
  position: absolute;
  inset: 0;
  z-index: -1;
  border-radius: 28px;
  background: #ffffff;
  pointer-events: none;
}

.goal-header {
  position: relative;
  z-index: 2;
  margin-bottom: 16px;
  padding: 24px 28px;
  overflow: hidden;
  border: 1px solid rgba(22, 119, 255, 0.08);
  border-radius: 24px;
  background:
    radial-gradient(circle at 14% 0%, rgba(22, 119, 255, 0.10), transparent 30%),
    radial-gradient(circle at 96% 18%, rgba(114, 46, 209, 0.08), transparent 26%),
    linear-gradient(135deg, #ffffff, #f7fbff);
  box-shadow: 0 12px 32px rgba(15, 35, 80, 0.055);
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
  font-weight: 650;
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
  border: 1px solid rgba(15, 35, 80, 0.055);
  border-radius: 16px;
  background: #ffffff;
  box-shadow: 0 8px 22px rgba(15, 35, 80, 0.035);
}

.goal-tabs {
  display: flex;
  gap: 6px;
  overflow-x: auto;
}

.goal-tab {
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

.goal-toolbar .goal-filter-btn {
  position: relative;
  width: 34px;
  height: 34px;
  border-color: transparent;
  border-radius: 12px;
  background: rgba(247, 248, 250, 0.92);
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
  border: 1px solid rgba(15, 35, 80, 0.055);
  border-radius: 16px;
  background: #ffffff;
  box-shadow: 0 8px 22px rgba(15, 35, 80, 0.035);
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
  gap: 24px;
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
  border-radius: 20px;
  background: #ffffff;
  box-shadow: 0 12px 30px rgba(15, 35, 80, 0.05), 0 1px 2px rgba(15, 35, 80, 0.035);
}

.goal-empty {
  padding: 78px 20px;
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
  background: #ffffff;
  transition: background-color 0.18s ease, transform 0.18s ease, box-shadow 0.18s ease;
  animation: goalFadeUp 0.30s ease both;
  animation-delay: calc(var(--anim-order) * 0.025s);
}

.goal-item:last-child {
  border-bottom: none;
}

.goal-item:hover {
  z-index: 1;
  background: #f8fbff;
  box-shadow: 0 10px 24px rgba(15, 35, 80, 0.05);
  transform: translateY(-1px);
}

.goal-item-done {
  opacity: 0.68;
  background: rgba(248, 250, 252, 0.88);
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
  width: 48px;
  height: 48px;
  flex-shrink: 0;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(22, 119, 255, 0.10);
  border-radius: 16px;
  background: rgba(22, 119, 255, 0.06);
  color: #1677ff;
  cursor: pointer;
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.55);
}

.goal-progress-number {
  font-size: 16px;
  font-weight: 850;
  line-height: 1;
  font-feature-settings: "tnum";
}

.goal-progress-unit {
  margin-top: 2px;
  font-size: 10px;
  font-weight: 700;
  opacity: 0.68;
}

.goal-item-done .goal-progress-ring {
  border-color: rgba(82, 196, 26, 0.14);
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
  margin-top: 2px;
  overflow: hidden;
  color: rgba(0, 0, 0, 0.42);
  font-size: 12px;
  line-height: 19px;
  word-break: break-word;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
}

.goal-item-track {
  height: 6px;
  margin-top: 10px;
  overflow: hidden;
  border-radius: 999px;
  background: rgba(15, 35, 80, 0.06);
}

.goal-item-track-fill {
  min-width: 6px;
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, #1677ff, #52c41a);
  transition: width 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.goal-item-high .goal-item-track-fill {
  background: linear-gradient(90deg, #ff4d4f, #ff7875);
}

.goal-item-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
  min-width: 0;
  margin-top: 10px;
}

.goal-tag {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  max-width: 150px;
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
  background: rgba(22, 119, 255, 0.09);
  color: #1677ff;
}

.goal-tag-status-paused {
  background: rgba(250, 173, 20, 0.12);
  color: #ad6800;
}

.goal-tag-status-cancelled {
  background: rgba(255, 77, 79, 0.08);
  color: #cf1322;
}

.goal-item-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12px;
  width: 104px;
  flex-shrink: 0;
}

.goal-item-date {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: rgba(0, 0, 0, 0.36);
  font-size: 12px;
  font-weight: 650;
  white-space: nowrap;
}

.goal-item-actions {
  display: flex;
  align-items: center;
  gap: 2px;
  opacity: 0;
  transition: opacity 0.18s ease;
}

.goal-item:hover .goal-item-actions {
  opacity: 1;
}

.goal-sidebar {
  position: sticky;
  top: 80px;
  display: flex;
  width: 286px;
  flex-shrink: 0;
  flex-direction: column;
  gap: 16px;
}

.goal-sidebar-card {
  position: relative;
  overflow: hidden;
  padding: 18px;
  border: 1px solid rgba(15, 35, 80, 0.06);
  border-radius: 20px;
  background:
    radial-gradient(circle at 12% 0%, rgba(22, 119, 255, 0.055), transparent 30%),
    #ffffff;
  box-shadow: 0 12px 28px rgba(15, 35, 80, 0.05), 0 1px 2px rgba(15, 35, 80, 0.035);
}

.goal-sidebar-card::after {
  content: "";
  position: absolute;
  right: -36px;
  bottom: -36px;
  width: 92px;
  height: 92px;
  border-radius: 50%;
  background: rgba(22, 119, 255, 0.045);
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
  padding: 9px 0;
  color: rgba(0, 0, 0, 0.52);
  font-size: 13px;
  border-bottom: 1px solid rgba(15, 35, 80, 0.045);
}

.goal-overview-row:last-child {
  border-bottom: none;
}

.goal-overview-row strong {
  color: rgba(0, 0, 0, 0.82);
  font-size: 18px;
  font-weight: 850;
  font-feature-settings: "tnum";
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
  background: linear-gradient(90deg, #1677ff, #52c41a);
  transition: width 0.5s ease;
}

.goal-sidebar-actions {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  gap: 9px;
}

.goal-sidebar-actions :deep(.ant-btn) {
  height: 36px;
  border-color: rgba(15, 35, 80, 0.07);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.72);
  color: rgba(0, 0, 0, 0.58);
  font-weight: 650;
}

.goal-sidebar-actions :deep(.ant-btn:hover) {
  border-color: rgba(22, 119, 255, 0.18);
  color: #1677ff;
  background: #fff;
}

.goal-tip-card {
  border-color: rgba(250, 173, 20, 0.10);
  background:
    radial-gradient(circle at 100% 0%, rgba(250, 173, 20, 0.08), transparent 30%),
    #ffffff;
}

.goal-tip-list {
  position: relative;
  z-index: 1;
  margin: 0;
  padding-left: 18px;
  color: rgba(0, 0, 0, 0.48);
  font-size: 12px;
  line-height: 1.8;
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

@media (max-width: 1180px) {
  .shanzhu-goal-page {
    max-width: 980px;
  }

  .goal-content {
    flex-direction: column;
  }

  .goal-sidebar {
    position: static;
    display: grid;
    width: 100%;
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .shanzhu-goal-page {
    padding: 16px 12px 36px;
  }

  .goal-header-top,
  .goal-toolbar,
  .goal-filter-inner {
    flex-direction: column;
    align-items: stretch;
  }

  .goal-search {
    width: 100%;
  }

  .goal-sidebar {
    grid-template-columns: 1fr;
  }

  .goal-item {
    gap: 12px;
    padding: 16px;
  }

  .goal-item-right {
    display: none;
  }
}
</style>
