package com.compass.yuhengapi.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author liuyu
 */
@Slf4j
@Controller
@RequestMapping("")
public class IndexController {

  @GetMapping(value = "")
  public ModelAndView index(HttpServletRequest httpServletRequest, ModelAndView modelAndView) {
    modelAndView.addObject("contextPath", httpServletRequest.getContextPath());
    modelAndView.setViewName("index");
    return modelAndView;
  }


}
