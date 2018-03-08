import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AbstractFrequencyAccessibilityEstimatorTest {
    @Test
    public void parseFrequencies_representativeInput() throws IOException {
        final String input = Stream.of(
                "# This is a comment line, and the next line is blank\n",
                "\n",
                "aardvark,2.1918854964100162e-8\n",
                "their,0.0011075003881958838\n",
                "zyzzyx,0\n"
        ).collect(Collectors.joining());
        final InputStream stream = new ByteArrayInputStream(input.getBytes());
        final Map<String, Double> actual =
                AbstractFrequencyAccessibilityEstimator.parseFrequencies(stream);
        final Map<String, Double> expected = new HashMap<>();
        expected.put("aardvark", 2.1918854964100162e-8);
        expected.put("their", 0.0011075003881958838);
        expected.put("zyzzyx", 0.0);
        Assert.assertEquals(expected, actual);
    }
}
