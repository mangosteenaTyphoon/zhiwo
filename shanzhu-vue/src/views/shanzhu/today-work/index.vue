<template>
  <div class="shanzhu-today-work-page">
    <div class="today-hero">
      <div class="today-hero-main">
        <div class="today-eyebrow">Today Workspace</div>
        <h2 class="today-page-title">今日工作台</h2>
        <p class="today-page-desc">{{ todayDate }} · 聚合今天要推进的任务、习惯和 Todo。</p>
      </div>
      <div class="today-hero-actions">
        <a-button class="today-secondary-btn" shape="round" @click="refreshData">
          <template #icon>
            <ReloadOutlined/>
          </template>
          刷新
        </a-button>
        <a-button class="today-secondary-btn" shape="round" @click="router.push('/shanzhu/todo')">
          <template #icon>
            <InboxOutlined/>
          </template>
          收集箱
        </a-button>
        <a-dropdown>
          <a-button class="today-secondary-btn" shape="round">
            <template #icon>
              <ThunderboltOutlined/>
            </template>
            快捷操作
          </a-button>
          <template #overlay>
            <a-menu>
              <a-menu-item :disabled="pendingHabits.length === 0" @click="confirmCheckinAllHabits">
                <CalendarOutlined/>
                一键打卡全部习惯 ({{ pendingHabits.length }})
              </a-menu-item>
              <a-menu-item :disabled="pendingTodos.length === 0" @click="confirmCompleteAllTodos">
                <CheckSquareOutlined/>
                一键完成全部 Todo ({{ pendingTodos.length }})
              </a-menu-item>
              <a-menu-divider/>
              <a-menu-item @click="openQuickAddTodo">
                <PlusOutlined/>
                快速添加 Todo (N)
              </a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
        <a-button type="primary" shape="round" size="large" @click="openQuickAddTodo">
          <template #icon>
            <PlusOutlined/>
          </template>
          快速添加
        </a-button>
      </div>
    </div>

    <div class="today-overview">
      <div class="today-focus-card">
        <div class="today-focus-ring" :class="{ 'today-focus-ring-low': todayCompletionRate < 50 }">
          <span>{{ todayCompletionRate }}%</span>
          <small>今日完成度</small>
        </div>
        <div class="today-focus-info">
          <h3>今天的执行进度</h3>
          <p>先清空今日 Todo，再处理高优任务，最后补齐习惯打卡。</p>
          <a-progress
              :percent="todayCompletionRate"
              :show-info="false"
              :status="todayCompletionRate >= 80 ? 'success' : todayCompletionRate >= 50 ? 'normal' : 'exception'"
          />
        </div>
      </div>

      <div class="today-stat-grid">
        <div class="today-stat-card today-stat-blue">
          <span class="today-stat-label">今日任务</span>
          <strong>{{ overview.taskCompletedCount || 0 }}/{{ overview.taskTotalCount || 0 }}</strong>
          <a-progress
              :percent="calculatePercent(overview.taskCompletedCount, overview.taskTotalCount)"
              size="small"
              :show-info="false"
          />
        </div>
        <div class="today-stat-card today-stat-green">
          <span class="today-stat-label">今日习惯</span>
          <strong>{{ overview.habitCheckedCount || 0 }}/{{ overview.habitTotalCount || 0 }}</strong>
          <a-progress
              :percent="calculatePercent(overview.habitCheckedCount, overview.habitTotalCount)"
              size="small"
              :show-info="false"
              status="success"
          />
        </div>
        <div class="today-stat-card today-stat-orange">
          <span class="today-stat-label">今日 Todo</span>
          <strong>{{ overview.todoCompletedCount || 0 }}/{{ overview.todoTotalCount || 0 }}</strong>
          <a-progress
              :percent="calculatePercent(overview.todoCompletedCount, overview.todoTotalCount)"
              size="small"
              :show-info="false"
              status="warning"
          />
        </div>
      </div>
    </div>

    <div class="today-board">
      <a-card :bordered="false" class="today-section-card today-task-card">
        <template #title>
          <div class="today-section-title">
            <span class="today-section-icon">✅</span>
            <span>今日任务</span>
            <em>{{ tasks.length }}</em>
          </div>
        </template>
        <template #extra>
          <a-button type="link" size="small" @click="router.push('/shanzhu/task')">任务中心</a-button>
        </template>
        <a-spin :spinning="loading">
          <a-empty v-if="tasks.length === 0" description="今日暂无任务">
            <a-button type="primary" shape="round" size="small" @click="router.push('/shanzhu/task')">
              前往任务中心
            </a-button>
          </a-empty>
          <div v-else class="today-list">
            <div
                v-for="item in tasks"
                :key="item.id"
                class="today-list-item"
                :class="{ 'item-completed': item.status === 'completed' }"
            >
              <a-checkbox
                  class="today-checkbox"
                  :checked="item.status === 'completed'"
                  @change="() => toggleTaskComplete(item)"
              />
              <div class="today-item-main">
                <div class="item-title" :class="{ 'title-completed': item.status === 'completed' }">
                  {{ item.title }}
                </div>
                <div class="item-meta">
                  <span v-if="item.goalTitle">🎯 {{ item.goalTitle }}</span>
                  <span v-if="item.estimatedMinutes">⏱ {{ item.estimatedMinutes }} 分钟</span>
                </div>
              </div>
              <div class="today-item-actions">
                <span v-if="item.priority" class="today-priority-chip" :class="'today-priority-' + item.priority">
                  {{ getPriorityLabel(item.priority) }}
                </span>
                <a-dropdown>
                  <a-button type="text" size="small" class="today-more-btn">
                    <MoreOutlined/>
                  </a-button>
                  <template #overlay>
                    <a-menu>
                      <a-menu-item @click="openTaskDetail(item)">
                        <EyeOutlined/>
                        查看详情
                      </a-menu-item>
                    </a-menu>
                  </template>
                </a-dropdown>
              </div>
            </div>
          </div>
        </a-spin>
      </a-card>

      <a-card :bordered="false" class="today-section-card today-habit-card">
        <template #title>
          <div class="today-section-title">
            <span class="today-section-icon">🌱</span>
            <span>今日习惯</span>
            <em>{{ habits.length }}</em>
          </div>
        </template>
        <template #extra>
          <a-button type="link" size="small" @click="router.push('/shanzhu/habit')">习惯打卡</a-button>
        </template>
        <a-spin :spinning="loading">
          <a-empty v-if="habits.length === 0" description="今日暂无习惯打卡">
            <a-button type="primary" shape="round" size="small" @click="router.push('/shanzhu/habit')">
              前往习惯打卡
            </a-button>
          </a-empty>
          <div v-else class="today-list">
            <div
                v-for="item in habits"
                :key="item.id"
                class="today-list-item"
                :class="{ 'item-completed': item.todayChecked }"
            >
              <a-checkbox
                  class="today-checkbox"
                  :checked="item.todayChecked"
                  @change="() => toggleHabitCheckin(item)"
              />
              <div class="today-item-main">
                <div class="item-title" :class="{ 'title-completed': item.todayChecked }">
                  {{ item.title }}
                </div>
                <div class="item-meta">
                  <span v-if="item.goalTitle">🎯 {{ item.goalTitle }}</span>
                  <span v-if="item.targetValue">目标值 {{ item.targetValue }}{{ item.unit || '' }}</span>
                </div>
              </div>
              <div class="today-item-actions">
                <span class="today-status-chip" :class="{ 'today-status-done': item.todayChecked }">
                  {{ item.todayChecked ? '已打卡' : '待打卡' }}
                </span>
              </div>
            </div>
          </div>
        </a-spin>
      </a-card>

      <a-card :bordered="false" class="today-section-card today-todo-card">
        <template #title>
          <div class="today-section-title">
            <span class="today-section-icon">📥</span>
            <span>今日 Todo</span>
            <em>{{ todos.length }}</em>
          </div>
        </template>
        <template #extra>
          <a-button type="link" size="small" @click="openQuickAddTodo">快速添加</a-button>
        </template>
        <a-spin :spinning="loading">
          <a-empty v-if="todos.length === 0" description="今日暂无 Todo">
            <a-button type="primary" shape="round" size="small" @click="openQuickAddTodo">
              快速添加 Todo
            </a-button>
          </a-empty>
          <div v-else class="today-list">
            <div
                v-for="item in todos"
                :key="item.id"
                class="today-list-item"
                :class="{ 'item-completed': item.status === 'done' }"
            >
              <a-checkbox
                  class="today-checkbox"
                  :checked="item.status === 'done'"
                  @change="() => toggleTodoComplete(item)"
              />
              <div class="today-item-main">
                <div class="item-title" :class="{ 'title-completed': item.status === 'done' }">
                  {{ item.title }}
                </div>
                <div class="item-meta">
                  <span v-if="item.goalTitle">🎯 {{ item.goalTitle }}</span>
                  <span v-if="item.estimatedMinutes">⏱ {{ item.estimatedMinutes }} 分钟</span>
                </div>
              </div>
              <div class="today-item-actions">
                <span v-if="item.priority" class="today-priority-chip" :class="'today-priority-' + item.priority">
                  {{ getPriorityLabel(item.priority) }}
                </span>
                <a-dropdown>
                  <a-button type="text" size="small" class="today-more-btn">
                    <MoreOutlined/>
                  </a-button>
                  <template #overlay>
                    <a-menu>
                      <a-menu-item @click="openEditTodoModal(item)">
                        <EditOutlined/>
                        编辑
                      </a-menu-item>
                      <a-menu-item v-if="item.status !== 'done'" @click="handleMoveToInbox(item)">
                        <InboxOutlined/>
                        移回收集箱
                      </a-menu-item>
                      <a-menu-item @click="handleConvertToTask(item)">
                        <CheckSquareOutlined/>
                        转为任务
                      </a-menu-item>
                      <a-menu-divider/>
                      <a-menu-item danger @click="confirmDeleteTodo(item)">
                        <DeleteOutlined/>
                        删除
                      </a-menu-item>
                    </a-menu>
                  </template>
                </a-dropdown>
              </div>
            </div>
          </div>
        </a-spin>
      </a-card>
    </div>

    <a-modal
        v-model:open="quickAddModal.open"
        :confirm-loading="quickAddModal.saveLoading"
        :title="quickAddModal.title"
        width="480px"
        :ok-text="editingTodoId ? '保 存' : '添 加'"
        cancel-text="取 消"
        @ok="editingTodoId ? handleEditTodo() : handleQuickAdd()"
    >
      <a-form ref="quickAddFormRef" :colon="false" :model="quickAddForm" :rules="quickAddRules">
        <a-form-item label="内容" name="title">
          <a-input v-model:value="quickAddForm.title" placeholder="想做什么？" :maxlength="120" show-count allow-clear/>
        </a-form-item>
        <a-form-item label="关联目标">
          <a-select
              v-model:value="quickAddForm.goalId"
              placeholder="请选择目标（可选）"
              allow-clear
              show-search
              :filter-option="filterGoalOption"
          >
            <a-select-option v-for="goal in goalOptions" :key="goal.id" :value="goal.id">
              {{ goal.title }}
            </a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import {computed, onMounted, onUnmounted, reactive, ref} from "vue";
import type {FormInstance, Rule} from "ant-design/vue/es/form";
import {message, Modal} from "ant-design-vue";
import {useRouter} from "vue-router";
import {
  CalendarOutlined,
  CheckSquareOutlined,
  DeleteOutlined,
  EditOutlined,
  EyeOutlined,
  InboxOutlined,
  MoreOutlined,
  PlusOutlined,
  ReloadOutlined,
  ThunderboltOutlined
} from "@ant-design/icons-vue";
import {queryGoalPage} from "@/api/shanzhu/goal/Goal.ts";
import type {ShanzhuGoalVO} from "@/api/shanzhu/goal/type/Goal.ts";
import type {BaseModalActiveType} from "@/api/global/Type.ts";
import {queryTodayWork} from "@/api/shanzhu/todayWork/TodayWork.ts";
import type {ShanzhuTodayWorkVO, TodayOverview} from "@/api/shanzhu/todayWork/type/TodayWork.ts";
import type {ShanzhuTaskVO} from "@/api/shanzhu/task/type/Task.ts";
import {updateTaskStatus} from "@/api/shanzhu/task/Task.ts";
import type {ShanzhuHabitTodayVO} from "@/api/shanzhu/habit/type/Habit.ts";
import {cancelHabitCheckin, saveHabitCheckin} from "@/api/shanzhu/habit/Habit.ts";
import type {ShanzhuTodoVO} from "@/api/shanzhu/todo/type/Todo.ts";
import {completeTodo, convertTodoToTask, deleteTodo, moveToInbox, saveTodo, uncompleteTodo} from "@/api/shanzhu/todo/Todo.ts";

const router = useRouter();
const loading = ref(false);
const todayWorkData = ref<ShanzhuTodayWorkVO>({});
const goalOptions = ref<ShanzhuGoalVO[]>([]);

const overview = computed<TodayOverview>(() => todayWorkData.value.overview || {});
const tasks = computed<ShanzhuTaskVO[]>(() => todayWorkData.value.tasks || []);
const habits = computed<ShanzhuHabitTodayVO[]>(() => todayWorkData.value.habits || []);
const todos = computed<ShanzhuTodoVO[]>(() => todayWorkData.value.todos || []);

const todayDate = computed(() => {
  const date = new Date();
  const weekDays = ["周日", "周一", "周二", "周三", "周四", "周五", "周六"];
  return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日 ${weekDays[date.getDay()]}`;
});

const todayCompletionRate = computed(() => {
  const total = (overview.value.taskTotalCount || 0) + (overview.value.habitTotalCount || 0) + (overview.value.todoTotalCount || 0);
  const completed = (overview.value.taskCompletedCount || 0) + (overview.value.habitCheckedCount || 0) + (overview.value.todoCompletedCount || 0);
  return total > 0 ? Math.round((completed / total) * 100) : 0;
});

const pendingHabits = computed(() => habits.value.filter(h => !h.todayChecked));
const pendingTodos = computed(() => todos.value.filter(t => t.status !== "done"));

const calculatePercent = (completed?: number, total?: number) => {
  const c = completed || 0;
  const t = total || 0;
  return t > 0 ? Math.round((c / t) * 100) : 0;
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
  try {
    const response = await queryGoalPage({pageNum: 1, pageSize: 100});
    if (response.code === 200) {
      goalOptions.value = response.data.records || [];
    }
  } catch (error) {
    // 忽略错误
  }
};

const refreshData = async () => {
  loading.value = true;
  try {
    const response = await queryTodayWork();
    if (response.code === 200) {
      todayWorkData.value = response.data || {};
    } else {
      message.error(response.msg || "加载失败");
    }
  } catch (error) {
    message.error("加载失败，请稍后重试");
  } finally {
    loading.value = false;
  }
};

const toggleTaskComplete = async (task: ShanzhuTaskVO) => {
  if (!task.id) {
    return;
  }

  const newStatus = task.status === "completed" ? "not_started" : "completed";
  const response = await updateTaskStatus({id: task.id, status: newStatus});
  if (response.code === 200) {
    message.success(newStatus === "completed" ? "任务已完成" : "已恢复任务");
    await refreshData();
  } else {
    message.error(response.msg || "操作失败");
  }
};

const toggleHabitCheckin = async (habit: ShanzhuHabitTodayVO) => {
  if (!habit.id) {
    return;
  }

  if (habit.todayChecked && habit.todayCheckinId) {
    // 取消打卡
    Modal.confirm({
      title: "确认取消今日打卡？",
      content: `取消后，习惯「${habit.title || '-'}」今日将恢复为未打卡。`,
      okText: "确认取消",
      cancelText: "关闭",
      okType: "danger",
      onOk: async () => {
        const response = await cancelHabitCheckin(habit.todayCheckinId || "");
        if (response.code === 200) {
          message.success("已取消打卡");
          await refreshData();
        } else {
          message.error(response.msg || "取消打卡失败");
        }
      }
    });
    return;
  }

  const response = await saveHabitCheckin({
    habitId: habit.id,
    checkinDate: getTodayString(),
    actualValue: habit.targetValue
  });
  if (response.code === 200) {
    message.success("打卡成功");
    await refreshData();
  } else {
    message.error(response.msg || "打卡失败");
  }
};

const toggleTodoComplete = async (todo: ShanzhuTodoVO) => {
  if (!todo.id) {
    return;
  }

  const isDone = todo.status === "done";
  const response = isDone ? await uncompleteTodo(todo.id) : await completeTodo(todo.id);
  if (response.code === 200) {
    message.success(isDone ? "已恢复" : "已完成");
    await refreshData();
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
    await refreshData();
  } else {
    message.error(response.msg || "操作失败");
  }
};

const handleConvertToTask = (todo: ShanzhuTodoVO) => {
  if (!todo.id) {
    return;
  }

  Modal.confirm({
    title: "确认转为任务？",
    content: `将「${todo.title || '-'}」转为正式任务。`,
    okText: "确认转换",
    cancelText: "取消",
    onOk: async () => {
      const response = await convertTodoToTask(todo.id || "");
      if (response.code === 200) {
        message.success("已转为任务");
        await refreshData();
      } else {
        message.error(response.msg || "转换失败");
      }
    }
  });
};

const openTaskDetail = (task: ShanzhuTaskVO) => {
  if (task.id) {
    // 跳转到任务中心，并传递任务ID参数（任务中心页面可扩展支持高亮或筛选）
    router.push({
      path: `/shanzhu/task`,
      query: {highlightTaskId: task.id}
    });
  }
};

const confirmCheckinAllHabits = () => {
  if (pendingHabits.value.length === 0) {
    return;
  }

  Modal.confirm({
    title: "一键打卡全部习惯",
    content: `将一键打卡 ${pendingHabits.value.length} 个习惯，确认继续？`,
    okText: "确认打卡",
    cancelText: "取消",
    onOk: async () => {
      let successCount = 0;
      let failCount = 0;

      for (const habit of pendingHabits.value) {
        if (!habit.id) {
          continue;
        }
        try {
          const response = await saveHabitCheckin({
            habitId: habit.id,
            checkinDate: getTodayString(),
            actualValue: habit.targetValue
          });
          if (response.code === 200) {
            successCount++;
          } else {
            failCount++;
          }
        } catch (error) {
          failCount++;
        }
      }

      if (successCount > 0) {
        message.success(`成功打卡 ${successCount} 个习惯`);
      }
      if (failCount > 0) {
        message.error(`${failCount} 个习惯打卡失败`);
      }
      await refreshData();
    }
  });
};

const confirmCompleteAllTodos = () => {
  if (pendingTodos.value.length === 0) {
    return;
  }

  Modal.confirm({
    title: "一键完成全部 Todo",
    content: `将一键完成 ${pendingTodos.value.length} 个 Todo，确认继续？`,
    okText: "确认完成",
    cancelText: "取消",
    onOk: async () => {
      let successCount = 0;
      let failCount = 0;

      for (const todo of pendingTodos.value) {
        if (!todo.id) {
          continue;
        }
        try {
          const response = await completeTodo(todo.id);
          if (response.code === 200) {
            successCount++;
          } else {
            failCount++;
          }
        } catch (error) {
          failCount++;
        }
      }

      if (successCount > 0) {
        message.success(`成功完成 ${successCount} 个 Todo`);
      }
      if (failCount > 0) {
        message.error(`${failCount} 个 Todo 完成失败`);
      }
      await refreshData();
    }
  });
};

const getTodayString = () => {
  const date = new Date();
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
};

// 快速添加
const quickAddFormRef = ref<FormInstance>();
const quickAddForm = ref({title: "", goalId: undefined as string | undefined});
const quickAddModal = reactive<BaseModalActiveType>({
  open: false,
  saveLoading: false,
  title: "快速添加 Todo"
});

const quickAddRules: Record<string, Rule[]> = {
  title: [{required: true, message: "请输入内容", trigger: "blur"}]
};

const openQuickAddTodo = () => {
  quickAddForm.value = {title: "", goalId: undefined};
  quickAddModal.title = "快速添加 Todo";
  quickAddModal.open = true;
};

const handleQuickAdd = async () => {
  await quickAddFormRef.value?.validate();

  quickAddModal.saveLoading = true;
  try {
    const response = await saveTodo({
      title: quickAddForm.value.title,
      goalId: quickAddForm.value.goalId,
      status: "today"
    });
    if (response.code === 200) {
      message.success("已添加");
      quickAddModal.open = false;
      await refreshData();
    } else {
      message.error(response.msg || "添加失败");
    }
  } finally {
    quickAddModal.saveLoading = false;
  }
};

// 编辑 Todo
const editingTodoId = ref<string>("");
const openEditTodoModal = (todo: ShanzhuTodoVO) => {
  editingTodoId.value = todo.id || "";
  quickAddForm.value = {
    title: todo.title || "",
    goalId: todo.goalId
  };
  quickAddModal.title = "编辑 Todo";
  quickAddModal.open = true;
};

const handleEditTodo = async () => {
  await quickAddFormRef.value?.validate();

  quickAddModal.saveLoading = true;
  try {
    const response = await saveTodo({
      id: editingTodoId.value,
      title: quickAddForm.value.title,
      goalId: quickAddForm.value.goalId,
      status: "today"
    });
    if (response.code === 200) {
      message.success("已保存");
      quickAddModal.open = false;
      editingTodoId.value = "";
      await refreshData();
    } else {
      message.error(response.msg || "保存失败");
    }
  } finally {
    quickAddModal.saveLoading = false;
  }
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
        await refreshData();
      } else {
        message.error(response.msg || "删除失败");
      }
    }
  });
};

const handleKeydown = (event: KeyboardEvent) => {
  // 按 N 键快速打开添加弹窗（不在输入框中时）
  if (event.key === "n" || event.key === "N") {
    const target = event.target as HTMLElement;
    if (target.tagName === "INPUT" || target.tagName === "TEXTAREA" || target.isContentEditable) {
      return;
    }
    event.preventDefault();
    openQuickAddTodo();
  }
};

onMounted(async () => {
  await loadGoalOptions();
  await refreshData();
  document.addEventListener("keydown", handleKeydown);
});

onUnmounted(() => {
  document.removeEventListener("keydown", handleKeydown);
});
</script>

<style scoped>
.shanzhu-today-work-page {
  max-width: 1360px;
  min-height: calc(100vh - 120px);
  margin: 0 auto;
  padding: 36px 48px 56px;
  overflow-x: hidden;
}

/* Header */
.today-hero {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 26px 28px;
  margin-bottom: 18px;
  overflow: hidden;
  border: 1px solid rgba(22, 119, 255, 0.08);
  border-radius: 26px;
  background:
    radial-gradient(circle at 16% 0%, rgba(22, 119, 255, 0.13), transparent 34%),
    radial-gradient(circle at 96% 18%, rgba(250, 173, 20, 0.13), transparent 28%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.98), rgba(247, 250, 255, 0.94));
  box-shadow: 0 18px 45px rgba(15, 35, 80, 0.06);
}

.today-hero-main {
  min-width: 0;
}

.today-eyebrow {
  margin-bottom: 8px;
  color: #1677ff;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.8px;
  text-transform: uppercase;
}

.today-page-title {
  margin: 0;
  color: rgba(0, 0, 0, 0.88);
  font-size: 30px;
  font-weight: 850;
  line-height: 1.2;
  letter-spacing: -0.7px;
}

.today-page-desc {
  margin: 8px 0 0;
  color: rgba(0, 0, 0, 0.48);
  font-size: 14px;
}

.today-hero-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
  flex-shrink: 0;
}

.today-secondary-btn {
  height: 38px;
  border-color: rgba(15, 35, 80, 0.08);
  background: rgba(255, 255, 255, 0.74);
  color: rgba(0, 0, 0, 0.68);
  font-weight: 650;
}

.today-hero-actions :deep(.ant-btn-primary) {
  min-width: 112px;
  height: 42px;
  box-shadow: 0 10px 24px rgba(22, 119, 255, 0.22);
}

/* Overview */
.today-overview {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(360px, 0.8fr);
  gap: 18px;
  margin-bottom: 18px;
}

.today-focus-card,
.today-stat-card,
.today-section-card {
  border: 1px solid rgba(15, 35, 80, 0.06);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 18px 45px rgba(15, 35, 80, 0.065), 0 1px 2px rgba(15, 35, 80, 0.04);
}

.today-focus-card {
  display: flex;
  align-items: center;
  gap: 22px;
  padding: 22px;
  background:
    radial-gradient(circle at 12% 0%, rgba(22, 119, 255, 0.10), transparent 30%),
    rgba(255, 255, 255, 0.94);
}

.today-focus-ring {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 112px;
  height: 112px;
  flex-shrink: 0;
  border-radius: 34px;
  background:
    radial-gradient(circle at 30% 20%, rgba(255, 255, 255, 0.88), transparent 34%),
    linear-gradient(135deg, rgba(22, 119, 255, 0.92), rgba(82, 196, 26, 0.82));
  box-shadow: 0 18px 34px rgba(22, 119, 255, 0.20);
  color: #fff;
}

.today-focus-ring-low {
  background:
    radial-gradient(circle at 30% 20%, rgba(255, 255, 255, 0.88), transparent 34%),
    linear-gradient(135deg, rgba(250, 140, 22, 0.92), rgba(255, 77, 79, 0.78));
}

.today-focus-ring span {
  font-size: 28px;
  font-weight: 900;
  line-height: 1;
}

.today-focus-ring small {
  margin-top: 8px;
  color: rgba(255, 255, 255, 0.84);
  font-size: 12px;
  font-weight: 700;
}

.today-focus-info {
  min-width: 0;
  flex: 1;
}

.today-focus-info h3 {
  margin: 0;
  color: rgba(0, 0, 0, 0.84);
  font-size: 18px;
  font-weight: 850;
}

.today-focus-info p {
  margin: 8px 0 14px;
  color: rgba(0, 0, 0, 0.48);
  font-size: 13px;
  line-height: 21px;
}

.today-focus-info :deep(.ant-progress-inner),
.today-stat-card :deep(.ant-progress-inner) {
  background: rgba(15, 35, 80, 0.06);
}

.today-stat-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.today-stat-card {
  padding: 18px;
}

.today-stat-label {
  display: block;
  margin-bottom: 9px;
  color: rgba(0, 0, 0, 0.44);
  font-size: 12px;
  font-weight: 750;
}

.today-stat-card strong {
  display: block;
  margin-bottom: 14px;
  color: rgba(0, 0, 0, 0.84);
  font-size: 24px;
  font-weight: 900;
  line-height: 1;
}

.today-stat-blue strong {
  color: #1677ff;
}

.today-stat-green strong {
  color: #389e0d;
}

.today-stat-orange strong {
  color: #d46b08;
}

/* Board */
.today-board {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
  align-items: start;
}

.today-section-card {
  overflow: hidden;
}

.today-section-card :deep(.ant-card-head) {
  min-height: 58px;
  border-bottom-color: rgba(15, 35, 80, 0.06);
}

.today-section-card :deep(.ant-card-head-title) {
  min-width: 0;
}

.today-section-card :deep(.ant-card-body) {
  padding: 0;
}

.today-section-card :deep(.ant-card-extra .ant-btn) {
  border-radius: 999px;
  color: rgba(0, 0, 0, 0.48);
  font-weight: 650;
}

.today-section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  color: rgba(0, 0, 0, 0.84);
  font-size: 16px;
  font-weight: 850;
}

.today-section-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  border-radius: 10px;
  background: rgba(22, 119, 255, 0.08);
}

.today-section-title em {
  min-width: 22px;
  padding: 1px 8px;
  border-radius: 999px;
  background: rgba(15, 35, 80, 0.06);
  color: rgba(0, 0, 0, 0.48);
  font-size: 12px;
  font-style: normal;
  font-weight: 800;
  text-align: center;
}

.today-list {
  display: flex;
  flex-direction: column;
}

.today-list-item {
  position: relative;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px 18px;
  overflow: hidden;
  border-bottom: 1px solid rgba(15, 35, 80, 0.055);
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.90), rgba(248, 251, 255, 0.72));
  transition: background-color 0.18s ease, transform 0.18s ease, box-shadow 0.18s ease;
}

.today-list-item:last-child {
  border-bottom: none;
}

.today-list-item:hover {
  z-index: 2;
  background: #fff;
  box-shadow: 0 12px 28px rgba(15, 35, 80, 0.07);
  transform: translateY(-1px);
}

.today-checkbox {
  margin-top: 2px;
  flex-shrink: 0;
}

.today-item-main {
  min-width: 0;
  flex: 1;
}

.item-completed {
  background:
    linear-gradient(135deg, rgba(250, 255, 250, 0.88), rgba(246, 255, 237, 0.66));
  opacity: 0.78;
}

.item-title {
  display: -webkit-box;
  margin-bottom: 5px;
  overflow: hidden;
  color: rgba(0, 0, 0, 0.84);
  font-size: 14px;
  font-weight: 720;
  line-height: 22px;
  word-break: break-word;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.title-completed {
  color: rgba(0, 0, 0, 0.38);
  text-decoration: line-through;
}

.item-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  max-height: 54px;
  overflow: hidden;
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
  font-weight: 650;
}

.item-meta span {
  max-width: 180px;
  padding: 3px 8px;
  overflow: hidden;
  border-radius: 999px;
  background: rgba(15, 35, 80, 0.045);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.today-item-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.today-priority-chip,
.today-status-chip {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  padding: 3px 9px;
  border-radius: 999px;
  background: rgba(15, 35, 80, 0.055);
  color: rgba(0, 0, 0, 0.54);
  font-size: 12px;
  font-weight: 750;
  white-space: nowrap;
}

.today-priority-1 {
  background: rgba(255, 77, 79, 0.10);
  color: #cf1322;
}

.today-priority-2 {
  background: rgba(250, 173, 20, 0.12);
  color: #d48806;
}

.today-priority-3 {
  background: rgba(22, 119, 255, 0.08);
  color: #1677ff;
}

.today-status-done {
  background: rgba(82, 196, 26, 0.12);
  color: #389e0d;
}

.today-more-btn {
  border-radius: 999px;
  color: rgba(0, 0, 0, 0.45);
}

.today-more-btn:hover {
  background: rgba(22, 119, 255, 0.08);
  color: #1677ff;
}

.today-section-card :deep(.ant-empty) {
  padding: 56px 18px;
}

@media (max-width: 1200px) {
  .shanzhu-today-work-page {
    padding: 28px 28px 48px;
  }

  .today-overview,
  .today-board {
    grid-template-columns: 1fr;
  }

  .today-stat-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 820px) {
  .today-hero {
    flex-direction: column;
    align-items: stretch;
  }

  .today-hero-actions {
    justify-content: flex-start;
  }

  .today-focus-card {
    flex-direction: column;
    align-items: flex-start;
  }

  .today-stat-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .shanzhu-today-work-page {
    padding: 18px 14px 36px;
  }

  .today-hero {
    padding: 22px 20px;
    border-radius: 22px;
  }

  .today-page-title {
    font-size: 26px;
  }

  .today-hero-actions :deep(.ant-btn) {
    flex: 1;
  }

  .today-focus-ring {
    width: 96px;
    height: 96px;
    border-radius: 28px;
  }

  .today-focus-ring span {
    font-size: 24px;
  }

  .today-list-item {
    gap: 10px;
    padding: 15px 14px;
  }

  .today-item-actions {
    flex-direction: column;
    align-items: flex-end;
  }

  .today-priority-chip,
  .today-status-chip {
    max-width: 72px;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}
</style>
