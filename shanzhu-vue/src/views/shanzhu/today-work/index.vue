<template>
  <div class="shanzhu-today-work-page">
    <a-flex :gap="16" vertical>
      <!-- 页面标题 -->
      <a-card :bordered="false" class="today-work-header">
        <a-flex justify="space-between" align="center" wrap="wrap" :gap="12">
          <div>
            <a-typography-title :level="3" class="page-title">今日工作台</a-typography-title>
            <a-typography-text type="secondary">{{ todayDate }}</a-typography-text>
          </div>
          <a-space>
            <a-button @click="refreshData">
              <template #icon>
                <ReloadOutlined/>
              </template>
              刷新
            </a-button>
            <a-button @click="router.push('/shanzhu/todo')">
              <template #icon>
                <InboxOutlined/>
              </template>
              收集箱
            </a-button>
            <a-dropdown>
              <a-button>
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
                    一键完成全部Todo ({{ pendingTodos.length }})
                  </a-menu-item>
                  <a-menu-divider/>
                  <a-menu-item @click="openQuickAddTodo">
                    <PlusOutlined/>
                    快速添加 Todo (N)
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
            <a-button type="primary" @click="openQuickAddTodo">
              <template #icon>
                <PlusOutlined/>
              </template>
              快速添加
            </a-button>
          </a-space>
        </a-flex>
      </a-card>

      <!-- 概览统计 -->
      <a-row :gutter="[16, 16]">
        <a-col :xs="24" :sm="12" :lg="6">
          <a-card :bordered="false">
            <a-statistic
                title="今日任务"
                :value="overview.taskCompletedCount || 0"
                :suffix="`/ ${overview.taskTotalCount || 0}`"
                :value-style="{ color: '#1890ff' }"
            />
            <a-progress
                :percent="calculatePercent(overview.taskCompletedCount, overview.taskTotalCount)"
                size="small"
                :show-info="false"
            />
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :lg="6">
          <a-card :bordered="false">
            <a-statistic
                title="今日习惯"
                :value="overview.habitCheckedCount || 0"
                :suffix="`/ ${overview.habitTotalCount || 0}`"
                :value-style="{ color: '#52c41a' }"
            />
            <a-progress
                :percent="calculatePercent(overview.habitCheckedCount, overview.habitTotalCount)"
                size="small"
                :show-info="false"
                status="success"
            />
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :lg="6">
          <a-card :bordered="false">
            <a-statistic
                title="今日Todo"
                :value="overview.todoCompletedCount || 0"
                :suffix="`/ ${overview.todoTotalCount || 0}`"
                :value-style="{ color: '#fa8c16' }"
            />
            <a-progress
                :percent="calculatePercent(overview.todoCompletedCount, overview.todoTotalCount)"
                size="small"
                :show-info="false"
                status="warning"
            />
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :lg="6">
          <a-card :bordered="false">
            <a-statistic
                title="今日完成度"
                :value="todayCompletionRate"
                suffix="%"
                :value-style="{ color: todayCompletionRate >= 80 ? '#52c41a' : todayCompletionRate >= 50 ? '#fa8c16' : '#f5222d' }"
            />
            <a-progress
                :percent="todayCompletionRate"
                size="small"
                :show-info="false"
                :status="todayCompletionRate >= 80 ? 'success' : todayCompletionRate >= 50 ? 'normal' : 'exception'"
            />
          </a-card>
        </a-col>
      </a-row>

      <!-- 今日任务 -->
      <a-card :bordered="false" class="today-section-card">
        <template #title>
          <a-flex align="center" :gap="8">
            <CheckSquareOutlined/>
            <span>今日任务</span>
            <a-tag color="blue">{{ tasks.length }}</a-tag>
          </a-flex>
        </template>
        <a-spin :spinning="loading">
          <a-empty v-if="tasks.length === 0" description="今日暂无任务">
            <a-button type="primary" size="small" @click="router.push('/shanzhu/task')">
              前往任务中心
            </a-button>
          </a-empty>
          <a-list v-else :data-source="tasks" :split="false">
            <template #renderItem="{ item }">
              <a-list-item class="today-list-item" :class="{ 'item-completed': item.status === 'completed' }">
                <a-flex justify="space-between" align="center" :gap="12" style="width: 100%">
                  <a-flex align="center" :gap="12" style="flex: 1; min-width: 0">
                    <a-checkbox
                        :checked="item.status === 'completed'"
                        @change="() => toggleTaskComplete(item)"
                    />
                    <div style="flex: 1; min-width: 0">
                      <div class="item-title" :class="{ 'title-completed': item.status === 'completed' }">
                        {{ item.title }}
                      </div>
                      <div class="item-meta">
                        <span v-if="item.goalTitle">目标：{{ item.goalTitle }}</span>
                        <span v-if="item.estimatedMinutes">预计 {{ item.estimatedMinutes }} 分钟</span>
                      </div>
                    </div>
                  </a-flex>
                  <a-space>
                    <a-tag v-if="item.priority" :color="getPriorityColor(item.priority)">
                      {{ getPriorityLabel(item.priority) }}
                    </a-tag>
                    <a-dropdown>
                      <a-button type="text" size="small">
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
                  </a-space>
                </a-flex>
              </a-list-item>
            </template>
          </a-list>
        </a-spin>
      </a-card>

      <!-- 今日习惯 -->
      <a-card :bordered="false" class="today-section-card">
        <template #title>
          <a-flex align="center" :gap="8">
            <CalendarOutlined/>
            <span>今日习惯</span>
            <a-tag color="green">{{ habits.length }}</a-tag>
          </a-flex>
        </template>
        <a-spin :spinning="loading">
          <a-empty v-if="habits.length === 0" description="今日暂无习惯打卡">
            <a-button type="primary" size="small" @click="router.push('/shanzhu/habit')">
              前往习惯打卡
            </a-button>
          </a-empty>
          <a-list v-else :data-source="habits" :split="false">
            <template #renderItem="{ item }">
              <a-list-item class="today-list-item" :class="{ 'item-completed': item.todayChecked }">
                <a-flex justify="space-between" align="center" :gap="12" style="width: 100%">
                  <a-flex align="center" :gap="12" style="flex: 1; min-width: 0">
                    <a-checkbox
                        :checked="item.todayChecked"
                        @change="() => toggleHabitCheckin(item)"
                    />
                    <div style="flex: 1; min-width: 0">
                      <div class="item-title" :class="{ 'title-completed': item.todayChecked }">
                        {{ item.title }}
                      </div>
                      <div class="item-meta">
                        <span v-if="item.goalTitle">目标：{{ item.goalTitle }}</span>
                        <span v-if="item.targetValue">目标值：{{ item.targetValue }}{{ item.unit || '' }}</span>
                      </div>
                    </div>
                  </a-flex>
                  <a-space>
                    <a-tag :color="item.todayChecked ? 'success' : 'default'">
                      {{ item.todayChecked ? '已打卡' : '待打卡' }}
                    </a-tag>
                  </a-space>
                </a-flex>
              </a-list-item>
            </template>
          </a-list>
        </a-spin>
      </a-card>

      <!-- 今日Todo -->
      <a-card :bordered="false" class="today-section-card">
        <template #title>
          <a-flex align="center" :gap="8">
            <InboxOutlined/>
            <span>今日Todo</span>
            <a-tag color="orange">{{ todos.length }}</a-tag>
          </a-flex>
        </template>
        <a-spin :spinning="loading">
          <a-empty v-if="todos.length === 0" description="今日暂无Todo">
            <a-button type="primary" size="small" @click="openQuickAddTodo">
              快速添加 Todo
            </a-button>
          </a-empty>
          <a-list v-else :data-source="todos" :split="false">
            <template #renderItem="{ item }">
              <a-list-item class="today-list-item" :class="{ 'item-completed': item.status === 'done' }">
                <a-flex justify="space-between" align="center" :gap="12" style="width: 100%">
                  <a-flex align="center" :gap="12" style="flex: 1; min-width: 0">
                    <a-checkbox
                        :checked="item.status === 'done'"
                        @change="() => toggleTodoComplete(item)"
                    />
                    <div style="flex: 1; min-width: 0">
                      <div class="item-title" :class="{ 'title-completed': item.status === 'done' }">
                        {{ item.title }}
                      </div>
                      <div class="item-meta">
                        <span v-if="item.goalTitle">目标：{{ item.goalTitle }}</span>
                        <span v-if="item.estimatedMinutes">预计 {{ item.estimatedMinutes }} 分钟</span>
                      </div>
                    </div>
                  </a-flex>
                  <a-space>
                    <a-tag v-if="item.priority" :color="getPriorityColor(item.priority)">
                      {{ getPriorityLabel(item.priority) }}
                    </a-tag>
                    <a-dropdown>
                      <a-button type="text" size="small">
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
                  </a-space>
                </a-flex>
              </a-list-item>
            </template>
          </a-list>
        </a-spin>
      </a-card>
    </a-flex>

    <!-- 快速添加Todo弹窗 -->
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
  width: 100%;
}

.today-work-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.today-work-header .page-title,
.today-work-header .ant-typography-secondary {
  color: #fff;
}

.today-section-card {
  margin-bottom: 8px;
}

.today-list-item {
  padding: 12px 16px;
  border-radius: 8px;
  transition: background-color 0.2s;
}

.today-list-item:hover {
  background-color: #f5f5f5;
}

.item-completed {
  opacity: 0.7;
}

.item-title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
}

.title-completed {
  text-decoration: line-through;
  color: var(--lihua-text-color-secondary);
}

.item-meta {
  color: var(--lihua-text-color-secondary);
  font-size: var(--lihua-font-size-sm);
}

.item-meta span {
  margin-right: 12px;
}
</style>
