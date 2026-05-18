<template>
  <div class="shanzhu-review-page">
    <a-flex :gap="16" vertical>
      <a-card :bordered="false" class="review-filter-card">
        <a-flex justify="space-between" align="center" wrap="wrap" :gap="16">
          <div>
            <a-typography-title :level="4" class="review-title">目标复盘</a-typography-title>
            <a-typography-text type="secondary">生成周复盘和月复盘，分析投入方向、任务效率和目标调整建议</a-typography-text>
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

const pageLoading = ref(false);
const dateRange = ref<string[]>([]);

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

const initReview = async () => {
  pageLoading.value = true;
  try {
    const data = await queryReviewSummary(reviewQuery);
    Object.assign(review, {
      ...data,
      overview: data.overview || review.overview,
      categoryInvestmentStats: data.categoryInvestmentStats || [],
      tagAnalysisStats: data.tagAnalysisStats || [],
      taskEfficiencyTrends: data.taskEfficiencyTrends || [],
      goalAchievementStats: data.goalAchievementStats || [],
      adjustmentSuggestions: data.adjustmentSuggestions || [],
      reviewSummary: data.reviewSummary || []
    });
    dateRange.value = data.startDate && data.endDate ? [data.startDate, data.endDate] : [];
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

  .trend-row {
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
