package com.mko.pms.repository;

import com.mko.pms.enetity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.Date;

public interface UserInfoRepository extends JpaRepository<UserInfo,Integer> {

    @Query(value="select *from pms WHERE tel=?1",nativeQuery = true)
    UserInfo findByPhone(String phone);

    @Query(value="select *from pms WHERE id=?1",nativeQuery = true)
    UserInfo findById(String id);

    @Transactional
    @Query(value = "select *from pms WHERE tel=?1 and password=?2 and state=1",nativeQuery = true)
    UserInfo userlogin(String tel,String password);

//    @Modifying
//    @Transactional
//    @Query(value = "UPDATE pms SET gmtCreate = ?2 WHERE tel = ?1", nativeQuery = true)
//    void updategmtCreate(String tel, Date gmtCreate);


    @Transactional
    @Query(value = "UPDATE pms SET password = ?1 WHERE id = ?2", nativeQuery = true)
    void updatePassword(String password, Integer userId);

}
