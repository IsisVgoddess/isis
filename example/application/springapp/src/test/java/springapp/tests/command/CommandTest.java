package springapp.tests.command;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.inject.Inject;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import lombok.val;
import lombok.extern.slf4j.Slf4j;
import springapp.boot.test.SpringBootTestApplication;
import springapp.dom.customer.CustomerRepository;
import springapp.dom.email.Email;
import springapp.dom.email.EmailRepository;
import springapp.dom.email.EmailVerificationService;
import springapp.tests.async.Futures;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {SpringBootTestApplication.class, CommandDemoBean.class})
@AutoConfigureTestDatabase
@EnableAsync
@TestMethodOrder(OrderAnnotation.class)
@Slf4j
class CommandTest {

	@Inject CommandDemoBean commandDemoBean;
	@Inject EmailRepository emailRepository;
	@Inject CustomerRepository customerRepository;
	@Inject EmailVerificationService emailVerificationService;
	
	static {
		System.setProperty("logging.level.org.springframework.transaction", "TRACE");
		System.setProperty("logging.level.org.springframework.orm", "TRACE");
	}
	
	@AfterEach
	void cleanup() {
		commandDemoBean.cleanup();
	}

	@Test @Order(1)
	void countEmails_not_verified() {

		commandDemoBean.setUp();
		
		val verifiedCount = emailRepository.countByVerified(true);
		val notVerifiedCount = emailRepository.countByVerified(false);

		System.out.println("verified: " + verifiedCount);
		System.out.println("not verified: " + notVerifiedCount);
		
		assertEquals(0, verifiedCount);
		assertEquals(1, notVerifiedCount);
			
	}
	
	@Test @Order(2)
	void countEmails_verified() {

		commandDemoBean.setUpVerified();
		
		val verifiedCount = emailRepository.countByVerified(true);
		val notVerifiedCount = emailRepository.countByVerified(false);

		System.out.println("verified: " + verifiedCount);
		System.out.println("not verified: " + notVerifiedCount);
		
		assertEquals(1, verifiedCount);
		assertEquals(0, notVerifiedCount);
			
	}
	
	@Test @Order(3)
	void emailVerification_sync() {

		commandDemoBean.setUp();
		
		for(Email email: emailRepository.findByVerified(false)) {
			
			// we want this to run within its own transaction
			// 'emailVerificationService' is managed by Spring, so the invocation honors any 
			// @Transactional annotation present on the method that gets invoked.
			emailVerificationService.startVerificationProcess(email);
			
			//backgroundService.execute(email).startVerificationProcess();
		}
		
		{
			val verifiedCount = emailRepository.countByVerified(true);
			val notVerifiedCount = emailRepository.countByVerified(false);
			System.out.println("verified: " + verifiedCount);
			System.out.println("not verified: " + notVerifiedCount);
			
			assertEquals(1, verifiedCount);
			assertEquals(0, notVerifiedCount);
		}
		
	}
	
	@Test @Order(4)
	void emailVerification_async() {

		commandDemoBean.setUp();
		
		val futures = new Futures<Void>();
		
		for(Email email: emailRepository.findByVerified(false)) {
			
			// we want this to run within its own transaction and in the background (async)
			// 'emailVerificationService' is managed by Spring, so the invocation honors any 
			// @Transactional and @Async annotation present on the method that gets invoked.
			val future = emailVerificationService.startVerificationProcessAsync(email);
			
			futures.add(future);
			
			//backgroundService.execute(email).startVerificationProcess();
		}
		
		log.info("join on the futures and wait for completion");
		
		// join on the futures and wait for completion
		futures.combine().join();

		log.info("all futures completed");
		
		{
			val verifiedCount = emailRepository.countByVerified(true);
			val notVerifiedCount = emailRepository.countByVerified(false);
			System.out.println("verified: " + verifiedCount);
			System.out.println("not verified: " + notVerifiedCount);
			
			assertEquals(1, verifiedCount);
			assertEquals(0, notVerifiedCount);
		}
		

		
	}
	

	


}
