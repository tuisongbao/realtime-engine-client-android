package com.tuisongbao.engine.demo.service;

/**
 * Created by user on 15-9-1.
 */

import com.squareup.okhttp.Response;
import com.tuisongbao.engine.demo.Constants;
import com.tuisongbao.engine.demo.bean.DemoGroup;
import com.tuisongbao.engine.demo.bean.DemoUser;

import org.androidannotations.annotations.rest.Get;
import org.androidannotations.annotations.rest.Post;
import org.androidannotations.annotations.rest.Rest;
import org.androidannotations.api.rest.RestClientErrorHandling;
import org.androidannotations.api.rest.RestClientHeaders;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;

import java.util.List;
import java.util.Map;

/**
 * Created by user on 15-8-17.
 */
@Rest(rootUrl = Constants.APIURL, converters = { StringHttpMessageConverter.class, FormHttpMessageConverter.class, MappingJackson2HttpMessageConverter.class  })
public interface ChatDemoService extends RestClientErrorHandling, RestClientHeaders {
    @Post("/validateChatUser")
    Response login(Map<String, String> data );

    @Post("/registerChatUser")
    String regist(Map<String, String> data );

    @Post("/getChatGroups")
    List<DemoGroup> getGroupDemoInfo(LinkedMultiValueMap<String, String> groupIds);

    @Get("/searchChatUsers?username={username}")
    List<DemoUser> getDemoUser(String username);
}

