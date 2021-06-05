package purifierrentalpjt;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface PaymentRepository extends CrudRepository<Payment, Long>{
    Optional<Payment> findByOrderId(Long orderId);
}
