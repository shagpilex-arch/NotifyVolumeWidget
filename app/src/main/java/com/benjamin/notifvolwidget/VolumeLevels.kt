package com.benjamin.notifvolwidget

/**
 * Maps between the phone's raw notification-volume steps (0..max, for example
 * 0..16) and the scale the widget shows: 0-100% in multiples of 10.
 *
 * A phone only has a fixed number of volume steps, so "exactly 60%" usually
 * doesn't exist. The widget therefore always *displays* the nearest multiple
 * of 10, and each press works out where to go from that rounded figure:
 * at 58% pressing - goes to 50%, at 54% it goes to 40%.
 */
object VolumeLevels {
    const val STEP = 10

    /** Where [index] sits on the 0-100 scale, rounded to the nearest 10 (halves
     *  round up). Never reads 0 unless the volume really is 0, or 100 unless it
     *  really is the maximum. */
    fun levelOf(index: Int, max: Int): Int {
        if (max <= 0 || index <= 0) return 0
        if (index >= max) return 100
        val rounded = (index * 100 + max * 5) / (max * 10) * STEP
        return rounded.coerceIn(STEP, 100 - STEP)
    }

    /** The raw step closest to [level] percent. */
    fun indexFor(level: Int, max: Int): Int = (level.coerceIn(0, 100) * max + 50) / 100

    /** The raw step to move to when + is pressed: one round level above the
     *  current (rounded) level, and always at least one raw step so a press
     *  never does nothing on phones with few volume steps. */
    fun raisedIndex(current: Int, max: Int): Int =
        maxOf(indexFor(levelOf(current, max) + STEP, max), minOf(current + 1, max))

    /** Same as [raisedIndex], downwards. */
    fun loweredIndex(current: Int, max: Int): Int =
        minOf(indexFor(levelOf(current, max) - STEP, max), maxOf(current - 1, 0))
}
