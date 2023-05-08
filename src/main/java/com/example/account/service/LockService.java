package com.example.account.service;

import com.example.account.exception.AccountException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import static com.example.account.type.ErrorCode.ACCOUNT_TRANSACTION_LOCK;
import static java.util.concurrent.TimeUnit.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class LockService {
    private final RedissonClient redissonClient;

    public void lock(String accountNumber) {
        RLock lock = redissonClient.getLock(getLockKey(accountNumber));
        log.debug("Trying lock for accountNumber : {}", accountNumber);

        try {
            boolean isLock = lock.tryLock(1, 15, SECONDS);
            if (!isLock) {
                log.error("=========== Lock acquisition failed ===========");
                throw new AccountException(ACCOUNT_TRANSACTION_LOCK);
            }
        } catch (AccountException e) {
            throw e;
        } catch (Exception e) {
            log.error("Redis lock failed", e);
        }
    }
    public void unlock(String accountNumber) {
        log.debug("*Unlock for accountNumber : {}", accountNumber);
        redissonClient.getLock(getLockKey(accountNumber)).unlock();
    }
    private static String getLockKey(String accountNumber) {
        return "ACLK:" + accountNumber;
    }
}
