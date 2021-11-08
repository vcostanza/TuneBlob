#ifndef TUNEBLOB_SAMPLEBUFFER_H
#define TUNEBLOB_SAMPLEBUFFER_H

/**
 * FIFO sample buffer
 */
class SampleBuffer {
public:

    SampleBuffer(int capacity);
    ~SampleBuffer();

    void addSamples(const float *samples, int numFrames);
    float *getSamples();
    int getCapacity() const;
    int getNumSamples();
    bool isFilled() const;

private:

    int capacity;
    int bufferSize;
    float *buffer;

};


#endif //TUNEBLOB_SAMPLEBUFFER_H
