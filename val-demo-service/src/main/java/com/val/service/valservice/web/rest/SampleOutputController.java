package com.val.service.valservice.web.rest;

import com.val.service.valservice.configuration.ApplicationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class SampleOutputController {

    private ApplicationProperties applicationProperties;

    @Autowired
    public SampleOutputController(final ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    @GetMapping
    @RefreshScope
    public String getTheSampleProperty() {
        return applicationProperties.getSample();
    }
}
