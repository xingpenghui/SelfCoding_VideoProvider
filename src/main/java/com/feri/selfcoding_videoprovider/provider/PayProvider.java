package com.feri.selfcoding_videoprovider.provider;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.feri.common.qo.QueryParam;
import com.feri.common.token.LoginToken;
import com.feri.common.token.TokenUtil;
import com.feri.common.util.DateUtil;
import com.feri.common.util.ResultUtil;
import com.feri.common.vo.PageVo;
import com.feri.common.vo.ResultVO;
import com.feri.dao.pay.PayCodeDao;
import com.feri.dao.pay.UserOrderDao;
import com.feri.dao.pay.UserPayDao;
import com.feri.dao.pay.UserPayLogDao;
import com.feri.dao.user.UserShellDao;
import com.feri.dao.user.UserWalletDao;
import com.feri.dao.video.VideoCourseDao;
import com.feri.domain.pay.Paycode;
import com.feri.domain.pay.Userorder;
import com.feri.domain.pay.Userpay;
import com.feri.domain.pay.Userpaylog;
import com.feri.domain.user.Usershell;
import com.feri.domain.user.Userwallet;
import com.feri.domain.video.Videocourse;
import com.feri.service.pay.PayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
/**
 *@Author feri
 *@Date Created in 2019/1/21 15:09
 */
@Service("payprovider")
public class PayProvider implements PayService {
    @Autowired
    private PayCodeDao payCodeDao;
    @Autowired
    private UserShellDao userShellDao;
    @Autowired
    private UserWalletDao userWalletDao;
    @Autowired
    private UserPayDao userPayDao;
    @Autowired
    private UserOrderDao userOrderDao;
    @Autowired
    private UserPayLogDao userPayLogDao;
    @Autowired
    private VideoCourseDao videoCourseDao;
    @Override
    public ResultVO saveOrder(Userorder userorder) {
        //1、订单对象保存
        userOrderDao.insert(userorder);
        //查询剩余学贝
        Userwallet userwallet=userWalletDao.selectById(userorder.getUid());
        int xy=userorder.getShell()*userorder.getDiscount();
        if(userwallet.getTotalshell()>=xy){
            //剩余学贝可以支付
            Userpaylog userpaylog=new Userpaylog();
            userpaylog.setCreatetime(LocalDateTime.now());
            userpaylog.setUoid(userorder.getId());
            userpaylog.setShell(userorder.getShell()*userorder.getDiscount());
            userpaylog.setContent(userorder.getUid()+":购买了,"+userorder.getVcid()+":课程");
            //存储消费流水
            userPayLogDao.insert(userpaylog);
            userwallet.setTotalshell(userwallet.getTotalshell()-userpaylog.getShell());
            userwallet.setConsumeshell(userwallet.getConsumeshell()+userpaylog.getShell());
            //更改钱包变动
            userWalletDao.updateById(userwallet);
            userorder.setFlag(2);
            userOrderDao.updateById(userorder);
            return ResultUtil.execOK(null);
        }else {
            //需要充值
            return ResultUtil.execOK(userwallet.getTotalshell() - xy);
        }
    }

    @Override
    public ResultVO savePayCode(Paycode paycode) {
        paycode.setEndtime(LocalDateTime.now());
        paycode.setEndtime(DateUtil.parse(DateUtil.getTime(Calendar.HOUR_OF_DAY,2)));


        return ResultUtil.exec(payCodeDao.insert(paycode),paycode.getPayurl());
    }

    @Override
    public ResultVO updatePay(int id, int flag) {
        Userpay userpay=userPayDao.selectById(id);
        userpay.setFlag(flag);
        return ResultUtil.exec( userPayDao.updateById(userpay),null);
    }

    @Override
    public ResultVO queryPayLog(String s) {
        LoginToken loginToken=TokenUtil.parseToken(s);
        return ResultUtil.execOK(userPayLogDao.selectList(
                new QueryWrapper<Userpaylog>().eq("uid",loginToken.getId())));
    }

    @Override
    public ResultVO queryCourse(String s) {
        LoginToken loginToken=TokenUtil.parseToken(s);

        return ResultUtil.execOK(videoCourseDao.queryBuyCourse(loginToken.getId()));
    }

    @Override
    public ResultVO queryShell(String s) {
        QueryWrapper<Usershell> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("uid",TokenUtil.parseToken(s).getId());
        return ResultUtil.execOK(userShellDao.selectList(queryWrapper));
    }
}
