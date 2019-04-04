package springapp.tests.command;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import springapp.boot.test.SpringBootTestApplication;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {SpringBootTestApplication.class, CommandDemoBean.class})
@AutoConfigureTestDatabase
@EnableAsync
@TestMethodOrder(OrderAnnotation.class)
class CommandTest {

	//@Inject AsyncExecutionService asyncExecutionService;

	@Test
	void shouldAllowTaskCancellation() {


	}


}
