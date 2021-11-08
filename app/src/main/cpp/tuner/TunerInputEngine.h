#ifndef TUNEBLOB_TUNERINPUTENGINE_H
#define TUNEBLOB_TUNERINPUTENGINE_H

#include <oboe/Oboe.h>
#include "SampleBuffer.h"
#include "../audacity/FrequencyReader.h"
#include "../data/WavData.h"
#include "../biquad/BiQuadFilter.h"

/**
 * Listens on an audio input device and saves samples to a buffer
 */
class TunerInputEngine: public oboe::AudioStreamDataCallback {
public:

    ~TunerInputEngine() override = default;

    bool setParameters(float bufferSize, float minAmp, float maxFreq);
    oboe::Result start(int deviceId, int channels, int sampleRate);
    oboe::Result stop();
    oboe::DataCallbackResult onAudioReady(oboe::AudioStream *oboeStream, void *audioData, int32_t numFrames) override;

    float queryFrequency();
    WavData *getWav();

private:

    float bufferSize = 0.2;
    float minAmp = 0.01;
    float maxFreq = 1000;

    std::shared_ptr<WavData> wav;
    std::shared_ptr<SampleBuffer> sampleBuffer;
    std::shared_ptr<FrequencyReader> freqReader;
    std::shared_ptr<BiQuadFilter> lowPass;

    std::mutex         mLock;
    std::shared_ptr<oboe::AudioStream> mStream;
    bool running;
};


#endif //TUNEBLOB_TUNERINPUTENGINE_H
