package com.mko.pms.controller;

import com.mko.pms.repository.*;
import com.mko.pms.util.MKOResponse;
import com.mko.pms.util.MKOResponseCode;
import com.mko.pms.util.MKOResult;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Created by Kevin on 2/18/17.
 */
public class BaseController {

    @Autowired
    MessageSource messageSource;

    @PersistenceContext
    EntityManager entityManager;

//    @Autowired
//    UserInfoRepository employeeRepository;
//
//    @Autowired
//    ExploderRepository exploderRepository;
//
//    @Autowired
//    ReportedRecordRepository reportedRecordRepository;
//
//    @Autowired
//    ReportedRecordRepository recordRepository;

    private final static String SPECIAL_Token = "DGSC_Token_";
    private final static String SPECIAL_UserToken = "DGSC_UserToken_";


    public MKOResponse makeResponse(MKOResponseCode response, Object data) {
        MKOResponse result = new MKOResponse();
        result.put("code", response.getCode());
        result.put("response", data);
        result.put("msg", response.getDesc());
        return result;
    }

    public MKOResponse makeResponse(MKOResponseCode response, Object data, String desc) {
        MKOResponse result = new MKOResponse();
        result.put("code", response.getCode());
        result.put("response", data);
        if (desc != null) {
            result.put("msg", desc);
        }
        return result;
    }

    public MKOResponse makeSuccessResponse(Object data) {
        return this.makeResponse(MKOResponseCode.Success, data);
    }

    public MKOResponse makeResponseByMKOResult(MKOResult mkoResult) {
        MKOResponse result = new MKOResponse();
        if (mkoResult.success)
            result.put("code", MKOResponseCode.Success.getCode());
        else
            result.put("code", MKOResponseCode.BusinessError);
        if (mkoResult.getMessage() != null)
            result.put("msg", mkoResult.getMessage());
        return result;
    }

    public MKOResponse makeBussessErrorResponse(String desc) {
        return this.makeResponse(MKOResponseCode.BusinessError, "", desc);
    }

    public MKOResponse makeParamsLackResponse(String desc) {
        return this.makeResponse(MKOResponseCode.ParamsLack, "", desc);
    }

    public String MD5String(String str) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");

        md5.update(StandardCharsets.UTF_8.encode(str));
        return String.format("%032x", new BigInteger(1, md5.digest())).toLowerCase();
    }

    /**
     * @param list
     * @param page        当前页数
     * @param count       当前页数数量
     * @param countNumber 总数量
     * @return
     */
    public Object listToString(List list, int page, int count, int countNumber) {
        Map<String, Object> map = new HashMap<String, Object>();
        // 当前页数
        map.put("page", page);
        // 总页数
        if (count == 0) {
            map.put("pageCount", count);
        } else {
            map.put("pageCount", (list.size() - 1) / count + 1);
        }
        // 每页条数
        map.put("count", count);
        // 总数量
        map.put("countNumber", countNumber);
        // 数据
        map.put("datas", list);
        return map;
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
    // setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list()方法返回一个LIst,但是List中的字段是以Map形式封装的，
    // 但是该方法有一坑就是在页面端取数据时，Key的大小写一定要与数据库中的字段一致，否则，即使你查处结果，你也无法获取
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

    public Object withoutData(){
        return listToString(new ArrayList(), 1, 10, 0);
    }

    public Object convertPageResult(Page data) {

        Map<String, Object> map = new HashMap<String, Object>();
        // 当前页数
        map.put("page", data.getNumber() + 1);
        // 总页数
        map.put("pageCount", data.getTotalPages());
        // 当前页数数量
        map.put("count", data.getSize());
        // 总数量
        map.put("countNumber", data.getTotalElements());
        // 数据
        map.put("datas", data.getContent());

        return map;
    }

    @ResponseBody
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Object missingParamterHandler(MissingServletRequestParameterException exception) {
        return this.makeParamsLackResponse(String.format("缺少[%s]参数", exception.getParameterName()));
    }

    public static String unicode2String(String unicode) {
        StringBuffer string = new StringBuffer();
        String[] hex = unicode.split("\\\\u");
        for (int i = 1; i < hex.length; i++) {
            // 转换出每一个代码点
            int data = Integer.parseInt(hex[i], 16);
            // 追加成string
            string.append((char) data);
        }
        return string.toString();
    }

    public static String string2Unicode(String string) {
        StringBuffer unicode = new StringBuffer();
        for (int i = 0; i < string.length(); i++) {
            // 取出每一个字符
            char c = string.charAt(i);
            // 转换为unicode
            unicode.append("\\u" + Integer.toHexString(c));
        }
        return unicode.toString();
    }

    String check(BindingResult result) {
        StringBuffer msg = new StringBuffer();
        //获取错误字段集合
        List<FieldError> fieldErrors = result.getFieldErrors();
        //获取本地locale,zh_CN
        Locale currentLocale = LocaleContextHolder.getLocale();
        //一次只取一条错误信息返回，避免消息堆积
        String errorMessage = messageSource.getMessage(fieldErrors.get(0), currentLocale);
        msg.append(errorMessage);
        return msg.toString();
    }

}
