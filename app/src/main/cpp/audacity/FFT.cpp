/*
 * Fast Fourier Transform calculator
 * Converted from https://github.com/audacity/audacity/blob/master/libraries/lib-math/FFT.cpp
 */

#include <algorithm>
#include "FFT.h"

FFT::FFT(int fftLen) : length(fftLen), length4(fftLen * 4) {
    /*
     *  FFT size is only half the number of data points
     *  The full FFT output can be reconstructed from this FFT's output.
     *  (This optimization can be made since the data is real.)
     */
    points = fftLen / 2;
    sinTable = new float[2*points];
    bitReversed = new int[points];
    buffer = new float[length];

    for(int i = 0; i < points; i++) {
        int temp = 0;
        for(int mask = points / 2; mask > 0; mask >>= 1)
            temp = (temp >> 1) + ((i & mask) != 0 ? points : 0);

        bitReversed[i] = temp;
    }

    for(int i = 0; i < points; i++) {
        sinTable[bitReversed[i]] = (float) -sin(2*PI*i/(2*points));
        sinTable[bitReversed[i]+1] = (float) -cos(2*PI*i/(2*points));
    }
}

FFT::~FFT() {
    delete[] sinTable;
    delete[] bitReversed;
    delete[] buffer;
}

void FFT::hannWindowFunc(bool extraSample, float *in) const {
    int NumSamples = length;
    if (extraSample)
        --NumSamples;

    // Hann
    auto multiplier = (float) (2 * PI / NumSamples);
    float coeff0 = 0.5, coeff1 = -0.5;
    for (int ii = 0; ii < NumSamples; ++ii)
        in[ii] *= coeff0 + coeff1 * cos((float) ii * multiplier);

    if (extraSample)
        in[NumSamples] *= 0;
}

void FFT::apply(float *RealIn, float *RealOut, float *ImagOut) const {

    // Copy the data into the processing buffer
    if (length >= 0) memcpy(buffer, RealIn, length4);

    // Perform the FFT
    apply();

    // Copy the data into the real and imaginary outputs
    for (int i = 1; i<(length / 2); i++) {
        RealOut[i] = buffer[bitReversed[i]];
        ImagOut[i] = buffer[bitReversed[i]+1];
    }
    // Handle the (real-only) DC and Fs/2 bins
    RealOut[0] = buffer[0];
    RealOut[length / 2] = buffer[1];
    ImagOut[0] = ImagOut[length / 2] = 0;
    // Fill in the upper half using symmetry properties
    for(int i = length / 2 + 1; i < length; i++) {
        RealOut[i] =  RealOut[length-i];
        ImagOut[i] = -ImagOut[length-i];
    }
}

void FFT::apply() const {
    int A, B;
    int sptr;
    int endptr1, endptr2;
    int br1, br2;
    float HRplus,HRminus,HIplus,HIminus;
    float v1,v2,sin,cos;

    int ButterfliesPerGroup = points / 2;

    /*
     *  Butterfly:
     *     Ain-----Aout
     *         \ /
     *         / \
     *     Bin-----Bout
     */

    endptr1 = points * 2;

    while (ButterfliesPerGroup > 0) {
        A = 0;
        B = ButterfliesPerGroup * 2;
        sptr = 0;

        while (A < endptr1) {
            sin = sinTable[sptr];
            cos = sinTable[sptr+1];
            endptr2 = B;
            while (A < endptr2) {
                v1 = buffer[B] * cos + buffer[B + 1] * sin;
                v2 = buffer[B] * sin - buffer[B + 1] * cos;
                buffer[B] = buffer[A] + v1;
                buffer[A++] = buffer[B++] - 2 * v1;
                buffer[B] = buffer[A] - v2;
                buffer[A++] = buffer[B++] + 2 * v2;
            }
            A = B;
            B += ButterfliesPerGroup * 2;
            sptr += 2;
        }
        ButterfliesPerGroup >>= 1;
    }
    /* Massage output to get the output for a real input sequence. */
    br1 = 1;
    br2 = points - 1;

    while(br1 < br2) {
        sin = sinTable[bitReversed[br1]];
        cos = sinTable[bitReversed[br1] + 1];
        A = bitReversed[br1];
        B = bitReversed[br2];
        HRplus = (HRminus = buffer[A] - buffer[B]) + (buffer[B] * 2);
        HIplus = (HIminus = buffer[A+1] - buffer[B+1]) + (buffer[B+1] * 2);
        v1 = (sin*HRminus - cos*HIplus);
        v2 = (cos*HRminus + sin*HIplus);
        buffer[A] = (HRplus + v1) * 0.5f;
        buffer[B] = buffer[A] - v1;
        buffer[A+1] = (HIminus + v2) * 0.5f;
        buffer[B+1] = buffer[A+1] - HIminus;

        br1++;
        br2--;
    }
    /* Handle the center bin (just need a conjugate) */
    A = bitReversed[br1] + 1;
    buffer[A] = -buffer[A];
    /* Handle DC bin separately - and ignore the Fs/2 bin
       buffer[0]+=buffer[1];
       buffer[1]=(fft_type)0;*/
    /* Handle DC and Fs/2 bins separately */
    /* Put the Fs/2 value into the imaginary part of the DC bin */
    v1 = buffer[0] - buffer[1];
    buffer[0] += buffer[1];
    buffer[1] = v1;
}
