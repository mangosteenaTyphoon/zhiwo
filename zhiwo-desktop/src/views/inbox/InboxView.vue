<template>
  <div class="inbox-page">
    <!-- 头部：标题 + 快速添加 -->
    <header class="inbox-header">
      <div class="header-top">
        <div>
          <h1 class="page-title">收集箱</h1>
          <p class="page-subtitle">想到什么就记，不用分类，定期整理</p>
        </div>
        <button class="btn-primary" @click="openCreate">
          <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
            <line x1="8" y1="2" x2="8" y2="14"/><line x1="2" y1="8" x2="14" y2="8"/>
          </svg>
          新建
        </button>
      </div>
      <div class="quick-add">
        <input
          v-model="quickAddTitle"
          type="text"
          placeholder="记下一个想法、待办或任何事情，按回车添加..."
          class="quick-input"
          @keydown.enter.prevent="handleQuickAdd"
        />
        <div class="quick-actions">
          <button class="quick-btn" title="添加到收集箱" @click="handleQuickAdd" :disabled="quickAddLoading">
            <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="14,6 8,12 2,6"/>
              <line x1="8" y1="12" x2="8" y2="2"/>
            </svg>
          </button>
          <button class="quick-btn" title="添加并设为今日待办" @click="handleQuickAddToToday" :disabled="quickAddLoading">
            <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round">
              <rect x="2" y="3" width="12" height="11" rx="2"/>
              <line x1="2" y1="7" x2="14" y2="7"/>
              <line x1="6" y1="1" x2="6" y2="5"/>
              <line x1="10" y1="1" x2="10" y2="5"/>
            </svg>
          </button>
        </div>
      </div>
    </header>

    <!-- 主体：左侧列表 + 右侧边栏 -->
    <div class="inbox-body">
      <div class="inbox-main">
        <!-- 筛选标签 + 搜索 -->
        <div class="toolbar">
          <div class="filter-tabs">
            <button
              v-for="tab in statusTabs"
              :key="tab.value ?? 'all'"
              class="filter-tab"
              :class="{ active: query.status === tab.value }"
              @click="handleTabChange(tab.value)"
            >
              {{ tab.label }}
              <span v-if="tab.count !== undefined" class="tab-count">{{ tab.count }}</span>
            </button>
          </div>
          <div class="search-box-inline">
            <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.2">
              <circle cx="7" cy="7" r="4.5"/>
              <line x1="10.5" y1="10.5" x2="14" y2="14" stroke-linecap="round"/>
            </svg>
            <input
              v-model="query.keyword"
              type="text"
              placeholder="搜索..."
              @keydown.enter="handleSearch"
            />
            <button v-if="query.keyword" class="search-clear" @click="clearSearch">
              <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round">
                <line x1="4" y1="4" x2="12" y2="12"/><line x1="12" y1="4" x2="4" y2="12"/>
              </svg>
            </button>
          </div>
        </div>

        <!-- Todo 列表 -->
        <div class="todo-list-card">
          <div v-if="loading" class="loading">加载中...</div>
          <div v-else-if="todoList.length === 0" class="empty-state">
            <div class="empty-illustration">
              <svg viewBox="0 0 120 120" fill="none">
                <rect x="20" y="25" width="80" height="70" rx="12" fill="rgba(0,113,227,0.04)" stroke="rgba(0,113,227,0.15)" stroke-width="1.5"/>
                <rect x="35" y="45" width="50" height="4" rx="2" fill="rgba(0,113,227,0.12)"/>
                <rect x="35" y="55" width="35" height="4" rx="2" fill="rgba(0,113,227,0.08)"/>
                <rect x="35" y="65" width="42" height="4" rx="2" fill="rgba(0,113,227,0.06)"/>
                <circle cx="88" cy="38" r="14" fill="rgba(0,113,227,0.06)" stroke="rgba(0,113,227,0.12)" stroke-width="1.5"/>
                <path d="M82 38L86 42L94 34" stroke="rgba(0,113,227,0.25)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <h3>这里空空如也</h3>
            <p>在上方输入框写下想法，按回车即可快速添加</p>
            <button class="btn-secondary" @click="openCreate">
              <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                <line x1="8" y1="2" x2="8" y2="14"/><line x1="2" y1="8" x2="14" y2="8"/>
              </svg>
              添加第一条
            </button>
          </div>
          <div v-else class="todo-list">
            <div
              v-for="todo in todoList"
              :key="todo.id"
              class="todo-item"
              :class="{
                'todo-done': todo.status === 'done',
                'todo-high': todo.priority === 1 && todo.status !== 'done'
              }"
            >
              <button
                class="todo-checkbox"
                :class="{ checked: todo.status === 'done' }"
                @click="todo.status === 'done' ? handleUncomplete(todo) : handleComplete(todo)"
              >
                <svg v-if="todo.status === 'done'" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="3,8 7,12 13,4"/>
                </svg>
              </button>

              <div class="todo-content" @click="openEdit(todo)">
                <div class="todo-title" :class="{ 'title-done': todo.status === 'done' }">
                  {{ todo.title }}
                </div>
                <div v-if="todo.description" class="todo-desc">{{ todo.description }}</div>
                <div class="todo-tags">
                  <span v-if="todo.priority === 1" class="tag tag-red">高优</span>
                  <span v-if="todo.priority === 3" class="tag tag-blue">低优</span>
                  <span v-if="todo.plannedDate" class="tag tag-date">
                    <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round">
                      <rect x="2" y="3" width="12" height="11" rx="2"/><line x1="2" y1="7" x2="14" y2="7"/><line x1="6" y1="1" x2="6" y2="5"/><line x1="10" y1="1" x2="10" y2="5"/>
                    </svg>
                    {{ todo.plannedDate }}
                  </span>
                  <span v-if="todo.goalTitle" class="tag tag-goal">
                    <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round">
                      <circle cx="8" cy="8" r="6"/><circle cx="8" cy="8" r="2"/>
                    </svg>
                    {{ todo.goalTitle }}
                  </span>
                </div>
              </div>

              <div class="todo-actions">
                <button
                  v-if="todo.status === 'inbox'"
                  class="action-btn"
                  title="移入今日待办"
                  @click.stop="handleMoveToToday(todo)"
                >
                  <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round">
                    <rect x="2" y="3" width="12" height="11" rx="2"/><line x1="2" y1="7" x2="14" y2="7"/><line x1="6" y1="1" x2="6" y2="5"/><line x1="10" y1="1" x2="10" y2="5"/>
                  </svg>
                </button>
                <button
                  v-if="todo.status === 'today'"
                  class="action-btn"
                  title="移回收集箱"
                  @click.stop="handleMoveToInbox(todo)"
                >
                  <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round">
                    <polyline points="14,6 8,12 2,6"/><line x1="8" y1="12" x2="8" y2="2"/>
                  </svg>
                </button>
                <div class="dropdown">
                  <button class="action-btn" @click.stop="toggleDropdown(todo.id)">
                    <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round">
                      <circle cx="8" cy="4" r="1" fill="currentColor" stroke="none"/><circle cx="8" cy="8" r="1" fill="currentColor" stroke="none"/><circle cx="8" cy="12" r="1" fill="currentColor" stroke="none"/>
                    </svg>
                  </button>
                  <div v-if="activeDropdown === todo.id" class="dropdown-menu">
                    <button v-if="todo.status !== 'done'" class="dropdown-item" @click="handleComplete(todo); closeDropdown()">
                      <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                        <polyline points="3,8 7,12 13,4"/>
                      </svg>
                      标记完成
                    </button>
                    <button v-if="todo.status === 'done'" class="dropdown-item" @click="handleUncomplete(todo); closeDropdown()">
                      <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                        <polyline points="3,8 7,12 13,4"/><line x1="3" y1="4" x2="13" y2="12"/>
                      </svg>
                      取消完成
                    </button>
                    <button v-if="todo.status === 'inbox'" class="dropdown-item" @click="handleMoveToToday(todo); closeDropdown()">
                      <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round">
                        <rect x="2" y="3" width="12" height="11" rx="2"/><line x1="2" y1="7" x2="14" y2="7"/><line x1="6" y1="1" x2="6" y2="5"/><line x1="10" y1="1" x2="10" y2="5"/>
                      </svg>
                      移入今日待办
                    </button>
                    <button v-if="todo.status === 'today'" class="dropdown-item" @click="handleMoveToInbox(todo); closeDropdown()">
                      <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round">
                        <polyline points="14,6 8,12 2,6"/><line x1="8" y1="12" x2="8" y2="2"/>
                      </svg>
                      移回收集箱
                    </button>
                    <button v-if="todo.status !== 'converted'" class="dropdown-item" @click="handleConvertToTask(todo); closeDropdown()">
                      <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                        <rect x="2" y="2" width="5" height="5" rx="1"/><rect x="9" y="2" width="5" height="5" rx="1"/><rect x="2" y="9" width="5" height="5" rx="1"/><rect x="9" y="9" width="5" height="5" rx="1"/>
                      </svg>
                      转为任务
                    </button>
                    <button class="dropdown-item" @click="openEdit(todo); closeDropdown()">
                      <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round">
                        <path d="M11 2l3 3-8 8H3v-3l8-8z"/><line x1="3" y1="13" x2="13" y2="13"/>
                      </svg>
                      编辑
                    </button>
                    <div class="dropdown-divider"></div>
                    <button class="dropdown-item danger" @click="confirmDelete(todo); closeDropdown()">
                      <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round">
                        <line x1="4" y1="4" x2="12" y2="12"/><line x1="12" y1="4" x2="4" y2="12"/>
                      </svg>
                      删除
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 分页 -->
          <div v-if="totalPages > 1" class="pagination">
            <button :disabled="pageNum <= 1" @click="pageNum--; loadTodoList()">上一页</button>
            <span>{{ pageNum }} / {{ totalPages }}</span>
            <button :disabled="pageNum >= totalPages" @click="pageNum++; loadTodoList()">下一页</button>
          </div>
        </div>
      </div>

      <!-- 右侧边栏 -->
      <aside class="inbox-sidebar">
        <!-- 概览 -->
        <div class="sidebar-card">
          <h4 class="sidebar-title">
            <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round">
              <line x1="8" y1="2" x2="8" y2="14"/><line x1="2" y1="8" x2="14" y2="8"/><line x1="4" y1="4" x2="4" y2="4" stroke-width="2" stroke-linecap="round"/><line x1="12" y1="4" x2="12" y2="4" stroke-width="2" stroke-linecap="round"/><line x1="4" y1="12" x2="4" y2="12" stroke-width="2" stroke-linecap="round"/><line x1="12" y1="12" x2="12" y2="12" stroke-width="2" stroke-linecap="round"/>
            </svg>
            概览
          </h4>
          <div class="stat-list">
            <div class="stat-item">
              <span class="stat-label">收集箱</span>
              <span class="stat-value">{{ stats.inboxCount }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">今日待办</span>
              <span class="stat-value stat-blue">{{ stats.todayCount }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">已完成</span>
              <span class="stat-value stat-green">{{ stats.doneCount }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">已归档</span>
              <span class="stat-value">{{ stats.archivedCount }}</span>
            </div>
          </div>
        </div>

        <!-- 快捷操作 -->
        <div class="sidebar-card">
          <h4 class="sidebar-title">
            <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round">
              <polygon points="9,2 3,9 8,9 7,14 13,7 8,7"/>
            </svg>
            快捷操作
          </h4>
          <div class="sidebar-actions">
            <button class="sidebar-btn" @click="$router.push('/today')">
              <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round">
                <rect x="2" y="3" width="12" height="11" rx="2"/><line x1="2" y1="7" x2="14" y2="7"/><line x1="6" y1="1" x2="6" y2="5"/><line x1="10" y1="1" x2="10" y2="5"/>
              </svg>
              今日工作台
            </button>
            <button class="sidebar-btn" @click="$router.push('/tasks')">
              <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <rect x="2" y="2" width="5" height="5" rx="1"/><rect x="9" y="2" width="5" height="5" rx="1"/><rect x="2" y="9" width="5" height="5" rx="1"/><rect x="9" y="9" width="5" height="5" rx="1"/>
              </svg>
              任务中心
            </button>
            <button class="sidebar-btn" @click="$router.push('/goals')">
              <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round">
                <circle cx="8" cy="8" r="6"/><circle cx="8" cy="8" r="2"/>
              </svg>
              目标列表
            </button>
          </div>
        </div>

        <!-- 使用提示 -->
        <div class="sidebar-card sidebar-tip">
          <h4 class="sidebar-title">
            <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round">
              <path d="M8 2v2M8 12v2M2 8h2M12 8h2M4.3 4.3l1.4 1.4M10.3 10.3l1.4 1.4M4.3 11.7l1.4-1.4M10.3 5.7l1.4-1.4"/>
            </svg>
            使用提示
          </h4>
          <ul class="tip-list">
            <li>想到什么就记，不用分类</li>
            <li>定期整理：转任务 / 移入今日 / 删除</li>
            <li>重要的事标高优先级</li>
            <li>按回车快速添加</li>
          </ul>
        </div>
      </aside>
    </div>

    <!-- 新建/编辑弹窗 -->
    <div class="modal-overlay" v-if="showModal" @click.self="closeModal">
      <div class="modal-card">
        <h2 class="modal-title">{{ editId ? '编辑 Todo' : '新建 Todo' }}</h2>
        <form @submit.prevent="handleSave" class="modal-form">
          <div class="form-group">
            <label>标题 <span>*</span></label>
            <input v-model="form.title" type="text" placeholder="这件事是..." required maxlength="120" />
          </div>
          <div class="form-group">
            <label>说明</label>
            <textarea v-model="form.description" placeholder="补充一下细节（可选）" rows="3" maxlength="500" />
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>状态</label>
              <select v-model="form.status">
                <option v-for="item in statusOptions" :key="item.value" :value="item.value">{{ item.label }}</option>
              </select>
            </div>
            <div class="form-group">
              <label>优先级</label>
              <select v-model.number="form.priority">
                <option :value="1">🔴 高</option>
                <option :value="2">🟡 中</option>
                <option :value="3">🔵 低</option>
              </select>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>计划日期</label>
              <input v-model="form.plannedDate" type="date" />
            </div>
            <div class="form-group">
              <label>截止时间</label>
              <input v-model="form.deadline" type="datetime-local" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>关联目标</label>
              <select v-model="form.goalId">
                <option value="">无</option>
                <option v-for="goal in goals" :key="goal.id" :value="goal.id">{{ goal.title }}</option>
              </select>
            </div>
            <div class="form-group">
              <label>预计耗时</label>
              <input v-model.number="form.estimatedMinutes" type="number" min="0" placeholder="分钟" />
            </div>
          </div>
          <div class="form-group">
            <label>备注</label>
            <textarea v-model="form.remark" placeholder="其他备注（可选）" rows="2" maxlength="300" />
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
import { ref, reactive, computed, onMounted, onBeforeUnmount } from "vue";
import {
  getTodoPage,
  getTodoList,
  saveTodo,
  deleteTodo,
  completeTodo,
  uncompleteTodo,
  moveToToday,
  moveToInbox,
  convertTodoToTask,
  type TodoVO,
  type TodoSave,
  type TodoQuery,
} from "@/api/todo";
import { getGoalPage, type GoalVO } from "@/api/goal";

const inboxStatus = "inbox";
const todayStatus = "today";
const doneStatus = "done";

const statusOptions = [
  { label: "收集箱", value: "inbox" },
  { label: "今日待办", value: "today" },
  { label: "已完成", value: "done" },
  { label: "已归档", value: "archived" },
  { label: "已转任务", value: "converted" },
];

const stats = reactive({
  totalCount: 0,
  inboxCount: 0,
  todayCount: 0,
  doneCount: 0,
  archivedCount: 0,
});

const statusTabs = computed(() => [
  { label: "全部", value: undefined as string | undefined, count: stats.totalCount },
  { label: "收集箱", value: "inbox", count: stats.inboxCount },
  { label: "今日", value: "today", count: stats.todayCount },
  { label: "已完成", value: "done", count: stats.doneCount },
  { label: "已归档", value: "archived", count: stats.archivedCount },
]);

const query = ref<TodoQuery>({ pageNum: 1, pageSize: 10 });
const todoList = ref<TodoVO[]>([]);
const goals = ref<GoalVO[]>([]);
const loading = ref(false);
const pageNum = ref(1);
const pageSize = ref(10);
const totalPages = ref(1);

const quickAddTitle = ref("");
const quickAddLoading = ref(false);

// 弹窗
const showModal = ref(false);
const editId = ref("");
const saving = ref(false);
const form = reactive<TodoSave>({
  title: "",
  description: "",
  status: inboxStatus,
  priority: 2,
  plannedDate: "",
  deadline: "",
  goalId: "",
  estimatedMinutes: undefined,
  remark: "",
});

// 下拉菜单
const activeDropdown = ref<string | null>(null);

function toggleDropdown(id: string) {
  activeDropdown.value = activeDropdown.value === id ? null : id;
}
function closeDropdown() {
  activeDropdown.value = null;
}
function onDocClick(e: MouseEvent) {
  const target = e.target as HTMLElement;
  if (!target.closest(".dropdown")) {
    activeDropdown.value = null;
  }
}

async function loadTodoList() {
  loading.value = true;
  try {
    const result = await getTodoPage({
      ...query.value,
      pageNum: pageNum.value,
      pageSize: pageSize.value,
    });
    todoList.value = result.records || [];
    totalPages.value = result.pages || 1;
  } finally {
    loading.value = false;
  }
}

async function loadStats() {
  try {
    const [allRes, inboxRes, todayRes, doneRes, archivedRes] = await Promise.all([
      getTodoList({}),
      getTodoList({ status: inboxStatus }),
      getTodoList({ status: todayStatus }),
      getTodoList({ status: doneStatus }),
      getTodoList({ status: "archived" }),
    ]);
    stats.totalCount = allRes?.length || 0;
    stats.inboxCount = inboxRes?.length || 0;
    stats.todayCount = todayRes?.length || 0;
    stats.doneCount = doneRes?.length || 0;
    stats.archivedCount = archivedRes?.length || 0;
  } catch {
    // 统计失败不影响主流程
  }
}

async function loadGoals() {
  try {
    const result = await getGoalPage({ pageSize: 100 });
    goals.value = result.records || [];
  } catch { /* ignore */ }
}

async function refresh() {
  await Promise.all([loadTodoList(), loadStats()]);
}

function handleTabChange(status?: string) {
  query.value.status = status;
  pageNum.value = 1;
  loadTodoList();
}

function handleSearch() {
  pageNum.value = 1;
  loadTodoList();
}

function clearSearch() {
  query.value.keyword = "";
  pageNum.value = 1;
  loadTodoList();
}

async function handleQuickAdd() {
  const title = quickAddTitle.value.trim();
  if (!title) return;
  quickAddLoading.value = true;
  try {
    await saveTodo({ title, status: inboxStatus });
    quickAddTitle.value = "";
    await refresh();
  } finally {
    quickAddLoading.value = false;
  }
}

async function handleQuickAddToToday() {
  const title = quickAddTitle.value.trim();
  if (!title) return;
  quickAddLoading.value = true;
  try {
    const today = new Date();
    const y = today.getFullYear();
    const m = String(today.getMonth() + 1).padStart(2, "0");
    const d = String(today.getDate()).padStart(2, "0");
    await saveTodo({
      title,
      status: todayStatus,
      plannedDate: `${y}-${m}-${d}`,
    });
    quickAddTitle.value = "";
    await refresh();
  } finally {
    quickAddLoading.value = false;
  }
}

function openCreate() {
  editId.value = "";
  Object.assign(form, {
    title: "",
    description: "",
    status: inboxStatus,
    priority: 2,
    plannedDate: "",
    deadline: "",
    goalId: "",
    estimatedMinutes: undefined,
    remark: "",
  });
  showModal.value = true;
}

function openEdit(todo: TodoVO) {
  editId.value = todo.id;
  Object.assign(form, {
    title: todo.title,
    description: todo.description || "",
    status: todo.status,
    priority: todo.priority || 2,
    plannedDate: todo.plannedDate || "",
    deadline: todo.deadline ? todo.deadline.replace(" ", "T") : "",
    goalId: todo.goalId || "",
    estimatedMinutes: todo.estimatedMinutes,
    remark: todo.remark || "",
  });
  showModal.value = true;
}

function closeModal() {
  showModal.value = false;
}

async function handleSave() {
  if (!form.title.trim()) return;
  saving.value = true;
  try {
    const data: TodoSave = { ...form };
    if (!data.deadline) delete data.deadline;
    if (!data.plannedDate) delete data.plannedDate;
    if (!data.goalId) delete data.goalId;
    if (editId.value) data.id = editId.value;
    await saveTodo(data);
    closeModal();
    await refresh();
  } finally {
    saving.value = false;
  }
}

async function handleComplete(todo: TodoVO) {
  if (!todo.id) return;
  await completeTodo(todo.id);
  await refresh();
}

async function handleUncomplete(todo: TodoVO) {
  if (!todo.id) return;
  await uncompleteTodo(todo.id);
  await refresh();
}

async function handleMoveToToday(todo: TodoVO) {
  if (!todo.id) return;
  await moveToToday(todo.id);
  await refresh();
}

async function handleMoveToInbox(todo: TodoVO) {
  if (!todo.id) return;
  await moveToInbox(todo.id);
  await refresh();
}

async function handleConvertToTask(todo: TodoVO) {
  if (!todo.id) return;
  if (!confirm(`将「${todo.title || "-"}」转为正式任务，进入任务中心执行？`)) return;
  await convertTodoToTask(todo.id);
  await refresh();
}

async function confirmDelete(todo: TodoVO) {
  if (!todo.id) return;
  if (!confirm(`删除后，「${todo.title || "-"}」将不再展示。确定删除？`)) return;
  await deleteTodo(todo.id);
  await refresh();
}

onMounted(() => {
  refresh();
  loadGoals();
  document.addEventListener("click", onDocClick);
});

onBeforeUnmount(() => {
  document.removeEventListener("click", onDocClick);
});
</script>

<style scoped>
.inbox-page {
  max-width: 1100px;
  margin: 0 auto;
}

/* Header */
.inbox-header {
  padding: 28px;
  margin-bottom: 20px;
  border-radius: var(--z-radius-xl);
  background:
    radial-gradient(circle at 10% 0%, rgba(0, 113, 227, 0.14), transparent 40%),
    radial-gradient(circle at 90% 100%, rgba(120, 180, 255, 0.08), transparent 50%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.98), rgba(247, 250, 255, 0.95));
  border: 1px solid rgba(0, 113, 227, 0.10);
  box-shadow:
    0 0 0 0.5px rgba(255,255,255,0.6) inset,
    0 18px 45px rgba(15, 35, 80, 0.07);
  position: relative;
  overflow: hidden;
}
.inbox-header::before {
  content: "";
  position: absolute;
  top: -30px; right: -30px;
  width: 140px; height: 140px;
  background: radial-gradient(circle, rgba(0,113,227,0.08) 0%, transparent 70%);
  pointer-events: none;
}

.header-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 18px;
}

.page-title {
  font-size: 30px;
  font-weight: 800;
  color: var(--z-text-primary);
  margin-bottom: 6px;
  letter-spacing: -0.8px;
  line-height: 1.2;
}

.page-subtitle {
  font-size: 14px;
  color: var(--z-text-secondary);
  font-weight: 450;
}

.btn-primary {
  display: inline-flex; align-items: center; gap: 6px;
  height: 40px; padding: 0 20px; border-radius: 12px;
  background: linear-gradient(135deg, #0071e3 0%, #0051d5 100%);
  color: white; border: none;
  font-size: 14px; font-weight: 600; cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4,0,0.2,1); font-family: inherit;
  flex-shrink: 0;
  box-shadow: 0 4px 14px rgba(0,113,227,0.35), 0 0 0 0.5px rgba(255,255,255,0.2) inset;
}
.btn-primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(0,113,227,0.45), 0 0 0 0.5px rgba(255,255,255,0.2) inset;
  background: linear-gradient(135deg, #0077ed 0%, #005dd6 100%);
}
.btn-primary:active { transform: translateY(0); }
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; transform: none; }
.btn-primary svg { width: 15px; height: 15px; }

.btn-secondary {
  display: inline-flex; align-items: center; gap: 6px;
  height: 36px; padding: 0 16px; border-radius: 10px;
  background: rgba(0,113,227,0.06); color: var(--z-accent); border: 1px solid rgba(0,113,227,0.12);
  font-size: 13px; font-weight: 600; cursor: pointer;
  transition: all 0.2s ease; font-family: inherit;
}
.btn-secondary:hover {
  background: rgba(0,113,227,0.10);
  border-color: rgba(0,113,227,0.18);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0,113,227,0.10);
}
.btn-secondary svg { width: 14px; height: 14px; }

/* Quick add */
.quick-add {
  display: flex;
  align-items: center;
  gap: 8px;
  background: rgba(255,255,255,0.72);
  border-radius: 14px;
  border: 1px solid rgba(0, 113, 227, 0.10);
  padding: 5px 5px 5px 18px;
  transition: all 0.25s cubic-bezier(0.4,0,0.2,1);
  backdrop-filter: blur(8px);
}
.quick-add:focus-within {
  background: rgba(255,255,255,0.95);
  border-color: rgba(0,113,227,0.25);
  box-shadow: 0 12px 32px rgba(0, 113, 227, 0.12), 0 0 0 3px rgba(0,113,227,0.06);
}
.quick-input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: 14.5px;
  color: var(--z-text-primary);
  font-family: inherit;
  height: 40px;
}
.quick-input::placeholder { color: #9aa4b2; }
.quick-actions { display: flex; gap: 3px; flex-shrink: 0; }
.quick-btn {
  width: 36px; height: 36px; border-radius: 10px; border: none;
  background: transparent; cursor: pointer; display: flex;
  align-items: center; justify-content: center;
  color: var(--z-text-tertiary); transition: all 0.2s cubic-bezier(0.4,0,0.2,1);
}
.quick-btn:hover {
  background: rgba(0,113,227,0.08);
  color: var(--z-accent);
  transform: scale(1.05);
}
.quick-btn:active { transform: scale(0.96); }
.quick-btn:disabled { opacity: 0.4; cursor: not-allowed; transform: none; }
.quick-btn svg { width: 16px; height: 16px; }

/* Body layout */
.inbox-body {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}
.inbox-main { flex: 1; min-width: 0; }

/* Toolbar */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  gap: 16px;
  padding: 5px;
  border-radius: 14px;
  background: rgba(255,255,255,0.55);
  border: 1px solid rgba(0,0,0,0.04);
  box-shadow: 0 10px 28px rgba(15, 35, 80, 0.04);
  backdrop-filter: blur(16px) saturate(140%);
}
.filter-tabs {
  display: flex;
  gap: 4px;
  overflow-x: auto;
  padding: 2px;
}
.filter-tab {
  min-height: 32px; padding: 6px 14px; border: none;
  background: transparent; color: var(--z-text-secondary);
  font-size: 13px; font-weight: 500; cursor: pointer;
  border-radius: 10px; transition: all 0.2s cubic-bezier(0.4,0,0.2,1);
  white-space: nowrap; display: flex; align-items: center; gap: 6px;
  font-family: inherit;
  position: relative;
}
.filter-tab:hover { color: var(--z-text-primary); background: rgba(0,0,0,0.03); }
.filter-tab.active {
  background: rgba(255,255,255,0.85);
  color: var(--z-text-primary);
  font-weight: 600;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06), 0 0 0 0.5px rgba(0,0,0,0.04);
}
.tab-count {
  font-size: 11px; font-weight: 700;
  background: rgba(0,0,0,0.05); padding: 1px 7px;
  border-radius: 999px; min-width: 20px; text-align: center;
  color: var(--z-text-tertiary);
  transition: all 0.2s ease;
}
.filter-tab.active .tab-count { background: var(--z-accent-light); color: var(--z-accent); }

.search-box-inline {
  width: 200px; flex-shrink: 0;
  display: flex; align-items: center; gap: 6px;
  padding: 6px 10px; border-radius: 10px;
  background: rgba(247,248,250,0.85); border: 1px solid transparent;
  transition: all 0.2s cubic-bezier(0.4,0,0.2,1);
}
.search-box-inline:focus-within {
  background: rgba(255,255,255,0.95);
  border-color: rgba(0,113,227,0.18);
  box-shadow: 0 4px 12px rgba(0,113,227,0.08);
}
.search-box-inline svg { width: 14px; height: 14px; color: var(--z-text-tertiary); flex-shrink: 0; }
.search-box-inline input { border: none; outline: none; background: transparent; font-size: 13px; color: var(--z-text-primary); width: 100%; font-family: inherit; }
.search-box-inline input::placeholder { color: #a1a1a6; }
.search-clear {
  width: 18px; height: 18px; border-radius: 50%; border: none;
  background: rgba(0,0,0,0.05); cursor: pointer; display: flex;
  align-items: center; justify-content: center; color: var(--z-text-tertiary);
  flex-shrink: 0; padding: 0; transition: all 0.15s ease;
}
.search-clear:hover { background: rgba(0,0,0,0.09); color: var(--z-text-secondary); }
.search-clear svg { width: 10px; height: 10px; }

/* Todo list card */
.todo-list-card {
  background: rgba(255,255,255,0.92);
  border-radius: var(--z-radius-xl);
  box-shadow:
    0 0 0 0.5px rgba(255,255,255,0.6) inset,
    0 18px 45px rgba(15, 35, 80, 0.07),
    0 1px 2px rgba(15, 35, 80, 0.04);
  border: 1px solid rgba(15, 35, 80, 0.05);
  overflow: hidden;
  min-height: 180px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 56px 20px;
  color: var(--z-text-tertiary);
}
.empty-illustration {
  width: 120px; height: 120px;
  margin-bottom: 8px;
  display: flex; align-items: center; justify-content: center;
}
.empty-illustration svg { width: 100%; height: 100%; }
.empty-state h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--z-text-secondary);
  margin: 8px 0 4px;
}
.empty-state p {
  font-size: 13px;
  color: var(--z-text-tertiary);
  margin-bottom: 20px;
  max-width: 280px;
  text-align: center;
  line-height: 1.5;
}
.loading { text-align: center; padding: 48px; color: var(--z-text-tertiary); font-size: 14px; }

.todo-list { display: flex; flex-direction: column; }
.todo-item {
  display: flex; align-items: flex-start;
  padding: 16px 22px;
  border-bottom: 1px solid rgba(15, 35, 80, 0.05);
  transition: all 0.22s cubic-bezier(0.4,0,0.2,1);
  gap: 14px; position: relative;
}
.todo-item:last-child { border-bottom: none; }
.todo-item:hover {
  z-index: 1;
  background-color: #fbfdff;
  transform: translateY(-1px);
  box-shadow: 0 12px 32px rgba(15, 35, 80, 0.07);
  border-bottom-color: transparent;
}
.todo-item:hover + .todo-item { border-top: 1px solid transparent; }
.todo-item:hover .todo-actions { opacity: 1; transform: translateX(0); }

.todo-done { opacity: 0.55; background: linear-gradient(90deg, rgba(52, 199, 89, 0.03), rgba(255,255,255,0.88)); }
.todo-done:hover { opacity: 0.7; background: linear-gradient(90deg, rgba(52, 199, 89, 0.05), #fff); }
.todo-high { border-left: 3px solid var(--z-danger); padding-left: 19px; background: linear-gradient(90deg, rgba(255, 59, 48, 0.03), rgba(255,255,255,0.96)); }

.todo-checkbox {
  width: 22px; height: 22px; border-radius: 50%; flex-shrink: 0;
  border: 2px solid rgba(0,0,0,0.13); background: none;
  cursor: pointer; display: flex; align-items: center; justify-content: center;
  transition: all 0.2s cubic-bezier(0.4,0,0.2,1); margin-top: 1px; padding: 0;
}
.todo-checkbox:hover { border-color: var(--z-success); background: rgba(52,199,89,0.06); transform: scale(1.08); }
.todo-checkbox:active { transform: scale(0.95); }
.todo-checkbox.checked { border-color: var(--z-success); background: var(--z-success); }
.todo-checkbox.checked svg { width: 12px; height: 12px; color: white; }

.todo-content { flex: 1; min-width: 0; cursor: pointer; }
.todo-title { font-size: 14px; font-weight: 500; color: var(--z-text-primary); line-height: 24px; word-break: break-word; }
.todo-title.title-done { text-decoration: line-through; color: var(--z-text-tertiary); font-weight: 400; }
.todo-desc { font-size: 12px; color: var(--z-text-tertiary); margin-top: 2px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

.todo-tags { display: flex; flex-wrap: wrap; gap: 6px; margin-top: 6px; }
.tag {
  font-size: 11px; padding: 2px 9px; border-radius: 5px;
  display: inline-flex; align-items: center; gap: 4px; line-height: 18px;
  font-weight: 500;
}
.tag-red { background: rgba(255, 59, 48, 0.08); color: #d9363e; }
.tag-blue { background: rgba(0, 113, 227, 0.08); color: #096dd9; }
.tag-date { background: rgba(0, 113, 227, 0.06); color: #1d39c4; }
.tag-date svg { width: 10px; height: 10px; }
.tag-goal { background: rgba(52, 199, 89, 0.08); color: #389e0d; }
.tag-goal svg { width: 10px; height: 10px; }

.todo-actions {
  display: flex; align-items: center; gap: 2px;
  flex-shrink: 0; opacity: 0;
  transform: translateX(4px);
  transition: all 0.22s cubic-bezier(0.4,0,0.2,1);
}
.action-btn {
  width: 30px; height: 30px; border-radius: 8px; border: none;
  background: transparent; cursor: pointer; display: flex;
  align-items: center; justify-content: center;
  color: var(--z-text-tertiary); transition: all 0.15s ease;
  flex-shrink: 0; padding: 0;
}
.action-btn:hover { background: rgba(0,0,0,0.04); color: var(--z-accent); transform: scale(1.08); }
.action-btn:active { transform: scale(0.95); }
.action-btn svg { width: 14px; height: 14px; }

/* Dropdown */
.dropdown { position: relative; }
.dropdown-menu {
  position: absolute; right: 0; top: calc(100% + 4px);
  min-width: 160px; z-index: 100;
  background: rgba(255,255,255,0.96);
  border-radius: 12px; padding: 6px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.12), 0 0 0 0.5px rgba(0,0,0,0.06);
  backdrop-filter: blur(20px);
  display: flex; flex-direction: column; gap: 1px;
}
.dropdown-item {
  display: flex; align-items: center; gap: 8px;
  padding: 8px 10px; border-radius: 8px; border: none;
  background: transparent; color: var(--z-text-primary);
  font-size: 13px; cursor: pointer; transition: all 0.15s ease;
  font-family: inherit; text-align: left;
}
.dropdown-item:hover { background: rgba(0,0,0,0.04); }
.dropdown-item.danger { color: var(--z-danger); }
.dropdown-item.danger:hover { background: rgba(255,59,48,0.08); }
.dropdown-item svg { width: 13px; height: 13px; flex-shrink: 0; }
.dropdown-divider { height: 1px; background: rgba(0,0,0,0.06); margin: 4px 0; }

/* Pagination */
.pagination { display: flex; align-items: center; justify-content: center; gap: 12px; padding: 14px 20px; border-top: 1px solid rgba(0,0,0,0.04); }
.pagination button {
  padding: 7px 16px; border-radius: 8px; border: 1px solid rgba(0,0,0,0.07);
  background: rgba(255,255,255,0.8); cursor: pointer; font-size: 13px; font-weight: 500;
  transition: all 0.2s ease; font-family: inherit; color: var(--z-text-secondary);
}
.pagination button:hover:not(:disabled) { background: rgba(0,0,0,0.04); color: var(--z-text-primary); transform: translateY(-1px); }
.pagination button:active:not(:disabled) { transform: translateY(0); }
.pagination button:disabled { opacity: 0.35; cursor: not-allowed; }
.pagination span { font-size: 13px; font-weight: 600; color: var(--z-text-secondary); background: rgba(0,0,0,0.03); padding: 4px 14px; border-radius: 8px; }

/* Sidebar */
.inbox-sidebar {
  width: 260px; flex-shrink: 0;
  display: flex; flex-direction: column; gap: 16px;
  position: sticky; top: 0;
}
.sidebar-card {
  background: rgba(255,255,255,0.88);
  border-radius: var(--z-radius-xl);
  padding: 20px;
  box-shadow:
    0 0 0 0.5px rgba(255,255,255,0.6) inset,
    0 16px 36px rgba(15, 35, 80, 0.06),
    0 1px 2px rgba(15, 35, 80, 0.04);
  border: 1px solid rgba(15, 35, 80, 0.05);
  backdrop-filter: blur(8px);
}
.sidebar-tip {
  background:
    linear-gradient(135deg, rgba(250,252,255,0.95) 0%, rgba(238,245,255,0.92) 100%);
  border: 1px solid rgba(0, 113, 227, 0.08);
}
.sidebar-title {
  font-size: 12px; font-weight: 700;
  color: rgba(0,0,0,0.55); margin: 0 0 16px 0;
  letter-spacing: 0.4px; display: flex; align-items: center; gap: 6px;
  text-transform: uppercase;
}
.sidebar-title svg { width: 14px; height: 14px; }

.stat-list {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}
.stat-item {
  display: flex; flex-direction: column;
  padding: 12px 10px;
  border-radius: 12px;
  background: rgba(247,249,252,0.85);
  transition: all 0.2s ease;
}
.stat-item:hover { background: rgba(240,245,252,0.95); transform: translateY(-1px); }
.stat-label { font-size: 11px; font-weight: 500; color: var(--z-text-tertiary); margin-bottom: 4px; }
.stat-value { font-size: 22px; font-weight: 700; color: var(--z-text-primary); font-variant-numeric: tabular-nums; line-height: 1.2; }
.stat-blue { color: var(--z-accent); }
.stat-green { color: var(--z-success); }

.sidebar-actions { display: flex; flex-direction: column; gap: 8px; }
.sidebar-btn {
  display: flex; align-items: center; gap: 10px;
  padding: 10px 12px; border-radius: 10px; border: 1px solid rgba(15,35,80,0.06);
  background: rgba(255,255,255,0.60); color: var(--z-text-secondary);
  font-size: 13px; font-weight: 500; cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4,0,0.2,1); font-family: inherit; text-align: left;
}
.sidebar-btn:hover {
  color: var(--z-accent); border-color: rgba(0,113,227,0.15);
  background: rgba(0,113,227,0.04); transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0,113,227,0.06);
}
.sidebar-btn svg { width: 15px; height: 15px; flex-shrink: 0; }

.tip-list { margin: 0; padding: 0; list-style: none; }
.tip-list li {
  font-size: 12px; color: var(--z-text-tertiary); line-height: 1.7;
  padding-left: 14px; position: relative;
}
.tip-list li::before {
  content: "";
  position: absolute;
  left: 0; top: 8px;
  width: 5px; height: 5px;
  border-radius: 50%;
  background: rgba(0,113,227,0.25);
}

/* Modal */
.modal-overlay {
  position: fixed; inset: 0; z-index: 1000;
  display: flex; align-items: center; justify-content: center;
  background: rgba(0,0,0,0.22); backdrop-filter: blur(10px);
  animation: fadeIn 0.2s ease;
}
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
.modal-card {
  width: 520px; max-height: 85vh; overflow-y: auto;
  padding: 28px 32px; border-radius: var(--z-radius-xl);
  background:
    linear-gradient(160deg, rgba(255,255,255,0.45) 0%, rgba(255,255,255,0.20) 50%, rgba(255,255,255,0.35) 100%);
  backdrop-filter: blur(80px) saturate(220%);
  box-shadow:
    0 0 0 0.5px rgba(255,255,255,0.6) inset,
    0 0 0 0.5px rgba(0,0,0,0.06),
    0 24px 60px rgba(0,0,0,0.18);
  animation: slideUp 0.3s cubic-bezier(0.4,0,0.2,1);
}
@keyframes slideUp {
  from { opacity: 0; transform: translateY(20px) scale(0.98); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}
.modal-title { font-size: 20px; font-weight: 700; margin-bottom: 24px; color: var(--z-text-primary); letter-spacing: -0.3px; }

.modal-form { display: flex; flex-direction: column; gap: 16px; }
.form-group { display: flex; flex-direction: column; gap: 6px; flex: 1; }
.form-group label { font-size: 11px; font-weight: 700; color: #8a8f99; text-transform: uppercase; letter-spacing: 0.5px; }
.form-group label span { color: var(--z-danger); }
.form-group input, .form-group select, .form-group textarea {
  height: 40px; padding: 0 14px; border-radius: 10px;
  border: 1px solid rgba(0,0,0,0.07); background: rgba(255,255,255,0.55);
  font-size: 14px; color: var(--z-text-primary); outline: none;
  transition: all 0.2s ease; font-family: inherit;
}
.form-group textarea { height: auto; padding: 10px 14px; resize: vertical; min-height: 72px; }
.form-group input:focus, .form-group select:focus, .form-group textarea:focus {
  border-color: var(--z-accent);
  background: rgba(255,255,255,0.75);
  box-shadow: 0 0 0 3px rgba(0,113,227,0.08), 0 4px 12px rgba(0,113,227,0.06);
}
.form-row { display: flex; gap: 14px; }

.modal-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 8px; padding-top: 4px; }
.btn-cancel {
  height: 40px; padding: 0 20px; border-radius: 10px;
  background: rgba(0,0,0,0.03); color: var(--z-text-secondary);
  border: 1px solid rgba(0,0,0,0.08); font-size: 14px; font-weight: 500;
  cursor: pointer; transition: all 0.2s ease; font-family: inherit;
}
.btn-cancel:hover { background: rgba(0,0,0,0.06); transform: translateY(-1px); }
.btn-cancel:active { transform: translateY(0); }
</style>
