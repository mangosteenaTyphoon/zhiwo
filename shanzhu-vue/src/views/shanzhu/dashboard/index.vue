<template>
  <div class="shanzhu-dashboard-page">
    <div class="dashboard-hero">
      <div>
        <div class="dashboard-eyebrow">Growth Dashboard</div>
        <h2 class="dashboard-page-title">目标仪表盘</h2>
        <p class="dashboard-page-desc">聚合目标、任务、习惯和风险数据，快速判断今天该把精力放在哪里。</p>
      </div>
      <a-button type="primary" shape="round" size="large" :loading="pageLoading" @click="initDashboard">
        <template #icon>
          <RedoOutlined/>
        </template>
        刷新数据
      </a-button>
    </div>

    <a-spin :spinning="pageLoading">
      <div class="dashboard-overview-grid">
        <div
            v-for="item in overviewCards"
            :key="item.title"
            class="overview-card"
            :style="{ '--card-color': item.color }"
        >
          <div class="overview-card-top">
            <span>{{ item.title }}</span>
            <em>{{ item.suffix }}</em>
          </div>
          <strong>{{ item.value }}</strong>
          <p>{{ item.description }}</p>
        </div>
      </div>

      <div class="dashboard-main-grid">
        <a-card :bordered="false" class="dashboard-panel habit-summary-panel">
          <template #title>
            <div class="dashboard-panel-title">
              <ExperimentOutlined/>
              <strong>习惯打卡统计</strong>
            </div>
          </template>
          <div class="habit-summary">
            <div class="habit-progress-ring">
              <span>{{ formatHabitPercent(habitStats.currentPeriodCompletionRate) }}%</span>
              <small>周期完成率</small>
            </div>
            <div class="habit-summary-stats">
              <div>
                <span>今日已打卡</span>
                <strong>{{ habitStats.todayCheckedCount || 0 }}/{{ habitStats.todayTotalCount || 0 }}</strong>
              </div>
              <div>
                <span>周期已完成</span>
                <strong>{{ habitStats.currentPeriodCompletedHabitCount || 0 }}</strong>
              </div>
              <div>
                <span>最长连续</span>
                <strong>{{ habitStats.maxContinuousDays || 0 }} 天</strong>
              </div>
            </div>
          </div>
          <a-progress
              :percent="formatHabitPercent(habitStats.currentPeriodCompletionRate)"
              :show-info="false"
              size="small"
          />
        </a-card>

        <a-card :bordered="false" class="dashboard-panel today-habit-panel">
          <template #title>
            <div class="dashboard-panel-title">
              <CheckCircleOutlined/>
              <strong>今日习惯打卡</strong>
              <em>{{ todayHabits.length }}</em>
            </div>
          </template>
          <template #extra>
            <a-button type="link" size="small" @click="openHabitPage">查看全部</a-button>
          </template>
          <a-empty v-if="todayHabits.length === 0" description="今日暂无待打卡习惯"/>
          <div v-else class="today-habit-list">
            <div
                v-for="item in todayHabits"
                :key="item.id"
                class="habit-list-item"
                :class="{ 'habit-list-item-done': item.todayChecked }"
            >
              <div class="habit-list-main">
                <div class="habit-title-row">
                  <span class="habit-title">{{ item.title }}</span>
                  <span class="habit-status-chip" :class="{ 'habit-status-done': item.todayChecked }">
                    {{ item.todayChecked ? '已打卡' : '待打卡' }}
                  </span>
                </div>
                <div class="habit-meta">
                  <span>目标值 {{ formatHabitTargetValue(item.targetValue, item.unit) }}</span>
                  <span v-if="item.goalTitle"><AimOutlined style="margin-right: 3px;"/> {{ item.goalTitle }}</span>
                  <span v-if="item.note">备注 {{ item.note }}</span>
                </div>
              </div>
              <a-button
                  v-if="item.todayChecked"
                  type="text"
                  size="small"
                  danger
                  class="habit-action-btn"
                  @click="confirmCancelHabitCheckin(item)"
              >
                取消
              </a-button>
              <a-button
                  v-else
                  type="primary"
                  size="small"
                  ghost
                  shape="round"
                  @click="handleQuickHabitCheckin(item)"
              >
                打卡
              </a-button>
            </div>
          </div>
        </a-card>
      </div>

      <div class="dashboard-insight-grid">
        <a-card :bordered="false" class="dashboard-panel category-panel">
          <template #title>
            <div class="dashboard-panel-title">
              <BarChartOutlined/>
              <strong>分类目标和任务分布</strong>
            </div>
          </template>
          <a-empty v-if="dashboard.categoryStats.length === 0" description="暂无分类统计数据"/>
          <div v-else class="category-list">
            <div
                v-for="record in dashboard.categoryStats"
                :key="record.categoryId || record.categoryName"
                class="category-item"
            >
              <div class="category-item-header">
                <span class="category-chip" :style="{ '--category-color': record.categoryColor || '#1677ff' }">
                  {{ record.categoryName || '未分类' }}
                </span>
                <span class="category-count">
                  目标 {{ record.goalCount || 0 }} · 任务 {{ record.taskCount || 0 }}
                </span>
              </div>
              <div class="category-progress-row">
                <span>目标完成</span>
                <a-progress
                    :percent="calculatePercent(record.completedGoalCount, record.goalCount)"
                    size="small"
                />
              </div>
              <div class="category-progress-row">
                <span>任务完成</span>
                <a-progress
                    :percent="calculatePercent(record.completedTaskCount, record.taskCount)"
                    size="small"
                    status="success"
                />
              </div>
            </div>
          </div>
        </a-card>

        <a-card :bordered="false" class="dashboard-panel trend-panel">
          <template #title>
            <div class="dashboard-panel-title">
              <RiseOutlined/>
              <strong>完成趋势</strong>
            </div>
          </template>
          <div class="time-stat-grid">
            <div>
              <span>今日完成</span>
              <strong>{{ dashboard.timeStats.todayCompletedTaskCount }}</strong>
            </div>
            <div>
              <span>本周完成</span>
              <strong>{{ dashboard.timeStats.weekCompletedTaskCount }}</strong>
            </div>
            <div>
              <span>本月完成</span>
              <strong>{{ dashboard.timeStats.monthCompletedTaskCount }}</strong>
            </div>
          </div>

          <div class="trend-list">
            <div
                v-for="item in dashboard.timeStats.recentSevenDayTaskCompletionTrend"
                :key="item.date"
                class="trend-row"
            >
              <span class="trend-date">{{ item.date }}</span>
              <a-progress
                  class="trend-progress"
                  :percent="calculateTrendPercent(item.completedTaskCount)"
                  :format="() => `${item.completedTaskCount} 个`"
              />
            </div>
          </div>
        </a-card>
      </div>

      <a-card :bordered="false" class="dashboard-panel risk-panel">
        <template #title>
          <div class="dashboard-panel-title">
            <WarningOutlined/>
            <strong>风险雷达</strong>
          </div>
        </template>
        <div class="risk-grid">
          <div class="risk-card">
            <div class="risk-card-title">逾期目标</div>
            <a-empty v-if="dashboard.riskStats.overdueGoals.length === 0" description="暂无逾期目标"/>
            <div v-else class="risk-list">
              <div v-for="item in dashboard.riskStats.overdueGoals" :key="item.id || item.title" class="risk-item">
                <div class="risk-item-title">{{ item.title }}</div>
                <div class="risk-item-meta">
                  <span>{{ item.categoryName || '未分类' }}</span>
                  <span>逾期 {{ item.overdueDays }} 天</span>
                  <span>进度 {{ item.progress || 0 }}%</span>
                </div>
              </div>
            </div>
          </div>

          <div class="risk-card">
            <div class="risk-card-title">逾期任务</div>
            <a-empty v-if="dashboard.riskStats.overdueTasks.length === 0" description="暂无逾期任务"/>
            <div v-else class="risk-list">
              <div v-for="item in dashboard.riskStats.overdueTasks" :key="item.id || item.title" class="risk-item">
                <div class="risk-item-title">{{ item.title }}</div>
                <div class="risk-item-meta">
                  <span><AimOutlined style="margin-right: 3px;"/> {{ item.goalTitle || '-' }}</span>
                  <span>逾期 {{ item.overdueDays }} 天</span>
                  <span>截止 {{ item.deadline || '-' }}</span>
                </div>
              </div>
            </div>
          </div>

          <div class="risk-card">
            <div class="risk-card-title">长期未推进目标</div>
            <a-empty v-if="dashboard.riskStats.longTimeNoProgressGoals.length === 0" description="暂无长期未推进目标"/>
            <div v-else class="risk-list">
              <div v-for="item in dashboard.riskStats.longTimeNoProgressGoals" :key="item.id || item.title" class="risk-item">
                <div class="risk-item-title">{{ item.title }}</div>
                <div class="risk-item-meta">
                  <span>{{ item.daysSinceLastProgress }} 天未推进</span>
                  <span>最近 {{ item.lastProgressDate || '-' }}</span>
                  <span>进度 {{ item.progress || 0 }}%</span>
                </div>
              </div>
            </div>
          </div>

          <div class="risk-card">
            <div class="risk-card-title">高优未完成任务</div>
            <a-empty v-if="dashboard.riskStats.highPriorityUnfinishedTasks.length === 0" description="暂无高优先级未完成任务"/>
            <div v-else class="risk-list">
              <div v-for="item in dashboard.riskStats.highPriorityUnfinishedTasks" :key="item.id || item.title" class="risk-item">
                <div class="risk-item-title">{{ item.title }}</div>
                <div class="risk-item-meta">
                  <span><AimOutlined style="margin-right: 3px;"/> {{ item.goalTitle || '-' }}</span>
                  <span>截止 {{ item.deadline || '-' }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </a-card>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import {computed, onMounted, reactive, ref} from "vue";
import {useRouter} from "vue-router";
import {message, Modal} from "ant-design-vue";
import {
  RedoOutlined,
  ExperimentOutlined,
  CheckCircleOutlined,
  BarChartOutlined,
  WarningOutlined,
  RiseOutlined,
  AimOutlined
} from "@ant-design/icons-vue";
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
  max-width: 1360px;
  min-height: calc(100vh - 120px);
  margin: 0 auto;
  padding: 36px 48px 56px;
  overflow-x: hidden;

  .dashboard-hero {
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
      radial-gradient(circle at 96% 18%, rgba(82, 196, 26, 0.10), transparent 28%),
      linear-gradient(135deg, rgba(255, 255, 255, 0.98), rgba(247, 250, 255, 0.94));
    box-shadow: 0 18px 45px rgba(15, 35, 80, 0.06);

    :deep(.ant-btn-primary) {
      min-width: 118px;
      height: 42px;
      box-shadow: 0 10px 24px rgba(22, 119, 255, 0.22);
    }
  }

  .dashboard-eyebrow {
    margin-bottom: 8px;
    color: #1677ff;
    font-size: 12px;
    font-weight: 800;
    letter-spacing: 0.8px;
    text-transform: uppercase;
  }

  .dashboard-page-title {
    margin: 0;
    color: rgba(0, 0, 0, 0.88);
    font-size: 30px;
    font-weight: 850;
    line-height: 1.2;
    letter-spacing: -0.7px;
  }

  .dashboard-page-desc {
    margin: 8px 0 0;
    color: rgba(0, 0, 0, 0.48);
    font-size: 14px;
  }

  .dashboard-overview-grid {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 14px;
    margin-bottom: 18px;
  }

  .overview-card,
  .dashboard-panel {
    border: 1px solid rgba(15, 35, 80, 0.06);
    border-radius: 22px;
    background: rgba(255, 255, 255, 0.94);
    box-shadow: 0 18px 45px rgba(15, 35, 80, 0.065), 0 1px 2px rgba(15, 35, 80, 0.04);
  }

  .overview-card {
    position: relative;
    min-height: 142px;
    padding: 18px;
    overflow: hidden;

    &::before {
      position: absolute;
      top: 18px;
      right: 18px;
      width: 46px;
      height: 46px;
      border-radius: 18px;
      background: color-mix(in srgb, var(--card-color) 14%, transparent);
      content: "";
    }

    &::after {
      position: absolute;
      top: 31px;
      right: 31px;
      width: 20px;
      height: 20px;
      border-radius: 999px;
      background: var(--card-color);
      opacity: 0.78;
      content: "";
    }

    strong {
      display: block;
      margin: 16px 0 8px;
      color: var(--card-color);
      font-size: 32px;
      font-weight: 900;
      line-height: 1;
    }

    p {
      display: -webkit-box;
      margin: 0;
      overflow: hidden;
      color: rgba(0, 0, 0, 0.46);
      font-size: 13px;
      line-height: 20px;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
    }
  }

  .overview-card-top {
    display: flex;
    align-items: center;
    gap: 8px;
    color: rgba(0, 0, 0, 0.54);
    font-size: 13px;
    font-weight: 800;

    em {
      padding: 1px 7px;
      border-radius: 999px;
      background: rgba(15, 35, 80, 0.06);
      font-size: 11px;
      font-style: normal;
    }
  }

  .dashboard-main-grid,
  .dashboard-insight-grid {
    display: grid;
    gap: 18px;
    margin-bottom: 18px;
  }

  .dashboard-main-grid {
    grid-template-columns: minmax(320px, 0.85fr) minmax(0, 1.15fr);
  }

  .dashboard-insight-grid {
    grid-template-columns: minmax(0, 1.25fr) minmax(320px, 0.75fr);
  }

  .dashboard-panel {
    overflow: hidden;

    :deep(.ant-card-head) {
      min-height: 58px;
      border-bottom-color: rgba(15, 35, 80, 0.06);
    }

    :deep(.ant-card-body) {
      padding: 18px;
    }

    :deep(.ant-card-extra .ant-btn) {
      border-radius: 999px;
      color: rgba(0, 0, 0, 0.48);
      font-weight: 650;
    }

    :deep(.ant-empty) {
      padding: 34px 12px;
    }
  }

  .dashboard-panel-title {
    display: flex;
    align-items: center;
    gap: 8px;
    min-width: 0;

    span {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 26px;
      height: 26px;
      border-radius: 10px;
      background: rgba(22, 119, 255, 0.08);
    }

    strong {
      color: rgba(0, 0, 0, 0.84);
      font-size: 16px;
      font-weight: 850;
    }

    em {
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
  }

  .habit-summary {
    display: flex;
    align-items: center;
    gap: 18px;
    margin-bottom: 18px;
  }

  .habit-progress-ring {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    width: 106px;
    height: 106px;
    flex-shrink: 0;
    border-radius: 32px;
    background:
      radial-gradient(circle at 30% 20%, rgba(255, 255, 255, 0.88), transparent 34%),
      linear-gradient(135deg, rgba(22, 119, 255, 0.92), rgba(82, 196, 26, 0.82));
    box-shadow: 0 18px 34px rgba(22, 119, 255, 0.20);
    color: #fff;

    span {
      font-size: 26px;
      font-weight: 900;
      line-height: 1;
    }

    small {
      margin-top: 8px;
      color: rgba(255, 255, 255, 0.84);
      font-size: 12px;
      font-weight: 700;
    }
  }

  .habit-summary-stats {
    display: grid;
    flex: 1;
    gap: 10px;

    div {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 10px;
      padding: 9px 11px;
      border-radius: 14px;
      background: rgba(15, 35, 80, 0.045);
    }

    span {
      color: rgba(0, 0, 0, 0.48);
      font-size: 12px;
      font-weight: 700;
    }

    strong {
      color: rgba(0, 0, 0, 0.78);
      font-size: 14px;
      font-weight: 850;
    }
  }

  .today-habit-list,
  .category-list,
  .risk-list,
  .trend-list {
    display: flex;
    flex-direction: column;
  }

  .habit-list-item {
    display: flex;
    align-items: flex-start;
    gap: 12px;
    padding: 14px 0;
    border-bottom: 1px solid rgba(15, 35, 80, 0.055);

    &:last-child {
      border-bottom: none;
    }
  }

  .habit-list-item-done {
    opacity: 0.76;
  }

  .habit-list-main {
    min-width: 0;
    flex: 1;
  }

  .habit-title-row {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 10px;
  }

  .habit-title,
  .risk-item-title {
    display: -webkit-box;
    overflow: hidden;
    color: rgba(0, 0, 0, 0.84);
    font-size: 14px;
    font-weight: 750;
    line-height: 22px;
    word-break: break-word;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }

  .habit-status-chip,
  .category-chip,
  .risk-item-meta span {
    display: inline-flex;
    align-items: center;
    max-width: 180px;
    min-height: 24px;
    padding: 3px 9px;
    overflow: hidden;
    border-radius: 999px;
    background: rgba(15, 35, 80, 0.055);
    color: rgba(0, 0, 0, 0.54);
    font-size: 12px;
    font-weight: 700;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .habit-status-done {
    background: rgba(82, 196, 26, 0.12);
    color: #389e0d;
  }

  .habit-meta,
  .risk-item-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    margin-top: 7px;
    color: rgba(0, 0, 0, 0.45);
    font-size: 12px;
    font-weight: 650;
  }

  .habit-action-btn {
    border-radius: 999px;
  }

  .category-item {
    padding: 15px 0;
    border-bottom: 1px solid rgba(15, 35, 80, 0.055);

    &:last-child {
      border-bottom: none;
    }
  }

  .category-item-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 10px;
    margin-bottom: 12px;
  }

  .category-chip {
    background: color-mix(in srgb, var(--category-color) 14%, transparent);
    color: var(--category-color);
  }

  .category-count {
    flex-shrink: 0;
    color: rgba(0, 0, 0, 0.42);
    font-size: 12px;
    font-weight: 700;
  }

  .category-progress-row {
    display: grid;
    grid-template-columns: 72px minmax(0, 1fr);
    align-items: center;
    gap: 10px;
    margin-top: 8px;

    span {
      color: rgba(0, 0, 0, 0.46);
      font-size: 12px;
      font-weight: 700;
    }
  }

  .time-stat-grid {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 10px;
    margin-bottom: 16px;

    div {
      padding: 13px;
      border-radius: 16px;
      background: rgba(15, 35, 80, 0.045);
    }

    span {
      display: block;
      margin-bottom: 8px;
      color: rgba(0, 0, 0, 0.44);
      font-size: 12px;
      font-weight: 750;
    }

    strong {
      color: rgba(0, 0, 0, 0.82);
      font-size: 22px;
      font-weight: 900;
      line-height: 1;
    }
  }

  .trend-row {
    display: grid;
    grid-template-columns: 90px minmax(0, 1fr);
    align-items: center;
    gap: 12px;
    margin-top: 10px;
  }

  .trend-date {
    color: rgba(0, 0, 0, 0.50);
    font-size: 12px;
    font-weight: 700;
  }

  .trend-progress {
    min-width: 0;
  }

  .risk-grid {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 14px;
  }

  .risk-card {
    min-width: 0;
    padding: 16px;
    border: 1px solid rgba(15, 35, 80, 0.055);
    border-radius: 18px;
    background:
      radial-gradient(circle at 10% 0%, rgba(255, 77, 79, 0.07), transparent 34%),
      rgba(255, 255, 255, 0.68);
  }

  .risk-card-title {
    margin-bottom: 12px;
    color: rgba(0, 0, 0, 0.78);
    font-size: 14px;
    font-weight: 850;
  }

  .risk-item {
    padding: 12px 0;
    border-bottom: 1px solid rgba(15, 35, 80, 0.055);

    &:last-child {
      border-bottom: none;
      padding-bottom: 0;
    }

    &:first-child {
      padding-top: 0;
    }
  }

  .risk-item-meta span {
    max-width: 150px;
  }

  :deep(.ant-progress-inner) {
    background: rgba(15, 35, 80, 0.06);
  }

  @media (max-width: 1200px) {
    padding: 28px 28px 48px;

    .dashboard-overview-grid {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }

    .dashboard-main-grid,
    .dashboard-insight-grid {
      grid-template-columns: 1fr;
    }

    .risk-grid {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }
  }

  @media (max-width: 760px) {
    .dashboard-hero {
      flex-direction: column;
      align-items: stretch;
    }

    .dashboard-overview-grid,
    .risk-grid,
    .time-stat-grid {
      grid-template-columns: 1fr;
    }

    .habit-summary {
      flex-direction: column;
      align-items: flex-start;
    }
  }

  @media (max-width: 640px) {
    padding: 18px 14px 36px;

    .dashboard-hero {
      padding: 22px 20px;
      border-radius: 22px;
    }

    .dashboard-page-title {
      font-size: 26px;
    }

    .overview-card strong {
      font-size: 28px;
    }

    .category-item-header,
    .habit-title-row {
      flex-direction: column;
      align-items: flex-start;
    }

    .trend-row,
    .category-progress-row {
      grid-template-columns: 1fr;
      gap: 6px;
    }
  }
}
</style>
