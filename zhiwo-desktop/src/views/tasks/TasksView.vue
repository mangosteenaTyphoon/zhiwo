<template>
  <div class="tasks-page">
    <!-- 头部 -->
    <header class="page-header">
      <div>
        <h1 class="page-title">任务</h1>
        <p class="page-subtitle">聚焦行动项，推动目标落地</p>
      </div>
      <button class="btn-primary" @click="openCreate">
        <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
          <line x1="8" y1="2" x2="8" y2="14"/><line x1="2" y1="8" x2="14" y2="8"/>
        </svg>
        新建任务
      </button>
    </header>

    <!-- 筛选标签 -->
    <div class="filter-tabs">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        class="filter-tab"
        :class="{ active: activeTab === tab.key }"
        @click="activeTab = tab.key; loadTasks()"
      >
        {{ tab.label }}
        <span class="tab-count">{{ tab.count }}</span>
      </button>
    </div>

    <!-- 任务列表 -->
    <div class="task-list" v-if="tasks.length">
      <div
        v-for="task in tasks"
        :key="task.id"
        class="task-card"
        :class="{ completed: task.status === 'completed' }"
      >
        <!-- 状态复选框 -->
        <button
          class="task-checkbox"
          :class="{ done: task.status === 'completed' }"
          @click="toggleStatus(task)"
          :title="task.status === 'completed' ? '重新打开' : '标记完成'"
        >
          <svg v-if="task.status === 'completed'" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="3,8 7,12 13,4"/>
          </svg>
        </button>

        <!-- 内容 -->
        <div class="task-content" @click="openEdit(task)">
          <div class="task-top">
            <span class="task-title" :class="{ 'line-through': task.status === 'completed' }">
              {{ task.title }}
            </span>
            <span class="task-priority" :class="'p' + task.priority">
              {{ priorityLabel(task.priority) }}
            </span>
          </div>
          <div class="task-meta">
            <span class="task-goal" v-if="task.goalTitle">
              <span class="goal-dot" :style="{ background: task.categoryColor || '#0071e3' }"></span>
              {{ task.goalTitle }}
            </span>
            <span class="task-date" v-if="task.plannedDate">{{ task.plannedDate }}</span>
            <span class="task-deadline" v-if="task.deadline" :class="{ overdue: isOverdue(task) }">
              ⏱ {{ formatDeadline(task.deadline) }}
            </span>
            <span class="task-estimate" v-if="task.estimatedMinutes">
              {{ task.estimatedMinutes }}min
            </span>
          </div>
        </div>

        <!-- 操作 -->
        <button class="task-delete" @click.stop="handleDelete(task)" title="删除">
          <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round">
            <line x1="4" y1="4" x2="12" y2="12"/><line x1="12" y1="4" x2="4" y2="12"/>
          </svg>
        </button>
      </div>
    </div>

    <!-- 空状态 -->
    <div class="empty-state" v-else-if="!loading">
      <svg viewBox="0 0 64 64" fill="none" stroke="currentColor" stroke-width="1" opacity="0.3">
        <rect x="14" y="10" width="36" height="44" rx="4"/>
        <line x1="22" y1="22" x2="42" y2="22"/><line x1="22" y1="30" x2="42" y2="30"/><line x1="22" y1="38" x2="34" y2="38"/>
      </svg>
      <p>暂无任务</p>
      <button class="btn-secondary" @click="openCreate">创建第一个任务</button>
    </div>

    <!-- 加载 -->
    <div class="loading" v-if="loading">
      <p>加载中...</p>
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="totalPages > 1">
      <button :disabled="pageNum <= 1" @click="pageNum--; loadTasks()">上一页</button>
      <span>{{ pageNum }} / {{ totalPages }}</span>
      <button :disabled="pageNum >= totalPages" @click="pageNum++; loadTasks()">下一页</button>
    </div>

    <!-- 编辑弹窗 -->
    <div class="modal-overlay" v-if="showModal" @click.self="closeModal">
      <div class="modal-card">
        <h2 class="modal-title">{{ editId ? '编辑任务' : '新建任务' }}</h2>
        <form @submit.prevent="handleSave" class="modal-form">
          <div class="form-group">
            <label>关联目标 <span>*</span></label>
            <select v-model="form.goalId" required>
              <option value="">请选择目标</option>
              <option v-for="g in goals" :key="g.id" :value="g.id">{{ g.title }}</option>
            </select>
          </div>
          <div class="form-group">
            <label>任务标题 <span>*</span></label>
            <input v-model="form.title" type="text" placeholder="输入任务标题" required maxlength="120" />
          </div>
          <div class="form-group">
            <label>任务说明</label>
            <textarea v-model="form.description" placeholder="补充说明（可选）" rows="3" />
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>优先级</label>
              <select v-model.number="form.priority">
                <option :value="2">中</option>
                <option :value="1">高</option>
                <option :value="3">低</option>
              </select>
            </div>
            <div class="form-group">
              <label>计划日期</label>
              <input v-model="form.plannedDate" type="date" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>截止时间</label>
              <input v-model="form.deadline" type="datetime-local" />
            </div>
            <div class="form-group">
              <label>预计耗时(分钟)</label>
              <input v-model.number="form.estimatedMinutes" type="number" min="1" placeholder="30" />
            </div>
          </div>
          <div class="modal-actions">
            <button type="button" class="btn-cancel" @click="closeModal">取消</button>
            <button type="submit" class="btn-primary" :disabled="saving">
              {{ saving ? '保存中...' : '保存' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from "vue";
import { getTaskPage, saveTask, deleteTask, updateTaskStatus, type TaskVO, type TaskSave } from "@/api/task";
import { getGoalPage, type GoalVO } from "@/api/goal";

// 筛选标签
const tabs = [
  { key: "", label: "全部", count: 0 },
  { key: "not_started", label: "未开始", count: 0 },
  { key: "in_progress", label: "进行中", count: 0 },
  { key: "completed", label: "已完成", count: 0 },
];
const activeTab = ref("");

// 列表数据
const tasks = ref<TaskVO[]>([]);
const loading = ref(false);
const pageNum = ref(1);
const pageSize = 20;
const totalPages = ref(1);
const goals = ref<GoalVO[]>([]);

async function loadTasks() {
  loading.value = true;
  try {
    const result = await getTaskPage({
      pageNum: pageNum.value,
      pageSize,
      status: activeTab.value || undefined,
    });
    tasks.value = result.records || [];
    totalPages.value = result.pages || 1;
    // 更新计数
    // TODO: 可以通过额外 API 获取各状态数量
  } finally {
    loading.value = false;
  }
}

async function loadGoals() {
  try {
    const result = await getGoalPage({ pageSize: 100 });
    goals.value = result.records || [];
  } catch { /* ignore */ }
}

// 创建/编辑
const showModal = ref(false);
const editId = ref("");
const saving = ref(false);
const form = reactive<TaskSave>({
  goalId: "",
  title: "",
  description: "",
  priority: 2,
  plannedDate: "",
  deadline: "",
  estimatedMinutes: undefined,
});

function openCreate() {
  editId.value = "";
  Object.assign(form, {
    goalId: "", title: "", description: "",
    priority: 2, plannedDate: "", deadline: "",
    estimatedMinutes: undefined,
  });
  showModal.value = true;
}

function openEdit(task: TaskVO) {
  editId.value = task.id;
  Object.assign(form, {
    goalId: task.goalId,
    title: task.title,
    description: task.description || "",
    priority: task.priority || 2,
    plannedDate: task.plannedDate || "",
    deadline: task.deadline ? task.deadline.replace(" ", "T") : "",
    estimatedMinutes: task.estimatedMinutes,
  });
  showModal.value = true;
}

function closeModal() {
  showModal.value = false;
}

async function handleSave() {
  saving.value = true;
  try {
    const data: TaskSave = { ...form };
    if (!data.deadline) delete data.deadline;
    if (!data.plannedDate) delete data.plannedDate;
    if (editId.value) data.id = editId.value;
    await saveTask(data);
    closeModal();
    await loadTasks();
  } finally {
    saving.value = false;
  }
}

// 状态切换
async function toggleStatus(task: TaskVO) {
  const newStatus = task.status === "completed" ? "in_progress" : "completed";
  await updateTaskStatus(task.id, newStatus);
  await loadTasks();
}

// 删除
async function handleDelete(task: TaskVO) {
  if (!confirm(`确定删除「${task.title}」？`)) return;
  await deleteTask(task.id);
  await loadTasks();
}

// 工具
function priorityLabel(p: number) {
  if (p === 1) return "高";
  if (p === 3) return "低";
  return "中";
}
function isOverdue(task: TaskVO) {
  if (!task.deadline || task.status === "completed") return false;
  return new Date(task.deadline) < new Date();
}
function formatDeadline(date: string) {
  const d = new Date(date);
  const now = new Date();
  const diff = d.getTime() - now.getTime();
  const days = Math.ceil(diff / 86400000);
  if (days < 0) return `逾期 ${Math.abs(days)} 天`;
  if (days === 0) return "今天截止";
  return `${days} 天后`;
}

onMounted(() => {
  loadTasks();
  loadGoals();
});
</script>

<style scoped>
.tasks-page { max-width: 900px; margin: 0 auto; }

/* 头部 */
.page-header {
  display: flex; align-items: flex-start; justify-content: space-between;
  margin-bottom: 24px;
}
.page-title { font-size: 28px; font-weight: 700; letter-spacing: -0.5px; color: var(--z-text-primary); margin-bottom: 4px; }
.page-subtitle { font-size: 14px; color: var(--z-text-secondary); }

.btn-primary {
  display: inline-flex; align-items: center; gap: 6px;
  height: 38px; padding: 0 18px; border-radius: 10px;
  background: var(--z-accent); color: white; border: none;
  font-size: 14px; font-weight: 600; cursor: pointer;
  transition: all 0.2s ease; font-family: inherit;
}
.btn-primary:hover { background: var(--z-accent-hover); box-shadow: 0 2px 8px rgba(0,113,227,0.25); }
.btn-primary svg { width: 14px; height: 14px; }
.btn-secondary {
  display: inline-flex; align-items: center; gap: 6px;
  height: 34px; padding: 0 14px; border-radius: 8px;
  background: rgba(0,0,0,0.04); color: var(--z-text-primary); border: 1px solid rgba(0,0,0,0.08);
  font-size: 13px; cursor: pointer; transition: all 0.2s ease; font-family: inherit;
}
.btn-secondary:hover { background: rgba(0,0,0,0.06); }

/* 筛选标签 */
.filter-tabs { display: flex; gap: 8px; margin-bottom: 20px; }
.filter-tab {
  padding: 6px 14px; border-radius: 8px; border: none;
  background: rgba(0,0,0,0.03); color: var(--z-text-secondary);
  font-size: 13px; font-weight: 500; cursor: pointer;
  transition: all 0.2s ease; font-family: inherit;
}
.filter-tab:hover { background: rgba(0,0,0,0.06); }
.filter-tab.active { background: var(--z-accent); color: white; }
.tab-count { font-size: 11px; opacity: 0.7; margin-left: 4px; }

/* 任务卡片 */
.task-list { display: flex; flex-direction: column; gap: 6px; }
.task-card {
  display: flex; align-items: flex-start; gap: 12px;
  padding: 14px 16px; border-radius: 12px;
  background: rgba(255,255,255,0.7);
  border: 1px solid rgba(0,0,0,0.04);
  transition: all 0.15s ease;
}
.task-card:hover { background: rgba(255,255,255,0.95); box-shadow: 0 2px 12px rgba(0,0,0,0.04); }
.task-card.completed { opacity: 0.6; }

.task-checkbox {
  width: 22px; height: 22px; border-radius: 50%; flex-shrink: 0;
  border: 2px solid rgba(0,0,0,0.15); background: none;
  cursor: pointer; display: flex; align-items: center; justify-content: center;
  transition: all 0.2s ease; margin-top: 1px;
}
.task-checkbox:hover { border-color: var(--z-accent); }
.task-checkbox.done { background: var(--z-accent); border-color: var(--z-accent); }
.task-checkbox.done svg { width: 12px; height: 12px; color: white; }

.task-content { flex: 1; cursor: pointer; min-width: 0; }
.task-top { display: flex; align-items: center; gap: 8px; margin-bottom: 4px; }
.task-title { font-size: 14px; font-weight: 600; color: var(--z-text-primary); }
.task-title.line-through { text-decoration: line-through; color: var(--z-text-tertiary); }

.task-priority {
  font-size: 10px; font-weight: 600; padding: 1px 6px; border-radius: 4px; flex-shrink: 0;
}
.task-priority.p1 { background: rgba(255,59,48,0.1); color: #ff3b30; }
.task-priority.p2 { background: rgba(255,149,0,0.1); color: #ff9500; }
.task-priority.p3 { background: rgba(52,199,89,0.1); color: #34c759; }

.task-meta { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.task-goal { font-size: 12px; color: var(--z-text-secondary); display: flex; align-items: center; gap: 4px; }
.goal-dot { width: 6px; height: 6px; border-radius: 50%; flex-shrink: 0; }
.task-date { font-size: 12px; color: var(--z-text-tertiary); }
.task-deadline { font-size: 12px; color: var(--z-text-secondary); }
.task-deadline.overdue { color: #ff3b30; font-weight: 500; }
.task-estimate { font-size: 12px; color: var(--z-text-tertiary); }

.task-delete {
  width: 28px; height: 28px; border-radius: 6px; border: none;
  background: transparent; cursor: pointer; display: flex;
  align-items: center; justify-content: center;
  color: var(--z-text-tertiary); transition: all 0.15s ease;
  flex-shrink: 0; margin-top: -2px;
}
.task-delete:hover { background: rgba(255,59,48,0.1); color: #ff3b30; }
.task-delete svg { width: 14px; height: 14px; }

/* 空状态 */
.empty-state { text-align: center; padding: 60px 20px; color: var(--z-text-tertiary); }
.empty-state svg { width: 64px; height: 64px; margin-bottom: 16px; }
.empty-state p { font-size: 15px; margin-bottom: 16px; }
.loading { text-align: center; padding: 40px; color: var(--z-text-tertiary); }

/* 分页 */
.pagination { display: flex; align-items: center; justify-content: center; gap: 16px; margin-top: 24px; }
.pagination button {
  padding: 6px 12px; border-radius: 6px; border: 1px solid rgba(0,0,0,0.08);
  background: white; cursor: pointer; font-size: 13px; transition: all 0.15s ease; font-family: inherit;
}
.pagination button:hover:not(:disabled) { background: rgba(0,0,0,0.04); }
.pagination button:disabled { opacity: 0.4; cursor: not-allowed; }
.pagination span { font-size: 13px; color: var(--z-text-secondary); }

/* 弹窗 */
.modal-overlay {
  position: fixed; inset: 0; z-index: 1000;
  display: flex; align-items: center; justify-content: center;
  background: rgba(0,0,0,0.2); backdrop-filter: blur(8px);
}
.modal-card {
  width: 480px; max-height: 80vh; overflow-y: auto;
  padding: 28px 32px; border-radius: 20px;
  background:
    linear-gradient(160deg, rgba(255,255,255,0.35) 0%, rgba(255,255,255,0.1) 50%, rgba(255,255,255,0.2) 100%);
  backdrop-filter: blur(60px) saturate(200%);
  box-shadow:
    0 0 0 0.5px rgba(255,255,255,0.5),
    0 8px 40px rgba(0,0,0,0.12);
}
.modal-title { font-size: 18px; font-weight: 700; margin-bottom: 20px; color: var(--z-text-primary); }

.modal-form { display: flex; flex-direction: column; gap: 14px; }
.form-group { display: flex; flex-direction: column; gap: 5px; flex: 1; }
.form-group label { font-size: 12px; font-weight: 600; color: var(--z-text-tertiary); text-transform: uppercase; letter-spacing: 0.3px; }
.form-group label span { color: var(--z-danger); }
.form-group input, .form-group select, .form-group textarea {
  height: 38px; padding: 0 12px; border-radius: 8px;
  border: 1px solid rgba(0,0,0,0.08); background: rgba(255,255,255,0.6);
  font-size: 14px; color: var(--z-text-primary); outline: none;
  transition: all 0.2s ease; font-family: inherit;
}
.form-group textarea { height: auto; padding: 10px 12px; resize: vertical; }
.form-group input:focus, .form-group select:focus, .form-group textarea:focus {
  border-color: var(--z-accent); box-shadow: 0 0 0 3px rgba(0,113,227,0.1);
}
.form-row { display: flex; gap: 12px; }

.modal-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 8px; }
.btn-cancel {
  height: 38px; padding: 0 18px; border-radius: 10px;
  background: rgba(0,0,0,0.04); color: var(--z-text-secondary);
  border: 1px solid rgba(0,0,0,0.08); font-size: 14px; cursor: pointer;
  transition: all 0.2s ease; font-family: inherit;
}
.btn-cancel:hover { background: rgba(0,0,0,0.08); }
</style>
