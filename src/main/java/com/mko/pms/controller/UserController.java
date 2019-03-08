package com.mko.pms.controller;

import com.mko.pms.annotation.MKOUserPermission;
import com.mko.pms.enetity.UserInfo;
import com.mko.pms.repository.UserInfoRepository;
import com.mko.pms.util.MKOResponse;
import com.mko.pms.util.MKOResponseCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@RequestMapping(value = "pms")
@RestController
public class UserController extends BaseController {
    @Autowired
    private UserInfoRepository personRepository;

    @GetMapping("login")
    MKOResponse login(@RequestParam String tel, @RequestParam String password)
//                      @RequestParam(value = "platform", defaultValue = "web") String platform)
    {
        try {

            UserInfo userInfo = personRepository.userlogin(tel, password);
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
            return makeBussessErrorResponse("其他异常！");
        }
    }


    @GetMapping("list")
    MKOResponse list(@RequestParam(defaultValue = "") String nameTel,
                     @RequestParam(defaultValue = "") Integer state,
                     PageRequest pageRequest, UserInfo userInfo) {
        try {
            if (userInfo.getRole().equals(0)) {
                return makeResponse(MKOResponseCode.NoPermission, "", "无权限查看用户列表");
            }
            int count = pageRequest.getPageSize();
            int page = pageRequest.getPageNumber();
            Map<String, Object> userParam = new HashMap<>();
            String fields = "*";
            String condition = "select *from pms where 1=1 ";
            if (!state.equals("")) {
                condition = condition + ("AND state = " + state + "");
            }
            if (!nameTel.equals("")) {
                condition = condition + "AND (name like '%" + nameTel + "%' OR tel like '%" + nameTel + "%' )";
            }
            String orderBy = "ORDER BY gmtCreate DESC";
            Object result = getRecord(fields, "pms", condition, userParam, orderBy, page, count);
            return makeSuccessResponse(result);
        } catch (Exception e) {
            e.printStackTrace();
            return makeBussessErrorResponse("未知错误");
        }
    }

    @GetMapping("Info")
    MKOResponse info(@RequestParam Integer id, UserInfo userInfo) {
        try {
            if (userInfo.getRole().equals(0)) {
                return makeResponse(MKOResponseCode.NoPermission, "", "无权限查看其他用户详情");
            }
            Optional<UserInfo> userResult = personRepository.findById(id);
            if (userResult.isPresent()) {
                return makeResponse(MKOResponseCode.DataNotFound, "", "找不到数据");
            }
            return makeSuccessResponse(userResult.get());
        } catch (Exception e) {
            e.printStackTrace();
            return makeBussessErrorResponse("未知错误");
        }
    }

    @GetMapping("delete")
    MKOResponse delete(@RequestParam Integer id, UserInfo userInfo) {
        try {
            if (userInfo.getRole().equals(0)) {
                return makeResponse(MKOResponseCode.NoPermission, "", "无权限访问");
            }
            Optional<UserInfo> userResult = personRepository.findById(id);
//             UserInfo userResult=personRepository.getOne(id);
            if (userResult.isPresent()) {
                return makeResponse(MKOResponseCode.DataNotFound, "", "找不到数据");
            }
            this.personRepository.delete(userResult.get());
            return makeSuccessResponse("");

        } catch (Exception e) {
            e.printStackTrace();
            return makeBussessErrorResponse("未知错误");
        }
    }
    @GetMapping("add")
    MKOResponse add(@RequestBody UserInfo userInfoData,String tel){
        try{
            UserInfo telResult =personRepository.findByTel(tel);
            if(telResult.equals("")){
                return makeResponse(MKOResponseCode.DataNotFound,"","找不到数据");
            }
            if(telResult.getTel().length()!=11){
                return makeResponse(MKOResponseCode.DataFormatError,"","格式错误");
            }
            UserInfo userInfo=new UserInfo();
            userInfo.setName(userInfoData.getName());
            userInfo.setTel(userInfoData.getTel());
            userInfo.setPassword(userInfoData.getPassword());
            userInfo.setAge(userInfoData.getAge());
            userInfo.setSex(userInfoData.getSex());
            userInfo.setRole(userInfoData.getRole());
            userInfo.setGmtCreate(new Date());
            personRepository.saveAndFlush(userInfo);
            return makeSuccessResponse("已添加");
        }catch (Exception e){
            e.printStackTrace();
            return makeBussessErrorResponse("未知错误");
        }
    }

    @GetMapping("update")
    MKOResponse update(@RequestBody UserInfo userInfoData,UserInfo userRole){
        try{
            if(userRole.getRole().equals(0)){
                return makeResponse(MKOResponseCode.NoPermission,"","无权限");
            }
            if(userInfoData.getId()==null || userInfoData.getId() <= 0){
                return makeParamsLackResponse("缺少参数或者[id]小于等于0");
            }
            Optional<UserInfo> userResult =personRepository.findById(userInfoData.getId());
            if(userResult.isPresent()){
                return makeResponse(MKOResponseCode.DataNotFound,"找不到数据");
            }
            UserInfo userInfo=new UserInfo();
            userInfo.setName(userInfoData.getName());
            userInfo.setPassword(userInfoData.getPassword());
            userInfo.setAge(userInfoData.getAge());
            userInfo.setSex(userInfoData.getSex());
            userInfo.setRole(userInfoData.getRole());
            userInfo.setState(userInfoData.getState());
            userInfo.setGmtCreate(new Date());
            personRepository.saveAndFlush(userInfo);
            return makeSuccessResponse(null);
        }catch (Exception e){
            e.printStackTrace();
            return makeBussessErrorResponse("未知错误");
        }

    }



}
