#ifndef TUNEBLOB_BIQUADFILTER_H
#define TUNEBLOB_BIQUADFILTER_H

#include "BiQuadPass.h"
#include "../data/WavData.h"

/**
 * High/low pass filter base class
 * Adapted from https://github.com/naudio/NAudio/blob/master/NAudio.Core/Dsp/BiQuadFilter.cs
 */
class BiQuadFilter {
public:

    /**
     * The type of pass filter (either low or high)
     */
    enum PassType {
        LOW_PASS,
        HIGH_PASS
    };

    /**
     * The number of poles
     */
    enum PoleType {
        TWO = 1,
        FOUR = 2,
        SIX = 3,
        EIGHT = 4
    };

    BiQuadFilter(PassType type, PoleType pole, double cutoffFrequency);

    void apply(WavData *wav);
    void setupPass(int sampleRate, double bandwidth);

protected:

    const PassType type;
    const PoleType pole;
    const double cutoffFrequency;
    std::shared_ptr<BiQuadPass> pass;

};

/**
 * Pole bandwidths
 */
static const float POLE_BANDWIDTHS[4][4] = {
        {0.7071},
        {0.60492333, 1.33722126},
        {0.58338080, 0.75932572, 1.95302407},
        {0.57622191, 0.66045510, 0.94276399, 2.57900101}
};


#endif //TUNEBLOB_BIQUADFILTER_H
