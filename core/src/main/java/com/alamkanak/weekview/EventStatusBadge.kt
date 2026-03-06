package com.alamkanak.weekview

import kotlin.math.max

internal object EventStatusBadge {

    private const val defaultColor: Int = 0xFF4CAF50.toInt()

    private const val textSizeScale = 0.65f
    private const val horizontalMarginScale = 0.35f
    private const val verticalPaddingScale = 0.35f

    private const val minimumHorizontalMargin = 1f
    private const val minimumVerticalPadding = 1f
    private const val minimumBottomSpacing = 1f
    private const val minimumTextSpacing = 1f

    fun text(event: ResolvedWeekViewEntity): String? {
        return event.style.statusString
            ?.toString()
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
    }

    fun color(event: ResolvedWeekViewEntity): Int {
        return event.style.statusColor ?: defaultColor
    }

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

    fun verticalPadding(viewState: ViewState): Float {
        val unscaled = viewState.eventPaddingVertical * verticalPaddingScale
        return max(minimumVerticalPadding, unscaled)
    }

    fun horizontalMargin(viewState: ViewState): Float {
        val unscaled = viewState.eventPaddingHorizontal * horizontalMarginScale
        return max(minimumHorizontalMargin, unscaled)
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
        if (text(event) == null) {
            return 0f
        }

        val badgeHeight = textSize(viewState, event) + verticalPadding(viewState) * 2
        return badgeHeight + bottomSpacing(viewState) + textSpacing(viewState)
    }
}
