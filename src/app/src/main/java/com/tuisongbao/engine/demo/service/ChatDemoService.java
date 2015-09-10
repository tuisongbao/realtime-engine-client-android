package com.tuisongbao.engine.demo.service;

/**
 * Created by user on 15-9-1.
 */

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

/**
 * Created by user on 15-8-17.
 */
@Rest(rootUrl = Constants.APIURL, converters = { StringHttpMessageConverter.class, FormHttpMessageConverter.class, MappingJackson2HttpMessageConverter.class  })
public interface ChatDemoService extends RestClientErrorHandling, RestClientHeaders {
    @Post("/getChatGroups?token={token}")
    List<DemoGroup> getGroupDemoInfo(LinkedMultiValueMap<String, String> groupIds, String token);

    @Get("/searchChatUsers?username={username}&token={token}")
    List<DemoUser> getDemoUser(String username, String token);
}

