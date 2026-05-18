<template>
  <div class="shanzhu-review-page">
    <a-flex :gap="16" vertical>
      <a-card :bordered="false" class="review-filter-card">
        <a-flex justify="space-between" align="center" wrap="wrap" :gap="16">
          <div>
            <a-typography-title :level="4" class="review-title">目标复盘</a-typography-title>
            <a-typography-text type="secondary">生成周复盘和月复盘，分析投入方向、任务效率、习惯坚持和目标调整建议</a-typography-text>
          </div>
          <a-space size="small" wrap>
            <a-segmented v-model:value="reviewQuery.reviewType" :options="reviewTypeOptions" @change="handleReviewTypeChange"/>
            <a-range-picker v-model:value="dateRange" value-format="YYYY-MM-DD" @change="handleDateRangeChange"/>
            <a-button type="primary" :loading="pageLoading" @click="initReview">生成复盘</a-button>
          </a-space>
        </a-flex>
      </a-card>

      <a-spin :spinning="pageLoading">
        <a-card :bordered="false" class="review-summary-card">
          <template #title>
            <a-flex justify="space-between" align="center" wrap="wrap" :gap="12">
              <span>{{ reviewTitle }}</span>
              <a-tag color="blue">{{ review.startDate }} ~ {{ review.endDate }}</a-tag>
            </a-flex>
          </template>
          <a-row :gutter="[16, 16]">
            <a-col v-for="item in overviewCards" :key="item.title" :xs="24" :sm="12" :lg="6">
              <a-statistic :title="item.title" :value="item.value" :suffix="item.suffix" :value-style="{ color: item.color }"/>
              <a-typography-text type="secondary">{{ item.description }}</a-typography-text>
            </a-col>
          </a-row>

          <a-divider/>

          <a-list :data-source="review.reviewSummary" size="small">
            <template #renderItem="{ item }">
              <a-list-item>
                <a-typography-text>{{ item }}</a-typography-text>
              </a-list-item>
            </template>
          </a-list>
        </a-card>

        <a-row :gutter="[16, 16]" class="review-section">
          <a-col :xs="24" :xl="12">
            <a-card :bordered="false" title="习惯完成分析">
              <a-row :gutter="[12, 12]">
                <a-col :span="12">
                  <a-statistic title="当前周期习惯" :value="habitStats.currentPeriodHabitCount || 0" suffix="个"/>
                </a-col>
                <a-col :span="12">
                  <a-statistic title="周期已完成" :value="habitStats.currentPeriodCompletedHabitCount || 0" suffix="个" :value-style="{ color: '#52c41a' }"/>
                </a-col>
                <a-col :span="12">
                  <a-statistic title="周期未完成" :value="habitStats.currentPeriodUncompletedHabitCount || 0" suffix="个"/>
                </a-col>
                <a-col :span="12">
                  <a-statistic title="最长连续" :value="habitStats.maxContinuousDays || 0" suffix="天"/>
                </a-col>
              </a-row>

              <a-divider/>

              <a-flex vertical :gap="8">
                <a-flex justify="space-between" align="center">
                  <a-typography-text type="secondary">当前周期完成率</a-typography-text>
                  <a-typography-text strong>{{ formatHabitCompletionRate(habitStats.currentPeriodCompletionRate) }}%</a-typography-text>
                </a-flex>
                <a-progress :percent="formatHabitCompletionRate(habitStats.currentPeriodCompletionRate)" size="small"/>
              </a-flex>

              <a-divider/>

              <a-empty v-if="habitCompletionAnalysis.length === 0" description="暂无习惯完成数据"/>
              <a-list v-else :data-source="habitCompletionAnalysis" size="small">
                <template #renderItem="{ item }">
                  <a-list-item class="habit-analysis-item">
                    <a-list-item-meta>
                      <template #title>
                        <a-space size="small" wrap>
                          <span>{{ item.title }}</span>
                          <a-tag :color="item.completionRate >= 100 ? 'success' : 'processing'">
                            {{ item.checkedCount }}/{{ item.targetCount }} 次
                          </a-tag>
                          <a-tag v-if="item.goalTitle" color="blue">{{ item.goalTitle }}</a-tag>
                        </a-space>
                      </template>
                      <template #description>
                        连续 {{ item.continuousDays }} 天，目标值 {{ formatHabitTargetValue(item.targetValue, item.unit) }}
                      </template>
                    </a-list-item-meta>
                    <a-progress class="habit-analysis-progress" :percent="item.completionRate" size="small"/>
                  </a-list-item>
                </template>
              </a-list>
            </a-card>
          </a-col>

          <a-col :xs="24" :xl="12">
            <a-card :bordered="false" title="习惯打卡记录洞察">
              <a-row :gutter="[12, 12]">
                <a-col :span="8">
                  <a-statistic title="周期打卡" :value="habitCheckinInsight.totalCheckinCount" suffix="次"/>
                </a-col>
                <a-col :span="8">
                  <a-statistic title="覆盖习惯" :value="habitCheckinInsight.checkedHabitCount" suffix="个"/>
                </a-col>
                <a-col :span="8">
                  <a-statistic title="平均每日" :value="habitCheckinInsight.averageDailyCheckinCount" suffix="次"/>
                </a-col>
              </a-row>

              <a-divider/>

              <a-empty v-if="habitCheckinRecords.length === 0" description="当前复盘周期内暂无习惯打卡记录"/>
              <a-list v-else :data-source="habitCheckinRecords.slice(0, 8)" size="small">
                <template #renderItem="{ item }">
                  <a-list-item class="habit-checkin-item">
                    <a-list-item-meta>
                      <template #title>
                        <a-space size="small" wrap>
                          <span>{{ item.habitTitle }}</span>
                          <a-tag color="green">{{ item.checkinDate }}</a-tag>
                          <a-tag v-if="item.goalTitle" color="blue">{{ item.goalTitle }}</a-tag>
                        </a-space>
                      </template>
                      <template #description>
                        完成值：{{ formatHabitTargetValue(item.actualValue, item.unit) }}
                        <template v-if="item.note">，备注：{{ item.note }}</template>
                      </template>
                    </a-list-item-meta>
                  </a-list-item>
                </template>
              </a-list>
            </a-card>
          </a-col>
        </a-row>

        <a-row :gutter="[16, 16]" class="review-section">
          <a-col :xs="24" :xl="12">
            <a-card :bordered="false" title="分类长期投入情况">
              <a-empty v-if="review.categoryInvestmentStats.length === 0" description="暂无分类投入数据"/>
              <a-table
                  v-else
                  :columns="categoryColumns"
                  :data-source="review.categoryInvestmentStats"
                  :pagination="false"
                  row-key="categoryId"
                  size="middle"
              >
                <template #bodyCell="{ column, record }">
                  <template v-if="column.key === 'categoryName'">
                    <a-tag :color="record.categoryColor || 'blue'">{{ record.categoryName || '未分类' }}</a-tag>
                  </template>
                  <template v-if="column.key === 'investmentRatio'">
                    <a-progress :percent="formatPercent(record.investmentRatio)" size="small"/>
                  </template>
                </template>
              </a-table>
            </a-card>
          </a-col>

          <a-col :xs="24" :xl="12">
            <a-card :bordered="false" title="标签维度任务类型分析">
              <a-empty v-if="review.tagAnalysisStats.length === 0" description="暂无标签分析数据"/>
              <a-table
                  v-else
                  :columns="tagColumns"
                  :data-source="review.tagAnalysisStats"
                  :pagination="false"
                  row-key="tagId"
                  size="middle"
              >
                <template #bodyCell="{ column, record }">
                  <template v-if="column.key === 'tagName'">
                    <a-tag :color="record.tagColor || 'blue'">{{ record.tagName || '未知标签' }}</a-tag>
                  </template>
                  <template v-if="column.key === 'completionRate'">
                    <a-progress :percent="formatPercent(record.completionRate)" size="small"/>
                  </template>
                </template>
              </a-table>
            </a-card>
          </a-col>
        </a-row>

        <a-row :gutter="[16, 16]" class="review-section">
          <a-col :xs="24" :xl="12">
            <a-card :bordered="false" title="任务完成效率变化">
              <a-empty v-if="review.taskEfficiencyTrends.length === 0" description="暂无效率趋势数据"/>
              <a-flex v-else vertical :gap="12">
                <div v-for="item in review.taskEfficiencyTrends" :key="item.date" class="trend-row">
                  <a-flex justify="space-between" align="center" :gap="12">
                    <span class="trend-date">{{ item.date }}</span>
                    <a-progress
                        class="trend-progress"
                        :percent="calculateEfficiencyPercent(item)"
                        :format="() => `${item.completedTaskCount} 个 / ${item.actualMinutes} 分钟`"
                    />
                  </a-flex>
                </div>
              </a-flex>
            </a-card>
          </a-col>

          <a-col :xs="24" :xl="12">
            <a-card :bordered="false" title="目标调整建议">
              <a-empty v-if="review.adjustmentSuggestions.length === 0" description="暂无需要调整的目标"/>
              <a-list v-else :data-source="review.adjustmentSuggestions">
                <template #renderItem="{ item }">
                  <a-list-item>
                    <a-list-item-meta>
                      <template #title>
                        <a-space size="small" wrap>
                          <span>{{ item.goalTitle }}</span>
                          <a-tag color="orange">建议调整</a-tag>
                        </a-space>
                      </template>
                      <template #description>
                        {{ item.suggestionContent }}
                      </template>
                    </a-list-item-meta>
                  </a-list-item>
                </template>
              </a-list>
            </a-card>
          </a-col>
        </a-row>

        <a-card :bordered="false" class="review-section" title="目标达成率分析">
          <a-empty v-if="review.goalAchievementStats.length === 0" description="暂无目标达成分析数据"/>
          <a-table
              v-else
              :columns="goalColumns"
              :data-source="review.goalAchievementStats"
              :pagination="{ pageSize: 8 }"
              row-key="goalId"
              size="middle"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'categoryName'">
                <a-tag :color="record.categoryColor || 'blue'">{{ record.categoryName || '未分类' }}</a-tag>
              </template>
              <template v-if="column.key === 'progress'">
                <a-progress :percent="record.progress || 0" size="small"/>
              </template>
              <template v-if="column.key === 'needAdjustment'">
                <a-tag :color="record.needAdjustment ? 'orange' : 'green'">
                  {{ record.needAdjustment ? '需关注' : '正常' }}
                </a-tag>
              </template>
            </template>
          </a-table>
        </a-card>
      </a-spin>
    </a-flex>
  </div>
</template>

<script setup lang="ts">
import {computed, onMounted, reactive, ref} from "vue";
import {message} from "ant-design-vue";
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
    averageEfficiencyRate: 0
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

const categoryColumns = [
  {
    title: '分类',
    dataIndex: 'categoryName',
    key: 'categoryName'
  },
  {
    title: '完成任务',
    dataIndex: 'completedTaskCount',
    key: 'completedTaskCount'
  },
  {
    title: '投入分钟',
    dataIndex: 'actualMinutes',
    key: 'actualMinutes'
  },
  {
    title: '投入占比',
    key: 'investmentRatio'
  },
  {
    title: '完成目标',
    dataIndex: 'completedGoalCount',
    key: 'completedGoalCount'
  }
];

const tagColumns = [
  {
    title: '标签',
    dataIndex: 'tagName',
    key: 'tagName'
  },
  {
    title: '任务数',
    dataIndex: 'taskCount',
    key: 'taskCount'
  },
  {
    title: '完成率',
    key: 'completionRate'
  },
  {
    title: '投入分钟',
    dataIndex: 'actualMinutes',
    key: 'actualMinutes'
  }
];

const goalColumns = [
  {
    title: '目标',
    dataIndex: 'goalTitle',
    key: 'goalTitle'
  },
  {
    title: '分类',
    dataIndex: 'categoryName',
    key: 'categoryName'
  },
  {
    title: '进度',
    key: 'progress'
  },
  {
    title: '进展记录',
    dataIndex: 'progressRecordCount',
    key: 'progressRecordCount'
  },
  {
    title: '完成任务',
    dataIndex: 'completedTaskCount',
    key: 'completedTaskCount'
  },
  {
    title: '状态',
    key: 'needAdjustment'
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

onMounted(() => {
  initReview();
});
</script>

<style scoped lang="scss">
.shanzhu-review-page {
  .review-filter-card,
  .review-summary-card,
  .review-section :deep(.ant-card),
  .review-section {
    border-radius: 12px;
  }

  .review-title {
    margin-bottom: 4px;
  }

  .review-section {
    margin-top: 16px;
  }

  .trend-row,
  .habit-analysis-item,
  .habit-checkin-item {
    width: 100%;
  }

  .habit-analysis-progress {
    width: 160px;
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
