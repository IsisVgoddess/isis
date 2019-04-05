package springapp.dom.email;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import springapp.dom.customer.Customer;

@DomainObject(nature=Nature.EXTERNAL_ENTITY)
@Entity 
@NoArgsConstructor(access = AccessLevel.PROTECTED) @RequiredArgsConstructor 
@ToString(exclude = {"customer"})
public class Email {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Getter 
    private Long id;
    
    @ManyToOne
    @Getter @Setter @NonNull 
    private Customer customer;
    
    @Getter @Setter @NonNull
    private String address;

    @Getter @Setter 
    private boolean verified;
   
    
}
