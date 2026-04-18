package com.compass.yuhengapi.util;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson2.JSON;
import com.compass.yuhengapi.common.lang.APIException;
import com.compass.yuhengapi.model.entities.ApiDatasource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


@Slf4j
public class PoolManager {

    private static final Lock lock = new ReentrantLock();

    //所有数据源的连接池存在map里
    static Map<String, DruidDataSource> map = new HashMap<>();

    public static DruidDataSource getPool(ApiDatasource ds) {
        if (map.containsKey(ds.getId())) {
            return map.get(ds.getId());
        } else {
            lock.lock();
            try {
                log.info(Thread.currentThread().getName() + "正在创建连接池");
                if (!map.containsKey(ds.getId())) {
                    DruidDataSource druidDataSource = new DruidDataSource();
                    druidDataSource.setUrl(ds.getUrl());
                    druidDataSource.setUsername(ds.getUsername());
                    druidDataSource.setPassword(ds.getPassword());
                    map.put(ds.getId(), druidDataSource);
                    log.info("创建Druid连接池成功：{}", JSON.toJSONString(ds));
                }
                return map.get(ds.getId());
            } catch (Exception e) {
                log.error(ExceptionUtils.getStackTrace(e));
                return null;
            } finally {
                lock.unlock();
            }
        }
    }

    public static DruidPooledConnection getPooledConnection(ApiDatasource ds) throws SQLException {
        DruidDataSource pool = PoolManager.getPool(ds);
        if (pool == null) {
            throw new APIException("获取连接池失败！");
        }
        return pool.getConnection();
    }

    public static void removePool(String datasourceId) {
        lock.lock();
        try {
            DruidDataSource pool = map.remove(datasourceId);
            if (pool != null) {
                pool.close();
                log.info("注销连接池成功：{}", datasourceId);
            }
        } catch (Exception e) {
            log.error("注销连接池失败：{}", datasourceId, e);
        } finally {
            lock.unlock();
        }
    }

}
