package purifierrentalpjt;

import purifierrentalpjt.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class OrderStatusViewHandler {
    @Autowired
    private OrderStatusRepository orderStatusRepository;
    
    /**
     * 가입주문완료시
     * @param joinCompleted
     */
    @StreamListener(KafkaProcessor.INPUT)
    public void when_JoinCompletionNotify (@Payload JoinCompleted joinCompleted) {
    	System.out.println("###OrderStatusViewHandler- 가입주문완료시");
    	
        try {
        	if( joinCompleted.isMe()) {
	        	// view 객체 생성
	        	OrderStatus orderStatus = new OrderStatus();
	            orderStatus.setId		(	joinCompleted.getOrderId());
	            orderStatus.setStatus	(	joinCompleted.getStatus());
	            orderStatusRepository.save(orderStatus);
        	}
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    
    /**
     * 주문취소 완료시
     * @param orderCanceled
     */
    @StreamListener(KafkaProcessor.INPUT)
    public void when_OrderCanceled (@Payload OrderCanceled orderCanceled) {
    	System.out.println("###OrderStatusViewHandler- 주문취소 완료시");
    	
        try {
        	if( orderCanceled.isMe()) {
	        	// view 객체 생성
	        	OrderStatus orderStatus = new OrderStatus();
	            orderStatus.setId		(	orderCanceled.getId());
	            orderStatus.setStatus	(	orderCanceled.getStatus());
	            orderStatusRepository.save(orderStatus);
        	}
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * 개인과제
     * @param PaymentCompleted
     */
    @StreamListener(KafkaProcessor.INPUT)
    public void when_surveyCompletionNotify (@Payload PaymentCompleted paymentCompleted) {
    	System.out.println("###OrderStatusViewHandler- 요금 결제시");
    	
        try {
        	if( paymentCompleted.isMe()) {
	        	// view 객체 생성
	        	OrderStatus orderStatus = new OrderStatus();
	            orderStatus.setId		    (	paymentCompleted.getId());
	            orderStatus.setStatus	    (	paymentCompleted.getStatus());
                orderStatus.setRentAmt      (	paymentCompleted.getRentAmt());
	            orderStatusRepository.save(orderStatus);
        	}
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}

}