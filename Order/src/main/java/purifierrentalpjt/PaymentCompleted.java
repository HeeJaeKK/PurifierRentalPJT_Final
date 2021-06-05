
package purifierrentalpjt;

import lombok.Data;

@Data
public class PaymentCompleted extends AbstractEvent {

    private Long id;
    private Long orderId;
    private Long billId;
    private Long customerId;
    private String status;
    private Long rentAmt;

}

