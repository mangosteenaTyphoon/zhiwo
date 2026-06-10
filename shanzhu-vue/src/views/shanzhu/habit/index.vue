<template>
  <div class="shanzhu-habit-page">
    <a-flex :gap="18" vertical>
      <div class="habit-hero">
        <div>
          <div class="habit-eyebrow">Habit Tracker</div>
          <h2 class="habit-page-title">习惯打卡</h2>
          <p class="habit-page-desc">把持续行动变成可复盘的数据，每天完成一点点。</p>
        </div>
        <a-button type="primary" shape="round" size="large" @click="openCreateHabitModal">
          <template #icon>
            <PlusOutlined/>
          </template>
          新建习惯
        </a-button>
      </div>

      <a-row :gutter="[16, 16]" class="habit-stat-grid">
        <a-col :xs="24" :sm="12" :lg="6">
          <div class="habit-stat-card habit-stat-card-blue">
            <span class="habit-stat-label">今日待打卡</span>
            <strong>{{ habitStats.todayTotalCount || 0 }}<small>个</small></strong>
          </div>
        </a-col>
        <a-col :xs="24" :sm="12" :lg="6">
          <div class="habit-stat-card habit-stat-card-green">
            <span class="habit-stat-label">今日已打卡</span>
            <strong>{{ habitStats.todayCheckedCount || 0 }}<small>个</small></strong>
          </div>
        </a-col>
        <a-col :xs="24" :sm="12" :lg="6">
          <div class="habit-stat-card habit-stat-card-purple">
            <span class="habit-stat-label">周期完成率</span>
            <strong>{{ habitStats.currentPeriodCompletionRate || 0 }}<small>%</small></strong>
          </div>
        </a-col>
        <a-col :xs="24" :sm="12" :lg="6">
          <div class="habit-stat-card habit-stat-card-orange">
            <span class="habit-stat-label">最长连续打卡</span>
            <strong>{{ habitStats.maxContinuousDays || 0 }}<small>天</small></strong>
          </div>
        </a-col>
      </a-row>

      <a-card :bordered="false" class="habit-today-card">
        <template #title>
          <a-flex justify="space-between" align="center" wrap="wrap" :gap="12">
            <div>
              <a-typography-title :level="4" class="habit-section-title">今日打卡</a-typography-title>
              <a-typography-text type="secondary">聚焦今天该完成的习惯，打卡后会同步更新统计。</a-typography-text>
            </div>
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
      todayHabits.value = [];
      message.error(response.msg || "今日打卡列表加载失败");
    }
  } catch (error) {
    todayHabits.value = [];
    message.error("今日打卡列表加载失败，请稍后重试");
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
  max-width: 1360px;
  margin: 0 auto;
  padding: 28px 24px 56px;
}

.habit-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 26px 28px;
  border-radius: 26px;
  background:
    radial-gradient(circle at 16% 0%, rgba(22, 119, 255, 0.13), transparent 34%),
    radial-gradient(circle at 96% 18%, rgba(82, 196, 26, 0.12), transparent 28%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.98), rgba(247, 250, 255, 0.94));
  border: 1px solid rgba(22, 119, 255, 0.08);
  box-shadow: 0 18px 45px rgba(15, 35, 80, 0.06);
}

.habit-eyebrow {
  margin-bottom: 8px;
  color: #1677ff;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.8px;
  text-transform: uppercase;
}

.habit-page-title {
  margin: 0;
  color: rgba(0, 0, 0, 0.88);
  font-size: 30px;
  font-weight: 850;
  letter-spacing: -0.7px;
  line-height: 1.2;
}

.habit-page-desc {
  margin: 8px 0 0;
  color: rgba(0, 0, 0, 0.48);
  font-size: 14px;
}

.habit-hero :deep(.ant-btn-primary) {
  min-width: 128px;
  height: 42px;
  box-shadow: 0 10px 24px rgba(22, 119, 255, 0.22);
}

.habit-stat-grid {
  margin-top: 2px;
}

.habit-stat-card {
  position: relative;
  min-height: 112px;
  padding: 22px 24px;
  overflow: hidden;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.94);
  border: 1px solid rgba(15, 35, 80, 0.06);
  box-shadow: 0 16px 36px rgba(15, 35, 80, 0.06), 0 1px 2px rgba(15, 35, 80, 0.04);
}

.habit-stat-card::after {
  content: "";
  position: absolute;
  right: -26px;
  top: -30px;
  width: 104px;
  height: 104px;
  border-radius: 50%;
  opacity: 0.12;
}

.habit-stat-card-blue::after {
  background: #1677ff;
}

.habit-stat-card-green::after {
  background: #52c41a;
}

.habit-stat-card-purple::after {
  background: #722ed1;
}

.habit-stat-card-orange::after {
  background: #fa8c16;
}

.habit-stat-label {
  display: block;
  margin-bottom: 12px;
  color: rgba(0, 0, 0, 0.46);
  font-size: 13px;
  font-weight: 600;
}

.habit-stat-card strong {
  display: flex;
  align-items: baseline;
  gap: 4px;
  color: rgba(0, 0, 0, 0.88);
  font-size: 30px;
  font-weight: 850;
  line-height: 1;
  font-variant-numeric: tabular-nums;
}

.habit-stat-card small {
  color: rgba(0, 0, 0, 0.42);
  font-size: 15px;
  font-weight: 650;
}

.habit-stat-card-green strong {
  color: #52c41a;
}

.habit-stat-card-purple strong {
  color: #722ed1;
}

.habit-stat-card-orange strong {
  color: #fa8c16;
}

.habit-section-title {
  margin-bottom: 4px;
  color: rgba(0, 0, 0, 0.86);
  font-weight: 800 !important;
  letter-spacing: -0.3px;
}

.habit-today-card,
.habit-filter-card,
.habit-list-card {
  overflow: hidden;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.94);
  border: 1px solid rgba(15, 35, 80, 0.06);
  box-shadow: 0 18px 45px rgba(15, 35, 80, 0.07), 0 1px 2px rgba(15, 35, 80, 0.04);
}

.habit-today-card :deep(.ant-card-head),
.habit-list-card :deep(.ant-card-head) {
  min-height: auto;
  padding: 20px 24px;
  border-bottom-color: rgba(15, 35, 80, 0.06);
}

.habit-today-card :deep(.ant-card-body),
.habit-list-card :deep(.ant-card-body) {
  padding: 24px;
}

.today-habit-card {
  height: 100%;
  border-radius: 18px;
  border: 1px solid rgba(15, 35, 80, 0.06);
  background: linear-gradient(135deg, #fff, rgba(248, 251, 255, 0.92));
  box-shadow: 0 10px 26px rgba(15, 35, 80, 0.05);
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
}

.today-habit-card:hover {
  transform: translateY(-2px);
  border-color: rgba(22, 119, 255, 0.18);
  box-shadow: 0 16px 34px rgba(15, 35, 80, 0.08);
}

.today-habit-card-checked {
  border-color: rgba(82, 196, 26, 0.24);
  background: linear-gradient(135deg, #f6ffed, rgba(255, 255, 255, 0.95));
}

.habit-filter-card {
  box-shadow: 0 12px 28px rgba(15, 35, 80, 0.045);
}

.habit-filter-card :deep(.ant-card-body) {
  padding: 20px 24px 4px;
}

.habit-filter-card :deep(.ant-form-item-label > label) {
  color: rgba(0, 0, 0, 0.58);
  font-weight: 650;
}

.habit-filter-card :deep(.ant-input),
.habit-filter-card :deep(.ant-select-selector) {
  border-radius: 12px;
  background: rgba(247, 249, 252, 0.86);
  border-color: rgba(15, 35, 80, 0.08) !important;
}

.habit-filter-card :deep(.ant-input:hover),
.habit-filter-card :deep(.ant-select-selector:hover) {
  border-color: rgba(22, 119, 255, 0.20) !important;
  background: #fff;
}

.habit-filter-card :deep(.ant-btn) {
  border-radius: 12px;
  min-width: 86px;
}

.habit-list-card :deep(.ant-card-actions),
.habit-list-card :deep(.ant-card-footer) {
  background: transparent;
}

.habit-card {
  height: 100%;
  border-radius: 18px;
  border: 1px solid rgba(15, 35, 80, 0.06);
  background: linear-gradient(135deg, #fff, rgba(249, 251, 255, 0.92));
  box-shadow: 0 10px 26px rgba(15, 35, 80, 0.05);
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
}

.habit-card:hover {
  transform: translateY(-2px);
  border-color: rgba(22, 119, 255, 0.18);
  box-shadow: 0 18px 38px rgba(15, 35, 80, 0.08);
}

.habit-card :deep(.ant-card-body),
.today-habit-card :deep(.ant-card-body) {
  padding: 18px;
}

.habit-main {
  min-width: 0;
  flex: 1;
}

.habit-title {
  margin-top: 8px;
  margin-bottom: 4px;
  color: rgba(0, 0, 0, 0.86);
  font-weight: 750 !important;
}

.habit-description,
.habit-note {
  margin-bottom: 0;
  color: rgba(0, 0, 0, 0.48);
  line-height: 1.7;
}

.habit-meta {
  color: rgba(0, 0, 0, 0.48);
  font-size: 12px;
}

.habit-meta span {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 999px;
  background: rgba(15, 35, 80, 0.04);
}

.habit-date {
  max-width: 220px;
  color: rgba(0, 0, 0, 0.42);
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.habit-card :deep(.ant-progress-bg) {
  height: 7px !important;
  border-radius: 999px;
  background: linear-gradient(90deg, #1677ff, #52c41a);
}

.habit-card :deep(.ant-tag),
.today-habit-card :deep(.ant-tag) {
  border-radius: 999px;
  padding: 1px 8px;
  font-weight: 600;
}

.habit-card :deep(.ant-btn-link) {
  padding-inline: 2px;
  font-weight: 600;
}

@media (max-width: 900px) {
  .shanzhu-habit-page {
    padding: 20px 16px 40px;
  }

  .habit-hero {
    align-items: stretch;
    flex-direction: column;
    padding: 22px;
    border-radius: 22px;
  }

  .habit-page-title {
    font-size: 26px;
  }

  .habit-hero :deep(.ant-btn-primary) {
    width: 100%;
  }

  .habit-today-card :deep(.ant-card-head),
  .habit-list-card :deep(.ant-card-head),
  .habit-today-card :deep(.ant-card-body),
  .habit-list-card :deep(.ant-card-body) {
    padding: 18px;
  }

  .habit-date {
    max-width: 100%;
  }
}
</style>
