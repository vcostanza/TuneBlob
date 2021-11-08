
#ifndef TUNEBLOB_WAVDATA_H
#define TUNEBLOB_WAVDATA_H

/**
 * Struct containing waveform samples
 */
class WavData {
public:

    WavData(int channels, int numFrames, int sampleRate, float *samples, const bool freeSamples);
    ~WavData();

    float getPeakAmplitude(int startFrame, int numFrames) const;

    int channels;
    int numFrames;
    int sampleRate;
    float *samples;
    const bool freeSamples;

};


#endif //TUNEBLOB_WAVDATA_H
