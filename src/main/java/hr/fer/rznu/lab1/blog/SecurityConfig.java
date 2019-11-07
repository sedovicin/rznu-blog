package hr.fer.rznu.lab1.blog;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
@PropertySource(value = "classpath:restapi.properties")
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Value("${register}")
	private String registerPath;

	@Override
	public void configure(final WebSecurity web) {
		web.ignoring().antMatchers(registerPath);
	}
}
