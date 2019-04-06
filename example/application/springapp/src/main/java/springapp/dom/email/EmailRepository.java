package springapp.dom.email;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.repository.CrudRepository;

public interface EmailRepository extends JpaRepository<Email, Long> {
	
	List<Email> findByVerified(boolean verified);
	long countByVerified(boolean verified);
	
}
