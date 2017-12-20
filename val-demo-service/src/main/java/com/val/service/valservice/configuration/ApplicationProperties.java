package com.val.service.valservice.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("application")
public class ApplicationProperties {

    private String sample;
    private boolean refreshOnConfigChange;
    private int refreshDelay;
    private boolean enableRefresh;

    public String getSample() {
        return sample;
    }

    public void setSample(final String sample) {
        this.sample = sample;
    }

    public boolean isRefreshOnConfigChange() {
        return refreshOnConfigChange;
    }

    public void setRefreshOnConfigChange(final boolean refreshOnConfigChange) {
        this.refreshOnConfigChange = refreshOnConfigChange;
    }

    public int getRefreshDelay() {
        return refreshDelay;
    }

    public void setRefreshDelay(final int refreshDelay) {
        this.refreshDelay = refreshDelay;
    }

    public boolean isEnableRefresh() {
        return enableRefresh;
    }

    public void setEnableRefresh(final boolean enableRefresh) {
        this.enableRefresh = enableRefresh;
    }
}
