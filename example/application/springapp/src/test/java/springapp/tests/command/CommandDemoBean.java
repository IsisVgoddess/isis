package springapp.tests.command;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.val;
import springapp.dom.customer.Customer;
import springapp.dom.customer.CustomerRepository;
import springapp.dom.email.Email;
import springapp.dom.email.EmailRepository;
import springapp.dom.email.EmailVerificationService;

@Service @Transactional
public class CommandDemoBean {
	
	@Inject EmailRepository emailRepository;
	@Inject CustomerRepository customerRepository;
	@Inject EmailVerificationService emailVerificationService;
	//@Inject PlatformTransactionManager platformTransactionManager;
	
	
	public void setUp() {
		val customer = customerRepository.save(new Customer("Jack", "Bauer"));
		val email = emailRepository.save(new Email(customer, "j.bauer@icu.fiction"));
	}
	
	public void setUpVerified() {
		val customer = customerRepository.save(new Customer("Jack", "Bauer"));
		
		val email = new Email(customer, "j.bauer@icu.fiction");
		email.setVerified(true);
		emailRepository.save(email);
	}
	
	
	public void cleanup() {
		emailRepository.deleteAll();
		customerRepository.deleteAll();
	}

}
