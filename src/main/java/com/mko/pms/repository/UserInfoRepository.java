package com.mko.pms.repository;

import com.mko.pms.enetity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.Date;

public interface UserInfoRepository extends JpaRepository<UserInfo,Integer> {

    @Query(value="select *from pms WHERE tel=?1",nativeQuery = true)
    UserInfo findByTel(String tel);


    @Transactional
    @Query(value = "select *from pms WHERE tel=?1 and password=?2 and state=1",nativeQuery = true)
    UserInfo userlogin(String tel,String password);
    @Query(value ="select  *from pms where role=?1",nativeQuery = true)
    UserInfo fingByrole(Integer role);
//    @Modifying
//    @Transactional
//    @Query(value = "UPDATE pms SET gmtCreate = ?2 WHERE tel = ?1", nativeQuery = true)
//    void updategmtCreate(String tel, Date gmtCreate);


    @Transactional
    @Query(value = "UPDATE pms SET password = ?1 WHERE id = ?2", nativeQuery = true)
    void updatePassword(String password, Integer userId);

}
