package cn.fintecher.pangolin.gateway.filter;


import cn.fintecher.pangolin.gateway.model.User;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by ChenChang on 2017/3/4.
 */
public class AccessFilter extends ZuulFilter {

    private static Logger log = LoggerFactory.getLogger(AccessFilter.class);
    @Autowired
    RestTemplate restTemplate;

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    public boolean shouldFilter() {
        return true;
    }

    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();

        log.debug(String.format("%s request to %s", request.getMethod(), request.getRequestURL().toString()));
        String accessToken = request.getHeader("X-UserToken");
        if (StringUtils.contains(request.getRequestURL().toString(), "api/login/login")
                || StringUtils.contains(request.getRequestURL().toString(), "swagger")
                || StringUtils.contains(request.getRequestURL().toString(), "api-docs")
                || StringUtils.containsIgnoreCase(request.getRequestURL().toString(), "HashCode")
                || StringUtils.containsIgnoreCase(request.getRequestURL().toString(), "resourceController")
                || StringUtils.containsIgnoreCase(request.getRequestURL().toString(), "dataDictController")) {
            return null;
        }
        if (StringUtils.isBlank(accessToken)) {
            log.debug("access token is empty");
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(403);
            return null;
        }
        try {
            ResponseEntity<User> entity = restTemplate.getForEntity("http://business-service/api/userResource/getUserByToken?token=" + accessToken, User.class);
            if (!entity.hasBody()) {
                log.debug("access token is wrong");
                ctx.setSendZuulResponse(false);
                ctx.setResponseStatusCode(403);
                return null;
            }
        } catch (Exception e) {
            log.debug("access token is wrong");
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(403);
            return null;
        }
        log.debug("access token ok");
        return null;
    }


}
