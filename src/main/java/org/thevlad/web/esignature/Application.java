package org.thevlad.web.esignature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.thevlad.web.esignature.model.EdocTemplate;
import org.thevlad.web.esignature.model.Role;
import org.thevlad.web.esignature.model.User;
import org.thevlad.web.esignature.repository.EdocTemplateRepository;
import org.thevlad.web.esignature.repository.RoleRepository;
import org.thevlad.web.esignature.repository.UserRepository;
import org.thevlad.web.esignature.service.TemplateProviderType;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public CommandLineRunner loadData(RoleRepository roleRepository, UserRepository userRepository,
			EdocTemplateRepository edocTemplateRepository, BCryptPasswordEncoder passwordEncoder) {
		return new CommandLineRunner() {

			@Override
			@Transactional
			public void run(String... args) throws Exception {
				Role userRole = null;
				Role adminRole = null;
				List<Role> roles = roleRepository.findAll();
				if (roles.isEmpty()) {
					userRole = new Role("ROLE_USER");
					adminRole = new Role("ROLE_ADMIN");
					roles = new ArrayList<Role>();
					roles.add(adminRole);
					roles.add(userRole);
					roles = roleRepository.saveAll(roles);
				} else {
					for (Role role : roles) {
						if (role.getName().equals("ROLE_USER")) {
							userRole = role;
						} else if (role.getName().equals("ROLE_ADMIN")) {
							adminRole = role;
						}
					}
				}
				User admin = userRepository.findByEmail("admin@m.com");
				if (admin == null) {
					admin = new User();
					admin.setFirstName("Admin");
					admin.setLastName("User");
					admin.setEmail("admin@m.com");
					admin.setPassword(passwordEncoder.encode("admin"));
					admin.setRoles(Arrays.asList(adminRole, userRole));
					userRepository.save(admin);
				}
				User user = userRepository.findByEmail("user@m.com");
				if (user == null) {
					user = new User();
					user.setFirstName("User");
					user.setLastName("User");
					user.setEmail("user@m.com");
					user.setPassword(passwordEncoder.encode("user"));
					user.setRoles(Arrays.asList(userRole));
					userRepository.save(user);
				}

				EdocTemplate locaEdocTemplate = edocTemplateRepository.findByTypeAndName(TemplateProviderType.LOCAL,
						"World Wide Corp");
				if (locaEdocTemplate == null) {
					locaEdocTemplate = new EdocTemplate();
					locaEdocTemplate.setType(TemplateProviderType.LOCAL);
					locaEdocTemplate.setName("World Wide Corp");
					locaEdocTemplate.setFileName("World_Wide_Corp_lorem.pdf");
//					locaEdocTemplate.se
					edocTemplateRepository.save(locaEdocTemplate);
				}
				EdocTemplate dealHubEdocTemplate = edocTemplateRepository
						.findByTypeAndName(TemplateProviderType.DEALHUB, "iCUvgNWKFqVBVLW2");
				if (dealHubEdocTemplate == null) {
					dealHubEdocTemplate = new EdocTemplate();
					dealHubEdocTemplate.setType(TemplateProviderType.DEALHUB);
					dealHubEdocTemplate.setName("iCUvgNWKFqVBVLW2");
					dealHubEdocTemplate.setFileName("DealHub_for_Verizon-4-Nov-2018-22-31-5.pdf");
					edocTemplateRepository.save(dealHubEdocTemplate);
				}
			}
		};

	}

}
