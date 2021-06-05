
package purifierrentalpjt;

import lombok.Data;

@Data
public class OrderCancelAccepted extends AbstractEvent {

    private Long id;
    private Long orderId;
    private String status;

}

