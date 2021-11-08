#include <iostream>
#include "BiQuadFilter.h"
#include "../PI.h"

/**
 * Create a new biquad filter
 * @param type Pass type (low pass or high pass)
 * @param pole Pole type (2, 4, 6, or 8; higher values mean more passes)
 * @param cutoffFrequency Cutoff frequency (low pass = max frequency, high pass = min frequency)
 */
BiQuadFilter::BiQuadFilter(PassType type, PoleType pole, double cutoffFrequency)
: type(type), pole(pole), cutoffFrequency(cutoffFrequency) {
    pass = std::make_shared<BiQuadPass>();
}

/**
 * Apply the biquad filter to a set of samples
 * @param wav Wav containing sample data
 */
void BiQuadFilter::apply(WavData *wav) {
    int totalFrames = wav->numFrames * wav->channels;
    for (int p = 0; p < pole; p++) {
        setupPass(wav->sampleRate, POLE_BANDWIDTHS[pole - 1][p]);
        for (int c = 0; c < wav->channels; c++) {
            pass->reset();
            for (int f = c; f < totalFrames; f += wav->channels) {
                float input = wav->samples[f];
                float output = pass->transform(input);
                wav->samples[f] = output;
            }
        }
    }
}

/**
 * Setup the filter for the next pass
 * @param sampleRate Sample rate
 * @param bandwidth Pole bandwidth
 */
void BiQuadFilter::setupPass(int sampleRate, double bandwidth) {
    double w0 = 2 * PI * cutoffFrequency / sampleRate;
    double cosw0 = cos(w0);
    double alpha = sin(w0) / (2 * bandwidth);

    double b0, b1, b2;
    double aa0 = 1 + alpha;
    double aa1 = -2 * cosw0;
    double aa2 = 1 - alpha;

    switch (type) {
        case LOW_PASS:
            b0 = (1 - cosw0) / 2;
            b1 = 1 - cosw0;
            b2 = (1 - cosw0) / 2;
            break;
        case HIGH_PASS:
            b0 = (1 + cosw0) / 2;
            b1 = -(1 + cosw0);
            b2 = (1 + cosw0) / 2;
            break;
        default:
            return;
    }

    pass->setCoefficients(aa0, aa1, aa2, b0, b1, b2);
}