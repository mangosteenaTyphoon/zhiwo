import { createRouter, createWebHistory } from "vue-router";
import type { RouteRecordRaw } from "vue-router";
import { useAppStore } from "@/stores/app";

const routes: RouteRecordRaw[] = [
  {
    path: "/login",
    name: "Login",
    component: () => import("@/views/login/LoginView.vue"),
    meta: { public: true },
  },
  {
    path: "/",
    redirect: "/today",
  },
  {
    path: "/today",
    name: "Today",
    component: () => import("@/views/today/TodayView.vue"),
    meta: { title: "今日", icon: "Today" },
  },
  {
    path: "/goals",
    name: "Goals",
    component: () => import("@/views/goals/GoalsView.vue"),
    meta: { title: "目标", icon: "Goals" },
  },
  {
    path: "/goals/:id",
    name: "GoalDetail",
    component: () => import("@/views/goals/GoalDetailView.vue"),
    meta: { title: "目标详情", hidden: true },
  },
  {
    path: "/tasks",
    name: "Tasks",
    component: () => import("@/views/tasks/TasksView.vue"),
    meta: { title: "任务", icon: "Tasks" },
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, _from, next) => {
  const appStore = useAppStore();
  if (!to.meta.public && !appStore.token) {
    next("/login");
  } else if (to.meta.public && appStore.token) {
    next("/");
  } else {
    next();
  }
});

export default router;