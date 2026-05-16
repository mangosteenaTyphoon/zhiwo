# 山竹项目开发规范

本文档用于沉淀当前项目的架构理解、开发约定和后续业务开发落点。后续在本仓库内新增或修改业务功能时，应优先遵循本文档。

## 1. 项目定位

当前项目是一个前后端分离的自我数据化管理系统基础工程，原始工程名为 `shanzhu` / `lihua`，已具备后台管理系统常见能力：

- 用户登录、注册、验证码、JWT 鉴权
- 用户、角色、菜单、部门等系统管理能力
- 动态路由、权限守卫、菜单初始化
- 统一请求、统一响应、统一异常与权限处理
- Redis、MyBatis-Plus、日志、缓存、WebSocket 等基础模块

后续业务围绕“数据化管理自我”展开，当前确定的业务域包括目标管理、习惯追踪、复盘记录、计划执行和个人仪表盘。

## 2. 总体技术架构

### 2.1 后端

后端是 Maven 多模块 Spring Boot 工程，核心结构如下：

- `shanzhu-admin`
  - 应用启动模块
  - 启动入口：`com.shanzhu.ShanzhuApplication`
  - 配置文件：`shanzhu-admin/src/main/resources/application-dev.yml`
- `shanzhu-biz`
  - 业务模块集合
  - `shanzhu-system`：系统管理相关业务
  - `shanzhu-monitor`：监控相关业务
- `shanzhu-base`
  - 基础能力模块集合
  - 包含 common、web、security、mybatis、cache、log、job、captcha、attachment 等能力

关键技术：

- Spring Boot
- MyBatis-Plus
- JWT 鉴权
- Redis + Caffeine 缓存
- 统一响应模型 `ApiResponseModel`
- Controller 基类 `ApiResponseController`

### 2.2 前端

前端位于 `shanzhu-vue`，是 Vue 3 + TypeScript + Vite 工程。

关键目录：

- `shanzhu-vue/src/views`
  - 页面目录
- `shanzhu-vue/src/api`
  - API 请求封装目录
- `shanzhu-vue/src/router/index.ts`
  - 静态路由与动态路由入口
- `shanzhu-vue/src/permission.ts`
  - 全局路由守卫
- `shanzhu-vue/src/utils/Request.ts`
  - 统一请求封装
- `shanzhu-vue/src/utils/AppInit.ts`
  - 登录后应用初始化逻辑

关键技术：

- Vue 3
- TypeScript
- Vite
- Pinia
- Vue Router
- Ant Design Vue
- Axios 封装请求

## 3. 后端开发规范

### 3.1 分层约定

新增业务优先参考 `SysDept` 相关代码的分层方式：

- Controller：接收请求、参数校验、返回统一响应
- Service：业务逻辑编排
- Mapper：数据库访问接口
- Mapper XML：复杂 SQL
- Entity / DTO / VO：按请求、数据库、响应进行对象分层

参考文件：

- `shanzhu-biz/shanzhu-system/src/main/java/com/shanzhu/controller/SysDeptController.java`
- `shanzhu-biz/shanzhu-system/src/main/java/com/shanzhu/service/impl/SysDeptServiceImpl.java`
- `shanzhu-biz/shanzhu-system/src/main/java/com/shanzhu/mapper/SysDeptMapper.java`
- `shanzhu-biz/shanzhu-system/src/main/java/com/shanzhu/mapper/xml/SysDeptMapper.xml`

### 3.2 Controller 返回规范

Controller 应继承或复用现有统一响应能力，返回 `ApiResponseModel`。

优先参考：

- `ApiResponseController`
- `ApiResponseModel`

常见返回语义：

- 查询成功：返回 `success(data)`
- 新增 / 修改 / 删除成功：返回统一成功响应
- 业务失败：返回带业务错误信息的统一失败响应

不要在 Controller 中直接返回裸对象或自定义不一致的响应结构。

### 3.3 接口路径规范

接口路径必须与业务模块保持一致，并按资源命名：

- 系统模块：沿用 `/system/...`
- 监控模块：沿用 `/monitor/...`
- 自我数据化管理业务：统一使用 `/shanzhu/...`

当前仓库没有独立的 `shanzhu-self` 或其他自我管理业务 Maven 模块。新增山竹业务时，后端代码先放在 `shanzhu-biz/shanzhu-system/src/main/java/com/shanzhu` 下的现有分层目录中，Controller 类按 `ShanzhuXxxController` 命名，接口路径按 `/shanzhu/xxx` 命名。只有当山竹业务已经形成独立边界，并同步调整父子 `pom.xml`、启动扫描、依赖关系和部署配置后，才允许拆成独立 Maven 模块。

新增接口前，必须检查已有 Controller 的路径风格，避免重复或风格不一致。

### 3.4 数据库与 Mapper 规范

- 简单 CRUD 优先使用 MyBatis-Plus 能力
- 复杂查询放在 Mapper XML 中
- XML 命名空间、Mapper 接口、方法名必须保持一致
- 查询条件应显式处理空值，避免产生异常 SQL
- 分页能力优先复用项目已有分页模型和写法

### 3.5 配置规范

环境配置优先放在对应环境配置文件中，例如：

- `shanzhu-admin/src/main/resources/application-dev.yml`

不要在业务代码中硬编码数据库地址、Redis 地址、密钥、外部服务地址等环境相关内容。

## 4. 前端开发规范

### 4.1 页面开发位置

新增页面必须放在 `shanzhu-vue/src/views` 下，并按业务域拆目录。

山竹业务页面目录固定为：

- `shanzhu-vue/src/views/shanzhu/dashboard`
- `shanzhu-vue/src/views/shanzhu/goal`
- `shanzhu-vue/src/views/shanzhu/habit`
- `shanzhu-vue/src/views/shanzhu/review`

当前仓库尚不存在 `shanzhu-vue/src/views/shanzhu`。第一次实现山竹业务页面时，需要创建该业务根目录，并保持页面目录、路由组件路径、菜单组件路径一致。

如果页面只在当前业务使用，组件应放在当前页面目录下；只有多个页面复用时，才提升到全局 `src/components`。

### 4.2 API 封装规范

所有接口请求必须通过项目统一请求工具：

- `shanzhu-vue/src/utils/Request.ts`

API 文件参考：

- `shanzhu-vue/src/api/system/dept/Dept.ts`
- `shanzhu-vue/src/api/system/dept/type/SysDept.ts`
- `shanzhu-vue/src/api/global/Type.ts`

山竹业务 API 目录固定为：

- `shanzhu-vue/src/api/shanzhu/goal/Goal.ts`
- `shanzhu-vue/src/api/shanzhu/goal/type/Goal.ts`

当前仓库尚不存在 `shanzhu-vue/src/api/shanzhu`。第一次实现山竹业务接口时，需要创建该业务 API 根目录，并保持 API 目录与页面目录的业务域一致。

约定：

- 不要在页面中直接写 `axios`
- 不要绕过 `Request.ts`
- 请求和响应类型必须显式声明
- 列表、详情、新增、修改、删除方法应命名清晰

### 4.3 类型规范

TypeScript 类型应放在对应 API 或业务目录下的 `type` 文件夹中。

要求：

- 公共 API 返回值必须有明确类型
- 表单模型、列表项、详情对象应拆分清楚
- 避免使用 `any`
- 字段命名应与后端 VO / DTO 尽量一致

### 4.4 路由与权限规范

路由相关逻辑主要由以下文件管理：

- `shanzhu-vue/src/router/index.ts`
- `shanzhu-vue/src/permission.ts`
- `shanzhu-vue/src/utils/AppInit.ts`

后续新增业务页面时，需要确认页面是：

- 静态路由页面
- 登录后动态路由页面
- 菜单驱动页面

如果是后台菜单页面，优先走现有动态路由和菜单初始化机制，不要私自新增绕过权限的入口。

### 4.5 UI 与样式规范

当前前端使用 Vue 3 + Ant Design Vue，不是 React / CNUI 项目。

页面实现时：

- 优先复用 Ant Design Vue 组件
- 样式使用当前项目已有风格
- 避免全局裸选择器污染
- 页面根节点应有稳定 className
- 不要大范围重排无关样式
- 暗色模式相关页面需要同步考虑可读性和对比度

登录页已调整为“自我数据中心”风格，后续业务 UI 建议延续数据化、仪表盘、卡片化表达。

## 5. 登录与验证码注意事项

登录页相关文件：

- `shanzhu-vue/src/views/login/index.vue`
- `shanzhu-vue/src/views/login/components/Login.vue`
- `shanzhu-vue/src/views/login/components/Register.vue`
- `shanzhu-vue/src/components/tianai-captcha/index.vue`

验证码组件已通过 `Teleport` 挂载到 `body`，用于避免被登录卡片的毛玻璃、缩放、滤镜等父级样式影响。

后续修改登录页时注意：

- 不要把验证码容器重新放回有 `backdrop-filter` 或 `transform` 的父级容器中
- 不要给验证码外层增加会导致模糊的缩放样式
- 暗色模式下要保证验证码弹层、输入框和按钮对比度

## 6. 后续业务开发规划

后续必须按以下顺序推进业务。下列目录、接口前缀和对象命名是当前项目的固定约定，开发时直接按本节落地。

### 6.1 自我数据仪表盘

业务目标：

- 展示目标完成率
- 展示习惯连续天数
- 展示今日计划完成情况
- 展示近期复盘趋势

落地位置：

- 后端 Controller：`shanzhu-biz/shanzhu-system/src/main/java/com/shanzhu/controller/ShanzhuDashboardController.java`
- 后端 Service：`shanzhu-biz/shanzhu-system/src/main/java/com/shanzhu/service/ShanzhuDashboardService.java`
- 后端 Service 实现：`shanzhu-biz/shanzhu-system/src/main/java/com/shanzhu/service/impl/ShanzhuDashboardServiceImpl.java`
- 前端页面：`shanzhu-vue/src/views/shanzhu/dashboard`
- 前端 API：`shanzhu-vue/src/api/shanzhu/dashboard`

接口前缀：

- `/shanzhu/dashboard`

核心响应对象：

- `ShanzhuDashboardSummaryVO`
- `ShanzhuGoalProgressVO`
- `ShanzhuHabitStreakVO`
- `ShanzhuReviewTrendVO`

### 6.2 目标管理

业务目标：

- 创建长期目标和阶段目标
- 拆解目标任务
- 记录目标进度
- 统计目标完成率

落地位置：

- 后端 Controller：`shanzhu-biz/shanzhu-system/src/main/java/com/shanzhu/controller/ShanzhuGoalController.java`
- 后端 Entity：`shanzhu-biz/shanzhu-system/src/main/java/com/shanzhu/entity/ShanzhuGoal.java`
- 后端 Mapper：`shanzhu-biz/shanzhu-system/src/main/java/com/shanzhu/mapper/ShanzhuGoalMapper.java`
- 后端 Mapper XML：`shanzhu-biz/shanzhu-system/src/main/java/com/shanzhu/mapper/xml/ShanzhuGoalMapper.xml`
- 后端 Service：`shanzhu-biz/shanzhu-system/src/main/java/com/shanzhu/service/ShanzhuGoalService.java`
- 后端 Service 实现：`shanzhu-biz/shanzhu-system/src/main/java/com/shanzhu/service/impl/ShanzhuGoalServiceImpl.java`
- 前端页面：`shanzhu-vue/src/views/shanzhu/goal`
- 前端 API：`shanzhu-vue/src/api/shanzhu/goal`

接口前缀：

- `/shanzhu/goal`

核心对象：

- `ShanzhuGoal`
- `ShanzhuGoalDTO`
- `ShanzhuGoalVO`
- `ShanzhuGoalProgressDTO`
- `ShanzhuGoalProgressVO`

### 6.3 习惯追踪

业务目标：

- 创建习惯
- 每日打卡
- 统计连续天数
- 记录中断与恢复情况

落地位置：

- 后端 Controller：`shanzhu-biz/shanzhu-system/src/main/java/com/shanzhu/controller/ShanzhuHabitController.java`
- 后端 Entity：`shanzhu-biz/shanzhu-system/src/main/java/com/shanzhu/entity/ShanzhuHabit.java`
- 后端 Mapper：`shanzhu-biz/shanzhu-system/src/main/java/com/shanzhu/mapper/ShanzhuHabitMapper.java`
- 后端 Mapper XML：`shanzhu-biz/shanzhu-system/src/main/java/com/shanzhu/mapper/xml/ShanzhuHabitMapper.xml`
- 后端 Service：`shanzhu-biz/shanzhu-system/src/main/java/com/shanzhu/service/ShanzhuHabitService.java`
- 后端 Service 实现：`shanzhu-biz/shanzhu-system/src/main/java/com/shanzhu/service/impl/ShanzhuHabitServiceImpl.java`
- 前端页面：`shanzhu-vue/src/views/shanzhu/habit`
- 前端 API：`shanzhu-vue/src/api/shanzhu/habit`

接口前缀：

- `/shanzhu/habit`

核心对象：

- `ShanzhuHabit`
- `ShanzhuHabitDTO`
- `ShanzhuHabitVO`
- `ShanzhuHabitCheckInDTO`
- `ShanzhuHabitCheckInVO`

### 6.4 复盘记录

业务目标：

- 支持日复盘、周复盘、月复盘
- 记录做得好的事、问题和改进项
- 支持按时间线查询复盘记录

落地位置：

- 后端 Controller：`shanzhu-biz/shanzhu-system/src/main/java/com/shanzhu/controller/ShanzhuReviewController.java`
- 后端 Entity：`shanzhu-biz/shanzhu-system/src/main/java/com/shanzhu/entity/ShanzhuReview.java`
- 后端 Mapper：`shanzhu-biz/shanzhu-system/src/main/java/com/shanzhu/mapper/ShanzhuReviewMapper.java`
- 后端 Mapper XML：`shanzhu-biz/shanzhu-system/src/main/java/com/shanzhu/mapper/xml/ShanzhuReviewMapper.xml`
- 后端 Service：`shanzhu-biz/shanzhu-system/src/main/java/com/shanzhu/service/ShanzhuReviewService.java`
- 后端 Service 实现：`shanzhu-biz/shanzhu-system/src/main/java/com/shanzhu/service/impl/ShanzhuReviewServiceImpl.java`
- 前端页面：`shanzhu-vue/src/views/shanzhu/review`
- 前端 API：`shanzhu-vue/src/api/shanzhu/review`

接口前缀：

- `/shanzhu/review`

核心对象：

- `ShanzhuReview`
- `ShanzhuReviewDTO`
- `ShanzhuReviewVO`
- `ShanzhuReviewItemDTO`
- `ShanzhuReviewItemVO`

## 7. 开发工作流

后续新增一个完整业务功能时，必须按以下流程执行：

1. 先搜索当前项目是否已有相近业务或通用能力
2. 先确定数据库表、实体、DTO、VO
3. 再实现后端 Controller / Service / Mapper / XML
4. 再实现前端 API 类型和请求方法
5. 再实现页面和业务组件
6. 最后检查路由、菜单、权限、暗色模式和明显 lint 问题

重要约束：

- 修改前必须搜索和阅读相关文件，确认现有能力和真实实现
- 不要重复实现已有工具函数、请求封装、响应模型
- 不要绕过现有权限体系
- 不要直接在页面里写底层请求
- 不要把只在单页使用的组件过早提升到全局组件目录

## 8. 给 Aone Copilot 的使用约定

后续在本仓库继续开发时，Aone Copilot 应遵守：

- 优先读取本文档理解项目约定
- 具体改代码前先搜索已有实现，避免重复造轮子
- 后端优先参考 `SysDept` 相关分层
- 前端优先参考 `SystemDept.vue`、`Dept.ts`、`Request.ts`
- 登录页和验证码相关修改要保留 `Teleport` 修复
- 当前项目使用 Ant Design Vue，不能套用 React / CNUI 规范
- 新业务应围绕“数据化管理自我”的产品方向展开
