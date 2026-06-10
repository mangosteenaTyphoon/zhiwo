<template>
  <div class="shanzhu-review-page">
    <div class="review-hero">
      <div class="review-hero-content">
        <div class="review-eyebrow">Review Report</div>
        <h2 class="review-page-title">目标复盘</h2>
        <p class="review-page-desc">把一段时间的任务、习惯、投入和目标变化沉淀成下一轮行动建议。</p>
      </div>
      <div class="review-hero-actions">
        <a-segmented v-model:value="reviewQuery.reviewType" :options="reviewTypeOptions" @change="handleReviewTypeChange"/>
        <a-range-picker v-model:value="dateRange" value-format="YYYY-MM-DD" @change="handleDateRangeChange"/>
        <a-button type="primary" shape="round" :loading="pageLoading" @click="initReview">生成复盘</a-button>
        <a-button shape="round" @click="goToTodo">
          <template #icon>
            <InboxOutlined/>
          </template>
          收集箱
        </a-button>
        <a-button shape="round" @click="goToTodayWork">
          <template #icon>
            <DesktopOutlined/>
          </template>
          工作台
        </a-button>
      </div>
    </div>

    <a-spin :spinning="pageLoading">
      <div class="review-report-card">
        <div class="review-report-header">
          <div>
            <span class="review-report-label">{{ reviewTitle }}</span>
            <h3>{{ review.startDate || '-' }} ~ {{ review.endDate || '-' }}</h3>
          </div>
          <a-button type="primary" ghost shape="round" @click="handleGenerateTodo()">
            <template #icon>
              <PlusOutlined/>
            </template>
            生成跟进 Todo
          </a-button>
        </div>

        <div class="review-overview-grid">
          <div
              v-for="item in overviewCards"
              :key="item.title"
              class="review-overview-card"
              :style="{ '--card-color': item.color }"
          >
            <span>{{ item.title }}</span>
            <strong>{{ item.value }}<em>{{ item.suffix }}</em></strong>
            <p>{{ item.description }}</p>
          </div>
        </div>

        <div class="review-summary-list">
          <div v-if="review.reviewSummary.length === 0" class="review-empty-tip">暂无复盘摘要，生成复盘后会在这里沉淀关键结论。</div>
          <div v-for="item in review.reviewSummary" v-else :key="item" class="review-summary-item">
            <span>✨</span>
            <p>{{ item }}</p>
          </div>
        </div>
      </div>

      <div class="review-section-grid">
        <a-card :bordered="false" class="review-panel habit-panel">
          <template #title>
            <div class="review-panel-title">
              <span>🌱</span>
              <strong>习惯完成分析</strong>
            </div>
          </template>

          <div class="habit-headline">
            <div class="habit-rate-orb">
              <strong>{{ formatHabitCompletionRate(habitStats.currentPeriodCompletionRate) }}%</strong>
              <span>周期完成率</span>
            </div>
            <div class="habit-stat-grid">
              <div>
                <span>周期习惯</span>
                <strong>{{ habitStats.currentPeriodHabitCount || 0 }}</strong>
              </div>
              <div>
                <span>已完成</span>
                <strong>{{ habitStats.currentPeriodCompletedHabitCount || 0 }}</strong>
              </div>
              <div>
                <span>未完成</span>
                <strong>{{ habitStats.currentPeriodUncompletedHabitCount || 0 }}</strong>
              </div>
              <div>
                <span>最长连续</span>
                <strong>{{ habitStats.maxContinuousDays || 0 }} 天</strong>
              </div>
            </div>
          </div>

          <a-progress
              :percent="formatHabitCompletionRate(habitStats.currentPeriodCompletionRate)"
              :show-info="false"
              size="small"
          />

          <a-empty v-if="habitCompletionAnalysis.length === 0" description="暂无习惯完成数据"/>
          <div v-else class="habit-analysis-list">
            <div v-for="item in habitCompletionAnalysis" :key="item.id || item.title" class="habit-analysis-item">
              <div class="habit-analysis-main">
                <div class="habit-analysis-title">
                  <span>{{ item.title }}</span>
                  <em>{{ item.checkedCount }}/{{ item.targetCount }} 次</em>
                </div>
                <div class="habit-analysis-meta">
                  <span>连续 {{ item.continuousDays }} 天</span>
                  <span>目标值 {{ formatHabitTargetValue(item.targetValue, item.unit) }}</span>
                  <span v-if="item.goalTitle">🎯 {{ item.goalTitle }}</span>
                </div>
              </div>
              <a-progress class="habit-analysis-progress" :percent="item.completionRate" size="small"/>
            </div>
          </div>
        </a-card>

        <a-card :bordered="false" class="review-panel checkin-panel">
          <template #title>
            <div class="review-panel-title">
              <span>✅</span>
              <strong>打卡记录洞察</strong>
            </div>
          </template>

          <div class="checkin-stat-grid">
            <div>
              <span>周期打卡</span>
              <strong>{{ habitCheckinInsight.totalCheckinCount }}</strong>
              <small>次</small>
            </div>
            <div>
              <span>覆盖习惯</span>
              <strong>{{ habitCheckinInsight.checkedHabitCount }}</strong>
              <small>个</small>
            </div>
            <div>
              <span>平均每日</span>
              <strong>{{ habitCheckinInsight.averageDailyCheckinCount }}</strong>
              <small>次</small>
            </div>
          </div>

          <a-empty v-if="habitCheckinRecords.length === 0" description="当前复盘周期内暂无习惯打卡记录"/>
          <div v-else class="checkin-list">
            <div v-for="item in habitCheckinRecords.slice(0, 8)" :key="item.id || `${item.habitId}-${item.checkinDate}`" class="checkin-item">
              <div>
                <strong>{{ item.habitTitle }}</strong>
                <p>
                  完成值 {{ formatHabitTargetValue(item.actualValue, item.unit) }}
                  <template v-if="item.note"> · {{ item.note }}</template>
                </p>
              </div>
              <div class="checkin-tags">
                <span>{{ item.checkinDate }}</span>
                <span v-if="item.goalTitle">🎯 {{ item.goalTitle }}</span>
              </div>
            </div>
          </div>
        </a-card>
      </div>

      <div class="review-section-grid">
        <a-card :bordered="false" class="review-panel">
          <template #title>
            <div class="review-panel-title">
              <span>🧭</span>
              <strong>分类长期投入情况</strong>
            </div>
          </template>
          <a-empty v-if="review.categoryInvestmentStats.length === 0" description="暂无分类投入数据"/>
          <div v-else class="investment-list">
            <div
                v-for="record in review.categoryInvestmentStats"
                :key="record.categoryId || record.categoryName"
                class="investment-item"
            >
              <div class="investment-header">
                <span class="investment-chip" :style="{ '--chip-color': record.categoryColor || '#1677ff' }">
                  {{ record.categoryName || '未分类' }}
                </span>
                <em>{{ record.actualMinutes || 0 }} 分钟</em>
              </div>
              <div class="investment-meta">
                <span>完成任务 {{ record.completedTaskCount || 0 }}</span>
                <span>完成目标 {{ record.completedGoalCount || 0 }}</span>
                <span>投入占比 {{ formatPercent(record.investmentRatio) }}%</span>
              </div>
              <a-progress :percent="formatPercent(record.investmentRatio)" size="small"/>
            </div>
          </div>
        </a-card>

        <a-card :bordered="false" class="review-panel">
          <template #title>
            <div class="review-panel-title">
              <span>🏷️</span>
              <strong>标签维度任务类型分析</strong>
            </div>
          </template>
          <a-empty v-if="review.tagAnalysisStats.length === 0" description="暂无标签分析数据"/>
          <div v-else class="tag-analysis-list">
            <div
                v-for="record in review.tagAnalysisStats"
                :key="record.tagId || record.tagName"
                class="tag-analysis-item"
            >
              <div class="tag-analysis-header">
                <span class="tag-chip" :style="{ '--chip-color': record.tagColor || '#1677ff' }">
                  {{ record.tagName || '未知标签' }}
                </span>
                <span>{{ record.taskCount || 0 }} 个任务</span>
              </div>
              <div class="tag-analysis-footer">
                <a-progress :percent="formatPercent(record.completionRate)" size="small"/>
                <em>{{ record.actualMinutes || 0 }} 分钟</em>
              </div>
            </div>
          </div>
        </a-card>
      </div>

      <div class="review-section-grid">
        <a-card :bordered="false" class="review-panel">
          <template #title>
            <div class="review-panel-title">
              <span>📈</span>
              <strong>任务完成效率变化</strong>
            </div>
          </template>
          <a-empty v-if="review.taskEfficiencyTrends.length === 0" description="暂无效率趋势数据"/>
          <div v-else class="efficiency-list">
            <div v-for="item in review.taskEfficiencyTrends" :key="item.date" class="efficiency-row">
              <span>{{ item.date }}</span>
              <a-progress
                  class="efficiency-progress"
                  :percent="calculateEfficiencyPercent(item)"
                  :format="() => `${item.completedTaskCount} 个 / ${item.actualMinutes} 分钟`"
              />
            </div>
          </div>
        </a-card>

        <a-card :bordered="false" class="review-panel suggestion-panel">
          <template #title>
            <div class="review-panel-title">
              <span>💡</span>
              <strong>目标调整建议</strong>
            </div>
          </template>
          <template #extra>
            <a-button type="primary" size="small" shape="round" @click="handleGenerateTodo()">
              <template #icon>
                <PlusOutlined/>
              </template>
              生成跟进 Todo
            </a-button>
          </template>
          <a-empty v-if="review.adjustmentSuggestions.length === 0" description="暂无需要调整的目标"/>
          <div v-else class="suggestion-list">
            <div v-for="item in review.adjustmentSuggestions" :key="item.goalId || item.goalTitle" class="suggestion-item">
              <div>
                <div class="suggestion-title">{{ item.goalTitle }}</div>
                <p>{{ item.suggestionContent }}</p>
              </div>
              <a-button type="link" size="small" @click="handleGenerateTodo(item.suggestionContent)">
                生成 Todo
              </a-button>
            </div>
          </div>
        </a-card>
      </div>

      <a-card :bordered="false" class="review-panel goal-achievement-panel">
        <template #title>
          <div class="review-panel-title">
            <span>🎯</span>
            <strong>目标达成率分析</strong>
          </div>
        </template>
        <a-empty v-if="review.goalAchievementStats.length === 0" description="暂无目标达成分析数据"/>
        <div v-else class="goal-achievement-grid">
          <div
              v-for="record in review.goalAchievementStats"
              :key="record.goalId || record.goalTitle"
              class="goal-achievement-card"
              :class="{ 'goal-achievement-risk': record.needAdjustment }"
          >
            <div class="goal-achievement-header">
              <div>
                <strong>{{ record.goalTitle }}</strong>
                <span :style="{ '--chip-color': record.categoryColor || '#1677ff' }">
                  {{ record.categoryName || '未分类' }}
                </span>
              </div>
              <em>{{ record.needAdjustment ? '需关注' : '正常' }}</em>
            </div>
            <a-progress :percent="record.progress || 0" size="small"/>
            <div class="goal-achievement-meta">
              <span>进展记录 {{ record.progressRecordCount || 0 }}</span>
              <span>完成任务 {{ record.completedTaskCount || 0 }}</span>
            </div>
          </div>
        </div>
      </a-card>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import {computed, onMounted, reactive, ref} from "vue";
import {message, Modal} from "ant-design-vue";
import {useRouter} from "vue-router";
import {DesktopOutlined, InboxOutlined, PlusOutlined} from "@ant-design/icons-vue";
import {queryReviewSummary} from "@/api/shanzhu/review/Review.ts";
import type {ShanzhuReview, ShanzhuReviewQuery, TaskEfficiencyTrend} from "@/api/shanzhu/review/type/Review.ts";
import {
  queryHabitCheckins,
  queryHabitList,
  queryHabitStats
} from "@/api/shanzhu/habit/Habit.ts";
import type {
  ShanzhuHabitCheckinVO,
  ShanzhuHabitStatsVO,
  ShanzhuHabitVO
} from "@/api/shanzhu/habit/type/Habit.ts";
import {saveTodo} from "@/api/shanzhu/todo/Todo.ts";

const router = useRouter();
const pageLoading = ref(false);
const dateRange = ref<string[]>([]);
const habitList = ref<ShanzhuHabitVO[]>([]);
const habitCheckinRecords = ref<ShanzhuHabitCheckinVO[]>([]);

const habitStats = reactive<ShanzhuHabitStatsVO>({
  todayTotalCount: 0,
  todayCheckedCount: 0,
  currentPeriodHabitCount: 0,
  currentPeriodCompletedHabitCount: 0,
  currentPeriodUncompletedHabitCount: 0,
  currentPeriodCompletionRate: 0,
  maxContinuousDays: 0
});

const reviewQuery = reactive<ShanzhuReviewQuery>({
  reviewType: 'week'
});

const review = reactive<ShanzhuReview>({
  reviewType: 'week',
  startDate: '',
  endDate: '',
  overview: {
    completedTaskCount: 0,
    progressRecordCount: 0,
    progressedGoalCount: 0,
    completedGoalCount: 0,
    totalEstimatedMinutes: 0,
    totalActualMinutes: 0,
    averageEfficiencyRate: 0,
    newTodoCount: 0,
    completedTodoCount: 0,
    convertedTodoCount: 0,
    longPendingTodoCount: 0
  },
  categoryInvestmentStats: [],
  tagAnalysisStats: [],
  taskEfficiencyTrends: [],
  goalAchievementStats: [],
  adjustmentSuggestions: [],
  reviewSummary: []
});

const reviewTypeOptions = [
  {
    label: '周复盘',
    value: 'week'
  },
  {
    label: '月复盘',
    value: 'month'
  }
];

const reviewTitle = computed(() => review.reviewType === 'month' ? '月复盘报告' : '周复盘报告');

const overviewCards = computed(() => [
  {
    title: '完成任务',
    value: review.overview.completedTaskCount,
    suffix: '个',
    color: '#1677ff',
    description: `推进目标 ${review.overview.progressedGoalCount} 个`
  },
  {
    title: '进展记录',
    value: review.overview.progressRecordCount,
    suffix: '条',
    color: '#52c41a',
    description: `完成目标 ${review.overview.completedGoalCount} 个`
  },
  {
    title: '实际投入',
    value: review.overview.totalActualMinutes,
    suffix: '分钟',
    color: '#fa8c16',
    description: `预计 ${review.overview.totalEstimatedMinutes} 分钟`
  },
  {
    title: '效率比',
    value: review.overview.averageEfficiencyRate,
    suffix: '',
    color: '#722ed1',
    description: '实际耗时 / 预计耗时'
  },
  {
    title: '新增 Todo',
    value: review.overview.newTodoCount || 0,
    suffix: '个',
    color: '#13c2c2',
    description: `完成 ${review.overview.completedTodoCount || 0} / 转任务 ${review.overview.convertedTodoCount || 0}`
  },
  {
    title: '长期未处理',
    value: review.overview.longPendingTodoCount || 0,
    suffix: '个',
    color: review.overview.longPendingTodoCount > 0 ? '#f5222d' : '#8c8c8c',
    description: '超过7天未处理'
  }
]);

const maxActualMinutes = computed(() => {
  const values = review.taskEfficiencyTrends.map(item => item.actualMinutes);
  return Math.max(...values, 1);
});

const habitCompletionAnalysis = computed(() => {
  return habitList.value
      .map(habit => ({
        id: habit.id,
        title: habit.title || '-',
        goalTitle: habit.goalTitle,
        checkedCount: habit.currentPeriodCheckedCount || 0,
        targetCount: habit.currentPeriodTargetCount || 0,
        completionRate: formatHabitCompletionRate(habit.currentPeriodCompletionRate),
        continuousDays: habit.continuousDays || 0,
        targetValue: habit.targetValue,
        unit: habit.unit
      }))
      .sort((firstItem, secondItem) => secondItem.completionRate - firstItem.completionRate)
      .slice(0, 6);
});

const habitCheckinInsight = computed(() => {
  const checkedHabitIds = new Set(habitCheckinRecords.value.map(item => item.habitId).filter(Boolean));
  const reviewDays = calculateReviewDays(review.startDate, review.endDate);
  return {
    totalCheckinCount: habitCheckinRecords.value.length,
    checkedHabitCount: checkedHabitIds.size,
    averageDailyCheckinCount: Number((habitCheckinRecords.value.length / reviewDays).toFixed(1))
  };
});

const loadHabitReviewData = async (startDate?: string, endDate?: string) => {
  const [habitStatsResponse, habitListResponse] = await Promise.all([
    queryHabitStats(),
    queryHabitList({})
  ]);

  if (habitStatsResponse.code === 200) {
    Object.assign(habitStats, habitStatsResponse.data || {});
  } else {
    message.error(habitStatsResponse.msg || "习惯统计加载失败");
  }

  if (habitListResponse.code !== 200) {
    message.error(habitListResponse.msg || "习惯列表加载失败");
    habitList.value = [];
    habitCheckinRecords.value = [];
    return;
  }

  habitList.value = habitListResponse.data || [];

  const queryStartDate = startDate || review.startDate;
  const queryEndDate = endDate || review.endDate;
  const habitCheckinResponses = await Promise.all(
      habitList.value
          .filter(habit => Boolean(habit.id))
          .map(habit => queryHabitCheckins(habit.id || "", {
            startDate: queryStartDate,
            endDate: queryEndDate
          }))
  );

  habitCheckinRecords.value = habitCheckinResponses
      .flatMap(response => response.code === 200 ? response.data || [] : [])
      .sort((firstItem, secondItem) => (secondItem.checkinDate || '').localeCompare(firstItem.checkinDate || ''));
};

const initReview = async () => {
  pageLoading.value = true;
  try {
    const response = await queryReviewSummary(reviewQuery);
    const reviewData = response.data;
    Object.assign(review, {
      ...reviewData,
      overview: reviewData.overview || review.overview,
      categoryInvestmentStats: reviewData.categoryInvestmentStats || [],
      tagAnalysisStats: reviewData.tagAnalysisStats || [],
      taskEfficiencyTrends: reviewData.taskEfficiencyTrends || [],
      goalAchievementStats: reviewData.goalAchievementStats || [],
      adjustmentSuggestions: reviewData.adjustmentSuggestions || [],
      reviewSummary: reviewData.reviewSummary || []
    });
    dateRange.value = reviewData.startDate && reviewData.endDate ? [reviewData.startDate, reviewData.endDate] : [];
    await loadHabitReviewData(reviewData.startDate, reviewData.endDate);
  } catch (error) {
    message.error('复盘报告生成失败');
  } finally {
    pageLoading.value = false;
  }
};

const handleReviewTypeChange = () => {
  reviewQuery.startDate = undefined;
  reviewQuery.endDate = undefined;
  dateRange.value = [];
  initReview();
};

const handleDateRangeChange = () => {
  reviewQuery.startDate = dateRange.value?.[0];
  reviewQuery.endDate = dateRange.value?.[1];
};

const formatPercent = (value: number) => {
  return Math.round((value || 0) * 100);
};

const formatHabitCompletionRate = (value?: number) => {
  return Math.min(Math.max(Math.round(Number(value || 0)), 0), 100);
};

const formatHabitTargetValue = (targetValue?: number, unit?: string) => {
  if (targetValue === undefined || targetValue === null) {
    return unit || "-";
  }
  return `${targetValue}${unit || ""}`;
};

const calculateReviewDays = (startDate?: string, endDate?: string) => {
  if (!startDate || !endDate) {
    return 1;
  }

  const startTime = new Date(startDate).getTime();
  const endTime = new Date(endDate).getTime();
  const oneDayMilliseconds = 24 * 60 * 60 * 1000;
  return Math.max(Math.floor((endTime - startTime) / oneDayMilliseconds) + 1, 1);
};

const calculateEfficiencyPercent = (item: TaskEfficiencyTrend) => {
  return Math.round(((item.actualMinutes || 0) / maxActualMinutes.value) * 100);
};

const handleGenerateTodo = (suggestionContent?: string) => {
  const defaultContent = suggestionContent || "根据复盘结果，需要跟进的事项";
  Modal.confirm({
    title: "生成 Todo",
    content: "将复盘建议转为 Todo，进入收集箱后续处理",
    okText: "确认生成",
    cancelText: "取消",
    onOk: async () => {
      const response = await saveTodo({
        title: defaultContent,
        status: "inbox"
      });
      if (response.code === 200) {
        message.success("已生成 Todo，请前往收集箱查看");
      } else {
        message.error(response.msg || "生成失败");
      }
    }
  });
};

const goToTodo = () => {
  router.push("/shanzhu/todo");
};

const goToTodayWork = () => {
  router.push("/shanzhu/today-work");
};

onMounted(() => {
  initReview();
});
</script>

<style scoped lang="scss">
.shanzhu-review-page {
  max-width: 1360px;
  min-height: calc(100vh - 120px);
  margin: 0 auto;
  padding: 36px 48px 56px;
  overflow-x: hidden;

  .review-hero {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 24px;
    padding: 26px 28px;
    margin-bottom: 18px;
    overflow: hidden;
    border: 1px solid rgba(114, 46, 209, 0.08);
    border-radius: 28px;
    background:
      radial-gradient(circle at 12% 0%, rgba(114, 46, 209, 0.12), transparent 34%),
      radial-gradient(circle at 92% 18%, rgba(22, 119, 255, 0.10), transparent 30%),
      linear-gradient(135deg, rgba(255, 255, 255, 0.98), rgba(248, 250, 255, 0.94));
    box-shadow: 0 18px 45px rgba(34, 20, 70, 0.07);
  }

  .review-hero-content {
    min-width: 240px;
  }

  .review-eyebrow {
    margin-bottom: 8px;
    color: #722ed1;
    font-size: 12px;
    font-weight: 850;
    letter-spacing: 0.8px;
    text-transform: uppercase;
  }

  .review-page-title {
    margin: 0;
    color: rgba(0, 0, 0, 0.88);
    font-size: 30px;
    font-weight: 850;
    line-height: 1.2;
    letter-spacing: -0.7px;
  }

  .review-page-desc {
    margin: 8px 0 0;
    color: rgba(0, 0, 0, 0.48);
    font-size: 14px;
  }

  .review-hero-actions {
    display: flex;
    flex-wrap: wrap;
    justify-content: flex-end;
    gap: 10px;

    :deep(.ant-segmented) {
      padding: 4px;
      border-radius: 999px;
      background: rgba(15, 35, 80, 0.055);
    }

    :deep(.ant-picker),
    :deep(.ant-btn) {
      height: 38px;
      border-radius: 999px;
    }

    :deep(.ant-btn-primary) {
      box-shadow: 0 10px 24px rgba(114, 46, 209, 0.18);
    }
  }

  .review-report-card,
  .review-panel {
    border: 1px solid rgba(15, 35, 80, 0.06);
    border-radius: 24px;
    background: rgba(255, 255, 255, 0.95);
    box-shadow: 0 18px 45px rgba(15, 35, 80, 0.065), 0 1px 2px rgba(15, 35, 80, 0.04);
  }

  .review-report-card {
    padding: 22px;
    margin-bottom: 18px;
  }

  .review-report-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    margin-bottom: 18px;

    h3 {
      margin: 6px 0 0;
      color: rgba(0, 0, 0, 0.84);
      font-size: 22px;
      font-weight: 850;
      line-height: 1.2;
    }
  }

  .review-report-label {
    display: inline-flex;
    padding: 4px 10px;
    border-radius: 999px;
    background: rgba(114, 46, 209, 0.10);
    color: #722ed1;
    font-size: 12px;
    font-weight: 850;
  }

  .review-overview-grid {
    display: grid;
    grid-template-columns: repeat(6, minmax(0, 1fr));
    gap: 12px;
  }

  .review-overview-card {
    position: relative;
    min-height: 122px;
    padding: 16px;
    overflow: hidden;
    border-radius: 18px;
    background:
      radial-gradient(circle at 86% 12%, color-mix(in srgb, var(--card-color) 15%, transparent), transparent 34%),
      rgba(15, 35, 80, 0.036);

    span {
      color: rgba(0, 0, 0, 0.48);
      font-size: 12px;
      font-weight: 800;
    }

    strong {
      display: block;
      margin: 14px 0 7px;
      color: var(--card-color);
      font-size: 28px;
      font-weight: 900;
      line-height: 1;

      em {
        margin-left: 3px;
        font-size: 12px;
        font-style: normal;
        font-weight: 750;
      }
    }

    p {
      display: -webkit-box;
      margin: 0;
      overflow: hidden;
      color: rgba(0, 0, 0, 0.43);
      font-size: 12px;
      line-height: 18px;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
    }
  }

  .review-summary-list {
    display: grid;
    gap: 10px;
    margin-top: 16px;
  }

  .review-empty-tip,
  .review-summary-item {
    border-radius: 16px;
    background: rgba(15, 35, 80, 0.04);
  }

  .review-empty-tip {
    padding: 18px;
    color: rgba(0, 0, 0, 0.42);
    font-size: 13px;
    text-align: center;
  }

  .review-summary-item {
    display: flex;
    align-items: flex-start;
    gap: 10px;
    padding: 13px 14px;

    span {
      flex-shrink: 0;
    }

    p {
      margin: 0;
      color: rgba(0, 0, 0, 0.70);
      font-size: 14px;
      line-height: 22px;
    }
  }

  .review-section-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 18px;
    margin-bottom: 18px;
  }

  .review-panel {
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
    }

    :deep(.ant-empty) {
      padding: 32px 12px;
    }
  }

  .review-panel-title {
    display: flex;
    align-items: center;
    gap: 8px;
    min-width: 0;

    > span {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 28px;
      height: 28px;
      border-radius: 11px;
      background: rgba(114, 46, 209, 0.09);
    }

    strong {
      color: rgba(0, 0, 0, 0.84);
      font-size: 16px;
      font-weight: 850;
    }
  }

  .habit-headline {
    display: flex;
    align-items: center;
    gap: 18px;
    margin-bottom: 16px;
  }

  .habit-rate-orb {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    width: 112px;
    height: 112px;
    flex-shrink: 0;
    border-radius: 34px;
    background:
      radial-gradient(circle at 28% 20%, rgba(255, 255, 255, 0.88), transparent 34%),
      linear-gradient(135deg, rgba(114, 46, 209, 0.92), rgba(22, 119, 255, 0.82));
    box-shadow: 0 18px 34px rgba(114, 46, 209, 0.18);
    color: #fff;

    strong {
      font-size: 26px;
      font-weight: 900;
      line-height: 1;
    }

    span {
      margin-top: 8px;
      color: rgba(255, 255, 255, 0.84);
      font-size: 12px;
      font-weight: 750;
    }
  }

  .habit-stat-grid,
  .checkin-stat-grid {
    display: grid;
    gap: 10px;
  }

  .habit-stat-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    flex: 1;

    div {
      padding: 11px 12px;
      border-radius: 15px;
      background: rgba(15, 35, 80, 0.045);
    }
  }

  .checkin-stat-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
    margin-bottom: 14px;

    div {
      padding: 14px;
      border-radius: 16px;
      background: rgba(15, 35, 80, 0.045);
    }

    small {
      margin-left: 3px;
      color: rgba(0, 0, 0, 0.44);
      font-size: 12px;
      font-weight: 700;
    }
  }

  .habit-stat-grid span,
  .checkin-stat-grid span {
    display: block;
    margin-bottom: 7px;
    color: rgba(0, 0, 0, 0.44);
    font-size: 12px;
    font-weight: 750;
  }

  .habit-stat-grid strong,
  .checkin-stat-grid strong {
    color: rgba(0, 0, 0, 0.80);
    font-size: 20px;
    font-weight: 900;
    line-height: 1;
  }

  .habit-analysis-list,
  .checkin-list,
  .investment-list,
  .tag-analysis-list,
  .efficiency-list,
  .suggestion-list {
    display: flex;
    flex-direction: column;
  }

  .habit-analysis-item,
  .checkin-item,
  .investment-item,
  .tag-analysis-item,
  .suggestion-item {
    border-bottom: 1px solid rgba(15, 35, 80, 0.055);

    &:last-child {
      border-bottom: none;
    }
  }

  .habit-analysis-item {
    display: grid;
    grid-template-columns: minmax(0, 1fr) 160px;
    gap: 14px;
    align-items: center;
    padding: 14px 0;
  }

  .habit-analysis-title,
  .investment-header,
  .tag-analysis-header,
  .suggestion-title {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 10px;
    min-width: 0;
  }

  .habit-analysis-title span,
  .checkin-item strong,
  .suggestion-title,
  .goal-achievement-header strong {
    display: -webkit-box;
    overflow: hidden;
    color: rgba(0, 0, 0, 0.82);
    font-size: 14px;
    font-weight: 800;
    line-height: 22px;
    word-break: break-word;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }

  .habit-analysis-title em,
  .investment-header em,
  .tag-analysis-footer em,
  .goal-achievement-header em {
    flex-shrink: 0;
    color: rgba(0, 0, 0, 0.44);
    font-size: 12px;
    font-style: normal;
    font-weight: 750;
  }

  .habit-analysis-meta,
  .investment-meta,
  .goal-achievement-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    margin-top: 7px;
  }

  .habit-analysis-meta span,
  .investment-meta span,
  .checkin-tags span,
  .goal-achievement-meta span,
  .goal-achievement-header span {
    display: inline-flex;
    align-items: center;
    max-width: 180px;
    min-height: 24px;
    padding: 3px 9px;
    overflow: hidden;
    border-radius: 999px;
    background: rgba(15, 35, 80, 0.055);
    color: rgba(0, 0, 0, 0.50);
    font-size: 12px;
    font-weight: 700;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .habit-analysis-progress {
    width: 160px;
  }

  .checkin-item {
    display: flex;
    justify-content: space-between;
    gap: 12px;
    padding: 13px 0;

    p {
      margin: 5px 0 0;
      color: rgba(0, 0, 0, 0.44);
      font-size: 12px;
      line-height: 18px;
    }
  }

  .checkin-tags {
    display: flex;
    flex-shrink: 0;
    flex-direction: column;
    align-items: flex-end;
    gap: 6px;
  }

  .investment-item,
  .tag-analysis-item {
    padding: 14px 0;
  }

  .investment-chip,
  .tag-chip,
  .goal-achievement-header span {
    display: inline-flex;
    align-items: center;
    max-width: 220px;
    min-height: 26px;
    padding: 4px 10px;
    overflow: hidden;
    border-radius: 999px;
    background: color-mix(in srgb, var(--chip-color) 13%, transparent);
    color: var(--chip-color);
    font-size: 12px;
    font-weight: 800;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .investment-meta {
    margin-bottom: 10px;
  }

  .tag-analysis-header > span:last-child {
    flex-shrink: 0;
    color: rgba(0, 0, 0, 0.44);
    font-size: 12px;
    font-weight: 750;
  }

  .tag-analysis-footer {
    display: grid;
    grid-template-columns: minmax(0, 1fr) 70px;
    align-items: center;
    gap: 10px;
    margin-top: 10px;
  }

  .efficiency-row {
    display: grid;
    grid-template-columns: 92px minmax(0, 1fr);
    align-items: center;
    gap: 12px;
    padding: 8px 0;

    > span {
      color: rgba(0, 0, 0, 0.50);
      font-size: 12px;
      font-weight: 750;
    }
  }

  .efficiency-progress {
    min-width: 0;
  }

  .suggestion-item {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 14px;
    padding: 14px 0;

    p {
      margin: 6px 0 0;
      color: rgba(0, 0, 0, 0.48);
      font-size: 13px;
      line-height: 20px;
    }
  }

  .goal-achievement-panel {
    margin-bottom: 0;
  }

  .goal-achievement-grid {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 14px;
  }

  .goal-achievement-card {
    padding: 16px;
    border: 1px solid rgba(15, 35, 80, 0.055);
    border-radius: 18px;
    background:
      radial-gradient(circle at 8% 0%, rgba(22, 119, 255, 0.07), transparent 32%),
      rgba(255, 255, 255, 0.72);
  }

  .goal-achievement-risk {
    background:
      radial-gradient(circle at 8% 0%, rgba(250, 140, 22, 0.09), transparent 32%),
      rgba(255, 255, 255, 0.72);

    .goal-achievement-header em {
      color: #fa8c16;
    }
  }

  .goal-achievement-header {
    display: flex;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 12px;

    > div {
      min-width: 0;
    }

    span {
      width: fit-content;
      margin-top: 8px;
    }
  }

  .goal-achievement-meta {
    margin-top: 10px;
  }

  :deep(.ant-progress-inner) {
    background: rgba(15, 35, 80, 0.06);
  }

  @media (max-width: 1280px) {
    padding: 28px 28px 48px;

    .review-overview-grid {
      grid-template-columns: repeat(3, minmax(0, 1fr));
    }

    .goal-achievement-grid {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }
  }

  @media (max-width: 980px) {
    .review-hero,
    .review-report-header {
      flex-direction: column;
      align-items: stretch;
    }

    .review-hero-actions {
      justify-content: flex-start;
    }

    .review-section-grid {
      grid-template-columns: 1fr;
    }
  }

  @media (max-width: 720px) {
    .review-overview-grid,
    .goal-achievement-grid {
      grid-template-columns: 1fr;
    }

    .habit-headline {
      flex-direction: column;
      align-items: flex-start;
    }

    .habit-stat-grid,
    .checkin-stat-grid {
      grid-template-columns: 1fr;
    }

    .habit-analysis-item,
    .tag-analysis-footer,
    .efficiency-row {
      grid-template-columns: 1fr;
    }

    .habit-analysis-progress {
      width: 100%;
    }

    .checkin-item,
    .suggestion-item {
      flex-direction: column;
    }

    .checkin-tags {
      align-items: flex-start;
    }
  }

  @media (max-width: 640px) {
    padding: 18px 14px 36px;

    .review-hero,
    .review-report-card {
      padding: 22px 20px;
      border-radius: 22px;
    }

    .review-page-title {
      font-size: 26px;
    }

    .review-hero-actions {
      :deep(.ant-picker),
      :deep(.ant-btn),
      :deep(.ant-segmented) {
        width: 100%;
      }
    }

    .review-overview-card strong {
      font-size: 26px;
    }

    .habit-analysis-title,
    .investment-header,
    .tag-analysis-header {
      flex-direction: column;
      align-items: flex-start;
    }
  }
}
</style>
