#ifndef TUNEBLOB_BIQUADPASS_H
#define TUNEBLOB_BIQUADPASS_H

/**
 * Contains data for computing a single pass of the BiQuadFilter
 * Adapted from https://github.com/naudio/NAudio/blob/master/NAudio.Core/Dsp/BiQuadFilter.cs
 */
class BiQuadPass {
public:

    void setCoefficients(double aa0, double aa1, double aa2, double b0, double b1, double b2);
    float transform(float inSample);
    void reset();

private:

    // Coefficients
    double a0;
    double a1;
    double a2;
    double a3;
    double a4;

    // State
    double x1 = 0;
    double x2 = 0;
    double y1 = 0;
    double y2 = 0;

};


#endif //TUNEBLOB_BIQUADPASS_H
