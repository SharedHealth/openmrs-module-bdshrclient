package org.openmrs.module.shrclient.handlers;

import org.openmrs.module.shrclient.util.FhirRestClient;
import org.openmrs.module.shrclient.util.Headers;
import org.openmrs.module.shrclient.util.PropertiesReader;
import org.openmrs.module.shrclient.util.RestClient;

import java.util.HashMap;
import java.util.Properties;


public class ServiceClientRegistry {
    private PropertiesReader propertiesReader;

    public ServiceClientRegistry(PropertiesReader propertiesReader) {

        this.propertiesReader = propertiesReader;
    }

    public RestClient getMCIClient() {
        Properties properties = propertiesReader.getMciProperties();
        String user = properties.getProperty("mci.user");
        String password = properties.getProperty("mci.password");

        return new RestClient(propertiesReader.getMciBaseUrl(),
                Headers.getBasicAuthHeader(user, password));
    }

    public FhirRestClient getSHRClient() {
        Properties properties = propertiesReader.getShrProperties();
        String user = properties.getProperty("shr.user");
        String password = properties.getProperty("shr.password");

        return new FhirRestClient(propertiesReader.getShrBaseUrl(),
                Headers.getBasicAuthHeader(user, password));
    }

    public RestClient getLRClient() {
        Properties properties = propertiesReader.getLrProperties();
        HashMap<String, String> headers = new HashMap<>();
        headers.put(properties.getProperty("lr.tokenName"), properties.getProperty("lr.tokenValue"));
        return new RestClient(propertiesReader.getLrBaseUrl(), headers);
    }

    public RestClient getFRClient() {
        Properties properties = propertiesReader.getFrProperties();
        HashMap<String, String> headers = new HashMap<>();
        headers.put(properties.getProperty("fr.tokenName"), properties.getProperty("fr.tokenValue"));
        return new RestClient(propertiesReader.getFrBaseUrl(), headers);
    }
}
