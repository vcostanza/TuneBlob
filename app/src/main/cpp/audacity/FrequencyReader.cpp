/*
 * Detect frequency range of an audio clip
 * Adapted from https://github.com/audacity/audacity/blob/master/libraries/lib-math/Spectrum.cpp
 */

#include "FrequencyReader.h"

FrequencyReader::FrequencyReader(int sampleRate, float minAmplitude)
: sampleRate(sampleRate), minAmplitude(minAmplitude) {

    if (sampleRate == 44100) // Most common sample rate - save some calc time
        windowSize = 4096;
    else
        windowSize = std::max(256, (int) round(pow(2.0, floor(log2(sampleRate / 20.0) + 0.5))));
    windowSizeH = windowSize / 2;
    windowSize2 = windowSize * 2;
    windowSize4 = windowSize * 4;

    fft = std::make_shared<FFT>(windowSize);
    processed = new float[windowSize];
    in = new float[windowSize];
    out = new float[windowSize];
    out2 = new float[windowSize];
    freq = new float[windowSizeH];
    freqa = new float[windowSizeH];
}

FrequencyReader::~FrequencyReader() {
    delete processed;
    delete in;
    delete out;
    delete out2;
    delete freq;
    delete freqa;
}

float FrequencyReader::getFrequency(WavData *wav, int channel, int startFrame, int scanFrames) {
    if (startFrame >= wav->numFrames)
        return 0;

    // Auto-size window -- high sample rates require larger windowSize.
    // Aim for around 2048 samples at 44.1 kHz (good down to about 100 Hz).
    // To detect single notes, analysis period should be about 0.2 seconds.
    // windowSize must be a power of 2.

    // Default to 0.2 seconds
    if (scanFrames <= 0)
        scanFrames = wav->numFrames / 5;

    // Number of windows based on scan time (4 by default)
    int numWindows = std::max(1, (int) round(scanFrames / windowSize));
    //int numWindows = Math.max(1, (int) Math.round(rate / (5.0f * windowSize)));

    startFrame = std::max(0, startFrame);

    memset(freq, 0, windowSize2);
    memset(freqa, 0, windowSize2);

    int srcPos = startFrame;
    int windowsUsed = 0;
    for(int i = 0; i < numWindows && srcPos + windowSize < wav->numFrames; i++) {

        // Strict amplitude filtering
        // If any of the windows are too quiet then return zero for the entire scan
        // This prevents annoying frequency spikes from showing up in the results
        if (wav->getPeakAmplitude(srcPos, windowSize) < minAmplitude)
            return 0;

        // Compute FFT spectrum
        if (computeSpectrum(wav, channel, srcPos, windowSize, freq, true)) {
            for (int j = 0; j < windowSizeH; j++)
                freqa[j] += freq[j];
            windowsUsed++;
        }
        srcPos += windowSize;
    }

    if (windowsUsed < 1)
        return 0;

    int argmax = 0;
    for(int j = 1; j < windowSizeH; j++)
        if (freqa[j] > freqa[argmax])
            argmax = j;

    int lag = (windowSizeH - 1) - argmax;
    return (float) sampleRate / lag;
}

bool FrequencyReader::computeSpectrum(WavData *wav, int channel, int wavStart,
                                      int width, float *output, bool autoCorrelation) {
    if (width < windowSize)
        return false;

    memset(processed, 0, windowSize4);
    memset(in, 0, windowSize4);
    memset(out, 0, windowSize4);
    memset(out2, 0, windowSize4);

    int start = 0;
    int windows = 0;
    while (start + windowSize <= width) {
        memcpy(in, wav->samples + (wavStart + start) * wav->channels + channel, windowSize4);

        //WindowFunc(windowFunc, windowSize, in);
        fft->hannWindowFunc(true, in);

        if (autoCorrelation) {
            // Take FFT
            fft->apply(in, out, out2);
            // Compute power
            for (int i = 0; i < windowSize; i++)
                in[i] = (out[i] * out[i]) + (out2[i] * out2[i]);

            // Tolonen and Karjalainen recommend taking the cube root
            // of the power, instead of the square root

            for (int i = 0; i < windowSize; i++)
                in[i] = pow(in[i], 1.0f / 3.0f);

            // Take FFT
            fft->apply(in, out, out2);
        }
        /*else
            PowerSpectrum(windowSize, in, out);*/

        // Take real part of result
        for (int i = 0; i < windowSizeH; i++)
            processed[i] += out[i];

        start += windowSizeH;
        windows++;
    }

    if (windows < 1)
        return false;

    if (autoCorrelation) {

        // Peak Pruning as described by Tolonen and Karjalainen, 2000
        /*
         Combine most of the calculations in a single for loop.
         It should be safe, as indexes refer only to current and previous elements,
         that have already been clipped, etc...
        */
        for (int i = 0; i < windowSizeH; i++) {
            // Clip at zero, copy to temp array
            if (processed[i] < 0.0)
                processed[i] = 0;
            out[i] = processed[i];
            // Subtract a time-doubled signal (linearly interp.) from the original
            // (clipped) signal
            if ((i % 2) == 0)
                processed[i] -= out[i / 2];
            else
                processed[i] -= ((out[i / 2] + out[i / 2 + 1]) / 2);

            // Clip at zero again
            if (processed[i] < 0.0)
                processed[i] = 0;
        }

        // Reverse and scale
        for (int i = 0; i < windowSizeH; i++)
            in[i] = processed[i] / (windowSize / 4);
        for (int i = 0; i < windowSizeH; i++)
            processed[windowSizeH - 1 - i] = in[i];
    } else {
        // Convert to decibels
        // But do it safely; -Inf is nobody's friend
        for (int i = 0; i < windowSizeH; i++){
            double temp = (processed[i] / windowSize / windows);
            if (temp > 0.0)
                processed[i] = 10 * log10(temp);
            else
                processed[i] = 0;
        }
    }

    memcpy(output, processed, windowSize2);

    return true;
}