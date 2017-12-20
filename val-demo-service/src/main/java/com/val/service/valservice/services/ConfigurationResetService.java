package com.val.service.valservice.services;

import java.net.URI;

import com.val.service.valservice.configuration.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.context.restart.RestartEndpoint;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class ConfigurationResetService {

    private static final Logger log = LoggerFactory.getLogger(ConfigurationResetService.class);
    private String config;
    private boolean firstFetch;

    @Value("${spring.cloud.config.uri}")
    private String configServerUri;

    @Value("${spring.application.name}")
    private String applicationName;

    private RestTemplate restTemplate;
    private ApplicationContext applicationContext;
    private RestartEndpoint restartEndpoint;
    private Environment environment;
    private ApplicationProperties applicationProperties;
    private ScheduledAnnotationBeanPostProcessor postProcessor;

    @Autowired
    public ConfigurationResetService(final RestTemplate restTemplate, final Environment environment, final ApplicationContext applicationContext, final RestartEndpoint restartEndpoint, final ApplicationProperties applicationProperties, ScheduledAnnotationBeanPostProcessor postProcessor) {
        this.restTemplate = restTemplate;
        this.applicationContext = applicationContext;
        this.restartEndpoint = restartEndpoint;
        this.environment = environment;
        this.applicationProperties = applicationProperties;
        this.postProcessor = postProcessor;
    }


    @Scheduled(fixedDelayString = "${application.refreshDelay:5000}")
    public void checkForConfigChange() {
        if (!applicationProperties.isEnableRefresh()) {
            postProcessor.postProcessBeforeDestruction(this, "checkForConfigChange");
            log.info("The check for config change disabled.");
            return;
        }

        boolean shouldRefresh = shouldRefresh();

        if (shouldRefresh) {
            if (applicationProperties.isRefreshOnConfigChange()) {
                log.info("The application will refresh....");
                Thread restartThread = new Thread(() -> restartEndpoint.restart());
                restartThread.setDaemon(false);
                restartThread.start();
            } else {
                log.info("The application exiting due to a changed configuration.");
                SpringApplication.exit(applicationContext);
            }
        }
    }

    private boolean shouldRefresh() {
        boolean shouldRefresh = false;
        String environmentActiveProfiles = environment.getActiveProfiles().length == 0 ? "default" : String.join(",", environment.getActiveProfiles());
        UriComponents uriComponents = UriComponentsBuilder
                .fromHttpUrl(configServerUri)
                .path("/{applicationName}/{profiles}")
                .buildAndExpand(applicationName, environmentActiveProfiles);

        URI uri = uriComponents.toUri();
        log.debug("Checking for new config at [{}]", uri);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.debug("Fetched configuration [{}]", response.getBody());

                if (config == null) {
                    log.debug("Configuration been set to [{}].", response.getBody());
                    config = response.getBody();
                } else {
                    if (firstFetch) {
                        firstFetch = false;
                        log.info("Configuration was not up when the application started.");
                        shouldRefresh = true;
                    }
                    if (!response.getBody().equals(config)) {
                        log.info("Configuration has changed to [{}]", response.getBody());
                        config = null;
                        shouldRefresh = true;
                    }
                }
            } else {
                log.error("Config server returned a non 2xx code [{}]. \nHeaders [{}]\n Body [{}]", response.getStatusCode(), response.getHeaders(), response.getBody());
            }
        } catch (final ResourceAccessException e) {
            log.error("Couldn't fetch configuration, make sure that the host [{}] is available.", configServerUri);
            firstFetch = (config == null);
        }

        return shouldRefresh;
    }
}
