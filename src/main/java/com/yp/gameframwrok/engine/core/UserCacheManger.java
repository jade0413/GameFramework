package com.yp.gameframwrok.engine.core;


import com.github.benmanes.caffeine.cache.Cache;
import com.yp.gameframwrok.model.cache.UserCache;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * @author yyp
 */
@Log4j2
@Service
public class UserCacheManger {

    @Autowired()
    @Qualifier("userCache")
    private Cache<Integer, UserCache> userCache;

    public void putUser(Integer userId, UserCache user) {                                                                                                                            
        userCache.put(userId, user);
    }

     public UserCache getUser(Integer userId) {
        return userCache.getIfPresent(userId);
    }

     public void removeUser(Integer userId) {
        userCache.invalidate(userId);
    }


}
