#include "WavData.h"

WavData::WavData(int channels, int numFrames, int sampleRate, float *samples, const bool freeSamples)
: channels(channels), numFrames(numFrames), sampleRate(sampleRate), samples(samples), freeSamples(freeSamples) {
}

WavData::~WavData() {
    if (freeSamples)
        delete samples;
}

float WavData::getPeakAmplitude(int startFrame, int numFrames) const {
    float max = 0;
    startFrame *= channels;
    int endFrame = startFrame + numFrames * channels;
    for (int i = startFrame; i < endFrame; i++) {
        if (samples[i] > max)
            max = samples[i];
    }
    return max;
}
