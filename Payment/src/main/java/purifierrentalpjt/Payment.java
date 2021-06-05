package purifierrentalpjt;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Date;

@Entity
@Getter
@Setter
@Table(name="Payment_table")
public class Payment {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long orderId;
    private Long billId;
    private Long customerId;
    private String status;
    private Long rentAmt;

    @PostPersist
    public void onPostPersist(){
        PaymentCompleted paymentCompleted = new PaymentCompleted();
        paymentCompleted.setId(this.getId());
        paymentCompleted.setOrderId(this.getOrderId());
        paymentCompleted.setBillId(this.getBillId());
        paymentCompleted.setCustomerId(this.getCustomerId());
        paymentCompleted.setRentAmt(this.getRentAmt());
        paymentCompleted.setStatus(this.getStatus());
        BeanUtils.copyProperties(this, paymentCompleted);
        paymentCompleted.publishAfterCommit();

    }
}
