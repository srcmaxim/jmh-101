package lviv.javaclub.benchmark;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Fork(warmups = 1, value = 1)
@Measurement(iterations = 1)
@Warmup(iterations = 1)
public class Top50EntriesBenchmark {

    private static final int PRODUCTS_COUNT = 500;




    @State(Scope.Benchmark)
    public static class TestData {
        private List<Product> products = new ArrayList<>(PRODUCTS_COUNT);

        @Setup
        public void setup() {
            for (int i = 0; i < PRODUCTS_COUNT; i++) {
                final Product product = new Product();
                product.id = UUID.randomUUID();
                product.name = RandomStringUtils.randomAlphabetic(40);
                product.active = RandomUtils.nextBoolean();
                product.price = BigDecimal.valueOf(RandomUtils.nextDouble());
                products.add(product);
            }
            products.sort(Comparator.comparing(Product::isActive).reversed());
        }
    }

    @Benchmark
    public void sumPriceActiveProductsBigDecimalStream(TestData testData, Blackhole bh) {
        final BigDecimal sum = testData.products.stream()
                .filter(Product::isActive)
                .map(Product::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        bh.consume(sum);
    }

    @Benchmark
    public void sumPriceIterable(TestData testData, Blackhole bh) {
        BigDecimal sum = BigDecimal.ZERO;
        for (Product product : testData.products) {
            if (product.isActive()) {
                sum = sum.add(product.price);
            }
        }
        bh.consume(sum);
    }

    @Benchmark
    public void topPricesStream(TestData testData, Blackhole bh) {
        final List<Product> top10 = testData.products.stream()
                .takeWhile(Product::isActive)
                .sorted(Comparator.comparing(Product::getPrice).reversed())
                .limit(10)
                .collect(Collectors.toList());
        bh.consume(top10);
    }

    @Benchmark
    public void topPricesIterable(TestData testData, Blackhole bh) {
        List<Product> active = new ArrayList<>(testData.products.size());
        for (Product product : testData.products) {
            if (product.isActive()) {
                active.add(product);
            } else {
                break;
            }
        }
        active.sort(Comparator.comparing(Product::getPrice).reversed());
        List<Product> top10 = active.subList(0, Math.min(10, active.size()));
        bh.consume(top10);
    }

    private static class Product {
        private UUID id;
        private String name;
        private boolean active;
        private BigDecimal price;

        public boolean isActive() {
            return active;
        }

        public BigDecimal getPrice() {
            return price;
        }
    }

}
