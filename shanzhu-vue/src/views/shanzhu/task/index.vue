<template>
  <div class="shanzhu-task-page">
    <a-flex :gap="16" vertical>
      <a-card :bordered="false" class="task-filter-card">
        <a-form :colon="false">
          <a-row :gutter="[16, 12]">
            <a-col :xs="24" :sm="12" :lg="6">
              <a-form-item label="关键词">
                <a-input v-model:value="taskQuery.keyword" placeholder="搜索任务标题" allow-clear/>
              </a-form-item>
            </a-col>
            <a-col :xs="24" :sm="12" :lg="6">
              <a-form-item label="目标">
                <a-select
                    v-model:value="taskQuery.goalId"
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
              <a-form-item label="分类">
                <shanzhu-category-select v-model:value="taskQuery.categoryId"/>
              </a-form-item>
            </a-col>
            <a-col :xs="24" :sm="12" :lg="6">
              <a-form-item label="标签">
                <shanzhu-tag-select v-model:value="taskQuery.tagIds" tag-type="task" placeholder="请选择任务标签"/>
              </a-form-item>
            </a-col>
            <a-col :xs="24" :sm="12" :lg="6">
              <a-form-item label="状态">
                <a-select v-model:value="taskQuery.status" placeholder="请选择状态" allow-clear>
                  <a-select-option v-for="item in taskStatusOptions" :key="item.value" :value="item.value">
                    {{ item.label }}
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :xs="24" :sm="12" :lg="6">
              <a-form-item label="优先级">
                <a-select v-model:value="taskQuery.priority" placeholder="请选择优先级" allow-clear>
                  <a-select-option v-for="item in taskPriorityOptions" :key="item.value" :value="item.value">
                    {{ item.label }}
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :xs="24" :sm="12" :lg="6">
              <a-form-item label="计划日期">
                <a-date-picker v-model:value="taskQuery.plannedDate" value-format="YYYY-MM-DD" style="width: 100%" placeholder="请选择计划日期"/>
              </a-form-item>
            </a-col>
            <a-col :xs="24" :sm="12" :lg="6">
              <a-form-item label="任务范围">
                <a-segmented v-model:value="taskQuery.queryType" :options="queryTypeOptions"/>
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

      <a-card :bordered="false" class="task-list-card">
        <template #title>
          <a-flex justify="space-between" align="center" wrap="wrap" :gap="12">
            <div>
              <a-typography-title :level="4" class="task-list-title">任务中心</a-typography-title>
              <a-typography-text type="secondary">集中查看今日、逾期和全部任务，快速推进日常执行</a-typography-text>
            </div>
            <a-button type="primary" @click="openCreateTaskModal">
              <template #icon>
                <PlusOutlined/>
              </template>
              新建任务
            </a-button>
          </a-flex>
        </template>

        <a-spin :spinning="tableLoading">
          <a-empty v-if="taskList.length === 0" description="暂无任务"/>
          <template v-else>
            <a-list :data-source="taskList" item-layout="vertical">
              <template #renderItem="{ item }">
                <a-list-item class="task-list-item">
                  <a-flex justify="space-between" align="flex-start" wrap="wrap" :gap="12">
                    <div class="task-main">
                      <a-space size="small" wrap>
                        <a-tag :color="getTaskStatusOption(item.status).color">
                          {{ getTaskStatusOption(item.status).label }}
                        </a-tag>
                        <a-tag :color="getTaskPriorityOption(item.priority).color">
                          {{ getTaskPriorityOption(item.priority).label }}
                        </a-tag>
                        <a-tag :color="item.categoryColor || 'blue'">
                          {{ item.categoryName || '未分类' }}
                        </a-tag>
                        <a-tag v-for="tag in item.tags" :key="tag.id" :color="tag.color || 'blue'">
                          {{ tag.name }}
                        </a-tag>
                      </a-space>

                      <a-typography-title :level="5" class="task-title">
                        {{ item.title }}
                      </a-typography-title>
                      <a-typography-paragraph class="task-description" :ellipsis="{ rows: 2 }">
                        {{ item.description || '暂无任务说明' }}
                      </a-typography-paragraph>

                      <a-space size="small" wrap class="task-meta">
                        <span>目标：{{ item.goalTitle || '-' }}</span>
                        <span>计划：{{ item.plannedDate || '-' }}</span>
                        <span>截止：{{ item.deadline || '-' }}</span>
                        <span>预计：{{ item.estimatedMinutes || 0 }} 分钟</span>
                        <span>实际：{{ item.actualMinutes || 0 }} 分钟</span>
                      </a-space>
                    </div>

                    <a-space size="small" wrap>
                      <a-button
                          v-if="item.status !== completedStatus"
                          type="primary"
                          size="small"
                          ghost
                          @click="handleQuickComplete(item)"
                      >
                        完成
                      </a-button>
                      <a-button type="link" size="small" @click="openGoalDetail(item.goalId)">目标详情</a-button>
                      <a-button type="link" size="small" @click="openEditTaskModal(item)">编辑</a-button>
                      <a-button type="link" size="small" danger @click="confirmDeleteTask(item)">删除</a-button>
                    </a-space>
                  </a-flex>
                </a-list-item>
              </template>
            </a-list>

            <a-flex class="task-pagination" justify="end">
              <a-pagination
                  v-model:current="taskPagination.current"
                  v-model:page-size="taskPagination.pageSize"
                  :total="taskPagination.total"
                  show-size-changer
                  show-quick-jumper
                  :show-total="total => `共 ${total} 条`"
                  @change="handlePageChange"
                  @showSizeChange="handlePageChange"
              />
            </a-flex>
          </template>
        </a-spin>
      </a-card>
    </a-flex>

    <a-modal
        v-model:open="taskModal.open"
        :title="taskModal.title"
        :confirm-loading="taskModal.saveLoading"
        width="720px"
        ok-text="保 存"
        cancel-text="取 消"
        @ok="handleSaveTask"
    >
      <a-form ref="taskFormRef" :colon="false" :model="taskForm" :rules="taskRules" :label-col="{ span: 4 }">
        <a-form-item label="所属目标" name="goalId">
          <a-select
              v-model:value="taskForm.goalId"
              placeholder="请选择目标"
              show-search
              :filter-option="filterGoalOption"
          >
            <a-select-option v-for="goal in goalOptions" :key="goal.id" :value="goal.id">
              {{ goal.title }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="任务标题" name="title">
          <a-input v-model:value="taskForm.title" placeholder="例如：完成今日核心任务" :maxlength="120" show-count allow-clear/>
        </a-form-item>
        <a-form-item label="状态">
          <a-radio-group v-model:value="taskForm.status">
            <a-radio v-for="item in taskStatusOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-radio>
          </a-radio-group>
        </a-form-item>
        <a-row>
          <a-col :span="8">
            <a-form-item label="优先级" :label-col="{ span: 10 }">
              <a-select v-model:value="taskForm.priority" style="width: 100%">
                <a-select-option v-for="item in taskPriorityOptions" :key="item.value" :value="item.value">
                  {{ item.label }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="预计分钟" :label-col="{ span: 10 }">
              <a-input-number v-model:value="taskForm.estimatedMinutes" :min="0" :max="99999" style="width: 100%"/>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="实际分钟" :label-col="{ span: 10 }">
              <a-input-number v-model:value="taskForm.actualMinutes" :min="0" :max="99999" style="width: 100%"/>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row>
          <a-col :span="12">
            <a-form-item label="计划日期" :label-col="{ span: 8 }">
              <a-date-picker v-model:value="taskForm.plannedDate" value-format="YYYY-MM-DD" style="width: 100%" placeholder="请选择计划日期"/>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="截止时间" :label-col="{ span: 8 }">
              <a-date-picker
                  v-model:value="taskForm.deadline"
                  show-time
                  value-format="YYYY-MM-DD HH:mm:ss"
                  style="width: 100%"
                  placeholder="请选择截止时间"
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="标签">
          <shanzhu-tag-select v-model:value="taskForm.tagIds" tag-type="task" placeholder="请选择任务标签"/>
        </a-form-item>
        <a-form-item label="任务说明">
          <a-textarea v-model:value="taskForm.description" placeholder="描述这个任务的执行方式或完成标准" :rows="4" :maxlength="500" show-count allow-clear/>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import {onMounted, reactive, ref} from "vue";
import {useRouter} from "vue-router";
import type {FormInstance, Rule} from "ant-design-vue/es/form";
import {message, Modal} from "ant-design-vue";
import {PlusOutlined, RedoOutlined, SearchOutlined} from "@ant-design/icons-vue";
import ShanzhuCategorySelect from "@/components/shanzhu-category-select/index.vue";
import ShanzhuTagSelect from "@/components/shanzhu-tag-select/index.vue";
import {queryGoalPage} from "@/api/shanzhu/goal/Goal.ts";
import type {GoalStatusOption, ShanzhuGoalVO} from "@/api/shanzhu/goal/type/Goal.ts";
import {deleteTask, queryTaskPage, saveTask, updateTaskStatus} from "@/api/shanzhu/task/Task.ts";
import type {ShanzhuTask, ShanzhuTaskQuery, ShanzhuTaskVO} from "@/api/shanzhu/task/type/Task.ts";
import type {BaseModalActiveType} from "@/api/global/Type.ts";

interface TaskPriorityOption {
  label: string;
  value: number;
  color: string;
}

interface QueryTypeOption {
  label: string;
  value: string;
}

const router = useRouter();
const completedStatus = "completed";

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

const queryTypeOptions: QueryTypeOption[] = [
  {label: "全部", value: ""},
  {label: "今日", value: "today"},
  {label: "逾期", value: "overdue"}
];

const defaultTaskQuery = (): ShanzhuTaskQuery => ({
  queryType: "",
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
const taskPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
});
const goalOptions = ref<ShanzhuGoalVO[]>([]);
const tableLoading = ref(false);
const goalLoading = ref(false);
const taskFormRef = ref<FormInstance>();
const taskForm = ref<ShanzhuTask>(defaultTaskForm());

const taskModal = reactive<BaseModalActiveType>({
  open: false,
  saveLoading: false,
  title: "新建任务"
});

const taskRules: Record<string, Rule[]> = {
  goalId: [{required: true, message: "请选择目标", trigger: "change"}],
  title: [{required: true, message: "请输入任务标题", trigger: "blur"}]
};

const getTaskStatusOption = (status?: string) => {
  return taskStatusOptions.find(item => item.value === status) || taskStatusOptions[0];
};

const getTaskPriorityOption = (priority?: number) => {
  return taskPriorityOptions.find(item => item.value === priority) || taskPriorityOptions[1];
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
      pageSize: 50
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

const loadTaskList = async (pageNum = taskPagination.current, pageSize = taskPagination.pageSize) => {
  tableLoading.value = true;
  try {
    const response = await queryTaskPage({
      ...taskQuery.value,
      pageNum,
      pageSize
    });
    if (response.code === 200) {
      taskList.value = response.data.records || [];
      taskPagination.current = response.data.current || pageNum;
      taskPagination.pageSize = response.data.size || pageSize;
      taskPagination.total = response.data.total || 0;
      taskQuery.value.pageNum = taskPagination.current;
      taskQuery.value.pageSize = taskPagination.pageSize;
    } else {
      message.error(response.msg || "任务列表加载失败");
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

const resetQuery = () => {
  taskQuery.value = defaultTaskQuery();
  taskPagination.current = 1;
  taskPagination.pageSize = 10;
  taskPagination.total = 0;
  loadTaskList(1, taskPagination.pageSize);
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
      await loadTaskList();
    } else {
      message.error(response.msg || "保存失败");
    }
  } finally {
    taskModal.saveLoading = false;
  }
};

const handleQuickComplete = async (task: ShanzhuTaskVO) => {
  if (!task.id) {
    return;
  }

  const response = await updateTaskStatus({
    id: task.id,
    status: completedStatus
  });
  if (response.code === 200) {
    message.success("任务已完成");
    await loadTaskList();
  } else {
    message.error(response.msg || "状态更新失败");
  }
};

const confirmDeleteTask = (task: ShanzhuTaskVO) => {
  if (!task.id) {
    return;
  }

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
        await loadTaskList();
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
  await loadTaskList();
});
</script>

<style scoped>
.shanzhu-task-page {
  width: 100%;
}

.task-list-title {
  margin-bottom: 4px;
}

.task-list-item {
  padding-right: 0;
  padding-left: 0;
}

.task-main {
  min-width: 0;
  flex: 1;
}

.task-title {
  margin-top: 8px;
  margin-bottom: 4px;
}

.task-description {
  margin-bottom: 8px;
  color: var(--lihua-text-color-secondary);
}

.task-meta {
  color: var(--lihua-text-color-secondary);
  font-size: var(--lihua-font-size-sm);
}

.task-pagination {
  margin-top: 16px;
}
</style>
