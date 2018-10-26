package io.raspberrywallet.manager.common.wrappers;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;

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
    private LocalDateTime expirationDate;
    
    public Token(String data, int sessionLength) {
        this(data, LocalDateTime.now().plusSeconds(sessionLength));
    }
    
    public boolean isExpired() {
        return expirationDate.compareTo(LocalDateTime.now()) < 0;
    }
}
