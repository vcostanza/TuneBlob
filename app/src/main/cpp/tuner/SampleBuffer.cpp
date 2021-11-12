#include <algorithm>
#include "SampleBuffer.h"
#include "../logging_macros.h"

/**
 * Create the sample buffer
 * @param capacity Sample capacity
 */
SampleBuffer::SampleBuffer(int capacity) : capacity(capacity) {
    bufferSize = 0;
    buffer = new float[capacity];
}

/**
 * Delete the sample buffer
 */
SampleBuffer::~SampleBuffer() {
    delete[] buffer;
}

/**
 * Add samples to the buffer
 * @param samples Array of samples to add
 * @param numFrames Number of samples
 */
void SampleBuffer::addSamples(const float *samples, int numFrames) {
    int s = sizeof(float);
    int pos = std::max(std::min(bufferSize, capacity - numFrames), 0);
    int amount = std::min(numFrames, capacity);
    int shift = std::max((bufferSize + numFrames) - capacity, 0);

    // Shift existing data to the beginning of the buffer
    if (shift > 0)
        memmove(buffer, buffer + shift, (capacity - shift) * s);

    // Add new samples to the end of the buffer
    memcpy(buffer + pos, samples, amount * s);

    // Update the buffer size
    bufferSize = std::min(bufferSize + amount, capacity);
}

/**
 * Get the current buffer data
 * @return Sample buffer data
 */
float *SampleBuffer::getSamples() {
    return buffer;
}

/**
 * Get the capacity of the buffer
 * @return Buffer capacity
 */
int SampleBuffer::getCapacity() const {
    return capacity;
}

/**
 * Get the current number of samples in the buffer
 * Once the buffer is filled this will be equal to capacity
 * @return Number of samples
 */
int SampleBuffer::getNumSamples() {
    return bufferSize;
}

/**
 * Check if the buffer is filled with samples to capacity
 * @return True if filled
 */
bool SampleBuffer::isFilled() const {
    return bufferSize == capacity;
}