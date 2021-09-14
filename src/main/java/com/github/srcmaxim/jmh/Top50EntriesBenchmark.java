package com.github.srcmaxim.jmh;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.math.BigDecimal;
import java.util.*;
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
        final List<Product> top50 = testData.products.stream()
                .takeWhile(Product::isActive)
                .sorted(Comparator.comparing(Product::getPrice).reversed())
                .limit(50)
                .collect(Collectors.toList());
        bh.consume(top50);
    }

    @Benchmark
    public void topPricesIterable(TestData testData, Blackhole bh) {
        var byPriceAsc = Comparator.comparing(Product::getPrice);
        var maxElements = 50 + 1;
        var top50 = new PriorityQueue<>(maxElements, byPriceAsc);
        BigDecimal minTopPrice = BigDecimal.ZERO;
        for (Product product : testData.products) {
            if (product.isActive()) {
                if (product.getPrice().compareTo(minTopPrice) > 0
                        && top50.offer(product)
                        && top50.size() == maxElements) {
                    BigDecimal minPrice;
                    if (!minTopPrice.equals(minPrice = top50.poll().getPrice()))
                        minTopPrice = minPrice;
                }
            }
        }
        bh.consume(top50);
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
