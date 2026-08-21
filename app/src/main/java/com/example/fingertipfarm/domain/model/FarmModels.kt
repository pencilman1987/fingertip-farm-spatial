package com.example.fingertipfarm.domain.model

enum class CatalogKind {
    CROP,
    FISH,
    PARTNER,
}

enum class PlotType {
    SOIL,
    POND,
}

enum class PlotStatus {
    EMPTY,
    GROWING,
    READY,
}

enum class FarmTheme {
    DAY,
    NIGHT,
}

enum class SpatialExperienceMode(val label: String) {
    WRITING("写作空间"),
    SCRAPBOOK("手账空间"),
    REVIEW("回顾空间"),
}

enum class SpatialAmbience(val label: String) {
    DAWN("晨光"),
    DAY("日光"),
    DUSK("暮色"),
    NIGHT("夜色"),
}

data class SpatialAmbienceState(
    val ambience: SpatialAmbience,
    val sourceLabel: String,
)

enum class WorkshopInputMode {
    ENGLISH,
    CHINESE,
}

enum class JournalMood(val label: String, val symbol: String) {
    CALM("平静", "○"),
    JOYFUL("开心", "☺"),
    TIRED("疲惫", "~"),
    THOUGHTFUL("沉思", "…"),
    GRATEFUL("感恩", "♡"),
}

enum class JournalBorderStyle(val label: String) {
    CLASSIC("素纸"),
    LEAF("叶脉"),
    SKY("晴空"),
    NIGHT("星夜"),
}

enum class JournalPageTheme(val label: String) {
    CREAM("奶油"),
    FOREST("森林"),
    SKY("天空"),
    NIGHT("夜读"),
}

enum class FarmSeason(val label: String, val gardenSymbol: String) {
    SPRING("春日花园", "芽"),
    SUMMER("夏日花园", "叶"),
    AUTUMN("秋日花园", "果"),
    WINTER("冬日花园", "籽"),
}

data class CatalogItem(
    val id: String,
    val name: String,
    val emoji: String,
    val kind: CatalogKind,
    val price: Int,
    val revenue: Int = 0,
    val growthNeeded: Int = 0,
    val description: String = "",
)

data class PlotState(
    val id: String,
    val type: PlotType,
    val contentId: String? = null,
    val growth: Int = 0,
    val count: Int = 0,
    val status: PlotStatus = PlotStatus.EMPTY,
)

data class JournalEntry(
    val id: String,
    val dateKey: String,
    val title: String,
    val body: String,
    val cropId: String,
    val linkedPlotId: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val rewardedMilestones: Int = 0,
    val completedAtMillis: Long? = null,
    val mood: JournalMood = JournalMood.CALM,
    val tags: Set<String> = emptySet(),
    val dailyPrompt: String = "",
    val stickerIds: Set<String> = emptySet(),
    val borderStyle: JournalBorderStyle = JournalBorderStyle.CLASSIC,
    val pageTheme: JournalPageTheme = JournalPageTheme.CREAM,
    val localImageUri: String? = null,
)

data class MemoryCard(
    val id: String,
    val journalEntryId: String,
    val harvestedAtMillis: Long,
)

data class ReflectionGardenSummary(
    val season: FarmSeason,
    val seasonEntryCount: Int,
    val seasonMemoryCount: Int,
    val weeklyEntryCount: Int,
    val weeklyCompletedCount: Int,
    val weeklyMemoryCount: Int,
    val weeklyCharacterCount: Int,
    val gentleMessage: String,
)

data class PartnerAbilityNote(
    val partnerId: String,
    val partnerName: String,
    val symbol: String,
    val message: String,
)

data class FarmGameState(
    val gold: Int,
    val activeCropId: String,
    val activeFishId: String,
    val unlockedCropIds: Set<String>,
    val unlockedFishIds: Set<String>,
    val ownedPartnerIds: Set<String>,
    val plots: List<PlotState>,
    val journalEntries: List<JournalEntry>,
    val activeJournalEntryId: String?,
    val memoryCards: List<MemoryCard>,
    val typedCharacterTotal: Int,
    val timerSeconds: Int,
    val timerRunning: Boolean,
    val theme: FarmTheme,
    val workshopInputMode: WorkshopInputMode,
)

object FarmCatalog {
    val crops = listOf(
        CatalogItem(
            id = "carrot",
            name = "胡萝卜",
            emoji = "🥕",
            kind = CatalogKind.CROP,
            price = 0,
            revenue = 10,
            growthNeeded = 15,
        ),
        CatalogItem(
            id = "tomato",
            name = "番茄",
            emoji = "🍅",
            kind = CatalogKind.CROP,
            price = 100,
            revenue = 35,
            growthNeeded = 35,
        ),
        CatalogItem(
            id = "watermelon",
            name = "西瓜",
            emoji = "🍉",
            kind = CatalogKind.CROP,
            price = 500,
            revenue = 150,
            growthNeeded = 100,
        ),
    )

    val fish = listOf(
        CatalogItem(
            id = "goldfish",
            name = "金鱼",
            emoji = "🐠",
            kind = CatalogKind.FISH,
            price = 0,
            revenue = 8,
            growthNeeded = 12,
        ),
        CatalogItem(
            id = "tropical",
            name = "热带鱼",
            emoji = "🐠",
            kind = CatalogKind.FISH,
            price = 200,
            revenue = 30,
            growthNeeded = 20,
        ),
        CatalogItem(
            id = "shark",
            name = "大鲨鱼",
            emoji = "🦈",
            kind = CatalogKind.FISH,
            price = 1500,
            revenue = 450,
            growthNeeded = 50,
        ),
    )

    val partners = listOf(
        CatalogItem(
            id = "sheep",
            name = "小羊",
            emoji = "🐑",
            kind = CatalogKind.PARTNER,
            price = 600,
            description = "逐行巡视农田，每 3 秒自动收获一株成熟作物",
        ),
        CatalogItem(
            id = "cat",
            name = "猫咪",
            emoji = "🐱",
            kind = CatalogKind.PARTNER,
            price = 1200,
            description = "守在鱼塘旁，每 30 秒自动钓起一条鱼",
        ),
        CatalogItem(
            id = "pig",
            name = "小猪助手",
            emoji = "🐷",
            kind = CatalogKind.PARTNER,
            price = 1000,
            description = "每天带来一个温柔的小问题，不催促回答",
        ),
        CatalogItem(
            id = "llama",
            name = "羊驼",
            emoji = "🦙",
            kind = CatalogKind.PARTNER,
            price = 1500,
            description = "在手账工具里带来额外的季节贴纸",
        ),
    )

    val allItems: List<CatalogItem> = crops + fish + partners

    fun find(id: String): CatalogItem? = allItems.firstOrNull { it.id == id }
}

object FarmDefaults {
    const val FocusDurationSeconds = 25 * 60

    val KeyboardLayout =
        listOf(
            listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "-", "="),
            listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P", "[", "]"),
            listOf("A", "S", "D", "F", "G", "H", "J", "K", "L", ";", "'", "ENTER"),
            listOf("Z", "X", "C", "V", "B", "N", "M", ",", ".", "/"),
            listOf("SPACE"),
        )

    val SheepPatrolPlotIndices: List<Int> =
        buildList {
            var rowStart = 0
            KeyboardLayout.forEachIndexed { rowIndex, row ->
                val indices = row.indices.map { rowStart + it }
                addAll(if (rowIndex % 2 == 0) indices else indices.reversed())
                rowStart += row.size
            }
        }

    fun initialState(): FarmGameState =
        FarmGameState(
            gold = 80,
            activeCropId = "carrot",
            activeFishId = "goldfish",
            unlockedCropIds = setOf("carrot"),
            unlockedFishIds = setOf("goldfish"),
            ownedPartnerIds = emptySet(),
            plots = KeyboardLayout.flatten().mapIndexed { index, key ->
                PlotState(
                    id = "plot_${index + 1}",
                    type = if (key == "ENTER" || key == "SPACE") PlotType.POND else PlotType.SOIL,
                )
            },
            journalEntries = emptyList(),
            activeJournalEntryId = null,
            memoryCards = emptyList(),
            typedCharacterTotal = 0,
            timerSeconds = FocusDurationSeconds,
            timerRunning = false,
            theme = FarmTheme.DAY,
            workshopInputMode = WorkshopInputMode.ENGLISH,
        )
}

object WorkshopPinyin {
    private val entries =
        mapOf(
            "jintian" to listOf("今天"),
            "jihua" to listOf("计划"),
            "nongchang" to listOf("农场"),
            "jilu" to listOf("记录"),
            "linggan" to listOf("灵感"),
            "wancheng" to listOf("完成"),
            "gongzuo" to listOf("工作"),
            "xuexi" to listOf("学习"),
            "shouhuo" to listOf("收获"),
            "bozhong" to listOf("播种"),
            "jiaoshui" to listOf("浇水"),
            "beiwang" to listOf("备忘"),
            "renwu" to listOf("任务"),
            "kaishi" to listOf("开始"),
            "jieshu" to listOf("结束"),
            "zaoshang" to listOf("早上"),
            "wanshang" to listOf("晚上"),
            "keyi" to listOf("可以"),
            "xuyao" to listOf("需要"),
        )

    fun candidates(pinyin: String): List<String> = entries[pinyin.lowercase()].orEmpty()
}
