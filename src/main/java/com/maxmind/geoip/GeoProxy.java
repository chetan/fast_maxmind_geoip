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

    private final File databaseFile;
    private long loadTime;

    private FastLookupService lookup;

    private final Thread thread;

    public GeoProxy(File databaseFile) throws IOException {
        this.databaseFile = databaseFile;
        createService();
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
        return lookup.getCountryCode(ipAddress);
    }

    protected void createService() throws IOException {
        FastLookupService service = new FastLookupService(databaseFile, LookupService.GEOIP_MEMORY_CACHE);
        this.loadTime = databaseFile.lastModified();
        this.lookup = service;
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

                if (databaseFile.lastModified() <= loadTime) {
                    continue; // don't update
                }
                try {
                    createService();
                } catch (IOException e) {
                    // creating new service failed, just wait for next interval
                }
            }
        }
    }

}
