
package purifierrentalpjt;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentCompleted extends AbstractEvent {

    private Long id;
    private Long orderId;
    private Long billId;
    private Long customerId;
    private String status;
    private Long rentAmt;

}

