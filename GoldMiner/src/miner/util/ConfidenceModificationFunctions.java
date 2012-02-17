package miner.util;


/**
 * Defines a number of functions used for modifying confidence values before being used by {@link RandomAxiomChooser}.
 */
public interface ConfidenceModificationFunctions {
    /**
     * Modifies the value according to the function
     *
     * @param x value to modify
     * @return resulting value
     */
    public double getValue(double x);

    /**
     * Determines the new value according to the function
     *  $\frac{\left(\arctan \left(\left(a\cdot x-c\right)\cdot b\right)\right)-\arctan \left(-cb\right)}{\left(\arctan \left(\left(a-c\right)\cdot b\right)\right)-\arctan \left(-cb\right)}$
     * with the following
     *
     */
    public static class ArctanModifier implements ConfidenceModificationFunctions {
        private double a;
        private double b;
        private double c;

        public ArctanModifier(double a, double b, double c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }

        @Override
        public double getValue(double x) {
            double res = ((Math.atan((a * x - c) * b)) - Math.atan(-c * b)) / (Math.atan((a - c) * b) - Math.atan(-c * b));
            System.out.format("Scaling %f to %f\n", x, res);
            return res;
        }
    }

}
