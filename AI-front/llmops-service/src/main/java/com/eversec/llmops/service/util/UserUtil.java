package com.eversec.llmops.service.util;

import com.eversec.framework.webboot.common.exception.BusinessException;

import com.eversec.llmops.service.interceptor.SecurityUserInterceptor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

import cn.hutool.core.util.URLUtil;

/**
 * 
 * @Description UserUtil.java
 * @Author luliang
 * @Date 2022年3月30日
 */
public class UserUtil {

    public static final String HEADER_USER_NAME_KEY = "username";
    public static final String HEADER_USER_ID_KEY = "userid";


    /**
     * 获取登录用户 username 网关传入 这个方法只有controller可以调用，保存时调用登录用户信息除外
     *
     * @return username
     */
    public static String getLoginUsername() {
        return verifyLogin(tryGetLoginUsername());
    }

    /**
     * 获取登录用户 userId 网关传入 这个方法只有controller可以调用，保存时调用登录用户信息除外
     *
     * @return userId
     */
    public static Long getLoginUserId() {
        return verifyLogin(tryGetLoginUserId());
    }

    /**
     * 获取登录用户 username 网关传入 这个方法只有controller可以调用，保存时调用登录用户信息除外
     *
     * @return username
     */
    public static String tryGetLoginUsername() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (!ObjectUtils.isEmpty(requestAttributes)) {
            HttpServletRequest request = requestAttributes.getRequest();
            // 先判断 header 中是否存在
            String username = request.getHeader(HEADER_USER_NAME_KEY);
            if (username!=null && !"".equals( username)) {
                return URLUtil.decode(username);
            }
        }
        return null;
    }

    /**
     * 获取登录用户 userId 网关传入 这个方法只有controller可以调用，保存时调用登录用户信息除外
     *
     * @return userId
     */
    public static Long tryGetLoginUserId() {
        RequestAttributes contextRequestAttributes = RequestContextHolder.getRequestAttributes();
        if (ObjectUtils.isEmpty(contextRequestAttributes)) {
            return null;
        }
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (!ObjectUtils.isEmpty(requestAttributes)) {
            HttpServletRequest request = requestAttributes.getRequest();
            // 先判断 header 中是否存在
            String userIdStr = request.getHeader(HEADER_USER_ID_KEY);
            if (StringUtils.hasText(userIdStr)) {
                return Long.parseLong(userIdStr);
            }
        }
        return null;
    }

    /**
     * 校验登录状态
     *
     * @param t
     * @param <T>
     * @return
     */
    private static <T> T verifyLogin(T t) {
        if (t != null) {
            return t;
        }
        throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), "用户未登录，请登录！");
    }


    //TODO
    public static HttpHeaders getHeaders(){
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "myapp_username=" + getAbUserUserName() + "; myapp_userid=" + getAbUserID());
        return headers;
    }



    public static Integer getAbUserID(HttpServletRequest  request){
        Object abUserIdObject = request.getAttribute(SecurityUserInterceptor.HEADER_AB_USER_ID_KEY);
        return abUserIdObject==null?null:Integer.parseInt(abUserIdObject.toString());
    }

    public static Integer getAbUserID(){
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        Object abUserIdObject = requestAttributes.getRequest().getAttribute(SecurityUserInterceptor.HEADER_AB_USER_ID_KEY);
        return abUserIdObject==null?null:Integer.parseInt(abUserIdObject.toString());
    }

    public static String getAbUserUserName(HttpServletRequest  request){
        Object abUserIdObject = request.getAttribute(SecurityUserInterceptor.HEADER_AB_USER_NAME_KEY);
        return abUserIdObject==null?null:abUserIdObject.toString();
    }

    public static String getAbUserUserName(){
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        Object abUserNameObject = null;
        if (requestAttributes != null) {
            abUserNameObject = requestAttributes.getRequest().getAttribute(SecurityUserInterceptor.HEADER_AB_USER_NAME_KEY);
        }
        return abUserNameObject==null?null:abUserNameObject.toString();
    }


    public static HttpHeaders getAbUserHeaders(){
        return getHeaders();
    }
}