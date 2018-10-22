package io.raspberrywallet.manager.common.wrappers;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDate;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class Token {
    
    @EqualsAndHashCode.Include
    private String data;
    
    /**
     * Since different UUID tokens couldn't have the same UUID,
     * then even with different expiration date they are the same.
     */
    @EqualsAndHashCode.Exclude
    private LocalDate expirationDate;
    
    public boolean isExpired() {
        if (expirationDate.compareTo(LocalDate.now()) < 0)
            return true;
        else
            return false;
    }
}
