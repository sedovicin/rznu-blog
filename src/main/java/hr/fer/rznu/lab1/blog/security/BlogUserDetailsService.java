package hr.fer.rznu.lab1.blog.security;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import hr.fer.rznu.lab1.blog.entities.User;
import hr.fer.rznu.lab1.blog.repositories.UserRepository;

@Service
public class BlogUserDetailsService implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
		Optional<User> optionalUser = userRepository.findById(username);
		if (!(optionalUser.isPresent())) {
			throw new UsernameNotFoundException("No user found with username " + username);
		}
		User user = optionalUser.get();
		return blogUserToSpringUser(user);
	}

	public static UserDetails blogUserToSpringUser(final User user) {
		return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(),
				new ArrayList<SimpleGrantedAuthority>());
	}
}
