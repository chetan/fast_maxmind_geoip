package com.maxmind.geoip;

import java.io.File;
import java.io.IOException;

public class FastLookupService extends LookupService {

    private static final String UNKNOWN_COUNTRY_CODE = "--";
    private static final char DOT = '.';

    public FastLookupService(File databaseFile, int options) throws IOException {
        super(databaseFile, options);
    }

    public FastLookupService(File databaseFile, String licenseKey) throws IOException {
        super(databaseFile, licenseKey);
    }

    public FastLookupService(File databaseFile) throws IOException {
        super(databaseFile);
    }

    public FastLookupService(int options, String licenseKey) throws IOException {
        super(options, licenseKey);
    }

    public FastLookupService(String databaseFile, int options) throws IOException {
        super(databaseFile, options);
    }

    public FastLookupService(String databaseFile, String licenseKey) throws IOException {
        super(databaseFile, licenseKey);
    }

    public FastLookupService(String databaseFile) throws IOException {
        super(databaseFile);
    }

    /**
     * Returns the country code the IP address is in.
     *
     * @param ipAddress String version of an IP address, i.e. "127.0.0.1"
     * @return the 2 letter country code
     */
    public String getCountryCode(String ipAddress) {
        int ret = seekCountry(ipToLong(ipAddress)) - COUNTRY_BEGIN;
        return (ret == 0 ? UNKNOWN_COUNTRY_CODE : countryCode[ret]);
    }

    @Override
    public int last_netmask() {
        throw new RuntimeException("not implemented in FastLookupService");
    }

    /**
     * Returns the long version of an IP address given as a String object.
     *
     * @param ip String
     * @return long form of IP
     */
    private static long ipToLong(String ip) {
        long num = 0;
        long res = 0;
        int pos = 0;
        int a = 0;
        char c;
        long y;
        for (int i = 0; i < ip.length(); i++) {
            c = ip.charAt(i);
            if (c == DOT) {
                y = -res;
                if (y < 0) {
                    y+= 256;
                }
                num += y << ((3-a)*8);
                pos = 0;
                res = 0;
                a++;
                continue;
            }
            res *= 10;
            res -= Character.digit(c, 10);
            pos++;
        }
        if (a != 3) {
            return 0;
        }
        y = -res;
        if (y < 0) {
            y+= 256;
        }
        num += y << ((3-a)*8);

        return num;
    }

}
