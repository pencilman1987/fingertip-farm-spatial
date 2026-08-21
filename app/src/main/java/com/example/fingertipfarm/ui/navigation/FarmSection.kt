package com.example.fingertipfarm.ui.navigation

enum class FarmSection(
    val title: String,
    val subtitle: String,
) {
    FARM("农场", "播种、照料与收获"),
    INVENTORY("仓库", "选择已解锁品种"),
    MARKET("集市", "购买新品种与伙伴"),
    WORKSHOP("键盘工坊", "打字、笔记与作物成长"),
}
