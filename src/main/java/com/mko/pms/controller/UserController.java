package com.mko.pms.controller;

import com.mko.pms.annotation.MKOUserPermission;
import com.mko.pms.enetity.UserInfo;
import com.mko.pms.repository.UserInfoRepository;
import com.mko.pms.util.MKOResponse;
import com.mko.pms.util.MKOResponseCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@RequestMapping(value = "pms")
@RestController
public class UserController extends BaseController {
    @Autowired
    private UserInfoRepository employeeRepository;

    @GetMapping("login")
    MKOResponse login(@RequestParam String tel, @RequestParam String password)
//                      @RequestParam(value = "platform", defaultValue = "web") String platform)
{
        try {

           UserInfo userInfo = employeeRepository.userlogin(tel, password);
            if (userInfo == null) {
                return this.makeResponse(MKOResponseCode.DataNotFound, "用户不存在或已停用");
            }
          if (!userInfo.getPassword().equals(password)) {
                return this.makeBussessErrorResponse("密码不匹配");
            }
//            if (platform.equals("web") && userInfo.getRole().equals(0)) {
//                return this.makeResponse(MKOResponseCode.NoPermission, "", "会员无权限登录此页面");
//            }
            return this.makeSuccessResponse(userInfo);

        } catch (Exception e) {
            e.printStackTrace();
            return makeBussessErrorResponse("其他异常！");}
    }
    @GetMapping("list")
    MKOResponse list(PageRequest pageRequest, UserInfo userInfo){
        try{
            if(userInfo.getRole().equals(0)){
                return  makeResponse(MKOResponseCode.NoPermission,"","无权限查看用户列表");
            }
            int count=pageRequest.getPageSize();
            int page=pageRequest.getPageNumber();
            Map<String,Object> userParam= new HashMap<>();

            String fields="*";
            String condition="";
            String orderBy = "ORDER BY gmtCreate DESC";
            Object result = getRecord(fields, "pms", condition, userParam, orderBy, page, count);
            return makeSuccessResponse(result);
        }catch (Exception e){
            e.printStackTrace();
            return makeBussessErrorResponse("未知错误");

        }
    }
    @MKOUserPermission
    @GetMapping("Info")
    MKOResponse info(@RequestParam Integer id,UserInfo userInfo) {
        try {
            if (userInfo.getRole().equals(0)) {
                return makeResponse(MKOResponseCode.NoPermission, "", "无权限查看其他用户详情");
            }
            Optional<UserInfo> userResult=employeeRepository.findById(id);
            if(userResult.isPresent()){
                return makeResponse(MKOResponseCode.DataNotFound,"","找不到数据");
            }
            return makeSuccessResponse(userResult.get());
        } catch (Exception e){
            e.printStackTrace();
            return makeBussessErrorResponse("未知错误");
            }
    }
//    @MKOUserPermission
//    @PostMapping("add")
//    MKOResponse add(@RequestParam )



}

