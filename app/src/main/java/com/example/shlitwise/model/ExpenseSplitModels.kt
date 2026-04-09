package com.example.shlitwise.model

enum class SplitType {
    EQUAL
}

enum class SingleParticipantSplitOption {
    YOU_PAID_SPLIT_EQUALLY,
    YOU_ARE_OWED_FULL_AMOUNT,
    OTHER_PAID_SPLIT_EQUALLY,
    OTHER_IS_OWED_FULL_AMOUNT
}

sealed class ExpenseSplitSelection {

    data class SingleParticipant(
        val option: SingleParticipantSplitOption = SingleParticipantSplitOption.YOU_PAID_SPLIT_EQUALLY
    ) : ExpenseSplitSelection()

    data class MultiParticipant(
        val paidByUserId: Long? = null,
        val paidByDisplayName: String = "You",
        val splitType: SplitType = SplitType.EQUAL
    ) : ExpenseSplitSelection()
}

data class PayerOption(
    val userId: Long?,
    val displayName: String
)

fun SingleParticipantSplitOption.toDisplayText(otherUserName: String): String {
    return when (this) {
        SingleParticipantSplitOption.YOU_PAID_SPLIT_EQUALLY ->
            "You paid, Split equally"

        SingleParticipantSplitOption.YOU_ARE_OWED_FULL_AMOUNT ->
            "You are owed the full amount"

        SingleParticipantSplitOption.OTHER_PAID_SPLIT_EQUALLY ->
            "$otherUserName paid, Split equally"

        SingleParticipantSplitOption.OTHER_IS_OWED_FULL_AMOUNT ->
            "$otherUserName is owed the full amount"
    }
}