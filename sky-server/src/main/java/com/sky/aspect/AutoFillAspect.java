package com.sky.aspect;// 指定切面所在的包

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面，实现公共字段自动填充处理逻辑
 */
@Aspect// 指定该类为切面类
@Component// 将该类交给Spring容器管理
@Slf4j// 使用lombok提供的日志注解
public class AutoFillAspect {

    /**
     * 切入点
     */
    // 指定切入点表达式,表示切入点为mapper包下的所有方法
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){

    }

    /**
     * 前置通知，在通知中进行公共字段的赋值
     */
    @Before("autoFillPointCut()")//前置通知
    public void AutoFill(JoinPoint joinPoint){
        log.info("开始进行公共字段的自动填充处理...");


            /**
             * 根据主键动态修改属性
             * @param employee
             */
            //@AutoFill(value = OperationType.UPDATE)
            //void update (Employee employee);

        //获取当前被拦截到的目标方法上的数据库操作类型
        // 这三行代码的作用就是  获取到  eg:@AutoFill(value = OperationType.UPDATE)  此处的操作类型（update）
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();//方法签名对象
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);//获取方法上的注解对象
        OperationType operationType = autoFill.value();//获取数据库操作类型

        //获取到当前被拦截到的方法的参数--实体对象 eg:(xx employee);  //约定：此处默认参数第一个实体为要操作的实体对象
        Object[] args = joinPoint.getArgs();//获取方法的所有参数
        if(args == null || args.length == 0){//如果没有参数
            return;
        }
        Object entity = args[0];//此处不要使用Employ进行接受，后续可能会有其他实体对象，所以使用Object进行接受

        //准备赋值的数据
        LocalDateTime now = LocalDateTime.now();//获取当前时间
        Long currentUserId = BaseContext.getCurrentId();//获取当前用户id

        //根据当前不同的操作类型，为对应的属性通过反射来赋值
        if(operationType == OperationType.INSERT) {//如果是插入操作
            //为4个公共字段赋值（createTime，updateTime，createUser，updateUser）
            //通过反射进行复制 TODO 学习一下反射和 AOP
            try {
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);//获取所有的属性
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);//为属性赋值
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);//获取所有的属性
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);//为属性赋值

                //通过反射为属性赋值
                setCreateTime.invoke(entity, now);
                setCreateUser.invoke(entity, currentUserId);
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentUserId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (operationType == OperationType.UPDATE) {//如果是更新操作
            //为2个公共字段赋值（updateTime，updateUser）

            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);//为属性赋值

                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentUserId);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }
}
