/*
 * Copyright (c) 2004-2009, Jean-Marc François. All Rights Reserved.
 * Licensed under the New BSD license.  See the LICENSE file.
 */
package jahmm.observables;

import jahmm.distributions.MultiGaussianDistribution;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * This class represents a multivariate Gaussian distribution function.
 */
public final class OpdfMultiGaussian extends OpdfBase<ObservationVector> implements Opdf<ObservationVector> {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(OpdfMultiGaussian.class.getName());

    private final MultiGaussianDistribution distribution;

    /**
     * Builds a new Gaussian probability distribution with zero mean and
     * identity covariance matrix.
     *
     * @param dimension The dimension of the vectors.
     */
    public OpdfMultiGaussian(int dimension) {
        distribution = new MultiGaussianDistribution(dimension);
    }

    /**
     * Builds a new Gaussian probability distribution with a given mean and
     * covariance matrix.
     *
     * @param mean The distribution's mean.
     * @param covariance The distribution's covariance matrix.
     */
    public OpdfMultiGaussian(double[] mean, double[][] covariance) {
        if (covariance.length == 0 || mean.length != covariance.length
                || covariance.length != covariance[0].length) {
            throw new IllegalArgumentException();
        }

        distribution = new MultiGaussianDistribution(mean, covariance);
    }

    private OpdfMultiGaussian(MultiGaussianDistribution distribution) {
        this.distribution = distribution;
    }

    /**
     * Returns (a copy of) this distribution's mean vector.
     *
     * @return The mean vector.
     */
    public double[] mean() {
        return distribution.mean();
    }

    /**
     * Returns (a copy of) this distribution's covariance matrix.
     *
     * @return The covariance matrix.
     */
    public double[][] covariance() {
        return distribution.covariance();
    }

    /**
     * Returns the dimension of the vectors handled by this distribution.
     *
     * @return The dimension of the vectors handled by this distribution.
     */
    public int dimension() {
        return distribution.dimension();
    }

    @Override
    public double probability(ObservationVector o) {
        if (o.dimension() != distribution.dimension()) {
            throw new IllegalArgumentException("Vector has a wrong dimension");
        }

        return distribution.probability(o.value);
    }

    @Override
    public ObservationVector generate() {
        return new ObservationVector(distribution.generate());
    }

    @Override
    public void fit(ObservationVector... oa) {
        fit(Arrays.asList(oa));
    }

    @Override
    public void fit(Collection<? extends ObservationVector> co) {
        if (co.isEmpty()) {
            throw new IllegalArgumentException("Empty observation set");
        }

        double[] weights = new double[co.size()];
        Arrays.fill(weights, 1.0d / co.size());

        fit(co, weights);
    }

    @Override
    public void fit(ObservationVector[] o, double... weights) {
        fit(Arrays.asList(o), weights);
    }

    @Override
    public void fit(Collection<? extends ObservationVector> co, double... weights) {
        if (co.isEmpty() || co.size() != weights.length) {
            throw new IllegalArgumentException();
        }

        // Compute mean
        for (int r = 0; r < dimension(); r++) {
            int i = 0;
            double meanr = 0.0d;
            for (ObservationVector o : co) {
                meanr += o.value[r] * weights[i++];
            }
            this.distribution.setMean(r, meanr);
        }

        // Compute covariance
        double[][] covariance = new double[dimension()][dimension()];
        int i = 0;
        for (ObservationVector o : co) {
            double[] obs = o.value;
            double[] omm = new double[obs.length];

            for (int j = 0; j < obs.length; j++) {
                omm[j] = obs[j] - this.distribution.mean(j);
            }

            for (int r = 0; r < dimension(); r++) {
                for (int c = 0; c < dimension(); c++) {
                    covariance[r][c] += omm[r] * omm[c] * weights[i];
                }
            }

            i++;
        }

        distribution.setCovariance(covariance);
    }

    @Override
    public OpdfMultiGaussian clone() throws CloneNotSupportedException {
        return new OpdfMultiGaussian(this.distribution.clone());
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return toString(NumberFormat.getInstance());
    }

    @Override
    public String toString(NumberFormat numberFormat) {
        StringBuilder sb = new StringBuilder("Multi-variate Gaussian distribution --- Mean: [ ");
        double[] mean = distribution.mean();
        for (int i = 0; i < mean.length; i++) {
            sb.append(numberFormat.format(mean[i]));
            sb.append(' ');
        }
        sb.append(']');
        return sb.toString();
    }
}
