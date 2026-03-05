package com.alamkanak.weekview

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.StaticLayout
import kotlin.math.max

internal class EventChipDrawer(
    private val viewState: ViewState
) {

    private val backgroundPaint = Paint()
    private val borderPaint = Paint()

    private val patternPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val statusBadgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val statusTextPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.LINEAR_TEXT_FLAG).apply {
        style = Paint.Style.FILL
        color = 0xFFFFFFFF.toInt()
        textAlign = Paint.Align.CENTER
    }

    internal fun draw(
        eventChip: EventChip,
        canvas: Canvas,
        textLayout: StaticLayout?
    ) {
        canvas.drawInBounds(eventChip.bounds) {
            val event = eventChip.event
            val bounds = eventChip.bounds
            val statusBadge = createStatusBadge(eventChip)

            val cornerRadius = event.style.cornerRadius?.toFloat() ?: viewState.eventCornerRadius.toFloat()
            updateBackgroundPaint(event, backgroundPaint)
            drawRoundRect(bounds, cornerRadius, cornerRadius, backgroundPaint)

            val pattern = event.style.pattern
            if (pattern != null) {
                drawPattern(
                    pattern = pattern,
                    bounds = eventChip.bounds,
                    isLtr = viewState.isLtr,
                    paint = patternPaint
                )
            }

            val borderWidth = event.style.borderWidth
            if (borderWidth != null && borderWidth > 0) {
                updateBorderPaint(event, borderPaint)
                val borderBounds = bounds.insetBy(borderWidth / 2f)
                drawRoundRect(borderBounds, cornerRadius, cornerRadius, borderPaint)
            }

            val originalEvent = eventChip.originalEvent
            if (originalEvent.isMultiDay && originalEvent.isNotAllDay) {
                drawCornersForMultiDayEvents(eventChip, cornerRadius)
            }

            if (textLayout != null) {
                drawEventTitle(eventChip, textLayout, statusBadge?.bounds?.top)
            }

            if (statusBadge != null) {
                drawStatusBadge(statusBadge)
            }
        }
    }

    private fun Canvas.drawCornersForMultiDayEvents(
        eventChip: EventChip,
        cornerRadius: Float
    ) {
        val event = eventChip.event
        val originalEvent = eventChip.originalEvent
        val bounds = eventChip.bounds

        updateBackgroundPaint(event, backgroundPaint)

        if (event.startsOnEarlierDay(originalEvent)) {
            val topRect = RectF(bounds)
            topRect.bottom = topRect.top + cornerRadius
            drawRect(topRect, backgroundPaint)
        }

        if (event.endsOnLaterDay(originalEvent)) {
            val bottomRect = RectF(bounds)
            bottomRect.top = bottomRect.bottom - cornerRadius
            drawRect(bottomRect, backgroundPaint)
        }

        if (event.style.borderWidth != null) {
            drawMultiDayBorderStroke(eventChip, cornerRadius)
        }
    }

    private fun Canvas.drawMultiDayBorderStroke(
        eventChip: EventChip,
        cornerRadius: Float
    ) {
        val event = eventChip.event
        val originalEvent = eventChip.originalEvent
        val bounds = eventChip.bounds

        val borderWidth = event.style.borderWidth ?: 0
        val borderStart = bounds.left + borderWidth / 2
        val borderEnd = bounds.right - borderWidth / 2

        updateBorderPaint(event, backgroundPaint)

        if (event.startsOnEarlierDay(originalEvent)) {
            drawVerticalLine(
                horizontalOffset = borderStart,
                startY = bounds.top,
                endY = bounds.top + cornerRadius,
                paint = backgroundPaint
            )

            drawVerticalLine(
                horizontalOffset = borderEnd,
                startY = bounds.top,
                endY = bounds.top + cornerRadius,
                paint = backgroundPaint
            )
        }

        if (event.endsOnLaterDay(originalEvent)) {
            drawVerticalLine(
                horizontalOffset = borderStart,
                startY = bounds.bottom - cornerRadius,
                endY = bounds.bottom,
                paint = backgroundPaint
            )

            drawVerticalLine(
                horizontalOffset = borderEnd,
                startY = bounds.bottom - cornerRadius,
                endY = bounds.bottom,
                paint = backgroundPaint
            )
        }
    }

    private fun Canvas.drawEventTitle(
        eventChip: EventChip,
        textLayout: StaticLayout,
        statusBadgeTop: Float?
    ) {
        val bounds = eventChip.bounds

        val horizontalOffset = if (viewState.isLtr) {
            bounds.left + viewState.eventPaddingHorizontal
        } else {
            bounds.right - viewState.eventPaddingHorizontal
        }

        val availableBottom = statusBadgeTop?.minus(EventStatusBadge.textSpacing(viewState)) ?: bounds.bottom
        val availableTextHeight = availableBottom - bounds.top
        if (availableTextHeight <= 0f) {
            return
        }

        val verticalOffset = if (eventChip.event.isAllDay) {
            max(viewState.eventPaddingVertical.toFloat(), (availableTextHeight - textLayout.height) / 2f)
        } else {
            viewState.eventPaddingVertical.toFloat()
        }

        val clipBounds = RectF(bounds.left, bounds.top, bounds.right, availableBottom)
        drawInBounds(clipBounds) {
            withTranslation(x = horizontalOffset, y = bounds.top + verticalOffset) {
                draw(textLayout)
            }
        }
    }

    private fun Canvas.drawStatusBadge(statusBadge: StatusBadge) {
        val bounds = statusBadge.bounds
        statusBadgePaint.color = statusBadge.color

        val cornerRadius = bounds.height() / 3f
        drawRoundRect(bounds, cornerRadius, cornerRadius, statusBadgePaint)

        val textOffset = (statusTextPaint.ascent() + statusTextPaint.descent()) / 2f
        drawText(
            statusBadge.text,
            bounds.centerX(),
            bounds.centerY() - textOffset,
            statusTextPaint
        )
    }

    private fun createStatusBadge(eventChip: EventChip): StatusBadge? {
        val event = eventChip.event
        val statusText = EventStatusBadge.text(event) ?: return null
        val bounds = eventChip.bounds

        statusTextPaint.textSize = EventStatusBadge.textSize(viewState, event)

        val textWidth = statusTextPaint.measureText(statusText)
        val textHeight = statusTextPaint.textHeight

        val horizontalPadding = EventStatusBadge.horizontalPadding(viewState)
        val verticalPadding = EventStatusBadge.verticalPadding(viewState)

        val badgeWidth = textWidth + horizontalPadding * 2
        val badgeHeight = textHeight + verticalPadding * 2

        val bottom = bounds.bottom - EventStatusBadge.bottomSpacing(viewState)
        val top = bottom - badgeHeight

        if (top >= bottom || bottom <= bounds.top) {
            return null
        }

        val start = if (viewState.isLtr) {
            bounds.left + viewState.eventPaddingHorizontal
        } else {
            bounds.right - viewState.eventPaddingHorizontal - badgeWidth
        }
        val end = start + badgeWidth

        return StatusBadge(
            text = statusText,
            color = EventStatusBadge.color(event),
            bounds = RectF(start, top, end, bottom)
        )
    }

    private fun updateBackgroundPaint(
        event: ResolvedWeekViewEntity,
        paint: Paint
    ) = with(paint) {
        color = event.style.backgroundColor ?: viewState.defaultEventColor
        isAntiAlias = true
        strokeWidth = 0f
        style = Paint.Style.FILL
    }

    private fun updateBorderPaint(
        event: ResolvedWeekViewEntity,
        paint: Paint
    ) = with(paint) {
        color = event.style.borderColor ?: viewState.defaultEventColor
        isAntiAlias = true
        strokeWidth = event.style.borderWidth?.toFloat() ?: 0f
        style = Paint.Style.STROKE
    }
}

private val Paint.textHeight: Float
    get() = descent() - ascent()

private data class StatusBadge(
    val text: String,
    val color: Int,
    val bounds: RectF
)
