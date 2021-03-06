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

    @Query(value = "select *from pms where name=?1",nativeQuery = true)
    UserInfo fingName(String name);
    @Query(value ="select *from pms WHERE id=?1",nativeQuery = true)
    UserInfo chooseID(Integer id);
//    @Modifying
//    @Transactional
//    @Query(value = "UPDATE pms SET gmtCreate = ?2 WHERE tel = ?1", nativeQuery = true)
//    void updategmtCreate(String tel, Date gmtCreate);




}
