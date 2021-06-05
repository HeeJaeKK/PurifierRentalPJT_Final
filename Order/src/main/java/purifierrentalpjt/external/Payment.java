package purifierrentalpjt.external;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Payment {

    private Long id;
    private Long orderId;
    private Long billId;
    private Long customerId;
    private String status;
    private String rentAmt;

}
