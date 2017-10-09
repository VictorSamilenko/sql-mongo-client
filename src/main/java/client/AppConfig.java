package client;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.io.IOException;
import java.util.Arrays;

@Configuration
@EnableWebMvc
@PropertySource(value={"classpath:application.properties"})
@ComponentScan(basePackages={"client"})
public class AppConfig
        extends WebMvcConfigurerAdapter {
    @Value(value="${user_name}")
    private String user;
    @Value(value="${database}")
    private String database;
    @Value(value="${password}")
    private String pass;
    @Value(value="${host}")
    private String host;
    @Value(value="${port}")
    private int port;

    @Bean
    public InternalResourceViewResolver internalResourceViewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/WEB-INF/");
        resolver.setSuffix(".html");
        return resolver;
    }

    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(new String[]{"/resources/**"}).addResourceLocations(new String[]{"/resources/"});
    }

    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    @Bean
    public MongoDatabase mongoDatabase() throws IOException {
        MongoCredential credential = null;
        MongoClientOptions.Builder builder = MongoClientOptions.builder().connectTimeout(300);
        if (this.user != null && this.pass != null && !"".equals(this.user) && !"".equals(this.pass)) {
            credential = MongoCredential.createCredential((String)this.user, (String)this.database, (char[])this.pass.toCharArray());
        }
        MongoClient mongo = credential != null ? new MongoClient(new ServerAddress(this.host, this.port), Arrays.asList(new MongoCredential[]{credential}), builder.build()) : new MongoClient(this.host, this.port);
        for (String s : mongo.listDatabaseNames()) {
            if (!s.equals(this.database)) continue;
            return mongo.getDatabase(this.database);
        }
        mongo.close();
        throw new RuntimeException("Database " + this.database + " unavailable!");
    }
}