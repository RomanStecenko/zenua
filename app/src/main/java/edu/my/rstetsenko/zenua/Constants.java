package edu.my.rstetsenko.zenua;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Constants {

    private Constants() {}

    public static final String LOG_TAG = "ZenUA_TAG";
    public static final String EXTRA_SOURCE = "extra_source";

    public static final int PRIVATE = 0;
    public static final int MIN_FIN = 1;
    public static final int JSON_RATES = 2;
    public static final int OPEN_EXCHANGE_RATES = 3;
    public static final int FINANCE = 4;

    public static final Set<Integer> singleRates = new HashSet<>(Arrays.asList(new Integer[]{JSON_RATES, OPEN_EXCHANGE_RATES}));
//    public static final Set<Integer> doubleRates = new HashSet<Integer>(Arrays.asList(new Integer[]{PRIVATE, MIN_FIN, FINANCE}));
}
