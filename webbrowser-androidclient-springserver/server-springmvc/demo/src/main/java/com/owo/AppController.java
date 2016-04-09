package com.owo;

import com.google.gson.Gson;
import com.owo.dao.mysql.ProgrammerMapper;
import com.owo.dao.mysql.entity.Customer;
import com.owo.dao.mysql.entity.Programmer;
import com.owo.entity.IntRetResponseBody;
import com.owo.entity.ProgrammerQueryResponseBody;
import com.owo.entity.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RequestMapping(value = "/programmer", produces = "application/json")
@Controller
public class AppController {

    @Resource
    ProgrammerMapper programmerMapper;

    @RequestMapping("query")
    @ResponseBody
    String query(HttpServletRequest request,
                 @RequestParam("name") String name,//
                 HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");

        List<Programmer> programmers = null;
        if (StringUtils.isEmpty(name)) {
            programmers = programmerMapper.selectAll();
        } else {
            programmers = programmerMapper.selectByName(name);
        }
        if (programmers == null) {
            programmers = new ArrayList<>();
        }
        ProgrammerQueryResponseBody programmerQueryResponseBody = new ProgrammerQueryResponseBody();
        programmerQueryResponseBody.setProgrammers(programmers);
        Response<ProgrammerQueryResponseBody> rsp = new Response<>();
        rsp.setData(programmerQueryResponseBody);
        return new Gson().toJson(rsp);
    }

    @RequestMapping("add")
    @ResponseBody
    String add(HttpServletRequest request,
               @RequestParam("name") String name,//
               @RequestParam("gender") String gender,//
               HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        Programmer p = new Programmer();
        p.setName(name);
        p.setGender(gender);
        Date d = new Date();
        p.setCreateTime(d);
        p.setUpdateTime(d);
        programmerMapper.insertSelective(p);
        Response<IntRetResponseBody> rsp = new Response<>();
        rsp.setCode(0);
        return new Gson().toJson(rsp);
    }

    @RequestMapping("update")
    @ResponseBody
    String update(HttpServletRequest request,
                  @RequestParam("id") int id,//
                  @RequestParam("name") String name,//
                  @RequestParam("gender") String gender,//
                  HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        Programmer p = new Programmer();
        p.setId(id);
        p.setName(name);
        p.setGender(gender);
        Date d = new Date();
        p.setUpdateTime(d);
        programmerMapper.updateByPrimaryKeySelective(p);
        Response<IntRetResponseBody> rsp = new Response<>();
        rsp.setCode(0);
        return new Gson().toJson(rsp);
    }

    @RequestMapping("delete")
    @ResponseBody
    String delete(HttpServletRequest request,
                  @RequestParam("id") int id,//
                  HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        programmerMapper.deleteByPrimaryKey(id);
        Response<IntRetResponseBody> rsp = new Response<>();
        rsp.setCode(0);
        return new Gson().toJson(rsp);
    }
}
