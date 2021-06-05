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
    @Autowired OrderRepository orderRepository;

    /**
     * 주문취소가 최종완료됬을때 처리
     * @param orderCancelAccepted
     */
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverOrderCancelAccepted_OrderCancelAccept(@Payload OrderCancelAccepted orderCancelAccepted){

        if(!orderCancelAccepted.validate()) return;

        System.out.println("\n\n##### listener OrderCancelAccept : " + orderCancelAccepted.toJson() + "\n\n");

        // Sample Logic //
        Order order = new Order();
        orderRepository.save(order);
    }
    
    /**
     * 가입처리가 완료됬을때 처리
     * @param joinCompleted
     */
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverJoinCompleted_JoinCompletionNotify(@Payload JoinCompleted joinCompleted){

        if(!joinCompleted.validate()) return;

        System.out.println("\n\n##### listener JoinCompletionNotify : " + joinCompleted.toJson() + "\n\n");

        // Sample Logic //
        Order order = new Order();
        orderRepository.save(order);
            
    }

    /**
     * 요금 납부가 완료되었을때 처리
     * @param paymentCompleted
     */
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPaymentCompleted_PayCompletionNotify(@Payload PaymentCompleted paymentCompleted){

        if(!paymentCompleted.validate()) return;

        System.out.println("\n\n##### listener PayCompletionNotify : " + paymentCompleted.toJson() + "\n\n");

        try {
            orderRepository.findById(paymentCompleted.getOrderId()).ifPresent(
                order -> {
                    order.setBillStatus("RentAmtPayed");
                    order.setRentAmt(paymentCompleted.getRentAmt());
                    orderRepository.save(order);
            });
        } catch(Exception e) {
            e.printStackTrace();
        }
            
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
