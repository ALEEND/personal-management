package com.mko.pms.controller;

import com.mko.pms.annotation.MKOUserPermission;
import com.mko.pms.enetity.UserInfo;
import com.mko.pms.repository.UserInfoRepository;
import com.mko.pms.util.MKOResponse;
import com.mko.pms.util.MKOResponseCode;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.*;
import java.util.concurrent.locks.Condition;

/**
 * @program: person manager system
 * @description: 功能模块
 * @author: Yuxz
 * @create: 2019-03-07
 **/
@RequestMapping(value = "pms")
@RestController
public class UserController extends BaseController {
    @Autowired
    private UserInfoRepository personRepository;


    /**
     * @program: person manager system
     * @description: 登陆模块
     * @author: Yuxz
     * @create: 2019-03-07
     **/
    @GetMapping("login")
    MKOResponse login(@RequestParam String tel, @RequestParam String password) {
        try {
            UserInfo userInfo = personRepository.userlogin(tel, password);
            if (userInfo == null) {
                return this.makeResponse(MKOResponseCode.DataNotFound, "用户不存在或已停用");
            }
            if (!userInfo.getPassword().equals(password)) {
                return this.makeBussessErrorResponse("密码不匹配");
            }
            Map<String,Object> map=new HashMap<>();
            map.put("id",userInfo.getId());
            map.put("role",userInfo.getRole());
            return this.makeSuccessResponse(map);
//            List list = new ArrayList();
//            {
//                list.add(userInfo.getRole());
//                list.add(userInfo.getId());
//            }
//            return this.makeSuccessResponse(list);

        } catch (Exception e) {
            e.printStackTrace();
            return makeBussessErrorResponse("其他异常！");
        }
    }


    /**
     * @program: person manager system
     * @description: 列表模块
     * @author: Yuxz
     * @create: 2019-03-07
     **/
    @GetMapping("list")
    MKOResponse list(@RequestParam(defaultValue = "") String nameAndTel,
                     @RequestParam(defaultValue = "-1") Integer state,
                     @RequestParam Integer userId,
                     Integer count,Integer page) {
        try {
            Optional<UserInfo> r = personRepository.findById(userId);
            if (r.get().getRole().equals(0)) {
                return makeResponse(MKOResponseCode.NoPermission, "", "无权限访问");
            }
            StringBuilder sqlCount=new StringBuilder("select count(*) count from pms where 1=1");
            StringBuilder sql=new StringBuilder("select *from pms where 1=1");
            String condition = " ";
            if (-1 != state) {
                condition = condition + ("AND state = " + state + " ");
            }
            if (!nameAndTel.equals("")|| nameAndTel.length()==0) {
                condition = condition + "AND (name like '%" + nameAndTel + "%' OR tel like '%" + nameAndTel + "%' ) ";
            }
            if(!condition.isEmpty()){
                sqlCount.append(condition);
                sql.append((condition));
            }
            Query queryCount=entityManager.createNativeQuery(sqlCount.toString());
            sql.append("LIMIT "+(page-1)*count +"," + count);
            Query query=entityManager.createNativeQuery(sql.toString());
            Map<String,Object> result=(Map<String,Object>) queryCount.unwrap(NativeQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).getSingleResult();
            int total=Integer.parseInt(result.get("count").toString());
            List list=query.unwrap(NativeQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).getResultList();
            Object Aresult=ListToString(list,page,count,total);
        return makeResponse(MKOResponseCode.Success, Aresult, "");
        } catch (Exception e) {
            e.printStackTrace();
            return makeBussessErrorResponse("未知错误");
        }
    }

    /**
     * @program: person manager system
     * @description: 详情模块
     * @author: Yuxz
     * @create: 2019-03-07
     **/
    @GetMapping("info")
    MKOResponse info(@RequestParam Integer id) {
        try {
            Optional<UserInfo> r = personRepository.findById(id);
            if (!r.isPresent()) {
                return makeResponse(MKOResponseCode.DataNotFound, "", "找不到数据");
            }
            if (r.get().getRole().equals(0)) {
                return makeResponse(MKOResponseCode.NoPermission, "", "无权限访问");
            }
            Optional<UserInfo> userResult = personRepository.findById(id);
            if (!userResult.isPresent()) {
                return makeResponse(MKOResponseCode.DataNotFound, "", "找不到数据");
            }
            return makeSuccessResponse(userResult.get());
        } catch (Exception e) {
            e.printStackTrace();
            return makeBussessErrorResponse("未知错误");
        }
    }

    /**
     * @program: person manager system
     * @description: 删除模块
     * @author: Yuxz
     * @create: 2019-03-07
     **/
    @GetMapping("delete")
    MKOResponse delete(@RequestParam Integer id) {
        try {
            Optional<UserInfo> r = personRepository.findById(id);
            if (!r.isPresent()) {
                return makeResponse(MKOResponseCode.DataNotFound, "", "找不到数据");
            }
            if (r.get().getRole().equals(0)) {
                return makeResponse(MKOResponseCode.NoPermission, "", "无权限访问");
            }
            Optional<UserInfo> userResult = personRepository.findById(id);
            if (!userResult.isPresent()) {
                return makeResponse(MKOResponseCode.DataNotFound, "", "找不到数据");
            }
            this.personRepository.delete(userResult.get());
            return makeSuccessResponse("");

        } catch (Exception e) {
            e.printStackTrace();
            return makeBussessErrorResponse("未知错误");
        }
    }

    /**
     * @program: person manager system
     * @description: 添加模块
     * @author: Yuxz
     * @create: 2019-03-07
     **/
    @PostMapping("add")
    MKOResponse add(@RequestBody UserInfo userInfoData, @RequestParam Integer id) {
        try {
            Optional<UserInfo> r = personRepository.findById(id);
            if (r.get().getRole().equals(0)) {
                return makeResponse(MKOResponseCode.NoPermission, "", "无权限访问");
            }
            if (userInfoData.getTel() == null || userInfoData.getTel().length() != 11) {
                return makeResponse(MKOResponseCode.DataFormatError, "", "格式错误");
            }
            UserInfo addResult = personRepository.findByTel(userInfoData.getTel());
            if (addResult != null) {
                return makeResponse(MKOResponseCode.DataExist, "", "数据已存在");
            }
            if (userInfoData.getPassword() == null || userInfoData.getTel() == null) {
                return makeResponse(MKOResponseCode.ParamsLack, "", "缺少参数");
            }
            UserInfo userInfo = new UserInfo();
            userInfo.setName(userInfoData.getName());
            userInfo.setTel(userInfoData.getTel());
            userInfo.setPassword(userInfoData.getPassword());
            userInfo.setAge(userInfoData.getAge());
            userInfo.setSex(userInfoData.getSex() == null ? 0 : userInfoData.getSex());
            userInfo.setRole(userInfoData.getRole() == null ? 0 : userInfoData.getRole());
            userInfo.setGmtCreate(new Date());
            personRepository.save(userInfo);
            return makeSuccessResponse("已添加");
        } catch (Exception e) {
            e.printStackTrace();
            return makeBussessErrorResponse("未知错误");
        }
    }

    /**
     * @program: person manager system
     * @description: 修改模块
     * @author: Yuxz
     * @create: 2019-03-07
     **/
    @PostMapping("update")
    MKOResponse update(@RequestBody UserInfo userInfoData) {
        try {

            if (userInfoData.getId() == null || userInfoData.getId() <= 0) {
                return makeParamsLackResponse("缺少参数或[id]小于等于0");
            }
            UserInfo updateResult = personRepository.chooseID(userInfoData.getId());
            if (updateResult == null) {
                return makeResponse(MKOResponseCode.DataNotFound, "", "找不到该数据");
            }
            UserInfo userInfo = new UserInfo();
            userInfo.setId(userInfoData.getId());
            userInfo.setTel(updateResult.getTel());
            userInfo.setName(userInfoData.getName() == null ? updateResult.getName() : userInfoData.getName());
            userInfo.setPassword(userInfoData.getPassword() == null ? updateResult.getPassword() : userInfoData.getPassword());
            userInfo.setAge(userInfoData.getAge() == null ? updateResult.getAge() : userInfoData.getAge());
            userInfo.setSex(userInfoData.getSex() == null ? updateResult.getSex() : userInfoData.getSex());
            userInfo.setRole(userInfoData.getRole() == null ? updateResult.getRole() : userInfoData.getRole());
            userInfo.setState(userInfoData.getState() == null ? updateResult.getState() : userInfoData.getState());
            userInfo.setGmtCreate(new Date());
            personRepository.saveAndFlush(userInfo);
            return makeSuccessResponse("");
        } catch (Exception e) {
            e.printStackTrace();
            return makeBussessErrorResponse("未知错误");
        }

    }

    public Object ListToString(List list,Integer page,Integer count ,int countNumber){
        Map<String,Object> map=new HashMap<String, Object>();
        //当前页数
        map.put("page",page);
        //总页数
        if(count==0){
            map.put("pageCount",count);
        }else {
            map.put("pageCount",countNumber/count);
        }
        //每页条数
        map.put("count",count);
        //总数
        map.put("countNumber",countNumber);
        //数据
        map.put("datas",list);
        return  map;
    }


}