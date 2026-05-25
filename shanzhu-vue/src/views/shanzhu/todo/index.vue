<template>
  <div class="shanzhu-todo-page">
    <a-flex :gap="16" vertical>
      <!-- 统计卡片 -->
      <a-row :gutter="[16, 16]">
        <a-col :xs="24" :sm="12" :lg="6">
          <a-card :bordered="false">
            <a-statistic title="收集箱" :value="todoStats.inboxCount || 0" suffix="个"/>
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :lg="6">
          <a-card :bordered="false">
            <a-statistic title="今日待办" :value="todoStats.todayCount || 0" suffix="个" :value-style="{ color: '#1890ff' }"/>
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :lg="6">
          <a-card :bordered="false">
            <a-statistic title="已完成" :value="todoStats.doneCount || 0" suffix="个" :value-style="{ color: '#52c41a' }"/>
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :lg="6">
          <a-card :bordered="false">
            <a-statistic title="已归档" :value="todoStats.archivedCount || 0" suffix="个" :value-style="{ color: '#8c8c8c' }"/>
          </a-card>
        </a-col>
      </a-row>

      <!-- 快速添加 -->
      <a-card :bordered="false" class="todo-quick-add-card">
        <a-input-search
            v-model:value="quickAddTitle"
            placeholder="快速添加一个想法、任务或待办事项，按回车进入收集箱..."
            size="large"
            :loading="quickAddLoading"
            @search="handleQuickAdd"
        >
          <template #enterButton>
            <a-button type="primary">
              <template #icon>
                <PlusOutlined/>
              </template>
              添加
            </a-button>
          </template>
        </a-input-search>
      </a-card>

      <!-- 筛选区 -->
      <a-card :bordered="false" class="todo-filter-card">
        <a-form :colon="false">
          <a-row :gutter="[16, 12]">
            <a-col :xs="24" :sm="12" :lg="6">
              <a-form-item label="关键词">
                <a-input v-model:value="todoQuery.keyword" placeholder="搜索Todo标题" allow-clear/>
              </a-form-item>
            </a-col>
            <a-col :xs="24" :sm="12" :lg="6">
              <a-form-item label="状态">
                <a-select v-model:value="todoQuery.status" placeholder="请选择状态" allow-clear>
                  <a-select-option v-for="item in todoStatusOptions" :key="item.value" :value="item.value">
                    {{ item.label }}
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :xs="24" :sm="12" :lg="6">
              <a-form-item label="目标">
                <a-select
                    v-model:value="todoQuery.goalId"
                    placeholder="请选择目标"
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
            <a-col :xs="24" :sm="12" :lg="6">
              <a-form-item label="优先级">
                <a-select v-model:value="todoQuery.priority" placeholder="请选择优先级" allow-clear>
                  <a-select-option :value="1">高</a-select-option>
                  <a-select-option :value="2">中</a-select-option>
                  <a-select-option :value="3">低</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :xs="24">
              <a-form-item>
                <a-space size="small">
                  <a-button type="primary" :loading="tableLoading" @click="handleSearch">
                    <template #icon>
                      <SearchOutlined/>
                    </template>
                    查 询
                  </a-button>
                  <a-button :loading="tableLoading" @click="resetQuery">
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

      <!-- Todo 列表 -->
      <a-card :bordered="false" class="todo-list-card">
        <template #title>
          <a-flex justify="space-between" align="center" wrap="wrap" :gap="12">
            <div>
              <a-typography-title :level="4" class="todo-section-title">Todo 列表</a-typography-title>
              <a-typography-text type="secondary">收集、整理和跟踪你的想法与待办事项</a-typography-text>
            </div>
            <a-space>
              <a-button @click="loadTodoList">
                <template #icon>
                  <ReloadOutlined/>
                </template>
                刷新
              </a-button>
              <a-button type="primary" @click="openCreateTodoModal">
                <template #icon>
                  <PlusOutlined/>
                </template>
                新建 Todo
              </a-button>
            </a-space>
          </a-flex>
        </template>

        <a-spin :spinning="tableLoading">
          <a-empty v-if="todoList.length === 0" description="暂无 Todo，快速添加一个吧"/>
          <a-row v-else :gutter="[16, 16]">
            <a-col v-for="todo in todoList" :key="todo.id" :xs="24" :lg="12" :xl="8">
              <a-card class="todo-card" :class="{ 'todo-card-done': todo.status === 'done' }" hoverable>
                <a-flex vertical :gap="12">
                  <a-flex justify="space-between" align="flex-start" :gap="12">
                    <div class="todo-main">
                      <a-space size="small" wrap>
                        <a-tag :color="getTodoStatusOption(todo.status).color">
                          {{ getTodoStatusOption(todo.status).label }}
                        </a-tag>
                        <a-tag v-if="todo.priority" :color="getPriorityColor(todo.priority)">
                          {{ getPriorityLabel(todo.priority) }}
                        </a-tag>
                        <a-tag v-if="todo.plannedDate" color="blue">
                          {{ todo.plannedDate }}
                        </a-tag>
                      </a-space>
                      <a-typography-title :level="5" class="todo-title" :class="{ 'todo-title-done': todo.status === 'done' }">
                        {{ todo.title }}
                      </a-typography-title>
                    </div>
                    <a-dropdown>
                      <a-button type="text" size="small">
                        <MoreOutlined/>
                      </a-button>
                      <template #overlay>
                        <a-menu>
                          <a-menu-item v-if="todo.status !== 'done'" @click="handleComplete(todo)">
                            <CheckOutlined/>
                            标记完成
                          </a-menu-item>
                          <a-menu-item v-if="todo.status === 'done'" @click="handleUncomplete(todo)">
                            <RollbackOutlined/>
                            取消完成
                          </a-menu-item>
                          <a-menu-item v-if="todo.status === 'inbox'" @click="handleMoveToToday(todo)">
                            <CalendarOutlined/>
                            移入今日待办
                          </a-menu-item>
                          <a-menu-item v-if="todo.status === 'today'" @click="handleMoveToInbox(todo)">
                            <InboxOutlined/>
                            移回收集箱
                          </a-menu-item>
                          <a-menu-item v-if="todo.status !== 'converted'" @click="handleConvertToTask(todo)">
                            <CheckSquareOutlined/>
                            转为任务
                          </a-menu-item>
                          <a-menu-item @click="openEditTodoModal(todo)">
                            <EditOutlined/>
                            编辑
                          </a-menu-item>
                          <a-menu-divider/>
                          <a-menu-item danger @click="confirmDeleteTodo(todo)">
                            <DeleteOutlined/>
                            删除
                          </a-menu-item>
                        </a-menu>
                      </template>
                    </a-dropdown>
                  </a-flex>

                  <a-typography-paragraph v-if="todo.description" class="todo-description" :ellipsis="{ rows: 2 }">
                    {{ todo.description }}
                  </a-typography-paragraph>

                  <a-flex v-if="todo.goalTitle || todo.subGoalTitle" class="todo-meta" wrap="wrap" :gap="8">
                    <span v-if="todo.goalTitle">目标：{{ todo.goalTitle }}</span>
                    <span v-if="todo.subGoalTitle">子目标：{{ todo.subGoalTitle }}</span>
                  </a-flex>

                  <a-flex justify="space-between" align="center" class="todo-footer">
                    <span class="todo-time">{{ todo.createTime }}</span>
                    <a-space size="small">
                      <a-button
                          v-if="todo.status === 'inbox'"
                          size="small"
                          @click="handleMoveToToday(todo)"
                      >
                        今日待办
                      </a-button>
                      <a-button
                          v-if="todo.status !== 'done'"
                          type="primary"
                          size="small"
                          @click="handleComplete(todo)"
                      >
                        完成
                      </a-button>
                    </a-space>
                  </a-flex>
                </a-flex>
              </a-card>
            </a-col>
          </a-row>

          <a-pagination
              v-if="todoPagination.total > 0"
              v-model:current="todoPagination.current"
              v-model:pageSize="todoPagination.pageSize"
              :total="todoPagination.total"
              :page-size-options="['9', '18', '27', '36']"
              show-size-changer
              show-total
              class="todo-pagination"
              @change="handlePageChange"
          />
        </a-spin>
      </a-card>
    </a-flex>

    <!-- 新建/编辑 Todo 弹窗 -->
    <a-modal
        v-model:open="todoModal.open"
        :confirm-loading="todoModal.saveLoading"
        :title="todoModal.title"
        width="600px"
        ok-text="保 存"
        cancel-text="取 消"
        @ok="handleSaveTodo"
    >
      <a-form ref="todoFormRef" :colon="false" :model="todoForm" :rules="todoRules" :label-col="{ span: 5 }">
        <a-form-item label="标题" name="title">
          <a-input v-model:value="todoForm.title" placeholder="请输入Todo标题" :maxlength="120" show-count allow-clear/>
        </a-form-item>
        <a-row>
          <a-col :span="12">
            <a-form-item label="状态" :label-col="{ span: 10 }">
              <a-select v-model:value="todoForm.status" placeholder="请选择状态">
                <a-select-option v-for="item in todoStatusOptions" :key="item.value" :value="item.value">
                  {{ item.label }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="优先级" :label-col="{ span: 10 }">
              <a-select v-model:value="todoForm.priority" placeholder="请选择优先级" allow-clear>
                <a-select-option :value="1">高</a-select-option>
                <a-select-option :value="2">中</a-select-option>
                <a-select-option :value="3">低</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row>
          <a-col :span="12">
            <a-form-item label="计划日期" :label-col="{ span: 10 }">
              <a-date-picker v-model:value="todoForm.plannedDate" value-format="YYYY-MM-DD" style="width: 100%" placeholder="请选择计划日期"/>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="截止时间" :label-col="{ span: 10 }">
              <a-date-picker
                  v-model:value="todoForm.deadline"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  show-time
                  style="width: 100%"
                  placeholder="请选择截止时间"
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row>
          <a-col :span="12">
            <a-form-item label="关联目标" :label-col="{ span: 10 }">
              <a-select
                  v-model:value="todoForm.goalId"
                  placeholder="请选择目标"
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
            <a-form-item label="预计耗时" :label-col="{ span: 10 }">
              <a-input-number v-model:value="todoForm.estimatedMinutes" :min="0" :max="99999" style="width: 100%" placeholder="分钟"/>
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="Todo说明">
          <a-textarea v-model:value="todoForm.description" placeholder="描述这个Todo的具体内容" :rows="4" :maxlength="500" show-count allow-clear/>
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model:value="todoForm.remark" placeholder="补充说明" :rows="3" :maxlength="300" show-count allow-clear/>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import {onMounted, reactive, ref} from "vue";
import type {FormInstance, Rule} from "ant-design-vue/es/form";
import {message, Modal} from "ant-design-vue";
import {
  CalendarOutlined,
  CheckOutlined,
  CheckSquareOutlined,
  DeleteOutlined,
  EditOutlined,
  InboxOutlined,
  MoreOutlined,
  PlusOutlined,
  RedoOutlined,
  ReloadOutlined,
  RollbackOutlined,
  SearchOutlined
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

const defaultTodoQuery = (): ShanzhuTodoQuery => ({
  pageNum: 1,
  pageSize: 9
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
  pageSize: 9,
  total: 0
});

const todoModal = reactive<BaseModalActiveType>({
  open: false,
  saveLoading: false,
  title: "新建 Todo"
});

const todoStats = reactive({
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
    const [inboxRes, todayRes, doneRes, archivedRes] = await Promise.all([
      queryTodoList({status: inboxStatus, pageNum: 1, pageSize: 1}),
      queryTodoList({status: todayStatus, pageNum: 1, pageSize: 1}),
      queryTodoList({status: doneStatus, pageNum: 1, pageSize: 1}),
      queryTodoList({status: "archived", pageNum: 1, pageSize: 1})
    ]);
    todoStats.inboxCount = inboxRes.code === 200 ? (inboxRes.data?.length || 0) : 0;
    todoStats.todayCount = todayRes.code === 200 ? (todayRes.data?.length || 0) : 0;
    todoStats.doneCount = doneRes.code === 200 ? (doneRes.data?.length || 0) : 0;
    todoStats.archivedCount = archivedRes.code === 200 ? (archivedRes.data?.length || 0) : 0;
  } catch (error) {
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
  todoPagination.pageSize = 9;
  todoPagination.total = 0;
  loadTodoList(1, todoPagination.pageSize);
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
  width: 100%;
}

.todo-section-title {
  margin-bottom: 4px;
}

.todo-quick-add-card {
  background: #f0f5ff;
}

.todo-card {
  height: 100%;
}

.todo-card-done {
  background: #f6ffed;
  opacity: 0.85;
}

.todo-main {
  min-width: 0;
  flex: 1;
}

.todo-title {
  margin-top: 8px;
  margin-bottom: 4px;
}

.todo-title-done {
  text-decoration: line-through;
  color: var(--lihua-text-color-secondary);
}

.todo-description {
  margin-bottom: 0;
  color: var(--lihua-text-color-secondary);
}

.todo-meta {
  color: var(--lihua-text-color-secondary);
  font-size: var(--lihua-font-size-sm);
}

.todo-footer {
  border-top: 1px solid var(--lihua-border-color-split);
  padding-top: 12px;
  margin-top: 4px;
}

.todo-time {
  color: var(--lihua-text-color-secondary);
  font-size: var(--lihua-font-size-sm);
}

.todo-pagination {
  margin-top: 24px;
  text-align: right;
}
</style>
