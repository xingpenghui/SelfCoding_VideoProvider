package com.feri.selfcoding_videoprovider.provider;

import com.alibaba.druid.sql.PagerUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.feri.common.qo.QueryParam;
import com.feri.common.util.ResultUtil;
import com.feri.common.vo.PageVo;
import com.feri.common.vo.ResultVO;
import com.feri.dao.video.VideoCourseDao;
import com.feri.dao.video.VideoDao;
import com.feri.dao.video.VideoTypeDao;
import com.feri.dao.video.VideoTypePointDao;
import com.feri.domain.video.Video;
import com.feri.domain.video.Videocourse;
import com.feri.domain.video.Videotype;
import com.feri.domain.video.Videotypepoint;
import com.feri.service.video.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *@Author feri
 *@Date Created in 2019/1/21 14:36
 */
@Service("videoprovider")
public class VideoProvider implements VideoService {
    @Autowired
    private VideoDao videoDao;
    @Autowired
    private VideoTypeDao videoTypeDao;
    @Autowired
    private VideoTypePointDao videoTypePointDao;
    @Autowired
    private VideoCourseDao videoCourseDao;
    @Override
    public ResultVO queryType() {
        QueryWrapper<Videotype> queryWrapper=new QueryWrapper<>();
        queryWrapper.orderByAsc("id");
        return ResultUtil.execOK(videoTypeDao.selectList(queryWrapper));
    }

    @Override
    public ResultVO queryPoint(int tid) {
        QueryWrapper<Videotypepoint> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("tid",tid);
        queryWrapper.orderByAsc("id");
        return ResultUtil.execOK(videoTypePointDao.selectList(queryWrapper));
    }
    //新增课程
    @Override
    public ResultVO saveCourse(Videocourse videocourse) {
        videocourse.setCreatetime(LocalDateTime.now());
        return ResultUtil.exec(videoCourseDao.insert(videocourse),videocourse);
    }

    @Override
    public PageVo<Videocourse> queryByPage(QueryParam queryParam) {
        QueryWrapper<Videocourse> queryWrapper=new QueryWrapper();
        String type=queryParam.get("type");
        if(type!=null && type.length()>0){
            queryWrapper.eq("tid",type);
        }
        /*String point=queryParam.get("point");
        if(point!=null && point.length()>0){
            queryWrapper.in()
        }*/
        int p=Integer.parseInt(queryParam.get("page"));
        int l=Integer.parseInt(queryParam.get("limit"));
        Page<Videocourse> page=new Page<Videocourse>(p,l);
        return ResultUtil.execPage(p,l,videoCourseDao.selectCount(queryWrapper),
                videoCourseDao.selectPage(page,queryWrapper).getRecords());
    }

    @Override
    public ResultVO saveVideo(Video video) {
        video.setCreatetime(LocalDateTime.now());

        return ResultUtil.exec(videoDao.insert(video),video);
    }

    @Override
    public ResultVO queryByCourse(int i) {
        QueryWrapper<Video> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("tid",i);

        return ResultUtil.execOK(videoDao.selectList(queryWrapper));
    }

    @Override
    public ResultVO queryIndex() {
        List<Object> list=new ArrayList<>();

        //最新  12条
        QueryWrapper<Video> queryWrapperNew=new QueryWrapper<>();
        queryWrapperNew.orderByDesc("createtime");
        Page<Video> pageNew=new Page<>(1,12);
        list.add(videoDao.selectPage(pageNew,queryWrapperNew).getRecords());

        //播放多
        list.add(videoDao.queryPlay());
        //卖的多
        list.add(videoCourseDao.queryCourse());

        return ResultUtil.execOK(list);
    }
}
