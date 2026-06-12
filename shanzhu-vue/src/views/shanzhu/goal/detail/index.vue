<template>
  <div class="shanzhu-goal-detail-page">
    <a-spin :spinning="pageLoading">
      <a-flex :gap="16" vertical>
        <div class="goal-cockpit-hero">
          <div class="goal-hero-grid"/>
          <div class="goal-hero-backdrop"/>
          <div class="goal-hero-content">
            <div class="goal-hero-nav">
              <a-button class="goal-back-btn" size="small" @click="goBack">← 返回目标列表</a-button>
              <span class="goal-breadcrumb">工作台 / 我的目标 / {{ goalDetail?.title || '目标详情' }}</span>
            </div>

            <div class="goal-hero-body">
              <div class="goal-hero-info">
                <div class="goal-eyebrow">
                  <span>GOAL DETAIL</span>
                  <i/>
                  <span>编号 G-{{ goalId }}</span>
                </div>
                <h2 class="goal-title">{{ goalDetail?.title || '目标详情' }}</h2>
                <p class="goal-description">{{ goalDetail?.description || '暂无目标描述' }}</p>

                <div class="goal-tags">
                  <span class="goal-chip" :class="'goal-chip-status-' + goalDetail?.status">
                    {{ getGoalStatusOption(goalDetail?.status).label }}
                  </span>
                  <span class="goal-chip goal-chip-category">{{ goalDetail?.categoryName || '未分类' }}</span>
                  <span class="goal-chip goal-chip-priority">{{ getGoalPriorityLabel(goalDetail?.priority) }}优先级</span>
                  <span v-for="tag in goalDetail?.tags" :key="tag.id" class="goal-tag">{{ tag.name }}</span>
                  <span v-if="!goalDetail?.tags || goalDetail.tags.length === 0" class="goal-tag">暂无标签</span>
                </div>

                <div class="goal-date-panel">
                  <div class="goal-date-card">
                    <span class="date-label">开始日期</span>
                    <strong>{{ goalDetail?.startDate || '-' }}</strong>
                    <span class="date-sub">目标启动时间</span>
                  </div>
                  <div class="goal-date-card">
                    <span class="date-label">截止日期</span>
                    <strong>{{ goalDetail?.deadline || '-' }}</strong>
                    <span class="date-sub">按计划提交</span>
                  </div>
                  <div class="goal-date-card date-card-warning">
                    <span class="date-label">剩余天数</span>
                    <strong :class="{ 'kpi-overdue': remainingDays === '已逾期' }">{{ remainingDays }}<small v-if="remainingDays !== '-' && remainingDays !== '已逾期'"> 天</small></strong>
                    <span class="date-sub">建议持续推进</span>
                  </div>
                </div>

                <a-space class="goal-hero-actions" wrap>
                  <a-button type="primary" shape="round" @click="openCreateProgressModal">
                    <template #icon><PlusOutlined/></template>
                    新增进展
                  </a-button>
                  <a-button shape="round" @click="openCreateSubGoalModal">
                    <template #icon><AimOutlined/></template>
                    新增子目标
                  </a-button>
                  <a-button shape="round" @click="() => openCreateTaskModal()">
                    <template #icon><PlusOutlined/></template>
                    新增任务
                  </a-button>
                  <a-button shape="round" @click="openCreateProgressModal">
                    <template #icon><RiseOutlined/></template>
                    记录进展
                  </a-button>
                </a-space>
              </div>

              <div class="goal-progress-panel">
                <div class="goal-progress-ring" :style="{ '--goal-progress': `${goalDetail?.progress || 0}%` }">
                  <div class="goal-progress-ring-inner">
                    <span>PROGRESS</span>
                    <strong>{{ goalDetail?.progress || 0 }}<small>%</small></strong>
                    <em>{{ getGoalStatusOption(goalDetail?.status).label }} · {{ remainingDays === '已逾期' ? '已逾期' : '持续推进' }}</em>
                  </div>
                </div>
                <div class="goal-health-card">
                  <span class="health-dot"/>
                  <span>健康度</span>
                  <strong>{{ remainingDays === '已逾期' ? '需关注' : '稳定' }}</strong>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="goal-summary-grid">
          <div class="goal-summary-card summary-sub">
            <div class="summary-card-head">
              <span class="summary-icon"><AimOutlined/></span>
              <span class="summary-label">子目标</span>
              <span class="summary-code">SUB</span>
            </div>
            <div class="summary-number">{{ completedSubGoalCount }}<small>/{{ goalDetail?.subGoalCount ?? subGoalList.length }} 已完成</small></div>
            <a-progress :percent="(goalDetail?.subGoalCount ?? subGoalList.length) ? Math.round(completedSubGoalCount / (goalDetail?.subGoalCount ?? subGoalList.length) * 100) : 0" :show-info="false" size="small"/>
            <div class="summary-footer">
              <span>建议拆解 2-3 个阶段</span>
              <a-button type="link" size="small" @click="openCreateSubGoalModal">立即拆解 →</a-button>
            </div>
          </div>

          <div class="goal-summary-card summary-task">
            <div class="summary-card-head">
              <span class="summary-icon"><ThunderboltOutlined/></span>
              <span class="summary-label">任务</span>
              <span class="summary-code">TASK</span>
            </div>
            <div class="summary-number">{{ completedTaskCount }}<small>/{{ goalDetail?.totalTaskCount ?? taskList.length }} 已完成</small></div>
            <a-progress :percent="(goalDetail?.totalTaskCount ?? taskList.length) ? Math.round(completedTaskCount / (goalDetail?.totalTaskCount ?? taskList.length) * 100) : 0" :show-info="false" size="small"/>
            <div class="summary-footer">
              <span>{{ nextActionTasks.length }} 个待推进</span>
              <a-button type="link" size="small" @click="() => openCreateTaskModal()">新增任务 →</a-button>
            </div>
          </div>

          <div class="goal-summary-card summary-habit">
            <div class="summary-card-head">
              <span class="summary-icon"><ExperimentOutlined/></span>
              <span class="summary-label">关联习惯</span>
              <span class="summary-code">HABIT</span>
            </div>
            <div class="summary-number">{{ goalHabitList.length }}<small>/ 今日 {{ goalHabitList.filter(habit => habit.todayChecked).length }} 待打卡</small></div>
            <div class="summary-dash-line"/>
            <div class="summary-footer">
              <span>{{ goalHabitList.length ? '习惯支撑目标推进' : '暂无习惯打卡' }}</span>
              <a-button type="link" size="small" @click="openHabitPage">查看习惯 →</a-button>
            </div>
          </div>

          <div class="goal-summary-card summary-log">
            <div class="summary-card-head">
              <span class="summary-icon"><RiseOutlined/></span>
              <span class="summary-label">进展记录</span>
              <span class="summary-code">LOG</span>
            </div>
            <div class="summary-number">{{ goalProgressList.length }}<small> 条进展</small></div>
            <div class="summary-dash-line"/>
            <div class="summary-footer">
              <span>{{ goalProgressList.length ? '最近已有记录' : '最近暂无记录' }}</span>
              <a-button type="link" size="small" @click="openCreateProgressModal">记录进展 →</a-button>
            </div>
          </div>
        </div>

        <!-- ═══ 双栏主体 ═══ -->
        <div class="goal-dual-columns">
          <!-- ── 左栏：执行拆解 ── -->
          <div class="goal-col-left">
            <a-card :bordered="false" class="goal-section-card">
              <div class="goal-section-header">
                <div class="section-title-group">
                  <span class="section-icon section-icon-blue"><AimOutlined/></span>
                  <div>
                    <h3>执行拆解</h3>
                    <p>将目标拆解成阶段性子目标，逐步推进。</p>
                  </div>
                </div>
                <a-space size="small" class="section-actions">
                  <a-button size="small" shape="round" @click="() => openCreateTaskModal()">
                    <template #icon><PlusOutlined/></template>
                    新增任务
                  </a-button>
                  <a-button type="primary" size="small" shape="round" @click="openCreateSubGoalModal">
                    <template #icon><PlusOutlined/></template>
                    新增子目标
                  </a-button>
                </a-space>
              </div>
              <div v-if="subGoalList.length === 0" class="goal-empty-workbench empty-sub-goal">
                <div class="empty-illustration"><AimOutlined/></div>
                <h4>暂无子目标</h4>
                <p>建议先把「{{ goalDetail?.title || '当前目标' }}」拆解成 2-3 个阶段目标，例如准备、核心推进、最终交付。</p>
                <a-space wrap>
                  <a-button type="primary" shape="round" @click="openCreateSubGoalModal">
                    <template #icon><PlusOutlined/></template>
                    立即拆解
                  </a-button>
                  <a-button shape="round" @click="() => openCreateTaskModal()">
                    <template #icon><PlusOutlined/></template>
                    先加任务
                  </a-button>
                </a-space>
                <div class="empty-template-tags">
                  <span>推荐模板：</span>
                  <em>环境与基础</em>
                  <em>核心模块开发</em>
                  <em>联调与上线</em>
                </div>
              </div>
              <a-flex v-else vertical :gap="16">
                <a-card
                    v-for="(subGoal, subGoalIndex) in subGoalList"
                    :key="subGoal.id"
                    class="sub-goal-card"
                    :class="'sub-goal-card-tone-' + (subGoalIndex % 4)"
                    hoverable
                >
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
              </a-flex>
            </a-card>

            <a-card v-if="unassignedTaskList.length > 0" :bordered="false" class="goal-section-card">
              <template #title><PushpinOutlined style="margin-right: 6px;"/> 未归属任务</template>
              <template #extra>
                <a-button type="primary" size="small" shape="round" @click="openCreateTaskModal()">
                  <template #icon><PlusOutlined/></template>
                  新增
                </a-button>
              </template>
              <a-spin :spinning="taskLoading">
                <a-list :data-source="unassignedTaskList" item-layout="vertical">
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
          </div>

          <!-- ── 右栏：行动 + 习惯 + 时间线 ── -->
          <div class="goal-col-right">
            <!-- 下一步行动 -->
            <a-card :bordered="false" class="goal-section-card goal-next-action-card">
              <div class="goal-section-header compact">
                <div class="section-title-group">
                  <span class="section-icon section-icon-orange"><ThunderboltOutlined/></span>
                  <div>
                    <h3>下一步行动</h3>
                    <p>系统为你挑选的优先动作</p>
                  </div>
                </div>
                <a-button shape="circle" size="small" @click="loadGoalDetail">↻</a-button>
              </div>
              <div v-if="nextActionTasks.length === 0" class="goal-empty-workbench side-empty empty-action">
                <div class="empty-illustration"><ThunderboltOutlined/></div>
                <h4>暂无待执行任务</h4>
                <p>可以先新增一个任务，或把目标拆成子目标后再推进。</p>
                <a-button type="primary" size="small" shape="round" @click="() => openCreateTaskModal()">
                  <template #icon><PlusOutlined/></template>
                  新增任务
                </a-button>
              </div>
              <a-flex v-else vertical :gap="10">
                <div v-for="actionTask in nextActionTasks" :key="actionTask.id" class="next-action-item">
                  <a-flex align="center" :gap="10">
                    <span class="next-action-dot" :class="'dot-' + actionTask.status"/>
                    <div class="next-action-info">
                      <span class="next-action-title">{{ actionTask.title }}</span>
                      <span class="next-action-sub">{{ actionTask.deadline ? '截止 ' + actionTask.deadline : '无截止' }}</span>
                    </div>
                    <a-select
                        :value="actionTask.status"
                        size="small"
                        class="next-action-status"
                        @change="(value: string) => handleTaskStatusChange(actionTask.id, value)"
                    >
                      <a-select-option v-for="statusItem in subGoalStatusOptions" :key="statusItem.value" :value="statusItem.value">
                        {{ statusItem.label }}
                      </a-select-option>
                    </a-select>
                  </a-flex>
                </div>
              </a-flex>
            </a-card>

            <!-- 关联习惯 -->
            <a-card :bordered="false" class="goal-section-card">
              <div class="goal-section-header compact">
                <div class="section-title-group">
                  <span class="section-icon section-icon-green"><ExperimentOutlined/></span>
                  <div>
                    <h3>关联习惯</h3>
                    <p>每天稳定推进的小动作</p>
                  </div>
                </div>
                <a-button size="small" shape="round" @click="openHabitPage">查看全部 →</a-button>
              </div>
              <a-spin :spinning="habitLoading">
                <div v-if="goalHabitList.length === 0" class="goal-empty-workbench side-empty empty-habit">
                  <div class="empty-illustration"><ExperimentOutlined/></div>
                  <h4>暂无关联习惯</h4>
                  <p>创建习惯来稳定推进目标，例如每天学习、复盘或练习。</p>
                  <a-button size="small" shape="round" @click="openHabitPage">去关联习惯</a-button>
                </div>
                <a-flex v-else vertical :gap="12">
                  <div v-for="habit in goalHabitList" :key="habit.id" class="habit-compact-item">
                    <a-flex justify="space-between" align="center" :gap="10">
                      <div class="habit-compact-info">
                        <a-flex align="center" :gap="8">
                          <span class="habit-checkin-dot" :class="{ checked: habit.todayChecked }"/>
                          <span class="habit-compact-title">{{ habit.title }}</span>
                        </a-flex>
                        <span class="habit-compact-sub">
                          {{ getHabitFrequencyLabel(habit.frequencyType) }} · 连续 {{ habit.continuousDays || 0 }} 天 · {{ habit.currentPeriodCheckedCount || 0 }}/{{ habit.currentPeriodTargetCount || 0 }}
                        </span>
                      </div>
                      <a-button
                          v-if="habit.todayChecked"
                          size="small"
                          danger
                          ghost
                          @click="confirmCancelHabitCheckin(habit)"
                      >取消</a-button>
                      <a-button
                          v-else
                          type="primary"
                          size="small"
                          ghost
                          :disabled="habit.status !== habitActiveStatus"
                          @click="handleQuickHabitCheckin(habit)"
                      >打卡</a-button>
                    </a-flex>
                    <a-progress :percent="formatHabitPercent(habit.currentPeriodCompletionRate)" size="small" :show-info="false" class="habit-compact-progress"/>
                  </div>
                </a-flex>
              </a-spin>
            </a-card>

            <!-- 进展时间线 -->
            <a-card :bordered="false" class="goal-section-card">
              <div class="goal-section-header compact">
                <div class="section-title-group">
                  <span class="section-icon section-icon-purple"><RiseOutlined/></span>
                  <div>
                    <h3>进展时间线</h3>
                    <p>按时间记录目标的关键变化</p>
                  </div>
                </div>
                <a-button size="small" shape="round" @click="openCreateProgressModal">
                  <template #icon><PlusOutlined/></template>
                  新增进展
                </a-button>
              </div>
              <a-spin :spinning="progressLoading">
                <div v-if="goalProgressList.length === 0" class="goal-empty-workbench side-empty empty-progress">
                  <div class="empty-illustration"><RiseOutlined/></div>
                  <h4>暂无进展记录</h4>
                  <p>记录一次进展，让目标拥有「呼吸」与节奏。</p>
                  <a-button size="small" shape="round" @click="openCreateProgressModal">记录进展</a-button>
                </div>
                <div v-else class="progress-timeline">
                  <div v-for="(progress, index) in goalProgressList" :key="progress.id || index" class="timeline-item">
                    <div class="timeline-rail">
                      <div class="timeline-dot"/>
                      <div v-if="index < goalProgressList.length - 1" class="timeline-line"/>
                    </div>
                    <div class="timeline-content">
                      <div class="timeline-header">
                        <span class="timeline-date">{{ progress.recordDate || '-' }}</span>
                        <span v-if="progress.progressBefore !== undefined || progress.progressAfter !== undefined" class="timeline-progress-tag">
                          {{ progress.progressBefore ?? '-' }}% → {{ progress.progressAfter ?? '-' }}%
                        </span>
                        <a-button type="link" size="small" danger class="timeline-delete" @click="confirmDeleteProgress(progress)">删除</a-button>
                      </div>
                      <h4 class="timeline-title">{{ progress.title }}</h4>
                      <p class="timeline-body">{{ progress.content || '暂无进展内容' }}</p>
                    </div>
                  </div>
                </div>
              </a-spin>
            </a-card>
          </div>
        </div>
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
import {AimOutlined, ExperimentOutlined, PlusOutlined, PushpinOutlined, RiseOutlined, ThunderboltOutlined} from "@ant-design/icons-vue";
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

const remainingDays = computed(() => {
  if (!goalDetail.value?.deadline) {
    return "-";
  }
  const deadlineDate = new Date(goalDetail.value.deadline);
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  deadlineDate.setHours(0, 0, 0, 0);
  const diffMs = deadlineDate.getTime() - today.getTime();
  const diffDays = Math.ceil(diffMs / (1000 * 60 * 60 * 24));
  return diffDays < 0 ? "已逾期" : String(diffDays);
});

const nextActionTasks = computed(() => {
  const activeTasks = taskList.value.filter(
    task => task.status === "not_started" || task.status === "in_progress"
  );
  return activeTasks.slice(0, 5);
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
/* ═══════ 全局页面 ═══════ */
.shanzhu-goal-detail-page {
  width: 100%;
  max-width: none;
  min-height: calc(100vh - 120px);
  margin: 0;
  padding: 24px 32px 56px;
  overflow-x: hidden;
  background:
    radial-gradient(circle at 10% 0%, rgba(219, 235, 255, 0.42), transparent 28%),
    radial-gradient(circle at 90% 8%, rgba(226, 218, 255, 0.32), transparent 26%),
    linear-gradient(180deg, #f6f9fd 0%, #eef3f8 100%);
}

.shanzhu-goal-detail-page :deep(.ant-card) {
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 14px 34px rgba(45, 45, 65, 0.055), 0 1px 2px rgba(15, 35, 80, 0.03);
}

.shanzhu-goal-detail-page :deep(.ant-card-head) {
  min-height: 52px;
  border-bottom-color: rgba(15, 35, 80, 0.06);
}

.shanzhu-goal-detail-page :deep(.ant-card-head-title) {
  color: rgba(0, 0, 0, 0.84);
  font-size: 15px;
  font-weight: 850;
}

.shanzhu-goal-detail-page :deep(.ant-btn) {
  font-weight: 650;
}

/* ═══════ Hero 工作台 ═══════ */
.goal-cockpit-hero {
  position: relative;
  overflow: hidden;
  min-height: 430px;
  border: 1px solid rgba(206, 218, 238, 0.92);
  border-radius: 34px;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.96), rgba(247, 251, 255, 0.90));
  box-shadow: 0 24px 62px rgba(43, 56, 92, 0.10), 0 1px 2px rgba(15, 35, 80, 0.03);
}

.goal-hero-grid {
  position: absolute;
  inset: 0;
  z-index: 0;
  opacity: 0.30;
  background-image:
    linear-gradient(rgba(58, 88, 145, 0.052) 1px, transparent 1px),
    linear-gradient(90deg, rgba(58, 88, 145, 0.052) 1px, transparent 1px);
  background-size: 30px 30px;
}

.goal-hero-backdrop {
  position: absolute;
  inset: 0;
  z-index: 0;
  background:
    radial-gradient(circle at 8% 6%, rgba(255, 255, 255, 0.76), transparent 24%),
    radial-gradient(circle at 72% 4%, rgba(201, 222, 255, 0.28), transparent 30%),
    radial-gradient(circle at 96% 78%, rgba(226, 214, 255, 0.46), transparent 34%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.02), rgba(245, 249, 255, 0.56));
}

.goal-hero-content {
  position: relative;
  z-index: 1;
  padding: 30px 38px 46px;
}

.goal-hero-nav {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
}

.goal-back-btn {
  height: 34px;
  border-color: rgba(25, 44, 84, 0.10);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.82);
  color: rgba(16, 30, 62, 0.74);
  font-size: 13px;
  font-weight: 760;
  box-shadow: 0 8px 20px rgba(31, 55, 105, 0.06);
}

.goal-breadcrumb {
  color: rgba(31, 45, 78, 0.46);
  font-size: 13px;
  font-weight: 680;
}

.goal-hero-body {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 42px;
  margin-top: 50px;
}

.goal-hero-info {
  min-width: 0;
  flex: 1;
}

.goal-eyebrow {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  color: #1768e8;
  font-size: 12px;
  font-weight: 900;
  letter-spacing: 5px;
}

.goal-eyebrow i {
  width: 54px;
  height: 1px;
  background: rgba(23, 104, 232, 0.44);
}

.goal-title {
  display: -webkit-box;
  max-width: 820px;
  margin: 0 0 14px;
  overflow: hidden;
  color: #0c1833;
  font-size: 42px;
  font-weight: 920;
  line-height: 1.14;
  letter-spacing: -1.3px;
  word-break: break-word;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.goal-description {
  display: -webkit-box;
  max-width: 780px;
  margin: 0;
  overflow: hidden;
  color: rgba(25, 43, 82, 0.68);
  font-size: 15px;
  font-weight: 580;
  line-height: 27px;
  word-break: break-word;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.goal-tags {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-top: 20px;
}

.goal-chip,
.goal-tag {
  display: inline-flex;
  align-items: center;
  max-width: 180px;
  min-height: 25px;
  padding: 4px 11px;
  overflow: hidden;
  border-radius: 999px;
  background: rgba(38, 65, 115, 0.065);
  color: rgba(24, 42, 82, 0.62);
  font-size: 12px;
  font-weight: 780;
  line-height: 18px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.goal-chip-category { background: rgba(37, 110, 255, 0.10); color: #1768e8; }
.goal-chip-priority { background: rgba(255, 123, 46, 0.12); color: #c84f12; }
.goal-chip-status-in_progress { background: rgba(22, 119, 255, 0.12); color: #1677ff; }
.goal-chip-status-completed { background: rgba(82, 196, 26, 0.13); color: #389e0d; }
.goal-chip-status-paused { background: rgba(250, 173, 20, 0.14); color: #d48806; }
.goal-chip-status-cancelled { background: rgba(255, 77, 79, 0.11); color: #cf1322; }

.goal-tag {
  background: rgba(126, 87, 255, 0.10);
  color: #6f4fe8;
}

.goal-date-panel {
  display: grid;
  grid-template-columns: repeat(3, minmax(150px, 1fr));
  gap: 14px;
  max-width: 700px;
  margin-top: 30px;
}

.goal-date-card {
  display: flex;
  flex-direction: column;
  gap: 7px;
  min-height: 100px;
  padding: 18px 18px;
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.70);
  box-shadow: 0 14px 30px rgba(43, 56, 92, 0.055);
}

.goal-date-card .date-label {
  color: rgba(33, 51, 89, 0.52);
  font-size: 12px;
  font-weight: 800;
}

.goal-date-card strong {
  color: #111b35;
  font-size: 20px;
  font-weight: 900;
  line-height: 1;
}

.goal-date-card strong small {
  margin-left: 2px;
  font-size: 13px;
  font-weight: 820;
}

.goal-date-card .date-sub {
  color: rgba(33, 51, 89, 0.42);
  font-size: 12px;
  font-weight: 660;
}

.date-card-warning {
  border-color: rgba(255, 132, 38, 0.28);
  background: linear-gradient(135deg, rgba(255, 247, 232, 0.92), rgba(255, 233, 205, 0.72));
}

.date-card-warning strong,
.kpi-overdue {
  color: #cf4f12;
}

.goal-hero-actions {
  margin-top: 24px;
}

.goal-hero-actions :deep(.ant-btn) {
  min-width: 112px;
  height: 40px;
  border-radius: 14px;
}

.goal-hero-actions :deep(.ant-btn-primary) {
  box-shadow: 0 12px 24px rgba(22, 119, 255, 0.22);
}

.goal-progress-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 238px;
  flex-shrink: 0;
  gap: 18px;
  padding-top: 4px;
}

.goal-progress-ring {
  display: grid;
  place-items: center;
  width: 176px;
  height: 176px;
  border-radius: 50%;
  background:
    conic-gradient(#2a7fff var(--goal-progress), rgba(42, 127, 255, 0.12) 0),
    linear-gradient(135deg, rgba(238, 244, 255, 0.84), rgba(250, 252, 255, 0.92));
  box-shadow: inset 0 0 0 16px rgba(255, 255, 255, 0.76), 0 18px 36px rgba(42, 127, 255, 0.12);
}

.goal-progress-ring-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 132px;
  height: 132px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.86);
}

.goal-progress-ring-inner span {
  color: rgba(26, 47, 88, 0.48);
  font-size: 12px;
  font-weight: 900;
  letter-spacing: 3px;
}

.goal-progress-ring-inner strong {
  margin-top: 6px;
  color: #111b35;
  font-size: 42px;
  font-weight: 940;
  line-height: 1;
}

.goal-progress-ring-inner strong small {
  margin-left: 1px;
  font-size: 18px;
  font-weight: 850;
}

.goal-progress-ring-inner em {
  max-width: 106px;
  margin-top: 8px;
  overflow: hidden;
  color: rgba(26, 47, 88, 0.52);
  font-size: 12px;
  font-style: normal;
  font-weight: 720;
  text-align: center;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.goal-health-card {
  display: flex;
  align-items: center;
  gap: 9px;
  min-width: 190px;
  padding: 11px 16px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.64);
  color: rgba(26, 47, 88, 0.58);
  font-size: 12px;
  font-weight: 760;
  box-shadow: 0 10px 24px rgba(43, 56, 92, 0.055);
}

.goal-health-card strong {
  margin-left: auto;
  color: #cf4f12;
  font-weight: 860;
}

.health-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #2a7fff;
  box-shadow: 0 0 0 4px rgba(42, 127, 255, 0.14);
}

/* ── 摘要指标卡 ── */
.goal-summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 20px;
  margin-top: 26px;
}

.goal-summary-card {
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-height: 176px;
  padding: 26px 24px 22px;
  border: 1px solid rgba(255, 255, 255, 0.74);
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.90);
  box-shadow: 0 22px 46px rgba(43, 56, 92, 0.085), 0 1px 2px rgba(15, 35, 80, 0.03);
}

.summary-card-head {
  display: flex;
  align-items: center;
  gap: 12px;
}

.summary-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 12px;
  font-size: 16px;
}

.summary-sub .summary-icon { background: rgba(42, 127, 255, 0.12); color: #1677ff; }
.summary-task .summary-icon { background: rgba(82, 196, 26, 0.14); color: #2f9e44; }
.summary-habit .summary-icon { background: rgba(255, 126, 48, 0.16); color: #d45b18; }
.summary-log .summary-icon { background: rgba(126, 87, 255, 0.14); color: #7657ea; }

.summary-label {
  color: #1c2948;
  font-size: 14px;
  font-weight: 840;
}

.summary-code {
  margin-left: auto;
  color: rgba(31, 45, 78, 0.38);
  font-size: 11px;
  font-weight: 900;
  letter-spacing: 1px;
}

.summary-number {
  color: #111b35;
  font-size: 38px;
  font-weight: 940;
  line-height: 1;
}

.summary-number small {
  margin-left: 4px;
  color: rgba(31, 45, 78, 0.54);
  font-size: 13px;
  font-weight: 760;
}

.goal-summary-card :deep(.ant-progress-inner) {
  background: rgba(28, 42, 82, 0.07);
}

.goal-summary-card :deep(.ant-progress-bg) {
  background: linear-gradient(90deg, #2a7fff, #6d8cff);
}

.summary-dash-line {
  height: 8px;
  border-radius: 999px;
  background:
    repeating-linear-gradient(90deg, rgba(31, 45, 78, 0.08) 0 38px, transparent 38px 48px);
}

.summary-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: auto;
  color: rgba(31, 45, 78, 0.52);
  font-size: 12px;
  font-weight: 720;
}

.summary-footer :deep(.ant-btn) {
  height: auto;
  padding: 0;
  font-size: 12px;
  font-weight: 820;
}

/* ═══════ 双栏布局 ═══════ */
.goal-dual-columns {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 380px;
  gap: 20px;
  align-items: start;
}

.goal-col-left,
.goal-col-right {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* ═══════ Section Card 通用 ═══════ */
.goal-section-card {
  overflow: hidden;
  border: 1px solid rgba(28, 35, 55, 0.055);
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(253, 253, 255, 0.90));
}

.goal-section-card :deep(.ant-card-head) {
  background: rgba(255, 255, 255, 0.72);
}

.goal-section-card :deep(.ant-card-extra .ant-btn) {
  border-radius: 999px;
}

.goal-section-card :deep(.ant-empty) {
  padding: 28px 0;
}

.goal-section-card :deep(.ant-card-body) {
  padding: 22px;
}

.goal-section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}

.goal-section-header.compact {
  margin-bottom: 16px;
}

.section-title-group {
  display: flex;
  align-items: center;
  min-width: 0;
  gap: 12px;
}

.section-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  flex-shrink: 0;
  border-radius: 13px;
  font-size: 16px;
}

.section-icon-blue { background: rgba(42, 127, 255, 0.12); color: #1677ff; }
.section-icon-orange { background: rgba(255, 126, 48, 0.16); color: #d45b18; }
.section-icon-green { background: rgba(82, 196, 26, 0.14); color: #2f9e44; }
.section-icon-purple { background: rgba(126, 87, 255, 0.14); color: #7657ea; }

.section-title-group h3 {
  margin: 0;
  color: #111b35;
  font-size: 20px;
  font-weight: 900;
  line-height: 1.2;
}

.goal-section-header.compact .section-title-group h3 {
  font-size: 16px;
}

.section-title-group p {
  margin: 4px 0 0;
  color: rgba(31, 45, 78, 0.48);
  font-size: 13px;
  font-weight: 620;
}

.section-actions {
  flex-shrink: 0;
}

.goal-empty-workbench {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 250px;
  padding: 34px 24px;
  border: 1px dashed rgba(42, 127, 255, 0.34);
  border-radius: 22px;
  background:
    radial-gradient(circle at 50% 0%, rgba(42, 127, 255, 0.06), transparent 34%),
    rgba(255, 255, 255, 0.46);
  text-align: center;
}

.goal-empty-workbench.side-empty {
  min-height: 170px;
  padding: 26px 18px;
}

.empty-illustration {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 54px;
  height: 54px;
  margin-bottom: 14px;
  border: 1px solid rgba(42, 127, 255, 0.16);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.72);
  color: #1677ff;
  font-size: 24px;
  box-shadow: 0 12px 26px rgba(42, 127, 255, 0.08);
}

.goal-empty-workbench h4 {
  margin: 0 0 10px;
  color: #111b35;
  font-size: 18px;
  font-weight: 900;
}

.goal-empty-workbench p {
  max-width: 560px;
  margin: 0 0 18px;
  color: rgba(31, 45, 78, 0.56);
  font-size: 13px;
  font-weight: 620;
  line-height: 22px;
}

.empty-template-tags {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
  margin-top: 18px;
  color: rgba(31, 45, 78, 0.45);
  font-size: 12px;
  font-weight: 700;
}

.empty-template-tags em {
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(42, 127, 255, 0.10);
  color: #1768e8;
  font-style: normal;
}

.empty-habit {
  border-color: rgba(47, 158, 68, 0.26);
}

.empty-habit .empty-illustration {
  color: #2f9e44;
}

.empty-progress {
  border-color: rgba(126, 87, 255, 0.28);
}

.empty-progress .empty-illustration {
  color: #7657ea;
}

/* ═══════ 子目标卡片 ═══════ */
.sub-goal-card {
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(28, 35, 55, 0.045);
  border-radius: 22px;
  box-shadow: 0 14px 28px rgba(45, 45, 65, 0.05);
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.sub-goal-card::before {
  position: absolute;
  top: -32px;
  right: -28px;
  width: 120px;
  height: 120px;
  border-radius: 38px;
  background: rgba(255, 255, 255, 0.30);
  content: "";
  transform: rotate(18deg);
}

.sub-goal-card-tone-0 {
  background: linear-gradient(135deg, #fff2c9, #ffe0ad);
}

.sub-goal-card-tone-1 {
  background: linear-gradient(135deg, #dff0ff, #c8e4ff);
}

.sub-goal-card-tone-2 {
  background: linear-gradient(135deg, #eadfff, #d7cbff);
}

.sub-goal-card-tone-3 {
  background: linear-gradient(135deg, #ffe0ee, #ffcce3);
}

.sub-goal-card:hover {
  box-shadow: 0 18px 34px rgba(45, 45, 65, 0.08);
  transform: translateY(-1px);
}

.sub-goal-card :deep(.ant-card-body) {
  position: relative;
  z-index: 1;
  padding: 20px;
}

.sub-goal-title-wrap,
.task-main {
  min-width: 0;
  flex: 1;
}

.sub-goal-title,
.task-title {
  display: -webkit-box;
  margin: 6px 0 4px;
  overflow: hidden;
  color: rgba(0, 0, 0, 0.82);
  font-weight: 820;
  line-height: 24px;
  word-break: break-word;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.sub-goal-description,
.task-description {
  display: -webkit-box;
  min-height: 20px;
  margin-bottom: 0;
  overflow: hidden;
  color: rgba(0, 0, 0, 0.50);
  font-size: 13px;
  line-height: 21px;
  word-break: break-word;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.task-description {
  margin-bottom: 6px;
}

.sub-goal-meta,
.task-meta {
  color: rgba(0, 0, 0, 0.44);
  font-size: 12px;
  font-weight: 650;
}

.sub-goal-meta span,
.task-meta span {
  padding: 3px 9px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.48);
}

.sub-goal-control {
  width: 100%;
}

.sub-goal-control :deep(.ant-select-selector),
.sub-goal-control :deep(.ant-input-number) {
  border-color: rgba(28, 35, 55, 0.08);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.58);
}

.sub-goal-card :deep(.ant-progress-inner) {
  background: rgba(255, 255, 255, 0.46);
}

.sub-goal-card :deep(.ant-progress-bg) {
  background: rgba(23, 23, 32, 0.74);
}

.sub-goal-task-divider {
  margin: 6px 0;
  border-color: rgba(28, 35, 55, 0.07);
}

.sub-goal-task-empty {
  margin: 8px 0;
}

.task-list-item {
  margin-top: 8px;
  padding: 12px 14px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.48);
}

.task-list-item:not(:last-child) {
  border-bottom: 0;
}

.task-list-item :deep(.ant-list-item-action) {
  margin-inline-start: 0;
}

.task-status-select {
  width: 110px;
}

.task-status-select :deep(.ant-select-selector) {
  border-color: rgba(28, 35, 55, 0.08);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.62);
}

/* ═══════ 右栏：下一步行动 ═══════ */
.goal-col-right .goal-section-card {
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.94), rgba(248, 250, 255, 0.86));
}

.goal-next-action-card :deep(.ant-card-body) {
  padding: 16px 18px;
}

.next-action-item {
  padding: 11px 12px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.62);
}

.next-action-item:not(:last-child) {
  border-bottom: 0;
}

.next-action-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
  background: rgba(0, 0, 0, 0.18);
}

.next-action-dot.dot-not_started { background: rgba(0, 0, 0, 0.18); }
.next-action-dot.dot-in_progress {
  background: #1677ff;
  box-shadow: 0 0 0 4px rgba(22, 119, 255, 0.12);
}

.next-action-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
  flex: 1;
  gap: 2px;
}

.next-action-title {
  color: rgba(0, 0, 0, 0.82);
  font-size: 14px;
  font-weight: 700;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.next-action-sub {
  color: rgba(0, 0, 0, 0.38);
  font-size: 12px;
  font-weight: 600;
}

.next-action-status {
  width: 90px;
  flex-shrink: 0;
}

.next-action-status :deep(.ant-select-selector) {
  border-color: rgba(28, 35, 55, 0.08);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.66);
}

/* ═══════ 右栏：关联习惯（紧凑） ═══════ */
.habit-compact-item {
  padding: 12px;
  border-radius: 14px;
  background: linear-gradient(135deg, rgba(232, 248, 215, 0.74), rgba(214, 240, 195, 0.54));
}

.habit-compact-item:not(:last-child) {
  border-bottom: 0;
}

.habit-checkin-dot {
  width: 11px;
  height: 11px;
  border-radius: 50%;
  flex-shrink: 0;
  background: rgba(0, 0, 0, 0.14);
  border: 2px solid rgba(255, 255, 255, 0.76);
  box-shadow: 0 0 0 3px rgba(0, 0, 0, 0.04);
  transition: all 0.2s ease;
}

.habit-checkin-dot.checked {
  background: #52c41a;
  border-color: rgba(255, 255, 255, 0.88);
  box-shadow: 0 0 0 4px rgba(82, 196, 26, 0.16);
}

.habit-compact-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
  flex: 1;
  gap: 3px;
}

.habit-compact-title {
  color: rgba(0, 0, 0, 0.82);
  font-size: 14px;
  font-weight: 700;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.habit-compact-sub {
  color: rgba(0, 0, 0, 0.38);
  font-size: 12px;
  font-weight: 600;
}

.habit-compact-progress {
  margin-top: 8px;
}

.habit-compact-progress :deep(.ant-progress-inner) {
  background: rgba(255, 255, 255, 0.56);
}

.habit-compact-progress :deep(.ant-progress-bg) {
  background: #2f9e44;
}

/* ═══════ 右栏：进展时间线 ═══════ */
.progress-timeline {
  position: relative;
  padding: 2px 0;
}

.timeline-item {
  position: relative;
  display: flex;
  gap: 14px;
  padding: 12px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.56);
}

.timeline-item:not(:last-child) {
  margin-bottom: 10px;
  padding-bottom: 12px;
}

.timeline-rail {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
  width: 16px;
  padding-top: 4px;
}

.timeline-dot {
  width: 10px;
  height: 10px;
  border: 2px solid rgba(255, 255, 255, 0.88);
  border-radius: 50%;
  background: #1677ff;
  box-shadow: 0 0 0 4px rgba(22, 119, 255, 0.12);
  flex-shrink: 0;
}

.timeline-line {
  width: 2px;
  flex: 1;
  margin-top: 8px;
  background: linear-gradient(180deg, rgba(22, 119, 255, 0.20), rgba(22, 119, 255, 0.02));
  border-radius: 1px;
}

.timeline-content {
  min-width: 0;
  flex: 1;
  padding-bottom: 0;
}

.timeline-header {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.timeline-date {
  color: rgba(0, 0, 0, 0.50);
  font-size: 12px;
  font-weight: 700;
}

.timeline-progress-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 999px;
  background: rgba(82, 196, 26, 0.14);
  color: #2f8f16;
  font-size: 11px;
  font-weight: 760;
}

.timeline-delete {
  margin-left: auto;
  opacity: 0;
  transition: opacity 0.15s;
}

.timeline-item:hover .timeline-delete {
  opacity: 1;
}

.timeline-title {
  margin: 6px 0 4px;
  color: rgba(0, 0, 0, 0.82);
  font-size: 14px;
  font-weight: 760;
  line-height: 22px;
}

.timeline-body {
  margin: 0;
  color: rgba(0, 0, 0, 0.46);
  font-size: 13px;
  line-height: 20px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* ═══════ 响应式 ═══════ */
@media (max-width: 1200px) {
  .shanzhu-goal-detail-page {
    padding: 28px 24px 48px;
  }

  .goal-hero-content {
    padding: 24px;
  }

  .goal-dual-columns {
    grid-template-columns: minmax(0, 1fr) 340px;
    gap: 16px;
  }
}

@media (max-width: 960px) {
  .goal-dual-columns {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .shanzhu-goal-detail-page {
    padding: 18px 14px 36px;
  }

  .goal-hero-content {
    padding: 20px 18px;
  }

  .goal-hero-body {
    flex-direction: column;
  }

  .goal-title {
    font-size: 22px;
  }

  .goal-progress-orb {
    width: 80px;
    height: 80px;
    border-radius: 22px;
  }

  .goal-progress-orb span {
    font-size: 24px;
  }

  .goal-kpi-strip {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 8px;
  }

  .goal-kpi-item {
    padding: 10px 12px;
  }

  .goal-kpi-value {
    font-size: 18px;
  }

  .task-status-select,
  .next-action-status {
    width: 100%;
  }
}
</style>
