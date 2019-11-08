package hr.fer.rznu.lab1.blog.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
@PropertySource(value = "classpath:restapi.properties")
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	BlogUserDetailsService blogUserDetailsService;

	@Value("${register}")
	private String registerPath;

	@Override
	public void configure(final WebSecurity web) {
		web.ignoring().antMatchers(registerPath);
	}

	@Override
	protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(blogUserDetailsService);
	}
}
