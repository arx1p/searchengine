package ru.gypsyjr.main;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties("conf")
public class Config {
    private List<String> sitesUrl = new ArrayList<>();
    private List<String> sitesName = new ArrayList<>();
    private String userAgent;
    private String referrer;

    public List<String> getSitesUrl() {
        return sitesUrl;
    }

    public void setSitesUrl(List<String> sitesUrl) {
        this.sitesUrl = sitesUrl;
    }

    public List<String> getSitesName() {
        return sitesName;
    }

    public void setSitesName(List<String> sitesName) {
        this.sitesName = sitesName;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getReferrer() {
        return referrer;
    }

    public void setReferrer(String referrer) {
        this.referrer = referrer;
    }
}
