package com.example.fingertipfarm.domain.usecase

import java.time.LocalDate

class DailyPromptUseCase {
    operator fun invoke(date: LocalDate): String = Prompts[Math.floorMod(date.toEpochDay(), Prompts.size.toLong()).toInt()]

    private companion object {
        val Prompts =
            listOf(
                "今天有什么小事，让你愿意停留一下？",
                "此刻的你，最需要怎样的照顾？",
                "今天哪一刻比想象中更轻松？",
                "有什么念头，值得先放在这里？",
                "如果今天是一种天气，它会是什么？",
                "今天的你，为自己做了什么？",
                "最近有什么正在慢慢变好？",
                "今天想感谢谁，或者感谢什么？",
                "此刻身体的哪个地方最需要放松？",
                "今天学到的一件小事是什么？",
                "有什么事情不必急着完成？",
                "今天想留给未来自己的哪句话？",
                "最近一次真心微笑发生在什么时候？",
                "明天醒来，你希望保留今天的什么？",
            )
    }
}
