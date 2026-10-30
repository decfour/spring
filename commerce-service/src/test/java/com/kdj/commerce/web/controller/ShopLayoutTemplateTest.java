package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.cart.Cart;
import com.kdj.commerce.domain.cart.CartItem;
import com.kdj.commerce.domain.item.*;
import com.kdj.commerce.domain.member.*;
import com.kdj.commerce.web.dto.item.ItemForm;
import com.kdj.commerce.web.dto.review.ItemReviewForm;
import com.kdj.commerce.web.dto.purchase.PurchaseItemForm;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ShopLayoutTemplateTest {
    @ParameterizedTest
    @ValueSource(strings={"list","empty","detail","soldout","closed","form","edit","review-list","review-detail","review-form","cart","order"})
    void rendersSharedThemeWithoutChangingPurchaseOrReviewDestinations(String view) throws Exception {
        var resolver=new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/"); resolver.setSuffix(".html"); resolver.setCharacterEncoding("UTF-8");
        var engine=new SpringTemplateEngine(); engine.setTemplateResolver(resolver);
        var views=new ThymeleafViewResolver(); views.setTemplateEngine(engine); views.setCharacterEncoding("UTF-8");
        var mvc=MockMvcBuilders.standaloneSetup(new PreviewController()).setViewResolvers(views).build();
        String html=mvc.perform(get("/shop-preview/"+view)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html).contains("theme shop-page", "/css/theme.css", "/css/shop.css", "/css/button.css");
        if(view.equals("detail")) assertThat(html).contains("/order/one", "/cart/item/add", "name=\"itemCount\"");
        if(view.equals("soldout")||view.equals("closed")) assertThat(html).doesNotContain("action=\"/cart/item/add\"");
        if(view.equals("form")||view.equals("edit")) assertThat(html).contains("multipart/form-data", "name=\"imageFile\"");
        if(view.equals("cart")) assertThat(html).contains("/cart/item/", "/order/cart", "12,000원");
        if(view.equals("order")) assertThat(html).contains("/order/create", "name=\"receiverName\"", "name=\"receiverAddress\"");
        if(view.equals("review-detail")) assertThat(html).contains("/shop/item/10/review/1/edit", "/shop/item/10/review/1/delete");
        Files.createDirectories(Path.of("build/ui-shop"));
        Files.writeString(Path.of("build/ui-shop",view+".html"),html);
    }

    @Controller
    static class PreviewController {
        @GetMapping("/shop-preview/{view}")
        ModelAndView preview(@PathVariable String view) {
            var item=Item.create("여행길에 읽는 작은 안내서",6000,view.equals("soldout")?0:8,"가볍게 들고 다니기 좋은 여행 안내서입니다.\n새로운 길에서 이야기를 발견해보세요.",!view.equals("closed"),false,ItemType.BOOK,DeliveryType.STANDARD,1L,null,null);
            item.setId(10L);
            var member=mock(Member.class); when(member.getId()).thenReturn(1L); when(member.getMemberType()).thenReturn(MemberType.ADMIN);
            String template=switch(view) {
                case "list","empty"->"shop/list";
                case "detail","soldout","closed"->"shop/detail";
                case "form","edit"->"shop/form";
                case "cart"->"cart/cartList";
                case "order"->"purchase/purchaseForm";
                default->view.replace('-','/');
            };
            var form=new ItemForm();form.setId(10L);form.setName(item.getName());form.setPrice(6000);form.setStock(8);form.setDescription(item.getDescription());form.setItemType(ItemType.BOOK);form.setDeliveryType(DeliveryType.STANDARD);form.setOpen(true);
            var review=Map.of("id",1L,"title","여행할 때 읽기 좋은 책", "content","들고 다니기 편하고 내용이 알차요.\n다음 여행에도 챙겨가려 합니다.","creator",Map.of("id",2L,"name","여행자"),"createdAt",LocalDateTime.of(2026,10,31,10,0));
            var reviewForm=new ItemReviewForm();reviewForm.setId(1L);
            return new ModelAndView(template).addObject("item",view.equals("form")||view.equals("edit")?form:item)
                    .addObject("items",view.equals("empty")?Page.empty():new PageImpl<>(List.of(item,item,item,item,item,item,item,item),PageRequest.of(0,8),40))
                    .addObject("itemTypes",ItemType.values()).addObject("deliveryTypes",DeliveryType.values()).addObject("member",member)
                    .addObject("isEdit",view.equals("edit")).addObject("itemId",10L).addObject("review",review).addObject("reviews",new PageImpl<>(List.of(review,review)))
                    .addObject("reviewForm",reviewForm).addObject("cartItems",List.of(CartItem.create(mock(Cart.class),item,2)))
                    .addObject("purchaseItems",List.of(new PurchaseItemForm(item.getName(),6000,2))).addObject("purchaseType","ONE").addObject("quantity",2).addObject("totalPrice",12000);
        }
    }
}
