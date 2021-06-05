package purifierrentalpjt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;

 @RestController
 public class PaymentController {
    @Autowired
    PaymentRepository paymentRepository;

    /**
     * 납부 완료
     * @param payment
     */
    @RequestMapping(method=RequestMethod.POST, path="/payment")
    public void surveyCompletion(@RequestBody Payment payment) {
    	
    	System.out.println( "### 동기호출 -납부완료");

    	Optional<Payment> opt = paymentRepository.findByOrderId(payment.getOrderId());
    	if( opt.isPresent()) {
    		Payment payCompleted =opt.get();
    		payCompleted.setStatus("rentAmtPayed");
    		paymentRepository.save(payCompleted);
    	} else {
    		System.out.println("### 납부 대상을 찾지 못했습니다.");
    	}
    }
 }
