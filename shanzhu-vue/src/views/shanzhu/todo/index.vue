<template>
  <div class="shanzhu-todo-page">
    <!-- 顶部：标题 + 快速添加 -->
    <div class="todo-header">
      <div class="todo-header-top">
        <h2 class="todo-page-title">收集箱</h2>
        <a-space>
          <a-button type="text" @click="router.push('/shanzhu/today-work')">
            <template #icon><DesktopOutlined/></template>
            今日工作台
          </a-button>
          <a-button type="primary" shape="round" @click="openCreateTodoModal">
            <template #icon><PlusOutlined/></template>
            新建
          </a-button>
        </a-space>
      </div>
      <div class="todo-quick-add">
        <a-input
            v-model:value="quickAddTitle"
            placeholder="记下一个想法、待办或任何事情，按回车添加..."
            size="large"
            class="todo-quick-input"
            @press-enter="handleQuickAdd"
        >
          <template #prefix>
            <PlusOutlined style="color: rgba(0,0,0,0.25);"/>
          </template>
          <template #suffix>
            <a-space :size="4">
              <a-tooltip title="添加到收集箱">
                <a-button
                    type="text"
                    size="small"
                    :loading="quickAddLoading"
                    @click="handleQuickAdd"
                >
                  <template #icon><InboxOutlined/></template>
                </a-button>
              </a-tooltip>
              <a-tooltip title="添加并设为今日待办">
                <a-button
                    type="text"
                    size="small"
                    :loading="quickAddLoading"
                    @click="handleQuickAddToToday"
                >
                  <template #icon><CalendarOutlined/></template>
                </a-button>
              </a-tooltip>
            </a-space>
          </template>
        </a-input>
      </div>
    </div>

    <!-- Tab 筛选 + 搜索 -->
    <div class="todo-toolbar">
      <div class="todo-tabs">
        <button
            v-for="tab in statusTabs"
            :key="tab.value"
            class="todo-tab"
            :class="{ 'todo-tab-active': todoQuery.status === tab.value }"
            @click="handleTabChange(tab.value)"
        >
          {{ tab.label }}
          <span v-if="tab.count !== undefined" class="todo-tab-count">{{ tab.count }}</span>
        </button>
      </div>
      <div class="todo-search">
        <a-input
            v-model:value="todoQuery.keyword"
            placeholder="搜索..."
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

    <!-- 主体：左列表 + 右侧栏 -->
    <div class="todo-content">
      <!-- 左：Todo 列表 -->
      <div class="todo-main">
        <div class="todo-body">
          <a-spin :spinning="tableLoading">
            <div v-if="todoList.length === 0" class="todo-empty">
              <a-empty description="这里什么都没有">
                <a-button type="primary" shape="round" @click="openCreateTodoModal">
                  <template #icon><PlusOutlined/></template>
                  添加第一条
                </a-button>
              </a-empty>
            </div>
            <div v-else class="todo-list">
              <div
                  v-for="todo in todoList"
                  :key="todo.id"
                  class="todo-item"
                  :class="{
                    'todo-item-done': todo.status === 'done',
                    'todo-item-high': todo.priority === 1 && todo.status !== 'done'
                  }"
              >
                <div class="todo-item-check" @click="todo.status !== 'done' ? handleComplete(todo) : handleUncomplete(todo)">
                  <div class="todo-checkbox" :class="{ 'todo-checkbox-checked': todo.status === 'done' }">
                    <CheckOutlined v-if="todo.status === 'done'" style="font-size: 11px; color: #fff;"/>
                  </div>
                </div>

                <div class="todo-item-body" @click="openEditTodoModal(todo)">
                  <div class="todo-item-title" :class="{ 'todo-item-title-done': todo.status === 'done' }">
                    {{ todo.title }}
                  </div>
                  <div v-if="todo.description" class="todo-item-desc">{{ todo.description }}</div>
                  <div class="todo-item-tags">
                    <span v-if="todo.priority === 1" class="todo-tag todo-tag-red">高优</span>
                    <span v-if="todo.priority === 3" class="todo-tag todo-tag-blue">低优</span>
                    <span v-if="todo.plannedDate" class="todo-tag todo-tag-date">
                      <CalendarOutlined style="margin-right: 2px;"/> {{ todo.plannedDate }}
                    </span>
                    <span v-if="todo.goalTitle" class="todo-tag todo-tag-goal">
                      <AimOutlined style="margin-right: 3px;"/> {{ todo.goalTitle }}
                    </span>
                  </div>
                </div>

                <div class="todo-item-actions">
                  <a-tooltip v-if="todo.status === 'inbox'" title="移入今日待办">
                    <a-button type="text" size="small" class="todo-action-btn" @click="handleMoveToToday(todo)">
                      <template #icon><CalendarOutlined/></template>
                    </a-button>
                  </a-tooltip>
                  <a-tooltip v-if="todo.status === 'today'" title="移回收集箱">
                    <a-button type="text" size="small" class="todo-action-btn" @click="handleMoveToInbox(todo)">
                      <template #icon><InboxOutlined/></template>
                    </a-button>
                  </a-tooltip>
                  <a-dropdown>
                    <a-button type="text" size="small" class="todo-action-btn">
                      <template #icon><MoreOutlined/></template>
                    </a-button>
                    <template #overlay>
                      <a-menu>
                        <a-menu-item v-if="todo.status !== 'done'" @click="handleComplete(todo)">
                          <CheckOutlined/> 标记完成
                        </a-menu-item>
                        <a-menu-item v-if="todo.status === 'done'" @click="handleUncomplete(todo)">
                          <RollbackOutlined/> 取消完成
                        </a-menu-item>
                        <a-menu-item v-if="todo.status === 'inbox'" @click="handleMoveToToday(todo)">
                          <CalendarOutlined/> 移入今日待办
                        </a-menu-item>
                        <a-menu-item v-if="todo.status === 'today'" @click="handleMoveToInbox(todo)">
                          <InboxOutlined/> 移回收集箱
                        </a-menu-item>
                        <a-menu-item v-if="todo.status !== 'converted'" @click="handleConvertToTask(todo)">
                          <CheckSquareOutlined/> 转为任务
                        </a-menu-item>
                        <a-menu-item @click="openEditTodoModal(todo)">
                          <EditOutlined/> 编辑
                        </a-menu-item>
                        <a-menu-divider/>
                        <a-menu-item danger @click="confirmDeleteTodo(todo)">
                          <DeleteOutlined/> 删除
                        </a-menu-item>
                      </a-menu>
                    </template>
                  </a-dropdown>
                </div>
              </div>
            </div>

            <div v-if="todoPagination.total > todoPagination.pageSize" class="todo-pagination">
              <a-pagination
                  v-model:current="todoPagination.current"
                  v-model:pageSize="todoPagination.pageSize"
                  :total="todoPagination.total"
                  :page-size-options="['10', '20', '30', '50']"
                  size="small"
                  show-size-changer
                  @change="handlePageChange"
              />
            </div>
          </a-spin>
        </div>
      </div>

      <!-- 右：侧边栏 -->
      <div class="todo-sidebar">
        <!-- 概览 -->
        <div class="sidebar-card">
          <h4 class="sidebar-title"><BarChartOutlined style="margin-right: 6px;"/> 概览</h4>
          <div class="sidebar-stats">
            <div class="stat-row">
              <span class="stat-label">收集箱</span>
              <span class="stat-value">{{ todoStats.inboxCount }}</span>
            </div>
            <div class="stat-row">
              <span class="stat-label">今日待办</span>
              <span class="stat-value stat-value-blue">{{ todoStats.todayCount }}</span>
            </div>
            <div class="stat-row">
              <span class="stat-label">已完成</span>
              <span class="stat-value stat-value-green">{{ todoStats.doneCount }}</span>
            </div>
            <div class="stat-row">
              <span class="stat-label">已归档</span>
              <span class="stat-value">{{ todoStats.archivedCount }}</span>
            </div>
          </div>
        </div>

        <!-- 快捷操作 -->
        <div class="sidebar-card">
          <h4 class="sidebar-title"><ThunderboltOutlined style="margin-right: 6px;"/> 快捷操作</h4>
          <div class="sidebar-actions">
            <a-button block @click="router.push('/shanzhu/today-work')">
              <template #icon><DesktopOutlined/></template>
              今日工作台
            </a-button>
            <a-button block @click="router.push('/shanzhu/task')">
              <template #icon><CheckSquareOutlined/></template>
              任务中心
            </a-button>
            <a-button block @click="router.push('/shanzhu/goal')">
              <template #icon><AimOutlined/></template>
              目标列表
            </a-button>
          </div>
        </div>

        <!-- 小贴士 -->
        <div class="sidebar-card sidebar-card-tip">
          <h4 class="sidebar-title"><BulbOutlined style="margin-right: 6px;"/> 使用提示</h4>
          <ul class="sidebar-tips">
            <li>想到什么就记，不用分类</li>
            <li>定期整理：转任务 / 移入今日 / 删除</li>
            <li>重要的事标高优先级</li>
            <li>按回车快速添加</li>
          </ul>
        </div>
      </div>
    </div>

    <!-- 新建/编辑 Todo 弹窗 -->
    <a-modal
        v-model:open="todoModal.open"
        :confirm-loading="todoModal.saveLoading"
        :title="todoModal.title"
        width="520px"
        ok-text="保存"
        cancel-text="取消"
        class="todo-modal"
        @ok="handleSaveTodo"
    >
      <a-form ref="todoFormRef" :colon="false" :model="todoForm" :rules="todoRules" layout="vertical" style="margin-top: 16px;">
        <a-form-item label="标题" name="title">
          <a-input v-model:value="todoForm.title" placeholder="这件事是..." :maxlength="120" show-count allow-clear/>
        </a-form-item>
        <a-form-item label="说明">
          <a-textarea v-model:value="todoForm.description" placeholder="补充一下细节（可选）" :rows="3" :maxlength="500" show-count allow-clear/>
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="状态">
              <a-select v-model:value="todoForm.status" placeholder="请选择状态">
                <a-select-option v-for="item in todoStatusOptions" :key="item.value" :value="item.value">
                  {{ item.label }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="优先级">
              <a-select v-model:value="todoForm.priority" placeholder="请选择" allow-clear>
                <a-select-option :value="1">🔴 高</a-select-option>
                <a-select-option :value="2">🟡 中</a-select-option>
                <a-select-option :value="3">🔵 低</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="计划日期">
              <a-date-picker v-model:value="todoForm.plannedDate" value-format="YYYY-MM-DD" style="width: 100%" placeholder="什么时候做？"/>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="截止时间">
              <a-date-picker
                  v-model:value="todoForm.deadline"
                  value-format="YYYY-MM-DDTHH:mm:ss"
                  show-time
                  style="width: 100%"
                  placeholder="最晚什么时候？"
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="关联目标">
              <a-select
                  v-model:value="todoForm.goalId"
                  placeholder="可选"
                  allow-clear
                  show-search
                  :filter-option="filterGoalOption"
              >
                <a-select-option v-for="goal in goalOptions" :key="goal.id" :value="goal.id">
                  {{ goal.title }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="预计耗时">
              <a-input-number v-model:value="todoForm.estimatedMinutes" :min="0" :max="99999" style="width: 100%" placeholder="分钟" addon-after="分钟"/>
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="备注">
          <a-textarea v-model:value="todoForm.remark" placeholder="其他备注（可选）" :rows="2" :maxlength="300" show-count allow-clear/>
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
  BarChartOutlined,
  BulbOutlined,
  CalendarOutlined,
  CheckOutlined,
  CheckSquareOutlined,
  DeleteOutlined,
  DesktopOutlined,
  EditOutlined,
  InboxOutlined,
  MoreOutlined,
  PlusOutlined,
  RollbackOutlined,
  SearchOutlined,
  ThunderboltOutlined
} from "@ant-design/icons-vue";
import {queryGoalPage} from "@/api/shanzhu/goal/Goal.ts";
import type {ShanzhuGoalVO} from "@/api/shanzhu/goal/type/Goal.ts";
import type {BaseModalActiveType} from "@/api/global/Type.ts";
import {
  archiveTodo,
  completeTodo,
  convertTodoToTask,
  deleteTodo,
  moveToInbox,
  moveToToday,
  queryTodoList,
  queryTodoPage,
  saveTodo,
  uncompleteTodo
} from "@/api/shanzhu/todo/Todo.ts";
import type {ShanzhuTodo, ShanzhuTodoQuery, ShanzhuTodoVO, TodoStatusOption} from "@/api/shanzhu/todo/type/Todo.ts";

const router = useRouter();

const inboxStatus = "inbox";
const todayStatus = "today";
const doneStatus = "done";

const todoStatusOptions: TodoStatusOption[] = [
  {label: "收集箱", value: "inbox", color: "default"},
  {label: "今日待办", value: "today", color: "processing"},
  {label: "已完成", value: "done", color: "success"},
  {label: "已归档", value: "archived", color: "warning"},
  {label: "已转任务", value: "converted", color: "purple"}
];

const statusTabs = computed(() => [
  {label: "全部", value: undefined as string | undefined, count: todoStats.totalCount},
  {label: "收集箱", value: "inbox", count: todoStats.inboxCount},
  {label: "今日", value: "today", count: todoStats.todayCount},
  {label: "已完成", value: "done", count: todoStats.doneCount},
  {label: "已归档", value: "archived", count: todoStats.archivedCount}
]);

const defaultTodoQuery = (): ShanzhuTodoQuery => ({
  pageNum: 1,
  pageSize: 10
});

const defaultTodoForm = (): ShanzhuTodo => ({
  status: inboxStatus,
  priority: 2,
  sortOrder: 0
});

const todoQuery = ref<ShanzhuTodoQuery>(defaultTodoQuery());
const todoList = ref<ShanzhuTodoVO[]>([]);
const goalOptions = ref<ShanzhuGoalVO[]>([]);
const tableLoading = ref(false);
const goalLoading = ref(false);
const quickAddLoading = ref(false);
const quickAddTitle = ref("");
const todoFormRef = ref<FormInstance>();
const todoForm = ref<ShanzhuTodo>(defaultTodoForm());

const todoPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
});

const todoModal = reactive<BaseModalActiveType>({
  open: false,
  saveLoading: false,
  title: "新建 Todo"
});

const todoStats = reactive({
  totalCount: 0,
  inboxCount: 0,
  todayCount: 0,
  doneCount: 0,
  archivedCount: 0
});

const todoRules: Record<string, Rule[]> = {
  title: [{required: true, message: "请输入标题", trigger: "blur"}]
};

const getTodoStatusOption = (status?: string) => {
  return todoStatusOptions.find(item => item.value === status) || todoStatusOptions[0];
};

const getPriorityColor = (priority?: number) => {
  switch (priority) {
    case 1:
      return "red";
    case 2:
      return "orange";
    case 3:
      return "blue";
    default:
      return "default";
  }
};

const getPriorityLabel = (priority?: number) => {
  switch (priority) {
    case 1:
      return "高";
    case 2:
      return "中";
    case 3:
      return "低";
    default:
      return "-";
  }
};

const filterGoalOption = (input: string, option?: { label?: string; value?: string; children?: string }) => {
  const keyword = input.toLowerCase();
  return String(option?.children || "").toLowerCase().includes(keyword);
};

const loadGoalOptions = async () => {
  goalLoading.value = true;
  try {
    const response = await queryGoalPage({
      pageNum: 1,
      pageSize: 100
    });
    if (response.code === 200) {
      goalOptions.value = response.data.records || [];
    } else {
      message.error(response.msg || "目标列表加载失败");
    }
  } finally {
    goalLoading.value = false;
  }
};

const loadTodoList = async (pageNum = todoPagination.current, pageSize = todoPagination.pageSize) => {
  tableLoading.value = true;
  try {
    const response = await queryTodoPage({
      ...todoQuery.value,
      pageNum,
      pageSize
    });
    if (response.code === 200) {
      todoList.value = response.data.records || [];
      todoPagination.current = response.data.current || pageNum;
      todoPagination.pageSize = response.data.size || pageSize;
      todoPagination.total = response.data.total || 0;
      todoQuery.value.pageNum = todoPagination.current;
      todoQuery.value.pageSize = todoPagination.pageSize;
    } else {
      message.error(response.msg || "Todo列表加载失败");
    }
  } finally {
    tableLoading.value = false;
  }
};

const loadTodoStats = async () => {
  try {
    const [allRes, inboxRes, todayRes, doneRes, archivedRes] = await Promise.all([
      queryTodoList({}),
      queryTodoList({status: inboxStatus}),
      queryTodoList({status: todayStatus}),
      queryTodoList({status: doneStatus}),
      queryTodoList({status: "archived"})
    ]);
    todoStats.totalCount = allRes.code === 200 ? (allRes.data?.length || 0) : 0;
    todoStats.inboxCount = inboxRes.code === 200 ? (inboxRes.data?.length || 0) : 0;
    todoStats.todayCount = todayRes.code === 200 ? (todayRes.data?.length || 0) : 0;
    todoStats.doneCount = doneRes.code === 200 ? (doneRes.data?.length || 0) : 0;
    todoStats.archivedCount = archivedRes.code === 200 ? (archivedRes.data?.length || 0) : 0;
  } catch {
    // 统计失败不影响主流程
  }
};

const refreshTodoData = async () => {
  await Promise.all([
    loadTodoList(),
    loadTodoStats()
  ]);
};

const handleSearch = () => {
  loadTodoList(1, todoPagination.pageSize);
};

const handlePageChange = (pageNum: number, pageSize: number) => {
  loadTodoList(pageNum, pageSize);
};

const resetQuery = () => {
  todoQuery.value = defaultTodoQuery();
  todoPagination.current = 1;
  todoPagination.pageSize = 10;
  todoPagination.total = 0;
  loadTodoList(1, todoPagination.pageSize);
};

const handleTabChange = (status?: string) => {
  todoQuery.value.status = status;
  loadTodoList(1, todoPagination.pageSize);
};

let searchTimer: ReturnType<typeof setTimeout> | null = null;
const handleSearchChange = () => {
  if (searchTimer) clearTimeout(searchTimer);
  searchTimer = setTimeout(() => {
    loadTodoList(1, todoPagination.pageSize);
  }, 400);
};

const handleQuickAdd = async () => {
  if (!quickAddTitle.value.trim()) {
    message.warning("请输入内容");
    return;
  }

  quickAddLoading.value = true;
  try {
    const response = await saveTodo({
      title: quickAddTitle.value.trim(),
      status: inboxStatus
    });
    if (response.code === 200) {
      message.success("已添加到收集箱");
      quickAddTitle.value = "";
      await refreshTodoData();
    } else {
      message.error(response.msg || "添加失败");
    }
  } finally {
    quickAddLoading.value = false;
  }
};

const handleQuickAddToToday = async () => {
  if (!quickAddTitle.value.trim()) {
    message.warning("请输入内容");
    return;
  }

  quickAddLoading.value = true;
  try {
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, "0");
    const day = String(today.getDate()).padStart(2, "0");
    const response = await saveTodo({
      title: quickAddTitle.value.trim(),
      status: todayStatus,
      plannedDate: `${year}-${month}-${day}`
    });
    if (response.code === 200) {
      message.success("已添加到今日待办");
      quickAddTitle.value = "";
      await refreshTodoData();
    } else {
      message.error(response.msg || "添加失败");
    }
  } finally {
    quickAddLoading.value = false;
  }
};

const openCreateTodoModal = () => {
  todoForm.value = defaultTodoForm();
  todoModal.title = "新建 Todo";
  todoModal.open = true;
};

const openEditTodoModal = (todo: ShanzhuTodoVO) => {
  todoForm.value = {
    ...todo
  };
  todoModal.title = "编辑 Todo";
  todoModal.open = true;
};

const handleSaveTodo = async () => {
  await todoFormRef.value?.validate();

  todoModal.saveLoading = true;
  try {
    const response = await saveTodo(todoForm.value);
    if (response.code === 200) {
      message.success("保存成功");
      todoModal.open = false;
      await refreshTodoData();
    } else {
      message.error(response.msg || "保存失败");
    }
  } finally {
    todoModal.saveLoading = false;
  }
};

const handleComplete = async (todo: ShanzhuTodoVO) => {
  if (!todo.id) {
    return;
  }

  const response = await completeTodo(todo.id);
  if (response.code === 200) {
    message.success("已完成");
    await refreshTodoData();
  } else {
    message.error(response.msg || "操作失败");
  }
};

const handleUncomplete = async (todo: ShanzhuTodoVO) => {
  if (!todo.id) {
    return;
  }

  const response = await uncompleteTodo(todo.id);
  if (response.code === 200) {
    message.success("已取消完成");
    await refreshTodoData();
  } else {
    message.error(response.msg || "操作失败");
  }
};

const handleMoveToToday = async (todo: ShanzhuTodoVO) => {
  if (!todo.id) {
    return;
  }

  const response = await moveToToday(todo.id);
  if (response.code === 200) {
    message.success("已移入今日待办");
    await refreshTodoData();
  } else {
    message.error(response.msg || "操作失败");
  }
};

const handleMoveToInbox = async (todo: ShanzhuTodoVO) => {
  if (!todo.id) {
    return;
  }

  const response = await moveToInbox(todo.id);
  if (response.code === 200) {
    message.success("已移回收集箱");
    await refreshTodoData();
  } else {
    message.error(response.msg || "操作失败");
  }
};

const handleConvertToTask = async (todo: ShanzhuTodoVO) => {
  if (!todo.id) {
    return;
  }

  Modal.confirm({
    title: "确认转为任务？",
    content: `将「${todo.title || '-'}」转为正式任务，进入任务中心执行。`,
    okText: "确认转换",
    cancelText: "取消",
    onOk: async () => {
      const response = await convertTodoToTask(todo.id || "");
      if (response.code === 200) {
        message.success("已转为任务");
        await refreshTodoData();
      } else {
        message.error(response.msg || "转换失败");
      }
    }
  });
};

const confirmDeleteTodo = (todo: ShanzhuTodoVO) => {
  if (!todo.id) {
    return;
  }

  Modal.confirm({
    title: "确认删除 Todo？",
    content: `删除后，「${todo.title || '-'}」将不再展示。`,
    okText: "确认删除",
    cancelText: "取消",
    okType: "danger",
    onOk: async () => {
      const response = await deleteTodo(todo.id || "");
      if (response.code === 200) {
        message.success("删除成功");
        await refreshTodoData();
      } else {
        message.error(response.msg || "删除失败");
      }
    }
  });
};

onMounted(async () => {
  await loadGoalOptions();
  await refreshTodoData();
});
</script>

<style scoped>
.shanzhu-todo-page {
  max-width: 1360px;
  margin: 0 auto;
  padding: 36px 48px 56px;
  min-height: calc(100vh - 120px);
}

/* Header */
.todo-header {
  padding: 24px;
  margin-bottom: 18px;
  border-radius: 24px;
  background:
    radial-gradient(circle at 18% 0%, rgba(22, 119, 255, 0.10), transparent 34%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.96), rgba(247, 250, 255, 0.92));
  border: 1px solid rgba(22, 119, 255, 0.08);
  box-shadow: 0 18px 45px rgba(15, 35, 80, 0.06);
}

.todo-header-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}

.todo-page-title {
  font-size: 28px;
  font-weight: 800;
  color: rgba(0, 0, 0, 0.88);
  margin: 0;
  letter-spacing: -0.6px;
}

/* Quick Add */
.todo-quick-add {
  margin-bottom: 0;
}

.todo-quick-input {
  height: 52px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.82);
  border-color: rgba(22, 119, 255, 0.08);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.72);
  transition: all 0.25s ease;
}

.todo-quick-input:hover {
  background: #fff;
  border-color: rgba(22, 119, 255, 0.18);
}

.todo-quick-input:focus-within {
  background: #fff;
  border-color: #1677ff;
  box-shadow: 0 10px 24px rgba(22, 119, 255, 0.10);
}

/* Toolbar */
.todo-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
  gap: 16px;
  padding: 8px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(0, 0, 0, 0.04);
  box-shadow: 0 10px 28px rgba(15, 35, 80, 0.04);
  backdrop-filter: blur(12px);
}

.todo-tabs {
  display: flex;
  gap: 6px;
  overflow-x: auto;
}

.todo-tab {
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

.todo-tab:hover {
  background: rgba(22, 119, 255, 0.06);
  color: rgba(0, 0, 0, 0.78);
}

.todo-tab-active {
  background: #eaf3ff;
  color: #1677ff;
  box-shadow: inset 0 0 0 1px rgba(22, 119, 255, 0.06);
}

.todo-tab-count {
  font-size: 11px;
  font-weight: 700;
  background: rgba(0, 0, 0, 0.06);
  padding: 1px 7px;
  border-radius: 999px;
  min-width: 20px;
  text-align: center;
}

.todo-tab-active .todo-tab-count {
  background: rgba(22, 119, 255, 0.14);
  color: #1677ff;
}

.todo-search {
  width: 220px;
  flex-shrink: 0;
}

.todo-search :deep(.ant-input-affix-wrapper) {
  height: 34px;
  border-radius: 12px;
  background: rgba(247, 248, 250, 0.92);
  border-color: transparent;
}

.todo-search :deep(.ant-input-affix-wrapper:hover),
.todo-search :deep(.ant-input-affix-wrapper-focused) {
  background: #fff;
  border-color: rgba(22, 119, 255, 0.20);
  box-shadow: 0 6px 16px rgba(22, 119, 255, 0.08);
}

/* Body */
.todo-body {
  background: rgba(255, 255, 255, 0.94);
  border-radius: 20px;
  box-shadow: 0 18px 45px rgba(15, 35, 80, 0.07), 0 1px 2px rgba(15, 35, 80, 0.04);
  border: 1px solid rgba(15, 35, 80, 0.06);
  overflow: hidden;
  min-height: 160px;
}

.todo-empty {
  padding: 72px 20px;
}

/* List */
.todo-list {
  display: flex;
  flex-direction: column;
}

.todo-item {
  display: flex;
  align-items: flex-start;
  padding: 18px 22px;
  border-bottom: 1px solid rgba(15, 35, 80, 0.06);
  transition: background-color 0.18s ease, transform 0.18s ease, box-shadow 0.18s ease;
  gap: 14px;
  position: relative;
}

.todo-item:last-child {
  border-bottom: none;
}

.todo-item:hover {
  z-index: 1;
  background-color: #fbfdff;
  transform: translateY(-1px);
  box-shadow: 0 10px 28px rgba(15, 35, 80, 0.06);
}

.todo-item:hover .todo-item-actions {
  opacity: 1;
}

.todo-item-done {
  opacity: 0.58;
  background: linear-gradient(90deg, rgba(82, 196, 26, 0.04), rgba(255, 255, 255, 0.84));
}

.todo-item-done:hover {
  background: linear-gradient(90deg, rgba(82, 196, 26, 0.06), #fff);
}

.todo-item-high {
  border-left: 3px solid #ff4d4f;
  padding-left: 19px;
  background: linear-gradient(90deg, rgba(255, 77, 79, 0.035), rgba(255, 255, 255, 0.94));
}

/* Checkbox */
.todo-item-check {
  flex-shrink: 0;
  padding-top: 2px;
  cursor: pointer;
}

.todo-checkbox {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 2px solid #d9d9d9;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.todo-checkbox:hover {
  border-color: #52c41a;
  background: rgba(82, 196, 26, 0.06);
}

.todo-checkbox-checked {
  border-color: #52c41a;
  background: #52c41a;
}

/* Body content */
.todo-item-body {
  flex: 1;
  min-width: 0;
  cursor: pointer;
}

.todo-item-title {
  font-size: 14px;
  font-weight: 500;
  color: rgba(0, 0, 0, 0.85);
  line-height: 24px;
  word-break: break-word;
}

.todo-item-title-done {
  text-decoration: line-through;
  color: rgba(0, 0, 0, 0.3);
  font-weight: 400;
}

.todo-item-desc {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.4);
  margin-top: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.todo-item-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 6px;
}

.todo-tag {
  font-size: 11px;
  padding: 1px 8px;
  border-radius: 4px;
  display: inline-flex;
  align-items: center;
  line-height: 18px;
}

.todo-tag-red {
  background: #fff1f0;
  color: #cf1322;
}

.todo-tag-blue {
  background: #e6f4ff;
  color: #0958d9;
}

.todo-tag-date {
  background: #f0f5ff;
  color: #1d39c4;
}

.todo-tag-goal {
  background: #f6ffed;
  color: #389e0d;
}

/* Actions */
.todo-item-actions {
  display: flex;
  align-items: center;
  gap: 2px;
  flex-shrink: 0;
  opacity: 0;
  transition: opacity 0.15s;
}

.todo-action-btn {
  color: rgba(0, 0, 0, 0.35);
}

.todo-action-btn:hover {
  color: #1677ff;
}

/* Pagination */
.todo-pagination {
  padding: 12px 20px;
  display: flex;
  justify-content: center;
  border-top: 1px solid rgba(0, 0, 0, 0.04);
}

/* Content Layout */
.todo-content {
  display: flex;
  gap: 28px;
  align-items: flex-start;
}

.todo-main {
  flex: 1;
  min-width: 0;
}

/* Sidebar */
.todo-sidebar {
  width: 300px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
  position: sticky;
  top: 80px;
}

.sidebar-card {
  background: rgba(255, 255, 255, 0.94);
  border-radius: 18px;
  padding: 20px;
  box-shadow: 0 16px 36px rgba(15, 35, 80, 0.06), 0 1px 2px rgba(15, 35, 80, 0.04);
  border: 1px solid rgba(15, 35, 80, 0.06);
}

.sidebar-card-tip {
  background: linear-gradient(135deg, rgba(250, 252, 255, 0.98) 0%, rgba(244, 248, 255, 0.96) 100%);
  border: 1px solid rgba(22, 119, 255, 0.06);
}

.sidebar-title {
  font-size: 13px;
  font-weight: 700;
  color: rgba(0, 0, 0, 0.70);
  margin: 0 0 16px 0;
  letter-spacing: 0.2px;
}

/* Stats */
.sidebar-stats {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.stat-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 10px;
  border-radius: 12px;
  background: rgba(247, 249, 252, 0.75);
}

.stat-label {
  font-size: 13px;
  font-weight: 500;
  color: rgba(0, 0, 0, 0.52);
}

.stat-value {
  font-size: 22px;
  font-weight: 800;
  color: rgba(0, 0, 0, 0.82);
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

/* Sidebar Actions */
.sidebar-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.sidebar-actions :deep(.ant-btn) {
  text-align: left;
  border-radius: 12px;
  height: 42px;
  font-size: 13px;
  font-weight: 500;
  color: rgba(0, 0, 0, 0.66);
  border-color: rgba(15, 35, 80, 0.08);
  background: rgba(255, 255, 255, 0.76);
  transition: all 0.2s ease;
}

.sidebar-actions :deep(.ant-btn:hover) {
  color: #1677ff;
  border-color: rgba(22, 119, 255, 0.26);
  background: rgba(22, 119, 255, 0.05);
  transform: translateY(-1px);
}

/* Tips */
.sidebar-tips {
  margin: 0;
  padding: 0 0 0 18px;
  list-style: none;
}

.sidebar-tips li {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.46);
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
  .shanzhu-todo-page {
    padding: 28px 28px 48px;
  }

  .todo-sidebar {
    width: 270px;
  }
}

@media (max-width: 900px) {
  .shanzhu-todo-page {
    padding: 20px 16px 40px;
  }

  .todo-header {
    padding: 20px;
    border-radius: 20px;
  }

  .todo-header-top,
  .todo-toolbar {
    align-items: stretch;
    flex-direction: column;
  }

  .todo-search {
    width: 100%;
  }

  .todo-content {
    flex-direction: column;
    gap: 18px;
  }

  .todo-sidebar {
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

/* Modal */
:deep(.todo-modal .ant-modal-content) {
  border-radius: 16px;
}

:deep(.todo-modal .ant-modal-header) {
  border-radius: 16px 16px 0 0;
}
</style>
