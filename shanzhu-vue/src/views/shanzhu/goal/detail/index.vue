<template>
  <div class="shanzhu-goal-detail-page">
    <a-spin :spinning="pageLoading">
      <a-flex :gap="16" vertical>
        <a-card :bordered="false" class="goal-overview-card">
          <div class="goal-hero">
            <div class="goal-hero-main">
              <div class="goal-hero-nav">
                <a-button class="goal-back-btn" shape="round" @click="goBack">← 返回目标列表</a-button>
                <span class="goal-chip goal-chip-category">{{ goalDetail?.categoryName || '未分类' }}</span>
                <span class="goal-chip" :class="'goal-chip-status-' + goalDetail?.status">
                  {{ getGoalStatusOption(goalDetail?.status).label }}
                </span>
              </div>
              <h2 class="goal-title">{{ goalDetail?.title || '目标详情' }}</h2>
              <p class="goal-description">{{ goalDetail?.description || '暂无目标描述' }}</p>

              <div class="goal-meta">
                <span>开始 {{ goalDetail?.startDate || '-' }}</span>
                <span>截止 {{ goalDetail?.deadline || '-' }}</span>
                <span>{{ getGoalPriorityLabel(goalDetail?.priority) }}优先级</span>
              </div>

              <div class="goal-tags">
                <span v-for="tag in goalDetail?.tags" :key="tag.id" class="goal-tag">{{ tag.name }}</span>
                <span v-if="!goalDetail?.tags || goalDetail.tags.length === 0" class="goal-tag">暂无标签</span>
              </div>
            </div>

            <div class="goal-hero-side">
              <div class="goal-progress-orb">
                <span>{{ goalDetail?.progress || 0 }}%</span>
                <small>目标进度</small>
              </div>
              <a-space wrap>
                <a-button shape="round" @click="openCreateTaskModal">
                  <template #icon>
                    <PlusOutlined/>
                  </template>
                  新增任务
                </a-button>
                <a-button type="primary" shape="round" @click="openCreateSubGoalModal">
                  <template #icon>
                    <PlusOutlined/>
                  </template>
                  新增子目标
                </a-button>
              </a-space>
            </div>
          </div>

          <a-progress class="goal-hero-progress" :percent="goalDetail?.progress || 0" :show-info="false"/>

          <a-row :gutter="[14, 14]" class="goal-stat-row">
            <a-col :xs="12" :md="6">
              <div class="goal-stat-card">
                <span class="goal-stat-label">子目标</span>
                <strong>{{ goalDetail?.subGoalCount ?? subGoalList.length }}</strong>
                <small>已完成 {{ completedSubGoalCount }}</small>
              </div>
            </a-col>
            <a-col :xs="12" :md="6">
              <div class="goal-stat-card">
                <span class="goal-stat-label">任务</span>
                <strong>{{ goalDetail?.totalTaskCount ?? taskList.length }}</strong>
                <small>全部行动项</small>
              </div>
            </a-col>
            <a-col :xs="12" :md="6">
              <div class="goal-stat-card">
                <span class="goal-stat-label">完成任务</span>
                <strong>{{ completedTaskCount }}</strong>
                <small>持续推进中</small>
              </div>
            </a-col>
            <a-col :xs="12" :md="6">
              <div class="goal-stat-card">
                <span class="goal-stat-label">进展记录</span>
                <strong>{{ goalProgressList.length }}</strong>
                <small>复盘痕迹</small>
              </div>
            </a-col>
          </a-row>
        </a-card>

        <a-card :bordered="false" class="goal-section-card" title="📈 进展记录">
          <template #extra>
            <a-button type="primary" size="small" @click="openCreateProgressModal">
              <template #icon>
                <PlusOutlined/>
              </template>
              新增进展
            </a-button>
          </template>
          <a-spin :spinning="progressLoading">
            <a-empty v-if="goalProgressList.length === 0" description="暂无进展记录"/>
            <a-list v-else :data-source="goalProgressList" item-layout="vertical">
              <template #renderItem="{ item }">
                <a-list-item class="progress-list-item">
                  <a-flex justify="space-between" align="flex-start" wrap="wrap" :gap="12">
                    <div class="progress-main">
                      <a-space size="small" wrap>
                        <a-tag color="blue">{{ item.recordDate || '-' }}</a-tag>
                        <a-tag v-if="item.progressBefore !== undefined || item.progressAfter !== undefined" color="green">
                          {{ item.progressBefore ?? '-' }}% → {{ item.progressAfter ?? '-' }}%
                        </a-tag>
                      </a-space>
                      <a-typography-title :level="5" class="progress-title">
                        {{ item.title }}
                      </a-typography-title>
                      <a-typography-paragraph class="progress-content">
                        {{ item.content || '暂无进展内容' }}
                      </a-typography-paragraph>
                    </div>
                    <a-button type="link" size="small" danger @click="confirmDeleteProgress(item)">删除</a-button>
                  </a-flex>
                </a-list-item>
              </template>
            </a-list>
          </a-spin>
        </a-card>

        <a-card :bordered="false" class="goal-section-card" title="🌱 关联习惯">
          <template #extra>
            <a-button type="link" size="small" @click="openHabitPage">查看全部习惯</a-button>
          </template>
          <a-spin :spinning="habitLoading">
            <a-empty v-if="goalHabitList.length === 0" description="暂无关联习惯，可在习惯打卡页创建并关联当前目标"/>
            <a-row v-else :gutter="[16, 16]">
              <a-col v-for="habit in goalHabitList" :key="habit.id" :xs="24" :lg="12">
                <a-card class="goal-habit-card" hoverable>
                  <a-flex vertical :gap="12">
                    <a-flex justify="space-between" align="flex-start" :gap="12">
                      <div class="goal-habit-main">
                        <a-space size="small" wrap>
                          <a-tag :color="getHabitStatusOption(habit.status).color">
                            {{ getHabitStatusOption(habit.status).label }}
                          </a-tag>
                          <a-tag color="blue">{{ getHabitFrequencyLabel(habit.frequencyType) }}</a-tag>
                          <a-tag :color="habit.todayChecked ? 'success' : 'default'">
                            {{ habit.todayChecked ? '今日已打卡' : '今日未打卡' }}
                          </a-tag>
                        </a-space>
                        <a-typography-title :level="5" class="goal-habit-title">
                          {{ habit.title }}
                        </a-typography-title>
                      </div>
                      <a-space size="small">
                        <a-button
                            v-if="habit.todayChecked"
                            size="small"
                            danger
                            ghost
                            @click="confirmCancelHabitCheckin(habit)"
                        >
                          取消打卡
                        </a-button>
                        <a-button
                            v-else
                            type="primary"
                            size="small"
                            ghost
                            :disabled="habit.status !== habitActiveStatus"
                            @click="handleQuickHabitCheckin(habit)"
                        >
                          快速打卡
                        </a-button>
                      </a-space>
                    </a-flex>

                    <a-typography-paragraph class="goal-habit-description" :ellipsis="{ rows: 2 }">
                      {{ habit.description || '暂无习惯说明' }}
                    </a-typography-paragraph>

                    <a-progress :percent="formatHabitPercent(habit.currentPeriodCompletionRate)" size="small"/>

                    <a-space size="small" wrap class="goal-habit-meta">
                      <span>周期：{{ habit.currentPeriodCheckedCount || 0 }}/{{ habit.currentPeriodTargetCount || 0 }}</span>
                      <span>连续：{{ habit.continuousDays || 0 }} 天</span>
                      <span>目标值：{{ formatHabitTargetValue(habit.targetValue, habit.unit) }}</span>
                      <span>子目标：{{ habit.subGoalTitle || '-' }}</span>
                    </a-space>
                  </a-flex>
                </a-card>
              </a-col>
            </a-row>
          </a-spin>
        </a-card>

        <a-card :bordered="false" class="goal-section-card" title="🎯 子目标列表">
          <a-empty v-if="subGoalList.length === 0" description="暂无子目标，先新增一个拆解项吧"/>
          <a-row v-else :gutter="[16, 16]">
            <a-col v-for="subGoal in subGoalList" :key="subGoal.id" :xs="24" :lg="12">
              <a-card class="sub-goal-card" hoverable>
                <a-flex vertical :gap="12">
                  <a-flex justify="space-between" align="flex-start" :gap="12">
                    <div class="sub-goal-title-wrap">
                      <a-typography-title :level="5" class="sub-goal-title">
                        {{ subGoal.title }}
                      </a-typography-title>
                      <a-typography-text type="secondary">
                        权重 {{ subGoal.weight || 1 }} · 排序 {{ subGoal.sortOrder || 0 }}
                      </a-typography-text>
                    </div>
                    <a-space size="small">
                      <a-button type="link" size="small" @click="openEditSubGoalModal(subGoal)">编辑</a-button>
                      <a-button type="link" size="small" danger @click="confirmDeleteSubGoal(subGoal)">删除</a-button>
                    </a-space>
                  </a-flex>

                  <a-typography-paragraph class="sub-goal-description" :ellipsis="{ rows: 2 }">
                    {{ subGoal.description || '暂无子目标描述' }}
                  </a-typography-paragraph>

                  <a-row :gutter="[12, 12]">
                    <a-col :xs="24" :md="12">
                      <a-select
                          :value="subGoal.status"
                          class="sub-goal-control"
                          size="small"
                          @change="(value: string) => handleSubGoalStatusChange(subGoal.id, value)"
                      >
                        <a-select-option v-for="item in subGoalStatusOptions" :key="item.value" :value="item.value">
                          {{ item.label }}
                        </a-select-option>
                      </a-select>
                    </a-col>
                    <a-col :xs="24" :md="12">
                      <a-input-number
                          :value="subGoal.progress || 0"
                          :min="0"
                          :max="100"
                          size="small"
                          class="sub-goal-control"
                          addon-after="%"
                          @change="(value: number) => handleSubGoalProgressChange(subGoal.id, value)"
                      />
                    </a-col>
                  </a-row>

                  <a-progress :percent="subGoal.progress || 0" size="small"/>

                  <a-flex justify="space-between" class="sub-goal-meta">
                    <span>开始：{{ subGoal.startDate || '-' }}</span>
                    <span>截止：{{ subGoal.deadline || '-' }}</span>
                  </a-flex>

                  <a-divider class="sub-goal-task-divider"/>
                  <a-flex justify="space-between" align="center">
                    <a-typography-text strong>子目标任务</a-typography-text>
                    <a-button type="link" size="small" @click="openCreateTaskModal(subGoal.id)">新增任务</a-button>
                  </a-flex>
                  <a-empty v-if="!subGoal.tasks || subGoal.tasks.length === 0" description="暂无任务" class="sub-goal-task-empty"/>
                  <a-list v-else :data-source="subGoal.tasks" size="small">
                    <template #renderItem="{ item }">
                      <a-list-item class="task-list-item">
                        <a-flex justify="space-between" align="flex-start" wrap="wrap" :gap="12">
                          <div class="task-main">
                            <a-space size="small" wrap>
                              <a-tag :color="getGoalStatusOption(item.status).color">
                                {{ getGoalStatusOption(item.status).label }}
                              </a-tag>
                              <a-tag color="blue">{{ getTaskPriorityLabel(item.priority) }}</a-tag>
                            </a-space>
                            <a-typography-title :level="5" class="task-title">
                              {{ item.title }}
                            </a-typography-title>
                            <a-typography-paragraph class="task-description" :ellipsis="{ rows: 2 }">
                              {{ item.description || '暂无任务说明' }}
                            </a-typography-paragraph>
                            <a-space size="small" wrap class="task-meta">
                              <span>计划：{{ item.plannedDate || '-' }}</span>
                              <span>截止：{{ item.deadline || '-' }}</span>
                              <span>预计：{{ item.estimatedMinutes || 0 }} 分钟</span>
                              <span>排序：{{ item.sortOrder || 0 }}</span>
                            </a-space>
                          </div>
                          <a-space size="small" wrap>
                            <a-select
                                :value="item.status"
                                size="small"
                                class="task-status-select"
                                @change="(value: string) => handleTaskStatusChange(item.id, value)"
                            >
                              <a-select-option v-for="statusItem in subGoalStatusOptions" :key="statusItem.value" :value="statusItem.value">
                                {{ statusItem.label }}
                              </a-select-option>
                            </a-select>
                            <a-button type="link" size="small" @click="openEditTaskModal(item)">编辑</a-button>
                            <a-button type="link" size="small" danger @click="confirmDeleteTask(item)">删除</a-button>
                          </a-space>
                        </a-flex>
                      </a-list-item>
                    </template>
                  </a-list>
                </a-flex>
              </a-card>
            </a-col>
          </a-row>
        </a-card>

        <a-card :bordered="false" class="goal-section-card" title="📌 未归属子目标任务">
          <template #extra>
            <a-button type="primary" size="small" @click="openCreateTaskModal()">
              <template #icon>
                <PlusOutlined/>
              </template>
              新增任务
            </a-button>
          </template>
          <a-spin :spinning="taskLoading">
            <a-empty v-if="unassignedTaskList.length === 0" description="暂无未归属任务"/>
            <a-list v-else :data-source="unassignedTaskList" item-layout="vertical">
              <template #renderItem="{ item }">
                <a-list-item class="task-list-item">
                  <a-flex justify="space-between" align="flex-start" wrap="wrap" :gap="12">
                    <div class="task-main">
                      <a-space size="small" wrap>
                        <a-tag :color="getGoalStatusOption(item.status).color">
                          {{ getGoalStatusOption(item.status).label }}
                        </a-tag>
                        <a-tag color="blue">{{ getTaskPriorityLabel(item.priority) }}</a-tag>
                      </a-space>
                      <a-typography-title :level="5" class="task-title">
                        {{ item.title }}
                      </a-typography-title>
                      <a-typography-paragraph class="task-description" :ellipsis="{ rows: 2 }">
                        {{ item.description || '暂无任务说明' }}
                      </a-typography-paragraph>
                      <a-space size="small" wrap class="task-meta">
                        <span>计划：{{ item.plannedDate || '-' }}</span>
                        <span>截止：{{ item.deadline || '-' }}</span>
                        <span>预计：{{ item.estimatedMinutes || 0 }} 分钟</span>
                        <span>排序：{{ item.sortOrder || 0 }}</span>
                      </a-space>
                    </div>
                    <a-space size="small" wrap>
                      <a-select
                          :value="item.status"
                          size="small"
                          class="task-status-select"
                          @change="(value: string) => handleTaskStatusChange(item.id, value)"
                      >
                        <a-select-option v-for="statusItem in subGoalStatusOptions" :key="statusItem.value" :value="statusItem.value">
                          {{ statusItem.label }}
                        </a-select-option>
                      </a-select>
                      <a-button type="link" size="small" @click="openEditTaskModal(item)">编辑</a-button>
                      <a-button type="link" size="small" danger @click="confirmDeleteTask(item)">删除</a-button>
                    </a-space>
                  </a-flex>
                </a-list-item>
              </template>
            </a-list>
          </a-spin>
        </a-card>
      </a-flex>
    </a-spin>

    <a-modal
        v-model:open="progressModal.open"
        :title="progressModal.title"
        :confirm-loading="progressModal.saveLoading"
        width="640px"
        ok-text="保 存"
        cancel-text="取 消"
        @ok="handleSaveProgress"
    >
      <a-form ref="progressFormRef" :colon="false" :model="progressForm" :rules="progressRules" :label-col="{ span: 4 }">
        <a-form-item label="进展标题" name="title">
          <a-input v-model:value="progressForm.title" placeholder="例如：完成阶段性里程碑" :maxlength="120" show-count allow-clear/>
        </a-form-item>
        <a-row>
          <a-col :span="12">
            <a-form-item label="记录日期" :label-col="{ span: 8 }">
              <a-date-picker v-model:value="progressForm.recordDate" value-format="YYYY-MM-DD" style="width: 100%" placeholder="请选择记录日期"/>
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item label="变更前" :label-col="{ span: 12 }">
              <a-input-number v-model:value="progressForm.progressBefore" :min="0" :max="100" addon-after="%" style="width: 100%"/>
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item label="变更后" :label-col="{ span: 12 }">
              <a-input-number v-model:value="progressForm.progressAfter" :min="0" :max="100" addon-after="%" style="width: 100%"/>
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="进展内容">
          <a-textarea v-model:value="progressForm.content" placeholder="记录本次推进了什么、有什么变化或下一步动作" :rows="4" :maxlength="500" show-count allow-clear/>
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
        v-model:open="subGoalModal.open"
        :title="subGoalModal.title"
        :confirm-loading="subGoalModal.saveLoading"
        width="680px"
        ok-text="保 存"
        cancel-text="取 消"
        @ok="handleSaveSubGoal"
    >
      <a-form ref="subGoalFormRef" :colon="false" :model="subGoalForm" :rules="subGoalRules" :label-col="{ span: 4 }">
        <a-form-item label="子目标名称" name="title">
          <a-input v-model:value="subGoalForm.title" placeholder="例如：完成目标详情页基础版" :maxlength="120" show-count allow-clear/>
        </a-form-item>
        <a-form-item label="状态">
          <a-radio-group v-model:value="subGoalForm.status">
            <a-radio v-for="item in subGoalStatusOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-radio>
          </a-radio-group>
        </a-form-item>
        <a-row>
          <a-col :span="8">
            <a-form-item label="进度" :label-col="{ span: 10 }">
              <a-input-number v-model:value="subGoalForm.progress" :min="0" :max="100" addon-after="%" style="width: 100%"/>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="权重" :label-col="{ span: 10 }">
              <a-input-number v-model:value="subGoalForm.weight" :min="1" :max="100" style="width: 100%"/>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="排序" :label-col="{ span: 10 }">
              <a-input-number v-model:value="subGoalForm.sortOrder" :min="0" :max="9999" style="width: 100%"/>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row>
          <a-col :span="12">
            <a-form-item label="开始日期" :label-col="{ span: 8 }">
              <a-date-picker v-model:value="subGoalForm.startDate" value-format="YYYY-MM-DD" style="width: 100%" placeholder="请选择开始日期"/>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="截止日期" :label-col="{ span: 8 }">
              <a-date-picker v-model:value="subGoalForm.deadline" value-format="YYYY-MM-DD" style="width: 100%" placeholder="请选择截止日期"/>
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="描述">
          <a-textarea v-model:value="subGoalForm.description" placeholder="描述这个子目标的完成标准" :rows="4" :maxlength="500" show-count allow-clear/>
        </a-form-item>
      </a-form>
    </a-modal>

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
        <a-form-item label="任务标题" name="title">
          <a-input v-model:value="taskForm.title" placeholder="例如：完成首页任务卡片设计" :maxlength="120" show-count allow-clear/>
        </a-form-item>
        <a-form-item label="关联子目标">
          <a-select v-model:value="taskForm.subGoalId" placeholder="可选择任务所属子目标" allow-clear>
            <a-select-option v-for="subGoal in subGoalList" :key="subGoal.id" :value="subGoal.id">
              {{ subGoal.title }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-radio-group v-model:value="taskForm.status">
            <a-radio v-for="item in subGoalStatusOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-radio>
          </a-radio-group>
        </a-form-item>
        <a-row>
          <a-col :span="8">
            <a-form-item label="优先级" :label-col="{ span: 10 }">
              <a-select v-model:value="taskForm.priority" style="width: 100%">
                <a-select-option :value="1">低</a-select-option>
                <a-select-option :value="2">中</a-select-option>
                <a-select-option :value="3">高</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="预计分钟" :label-col="{ span: 10 }">
              <a-input-number v-model:value="taskForm.estimatedMinutes" :min="0" :max="99999" style="width: 100%"/>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="排序" :label-col="{ span: 10 }">
              <a-input-number v-model:value="taskForm.sortOrder" :min="0" :max="9999" style="width: 100%"/>
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
        <a-form-item label="任务说明">
          <a-textarea v-model:value="taskForm.description" placeholder="描述这个任务的执行方式或完成标准" :rows="4" :maxlength="500" show-count allow-clear/>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import {computed, onMounted, reactive, ref} from "vue";
import {useRoute, useRouter} from "vue-router";
import type {FormInstance, Rule} from "ant-design-vue/es/form";
import {message, Modal} from "ant-design-vue";
import {PlusOutlined} from "@ant-design/icons-vue";
import {queryGoalById} from "@/api/shanzhu/goal/Goal.ts";
import type {GoalStatusOption, ShanzhuGoalVO} from "@/api/shanzhu/goal/type/Goal.ts";
import {deleteGoalProgress, queryGoalProgressList, saveGoalProgress} from "@/api/shanzhu/goal-progress/GoalProgress.ts";
import type {ShanzhuGoalProgress, ShanzhuGoalProgressVO} from "@/api/shanzhu/goal-progress/type/GoalProgress.ts";
import type {BaseModalActiveType} from "@/api/global/Type.ts";
import {
  deleteSubGoal,
  saveSubGoal,
  updateSubGoalProgress,
  updateSubGoalStatus
} from "@/api/shanzhu/sub-goal/SubGoal.ts";
import type {ShanzhuSubGoal, ShanzhuSubGoalVO} from "@/api/shanzhu/sub-goal/type/SubGoal.ts";
import {
  deleteTask,
  saveTask,
  updateTaskStatus
} from "@/api/shanzhu/task/Task.ts";
import type {ShanzhuTask, ShanzhuTaskVO} from "@/api/shanzhu/task/type/Task.ts";
import {
  cancelHabitCheckin,
  queryHabitList,
  saveHabitCheckin
} from "@/api/shanzhu/habit/Habit.ts";
import type {HabitFrequencyOption, HabitStatusOption, ShanzhuHabitVO} from "@/api/shanzhu/habit/type/Habit.ts";

const route = useRoute();
const router = useRouter();

const pageLoading = ref(false);
const taskLoading = ref(false);
const progressLoading = ref(false);
const habitLoading = ref(false);
const goalDetail = ref<ShanzhuGoalVO>();
const goalProgressList = ref<ShanzhuGoalProgressVO[]>([]);
const goalHabitList = ref<ShanzhuHabitVO[]>([]);
const taskList = computed(() => {
  const subGoalTasks = subGoalList.value.flatMap(subGoal => subGoal.tasks || []);
  return [...subGoalTasks, ...unassignedTaskList.value];
});
const progressFormRef = ref<FormInstance>();
const subGoalFormRef = ref<FormInstance>();
const taskFormRef = ref<FormInstance>();
const progressForm = ref<ShanzhuGoalProgress>({});
const subGoalForm = ref<ShanzhuSubGoal>({});
const taskForm = ref<ShanzhuTask>({});

const progressModal = reactive<BaseModalActiveType>({
  open: false,
  saveLoading: false,
  title: "新增进展"
});

const subGoalModal = reactive<BaseModalActiveType>({
  open: false,
  saveLoading: false,
  title: "新增子目标"
});

const taskModal = reactive<BaseModalActiveType>({
  open: false,
  saveLoading: false,
  title: "新增任务"
});

const subGoalStatusOptions: GoalStatusOption[] = [
  {label: "未开始", value: "not_started", color: "default"},
  {label: "进行中", value: "in_progress", color: "processing"},
  {label: "已完成", value: "completed", color: "success"},
  {label: "已暂停", value: "paused", color: "warning"},
  {label: "已取消", value: "cancelled", color: "error"}
];

const habitActiveStatus = "active";

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

const progressRules: Record<string, Rule[]> = {
  title: [{required: true, message: "请输入进展标题", trigger: "blur"}]
};

const subGoalRules: Record<string, Rule[]> = {
  title: [{required: true, message: "请输入子目标名称", trigger: "blur"}]
};

const taskRules: Record<string, Rule[]> = {
  title: [{required: true, message: "请输入任务标题", trigger: "blur"}]
};

const goalId = computed(() => route.params.id as string);
const subGoalList = computed(() => goalDetail.value?.subGoals || []);
const unassignedTaskList = computed(() => goalDetail.value?.unassignedTasks || []);
const completedSubGoalCount = computed(() => {
  return subGoalList.value.filter(subGoal => subGoal.status === "completed").length;
});
const completedTaskCount = computed(() => {
  return goalDetail.value?.completedTaskCount ?? taskList.value.filter(task => task.status === "completed").length;
});

const defaultProgressForm = (): ShanzhuGoalProgress => ({
  goalId: goalId.value,
  progressBefore: goalDetail.value?.progress || 0,
  progressAfter: goalDetail.value?.progress || 0
});

const defaultSubGoalForm = (): ShanzhuSubGoal => ({
  goalId: goalId.value,
  status: "not_started",
  progress: 0,
  weight: 1,
  sortOrder: subGoalList.value.length + 1
});

const defaultTaskForm = (): ShanzhuTask => ({
  goalId: goalId.value,
  status: "not_started",
  priority: 2,
  sortOrder: taskList.value.length + 1
});

const getGoalStatusOption = (status?: string) => {
  return subGoalStatusOptions.find(item => item.value === status) || subGoalStatusOptions[0];
};

const getGoalPriorityLabel = (priority?: number) => {
  const priorityMap: Record<number, string> = {
    1: "低",
    2: "中",
    3: "高"
  };
  return priority ? priorityMap[priority] || "-" : "-";
};

const getTaskPriorityLabel = (priority?: number) => {
  return `优先级：${getGoalPriorityLabel(priority)}`;
};

const getHabitStatusOption = (status?: string) => {
  return habitStatusOptions.find(item => item.value === status) || habitStatusOptions[0];
};

const getHabitFrequencyLabel = (frequencyType?: string) => {
  return habitFrequencyOptions.find(item => item.value === frequencyType)?.label || "每日";
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

const loadGoalDetail = async () => {
  if (!goalId.value) {
    message.error("目标不存在");
    return;
  }

  pageLoading.value = true;
  try {
    const response = await queryGoalById(goalId.value);
    if (response.code === 200 && response.data) {
      goalDetail.value = response.data;
    } else {
      message.error(response.msg || "目标不存在");
    }
  } finally {
    pageLoading.value = false;
  }
};

const loadGoalProgressList = async () => {
  if (!goalId.value) {
    return;
  }

  progressLoading.value = true;
  try {
    const response = await queryGoalProgressList({
      goalId: goalId.value
    });
    if (response.code === 200) {
      goalProgressList.value = response.data || [];
    } else {
      message.error(response.msg || "进展记录加载失败");
    }
  } finally {
    progressLoading.value = false;
  }
};

const loadGoalHabitList = async () => {
  if (!goalId.value) {
    return;
  }

  habitLoading.value = true;
  try {
    const response = await queryHabitList({
      goalId: goalId.value
    });
    if (response.code === 200) {
      goalHabitList.value = response.data || [];
    } else {
      message.error(response.msg || "关联习惯加载失败");
    }
  } finally {
    habitLoading.value = false;
  }
};

const goBack = () => {
  router.push("/shanzhu/goal");
};

const openHabitPage = () => {
  router.push("/shanzhu/habit");
};

const openCreateProgressModal = () => {
  progressForm.value = defaultProgressForm();
  progressModal.title = "新增进展";
  progressModal.open = true;
};

const openCreateSubGoalModal = () => {
  subGoalForm.value = defaultSubGoalForm();
  subGoalModal.title = "新增子目标";
  subGoalModal.open = true;
};

const openEditSubGoalModal = (subGoal: ShanzhuSubGoalVO) => {
  subGoalForm.value = {...subGoal};
  subGoalModal.title = "编辑子目标";
  subGoalModal.open = true;
};

const openCreateTaskModal = (subGoalId?: string) => {
  taskForm.value = {
    ...defaultTaskForm(),
    subGoalId
  };
  taskModal.title = "新增任务";
  taskModal.open = true;
};

const openEditTaskModal = (task: ShanzhuTaskVO) => {
  taskForm.value = {...task};
  taskModal.title = "编辑任务";
  taskModal.open = true;
};

const handleSaveProgress = async () => {
  await progressFormRef.value?.validate();

  progressModal.saveLoading = true;
  try {
    const response = await saveGoalProgress({
      ...progressForm.value,
      goalId: goalId.value
    });
    if (response.code === 200) {
      message.success("进展已保存");
      progressModal.open = false;
      await loadGoalProgressList();
    } else {
      message.error(response.msg || "保存失败");
    }
  } finally {
    progressModal.saveLoading = false;
  }
};

const confirmDeleteProgress = (progress: ShanzhuGoalProgressVO) => {
  if (!progress.id) {
    return;
  }

  Modal.confirm({
    title: "确认删除进展记录？",
    content: `删除后，进展「${progress.title || '-'}」将不再展示。`,
    okText: "确认删除",
    cancelText: "取消",
    okType: "danger",
    onOk: async () => {
      const response = await deleteGoalProgress(progress.id || "");
      if (response.code === 200) {
        message.success("删除成功");
        await loadGoalProgressList();
      } else {
        message.error(response.msg || "删除失败");
      }
    }
  });
};

const handleSaveSubGoal = async () => {
  await subGoalFormRef.value?.validate();

  subGoalModal.saveLoading = true;
  try {
    const response = await saveSubGoal({
      ...subGoalForm.value,
      goalId: goalId.value
    });
    if (response.code === 200) {
      message.success("保存成功");
      subGoalModal.open = false;
      await loadGoalDetail();
      await loadGoalProgressList();
    } else {
      message.error(response.msg);
    }
  } finally {
    subGoalModal.saveLoading = false;
  }
};

const confirmDeleteSubGoal = (subGoal: ShanzhuSubGoalVO) => {
  if (!subGoal.id) {
    return;
  }

  Modal.confirm({
    title: "确认删除子目标？",
    content: `删除后，子目标「${subGoal.title || '-'}」将不再展示。`,
    okText: "确认删除",
    cancelText: "取消",
    okType: "danger",
    onOk: async () => {
      const response = await deleteSubGoal(subGoal.id || "");
      if (response.code === 200) {
        message.success("删除成功");
        await loadGoalDetail();
      } else {
        message.error(response.msg);
      }
    }
  });
};

const handleSubGoalStatusChange = async (subGoalId: string | undefined, status: string) => {
  if (!subGoalId) {
    return;
  }

  const response = await updateSubGoalStatus({
    id: subGoalId,
    status
  });
  if (response.code === 200) {
    message.success("状态已更新");
    await loadGoalDetail();
    await loadGoalProgressList();
  } else {
    message.error(response.msg);
  }
};

const handleSaveTask = async () => {
  await taskFormRef.value?.validate();

  taskModal.saveLoading = true;
  try {
    const response = await saveTask({
      ...taskForm.value,
      goalId: goalId.value
    });
    if (response.code === 200) {
      message.success("保存成功");
      taskModal.open = false;
      await loadGoalDetail();
      await loadGoalProgressList();
    } else {
      message.error(response.msg);
    }
  } finally {
    taskModal.saveLoading = false;
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
        await loadGoalDetail();
      } else {
        message.error(response.msg);
      }
    }
  });
};

const handleTaskStatusChange = async (taskId: string | undefined, status: string) => {
  if (!taskId) {
    return;
  }

  const response = await updateTaskStatus({
    id: taskId,
    status
  });
  if (response.code === 200) {
    message.success("状态已更新");
    await loadGoalDetail();
    await loadGoalProgressList();
  } else {
    message.error(response.msg);
  }
};

const handleSubGoalProgressChange = async (subGoalId: string | undefined, progressValue: number | null) => {
  if (!subGoalId || progressValue === null) {
    return;
  }

  const response = await updateSubGoalProgress({
    id: subGoalId,
    progress: progressValue
  });
  if (response.code === 200) {
    message.success("进度已更新");
    await loadGoalDetail();
    await loadGoalProgressList();
  } else {
    message.error(response.msg);
  }
};

const handleQuickHabitCheckin = async (habit: ShanzhuHabitVO) => {
  if (!habit.id) {
    return;
  }

  const response = await saveHabitCheckin({
    habitId: habit.id,
    checkinDate: getTodayString(),
    actualValue: habit.targetValue,
    note: `来自目标「${goalDetail.value?.title || '-'}」详情页快速打卡`
  });
  if (response.code === 200) {
    message.success("打卡成功");
    await loadGoalHabitList();
  } else {
    message.error(response.msg || "打卡失败");
  }
};

const confirmCancelHabitCheckin = (habit: ShanzhuHabitVO) => {
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
        await loadGoalHabitList();
      } else {
        message.error(response.msg || "取消打卡失败");
      }
    }
  });
};

onMounted(() => {
  loadGoalDetail();
  loadGoalProgressList();
  loadGoalHabitList();
});
</script>

<style scoped>
.shanzhu-goal-detail-page {
  max-width: 1360px;
  min-height: calc(100vh - 120px);
  margin: 0 auto;
  padding: 36px 48px 56px;
  overflow-x: hidden;
}

.shanzhu-goal-detail-page :deep(.ant-card) {
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 18px 45px rgba(15, 35, 80, 0.065), 0 1px 2px rgba(15, 35, 80, 0.04);
}

.shanzhu-goal-detail-page :deep(.ant-card-head) {
  min-height: 58px;
  border-bottom-color: rgba(15, 35, 80, 0.06);
}

.shanzhu-goal-detail-page :deep(.ant-card-head-title) {
  color: rgba(0, 0, 0, 0.84);
  font-size: 16px;
  font-weight: 850;
}

.shanzhu-goal-detail-page :deep(.ant-btn) {
  font-weight: 650;
}

.goal-overview-card {
  overflow: hidden;
  border: 1px solid rgba(22, 119, 255, 0.08);
  background:
    radial-gradient(circle at 15% 0%, rgba(22, 119, 255, 0.13), transparent 34%),
    radial-gradient(circle at 96% 14%, rgba(82, 196, 26, 0.11), transparent 28%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.98), rgba(247, 250, 255, 0.94));
}

.goal-overview-card :deep(.ant-card-body) {
  padding: 28px;
}

.goal-hero {
  display: flex;
  justify-content: space-between;
  gap: 28px;
}

.goal-hero-main {
  min-width: 0;
  flex: 1;
}

.goal-hero-nav,
.goal-meta,
.goal-tags {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.goal-back-btn {
  height: 30px;
  border-color: rgba(15, 35, 80, 0.08);
  background: rgba(255, 255, 255, 0.72);
  color: rgba(0, 0, 0, 0.58);
}

.goal-chip,
.goal-tag {
  display: inline-flex;
  align-items: center;
  max-width: 180px;
  min-height: 26px;
  padding: 4px 10px;
  overflow: hidden;
  border-radius: 999px;
  background: rgba(15, 35, 80, 0.055);
  color: rgba(0, 0, 0, 0.56);
  font-size: 12px;
  font-weight: 700;
  line-height: 18px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.goal-chip-category {
  background: rgba(22, 119, 255, 0.09);
  color: #1677ff;
}

.goal-chip-status-in_progress {
  background: rgba(22, 119, 255, 0.10);
  color: #1677ff;
}

.goal-chip-status-completed {
  background: rgba(82, 196, 26, 0.12);
  color: #389e0d;
}

.goal-chip-status-paused {
  background: rgba(250, 173, 20, 0.13);
  color: #d48806;
}

.goal-chip-status-cancelled {
  background: rgba(255, 77, 79, 0.10);
  color: #cf1322;
}

.goal-title {
  display: -webkit-box;
  max-width: 820px;
  margin: 18px 0 10px;
  overflow: hidden;
  color: rgba(0, 0, 0, 0.88);
  font-size: 30px;
  font-weight: 850;
  line-height: 1.25;
  letter-spacing: -0.7px;
  word-break: break-word;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.goal-description {
  display: -webkit-box;
  max-width: 820px;
  margin: 0;
  overflow: hidden;
  color: rgba(0, 0, 0, 0.50);
  font-size: 14px;
  line-height: 22px;
  word-break: break-word;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

.goal-meta {
  margin-top: 16px;
  color: rgba(0, 0, 0, 0.48);
  font-size: 13px;
  font-weight: 650;
}

.goal-meta span {
  padding: 5px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.68);
}

.goal-tags {
  margin-top: 12px;
}

.goal-tag {
  background: rgba(114, 46, 209, 0.08);
  color: #722ed1;
}

.goal-hero-side {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  justify-content: space-between;
  gap: 18px;
  flex-shrink: 0;
}

.goal-progress-orb {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 112px;
  height: 112px;
  border-radius: 34px;
  background:
    radial-gradient(circle at 30% 20%, rgba(255, 255, 255, 0.88), transparent 34%),
    linear-gradient(135deg, rgba(22, 119, 255, 0.92), rgba(82, 196, 26, 0.82));
  box-shadow: 0 18px 34px rgba(22, 119, 255, 0.20);
  color: #fff;
}

.goal-progress-orb span {
  font-size: 28px;
  font-weight: 900;
  line-height: 1;
}

.goal-progress-orb small {
  margin-top: 8px;
  color: rgba(255, 255, 255, 0.82);
  font-size: 12px;
  font-weight: 700;
}

.goal-hero-progress {
  margin: 22px 0 4px;
}

.goal-hero-progress :deep(.ant-progress-inner) {
  background: rgba(15, 35, 80, 0.06);
}

.goal-stat-row {
  margin-top: 18px;
}

.goal-stat-card {
  height: 100%;
  padding: 16px;
  border: 1px solid rgba(15, 35, 80, 0.055);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.72);
}

.goal-stat-label {
  display: block;
  margin-bottom: 8px;
  color: rgba(0, 0, 0, 0.44);
  font-size: 12px;
  font-weight: 750;
}

.goal-stat-card strong {
  display: block;
  color: rgba(0, 0, 0, 0.84);
  font-size: 24px;
  font-weight: 900;
  line-height: 1;
}

.goal-stat-card small {
  display: block;
  margin-top: 8px;
  color: rgba(0, 0, 0, 0.40);
  font-size: 12px;
  font-weight: 650;
}

.goal-section-card {
  border: 1px solid rgba(15, 35, 80, 0.06);
  overflow: hidden;
}

.goal-section-card :deep(.ant-card-extra .ant-btn) {
  border-radius: 999px;
}

.goal-section-card :deep(.ant-empty) {
  padding: 36px 0;
}

.progress-list-item,
.task-list-item {
  padding: 16px 0;
}

.progress-list-item:not(:last-child),
.task-list-item:not(:last-child) {
  border-bottom: 1px solid rgba(15, 35, 80, 0.055);
}

.progress-main,
.task-main,
.goal-habit-main,
.sub-goal-title-wrap {
  min-width: 0;
  flex: 1;
}

.progress-title,
.task-title,
.goal-habit-title,
.sub-goal-title {
  display: -webkit-box;
  margin: 8px 0 4px;
  overflow: hidden;
  color: rgba(0, 0, 0, 0.84);
  font-weight: 760;
  line-height: 24px;
  word-break: break-word;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.progress-content,
.task-description,
.goal-habit-description,
.sub-goal-description {
  display: -webkit-box;
  min-height: 22px;
  margin-bottom: 0;
  overflow: hidden;
  color: rgba(0, 0, 0, 0.48);
  font-size: 13px;
  line-height: 21px;
  word-break: break-word;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.task-description {
  margin-bottom: 8px;
}

.goal-habit-card,
.sub-goal-card {
  height: 100%;
  overflow: hidden;
  border: 1px solid rgba(15, 35, 80, 0.06);
  border-radius: 20px;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.94), rgba(248, 251, 255, 0.76));
  box-shadow: 0 12px 28px rgba(15, 35, 80, 0.05);
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.goal-habit-card:hover,
.sub-goal-card:hover {
  box-shadow: 0 18px 36px rgba(15, 35, 80, 0.09);
  transform: translateY(-1px);
}

.goal-habit-card :deep(.ant-card-body),
.sub-goal-card :deep(.ant-card-body) {
  padding: 18px;
}

.goal-habit-meta,
.task-meta,
.sub-goal-meta {
  color: rgba(0, 0, 0, 0.46);
  font-size: 12px;
  font-weight: 650;
}

.goal-habit-meta span,
.task-meta span,
.sub-goal-meta span {
  padding: 4px 9px;
  border-radius: 999px;
  background: rgba(15, 35, 80, 0.045);
}

.task-status-select {
  width: 120px;
}

.task-status-select :deep(.ant-select-selector),
.sub-goal-control :deep(.ant-select-selector),
.sub-goal-control :deep(.ant-input-number),
.sub-goal-control {
  border-radius: 12px;
}

.sub-goal-control {
  width: 100%;
}

.sub-goal-task-divider {
  margin: 4px 0;
  border-color: rgba(15, 35, 80, 0.06);
}

.sub-goal-task-empty {
  margin: 8px 0;
}

@media (max-width: 1200px) {
  .shanzhu-goal-detail-page {
    padding: 28px 28px 48px;
  }
}

@media (max-width: 900px) {
  .goal-hero {
    flex-direction: column;
  }

  .goal-hero-side {
    align-items: flex-start;
  }

  .goal-progress-orb {
    width: 96px;
    height: 96px;
    border-radius: 28px;
  }

  .goal-progress-orb span {
    font-size: 24px;
  }
}

@media (max-width: 640px) {
  .shanzhu-goal-detail-page {
    padding: 18px 14px 36px;
  }

  .goal-overview-card :deep(.ant-card-body) {
    padding: 22px 18px;
  }

  .goal-title {
    font-size: 25px;
  }

  .goal-hero-side :deep(.ant-space) {
    width: 100%;
  }

  .goal-hero-side :deep(.ant-btn) {
    flex: 1;
  }

  .goal-stat-card {
    padding: 14px;
  }

  .goal-stat-card strong {
    font-size: 21px;
  }

  .task-status-select {
    width: 100%;
  }
}
</style>
