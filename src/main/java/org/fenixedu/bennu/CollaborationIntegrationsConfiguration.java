package org.fenixedu.bennu;

import org.fenixedu.bennu.spring.BennuSpringModule;
import org.fenixedu.commons.configuration.ConfigurationInvocationHandler;
import org.fenixedu.commons.configuration.ConfigurationManager;
import org.fenixedu.commons.configuration.ConfigurationProperty;

@BennuSpringModule(basePackages = "org.fenixedu.collaboration", bundles = "CollaborationIntegrationsResources")
public class CollaborationIntegrationsConfiguration {

    @ConfigurationManager(description = "Collaboration Integrations Configuration")
    public interface ConfigurationProperties {
        @ConfigurationProperty(key = "collaboration.azure.tenentId", defaultValue = "tenentId")
        public String tenentId();

        @ConfigurationProperty(key = "collaboration.azure.clientId", defaultValue = "clientId")
        public String clientId();

        @ConfigurationProperty(key = "collaboration.azure.clientSecret", defaultValue = "clientSecret")
        public String clientSecret();

        @ConfigurationProperty(key = "collaboration.azure.organization.id", defaultValue = "123")
        public String organizationId();

        @ConfigurationProperty(key = "collaboration.azure.organization.prefix", defaultValue = "XPTO")
        public String organizationPrefix();

        @ConfigurationProperty(key = "collaboration.azure.organization.domain", defaultValue = "@domain.pt")
        public String organizationDomain();
    }

    public static ConfigurationProperties getConfiguration() {
        return ConfigurationInvocationHandler.getConfiguration(ConfigurationProperties.class);
    }

}
