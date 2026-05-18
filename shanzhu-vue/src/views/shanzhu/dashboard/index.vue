<template>
  <div class="shanzhu-dashboard-page">
    <a-flex :gap="16" vertical>
      <a-card :bordered="false" class="dashboard-header-card">
        <a-flex justify="space-between" align="center" wrap="wrap" :gap="12">
          <div>
            <a-typography-title :level="4" class="dashboard-title">目标任务仪表盘</a-typography-title>
            <a-typography-text type="secondary">聚合目标、任务、习惯和进展记录数据，快速识别执行状态和风险</a-typography-text>
          </div>
          <a-button type="primary" :loading="pageLoading" @click="initDashboard">
            <template #icon>
              <RedoOutlined/>
            </template>
            刷 新
          </a-button>
        </a-flex>
      </a-card>

      <a-spin :spinning="pageLoading">
        <a-row :gutter="[16, 16]">
          <a-col v-for="item in overviewCards" :key="item.title" :xs="24" :sm="12" :lg="6">
            <a-card :bordered="false" class="overview-card">
              <a-statistic :title="item.title" :value="item.value" :value-style="{ color: item.color }">
                <template #suffix>
                  <span class="overview-suffix">{{ item.suffix }}</span>
                </template>
              </a-statistic>
              <a-typography-text type="secondary">{{ item.description }}</a-typography-text>
            </a-card>
          </a-col>
        </a-row>

        <a-row :gutter="[16, 16]" class="dashboard-section">
          <a-col :xs="24" :xl="10">
            <a-card :bordered="false" title="习惯打卡统计">
              <a-row :gutter="[12, 12]">
                <a-col :span="12">
                  <a-statistic title="今日待打卡" :value="habitStats.todayTotalCount || 0"/>
                </a-col>
                <a-col :span="12">
                  <a-statistic title="今日已打卡" :value="habitStats.todayCheckedCount || 0" :value-style="{ color: '#52c41a' }"/>
                </a-col>
                <a-col :span="12">
                  <a-statistic title="周期已完成" :value="habitStats.currentPeriodCompletedHabitCount || 0"/>
                </a-col>
                <a-col :span="12">
                  <a-statistic title="最长连续" :value="habitStats.maxContinuousDays || 0" suffix="天"/>
                </a-col>
              </a-row>

              <a-divider/>

              <a-flex vertical :gap="8">
                <a-flex justify="space-between" align="center">
                  <a-typography-text type="secondary">当前周期完成率</a-typography-text>
                  <a-typography-text strong>{{ formatHabitPercent(habitStats.currentPeriodCompletionRate) }}%</a-typography-text>
                </a-flex>
                <a-progress :percent="formatHabitPercent(habitStats.currentPeriodCompletionRate)" size="small"/>
              </a-flex>
            </a-card>
          </a-col>

          <a-col :xs="24" :xl="14">
            <a-card :bordered="false" title="今日习惯打卡">
              <template #extra>
                <a-button type="link" size="small" @click="openHabitPage">查看全部</a-button>
              </template>
              <a-empty v-if="todayHabits.length === 0" description="今日暂无待打卡习惯"/>
              <a-list v-else :data-source="todayHabits" size="small">
                <template #renderItem="{ item }">
                  <a-list-item class="habit-list-item">
                    <a-list-item-meta>
                      <template #title>
                        <a-space size="small" wrap>
                          <span>{{ item.title }}</span>
                          <a-tag :color="item.todayChecked ? 'success' : 'processing'">
                            {{ item.todayChecked ? '已打卡' : '待打卡' }}
                          </a-tag>
                          <a-tag v-if="item.goalTitle" color="blue">{{ item.goalTitle }}</a-tag>
                        </a-space>
                      </template>
                      <template #description>
                        目标值：{{ formatHabitTargetValue(item.targetValue, item.unit) }}
                        <template v-if="item.note">，备注：{{ item.note }}</template>
                      </template>
                    </a-list-item-meta>
                    <template #actions>
                      <a-button
                          v-if="item.todayChecked"
                          type="link"
                          size="small"
                          danger
                          @click="confirmCancelHabitCheckin(item)"
                      >
                        取消打卡
                      </a-button>
                      <a-button
                          v-else
                          type="link"
                          size="small"
                          @click="handleQuickHabitCheckin(item)"
                      >
                        快速打卡
                      </a-button>
                    </template>
                  </a-list-item>
                </template>
              </a-list>
            </a-card>
          </a-col>
        </a-row>

        <a-row :gutter="[16, 16]" class="dashboard-section">
          <a-col :xs="24" :xl="14">
            <a-card :bordered="false" title="分类目标和任务分布">
              <a-empty v-if="dashboard.categoryStats.length === 0" description="暂无分类统计数据"/>
              <a-table
                  v-else
                  :columns="categoryColumns"
                  :data-source="dashboard.categoryStats"
                  :pagination="false"
                  row-key="categoryId"
                  size="middle"
              >
                <template #bodyCell="{ column, record }">
                  <template v-if="column.key === 'categoryName'">
                    <a-tag :color="record.categoryColor || 'blue'">{{ record.categoryName || '未分类' }}</a-tag>
                  </template>
                  <template v-if="column.key === 'goalCompletionRate'">
                    <a-progress :percent="calculatePercent(record.completedGoalCount, record.goalCount)" size="small"/>
                  </template>
                  <template v-if="column.key === 'taskCompletionRate'">
                    <a-progress :percent="calculatePercent(record.completedTaskCount, record.taskCount)" size="small"/>
                  </template>
                </template>
              </a-table>
            </a-card>
          </a-col>

          <a-col :xs="24" :xl="10">
            <a-card :bordered="false" title="本周本月完成情况">
              <a-row :gutter="[12, 12]">
                <a-col :span="8">
                  <a-statistic title="今日完成" :value="dashboard.timeStats.todayCompletedTaskCount"/>
                </a-col>
                <a-col :span="8">
                  <a-statistic title="本周完成" :value="dashboard.timeStats.weekCompletedTaskCount"/>
                </a-col>
                <a-col :span="8">
                  <a-statistic title="本月完成" :value="dashboard.timeStats.monthCompletedTaskCount"/>
                </a-col>
              </a-row>

              <a-divider/>

              <a-flex vertical :gap="12">
                <div
                    v-for="item in dashboard.timeStats.recentSevenDayTaskCompletionTrend"
                    :key="item.date"
                    class="trend-row"
                >
                  <a-flex justify="space-between" align="center" :gap="12">
                    <span class="trend-date">{{ item.date }}</span>
                    <a-progress
                        class="trend-progress"
                        :percent="calculateTrendPercent(item.completedTaskCount)"
                        :format="() => `${item.completedTaskCount} 个`"
                    />
                  </a-flex>
                </div>
              </a-flex>
            </a-card>
          </a-col>
        </a-row>

        <a-row :gutter="[16, 16]" class="dashboard-section">
          <a-col :xs="24" :xl="12">
            <a-card :bordered="false" title="逾期目标">
              <a-empty v-if="dashboard.riskStats.overdueGoals.length === 0" description="暂无逾期目标"/>
              <a-list v-else :data-source="dashboard.riskStats.overdueGoals">
                <template #renderItem="{ item }">
                  <a-list-item>
                    <a-list-item-meta>
                      <template #title>
                        <a-space size="small" wrap>
                          <span>{{ item.title }}</span>
                          <a-tag :color="item.categoryColor || 'blue'">{{ item.categoryName || '未分类' }}</a-tag>
                        </a-space>
                      </template>
                      <template #description>
                        截止 {{ item.deadline || '-' }}，已逾期 {{ item.overdueDays }} 天，当前进度 {{ item.progress || 0 }}%
                      </template>
                    </a-list-item-meta>
                  </a-list-item>
                </template>
              </a-list>
            </a-card>
          </a-col>

          <a-col :xs="24" :xl="12">
            <a-card :bordered="false" title="逾期任务">
              <a-empty v-if="dashboard.riskStats.overdueTasks.length === 0" description="暂无逾期任务"/>
              <a-list v-else :data-source="dashboard.riskStats.overdueTasks">
                <template #renderItem="{ item }">
                  <a-list-item>
                    <a-list-item-meta>
                      <template #title>
                        <a-space size="small" wrap>
                          <span>{{ item.title }}</span>
                          <a-tag color="red">逾期 {{ item.overdueDays }} 天</a-tag>
                        </a-space>
                      </template>
                      <template #description>
                        目标：{{ item.goalTitle || '-' }}，分类：{{ item.categoryName || '未分类' }}，截止：{{ item.deadline || '-' }}
                      </template>
                    </a-list-item-meta>
                  </a-list-item>
                </template>
              </a-list>
            </a-card>
          </a-col>

          <a-col :xs="24" :xl="12">
            <a-card :bordered="false" title="长期未推进目标">
              <a-empty v-if="dashboard.riskStats.longTimeNoProgressGoals.length === 0" description="暂无长期未推进目标"/>
              <a-list v-else :data-source="dashboard.riskStats.longTimeNoProgressGoals">
                <template #renderItem="{ item }">
                  <a-list-item>
                    <a-list-item-meta>
                      <template #title>
                        <a-space size="small" wrap>
                          <span>{{ item.title }}</span>
                          <a-tag color="orange">{{ item.daysSinceLastProgress }} 天未推进</a-tag>
                        </a-space>
                      </template>
                      <template #description>
                        最近推进：{{ item.lastProgressDate || '-' }}，当前进度 {{ item.progress || 0 }}%
                      </template>
                    </a-list-item-meta>
                  </a-list-item>
                </template>
              </a-list>
            </a-card>
          </a-col>

          <a-col :xs="24" :xl="12">
            <a-card :bordered="false" title="高优先级未完成任务">
              <a-empty v-if="dashboard.riskStats.highPriorityUnfinishedTasks.length === 0" description="暂无高优先级未完成任务"/>
              <a-list v-else :data-source="dashboard.riskStats.highPriorityUnfinishedTasks">
                <template #renderItem="{ item }">
                  <a-list-item>
                    <a-list-item-meta>
                      <template #title>
                        <a-space size="small" wrap>
                          <span>{{ item.title }}</span>
                          <a-tag color="red">高优先级</a-tag>
                        </a-space>
                      </template>
                      <template #description>
                        目标：{{ item.goalTitle || '-' }}，截止：{{ item.deadline || '-' }}
                      </template>
                    </a-list-item-meta>
                  </a-list-item>
                </template>
              </a-list>
            </a-card>
          </a-col>
        </a-row>
      </a-spin>
    </a-flex>
  </div>
</template>

<script setup lang="ts">
import {computed, onMounted, reactive, ref} from "vue";
import {useRouter} from "vue-router";
import {message, Modal} from "ant-design-vue";
import {RedoOutlined} from "@ant-design/icons-vue";
import {queryDashboard} from "@/api/shanzhu/dashboard/Dashboard.ts";
import type {DashboardOverviewStats, ShanzhuDashboard} from "@/api/shanzhu/dashboard/type/Dashboard.ts";
import {
  cancelHabitCheckin,
  queryHabitStats,
  queryTodayHabits,
  saveHabitCheckin
} from "@/api/shanzhu/habit/Habit.ts";
import type {ShanzhuHabitStatsVO, ShanzhuHabitTodayVO} from "@/api/shanzhu/habit/type/Habit.ts";

const router = useRouter();
const pageLoading = ref(false);
const todayHabits = ref<ShanzhuHabitTodayVO[]>([]);

const habitStats = reactive<ShanzhuHabitStatsVO>({
  todayTotalCount: 0,
  todayCheckedCount: 0,
  currentPeriodHabitCount: 0,
  currentPeriodCompletedHabitCount: 0,
  currentPeriodUncompletedHabitCount: 0,
  currentPeriodCompletionRate: 0,
  maxContinuousDays: 0
});

const dashboard = reactive<ShanzhuDashboard>({
  overview: {
    totalGoalCount: 0,
    inProgressGoalCount: 0,
    completedGoalCount: 0,
    pausedGoalCount: 0,
    abandonedGoalCount: 0,
    totalTaskCount: 0,
    pendingTaskCount: 0,
    completedTaskCount: 0,
    overdueGoalCount: 0,
    overdueTaskCount: 0,
    longTimeNoProgressGoalCount: 0,
    highPriorityUnfinishedTaskCount: 0
  },
  categoryStats: [],
  timeStats: {
    todayCompletedTaskCount: 0,
    weekCompletedTaskCount: 0,
    monthCompletedTaskCount: 0,
    recentSevenDayTaskCompletionTrend: []
  },
  riskStats: {
    overdueGoals: [],
    overdueTasks: [],
    longTimeNoProgressGoals: [],
    highPriorityUnfinishedTasks: []
  }
});

const categoryColumns = [
  {
    title: '分类',
    dataIndex: 'categoryName',
    key: 'categoryName'
  },
  {
    title: '目标数',
    dataIndex: 'goalCount',
    key: 'goalCount'
  },
  {
    title: '目标完成率',
    key: 'goalCompletionRate'
  },
  {
    title: '任务数',
    dataIndex: 'taskCount',
    key: 'taskCount'
  },
  {
    title: '任务完成率',
    key: 'taskCompletionRate'
  }
];

const overviewCards = computed(() => {
  const overview: DashboardOverviewStats = dashboard.overview;
  return [
    {
      title: '目标总数',
      value: overview.totalGoalCount,
      suffix: '个',
      color: '#1677ff',
      description: `进行中 ${overview.inProgressGoalCount} 个，已完成 ${overview.completedGoalCount} 个`
    },
    {
      title: '任务总数',
      value: overview.totalTaskCount,
      suffix: '个',
      color: '#52c41a',
      description: `待处理 ${overview.pendingTaskCount} 个，已完成 ${overview.completedTaskCount} 个`
    },
    {
      title: '风险目标',
      value: overview.overdueGoalCount + overview.longTimeNoProgressGoalCount,
      suffix: '个',
      color: '#fa8c16',
      description: `逾期 ${overview.overdueGoalCount} 个，长期未推进 ${overview.longTimeNoProgressGoalCount} 个`
    },
    {
      title: '风险任务',
      value: overview.overdueTaskCount + overview.highPriorityUnfinishedTaskCount,
      suffix: '个',
      color: '#f5222d',
      description: `逾期 ${overview.overdueTaskCount} 个，高优未完成 ${overview.highPriorityUnfinishedTaskCount} 个`
    }
  ];
});

const maxTrendValue = computed(() => {
  const values = dashboard.timeStats.recentSevenDayTaskCompletionTrend.map(item => item.completedTaskCount);
  return Math.max(...values, 1);
});

const loadDashboardData = async () => {
  const response = await queryDashboard();
  const dashboardData = response.data;
  Object.assign(dashboard.overview, dashboardData.overview || {});
  dashboard.categoryStats = dashboardData.categoryStats || [];
  Object.assign(dashboard.timeStats, dashboardData.timeStats || {});
  Object.assign(dashboard.riskStats, dashboardData.riskStats || {});
};

const loadHabitStats = async () => {
  const response = await queryHabitStats();
  if (response.code === 200) {
    Object.assign(habitStats, response.data || {});
  } else {
    message.error(response.msg || "习惯统计加载失败");
  }
};

const loadTodayHabits = async () => {
  const response = await queryTodayHabits();
  if (response.code === 200) {
    todayHabits.value = response.data || [];
  } else {
    message.error(response.msg || "今日习惯加载失败");
  }
};

const initDashboard = async () => {
  pageLoading.value = true;
  try {
    await Promise.all([
      loadDashboardData(),
      loadHabitStats(),
      loadTodayHabits()
    ]);
  } catch (error) {
    message.error('仪表盘数据加载失败');
  } finally {
    pageLoading.value = false;
  }
};

const refreshHabitDashboard = async () => {
  await Promise.all([
    loadHabitStats(),
    loadTodayHabits()
  ]);
};

const calculatePercent = (completedCount: number, totalCount: number) => {
  if (!totalCount) {
    return 0;
  }
  return Math.round((completedCount / totalCount) * 100);
};

const calculateTrendPercent = (completedTaskCount: number) => {
  return Math.round((completedTaskCount / maxTrendValue.value) * 100);
};

const formatHabitPercent = (value?: number) => {
  return Math.min(Math.max(Number(value || 0), 0), 100);
};

const formatHabitTargetValue = (targetValue?: number, unit?: string) => {
  if (targetValue === undefined || targetValue === null) {
    return unit || "-";
  }
  return `${targetValue}${unit || ""}`;
};

const getTodayString = () => {
  const currentDate = new Date();
  const year = currentDate.getFullYear();
  const month = String(currentDate.getMonth() + 1).padStart(2, "0");
  const day = String(currentDate.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
};

const openHabitPage = () => {
  router.push("/shanzhu/habit");
};

const handleQuickHabitCheckin = async (habit: ShanzhuHabitTodayVO) => {
  if (!habit.id) {
    return;
  }

  const response = await saveHabitCheckin({
    habitId: habit.id,
    checkinDate: getTodayString(),
    actualValue: habit.targetValue,
    note: "来自仪表盘快速打卡"
  });
  if (response.code === 200) {
    message.success("打卡成功");
    await refreshHabitDashboard();
  } else {
    message.error(response.msg || "打卡失败");
  }
};

const confirmCancelHabitCheckin = (habit: ShanzhuHabitTodayVO) => {
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
        await refreshHabitDashboard();
      } else {
        message.error(response.msg || "取消打卡失败");
      }
    }
  });
};

onMounted(() => {
  initDashboard();
});
</script>

<style scoped lang="scss">
.shanzhu-dashboard-page {
  .dashboard-header-card,
  .overview-card,
  .dashboard-section :deep(.ant-card) {
    border-radius: 12px;
  }

  .dashboard-title {
    margin-bottom: 4px;
  }

  .overview-suffix {
    font-size: 14px;
  }

  .dashboard-section {
    margin-top: 16px;
  }

  .trend-row,
  .habit-list-item {
    width: 100%;
  }

  .trend-date {
    width: 96px;
    color: rgba(0, 0, 0, 0.65);
  }

  .trend-progress {
    flex: 1;
  }
}
</style>
