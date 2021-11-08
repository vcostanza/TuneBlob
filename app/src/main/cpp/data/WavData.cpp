#include <cstdlib>
#include "WavData.h"

/**
 * Initialize WAV data
 * @param channels Number of audio channels (mono = 1, stereo = 2)
 * @param numFrames Number of frames (samples)
 * @param sampleRate Sample rate (frames per second)
 * @param samples Samples buffer (interleaved based on number of channels)
 * @param freeSamples True to free the samples on deletion
 */
WavData::WavData(int channels, int numFrames, int sampleRate, float *samples, const bool freeSamples)
: channels(channels), numFrames(numFrames), sampleRate(sampleRate), samples(samples), freeSamples(freeSamples) {
}

/**
 * Free the samples buffer on deletion
 */
WavData::~WavData() {
    if (freeSamples)
        delete samples;
}

/**
 * Get the peak amplitude for a given range of samples
 * @param startFrame Start frame
 * @param numFrames Number of frames to scan
 * @return Peak amplitude (absolute value)
 */
float WavData::getPeakAmplitude(int startFrame, int numFrames) const {
    float max = 0;
    startFrame *= channels;
    int endFrame = startFrame + numFrames * channels;
    for (int i = startFrame; i < endFrame; i++) {
        float amp = fabs(samples[i]);
        if (amp > max) max = amp;
    }
    return max;
}
