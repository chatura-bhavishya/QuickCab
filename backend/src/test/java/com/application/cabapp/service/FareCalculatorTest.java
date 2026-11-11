package com.application.cabapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

class FareCalculatorTest {

    private final FareCalculator fares = new FareCalculator();

    @Test
    void measuresStraightLineDistance() {
        // Bengaluru to Mysuru is about 128 km as the crow flies.
        double km = fares.distanceKm(12.9716, 77.5946, 12.2958, 76.6394);
        assertThat(km).isCloseTo(128.0, within(3.0));

        assertThat(fares.distanceKm(12.97, 77.59, 12.97, 77.59)).isZero();
    }

    @Test
    void shortTripsPayTheMinimum() {
        assertThat(fares.fareFor("car", 0.5)).isEqualTo(150);
        assertThat(fares.fareFor("auto", 1)).isEqualTo(90);
        assertThat(fares.fareFor("bike", 2)).isEqualTo(60);
    }

    @Test
    void longerTripsPayBasePlusPerKm() {
        assertThat(fares.fareFor("car", 10)).isEqualTo(50 + 15 * 10);
        assertThat(fares.fareFor("auto", 10)).isEqualTo(30 + 10 * 10);
        assertThat(fares.fareFor("bike", 10.4)).isEqualTo(Math.round(20 + 6 * 10.4));
    }

    @Test
    void minimumFareMatchesTheOldFlatFare() {
        assertThat(fares.minimumFare("car")).isEqualTo(150);
        assertThat(fares.minimumFare("auto")).isEqualTo(90);
        assertThat(fares.minimumFare("bike")).isEqualTo(60);
    }
}
