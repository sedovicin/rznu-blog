package hr.fer.rznu.lab1.blog.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import hr.fer.rznu.lab1.blog.entities.User;

public interface UserRepository extends JpaRepository<User, String> {
}
