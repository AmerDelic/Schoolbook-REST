//package com.amerd.schoolbook.resource;
//
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.context.web.WebAppConfiguration;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//import org.springframework.web.context.WebApplicationContext;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
//import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
//
//@WithMockUser
//@SpringBootTest
//@WebAppConfiguration
//class UserResourceTest {
//
//    @Autowired
//    private WebApplicationContext context;
//
//    protected MockMvc mvc;
//
//    @BeforeEach
//    public void setup() {
//        mvc = MockMvcBuilders
//                .webAppContextSetup(context)
//                .apply(springSecurity())
//                .build();
//    }
//
//    @AfterEach
//    void tearDown() {
//    }
//
////    @Test
////    void getAll() {
////        mvc.perform()
////
////    }
//
//    @Test
//    void create() {
//    }
//
//    @Test
//    void login() {
//    }
//
//    @Test
//    void edit() {
//    }
//
//    @Test
//    void resetPassword() {
//    }
//}