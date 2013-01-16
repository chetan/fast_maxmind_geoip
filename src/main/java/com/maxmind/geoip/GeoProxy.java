package com.maxmind.geoip;

import java.io.File;
import java.io.IOException;

/**
 * Simple proxy for {@link FastLookupService} which uses a background thread to check for an
 * updated database file at one hour increments.
 *
 * @author chetan
 *
 */
public class GeoProxy {

    // one hour
    private static final long UPDATE_INTERVAL = 3600*1000;

    private final File databaseFileV4;
    private final File databaseFileV6;
    private long loadTimeV4;
    private long loadTimeV6;

    private FastLookupService lookupV4;
    private FastLookupService lookupV6;

    private final Thread thread;

    /**
     * Create new GeoProxy service
     *
     * @param databaseFileV4
     * @param databaseFileV6
     * @throws IOException
     */
    public GeoProxy(File databaseFileV4, File databaseFileV6) throws IOException {
        this.databaseFileV4 = databaseFileV4;
        this.databaseFileV6 = databaseFileV6;
        createServices();
        this.thread = new UpdateThread();
        this.thread.start();
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
            return lookupV6.getCountryCodeV6(ipAddress);
        } else {
            // ipv4
            return lookupV4.getCountryCodeV4(ipAddress);
        }
    }

    protected void createServices() throws IOException {
        FastLookupService serviceV4 = new FastLookupService(databaseFileV4, LookupService.GEOIP_MEMORY_CACHE);
        this.loadTimeV4 = databaseFileV4.lastModified();
        this.lookupV4 = serviceV4;

        FastLookupService serviceV6 = new FastLookupService(databaseFileV6, LookupService.GEOIP_MEMORY_CACHE);
        this.loadTimeV6 = databaseFileV6.lastModified();
        this.lookupV6 = serviceV6;
    }

    private class UpdateThread extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(UPDATE_INTERVAL);
                } catch (InterruptedException e) {
                    return;
                }

                if (databaseFileV4.lastModified() <= loadTimeV4 ||
                        databaseFileV6.lastModified() <= loadTimeV6) {

                    continue; // don't update
                }
                try {
                    createServices();
                } catch (IOException e) {
                    // creating new service failed, just wait for next interval
                }
            }
        }
    }

}
