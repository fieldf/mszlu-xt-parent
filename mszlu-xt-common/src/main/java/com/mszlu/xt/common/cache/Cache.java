package com.mszlu.xt.common.cache;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Cache {
    /**
     * 緩存前綴名稱
     * @return
     */
    String name() default "";

    /**
     * 過期時間 默認是60s
     * @return
     */
    int time() default 60;

    /**
     * 是否需要当前的用户作为key的一部分
     */
    boolean hasUser() default false;
}
