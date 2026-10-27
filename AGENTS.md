# 指尖农场·空间版

## 项目定位

这是从 Web 桌面游戏《指尖农场》迁移而来的 PICO OS 6 空间应用。当前版本保留农场运营循环，并加入“键盘农场”核心玩法：手势、手柄射线、PICO 实体键盘与系统输入法共享同一套键位状态机；空键播种、成长键增加 1 点进度、成熟键收获，`SPACE` / `ENTER` 经营鱼塘。

## 空间架构

- Spatial SDK BOM: `0.13.3`
- 根容器：平面 `DefaultWindowContainer` / Shared Space；可与其他应用窗口并存
- 窗口结构：单主窗口包含农场/日记主面板与相框、记忆果实、日记植物记忆条；仓库/集市按原作以主面板内弹窗房间呈现
- 根节点由 `PicoTheme` 包裹
- WindowContainer 根节点使用系统默认 `Material.Regular` 玻璃，不额外绘制实色背景；内部面板可使用 SpatialUI Material 玻璃
- 所有 2D UI 必须使用 `com.pico.spatial.ui.*` / SpatialUI，禁止 Material / Material3
- 自定义可点击农田格必须保留 `spatialHoverEffect` + `LocalIndication` + `controllerHapticFeedback`

## 视觉基准

- 视觉权威来源：`../reference-web/frontend/public/css/styles.css` 与原始 Web 包插画
- 固定品牌色：棕色土壤、绿色成长键帽、蓝色鱼塘、深浅木色、奶油白、CRT 黑绿；定义于 `ui/theme/FarmVisualTokens.kt`
- 原始插画已本地化到 `app/src/main/res/drawable-nodpi/`，禁止用通用占位图替换
- 主农场保留天空草地场景与键帽农田；工坊保留厚框 CRT 与等距键盘母题
- 首页和工作间都使用原作 47 键布局；`ENTER` / `SPACE` 为鱼塘
- 工作间与首页必须直接读取同一组 `FarmGameState.plots`；工坊手势键通过地块 id 更新对应农田，禁止再用总成长比例推算前 N 个绿色键帽
- 首页实体键盘事件必须按 47 键布局映射到同一个地块 id；系统输入法中的拉丁字符按键位映射，无法映射的已提交字符按“成长中 → 空地 → 成熟地”的稳定顺序回退
- 记忆工坊支持多篇日记：每篇拥有日期、标题、正文、当前作物和独占农田；系统 IME 与手势键盘双通道继续共用统一键位状态机
- 新建日记自动占用第一块空农田并播下当前作物；每 20 个新增非空白字符给关联作物 1 点里程碑成长，单次最多 3 点；完成日记一次性增加 3 点
- 日记完成后，其关联成熟作物才能被收获；收获会生成可重新打开原文的记忆卡。已完成与已收获日记仍可补写并自动保存
- `WorkshopInputMode` 随存档保存；拼音组合串仅属于 `FarmUiState` 临时状态；系统 IME 组合阶段不能驱动农场，候选真正上屏后只能计数一次
- 手势射线键帽使用 SpatialUI `clickable` + `spatialHoverEffect` + indication + controller haptics；平面 Compose 键不使用 3D 碰撞手势 API
- 全局 `LocalIndication` 使用 `FarmSilentIndication`：保留按压高亮、悬停与手柄触觉反馈，但禁止默认点击音效，避免键盘连续输入产生噪音
- 首页右上角太阳/月亮可切换昼夜插画，`FarmTheme` 必须随游戏状态写入本地存档
- 金币闭环按原作：收获作物/鱼类增加金币，集市解锁扣除金币；新存档初始 80G，已有存档金币原样保留。WPM 仅作为即时输入反馈，不再提供成长倍率
- 日记支持心情、最多 3 个标签、每日问题、贴纸、边框、页面主题与单张本地图片；所有字段随日记自动保存并兼容旧存档
- 日历花园按真实月份显示记录日期；每周回顾与季节成长只累计书写、完成与记忆收获，不设置断签、枯萎、死亡、补签或缺席惩罚
- 合伙人能力：小羊使用透明角色素材沿 47 键蛇形路线每 3 秒巡逻，并只收获当前落点上可收获的成熟作物（未完成日记的关联作物受保护）；猫咪守在鱼塘并在每 30 秒实际钓起一条鱼时播放三段抛竿动画；小猪提供每日问题；羊驼扩展季节贴纸
- 首页 HUD 不再显示遮挡背景标题的品牌木牌和图标气泡，只保留顶部居中的文本状态带
- 主窗口提供写作、手账、回顾三种应用状态；模式切换由 `SpatialExperienceMode` 统一驱动，禁止只做视觉假切换
- 昼夜氛围由真实本地时间与当前日记心情共同派生：强心情优先，平静或无心情时跟随时间
- 记忆果实、相框、日记植物使用 `drawable-nodpi/spatial_*.png` 透明素材，在主窗口记忆条中保留 `spatialHoverEffect`；禁止用 emoji 替代

## 主要文件

- `app/src/main/java/com/example/fingertipfarm/Main.kt`：空间根容器入口，保持精简
- `platform/`：`SpatialApplication` 与 `LaunchActivity`
- `domain/model/FarmModels.kt`：品种、地块、存档模型与默认数据
- `domain/usecase/`：地块交互、购买、品种选择和打字生长规则
- `data/repository/`：存档接口与 SharedPreferences 本地实现
- `ui/farm/FarmViewModel.kt`：单向状态、事件与专注计时
- `ui/farm/components/FarmGrid.kt`：农场场景、47 键布局以及与伙伴机制同步的小羊巡逻/猫咪抛竿动画
- `ui/farm/SpatialFarmExperience.kt`：Shared Space 主窗口状态、氛围、主面板与记忆条
- `ui/farm/components/WorkshopContent.kt`：写作优先的记忆工坊、日期归档、记忆卡、折叠式 QWERTY 手势键盘与快捷中文词
- `domain/usecase/CreateJournalEntryUseCase.kt`：为新日记分配空农田并播下关联作物
- `domain/usecase/ApplyJournalWritingRewardUseCase.kt`：按非空白字符里程碑推动关联作物成长
- `domain/usecase/CompleteJournalEntryUseCase.kt`：完成日记与一次性成长奖励
- `domain/usecase/CreateMemoryCardUseCase.kt`：关联作物收获后生成记忆卡
- `domain/usecase/ApplyTypingGrowthUseCase.kt`：把系统输入的已提交字符映射到 47 键农田，并复用统一地块交互状态机
- `ui/farm/components/FarmIllustratedHeader.kt`：仓库/集市等原作插画标题组件
- `ui/farm/components/FarmRoomOverlay.kt`：原作奶油色仓库/集市弹窗房间
- `ui/theme/FarmVisualTokens.kt`：从原始 CSS 提取的固定装饰色；每个色值带 design-style 来源注解
- `ui/theme/FarmSilentIndication.kt`：无声的按压视觉反馈，替代 SpatialUI 默认点击音
- `drawable-nodpi/partner_sheep_patrol.png`、`partner_cat_fishing.png`：ImageGen 生成并去除键色背景的伙伴透明素材
- `ui/farm/components/`：侧栏、农场、仓库、集市和工坊 SpatialUI 组件
- `.scratch/`：Anything-to-Spatial 工作流的输入、证据、假设、布局与验证产物

## 当前暂缓功能

- 小猪联网 AI 问答（当前为本地每日问题）
- 账户登录与云同步
- 手账图片导出
- 环境锚点、房间网格和全虚拟天空盒

## 构建与验证

```bash
./gradlew testDebugUnitTest assembleDebug
./gradlew installDebug
```

连接 PICO 设备或启动模拟器后，运行 `connectedAndroidTest`。新增 Compose UI 后还必须运行 SpatialUI design-style verifier，且错误数必须为 0。

## 后续演进

优先在当前 Shared Space 的 `DefaultWindowContainer` 架构内迭代。只有用户明确要求全沉浸环境、真实平面锚定或房间网格时，才重新评估 Stage。
