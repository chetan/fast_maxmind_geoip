# A Faster MaxMind GeoIP

While the MaxMind GeoIP library is thread-safe, it achieves thread-safety by adding the [synchronized keyword](https://github.com/chetan/fast_maxmind_geoip/blob/master/src/main/java/com/maxmind/geoip/LookupService.java#L1143) to various methods. This obviously slows things down quite dramatically in a heavily threaded environment (e.g., when used in a web service) due to thread contention on these methods.

This fork solves those issues, with a few caveats:

  * Only the IPv4 getCountry() APIs have been modified
  * IPv6 is supported via getCountryCode(), but uses the same old code paths
  * Optimized for in-memory cache use only
  * Database reloading has been disabled (extra method call/stat on every hit)
  * Netmask calculation has been disabled

## Modifications

The following modifications were made to the original LookupService class:

  * Implemented a faster ipToLong(string) method which uses no intermediate objects (i.e. InetAddress)
  * Removed *synchronized* keywords on seekCountry() and _check_mtime() methods
  * Modified seekCountry() - skip mtime check, ignore netmask, only use in-memory cache
  * Added getCountryCode() method
  * _check_mtime() is now a no-op; as such, updating the db during operation is not currently supported

## Usage

```java
FastLookupService geo = new FastLookupService("/path/to/db", FastLookupService.GEOIP_MEMORY_CACHE);
String countryCode = geo.getCountryCode("4.2.2.2");
```
