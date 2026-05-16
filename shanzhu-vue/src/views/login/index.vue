<template>
  <a-flex class="login-background" justify="center" align="center">
    <div class="login-grid" v-if="!showSetting">
      <!--      主题切换开关-->
      <head-theme-switch class="head-theme-switch"/>
      <!--        左侧数据化说明-->
      <section class="title">
        <transition name="fade" mode="out-in">
          <div v-show="showTitle" class="hero-panel">
            <a-tag class="system-tag" :bordered="false">Self Data OS · {{ settings.version }}</a-tag>
            <a-typography-title class="hero-title">
              把每一天，沉淀成可复盘的自我数据
            </a-typography-title>
            <a-typography-paragraph class="hero-description">
              用统一的数据视角管理目标、习惯、计划与成长轨迹，让个人状态被看见、被分析、被持续优化。
            </a-typography-paragraph>

            <div class="dashboard-preview">
              <div class="dashboard-header">
                <div>
                  <span class="dashboard-label">今日自我画像</span>
                  <strong>Personal cockpit</strong>
                </div>
                <span class="dashboard-status">实时同步</span>
              </div>
              <div class="dashboard-chart">
                <span class="chart-bar chart-bar-short"></span>
                <span class="chart-bar chart-bar-middle"></span>
                <span class="chart-bar chart-bar-high"></span>
                <span class="chart-bar chart-bar-active"></span>
                <span class="chart-bar chart-bar-middle"></span>
                <span class="chart-bar chart-bar-high"></span>
              </div>
              <div class="dashboard-stats">
                <div v-for="item in dashboardStats" :key="item.label" class="stat-card">
                  <span>{{ item.label }}</span>
                  <strong>{{ item.value }}</strong>
                </div>
              </div>
            </div>

            <div class="insight-list">
              <div v-for="item in insightCards" :key="item.title" class="insight-card">
                <span class="insight-dot"></span>
                <div>
                  <strong>{{ item.title }}</strong>
                  <p>{{ item.description }}</p>
                </div>
              </div>
            </div>
          </div>
        </transition>
      </section>
      <!--      右侧表单-->
      <section class="form">
        <transition name="card" mode="out-in" v-show="showCard">
          <a-card class="login-card">
            <transition name="form" mode="out-in" v-show="showCard">
              <!-- 用户登录/注册等卡片内表单在这儿通过组件形式切换 -->
              <component :is="activeComponent" @change-component="handleChangeComponent" @show-login-setting="startLoginSetting"/>
            </transition>
          </a-card>
        </transition>
      </section>
    </div>
    <!--    登录设置-->
    <transition name="setting" mode="out-in">
      <login-setting :component-names="settingComponentNames"
                     v-if="showSetting"
                     @go-login="handleGoLogin"
      ></login-setting>
    </transition>
  </a-flex>

</template>

<script setup lang="ts">
import {markRaw, onMounted, provide, ref} from "vue"
import HeadThemeSwitch from "@/components/light-dark-switch/index.vue"
import LoginSetting from "@/components/login-setting/index.vue"
import UserRegister from "@/views/login/components/Register.vue"
import UserLogin from "@/views/login/components/Login.vue"
import settings from "@/settings"
import {screenUnlock} from "@/utils/LockScreenUtils.ts";

// 显示登录卡片
const showCard = ref<boolean>(false)
// 显示左侧title
const showTitle = ref<boolean>(false)

// 注册的用户数据，定义registerUsername后，注册组件通过inject接收值，并在注册成功后赋值为用户名，登录组件可获取后进行处理
provide("registerUsername",ref<string>())

const dashboardStats = [
  {
    label: "专注指数",
    value: "86%"
  },
  {
    label: "目标推进",
    value: "12 项"
  },
  {
    label: "习惯连续",
    value: "21 天"
  }
]

const insightCards = [
  {
    title: "目标仪表盘",
    description: "统一追踪长期目标、阶段任务和关键结果。"
  },
  {
    title: "习惯数据流",
    description: "沉淀打卡、复盘、精力与效率趋势。"
  },
  {
    title: "成长分析",
    description: "把碎片记录转化为可执行的自我洞察。"
  }
]

// 初始化组件切换相关逻辑
const initChangeComponent = () => {
  // 当前显示的组件
  const activeComponent = ref()
  // 全部组件
  const allComponents = [
    {
      name: "login",
      com: markRaw(UserLogin)
    },
    {
      name: "register",
      com: markRaw(UserRegister)
    },
  ]
  // 处理切换组件
  const handleChangeComponent = (name: string) => {
    const target = allComponents.filter(component => component.name === name)
    if (!target || target.length === 0) {
      console.error("组件name未注册")
      return
    }

    activeComponent.value = target[0].com
    handleShowCard()
  }

  return {
    activeComponent,
    handleChangeComponent
  }
}
const {activeComponent, handleChangeComponent} = initChangeComponent()

const initLoginSetting = () => {
  // 是否显示setting组件
  const showSetting = ref<boolean>(false)
  // setting组件名
  const settingComponentNames = ref<string[]>([])
  // 登录后配置
  const startLoginSetting = (settingComponentNameList: string[]) => {
    if (settingComponentNameList && settingComponentNameList.length > 0) {
      showTitle.value = false
      showCard.value = false
      showSetting.value = true
      settingComponentNames.value = settingComponentNameList
    }
  }
  // 当需要登录后配置时，刷新页面读取路由携带参数，加载配置页面
  const routerCheckLoginSetting = () => {
    startLoginSetting(history.state.settingComponentNameList)
  }
  // 从配置页面退回到登录页面
  const handleGoLogin = async () => {
    // 关闭设置页面
    showSetting.value = false
    showCard.value = false
    settingComponentNames.value = []
    // 清空路由参数
    if (history.state.settingComponentNameList) {
      history.state.settingComponentNameList = undefined
    }
    setTimeout(() => {
      // 登录卡片弹出动画
      handleShowCard()
    }, 200)
  }

  return {
    showSetting,
    settingComponentNames,
    startLoginSetting,
    routerCheckLoginSetting,
    handleGoLogin
  }
}
const {showSetting, settingComponentNames, startLoginSetting, routerCheckLoginSetting, handleGoLogin} = initLoginSetting()

// 显示卡片
const handleShowCard = () => {
  showCard.value = false
  showTitle.value = true
  setTimeout(() => showCard.value = true, 100)
}

onMounted(() => {
  // 默认显示login
  handleChangeComponent("login")
  // 检查history.state中是否存在登录后配置
  routerCheckLoginSetting()
  // 进入登录页的用户关闭锁屏
  screenUnlock()
})
</script>
<style scoped>
/* 登录背景 */
.login-background {
  position: relative;
  width: 100%;
  min-height: 100vh;
  padding: clamp(32px, 6vh, 72px) clamp(16px, 4vw, 64px);
  overflow-x: hidden;
  overflow-y: auto;
  background:
      radial-gradient(circle at 12% 18%, rgba(56, 189, 248, 0.32), transparent 28%),
      radial-gradient(circle at 88% 12%, rgba(129, 140, 248, 0.32), transparent 30%),
      linear-gradient(135deg, #eef7ff 0%, #f6fbff 42%, #eaf2ff 100%);
}

.login-background::before,
.login-background::after {
  position: absolute;
  content: "";
  border-radius: 999px;
  pointer-events: none;
}

.login-background::before {
  width: 420px;
  height: 420px;
  left: -160px;
  bottom: -120px;
  background: rgba(22, 119, 255, 0.16);
  filter: blur(12px);
}

.login-background::after {
  width: 560px;
  height: 560px;
  right: -220px;
  top: -180px;
  background: rgba(14, 165, 233, 0.14);
  filter: blur(16px);
}

.login-grid {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: minmax(360px, 1fr) minmax(360px, 420px);
  gap: clamp(32px, 5vw, 88px);
  width: min(1280px, 100%);
  align-items: center;
}

.hero-panel {
  color: #10233f;
}

.system-tag {
  margin-bottom: var(--lihua-space-base);
  padding: 6px var(--lihua-space-base);
  color: #1d4ed8;
  background: rgba(255, 255, 255, 0.72);
  border-radius: 999px;
  box-shadow: 0 10px 30px rgba(37, 99, 235, 0.12);
}

.hero-title {
  max-width: 620px;
  margin-bottom: var(--lihua-space-base) !important;
  font-size: clamp(38px, 4vw, 60px) !important;
  line-height: 1.12 !important;
  letter-spacing: -2px;
}

.hero-description {
  max-width: 560px;
  margin-bottom: var(--lihua-space-xl) !important;
  color: rgba(16, 35, 63, 0.72);
  font-size: var(--lihua-font-size-lg);
}

.dashboard-preview {
  width: 100%;
  max-width: 560px;
  padding: var(--lihua-space-lg);
  margin-bottom: var(--lihua-space-base);
  overflow: hidden;
  background: rgba(255, 255, 255, 0.66);
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: var(--lihua-radius-lg);
  box-shadow: 0 24px 80px rgba(37, 99, 235, 0.16);
  backdrop-filter: blur(18px);
}

.dashboard-header,
.dashboard-stats,
.insight-list {
  display: flex;
}

.dashboard-header {
  justify-content: space-between;
  gap: var(--lihua-space-base);
  margin-bottom: var(--lihua-space-lg);
}

.dashboard-header strong {
  display: block;
  margin-top: var(--lihua-space-xxs);
  color: #0f172a;
  font-size: var(--lihua-font-size-xl);
}

.dashboard-label,
.dashboard-status,
.stat-card span,
.insight-card p {
  color: rgba(15, 23, 42, 0.58);
  font-size: var(--lihua-font-size-sm);
}

.dashboard-status {
  height: fit-content;
  padding: var(--lihua-space-xxs) var(--lihua-space-sm);
  color: #0f766e;
  background: rgba(20, 184, 166, 0.12);
  border-radius: 999px;
}

.dashboard-chart {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: var(--lihua-space-sm);
  align-items: end;
  height: 126px;
  padding: var(--lihua-space-base);
  margin-bottom: var(--lihua-space-base);
  background:
      linear-gradient(rgba(37, 99, 235, 0.08) 1px, transparent 1px),
      linear-gradient(90deg, rgba(37, 99, 235, 0.08) 1px, transparent 1px);
  background-size: 100% 33.3%, 20% 100%;
  border-radius: var(--lihua-radius-base);
}

.chart-bar {
  display: block;
  min-height: 36px;
  background: linear-gradient(180deg, #60a5fa 0%, #2563eb 100%);
  border-radius: 999px 999px var(--lihua-radius-xs) var(--lihua-radius-xs);
  box-shadow: 0 12px 24px rgba(37, 99, 235, 0.2);
}

.chart-bar-short {
  height: 38%;
}

.chart-bar-middle {
  height: 58%;
}

.chart-bar-high {
  height: 78%;
}

.chart-bar-active {
  height: 96%;
  background: linear-gradient(180deg, #22d3ee 0%, #2563eb 100%);
}

.dashboard-stats {
  gap: var(--lihua-space-sm);
}

.stat-card {
  flex: 1;
  padding: var(--lihua-space-sm);
  background: rgba(255, 255, 255, 0.62);
  border-radius: var(--lihua-radius-sm);
}

.stat-card strong {
  display: block;
  margin-top: var(--lihua-space-xxs);
  color: #0f172a;
  font-size: var(--lihua-font-size-lg);
}

.insight-list {
  width: 100%;
  max-width: 560px;
  gap: var(--lihua-space-sm);
}

.insight-card {
  flex: 1;
  min-height: 126px;
  padding: var(--lihua-space-base);
  background: rgba(255, 255, 255, 0.46);
  border: 1px solid rgba(255, 255, 255, 0.62);
  border-radius: var(--lihua-radius-base);
  backdrop-filter: blur(16px);
}

.insight-dot {
  display: block;
  width: 10px;
  height: 10px;
  margin-bottom: var(--lihua-space-sm);
  background: #1677ff;
  border-radius: 999px;
  box-shadow: 0 0 0 6px rgba(22, 119, 255, 0.12);
}

.insight-card strong {
  color: #0f172a;
}

.insight-card p {
  margin: var(--lihua-space-xs) 0 0;
  line-height: 1.6;
}

/* 登录卡片 */
.login-card {
  width: 100%;
  padding: var(--lihua-space-base) var(--lihua-space-base) var(--lihua-space-sm);
  overflow: hidden;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 32px;
  box-shadow: 0 30px 90px rgba(15, 23, 42, 0.18);
  backdrop-filter: blur(18px);
}

/* 表单 */
.form {
  width: min(420px, 100%);
}

.form :deep(.ant-card-body) {
  padding: var(--lihua-space-xl);
}

@media screen and (min-width: 1600px) {
  .login-grid {
    width: min(1360px, 86vw);
  }
}

@media screen and (max-width: 1280px) {
  .login-grid {
    grid-template-columns: minmax(320px, 560px) minmax(360px, 400px);
    gap: clamp(28px, 4vw, 56px);
  }

  .hero-title {
    max-width: 540px;
  }

  .dashboard-chart {
    height: 112px;
  }

  .insight-card {
    min-height: 112px;
  }
}

@media screen and (max-width: 1024px) {
  .login-grid {
    grid-template-columns: minmax(360px, 420px);
    justify-content: center;
    width: min(420px, 100%);
  }

  .title {
    display: none;
  }

  .head-theme-switch {
    right: 0;
  }
}

@media screen and (max-width: 520px) {
  .login-background {
    align-items: flex-start;
    padding: 72px 12px 24px;
  }

  .login-grid,
  .form {
    width: 100%;
  }

  .form :deep(.ant-card-body) {
    padding: var(--lihua-space-lg);
  }

  .login-card {
    border-radius: var(--lihua-radius-lg);
  }

  .head-theme-switch {
    top: var(--lihua-space-base);
    right: var(--lihua-space-base);
  }
}

@media screen and (max-width: 380px) {
  .form :deep(.ant-card-body) {
    padding: var(--lihua-space-base);
  }

  .login-card {
    padding: var(--lihua-space-sm);
  }
}

@media screen and (max-height: 720px) and (min-width: 1025px) {
  .login-background {
    align-items: flex-start;
  }

  .login-grid {
    margin: auto 0;
  }

  .dashboard-chart {
    height: 96px;
  }

  .insight-card {
    min-height: 104px;
  }
}

/* 暗色模式切换开关 */
.head-theme-switch {
  position: absolute;
  top: var(--lihua-space-base);
  right: var(--lihua-space-lg);
}

.card-enter-active {
  transition: all 0.8s ease-in-out;
}

.card-enter-from {
  transform: translateY(80px) scale(88%);
  opacity: 0;
}

.form-enter-active {
  transition: all 0.6s ease-in-out;
}

.form-enter-from {
  transform: translateY(24px);
  opacity: 0;
}

/* 公共优化 */
.setting-enter-active,
.setting-leave-active {
  will-change: transform, opacity;
  transform: translate3d(0, 0, 0);
}

/* 登录后设置卡片呼出 */
.setting-enter-active {
  transition:
      transform 0.7s cubic-bezier(0.22, 1, 0.36, 1),
      opacity   0.5s ease-out;
}

.setting-enter-from {
  transform: translate3d(0, 100%, 0);
  opacity: 0;
}

.setting-enter-to {
  transform: translate3d(0, 0, 0);
  opacity: 1;
}

/* 登录后设置卡片隐藏 */
.setting-leave-active {
  transition:
      transform 0.35s cubic-bezier(0.4, 0.0, 0.2, 1),
      opacity   0.25s ease-in;
}

.setting-leave-from {
  transform: translate3d(0, 0, 0);
  opacity: 1;
}

.setting-leave-to {
  transform: translate3d(0, 100%, 0);
  opacity: 0;
}

</style>

<style>
[data-theme = dark] {
  .login-background {
    background:
        radial-gradient(circle at 12% 18%, rgba(14, 165, 233, 0.18), transparent 28%),
        radial-gradient(circle at 88% 12%, rgba(99, 102, 241, 0.2), transparent 30%),
        linear-gradient(135deg, #020617 0%, #0f172a 46%, #111827 100%);
  }

  .hero-panel,
  .hero-title,
  .dashboard-header strong,
  .stat-card strong,
  .insight-card strong {
    color: rgba(255, 255, 255, 0.92) !important;
  }

  .hero-description,
  .dashboard-label,
  .stat-card span,
  .insight-card p {
    color: rgba(255, 255, 255, 0.58) !important;
  }

  .system-tag,
  .dashboard-preview,
  .insight-card,
  .stat-card,
  .login-card {
    background: rgba(15, 23, 42, 0.68) !important;
    border-color: rgba(148, 163, 184, 0.18) !important;
  }

  .dashboard-chart {
    background:
        linear-gradient(rgba(148, 163, 184, 0.1) 1px, transparent 1px),
        linear-gradient(90deg, rgba(148, 163, 184, 0.1) 1px, transparent 1px) !important;
    background-size: 100% 33.3%, 20% 100% !important;
  }
}
</style>
