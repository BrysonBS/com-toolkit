package cn.com.toolkit.framework.core.annotation;

import cn.com.toolkit.framework.core.enums.IsolationEnum;
import cn.com.toolkit.framework.core.enums.PropagationEnum;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Transaction {
    PropagationEnum propagation() default PropagationEnum.REQUIRED;
    IsolationEnum isolation() default IsolationEnum.DEFAULT;
    int timeout() default -1;
    boolean readOnly() default false;
    Class<? extends Throwable>[] rollbackFor() default {};
    Class<? extends Throwable>[] noRollbackFor() default {};
}
