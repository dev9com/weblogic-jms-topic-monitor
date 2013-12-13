package com.dynacrongroup

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory

import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator

class SettingsConf {
    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();
    private static final Config BASE_CONFIG = ConfigFactory.load();
    private static final ObjectMapper MAPPER = buildObjectMapper();
    private static final SettingsConf SETTINGS_CONF = buildTestConfSingleton(BASE_CONFIG);

    @Valid
    private String destination

    @Valid
    private String url

    @Valid
    private String connectionFactory

    @Valid
    private String initialContextFactory

    public static SettingsConf getSoleInstance() {
        return SETTINGS_CONF;
    }

    def String getDestination() {
        return destination
    }

    def String getUrl() {
        return url
    }

    def String getConnectionFactory() {
        return connectionFactory
    }

    def String getInitialContextFactory() {
        return initialContextFactory
    }

    private static ObjectMapper buildObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    private static SettingsConf buildTestConfSingleton(Config environmentConfig) {
        Map<String, Object> unwrappedConfig = environmentConfig.root().unwrapped();
        SettingsConf conf = MAPPER.convertValue(unwrappedConfig, SettingsConf.class);
        Set<ConstraintViolation<SettingsConf>> constraintViolations = VALIDATOR.validate(conf);
        if (!constraintViolations.isEmpty()) {
            StringBuilder message = new StringBuilder();
            message.append("Configuration is invalid.  The following problems must be corrected for the application to start:\n");
            for (ConstraintViolation<SettingsConf> violation : constraintViolations) {
                message.append("  ");
                message.append(violation.getPropertyPath());
                message.append(" : ");
                message.append(violation.getMessage());
                message.append('\n');
            }
            throw new IllegalStateException("Application's test configuration file invalid.");
        }
        return conf;
    }
}
