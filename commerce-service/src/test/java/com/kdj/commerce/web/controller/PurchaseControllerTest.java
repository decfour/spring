package com.kdj.commerce.web.controller;

import com.kdj.commerce.exception.PermissionDeniedException;
import com.kdj.commerce.service.CartService;
import com.kdj.commerce.service.ItemService;
import com.kdj.commerce.service.PurchaseService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.core.MethodParameter;
import com.kdj.commerce.domain.member.Member;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PurchaseControllerTest {
    @Test
    void unknownPurchaseTypeDoesNotFallBackToCartOrder() {
        PurchaseService service = mock(PurchaseService.class);
        CartService carts = mock(CartService.class);
        PurchaseController controller = new PurchaseController(service, carts, mock(ItemService.class));

        assertThatThrownBy(() -> controller.createPurchase(mock(Member.class), "INVALID",
                null, null, "name", "address"))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(service, carts);
    }

    @Test
    void cartOrderPassesAuthenticatedMemberIdWithoutLoadingCartInController() {
        PurchaseService service = mock(PurchaseService.class);
        CartService carts = mock(CartService.class);
        PurchaseController controller = new PurchaseController(service, carts, mock(ItemService.class));
        Member member = mock(Member.class);
        when(member.getId()).thenReturn(1L);

        assertThat(controller.createPurchase(member, "CART", null, null, "name", "address"))
                .isEqualTo("redirect:/member/my-order");
        verify(service).purchaseCart(1L, "name", "address");
        verifyNoInteractions(carts);
    }

    @Test
    void permissionDeniedByServiceRedirectsToMyOrders() throws Exception {
        PurchaseService service = mock(PurchaseService.class);
        Member member = mock(Member.class);
        when(member.getId()).thenReturn(2L);
        doThrow(new PermissionDeniedException("본인의 주문만 취소할 수 있습니다."))
                .when(service).cancel(10L, 2L);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new PurchaseController(
                        service, mock(CartService.class), mock(ItemService.class)))
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.getParameterType() == Member.class;
                    }

                    @Override
                    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer container,
                                                  NativeWebRequest request, WebDataBinderFactory factory) {
                        return member;
                    }
                }).build();

        mvc.perform(post("/order/10/cancel"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/member/my-order"));
        verify(service).cancel(10L, 2L);
        verify(service, never()).findById(anyLong());
    }
}
