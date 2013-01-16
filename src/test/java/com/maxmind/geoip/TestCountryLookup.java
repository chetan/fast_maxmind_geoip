package com.maxmind.geoip;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import junit.framework.TestCase;

import org.junit.Test;

public class TestCountryLookup extends TestCase {

    String ipv6ip = "2a01:7e00::f03c:91ff:fedf:3a21";

    @Test
    public void testLookup() throws IOException, URISyntaxException {

        URL dat = this.getClass().getClassLoader().getResource("GeoIP.dat");

        LookupService old = new LookupService(new File(dat.toURI()),
                LookupService.GEOIP_MEMORY_CACHE);

        FastLookupService lookup = new FastLookupService(new File(dat.toURI()),
                LookupService.GEOIP_MEMORY_CACHE);

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(new GZIPInputStream(
                        this.getClass().getClassLoader().getResourceAsStream("ip.txt.gz"))));

        String ip = null;
        while ((ip = reader.readLine()) != null) {
            String code = old.getCountry(ip).getCode();
            String newCode = lookup.getCountryCode(ip);
            assertEquals(code + " is expected for " + ip + ";", code, newCode);
        }

    }

    @Test
    public void testLookupVsCLibrary() throws IOException, URISyntaxException {

        URL dat = this.getClass().getClassLoader().getResource("GeoIP.dat");

        FastLookupService lookup = new FastLookupService(new File(dat.toURI()),
                LookupService.GEOIP_MEMORY_CACHE);

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(new GZIPInputStream(
                        this.getClass().getClassLoader().getResourceAsStream("test.txt.gz"))));

        String line = null;
        String dash = "--";
        while ((line = reader.readLine()) != null) {
            String[] split = line.split("\\t");
            String ip = split[0];
            String code = dash;
            if (split.length == 2) {
                code = split[1];
            }

            String newCode = lookup.getCountryCode(ip);
            assertEquals(code + " is expected for " + ip + ";", code, newCode);
        }

    }

    @Test
    public void testIPV6Lookup() throws IOException, URISyntaxException {

        URL dat = this.getClass().getClassLoader().getResource("GeoIPv6.dat");

        LookupService old = new LookupService(new File(dat.toURI()),
                LookupService.GEOIP_MEMORY_CACHE);

        FastLookupService lookup = new FastLookupService(new File(dat.toURI()),
                LookupService.GEOIP_MEMORY_CACHE);

        String code = old.getCountryV6(ipv6ip).getCode();
        String newCode = lookup.getCountryCode(ipv6ip);
        assertEquals(code + " is expected for " + ipv6ip + ";", code, newCode);
        assertEquals("GB", newCode);

    }

    @Test
    public void testGeoProxyLookup()  throws IOException, URISyntaxException {

        URL datV4 = this.getClass().getClassLoader().getResource("GeoIP.dat");
        URL datV6 = this.getClass().getClassLoader().getResource("GeoIPv6.dat");

        GeoProxy proxy = new GeoProxy(new File(datV4.toURI()), new File(datV6.toURI()));

        assertEquals("GB", proxy.getCountryCode(ipv6ip));
        assertEquals("--", proxy.getCountryCode("127.0.0.1"));
        assertEquals("US", proxy.getCountryCode("4.2.2.2"));
    }

}
