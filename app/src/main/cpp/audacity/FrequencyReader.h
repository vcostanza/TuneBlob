#ifndef TUNEBLOB_FREQUENCYREADER_H
#define TUNEBLOB_FREQUENCYREADER_H


#include "FFT.h"
#include "../data/WavData.h"

class FrequencyReader {
public:

    FrequencyReader(int sampleRate, float minAmplitude);
    ~FrequencyReader();

    float getFrequency(WavData *wav, int channel, int startFrame, int scanFrames);
    bool computeSpectrum(WavData *wav, int channel, int wavStart, int width, float *output, bool autoCorrelation);

private:

    const int sampleRate;
    const float minAmplitude;
    int windowSize, windowSizeH, windowSize2, windowSize4;
    std::shared_ptr<FFT> fft;

    float *processed;
    float *in;
    float *out;
    float *out2;
    float *freq;
    float *freqa;
};


#endif //TUNEBLOB_FREQUENCYREADER_H
