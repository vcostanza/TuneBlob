#include "TunerInputEngine.h"
#include "../logging_macros.h"

/**
 * Set engine parameters
 * @param bufferSize Buffer size in seconds
 * @param minAmp Minimum amplitude
 * @param maxFreq Maximum frequency
 * @return True if parameters were set successfully
 */
bool TunerInputEngine::setParameters(float bufferSize, float minAmp, float maxFreq) {

    // Engine cannot be running when this call is made
    if (running) {
        LOGE("Cannot setParameters while engine is running");
        return false;
    }

    this->bufferSize = bufferSize;
    this->minAmp = minAmp;
    this->maxFreq = maxFreq;
    return true;
}

/**
 * Start the tuner engine, which continuously reads audio samples from a given input device
 * @param deviceId Audio input device ID
 * @param channels Number of channels used by the input
 * @param sampleRate Sample rate of the input
 * @return Success result
 */
oboe::Result TunerInputEngine::start(int deviceId, int channels, int sampleRate) {

    std::lock_guard<std::mutex> lock(mLock);

    // No need to start when we're already running
    if (running)
        return oboe::Result::OK;

    int bufferSize = (int) (this->bufferSize * (float) sampleRate);

    sampleBuffer = std::make_shared<SampleBuffer>(bufferSize);
    freqReader = std::make_shared<FrequencyReader>(sampleRate, this->minAmp);
    lowPass = std::make_shared<BiQuadFilter>(BiQuadFilter::LOW_PASS, BiQuadFilter::EIGHT, maxFreq);

    // Create the Oboe stream listener
    oboe::AudioStreamBuilder builder;
    oboe::Result result = builder.setDeviceId(deviceId)
            ->setChannelCount(channels)
            ->setSampleRate(sampleRate)
            ->setSharingMode(oboe::SharingMode::Exclusive)
            ->setDirection(oboe::Direction::Input)
            ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
            ->setSampleRateConversionQuality(oboe::SampleRateConversionQuality::Medium)
            ->setFormat(oboe::AudioFormat::Float)
            ->setDataCallback(this)
            ->openStream(mStream);

    if (result != oboe::Result::OK)
        return result;

    // Start the stream
    result = mStream->requestStart();

    // If all went well then initialize the wav container and flag as running
    if (result == oboe::Result::OK) {
        this->wav = std::make_shared<WavData>(channels, bufferSize,sampleRate,
                                              new float[bufferSize], true);
        this->running = true;
    }

    return result;
}

/**
 * Stop the tuner engine
 * @return Success result
 */
oboe::Result TunerInputEngine::stop() {
    oboe::Result result = oboe::Result::OK;
    std::lock_guard<std::mutex> lock(mLock);
    if (running) {
        result = mStream->stop();
        mStream->close();
        mStream.reset();
        running = false;
    }
    return result;
}

/**
 * Called whenever a new batch is samples have been received from the input device
 * @param oboeStream Audio stream instance
 * @param inputData Audio sample data
 * @param numFrames Number of samples
 * @return Whether to continue listening or stop
 */
oboe::DataCallbackResult
TunerInputEngine::onAudioReady(oboe::AudioStream *oboeStream, void *inputData, int32_t numFrames) {

    if (!running)
        return oboe::DataCallbackResult::Stop;

    const auto *inputFloats = static_cast<const float *>(inputData);

    sampleBuffer->addSamples(inputFloats, numFrames);

    return oboe::DataCallbackResult::Continue;
}

/**
 * Compute the frequency using the current sample buffer
 * @return Frequency in hertz
 */
float TunerInputEngine::queryFrequency() {
    // Sample buffer hasn't been filled yet
    if (!sampleBuffer->isFilled())
        return 0;

    // Copy the latest samples into the wav buffer so we don't run into threading issues
    memcpy(wav->samples, sampleBuffer->getSamples(), sampleBuffer->getCapacity() * sizeof(float));

    // Apply low pass filter
    lowPass->apply(wav.get());

    // Get frequency using the frequency detector
    return freqReader->getFrequency(wav.get(), 0, 0, wav->numFrames);
}

/**
 * Get the wav data instance that holds the sample buffer
 * @return Wav data
 */
WavData *TunerInputEngine::getWav() {
    return wav.get();
}