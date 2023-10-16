package com.sinnerschrader.skillwill.controller;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
public class SearchControllerTest {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private SearchController searchController;

  @ParameterizedTest
  @CsvSource({
    "java, London, Apple",
    "jpa, Paris, Google",
    "hibernate, Rome, Microsoft"
  })
  void getUsersTest(String skills, String location, String company) throws Exception {

    // given

    // when
    MockHttpServletResponse response = mvc.perform(get("/users")
      .queryParam("skills",skills)
      .queryParam("location", location)
      .queryParam("company", company)
    ).andDo(print()).andReturn().getResponse();

    // then
    assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

  }





}
