package com.dongbantest.app.data.repository

/** 동승자 대화 유도 질문 (정적 목록, AI API 미사용). */
object QuestionBank {

    val questions: List<String> = listOf(
        "오늘 같이 차에 탄 사람들에게 한마디 해볼까요?",
        "오늘 여행에서 가장 기대되는 순간은?",
        "우리가 처음 함께 여행했던 날 기억나?",
        "오늘 가장 즐거웠던 순간은?",
        "다음에는 우리 어디로 떠나볼까?",
        "우리 가족(친구)만의 여행 노래를 하나 정한다면?",
        "지금 창밖에서 보이는 것 중 가장 마음에 드는 건?",
        "함께했던 여행 중 가장 웃겼던 일은?",
        "이번 여행에서 꼭 먹고 싶은 음식은?",
        "옆 사람에게 고마웠던 순간을 이야기해 볼까요?",
        "10년 뒤에 우리 또 어디를 함께 가고 있을까?",
        "오늘 여행을 한 단어로 표현한다면?",
    )

    /** [current] 와 다른 질문을 무작위로 고른다. */
    fun next(current: String?): String {
        val candidates = questions.filter { it != current }
        return candidates.random()
    }
}
