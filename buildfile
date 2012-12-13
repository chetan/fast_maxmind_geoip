VERSION_NUMBER = "1.2.8-fast"
GROUP = "com.maxmind.geoip"
COPYRIGHT = "MaxMind LLC"

repositories.remote << "http://www.ibiblio.org/maven2/"

desc "A Fast MaxMind GeoIP implementation"
define "geoip" do

  project.version = VERSION_NUMBER
  project.group = GROUP
  manifest["Implementation-Vendor"] = COPYRIGHT
  compile.with # Add classpath dependencies
  resources
  test.compile.with # Add classpath dependencies
  test.resources
  package(:jar)
  package(:sources)
end
