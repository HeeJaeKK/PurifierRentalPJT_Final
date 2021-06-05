package purifierrentalpjt;

import purifierrentalpjt.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired PaymentRepository paymentRepository;

    /**
     * 납부가 완료됬을때 처리
     * @param paymentCompleted
     */
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPaymentCompleted_PayCompletionNotify(@Payload PaymentCompleted paymentCompleted){

        if(!paymentCompleted.validate()) return;

        System.out.println("\n\n##### listener PayCompletionNotify : " + paymentCompleted.toJson() + "\n\n");

        try {
            paymentRepository.findById(paymentCompleted.getOrderId()).ifPresent(
                payment -> {
                    payment.setStatus("RentAmtPayed");
                    paymentRepository.save(payment);
            });
        } catch(Exception e) {
            e.printStackTrace();
        }
            
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
