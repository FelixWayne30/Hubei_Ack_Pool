package com.example.hubeiatlasbackend.common;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import java.io.File;

@Configuration
public class PicturePublish extends WebMvcConfigurerAdapter {
    @Value("${pictures.location}")
    private String picture_loc;

    @Value("${pictures.url}")
    private String picture_url;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射URL为/images/**时到指定文件夹
        registry.addResourceHandler(picture_url+"**").addResourceLocations("file:"+picture_loc + File.separator);
    }
}
