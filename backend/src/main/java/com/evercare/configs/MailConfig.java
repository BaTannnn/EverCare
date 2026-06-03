package com.evercare.configs;

import java.util.Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
@PropertySource("classpath:configs.properties")
public class MailConfig {

    @Autowired
    private Environment env;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(env.getProperty("mail.host", "smtp.gmail.com"));
        mailSender.setPort(env.getProperty("mail.port", Integer.class, 587));
        mailSender.setUsername(env.getProperty("mail.username"));
        mailSender.setPassword(env.getProperty("mail.password"));

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", env.getProperty("mail.transport.protocol", "smtp"));
        props.put("mail.smtp.auth", env.getProperty("mail.smtp.auth", "true"));
        props.put("mail.smtp.starttls.enable", env.getProperty("mail.smtp.starttls.enable", "true"));
        props.put("mail.smtp.connectiontimeout", env.getProperty("mail.smtp.connectiontimeout", "5000"));
        props.put("mail.smtp.timeout", env.getProperty("mail.smtp.timeout", "5000"));
        props.put("mail.smtp.writetimeout", env.getProperty("mail.smtp.writetimeout", "5000"));

        return mailSender;
    }
}
