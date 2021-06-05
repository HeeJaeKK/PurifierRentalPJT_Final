package purifierrentalpjt;

import lombok.Data;

@Data
public class RentAmtPayed extends AbstractEvent {

    private Long id;
    private String status;
    private Long customerId;
    private Long billId;
    private String RentAmt;

}
