<template>
  <div class="shanzhu-task-page">
    <!-- 顶部：标题 + 操作按钮 -->
    <div class="task-header">
      <div class="task-header-top">
        <div>
          <div class="task-eyebrow">Task Center</div>
          <h2 class="task-page-title">任务中心</h2>
          <p class="task-page-desc">聚焦目标拆解后的行动项，优先处理今天和逾期任务。</p>
        </div>
        <a-space>
          <a-button class="task-secondary-btn" @click="router.push('/shanzhu/goal')">
            🎯 目标管理
          </a-button>
          <a-button type="primary" shape="round" size="large" @click="openCreateTaskModal">
            <template #icon><PlusOutlined/></template>
            新建任务
          </a-button>
        </a-space>
      </div>
    </div>

    <!-- Tab 筛选 + 搜索 -->
    <div class="task-toolbar">
      <div class="task-tabs">
        <button
            v-for="tab in statusTabs"
            :key="tab.value || 'all'"
            class="task-tab"
            :class="{ 'task-tab-active': activeTab === tab.value }"
            @click="handleTabChange(tab.value)"
        >
          {{ tab.label }}
          <span class="task-tab-count">{{ tab.count }}</span>
        </button>
      </div>
      <div class="task-search">
        <a-input
            v-model:value="taskQuery.keyword"
            placeholder="搜索任务..."
            allow-clear
            @press-enter="handleSearch"
            @change="handleSearchChange"
        >
          <template #prefix>
            <SearchOutlined style="color: rgba(0,0,0,0.25);"/>
          </template>
        </a-input>
      </div>
    </div>

    <!-- 主体：任务列表 + 侧边栏 -->
    <div class="task-content">
      <div class="task-main">
        <div class="task-body">
          <a-spin :spinning="tableLoading">
            <div v-if="!tableLoading && taskList.length === 0" class="task-empty">
              <a-empty description="暂无任务">
                <a-button type="primary" shape="round" @click="openCreateTaskModal">
                  <template #icon><PlusOutlined/></template>
                  添加第一个任务
                </a-button>
              </a-empty>
            </div>
            <TransitionGroup v-else-if="taskList.length > 0" name="task-list-anim" tag="div" class="task-list">
              <div
                  v-for="(task, index) in taskList"
                  :key="task.id"
                  class="task-item"
                  :class="{
                    'task-item-done': task.status === 'completed',
                    'task-item-high': task.priority === 3 && task.status !== 'completed'
                  }"
                  :style="{ '--anim-order': index }"
              >
                <div class="task-item-check" @click="task.status !== 'completed' ? handleQuickComplete(task) : null">
                  <div class="task-checkbox" :class="{ 'task-checkbox-checked': task.status === 'completed' }">
                    <CheckOutlined v-if="task.status === 'completed'" style="font-size: 11px; color: #fff;"/>
                  </div>
                </div>

                <div class="task-item-body" @click="openEditTaskModal(task)">
                  <div class="task-item-title" :class="{ 'task-item-title-done': task.status === 'completed' }">
                    {{ task.title }}
                  </div>
                  <div v-if="task.description" class="task-item-desc">{{ task.description }}</div>
                  <div class="task-item-tags">
                    <span v-if="task.priority === 3" class="task-tag task-tag-red">高优</span>
                    <span v-if="task.priority === 1" class="task-tag task-tag-blue">低优</span>
                    <span class="task-tag task-tag-status" :class="'task-tag-status-' + task.status">
                      {{ getTaskStatusOption(task.status).label }}
                    </span>
                    <span v-if="task.plannedDate" class="task-tag task-tag-date">
                      <CalendarOutlined style="margin-right: 2px;"/> {{ task.plannedDate }}
                    </span>
                    <span v-if="task.goalTitle" class="task-tag task-tag-goal">
                      🎯 {{ task.goalTitle }}
                    </span>
                    <span v-if="task.categoryName" class="task-tag task-tag-category">
                      {{ task.categoryName }}
                    </span>
                    <span v-for="tag in task.tags" :key="tag.id" class="task-tag task-tag-custom">
                      {{ tag.name }}
                    </span>
                  </div>
                </div>

                <div class="task-item-right">
                  <div v-if="task.estimatedMinutes" class="task-item-time">
                    {{ task.actualMinutes || 0 }}/{{ task.estimatedMinutes }}分钟
                  </div>
                  <div class="task-item-actions">
                    <a-tooltip v-if="task.status !== 'completed'" title="标记完成">
                      <a-button type="text" size="small" class="task-action-btn" @click.stop="handleQuickComplete(task)">
                        <template #icon><CheckOutlined/></template>
                      </a-button>
                    </a-tooltip>
                    <a-dropdown>
                      <a-button type="text" size="small" class="task-action-btn" @click.stop>
                        <template #icon><MoreOutlined/></template>
                      </a-button>
                      <template #overlay>
                        <a-menu>
                          <a-menu-item v-if="task.status !== 'completed'" @click="handleQuickComplete(task)">
                            <CheckOutlined/> 标记完成
                          </a-menu-item>
                          <a-menu-item v-if="task.goalId" @click="openGoalDetail(task.goalId)">
                            🎯 查看目标
                          </a-menu-item>
                          <a-menu-item @click="openEditTaskModal(task)">
                            <EditOutlined/> 编辑
                          </a-menu-item>
                          <a-menu-divider/>
                          <a-menu-item danger @click="confirmDeleteTask(task)">
                            <DeleteOutlined/> 删除
                          </a-menu-item>
                        </a-menu>
                      </template>
                    </a-dropdown>
                  </div>
                </div>
              </div>
            </TransitionGroup>

            <div v-if="taskPagination.total > taskPagination.pageSize" class="task-pagination">
              <a-pagination
                  v-model:current="taskPagination.current"
                  v-model:pageSize="taskPagination.pageSize"
                  :total="taskPagination.total"
                  :page-size-options="['10', '20', '30', '50']"
                  size="small"
                  show-size-changer
                  @change="handlePageChange"
              />
            </div>
          </a-spin>
        </div>
      </div>

      <!-- 侧边栏 -->
      <div class="task-sidebar">
        <div class="sidebar-card sidebar-anim" style="animation-delay: 0.1s;">
          <h4 class="sidebar-title">📊 概览</h4>
          <div class="sidebar-stats">
            <div class="stat-row">
              <span class="stat-label">今日任务</span>
              <span class="stat-value stat-value-blue">{{ taskStats.today }}</span>
            </div>
            <div class="stat-row" :class="{ 'stat-row-warn': taskStats.overdue > 0 }">
              <span class="stat-label">逾期任务</span>
              <span class="stat-value" :class="{ 'stat-value-red': taskStats.overdue > 0 }">{{ taskStats.overdue }}</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-row">
              <span class="stat-label">进行中</span>
              <span class="stat-value">{{ taskStats.inProgress }}</span>
            </div>
            <div class="stat-row">
              <span class="stat-label">未开始</span>
              <span class="stat-value">{{ taskStats.notStarted }}</span>
            </div>
            <div class="stat-row">
              <span class="stat-label">已完成</span>
              <span class="stat-value stat-value-green">{{ taskStats.completed }}</span>
            </div>
          </div>
        </div>

        <div class="sidebar-card sidebar-anim" style="animation-delay: 0.2s;">
          <h4 class="sidebar-title">⚡ 快捷操作</h4>
          <div class="sidebar-actions">
            <a-button block @click="router.push('/shanzhu/today-work')">
              <template #icon><DesktopOutlined/></template>
              今日工作台
            </a-button>
            <a-button block @click="router.push('/shanzhu/todo')">
              <template #icon><InboxOutlined/></template>
              收集箱
            </a-button>
          </div>
        </div>

        <div class="sidebar-card sidebar-card-tip sidebar-anim" style="animation-delay: 0.3s;">
          <h4 class="sidebar-title">💡 使用提示</h4>
          <ul class="sidebar-tips">
            <li>任务从目标拆解而来</li>
            <li>高优任务优先处理</li>
            <li>预估时间帮助合理安排</li>
            <li>完成后记得标记</li>
          </ul>
        </div>
      </div>
    </div>

    <!-- 新建/编辑任务弹窗 -->
    <a-modal
        v-model:open="taskModal.open"
        :title="taskModal.title"
        :confirm-loading="taskModal.saveLoading"
        width="600px"
        ok-text="保存"
        cancel-text="取消"
        class="task-modal"
        @ok="handleSaveTask"
    >
      <a-form ref="taskFormRef" :colon="false" :model="taskForm" :rules="taskRules" layout="vertical" style="margin-top: 16px;">
        <a-form-item label="所属目标" name="goalId">
          <a-select
              v-model:value="taskForm.goalId"
              placeholder="选择关联的目标"
              show-search
              :filter-option="filterGoalOption"
          >
            <a-select-option v-for="goal in goalOptions" :key="goal.id" :value="goal.id">
              {{ goal.title }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="任务标题" name="title">
          <a-input v-model:value="taskForm.title" placeholder="这个任务是..." :maxlength="120" show-count allow-clear/>
        </a-form-item>
        <a-form-item label="状态">
          <a-radio-group v-model:value="taskForm.status">
            <a-radio v-for="item in taskStatusOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-radio>
          </a-radio-group>
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="优先级">
              <a-select v-model:value="taskForm.priority">
                <a-select-option v-for="item in taskPriorityOptions" :key="item.value" :value="item.value">
                  {{ item.label }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="预计(分钟)">
              <a-input-number v-model:value="taskForm.estimatedMinutes" :min="0" :max="99999" style="width: 100%"/>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="实际(分钟)">
              <a-input-number v-model:value="taskForm.actualMinutes" :min="0" :max="99999" style="width: 100%"/>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="计划日期">
              <a-date-picker v-model:value="taskForm.plannedDate" value-format="YYYY-MM-DD" style="width: 100%" placeholder="什么时候开始？"/>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="截止时间">
              <a-date-picker
                  v-model:value="taskForm.deadline"
                  show-time
                  value-format="YYYY-MM-DDTHH:mm:ss"
                  style="width: 100%"
                  placeholder="什么时候完成？"
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="分类">
          <shanzhu-category-select v-model:value="taskForm.categoryId"/>
        </a-form-item>
        <a-form-item label="标签">
          <shanzhu-tag-select v-model:value="taskForm.tagIds" tag-type="task" placeholder="选择标签"/>
        </a-form-item>
        <a-form-item label="任务说明">
          <a-textarea v-model:value="taskForm.description" placeholder="描述一下这个任务（可选）" :rows="3" :maxlength="500" show-count allow-clear/>
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
  CalendarOutlined,
  CheckOutlined,
  DeleteOutlined,
  DesktopOutlined,
  EditOutlined,
  InboxOutlined,
  MoreOutlined,
  PlusOutlined,
  SearchOutlined
} from "@ant-design/icons-vue";
import ShanzhuCategorySelect from "@/components/shanzhu-category-select/index.vue";
import ShanzhuTagSelect from "@/components/shanzhu-tag-select/index.vue";
import {queryGoalPage} from "@/api/shanzhu/goal/Goal.ts";
import type {GoalStatusOption, ShanzhuGoalVO} from "@/api/shanzhu/goal/type/Goal.ts";
import {deleteTask, queryTaskList, queryTaskPage, saveTask, updateTaskStatus} from "@/api/shanzhu/task/Task.ts";
import type {ShanzhuTask, ShanzhuTaskQuery, ShanzhuTaskVO} from "@/api/shanzhu/task/type/Task.ts";
import type {BaseModalActiveType} from "@/api/global/Type.ts";

interface TaskPriorityOption {
  label: string;
  value: number;
  color: string;
}

const router = useRouter();

const taskStatusOptions: GoalStatusOption[] = [
  {label: "未开始", value: "not_started", color: "default"},
  {label: "进行中", value: "in_progress", color: "processing"},
  {label: "已完成", value: "completed", color: "success"},
  {label: "已暂停", value: "paused", color: "warning"},
  {label: "已取消", value: "cancelled", color: "error"}
];

const taskPriorityOptions: TaskPriorityOption[] = [
  {label: "低", value: 1, color: "blue"},
  {label: "中", value: 2, color: "orange"},
  {label: "高", value: 3, color: "red"}
];

const defaultTaskQuery = (): ShanzhuTaskQuery => ({
  pageNum: 1,
  pageSize: 10
});

const defaultTaskForm = (): ShanzhuTask => ({
  status: "not_started",
  priority: 2,
  estimatedMinutes: 0,
  actualMinutes: 0,
  sortOrder: 0,
  tagIds: []
});

const taskQuery = ref<ShanzhuTaskQuery>(defaultTaskQuery());
const taskList = ref<ShanzhuTaskVO[]>([]);
const taskPagination = reactive({current: 1, pageSize: 10, total: 0});
const taskStats = reactive({total: 0, notStarted: 0, inProgress: 0, completed: 0, paused: 0, cancelled: 0, today: 0, overdue: 0});
const goalOptions = ref<ShanzhuGoalVO[]>([]);
const tableLoading = ref(false);
const taskFormRef = ref<FormInstance>();
const taskForm = ref<ShanzhuTask>(defaultTaskForm());
const taskModal = reactive<BaseModalActiveType>({open: false, saveLoading: false, title: "新建任务"});

const taskRules: Record<string, Rule[]> = {
  goalId: [{required: true, message: "请选择目标", trigger: "change"}],
  title: [{required: true, message: "请输入任务标题", trigger: "blur"}]
};

const activeTab = ref<string>("all");

const statusTabs = computed(() => [
  {label: "全部", value: "all", count: taskStats.total},
  {label: "今日", value: "today", count: taskStats.today},
  {label: "逾期", value: "overdue", count: taskStats.overdue},
  {label: "进行中", value: "in_progress", count: taskStats.inProgress},
  {label: "未开始", value: "not_started", count: taskStats.notStarted},
  {label: "已完成", value: "completed", count: taskStats.completed}
]);

const getTaskStatusOption = (status?: string) => {
  return taskStatusOptions.find(item => item.value === status) || taskStatusOptions[0];
};

const filterGoalOption = (input: string, option?: { children?: string }) => {
  const keyword = input.toLowerCase();
  return String(option?.children || "").toLowerCase().includes(keyword);
};

const loadGoalOptions = async () => {
  try {
    const response = await queryGoalPage({pageNum: 1, pageSize: 50});
    if (response.code === 200) {
      goalOptions.value = response.data.records || [];
    }
  } catch {
    // ignore
  }
};

const loadTaskStats = async () => {
  try {
    const [allRes, todayRes, overdueRes] = await Promise.all([
      queryTaskList({}),
      queryTaskList({queryType: "today"}),
      queryTaskList({queryType: "overdue"})
    ]);
    if (allRes.code === 200) {
      const allTasks = allRes.data || [];
      taskStats.total = allTasks.length;
      taskStats.notStarted = allTasks.filter(item => item.status === "not_started").length;
      taskStats.inProgress = allTasks.filter(item => item.status === "in_progress").length;
      taskStats.completed = allTasks.filter(item => item.status === "completed").length;
      taskStats.paused = allTasks.filter(item => item.status === "paused").length;
      taskStats.cancelled = allTasks.filter(item => item.status === "cancelled").length;
    }
    if (todayRes.code === 200) {
      taskStats.today = (todayRes.data || []).length;
    }
    if (overdueRes.code === 200) {
      taskStats.overdue = (overdueRes.data || []).length;
    }
  } catch {
    // ignore
  }
};

const loadTaskList = async (pageNum = taskPagination.current, pageSize = taskPagination.pageSize) => {
  tableLoading.value = true;
  try {
    const response = await queryTaskPage({...taskQuery.value, pageNum, pageSize});
    if (response.code === 200) {
      taskList.value = response.data.records || [];
      taskPagination.current = response.data.current || pageNum;
      taskPagination.pageSize = response.data.size || pageSize;
      taskPagination.total = response.data.total || 0;
    } else {
      message.error(response.msg || "加载失败");
    }
  } finally {
    tableLoading.value = false;
  }
};

const handleSearch = () => {
  loadTaskList(1, taskPagination.pageSize);
};

const handlePageChange = (pageNum: number, pageSize: number) => {
  loadTaskList(pageNum, pageSize);
};

const handleTabChange = (tabValue: string) => {
  activeTab.value = tabValue;
  // 重置筛选条件
  taskQuery.value.status = undefined;
  taskQuery.value.queryType = undefined;

  if (tabValue === "today" || tabValue === "overdue") {
    taskQuery.value.queryType = tabValue;
  } else if (tabValue !== "all") {
    taskQuery.value.status = tabValue;
  }
  loadTaskList(1, taskPagination.pageSize);
};

let searchTimer: ReturnType<typeof setTimeout> | null = null;
const handleSearchChange = () => {
  if (searchTimer) clearTimeout(searchTimer);
  searchTimer = setTimeout(() => {
    loadTaskList(1, taskPagination.pageSize);
  }, 400);
};

const openCreateTaskModal = () => {
  taskForm.value = defaultTaskForm();
  taskModal.title = "新建任务";
  taskModal.open = true;
};

const openEditTaskModal = (task: ShanzhuTaskVO) => {
  taskForm.value = {
    ...task,
    tagIds: task.tags?.map(tag => tag.id || "").filter(Boolean) || []
  };
  taskModal.title = "编辑任务";
  taskModal.open = true;
};

const handleSaveTask = async () => {
  await taskFormRef.value?.validate();
  taskModal.saveLoading = true;
  try {
    const response = await saveTask(taskForm.value);
    if (response.code === 200) {
      message.success("保存成功");
      taskModal.open = false;
      await Promise.all([loadTaskList(), loadTaskStats()]);
    } else {
      message.error(response.msg || "保存失败");
    }
  } finally {
    taskModal.saveLoading = false;
  }
};

const handleQuickComplete = async (task: ShanzhuTaskVO) => {
  if (!task.id) return;
  const response = await updateTaskStatus({id: task.id, status: "completed"});
  if (response.code === 200) {
    message.success("任务已完成 🎉");
    await Promise.all([loadTaskList(), loadTaskStats()]);
  } else {
    message.error(response.msg || "状态更新失败");
  }
};

const confirmDeleteTask = (task: ShanzhuTaskVO) => {
  if (!task.id) return;
  Modal.confirm({
    title: "确认删除任务？",
    content: `删除后，任务「${task.title || '-'}」将不再展示。`,
    okText: "确认删除",
    cancelText: "取消",
    okType: "danger",
    onOk: async () => {
      const response = await deleteTask(task.id || "");
      if (response.code === 200) {
        message.success("删除成功");
        await Promise.all([loadTaskList(), loadTaskStats()]);
      } else {
        message.error(response.msg || "删除失败");
      }
    }
  });
};

const openGoalDetail = (goalId?: string) => {
  if (!goalId) {
    message.warning("任务未关联目标");
    return;
  }
  router.push(`/shanzhu/goal/detail/${goalId}`);
};

onMounted(async () => {
  await loadGoalOptions();
  await Promise.all([loadTaskList(), loadTaskStats()]);
});
</script>

<style scoped>
.shanzhu-task-page {
  max-width: 1360px;
  margin: 0 auto;
  padding: 36px 48px 56px;
  min-height: calc(100vh - 120px);
  overflow-x: hidden;
}

/* Header */
.task-header {
  position: relative;
  z-index: 2;
  padding: 26px 28px;
  margin-bottom: 18px;
  overflow: hidden;
  border-radius: 26px;
  background:
    radial-gradient(circle at 16% 0%, rgba(22, 119, 255, 0.13), transparent 34%),
    radial-gradient(circle at 96% 18%, rgba(114, 46, 209, 0.10), transparent 28%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.98), rgba(247, 250, 255, 0.94));
  border: 1px solid rgba(22, 119, 255, 0.08);
  box-shadow: 0 18px 45px rgba(15, 35, 80, 0.06);
}

.task-header-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 24px;
}

.task-eyebrow {
  margin-bottom: 8px;
  color: #1677ff;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.8px;
  text-transform: uppercase;
}

.task-page-title {
  font-size: 30px;
  font-weight: 850;
  color: rgba(0, 0, 0, 0.88);
  margin: 0;
  letter-spacing: -0.7px;
  line-height: 1.2;
}

.task-page-desc {
  margin: 8px 0 0;
  color: rgba(0, 0, 0, 0.48);
  font-size: 14px;
}

.task-secondary-btn {
  height: 38px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.74);
  border-color: rgba(15, 35, 80, 0.08);
  color: rgba(0, 0, 0, 0.68);
  font-weight: 600;
}

.task-header-top :deep(.ant-btn-primary) {
  min-width: 122px;
  height: 42px;
  box-shadow: 0 10px 24px rgba(22, 119, 255, 0.22);
}

/* Toolbar */
.task-toolbar {
  position: relative;
  z-index: 2;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
  gap: 16px;
  padding: 8px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.76);
  border: 1px solid rgba(0, 0, 0, 0.04);
  box-shadow: 0 10px 28px rgba(15, 35, 80, 0.04);
  backdrop-filter: blur(12px);
}

.task-tabs {
  display: flex;
  gap: 6px;
  overflow-x: auto;
}

.task-tab {
  min-height: 34px;
  padding: 7px 14px;
  border: none;
  background: transparent;
  color: rgba(0, 0, 0, 0.54);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  border-radius: 12px;
  transition: all 0.2s ease;
  white-space: nowrap;
  display: flex;
  align-items: center;
  gap: 6px;
}

.task-tab:hover {
  background: rgba(22, 119, 255, 0.06);
  color: rgba(0, 0, 0, 0.78);
}

.task-tab-active {
  background: #eaf3ff;
  color: #1677ff;
  box-shadow: inset 0 0 0 1px rgba(22, 119, 255, 0.06);
}

.task-tab-count {
  font-size: 11px;
  font-weight: 700;
  background: rgba(0, 0, 0, 0.06);
  padding: 1px 7px;
  border-radius: 999px;
  min-width: 20px;
  text-align: center;
}

.task-tab-active .task-tab-count {
  background: rgba(22, 119, 255, 0.14);
  color: #1677ff;
}

.task-search {
  width: 220px;
  flex-shrink: 0;
}

.task-search :deep(.ant-input-affix-wrapper) {
  height: 34px;
  border-radius: 12px;
  background: rgba(247, 248, 250, 0.92);
  border-color: transparent;
}

.task-search :deep(.ant-input-affix-wrapper:hover),
.task-search :deep(.ant-input-affix-wrapper-focused) {
  background: #fff;
  border-color: rgba(22, 119, 255, 0.20);
  box-shadow: 0 6px 16px rgba(22, 119, 255, 0.08);
}

/* Content Layout */
.task-content {
  position: relative;
  z-index: 1;
  display: flex;
  gap: 28px;
  align-items: flex-start;
  overflow: visible;
}

.task-main {
  flex: 1;
  min-width: 0;
}

/* Body */
.task-body {
  min-height: 240px;
  overflow: hidden;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid rgba(15, 35, 80, 0.06);
  box-shadow: 0 18px 45px rgba(15, 35, 80, 0.07), 0 1px 2px rgba(15, 35, 80, 0.04);
}

.task-empty {
  padding: 88px 20px;
}

/* List */
.task-list {
  display: flex;
  flex-direction: column;
}

.task-item {
  position: relative;
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 18px 22px;
  overflow: hidden;
  border-bottom: 1px solid rgba(15, 35, 80, 0.055);
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.88), rgba(248, 251, 255, 0.72));
  transition: background-color 0.18s ease, transform 0.18s ease, box-shadow 0.18s ease;
}

.task-item:last-child {
  border-bottom: none;
}

.task-item:hover {
  z-index: 1;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.98), rgba(247, 251, 255, 0.96));
  box-shadow: 0 12px 28px rgba(15, 35, 80, 0.065);
  transform: translateY(-1px);
}

.task-item:hover .task-item-actions {
  opacity: 1;
}

.task-item-done {
  opacity: 0.62;
  background: rgba(248, 250, 252, 0.88);
}

.task-item-done:hover {
  background: rgba(248, 250, 252, 0.98);
}

.task-item-high::before {
  content: "";
  position: absolute;
  left: 0;
  top: 16px;
  bottom: 16px;
  width: 3px;
  border-radius: 999px;
  background: #ff4d4f;
}

/* Checkbox */
.task-item-check {
  flex-shrink: 0;
  padding-top: 2px;
  cursor: pointer;
}

.task-checkbox {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 2px solid #d9d9d9;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.task-checkbox:hover {
  border-color: #52c41a;
  background: rgba(82, 196, 26, 0.06);
}

.task-checkbox-checked {
  border-color: #52c41a;
  background: #52c41a;
}

/* Body content */
.task-item-body {
  flex: 1;
  min-width: 0;
  cursor: pointer;
  overflow: hidden;
}

.task-item-title {
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

.task-item-title-done {
  text-decoration: line-through;
  color: rgba(0, 0, 0, 0.3);
  font-weight: 400;
}

.task-item-desc {
  display: -webkit-box;
  overflow: hidden;
  margin-top: 4px;
  color: rgba(0, 0, 0, 0.42);
  font-size: 12px;
  line-height: 20px;
  word-break: break-word;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.task-item-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
  min-width: 0;
  margin-top: 9px;
}

.task-tag {
  display: inline-flex;
  align-items: center;
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

.task-tag-red {
  background: rgba(255, 77, 79, 0.10);
  color: #cf1322;
  border-color: rgba(255, 77, 79, 0.10);
}

.task-tag-blue {
  background: rgba(22, 119, 255, 0.10);
  color: #0958d9;
  border-color: rgba(22, 119, 255, 0.10);
}

.task-tag-date {
  background: rgba(47, 84, 235, 0.09);
  color: #1d39c4;
  border-color: rgba(47, 84, 235, 0.08);
}

.task-tag-goal {
  background: rgba(82, 196, 26, 0.10);
  color: #389e0d;
  border-color: rgba(82, 196, 26, 0.10);
}

.task-tag-category {
  background: rgba(250, 173, 20, 0.13);
  color: #ad6800;
  border-color: rgba(250, 173, 20, 0.12);
}

.task-tag-custom {
  background: rgba(114, 46, 209, 0.08);
  color: #531dab;
  border-color: rgba(114, 46, 209, 0.08);
}

.task-tag-status {
  background: rgba(0, 0, 0, 0.045);
  color: rgba(0, 0, 0, 0.48);
  border-color: rgba(0, 0, 0, 0.035);
}

.task-tag-status-in_progress {
  background: rgba(22, 119, 255, 0.10);
  color: #0958d9;
  border-color: rgba(22, 119, 255, 0.10);
}

.task-tag-status-completed {
  background: rgba(82, 196, 26, 0.10);
  color: #389e0d;
  border-color: rgba(82, 196, 26, 0.10);
}

.task-tag-status-paused {
  background: rgba(250, 219, 20, 0.18);
  color: #ad6800;
  border-color: rgba(250, 219, 20, 0.14);
}

.task-tag-status-cancelled {
  background: rgba(255, 77, 79, 0.10);
  color: #cf1322;
  border-color: rgba(255, 77, 79, 0.10);
}

/* Right area */
.task-item-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
  width: 118px;
  flex-shrink: 0;
}

.task-item-time {
  max-width: 118px;
  padding: 2px 8px;
  overflow: hidden;
  border-radius: 999px;
  background: rgba(15, 35, 80, 0.045);
  color: rgba(0, 0, 0, 0.42);
  font-size: 11px;
  line-height: 18px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.task-item-actions {
  display: flex;
  align-items: center;
  gap: 2px;
  opacity: 0;
  transition: opacity 0.15s;
}

.task-action-btn {
  color: rgba(0, 0, 0, 0.35);
}

.task-action-btn:hover {
  color: #1677ff;
}

/* Pagination */
.task-pagination {
  padding: 12px 20px;
  display: flex;
  justify-content: center;
  border-top: 1px solid rgba(0, 0, 0, 0.04);
}

/* Sidebar */
.task-sidebar {
  width: 300px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
  position: sticky;
  top: 80px;
}

.sidebar-card {
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

.sidebar-card::after {
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

.sidebar-card-tip {
  background:
    radial-gradient(circle at 100% 0%, rgba(250, 173, 20, 0.12), transparent 32%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.96), rgba(250, 252, 255, 0.90));
  border-color: rgba(250, 173, 20, 0.10);
}

.sidebar-title {
  position: relative;
  z-index: 1;
  margin: 0 0 16px 0;
  color: rgba(0, 0, 0, 0.72);
  font-size: 13px;
  font-weight: 800;
  letter-spacing: 0.5px;
}

.sidebar-stats {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.stat-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 2px 0;
}

.stat-label {
  font-size: 13px;
  color: rgba(0, 0, 0, 0.5);
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: rgba(0, 0, 0, 0.8);
  min-width: 28px;
  text-align: right;
  font-variant-numeric: tabular-nums;
}

.stat-value-blue {
  color: #1677ff;
}

.stat-value-green {
  color: #52c41a;
}

.stat-value-red {
  color: #ff4d4f;
}

.stat-row-warn {
  margin: -2px -8px;
  padding: 6px 8px;
  border-radius: 12px;
  background: rgba(255, 77, 79, 0.08);
  box-shadow: inset 0 0 0 1px rgba(255, 77, 79, 0.06);
}

.stat-divider {
  height: 1px;
  background: rgba(0, 0, 0, 0.06);
  margin: 4px 0;
}

.sidebar-actions {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  gap: 9px;
}

.sidebar-actions :deep(.ant-btn) {
  height: 40px;
  border-color: rgba(15, 35, 80, 0.06);
  border-radius: 13px;
  background: rgba(255, 255, 255, 0.66);
  color: rgba(0, 0, 0, 0.66);
  font-size: 13px;
  font-weight: 650;
  text-align: left;
}

.sidebar-actions :deep(.ant-btn:hover) {
  color: #1677ff;
  border-color: rgba(22, 119, 255, 0.20);
  background: rgba(22, 119, 255, 0.06);
  transform: translateY(-1px);
}

.sidebar-tips {
  position: relative;
  z-index: 1;
  margin: 0;
  padding: 0 0 0 18px;
  list-style: none;
}

.sidebar-tips li {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.4);
  line-height: 24px;
  position: relative;
}

.sidebar-tips li::before {
  content: "•";
  position: absolute;
  left: -14px;
  color: rgba(0, 0, 0, 0.2);
  font-size: 10px;
}

/* Responsive */
@media (max-width: 1200px) {
  .shanzhu-task-page {
    padding: 28px 28px;
  }

  .task-sidebar {
    width: 260px;
  }
}

@media (max-width: 900px) {
  .shanzhu-task-page {
    padding: 20px 16px;
  }

  .task-header-top,
  .task-toolbar {
    align-items: stretch;
    flex-direction: column;
  }

  .task-header-top :deep(.ant-space) {
    justify-content: flex-start;
  }

  .task-search {
    width: 100%;
  }

  .task-content {
    flex-direction: column;
  }

  .task-sidebar {
    width: 100%;
    flex-direction: row;
    flex-wrap: wrap;
    position: static;
  }

  .sidebar-card {
    flex: 1;
    min-width: 220px;
  }
}

@media (max-width: 640px) {
  .task-header {
    padding: 22px 20px;
    border-radius: 22px;
  }

  .task-page-title {
    font-size: 26px;
  }

  .task-header-top :deep(.ant-space) {
    width: 100%;
    flex-direction: column;
    align-items: stretch;
  }

  .task-header-top :deep(.ant-space-item),
  .task-header-top :deep(.ant-btn) {
    width: 100%;
  }

  .task-tabs {
    padding-bottom: 2px;
  }

  .task-item {
    flex-wrap: wrap;
    gap: 12px;
    padding: 16px 18px;
  }

  .task-item-body {
    width: calc(100% - 36px);
    flex-basis: calc(100% - 36px);
  }

  .task-item-right {
    width: 100%;
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
    padding-left: 34px;
  }

  .task-item-actions {
    opacity: 1;
  }

  .task-tag {
    max-width: 100%;
  }

  .task-sidebar {
    flex-direction: column;
  }

  .sidebar-card {
    min-width: 0;
  }
}

/* ============ Animations ============ */

/* 列表项入场：渐入 + 上移 */
.task-list-anim-enter-active {
  animation: taskSlideIn 0.35s ease-out both;
  animation-delay: calc(var(--anim-order, 0) * 0.04s);
}

.task-list-anim-leave-active {
  animation: taskSlideOut 0.25s ease-in both;
}

.task-list-anim-move {
  transition: transform 0.3s ease;
}

@keyframes taskSlideIn {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes taskSlideOut {
  from {
    opacity: 1;
    transform: translateY(0);
  }
  to {
    opacity: 0;
    transform: translateX(-20px);
  }
}

/* 侧边栏卡片入场 */
.sidebar-anim {
  animation: sidebarFadeIn 0.4s ease-out both;
}

@keyframes sidebarFadeIn {
  from {
    opacity: 0;
    transform: translateY(16px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Checkbox 完成弹跳 */
.task-checkbox-checked {
  animation: checkBounce 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes checkBounce {
  0% {
    transform: scale(0.6);
  }
  50% {
    transform: scale(1.2);
  }
  100% {
    transform: scale(1);
  }
}

/* 列表卡片入场 */
.task-body {
  animation: bodyFadeIn 0.3s ease-out;
}

@keyframes bodyFadeIn {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 标签微动效 */
.task-tag {
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.task-tag:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
}

/* 操作按钮 hover 缩放 */
.task-action-btn {
  transition: all 0.2s ease;
}

.task-action-btn:hover {
  transform: scale(1.15);
}

/* Tab 切换下划线动效 */
.task-tab {
  position: relative;
}

.task-tab::after {
  content: "";
  position: absolute;
  bottom: -2px;
  left: 50%;
  width: 0;
  height: 2px;
  background: #1677ff;
  border-radius: 1px;
  transition: all 0.25s ease;
  transform: translateX(-50%);
}

.task-tab-active::after {
  width: 60%;
}

/* 新建按钮脉冲 */
.task-header-top :deep(.ant-btn-primary) {
  transition: all 0.2s ease;
}

.task-header-top :deep(.ant-btn-primary:hover) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.3);
}

.task-header-top :deep(.ant-btn-primary:active) {
  transform: translateY(0);
  box-shadow: none;
}

/* Modal */
:deep(.task-modal .ant-modal-content) {
  border-radius: 16px;
}

:deep(.task-modal .ant-modal-header) {
  border-radius: 16px 16px 0 0;
}
</style>
