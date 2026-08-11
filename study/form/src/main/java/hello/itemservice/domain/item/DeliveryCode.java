package hello.itemservice.domain.item;

import lombok.AllArgsConstructor;
import lombok.Data;

/*
    1. FAST
    2. NORMAL
    3. SLOW
 */

@Data
@AllArgsConstructor
public class DeliveryCode {

    private String code;
    private String displayName;
}
