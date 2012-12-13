package com.maxmind.geoip;

import java.io.File;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.UnknownHostException;

/**
 * A faster version of {@link LookupService}. Offers a single new method: getCountryCode()
 *
 * <p>NOTE: Retrieving the netmask is no longer support in this implementation and trying to do so
 * will result in a {@link RuntimeException}.
 * @author chetan
 *
 */
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
     * Returns the country code the IP address is in. Supports both IPv4 and IPv6 addresses.
     *
     * <p>Invalid IPs will return UNKNOWN_COUNTRY_CODE ("--") instead of throwing an exception.
     *
     * @param ipAddress String version of an IP address, i.e. "127.0.0.1"
     * @return the 2 letter country code
     */
    public String getCountryCode(String ipAddress) {

        if (ipAddress.charAt(0) == '[' || ipAddress.indexOf(':') >= 0) {
            // ipv6
            try {
                return getCountryV6(Inet6Address.getByName(ipAddress)).getCode();
            } catch (UnknownHostException e) {
                return UNKNOWN_COUNTRY_CODE;
            }

        } else {
            // ipv4
            int ret = seekCountry(ipToLong(ipAddress)) - COUNTRY_BEGIN;
            return (ret == 0 ? UNKNOWN_COUNTRY_CODE : countryCode[ret]);
        }


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
