# 发现与决策

> **范围**：本项目的架构梳理。每条结论均已对照 `erp-boot` / `erp-web` / `erp-app` 三个工程的实际代码核对
> **参考工程**：`jshERP/jshERP-boot`（管伊佳ERP）+ `jshERP/jshERP-web`，探索结论见第十节
> **日期**：2026-11-23（初版）／2026-11-24（补入 jshERP-boot 参考工程探索）

---

## 需求

做一套**生产型 ERP**，业务覆盖两大块：

- **进销存**：采购、销售、库存
- **生产模块**：BOM、MRP、生产工单、领料/退料、委外加工

核心判断：这个项目的难点**不在 CRUD**，而在业务规则 —— BOM 的多级结构、物料需求的拆解算法、库存扣减的并发安全、单据在各环节的状态流转。真正的门槛在业务建模，不在技术选型。

---

## 研究发现

### 一、业务域与模块划分

按生产型 ERP 的定位，拆成七个域：

| 域 | 承担内容 | 建模要点 |
|---|---|---|
| 基础资料 | 物料、物料分类、供应商、客户、仓库、部门、员工 | 物料需带 BOM 关联，是生产侧的主数据入口 |
| 进销存 | 采购订单/退货/审批、销售订单、仓库管理、入库、出库、库存查询 | 单据统一表头 + 明细 |
| 库存台账 | 库存表 + 库存流水表、仓库操作日志 | 流水表是全系统的账实一致性基础 |
| 生产 | BOM 物料清单、生产工单、领料单、退料单、生产入库、MRP 物料需求运算 | BOM 多级递归；MRP 强依赖前序数据准确 |
| 委外 | 委外加工单、委外发料、委外回货 | 发料与回货都必须过库存流水 |
| 成本与报表 | 移动加权平均成本、生产进度报表、物料需求报表 | 成本口径统一为移动加权平均 |
| 系统权限 | 用户/角色/菜单/字典 | 由前端脚手架自带，不需自研 |

### 二、管理系统菜单结构

7 个顶级模块、26 个末级页面：

```
ERP 管理系统
│
├── 系统管理 ───────────── 平台级与权限基础
│   ├── 部门管理 ───────── 组织架构，决定数据权限归属与操作人所属部门
│   ├── 用户管理 ───────── 账号、所属部门、所属租户，对应登录身份与单据操作人
│   ├── 角色管理 ───────── RBAC 角色，含数据范围（本人/本部门/全部）与价格查看权限
│   ├── 菜单管理 ───────── 菜单树 + 按钮级权限点，前端路由与鉴权的共同数据源
│   ├── 字典管理 ───────── 数据字典类型与字典项
│   ├── 日志管理 ───────── 操作日志，谁在什么时间对哪张单据做了什么
│   ├── 系统配置 ───────── 业务开关：负库存、强制审核、移动平均价、出入库管理等
│   ├── 平台配置 ───────── 平台级参数：平台名、注册开关、验证码开关、APP 版本
│   ├── 租户管理 ───────── 多租户主体的开通、禁用与到期管理
│   └── 插件管理 ───────── 插件的列表、安装、启动、停止、卸载
│
├── 商品管理 ───────────── 主数据
│   ├── 商品类别 ───────── 分类树，商品的类目归属
│   ├── 商品信息 ───────── 商品主档：名称、型号、颜色、品牌、产地、单位、默认仓库
│   ├── 多单位 ─────────── 基本单位与其它单位，及其换算比例
│   ├── 多属性 ─────────── 商品自定义扩展属性的「值」
│   └── 商品属性 ───────── 商品属性项定义（属性的「名」）
│
├── 基础资料 ───────────── 往来单位与核算主数据
│   ├── 供应商信息 ─────── 供应商档案，采购单的来源方，含期初欠款
│   ├── 客户信息 ───────── 客户档案，销售单的来源方，含期初欠款
│   ├── 会员信息 ───────── 会员档案与预付款余额
│   ├── 仓库信息 ───────── 多仓库基础信息，出入库的归属仓库，含库存预警阈值
│   ├── 结算账户 ───────── 资金账户，收付款的结算目标，含期初余额
│   ├── 收支项目 ───────── 收入 / 支出的分类项目
│   └── 经手人管理 ─────── 业务员、仓管员、财务员等，单据上的操作人
│
├── 采购管理
│   ├── 请购单 ─────────── 采购需求发起，可被采购订单关联
│   ├── 采购订单 ───────── 状态：未审核 → 已审核 → 部分完成 / 全部完成
│   ├── 采购入库 ───────── 验收入库，写库存流水、抬升移动加权平均成本价
│   └── 采购退货 ───────── 向供应商退货，生成出库并冲减库存与成本价
│
├── 销售管理
│   ├── 销售订单 ───────── 客户下单，状态：未审核 → 已审核 → 部分完成 / 全部完成
│   ├── 销售出库 ───────── 拣货出库，扣减库存并按当前成本价结转
│   └── 销售退货 ───────── 客户退货，生成入库并回补库存
│
├── 零售管理
│   ├── 零售出库 ───────── 门店零售开单，直接出库并统计零售业绩
│   └── 零售退货 ───────── 零售退货，回补库存
│
├── 仓库管理
│   ├── 其它入库 ───────── 采购 / 退货之外的入库来源
│   ├── 其它出库 ───────── 采购 / 退货之外的出库去向
│   ├── 调拨出库 ───────── 仓库间调拨，拆为调拨出库与调拨入库两张单据
│   ├── 组装单 ─────────── 由子件组装成品，按当前成本价结转
│   └── 拆卸单 ─────────── 将成品拆解为子件，按当前成本价结转
│
├── 财务管理
│   ├── 收入单 ─────────── 其它收入登记
│   ├── 支出单 ─────────── 其它支出登记
│   ├── 收款单 ─────────── 客户还款，记账后回写业务单据的欠款字段
│   ├── 付款单 ─────────── 供应商付款，记账后回写业务单据的欠款字段
│   ├── 转账单 ─────────── 账户之间划转，不产生收支
│   └── 收预付款 ───────── 收取预付款，更新会员预付款余额与订单订金
│
└── 报表查询
    ├── 进销存统计 ─────── 按时间、仓库、商品统计进出库数量与金额
    ├── 采购统计 ───────── 按时间、供应商、类别统计进货量与金额
    ├── 销售统计 ───────── 按时间、商品、类别统计销量与销售额
    ├── 零售统计 ───────── 按时间、商品统计零售销售额
    ├── 账户统计 ───────── 账户余额与收付款流水
    ├── 入库明细 ───────── 逐笔入库流水明细
    ├── 出库明细 ───────── 逐笔出库流水明细
    ├── 入库汇总 ───────── 按商品汇总的入库统计
    ├── 出库汇总 ───────── 按商品汇总的出库统计
    ├── 调拨明细 ───────── 调拨单据流水明细
    ├── 商品库存 ───────── 按 仓库 × 商品 的库存结存
    ├── 库存预警 ───────── 低于预警阈值的库存清单
    ├── 客户对账 ───────── 客户往来对账
    └── 供应商对账 ─────── 供应商往来对账
```

**菜单落地要点**

1. **菜单即路由**：`菜单维护` 的树形数据就是 `erp-web` 的 vue-router 路由来源，父节点是分组、末级节点才是页面。菜单表要带 `component` 路径、`perms` 权限标识、排序、图标四类字段。
2. **审批不是独立状态**：采购/销售各有一个「审批」菜单，但它对应的是订单单据上的**同一个状态字段**（待审批）。审批页应理解为"按状态筛出的待办列表"，而不是另一套状态机 —— 否则同一张订单会出现两处状态打架。
3. **单据状态机有两套说法**：菜单里是 `待审批 → 已审批 → 已入库/已出库`，第五节业务规则写的是 `草稿 → 已审核 → 已关闭`。落地前必须统一成一套。建议以菜单为准：草稿(待审批) → 已审批 → 已完成(已入库/已出库/已关闭)。
4. **库存维度是 仓库 × 商品 × 规格**：菜单同时出现「商品」和「商品规格」，且规格「有独立库存」，所以库存表的主键维度是三段，`库存查询` 也是按这三段查。第四节数据模型里的「物料 × 仓库」要补上 SKU 维度。
5. **报表入口是独立菜单**，不挂在单据下面，说明报表是跨单据的聚合查询 —— 走独立 Controller，不复用单据列表接口。

**菜单尚未覆盖的业务（需确认）**

| 缺口 | 说明 |
|---|---|
| 客户档案无处维护 | 销售订单要挂客户，但菜单里没有客户管理入口。建议归入「商品管理」与供应商并列，或「基础维护」 |
| 承运人档案无处维护 | 运输任务指派要选承运人，但没有承运人档案的维护入口 |
| 无生产模块入口 | 当前菜单是**进销存 + 运输 + 报表**，没有 BOM / MRP / 生产工单 / 领料 / 委外，与"生产型 ERP"的定位不一致 —— 是本期不做，还是后续追加？<br>*（2026-11-24 补：参考工程 jshERP 的核心菜单同样没有生产入口 —— 它的"生产任务 / 委外任务"在闭源插件 `produce` 里。也就是说**没有现成菜单可以抄**，这块是纯自研，见 10.2）* |
| 无成本核算入口 | 移动加权平均成本没有承载页面（成本口径仍要在出库时计算）<br>*（2026-11-24 补：参考工程同样没有成本模块页，成本价只在出入库明细里以"当前成本"列呈现，见 10.2 / 10.4）* |

### 三、系统分层

```
┌─────────────────────────────────────────────────────────┐
│  erp-web   管理系统   Vue3 + Vite + Pinia + vue-router      │
│  erp-app   App        uni-app（Vue3）                        │
└───────────────────────────┬─────────────────────────────┘
                            │ HTTP / JSON（开发期经 Vite proxy 转发）
┌───────────────────────────▼─────────────────────────────┐
│  erp-boot   Spring Boot 4.2.0 / JDK 17                    │
│  ├─ Web 层    Controller + springdoc(Swagger)            │
│  ├─ 业务层    Service（单据状态机、库存扣减、BOM 递归、MRP）  │
│  ├─ 持久层    MyBatis + XML Mapper（SQL 显式可控）         │
│  └─ 横切      Redis（缓存/单据锁）、springdoc、devtools     │
└───────────────────────────┬─────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────┐
│  MySQL 8.0.46（业务主库）   Redis 6379（缓存 / 分布式锁）    │
└─────────────────────────────────────────────────────────┘
```

**分层要点**：后端是标准三层 + MyBatis XML。Redis 不是可有可无的缓存，而是**单据审核锁**的落点 —— 多实例部署时单据审核与库存变动要靠它加分布式锁，所以它属于基础设施而非性能优化项。

### 四、数据模型骨架

贯穿全系统的规则：**所有业务单据统一「表头 + 明细」结构**。这条规则比任何单表设计都重要，因为它同时决定了：

- 数据库层面：订单/工单/领料单/出入库单都是一对多，父表存状态与汇总，子表存明细行
- 应用层面：单据页可以做成一套通用模板，只换字段与校验
- 报表层面：主表做汇总查询，明细表做穿透

按此规则展开的核心表：

| 表组 | 主表 | 明细表 |
|---|---|---|
| 主数据 | 物料、物料分类、仓库、供应商、客户、部门、员工 | — |
| 结构件 | BOM 主表 | BOM 明细表（父件 → 子件，含数量、损耗率、层级） |
| 交易件 | 销售订单、采购订单、生产工单、领料单/退料单、委外单、出入库单 | 各自对应的明细表 |
| 台账件 | 库存表（物料 × 仓库 维度） | 库存流水表（每一次变动一条，只增不改） |

BOM 是**多级递归结构**：成品 → 半成品 → 物料，展开深度不定。递归查询的性能需要专门处理，实现阶段要在「一次性拉平 + 内存展开」和「递归 CTE」之间选一个，不能直接递归查库。

### 五、核心业务规则（系统的真正难点）

| 规则 | 约束 | 为什么重要 |
|---|---|---|
| 库存不可直接修改 | 库存值只能由单据审核驱动变更，同时写入流水表 | 保证账实可对账、变动可追溯；否则库存就成了一个无法审计的数字 |
| 单据状态机 | 草稿 → 已审核 → 已关闭 | 审核是唯一触发业务动作的时点（改库存、扣料、记成本都在审核时发生） |
| 库存扣减并发控制 | 用乐观锁防超扣；多实例时单据审核 + 库存变动再叠加分布式锁 | 唯一会直接产生资金/实物损失的地方 |
| 成本核算 | 移动加权平均法 | 进销存出库成本、委外结算、生产成本归集的共同基础 |
| BOM 多级展开 | 递归结构，注意深度与性能 | 生产模块与 MRP 的输入源 |
| MRP 排期 | **不放在第一版** | 依赖 BOM、库存、在途量的准确性，底层不稳时做 MRP 只会放大错误 |

> **2026-11-24 补（依据参考工程实测）**：
> - **状态机**：参考工程实际用的是 5 值主状态（`0` 未审核 / `1` 已审核 / `2` 完成 / `3` 部分 / `9` 审核中）+ 采购进度独立字段，不是本节的三态。裁决见 10.7。
> - **并发控制**：参考工程**一条都没做到** —— 无乐观锁、无 `WHERE qty >= n` 原子扣减、可用量校验是"先查后比"的 TOCTOU；它的 `RedisLockUtil` 只用在单号生成上。本节的三条约束必须自己实现，详见 10.5 第 7/8/9 条。
> - **成本核算**：算法可直接采纳参考工程的移动加权平均分录表（见 10.4），但参考工程把成本价刷新放在"保存/删除单据"而不是"审核"，且审核只重算库存 —— 本项目应在审核时**库存与成本同时落账**。

### 六、单据页统一范式

所有列表型管理页与单据页统一采用四段式布局，采购订单、入库单、出库单等单据页同样适用。这套范式实际就是**一个可复用的 CRUD 页模板**：

```
[顶部操作区]  系统标题 / 修改密码 / 注销
[筛选区]      多条件组合查询（用户名、姓名、电话、部门下拉） + 右侧「新建」按钮
[数据表格]    列表字段 + 操作列（分配角色 / 修改 / 删除）
[分页区]      首页 / 上一页 / 下一页 / 末页 / 当前页码
```

必须抽象成通用列表组件，而不是每个模块复制粘贴 —— 单据页有十几张，逐页复制会产出十几份重复代码；抽成组件后，新增一张单据页只需配置字段与校验规则。

### 七、权限模型

四级模型：**员工 + 部门 + 角色 + 资源**（对应 user/dept/role/menu 字典体系）。特点是：

- 菜单即权限资源，用**树形父子结构**递归生成前端路由
- 角色绑定资源，部门承载组织树
- 前后端一致的四级模型，后端不做额外权限体系

### 八、分阶段落地顺序

按**业务复杂度**而非技术复杂度排序：

```
① 需求梳理与模块拆分
② 数据库设计（主数据 → 单据表头/明细 → 台账）
③ 跑通脚手架：前后端本地起得来，菜单与公共单据页面就位
④ 编码：基础资料 → 采购入库/销售出库/库存盘点 → BOM 树形维护
       → 工单/领料退料/生产入库 → MRP 运算 → 报表
⑤ 业务规则落地：乐观锁、状态机、库存流水
⑥ 部署上线：前后端打包，MySQL 与 Redis 独立部署
```

④ 里 BOM 被排在进销存之后、MRP 之前，是刻意为之 —— **BOM 是生产模块的地基，必须先于工单和 MRP**。

### 九、实现约定

| 场景 | 约定 |
|---|---|
| 一对多关系 | 用 MyBatis `resultMap` 或分表查询组装，不用 ORM 自动映射 |
| 树形菜单 | 菜单表父子结构 + 递归，生成前端路由 |
| Excel 导入导出 | EasyExcel 或 POI，大数据量分批写出，避免 OOM |
| 登录与鉴权 | 拦截器 / Spring Security，后端统一鉴权，前端按权限动态生成路由 |
| 对象管理 | Spring Boot 自动装配与 `@Bean`，不写 XML 配置 |
| 依赖注入 | 用 `@Resource`（或 `@Autowired`）字段注入，**不用构造器注入**（Controller / Service 统一按此写法） |
| 通用 CRUD 代码 | 用 MyBatis-Plus 的 `BaseMapper` / `IService` 收敛，不重复造轮子 |

---

## 参考工程：jshERP-boot（探索结论）

> **参考对象**：`D:/study/my-workspace/erp-workspace/jshERP/jshERP-boot`（上游 `jshERP` = 管伊佳ERP，原名"华夏 ERP"）
> **配套前端**：`D:/study/my-workspace/erp-workspace/jshERP/jshERP-web`
> **探索方式**：三个只读代理并行扫接口层 / 业务与数据层 / 配置与数据库；结论均落到 `file:line`
> **日期**：2026-11-24

### 10.1 参考工程是什么

| 项 | 值 |
|---|---|
| 形态 | 单体**多租户** SaaS：一个部署实例承载 N 个租户 |
| 规模 | 274 个 Java 文件、约 71,000 行、30 个 Controller、31 个 Service、32 张表、61 个 Mapper XML |
| 后端栈 | Spring Boot **2.0.0** / Java **8** / MyBatis-Plus 3.0.7.1 / PageHelper / fastjson 1.2.83 / Redis / Swagger2 |
| 前端栈 | Vue **2.7** + **ant-design-vue 1.5.2** + vuex 3 + vue-router 3 + vue-cli（**不是** Element Plus，**不是** Vue3） |
| 业务覆盖 | 基础资料 / 进销存 / 零售 / 组装拆卸 / 财务收付款 / 报表 / 组织权限 / 多租户 / 插件扩展 |
| 文档 | `README.md` 533 行深度技术文档；`docs/jsh_erp.sql` 完整 32 表 DDL + 初始数据 |

### 10.2 最重要的一条结论：生产模块在参考工程里**不存在**

对全仓库（排除 `target/`）grep `BOM|MRP|物料清单|生产工单|委外|workOrder`，**代码命中数为 0**。逐项核实：

| 模块 | 参考工程有无 | 证据 |
|---|---|---|
| BOM / 物料清单 | ❌ 无 | 无 `jsh_bom*` 表，无实体 / Mapper / Service / Controller |
| MRP / 物料需求计划 | ❌ 无 | 无关键字命中，无展开算法 |
| 生产工单 | ❌ 无 | 无工单表，无工单服务 |
| 委外加工 | ❌ 无 | 在**闭源插件** `produce` 里（另有 `stock-check` 盘点插件），核心代码只预留了 `SUB_TYPE_CHECK_ENTER` / `SUB_TYPE_REPLAY` 两个常量 |
| 独立成本核算（卷积/差异/成本对象） | ❌ 无 | 只有库存移动加权平均，没有 BOM 展开、没有标准成本 |

仅有的两处"生产"痕迹都不是生产管理模块：
1. `BusinessConstants.BILL_TYPE_PRODUCE_IN = "生产入库"`（`constants/BusinessConstants.java:104`）—— 只是一个 `bill_type` 字符串标签；
2. **组装单 / 拆卸单**（`SUB_TYPE_ASSEMBLE` / `SUB_TYPE_DISASSEMBLE`）—— 库存口径上的两个加减分支（`service/DepotItemService.java:927`），**没有父件-子件清单、没有用量、没有工单、不产生在制品**。

**结论**：进销存 / 财务 / 权限这三块可以参考；**本项目的定位重心（生产）零参考，必须全部自研**。这是本次探索对项目排期影响最大的一条。

### 10.3 可直接借鉴的部分

| # | 借鉴点 | 参考工程实现 | 对本项目的价值 |
|---|---|---|---|
| 1 | **移动加权平均成本价算法** | `DepotItemService.calculateCurrentUnitPrice`（`service/DepotItemService.java:1213`）—— 滚动维护 `currentNumber` / `currentAllPrice` / `currentUnitPrice`，4 位小数 HALF_UP，绝对值 > 1 亿归零，金额或数量 ≤ 0 退回单据单价 | 第五节的"成本口径统一为移动加权平均"有现成算法，分录规则见 10.4 表，可直接翻译到 Java 17 |
| 2 | **库存由已审核单据派生** | 聚合 SQL 里 `forceFlag` 只统计 `dh.status in ('1','2','3')`（`mapper_xml/DepotItemMapperEx.xml:810`），未审核单据自然不进库存 | 与本项目"审核是唯一触发业务动作的时点"一致，且实现极简 |
| 3 | **单据号集中生成 + Redis Lua 分布式锁** | `jsh_sequence` 表 + `utils/RedisLockUtil`（Lua `setnx` + `pexpire`，支持重入与安全解锁），唯一调用点 `service/SequenceService.java:85` | 第五节要求的"单据审核锁"有现成实现范式可抄（**但要注意：参考工程只把锁用在单号上，没用在库存/审核上**） |
| 4 | **通用单据页模板（前端）** | `jshERP-web/src/views/bill/mixins/BillListMixin.js`（1111 行）+ `BillModalMixin.js`（1277 行）+ 通用弹窗 `BillDetail.vue` / `BatchSetDepot.vue` / `QuickEditModal.vue`，13 个列表页 + 13 个弹窗页共用 | 直接印证第六节"单据页统一范式"的判断，且**证明了 mixin 抽象路线可行** |
| 5 | **菜单即路由** | `jsh_function` 一张表同时承载菜单树与按钮级权限；前端 `permission.js` 调 `generateIndexRouter(menuData)` 动态 `router.addRoutes` | 印证第七节权限模型 |
| 6 | **系统开关驱动业务分支** | `jsh_system_config` 一张表放 15 个开关：强审核 / 负库存 / 出入库管理 / 移动平均价 / 多账户 / 含税价 / 以销定购 / 强制结单 …… | 「可配置性」的落地范式：业务代码读开关而非硬编码。本项目的"是否允许负库存""是否强制审核"应走同一套路 |
| 7 | **单位换算统一出口** | `UnitService.parseAllPriceByUnit` / `parseUnitPriceByUnit` / `parseStockByUnit` 三个方法，所有数量与金额换算都走它们 | 避免各处散落除法导致的精度漂移 |
| 8 | **数据范围用通用 EAV 表** | `jsh_user_business`（`type` / `key_id` / `value` / `btn_str`），一种表承载 用户-角色 / 用户-仓库 / 用户-客户 / 角色-功能 四类关联 | 权限模型可以少建 4 张关系表 |
| 9 | **操作日志** | `LogService.insertLog`，单据保存 / 审核 / 反审核后落库，含操作人、类型、内容、耗时 | 审计追溯的基础设施 |
| 10 | **排序防注入** | `SqlUtil.escapeOrderBySql` 白名单（仅字母数字下划线空格逗号小数点，限长 500），驼峰转下划线 | 报表排序参数必须过这一道 |
| 11 | **多单位与多属性商品模型** | `jsh_material`（主档）+ `jsh_material_extend`（条码 / SKU / 多单位 / 四档价，一商品多行）+ `jsh_unit`（换算比例）+ `jsh_material_property` / `_attribute`（自定义属性） | 对应本项目"商品规格"维度，比"一张商品表带规格字段"更干净 |
| 12 | **完整建表脚本** | `docs/jsh_erp.sql`（32 表 DDL + 初始数据 + 索引） | **最大的现成资产**：表命名、字段、索引、逻辑删除约定可直接改写复用 |

### 10.4 移动加权平均分录规则（可直接落地的算法表）

参考工程 `DepotItemService.java:1219-1283` 的分支逻辑：

| 场景 | 处理方式 | 行号 |
|---|---|---|
| 入库（采购、其它入库） | `currentAllPrice += allPrice; currentNumber += basicNumber;` 后重算均价 | `:1236-1243` |
| 入库（销售退货、零售退货） | 按当前成本回冲：`currentAllPrice += basicNumber × currentUnitPrice`（**不改成本价**） | `:1230-1234` |
| 出库（销售、其它出库） | 同上，按当前成本价结转 | `:1258-1262` |
| 出库（采购退货） | 反向按入库处理，重算均价 | `:1249-1257` |
| 组装单 / 拆卸单 / 盘点复盘 | 按当前成本价结转 | `:1265-1271` |
| 兜底 | 金额或数量 ≤ 0 时退回单据上的 `unitPrice` | `:1242` / `:1256` |
| 溢出保护 | 单价绝对值 > 1 亿时归零 | `:1273-1276` |

**注意**：`getMoveAvgPriceFlag()` 不是成本算法开关，它只决定销售/零售行上 `purchaseUnitPrice` 取"当前移动平均成本"还是"商品档案采购价"（`:604-608`）。成本核算本身恒为移动加权平均。

### 10.5 不可借鉴、必须自己重做的部分

以下都是参考工程**实际存在但不该抄**的东西，抄了就是把 bug 一起搬过来：

| # | 问题 | 参考工程现状 | 本项目怎么处理 |
|---|---|---|---|
| 1 | **接口层零权限校验** | 按钮权限只在前端隐藏（`UserController.java:436` 返回按钮权限给前端），后端任何登录用户都能直接调任意接口 | 用 Spring Security 做后端统一鉴权（第九节已约定） |
| 2 | **登录失效返回 HTTP 500** | `filter/LogCostFilter.java:65` 未登录时返回 500 + 纯文本 `loginOut` | 返回 401 + 统一 `Result` |
| 3 | **响应格式四套并存** | `AjaxResult{code,msg,data}` / `TableDataInfo{code,data:{rows,total}}` / `ResponseCode{code,message,data}`（且是 **JSON 字符串**）/ 裸 `JSONObject{code,data}`（全局异常处理器返回的就是这个，无 `msg`） | 保持 `com.erp.common.result.Result` **单一返回体**（已实现） |
| 4 | **没有独立库存流水表** | 库存结存靠每次全量聚合重算（`DepotItemService.java:1301` → `getStockByParam`），单据明细兼作流水 | **保留本项目原设计的库存流水表** —— 详见 10.6 |
| 5 | **成本价重算 O(该商品全量明细)** | `updateCurrentUnitPrice` 每次拉该商品全部历史明细滚动计算（`:1170-1174`），商品流水一多就慢 | 改为增量维护 + 流水表驱动 |
| 6 | **审核与成本价刷新不同步** | 审核只重算库存（`DepotHeadService.java:811-818`），成本价只在 save/delete 时刷（`:717` / `:546`），开强审核后成本价滞后 | 审核时库存与成本**同时**落账到流水表 |
| 7 | **可用量校验非原子（TOCTOU）** | `checkMaterialStock`（`DepotItemService.java:1587`）先 SELECT 求和 → Java 层比较 → 再 UPDATE，无 `WHERE current_number >= n` | 用条件更新 `UPDATE ... SET qty = qty - n WHERE id = ? AND qty >= n`，配合乐观锁 version |
| 8 | **无乐观锁** | 全部实体 grep `version` 零命中，无 `@Version` | 按第九节加 version 字段 |
| 9 | **分布式锁没用在关键路径** | `RedisLockUtil` 唯一调用点是单号生成，**单据审核与库存变动都没加锁** | 审核 + 库存变动必须叠加分布式锁 |
| 10 | **租户 ID 明文拼在 token 后缀** | `token = uuid + "_" + tenantId`（`UserService.java:348`），伪造后缀即可越权 | 本项目不做多租户；若做绝不能这样 |
| 11 | **白名单硬编码 context-path** | `LogCostFilter` 的 `filterPath` 写死 `/jshERP-boot/` 前缀，改 `context-path` 即全失效 | 白名单用配置项或常量，不写死 |
| 12 | **逻辑删除靠手写片段** | 所有查询带 `ifnull(delete_flag,'0') != '1'`，漏写即查出已删数据；且 `jsh_account` / `jsh_material_extend` / `jsh_depot` 用的是大写 `delete_Flag` | 用框架级拦截器统一处理，不靠人记 |
| 13 | **审计字段严重缺失** | `create_time` 仅 10/32 张表、`update_time` 仅 5 张，`create_by` / `update_by` **只有字典表有**；业务表各用 `creator` / `updater` / `oper_time` 自行其是 | 新表统一 `create_by` / `create_time` / `update_by` / `update_time` / `deleted` |
| 14 | **Excel 用 jxl 2.6.12** | 老牌 JExcelAPI，有已知 CVE，且不支持 xlsx 新特性 | 按第九节用 EasyExcel |
| 15 | **版本偏老** | Spring Boot 2.0 / fastjson 1.2.83 / MyBatis-Plus 3.0.7.1（`PaginationInterceptor`、`TenantSqlParser` 均为旧版 API） | 本项目维持 Spring Boot 4.2 / Jackson 3 |
| 16 | **明文凭据入仓** | `application.yml` 里数据库 `root/123456`、Redis `1234abcd` | 本项目已做 classpath 外 + dev/prod profile 隔离，**不回退** |

### 10.6 数据模型对照：两个项目差在哪

| 本项目设想（findings.md 第四节） | 参考工程对应表 | 差异与判断 |
|---|---|---|
| 物料 / 物料分类 | `jsh_material` / `jsh_material_category` | 基本一致 |
| **商品规格（SKU）** | `jsh_material_extend` + `jsh_depot_item.sku` | 参考工程把 SKU 放在**明细**上，库存结存表不带 SKU 维度 |
| 供应商 / 客户 | `jsh_supplier`（**同表**，`type` 区分客户/供应商/会员） | 本项目原计划分表。参考工程合表少一次 JOIN，但要在同表里区分三类往来单位 |
| 仓库 | `jsh_depot` | 一致 |
| 采购订单 / 销售订单 / 出入库单 / 退货 / 调拨 / 组装 / 拆卸 …… | `jsh_depot_head` + `jsh_depot_item`，**16 种 `sub_type` 合一** | **重要范式**：不是"每类单据一张表"，而是"一张主表 + `type` + `sub_type` + `bill_type` 三级分类"。单据属性差异用 `sub_type` 下的分支逻辑处理 |
| 库存表（仓库 × 商品 × 规格 三段维度） | `jsh_material_current_stock`（**material × depot 两段**） | 参考工程**没有 SKU 维度**，SKU/批次库存靠明细表实时聚合。三段维度做主库存表会导致组合爆炸 —— **建议采纳两段维度，SKU 降级为明细属性** |
| **库存流水表** | **不存在** | 参考工程用单据明细兼作流水。**本项目保留独立流水表**：审计性更强，且能把成本重算从 O(全量明细) 降到 O(增量) |
| BOM 主表 / BOM 明细表 | **无** | 自研 |
| 生产工单 / 领料单 / 委外单 | **无** | 自研 |
| 客户档案 / 承运人档案 | `jsh_supplier`（客户）/ `jsh_person`（经手人） | 承运人无对应，jshERP 也没有运输模块 |
| 结算账户 / 收付款 | `jsh_account` / `jsh_account_head` / `jsh_account_item` | 参考工程的财务侧比本项目设想完整，可整体借鉴 |
| 用户 / 角色 / 菜单 / 字典 | `jsh_user` / `jsh_role` / `jsh_function` / `jsh_sys_dict_*` + `jsh_user_business` | 一致，可借鉴 |

### 10.7 单据状态机：用参考工程的实际取值裁决

第二节的"菜单口径（待审批 → 已审批 → 已入库/已出库）"与第五节的"草稿 → 已审核 → 已关闭"至今未统一。参考工程给出了第三方答案：

`depot_head.status`（`constants/BusinessConstants.java:70-77`）

| 值 | 含义 | 说明 |
|---|---|---|
| `"0"` | 未审核 | |
| `"1"` | 已审核 | 审核动作在 `/depotHead/batchSetStatus` |
| `"2"` | 完成采购/销售 | 关联单据全部执行完 |
| `"3"` | 部分采购/销售 | 关联单据部分执行完 |
| `"9"` | 审核中 | 多级审核 |

`depot_head.purchase_status`：`"0"` 未采购 / `"2"` 完成采购 / `"3"` 部分采购

**判断**：采用**主 status + 采购进度 purchase_status 双字段**的结构，而不是单字段三态。理由是"订单 → 入库"天然是 1:N，"部分完成"是进销存的必需状态，压进单字段会与"待审批"抢位置。本项目第一版按三值落地（`DRAFT` / `APPROVED` / `COMPLETED`），第二版补 `PARTIAL` 与多级审核。

### 10.8 前端：能抄结构，不能抄代码

| 维度 | jshERP-web | erp-web | 判断 |
|---|---|---|---|
| 框架 | Vue 2.7 + vue-cli | Vue 3 + Vite | **代码不能直接抄** |
| UI 库 | ant-design-vue **1.5.2**（Vue2 版） | 无（空壳） | 需在 Vue3 生态里重选（ant-design-vue 4.x 或 Element Plus） |
| 状态 / 路由 | vuex 3 + vue-router 3 | Pinia + vue-router（空壳） | 版本代差，需重写 |
| **单据页抽象** | 2 个 mixin + 13 个列表页 + 13 个弹窗页共用 | — | **结构可抄**：mixin / composable 抽象 + 通用弹窗 + 菜单驱动路由 |
| 动态路由 | `permission.js` → `generateIndexRouter(menuData)` → `router.addRoutes` | — | 结构可抄 |

结论：`erp-web` 应照抄 jshERP-web 的**页面组织方式**（通用列表 + 通用详情弹窗 + 混合同步点），但组件实现要用 Vue3 重写。

### 10.9 可以直接搬的资产清单

| 资产 | 路径 | 怎么用 |
|---|---|---|
| 建表脚本 | `jshERP/jshERP-boot/docs/jsh_erp.sql` | 32 表 DDL + 初始数据。改表名（去 `jsh_` 前缀）、改字段规范、补审计字段后，作为本项目建库脚本的**进销存部分**基线 |
| 增量脚本的写法参考 | `docs/数据库更新记录-首次安装请勿使用.txt`（125 个版本块） | 本项目后续迭代的版本化 SQL 写法范式 |
| 成本价分录规则 | 见 10.4 | 直接翻译 |
| 单据页结构 | `jshERP-web/src/views/bill/` | 目录组织与 mixin 拆分思路（Vue3 重写） |
| 菜单树数据 | `jsh_function` 表初始数据 | 本项目菜单/权限表的初始数据参考 |

---

## 目标形态 vs 代码现状（实测比对）

当前仓库处于**起步态**，下面是距离目标还差什么（2026-11-24 实测）：

| 维度 | 目标形态 | 代码现状 | 判定 |
|---|---|---|---|
| 后端框架 | SpringBoot + MyBatis / MyBatis-Plus | Spring Boot **4.2.0** + `mybatis-spring-boot-starter 4.1.0`，**未引入 MyBatis-Plus** | 需决策：见下 |
| 管理系统 | RuoYi-Vue 脚手架 + Element Plus | `erp-web` 是 Vite 原生 Vue3 空壳（仅 App.vue / main.js / router / stores 各一），**无 Element Plus、无 Axios** | 需决策：见下 |
| 统一返回体 | 单一 `Result` | ✅ 已实现 `com.erp.common.result.Result`（code 与 HTTP 状态码一致）+ `GlobalExceptionHandler`（BusinessException / @Valid / 报文错误 / 兜底四档） | ✅ 已就绪 |
| 鉴权 | 后端统一鉴权 | ⚠️ 已用 **JWT**（jjwt）+ `spring-security-crypto`（BCrypt），但**没有 Spring Security**（无过滤器链、无方法级鉴权），`roles` / `permissions` 目前签空 | 部分完成 |
| 登录接口 | — | ✅ `AdminLoginController` → `POST /api/admin/login`，含参数校验、停用拦截、明文密码首登自动升级 BCrypt | ✅ 已就绪 |
| 权限体系 | 脚手架自带 user/role/menu/dict | 后端**无任何 Security 依赖**，前端无权限路由；只有一张 `admin` 表 | 未开始 |
| 数据库 | MySQL 8.0 | 本机 MySQL **8.0.46**，已建库 `erp_db`，`doc/sql/erp_db_init.sql` 只建了 `admin` 一张表 | ✅ 一致（表待补） |
| Redis | 缓存 + 单据锁 | `spring-boot-starter-data-redis` 已接入，配置就绪 | ✅ 一致 |
| 接口文档 | — | springdoc 3.2.1，`/swagger-ui.html` + `/v3/api-docs` 可访问 | ✅ 已就绪 |
| BOM / MRP / 工单 / 领料 / 委外 | 核心模块 | 无 Controller / Service / Mapper / 实体；**参考工程也没有（在其闭源插件里）→ 全部自研** | 未开始（零参考） |
| 进销存单据 / 库存 / 成本 | 核心模块 | 无 | 未开始（可参考 jshERP） |
| 库存乐观锁、分布式锁 | 关键规则 | 无相关代码 | 未开始 |
| Excel 导入导出（EasyExcel/POI） | 报表与单据导入 | 无相关依赖 | 未开始 |
| App（移动端） | — | `erp-app` 是 uni-app（uni-starter）工程，已含 grid / list / ucenter 等模板默认页，业务页未开始 | 定位待明确 |
| 构建与配置 | — | 单模块 Maven；配置已按 dev/prod profile 拆分，凭据在 classpath 外 | ✅ 基础设施就绪 |

### 落地前必须先定的三个岔路

1. **前端**：接 RuoYi-Vue 脚手架，还是继续在现有 Vite 空壳上自建？
   接脚手架能一次性拿到用户/角色/菜单/字典四件套和权限路由，直接满足第七节权限模型；代价是要迁就脚手架的目录约定与组件风格。现有 `erp-web` 目前只有 4 个文件，迁移成本极低，**现在做选择最便宜**。
   *（补入参考工程后的补充：jshERP-web 走的是"Vue2 + ant-design-vue 1.x + 菜单驱动动态路由"的自建路线，且已把单据页抽象成 2 个 mixin 复用 26 个页面 —— 若本项目走自建路线，应照抄这个**页面组织结构**，UI 库在 Vue3 生态里另选。）*
2. **持久层**：MyBatis XML 还是 MyBatis-Plus？
   现有 `application.yml` 已配好 `mybatis.mapper-locations: classpath*:mapper/*.xml`，走的是 XML 路线。XML 对库存扣减、MRP 拆解这类**多表关联 + 精确控制**的 SQL 更友好；MyBatis-Plus 的价值在快速 CRUD。**建议：XML 为主，MyBatis-Plus 仅用于主数据的简单 CRUD** —— 但这需要先确认。
   *（补入参考工程后的补充：jshERP 用的是 MyBatis Generator 生成 `Xxx` / `XxxExample` / `XxxMapper` + 手写 `XxxMapperEx` 的**两段式**，Ex 里放所有 JOIN 与聚合 SQL。MBG 那套对快速单表 CRUD 很省事，但生成物噪音大。本项目建议**不引入 MBG**，直接手写 Mapper + XML，Ex 风格可作参照。）*
3. **App（移动端）**：`erp-app` 这个 uni-app 工程是要纳入本次 ERP 范围，还是暂时搁置？
   它已经出现在工作区里（当前是 uni-starter 模板的默认页），但业务范围一直没定。在动手前应当明确，否则容易做到一半才发现权限模型、单据流转在移动端需要重新设计。

### 补入参考工程后新增的岔路

4. **库存表维度**：`仓库 × 商品`（两段，采纳参考工程）还是 `仓库 × 商品 × 规格`（三段，原设计）？
   参考工程的主库存表 `jsh_material_current_stock` 只有 `material_id × depot_id`，SKU 与批次库存靠明细表实时聚合。三段维度会让主库存表的行数按 SKU 组合膨胀，且每次出入库要维护多行。**建议：主库存表用两段维度，SKU / 批次作为单据明细属性，出库时实时校验可用量**（对应 10.6 的差异判断）。
5. **单据是"每类一张表"还是"一张主表 + 类型字段"**？
   参考工程把 16 种出入库单据塞进 `jsh_depot_head` 一张表，靠 `type`（入/出/其它）+ `sub_type`（16 种业务类型）+ `bill_type`（来源标记）三级分类。优点是审核、库存、报表逻辑只写一遍；缺点是字段差异靠 `sub_type` 分支硬塞，SQL 会越来越宽。
   **建议：进销存类单据合一表**（照抄参考工程），**生产类单据（工单 / 领料 / 退料 / 委外）独立成表** —— 它们要挂 BOM 展开、工单关联、委外供应商等进销存单据没有的字段，硬塞进一张表会让两边都难受。
6. **库存流水表要不要独立建**？
   参考工程**没有**，用单据明细兼作流水，靠全量聚合重算库存结存（`DepotItemService.java:1301`）。好处是永远不会有"账实不符"（结存是算出来的）；坏处是成本价重算是 O(该商品全量明细)、审计链路要靠 JOIN 单据表还原。
   **建议：保留本项目原设计的独立流水表**。参考工程的做法证明了"流水表"不是可有可无的 —— 它把成本重算从全量降到了增量，把审计从 JOIN 变成了直查。这是本项目要**超越**参考工程的地方，不是照搬的地方。

---

## 技术决策

| 决策 | 理由 |
|------|------|
| 业务先行、CRUD 后置 | ERP 的复杂度在规则层（库存流水、BOM 递归、MRP），先写 CRUD 会把结构做死 |
| 单据统一「表头 + 明细」 | 同时决定库表结构、页面模板、报表口径，一处规则三处受益 |
| 库存只由单据审核驱动变更 | 流水表是审计与对账的唯一依据 |
| BOM 先于工单与 MRP | BOM 是生产侧地基，顺序颠倒会导致工单反复重构 |
| MRP 推迟到第一版之后 | 强依赖 BOM/库存/在途量准确性，底层不稳时做只会放大错误 |
| 权限直接套用脚手架四级模型 | 自研权限的成本远高于收益 |
| 单据范式抽象成通用列表组件 | 单据页有十几张，逐页复制会产出十几份重复代码；抽象后新增单据只需配字段与校验 |
| 凭据放 classpath 外 + dev/prod profile 隔离 | 已落地并实测验证：dev 加载、prod 不加载，避免密码进 jar 进仓库 |

**补入参考工程（2026-11-24）后的新增决策**

| 决策 | 理由 |
|------|------|
| **进销存单据合一表**（`type` + `sub_type` + `bill_type` 三级分类），生产单据独立成表 | 参考工程 16 种出入库单据合一表，审核/库存/报表逻辑只写一遍；生产类单据要挂 BOM 展开与工单关联，字段差异太大，硬塞会让两边都难受 |
| **主库存表用「仓库 × 商品」两段维度**，SKU / 批次降级为单据明细属性 | 参考工程的 `jsh_material_current_stock` 就是两段；三段维度会让行数按 SKU 组合膨胀。SKU 可用量在出库时实时聚合校验 |
| **保留独立库存流水表**（不学参考工程的全量重算） | 参考工程靠每次全量聚合重算结存，成本价重算是 O(该商品全量明细)，且审计要 JOIN 单据表还原。流水表把成本重算降到增量、把审计变成直查 |
| **成本核算直接采纳参考工程的移动加权平均分录表** | 规则已验证可用（见 10.4），只需翻译到 Java 17 + `BigDecimal`；4 位小数 HALF_UP 与溢出保护一并沿用 |
| **业务分支由系统开关表驱动**，不硬编码 | 参考工程 `jsh_system_config` 放 15 个开关（强审核 / 负库存 / 出入库管理 / 移动平均价 / 多账户 / 含税价…），业务代码读开关。这是"可配置性"的落地范式 |
| **鉴权不采用参考工程的 Filter + Redis token 方案** | 参考工程后端**零权限校验**（按钮权限只在前端隐藏）、未登录返回 500、租户 ID 明文拼在 token 后缀。本项目已用 JWT，继续走 Spring Security 过滤器链 + 方法级鉴权 |
| **响应结构保持单一 `Result`** | 参考工程四套返回体并存（`AjaxResult` / `TableDataInfo` / JSON 字符串 / 裸 JSONObject），前端要分别处理 —— 反面教材 |
| **持久层不引入 MyBatis Generator** | 参考工程用 MBG 生成 `Xxx` / `XxxExample` / `XxxMapper` 制造大量噪音；本项目直接手写 Mapper + XML，复杂 SQL 参照其 `XxxMapperEx` 风格 |
| **Excel 用 EasyExcel，不用 jxl** | 参考工程用 jxl 2.6.12，有已知 CVE 且不支持新特性 |
| **本项目不做多租户** | 参考工程的多租户方案（token 后缀携带 tenantId）有越权风险，不值得借鉴 |
| **生产模块零参考、全部自研** | 参考工程的生产（生产任务 / 委外任务）在闭源插件 `produce` 里，核心代码没有 BOM / MRP / 工单 / 委外任何实现 |

---

## 遇到的问题

| 问题 | 解决方案 |
|------|---------|
| 配置属性在 Boot 4.2 中已迁移 | `server.error.*` → `spring.web.error.*`；`server.servlet.encoding.*` → `spring.servlet.encoding.*`。旧名在 4.2 已移除且**静默失效**，不报错 |
| Jackson 版本换代 | Boot 4.2 用 Jackson 3（`tools.jackson`），java.time 支持已内置，`spring.jackson.date-format` 不作用于 LocalDateTime |
| 数据库凭据错误但启动不报错 | devtools 的 `DevToolsDataSourceAutoConfiguration` 把 DataSource 延迟到首次取连接才创建，凭据问题要到第一次查库才暴露 |
| `spring.config.import` 不做 profile 隔离 | 显式 import 的具体文件固定按 `NO_PROFILE` 解析，prod 也会加载；必须依赖 Boot 默认搜索位置 `file:./config/` 的 profile 机制 |
| 数据库密码不匹配 | `src/main/resources/application-dev.yml` 里配的账号被本机 MySQL 8.0.46 拒绝（Access denied），且本机原本没有该账号；IDEA 数据源用的是 `root`，需确认真实账号密码 |
| 参考工程 `datasource` 包名有歧义 | `com.jsh.erp.datasource` 是"数据访问层"（entities / mappers / vo），**不是多数据源**。全项目只有一个 `spring.datasource`，连接池是 Boot 2.0 默认的 HikariCP，无 druid。阅读时极易误读 |
| 参考工程 README 与实际代码有小幅偏差 | README 说 60 个 mapper_xml / 31 个 Controller / 34 个 utils，实际是 **61 / 30 / 29**。README 质量高但不能当精确清单用 |
| 参考工程逻辑删除字段大小写不统一 | `jsh_account` / `jsh_material_extend` / `jsh_depot` 用 `delete_Flag`（大写 F），其余表用 `delete_flag`。本项目新表统一小写并由框架级拦截器处理，不靠手写 `ifnull(delete_flag,'0') != '1'` |
| **Spring Boot 4.2 在 Windows 上启动失败** | `Existing directory '...\Temp\<32位哈希>' is not owned by BUILTIN\Administrators`。Boot 4.2 新增了临时目录所有权断言（`ApplicationTemp.assertDirectoryOwnership`），它比对的是 `Files.getOwner()` 返回的**具体用户**，而 Windows 管理员组的成员用户身份不等于组名，断言必然失败。**绕过方式：`mvn spring-boot:run` 会走 devtools 的 restart classloader 触发该检查，改用 `mvn clean package -DskipTests` + `java -jar target/erp-boot-0.0.1-SNAPSHOT.jar` 启动即可绕开**（jar 启动不含 devtools） |
| **ant-design-vue 3.2.20 与 4.x 的 API 差异** | 本项目锁定 3.2.20，踩了三个坑：① `a-modal` 只支持 `v-model:visible`，用 `v-model:open` 会导致弹窗打不开；② `a-form` 缺 `:model` 时 `handleSubmit` 直接跳过、不 emit `finish`，查询按钮点了没反应 —— 必须写 `:model="query"`；③ `a-select` / `a-tree-select` 不加 `show-search` 时鼠标点击无响应（placeholder 是 `pointer-events:none`），只能键盘操作 —— **必须加 `show-search`** |
| **前端响应拦截器读错字段** | 后端 `Result` 的提示语字段是 `msg`，原拦截器读的是 `error.response.data.message`，永远取不到值。已统一改为读 `.msg` |

---

## 资源

**工程路径与职责**

| 工程 | 路径 | 职责 | 技术栈 / 端口 |
|---|---|---|---|
| 后端 | `D:/study/my-workspace/erp-workspace/erp-boot` | 业务接口与数据访问，ERP 唯一后端 | Spring Boot 4.2.0 / JDK 17 / MyBatis / 端口 8082 |
| **管理系统** | `D:/study/my-workspace/erp-workspace/erp-web` | **PC 端管理界面**（后台表单、表格、单据操作） | Vue3 + Vite + Pinia + vue-router / 端口 5175，dev 期 proxy 转发到 8082 |
| **App** | `D:/study/my-workspace/erp-workspace/erp-app` | **移动端 App**（uni-app） | uni-app（Vue3），当前为 uni-starter 模板默认页面 |

三者同属 `D:/study/my-workspace/erp-workspace` 工作区，统一由 `erp-boot` 提供 API。

**参考工程（只读，不参与构建）**

| 工程 | 路径 | 用途 |
|---|---|---|
| 参考后端 | `D:/study/my-workspace/erp-workspace/jshERP/jshERP-boot` | 进销存 / 财务 / 权限的算法与数据模型参考；`docs/jsh_erp.sql` 是 32 表建表脚本 |
| 参考前端 | `D:/study/my-workspace/erp-workspace/jshERP/jshERP-web` | 单据页组织结构与菜单驱动路由的参考（Vue2 + ant-design-vue 1.x，代码不可直接移植） |

**已验证可用的外部服务**

- MySQL 8.0.46 @ 127.0.0.1:3306（库 `erp_db`）
- Redis @ 127.0.0.1:6379
- Swagger UI：`http://127.0.0.1:8082/swagger-ui.html`

---

## 部署与运维（生产环境）

> 本节为固定规则，后续沟通不再重复确认。

### 线上环境

| 项 | 值 |
|---|---|
| 云厂商 / 形态 | 腾讯云轻量应用服务器（控制台自带网站 / 数据库 / Docker / SSL / 终端 / 计划任务） |
| 公网 IP | `193.112.176.228` |
| 应用部署目录 | `/www/java/erp-boot`（另有 `/www/java/avatar` 等同级目录，属其他项目） |
| 运行方式 | **全部跑在 Docker 里**，不以裸 jar 方式常驻 |
| 发布方式 | 本地写脚本上传执行，通过 **deploy 命令**触发一次发布 |
| 域名 | `jiemids.com`（DNSPod 注册 + **EdgeOne 接管 NS**，`ns1/ns2.qeodns.com`） |

### 域名解析规则（重要）

`jiemids.com` 的权威 NS 是 `qeodns.com`，即 **EdgeOne 接管了 DNS**。

因此：

- **在 DNSPod「记录管理」里加的记录不会生效** —— 那个面板不向权威服务器下发数据。
- 正确做法是去 **EdgeOne 控制台 → 站点列表 → `jiemids.com` → 「DNS 记录」页签** 添加记录。
- **不要在腾讯云域名管理里点「修改 DNS 服务器」**，那会把 EdgeOne 的 NS 覆盖掉，直接打断线上访问。

各端子域名规划（**已确定**）：

| 用途 | 子域名 | 记录 | 状态 |
|---|---|---|---|
| **ERP 管理系统（Web 端）** | **`erp.jiemids.com`** | A → `193.112.176.228` | **已定，就是这个** |
| APP / 小程序接口 | `api.jiemids.com` | A → `193.112.176.228`（与 ERP 同源站，由 Nginx 按路径分流） | 待建 |

> `erp.jiemids.com` 是 **ERP 的 Web 管理端正式域名**，不是临时域名。
> 宝塔面板里已存在该站点的 nginx 配置（`/www/server/panel/vhost/nginx/erp.jiemids.com.conf`），
> 当前反代指向 `127.0.0.1:8082`，站点根目录 `/www/wwwroot/erp.jiemids.com`。
> ERP 前后端部署完成后，这个站点会同时承载：前端静态页面 + 后端 API（`/api/` 反代到 8082）。
>
> 现状提示：该站点当前访问返回 **404**（前端静态资源还没放进去），
> `/api/` 反代返回 **502**（8082 端口还没有服务在监听）。这两项都会在部署完成后自然恢复。

### 域名成本结论

**不需要额外购买域名。** 一个主域名可派生任意多个子域名，不额外收费、不需要单独备案。
子域名自动继承主域名的备案（同云厂商前提下）。唯一要花钱的是一张**通配符证书 `*.jiemids.com`**，覆盖全部子域名。

### 服务器数据库现状（2026-11-26 实测）

宝塔 MySQL **5.7.44-log**（不是 8.0），与电商项目**共用同一个实例**，库与账号各自独立。

| 项 | 值 |
|---|---|
| MySQL root 密码 | `7b9f162521890cc1`（宝塔面板 `config.mysql_root` 里读出来的；面板里旧的 `admin` 值已失效） |
| ERP 库 | `erp_db`（utf8mb4_general_ci） |
| ERP 账号 | `erp_db` / `erp_9f3c7a21d84b6e05`，host 为 `127.0.0.1` + `localhost` |
| 电商库 | `e_commerce_db` / `e_commerce_db` / `123456` |
| 建库方式 | **必须走宝塔面板的 `AddDatabase` API**，不能手工执行 `CREATE DATABASE` —— 手工建的库不会写进面板的 `databases` 表，面板列表里看不到，后续无法在面板上管理 |

**建库脚本**（在服务器上执行，面板 pyenv 的 python 才能 import 面板模块）：

```bash
cd /www/server/panel/pyenv/bin
./python3 - <<'PY'
import sys, os
sys.path.insert(0, '/www/server/panel'); sys.path.insert(0, '/www/server/panel/class')
os.chdir('/www/server/panel')
import database
class Get(dict):
    def __getattr__(self, k):
        try: return self[k]
        except KeyError: raise AttributeError(k)
    def __setattr__(self, k, v): self[k] = v
db = database.database()
print(db.AddDatabase(Get({
    'name': 'erp_db', 'db_user': 'erp_db',
    'password': 'erp_9f3c7a21d84b6e05', 'codeing': 'utf8mb4',
    'address': '127.0.0.1', 'ps': 'ERP 系统业务库', 'sid': 0,
})))
PY
```

> `Get` 必须是 **dict 子类**并同时支持 `getattr` / `setattr`：
> `AddDatabase` 内部有 `get['ps'] = ...` 的赋值，普通的自定义类会抛 `TypeError`；
> 一旦在写面板 SQLite 之前抛异常，**库已建但面板无记录**，需要手工 DROP 清理后重来。

### MySQL socket 被删导致宝塔面板爆红（已修复）

**现象**：宝塔「添加数据库」弹窗爆红 —— `127.0.0.1 状态：数据库连接失败，请检查 Mysql 是否正常运行`。

**根因**：`/etc/my.cnf` 把 socket 配在 `/tmp/mysql.sock`，mysqld 从 10-24 起一直没重启；
某个时刻该 socket 文件被删除，**进程仍在内核里 LISTEN 那个已删除的 inode**，
于是「文件不存在 + 监听还在」的诡异状态：

```
ss -lx | grep mysql        → u_str LISTEN ... /tmp/mysql.sock   ← 内核层面还在
ls -la /tmp/mysql.sock     → No such file or directory           ← 文件没了
```

宝塔的 `class/db_mysql.py` 在 `host == "localhost"` 时会**强制走 socket**：

```python
if self.__CONN_KWARGS["host"] == "localhost":
    myconf = public.readFile("/etc/my.cnf")
    socket_re = re.search(r"\nsocket\s*=\s*([^\n]+)", myconf)
    if socket_re:
        self.__CONN_KWARGS["unix_socket"] = socket_re.group(1)   # → 已失效的 /tmp/mysql.sock
```

所以面板连不上 → 爆红。**TCP 通路一直是好的**（`mysql -h127.0.0.1 --protocol=TCP` 正常）。

**修复**（已执行，电商中断约 5 秒）：

```bash
# 1. 加防删规则（保险；systemd-tmpfiles 的 D 规则本来只清目录不删文件，
#    但万一将来有带 age 的 f 规则，这条能挡住）
echo 'x /tmp/mysql.sock' > /etc/tmpfiles.d/keep-mysql-sock.conf

# 2. 重启 mysqld 重建 socket 文件
/etc/init.d/mysqld restart
```

验证：`mysql -uroot -e 'select 1'`（socket 路径）已恢复；面板 `GetDatabasesList` 能列出
`e_commerce_db` 与 `erp_db` 两个库。

**长期方案（已完成，2026-11-26）**：socket 已迁到 `/run/mysqld/mysqld.sock`。

```bash
# 1. 改配置（备份在 /etc/my.cnf.bak-socket-migrate）
sed -i 's|^socket\s*=.*|socket\t\t= /run/mysqld/mysqld.sock|' /etc/my.cnf   # 两行：client + mysqld 段

# 2. 目录持久化：/run 是 tmpfs，机器重启后目录会消失
cat > /etc/tmpfiles.d/mysql-sock-dir.conf <<'EOF'
d /run/mysqld 0755 mysql mysql -
EOF

# 3. 建目录 + 重启
mkdir -p /run/mysqld && chown mysql:mysql /run/mysqld
/etc/init.d/mysqld restart
```

**为什么宝塔会自动跟随**：`class/db_mysql.py` 在 `host == "localhost"` 时会从 `/etc/my.cnf`
正则读出 `socket=` 路径传给 pymysql，所以**只改配置即可，面板代码不用动**。

**为什么这样就彻底了**：`/run` 是 tmpfs（内存盘），`systemd-tmpfiles` 不按文件年龄清理它，
不会再出现「进程还在监听、socket 文件被删掉」的悬空状态。

迁移后实测：
- socket 落在 `/run/mysqld/mysqld.sock`，`/tmp/mysql.sock` 已不存在
- 删掉 `/run/mysqld` 目录后 `systemd-tmpfiles --create` 能重建，重启 mysqld 后 socket 正常恢复
- 面板 `CheckDatabaseStatus` 返回 `{'status': True, 'msg': '正常', 'db_status': True}`
- 面板数据库列表正常列出 `e_commerce_db` 与 `erp_db`
- 电商 11 个容器未受影响，`gateway` 与 `api.jiemids.com` 均返回 200

> 旧的 `/etc/tmpfiles.d/keep-mysql-sock.conf` 已随迁移删除（不再需要）。
> `/etc/my.cnf.bak-socket-migrate` 保留了改前原文件，**不带时间戳**。

### 镜像仓库（TCR）

| 项 | 值 |
|---|---|
| ERP 仓库 | `ccr.ccs.tencentyun.com/erp-boot/erp`（独立命名空间 `erp-boot`，仓库名 `erp`） |
| 镜像 tag | `erp-日期-git短SHA`，工作区有改动带 `-dirty` 后缀 |
| 登录凭证 | 本地与服务器都已登录 `ccr.ccs.tencentyun.com`，凭证对整个域名有效，**新仓库无需重新登录** |

> **与电商项目同账号但不会混**：TCR 账号下还有电商的 `jiemi-ecommerce` 命名空间（13 个仓库）。
> 镜像靠**完整路径**区分（`.../erp-boot/erp:erp-xxx` vs `.../jiemi-ecommerce/gateway:20261125-xxx`），
> 仓库名不同即互不相干。tag 加 `erp-` 前缀进一步隔离，
> `release.sh --rollback` 会**拒绝非 `erp-` 前缀的 tag**，防止误拉电商镜像。

> **磁盘注意**：服务器上电商镜像已占 7.2GB（37 个镜像，仅 14 个在用，3.6GB 可回收）。
> 每次发布都留一个 tag 会持续累积，发布后可跑 `docker image prune -f` 清理。

### 服务器上有两套 nginx（踩过坑）

实测发现服务器同时跑着两个 nginx master：

| nginx | master pid | 监听 80/443 | 配置目录 | include 路径 |
|---|---|---|---|---|
| 系统 nginx | 1117 | ❌ | `/etc/nginx/` | `/etc/nginx/conf.d/*.conf`、`sites-enabled/*` |
| **宝塔 nginx** | **68690** | ✅ **实际对外服务的** | `/www/server/nginx/conf/` | `/www/server/panel/vhost/nginx/*.conf` |

**踩坑过程**：改完宝塔站点配置后用 `nginx -t` / `nginx -s reload`，
结果操作的是**系统那套**（`/usr/sbin/nginx`），宝塔 nginx 压根没重载，
配置改了不生效，排查绕了一圈。

**正确命令**（必须带 `-c` 指定宝塔配置）：

```bash
/www/server/nginx/sbin/nginx -t    -c /www/server/nginx/conf/nginx.conf
/www/server/nginx/sbin/nginx -s reload -c /www/server/nginx/conf/nginx.conf
```

判断哪个在服务：`ss -lntp | grep -E ':80 |:443 '` 看 socket 归属的 pid。

### EdgeOne 与源站的缓存关系

链路：`浏览器 → 腾讯云 EdgeOne → 源站 nginx`。

- **源站返回 `Cache-Control: no-cache`，EdgeOne 仍然缓存** —— EdgeOne 的缓存规则由控制台配置决定，
  **不理会源站的 `no-cache`**。源站侧的 no-cache 对边缘无效。
- 边缘缓存的旧页面必须**在 EdgeOne 控制台手动刷新**，命令行刷不掉：
  - 站点 → **缓存刷新 → 整站刷新**（换 index.html 时）
  - 或 **URL 刷新**（单个文件）
- **命令行自查是否命中边缘缓存**：

```bash
curl -I https://erp.jiemids.com/ | grep -i "EO-Cache-Status\|Age"
# EO-Cache-Status: HIT + Age 很大 = 命中边缘缓存，需要去控制台刷新
```

- 加随机参数（`?_hc=时间戳`）会绕过边缘缓存打到源站，
  适合验证「源站是好的、线上是坏的」这类问题，但**不能替代刷新缓存**。

### 生产 MySQL 是 5.7，SQL 必须兼容 5.7

服务器 MySQL 版本 **5.7.44-log**（不是 8.0），窗口函数（`ROW_NUMBER() OVER`）**不支持**：

```sql
select 1 as x, count(*) over () as y;
→ ERROR 1064 (42000): ... near '() as y'
```

`doc/sql/product_phase1_seed.sql` 原来用窗口函数给属性值编号，
已改写成 **5.7 兼容的自连接计数**（结果与窗口函数版本一致）：

```sql
rn  = (比它 id 小的同组值个数) + 1
cnt = (同组值的总个数)
```

> 写 SQL 时不要默认服务器是 8.0。电商项目同样是 5.7。

### 容器连 MySQL 的账号授权范围

Docker 容器是以**网段 IP**（如 `172.20.0.3`）连 MySQL 的，
不是 `localhost`。账号只授权 `localhost` / `127.0.0.1` 会导致：

```
Access denied for user 'erp_db'@'172.20.0.3' (using password: YES)
```

**必须授权 `'用户'@'%'`**（或对应网段）。宝塔面板建库时「访问权限」选「本地服务器」
只建了 `localhost` 账号，容器连不上，要手工补：

```sql
CREATE USER IF NOT EXISTS 'erp_db'@'%' IDENTIFIED BY '密码';
GRANT ALL PRIVILEGES ON erp_db.* TO 'erp_db'@'%';
FLUSH PRIVILEGES;
```

### SSL 证书复用（通配符）

`jiemids.com` 站点用的是 **CloudFlare Origin CA 通配符证书**：

```
subject = CloudFlare Origin Certificate
SAN     = *.jiemids.com, jiemids.com
有效期  = 2026-09-22 ~ 2041-09-18
```

`*.jiemids.com` **天然覆盖 `erp.jiemids.com`**，不用单独申请证书，
把文件复制到站点目录即可：

```bash
cp /www/server/panel/vhost/cert/jiemids.com/fullchain.pem \
   /www/server/panel/vhost/cert/erp.jiemids.com/fullchain.pem
cp /www/server/panel/vhost/cert/jiemids.com/privkey.pem \
   /www/server/panel/vhost/cert/erp.jiemids.com/privkey.pem
```

> EdgeOne 边缘侧也已经有 `erp.jiemids.com` 的证书（免费证书，已部署，自动续期到 15 天前）。
> 边缘证书与源站证书是**两套**，各管各的：边缘给用户看的，源站给 EdgeOne 回源看的。

### 部署规范（已落地，见 `deploy/` 目录）

| 文件 | 职责 |
|---|---|
| `deploy/Dockerfile` | ERP 后端镜像，构建上下文是 `target/`（里面只有一个 jar） |
| `deploy/docker-compose.yml` | 生产编排：`erp-boot`（127.0.0.1:8082）+ `erp-redis`（127.0.0.1:16380），独立网络 `erp-net` |
| `deploy/.env.example` | 环境变量模板，复制成 `.env` 后填真实值（`.env` 已 gitignore） |
| `deploy/release.sh` | 后端发布：Maven 打包 → 构建镜像 → 推 TCR → 服务器 pull → compose 重启。支持 `--dry-run` / `--no-build` / `--no-deploy` / `--rollback <tag>` |
| `deploy/release-web.sh` | 前端发布：npm build → tar 上传 → rsync 到 `/www/wwwroot/erp.jiemids.com`。支持 `--dry-run` / `--no-build` / `--rollback` |
| `deploy/run-sql-prod.sh` | 在服务器上执行 SQL 脚本 |
| `deploy/nginx-erp.conf` | 宝塔站点配置参考（前端静态 + `/api/` 反代 8082 + Swagger 关闭 + SPA fallback） |
| `deploy/sql/` | 数据库变更脚本，按日期命名 |

发布规则：

- **服务器只 pull 不 build**（服务器内存偏紧，编译会压垮它）；构建在本地做。
- TAG 格式 `日期-git短SHA`，工作区有改动带 `-dirty` 后缀。
- 备份文件名**禁止带时间戳**（避免越积越多）；数据库变更脚本按日期命名归档到 `deploy/sql/`。
- 服务器配置文件的备份同样不带时间戳。
- 同业务域的 Nginx 路由规则集中分组并加注释，不允许分散堆放。

### 生产构建的关键点（已验证）

| 事项 | 做法 | 原因 |
|---|---|---|
| **本地凭据不能进 jar** | `pom.xml` 加 `prod` profile，`<resources>` 排除 `application-dev.yml`；`release.sh` 用 `mvn -Pprod package` | `application-dev.yml` 里有真实数据库密码与 JWT 密钥，位于 `src/main/resources` 下会被打进 jar。**注意：spring-boot-maven-plugin 的 `<excludes>` 是排除依赖的，不能用它排除资源** |
| **必须钉死 prod profile** | Dockerfile 里 `ENV SPRING_PROFILES_ACTIVE=prod` | `application.yml` 的 `spring.profiles.default=dev` 只在没指定时生效；生产的 jar 又没有 `application-dev.yml`，回落到 dev 会导致 SQL 打印、异常外泄 |
| **Redis 密码** | `application.yml` 的 `spring.data.redis.password` 读 `${REDIS_PASSWORD:}`，空值表示无密码 | 本地 dev 无密码、生产容器有密码，一份配置两边都能用 |
| **未知路径返回 404** | `GlobalExceptionHandler` 加 `NoResourceFoundException` 处理 | 兜底 handler 会把 404 吃成 500，导致 nginx 的 `return 404` 规则形同虚设；生产关掉 Swagger 后被拦下的 `/v3/api-docs` 就会踩到 |
| **Swagger 生产关闭** | `application-prod.yml` 里 `springdoc.api-docs.enabled=false` + `swagger-ui.enabled=false` | 已在容器内实测：`/v3/api-docs`、`/swagger-ui/index.html` 均返回 404 |

已实测通过的验证链：`mvn -Pprod package` → jar 内只有 `application.yml` + `application-prod.yml`（无 dev）→
`docker build` → 容器启动 → prod profile 生效 → Swagger 404 → 未知路径 404 → `docker compose config` 语法正确 →
三个 bash 脚本 `bash -n` 语法检查通过 → `release.sh --dry-run` 跑通。

### 服务器现状备忘（2026-11-26 实测）

| 项 | 值 |
|---|---|
| 系统 | Ubuntu 24.04.4 LTS |
| Docker | 29.9.1 / Compose v5.6.0 |
| 规格 | 2 核 / 7.5G 内存（已用 4.2G，可用仅 3.4G）/ 99G 磁盘（已用 22G） |
| 现有容器 | 电商的 11 个（9 Java + nacos + redis），**尚无 erp** |
| Docker 网络 | `java_my-net`（172.19.0.0/16，电商在用）、`bridge`（172.17.0.0/16） |
| 宝塔 nginx | 已在跑；`erp.jiemids.com` 站点配置已存在，反代 `127.0.0.1:8082` |
| 宝塔面板 | 39999 端口，账号 `admin` / 密码 `7b9f162521890cc1` |
| SSH | 别名 `ecommerce`（root@193.112.176.228，密钥 `~/.ssh/ecommerce.pem`） |

> ERP 容器使用**独立网络 `erp-net`**，不挤进 `java_my-net`：
> 电商已经占掉 4.2G 内存，ERP 再加两个容器会逼近上限。

---

## 视觉/浏览器发现

本轮无图片与浏览器会话；全部结论来自代码实测、配置元数据核对，以及对 `jshERP-boot` / `jshERP-web` 的只读源码探索。
