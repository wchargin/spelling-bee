import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class SimpleOrdinaryLinearRegressionTest {

    private static SimpleOrdinaryLinearRegression<Point> modelFor(Point... data) {
        final SimpleOrdinaryLinearRegression<Point> result =
                new SimpleOrdinaryLinearRegression<>(Point::getX, Point::getY);
        result.train(Arrays.asList(data));
        return result;
    }

    @Test
    public void train_twoPoints() {
        final SimpleOrdinaryLinearRegression<Point> model = modelFor(
                new Point(1.0, 3.0), new Point(1.2, 3.4));
        Assert.assertEquals(1.0, model.beta0, 1e-6);
        Assert.assertEquals(2.0, model.beta1, 1e-6);
    }

    @Test
    public void predict_twoPoints() {
        final SimpleOrdinaryLinearRegression<Point> model = modelFor(
                new Point(1.0, 3.0), new Point(1.2, 3.4));
        Assert.assertEquals(3.2, model.predict(1.1), 1e-6);
    }

    @Test
    public void mse_twoPoints() {
        final SimpleOrdinaryLinearRegression<Point> model = modelFor(
                new Point(1.0, 3.0), new Point(1.2, 3.4));
        final List<Point> testSet = Arrays.asList(
                new Point(1.1, 3.7), new Point(1.3, 3.6));
        Assert.assertEquals(0.125, model.mse(testSet), 1e-6);
    }

    @Test
    public void msre_twoPoints_roundingHelps() {
        final SimpleOrdinaryLinearRegression<Point> model = modelFor(
                new Point(1.0, 3.0), new Point(1.2, 3.4));
        final List<Point> testSet = Arrays.asList(
                new Point(1.1, 3.0),  // predicts 3.2, round down to 3
                new Point(1.3, 5.0),  // predicts 3.6, round up to 4
                new Point(1.4, 5.0),  // predicts 3.8, round up to 4
                new Point(1.6, 4.0)   // predicts 4.2, round down to 4
        );
        Assert.assertEquals(0.5, model.msre(testSet), 1e-6);
    }

    @Test
    public void train_conflictingData() {
        final SimpleOrdinaryLinearRegression<Point> model = modelFor(
                new Point(1.0, 110.0),
                new Point(2.0, 118.0),
                new Point(2.0, 122.0),
                new Point(3.0, 130.0));
        Assert.assertEquals(100.0, model.beta0, 1e-6);
        Assert.assertEquals(10.0, model.beta1, 1e-6);
    }

    @Test
    public void predict_conflictingData() {
        final SimpleOrdinaryLinearRegression<Point> model = modelFor(
                new Point(1.0, 110.0),
                new Point(2.0, 118.0),
                new Point(2.0, 122.0),
                new Point(3.0, 130.0));
        Assert.assertEquals(120.0, model.predict(2.0), 1e-6);
    }

    @Test
    public void mse_conflictingData() {
        final SimpleOrdinaryLinearRegression<Point> model = modelFor(
                new Point(1.0, 110.0),
                new Point(2.0, 118.0),
                new Point(2.0, 122.0),
                new Point(3.0, 130.0));
        final List<Point> testSet = Arrays.asList(
                new Point(2.0, 115.0),
                new Point(2.0, 120.0),
                new Point(2.0, 120.0),
                new Point(2.0, 125.0));
        Assert.assertEquals(12.5, model.mse(testSet), 1e-6);
    }

    @Test
    public void msre_conflictingData_roundingHurts() {
        final SimpleOrdinaryLinearRegression<Point> model = modelFor(
                new Point(1.0, 110.0),
                new Point(2.0, 118.0),
                new Point(2.0, 122.0),
                new Point(3.0, 130.0));
        final List<Point> testSet = Arrays.asList(
                new Point(2.04, 120.4),
                new Point(2.04, 120.0),
                new Point(2.04, 120.0),
                new Point(2.04, 120.4));
        Assert.assertEquals(0.08, model.msre(testSet), 1e-6);
    }

    private static class Point {
        final double x;
        final double y;

        Point(double x, double y) {
            this.x = x;
            this.y = y;
        }

        double getX() {
            return x;
        }

        double getY() {
            return y;
        }
    }

}
