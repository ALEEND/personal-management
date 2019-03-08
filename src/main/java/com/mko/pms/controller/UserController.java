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
import java.util.*;


@RequestMapping(value = "pms")
@RestController
public class UserController extends BaseController {
    @Autowired
    private UserInfoRepository personRepository;

    @GetMapping("login")
    MKOResponse login(@RequestParam String tel, @RequestParam String password)
    {
        try {
            UserInfo userInfo = personRepository.userlogin(tel, password);
            if (userInfo == null) {
                return this.makeResponse(MKOResponseCode.DataNotFound, "用户不存在或已停用");
            }
            if (!userInfo.getPassword().equals(password)) {
                return this.makeBussessErrorResponse("密码不匹配");
            }
            List list=new ArrayList();{
                list.add(userInfo.getRole());
                list.add(userInfo.getId());
            }
            return this.makeSuccessResponse(list);

        } catch (Exception e) {
            e.printStackTrace();
            return makeBussessErrorResponse("其他异常！");
        }
    }


    @GetMapping("list")
    MKOResponse list(@RequestParam(defaultValue = "") String nameTel,
                     @RequestParam(defaultValue = "") Integer state,
                     PageRequest pageRequest,@RequestParam Integer id) {
        try {
            Optional<UserInfo> r = personRepository.findById(id);
            if ( r.get().getRole().equals(0)) {
                return makeResponse(MKOResponseCode.NoPermission, "", "无权限访问");
            }
            int count = pageRequest.getPageSize();
            int page = pageRequest.getPageNumber();
            Map<String, Object> userParam = new HashMap<>();
            String fields = "*";
            String condition = " ";
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

    @GetMapping("info")
    MKOResponse info(@RequestParam Integer id) {
        try {
            Optional<UserInfo> r = personRepository.findById(id);
            if (!r.isPresent()) {
                return makeResponse(MKOResponseCode.DataNotFound, "", "找不到数据");
            }
            if ( r.get().getRole().equals(0)) {
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

    @GetMapping("delete")
    MKOResponse delete(@RequestParam Integer id) {
        try {
            Optional<UserInfo> r = personRepository.findById(id);
            if (!r.isPresent()) {
                return makeResponse(MKOResponseCode.DataNotFound, "", "找不到数据");
            }
            if ( r.get().getRole().equals(0)) {
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
    @PostMapping ("add")
    MKOResponse add(@RequestBody UserInfo userInfoData,@RequestParam Integer id){
        try{
            Optional<UserInfo> r=personRepository.findById(id);
            if(r.get().getRole().equals(0)){
                return  makeResponse(MKOResponseCode.NoPermission,"","无权限访问");
            }
            if(userInfoData.getTel()==null || userInfoData.getTel().length()!=11){
                return makeResponse(MKOResponseCode.DataFormatError,"","格式错误");
            }
            UserInfo addResult=personRepository.findByTel(userInfoData.getTel());
            if(addResult != null){
                return makeResponse(MKOResponseCode.DataExist,"","数据已存在");
            }
            if(userInfoData.getPassword()==null || userInfoData.getTel()==null){
                return makeResponse(MKOResponseCode.ParamsLack,"","缺少参数");
            }
            UserInfo userInfo=new UserInfo();
            userInfo.setName(userInfoData.getName());
            userInfo.setTel(userInfoData.getTel());
            userInfo.setPassword(userInfoData.getPassword());
            userInfo.setAge(userInfoData.getAge());
            userInfo.setSex(userInfoData.getSex()==null ? 0:userInfoData.getSex());
            userInfo.setRole(userInfoData.getRole()==null ? 0 :userInfoData.getRole());
            userInfo.setGmtCreate(new Date());
            personRepository.save(userInfo);
            return makeSuccessResponse("已添加");
        }catch (Exception e){
            e.printStackTrace();
            return makeBussessErrorResponse("未知错误");
        }
    }

    @PostMapping("update")
    MKOResponse update(@RequestBody UserInfo userInfoData){
        try{

            if(userInfoData.getId()==null || userInfoData.getId()<=0){
                return makeParamsLackResponse("缺少参数或[id]小于等于0");
            }
            UserInfo updateResult=personRepository.chooseID(userInfoData.getId());
            if(updateResult==null){
                return makeResponse(MKOResponseCode.DataNotFound,"","找不到该数据");
            }
            UserInfo userInfo=new UserInfo();
            userInfo.setId(userInfoData.getId());
            userInfo.setTel(updateResult.getTel());
            userInfo.setName(userInfoData.getName()==null?  updateResult.getName():userInfoData.getName());
            userInfo.setPassword(userInfoData.getPassword()== null ? updateResult.getPassword():userInfoData.getPassword());
            userInfo.setAge(userInfoData.getAge()==null ? updateResult.getAge() : userInfoData.getAge());
            userInfo.setSex(userInfoData.getSex()==null ? updateResult.getSex(): userInfoData.getSex());
            userInfo.setRole(userInfoData.getRole()==null ? updateResult.getRole():userInfoData.getRole());
            userInfo.setState(userInfoData.getState() ==null ? updateResult.getState():userInfoData.getState());
            userInfo.setGmtCreate(new Date());
            personRepository.saveAndFlush(userInfo);
            return makeSuccessResponse("");
        }catch (Exception e){
            e.printStackTrace();
            return makeBussessErrorResponse("未知错误");
        }

    }
    public Object getRecord(String fields, String table, String condition, Map<String, Object> parameter, String orderBy, Integer page, Integer count){
        try {
            StringBuilder sqlCount = new StringBuilder(String.format("SELECT COUNT(*) count FROM %s WHERE 1=1 ", table));

            StringBuilder sql = new StringBuilder(String.format("SELECT %s FROM %s WHERE 1=1 ", fields, table));

            //构建sql
            if (!condition.isEmpty()) {
                sqlCount.append(condition);
                sql.append(condition);
            }

            Query queryCount = entityManager.createNativeQuery(sqlCount.toString());


            //分页
            sql.append(String.format(" %s LIMIT %s, %s", orderBy, ((page - 1) * count), count));
            Query query = entityManager.createNativeQuery(sql.toString());

            for(Map.Entry<String, Object> entry : parameter.entrySet()){
                queryCount.setParameter(entry.getKey(), entry.getValue());
                query.setParameter(entry.getKey(), entry.getValue());
            }

            Map<String, Object> result = (Map<String, Object>) queryCount.unwrap(NativeQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).getSingleResult();

            int total = Integer.valueOf(result.get("count").toString());

            if (total <= 0){
                return withoutData();
            }

            List list = query.unwrap(NativeQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).getResultList();

            return listToString(list, page, count, total);
        }catch (Exception e){
            e.printStackTrace();
            return withoutData();
        }
    }


}
