package com.example.mezahub.audio

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class RecordingBufferTest {

    @Test
    fun latest_returnsTheMostRecentSamples() {
        val buffer = RecordingBuffer(capacity = 10)
        buffer.append(shortArrayOf(1, 2, 3, 99), count = 3) // only the first `count` samples are real
        buffer.append(shortArrayOf(4, 5), count = 2)
        assertEquals(5, buffer.size)
        assertArrayEquals(shortArrayOf(3, 4, 5), buffer.latest(3))
        assertArrayEquals(shortArrayOf(1, 2, 3, 4, 5), buffer.latest(100))
    }

    @Test
    fun append_dropsSamplesPastCapacity() {
        val buffer = RecordingBuffer(capacity = 4)
        buffer.append(shortArrayOf(1, 2, 3), count = 3)
        buffer.append(shortArrayOf(4, 5, 6), count = 3)
        buffer.append(shortArrayOf(7), count = 1)
        assertArrayEquals(shortArrayOf(1, 2, 3, 4), buffer.latest(10))
    }

    @Test
    fun latest_onEmptyBuffer_isEmpty() {
        assertEquals(0, RecordingBuffer(capacity = 4).latest(2).size)
    }
}
