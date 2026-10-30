package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.member.MemberType;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TalkDetailTemplateTest {
 @Test
 void retainsPostAndCommentActionsAndEscapesText() throws Exception {
  var resolver=new ClassLoaderTemplateResolver();resolver.setPrefix("templates/");resolver.setSuffix(".html");resolver.setCharacterEncoding("UTF-8");
  var engine=new SpringTemplateEngine();engine.setTemplateResolver(resolver);
  var views=new ThymeleafViewResolver();views.setTemplateEngine(engine);views.setCharacterEncoding("UTF-8");
  var mvc=MockMvcBuilders.standaloneSetup(new PreviewController()).setViewResolvers(views).build();
  String html=mvc.perform(get("/talk-detail-preview")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
  assertThat(html).contains("/css/talk.css","panel post-card","panel comment-item","/community/post/1/edit","/community/post/1/delete","/community/post/1/like","/comment/1/delete","/comment/1/add","&lt;script&gt;")
          .doesNotContain("surface-card","col-empty");
  assertThat(html.indexOf("panel post-card")).isLessThan(html.indexOf("class=\"detail-meta\""));
  Files.createDirectories(Path.of("build/ui-talk"));Files.writeString(Path.of("build/ui-talk/detail.html"),html);
 }
 @Controller
 static class PreviewController {
  @GetMapping("/talk-detail-preview")
  ModelAndView preview(){
   var creator=Map.of("id",1L,"name","산책자");var now=LocalDateTime.of(2026,10,31,10,0);
   var post=Map.of("id",1L,"title","길 위에서 발견한 작은 이야기","content","오늘은 익숙한 길에서 새로운 풍경을 발견했어요.\n".repeat(8),"creator",creator,"createdAt",now,"viewCount",15,"likeCount",5);
   var comment=Map.of("id",1L,"content","저도 걸어보고 싶네요. <script>문자 그대로 표시</script>","creator",creator,"createdAt",now);
   return new ModelAndView("community/detail").addObject("post",post).addObject("loginMember",Map.of("id",1L,"memberType",MemberType.ADMIN))
           .addObject("comments",new PageImpl<>(List.of(comment,comment),PageRequest.of(0,10),30));
  }
 }
}
