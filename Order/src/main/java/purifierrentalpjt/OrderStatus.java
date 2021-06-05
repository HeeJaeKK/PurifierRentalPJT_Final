package purifierrentalpjt;

import javax.persistence.*;

import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name="OrderStatus_table")
public class OrderStatus {

        @Id
        @GeneratedValue(strategy=GenerationType.AUTO)
        private Long id;
        private String status;	// 상태정보
        private Long rentAmt; //총 납부해야할 금액

}
