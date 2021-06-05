package purifierrentalpjt;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;

import lombok.Data;
import lombok.Getter;

import java.util.List;
import java.util.Date;

@Entity
@Table(name="Order_table")
@Data
public class Order {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String status;
    private Long productId;
    private String productName;
    private String installationAddress;
    private Long customerId;
    private String orderDate;
    private Long rentAmt;
    private String billStatus;

    @PostPersist
    public void onPostPersist(){

        JoinOrdered joinOrdered = new JoinOrdered();
        BeanUtils.copyProperties(this, joinOrdered);
        joinOrdered.publishAfterCommit();

    }

    @PostUpdate
    public void onPostUpdate(){
        /* 개인과제 */
    	System.out.println("### Payment Update and Update Event raised..." + this.getStatus());
        if(this.getBillStatus().equals("rentAmtPay")) {
            RentAmtPayed rentAmtPayed = new RentAmtPayed();
            BeanUtils.copyProperties(this, rentAmtPayed);
            rentAmtPayed.publishAfterCommit();

            purifierrentalpjt.external.Payment payment = new purifierrentalpjt.external.Payment();

            payment.setId(this.getId());

            OrderApplication.applicationContext.getBean(purifierrentalpjt.external.PaymentService.class).payMonthlyAmt(payment);
        }


    }

    /**
     * 주문삭제전, 이벤트발생
     */
    @PreRemove
    public void onPreRemove() {
    	CancelOrdered cancelOrdered = new CancelOrdered();
    	BeanUtils.copyProperties(this, cancelOrdered);
    	cancelOrdered.publishAfterCommit();
    }

}
