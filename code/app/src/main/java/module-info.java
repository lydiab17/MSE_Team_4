open module com.evote.app {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    // Spring Boot / Web
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.web;
    requires spring.beans;
    requires spring.core;
    requires java.sql;
    requires java.desktop;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires spring.aop;
    requires org.aspectj.weaver;
    requires org.slf4j;
    requires org.apache.tomcat.embed.core;
    requires jjwt;
    requires spring.security.core;
    requires spring.security.config;
    requires spring.security.web;
    requires io.github.resilience4j.annotations;
    requires io.github.resilience4j.ratelimiter;

    exports com.evote.app;
}
