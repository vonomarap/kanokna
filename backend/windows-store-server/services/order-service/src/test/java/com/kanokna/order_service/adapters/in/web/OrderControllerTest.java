package com.kanokna.order_service.adapters.in.web;

import com.kanokna.order_service.application.port.in.CheckoutPort;
import com.kanokna.order_service.application.port.in.OrderQueryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    MockMvc mockMvc;

    @Mock
    CheckoutPort checkoutPort;
    @Mock
    OrderQueryPort orderQueryPort;

    @BeforeEach
    void setUp() {
        OrderController controller = new OrderController(checkoutPort, orderQueryPort);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void placeOrder_requires_body() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getOrder_not_found_returns_404() throws Exception {
        Mockito.when(orderQueryPort.getOrder(Mockito.any(), Mockito.any())).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/v1/orders/123"))
            .andExpect(status().isNotFound());
    }
}
