package com.alamkanak.weekview

import kotlin.math.max

internal object EventStatusBadge {

    const val text: String = "PAID"

    private const val textSizeScale = 0.65f
    private const val horizontalPaddingScale = 0.75f
    private const val verticalPaddingScale = 0.35f

    private const val minimumHorizontalPadding = 2f
    private const val minimumVerticalPadding = 1f
    private const val minimumBottomSpacing = 1f
    private const val minimumTextSpacing = 1f

    fun textSize(
        viewState: ViewState,
        event: ResolvedWeekViewEntity
    ): Float {
        val baseTextSize = if (event.isAllDay) {
            viewState.allDayEventTextPaint.textSize
        } else {
            viewState.eventTextPaint.textSize
        }
        return baseTextSize * textSizeScale
    }

    fun horizontalPadding(viewState: ViewState): Float {
        val unscaled = viewState.eventPaddingHorizontal * horizontalPaddingScale
        return max(minimumHorizontalPadding, unscaled)
    }

    fun verticalPadding(viewState: ViewState): Float {
        val unscaled = viewState.eventPaddingVertical * verticalPaddingScale
        return max(minimumVerticalPadding, unscaled)
    }

    fun bottomSpacing(viewState: ViewState): Float {
        return max(minimumBottomSpacing, viewState.eventPaddingVertical.toFloat())
    }

    fun textSpacing(viewState: ViewState): Float {
        return max(minimumTextSpacing, verticalPadding(viewState))
    }

    fun reservedHeight(
        viewState: ViewState,
        event: ResolvedWeekViewEntity
    ): Float {
        val badgeHeight = textSize(viewState, event) + verticalPadding(viewState) * 2
        return badgeHeight + bottomSpacing(viewState) + textSpacing(viewState)
    }
}
