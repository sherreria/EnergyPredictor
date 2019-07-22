package es.uvigo.det.netlab.predictor;

/**
 * This class provides Taylor and Chebyshev approximations of main trigonometric functions.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
public final class TrigTools
{
    public static final float PI = (float) Math.PI;

    public enum SeriesType {
	TAYLOR, CHEBYSHEV
    }
    public static SeriesType series = SeriesType.TAYLOR;
    
    public static int seriesDegree = 0;

    private static final float[] sinTaylorCoefficients = {1f, 0.166666667f, 0.008333333f, 0.000198413f, 0.000002756f, 0.000000025f};
    private static final float[] asinTaylorCoefficients = {1f, 0.166666667f, 0.075f, 0.044642857f, 0.030381944f, 0.022372159f, 0.017352764f};

    private static final float[][] sinChebyshevCoefficients = {
	{0.99749f, 0.15652f},
	{0.999978675f, -0.1664971f, 0.00799224f},
	{0.999999904f, -0.166665402f, 0.008328768f, -0.000192298f},
	{0.999999999f, -0.166666661f, 0.008333304f, -0.000198345f, 0.000002688f},
	{1.0f, -0.166666667f, 0.008333333f, -0.000198412f, 0.000002755f, -0.000000025f}
    };
    private static final float[][] asinChebyshevCoefficients = {
	{0.8614f, 0.5408f},
	{1.08587f, -0.35788f, 0.719088f},
	{0.943528878f, 0.780838836f, -1.558348544f, 1.301392704f},
	{1.037836385f, -0.476593933f, 2.968409379f, -4.734284512f, 2.682523200f},
	{0.974704947f, 0.786034821f, -4.102311638f, 11.42736353f, -13.47912484f, 5.876962924f},
	{1.016676946f, -0.389181173f, 5.299416308f, -20.80713228f, 40.24503485f, -37.10236483f, 13.22440854f}
    };
    
    private TrigTools () {}
    
    /**
     * Calculates the sine of the given angle using the corresponding Taylor series.
     *
     * @param angle  the angle (in radians)
     * @param degree the degree of the Taylor polynomial
     * @return the sine of the given angle
     */
    public static float sinTaylor (float angle, int degree)
    {
	if (degree <= 0) {
	    return (float) Math.sin(angle);
	}
	if (degree > 11) {
	    degree = 11;
	}
	float bangle = angle;
	while (bangle > PI) {
	    bangle -= 2 * PI;
	}
	while (bangle < -1 * PI) {
	    bangle += 2 * PI;
	}
	float bangle2 = bangle * bangle;
	float sine = 0;
	for (int i = (degree - 1) / 2; i > 0; i--) {
	    sine = bangle2 * (sinTaylorCoefficients[i] - sine);
	}
	return bangle * (1 - sine);
    }
    
    public static float sinTaylor (float angle)
    {
	return sinTaylor(angle, seriesDegree);
    }
    
    /**
     * Calculates the sine of the given angle using the corresponding Chebyshev series.
     *
     * @param angle  the angle (in radians)
     * @param degree the degree of the Chebyshev polynomial
     * @return the sine of the given angle
     */
    public static float sinChebyshev (float angle, int degree)
    {
	if (degree <= 0) {
	    return (float) Math.sin(angle);
	}
	if (degree > 11) {
	    degree = 11;
	}
	float bangle = angle;
	while (bangle > PI) {
	    bangle -= 2 * PI;
	}
	while (bangle < -1 * PI) {
	    bangle += 2 * PI;
	}
	int chebyshevCoefficientsIndex = (degree - 3) / 2;
	float bangle2 = bangle * bangle;
	float sine = 0;
	for (int i = (degree - 1) / 2; i > 0; i--) {
	    sine = bangle2 * (sinChebyshevCoefficients[chebyshevCoefficientsIndex][i] + sine);
	}
	return bangle * (sinChebyshevCoefficients[chebyshevCoefficientsIndex][0] + sine);
    }
     
    public static float sinChebyshev (float angle)
    {
	return sinChebyshev(angle, seriesDegree);
    }
    
    /**
     * Calculates the sine of the given angle using the corresponding Taylor/Chebyshev series.
     *
     * @param angle  the angle (in radians)
     * @param degree the degree of the Taylor/Chebyshev polynomial
     * @return the sine of the given angle
     */
    public static float sin (float angle, int degree) {
	if (series == SeriesType.TAYLOR) {
	    return sinTaylor(angle, degree);
	}
	return sinChebyshev(angle, degree);
    }
    
    public static float sin (float angle) {
	if (series == SeriesType.TAYLOR) {
	    return sinTaylor(angle);
	}
	return sinChebyshev(angle);
    }
    
    /**
     * Calculates the cosine of the given angle using the corresponding Taylor series.
     *
     * @param angle  the angle (in radians)
     * @param degree the degree of the Taylor polynomial
     * @return the cosine of the given angle
     */
    public static float cosTaylor (float angle, int degree)
    {
	return sinTaylor(angle + PI / 2, degree);
    }
    
    public static float cosTaylor (float angle)
    {
	return sinTaylor(angle + PI / 2, seriesDegree);
    }
    
    /**
     * Calculates the cosine of the given angle using the corresponding Chebyshev series.
     *
     * @param angle  the angle (in radians)
     * @param degree the degree of the Chebyshev polynomial
     * @return the cosine of the given angle
     */
    public static float cosChebyshev (float angle, int degree)
    {
	return sinChebyshev(angle + PI / 2, degree);
    }
    
    public static float cosChebyshev (float angle)
    {
	return sinChebyshev(angle + PI / 2, seriesDegree);
    }
        
    /**
     * Calculates the cosine of the given angle using the corresponding Taylor/Chebyshev series.
     *
     * @param angle  the angle (in radians)
     * @param degree the degree of the Taylor/Chebyshev polynomial
     * @return the cosine of the given angle
     */
    public static float cos (float angle, int degree) {
	if (series == SeriesType.TAYLOR) {
	    return cosTaylor(angle, degree);
	}
	return cosChebyshev(angle, degree);
    }
    
    public static float cos (float angle) {
	if (series == SeriesType.TAYLOR) {
	    return cosTaylor(angle);
	}
	return cosChebyshev(angle);
    }
    
    /**
     * Calculates the arcsine of the given value using the corresponding Taylor series.
     *
     * @param value  the value
     * @param degree the degree of the Taylor polynomial
     * @return the arcsine of the given value
     */
    public static float asinTaylor (float value, int degree)
    {
	if (degree <= 0) {
	    return (float) Math.asin(value);
	}
	if (degree > 13) {
	    degree = 13;
	}
	float value2 = value * value;
	float arcsine = 0;
	for (int i = (degree - 1) / 2; i > 0; i--) {
	    arcsine = value2 * (asinTaylorCoefficients[i] + arcsine);
	}
	return value * (1 + arcsine);
    }

    public static float asinTaylor (float value)
    {
	return asinTaylor(value, seriesDegree);
    }
    
    /**
     * Calculates the arcsine of the given value using the corresponding Chebyshev series.
     *
     * @param value  the value
     * @param degree the degree of the Chebyshev polynomial
     * @return the arcsine of the given value
     */
    public static float asinChebyshev (float value, int degree)
    {
	if (degree <= 0) {
	    return (float) Math.asin(value);
	}
	if (degree > 13) {
	    degree = 13;
	}
	int chebyshevCoefficientsIndex = (degree - 3) / 2;
	float value2 = value * value;
	float arcsine = 0;
	for (int i = (degree - 1) / 2; i > 0; i--) {
	    arcsine = value2 * (asinChebyshevCoefficients[chebyshevCoefficientsIndex][i] + arcsine);
	}
	return value * (asinChebyshevCoefficients[chebyshevCoefficientsIndex][0] + arcsine);
    }
    
    public static float asinChebyshev (float value)
    {
	return asinChebyshev(value, seriesDegree);
    }
        
    /**
     * Calculates the arcsine of the given angle using the corresponding Taylor/Chebyshev series.
     *
     * @param angle  the angle (in radians)
     * @param degree the degree of the Taylor/Chebyshev polynomial
     * @return the arcsine of the given angle
     */
    public static float asin (float angle, int degree) {
	if (series == SeriesType.TAYLOR) {
	    return asinTaylor(angle, degree);
	}
	return asinChebyshev(angle, degree);
    }
    
    public static float asin (float angle) {
	if (series == SeriesType.TAYLOR) {
	    return asinTaylor(angle);
	}
	return asinChebyshev(angle);
    }
}
