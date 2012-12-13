package com.maxmind.geoip;

import java.io.File;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
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

    @Override
    void _check_mtime() {
    }

    /**
     * Finds the country index value given an IPv6 address.
     *
     * @param addr the ip address to find in long format.
     * @return the country index.
     */
    @Override
    protected int seekCountryV6(InetAddress addr) {
        byte [] v6vec = addr.getAddress();
        byte [] buf = new byte[2 * MAX_RECORD_LENGTH];
        int [] x = new int[2];
        int offset = 0;
        _check_mtime();
        for (int depth = 127; depth >= 0; depth--) {
            if ((dboptions & GEOIP_MEMORY_CACHE) == 1) {
                //read from memory
                for (int i = 0;i < 2 * MAX_RECORD_LENGTH;i++) {
                    buf[i] = dbbuffer[(2 * recordLength * offset)+i];
                }
            } else if ((dboptions & GEOIP_INDEX_CACHE) != 0) {
                //read from index cache
                for (int i = 0;i < 2 * MAX_RECORD_LENGTH;i++) {
                    buf[i] = index_cache[(2 * recordLength * offset)+i];
                }
            } else {
                //read from disk
                try {
                    file.seek(2 * recordLength * offset);
                    file.readFully(buf);
                }
                catch (IOException e) {
                    System.out.println("IO Exception");
                }
            }
            for (int i = 0; i<2; i++) {
                x[i] = 0;
                for (int j = 0; j<recordLength; j++) {
                    int y = buf[i*recordLength+j];
                    if (y < 0) {
                        y+= 256;
                    }
                    x[i] += (y << (j * 8));
                }
            }

            int bnum = 127 - depth;
            int idx = bnum >> 3;
                    int b_mask = 1 << ( bnum & 7 ^ 7 );
                    if ((v6vec[idx] & b_mask) > 0) {
                        if (x[1] >= databaseSegments[0]) {
                            last_netmask = 128 - depth;
                            return x[1];
                        }
                        offset = x[1];
                    }
                    else {
                        if (x[0] >= databaseSegments[0]) {
                            last_netmask = 128 - depth;
                            return x[0];
                        }
                        offset = x[0];
                    }
        }

        // shouldn't reach here
        System.err.println("Error seeking country while seeking " + addr.getHostAddress() );
        return 0;
    }

    /**
     * Finds the country index value given an IP address.
     *
     * @param ipAddress the ip address to find in long format.
     * @return the country index.
     */
    @Override
    protected int seekCountry(long ipAddress) {
        byte [] buf = new byte[2 * MAX_RECORD_LENGTH];
        int [] x = new int[2];
        int offset = 0;
        _check_mtime();
        for (int depth = 31; depth >= 0; depth--) {
            if ((dboptions & GEOIP_MEMORY_CACHE) == 1) {
                //read from memory
                for (int i = 0;i < 2 * MAX_RECORD_LENGTH;i++) {
                    buf[i] = dbbuffer[(2 * recordLength * offset)+i];
                }
            } else if ((dboptions & GEOIP_INDEX_CACHE) != 0) {
                //read from index cache
                for (int i = 0;i < 2 * MAX_RECORD_LENGTH;i++) {
                    buf[i] = index_cache[(2 * recordLength * offset)+i];
                }
            } else {
                //read from disk
                try {
                    file.seek(2 * recordLength * offset);
                    file.readFully(buf);
                }
                catch (IOException e) {
                    System.out.println("IO Exception");
                }
            }
            for (int i = 0; i<2; i++) {
                x[i] = 0;
                for (int j = 0; j<recordLength; j++) {
                    int y = buf[i*recordLength+j];
                    if (y < 0) {
                        y+= 256;
                    }
                    x[i] += (y << (j * 8));
                }
            }

            if ((ipAddress & (1 << depth)) > 0) {
                if (x[1] >= databaseSegments[0]) {
                    last_netmask = 32 - depth;
                    return x[1];
                }
                offset = x[1];
            }
            else {
                if (x[0] >= databaseSegments[0]) {
                    last_netmask = 32 - depth;
                    return x[0];
                }
                offset = x[0];
            }
        }

        // shouldn't reach here
        System.err.println("Error seeking country while seeking " + ipAddress);
        return 0;
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
