import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.ToDoubleFunction;

/**
 * <p>
 * A class to perform ordinary (least-squares) linear regression on data and evaluate the resulting
 * model on new inputs.
 * </p>
 * <p>
 * For simplicity of implementation, this performs only univariate (simple) linear regression,
 * and requires that the training set have positive variance in the output values.
 * </p>
 *
 * @param <T>
 *         the type of labeled data used to train and assess this regression
 */
final class SimpleOrdinaryLinearRegression<T> {
    private final ToDoubleFunction<T> xAccessor;
    private final ToDoubleFunction<T> yAccessor;
    double beta0;
    double beta1;

    /**
     * Create a regression with the given data accessors. The coefficients will be initialized to
     * zeros; call {@link #train(Collection)} to train the model.
     *
     * @param xAccessor
     *         a function that efficiently computes the <i>x</i>-value for a datum
     * @param yAccessor
     *         a function that efficiently computes the <i>y</i>-value for a datum
     */
    SimpleOrdinaryLinearRegression(ToDoubleFunction<T> xAccessor, ToDoubleFunction<T> yAccessor) {
        this.xAccessor = xAccessor;
        this.yAccessor = yAccessor;
    }

    /**
     * Train the model on the given dataset, overwriting any previous training data.
     *
     * @param data
     *         a dataset whose <i>y</i>-value variance is positive
     * @return {@code this}
     */
    SimpleOrdinaryLinearRegression<T> train(Collection<? extends T> data) {
        final List<T> dataList = new ArrayList<>(data);
        if (dataList.isEmpty()) {
            throw new AssertionError("empty data set");
        }
        final double meanX = dataList.stream().mapToDouble(xAccessor).average().getAsDouble();
        final double meanY = dataList.stream().mapToDouble(yAccessor).average().getAsDouble();
        beta1 = dataList.stream().mapToDouble(d ->
                (xAccessor.applyAsDouble(d) - meanX) * (yAccessor.applyAsDouble(d) - meanY)
        ).sum() / dataList.stream().mapToDouble(d ->
                Math.pow(xAccessor.applyAsDouble(d) - meanX, 2)
        ).sum();
        beta0 = meanY - beta1 * meanX;
        return this;
    }

    /**
     * Predict the value of <i>y</i> for a datum with the given <i>x</i>-value.
     *
     * @param x
     *         the input value
     * @return the least-squares prediction of the output value
     */
    double predict(double x) {
        return beta1 * x + beta0;
    }

    /**
     * Compute the mean-squared error on the given test set.
     *
     * @param data
     *         a dataset
     * @return the mean squared difference between a datum's predicted value and its actual
     * value, or {@link Double#NaN} if the dataset is empty
     */
    double mse(Collection<? extends T> data) {
        return data.stream()
                .mapToDouble(d -> {
                    final double predicted = predict(xAccessor.applyAsDouble(d));
                    final double actual = yAccessor.applyAsDouble(d);
                    return Math.pow(predicted - actual, 2);
                })
                .average().orElse(Double.NaN);
    }

    /**
     * Compute the mean-squared rounded error on the given test set.
     *
     * @param data
     *         a dataset
     * @return the mean squared difference between a datum's predicted value, after rounding, and
     * its actual value, or {@link Double#NaN} if the dataset is empty
     */
    double msre(Collection<? extends T> data) {
        return data.stream()
                .mapToDouble(d -> {
                    final double predicted = Math.round(predict(xAccessor.applyAsDouble(d)));
                    final double actual = yAccessor.applyAsDouble(d);
                    return Math.pow(predicted - actual, 2);
                })
                .average().orElse(Double.NaN);
    }
}
