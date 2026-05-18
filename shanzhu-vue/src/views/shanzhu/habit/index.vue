<template>
  <div class="shanzhu-habit-page">
    <a-flex :gap="16" vertical>
      <a-row :gutter="[16, 16]">
        <a-col :xs="24" :sm="12" :lg="6">
          <a-card :bordered="false">
            <a-statistic title="今日待打卡" :value="habitStats.todayTotalCount || 0" suffix="个"/>
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :lg="6">
          <a-card :bordered="false">
            <a-statistic title="今日已打卡" :value="habitStats.todayCheckedCount || 0" suffix="个" :value-style="{ color: '#52c41a' }"/>
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :lg="6">
          <a-card :bordered="false">
            <a-statistic title="周期完成率" :value="habitStats.currentPeriodCompletionRate || 0" suffix="%"/>
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :lg="6">
          <a-card :bordered="false">
            <a-statistic title="最长连续打卡" :value="habitStats.maxContinuousDays || 0" suffix="天"/>
          </a-card>
        </a-col>
      </a-row>

      <a-card :bordered="false" class="habit-today-card">
        <template #title>
          <a-flex justify="space-between" align="center" wrap="wrap" :gap="12">
            <div>
              <a-typography-title :level="4" class="habit-section-title">今日打卡</a-typography-title>
              <a-typography-text type="secondary">把持续性行动沉淀为可复盘的事实数据</a-typography-text>
            </div>
            <a-button type="primary" @click="openCreateHabitModal">
              <template #icon>
                <PlusOutlined/>
              </template>
              新建习惯
            </a-button>
          </a-flex>
        </template>

        <a-spin :spinning="todayLoading">
          <a-empty v-if="todayHabits.length === 0" description="今日暂无待打卡习惯"/>
          <a-row v-else :gutter="[16, 16]">
            <a-col v-for="habit in todayHabits" :key="habit.id" :xs="24" :md="12" :xl="8">
              <a-card class="today-habit-card" :class="{ 'today-habit-card-checked': habit.todayChecked }">
                <a-flex vertical :gap="12">
                  <a-flex justify="space-between" align="flex-start" :gap="12">
                    <div class="habit-main">
                      <a-typography-title :level="5" class="habit-title">{{ habit.title }}</a-typography-title>
                      <a-space size="small" wrap class="habit-meta">
                        <span>目标：{{ habit.goalTitle || '-' }}</span>
                        <span>目标值：{{ formatTargetValue(habit.targetValue, habit.unit) }}</span>
                      </a-space>
                    </div>
                    <a-tag :color="habit.todayChecked ? 'success' : 'processing'">
                      {{ habit.todayChecked ? '已打卡' : '待打卡' }}
                    </a-tag>
                  </a-flex>

                  <a-typography-paragraph v-if="habit.note" class="habit-note" :ellipsis="{ rows: 2 }">
                    今日备注：{{ habit.note }}
                  </a-typography-paragraph>

                  <a-flex justify="flex-end" align="center" :gap="8">
                    <a-button
                        v-if="habit.todayChecked"
                        size="small"
                        danger
                        ghost
                        @click="confirmCancelCheckin(habit)"
                    >
                      取消打卡
                    </a-button>
                    <a-button type="primary" size="small" @click="openCheckinModal(habit)">
                      {{ habit.todayChecked ? '修改打卡' : '立即打卡' }}
                    </a-button>
                  </a-flex>
                </a-flex>
              </a-card>
            </a-col>
          </a-row>
        </a-spin>
      </a-card>

      <a-card :bordered="false" class="habit-filter-card">
        <a-form :colon="false">
          <a-row :gutter="[16, 12]">
            <a-col :xs="24" :sm="12" :lg="6">
              <a-form-item label="关键词">
                <a-input v-model:value="habitQuery.keyword" placeholder="搜索习惯名称" allow-clear/>
              </a-form-item>
            </a-col>
            <a-col :xs="24" :sm="12" :lg="6">
              <a-form-item label="目标">
                <a-select
                    v-model:value="habitQuery.goalId"
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
              <a-form-item label="状态">
                <a-select v-model:value="habitQuery.status" placeholder="请选择状态" allow-clear>
                  <a-select-option v-for="item in habitStatusOptions" :key="item.value" :value="item.value">
                    {{ item.label }}
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :xs="24" :sm="12" :lg="6">
              <a-form-item label="频率">
                <a-select v-model:value="habitQuery.frequencyType" placeholder="请选择频率" allow-clear>
                  <a-select-option v-for="item in habitFrequencyOptions" :key="item.value" :value="item.value">
                    {{ item.label }}
                  </a-select-option>
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

      <a-card :bordered="false" class="habit-list-card">
        <template #title>
          <a-flex justify="space-between" align="center" wrap="wrap" :gap="12">
            <div>
              <a-typography-title :level="4" class="habit-section-title">习惯列表</a-typography-title>
              <a-typography-text type="secondary">管理持续性行动，跟踪周期完成率和连续打卡天数</a-typography-text>
            </div>
          </a-flex>
        </template>

        <a-spin :spinning="tableLoading">
          <a-empty v-if="habitList.length === 0" description="暂无习惯，先创建一个习惯吧"/>
          <a-row v-else :gutter="[16, 16]">
            <a-col v-for="habit in habitList" :key="habit.id" :xs="24" :lg="12" :xl="8">
              <a-card class="habit-card" hoverable>
                <a-flex vertical :gap="12">
                  <a-flex justify="space-between" align="flex-start" :gap="12">
                    <div class="habit-main">
                      <a-space size="small" wrap>
                        <a-tag :color="getHabitStatusOption(habit.status).color">
                          {{ getHabitStatusOption(habit.status).label }}
                        </a-tag>
                        <a-tag color="blue">{{ getHabitFrequencyLabel(habit.frequencyType) }}</a-tag>
                        <a-tag :color="habit.todayChecked ? 'success' : 'default'">
                          {{ habit.todayChecked ? '今日已打卡' : '今日未打卡' }}
                        </a-tag>
                      </a-space>
                      <a-typography-title :level="5" class="habit-title">{{ habit.title }}</a-typography-title>
                    </div>
                  </a-flex>

                  <a-typography-paragraph class="habit-description" :ellipsis="{ rows: 2 }">
                    {{ habit.description || '暂无习惯说明' }}
                  </a-typography-paragraph>

                  <a-progress :percent="formatPercent(habit.currentPeriodCompletionRate)" size="small"/>

                  <a-space size="small" wrap class="habit-meta">
                    <span>目标：{{ habit.goalTitle || '-' }}</span>
                    <span>周期：{{ habit.currentPeriodCheckedCount || 0 }}/{{ habit.currentPeriodTargetCount || 0 }}</span>
                    <span>连续：{{ habit.continuousDays || 0 }} 天</span>
                    <span>单位：{{ habit.unit || '-' }}</span>
                  </a-space>

                  <a-flex justify="space-between" align="center">
                    <span class="habit-date">开始：{{ habit.startDate || '-' }}，结束：{{ habit.endDate || '-' }}</span>
                    <a-space size="small">
                      <a-button type="link" size="small" @click="openEditHabitModal(habit)">编辑</a-button>
                      <a-button
                          v-if="habit.status === activeStatus"
                          type="link"
                          size="small"
                          @click="handlePauseHabit(habit)"
                      >
                        暂停
                      </a-button>
                      <a-button
                          v-if="habit.status === pausedStatus"
                          type="link"
                          size="small"
                          @click="handleResumeHabit(habit)"
                      >
                        恢复
                      </a-button>
                      <a-button type="link" size="small" danger @click="confirmDeleteHabit(habit)">删除</a-button>
                    </a-space>
                  </a-flex>
                </a-flex>
              </a-card>
            </a-col>
          </a-row>
        </a-spin>

        <template #footer>
          <a-flex justify="flex-end">
            <a-pagination
                v-model:current="habitPagination.current"
                v-model:page-size="habitPagination.pageSize"
                show-size-changer
                :total="habitPagination.total"
                :show-total="(total:number) => `共 ${total} 条`"
                @change="handlePageChange"
            />
          </a-flex>
        </template>
      </a-card>
    </a-flex>

    <a-modal
        v-model:open="habitModal.open"
        :confirm-loading="habitModal.saveLoading"
        :title="habitModal.title"
        width="760px"
        ok-text="保 存"
        cancel-text="取 消"
        @ok="handleSaveHabit"
    >
      <a-form ref="habitFormRef" :colon="false" :model="habitForm" :rules="habitRules" :label-col="{ span: 4 }">
        <a-form-item label="关联目标">
          <a-select
              v-model:value="habitForm.goalId"
              placeholder="可选择关联目标"
              allow-clear
              show-search
              :filter-option="filterGoalOption"
          >
            <a-select-option v-for="goal in goalOptions" :key="goal.id" :value="goal.id">
              {{ goal.title }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="习惯名称" name="title">
          <a-input v-model:value="habitForm.title" placeholder="例如：每天阅读 30 分钟" :maxlength="120" show-count allow-clear/>
        </a-form-item>
        <a-form-item label="状态">
          <a-radio-group v-model:value="habitForm.status">
            <a-radio v-for="item in habitStatusOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-radio>
          </a-radio-group>
        </a-form-item>
        <a-row>
          <a-col :span="8">
            <a-form-item label="频率" :label-col="{ span: 10 }" name="frequencyType">
              <a-select v-model:value="habitForm.frequencyType" style="width: 100%">
                <a-select-option v-for="item in habitFrequencyOptions" :key="item.value" :value="item.value">
                  {{ item.label }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="周期次数" :label-col="{ span: 10 }">
              <a-input-number v-model:value="habitForm.targetCount" :min="1" :max="999" style="width: 100%"/>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="排序" :label-col="{ span: 10 }">
              <a-input-number v-model:value="habitForm.sortOrder" :min="0" :max="99999" style="width: 100%"/>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row>
          <a-col :span="12">
            <a-form-item label="目标值" :label-col="{ span: 8 }">
              <a-input-number v-model:value="habitForm.targetValue" :min="0" :max="999999" style="width: 100%"/>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="单位" :label-col="{ span: 8 }">
              <a-input v-model:value="habitForm.unit" placeholder="例如：分钟、页、次" :maxlength="20" allow-clear/>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row>
          <a-col :span="12">
            <a-form-item label="开始日期" :label-col="{ span: 8 }">
              <a-date-picker v-model:value="habitForm.startDate" value-format="YYYY-MM-DD" style="width: 100%" placeholder="请选择开始日期"/>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="结束日期" :label-col="{ span: 8 }">
              <a-date-picker v-model:value="habitForm.endDate" value-format="YYYY-MM-DD" style="width: 100%" placeholder="可不填"/>
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="习惯说明">
          <a-textarea v-model:value="habitForm.description" placeholder="描述这个习惯的执行方式或完成标准" :rows="4" :maxlength="500" show-count allow-clear/>
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model:value="habitForm.remark" placeholder="补充说明" :rows="3" :maxlength="300" show-count allow-clear/>
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
        v-model:open="checkinModal.open"
        :confirm-loading="checkinModal.saveLoading"
        :title="checkinModal.title"
        width="560px"
        ok-text="保 存"
        cancel-text="取 消"
        @ok="handleSaveCheckin"
    >
      <a-form ref="checkinFormRef" :colon="false" :model="checkinForm" :rules="checkinRules" :label-col="{ span: 5 }">
        <a-form-item label="打卡日期" name="checkinDate">
          <a-date-picker v-model:value="checkinForm.checkinDate" value-format="YYYY-MM-DD" style="width: 100%" placeholder="请选择打卡日期"/>
        </a-form-item>
        <a-form-item label="实际完成值">
          <a-input-number v-model:value="checkinForm.actualValue" :min="0" :max="999999" style="width: 100%"/>
        </a-form-item>
        <a-form-item label="心情">
          <a-select v-model:value="checkinForm.mood" placeholder="请选择心情" allow-clear>
            <a-select-option value="great">很好</a-select-option>
            <a-select-option value="normal">一般</a-select-option>
            <a-select-option value="tired">疲惫</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="打卡备注">
          <a-textarea v-model:value="checkinForm.note" placeholder="记录今天执行这个习惯的真实情况" :rows="4" :maxlength="300" show-count allow-clear/>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import {onMounted, reactive, ref} from "vue";
import type {FormInstance, Rule} from "ant-design-vue/es/form";
import {message, Modal} from "ant-design-vue";
import {PlusOutlined, RedoOutlined, SearchOutlined} from "@ant-design/icons-vue";
import {queryGoalPage} from "@/api/shanzhu/goal/Goal.ts";
import type {ShanzhuGoalVO} from "@/api/shanzhu/goal/type/Goal.ts";
import type {BaseModalActiveType} from "@/api/global/Type.ts";
import {
  cancelHabitCheckin,
  deleteHabit,
  pauseHabit,
  queryHabitPage,
  queryHabitStats,
  queryTodayHabits,
  resumeHabit,
  saveHabit,
  saveHabitCheckin
} from "@/api/shanzhu/habit/Habit.ts";
import type {
  HabitFrequencyOption,
  HabitStatusOption,
  ShanzhuHabit,
  ShanzhuHabitCheckin,
  ShanzhuHabitQuery,
  ShanzhuHabitStatsVO,
  ShanzhuHabitTodayVO,
  ShanzhuHabitVO
} from "@/api/shanzhu/habit/type/Habit.ts";

const activeStatus = "active";
const pausedStatus = "paused";

const habitStatusOptions: HabitStatusOption[] = [
  {label: "进行中", value: "active", color: "processing"},
  {label: "已暂停", value: "paused", color: "warning"},
  {label: "已完成", value: "completed", color: "success"},
  {label: "已取消", value: "cancelled", color: "error"}
];

const habitFrequencyOptions: HabitFrequencyOption[] = [
  {label: "每日", value: "daily"},
  {label: "每周", value: "weekly"},
  {label: "每月", value: "monthly"}
];

const defaultHabitQuery = (): ShanzhuHabitQuery => ({
  pageNum: 1,
  pageSize: 9
});

const defaultHabitForm = (): ShanzhuHabit => ({
  frequencyType: "daily",
  targetCount: 1,
  targetValue: 1,
  status: activeStatus,
  sortOrder: 0
});

const getTodayString = () => {
  const currentDate = new Date();
  const year = currentDate.getFullYear();
  const month = String(currentDate.getMonth() + 1).padStart(2, "0");
  const day = String(currentDate.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
};

const defaultCheckinForm = (): ShanzhuHabitCheckin => ({
  checkinDate: getTodayString()
});

const habitQuery = ref<ShanzhuHabitQuery>(defaultHabitQuery());
const habitList = ref<ShanzhuHabitVO[]>([]);
const todayHabits = ref<ShanzhuHabitTodayVO[]>([]);
const habitStats = ref<ShanzhuHabitStatsVO>({});
const goalOptions = ref<ShanzhuGoalVO[]>([]);
const tableLoading = ref(false);
const todayLoading = ref(false);
const statsLoading = ref(false);
const goalLoading = ref(false);
const habitFormRef = ref<FormInstance>();
const checkinFormRef = ref<FormInstance>();
const habitForm = ref<ShanzhuHabit>(defaultHabitForm());
const checkinForm = ref<ShanzhuHabitCheckin>(defaultCheckinForm());

const habitPagination = reactive({
  current: 1,
  pageSize: 9,
  total: 0
});

const habitModal = reactive<BaseModalActiveType>({
  open: false,
  saveLoading: false,
  title: "新建习惯"
});

const checkinModal = reactive<BaseModalActiveType>({
  open: false,
  saveLoading: false,
  title: "习惯打卡"
});

const habitRules: Record<string, Rule[]> = {
  title: [{required: true, message: "请输入习惯名称", trigger: "blur"}],
  frequencyType: [{required: true, message: "请选择频率", trigger: "change"}]
};

const checkinRules: Record<string, Rule[]> = {
  checkinDate: [{required: true, message: "请选择打卡日期", trigger: "change"}]
};

const getHabitStatusOption = (status?: string) => {
  return habitStatusOptions.find(item => item.value === status) || habitStatusOptions[0];
};

const getHabitFrequencyLabel = (frequencyType?: string) => {
  return habitFrequencyOptions.find(item => item.value === frequencyType)?.label || "每日";
};

const formatPercent = (value?: number) => {
  return Math.min(Math.max(Number(value || 0), 0), 100);
};

const formatTargetValue = (targetValue?: number, unit?: string) => {
  if (targetValue === undefined || targetValue === null) {
    return unit || "-";
  }
  return `${targetValue}${unit || ""}`;
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

const loadHabitList = async (pageNum = habitPagination.current, pageSize = habitPagination.pageSize) => {
  tableLoading.value = true;
  try {
    const response = await queryHabitPage({
      ...habitQuery.value,
      pageNum,
      pageSize
    });
    if (response.code === 200) {
      habitList.value = response.data.records || [];
      habitPagination.current = response.data.current || pageNum;
      habitPagination.pageSize = response.data.size || pageSize;
      habitPagination.total = response.data.total || 0;
      habitQuery.value.pageNum = habitPagination.current;
      habitQuery.value.pageSize = habitPagination.pageSize;
    } else {
      message.error(response.msg || "习惯列表加载失败");
    }
  } finally {
    tableLoading.value = false;
  }
};

const loadTodayHabits = async () => {
  todayLoading.value = true;
  try {
    const response = await queryTodayHabits();
    if (response.code === 200) {
      todayHabits.value = response.data || [];
    } else {
      message.error(response.msg || "今日打卡列表加载失败");
    }
  } finally {
    todayLoading.value = false;
  }
};

const loadHabitStats = async () => {
  statsLoading.value = true;
  try {
    const response = await queryHabitStats();
    if (response.code === 200) {
      habitStats.value = response.data || {};
    } else {
      message.error(response.msg || "习惯统计加载失败");
    }
  } finally {
    statsLoading.value = false;
  }
};

const refreshHabitData = async () => {
  await Promise.all([
    loadHabitList(),
    loadTodayHabits(),
    loadHabitStats()
  ]);
};

const handleSearch = () => {
  loadHabitList(1, habitPagination.pageSize);
};

const handlePageChange = (pageNum: number, pageSize: number) => {
  loadHabitList(pageNum, pageSize);
};

const resetQuery = () => {
  habitQuery.value = defaultHabitQuery();
  habitPagination.current = 1;
  habitPagination.pageSize = 9;
  habitPagination.total = 0;
  loadHabitList(1, habitPagination.pageSize);
};

const openCreateHabitModal = () => {
  habitForm.value = defaultHabitForm();
  habitModal.title = "新建习惯";
  habitModal.open = true;
};

const openEditHabitModal = (habit: ShanzhuHabitVO) => {
  habitForm.value = {
    ...habit
  };
  habitModal.title = "编辑习惯";
  habitModal.open = true;
};

const handleSaveHabit = async () => {
  await habitFormRef.value?.validate();

  habitModal.saveLoading = true;
  try {
    const response = await saveHabit(habitForm.value);
    if (response.code === 200) {
      message.success("保存成功");
      habitModal.open = false;
      await refreshHabitData();
    } else {
      message.error(response.msg || "保存失败");
    }
  } finally {
    habitModal.saveLoading = false;
  }
};

const handlePauseHabit = async (habit: ShanzhuHabitVO) => {
  if (!habit.id) {
    return;
  }

  const response = await pauseHabit(habit.id);
  if (response.code === 200) {
    message.success("已暂停习惯");
    await refreshHabitData();
  } else {
    message.error(response.msg || "暂停失败");
  }
};

const handleResumeHabit = async (habit: ShanzhuHabitVO) => {
  if (!habit.id) {
    return;
  }

  const response = await resumeHabit(habit.id);
  if (response.code === 200) {
    message.success("已恢复习惯");
    await refreshHabitData();
  } else {
    message.error(response.msg || "恢复失败");
  }
};

const confirmDeleteHabit = (habit: ShanzhuHabitVO) => {
  if (!habit.id) {
    return;
  }

  Modal.confirm({
    title: "确认删除习惯？",
    content: `删除后，习惯「${habit.title || '-'}」将不再展示。`,
    okText: "确认删除",
    cancelText: "取消",
    okType: "danger",
    onOk: async () => {
      const response = await deleteHabit(habit.id || "");
      if (response.code === 200) {
        message.success("删除成功");
        await refreshHabitData();
      } else {
        message.error(response.msg || "删除失败");
      }
    }
  });
};

const openCheckinModal = (habit: ShanzhuHabitTodayVO) => {
  checkinForm.value = {
    habitId: habit.id,
    checkinDate: getTodayString(),
    actualValue: habit.actualValue ?? habit.targetValue,
    note: habit.note
  };
  checkinModal.title = habit.todayChecked ? "修改打卡" : "习惯打卡";
  checkinModal.open = true;
};

const handleSaveCheckin = async () => {
  await checkinFormRef.value?.validate();

  checkinModal.saveLoading = true;
  try {
    const response = await saveHabitCheckin(checkinForm.value);
    if (response.code === 200) {
      message.success("打卡成功");
      checkinModal.open = false;
      await refreshHabitData();
    } else {
      message.error(response.msg || "打卡失败");
    }
  } finally {
    checkinModal.saveLoading = false;
  }
};

const confirmCancelCheckin = (habit: ShanzhuHabitTodayVO) => {
  if (!habit.todayCheckinId) {
    return;
  }

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
        await refreshHabitData();
      } else {
        message.error(response.msg || "取消打卡失败");
      }
    }
  });
};

onMounted(async () => {
  await loadGoalOptions();
  await refreshHabitData();
});
</script>

<style scoped>
.shanzhu-habit-page {
  width: 100%;
}

.habit-section-title {
  margin-bottom: 4px;
}

.today-habit-card {
  height: 100%;
  border: 1px solid transparent;
}

.today-habit-card-checked {
  border-color: #b7eb8f;
  background: #f6ffed;
}

.habit-card {
  height: 100%;
}

.habit-main {
  min-width: 0;
  flex: 1;
}

.habit-title {
  margin-top: 8px;
  margin-bottom: 4px;
}

.habit-description,
.habit-note {
  margin-bottom: 0;
  color: var(--lihua-text-color-secondary);
}

.habit-meta,
.habit-date {
  color: var(--lihua-text-color-secondary);
  font-size: var(--lihua-font-size-sm);
}
</style>
