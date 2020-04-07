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

        @ConfigurationProperty(key = "collaboration.google.dir", defaultValue = "/dev/null")
        public String googleDir();

        @ConfigurationProperty(key = "collaboration.google.service.client.iss", defaultValue = "google@google.com")
        public String googleServiceClientISS();

        @ConfigurationProperty(key = "collaboration.google.service.client.subject", defaultValue = "ist@tecnico.pt")
        public String googleServiceClientSubject();

        @ConfigurationProperty(key = "collaboration.google.organization.id", defaultValue = "C1234")
        public String googleOrganizationId();

        @ConfigurationProperty(key = "collaboration.limesurvey.username", defaultValue = "admin")
        public String limeSurveyUsername();

        @ConfigurationProperty(key = "collaboration.limesurvey.password", defaultValue = "pass")
        public String limeSurveyPassword();
    }

    public static ConfigurationProperties getConfiguration() {
        return ConfigurationInvocationHandler.getConfiguration(ConfigurationProperties.class);
    }

}
