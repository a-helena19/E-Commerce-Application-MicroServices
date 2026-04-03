package at.fhv.springcloudgateway.rest.dtos;

public class GatewayInfo {
    public String name;
    public String port;
    public String description;
    public String version;

    public GatewayInfo(String name, String port, String description, String version) {
        this.name = name;
        this.port = port;
        this.description = description;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public String getPort() {
        return port;
    }

    public String getDescription() {
        return description;
    }

    public String getVersion() {
        return version;
    }
}
