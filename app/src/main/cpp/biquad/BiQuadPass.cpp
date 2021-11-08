#include "BiQuadPass.h"

/**
 * Precompute the coefficients need to perform transformations
 */
void BiQuadPass::setCoefficients(double aa0, double aa1, double aa2,
                                 double b0, double b1, double b2) {
    a0 = b0/aa0;
    a1 = b1/aa0;
    a2 = b2/aa0;
    a3 = aa1/aa0;
    a4 = aa2/aa0;
}

/**
 * Transforms a sample using the biquad filter
 * @param inSample Input sample
 * @return Output sample
 */
float BiQuadPass::transform(float inSample) {

    // Compute result
    double result = a0 * inSample + a1 * x1 + a2 * x2 - a3 * y1 - a4 * y2;

    // Shift x1 to x2, sample to x1
    x2 = x1;
    x1 = inSample;

    // Shift y1 to y2, result to y1
    y2 = y1;
    y1 = result;

    return (float) y1;
}

/**
 * Resets the state parameters
 */
void BiQuadPass::reset() {
    x1 = x2 = y1 = y2 = 0;
}
